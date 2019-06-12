// MIT License
// -----------

// Copyright (c) 2019 WeiDian Group
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without
// restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following
// conditions:

// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
import 'dart:async';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:hybrid_router/src/back_pressed.dart';

import 'hybrid_plugin.dart';
import 'model.dart';
import 'navigator.dart';
import 'observer.dart';

/// 混合栈管理类
class HybridRouterManager extends NavigatorObserver {
  /// 判断是够初始化了
  static bool isInit() {
    return _instance != null;
  }

  /// 全局单例
  static HybridRouterManager get singleton {
    if (_instance == null) {
      throw FlutterError('Must init HybridRouterManager first');
    }
    return _instance;
  }

  static HybridRouterManager _instance;

  /// 路由表
  final Map<String, HybridWidgetBuilder> routes;

  /// 路由观察者
  final List<HybridNavigatorObserver> observers;

  /// 路由匹配失败时候的 builder
  HybridRouteFactory unknownRouteBuilder;

  /// 默认的 push type
  HybridPushType defaultPushType;

  /// init hybrid stack manager
  /// [rootNavStateKey] can get from [MaterialApp]
  HybridRouterManager.init(
      {this.routes = const <String, HybridWidgetBuilder>{},
      this.defaultPushType = HybridPushType.Native,
      this.observers = const <HybridNavigatorObserver>[],
      this.unknownRouteBuilder}) {
    assert(defaultPushType != null);
    _instance = this;
    _checkObservable();
  }

  /// 获取初始路由
  void startInitRoute() async {
    _checkObservable();
    Map initRoute = await HybridPlugin.singleton.getInitRoute();
    assert(initRoute != null);
    String pageName = initRoute["pageName"];
    assert(pageName != null);
    Object args = initRoute["args"];
    String nativePageId = initRoute["nativePageId"];
    push(pageName: pageName, args: args, nativePageId: nativePageId);
  }

  /// 获取 context 所在页面的 native page id
  String getNativePageIdByContext(BuildContext context) {
    return getNativePageIdByRoute(ModalRoute.of(context));
  }

  /// 通过此 route 获取到对应的 native page id
  /// 此 route 可以是混合栈容器内的 route，也可以是混合栈本身的 route
  String getNativePageIdByRoute(Route<dynamic> route) {
    if (route == null) return null;
    for (Route<dynamic> navRoute in _navigatorHistory) {
      if (navRoute == route) {
        if (navRoute is _HybridKeyRoute) {
          return navRoute.nativePageId;
        }
        return null;
      } else if (navRoute is _HybridKeyRoute &&
          navRoute.hybridNavigator.key is GlobalKey<HybridNavigatorState>) {
        /// find route in container
        GlobalKey<HybridNavigatorState> key = navRoute.hybridNavigator.key;
        if (key.currentState.containsRoute(route)) {
          return navRoute.nativePageId;
        }
      }
    }
    return null;
  }

  /// 获取 native 容器包含的 route 列表
  List<Route<dynamic>> getRoutesInNativePage(String nativePageId) {
    List<Route<dynamic>> ret = [];
    if (nativePageId?.isNotEmpty != true) return ret;
    for (Route<dynamic> navRoute in _navigatorHistory) {
      if (navRoute is _HybridKeyRoute &&
          navRoute.nativePageId == nativePageId &&
          navRoute.hybridNavigator.key is GlobalKey<HybridNavigatorState>) {
        GlobalKey<HybridNavigatorState> key = navRoute.hybridNavigator.key;
        ret.addAll(key.currentState.routeHistoryCopy);
        break;
      }
    }
    return ret;
  }

