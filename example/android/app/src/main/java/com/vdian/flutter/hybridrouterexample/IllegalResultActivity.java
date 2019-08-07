package com.vdian.flutter.hybridrouterexample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IllegalResultActivity extends AppCompatActivity {

    public static class TestResult implements Serializable {
        private String fieldA;
        private String fieldB;

        public String getFieldA() {
            return fieldA;
        }

        public void setFieldA(String fieldA) {
            this.fieldA = fieldA;
        }

        public String getFieldB() {
            return fieldB;
        }

        public void setFieldB(String fieldB) {
            this.fieldB = fieldB;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_illegal_result);
        findViewById(R.id.btn_return).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra("illegal", new TestResult());
                data.putExtra("message", "我是合法数据");
                data.putExtra("int", 0);
                data.putExtra("bool", true);
                data.putExtra("double", 0.0);
                data.putExtra("float", 0.0f);
                data.putExtra("long", 123L);
                ArrayList list = new ArrayList();
                list.add("我是合法数据");
                list.add(new TestResult());
                list.add(true);
                data.putExtra("list", list);
                HashMap map = new HashMap();
                map.put("message", "我是合法数据");
                map.put("illegal", new TestResult());
                map.put("int", 0);
                data.putExtra("map", map);
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }
}
