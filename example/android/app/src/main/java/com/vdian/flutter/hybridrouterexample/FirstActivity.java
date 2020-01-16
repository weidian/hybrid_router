package com.vdian.flutter.hybridrouterexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.vdian.flutter.hybridrouter.page.FlutterRouteOptions;
import com.vdian.flutter.hybridrouter.page.HybridFlutterActivity;

public class FirstActivity extends AppCompatActivity {

    public static final String TAG = "FirstActivity" + Build.VERSION.SDK_INT;

    private static final int REQUEST_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate:" + this);
        setContentView(R.layout.activity_first);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(FirstActivity.this, SecondActivity.class), REQUEST_CODE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Intent intent = HybridFlutterActivity.newBuilder().route(new FlutterRouteOptions.Builder("example")
                    .setArgs("Jump from first activity")
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
