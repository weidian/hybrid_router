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

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';

import 'hybrid_plugin.dart';
import 'model.dart';
import 'navigator.dart';
import 'observer.dart';

/// [NativeContainer] 管理类，管理当前 app 所有的 [NativeContainer]
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
      bool isTab = false,
      bool canPop = true}) {
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

  /// 通过 context 获取当前页面的 native page id
  static String getNativePageIdByContext(BuildContext context) {
    NativeContainerState state = NativeContainer.of(context);
    return state?.widget?.nativePageId ?? null;
  }

  /// 通过 nativePageId 获取到当前 native 容器所有的 route
  static List<Route<dynamic>> getRoutesInNativePage(String nativePageId) {
    _checkState();
    for (NativeContainer container in state._containerHistory) {
      if (container.nativePageId == nativePageId) {
        return container.navKey.currentState?.routeHistoryCopy ?? [];
      }
    }
    return [];
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
    try {
      return await container.maybePop();
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
      bool isTab = false}) {
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
      observers: widget.pageObserver,
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

    /// 这里通过设置 overlay 不透明来拦截 select widget mode
    OverlayEntry overlayEntry = OverlayEntry(
        builder: (context) => container, opaque: true, maintainState: true);
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
    if (container != null) {
      if (!_containerHistory.contains(container)) {
        // need push
        push(container);
        // log error to console
        FlutterError.onError(FlutterErrorDetails(
            exception: "Attempt to show page that "
                "not pushed in stack: initRouteName: ${container.initRouteName}"));
      } else if (container != _containerHistory.last) {
        // need move to top
        overlay.rearrange([container._overlayEntry],
            below: container._overlayEntry);
        NativeContainer topContainer =
            _containerHistory.isEmpty ? null : _containerHistory.last;
        _containerHistory.remove(container);
        _containerHistory.add(container);
        _didShow(container, topContainer);
      }
    }
  }

  /// remove a native container by native page Id
  Future<bool> removeNamed({@required String nativePageId, dynamic result}) {
    try {
      // 此函数可能会在相同的 nativePageId 调用 2 次 （dart 层移除一次，native 页面结束后一次），
      // 所以需要做好判定
      NativeContainer c;
      for (int i = 0; i < _containerHistory.length; ++i) {
        NativeContainer cc = _containerHistory[i];
        if (cc.nativePageId == nativePageId) {
          c = cc;
          break;
        }
      }
      // 未找到，remove 失败
      if (c == null) return Future.value(false);
      c._result = result;
      return remove(c);
    } catch (e, stack) {
      FlutterError.dumpErrorToConsole(
          FlutterErrorDetails(exception: e, stack: stack));
    }
    return SynchronousFuture<bool>(false);
  }

  /// remove a native container
  /// 如果 container 内部含有 Route ，依次调用 route 的 didPop 属性
  Future<bool> remove(NativeContainer container) async {
    if (container == null) {
      return false;
    }

    // 优先查找 container 是否已经移除，因为下面的 await 是一个异步的处理
    // 有可能会触发两次相同 container 的 remove，把 containerHistory
    // 提前就不会了
    int index = _containerHistory.indexOf(container);
    if (index < 0) {
      // 表明 container 已经移除或者在移除中，直接返回 false
      return false;
    }
    // remove history
    _containerHistory.removeAt(index);

    // 通知 native 组件，我将要 pop
    await HybridPlugin.singleton.onNativeRouteEvent(
        event: NativeRouteEvent.beforeDestroy,
        nativePageId: container.nativePageId,
        result: container._result);

    // maybe overlayEntry is empty
    container._overlayEntry?.remove();
    // set the overlayEntry to null that means container's overlay is removed
    container._overlayEntry = null;
    NativeContainer preContainer;
    if (index > 0) {
      preContainer = _containerHistory[index - 1];
    }
    _didRemove(container, preContainer);
    return true;
  }

  @override
  void initState() {
    // https://github.com/flutter/flutter/pull/39535
    // https://github.com/flutter/flutter/issues/39494
    // 以上提交导致息屏状态下如果直接启动flutter界面，会导致界面不会进行渲染，此处强制注册回调
    // ignore: invalid_use_of_protected_member
    SchedulerBinding.instance?.ensureFrameCallbacksRegistered();
    super.initState();
    NativeContainerManager.state = this;
    _startInitRoute();
  }

  @override
  Widget build(BuildContext context) {
    List<OverlayEntry> initEntry;
    if (widget.backgroundBuilder != null) {
      initEntry = [
        OverlayEntry(
            builder: widget.backgroundBuilder,
            opaque: false,
            maintainState: true)
      ];
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
    widget.containerObserver?.forEach((o) {
      try {
        o.didShow(container, topContainer);
      } catch (e) {
        FlutterError.onError(e);
      }
    });
    HybridPlugin.singleton.onNativeRouteEvent(
        event: NativeRouteEvent.onResume, nativePageId: container.nativePageId);
    if (topContainer != null && topContainer != container) {
      HybridPlugin.singleton.onNativeRouteEvent(
          event: NativeRouteEvent.onPause,
          nativePageId: topContainer.nativePageId);
    }
  }

  void _didRemove(NativeContainer container, NativeContainer preContainer) {
    // 通知当前 container 所有的 route 移除事件
    if (container._state != null) {
      List<Route<dynamic>> history = container._state._history;
      for (int i = history.length - 1; i >= 0; --i) {
        Route<dynamic> route = history[i];
        Route<dynamic> preRoute = i == 0 ? null : history[i - 1];
        route.navigator.widget.observers.forEach((o) {
          o.didPop(route, preRoute);
        });
      }
    }
    // 通知 native container 移除事件
    widget.containerObserver?.forEach((o) {
      try {
        o.didRemove(container, preContainer);
      } catch (e) {
        FlutterError.onError(e);
      }
    });
    // 通知 native container 结束事件到 native
    HybridPlugin.singleton.onNativeRouteEvent(
        event: NativeRouteEvent.onDestroy,
        nativePageId: container.nativePageId,
        result: container._result);
    if (preContainer != null) {
      HybridPlugin.singleton.onNativeRouteEvent(
          event: NativeRouteEvent.onResume,
          nativePageId: preContainer.nativePageId);
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

/// Native 容器，一个 native 页面对应一个 [NativeContainer]
class NativeContainer extends StatefulWidget {
  /// 获取 native container 的 state
  static NativeContainerState of(BuildContext context) {
    if (context is StatefulElement && context.state is NativeContainerState) {
      return context.state;
    }
    return context.ancestorStateOfType(TypeMatcher<NativeContainerState>());
  }

  /// 当前 native 容器对应的 nativePageId
  final String nativePageId;

  /// 初始路由名
  final String initRouteName;

  /// 初始路由 为 null 时根据 [initRouteName] 来跳转初始页面
  final Route<dynamic> initRoute;

  /// 路由跳转过来的参数
  final Object args;

  /// 当前 native 容器是否是 tab 页面
  final bool isTab;

  final HybridRouteFactory generateBuilder;

  final HybridRouteFactory unknownBuilder;

  final GlobalKey<HybridNavigatorState> navKey = GlobalKey();

  final List<HybridNavigatorObserver> observers;

  /// 私有非 final 属性
  NativeContainerManagerState _manager;
  OverlayEntry _overlayEntry;
  dynamic _result;
  NativeContainerState _state;

  /// Pop a flutter router in native container
  bool pop<T extends Object>({T result}) {
    assert(
        navKey.currentState != null, "The key of Navigator return null state");
    return navKey.currentState.pop(result);
  }

  /// call maybe pop
  Future<bool> maybePop<T extends Object>({T result}) {
    assert(
        navKey.currentState != null, "The key of Navigator return null state");
    return navKey.currentState.maybePop<T>(result);
  }

  NativeContainer(
      {Key key,
      @required this.nativePageId,
      this.initRouteName,
      this.initRoute,
      this.args,
      this.observers,
      this.isTab = false,
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
  void initState() {
    super.initState();
    widget._state = this;
  }

  @override
  void didUpdateWidget(NativeContainer oldWidget) {
    super.didUpdateWidget(oldWidget);
    widget._state = this;
  }

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
