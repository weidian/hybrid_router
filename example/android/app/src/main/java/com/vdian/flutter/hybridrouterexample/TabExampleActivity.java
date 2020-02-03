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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.vdian.flutter.hybridrouter.page.FlutterRouteOptions;
import com.vdian.flutter.hybridrouter.page.HybridFlutterFragment;

import io.flutter.embedding.android.FlutterView;

public class TabExampleActivity extends AppCompatActivity {

    public static class NativeFragment extends Fragment {

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            FrameLayout frameLayout = new FrameLayout(container.getContext());
            TextView textView = new TextView(frameLayout.getContext());
            textView.setText("我是 native fragment");
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            );
            lp.gravity = Gravity.CENTER;
            frameLayout.addView(textView, lp);
            return frameLayout;
        }
    }

    HybridFlutterFragment hybridFlutterFragmentA;
    HybridFlutterFragment hybridFlutterFragmentB;
    Fragment nativeFragment;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_example);
        hybridFlutterFragmentA = new HybridFlutterFragment.Builder()
                .renderMode(FlutterView.RenderMode.texture)
                .route(new FlutterRouteOptions.Builder("tab_example").build())
                .build();
        hybridFlutterFragmentB = new HybridFlutterFragment.Builder()
                .renderMode(FlutterView.RenderMode.texture)
                .route(new FlutterRouteOptions.Builder("tab_example").build())
                .build();
        nativeFragment = new NativeFragment();
        tabLayout = findViewById(R.id.tab);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                show(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
        show(0);
    }

    Fragment preFragment;
    boolean[] isAdded = new boolean[]{false, false, false};

    private void show(int index) {
        Fragment[] fragments = new Fragment[]{hybridFlutterFragmentA, nativeFragment, hybridFlutterFragmentB};
        Fragment fragment = fragments[index];
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (preFragment != null) {
            transaction.detach(preFragment);
            preFragment = null;
        }
        if (isAdded[index]) {
            transaction.attach(fragment);
        } else {
            isAdded[index] = true;
            transaction.add(R.id.fra_content, fragment);
        }
        transaction.commit();
        preFragment = fragment;
    }
}
