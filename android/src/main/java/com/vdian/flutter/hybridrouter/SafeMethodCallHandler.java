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

import android.support.annotation.RestrictTo;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

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
 * @since 2019/2/5 2:00 PM
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
abstract public class SafeMethodCallHandler implements MethodChannel.MethodCallHandler {

    /**
     * 安全的结果回调
     */
    static class SafeResult implements MethodChannel.Result {
        final MethodChannel.Result result;

        SafeResult(MethodChannel.Result result) {
            this.result = result;
        }

        boolean isReturn = false;

        @Override
        public void success(Object result) {
            if (isReturn) {
                return;
            }
            isReturn = true;
            this.result.success(result);
        }

        @Override
        public void error(String errorCode, String errorMessage, Object errorDetail) {
            if (isReturn) {
                return;
            }
            isReturn = true;
            this.result.error(errorCode, errorMessage, errorDetail);
        }

        @Override
        public void  notImplemented() {
            if (isReturn) {
                return;
            }
            isReturn = true;
            this.result.notImplemented();
        }
    }

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        if (methodCall == null) {
            result.notImplemented();
            return;
        }
        SafeResult sr = new SafeResult(result);
        try {
            // protect exception from method call
            onSafeMethodCall(methodCall, sr);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            sr.error("-1", throwable.getMessage(), null);
        }
    }

    abstract protected void onSafeMethodCall(MethodCall methodCall, SafeResult result);
}
