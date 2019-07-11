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
import android.arch.lifecycle.Lifecycle;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.vdian.flutter.hybridrouter.FlutterManager;
import com.vdian.flutter.hybridrouter.FlutterStackManagerUtil;
import com.vdian.flutter.hybridrouter.HybridRouterPlugin;
import com.vdian.flutter.hybridrouter.ScreenshotManager;
import com.vdian.flutter.hybridrouter.engine.FixFlutterEngine;
import com.vdian.flutter.hybridrouter.engine.FixPlatformPlugin;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.android.FlutterView;
import io.flutter.embedding.engine.FlutterShellArgs;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.embedding.engine.renderer.OnFirstFrameRenderedListener;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.view.FlutterMain;

import static android.app.Activity.RESULT_OK;
import static android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW;
import static com.vdian.flutter.hybridrouter.FlutterStackManagerUtil.assertNotNull;

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
 * @since 2019-06-19 10:23
 */
public class FlutterWrapFragment extends Fragment implements IFlutterNativePage {

    public interface IPageDelegate {

        /**
         * flutter 请求结束 native 页面
         */
        void finishNativePage(FlutterWrapFragment page, @Nullable Object result);

        /**
         * 判断是否是 tab
         * @return
         */
        boolean isTab(FlutterWrapFragment page);
    }

    /**
     * activity 页面结束代替
     */
    public static class ActivityPageDelegate implements IPageDelegate {

        private boolean isTab;

        public ActivityPageDelegate() {
            this.isTab = false;
        }

        /**
         * 当前的页面是否是 tab 页面
         */
        public ActivityPageDelegate tab(boolean tab) {
            isTab = tab;
            return this;
        }

        @Override
        public void finishNativePage(FlutterWrapFragment page, @Nullable Object result) {
            if (page.getActivity() != null) {
                FragmentActivity activity = page.getActivity();
                if (result == null) {
                    activity.setResult(RESULT_OK);
                } else {
                    Intent data = new Intent();
                    FlutterStackManagerUtil.updateIntent(data, EXTRA_RESULT_KEY, result);
                    activity.setResult(RESULT_OK, data);
                }
                // 结束当前 native 页面
                activity.finish();
            }
        }

        @Override
        public boolean isTab(FlutterWrapFragment page) {
            return isTab;
        }
    }

    public static class Builder {

        IPageDelegate delegate;
        Bundle arguments = new Bundle();

        public Builder pageDelegate(IPageDelegate delegate) {
            this.delegate = delegate;
            return this;
        }

        /**
         * 设置 flutter view 的渲染模式
         */
        public Builder renderMode(FlutterView.RenderMode renderMode) {
            if (renderMode != null) {
                arguments.putString(EXTRA_FLUTTERVIEW_RENDER_MODE, renderMode.name());
            }
            return this;
        }

        /**
         * 设置 flutter view 的透明度模式
         */
        public Builder transparencyMode(FlutterView.TransparencyMode transparencyMode) {
            if (transparencyMode != null) {
                arguments.putString(EXTRA_FLUTTERVIEW_TRANSPARENCY_MOD, transparencyMode.name());
            }
            return this;
        }

        /**
         * 路由参数
         * @param routeOptions
         * @return
         */
        public Builder route(FlutterRouteOptions routeOptions) {
            if (routeOptions != null) {
                arguments.putParcelable(EXTRA_FLUTTER_ROUTE, routeOptions);
            }
            return this;
        }

        /**
         * 引擎初始化信息
         * @param args
         * @return
         */
        public Builder initializationArgs(String[] args) {
            if (args != null) {
                arguments.putStringArray(EXTRA_INITIALIZATION_ARGS, args);
            }
            return this;
        }

        /**
         * app bundle 启动路径
         * @param appBundlePath
         * @return
         */
        public Builder appBundlePath(String appBundlePath) {
            if (appBundlePath != null) {
                arguments.putString(EXTRA_APP_BUNDLE_PATH, appBundlePath);
            }
            return this;
        }

