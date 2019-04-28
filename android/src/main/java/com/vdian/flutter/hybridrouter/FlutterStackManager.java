package com.vdian.flutter.hybridrouter;

import android.support.annotation.Nullable;

import com.vdian.flutter.hybridrouter.page.IFlutterNativePage;

import java.util.HashMap;
import java.util.Map;

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
 * @since 2019/2/7 12:53 PM
 */
public class FlutterStackManager {

    private static FlutterStackManager instance;

    public static synchronized FlutterStackManager getInstance() {
        if (instance == null) {
            instance = new FlutterStackManager();
        }
        return instance;
    }

    private FlutterStackManager() {

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
        nativePageMap.remove(nativePage.getNativePageId());
    }

    @Nullable
    public IFlutterNativePage getNativePageById(String pageId) {
        return nativePageMap.get(pageId);
    }

    @Nullable
    private IFlutterNativePage curNativePage;
    private Map<String, IFlutterNativePage> nativePageMap = new HashMap<>();
}
