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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.vdian.flutter.hybridrouter.HybridRouterPlugin;

import io.flutter.embedding.android.FlutterView;
import io.flutter.plugin.common.MethodChannel;

import static com.vdian.flutter.hybridrouter.page.FlutterWrapFragment.EXTRA_FLUTTER_ROUTE;

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
 * flutter 容器
 *
 * @author qigengxin
 * @since 2019/2/5 2:26 PM
 */
public class FlutterWrapActivity extends AppCompatActivity {

    /**
     * 直接打开 flutter 页面
     *
     * @param context      上下文
     * @param routeOptions 路由参数
     */
    public static void start(@NonNull Context context, @NonNull FlutterRouteOptions routeOptions) {
        Intent intent = new Intent(context, FlutterWrapActivity.class);
        intent.putExtra(EXTRA_FLUTTER_ROUTE, routeOptions);
        context.startActivity(intent);
    }

    /**
     * 返回打开 flutter 页面的 intent
     *
     * @param context      上下文
     * @param routeOptions 路由参数
     */
    public static Intent startIntent(@NonNull Context context, @NonNull FlutterRouteOptions routeOptions) {
        Intent intent = new Intent(context, FlutterWrapActivity.class);
        intent.putExtra(EXTRA_FLUTTER_ROUTE, routeOptions);
        return intent;
    }

    public FlutterWrapFragment getFlutterWrapFragment() {
        return flutterWrapFragment;
    }

    private FlutterWrapFragment flutterWrapFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        flutterWrapFragment = createAndSetupFragment();
    }

    protected FlutterWrapFragment createAndSetupFragment() {
        FlutterWrapFragment ret = new FlutterWrapFragment.Builder()
                .renderMode(FlutterView.RenderMode.texture)
                .pageDelegate(new FlutterWrapFragment.ActivityPageDelegate())
                .extra(getFlutterExtra())
                .build();
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, ret)
                .commit();
        return ret;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (flutterWrapFragment != null) {
            flutterWrapFragment.onNewIntent(intent);
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (flutterWrapFragment != null) {
            flutterWrapFragment.onUserLeaveHint();
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (flutterWrapFragment != null) {
            flutterWrapFragment.onTrimMemory(level);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (flutterWrapFragment != null) {
            flutterWrapFragment.onLowMemory();
        }
    }

    @Override
    public void onBackPressed() {
        if (HybridRouterPlugin.isRegistered()) {
            HybridRouterPlugin.getInstance().onBackPressed(new MethodChannel.Result() {
                @Override
                public void success(@Nullable Object o) {
                    if (!(o instanceof Boolean) || !((Boolean) o)) {
                        FlutterWrapActivity.super.onBackPressed();
                    }
                }

                @Override
                public void error(String s, @Nullable String s1, @Nullable Object o) {
                    FlutterWrapActivity.super.onBackPressed();
                }

                @Override
                public void notImplemented() {
                    FlutterWrapActivity.super.onBackPressed();
                }
            });
        } else {
            super.onBackPressed();
        }
    }

    /**
     * 获取 flutter 页面的参数，参数里面包含了
     * intent 过来的 uri 里面的 param
     */
    @Nullable
    protected Bundle getFlutterExtra() {
        Bundle ret = new Bundle();
        if (getIntent() == null) {
            return ret;
        }
        Uri data = getIntent().getData();
        if (data != null) {
            for (String key : data.getQueryParameterNames()) {
                ret.putString(key, data.getQueryParameter(key));
            }
        }
        ret.putAll(getIntent().getExtras());
        return ret;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