        /**
         * dart 虚拟机主函数入口
         * @param dartEntrypoint
         * @return
         */
        public Builder dartEntrypoint(String dartEntrypoint) {
            if (dartEntrypoint != null) {
                arguments.putString(EXTRA_DART_ENTRYPOINT, dartEntrypoint);
            }
            return this;
        }

        /**
         * fragment arguments 额外参数
         * @param arguments
         * @return
         */
        public Builder extra(Bundle arguments) {
            this.arguments.putAll(arguments);
            return this;
        }

        /**
         * 是否启用截屏功能，实验性，默认关闭，后续可能会移除
         * @param openScreenshot
         * @return
         */
        public Builder screenshot(boolean openScreenshot) {
            this.arguments.putBoolean(EXTRA_ENABLE_SCREENSHOT, openScreenshot);
            return this;
        }

        public FlutterWrapFragment build() {
            FlutterWrapFragment ret = new FlutterWrapFragment();
            ret.delegate = delegate;
            ret.setArguments(arguments);
            return ret;
        }
    }

    public static final String EXTRA_RESULT_KEY = "ext_result_key";
    public static final String EXTRA_FLUTTER_ROUTE = "ext_flutter_route";
    public static final String EXTRA_INITIALIZATION_ARGS = "ext_initialization_args";
    public static final String EXTRA_APP_BUNDLE_PATH = "ext_app_bundle_path";
    public static final String EXTRA_DART_ENTRYPOINT = "ext_dart_entrypoint";
    public static final String EXTRA_FLUTTERVIEW_RENDER_MODE = "ext_flutterview_render_mode";
    public static final String EXTRA_FLUTTERVIEW_TRANSPARENCY_MOD = "ext_flutterview_transparency_mode";
    public static final String EXTRA_ENABLE_SCREENSHOT = "ext_enable_screenshot";

    private static final int FLAG_ATTACH = 1;
    private static final int FLAG_ENGINE_INIT = 2;
    private static final int FLAG_PAGE_PUSHED = 8;
    private static final int MAX_REQUEST_CODE = 100;

    @Override
    public boolean isAttachToFlutter() {
        return hasFlag(flag, FLAG_ATTACH);
    }

    @Override
    public void attachFlutter() {
        localAttachFlutter();
    }

    @Override
    public void detachFlutter() {
        localDetachFlutter();
    }

    @Override
    public void requestDetachFromWindow() {
        detachFlutter();
    }

    @Override
    public void openNativePage(@NonNull NativeRouteOptions routeOptions, @NonNull MethodChannel.Result result) {
        onNativePageRoute(routeOptions, generateRequestCodeByChannel(result));
    }


    @Override
    public void openFlutterPage(@NonNull FlutterRouteOptions routeOptions, @NonNull MethodChannel.Result result) {
        onFlutterPageRoute(routeOptions, generateRequestCodeByChannel(result));
    }

    @Override
    public void finishNativePage(@Nullable Object result) {
        // 先让 flutter 脱离渲染
        detachFlutter();
        if (delegate != null) {
            // 走代理
            delegate.finishNativePage(this, result);
        } else {
            // 结束当前 native 页面，移除当前的 fragment
            FragmentManager fm = getFragmentManager();
            if (fm != null) {
                fm.beginTransaction()
                        .remove(this)
                        .commit();
            }
        }
    }

    @Override
    public String getNativePageId() {
        return nativePageId;
    }

    /**
     * 获取初始路由，如果是首次打开页面，需要通过 flutter 来主动获取路由信息
     */
    @Override
    public Map<String, Object> getInitRoute() {
        // 这里获取 initRoute 可以认为 push 了 page
        flag |= FLAG_PAGE_PUSHED;
        Map<String, Object> ret = new HashMap<>();
        if (routeOptions != null) {
            ret.put("pageName", routeOptions.pageName);
            if (routeOptions.args != null) {
                ret.put("args", routeOptions.args);
            }
        }
        ret.put("nativePageId", nativePageId);
        ret.put("isTab", isTab());
        return ret;
    }

