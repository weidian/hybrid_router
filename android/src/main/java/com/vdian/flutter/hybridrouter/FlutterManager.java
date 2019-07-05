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

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.vdian.flutter.hybridrouter.engine.FixFlutterEngine;
import com.vdian.flutter.hybridrouter.engine.FixPlatformPlugin;
import com.vdian.flutter.hybridrouter.page.IFlutterNativePage;
import com.vdian.flutter.hybridrouter.page.IFlutterWrapConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.view.FlutterNativeView;

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
 * flutter 管理器接口，线程不安全
 *
 * @author qigengxin
 * @since 2019-07-01 17:46
 */
public class FlutterManager {
    private volatile static FlutterManager instance;

    public static FlutterManager getInstance() {
        if (instance == null) {
            synchronized (FlutterManager.class) {
                if (instance == null) {
                    instance = new FlutterManager();
                }
            }
        }
        return instance;
    }

    /**
     * 获取并生成一个新的 native page Id
     * @return
     */
    public String generateNativePageId() {
        return String.valueOf(growNativePageId.getAndIncrement());
    }

    /**
     * 获取当前 attach 的 native page
     * @return
     */
    @Nullable
    public IFlutterNativePage getCurNativePage() {
        return curNativePage;
    }

    /**
     * 设置当前 attach 的 native page
     * @param nativePage
     */
    public void setCurNativePage(@Nullable IFlutterNativePage nativePage) {
        this.curNativePage = nativePage;
    }

    /**
     * 添加一个 native page
     * @param nativePage
     */
    public void addNativePage(IFlutterNativePage nativePage) {
        nativePageMap.put(nativePage.getNativePageId(), nativePage);
    }

    /**
     * 移除一个 native page
     * @param nativePage
     */
    public void removeNativePage(IFlutterNativePage nativePage) {
        if (curNativePage == nativePage) {
            curNativePage = null;
        }
        nativePageMap.remove(nativePage.getNativePageId());
    }

    /**
     * 插件是否已经注册
     * @return
     */
    public boolean isPluginRegistry() {
        return isPluginRegistry;
    }

    /**
     * 注册插件
     */
    public void registerPlugins(PluginRegistry pluginRegistry) {
        if (!isPluginRegistry) {
            isPluginRegistry = true;
            try {
                Class.forName("io.flutter.plugins.GeneratedPluginRegistrant")
                        .getDeclaredMethod("registerWith", PluginRegistry.class)
                        .invoke(null, pluginRegistry);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public FlutterNativeView getFlutterNativeView() {
        return flutterNativeView;
    }

    public FlutterNativeView getOrCreateNativeView(Context context) {
        if (flutterNativeView == null) {
            flutterNativeView = new FlutterNativeView(context instanceof Application ?
                    context : context.getApplicationContext());
        }
        return flutterNativeView;
    }

    @Nullable
    public FixFlutterEngine getFlutterEngine() {
        return flutterEngine;
    }

    @NonNull
    public FixFlutterEngine getOrCreateFlutterEngine(@NonNull Context context) {
        if (flutterEngine == null) {
            flutterEngine = new FixFlutterEngine(context instanceof Application ? context
                    : context.getApplicationContext());
        }
        return flutterEngine;
    }

    @Nullable
    public FixPlatformPlugin getPlatformPlugin() {
        return platformPlugin;
    }

    @NonNull
    public FixPlatformPlugin getOrCreatePlatformPlugin() {
        if (platformPlugin == null) {
            platformPlugin = new FixPlatformPlugin();
        }
        return platformPlugin;
    }

    @Nullable
    public IFlutterNativePage getNativePageById(String pageId) {
        return nativePageMap.get(pageId);
    }

    /**
     * 自定义 flutter 的一些协议
     * @param flutterWrapConfig
     */
    public void setFlutterWrapConfig(IFlutterWrapConfig flutterWrapConfig) {
        this.flutterWrapConfig = flutterWrapConfig;
    }

    public IFlutterWrapConfig getFlutterWrapConfig() {
        return flutterWrapConfig;
    }

    /**
     * 用于判定 flutter init route 是否已经执行，如果已经执行了，可以放心执行 push channel，
     * 否则页面需要等待
     * @return
     */
    public boolean isFlutterRouteStart() {
        return isFlutterRouteStart;
    }

    public void setFlutterRouteStart(boolean flutterRouteStart) {
        isFlutterRouteStart = flutterRouteStart;
    }

    @Nullable
    private IFlutterNativePage curNativePage;
    private Map<String, IFlutterNativePage> nativePageMap = new HashMap<>();
    private AtomicLong growNativePageId = new AtomicLong(1);
    private FlutterNativeView flutterNativeView;
    private FixFlutterEngine flutterEngine;
    private FixPlatformPlugin platformPlugin;
    // flutter init route 是否已经执行完毕
    private boolean isFlutterRouteStart = false;
    private IFlutterWrapConfig flutterWrapConfig;
    // 插件是否已经注册
    private boolean isPluginRegistry = false;

    private FlutterManager() {}
}
