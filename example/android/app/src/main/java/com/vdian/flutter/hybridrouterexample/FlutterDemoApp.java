package com.vdian.flutter.hybridrouterexample;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Window;

import com.vdian.flutter.hybridrouter.FlutterManager;
import com.vdian.flutter.hybridrouter.page.BaseFlutterWrapConfig;
import com.vdian.flutter.hybridrouter.page.FlutterRouteOptions;
import com.vdian.flutter.hybridrouter.page.FlutterWrapActivity;
import com.vdian.flutter.hybridrouter.page.FlutterWrapFragment;
import com.vdian.flutter.hybridrouter.page.IFlutterNativePage;

import io.flutter.view.FlutterMain;

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
 * @since 2019/2/11 12:08 PM
 */
public class FlutterDemoApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 提前初始化
        FlutterMain.startInitialization(this);
        // 可以添加一些自定义的行为
        FlutterManager.getInstance().setFlutterWrapConfig(new BaseFlutterWrapConfig() {

            @Override
            public void postFlutterApplyTheme(@NonNull IFlutterNativePage nativePage) {
                // 修改当前沉浸式主题的背景色为透明
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                        nativePage.getContext() instanceof Activity) {
                    Window window = ((Activity)nativePage.getContext()).getWindow();
                    window.setStatusBarColor(Color.TRANSPARENT);
                }
            }

            @Override
            public boolean onFlutterPageRoute(@NonNull IFlutterNativePage nativePage,
                                              @Nullable FlutterRouteOptions routeOptions, int requestCode) {
                // 自定义flutter 页面的跳转
                Intent intent = FlutterWrapActivity.startIntent(nativePage.getContext(), routeOptions);
                nativePage.startActivityForResult(intent, requestCode);
                return true;
            }
        });
    }
}
