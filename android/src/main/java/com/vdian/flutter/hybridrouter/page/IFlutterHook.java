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


import androidx.annotation.NonNull;

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

    /**
     * FlutterView 首帧渲染完毕
     */
    void onFirstFrameRendered(@NonNull FlutterView flutterView);
}
