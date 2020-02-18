package com.vdian.flutter.hybridrouter.engine;

// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityManager;

import java.lang.reflect.Field;

import io.flutter.view.FixAccessibilityBridge;
import io.flutter.embedding.android.FlutterView;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.view.AccessibilityBridge;

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
            FixAccessibilityBridge nab = new FixAccessibilityBridge(this, flutterEngine.getAccessibilityChannel(), (AccessibilityManager) this.getContext().getSystemService("accessibility"), this.getContext().getContentResolver(), flutterEngine.getPlatformViewsController());
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