  /// native android 按了返回键
  Future<bool> onBackPressed() async {
    _checkObservable();
    // 获取 root navigator 最后一个 route
    Route<dynamic> route =
        _navigatorHistory.isEmpty ? null : _navigatorHistory.last;
    // 如果此 route 是 _HybridKeyRoute，表示返回键需要应用到内部
    if (route is _HybridKeyRoute) {
      // 这里把返回键处理拦截到儿子
      if (await route.emmiter.emmit()) {
        return true;
      }
      Key key = route.hybridNavigator?.key;

      /// 通过 key 来获取 state
      if (key != null && key is GlobalKey<HybridNavigatorState>) {
        key.currentState.pop();
        return true;
      }
    }
    // 否则调用 root navigator 的 pop
    if (navigator != null && navigator.canPop()) {
      return navigator.pop();
    }
    // 如果 pop 失败，返回 false 给 native 容器，让 native 容器决定是否结束
    return false;
  }

  /// receive native page finish
  void onNativePageFinished(String nativePageId) {
    _checkObservable();
    if (nativePageId == null) {
      return;
    }

    /// find key route
    int findIndex = _navigatorHistory.length - 1;
    while (findIndex >= 0) {
      Route<dynamic> route = _navigatorHistory[findIndex];
      if (route is _HybridKeyRoute<dynamic> &&
          route.nativePageId == nativePageId) {
        break;
      }
      findIndex--;
    }
    if (findIndex < 0) {
      /// native page not found
      return;
    }

    /// remove the route
    navigator.removeRoute(_navigatorHistory[findIndex]);
  }

  /// native 请求 push 一个 flutter 页面
  void push(
      {@required String nativePageId, @required String pageName, Object args}) {
    assert(navigator != null);
    assert(pageName != null);
    _checkObservable();

    Route<dynamic> initRoute = _matchLocalRoute(pageName);
    if (initRoute is HybridPageRoute) {
      // 强制改为 flutter 的打开方式，防止死循环
      initRoute.pushType = HybridPushType.Flutter;
    }

    _HybridKeyRoute pushRoute = _HybridKeyRoute(
      nativePageId: nativePageId,

      // key 必须与 widget 一一对应
      hybridNavigator: HybridNavigator(
        key: GlobalKey<HybridNavigatorState>(),
        nativePageId: nativePageId,
        initialRoute: pageName,
        initRouteArgs: args,
        initRoute: initRoute,
        observers: [_ChildNavigatorObserver(this, nativePageId)],
        generateBuilder: _subRouteGenerator,
        unknownBuilder: _subUnknownRouteGenerator,
      ),
    );
    navigator.push(pushRoute);
  }

  /// flutter 请求在新的 native 容器中 open 一个 flutter 页面
  Future<NativePageResult<T>> openFlutterNamedInNative<T extends Object>(
      {@required String nativePageId,
      @required String pageName,
      Object args,
      NativePageTransitionType type}) {
    _checkObservable();
    type = type ?? NativePageTransitionType.DEFAULT;
    return HybridPlugin.singleton.openFlutterPage<T>(
        nativePageId: nativePageId,
        pageName: pageName,
        args: args,
        transitionType: type);
  }

  /// flutter 请求在新的 native 容器中 open 一个 flutter route
  Future<NativePageResult<T>> openFlutterRouteInNative<T extends Object>(
      {@required String nativePageId,
      @required Route<T> route,
      NativePageTransitionType type}) {
    type = type ?? NativePageTransitionType.DEFAULT;
    String pageName = _addLocalRoute(route);
    return HybridPlugin.singleton.openFlutterPage<T>(
        nativePageId: nativePageId,
        pageName: pageName,
        args: {},
        transitionType: type);
  }

  /// flutter 请求 open 一个 native 页面
  Future<NativePageResult<T>> openNativePage<T extends Object>(
      {@required String url,
      @required String nativePageId,
      Map args,
      NativePageTransitionType transitionType}) {
    _checkObservable();
    assert(url != null);
    transitionType = transitionType ?? NativePageTransitionType.DEFAULT;
    return HybridPlugin.singleton.openNativePage(
        url: url,
        args: args,
        nativePageId: nativePageId,
        transitionType: transitionType);
  }

