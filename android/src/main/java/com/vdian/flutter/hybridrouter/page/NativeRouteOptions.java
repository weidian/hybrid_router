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
package com.vdian.flutter.hybridrouter.page;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
 * flutter 请求打开 native 页面需要的 参数
 *
 * @author qigengxin
 * @since 2019/2/28 10:54 AM
 */
public class NativeRouteOptions {

    public static class Builder {

        /**
         * 路由地址
         */
        @NonNull
        String url;

        /**
         * 路由参数
         */
        @Nullable
        Map<String, Object> args;

        /**
         * 切换动画类型
         * {@link FlutterRouteOptions#TRANSITION_TYPE_DEFAULT}
         */
        int transitionType;

        @NonNull
        public String getUrl() {
            return url;
        }

        public Builder setUrl(@NonNull String url) {
            this.url = url;
            return this;
        }

        @Nullable
        public Map<String, Object> getArgs() {
            return args;
        }

        public Builder setArgs(@Nullable Map<String, Object> args) {
            this.args = args;
            return this;
        }

        public int getTransitionType() {
            return transitionType;
        }

        public Builder setTransitionType(int transitionType) {
            this.transitionType = transitionType;
            return this;
        }

        public NativeRouteOptions build() {
            return new NativeRouteOptions(
                    url, args, transitionType
            );
        }
    }

    public NativeRouteOptions(@NonNull String url, @Nullable Map<String, Object> args, int transitionType) {
        this.url = url;
        this.args = args;
        this.transitionType = transitionType;
    }

    /**
     * 路由地址
     */
    @NonNull
    public final String url;

    /**
     * 路由参数
     */
    @Nullable
    public final Map<String, Object> args;

    /**
     * 切换动画类型
     * {@link #TRANSITION_TYPE_DEFAULT}
     */
    public final int transitionType;
}
