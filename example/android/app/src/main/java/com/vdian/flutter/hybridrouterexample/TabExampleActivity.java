package com.vdian.flutter.hybridrouterexample;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.vdian.flutter.hybridrouter.page.FlutterRouteOptions;
import com.vdian.flutter.hybridrouter.page.FlutterWrapFragment;

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

    FlutterWrapFragment flutterWrapFragmentA;
    FlutterWrapFragment flutterWrapFragmentB;
    Fragment nativeFragment;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_example);
        flutterWrapFragmentA = new FlutterWrapFragment.Builder()
                .route(new FlutterRouteOptions.Builder("example").build())
                .build();
        flutterWrapFragmentB = new FlutterWrapFragment.Builder()
                .route(new FlutterRouteOptions.Builder("example").build())
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
        Fragment[] fragments = new Fragment[]{flutterWrapFragmentA, nativeFragment, flutterWrapFragmentB};
        Fragment fragment = fragments[index];
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (preFragment != null) {
            transaction.detach(preFragment);
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
