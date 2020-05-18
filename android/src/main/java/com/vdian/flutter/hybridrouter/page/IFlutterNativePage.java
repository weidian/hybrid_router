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
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;

import java.util.Map;

import io.flutter.embedding.android.FlutterView;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.FlutterShellArgs;
import io.flutter.plugin.common.MethodChannel;
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
 * @author qigengxin
 * @since 2019/2/5 2:13 PM
 */
public interface IFlutterNativePage {
    /**
     * 是否 attach 到了 flutter engine
     */
    boolean isAttachToFlutter();

    /**
     * request attach to flutter
     * 1.12.13开始 把attach放到了resume,因为官方的bug ，导致engine.isResume 之前attachEngine会引起无法渲染的bug
     */
    void attachFlutter();

    /**
     * request detach from flutter
     */
    void detachFlutter();

    /**
     * 请求 flutter native view 断开与 window 的连接
     */
    void requestDetachFromWindow();

    /**
     * 页面收到新的 intent 信息
     * @param intent
     */
    void onNewIntent(Intent intent);

    /**
     * flutter 内 请求打开 native page
     */
    void openNativePage(@NonNull NativeRouteOptions routeOptions, @NonNull MethodChannel.Result result);

    /**
     * flutter 内请求通过一个新的 native 页面打开一个 flutter 页面
     * @param routeOptions 打开 flutter 页面需要的路由参数
     * @param result channel 回调方法
     */
    void openFlutterPage(@NonNull FlutterRouteOptions routeOptions, @NonNull MethodChannel.Result result);

    /**
     * flutter 请求结束 native 页面
     */
    void finishNativePage(@Nullable Object result);

    /**
     * 获取 native page id，传给 flutter 绑定关键用
     */
    String getNativePageId();

    /**
     * 获取初始路由
     */
    Map<String, Object> getInitRoute();

    /**
     * 通过一个 channel 生成一个 result code
     * @param result
     * @return
     */
    int generateRequestCodeByChannel(MethodChannel.Result result);

    /**
     * 通过 page result callback 生成一个 request code
     * @param callback
     * @return
     */
    int generateRequestCodeByCallback(@NonNull IPageResultCallback callback);

    /**
     * 打开一个第三方页面
     * @param intent
     * @param requestCode
     */
    void startActivityForResult(Intent intent, int requestCode);

    /**
     * 获取当前页面的上下文环境, 如果当前容器是 activity 的话这里就是本身
     * @return
     */
    @Nullable
    Context getContext();


    /**
     * 获取当前页面所在的 activity，如果当前容器是 activity 的话这里就是本身
     * @return
     */
    @Nullable
    Activity getActivity();


    /**
     * 获取到当前页面的路由信息
     * must call after onCreate
     */
    FlutterRouteOptions getStartRouteOptions();

    /**
     * Returns the {@link Lifecycle} that backs the host {@link Activity} or {@code Fragment}.
     */
    @NonNull
    Lifecycle getLifecycle();

    /**
     * flutter route 事件
     * @param name
     * @param eventId
     */
    void onFlutterRouteEvent(String name, int eventId, Map extra);

    /**
     * 当前页面是否是一个 tab 页面
     * 这里要注意下 fragment默认写死的true
     */
    boolean isTab();

    /**
     * Returns the {@link FlutterShellArgs} that should be used when initializing Flutter.
     */
    @NonNull
    FlutterShellArgs getFlutterShellArgs();

    /**
     * Returns the Dart entrypoint that should run when a new {@link FlutterEngine} is
     * created.
     */
    @NonNull
    String getDartEntrypointFunctionName();

    /**
     * Returns the path to the app bundle where the Dart code exists.
     */
    @NonNull
    String getAppBundlePath();

    /**
     * Returns the {@link FlutterView.RenderMode} used by the {@link FlutterView} that
     * displays the {@link FlutterEngine}'s content.
     */
    @NonNull
    FlutterView.RenderMode getRenderMode();

    /**
     * Returns the {@link FlutterView.TransparencyMode} used by the {@link FlutterView} that
     * displays the {@link FlutterEngine}'s content.
     */
    @NonNull
    FlutterView.TransparencyMode getTransparencyMode();

    /**
     * Hook for the host to create/provide a {@link PlatformPlugin} if the associated
     * Flutter experience should control system chrome.
     */
    @Nullable
    PlatformPlugin providePlatformPlugin(@Nullable Activity activity, @NonNull FlutterEngine flutterEngine);

    @Nullable
    View provideSplashScreen();
}
