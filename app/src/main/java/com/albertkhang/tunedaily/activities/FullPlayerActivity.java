package com.albertkhang.tunedaily.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.adapters.ViewPagerAdapter;
import com.albertkhang.tunedaily.fragments.FragmentDetail;
import com.albertkhang.tunedaily.fragments.FragmentFullPlayer;
import com.albertkhang.tunedaily.fragments.FragmentLyric;
import com.rd.PageIndicatorView;

public class FullPlayerActivity extends AppCompatActivity {
    public static final int BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT = 1;

    private ViewPager vpFullPlayer;
    private ViewPagerAdapter adapter;
    private PageIndicatorView pageIndicatorView;

    private FragmentFullPlayer fragmentFullPlayer;
    private FragmentDetail fragmentDetail;
    private FragmentLyric fragmentLyric;

    private ImageView imgCollapse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_player);

        addControl();
        addEvent();
    }

    private void addControl() {
        vpFullPlayer = findViewById(R.id.vpFullPlayer);
        adapter = new ViewPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        pageIndicatorView = findViewById(R.id.pageIndicatorView);

        fragmentFullPlayer = new FragmentFullPlayer();
        fragmentDetail = new FragmentDetail();
        fragmentLyric = new FragmentLyric();

        adapter.addFragment(fragmentDetail);
        adapter.addFragment(fragmentFullPlayer);
        adapter.addFragment(fragmentLyric);

        vpFullPlayer.setAdapter(adapter);
        vpFullPlayer.setCurrentItem(1);

        imgCollapse = findViewById(R.id.imgCollapse);
    }

    private void addEvent() {
        imgCollapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
