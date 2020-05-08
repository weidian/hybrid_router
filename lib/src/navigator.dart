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

///
/// ┏┛ ┻━━━━━┛ ┻┓
/// ┃　　　　　　 ┃
/// ┃　　　━　　　┃
/// ┃　┳┛　  ┗┳　┃
/// ┃　　　　　　 ┃
/// ┃　　　┻　　　┃
/// ┃　　　　　　 ┃
/// ┗━┓　　　┏━━━┛
/// * ┃　　　┃   神兽保佑
/// * ┃　　　┃   代码无BUG！
/// * ┃　　　┗━━━━━━━━━┓
/// * ┃　　　　　　　    ┣┓
/// * ┃　　　　         ┏┛
/// * ┗━┓ ┓ ┏━━━┳ ┓ ┏━┛
/// * * ┃ ┫ ┫   ┃ ┫ ┫
/// * * ┗━┻━┛   ┗━┻━┛
/// 微店路由导航
/// @author qigengxin
/// @since 2019-02-26 14:29
///
import 'package:flutter/material.dart';

import 'manager.dart';
import 'model.dart';
import 'observer.dart';

//v1.17适配 https://flutter.dev/docs/release/breaking-changes/route-navigator-refactoring
class HybridNavigator extends Navigator {
  /// 获取页面默认打开方式
  static HybridPushType get defaultPushType {
    if (NativeContainerManager.isInit()) {
      return NativeContainerManager.state.widget.defaultPushType;
    }
    return HybridPushType.Native;
  }

  static NativeContainerManager init(
      {Key key,
      @required Map<String, HybridWidgetBuilder> routes,
      WidgetBuilder backgroundBuilder,
      HybridPushType defaultPushType,
      List<NativeContainerObserver> containerObserver = const [],
      List<HybridNavigatorObserver> pageObserver = const [],
      HybridRouteFactory unknownRouteBuilder}) {
    return NativeContainerManager(
      key: key,
      routes: routes,
      defaultPushType: defaultPushType,
      containerObserver: containerObserver,
      pageObserver: pageObserver,
      backgroundBuilder: backgroundBuilder,
      unknownRouteBuilder: unknownRouteBuilder,
    );
  }

  // 根据 route 获取到 nativePageId
  static String getNativePageIdByRoute(Route<dynamic> route) {
    if (route?.navigator is HybridNavigatorState) {
      return (route.navigator as HybridNavigatorState).widget?.nativePageId;
    }
    return null;
  }

  /// 通过此 context 获取到对应的 native page id
  static String getNativePageIdByContext(BuildContext context) {
    return NativeContainerManager.getNativePageIdByContext(context);
  }

  /// 获取 native 容器包含的 route 列表
  static List<Route<dynamic>> getRoutesInNativePage(String nativePageId) {
    return NativeContainerManager.getRoutesInNativePage(nativePageId);
  }

  /// 获取 HybridNavigatorState
  static HybridNavigatorState of(BuildContext context, {bool nullOk = false}) {
    final HybridNavigatorState state =
        context.ancestorStateOfType(TypeMatcher<HybridNavigatorState>());
    assert(() {
      if (state == null && !nullOk) {
        throw FlutterError(
            'Navigator operation requested with a context that does not include a HybridNavigator.\n'
            'The context used to push or pop routes from the HybridNavigator must be that of a '
            'widget that is a descendant of a HybridNavigator widget.');
      }
      return true;
    }());
    return state;
  }

  /// generate page builder
  final HybridRouteFactory generateBuilder;

  /// 404 page builder
  final HybridRouteFactory unknownBuilder;

  /// current bind native page Id
  final String nativePageId;

  /// 初始路由的参数
  final Object initRouteArgs;

  /// 标示tab页
  final bool isTab;

  /// 初始路由
  final Route<dynamic> initRoute;

