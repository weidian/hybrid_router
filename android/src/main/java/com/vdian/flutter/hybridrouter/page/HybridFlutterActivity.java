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
package com.vdian.flutter.hybridrouter.page;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

import io.flutter.embedding.android.FlutterView;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.FlutterShellArgs;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformPlugin;
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
 * @since 2019-10-16 19:12
 */
public class HybridFlutterActivity extends AppCompatActivity implements IFlutterNativePage {

    /**
     * 获取 flutter 返回的页面结果
     */
    public static Object getFlutterResule(@NonNull Intent data) {
        return FlutterNativePageDelegate.getFlutterResult(data);
    }

    /**
     * return a new intent builder
     *
     * @return
     */
    public static IntentBuilder newBuilder() {
        return new IntentBuilder();
    }

    public static class IntentBuilder extends GenericIntentBuilder<IntentBuilder> {

        protected IntentBuilder() {
            super(HybridFlutterActivity.class);
        }
    }

    public abstract static class GenericIntentBuilder<T extends GenericIntentBuilder<T>> {

        private FlutterView.RenderMode renderMode;
        private FlutterView.TransparencyMode transparencyMode;
        private FlutterRouteOptions routeOptions;
        private FlutterShellArgs shellArgs;
        private String dartEntrypoint = "main";
        private String appBundlePath = null;
        private Bundle arguments;
        private final Class<? extends HybridFlutterActivity> activityClass;

        protected GenericIntentBuilder(Class<? extends HybridFlutterActivity> activityClass) {
            this.activityClass = activityClass;
        }

        /**
         * 设置 flutter view 的渲染模式
         */
        public T renderMode(FlutterView.RenderMode renderMode) {
            this.renderMode = renderMode;
            return (T) this;
        }

        /**
         * 设置 flutter view 的透明度模式
         */
        public T transparencyMode(FlutterView.TransparencyMode transparencyMode) {
            this.transparencyMode = transparencyMode;
            return (T) this;
        }

        /**
         * 路由参数
         */
        public T route(FlutterRouteOptions routeOptions) {
            this.routeOptions = routeOptions;
            return (T) this;
        }

        /**
         * 引擎初始化信息
         */
        public T initializationArgs(FlutterShellArgs shellArgs) {
            this.shellArgs = shellArgs;
            return (T) this;
        }

        /**
         * app bundle 启动路径
         */
        public T appBundlePath(String appBundlePath) {
            this.appBundlePath = appBundlePath;
            return (T) this;
        }

        /**
         * dart 虚拟机主函数入口
         */
        public T dartEntrypoint(String dartEntrypoint) {
            this.dartEntrypoint = dartEntrypoint;
            return (T) this;
        }

        /**
         * fragment arguments 额外参数
         */
        public T arguments(Bundle arguments) {
            this.arguments = arguments;
            return (T) this;
        }

        @NonNull
        public Intent buildIntent(@NonNull Context context) {
            Intent ret = new Intent(context, activityClass);
            ret.putExtra(ARG_APP_BUNDLE_PATH, appBundlePath);
            ret.putExtra(ARG_DART_ENTRYPOINT, dartEntrypoint);
            if (null != shellArgs) {
                ret.putExtra(ARG_FLUTTER_SHELL_ARGS, shellArgs.toArray());
            }
            if (routeOptions != null) {
                ret.putExtra(ARG_FLUTTER_ROUTE, routeOptions);
            }
            ret.putExtra(ARG_FLUTTERVIEW_RENDER_MODE, renderMode != null ? renderMode.name()
                    : FlutterView.RenderMode.surface.name());
            ret.putExtra(ARG_FLUTTERVIEW_TRANSPARENCY_MOD, transparencyMode != null ?
                    transparencyMode.name() : FlutterView.TransparencyMode.transparent.name());
            if (arguments != null) {
                ret.getExtras().putAll(arguments);
            }
            return ret;

        }
    }

    private static final String ARG_FLUTTER_ROUTE = "arg_flutter_route";
    private static final String ARG_FLUTTER_SHELL_ARGS = "arg_initialization_args";
    private static final String ARG_APP_BUNDLE_PATH = "arg_app_bundle_path";
    private static final String ARG_DART_ENTRYPOINT = "arg_dart_entrypoint";
    // 内部视图渲染模式
    private static final String ARG_FLUTTERVIEW_RENDER_MODE = "arg_flutterview_render_mode";
    private static final String ARG_FLUTTERVIEW_TRANSPARENCY_MOD = "arg_flutterview_transparency_mode";


    @Override
    public boolean isAttachToFlutter() {
        return pageDelegate.isAttachToFlutter();
    }

    @Override
    public void attachFlutter() {
        pageDelegate.attachEngine();
    }

    @Override
    public void detachFlutter() {
        pageDelegate.detachEngine();
    }

    @Override
    public void requestDetachFromWindow() {
        pageDelegate.detachEngine();
    }

    @Override
    public void openNativePage(@NonNull NativeRouteOptions routeOptions, @NonNull MethodChannel.Result result) {
        pageDelegate.openNativePage(routeOptions, result);
    }