  /// 结束一个 flutter 容器
  /// [removeRoute] 结束的 flutter 容器中包含的页面
  Future<bool> closeKeyPage(
      String nativePageId, dynamic result, Route<dynamic> removeRoute) async {
    _checkObservable();

    // find current route in context
    // before pop page
    await HybridPlugin.singleton.onNativeRouteEvent(
        event: NativeRouteEvent.beforeDestroy,
        nativePageId: nativePageId,
        result: result);

    Route<dynamic> route = _findRouteByNativePageId(nativePageId);

    /// pop page
    if (_navigatorHistory.isNotEmpty && route == _navigatorHistory.last) {
      navigator.pop(result);
      observers?.forEach((o) {
        o.didPop(removeRoute, null);
      });
    } else if (route.navigator == navigator) {
      if (route is _HybridKeyRoute) {
        route.currentResult = result;
      }
      navigator.removeRoute(route);
      observers?.forEach((o) {
        o.didRemove(removeRoute, null);
      });
    }
    return true;
  }

  /// 这里是监听了 root navigator 的 pop 事件
  @override
  void didPop(Route route, Route previousRoute) {
    // route 所在 native 容器的前一个容器
    String preNativePageId = _findNativePageIdBelow(route);
    _navigatorHistory.remove(route);
    if (route is _HybridKeyRoute<dynamic>) {
      // native 容器结束的时候，从 route 中获取页面返回的结果
      HybridPlugin.singleton.onNativeRouteEvent(
          event: NativeRouteEvent.onDestroy,
          nativePageId: route.nativePageId,
          result: route.currentResult);
      // 只有 pop native container 的时候，下一个 native container 才会 resume
      if (preNativePageId != null) {
        HybridPlugin.singleton.onNativeRouteEvent(
            event: NativeRouteEvent.onResume, nativePageId: preNativePageId);
      }
    }
    _repairFrameSchedule();
  }

  @override
  void didPush(Route route, Route previousRoute) {
    _navigatorHistory.add(route);
    if (route is _HybridKeyRoute<dynamic>) {
      String preNativePageId = _findNativePageIdBelow(route);
      // 打开的新的 native 容器页面，需要通知 create 事件
      HybridPlugin.singleton.onNativeRouteEvent(
          event: NativeRouteEvent.onCreate, nativePageId: route.nativePageId);
      // 打开的新页面是 native container 的时候，前一个 native container 处于暂停状态
      if (preNativePageId != null) {
        HybridPlugin.singleton.onNativeRouteEvent(
            event: NativeRouteEvent.onPause, nativePageId: preNativePageId);
      }
    }
  }

  @override
  void didRemove(Route route, Route previousRoute) {
    // 所在 native 容器的前一个 native 容器
    String preNativePageId = _findNativePageIdBelow(route);
    _navigatorHistory.remove(route);
    if (route is _HybridKeyRoute<dynamic>) {
      // native 容器结束的时候，从 route 中获取页面返回的结果
      HybridPlugin.singleton.onNativeRouteEvent(
          event: NativeRouteEvent.onDestroy, nativePageId: route.nativePageId);
      // 只有移除前一个 native container，下一个 native container 才会显示
      if (preNativePageId != null) {
        HybridPlugin.singleton.onNativeRouteEvent(
            event: NativeRouteEvent.onResume, nativePageId: preNativePageId);
      }
    }
  }

  @override
  void didReplace({Route newRoute, Route oldRoute}) {
    int index = _navigatorHistory.indexOf(oldRoute);
    if (index >= 0) {
      _navigatorHistory.removeAt(index);
      _navigatorHistory.insert(index, newRoute);
    }
  }

  List<Route<dynamic>> _navigatorHistory = [];

  Map<String, Route<dynamic>> _localRoutePending = {};

  int _localRouteIndex = 11037;

  String _addLocalRoute(Route<dynamic> route) {
    String pageName = "\hybrid_router_local_route_${_localRouteIndex++}";
    _localRoutePending[pageName] = route;
    return pageName;
  }

