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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

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
 * <p>
 * 打开 flutter 页面需要的参数
 *
 * @author qigengxin
 * @since 2019/2/25 10:36 AM
 */
public class FlutterRouteOptions implements Parcelable {

    public static final FlutterRouteOptions home = new Builder("/").build();

    // 默认的 切换动画
    public static final int TRANSITION_TYPE_DEFAULT = 0;
    // 底部弹出动画
    public static final int TRANSITION_TYPE_BOTTOM_TOP = 1;
    // 从右往左的动画
    public static final int TRANSITION_TYPE_RIGHT_LEFT = 2;


    protected FlutterRouteOptions(Parcel in) {
        pageName = in.readString();
        args = in.readSerializable();
        transitionType = in.readInt();
    }

    public static final Creator<FlutterRouteOptions> CREATOR = new Creator<FlutterRouteOptions>() {
        @Override
        public FlutterRouteOptions createFromParcel(Parcel in) {
            return new FlutterRouteOptions(in);
        }

        @Override
        public FlutterRouteOptions[] newArray(int size) {
            return new FlutterRouteOptions[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pageName);
        if (args instanceof Serializable) {
            dest.writeSerializable((Serializable) args);
        } else {
            dest.writeSerializable(null);
        }
        dest.writeInt(transitionType);
    }

    public static class Builder {
        /**
         * 路由参数
         */
        @Nullable
        Object args;

        /**
         * 当前 flutter 的路由名
         */
        @NonNull
        String pageName;

        /**
         * 页面切换动画
         */
        int transitionType = TRANSITION_TYPE_DEFAULT;

        public Builder(@NonNull String pageName) {
            this.pageName = pageName;
        }

        @Nullable
        public Object getArgs() {
            return args;
        }

        public Builder setArgs(@Nullable Object args) {
            this.args = args;
            return this;
        }

        @NonNull
        public String getPageName() {
            return pageName;
        }

        public Builder setPageName(@NonNull String pageName) {
            this.pageName = pageName;
            return this;
        }

        public int getTransitionType() {
            return transitionType;
        }

        /**
         * @param transitionType {@link Builder#TRANSITION_TYPE_DEFAULT}
         * @return
         */
        public Builder setTransitionType(int transitionType) {
            this.transitionType = transitionType;
            return this;
        }

        public FlutterRouteOptions build() {
            return new FlutterRouteOptions(
                    pageName,
                    args,
                    transitionType
            );
        }
    }

    /**
     * 路由参数
     */
    @Nullable
    public final Object args;

    /**
     * 当前 flutter 的路由名
     */
    @NonNull
    public final String pageName;

    /**
     * native 页面切换动画
     * @link()
     */
    public final int transitionType;

    public FlutterRouteOptions(@NonNull String pageName, @Nullable Object args,
                               int transitionType) {
        this.args = args;
        this.pageName = pageName;
        this.transitionType = transitionType;
    }

    /**
     * 从当前参数复制一份 builder
     * @return
     */
    public Builder withBuilder() {
        return new Builder(pageName)
                .setArgs(args)
                .setTransitionType(transitionType);
    }
}