  HybridNavigator({
    Key key,
    @required this.nativePageId,
    String initialRoute,
    Object initRouteArgs,
    bool isTab = false,
    List<NavigatorObserver> observers,
    Route<dynamic> initRoute,
    this.generateBuilder,
    this.unknownBuilder,
  })  : assert(initialRoute != null || initRoute != null),
        assert(nativePageId != null),
        initRouteArgs = initRouteArgs,
        initRoute = initRoute,
        this.isTab = isTab ?? false,
        super(
          key: key,
          initialRoute: initialRoute,
          observers: observers ?? [],
          onGenerateRoute:
              _builderMapFactory(generateBuilder, initRouteArgs, initRoute),
          onUnknownRoute:
              _builderMapFactory(unknownBuilder, initRouteArgs, initRoute),
        );

  @override
  HybridNavigatorState createState() => HybridNavigatorState();

  /// map the builder to [RouteFactory], use in [_routeNamed(name)]
  static RouteFactory _builderMapFactory(HybridRouteFactory builder,
      Object initRouteArgs, Route<dynamic> initRoute) {
    return (settings) {
      if (settings != null) {
        if (initRoute != null) {
          return initRoute;
        } else {
          settings = settings.copyWith(arguments: initRouteArgs);
        }
      }
      return builder<dynamic>(settings);
    };
  }
}

class HybridNavigatorState extends NavigatorState {
  /// 当前 native 混合栈是否可以退出，如果为 true 将会把当前页面从 [NavigatorContainerManager]
  /// 中移除，代码详见 [pop]
  bool canExit;

  @override
  HybridNavigator get widget => super.widget;

  /// 记录下来的初始路由
  Route<dynamic> _initialRoute;
  _HybridNavigatorObservable _observable;

  @override
  void initState() {
    _observable = _HybridNavigatorObservable(widget.nativePageId);
    widget.observers.add(_observable);
    super.initState();
  }

  @override
  void didUpdateWidget(Navigator oldWidget) {
    widget.observers.add(_observable);
    super.didUpdateWidget(oldWidget);
  }

  /// 获取当前 navigator 的历史拷贝
  List<Route<dynamic>> get routeHistoryCopy {
    List<Route<dynamic>> ret = [];
    if (_observable != null && _observable._routeHistory != null) {
      ret.addAll(_observable._routeHistory);
    }
    return ret;
  }

  /// 判断当前容器是否包含了此 route
  bool containsRoute(Route<dynamic> route) {
    return _observable?.containsRoute(route) ?? false;
  }

  /// because can not override _routeNamed logic
  /// override the indirect function instead
  @override
  Future<T> pushNamed<T extends Object>(String routeName,
      {Object arguments,
      HybridPushType pushType,
      NativePageTransitionType transitionType}) {
    pushType = pushType ?? HybridNavigator.defaultPushType;
    assert(routeName != null);
    assert(pushType != null);
    if (pushType == HybridPushType.Native) {
      return _parseFlutterNativeResult(
          NativeContainerManager.openFlutterNamedInNative(
              nativePageId: widget.nativePageId,
              pageName: routeName,
              args: arguments,
              type: transitionType));
    }
    return super.push<T>(getRouteByName<T>(
      routeName,
      args: arguments,
    ));
  }

  /// 如果想指定 Route 打开页面的方式，传递 [HybridPageRoute]
  @override
  Future<T> push<T extends Object>(Route<T> route) {
    if (_initialRoute == null) {
      _initialRoute = route;
    }
    HybridPushType pushType;
    NativePageTransitionType transitionType;
    if (route is HybridPageRoute<T>) {
      pushType = route.pushType;
      transitionType = route.transitionType;
    } else if (route == widget.initRoute) {
      // 如果是初始路由，必须使用 Flutter 打开方式，防止出现循环打开 native
      pushType = HybridPushType.Flutter;
    } else {
      // 否则使用默认的跳转
      pushType = HybridNavigator.defaultPushType;
    }
    if (pushType == HybridPushType.Native) {
      return _parseFlutterNativeResult(
          NativeContainerManager.openFlutterRouteInNative(
              nativePageId: widget.nativePageId,
              route: route,
              type: transitionType));
    }
    return super.push(route);
  }

