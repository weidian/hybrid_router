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
    String pageName = "hybrid_router_local_route_${_localRouteIndex++}";
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

/// Manage the native container
class NativeContainerManager extends StatefulWidget {
  /// 静态状态实例，这里表明是单例模型
  static NativeContainerManagerState state;

  /// 当前 manager 是否 state 有效
  static bool isInit() {
    return state != null;
  }

  static Future<bool> onBackPressed() {
    _checkState();
    return state.onBackPressed();
  }

  static void pushNamed(
      {@required String nativePageId,
      @required String pageName,
      Object args,
      bool isTab}) {
    _checkState();
    state.pushNamed(
        nativePageId: nativePageId,
        pageName: pageName,
        args: args,
        isTab: isTab);
  }

  static void push(NativeContainer containter) {
    _checkState();
    state.push(containter);
  }

  static void showNamed({@required String nativePageId}) {
    _checkState();
    state.showNamed(nativePageId: nativePageId);
  }

  static void show(NativeContainer container) {
    _checkState();
    state.show(container);
  }

  static Future<bool> removeNamed(
      {@required String nativePageId, dynamic result}) {
    _checkState();
    return state.removeNamed(nativePageId: nativePageId, result: result);
  }

  static Future<bool> remove(NativeContainer container) {
    _checkState();
    return state.remove(container);
  }

  /// flutter 请求在新的 native 容器中 open 一个 flutter 页面
  static Future<NativePageResult<T>> openFlutterNamedInNative<T extends Object>(
      {@required String nativePageId,
      @required String pageName,
      Object args,
      NativePageTransitionType type}) {
    type = type ?? NativePageTransitionType.DEFAULT;
    return HybridPlugin.singleton.openFlutterPage<T>(
        nativePageId: nativePageId,
        pageName: pageName,
        args: args,
        transitionType: type);
  }

  /// flutter 请求在新的 native 容器中 open 一个 flutter route
  static Future<NativePageResult<T>> openFlutterRouteInNative<T extends Object>(
      {@required String nativePageId,
      @required Route<T> route,
      NativePageTransitionType type}) {
    _checkState();
    type = type ?? NativePageTransitionType.DEFAULT;
    String pageName = state._addLocalRoute(route);
    return HybridPlugin.singleton.openFlutterPage<T>(
        nativePageId: nativePageId,
        pageName: pageName,
        args: {},
        transitionType: type);
  }

  /// flutter 请求 open 一个 native 页面
  static Future<NativePageResult<T>> openNativePage<T extends Object>(
      {@required String url,
      @required String nativePageId,
      Map args,
      NativePageTransitionType transitionType}) {
    assert(url != null);
    transitionType = transitionType ?? NativePageTransitionType.DEFAULT;
    return HybridPlugin.singleton.openNativePage(
        url: url,
        args: args,
        nativePageId: nativePageId,
        transitionType: transitionType);
  }

  static _checkState() {
    assert(
        state != null,
        "NativeContainerManager's state is null\n"
        "Please add NativeContainerManager to App");
  }

  final WidgetBuilder backgroundBuilder;

  /// 路由表
  final Map<String, HybridWidgetBuilder> routes;

  /// 路由匹配失败时候的 builder
  final HybridRouteFactory unknownRouteBuilder;

  /// 默认的 push type
  final HybridPushType defaultPushType;

  /// native container 变化观察者
  final List<NativeContainerObserver> containerObserver;

  /// native container 内的 hybrid navigator 内页面变化观察者
  final List<HybridNavigatorObserver> pageObserver;

  const NativeContainerManager(
      {Key key,
      @required this.routes,
      this.backgroundBuilder,
      this.unknownRouteBuilder,
      this.defaultPushType,
      this.containerObserver = const [],
      this.pageObserver = const []})
      : assert(defaultPushType != null),
        assert(routes != null),
        assert(containerObserver != null),
        assert(pageObserver != null),
        super(key: key);

  @override
  NativeContainerManagerState createState() => NativeContainerManagerState();
}

class NativeContainerManagerState extends State<NativeContainerManager> {
  /// The overlay this navigator uses for its visual presentation.
  OverlayState get overlay => _overlayKey.currentState;

