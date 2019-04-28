package com.vdian.flutter.hybridrouter;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.util.Log;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import io.flutter.embedding.engine.FlutterJNI;
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
 *
 * @author qigengxin
 * @since 2018/11/13 1:07 PM
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class FlutterStackManagerUtil {

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

    public static boolean isAttached(FlutterNativeView flutterNativeView) {
        try {
            Field mFlutterViewField = FlutterNativeView.class.getDeclaredField("mFlutterView");
            mFlutterViewField.setAccessible(true);
            Object mFlutterView = mFlutterViewField.get(flutterNativeView);
            return mFlutterView != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void onSurfaceDestroyed(FlutterView flutterView, FlutterNativeView flutterNativeView) {
        try {
            Method flutterJNIMethod = FlutterNativeView.class.getDeclaredMethod("getFlutterJNI");
            flutterJNIMethod.setAccessible(true);
            FlutterJNI flutterJNI = (FlutterJNI) flutterJNIMethod.invoke(flutterNativeView);
            flutterJNI.onSurfaceDestroyed();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void attachFlutterNativeVIew(FlutterNativeView flutterNativeView) {
        flutterNativeView.getDartExecutor().onAttachedToJNI();
    }
}
