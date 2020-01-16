package com.vdian.flutter.hybridrouterexample;

import android.content.Context;
import android.content.Intent;

import com.vdian.flutter.hybridrouter.FlutterManager;
import com.vdian.flutter.hybridrouter.page.IFlutterNativePage;


import io.flutter.plugin.common.JSONMethodCodec;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

/**
 * @author lizhangqu
 * @version V1.0
 * @since 2020-01-16 18:15
 */
public class HybridPlugin implements MethodChannel.MethodCallHandler {

    /**
     * Plugin registration.
     */
    public static void registerWith(PluginRegistry.Registrar registrar) {
        MethodChannel hybrid_plugin = new MethodChannel(registrar.messenger(), "hybrid_plugin", JSONMethodCodec.INSTANCE);
        hybrid_plugin.setMethodCallHandler(new HybridPlugin());

    }

    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        switch (call.method) {
            case "toActivity": {
                IFlutterNativePage nativePage = FlutterManager.getInstance().getCurNativePage();
                Context context = nativePage.getContext();
                context.startActivity(new Intent(context, FirstActivity.class));
                break;
            }
            default:
                result.notImplemented();
                break;
        }
    }
}

