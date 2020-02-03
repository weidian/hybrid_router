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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vdian.flutter.hybridrouter.FlutterManager;
import com.vdian.flutter.hybridrouter.FlutterStackManagerUtil;
import com.vdian.flutter.hybridrouter.HybridRouterPlugin;
import com.vdian.flutter.hybridrouter.engine.FixFlutterView;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.android.FlutterView;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.embedding.engine.renderer.FlutterUiDisplayListener;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformPlugin;
import io.flutter.view.FlutterMain;

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
 * @since 2019-10-16 15:51
 */

public class FlutterNativePageDelegate {

    /**
     * 获取 flutter 返回的页面结果
     */
    public static Object getFlutterResult(@NonNull Intent data) {
        if (data.getExtras() == null) return null;
        return data.getExtras().get(ARG_RESULT_KEY);
    }

    private static final int FLAG_ATTACH = 1;
    private static final int FLAG_ENGINE_INIT = 2;
    private static final int FLAG_PUSH_PAGE = 4;

    private static final int FLAG_CREATED = 1;
    private static final int FLAG_STARTED = 2;
    private static final int FLAG_RESUMED = 4;

    private static final int MAX_REQUEST_CODE = 100;

    private static final String TAG = "FlutterDelegate";
    private static final String ARG_RESULT_KEY = "arg_flutter_result";
    private static final String ARG_SAVED_START_ROUTE_OPTIONS = "arg_flutter_saved_start_route_options";

    // 这里记录 resumed 的 delegate 列表，在 android 10 上，可能会有多个 activity 同时 resume 的情况
    private static final List<FlutterNativePageDelegate> resumedDelegate = new LinkedList<>();

    private final IFlutterNativePage page;
    private final String nativePageId = FlutterManager.getInstance().generateNativePageId();

    private FlutterRouteOptions routeOptions;
    @Nullable
    private FlutterEngine flutterEngine;
    @Nullable
    private FlutterView flutterView;
    private FlutterSplashView flutterSplashView;
    private PlatformPlugin platformPlugin;
    private SparseArray<MethodChannel.Result> resultChannelMap = new SparseArray<>();
    private SparseArray<IPageResultCallback> pageCallbackMap = new SparseArray<>();
    // 当前 flutter attach 的状态
    private int attachFlag;
    // 当前 delegate 生命周期 flag
    private int lifecycleFlag;

    // 因为 engine 没有 attach 收到的信息
    private Intent pendingNewIntent;
    private PendingActivityResult pendingActivityResult;
    private PendingPermissionResult pendingPermissionResult;

    public FlutterNativePageDelegate(IFlutterNativePage page) {
        this.page = page;
    }

    /**
     * Current native page's page id
     *
     * @return
     */
    public String getNativePageId() {
        return nativePageId;
    }

    /**
     * If the native container is attach to flutter
     *
     * @return
     */
    public boolean isAttachToFlutter() {
        return hasFlag(attachFlag, FLAG_ATTACH);
    }

    /**
     * 获取初始路由，如果是首次打开页面，需要通过 flutter 来主动获取路由信息
     */
    public Map<String, Object> getInitRoute() {
        // 这里获取 initRoute 可以认为 push 了 page
        attachFlag |= FLAG_PUSH_PAGE;
        Map<String, Object> ret = new HashMap<>();
        if (routeOptions != null) {
            ret.put("pageName", routeOptions.pageName);
            if (routeOptions.args != null) {
                ret.put("args", routeOptions.args);
            }
        }
        ret.put("nativePageId", nativePageId);
        ret.put("isTab", page.isTab());
        return ret;
    }

    // ==================== open page manager====================

    /**
     * 打开一个新的 native 页面
     * 此方法会优先判断 {@link IFlutterWrapConfig#onNativePageRoute(IFlutterNativePage, NativeRouteOptions, int)}
     * 如果上述方法返回 true ，则不处理
     */
    public void openNativePage(@NonNull NativeRouteOptions routeOptions,
                               @NonNull MethodChannel.Result result) {
        onNativePageRoute(routeOptions, generateRequestCodeByChannel(result));
    }