    @Override
    public void openFlutterPage(@NonNull FlutterRouteOptions routeOptions, @NonNull MethodChannel.Result result) {
        pageDelegate.openFlutterPage(routeOptions, result);
    }

    @Override
    public void finishNativePage(@Nullable Object result) {
        // 先让 flutter 脱离渲染
        detachFlutter();
        pageDelegate.setPageResult(result);
        finish();
    }

    @Override
    public String getNativePageId() {
        return pageDelegate.getNativePageId();
    }

    /**
     * 获取初始路由，如果是首次打开页面，需要通过 flutter 来主动获取路由信息
     */
    @Override
    public Map<String, Object> getInitRoute() {
        return pageDelegate.getInitRoute();
    }

    @Override
    public int generateRequestCodeByChannel(MethodChannel.Result result) {
        return pageDelegate.generateRequestCodeByChannel(result);
    }

    @Override
    public int generateRequestCodeByCallback(@NonNull IPageResultCallback callback) {
        return pageDelegate.generateRequestCodeByCallback(callback);
    }

    @Nullable
    @Override
    public Context getContext() {
        return this;
    }

    @Nullable
    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public FlutterRouteOptions getStartRouteOptions() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            return bundle.getParcelable(ARG_FLUTTER_ROUTE);
        }
        return null;
    }

    @Override
    public void onFlutterRouteEvent(String name, int eventId, Map extra) {
    }

    @Override
    public boolean isTab() {
        return false;
    }

    @NonNull
    @Override
    public FlutterShellArgs getFlutterShellArgs() {
        String[] flutterShellArgsArray = getArguments().getStringArray(ARG_FLUTTER_SHELL_ARGS);
        return new FlutterShellArgs(
                flutterShellArgsArray != null ? flutterShellArgsArray : new String[]{}
        );
    }

    @NonNull
    @Override
    public String getDartEntrypointFunctionName() {
        return getArguments().getString(ARG_DART_ENTRYPOINT, "main");
    }

    @NonNull
    @Override
    public String getAppBundlePath() {
        return getArguments().getString(ARG_APP_BUNDLE_PATH, FlutterMain.findAppBundlePath());
    }

    @NonNull
    @Override
    public FlutterView.RenderMode getRenderMode() {
        String renderModeName = getArguments().getString(
                ARG_FLUTTERVIEW_RENDER_MODE,
                FlutterView.RenderMode.surface.name()
        );
        return FlutterView.RenderMode.valueOf(renderModeName);
    }

    @NonNull
    @Override
    public FlutterView.TransparencyMode getTransparencyMode() {
        String transparencyModeName = getArguments().getString(
                ARG_FLUTTERVIEW_TRANSPARENCY_MOD,
                FlutterView.TransparencyMode.transparent.name()
        );
        return FlutterView.TransparencyMode.valueOf(transparencyModeName);
    }

    @Nullable
    @Override
    public PlatformPlugin providePlatformPlugin(@Nullable Activity activity, @NonNull FlutterEngine flutterEngine) {
        return new PlatformPlugin(activity, flutterEngine.getPlatformChannel());
    }

    @Nullable
    @Override
    public View provideSplashScreen() {
        View background = new View(getContext());
        background.setBackground(getLaunchScreenDrawableFromActivityTheme());
        return background;
    }

    @NonNull
    private FlutterNativePageDelegate pageDelegate = createPageDelegate();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageDelegate.onCreate(savedInstanceState);
        View view = pageDelegate.onCreateView(null, null,
                savedInstanceState);
        view = view == null ? new View(this) : view;
        setContentView(view);
    }

    @Override
    public void onStart() {
        super.onStart();
        pageDelegate.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        pageDelegate.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        pageDelegate.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        pageDelegate.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pageDelegate.onDestroyView();
        pageDelegate.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        pageDelegate.onSaveInstance(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        pageDelegate.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        pageDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        pageDelegate.onBackPressed(new Runnable() {
            @Override
            public void run() {
                HybridFlutterActivity.super.onBackPressed();
            }
        });
    }

    @Override
    public void onNewIntent(@NonNull Intent intent) {
        pageDelegate.onNewIntent(intent);
    }

    @Override
    public void onUserLeaveHint() {
        pageDelegate.onUserLeaveHint();
    }

    @Override
    public void onTrimMemory(int level) {
        pageDelegate.onTrimMemory(level);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        pageDelegate.onLowMemory();
    }

    protected FlutterNativePageDelegate createPageDelegate() {
        return new FlutterNativePageDelegate(this);
    }

    private Bundle getArguments() {
        return getIntent().getExtras();
    }

    @Nullable
    private Drawable getLaunchScreenDrawableFromActivityTheme() {
        Context context = getContext();
        if (context == null) return null;
        TypedValue typedValue = new TypedValue();
        if (!context.getTheme().resolveAttribute(
                android.R.attr.windowBackground,
                typedValue,
                true)) {
            return null;
        }
        if (typedValue.resourceId == 0) {
            return null;
        }
        try {
            return getResources().getDrawable(typedValue.resourceId);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
