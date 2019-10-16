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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
 * flutter wrap activity 的一些配置信息
 *
 * @author qigengxin
 * @since 2019/3/23 3:29 PM
 */
public interface IFlutterWrapConfig {

    /**
     * 在 flutter 更新 native 状态栏主题之后调用
     */
    void postFlutterApplyTheme(@NonNull IFlutterNativePage nativePage);

    /**
     * 请求打开 flutter page route
     * @param routeOptions
     * @param requestCode
     */
    boolean onFlutterPageRoute(@NonNull IFlutterNativePage nativePage,
                               @Nullable FlutterRouteOptions routeOptions, int requestCode);

    /**
     * 请求打开 native 页面
     * @param routeOptions
     * @param requestCode
     */
    boolean onNativePageRoute(@NonNull IFlutterNativePage nativePage,
                              @NonNull NativeRouteOptions routeOptions, int requestCode);

    /**
     * 从 FlutterNativePage 中解析 flutterRouteOptions
     */
    FlutterRouteOptions parseFlutterRouteFromBundle(@NonNull IFlutterNativePage nativePage);
}
