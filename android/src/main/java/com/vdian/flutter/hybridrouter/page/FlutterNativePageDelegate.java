package com.vdian.flutter.hybridrouter.page;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import com.vdian.flutter.hybridrouter.R;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.android.FlutterView;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.embedding.engine.renderer.OnFirstFrameRenderedListener;
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
    private static final int MAX_REQUEST_CODE = 100;

    private static final String TAG = "FlutterDelegate";
    private static final String ARG_RESULT_KEY = "arg_flutter_result";
    private static final String ARG_SAVED_START_ROUTE_OPTIONS = "arg_flutter_saved_start_route_options";

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
    // 当前 flutter 的状态
    private int flag;

    public FlutterNativePageDelegate(IFlutterNativePage page) {
        this.page = page;
    }

    /**
     * Current native page's page id
     * @return
     */
    public String getNativePageId() {
        return nativePageId;
    }

    /**
     * If the native container is attach to flutter
     * @return
     */
    public boolean isAttachToFlutter() {
        return hasFlag(flag, FLAG_ATTACH);
    }

    /**
     * 获取初始路由，如果是首次打开页面，需要通过 flutter 来主动获取路由信息
     */
    public Map<String, Object> getInitRoute() {
        // 这里获取 initRoute 可以认为 push 了 page
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
    public void openNativePage(@NonNull NativeRouteOptions routeOptions, @NonNull MethodChannel.Result result) {
        onNativePageRoute(routeOptions, generateRequestCodeByChannel(result));
    }


    /**
     * 通过 native 打开一个新的 flutter 页面
     * 此方法会优先判断 {@link IFlutterWrapConfig#onFlutterPageRoute(IFlutterNativePage, FlutterRouteOptions, int)}
     * 如果上述方法返回 true ，则不处理
     */
    public void openFlutterPage(@NonNull FlutterRouteOptions routeOptions, @NonNull MethodChannel.Result result) {
        onFlutterPageRoute(routeOptions, generateRequestCodeByChannel(result));
    }

    /**
     * 通过 result 生成一个对应的 result code
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
     * @param saveInstanceState
     */
    public void onCreate(@Nullable Bundle saveInstanceState) {
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

            flag |= FLAG_ENGINE_INIT;
        } catch (Throwable t) {
            // engine init error
            t.printStackTrace();
            if (page instanceof IFlutterHook) {
                ((IFlutterHook) page).onFlutterInitFailure(t);
            }
        }
    }

    public void onSaveInstance(@NonNull Bundle outState) {
        if (routeOptions != null) {
            outState.putParcelable(ARG_SAVED_START_ROUTE_OPTIONS, routeOptions);
        }
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(TAG, "Creating FlutterView.");
        if (!hasFlag(flag, FLAG_ENGINE_INIT)) {
            Log.e(TAG, "Engine init failure, return null view.");
            // engine init is failure, return null
            return null;
        }

        Context context = page.getContext();
        assertNotNull(context);
        flutterView = new FlutterView(context, page.getRenderMode(),
                page.getTransparencyMode());
        flutterView.addOnFirstFrameRenderedListener(firstFrameListener);

        flutterSplashView = new FlutterSplashView(context);
        flutterSplashView.displayFlutterViewWithSplash(flutterView, page.provideSplashScreen());
        return flutterSplashView;
    }

    public void onStart() {
        Log.v(TAG, "onStart()");
        if (hasFlag(flag, FLAG_ENGINE_INIT)) {
            // 官方这里放到了 post 中，为了防止启动的时候卡住，有必要吗？
            Log.v(TAG, "Attaching FlutterEngine to FlutterView.");
            assertNotNull(flutterView);
            assertNotNull(flutterEngine);
            attachEngine();
        }
    }

    public void onResume() {
        Log.v(TAG, "onResume");
        if (hasFlag(flag, FLAG_ENGINE_INIT)) {
            Log.v(TAG, "Resume flutter engine");
            assertNotNull(flutterEngine);
            // make sure the flutter is attach
            attachEngine();
            flutterEngine.getLifecycleChannel().appIsResumed();

            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    if (flutterEngine != null && platformPlugin != null) {
                        platformPlugin.updateSystemUiOverlays();
                        if (page instanceof IFlutterHook) {
                            ((IFlutterHook) page).afterUpdateSystemUiOverlays(flutterView);
                        }
                        IFlutterWrapConfig wrapConfig = FlutterManager.getInstance().getFlutterWrapConfig();
                        if (wrapConfig != null) {
                            wrapConfig.postFlutterApplyTheme(page);
                        }
                    }
                }
            });
        }
    }

    public void onPause() {
        Log.v(TAG, "onPause");
        if (hasFlag(flag, FLAG_ATTACH)) {
            assertNotNull(flutterEngine);
            Log.v(TAG, "Inactive flutter engine");
            flutterEngine.getLifecycleChannel().appIsInactive();
        }
    }

    public void onStop() {
        Log.v(TAG, "onStop");
        if (hasFlag(flag, FLAG_ATTACH)) {
            assertNotNull(flutterEngine);
            Log.v(TAG, "Paused flutter engine");
            flutterEngine.getLifecycleChannel().appIsPaused();
            // page is invisible, detach flutter engine
            detachEngine();
        }
    }

    public void onDestroyView() {
        Log.v(TAG, "onDestroyView");
        if (flutterView != null) {
            flutterView.removeOnFirstFrameRenderedListener(firstFrameListener);
            flutterView = null;
        }
    }

    public void onDestroy() {
        // 如果结束了，取消所有的结果
        cancelAllResult();
        // 从 manager 中移除当前页
        FlutterManager.getInstance().removeNativePage(page);
        if (hasFlag(flag, FLAG_ENGINE_INIT)) {
            if (page instanceof IFlutterHook) {
                ((IFlutterHook) page).cleanUpFlutterEngine(flutterEngine);
            }
        }
        // 通知 flutter naitive 容器被移除
        if (HybridRouterPlugin.isRegistered()) {
            HybridRouterPlugin.getInstance().onNativePageFinished(nativePageId);
        }
        flutterEngine = null;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (flutterEngine != null) {
            flutterEngine.getActivityControlSurface().onActivityResult(requestCode, resultCode, data);
        }
        sendResult(requestCode, resultCode, data);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (flutterEngine != null && isAttachToFlutter()) {
            flutterEngine.getActivityControlSurface()
                    .onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else {
            Log.w(TAG,
                    "onRequestPermissionResult() invoked before FlutterFragment was attached to an Activity.");
        }
    }

    public void onNewIntent(@NonNull Intent intent) {
        Log.v(TAG, "onNewIntent");
        if (isAttachToFlutter() && flutterEngine != null) {
            flutterEngine.getActivityControlSurface().onNewIntent(intent);
        }
    }

    public void onUserLeaveHint() {
        Log.v(TAG, "onUserLeaveHint");
        if (flutterEngine != null && isAttachToFlutter()) {
            flutterEngine.getActivityControlSurface().onUserLeaveHint();
        }
    }

    public void onTrimMemory(int level) {
        Log.v(TAG, "onTrimMemory");
        if (flutterEngine != null && isAttachToFlutter()) {
            if (level == TRIM_MEMORY_RUNNING_LOW) {
                flutterEngine.getSystemChannel().sendMemoryPressureWarning();
            }
        } else {
            Log.w(TAG, "onTrimMemory() invoked before FlutterFragment was attached to an Activity.");

        }
    }

    public void onLowMemory() {
        Log.v(TAG, "onLowMemory");
        if (flutterEngine != null && isAttachToFlutter()) {
            flutterEngine.getSystemChannel().sendMemoryPressureWarning();
        }
    }

    // ==================== engine attach and detach====================

    public void attachEngine() {
        if (hasFlag(flag, FLAG_ATTACH) || !hasFlag(flag, FLAG_ENGINE_INIT)) {
            return;
        }
        Log.v(TAG, "startAttachEngine");
        final IFlutterNativePage curNativePage = FlutterManager.getInstance().getCurNativePage();
        if (curNativePage != null) {
            curNativePage.detachFlutter();
        }
        // 这里的 flag 设置必须在 上面 detachFlutter call 之后
        flag |= FLAG_ATTACH;
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
    }

    public void detachEngine() {
        if (!hasFlag(flag, FLAG_ATTACH) || !hasFlag(flag, FLAG_ENGINE_INIT)) {
            return;
        }
        flag &= ~FLAG_ATTACH;
        // null check
        assertNotNull(flutterView);
        assertNotNull(flutterEngine);
        assertNotNull(platformPlugin);
        // hook method
        if (page instanceof IFlutterHook) {
            ((IFlutterHook) page).beforeFlutterViewDetachFromEngine(flutterView, flutterEngine);
        }
        // 生命周期感知
        if (page.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            flutterEngine.getActivityControlSurface().onUserLeaveHint();
            flutterEngine.getLifecycleChannel().appIsInactive();
        }
        if (page.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
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

    private void initializeFlutter(@NonNull Context context) {
        FlutterMain.ensureInitializationComplete(
                context.getApplicationContext(),
                page.getFlutterShellArgs().toArray()
        );
    }

    /**
     * 并持有一个 flutter engine，如果没有 flutter engine 创建过的话重新创建一个
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
        if (flutterEngine.getDartExecutor().isExecutingDart()) {
            // push page
            HybridRouterPlugin.getInstance().pushFlutterPager(routeOptions.pageName,
                    routeOptions.args, nativePageId, page.isTab(), null);
            return;
        }

        DartExecutor.DartEntrypoint entrypoint = new DartExecutor.DartEntrypoint(
                page.getAppBundlePath(),
                page.getDartEntrypointFunctionName()
        );
        flutterEngine.getDartExecutor().executeDartEntrypoint(entrypoint);
    }

    private OnFirstFrameRenderedListener firstFrameListener = new OnFirstFrameRenderedListener() {
        @Override
        public void onFirstFrameRendered() {

        }
    };

    private boolean hasFlag(int flag, int target) {
        return (flag & target) == target;
    }


    private void onNativePageRoute(NativeRouteOptions routeOptions, int requestCode) {
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
    private void onFlutterPageRoute(FlutterRouteOptions routeOptions, int requestCode) {
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
            // 次数据 key 定义在 dart 测
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
            // 次数据 key 定义在 dart 测
            ret.put("data", result);
            resultChannel.success(ret);
        }
        IPageResultCallback callback = pageCallbackMap.get(requestCode);
        if (callback != null) {
            pageCallbackMap.remove(requestCode);
            callback.onPageResult(requestCode, resultCode, data);
        }
    }
}
