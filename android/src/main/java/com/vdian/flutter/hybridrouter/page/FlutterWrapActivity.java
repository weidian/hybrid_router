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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.vdian.flutter.hybridrouter.FlutterStackManagerUtil;
import com.vdian.flutter.hybridrouter.HybridRouterPlugin;

import io.flutter.plugin.common.MethodChannel;

import static com.vdian.flutter.hybridrouter.page.FlutterWrapFragment.EXTRA_FLUTTER_ROUTE;
import static com.vdian.flutter.hybridrouter.page.FlutterWrapFragment.EXTRA_RESULT_KEY;

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
public class FlutterWrapActivity extends AppCompatActivity implements FlutterWrapFragment.IPageDelegate {

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

    private FlutterWrapFragment flutterWrapFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        flutterWrapFragment = new FlutterWrapFragment.Builder()
                .pageDelegate(this)
                .extra(getIntent().getExtras())
                .build();
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, flutterWrapFragment)
                .commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        flutterWrapFragment.onNewIntent(intent);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        flutterWrapFragment.onUserLeaveHint();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        flutterWrapFragment.onTrimMemory(level);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        flutterWrapFragment.onLowMemory();
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

    @Override
    public void finishNativePage(IFlutterNativePage page, @Nullable Object result) {
        if (result == null) {
            setResult(RESULT_OK);
        } else {
            Intent data = new Intent();
            FlutterStackManagerUtil.updateIntent(data, EXTRA_RESULT_KEY, result);
            setResult(RESULT_OK, data);
        }
        // 结束当前 native 页面
        finish();
    }

    @Override
    public boolean isTab(IFlutterNativePage page) {
        return false;
    }
}
