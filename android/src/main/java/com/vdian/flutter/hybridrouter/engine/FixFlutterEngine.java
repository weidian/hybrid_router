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

    private FlutterJNI flutterJNI;
    private FixFlutterPluginRegistry fixPluginRegistry;

    public FixFlutterEngine(@NonNull Context context) {
        super(context);
        flutterJNI = reflectFlutterJNI();
        fixPluginRegistry = new FixFlutterPluginRegistry(this, context);
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

    public FixFlutterPluginRegistry getFixPluginRegistry() {
        return fixPluginRegistry;
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
