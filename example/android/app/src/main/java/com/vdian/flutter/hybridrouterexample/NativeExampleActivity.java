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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.vdian.flutter.hybridrouter.page.FlutterRouteOptions;

public class NativeExampleActivity extends AppCompatActivity {

    private static final int REQUEST_FLUTTER = 10;
    private static final int REQUEST_NATIVE = 11;

    private TextView txtRet;
    private Button btnJumpToFlutter;
    private Button btnJumpToNative;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_example);
        toolbar = findViewById(R.id.tool_bar);
        txtRet = findViewById(R.id.txt_ret);
        btnJumpToFlutter = findViewById(R.id.btn_jump_to_flutter);
        btnJumpToNative = findViewById(R.id.btn_jump_to_native);

        Intent data = getIntent();
        if (data != null) {
            String title = data.getStringExtra("title");
            if (!TextUtils.isEmpty(title)) {
                toolbar.setTitle(title);
            }
        }

        btnJumpToFlutter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = FlutterExampleActivity.builder().route(new FlutterRouteOptions.Builder("example")
                        .setArgs("Jump from native example")
                        .build()).buildIntent(v.getContext());
                startActivityForResult(intent, REQUEST_FLUTTER);
            }
        });

        btnJumpToNative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), NativeExampleActivity.class);
                intent.putExtra("title", "Jump from native example");
                startActivityForResult(intent, REQUEST_NATIVE);
            }
        });

        findViewById(R.id.btn_jump_to_tab_flutter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), TabExampleActivity.class);
                startActivityForResult(intent, REQUEST_FLUTTER);
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra("message", "Return message from native example");
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_NATIVE: {
                String message = null;
                if (data != null && data.hasExtra("message")) {
                    message = data.getStringExtra("message");
                }
                message = message == null ? "No message from native example" : message;
                txtRet.setText(message);
                break;
            }
            case REQUEST_FLUTTER: {
                String message = null;
                if (data != null) {
                    message = (String) FlutterExampleActivity.getFlutterResule(data);
                }
                message = message == null ? "No message from flutter example" : message;
                txtRet.setText(message);
                break;
            }
        }
    }
}
