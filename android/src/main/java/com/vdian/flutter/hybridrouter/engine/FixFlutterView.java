package com.vdian.flutter.hybridrouter.engine;

// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;

import io.flutter.embedding.android.FlutterView;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.renderer.FlutterRenderer;
import io.flutter.embedding.engine.renderer.FlutterUiDisplayListener;
import io.flutter.embedding.engine.renderer.RenderSurface;
import io.flutter.plugin.editing.TextInputPlugin;
import io.flutter.view.AccessibilityBridge;
import io.flutter.view.FixAccessibilityBridge;

/**
 * Displays a Flutter UI on an Android device.
 * <p>
 * A {@code FlutterView}'s UI is painted by a corresponding {@link FlutterEngine}.
 * <p>
 * A {@code FlutterView} can operate in 2 different {@link RenderMode}s:
 * <ol>
 * <li>{@link RenderMode#surface}, which paints a Flutter UI to a {@link android.view.SurfaceView}.
 * This mode has the best performance, but a {@code FlutterView} in this mode cannot be positioned
 * between 2 other Android {@code View}s in the z-index, nor can it be animated/transformed.
 * Unless the special capabilities of a {@link android.graphics.SurfaceTexture} are required,
 * developers should strongly prefer this render mode.</li>
 * <li>{@link RenderMode#texture}, which paints a Flutter UI to a {@link android.graphics.SurfaceTexture}.
 * This mode is not as performant as {@link RenderMode#surface}, but a {@code FlutterView} in this
 * mode can be animated and transformed, as well as positioned in the z-index between 2+ other
 * Android {@code Views}. Unless the special capabilities of a {@link android.graphics.SurfaceTexture}
 * are required, developers should strongly prefer the {@link RenderMode#surface} render mode.</li>
 * </ol>
 * See <a>https://source.android.com/devices/graphics/arch-tv#surface_or_texture</a> for more
 * information comparing {@link android.view.SurfaceView} and {@link android.view.TextureView}.
 */
public class FixFlutterView extends FlutterView {
    public FixFlutterView(@NonNull Context context) {
        super(context);
    }

    public FixFlutterView(@NonNull Context context, @NonNull RenderMode renderMode) {
        super(context, renderMode);
    }

    public FixFlutterView(@NonNull Context context, @NonNull TransparencyMode transparencyMode) {
        super(context, transparencyMode);
    }

    public FixFlutterView(@NonNull Context context, @NonNull RenderMode renderMode, @NonNull TransparencyMode transparencyMode) {
        super(context, renderMode, transparencyMode);
    }

