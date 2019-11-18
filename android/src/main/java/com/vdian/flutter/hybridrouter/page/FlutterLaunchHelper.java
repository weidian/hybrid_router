package com.vdian.flutter.hybridrouter.page;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.flutter.embedding.engine.FlutterShellArgs;

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
 * launch flutter app 用的辅助类
 *
 * 混合栈的 activity 和 fragment 是自己开发的，但是 flutter_tools 用的还是原生的，
 * 这里需要匹配下原生 flutter_tools 的参数解析，像：--trace-skia。需要解析的 intent
 * 有以下几个类型：
 * * FlutterShellArgs
 * * AppBundlePath
 * * Dart enter point
 * * init route
 *
 * @author qigengxin
 * @since 2019-11-18 15:03
 */
public class FlutterLaunchHelper {

    /**
     * 从启动参数 intent 中解析出 flutter shell args
     */
    @NonNull
    public static FlutterShellArgs parseFlutterShellArgs(@NonNull Intent intent) {
        return FlutterShellArgs.fromIntent(intent);
    }

    /**
     * 从启动参数中解析出当前的 dart main 函数入口
     */
    @Nullable
    public static String getDartEntrypointName(@NonNull Intent intent) {
        if (intent.hasExtra("dart_entrypoint")) {
            return intent.getStringExtra("dart_entrypoint");
        }
        return null;
    }

    /**
     * 从启动参数中解析当前 flutter 的启动路由
     */
    @Nullable
    public static String getInitRoute(@NonNull Intent intent) {
        if (intent.hasExtra("initial_route")) {
            return intent.getStringExtra("initial_route");
        }
        return null;
    }
}