  /// native android 按了返回键
  Future<bool> onBackPressed() async {
    // get last navigate container
    NativeContainer container =
        _containerHistory.isEmpty ? null : _containerHistory.last;
    if (await container.emmiter.emmit()) {
      return true;
    }
    try {
      return container.pop();
    } catch (e) {
      FlutterError.onError(e);
    }
    return false;
  }

  /// native request to push a flutter page
  void pushNamed(
      {@required String nativePageId,
      @required String pageName,
      Object args,
      bool isTab}) {
    assert(nativePageId != null);
    assert(pageName != null);

    Route<dynamic> initRoute = _matchLocalRoute(pageName);
    push(NativeContainer(
      nativePageId: nativePageId,
      initRouteName: pageName,
      initRoute: initRoute,
      args: args,
      isTab: isTab,
      generateBuilder: _routeGenerator,
      unknownBuilder: _unknownRouteGenerator,
    ));
  }

  /// push a native container to manager
  void push(NativeContainer container) {
    assert(container != null, "Push container should not be null");
    assert(container.nativePageId != null,
        "Container's nativePageId should not be null");
    NativeContainer lastContainer =
        _containerHistory.isEmpty ? null : _containerHistory.last;
    _containerHistory.add(container);
    OverlayEntry overlayEntry = OverlayEntry(builder: (context) => container);
    container._overlayEntry = overlayEntry;
    container._manager = this;
    overlay.insert(overlayEntry, above: lastContainer?._overlayEntry);
    _didPush(container, lastContainer);
  }

  /// move the container to top by name
  void showNamed({@required String nativePageId}) {
    assert(nativePageId != null, "NativePageId should not be null");
    NativeContainer c = _containerHistory.firstWhere((c) {
      return c.nativePageId == nativePageId;
    });
    show(c);
  }

  /// move the container to top
  void show(NativeContainer container) {
    if (container != null && container != _containerHistory.last) {
      overlay
          .rearrange([container._overlayEntry], below: container._overlayEntry);
      NativeContainer topContainer =
          _containerHistory.isEmpty ? null : _containerHistory.last;
      _containerHistory.remove(container);
      _containerHistory.add(container);
      _didShow(container, topContainer);
    }
  }

  /// remove a native container by native page Id
  Future<bool> removeNamed({@required String nativePageId, dynamic result}) {
    NativeContainer c = _containerHistory.firstWhere((c) {
      return c.nativePageId == nativePageId;
    });
    c._result = result;
    return remove(c);
  }

  /// remove a native container
  Future<bool> remove(NativeContainer container) async {
    if (container == null) {
      return false;
    }

    // 通知 native 组件，我将要 pop
    await HybridPlugin.singleton.onNativeRouteEvent(
        event: NativeRouteEvent.beforeDestroy,
        nativePageId: container.nativePageId,
        result: container._result);

    container._overlayEntry.remove();
    int index = _containerHistory.indexOf(container);
    NativeContainer preContainer;
    if (index > 0) {
      preContainer = _containerHistory[index];
    }
    _containerHistory.removeAt(index);
    _didRemove(container, preContainer);
    return true;
  }

  @override
  void initState() {
    super.initState();
    NativeContainerManager.state = this;
    _startInitRoute();
  }

  @override
  Widget build(BuildContext context) {
    List<OverlayEntry> initEntry;
    if (widget.backgroundBuilder != null) {
      initEntry = [OverlayEntry(builder: widget.backgroundBuilder)];
    } else {
      initEntry = [];
    }
    return Overlay(
      key: _overlayKey,
      initialEntries: initEntry,
    );
  }

  final GlobalKey<OverlayState> _overlayKey = GlobalKey<OverlayState>();

  final List<NativeContainer> _containerHistory = [];

  /// page name 对应的 route
  final Map<String, Route<dynamic>> _localRoutePending = {};

  /// 别问我这个数字为啥是 11037
  int _localRouteIndex = 11037;