    public FixFlutterView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void attachToFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.attachToFlutterEngine(flutterEngine);
        // 替换 accessibilityBridge，原先的会 crash
        try {
            Field accessibilityBridge = FlutterView.class.getDeclaredField("accessibilityBridge");
            accessibilityBridge.setAccessible(true);
            AccessibilityBridge ab = (AccessibilityBridge) accessibilityBridge.get(this);
            ab.setOnAccessibilityChangeListener(null);
            ab.release();
            FixAccessibilityBridge nab = new FixAccessibilityBridge(this, flutterEngine.getAccessibilityChannel(), (AccessibilityManager) this.getContext().getSystemService(Context.ACCESSIBILITY_SERVICE), this.getContext().getContentResolver(), flutterEngine.getPlatformViewsController());
            Field onAccessibilityChangeListenerField = FlutterView.class.getDeclaredField("onAccessibilityChangeListener");
            onAccessibilityChangeListenerField.setAccessible(true);
            Object listener = onAccessibilityChangeListenerField.get(this);
            nab.setOnAccessibilityChangeListener((AccessibilityBridge.OnAccessibilityChangeListener) listener);
            flutterEngine.getPlatformViewsController().attachAccessibilityBridge(nab);
            accessibilityBridge.set(this, nab);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * bug1 https://bugly.qq.com/v2/crash-reporting/crashes/900001590/1123015?pid=1&crashDataType=unSystemExit
     * fixme 1.17 低版本手机 在页面destroy的时候 surface 的onSurfaceTextureDestroyed被调用了两次
     * 第二次被调用的时候为null,暂时先兼容下
     * 还是走系统方法，触发null异常后，需要把engine置为空，不然会内存泄漏
     *
     * bug2 https://bugly.qq.com/v2/crash-reporting/crashes/900001590/1126920?pid=1&crashDataType=undefined
     * AccessibilityBridge 可能为空，虽然做了try catch ，但是为了防止内存泄漏 还是要处理，这个bug暂时无法复现
     */
    @Override
    public void detachFromFlutterEngine() {
        try {
            super.detachFromFlutterEngine();
        } catch (NullPointerException e) {
            e.printStackTrace();
            String message = e.getMessage();
            if (!TextUtils.isEmpty(message)){
                if (message.contains("AccessibilityBridge")){
                    fixAccessibilityBridgeNullException();
                } else if (message.contains("Surface")){
                    fixSurfaceNullException();
                }
            }
        }
    }

    /**
     * 低版本概率很高
     */
    private void fixSurfaceNullException(){
        Class<FlutterView> flutterViewClass = FlutterView.class;
        try {
            Field field = flutterViewClass.getDeclaredField("flutterEngine");
            field.setAccessible(true);
            field.set(this, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    /**
     * 触发概率很低,定位不到原因，猜测如下
     * attach的时候 反射没设置进去,或者重新创建的时候有可能就是空的
     */
    private void fixAccessibilityBridgeNullException(){
        Class<FlutterView> flutterViewClass = FlutterView.class;
        try {
            Field fieldEngine = flutterViewClass.getDeclaredField("flutterEngine");
            fieldEngine.setAccessible(true);

            //reset isFlutterUiDisplayed
            Field fieldIsFlutterUiDisplayed = flutterViewClass.getDeclaredField("isFlutterUiDisplayed");
            fieldIsFlutterUiDisplayed.setAccessible(true);
            fieldIsFlutterUiDisplayed.set(this, false);

            // reset FlutterRenderer
            FlutterEngine engine = (FlutterEngine)fieldEngine.get(this);
            FlutterRenderer flutterRenderer = engine.getRenderer();

            Field flutterUiDisplayListenerField = flutterViewClass.getDeclaredField("flutterUiDisplayListener");
            flutterUiDisplayListenerField.setAccessible(true);
            flutterRenderer.removeIsDisplayingFlutterUiListener((FlutterUiDisplayListener)flutterUiDisplayListenerField.get(this));
            flutterRenderer.stopRenderingToSurface();
            flutterRenderer.setSemanticsEnabled(false);

            //set engine null
            fieldEngine.set(this, null);

            Field renderSurfaceField = flutterViewClass.getDeclaredField("renderSurface");
            renderSurfaceField.setAccessible(true);
            RenderSurface renderSurface = (RenderSurface)renderSurfaceField.get(this);
            renderSurface.detachFromRenderer();
        } catch (Exception ex) {
            ex.printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        //destroy textInputPlugin
        try {
            Field field = flutterViewClass.getDeclaredField("textInputPlugin");
            field.setAccessible(true);
            TextInputPlugin inputPlugin = (TextInputPlugin) field.get(this);
            inputPlugin.getInputMethodManager().restartInput(this);
            inputPlugin.destroy();
        } catch (Exception ex) {
            ex.printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        }


    }

    @Override
    protected void onConfigurationChanged(@NonNull Configuration newConfig) {
        if (isAttachedToFlutterEngine()) {
            super.onConfigurationChanged(newConfig);
        }
    }

    /**
     * Fix java.lang.NullPointerException: Attempt to invoke virtual method 'android.content.Context android.view.View.getContext()' on a null object reference at io.flutter.plugin.platform.PlatformViewsController.checkInputConnectionProxy
     */
    @Override
    public boolean checkInputConnectionProxy(View view) {
        return view != null && super.checkInputConnectionProxy(view);
    }


}
