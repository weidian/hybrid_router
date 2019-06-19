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
/// 路由观察者
/// @author qigengxin
/// @since 2019-03-23 15:51
///
import 'package:flutter/material.dart';
import 'package:hybrid_router/src/manager.dart';
import 'navigator.dart';

/// 混合栈内部的 Observer，可以观察到混合栈中不同 Navigator 中 route 的变化
class HybridNavigatorObserver {

  /// The [HybridNavigator] pushed `route`.
  ///
  /// The route immediately below that one, and thus the previously active
  /// route, is `previousRoute`.
  void didPush(Route<dynamic> route, Route<dynamic> previousRoute) { }

  /// The [HybridNavigator] popped `route`.
  ///
  /// The route immediately below that one, and thus the newly active
  /// route, is `previousRoute`.
  void didPop(Route<dynamic> route, Route<dynamic> previousRoute) { }

  /// The [HybridNavigator] removed `route`.
  ///
  /// If only one route is being removed, then the route immediately below
  /// that one, if any, is `previousRoute`.
  ///
  /// If multiple routes are being removed, then the route below the
  /// bottommost route being removed, if any, is `previousRoute`, and this
  /// method will be called once for each removed route, from the topmost route
  /// to the bottommost route.
  void didRemove(Route<dynamic> route, Route<dynamic> previousRoute) { }

  /// The [HybridNavigator] replaced `oldRoute` with `newRoute`.
  void didReplace({ Route<dynamic> newRoute, Route<dynamic> oldRoute }) { }

  /// The [HybridNavigator]'s route `route` is being moved by a user gesture.
  ///
  /// For example, this is called when an iOS back gesture starts.
  ///
  /// Paired with a call to [didStopUserGesture] when the route is no longer
  /// being manipulated via user gesture.
  ///
  /// If present, the route immediately below `route` is `previousRoute`.
  /// Though the gesture may not necessarily conclude at `previousRoute` if
  /// the gesture is canceled. In that case, [didStopUserGesture] is still
  /// called but a follow-up [didPop] is not.
  void didStartUserGesture(Route<dynamic> route, Route<dynamic> previousRoute) { }

  /// User gesture is no longer controlling the [HybridNavigator].
  ///
  /// Paired with an earlier call to [didStartUserGesture].
  void didStopUserGesture() { }
}

class NativeContainerObserver {

  void didPush(NativeContainer container, NativeContainer previousContainer) { }

  void didShow(NativeContainer container, NativeContainer topContainer) { }

  void didRemove(NativeContainer container, NativeContainer previousContainer) { }
}