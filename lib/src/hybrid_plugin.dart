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
/// @author qigengxin
/// @since 2019-01-11 17:37

import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter/material.dart';
import 'model.dart';
import 'manager.dart';

/// Native 容器对应的 route 变化的事件
enum NativeRouteEvent {
  /// native 容器对应的 route 被创建 push 加入
  onCreate,

  /// 当前 native 容器处于非激活状态
  onPause,

  /// 当前 native 容器处于激活状态
  onResume,

  /// 当前 native 容器关闭之前回调
  beforeDestroy,

  /// 当前 native 容器对应的 route 被弹出 root 堆栈
  onDestroy
}

/// Native 接收到的 flutter page 事件
/// pop /remove 在 resume 前
/// push 在 pause 后
enum FlutterRouteEvent {
  /// flutter route push 事件
  onPush,

  /// flutter route resume 事件
  onResume,

  /// flutter route pause 事件
  onPause,

  /// flutter route replace 事件
  onReplace,

  /// flutter route pop 事件
  onPop,

  /// flutter route remove 事件
  onRemove
}

class HybridPlugin {
  static HybridPlugin get singleton {
    if (_instance == null) {
      _instance = HybridPlugin._();
    }
    return _instance;
  }

  static HybridPlugin _instance;

  /// 对应 [nativePageId] 的 native container 生命周期事件
  /// [event] 页面生命周期事件
  /// [nativePageId] 页面对应的 flutter 容器的 native page id
  Future<void> onNativeRouteEvent(
      {@required NativeRouteEvent event,
      @required String nativePageId,
      dynamic result}) async {
    assert(event != null && nativePageId != null);
    print('onNativeRouteEvent: ($nativePageId): $event');
    return _channel.invokeMethod("onNativeRouteEvent", {
      "eventId": event.index,
      "nativePageId": nativePageId,
      "result": result
    });
  }

  /// 对应 [nativePageId] 的 native container 内 flutter route 变化事件
  /// [event] 事件详情
  Future<void> onFlutterRouteEvent(
      {@required String nativePageId,
      @required FlutterRouteEvent event,
      @required String name,
      Map extra}) {
    assert(nativePageId != null && event != null);
    print('onFlutterRouteEvent: ($nativePageId): $event');
    return _channel.invokeMethod("onFlutterRouteEvent", {
      "eventId": event.index,
      "nativePageId": nativePageId,
      "name": name ?? '',
      "extra": extra
    });
  }

  /// 从 native 端打开一个页面
  /// [url] 页面路由
  /// [args] 页面参数
  /// [nativePageId] 当前堆栈对应的关键帧所在的 native page id
  Future<NativePageResult> openNativePage(
      {String url,
      String nativePageId,
      Map args,
      NativePageTransitionType transitionType}) async {
    assert(url != null);
    assert(nativePageId != null);
    assert(transitionType != null);

    Map param = {"url": url, "nativePageId": nativePageId, "args": args ?? {}};

    /// 这里定义下动画类型的 type to int
    int navTranType = 0;
    switch (transitionType) {
      case NativePageTransitionType.DEFAULT:
        navTranType = 0;
        break;
      case NativePageTransitionType.BOTTOM_TOP:
        navTranType = 1;
        break;
      case NativePageTransitionType.RIGHT_LEFT:
        navTranType = 2;
        break;
    }
    param["transitionType"] = navTranType;

    Map result = await _channel.invokeMethod("openNativePage", param) ?? {};
    return NativePageResult(
        resultCode: result["resultCode"], data: result["data"]);
  }

  /// 通过 native 重新打开一个 flutter page
  /// [pageName] 页面路由名
  /// [nativePageId] 打开页面需要的上下文环境
  Future<NativePageResult<T>> openFlutterPage<T extends Object>({
    @required String pageName,
    @required String nativePageId,
    @required NativePageTransitionType transitionType,
    Object args,
  }) async {
    assert(pageName != null);
    assert(transitionType != null);
    assert(nativePageId != null);
    Map param = {};
    param["pageName"] = pageName;
    param["args"] = args;
    param["nativePageId"] = nativePageId;

    /// 这里定义下动画类型的 type to int
    int navTranType = 0;
    switch (transitionType) {
      case NativePageTransitionType.DEFAULT:
        navTranType = 0;
        break;
      case NativePageTransitionType.BOTTOM_TOP:
        navTranType = 1;
        break;
      case NativePageTransitionType.RIGHT_LEFT:
        navTranType = 2;
        break;
    }
    param["transitionType"] = navTranType;
    Map result = await _channel.invokeMethod("openFlutterPage", param) ?? {};
    return NativePageResult<T>(data: result["data"]);
  }

  /// 获取初始路由参数
  Future<Map> getInitRoute() async {
    return await _channel.invokeMethod("getInitRoute");
  }

  _setupChannelHandler() {
    _channel.setMethodCallHandler((MethodCall call) async {
      /// method name
      String methodName = call.method;
      switch (methodName) {
        case "pushFlutterPage":
          Map args = call.arguments;
          assert(args != null);
          NativeContainerManager.pushNamed(
              nativePageId: args["nativePageId"],
              pageName: args["pageName"],
              args: args["args"]);
          break;
        case "requestUpdateTheme":

          /// 请求更新主题色到 native 端，这里使用了一个测试接口，以后要注意
          /// 目前 android 才有
          var preTheme = SystemChrome.latestStyle;
          if (preTheme != null) {
            SystemChannels.platform.invokeMethod(
                "SystemChrome.setSystemUIOverlayStyle", _toMap(preTheme));
          }
          break;
        case "onBackPressed":

          /// 这里重写了 onBackPressed 是防止出现黑屏无法返回退出的情况
          /// 仅 android 才有
          return await NativeContainerManager.onBackPressed();
        case "onNativePageFinished":
          Map args = call.arguments;
          assert(args != null);
          print('onNativePageFinished: ${args['nativePageId']}');
          NativeContainerManager
              .removeNamed(nativePageId: args["nativePageId"]);
          break;
        case "onNativePageResumed":
          Map args = call.arguments;
          assert(args != null);
          print('onNativePageFinished: ${args['nativePageId']}');
          NativeContainerManager
              .showNamed(nativePageId: args["nativePageId"]);
          break;
        default:
          assert(false, "Method: ${call.method} not implemented in flutter");
          break;
      }
    });
  }

  Map<String, dynamic> _toMap(SystemUiOverlayStyle style) {
    return <String, dynamic>{
      'systemNavigationBarColor': style.systemNavigationBarColor?.value,
      'systemNavigationBarDividerColor':
          style.systemNavigationBarDividerColor?.value,
      'statusBarColor': style.statusBarColor?.value,
      'statusBarBrightness': style.statusBarBrightness?.toString(),
      'statusBarIconBrightness': style.statusBarIconBrightness?.toString(),
      'systemNavigationBarIconBrightness':
          style.systemNavigationBarIconBrightness?.toString(),
    };
  }

  /// demo channel
  Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  /// channel
  static MethodChannel _channel;

  HybridPlugin._() {
    if (_channel == null) {
      _channel = MethodChannel('com.vdian.flutter.hybridrouter');
    }
    _setupChannelHandler();
  }
}