    @Override
    public int generateRequestCodeByChannel(MethodChannel.Result result) {
        int requestCode = generateRequestCode();
        if (requestCode < 0) {
            result.error("-1", "Not enough request code, Fuck what??, map size:"
                    + resultChannelMap.size(), null);
            return requestCode;
        }
        resultChannelMap.put(requestCode, result);
        return requestCode;
    }

    @Override
    public int generateRequestCodeByCallback(@NonNull IPageResultCallback callback) {
        int requestCode = generateRequestCode();
        if (requestCode < 0) {
            callback.onPageResult(requestCode, Activity.RESULT_CANCELED, null);
        }
        pageCallbackMap.put(requestCode, callback);
        return requestCode;
    }

    @Override
    public void onFlutterRouteEvent(String name, int eventId, Map extra) {
    }

    @Override
    public boolean isTab() {
        if (delegate != null) {
            return delegate.isTab(this);
        }
        return true;
    }

    // 当前页面的 id
    protected final String nativePageId = FlutterManager.getInstance().generateNativePageId();
    /**
     * 当前 flutter 页面的 page name
     */
    protected FlutterRouteOptions routeOptions;
    @Nullable
    protected FixFlutterEngine flutterEngine;
    @Nullable
    protected FlutterView flutterView;
    @Nullable
    protected FixPlatformPlugin platformPlugin;
    protected FrameLayout container;
    protected View maskView;
    // 当前 native page 是否是首次启动的 flutter native page
    protected boolean isCreatePage;
    protected SparseArray<MethodChannel.Result> resultChannelMap = new SparseArray<>();
    protected SparseArray<IPageResultCallback> pageCallbackMap = new SparseArray<>();
    // 当前 fragment 不能处理的 delegate，比如页面结束，tab 判断，native 跳转动画
    protected IPageDelegate delegate;
    // 当前 flutter 的状态
    private int flag;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 混合栈中添加此页面
        FlutterManager.getInstance().addNativePage(this);
        // 解析参数
        IFlutterWrapConfig wrapConfig = FlutterManager.getInstance().getFlutterWrapConfig();
        if (routeOptions == null) {
            // 理由参数获取
            if (savedInstanceState != null) {
                routeOptions = savedInstanceState.getParcelable(EXTRA_FLUTTER_ROUTE);
            } else {
                Bundle arguments = getArguments();
                if (arguments != null && arguments.containsKey(EXTRA_FLUTTER_ROUTE)) {
                    routeOptions = arguments.getParcelable(EXTRA_FLUTTER_ROUTE);
                } else if (wrapConfig != null) {
                    routeOptions = wrapConfig.parseFlutterRouteFromBundle(this, getArguments());
                }
            }
        }
        // 解析是否开启截图缓存
        Bundle arguments = getArguments();
        if (arguments != null) {
            openScreenshot = arguments.getBoolean(EXTRA_ENABLE_SCREENSHOT, openScreenshot);
        }
        if (routeOptions == null) {
            // 如果没有路由信息，默认打开根路径
            routeOptions = FlutterRouteOptions.home;
        }
        if (FlutterManager.getInstance().getFlutterEngine() == null) {
            isCreatePage = true;
        }
        // init flutter
        try {
            Context context = getContext();
            assertNotNull(context);
            initializeFlutter(context);
            setupFlutterEngine(context);
            assertNotNull(flutterEngine);
            assertNotNull(platformPlugin);
            flag |= FLAG_ENGINE_INIT;
        } catch (Throwable t) {
            // engine init error
            t.printStackTrace();
            onFlutterInitFailure(t);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (!hasFlag(flag, FLAG_ENGINE_INIT)) {
            return null;
        }
        // 创建视图层
        Context context = getContext();
        assert context != null;
        // flutter 容器
        this.container = new FrameLayout(context);
        this.container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.MATCH_PARENT));
        // flutter 视图
        flutterView = createFlutterView(this.container);
        // 遮罩层
        maskView = createMaskView(this.container, getScreenshot());
        return this.container;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (hasFlag(flag, FLAG_ENGINE_INIT)) {
            // 链接到 flutter
            attachFlutter();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (hasFlag(flag, FLAG_ENGINE_INIT)) {
            // 检查链接到 flutter
            attachFlutter();
            // 主题修复
            innerPreFlutterApplyTheme();
            if (flutterEngine != null) {
                flutterEngine.getLifecycleChannel().appIsResumed();
            }
            if (platformPlugin != null) {
                // 此方法会同步 flutter 主题到 native
                platformPlugin.onPostResume();
            }
            innerPostFlutterApplyTheme();
            if (flutterView != null) {
                // 此处修复 flutter 和 native 沉浸式同步的问题
                ViewCompat.requestApplyInsets(flutterView);
            }
            // 通知 flutter，native page resume 了
            if (HybridRouterPlugin.isRegistered()) {
                HybridRouterPlugin.getInstance().onNativePageResumed(this);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (hasFlag(flag, FLAG_ATTACH)) {
            if (flutterEngine != null) {
                flutterEngine.getLifecycleChannel().appIsInactive();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (hasFlag(flag, FLAG_ATTACH)) {
            if (flutterEngine != null) {
                flutterEngine.getLifecycleChannel().appIsPaused();
            }
            // fragment 不可见，脱离 flutter 渲染
            detachFlutter();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (hasFlag(flag, FLAG_ATTACH)) {
            // 视图被销毁，脱离 flutter 渲染
            detachFlutter();
            flutterView = null;
            container = null;
            maskView = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 如果结束了，取消所有的结果
        cancelAllResult();
        // 从混合栈中移除 flutter manager
        FlutterManager.getInstance().removeNativePage(this);
        // 检查脱离 flutter
        detachFlutter();
        // 移除截图，如果有的话
        removeScreenShot();
        // 通知 flutter naitive 容器被移除
        if (HybridRouterPlugin.isRegistered()) {
            HybridRouterPlugin.getInstance().onNativePageFinished(nativePageId);
        }
        // 这里去掉打开页面的标记
        flag &= ~FLAG_PAGE_PUSHED;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (routeOptions != null) {
            outState.putParcelable(EXTRA_FLUTTER_ROUTE, routeOptions);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (flutterEngine != null) {
            flutterEngine.getActivityControlSurface()
                    .onActivityResult(requestCode, resultCode, data);
        }
        sendResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (flutterEngine != null) {
            flutterEngine.getActivityControlSurface()
                    .onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else {
            Log.w("FlutterWrapFragment",
                    "onRequestPermissionResult() invoked before FlutterFragment was attached to an Activity.");
        }
    }

    // ----------------------  以下方法需要 activity 主动调用 ---------------------
    public void onNewIntent(@NonNull Intent intent) {
        if (flutterEngine != null && isAttachToFlutter()) {
            // 这个方法有啥用?
            flutterEngine.getActivityControlSurface().onNewIntent(intent);
        }
    }

    public void onUserLeaveHint() {
        if (flutterEngine != null && isAttachToFlutter()) {
            // 这个方法有啥用?
            flutterEngine.getActivityControlSurface().onUserLeaveHint();
        }
    }

    public void onTrimMemory(int level) {
        if (this.flutterEngine != null) {
            if (level == TRIM_MEMORY_RUNNING_LOW) {
                flutterEngine.getSystemChannel().sendMemoryPressureWarning();
            }
        } else {
            Log.w("FlutterFragment", "onTrimMemory() invoked before FlutterFragment was attached to an Activity.");
        }

    }

    public void onLowMemory() {
        super.onLowMemory();
        if (this.flutterEngine != null) {
            flutterEngine.getSystemChannel().sendMemoryPressureWarning();
        }
    }

    /**
     * 初始化 flutter engine
     */
    protected void initializeFlutter(@NonNull Context context) {
        if (isCreatePage) {
            // 没有初始化的时候需要初始化
            Bundle arguments = getArguments();
            String[] flutterShellArgsArray = null;
            if (arguments != null) {
                flutterShellArgsArray = arguments.getStringArray(EXTRA_INITIALIZATION_ARGS);
            }
            FlutterShellArgs flutterShellArgs = new FlutterShellArgs(flutterShellArgsArray == null
                    ? new String[0] : flutterShellArgsArray);
            FlutterMain.startInitialization(context.getApplicationContext());
            FlutterMain.ensureInitializationComplete(context.getApplicationContext(),
                    flutterShellArgs.toArray());
        }
    }

    /**
     * 启动 dart 虚拟机
     */
    protected void doInitialFlutterViewRun() {
        assertNotNull(flutterEngine != null);
        if (!flutterEngine.getDartExecutor().isExecutingDart()) {
            DartExecutor.DartEntrypoint entrypoint = new DartExecutor.DartEntrypoint(
                    getResources().getAssets(),
                    getAppBundlePath(),
                    getEntrypoint()
            );
            flutterEngine.getDartExecutor().executeDartEntrypoint(entrypoint);
        }
    }

    protected void setupFlutterEngine(@NonNull Context context) {
        if (flutterEngine == null) {
            flutterEngine = FlutterManager.getInstance().getOrCreateFlutterEngine(context);
        }
        if (platformPlugin == null) {
            platformPlugin = FlutterManager.getInstance().getOrCreatePlatformPlugin();
        }
    }

    protected void onNativePageRoute(NativeRouteOptions routeOptions, int requestCode) {
        IFlutterWrapConfig wrapConfig = FlutterManager.getInstance().getFlutterWrapConfig();
        if (wrapConfig == null || !wrapConfig.onNativePageRoute(this,
                routeOptions, requestCode)) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(routeOptions.url));
            FlutterStackManagerUtil.updateIntent(intent, routeOptions.args);
            startActivityForResult(intent, requestCode);
        }
    }

    /**
     * 请求打开 新的 flutter page，借助新的 native page 容器
     *
     * @param routeOptions
     * @param requestCode
     */
    protected void onFlutterPageRoute(FlutterRouteOptions routeOptions, int requestCode) {
        IFlutterWrapConfig wrapConfig = FlutterManager.getInstance().getFlutterWrapConfig();
        if (wrapConfig == null || !wrapConfig.onFlutterPageRoute(this,
                routeOptions, requestCode)) {
            if (getContext() != null) {
                Intent intent = FlutterWrapActivity.startIntent(getContext(), routeOptions);
                startActivityForResult(intent, requestCode);
            } else {
                throw new IllegalStateException("The context of current fragment is null");
            }
        }
    }

    // 引擎初始化失败
    protected void onFlutterInitFailure(Throwable t) {
    }

    /**
     * 注册插件
     */
    protected void onRegisterPlugin(PluginRegistry pluginRegistry) {
        FlutterManager.getInstance().registerPlugins(pluginRegistry);
    }

    protected void preFlutterApplyTheme() {
    }

    protected void postFlutterApplyTheme() {
    }

    /**
     * Returns the desired {@link FlutterView.RenderMode} for the {@link FlutterView} displayed in
     * this {@code FlutterFragment}.
     *
     * Defaults to {@link FlutterView.RenderMode#surface}.
     */
    @NonNull
    protected FlutterView.RenderMode getRenderMode() {
        Bundle arguments = getArguments();
        String renderModeName = FlutterView.RenderMode.texture.name();
        if (arguments != null) {
            renderModeName = arguments.getString(EXTRA_FLUTTERVIEW_RENDER_MODE, renderModeName);
        }
        return FlutterView.RenderMode.valueOf(renderModeName);
    }

    /**
     * Returns the desired {@link FlutterView.TransparencyMode} for the {@link FlutterView} displayed in
     * this {@code FlutterFragment}.
     * <p>
     * Defaults to {@link FlutterView.TransparencyMode#transparent}.
     */
    @NonNull
    protected FlutterView.TransparencyMode getTransparencyMode() {
        Bundle arguments = getArguments();
        String transparencyModeName = FlutterView.TransparencyMode.opaque.name();
        if (arguments != null) {
            transparencyModeName= arguments.getString(EXTRA_FLUTTERVIEW_TRANSPARENCY_MOD, transparencyModeName);
        }
        return FlutterView.TransparencyMode.valueOf(transparencyModeName);
    }

    @NonNull
    protected FlutterView createFlutterView(ViewGroup container) {
        return new FlutterView(container.getContext(), getRenderMode(), getTransparencyMode());
    }

    // 创建遮罩层
    protected View createMaskView(ViewGroup container, Bitmap screenshot) {
        View background = new View(container.getContext());
        background.setClickable(true);
        return background;
    }

    /**
     * 更新遮罩层内容
     * @param maskView 遮罩层视图
     * @param screenshot 截图，可能为 null
     */
    protected void updateMaskScreenshot(View maskView, @Nullable Bitmap screenshot) {
        if (screenshot != null) {
            maskView.setBackground(new BitmapDrawable(getResources(), screenshot));
        } else {
            maskView.setBackground(getLaunchScreenDrawableFromActivityTheme());
        }
    }

    /**
     * 移除遮罩层的 screen shot
     * @param maskView
     */
    protected void removeMaskScreenshot(View maskView) {
        if (maskView.getBackground() instanceof  BitmapDrawable) {
            maskView.setBackground(getLaunchScreenDrawableFromActivityTheme());
        }
    }

    /**
     * 配置flutter 页面
     */
    protected void setupFlutterView(ViewGroup container, final FlutterView flutterView,
                                                    View maskView) {
        // 这里先移除不是当前 flutter view 的 flutter view，因为 flutter view 只有在创建的时候才会 attach
        // 所以在 attach 的时候会重新创建
        for (int i = 0; i < container.getChildCount(); ++i) {
            View view = container.getChildAt(i);
            if (view instanceof FlutterView && view != flutterView) {
                container.removeViewAt(i);
            }
        }
        if (flutterView.getParent() != container) {
            // add flutter view to root view
            ViewGroup.LayoutParams lp = flutterView.getLayoutParams();
            lp = lp == null ? new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT) : lp;
            container.addView(flutterView, lp);
        }
        if (maskView.getParent() != container) {
            ViewGroup.LayoutParams lp = maskView.getLayoutParams();
            lp = lp == null ? new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT) : lp;
            container.addView(maskView, lp);
        }
        // 更新遮罩层
        updateMaskScreenshot(maskView, getScreenshot());

        final View finalMaskView = maskView;
        finalMaskView.setVisibility(View.VISIBLE);
        flutterView.addOnFirstFrameRenderedListener(new OnFirstFrameRenderedListener() {
            @Override
            public void onFirstFrameRendered() {
                finalMaskView.setVisibility(View.INVISIBLE);
                // flutter 可见，如果可能，移除截图
                removeScreenShot();
            }
        });
    }

    /**
     * 发送 onActivityResult 获取到的结果
     */
    protected void sendResult(int requestCode, int resultCode, @Nullable Intent data) {
        MethodChannel.Result resultChannel = resultChannelMap.get(requestCode);
        if (resultChannel != null) {
            resultChannelMap.remove(requestCode);
            HashMap<String, Object> ret = new HashMap<>();
            ret.put("resultCode", resultCode);
            Object result = null;
            Bundle extras = data == null ? null : data.getExtras();
            if (extras != null && data.hasExtra(EXTRA_RESULT_KEY)) {
                // 从 flutter 过来的数据
                result = extras.get(EXTRA_RESULT_KEY);
            } else if (extras != null){
                HashMap<String, Object> retMap = new HashMap<>();
                if (data.getExtras() != null) {
                    for (String key : data.getExtras().keySet()) {
                        retMap.put(key, data.getExtras().get(key));
                    }
                }
                result = retMap;
            }
            ret.put("data", result);
            resultChannel.success(ret);
        }
        IPageResultCallback callback = pageCallbackMap.get(requestCode);
        if (callback != null) {
            pageCallbackMap.remove(requestCode);
            callback.onPageResult(requestCode, resultCode, data);
        }
    }

    /**
     * 结束所有的 result channel，如果当前页面有打开一个新的页面的请求，但是页面请求没有返回结果
     * 这里手动结束掉所有的 result 回调
     */
    protected void cancelAllResult() {
        for (int i = 0, size = resultChannelMap.size(); i < size; ++i) {
            MethodChannel.Result result = resultChannelMap.valueAt(i);
            HashMap<String, Object> ret = new HashMap<>();
            ret.put("resultCode", Activity.RESULT_CANCELED);
            result.success(ret);
        }
        resultChannelMap.clear();
        for (int i = 0, size = pageCallbackMap.size(); i < size; ++i) {
            IPageResultCallback callback = pageCallbackMap.valueAt(i);
            callback.onPageResult(pageCallbackMap.keyAt(i), Activity.RESULT_CANCELED, null);
        }
        pageCallbackMap.clear();
    }

    protected String getAppBundlePath() {
        Bundle arguments = getArguments();
        String appBundlePath = getContext() == null ? null
                : FlutterMain.findAppBundlePath(getContext().getApplicationContext());
        if (arguments != null) {
            appBundlePath = arguments.getString(EXTRA_APP_BUNDLE_PATH, appBundlePath);
        }
        return appBundlePath;
    }

    protected String getEntrypoint() {
        Bundle arguments = getArguments();
        String entrypoint = "main";
        if (arguments != null) {
            entrypoint = arguments.getString(EXTRA_DART_ENTRYPOINT, entrypoint);
        }
        return entrypoint;
    }

    private void innerPreFlutterApplyTheme() {
        IFlutterWrapConfig wrapConfig = FlutterManager.getInstance().getFlutterWrapConfig();
        if (wrapConfig != null) {
            wrapConfig.preFlutterApplyTheme(this);
        }
        preFlutterApplyTheme();
    }

    private void innerPostFlutterApplyTheme() {
        IFlutterWrapConfig wrapConfig = FlutterManager.getInstance().getFlutterWrapConfig();
        if (wrapConfig != null) {
            wrapConfig.postFlutterApplyTheme(this);
        }
        postFlutterApplyTheme();
    }

    private boolean hasFlag(int flag, int target) {
        return (flag & target) == target;
    }

    private int generateRequestCode() {
        if (resultChannelMap.size() == 0) {
            return 1;
        }
        int count = 0;
        int start = resultChannelMap.keyAt(resultChannelMap.size() - 1) + 1;
        while (count < MAX_REQUEST_CODE) {
            start = (start > MAX_REQUEST_CODE) ? (start - MAX_REQUEST_CODE) : start;
            if (resultChannelMap.get(start) == null) {
                // found request code
                return start;
            }
            count++;
        }
        // failure to generate
        return -1;
    }

    private void localAttachFlutter() {
        if (hasFlag(flag, FLAG_ATTACH) || !hasFlag(flag, FLAG_ENGINE_INIT)) {
            return;
        }
        final IFlutterNativePage curNativePage = FlutterManager.getInstance().getCurNativePage();
        if (curNativePage != null) {
            curNativePage.detachFlutter();
        }
        flag |= FLAG_ATTACH;
        // 设置当前页面是 native page
        FlutterManager.getInstance().setCurNativePage(this);
        // null check
        assertNotNull(container);
        assertNotNull(maskView);
        assertNotNull(flutterEngine);
        assertNotNull(flutterView);
        assertNotNull(platformPlugin);
        // attach plugin registry
        flutterEngine.getActivityControlSurface().attachToActivity(getActivity(), getLifecycle());
        // attach platform plugin
        platformPlugin.attach(getActivity(), flutterEngine.getPlatformChannel());
        // register plugin
        if (!FlutterManager.getInstance().isPluginRegistry()) {
            onRegisterPlugin(flutterEngine.getFixPluginRegistry());
        }
        flutterEngine.getFixPluginRegistry().attach(getActivity());
        // attach flutter view to engine
        flutterView.attachToFlutterEngine(flutterEngine);
        setupFlutterView(container, flutterView, maskView);
        if (FlutterManager.getInstance().isFlutterRouteStart() && !hasFlag(flag, FLAG_PAGE_PUSHED)) {
            flag |= FLAG_PAGE_PUSHED;
            HybridRouterPlugin.getInstance().pushFlutterPager(routeOptions.pageName,
                    routeOptions.args, nativePageId, isTab(), null);
        }
        // 检查 dart 是否执行
        doInitialFlutterViewRun();
    }

    private void localDetachFlutter() {
        if (!hasFlag(flag, FLAG_ATTACH) || !hasFlag(flag, FLAG_ENGINE_INIT)) {
            return;
        }
        flag &= ~FLAG_ATTACH;
        assertNotNull(flutterView);
        assertNotNull(flutterEngine);
        assertNotNull(platformPlugin);
        // 如果可能，detach 前保存截图
        saveScreenshot();
        // 生命周期通知
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            flutterEngine.getActivityControlSurface().onUserLeaveHint();
            flutterEngine.getLifecycleChannel().appIsInactive();
        }
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            flutterEngine.getLifecycleChannel().appIsPaused();
        }
        //
//        FlutterStackManagerUtil.detachFlutterFromEngine(flutterView, flutterEngine);
        flutterView.detachFromFlutterEngine();
        flutterEngine.getFixPluginRegistry().detach();
        platformPlugin.detach();
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

    // ================ 截图管理 =================
    private static ScreenshotManager screenshotManager;
    // 是否开启截图
    private boolean openScreenshot = false;

    @Nullable
    private Bitmap getScreenshot() {
        if (!openScreenshot) {
            return null;
        }
        ScreenshotManager screenshotManager = checkScreenshotManager();
        if (screenshotManager == null) return null;
        return screenshotManager.getBitmap(nativePageId);
    }

    private void saveScreenshot() {
        // 如果没有开启保存截图，直接退出
        if (!openScreenshot) {
            return;
        }
        ScreenshotManager screenshotManager = checkScreenshotManager();
        if (flutterEngine != null && screenshotManager != null) {
            Bitmap bitmap = flutterEngine.getFlutterBitmap();
            if (bitmap != null) {
                screenshotManager.addBitmap(nativePageId, bitmap);
            }
        }
    }

    private void removeScreenShot() {
        // 同时移除 mask view 的遮罩
        if (maskView != null) {
            removeMaskScreenshot(maskView);
        }
        ScreenshotManager screenshotManager = checkScreenshotManager();
        if (screenshotManager != null) {
            screenshotManager.removeCache(nativePageId);
        }
    }

    @Nullable
    private ScreenshotManager checkScreenshotManager() {
        if (screenshotManager != null) return screenshotManager;
        if (getContext() != null) {
            screenshotManager = new ScreenshotManager(getContext());
        }
        return null;
    }
}
