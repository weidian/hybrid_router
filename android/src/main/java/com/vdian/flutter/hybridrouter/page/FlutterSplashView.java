package com.vdian.flutter.hybridrouter.page;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import io.flutter.embedding.android.FlutterView;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.renderer.FlutterUiDisplayListener;

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
 * @since 2019-10-16 16:37
 */
public class FlutterSplashView extends FrameLayout {

    public FlutterSplashView(@NonNull Context context) {
        super(context);
    }

    public FlutterSplashView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FlutterSplashView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public FlutterSplashView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void displayFlutterViewWithSplash(
            @NonNull FlutterView flutterView, View splashScreen) {
        // If we were displaying a previous FlutterView, remove it1
        if (this.flutterView != null) {
            this.flutterView.removeOnFirstFrameRenderedListener(firstFrameListener);
            removeView(this.flutterView);
        }

        // If we were displaying a previous splash screen view, remove it
        if (this.splashScreen != null) {
            removeView(this.splashScreen);
        }

        // Display the new FlutterView.
        this.flutterView = flutterView;
        addView(flutterView);

        this.splashScreen = splashScreen;

        // Display the new splash screen if needed.
        if (splashScreen != null) {
            if (isSplashScreenNeededNow(flutterView)) {
                addView(splashScreen);
                flutterView.addOnFirstFrameRenderedListener(firstFrameListener);
            } else if (!flutterView.isAttachedToFlutterEngine()) {
                flutterView.addFlutterEngineAttachmentListener(attachmentListener);
            }
        }
    }

    private View splashScreen;
    private FlutterView flutterView;

    private final FlutterUiDisplayListener firstFrameListener = new FlutterUiDisplayListener() {
        @Override
        public void onFlutterUiDisplayed() {
            transitionToFlutter();
        }

        @Override
        public void onFlutterUiNoLongerDisplayed() {

        }
    };

    private void transitionToFlutter() {
        if (splashScreen != null) {
            removeView(splashScreen);
        }
    }

    private final FlutterView.FlutterEngineAttachmentListener attachmentListener =
            new FlutterView.FlutterEngineAttachmentListener() {
        @Override
        public void onFlutterEngineAttachedToFlutterView(@NonNull FlutterEngine flutterEngine) {
            flutterView.removeFlutterEngineAttachmentListener(this);
            displayFlutterViewWithSplash(flutterView, splashScreen);
        }

        @Override
        public void onFlutterEngineDetachedFromFlutterView() {
        }
    };

    /**
     * Returns true if current conditions require a splash UI to be displayed.
     * <p>
     * This method does not evaluate whether a previously interrupted splash transition needs
     * to resume
     */
    private boolean isSplashScreenNeededNow(@NonNull FlutterView flutterView) {
        return flutterView.isAttachedToFlutterEngine()
                && !flutterView.hasRenderedFirstFrame();
    }
}
