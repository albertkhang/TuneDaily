package com.albertkhang.tunedaily.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.adapters.ViewPagerAdapter;
import com.albertkhang.tunedaily.fragments.FragmentDetail;
import com.albertkhang.tunedaily.fragments.FragmentFullPlayer;
import com.albertkhang.tunedaily.fragments.FragmentLyric;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.rd.PageIndicatorView;

public class FullPlayerActivity extends AppCompatActivity {
    public static final int BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT = 1;
    private SettingManager settingManager;

    private ViewPager vpFullPlayer;
    private ViewPagerAdapter adapter;
    private PageIndicatorView pageIndicatorView;

    private FragmentFullPlayer fragmentFullPlayer;
    private FragmentDetail fragmentDetail;
    private FragmentLyric fragmentLyric;

    private ImageView imgCollapse;
    private ImageView imgMore;
    private ConstraintLayout root_view;
    private ConstraintLayout top_frame;
    private TextView txtTitle;
    private TextView txtArtist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_player);

        addControl();
        addEvent();
    }

    private void addControl() {
        settingManager = SettingManager.getInstance(this);

        vpFullPlayer = findViewById(R.id.vpFullPlayer);
        adapter = new ViewPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        pageIndicatorView = findViewById(R.id.pageIndicatorView);
        root_view = findViewById(R.id.root_view);
        top_frame = findViewById(R.id.top_frame);

        fragmentFullPlayer = new FragmentFullPlayer();
        fragmentDetail = new FragmentDetail();
        fragmentLyric = new FragmentLyric();

        adapter.addFragment(fragmentDetail);
        adapter.addFragment(fragmentFullPlayer);
        adapter.addFragment(fragmentLyric);

        vpFullPlayer.setAdapter(adapter);
        vpFullPlayer.setCurrentItem(1);

        imgCollapse = findViewById(R.id.imgCollapse);
        imgMore = findViewById(R.id.imgMore);
        txtTitle = findViewById(R.id.txtTitle);
        txtArtist = findViewById(R.id.txtArtist);

        updateTheme();
    }

    private void updateTheme() {
        if (settingManager.isDarkTheme()) {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorDark1));
            top_frame.setBackgroundColor(getResources().getColor(R.color.colorDark2));

            imgCollapse.setColorFilter(getResources().getColor(R.color.colorLight2));
            imgMore.setColorFilter(getResources().getColor(R.color.colorLight2));

            txtTitle.setTextColor(getResources().getColor(R.color.colorLight1));
            txtArtist.setTextColor(getResources().getColor(R.color.colorLight4));

            pageIndicatorView.setUnselectedColor(getResources().getColor(R.color.colorDark2));
        } else {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorLight1));
            top_frame.setBackgroundColor(getResources().getColor(R.color.colorLight2));

            imgCollapse.setColorFilter(getResources().getColor(R.color.colorDark2));
            imgMore.setColorFilter(getResources().getColor(R.color.colorDark2));

            txtTitle.setTextColor(getResources().getColor(R.color.colorDark1));
            txtArtist.setTextColor(getResources().getColor(R.color.colorDark4));

            pageIndicatorView.setUnselectedColor(getResources().getColor(R.color.colorLight2));
        }
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
