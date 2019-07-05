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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.flutter.app.FlutterPluginRegistry;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.platform.PlatformViewRegistry;
import io.flutter.plugin.platform.PlatformViewsController;
import io.flutter.view.FlutterMain;
import io.flutter.view.FlutterNativeView;
import io.flutter.view.FlutterView;
import io.flutter.view.TextureRegistry;

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
 * 目前 FixFlutterPluginRegistry 不完善，这里补充它的功能
 *
 * @author qigengxin
 * @since 2019-07-03 22:55
 */
public class FixFlutterPluginRegistry extends FlutterPluginRegistry {
    private Activity mActivity;
    private Context mAppContext;
    private FlutterEngine mFlutterEngine;

    private final PlatformViewsController mPlatformViewsController;
    private final Map<String, Object> mPluginMap = new LinkedHashMap<>(0);
    private final List<RequestPermissionsResultListener> mRequestPermissionsResultListeners = new ArrayList<>(0);
    private final List<ActivityResultListener> mActivityResultListeners = new ArrayList<>(0);
    private final List<NewIntentListener> mNewIntentListeners = new ArrayList<>(0);
    private final List<UserLeaveHintListener> mUserLeaveHintListeners = new ArrayList<>(0);
    private final List<ViewDestroyListener> mViewDestroyListeners = new ArrayList<>(0);

    public FixFlutterPluginRegistry(FlutterEngine engine, Context context) {
        super(engine, context);
        mFlutterEngine = engine;
        mAppContext = context;
        mPlatformViewsController = new PlatformViewsController();
    }

    @Override
    public boolean hasPlugin(String key) {
        return mPluginMap.containsKey(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T valuePublishedByPlugin(String pluginKey) {
        return (T) mPluginMap.get(pluginKey);
    }

    @Override
    public Registrar registrarFor(String pluginKey) {
        if (mPluginMap.containsKey(pluginKey)) {
            throw new IllegalStateException("Plugin key " + pluginKey + " is already in use");
        }
        mPluginMap.put(pluginKey, null);
        return new FlutterRegistrar(pluginKey);
    }

    public void attach(Activity activity) {
        mActivity = activity;
        mPlatformViewsController.attach(activity, mFlutterEngine.getRenderer(), mFlutterEngine.getDartExecutor());
    }

    public void detach() {
        mPlatformViewsController.detach();
        mPlatformViewsController.onFlutterViewDestroyed();
        mActivity = null;
    }

    public void onPreEngineRestart() {
        mPlatformViewsController.onPreEngineRestart();
    }

    public PlatformViewsController getPlatformViewsController() {
        return mPlatformViewsController;
    }

    private class FlutterRegistrar implements Registrar {
        private final String pluginKey;

        FlutterRegistrar(String pluginKey) {
            this.pluginKey = pluginKey;
        }

        @Override
        public Activity activity() {
            return mActivity;
        }

        @Override
        public Context context() {
            return mAppContext;
        }

        @Override
        public Context activeContext() {
            return (mActivity != null) ? mActivity : mAppContext;
        }

        @Override
        public BinaryMessenger messenger() {
            return mFlutterEngine.getDartExecutor();
        }

        @Override
        public TextureRegistry textures() {
            return mFlutterEngine.getRenderer();
        }

        @Override
        public PlatformViewRegistry platformViewRegistry() {
            return mPlatformViewsController.getRegistry();
        }

        @Override
        public FlutterView view() {
            return null;
        }

        @Override
        public String lookupKeyForAsset(String asset) {
            return FlutterMain.getLookupKeyForAsset(asset);
        }

        @Override
        public String lookupKeyForAsset(String asset, String packageName) {
            return FlutterMain.getLookupKeyForAsset(asset, packageName);
        }

        @Override
        public Registrar publish(Object value) {
            mPluginMap.put(pluginKey, value);
            return this;
        }

        @Override
        public Registrar addRequestPermissionsResultListener(
                RequestPermissionsResultListener listener) {
            mRequestPermissionsResultListeners.add(listener);
            return this;
        }

        @Override
        public Registrar addActivityResultListener(ActivityResultListener listener) {
            mActivityResultListeners.add(listener);
            return this;
        }

        @Override
        public Registrar addNewIntentListener(NewIntentListener listener) {
            mNewIntentListeners.add(listener);
            return this;
        }

        @Override
        public Registrar addUserLeaveHintListener(UserLeaveHintListener listener) {
            mUserLeaveHintListeners.add(listener);
            return this;
        }

        @Override
        public Registrar addViewDestroyListener(ViewDestroyListener listener) {
            mViewDestroyListeners.add(listener);
            return this;
        }
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        for (RequestPermissionsResultListener listener : mRequestPermissionsResultListeners) {
            if (listener.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        for (ActivityResultListener listener : mActivityResultListeners) {
            if (listener.onActivityResult(requestCode, resultCode, data)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onNewIntent(Intent intent) {
        for (NewIntentListener listener : mNewIntentListeners) {
            if (listener.onNewIntent(intent)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onUserLeaveHint() {
        for (UserLeaveHintListener listener : mUserLeaveHintListeners) {
            listener.onUserLeaveHint();
        }
    }

    public boolean onViewDestroy(FlutterNativeView view) {
        boolean handled = false;
        for (ViewDestroyListener listener : mViewDestroyListeners) {
            if (listener.onViewDestroy(view)) {
                handled = true;
            }
        }
        return handled;
    }

    public void destroy() {
        mPlatformViewsController.onFlutterViewDestroyed();
    }
}
