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
package com.vdian.flutter.hybridrouterexample;

import android.support.annotation.NonNull;

import com.vdian.flutter.hybridrouter.page.HybridFlutterActivity;
import com.vdian.flutter.hybridrouter.page.IFlutterHook;

import io.flutter.embedding.android.FlutterView;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.PluginRegistry;

public class FlutterExampleActivity extends HybridFlutterActivity implements IFlutterHook {


    public static FlutterExampleActivity.IntentBuilder builder() {
        return new FlutterExampleActivity.IntentBuilder();
    }

    public static class IntentBuilder extends GenericIntentBuilder<FlutterExampleActivity.IntentBuilder> {

        protected IntentBuilder() {
            super(FlutterExampleActivity.class);
        }
    }


    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {

    }

    @Override
    public void cleanUpFlutterEngine(@NonNull FlutterEngine flutterEngine) {

    }

    @Override
    public void onFlutterInitFailure(@NonNull Throwable error) {

    }

    @Override
    public void onRegisterPlugin(PluginRegistry pluginRegistry) {
        HybridPluginRegistrant.registerWith(pluginRegistry);
    }

    @Override
    public void afterFlutterViewAttachToEngine(@NonNull FlutterView flutterView, @NonNull FlutterEngine flutterEngine) {

    }

    @Override
    public void beforeFlutterViewDetachFromEngine(@NonNull FlutterView flutterView, @NonNull FlutterEngine flutterEngine) {

    }

    @Override
    public void afterUpdateSystemUiOverlays(FlutterView flutterView) {

    }

    @Override
    public void onFirstFrameRendered(@NonNull FlutterView flutterView) {

    }
}
