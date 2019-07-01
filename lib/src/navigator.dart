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
import 'dart:async';
import 'manager.dart';
import 'observer.dart';
import 'model.dart';

class HybridNavigator extends Navigator {
  /// 获取页面默认打开方式
  static HybridPushType get defaultPushType {
    if (NativeContainerManager.isInit()) {
      return NativeContainerManager.state.widget.defaultPushType;
    }
    return HybridPushType.Native;
  }

  static NativeContainerManager init(
      {@required Map<String, HybridWidgetBuilder> routes,
      WidgetBuilder backgroundBuilder,
      HybridPushType defaultPushType,
      List<NativeContainerObserver> containerObserver = const [],
      List<HybridNavigatorObserver> pageObserver = const [],
      HybridRouteFactory unknownRouteBuilder}) {
    return NativeContainerManager(
      routes: routes,
      defaultPushType: defaultPushType,
      containerObserver: containerObserver,
      pageObserver: pageObserver,
      backgroundBuilder: backgroundBuilder,
      unknownRouteBuilder: unknownRouteBuilder,
    );
  }

  /// 通过此 context 获取到对应的 native page id
  static String getNativePageIdByContext(BuildContext context) {}

  /// 通过此 route 获取到对应的 native page id
  /// 此 route 可以是混合栈容器内的 route，也可以是混合栈本身的 route
  static String getNativePageIdByRoute(Route<dynamic> route) {}

  /// 获取 native 容器包含的 route 列表
  static List<Route<dynamic>> getRoutesInNativePage(String nativePageId) {}

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
    bool isTab,
    List<NavigatorObserver> observers,
    Route<dynamic> initRoute,
    this.generateBuilder,
    this.unknownBuilder,
  })  : assert(initialRoute != null || initRoute != null),
        assert(nativePageId != null),
        initRouteArgs = initRouteArgs,
        initRoute = initRoute,
        isTab = isTab,
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
      if (settings.isInitialRoute) {
        if (initRoute != null) {
          return initRoute;
        } else {
          settings = settings.copyWith(arguments: initRouteArgs);
        }
      }
      return builder(settings);
    };
  }
}

class HybridNavigatorState extends NavigatorState {
  _HybridNavigatorObservable _observable;

  @override
  HybridNavigator get widget => super.widget;

  /// 记录下来的初始路由
  Route<dynamic> _initialRoute;

  @override
  void initState() {
    _observable = _HybridNavigatorObservable(widget.nativePageId);
    widget.observers.add(_observable);
    super.initState();
  }

  @override
  void didUpdateWidget(Navigator oldWidget) {
    oldWidget.observers.remove(_observable);
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

    if(widget.isTab) {
      pushType = HybridPushType.Native;
    }

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
    if (route.settings.isInitialRoute && _initialRoute == null) {
      _initialRoute = route;
    }
    HybridPushType pushType;
    NativePageTransitionType transitionType;
    if (route is HybridPageRoute<T>) {
      pushType = route.pushType;
      transitionType = route.transitionType;
    } else if (route == widget.initRoute || route.settings.isInitialRoute) {
      // 如果是初始路由，必须使用 Flutter 打开方式，防止出现循环打开 native
      pushType = HybridPushType.Flutter;
    } else {
      // 否则使用默认的跳转
      pushType = HybridNavigator.defaultPushType;
    }
    if(widget.isTab && !route.settings.isInitialRoute) {
      pushType = HybridPushType.Native;
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

  @override
  bool pop<T extends Object>([T result]) {
    if (canPop()) {
      return super.pop(result);
    }
    if(widget.isTab == null || !widget.isTab) {
      // pop 函数本来是非异步的，但是这里因为是当前 Navigator 最后一个页面了，所以可以放心
      // 使用 channel 关闭页面
      NativeContainerManager.removeNamed(
          nativePageId: widget.nativePageId, result: result);
    }
    return true;
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
    Route<T> route = widget.generateBuilder<T>(
        RouteSettings(name: routeName, isInitialRoute: false, arguments: args));

    if (route == null && widget.unknownBuilder != null) {
      route = widget.unknownBuilder(RouteSettings(
          name: routeName, isInitialRoute: false, arguments: args));
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
