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
/// @since 2019-02-26 15:10
///
import 'package:flutter/material.dart';
import 'navigator.dart';

/// the data struct that native page return
class NativePageResult<T> {
  /// the code that android platform return
  final int resultCode;

  /// the data that native page return
  final T data;

  NativePageResult({this.resultCode, this.data});
}

/// 路由 push 类型
enum HybridPushType { Flutter, Native }

/// 路由页面构造方法
typedef HybridRouteFactory = Route<T> Function<T extends Object>(
    RouteSettings settings, Object arguments);

typedef HybridWidgetBuilder = Widget Function(BuildContext context, Object args);

/// 页面 transition 动画类型
enum NativePageTransitionType {
  /// 使用默认动画，不做修改
  DEFAULT,

  /// 强制使用底部弹出
  BOTTOM_TOP,

  /// 强制使用从右往左
  RIGHT_LEFT
}

/// [Navigator] push route 的时候，可以传入此 route 来决定是否通过 native 来启动
/// 新的 flutter 页面
class HybridPageRoute<T> extends MaterialPageRoute<T> {
  /// native 页面动画
  final NativePageTransitionType transitionType;

  /// 页面打开类型
  HybridPushType pushType;

  HybridPageRoute({
    @required WidgetBuilder builder,
    this.transitionType = NativePageTransitionType.DEFAULT,
    HybridPushType pushType,
    RouteSettings settings
  })  : assert(builder != null),
        assert(transitionType != null),
        this.pushType = pushType ?? HybridNavigator.defaultPushType,
        super(
            settings: _parseSetting(pushType, settings),
            builder: builder);

  static RouteSettings _parseSetting(HybridPushType pushType, RouteSettings defaultSetting) {
    pushType = pushType ?? HybridNavigator.defaultPushType;
    if (pushType == HybridPushType.Native) {
      if (defaultSetting == null) {
        return RouteSettings(isInitialRoute: true);
      } else if (!defaultSetting.isInitialRoute){
        return defaultSetting.copyWith(isInitialRoute: true);
      }
    }
    return defaultSetting;
  }
}