    /**
     * 通过 native 打开一个新的 flutter 页面
     * 此方法会优先判断 {@link IFlutterWrapConfig#onFlutterPageRoute(IFlutterNativePage, FlutterRouteOptions, int)}
     * 如果上述方法返回 true ，则不处理
     */
    public void openFlutterPage(@NonNull FlutterRouteOptions routeOptions,
                                @NonNull MethodChannel.Result result) {
        onFlutterPageRoute(routeOptions, generateRequestCodeByChannel(result));
    }

    /**
     * 通过 result 生成一个对应的 result code
     *
     * @param result
     * @return
     */
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

    public int generateRequestCodeByCallback(@NonNull IPageResultCallback callback) {
        int requestCode = generateRequestCode();
        if (requestCode < 0) {
            callback.onPageResult(requestCode, Activity.RESULT_CANCELED, null);
        }
        pageCallbackMap.put(requestCode, callback);
        return requestCode;
    }

    /**
     * 设置 activity 的页面结果
     */
    public void setPageResult(@Nullable Object result) {
        Activity activity = page.getActivity();
        if (activity != null) {
            if (result == null) {
                activity.setResult(Activity.RESULT_OK);
            } else {
                Intent data = new Intent();
                FlutterStackManagerUtil.updateIntent(data, ARG_RESULT_KEY, result);
                activity.setResult(Activity.RESULT_OK, data);
            }
        }
    }

    // ==================== 生命周期回调 ====================

    /**
     * Invoke this method from {@code Activity#onCreate(Bundle)} or {@code Fragment#onCreate(Context)}
     *
     * @param saveInstanceState
     */
    public void onCreate(@Nullable Bundle saveInstanceState) {
        Log.v(TAG, "onCreate(): " + nativePageId);
        lifecycleFlag |= FLAG_CREATED;
        // 添加页面到混合栈管理
        FlutterManager.getInstance().addNativePage(page);
        // 解析参数
        if (saveInstanceState != null) {
            routeOptions = saveInstanceState.getParcelable(ARG_SAVED_START_ROUTE_OPTIONS);
        }
        if (routeOptions == null) {
            routeOptions = page.getStartRouteOptions();
        }
        if (routeOptions == null) {
            IFlutterWrapConfig config = FlutterManager.getInstance().getFlutterWrapConfig();
            if (config != null) {
                routeOptions = config.parseFlutterRouteFromBundle(page);
            }
        }

        // 如果没有启动页面信息，从 home 启动
        routeOptions = routeOptions == null ? FlutterRouteOptions.home : routeOptions;

        Context context = page.getContext();
        try {
            assertNotNull(context);
            initializeFlutter(context);
            setupFlutterEngine(context);
            assertNotNull(flutterEngine);

            // hook to configure engine.
            if (page instanceof IFlutterHook) {
                ((IFlutterHook) page).configureFlutterEngine(flutterEngine);
            }

            attachFlag |= FLAG_ENGINE_INIT;
        } catch (Throwable t) {
            // engine init error
            t.printStackTrace();
            if (page instanceof IFlutterHook) {
                ((IFlutterHook) page).onFlutterInitFailure(t);
            }
        }
    }

    /**
     * 用于保存数据
     * 这里保存了初始路由信息 {@link #routeOptions}
     */
    public void onSaveInstance(@NonNull Bundle outState) {
        Log.v(TAG, "onSaveInstance(): " + nativePageId);
        if (routeOptions != null) {
            outState.putParcelable(ARG_SAVED_START_ROUTE_OPTIONS, routeOptions);
        }
    }

    /**
     * 创建带 SplashScreen 的 {@link FlutterSplashView}
     *
     * @return {@link FlutterSplashView}
     */
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(TAG, "Creating FlutterView: " + nativePageId);
        if (!hasFlag(attachFlag, FLAG_ENGINE_INIT)) {
            Log.e(TAG, "Engine init failure, return null view.");
            // engine init is failure, return null
            return null;
        }

        Context context = page.getContext();
        assertNotNull(context);
        flutterView = new FixFlutterView(context, page.getRenderMode(),
                page.getTransparencyMode());
        flutterView.addOnFirstFrameRenderedListener(firstFrameListener);

