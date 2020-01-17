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
package com.vdian.flutter.hybridrouterexample.translucent;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.vdian.flutter.hybridrouter.page.FlutterRouteOptions;
import com.vdian.flutter.hybridrouterexample.FlutterExampleActivity;
import com.vdian.flutter.hybridrouterexample.R;

public class TranslucentActivity extends AppCompatActivity {
    public static final String TAG = "FlutterLifecycle" + Build.VERSION.SDK_INT;

    private static final int REQUEST_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate:" + this);
        setContentView(R.layout.activity_translucent);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(TranslucentActivity.this, SetResultActivity.class), REQUEST_CODE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Intent intent = FlutterExampleActivity.builder().route(new FlutterRouteOptions.Builder("example")
                    .setArgs("Jump from translucent theme activity")
                    .build()).buildIntent(getApplicationContext());
            startActivity(intent);
            finish();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "onStart:" + this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume:" + this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause:" + this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG, "onStop:" + this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy:" + this);
    }

}
