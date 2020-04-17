package io.flutter.view;

import android.content.ContentResolver;
import android.view.View;
import android.view.accessibility.AccessibilityManager;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

import io.flutter.embedding.engine.systemchannels.AccessibilityChannel;
import io.flutter.plugin.platform.PlatformViewsAccessibilityDelegate;

public class FixAccessibilityBridge extends AccessibilityBridge {
    public FixAccessibilityBridge(@NonNull View rootAccessibilityView, @NonNull AccessibilityChannel accessibilityChannel, @NonNull AccessibilityManager accessibilityManager, @NonNull ContentResolver contentResolver, PlatformViewsAccessibilityDelegate platformViewsAccessibilityDelegate) {
        super(rootAccessibilityView, accessibilityChannel, accessibilityManager, contentResolver, platformViewsAccessibilityDelegate);
    }

    @Override
    public boolean isAccessibilityEnabled() {
        return false;
    }

    @Override
    public boolean isTouchExplorationEnabled() {
        return false;
    }

    @Override
    void updateSemantics(@NonNull ByteBuffer buffer, @NonNull String[] strings) {
        // do nothing
    }
}
