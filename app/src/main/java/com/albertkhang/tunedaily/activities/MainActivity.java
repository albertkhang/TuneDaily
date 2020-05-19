package com.albertkhang.tunedaily.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.adapters.ViewPagerAdapter;
import com.albertkhang.tunedaily.fragments.DiscoverFragment;
import com.albertkhang.tunedaily.fragments.LibraryFragment;
import com.albertkhang.tunedaily.fragments.MiniPlayerFragment;
import com.albertkhang.tunedaily.fragments.SearchFragment;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.utils.UpdateThemeEvent;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private ConstraintLayout splash_screen;
    private static final int SHOWING_INTERVAL = 2000;

    //This is our viewPager
    private ViewPager viewPager;
    public static final int BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT = 1;

    //Fragments
    private DiscoverFragment discoverFragment;
    private SearchFragment searchFragment;
    private LibraryFragment libraryFragment;

    private MenuItem menuItem;

    private SettingManager settingManager;
    private FrameLayout bottom_gradient_frame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addControl();
        addEvent();
    }

    private void addControl() {
        viewPager = findViewById(R.id.viewpager);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        splash_screen = findViewById(R.id.splash_screen);
        bottom_gradient_frame = findViewById(R.id.bottom_gradient_frame);

        settingManager = SettingManager.getInstance(this);

        addMiniPlayer();
    }

    private void addMiniPlayer() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.miniPlayer_frame, new MiniPlayerFragment()).commit();
    }

    private void addEvent() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.discover_item:
                        viewPager.setCurrentItem(0);
                        break;
                    case R.id.search_item:
                        viewPager.setCurrentItem(1);
                        break;
                    case R.id.library_item:
                        viewPager.setCurrentItem(2);
                        break;
                }
                return false;
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                Log.d("viewPager", "onPageScrolled: position: " + position + ", positionOffset: " + positionOffset + ", positionOffsetPixels: " + positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                if (menuItem != null) {
                    menuItem.setChecked(false);
                } else {
                    bottomNavigationView.getMenu().getItem(position).setChecked(false);
                }

                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                menuItem = bottomNavigationView.getMenu().getItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d("viewPager", "onPageScrollStateChanged: " + state);
            }
        });

        setupViewPager(viewPager);

        runSplashScreen();
    }

    private void runSplashScreen() {
        Log.d("runSplashScreen", "openMainActivity");

        splash_screen.setVisibility(View.VISIBLE);
        bottomNavigationView.setVisibility(View.GONE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("runSplashScreen", "run");

                bottomNavigationView.setVisibility(View.VISIBLE);
                splash_screen.setVisibility(View.GONE);

            }
        }, SHOWING_INTERVAL);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        discoverFragment = new DiscoverFragment();
        searchFragment = new SearchFragment();
        libraryFragment = new LibraryFragment();

        adapter.addFragment(discoverFragment);
        adapter.addFragment(searchFragment);
        adapter.addFragment(libraryFragment);
        viewPager.setAdapter(adapter);

        viewPager.setOffscreenPageLimit(adapter.getCount());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTheme();
    }

    private void updateTheme() {
        Log.d("updateTheme", "updated");
        if (settingManager.isDarkTheme()) {
            bottomNavigationView.setItemBackgroundResource(R.color.colorDark2);
            bottom_gradient_frame.setBackgroundResource(R.drawable.lyric_hidden_bottom_gradient_dark);
        } else {
            bottomNavigationView.setItemBackgroundResource(R.color.colorLight2);
            bottom_gradient_frame.setBackgroundResource(R.drawable.lyric_hidden_bottom_gradient_light);
        }

        sendUpdateThemeEvent();
    }

    private void sendUpdateThemeEvent() {
        EventBus.getDefault().post(new UpdateThemeEvent());
    }
}