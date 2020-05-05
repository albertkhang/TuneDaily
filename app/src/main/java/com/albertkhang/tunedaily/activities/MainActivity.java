package com.albertkhang.tunedaily.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.adapters.ViewPagerAdapter;
import com.albertkhang.tunedaily.fragments.HomeFragment;
import com.albertkhang.tunedaily.fragments.LibraryFragment;
import com.albertkhang.tunedaily.fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    //This is our viewPager
    private ViewPager viewPager;
    public static final int BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT = 1;

    //Fragments
    HomeFragment homeFragment;
    SearchFragment searchFragment;
    LibraryFragment libraryFragment;

    MenuItem prevMenuItem;

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
    }

    private void addEvent() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.fragment_home:
                        viewPager.setCurrentItem(0);
                        break;
                    case R.id.fragment_search:
                        viewPager.setCurrentItem(1);
                        break;
                    case R.id.fragment_library:
                        viewPager.setCurrentItem(2);
                        break;
                }
                return false;
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }

                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                prevMenuItem = bottomNavigationView.getMenu().getItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        setupViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        homeFragment = new HomeFragment();
        searchFragment = new SearchFragment();
        libraryFragment = new LibraryFragment();

        adapter.addFragment(homeFragment);
        adapter.addFragment(searchFragment);
        adapter.addFragment(libraryFragment);
        viewPager.setAdapter(adapter);
    }
}
