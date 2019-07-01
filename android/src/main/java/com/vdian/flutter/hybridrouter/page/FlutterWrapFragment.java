package com.vdian.flutter.hybridrouter.page;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.vdian.flutter.hybridrouter.FlutterStackManagerUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import io.flutter.app.FlutterActivityDelegate;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.view.FlutterNativeView;

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

    // TODO 这里需要统一下配置
    // flutter wrap activity 的配置信息
    private static IFlutterWrapConfig sFlutterWrapConfig;
    private static AtomicLong sGlobalPageId = new AtomicLong(100);
    private static int FLAG_ATTACH = 1;
    private static int FLAG_SURFACE_CREATED = 2;
    private static int MAX_REQUEST_CODE = 100;
    // 注意会持有 context，传递 application
    @SuppressLint("StaticFieldLeak")
    private static FlutterNativeView sFlutterNativeView;

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
        // 结束当前 native 页面
        // TODO finish();
    }

    @Override
    public String getNativePageId() {
        return nativePageId;
    }

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
    public void onFlutterRouteEvent(String name, int eventId, Map extra) {

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
    // 当前 flutter 的状态
    protected int flag;

    protected void onNativePageRoute(NativeRouteOptions routeOptions, int generateRequestCodeByChannel) {

    }

    protected void onFlutterPageRoute(FlutterRouteOptions routeOptions, int generateRequestCodeByChannel) {

    }


    private boolean hasFlag(int flag, int target) {
        return (flag & target) == target;
    }

    private int generateRequestCode() {
        return 0;
    }

    private void localAttachFlutter() {

    }

    private void localDetachFlutter() {

    }
}
