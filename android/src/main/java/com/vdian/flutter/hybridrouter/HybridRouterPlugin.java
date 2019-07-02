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
package com.vdian.flutter.hybridrouter;

import android.support.annotation.Nullable;

import com.vdian.flutter.hybridrouter.page.FlutterRouteOptions;
import com.vdian.flutter.hybridrouter.page.IFlutterNativePage;
import com.vdian.flutter.hybridrouter.page.NativeRouteOptions;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * HybridRouterPlugin
 */
public class HybridRouterPlugin extends SafeMethodCallHandler {

    // flutter route 事件
    // pop /remove 在 resume 前
    // push 在 pause 后
    public static final int FLUTTER_ON_PUSH = 0;
    public static final int FLUTTER_ON_RESUME = 1;
    public static final int FLUTTER_ON_PAUSE = 2;
    public static final int FLUTTER_ON_REPLACE = 3;
    public static final int FLUTTER_ON_POP = 4;
    public static final int FLUTTER_ON_REMOVE = 5;

    private static final int ON_CREATE = 0;
    private static final int ON_PAUSE = 1;
    private static final int ON_RESUME = 2;
    private static final int BEFORE_DESTROY = 3;
    private static final int ON_DESTROY = 4;

    private static HybridRouterPlugin instance;

    /**
     * 混合栈插件是否已经注册了
     * @return
     */
    public static boolean isRegistered() {
        return instance != null;
    }

    public static synchronized HybridRouterPlugin getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Must register plugin first");
        }
        return instance;
    }

    /**
     * Plugin registration.
     */
    public static synchronized void registerWith(Registrar registrar) {
        if (instance != null) {
            // unregister instance
            instance.channel.setMethodCallHandler(null);
            instance = null;
        }
        instance = new HybridRouterPlugin(registrar);
    }

    private final MethodChannel channel;

    private HybridRouterPlugin(Registrar registrar) {
        channel = new MethodChannel(registrar.messenger(),
         "com.vdian.flutter.hybridrouter");
        channel.setMethodCallHandler(this);
    }

    /**
     * 打开推送一个 flutter page
     *
     * @param pageName
     * @param args
     * @param nativePageId
     * @param result
     * @param isTab 是否是 tab 页面
     */
    public void pushFlutterPager(String pageName, Object args, String nativePageId
            , @Nullable Result result, boolean isTab) {
        if (channel != null) {
            HashMap<String, Object> channelArgs = new HashMap<>();
            channelArgs.put("args", args);
            channelArgs.put("pageName", pageName);
            channelArgs.put("nativePageId", nativePageId);
            channelArgs.put("isTab", isTab);
            channel.invokeMethod("pushFlutterPage", channelArgs, result);
        }
    }

    /**
     * native 页面结束事件
     * @param nativePageId
     */
    public void onNativePageFinished(String nativePageId) {
        if (channel != null) {
            HashMap<String, Object> args = new HashMap<>();
            args.put("nativePageId", nativePageId);
            channel.invokeMethod("onNativePageFinished", args);
        }
    }

    /**
     * 请求 flutter 更新主题
     *
     * @param result
     */
    public void requestUpdateTheme(@Nullable Result result) {
        if (channel != null) {
            channel.invokeMethod("requestUpdateTheme", null, result);
        }
    }

    /**
     * 按了返回键，这里重写返回键是为了防止出现黑屏无法退出的情况
     * @param result
     */
    public void onBackPressed(@Nullable Result result) {
        if (channel != null) {
            channel.invokeMethod("onBackPressed", null, result);
        } else if (result != null){
            result.error("-1", "channel is null", null);
        }
    }

    @Override
    protected void onSafeMethodCall(MethodCall call, SafeResult result) {
        switch (call.method) {
            case "getInitRoute": {
                IFlutterNativePage nativePage = FlutterManager.getInstance().getCurNativePage();
                if (nativePage != null) {
                    result.success(nativePage.getInitRoute());
                } else {
                    result.error("-1", "no native page found", null);
                }
                FlutterManager.getInstance().setFlutterRouteStart(true);
                break;
            }
            case "openNativePage": {
                HashMap<String, Object> args = call.arguments();
                String url = (String) args.get("url");
                String nativePageId = (String) args.get("nativePageId");
                int transitionType = (int) args.get("transitionType");
                Map<String, Object> subArgs = (Map<String, Object>) args.get("args");
                IFlutterNativePage nativePage = FlutterManager.getInstance().getNativePageById(nativePageId);
                if (nativePage != null) {
                    nativePage.openNativePage(new NativeRouteOptions.Builder()
                            .setUrl(url).setArgs(subArgs).setTransitionType(transitionType)
                            .build(), result);
                } else {
                    result.error("-1", "no native page found", null);
                }
                break;
            }
            case "openFlutterPage": {
                HashMap<String, Object> args = call.arguments();
                String pageName = (String) args.get("pageName");
                String nativePageId = (String) args.get("nativePageId");
                int transitionType = (int) args.get("transitionType");
                Object subArgs = args.get("args");
                IFlutterNativePage nativePage = FlutterManager.getInstance().getNativePageById(nativePageId);
                if (nativePage != null) {
                    nativePage.openFlutterPage(new FlutterRouteOptions.Builder(pageName)
                            .setArgs(subArgs)
                            .setTransitionType(transitionType)
                            .build(), result);
                } else {
                    result.error("-1", "native page not found for : " + pageName,
                            null);
                }
                break;
            }
            case "onNativeRouteEvent": {
                HashMap<String, Object> args = call.arguments();
                int eventId = (int) args.get("eventId");
                String nativePageId = (String) args.get("nativePageId");
                Object ret = args.get("result");
                switch (eventId) {
                    case ON_CREATE:
                    case ON_PAUSE:
                    case ON_RESUME:
                        result.success(null);
                        break;
                    case BEFORE_DESTROY:
                        beforeNativeRouteDestroy(nativePageId, result);
                        break;
                    case ON_DESTROY:
                        onNativeRouteDestroy(nativePageId, result, ret);
                        break;
                    default:
                        result.notImplemented();
                }
                break;
            }
            case "onFlutterRouteEvent": {
                HashMap<String, Object> args = call.arguments();
                String nativePageId = (String) args.get("nativePageId");
                String name = (String) args.get("name");
                Map extra = (Map) args.get("extra");
                int eventId = (int) args.get("eventId");
                IFlutterNativePage nativePage = FlutterManager.getInstance().getNativePageById(nativePageId);
                if (nativePage != null) {
                    nativePage.onFlutterRouteEvent(name, eventId, extra);
                }
                result.success(null);
                break;
            }
            default: {
                result.notImplemented();
            }
        }
    }

    private void beforeNativeRouteDestroy(String nativePageId, Result result) {
        // 在 pop 之前，请求 native detach form window
        IFlutterNativePage nativePage = FlutterManager.getInstance().getNativePageById(nativePageId);
        if (nativePage != null) {
            nativePage.requestDetachFromWindow();
        }
        result.success(null);
    }

    private void onNativeRouteDestroy(String nativePageId, Result result, Object ret) {
        IFlutterNativePage nativePage = FlutterManager.getInstance().getNativePageById(nativePageId);
        if (nativePage != null) {
            nativePage.finishNativePage(ret);
        }
        result.success(null);
    }
}
