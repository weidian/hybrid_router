package com.vdian.flutter.hybridrouter.page;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * ┏┛ ┻━━━━━┛ ┻┓
 * ┃　　　　　　 ┃
 * ┃　　　━　　　┃
 * ┃　┳┛　  ┗┳　┃
 * ┃　　　　　　 ┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　 ┃
 * ┗━┓　　　┏━━━┛
 * * ┃　　　┃   神兽保佑
 * * ┃　　　┃   代码无BUG！
 * * ┃　　　┗━━━━━━━━━┓
 * * ┃　　　　　　　    ┣┓
 * * ┃　　　　         ┏┛
 * * ┗━┓ ┓ ┏━━━┳ ┓ ┏━┛
 * * * ┃ ┫ ┫   ┃ ┫ ┫
 * * * ┗━┻━┛   ┗━┻━┛
 *
 * flutter wrap activity 的一些配置信息
 *
 * @author qigengxin
 * @since 2019/3/23 3:29 PM
 */
public interface IFlutterWrapConfig {

    /**
     * 在 flutter 更新 native 状态栏主题前调用
     */
    void preFlutterApplyTheme(@NonNull FlutterWrapActivity activity);

    /**
     * 在 flutter 更新 native 状态栏主题之后调用
     */
    void postFlutterApplyTheme(@NonNull FlutterWrapActivity activity);

    /**
     * 请求打开 flutter page route
     * @param routeOptions
     * @param requestCode
     */
    boolean onFlutterPageRoute(@NonNull FlutterWrapActivity activity,
                               @Nullable FlutterRouteOptions routeOptions, int requestCode);

    /**
     * 请求打开 native 页面
     * @param routeOptions
     * @param requestCode
     */
    boolean onNativePageRoute(@NonNull FlutterWrapActivity activity,
                              @NonNull NativeRouteOptions routeOptions, int requestCode);

    /**
     * 从 intent 中解析 flutterRouteOptions
     * @param intent
     * @return
     */
    FlutterRouteOptions parseFlutterRouteFromIntent(@NonNull FlutterWrapActivity activity,
                                                    Intent intent);

    /**
     * 获取页面切换动画
     * @param transitionType 动画切换类型 {@link FlutterRouteOptions#TRANSITION_TYPE_DEFAULT}
     * @param isOpenPage 是否是打开页面
     * @return true 表示拦截动画处理
     */
    boolean updatePageTransition(@NonNull FlutterWrapActivity activity,
                                 int transitionType, boolean isOpenPage);
}