  Route<dynamic> _matchLocalRoute(String pageName) {
    return _localRoutePending.remove(pageName);
  }

  void _checkObservable() {
    if (navigator != null && !navigator.widget.observers.contains(this)) {
      throw FlutterError('Please add observable in the root nativator');
    }
  }

  void _repairFrameSchedule() {
    // need to repair onDrawFrame callback
    Future.delayed(Duration(microseconds: 0), () {
      window.onBeginFrame(null);
      window.onDrawFrame();
    });
  }

  Route<T> _subRouteGenerator<T extends Object>(RouteSettings settings) {
    HybridWidgetBuilder builder = routes[settings.name];
    if (builder != null) {
      return MaterialPageRoute<T>(
          builder: (context) {
            return builder(context, settings.arguments);
          },
          settings: settings);
    }
    return null;
  }

  Route<T> _subUnknownRouteGenerator<T>(RouteSettings settings) {
    if (unknownRouteBuilder != null) {
      return unknownRouteBuilder(settings);
    }
    return null;
  }

  /// 查找 <= 当前 route 的 key route
  /// [count] 表示向下找几个 key route
  String _findNativePageIdBelow(Route<dynamic> route, {int count = 1}) {
    _HybridKeyRoute<dynamic> keyRoute = _findKeyRouteBelow(route, count: count);
    if (keyRoute == null) {
      return null;
    }
    return keyRoute.nativePageId;
  }

  Route<dynamic> _findRouteByNativePageId(String nativePageId) {
    for (Route<dynamic> route in _navigatorHistory) {
      if (route is _HybridKeyRoute && route.nativePageId == nativePageId) {
        return route;
      }
    }
    return null;
  }

  _HybridKeyRoute<dynamic> _findKeyRouteBelow(Route<dynamic> route,
      {int count = 1}) {
    assert(count >= 0);
    if (route == null) {
      return null;
    }
    int index = _navigatorHistory.indexOf(route);
    if (index < 0) {
      return null;
    }
    while (index >= 0) {
      Route<dynamic> ret = _navigatorHistory[index];
      if (ret is _HybridKeyRoute && count-- == 0) {
        return ret;
      }
      index--;
    }
    return null;
  }
}

/// 带有 native page id 的路由
class _HybridKeyRoute<T> extends MaterialPageRoute<T> {
  /// 绑定的 native page id
  final String nativePageId;

  final HybridNavigator hybridNavigator;

  final BackPressedEmmiter emmiter = BackPressedEmmiter();

  _HybridKeyRoute(
      {@required this.nativePageId,
      @required this.hybridNavigator,
      RouteSettings settings = const RouteSettings()})
      : super(
            builder: (context) {
              return hybridNavigator;
            },
            settings: settings);

  @override
  Duration get transitionDuration => nativePageId?.isNotEmpty == true
      ? Duration(milliseconds: 0)
      : super.transitionDuration;

  @override
  T get currentResult => _currentResult;

  /// 保存结果，方便返回数据给 native 端
  set currentResult(result) {
    _currentResult = result;
  }

  @override
  bool get hasScopedWillPopCallback {
    return true;
  }

  @override
  Widget buildPage(BuildContext context, Animation<double> animation,
      Animation<double> secondaryAnimation) {
    Widget child = super.buildPage(context, animation, secondaryAnimation);
    // 加上 back pressed 处理
    child = BackPressed(
      child: child,
      emmiter: emmiter,
    );
    return child;
  }

  @override
  bool didPop([T result]) {
    if (result != null) {
      _currentResult = result;
    }

    // 这里修复下 duration 为 0 的时候，TransitionRoute 中 _handleStatusChanged
    // 异常的问题
    if (overlayEntries.isNotEmpty) overlayEntries.first.opaque = false;
    return super.didPop(result);
  }

  dynamic _currentResult;
}

