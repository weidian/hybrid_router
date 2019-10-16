package com.vdian.flutter.hybridrouter.page;

import android.support.annotation.NonNull;

import io.flutter.embedding.android.FlutterView;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.platform.PlatformPlugin;

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
 * Flutter engine hook
 *
 * @author qigengxin
 * @since 2019-10-16 16:15
 */
public interface IFlutterHook {


    /**
     * Hook for the host to configure the {@link FlutterEngine} as desired.
     */
    void configureFlutterEngine(@NonNull FlutterEngine flutterEngine);

    /**
     * Hook for the host to cleanup references that were established in
     * {@link #configureFlutterEngine(FlutterEngine)} before the host is destroyed or detached.
     */
    void cleanUpFlutterEngine(@NonNull FlutterEngine flutterEngine);

    /**
     * flutter engine initialize error
     */
    void onFlutterInitFailure(@NonNull Throwable error);

    /**
     * This method will invoke after register GeneratedPluginRegistrant
     * @param pluginRegistry
     */
    void onRegisterPlugin(PluginRegistry pluginRegistry);

    /**
     * This method will invoke after flutterView attach to flutter engine.
     */
    void afterFlutterViewAttachToEngine(@NonNull FlutterView flutterView,
                                        @NonNull FlutterEngine flutterEngine);

    /**
     * This method will invoke before flutterView detach from flutter engine.
     */
    void beforeFlutterViewDetachFromEngine(@NonNull FlutterView flutterView,
                                           @NonNull FlutterEngine flutterEngine);


    /**
     * This method will invoke after {@link PlatformPlugin#updateSystemUiOverlays()}
     * @param flutterView
     */
    void afterUpdateSystemUiOverlays(FlutterView flutterView);
}
