package com.vdian.flutter.hybridrouter.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.Field;

import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.FlutterJNI;

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
 * 目前官方的 FixFlutterEngine 不完善，这里补充它的功能
 *
 * @author qigengxin
 * @since 2019-07-03 22:54
 */
public class FixFlutterEngine extends FlutterEngine {

    private FixFlutterPluginRegistry pluginRegistry;
    private FlutterJNI flutterJNI;

    public FixFlutterEngine(@NonNull Context context) {
        super(context);
        pluginRegistry = new FixFlutterPluginRegistry(this, context);
        flutterJNI = reflectFlutterJNI();
    }

    /**
     * 获取 flutter engine 的截图
     * @return
     */
    @Nullable
    public Bitmap getFlutterBitmap() {
        if (flutterJNI == null) {
            return null;
        }
        return flutterJNI.getBitmap();
    }

    @NonNull
    @Override
    public FixFlutterPluginRegistry getPluginRegistry() {
        return pluginRegistry;
    }

    private FlutterJNI reflectFlutterJNI() {
        try {
            Field flutterJNIField = FlutterEngine.class.getDeclaredField("flutterJNI");
            flutterJNIField.setAccessible(true);
            return (FlutterJNI) flutterJNIField.get(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