  void _didPush(NativeContainer container, NativeContainer preContainer) {
    HybridPlugin.singleton.onNativeRouteEvent(
        event: NativeRouteEvent.onCreate, nativePageId: container.nativePageId);
    widget.containerObserver?.forEach((o) {
      try {
        o.didPush(container, preContainer);
      } catch (e) {
        FlutterError.onError(e);
      }
    });
    if (preContainer != null) {
      HybridPlugin.singleton.onNativeRouteEvent(
          event: NativeRouteEvent.onPause,
          nativePageId: preContainer.nativePageId);
    }
  }

  void _didShow(NativeContainer container, NativeContainer topContainer) {
    HybridPlugin.singleton.onNativeRouteEvent(
        event: NativeRouteEvent.onResume, nativePageId: container.nativePageId);
    if (topContainer != null && topContainer != container) {
      HybridPlugin.singleton.onNativeRouteEvent(
          event: NativeRouteEvent.onPause,
          nativePageId: topContainer.nativePageId);
    }
    widget.containerObserver?.forEach((o) {
      try {
        o.didShow(container, topContainer);
      } catch (e) {
        FlutterError.onError(e);
      }
    });
  }

  void _didRemove(NativeContainer container, NativeContainer preContainer) {
    HybridPlugin.singleton.onNativeRouteEvent(
        event: NativeRouteEvent.onDestroy,
        nativePageId: container.nativePageId);
    if (preContainer != null) {
      HybridPlugin.singleton.onNativeRouteEvent(
          event: NativeRouteEvent.onResume,
          nativePageId: container.nativePageId);
    }
    _repairFrameSchedule();
  }

  void _repairFrameSchedule() {
    // need to repair onDrawFrame callback
    Future.delayed(Duration(microseconds: 0), () {
      window.onBeginFrame(null);
      window.onDrawFrame();
    });
  }

  String _addLocalRoute(Route<dynamic> route) {
    String pageName = "hybrid_router_local_route_${_localRouteIndex++}";
    _localRoutePending[pageName] = route;
    return pageName;
  }

  Route<dynamic> _matchLocalRoute(String pageName) {
    return _localRoutePending.remove(pageName);
  }

  Route<T> _routeGenerator<T extends Object>(RouteSettings settings) {
    HybridWidgetBuilder builder = widget.routes[settings.name];
    if (builder != null) {
      return MaterialPageRoute<T>(
          builder: (context) {
            return builder(context, settings.arguments);
          },
          settings: settings);
    }
    return null;
  }

  Route<T> _unknownRouteGenerator<T extends Object>(RouteSettings settings) {
    if (widget.unknownRouteBuilder != null) {
      return widget.unknownRouteBuilder(settings);
    }
    return null;
  }

  /// 这里是异步的，所以可以直接用 push
  void _startInitRoute() async {
    Map initRoute = await HybridPlugin.singleton.getInitRoute();
    assert(initRoute != null);
    String pageName = initRoute["pageName"];
    assert(pageName != null);
    Object args = initRoute["args"];
    String nativePageId = initRoute["nativePageId"];
    bool isTab = initRoute["isTab"];
    pushNamed(
        nativePageId: nativePageId,
        pageName: pageName,
        args: args,
        isTab: isTab);
  }
}

/// native container
class NativeContainer extends StatefulWidget {
  final String nativePageId;

  final String initRouteName;

  final Route<dynamic> initRoute;

  final Object args;

  final bool isTab;

  final HybridRouteFactory generateBuilder;

  final HybridRouteFactory unknownBuilder;

  final BackPressedEmmiter emmiter = BackPressedEmmiter();

  final GlobalKey<NavigatorState> navKey = GlobalKey();

  final List<HybridNavigatorObserver> observers;

  NativeContainerManagerState _manager;

  OverlayEntry _overlayEntry;

  dynamic _result;

  /// Pop a flutter router in native container
  bool pop<T extends Object>({T result}) {
    assert(
        navKey.currentState != null, "The key of Navigator return null state");
    return navKey.currentState.pop(result);
  }

