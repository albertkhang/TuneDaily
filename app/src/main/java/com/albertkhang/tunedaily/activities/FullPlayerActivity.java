package com.albertkhang.tunedaily.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.adapters.ViewPagerAdapter;
import com.albertkhang.tunedaily.events.UpdateTitleArtistEvent;
import com.albertkhang.tunedaily.fragments.DetailFragment;
import com.albertkhang.tunedaily.fragments.FullPlayerFragment;
import com.albertkhang.tunedaily.fragments.LyricFragment;
import com.albertkhang.tunedaily.fragments.MiniPlayerFragment;
import com.albertkhang.tunedaily.fragments.TrackMoreFragment;
import com.albertkhang.tunedaily.services.MediaPlaybackService;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.models.Track;
import com.rd.PageIndicatorView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;

public class FullPlayerActivity extends AppCompatActivity implements Serializable {
    public static final int BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT = 1;
    private SettingManager settingManager;

    private ViewPager vpFullPlayer;
    private ViewPagerAdapter adapter;
    private PageIndicatorView pageIndicatorView;

    private FullPlayerFragment fullPlayerFragment;
    private DetailFragment detailFragment;
    private LyricFragment lyricFragment;

    private ImageView imgCollapse;
    private ImageView imgMore;
    private ConstraintLayout root_view;
    private ConstraintLayout top_frame;
    private TextView txtTitle;
    private TextView txtArtist;

    private FrameLayout miniPlayer_frame;
    private Track currentTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_player);

        addControl();
        updateTheme();
        updateDataIntent();
        addMiniPlayer();
        addEvent();
    }

    private void updateDataIntent() {
        currentTrack = MediaPlaybackService.getCurrentTrack();

        if (currentTrack != null) {
            txtTitle.setText(currentTrack.getTitle());
            txtArtist.setText(currentTrack.getArtist());
        }
    }

    private void addControl() {
        settingManager = SettingManager.getInstance(this);

        vpFullPlayer = findViewById(R.id.vpFullPlayer);
        adapter = new ViewPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        pageIndicatorView = findViewById(R.id.pageIndicatorView);
        root_view = findViewById(R.id.root_view);
        top_frame = findViewById(R.id.top_frame);
        miniPlayer_frame = findViewById(R.id.miniPlayer_frame);

        fullPlayerFragment = new FullPlayerFragment();
        detailFragment = new DetailFragment();
        lyricFragment = new LyricFragment();

        adapter.addFragment(detailFragment);
        adapter.addFragment(fullPlayerFragment);
        adapter.addFragment(lyricFragment);

        vpFullPlayer.setAdapter(adapter);
        vpFullPlayer.setCurrentItem(1);
        vpFullPlayer.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position != 1) {
                    miniPlayer_frame.setVisibility(View.VISIBLE);
                } else {
                    miniPlayer_frame.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        imgCollapse = findViewById(R.id.imgCollapse);
        imgMore = findViewById(R.id.imgMore);
        txtTitle = findViewById(R.id.txtTitle);
        txtArtist = findViewById(R.id.txtArtist);
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

        imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreItem();
            }
        });
    }

    private void addMiniPlayer() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.miniPlayer_frame, new MiniPlayerFragment()).commit();
    }

    private void showMoreItem() {
        currentTrack = MediaPlaybackService.getCurrentTrack();
        TrackMoreFragment trackMoreFragment = new TrackMoreFragment(currentTrack);
        trackMoreFragment.show(getSupportFragmentManager(), "FragmentTrackMore");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayAction(UpdateTitleArtistEvent updateTitleArtistEvent) {
        txtTitle.setText(updateTitleArtistEvent.getTitle());
        txtArtist.setText(updateTitleArtistEvent.getArtist());
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        MiniPlayerFragment.isOpenedFullPlayer = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        MiniPlayerFragment.isOpenedFullPlayer = false;
    }
}