        flutterSplashView = new FlutterSplashView(context);
        flutterSplashView.displayFlutterViewWithSplash(flutterView, page.provideSplashScreen());
        return flutterSplashView;
    }

    public void onStart() {
        Log.v(TAG, "onStart(): " + nativePageId);
        if (!hasFlag(lifecycleFlag, FLAG_CREATED)) {
            throw new IllegalStateException("Call onStart before onCreate");
        }
        lifecycleFlag |= FLAG_STARTED;
        if (hasFlag(attachFlag, FLAG_ENGINE_INIT)) {
            assertNotNull(flutterView);
            assertNotNull(flutterEngine);
            flutterView.post(new Runnable() {
                @Override
                public void run() {
                    if (hasFlag(lifecycleFlag, FLAG_STARTED)) {
                        // 如果在 onStart 的时候 attachEngine，会让 FlutterView
                        // sendViewportMetricsToFlutter，导致 flutter 端收到 width=0 和 height = 0 的
                        // viewportMetrics
                        Log.v(TAG, "Attaching FlutterEngine to FlutterView.");
                        attachEngine();
                    }
                }
            });
        }
    }

    public void onResume() {
        Log.v(TAG, "onResume(): " + nativePageId);
        if (!hasFlag(lifecycleFlag, FLAG_CREATED|FLAG_STARTED)) {
            throw new IllegalStateException("Call onResume before onCreate or onStart; current flag: " + lifecycleFlag);
        }
        lifecycleFlag |= FLAG_RESUMED;
        addToResumeList();
        if (hasFlag(attachFlag, FLAG_ENGINE_INIT)) {

            // 通知 flutter native page resume 了
            if (HybridRouterPlugin.isRegistered()) {
                HybridRouterPlugin.getInstance().onNativePageResumed(page);
            }

            // resume 逻辑处理放到 post 中
            assertNotNull(flutterView);
            resumeEngine();
        }
    }

    // 在 onResume 中使用，这里去掉了 post resume，原代码是有 post 的，google 官方也不知道为啥要加 post
    private void resumeEngine() {
        if (!hasFlag(attachFlag, FLAG_ENGINE_INIT)) {
            return;
        }
        if (hasFlag(lifecycleFlag, FLAG_STARTED)) {
            // make sure the flutter is attach
            attachEngine();
        }
        if (hasFlag(lifecycleFlag, FLAG_RESUMED)) {
            Log.v(TAG, "Resume flutter engine");
            // if is attach, resume the flutter engine
            assertNotNull(flutterEngine);
            flutterEngine.getLifecycleChannel().appIsResumed();

            if (platformPlugin != null) {
                platformPlugin.updateSystemUiOverlays();
                if (flutterView != null &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                    // 用于修复 tab 切换时候状态栏 inset 变化的问题
                    flutterView.requestApplyInsets();
                }

                // hook update system ui overlays
                // 可以用来自定义系统状态栏样式
                IFlutterWrapConfig wrapConfig = FlutterManager.getInstance().getFlutterWrapConfig();
                if (wrapConfig != null) {
                    wrapConfig.postFlutterApplyTheme(page);
                }
                if (page instanceof IFlutterHook) {
                    ((IFlutterHook) page).afterUpdateSystemUiOverlays(flutterView);
                }
            }
        }
    }

    public void onPause() {
        Log.v(TAG, "onPause(): " + nativePageId);
        if (!hasFlag(lifecycleFlag, FLAG_RESUMED)) {
            throw new IllegalStateException("Call onPause before onResume; current flag: " + lifecycleFlag);
        }
        lifecycleFlag &= ~FLAG_RESUMED;
        if (hasFlag(attachFlag, FLAG_ATTACH)) {
            assertNotNull(flutterEngine);
            Log.v(TAG, "Inactive flutter engine");
            flutterEngine.getLifecycleChannel().appIsInactive();
        }
        removeFromResumeList();
    }

    public void onStop() {
        Log.v(TAG, "onStop(): " + nativePageId);
        if (!hasFlag(lifecycleFlag, FLAG_STARTED)) {
            throw new IllegalStateException("Call onPause before onStart; current flag: " + lifecycleFlag);
        }
        lifecycleFlag &= ~FLAG_STARTED;
        if (hasFlag(attachFlag, FLAG_ATTACH)) {
            assertNotNull(flutterEngine);
            Log.v(TAG, "Paused flutter engine");
            flutterEngine.getLifecycleChannel().appIsPaused();

            Log.v(TAG, "Detach flutter engine from flutter view.");
            // page is invisible, detach flutter engine
            detachEngine();
        }
    }

    public void onDestroyView() {
        Log.v(TAG, "onDestroyView(): " + nativePageId);
        if (flutterView != null) {
            flutterView.removeOnFirstFrameRenderedListener(firstFrameListener);
            flutterView = null;
        }
    }

    public void onDestroy() {
        Log.v(TAG, "onDestroy(): " + nativePageId);
        if (!hasFlag(lifecycleFlag, FLAG_CREATED)) {
            throw new IllegalStateException("Call onPause before onCreate; current flag: " + lifecycleFlag);
        }
        lifecycleFlag = 0;
        // 清理 pending action
        clearPendingAction();
        // 如果结束了，取消所有的结果
        cancelAllResult();
        // 从 manager 中移除当前页
        FlutterManager.getInstance().removeNativePage(page);
        if (hasFlag(attachFlag, FLAG_ENGINE_INIT)) {
            if (page instanceof IFlutterHook) {
                ((IFlutterHook) page).cleanUpFlutterEngine(flutterEngine);
            }
        }
        // 通知 flutter native 容器被移除
        if (HybridRouterPlugin.isRegistered()) {
            HybridRouterPlugin.getInstance().onNativePageFinished(nativePageId);
        }
        flutterEngine = null;
        attachFlag = 0;
    }

    /**
     * Call from {@code Activity#onActivtyResult(int, int, Intent)} or
     * {@code Fragment#onActivtyResult(int, int, Intent)}
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (flutterEngine != null && isAttachToFlutter()) {
            flutterEngine.getActivityControlSurface().onActivityResult(requestCode, resultCode, data);
        } else {
            pendingActivityResult = new PendingActivityResult(requestCode, resultCode, data);
            Log.w(TAG,
                    "onRequestPermissionResult() invoked before FlutterFragment was attached to an Activity.");
        }
        sendResult(requestCode, resultCode, data);
    }

    /**
     * Call from {@code Activity#onActivtyResult(int, String[], int[])} or
     * {@code Fragment#onRequestPermissionsResult(int, String[], int[])}
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (flutterEngine != null && isAttachToFlutter()) {
            flutterEngine.getActivityControlSurface()
                    .onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else {
            pendingPermissionResult = new PendingPermissionResult(requestCode, permissions,
                    grantResults);
            Log.w(TAG,
                    "onRequestPermissionResult() invoked before FlutterFragment was attached to an Activity.");
        }
    }

    /**
     * Call from {@code Activity#onNewIntent}
     * {@code Fragment#onNewIntent}
     */
    public void onNewIntent(@NonNull Intent intent) {
        Log.v(TAG, "onNewIntent(): " + nativePageId);
        if (isAttachToFlutter() && flutterEngine != null) {
            flutterEngine.getActivityControlSurface().onNewIntent(intent);
        } else {
            pendingNewIntent = intent;
            Log.w(TAG, "onNewIntent() invoked before flutter engine is attached.");
        }
    }

    /**
     * Call from {@code Activity#onUserLeaveHint}
     */
    public void onUserLeaveHint() {
        Log.v(TAG, "onUserLeaveHint");
        if (flutterEngine != null && isAttachToFlutter()) {
            flutterEngine.getActivityControlSurface().onUserLeaveHint();
        } else {
            Log.w(TAG, "onNewIntent() invoked before flutter engine is attached.");
        }
    }

    /**
     * Call from {@code Activity#onTrimMemory}
     */
    public void onTrimMemory(int level) {
        Log.v(TAG, "onTrimMemory(): " + nativePageId);
        if (flutterEngine != null) {
            if (level == TRIM_MEMORY_RUNNING_LOW) {
                flutterEngine.getSystemChannel().sendMemoryPressureWarning();
            }
        } else {
            Log.w(TAG, "onTrimMemory() invoked flutter engine is null.");
        }
    }

    /**
     * Call from {@code Activity#onLowMemory}
     * {@code Fragment#onLowMemory}
     */
    public void onLowMemory() {
        Log.v(TAG, "onLowMemory(): " + nativePageId);
        if (flutterEngine != null) {
            flutterEngine.getSystemChannel().sendMemoryPressureWarning();
        } else {
            Log.w(TAG, "onLowMemory() invoked flutter engine is null.");
        }
    }

    /**
     * 返回按钮点击
     *
     * @param nativeBackPressed native 测的返回按钮事件，比如 {@link Activity#onBackPressed()}
     */
    public void onBackPressed(final Runnable nativeBackPressed) {
        if (HybridRouterPlugin.isRegistered()) {
            HybridRouterPlugin.getInstance().onBackPressed(new MethodChannel.Result() {
                @Override
                public void success(@Nullable Object o) {
                    if (!(o instanceof Boolean) || !((Boolean) o)) {
                        // 如果返回类型不是 boolean ，或者返回了 false
                        nativeBackPressed.run();
                    }
                }

                @Override
                public void error(String s, @Nullable String s1, @Nullable Object o) {
                    nativeBackPressed.run();
                }

                @Override
                public void notImplemented() {
                    nativeBackPressed.run();
                }
            });
        } else {
            nativeBackPressed.run();
        }
    }

    // ==================== engine attach and detach====================

    /**
     * Attach to the engine
     * 会进行以下步骤：
     * - 设置 {@link FlutterManager} 的当前 {@link IFlutterNativePage} 页面为当前页面
     * - Attach {@link FlutterEngine#getActivityControlSurface()}
     * - Create {@link #platformPlugin}
     * - Registered plugins
     * - Attach flutter engine to FlutterView
     * - Run dart VM or push start page
     */
    public void attachEngine() {
        if (hasFlag(attachFlag, FLAG_ATTACH) || !hasFlag(attachFlag, FLAG_ENGINE_INIT)) {
            return;
        }
        Log.v(TAG, "startAttachEngine");
        final IFlutterNativePage curNativePage = FlutterManager.getInstance().getCurNativePage();
        if (curNativePage != null) {
            curNativePage.detachFlutter();
        }
        // 这里的 attachFlag 设置必须在 上面 detachFlutter call 之后
        attachFlag |= FLAG_ATTACH;
        // 把当前页面设置成 native 页面
        FlutterManager.getInstance().setCurNativePage(page);
        // null check
        assertNotNull(flutterEngine);
        assertNotNull(page.getActivity());
        assertNotNull(flutterView);
        // attach plugin registry to activity
        flutterEngine.getActivityControlSurface().attachToActivity(page.getActivity(),
                page.getLifecycle());
        // platform plugin is retain by engine, so should attach and detach
        platformPlugin = page.providePlatformPlugin(page.getActivity(), flutterEngine);
        // register plugin
        if (!FlutterManager.getInstance().isPluginRegistry()) {
            FlutterManager.getInstance().registerPlugins(
                    FlutterManager.getInstance().getShimPluginRegistry());
            if (page instanceof IFlutterHook) {
                ((IFlutterHook) page).onRegisterPlugin(
                        FlutterManager.getInstance().getShimPluginRegistry());
            }
        }
        // attach flutter view to engine
        flutterView.attachToFlutterEngine(flutterEngine);
        // 可能的话开始执行 dart
        doInitialRunOrPushPage();
        // hook method
        if (page instanceof IFlutterHook) {
            ((IFlutterHook) page).afterFlutterViewAttachToEngine(flutterView, flutterEngine);
        }
        // do pending action
        checkPendingAction();
    }

    /**
     * detach from engine
     * 会进行以下步骤：
     * - 如果生命周期是 resumed 的，调用: {@link #onUserLeaveHint()} 和
     * {@code flutterEngine.getLifecycleChannel().appIsInactive()}
     * - 如果生命周期是 started 的，调用 {@code flutterEngine.getLifecycleChannel().appIsPaused()}
     * - 如果不是 configurations change 造成的 detach
     * 执行 Detach {@link FlutterEngine#getActivityControlSurface()}
     * - 如果是 configurations change 造成的 detach
     * 执行 {@code flutterEngine.getActivityControlSurface().detachFromActivityForConfigChanges()}
     * - Detach flutter engine from FlutterView
     * - Destroy {@link #platformPlugin}
     */
    public void detachEngine() {
        if (!hasFlag(attachFlag, FLAG_ATTACH) || !hasFlag(attachFlag, FLAG_ENGINE_INIT)) {
            return;
        }
        attachFlag &= ~FLAG_ATTACH;
        // null check
        assertNotNull(flutterView);
        assertNotNull(flutterEngine);
        assertNotNull(platformPlugin);
        // hook method
        if (page instanceof IFlutterHook) {
            ((IFlutterHook) page).beforeFlutterViewDetachFromEngine(flutterView, flutterEngine);
        }
        // 生命周期感知
        if (hasFlag(lifecycleFlag, FLAG_RESUMED)) {
            flutterEngine.getActivityControlSurface().onUserLeaveHint();
            flutterEngine.getLifecycleChannel().appIsInactive();
        }
        if (hasFlag(lifecycleFlag, FLAG_STARTED)) {
            flutterEngine.getLifecycleChannel().appIsPaused();
        }
        Activity activity = page.getActivity();
        if (activity != null && activity.isChangingConfigurations()) {
            flutterEngine.getActivityControlSurface().detachFromActivityForConfigChanges();
        } else {
            flutterEngine.getActivityControlSurface().detachFromActivity();
            // 这里不这么做会内存泄漏，因为 ShimPluginRegistry 没有在 onDetachFromActivity 的时候
            // 把 activityPluginBinding 置为空
            FlutterStackManagerUtil.detachShimPluginRegistryFromActivity(FlutterManager
                    .getInstance().getShimPluginRegistry());
        }
        // detach flutter engine from flutterView
        flutterView.detachFromFlutterEngine();
        // 修复内存泄漏
        FlutterStackManagerUtil.detachFlutterFromEngine(flutterView, flutterEngine);
        platformPlugin.destroy();
        platformPlugin = null;
    }

    /**
     * 处理 pending action
     */
    private void checkPendingAction() {
        if (isAttachToFlutter() && flutterEngine != null) {
            if (pendingNewIntent != null) {
                flutterEngine.getActivityControlSurface().onNewIntent(pendingNewIntent);
                pendingNewIntent = null;
            }

            if (pendingActivityResult != null) {
                flutterEngine.getActivityControlSurface().onActivityResult(
                        pendingActivityResult.requestCode,
                        pendingActivityResult.resultCode,
                        pendingActivityResult.data
                );
                pendingPermissionResult = null;
            }

            if (pendingPermissionResult != null) {
                flutterEngine.getActivityControlSurface().onRequestPermissionsResult(
                        pendingPermissionResult.requestCode,
                        pendingPermissionResult.requestPermission,
                        pendingPermissionResult.grantResult
                );
                pendingPermissionResult = null;
            }
        }
    }

    private void clearPendingAction() {
        pendingPermissionResult = null;
        pendingActivityResult = null;
        pendingNewIntent = null;
    }

    private void initializeFlutter(@NonNull Context context) {
        FlutterMain.startInitialization(context.getApplicationContext());
        FlutterMain.ensureInitializationComplete(
                context.getApplicationContext(),
                page.getFlutterShellArgs().toArray()
        );
    }

    /**
     * 持有一个 flutter engine，如果没有 flutter engine 创建过的话重新创建一个
     */
    private void setupFlutterEngine(@NonNull Context context) {
        if (flutterEngine == null) {
            Log.d(TAG, "FlutterEngine is null, create a new one");
            flutterEngine = FlutterManager.getInstance().getOrCreateFlutterEngine(context);
        }
    }

    /**
     * Start running Dart VM or push a new flutter page
     * if current engine is running, it does nothing
     */
    private void doInitialRunOrPushPage() {
        assertNotNull(flutterEngine);
        if (flutterEngine.getDartExecutor().isExecutingDart()) {
            if (!hasFlag(attachFlag, FLAG_PUSH_PAGE)) {
                attachFlag |= FLAG_PUSH_PAGE;
                // push page
                HybridRouterPlugin.getInstance().pushFlutterPager(routeOptions.pageName,
                        routeOptions.args, nativePageId, page.isTab(), null);
            }
            return;
        }

        DartExecutor.DartEntrypoint entrypoint = new DartExecutor.DartEntrypoint(
                page.getAppBundlePath(),
                page.getDartEntrypointFunctionName()
        );
        flutterEngine.getDartExecutor().executeDartEntrypoint(entrypoint);
    }

    private FlutterUiDisplayListener firstFrameListener = new FlutterUiDisplayListener() {
        @Override
        public void onFlutterUiDisplayed() {
            if (page instanceof IFlutterHook && flutterView != null) {
                ((IFlutterHook) page).onFirstFrameRendered(flutterView);
            }
        }

        @Override
        public void onFlutterUiNoLongerDisplayed() {

        }
    };

    private boolean hasFlag(int flag, int target) {
        return (flag & target) == target;
    }


    private void onNativePageRoute(@NonNull NativeRouteOptions routeOptions, int requestCode) {
        IFlutterWrapConfig wrapConfig = FlutterManager.getInstance().getFlutterWrapConfig();
        if (wrapConfig == null || !wrapConfig.onNativePageRoute(page,
                routeOptions, requestCode)) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(routeOptions.url));
            FlutterStackManagerUtil.updateIntent(intent, routeOptions.args);
            page.startActivityForResult(intent, requestCode);
        }
    }

    /**
     * 请求打开 新的 flutter page，借助新的 native page 容器
     */
    private void onFlutterPageRoute(@NonNull FlutterRouteOptions routeOptions, int requestCode) {
        IFlutterWrapConfig wrapConfig = FlutterManager.getInstance().getFlutterWrapConfig();
        if (wrapConfig == null || !wrapConfig.onFlutterPageRoute(page,
                routeOptions, requestCode)) {
            Context context = page.getContext();
            if (context != null) {
                Intent intent = new HybridFlutterActivity.IntentBuilder()
                        .route(routeOptions).buildIntent(context);
                page.startActivityForResult(intent, requestCode);
            } else {
                throw new IllegalStateException("The context of current fragment is null");
            }
        }
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

    /**
     * 结束所有的 result channel，如果当前页面有打开一个新的页面的请求，但是页面请求没有返回结果
     * 这里手动结束掉所有的 result 回调
     */
    private void cancelAllResult() {
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

    /**
     * 发送 onActivityResult 获取到的结果
     */
    private void sendResult(int requestCode, int resultCode, @Nullable Intent data) {
        MethodChannel.Result resultChannel = resultChannelMap.get(requestCode);
        if (resultChannel != null) {
            resultChannelMap.remove(requestCode);
            HashMap<String, Object> ret = new HashMap<>();
            // 此数据 key 定义在 dart 测
            ret.put("resultCode", resultCode);
            Object result = null;
            Bundle extras = data == null ? null : data.getExtras();
            if (extras != null && data.hasExtra(ARG_RESULT_KEY)) {
                // 从 flutter 过来的数据
                result = extras.get(ARG_RESULT_KEY);
            } else if (extras != null) {
                HashMap<String, Object> retMap = new HashMap<>();
                if (data.getExtras() != null) {
                    for (String key : data.getExtras().keySet()) {
                        retMap.put(key, data.getExtras().get(key));
                    }
                }
                result = retMap;
            }
            // 此数据 key 定义在 dart 测
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
     * pending activity result
     */
    private static class PendingActivityResult {
        public final int requestCode;
        public final int resultCode;
        public final Intent data;

        private PendingActivityResult(int requestCode, int resultCode, Intent data) {
            this.requestCode = requestCode;
            this.resultCode = resultCode;
            this.data = data;
        }
    }

    /**
     * pending permission result
     */
    private static class PendingPermissionResult {
        public final int requestCode;
        public final String[] requestPermission;
        public final int[] grantResult;

        private PendingPermissionResult(int requestCode, String[] requestPermission, int[] grantResult) {
            this.requestCode = requestCode;
            this.requestPermission = requestPermission;
            this.grantResult = grantResult;
        }
    }

    private void addToResumeList() {
        if (hasFlag(lifecycleFlag, FLAG_RESUMED)) {
            if (!resumedDelegate.contains(this)) {
                resumedDelegate.add(0, this);
            }
        }
    }

    private void removeFromResumeList() {
        if (!hasFlag(lifecycleFlag, FLAG_RESUMED)) {
            if (resumedDelegate.remove(this)) {
                // remove success
                if (resumedDelegate.size() > 0) {
                    FlutterNativePageDelegate delegate = resumedDelegate.get(0);
                    delegate.onPause();
                    delegate.onResume();
                }
            }
        }
    }
}