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