  //v1.17 pop的回参由bool改成void ,迁移详细见 https://flutter.dev/docs/release/breaking-changes/route-navigator-refactoring
  @override
  void pop<T extends Object>([T result]) {
    if (canPop()) {
      return super.pop<T>(result);
    }
    // 是否可以退出
    bool canExit = this.canExit ?? !(widget.isTab == true);
    if (canExit) {
      /// pop 函数本来是非异步的，但是这里因为是当前 Navigator 最后一个页面了，所以可以放心
      /// 使用 channel 关闭页面
      /// 对于对应 route 的结束事件回调，交给 [NaviteContainerManager]
      NativeContainerManager.removeNamed(
          nativePageId: widget.nativePageId, result: result);
    }
    return;
  }

  @override
  Future<bool> maybePop<T extends Object>([T result]) async {
    final Route<dynamic> route = _observable._routeHistory.last;
    final RoutePopDisposition disposition = await route.willPop();
    if (disposition != RoutePopDisposition.bubble && mounted) {
      // 不需要冒泡的，走原先策略
      if (disposition == RoutePopDisposition.pop) {
        pop(result);
      }
      return true;
    }
    // 是否可以退出
    bool canExit = this.canExit ?? !(widget.isTab == true);
    if (canExit && !canPop()) {
      // 可以退出 native 页面
      await NativeContainerManager.removeNamed(
          nativePageId: widget.nativePageId, result: result);
      return true;
    }
    return false;
  }

  /// open a native page
  /// [url] the native route url
  Future<NativePageResult<T>> openNativePage<T extends Object>(
      {@required String url,
      Map args,
      NativePageTransitionType transitionType}) {
    assert(url != null);
    return NativeContainerManager.openNativePage<T>(
        url: url,
        nativePageId: widget.nativePageId,
        args: args,
        transitionType: transitionType);
  }

  /// 通过 name 获取到 route
  Route<T> getRouteByName<T>(
    String routeName, {
    Object args,
    bool allowNull = false,
  }) {
    Route<T> route = widget
        .generateBuilder<T>(RouteSettings(name: routeName, arguments: args));

    if (route == null && widget.unknownBuilder != null) {
      route = widget
          .unknownBuilder(RouteSettings(name: routeName, arguments: args));
    }

    if (route == null && !allowNull) {
      assert(() {
        if (route == null) {
          throw FlutterError('A Navigator\'s onUnknownRoute returned null.\n'
              'When trying to build the route "$routeName", both onGenerateRoute and onUnknownRoute returned '
              'null. The onUnknownRoute callback should never return null.\n'
              'The Navigator was:\n'
              '  $this');
        }
        return true;
      }());
    }

    return route;
  }

  @override
  Widget build(BuildContext context) {
    return super.build(context);
  }

  /// 提取 [NativePageResult] 中的 data
  Future<T> _parseFlutterNativeResult<T extends Object>(
      Future<NativePageResult<T>> future) async {
    NativePageResult result = await future;
    return result.data;
  }
}

/// 当前 navigator 容器的辅助 route 工具
class _HybridNavigatorObservable extends NavigatorObserver {
  String nativePageId;

  /// 内部容器的 route 栈
  List<Route<dynamic>> _routeHistory = [];

  _HybridNavigatorObservable(this.nativePageId);

  /// 判断当前内部容器栈是否包含了此 route
  bool containsRoute(Route<dynamic> route) {
    return _routeHistory.contains(route);
  }

  @override
  void didPop(Route<dynamic> route, Route<dynamic> previousRoute) {
    _routeHistory.remove(route);
    super.didPop(route, previousRoute);
  }

  @override
  void didPush(Route<dynamic> route, Route<dynamic> previousRoute) {
    _routeHistory.add(route);
    super.didPush(route, previousRoute);
  }

  @override
  void didRemove(Route route, Route previousRoute) {
    _routeHistory.remove(route);
    super.didRemove(route, previousRoute);
  }
}
