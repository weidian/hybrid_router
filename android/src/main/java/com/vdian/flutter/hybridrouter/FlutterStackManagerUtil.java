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

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.util.Log;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import io.flutter.embedding.android.FlutterView;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.FlutterJNI;
import io.flutter.embedding.engine.systemchannels.TextInputChannel;
import io.flutter.plugin.editing.TextInputPlugin;
import io.flutter.view.AccessibilityBridge;

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
 * @since 2018/11/13 1:07 PM
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class FlutterStackManagerUtil {

    public static <T> T assertNotNull(T object) {
        if (object == null)
            throw new AssertionError("Object cannot be null");
        return object;
    }

    public static void updateIntent(Intent intent, @Nullable Map<String, Object> args) {
        if (args == null) {
            return;
        }
        for (String key : args.keySet()) {
            updateIntent(intent, key, args.get(key));
        }
    }

    public static void updateIntent(Intent intent, String key, @Nullable Object value) {
        if (value == null) {
            intent.removeExtra(key);
        } else {
            if (value instanceof Integer) {
                intent.putExtra(key, (int) value);
            } else if (value instanceof Long) {
                intent.putExtra(key, (long) value);
            } else if (value instanceof Float) {
                intent.putExtra(key, (float) value);
            } else if (value instanceof Double) {
                intent.putExtra(key, (long) value);
            } else if (value instanceof String) {
                intent.putExtra(key, (String) value);
            } else if (value instanceof Serializable) {
                intent.putExtra(key, (Serializable) value);
            } else {
                try {
                    Method putExtra = intent.getClass().getDeclaredMethod("putExtra", String.class, value.getClass());
                    putExtra.setAccessible(true);
                    putExtra.invoke(intent, key, value);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("updateIntent", "unknow value: " + value);
                }
            }
        }
    }

    public static FlutterJNI getJNIFromFlutterEngine(FlutterEngine flutterEngine) {
        try {
            Field flutterJNIField = FlutterEngine.class.getDeclaredField("flutterJNI");
            flutterJNIField.setAccessible(true);
            return (FlutterJNI) flutterJNIField.get(flutterEngine);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void detachFlutterFromEngine(FlutterView flutterView, FlutterEngine flutterEngine) {
        // 1.5.4 版本 FlutterView 的内存泄漏修复
        // 释放 AccessibilityBridge
        flutterEngine.getAccessibilityChannel().setAccessibilityMessageHandler(null);
        try {
            Field accessibilityBridgeField = FlutterView.class.getDeclaredField("accessibilityBridge");
            accessibilityBridgeField.setAccessible(true);
            AccessibilityBridge accessibilityBridge = (AccessibilityBridge) accessibilityBridgeField.get(flutterView);
            accessibilityBridge.release();
            accessibilityBridgeField.set(flutterView, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 释放 text input plugin
        try {
            Field textInputPluginField = FlutterView.class.getDeclaredField("textInputPlugin");
            textInputPluginField.setAccessible(true);
            Field textInputChannelField = TextInputPlugin.class.getDeclaredField("textInputChannel");
            textInputChannelField.setAccessible(true);
            TextInputChannel textInputChannel = (TextInputChannel) textInputChannelField
                    .get(textInputPluginField.get(flutterView));
            textInputChannel.channel.setMethodCallHandler(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static boolean isAttached(FlutterNativeView flutterNativeView) {
//        try {
//            Field mFlutterViewField = FlutterNativeView.class.getDeclaredField("mFlutterView");
//            mFlutterViewField.setAccessible(true);
//            Object mFlutterView = mFlutterViewField.get(flutterNativeView);
//            return mFlutterView != null;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//    public static void onSurfaceDestroyed(FlutterView flutterView, FlutterNativeView flutterNativeView) {
//        // FlutterNativeView 方案 surface destroy 结束
//        try {
//            Method flutterJNIMethod = FlutterNativeView.class.getDeclaredMethod("getFlutterJNI");
//            flutterJNIMethod.setAccessible(true);
//            FlutterJNI flutterJNI = (FlutterJNI) flutterJNIMethod.invoke(flutterNativeView);
//            flutterJNI.onSurfaceDestroyed();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void attachFlutterNativeView(FlutterNativeView flutterNativeView) {
//        // FlutterNativeView 方案 detach bug 修复
//        flutterNativeView.getDartExecutor().onAttachedToJNI();
//    }
}
