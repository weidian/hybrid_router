package com.vdian.flutter.hybridrouterexample;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class SecondActivity extends AppCompatActivity {
    public static final String TAG = "SecondActivity" + Build.VERSION.SDK_INT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate:" + this);
        setContentView(R.layout.activity_second);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
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
