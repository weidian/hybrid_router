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
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vdian.flutter.hybridrouter.page.FlutterLaunchHelper;
import com.vdian.flutter.hybridrouter.page.FlutterRouteOptions;

import org.voiddog.android.lib.base.recycler.adapter.ListMultiTypeBindAdapter;
import org.voiddog.android.lib.base.recycler.adapter.MultiTypeBindAdapter;
import org.voiddog.android.lib.base.recycler.viewholder.BindViewHolder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static class MenuItem {
        private String title;
        private View.OnClickListener clickListener;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public View.OnClickListener getClickListener() {
            return clickListener;
        }

        public void setClickListener(View.OnClickListener clickListener) {
            this.clickListener = clickListener;
        }

        public MenuItem() {
        }

        public MenuItem(String title, View.OnClickListener clickListener) {
            this.title = title;
            this.clickListener = clickListener;
        }
    }

    public static class MenuVH extends BindViewHolder<MenuItem> {

        private TextView txtContent;

        public MenuVH(@NonNull ViewGroup parent) {
            super(parent, R.layout.rec_main_item);
            txtContent = itemView.findViewById(R.id.txt_content);
        }

        @Override
        public void onBindData(@NonNull MenuItem data) {
            txtContent.setText(data.title);
            itemView.setOnClickListener(data.clickListener);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListMultiTypeBindAdapter<MenuItem> adapter = new ListMultiTypeBindAdapter<>();
        adapter.registerItemProvider(new MultiTypeBindAdapter.ItemProvider<MenuItem>() {
            @Override
            public BindViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
                return new MenuVH(parent);
            }
        });
        RecyclerView recList = findViewById(R.id.rec_list);
        // data
        List<MenuItem> dataList = new ArrayList<>();
        dataList.add(new MenuItem(
                "跳转到 flutter 页面", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = FlutterExampleActivity.builder()
                        // flutter_tools 会把参数传递到 intent 中
                        .initializationArgs(FlutterLaunchHelper.parseFlutterShellArgs(getIntent()))
                        .dartEntrypoint(FlutterLaunchHelper.getDartEntrypointName(getIntent()))
                        .route(new FlutterRouteOptions.Builder("example")
                                .setArgs("Jump From Main").build())
                        .buildIntent(MainActivity.this);
                startActivity(intent);
            }
        }));
        dataList.add(new MenuItem(
                "跳转到 native 页面", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), NativeExampleActivity.class);
                intent.putExtra("title", "Jump from main");
                startActivity(intent);
            }
        }
        ));
        dataList.add(new MenuItem(
                "跳转到 tab 页面", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), TabExampleActivity.class);
                startActivity(intent);
            }
        }
        ));
        adapter.set(dataList);
        recList.setLayoutManager(new LinearLayoutManager(this));
        recList.setAdapter(adapter);

        Intent intent = FlutterExampleActivity.builder()
                // flutter_tools 会把参数传递到 intent 中
                .initializationArgs(FlutterLaunchHelper.parseFlutterShellArgs(getIntent()))
                .dartEntrypoint(FlutterLaunchHelper.getDartEntrypointName(getIntent()))
                .route(new FlutterRouteOptions.Builder("example")
                        .setArgs("Jump From Main").build())
                .buildIntent(MainActivity.this);
        startActivity(intent);


//        Intent intent = new Intent(MainActivity.this, TabExampleActivity.class);
//        startActivity(intent);
    }
}