  NativeContainer(
      {Key key,
      @required this.nativePageId,
      this.initRouteName,
      this.initRoute,
      this.args,
      this.observers,
      this.isTab,
      @required this.generateBuilder,
      @required this.unknownBuilder})
      : assert(nativePageId != null),
        assert(generateBuilder != null),
        assert(unknownBuilder != null),
        super(key: key) {
    if (initRoute is HybridPageRoute) {
      // 强制改为 flutter 的打开方式，防止死循环
      (initRoute as HybridPageRoute).pushType = HybridPushType.Flutter;
    }
  }

  @override
  NativeContainerState createState() => NativeContainerState();
}

class NativeContainerState extends State<NativeContainer>
    with NavigatorObserver {
  final List<Route<dynamic>> _history = [];

  @override
  Widget build(BuildContext context) {
    return HybridNavigator(
      key: widget.navKey,
      nativePageId: widget.nativePageId,
      initialRoute: widget.initRouteName,
      initRoute: widget.initRoute,
      initRouteArgs: widget.args,
      isTab: widget.isTab,
      generateBuilder: widget.generateBuilder,
      unknownBuilder: widget.unknownBuilder,
      observers: [this],
    );
  }

  /// 分发子页面的生命周期
  @override
  void didPush(Route<dynamic> route, Route<dynamic> previousRoute) {
    super.didPush(route, previousRoute);
    _history.add(route);

    if (previousRoute != null) {
      HybridPlugin.singleton.onFlutterRouteEvent(
          nativePageId: widget.nativePageId,
          event: FlutterRouteEvent.onPause,
          name: previousRoute.settings.name);
    }

    HybridPlugin.singleton.onFlutterRouteEvent(
        nativePageId: widget.nativePageId,
        event: FlutterRouteEvent.onPush,
        name: route.settings.name);
    widget.observers?.forEach((o) {
      try {
        o.didPush(route, previousRoute);
      } catch (e) {
        FlutterError.onError(e);
      }
    });
  }

  @override
  void didPop(Route<dynamic> route, Route<dynamic> previousRoute) {
    _history.removeLast();
    HybridPlugin.singleton.onFlutterRouteEvent(
        nativePageId: widget.nativePageId,
        event: FlutterRouteEvent.onPop,
        name: route.settings.name);
    widget.observers?.forEach((o) {
      try {
        o.didPop(route, previousRoute);
      } catch (e) {
        FlutterError.onError(e);
      }
    });
    if (previousRoute != null) {
      HybridPlugin.singleton.onFlutterRouteEvent(
          nativePageId: widget.nativePageId,
          event: FlutterRouteEvent.onResume,
          name: previousRoute.settings.name);
    }
    super.didPop(route, previousRoute);
  }

  @override
  void didRemove(Route<dynamic> route, Route<dynamic> previousRoute) {
    _history.remove(route);
    HybridPlugin.singleton.onFlutterRouteEvent(
        nativePageId: widget.nativePageId,
        event: FlutterRouteEvent.onRemove,
        name: route.settings.name);
    widget.observers?.forEach((o) {
      try {
        o.didRemove(route, previousRoute);
      } catch (e) {
        FlutterError.onError(e);
      }
    });

    if (_history.isNotEmpty && _history.last == previousRoute) {
      HybridPlugin.singleton.onFlutterRouteEvent(
          nativePageId: widget.nativePageId,
          event: FlutterRouteEvent.onResume,
          name: previousRoute.settings.name);
    }
    super.didRemove(route, previousRoute);
  }

  @override
  void didReplace({Route<dynamic> newRoute, Route<dynamic> oldRoute}) {
    super.didReplace(newRoute: newRoute, oldRoute: oldRoute);
    int index = _history.indexOf(oldRoute);
    bool isTop = index == _history.length - 1;
    assert(index >= 0);
    _history[index] = newRoute;
    HybridPlugin.singleton.onFlutterRouteEvent(
        nativePageId: widget.nativePageId,
        event: FlutterRouteEvent.onReplace,
        name: newRoute.settings.name,
        extra: {"oldRouteName": oldRoute.settings.name, "isTop": isTop});
    widget.observers?.forEach((o) {
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
    widget.observers?.forEach((o) {
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
    widget.observers?.forEach((o) {
      o.didStopUserGesture();
    });
  }
}
