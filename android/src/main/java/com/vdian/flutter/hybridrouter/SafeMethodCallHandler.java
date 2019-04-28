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
