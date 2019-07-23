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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.util.Map;

import io.flutter.plugin.common.MethodChannel;

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
     * 获取 android 上下文环境
     * @return
     */
    @Nullable
    Context getContext();

    /**
     * 获取对应的 fragment，如果当前页面是一个 fragment 的话
     * @return
     */
    @Nullable
    Fragment getFragment();

    /**
     * flutter route 事件
     * @param name
     * @param eventId
     */
    void onFlutterRouteEvent(String name, int eventId, Map extra);

    /**
     * 当前页面是否是一个 tab 页面
     */
    boolean isTab();
}