/// 二级 Navigator 监听，用于监听 native 容器中的那个 navigator 栈
class _ChildNavigatorObserver extends NavigatorObserver {
  final HybridRouterManager _manager;
  final String _nativePageId;
  final List<Route<dynamic>> _history = [];

  _ChildNavigatorObserver(this._manager, this._nativePageId);

  @override
  void didPop(Route<dynamic> route, Route<dynamic> previousRoute) {
    super.didPop(route, previousRoute);
    _history.removeLast();
    HybridPlugin.singleton.onFlutterRouteEvent(
        nativePageId: _nativePageId,
        event: FlutterRouteEvent.onPop,
        name: route.settings.name);
    _manager.observers?.forEach((o) {
      try {
        o.didPop(route, previousRoute);
      } catch (e) {
        FlutterError.onError(e);
      }
    });
    if (previousRoute != null) {
      HybridPlugin.singleton.onFlutterRouteEvent(
          nativePageId: _nativePageId,
          event: FlutterRouteEvent.onResume,
          name: previousRoute.settings.name);
    }
  }

  @override
  void didPush(Route<dynamic> route, Route<dynamic> previousRoute) {
    super.didPush(route, previousRoute);
    _history.add(route);

    if (previousRoute != null) {
      HybridPlugin.singleton.onFlutterRouteEvent(
          nativePageId: _nativePageId,
          event: FlutterRouteEvent.onPause,
          name: previousRoute.settings.name);
    }

    HybridPlugin.singleton.onFlutterRouteEvent(
        nativePageId: _nativePageId,
        event: FlutterRouteEvent.onPush,
        name: route.settings.name);
    _manager.observers?.forEach((o) {
      try {
        o.didPush(route, previousRoute);
      } catch (e) {
        FlutterError.onError(e);
      }
    });
  }

  @override
  void didRemove(Route<dynamic> route, Route<dynamic> previousRoute) {
    super.didRemove(route, previousRoute);
    _history.remove(route);
    HybridPlugin.singleton.onFlutterRouteEvent(
        nativePageId: _nativePageId,
        event: FlutterRouteEvent.onRemove,
        name: route.settings.name);
    _manager.observers?.forEach((o) {
      try {
        o.didRemove(route, previousRoute);
      } catch (e) {
        FlutterError.onError(e);
      }
    });

    if (_history.isNotEmpty && _history.last == previousRoute) {
      HybridPlugin.singleton.onFlutterRouteEvent(
          nativePageId: _nativePageId,
          event: FlutterRouteEvent.onResume,
          name: previousRoute.settings.name);
    }
  }

  @override
  void didReplace({Route<dynamic> newRoute, Route<dynamic> oldRoute}) {
    super.didReplace(newRoute: newRoute, oldRoute: oldRoute);
    int index = _history.indexOf(oldRoute);
    bool isTop = index == _history.length - 1;
    assert(index >= 0);
    _history[index] = newRoute;
    HybridPlugin.singleton.onFlutterRouteEvent(
        nativePageId: _nativePageId,
        event: FlutterRouteEvent.onReplace,
        name: newRoute.settings.name,
        extra: {"oldRouteName": oldRoute.settings.name, "isTop": isTop});
    _manager.observers?.forEach((o) {
      try {
        o.didReplace(newRoute: newRoute, oldRoute: oldRoute);
      } catch (e) {
        FlutterError.onError(e);
      }
    });
  }

  @override
  void didStartUserGesture(Route<dynamic> route, Route<dynamic> previousRoute) {
    super.didStartUserGesture(route, previousRoute);
    _manager.observers?.forEach((o) {
      try {
        o.didStartUserGesture(route, previousRoute);
      } catch (e) {
        FlutterError.onError(e);
      }
    });
  }

  @override
  void didStopUserGesture() {
    super.didStopUserGesture();
    _manager.observers?.forEach((o) {
      o.didStopUserGesture();
    });
  }
}
