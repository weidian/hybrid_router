package com.vdian.flutter.hybridrouter.page;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import com.vdian.flutter.hybridrouter.FlutterStackManager;
import com.vdian.flutter.hybridrouter.FlutterStackManagerUtil;
import com.vdian.flutter.hybridrouter.HybridRouterPlugin;
import com.vdian.flutter.hybridrouter.R;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import io.flutter.app.FlutterActivityDelegate;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.view.FlutterMain;
import io.flutter.view.FlutterNativeView;
import io.flutter.view.FlutterView;

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
 * <p>
 * flutter 容器
 *
 * @author qigengxin
 * @since 2019/2/5 2:26 PM
 */
public class FlutterWrapActivity extends AppCompatActivity implements PluginRegistry,
        FlutterActivityDelegate.ViewFactory, FlutterView.Provider, IFlutterNativePage {

    public static final String EXTRA_RESULT_KEY = "ext_result_key";
    public static final String EXTRA_FLUTTER_ROUTE = "ext_flutter_route";

    /**
     * 直接打开 flutter 页面
     *
     * @param context      上下文
     * @param routeOptions 路由参数
     */
    public static void start(@NonNull Context context, @NonNull FlutterRouteOptions routeOptions) {
        Intent intent = new Intent(context, FlutterWrapActivity.class);
        intent.putExtra(EXTRA_FLUTTER_ROUTE, routeOptions);
        context.startActivity(intent);
    }

    /**
     * 返回打开 flutter 页面的 intent
     *
     * @param context      上下文
     * @param routeOptions 路由参数
     */
    public static Intent startIntent(@NonNull Context context, @NonNull FlutterRouteOptions routeOptions) {
        Intent intent = new Intent(context, FlutterWrapActivity.class);
        intent.putExtra(EXTRA_FLUTTER_ROUTE, routeOptions);
        return intent;
    }

    /**
     * 配置页面行为
     */
    public static void setFlutterWrapConfig(IFlutterWrapConfig config) {
        sFlutterWrapConfig = config;
    }

    private static AtomicLong sGlobalPageId = new AtomicLong(1);
    private static int FLAG_ATTACH = 1;
    private static int FLAG_SURFACE_CREATED = 2;
    private static int MAX_REQUEST_CODE = 100;
    // 注意会持有 context，传递 application
    @SuppressLint("StaticFieldLeak")
    private static FlutterNativeView sFlutterNativeView;
    // flutter wrap activity 的配置信息
    private static IFlutterWrapConfig sFlutterWrapConfig;

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
        if (result == null) {
            setResult(RESULT_OK);
        } else {
            Intent data = new Intent();
            FlutterStackManagerUtil.updateIntent(data, EXTRA_RESULT_KEY, result);
            setResult(RESULT_OK, data);
        }
        // 结束当前 native 页面
        finish();
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
        Map<String, Object> ret = new HashMap<>();
        if (routeOptions != null) {
            ret.put("pageName", routeOptions.pageName);
            if (routeOptions.args != null) {
                ret.put("args", routeOptions.args);
            }
        }
        ret.put("nativePageId", nativePageId);
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
    public Context getContext() {
        return this;
    }

    @Override
    public FlutterView createFlutterView(Context context) {
        FlutterNativeView flutterNativeView = createFlutterNativeView();
        return new FlutterView(this, null, flutterNativeView);
    }

    @Override
    public FlutterNativeView createFlutterNativeView() {
        if (sFlutterNativeView == null) {
            isCreatePage = true;
            sFlutterNativeView = new FlutterNativeView(getApplicationContext());
        }
        return sFlutterNativeView;
    }

    @Override
    public boolean retainFlutterNativeView() {
        return true;
    }

    @Override
    public Registrar registrarFor(String key) {
        return delegate.registrarFor(key);
    }

    @Override
    public boolean hasPlugin(String key) {
        return delegate.hasPlugin(key);
    }

    @Override
    public <T> T valuePublishedByPlugin(String pluginKey) {
        return delegate.valuePublishedByPlugin(pluginKey);
    }

    @Override
    public FlutterView getFlutterView() {
        if (delegate == null) {
            return null;
        }
        return delegate.getFlutterView();
    }

    // 当前页面的 id
    protected final String nativePageId = String.valueOf(sGlobalPageId.getAndIncrement());
    /**
     * 当前 flutter 页面的 page name
     */
    protected FlutterRouteOptions routeOptions;
    // 是否是首次启动 flutter engine
    protected boolean isCreatePage;
    // flutter activity 代理类
    protected FlutterActivityDelegate delegate;
    protected ViewGroup rootView;
    protected SparseArray<MethodChannel.Result> resultChannelMap = new SparseArray<>();
    protected SparseArray<IPageResultCallback> pageCallbackMap = new SparseArray<>();
    // 是否需要在 destroy 的时候调用 destroy surface
    protected boolean needDestroySurface = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // push the page to stack manager
        FlutterStackManager.getInstance().addNativePage(this);
        // 从 saveInstance 或者 intent 中获取 uri
        if (savedInstanceState != null) {
            routeOptions = savedInstanceState.getParcelable(EXTRA_FLUTTER_ROUTE);
        } else {
            Intent intent = getIntent();
            if (intent.hasExtra(EXTRA_FLUTTER_ROUTE)) {
                routeOptions = intent.getParcelableExtra(EXTRA_FLUTTER_ROUTE);
            } else if (sFlutterWrapConfig != null) {
                routeOptions = sFlutterWrapConfig.parseFlutterRouteFromIntent(this, intent);
            }
        }
        // 如果没有路由信息，退出页面
        if (routeOptions == null) {
            finish();
            return;
        }
        if (sFlutterNativeView == null) {
            isCreatePage = true;
        }
        // init flutter
        try {
            FlutterMain.startInitialization(getApplicationContext());
            attachFlutter();
            delegate.onCreate(savedInstanceState);
        } catch (Throwable t) {
            // 初始化异常
            t.printStackTrace();
            onFlutterInitFailure(t);
            finish();
            return;
        }
        // 引擎初始化完毕，更新状态栏颜色
        updateStatusBarColor(statusBarColor);
        if (isCreatePage) {
            // 首次打开 flutter engine 注册插件
            onRegisterPlugin();
        }
        if (!isCreatePage) {
            // 非首次打开，设置沉浸式主题，因为不是首次打开，flutter 不会请求 native 来更新沉浸式主题，需要手动请求更新
            innerPreFlutterApplyTheme();
            HybridRouterPlugin.getInstance().requestUpdateTheme(new MethodChannel.Result() {
                @Override
                public void success(@Nullable Object o) {
                    innerPostFlutterApplyTheme();
                }

                @Override
                public void error(String s, @Nullable String s1, @Nullable Object o) {
                    innerPostFlutterApplyTheme();
                }

                @Override
                public void notImplemented() {
                    innerPostFlutterApplyTheme();
                }
            });
        } else {
            // 是首次打开直接更新沉浸式主题
            innerPreFlutterApplyTheme();
            innerPostFlutterApplyTheme();
        }
        // 创建根视图
        rootView = createRootView();
        setContentView(rootView);
        MethodChannel.Result result = setupLaunchView(true);
        if (!isCreatePage) {
            // 首次打开需要 flutter 来主动获取路由，非首次打开由当前页面来打开路由
            HybridRouterPlugin.getInstance().pushFlutterPager(routeOptions.pageName, routeOptions.args,
                    nativePageId, result);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (delegate != null) {
            delegate.onNewIntent(intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachFlutter();
        if (delegate != null) {
            delegate.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        attachFlutter();
        if (delegate != null) {
            delegate.onResume();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        attachFlutter();
        if (delegate != null) {
            delegate.onPostResume();
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (delegate != null) {
            delegate.onUserLeaveHint();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (delegate != null) {
            delegate.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (delegate != null) {
            delegate.onStop();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null && routeOptions != null) {
            outState.putParcelable(EXTRA_FLUTTER_ROUTE, routeOptions);
        }
    }

    @Override
    protected void onDestroy() {
        cancelAllResult();
        FlutterStackManager.getInstance().removeNativePage(this);
        if (delegate != null) {
            // 手动把 handler 设置为 null，防止内存泄漏
            FlutterView flutterView = getFlutterView();
            if (flutterView != null) {
                flutterView.setMessageHandler("flutter/platform", null);
            }
            destroyDelegate(delegate);
        }
        detachFlutter();
        if (HybridRouterPlugin.isRegistered()) {
            HybridRouterPlugin.getInstance().onNativePageFinished(nativePageId);
        }
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (delegate != null) {
            delegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (delegate != null) {
            delegate.onActivityResult(requestCode, resultCode, data);
        }
        sendResult(requestCode, resultCode, data);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (delegate != null) {
            delegate.onTrimMemory(level);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (delegate != null) {
            delegate.onLowMemory();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (delegate != null) {
            delegate.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onBackPressed() {
        HybridRouterPlugin.getInstance().onBackPressed(new MethodChannel.Result() {
            @Override
            public void success(@Nullable Object o) {
                if (!(o instanceof Boolean) || !((Boolean) o)) {
                    FlutterWrapActivity.super.onBackPressed();
                }
            }

            @Override
            public void error(String s, @Nullable String s1, @Nullable Object o) {
                FlutterWrapActivity.super.onBackPressed();
            }

            @Override
            public void notImplemented() {
                FlutterWrapActivity.super.onBackPressed();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        if (routeOptions != null) {
            if (sFlutterWrapConfig == null ||
                    !sFlutterWrapConfig.updatePageTransition(this,
                            routeOptions.transitionType, false)) {
                switch (routeOptions.transitionType) {
                    case FlutterRouteOptions.TRANSITION_TYPE_RIGHT_LEFT: {
                        overridePendingTransition(R.anim.flutter_hybrid_exit_left, R.anim.flutter_hybrid_exit_right);
                        break;
                    }
                    case FlutterRouteOptions.TRANSITION_TYPE_BOTTOM_TOP: {
                        overridePendingTransition(R.anim.flutter_hybrid_enter_bottom, 0);
                        break;
                    }
                }
            }
        }
    }

    /**
     * flutter 初始化出现问题回调函数
     */
    protected void onFlutterInitFailure(Throwable t) {
        t.printStackTrace();
    }

    /**
     * 注册插件
     */
    protected void onRegisterPlugin() {
        try {
            Class.forName("io.flutter.plugins.GeneratedPluginRegistrant")
                    .getDeclaredMethod("registerWith", PluginRegistry.class)
                    .invoke(null, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void preFlutterApplyTheme() {
    }

    protected void postFlutterApplyTheme() {
    }

    /**
     * 创建根视图
     * @return
     */
    protected ViewGroup createRootView() {
        ViewGroup ret = new FrameLayout(this);
        ret.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        ret.addView(createMaskView());
        return ret;
    }

    /**
     * 创建 flutter view 还没有加载完成时候的遮罩
     */
    protected View createMaskView() {
        View background = new View(this);
        background.setBackground(getLaunchScreenDrawableFromActivityTheme());
        background.setClickable(true);
        return background;
    }

    /**
     * 配置启动页
     * 默认显示占位图，等待首帧和 channel 回调之后显示页面
     */
    protected void setupLaunchView() {
        setupLaunchView(false);
    }

    /**
     * 配置启动页
     * @param needWaitForResult 是否需要等待 channel 调用结束后才显示页面
     * @return 返回的数据用于打开页面 channel 用
     */
    protected MethodChannel.Result setupLaunchView(boolean needWaitForResult) {
        View maskView = null;
        final FlutterView flutterView = getFlutterView();
        int i = 0;
        // 移除前一个 flutter view，然后选择最顶层的 view 为 maskview
        while (i < rootView.getChildCount()) {
            View child = rootView.getChildAt(i);
            if (child instanceof FlutterView && child != flutterView) {
                rootView.removeViewAt(i);
            } else {
                i++;
                maskView = child;
            }
        }

        if (flutterView.getParent() != rootView) {
            // add flutter view to root view
            rootView.addView(flutterView, 0,
                    new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                            , ViewGroup.LayoutParams.MATCH_PARENT));
        }

        // surface 的生命周期回调，flutter detach 需要用到
        flutterView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                flag = flag | FLAG_SURFACE_CREATED;
                needDestroySurface = true;
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                flag = flag & (~FLAG_SURFACE_CREATED);
                if (flutterView.getFlutterNativeView() != null) {
                    /// not detach, need not to destroy
                    needDestroySurface = false;
                }
            }
        });
        final AtomicInteger visibleCount = (isCreatePage || !needWaitForResult) ?
                new AtomicInteger(1) : new AtomicInteger(0);
        final View finalMaskView = maskView;
        // 通过 result 和 first frame listener，判定首帧时间
        MethodChannel.Result result = new MethodChannel.Result() {
            @Override
            public void success(@Nullable Object result) {
                if (visibleCount.incrementAndGet() >= 2 && finalMaskView != null) {
                    finalMaskView.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void error(String s, @Nullable String s1, @Nullable Object o) {
            }

            @Override
            public void notImplemented() {
            }
        };
        flutterView.addFirstFrameListener(new FlutterView.FirstFrameListener() {
            @Override
            public void onFirstFrame() {
                flutterView.removeFirstFrameListener(this);
                if (visibleCount.incrementAndGet() >= 2 && finalMaskView != null) {
                    finalMaskView.setVisibility(View.INVISIBLE);
                }
            }
        });
        // 此处修复 flutter 和 native 沉浸式同步的问题
        ViewCompat.requestApplyInsets(flutterView);
        return result;
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

    /**
     * 请求生成一个新的没有用过的 request code
     *
     * @return < 0 generate failure
     */
    protected int generateRequestCode() {
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
     * 请求跳转 native 页面
     *
     * @param routeOptions
     * @param requestCode
     */
    protected void onNativePageRoute(@NonNull NativeRouteOptions routeOptions, int requestCode) {
        if (TextUtils.isEmpty(routeOptions.url)) {
            return;
        }
        if (sFlutterWrapConfig == null || !sFlutterWrapConfig.onNativePageRoute(this,
                routeOptions, requestCode)) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(routeOptions.url));
            intent.setPackage(getPackageName());
            FlutterStackManagerUtil.updateIntent(intent, routeOptions.args);
            startActivityForResult(intent, requestCode);
        }
        updatePageTransition(routeOptions.transitionType);
    }

    /**
     * 请求打开 新的 flutter page，借助新的 native page 容器
     *
     * @param routeOptions
     * @param requestCode
     */
    protected void onFlutterPageRoute(@NonNull FlutterRouteOptions routeOptions, int requestCode) {
        if (sFlutterWrapConfig == null || !sFlutterWrapConfig.onFlutterPageRoute(this,
                routeOptions, requestCode)) {
            Intent intent = startIntent(this, routeOptions);
            startActivityForResult(intent, requestCode);
        }
        updatePageTransition(routeOptions.transitionType);
    }

    /**
     * 更新 native 页面的动画信息
     * @param transitionType
     */
    protected void updatePageTransition(int transitionType) {
        if (sFlutterWrapConfig == null ||
                !sFlutterWrapConfig.updatePageTransition(this,
                        transitionType, true)) {
            switch (transitionType) {
                case FlutterRouteOptions.TRANSITION_TYPE_RIGHT_LEFT: {
                    overridePendingTransition(R.anim.flutter_hybrid_enter_right, R.anim.flutter_hybrid_enter_left);
                    break;
                }
                case FlutterRouteOptions.TRANSITION_TYPE_BOTTOM_TOP: {
                    overridePendingTransition(R.anim.flutter_hybrid_enter_bottom, 0);
                    break;
                }
                default:
                    break;
            }
        }
    }

    protected void superFinish() {
        super.finish();
    }

    protected void destroyDelegate(FlutterActivityDelegate delegate) {
        if (delegate != null) {
            delegate.onDestroy();
            if (sFlutterNativeView != null) {
                FlutterStackManagerUtil.attachFlutterNativeVIew(sFlutterNativeView);
            }
        }
    }

    /**
     * 更新状态栏辅助方法
     * @param color
     */
    protected void updateStatusBarColor(int color) {
        statusBarColor = color;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            if (window != null) {
                window.setStatusBarColor(statusBarColor);
            }
        }
    }

    private void innerPreFlutterApplyTheme() {
        if (sFlutterWrapConfig != null) {
            sFlutterWrapConfig.preFlutterApplyTheme(this);
        }
        preFlutterApplyTheme();
    }

    private void innerPostFlutterApplyTheme() {
        if (sFlutterWrapConfig != null) {
            sFlutterWrapConfig.postFlutterApplyTheme(this);
        }
        postFlutterApplyTheme();
    }


    /**
     * 私有保护区域
     */
    // 当前页面的状态
    private int flag = 0;
    private int statusBarColor = 0x40000000;

    /**
     * attach flutter
     */
    private void localAttachFlutter() {
        if (hasFlag(flag, FLAG_ATTACH)) {
            return;
        }
        flag |= FLAG_ATTACH;
        final IFlutterNativePage curNativePage = FlutterStackManager.getInstance().getCurNativePage();
        if (curNativePage != null) {
            curNativePage.detachFlutter();
        }
        // 设置当前页面是 native page
        FlutterStackManager.getInstance().setCurNativePage(this);
        final FlutterActivityDelegate localDt = new FlutterActivityDelegate(this, this);
        delegate = localDt;
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.CREATED)) {
            localDt.onCreate(null);
            innerPreFlutterApplyTheme();
            HybridRouterPlugin.getInstance().requestUpdateTheme(new MethodChannel.Result() {
                @Override
                public void success(@Nullable Object o) {
                    innerPostFlutterApplyTheme();
                }

                @Override
                public void error(String s, @Nullable String s1, @Nullable Object o) {
                    innerPostFlutterApplyTheme();
                }

                @Override
                public void notImplemented() {
                    innerPostFlutterApplyTheme();
                }
            });
            setupLaunchView();
        }
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            localDt.onStart();
        }
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            localDt.onResume();
            localDt.onPostResume();
        }
    }

    /**
     * detach flutter
     */
    private void localDetachFlutter() {
        if (!hasFlag(flag, FLAG_ATTACH)) {
            return;
        }
        flag &= ~FLAG_ATTACH;
        FlutterActivityDelegate localDt = delegate;
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            localDt.onUserLeaveHint();
            localDt.onPause();
        }
        // 如果 flutter 的 surface 已经 create 了，先 destroy
        FlutterView flutterView = getFlutterView();
        if (hasFlag(flag, FLAG_SURFACE_CREATED) || needDestroySurface) {
            flag &= ~FLAG_SURFACE_CREATED;
            needDestroySurface = false;
            FlutterStackManagerUtil.onSurfaceDestroyed(flutterView, flutterView.getFlutterNativeView());
        }
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            localDt.onStop();
        }
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.CREATED)) {
            // 这是设置 handler 为 null 防止内存泄漏
            flutterView.setMessageHandler("flutter/platform", null);
            destroyDelegate(localDt);
        }
        delegate = null;
        if (FlutterStackManager.getInstance().getCurNativePage() == this) {
            FlutterStackManager.getInstance().setCurNativePage(null);
        }
    }

    @Nullable
    private Drawable getLaunchScreenDrawableFromActivityTheme() {
        TypedValue typedValue = new TypedValue();
        if (!getTheme().resolveAttribute(
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

    private boolean hasFlag(int flag, int target) {
        return (flag & target) == target;
    }
}
