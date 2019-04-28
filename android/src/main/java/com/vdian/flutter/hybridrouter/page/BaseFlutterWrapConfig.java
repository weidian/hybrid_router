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
 * @author qigengxin
 * @since 2019-04-25 15:22
 */
public class BaseFlutterWrapConfig implements IFlutterWrapConfig {
    @Override
    public void preFlutterApplyTheme(@NonNull FlutterWrapActivity activity) {
    }

    @Override
    public void postFlutterApplyTheme(@NonNull FlutterWrapActivity activity) {

    }

    @Override
    public boolean onFlutterPageRoute(@NonNull FlutterWrapActivity activity, @Nullable FlutterRouteOptions routeOptions, int requestCode) {
        return false;
    }

    @Override
    public boolean onNativePageRoute(@NonNull FlutterWrapActivity activity, @NonNull NativeRouteOptions routeOptions, int requestCode) {
        return false;
    }

    @Override
    public FlutterRouteOptions parseFlutterRouteFromIntent(@NonNull FlutterWrapActivity activity, Intent intent) {
        return null;
    }

    @Override
    public boolean updatePageTransition(@NonNull FlutterWrapActivity activity, int transitionType, boolean isOpenPage) {
        return false;
    }
}
