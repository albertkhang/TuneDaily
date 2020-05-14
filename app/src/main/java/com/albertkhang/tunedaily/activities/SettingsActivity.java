package com.albertkhang.tunedaily.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.views.RoundImageView;

public class SettingsActivity extends AppCompatActivity {
    private ImageView imgBack;
    private RoundImageView imgDarkTheme;
    private RoundImageView imgLightTheme;
    private ImageView imgDarkChecked;
    private ImageView imgLightChecked;
    private RelativeLayout top_frame;
    private LinearLayout root_view;
    private TextView txtTopBarHeader;
    private TextView txtThemeTitle;
    private TextView txtLanguageTitle;
    private TextView txtEnglish;
    private TextView txtVietnamese;

    private SettingManager settingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        addControl();
        addEvent();
    }

    private void addControl() {
        imgBack = findViewById(R.id.imgBack);
        imgDarkTheme = findViewById(R.id.imgDarkTheme);
        imgLightTheme = findViewById(R.id.imgLightTheme);
        imgDarkChecked = findViewById(R.id.imgDarkChecked);
        imgLightChecked = findViewById(R.id.imgLightChecked);
        top_frame = findViewById(R.id.top_frame);
        root_view = findViewById(R.id.root_view);
        txtTopBarHeader = findViewById(R.id.txtTopBarHeader);
        txtThemeTitle = findViewById(R.id.txtThemeTitle);
        txtLanguageTitle = findViewById(R.id.txtLanguageTitle);
        txtEnglish = findViewById(R.id.txtEnglish);
        txtVietnamese = findViewById(R.id.txtVietnamese);

        settingManager = new SettingManager(this);

        changeTheme();
    }

    private void addEvent() {
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgBack.setImageResource(R.drawable.ic_previous);
                finish();
            }
        });

        imgDarkTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingManager.setTheme(true);
                changeTheme();
            }
        });

        imgLightTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingManager.setTheme(false);
                changeTheme();
            }
        });

        txtEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingManager.setLanguage(true);
                changeTheme();
            }
        });

        txtVietnamese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingManager.setLanguage(false);
                changeTheme();
            }
        });
    }

    private void changeTheme() {
        if (settingManager.isDarkTheme()) {
            imgDarkChecked.setVisibility(View.VISIBLE);
            imgLightChecked.setVisibility(View.INVISIBLE);

            root_view.setBackgroundColor(getResources().getColor(R.color.colorDark1));

            top_frame.setBackgroundColor(getResources().getColor(R.color.colorDark2));
            txtTopBarHeader.setTextColor(getResources().getColor(R.color.colorLight1));
            imgBack.setColorFilter(getResources().getColor(R.color.colorLight1));

            txtThemeTitle.setTextColor(getResources().getColor(R.color.colorLight1));
            txtLanguageTitle.setTextColor(getResources().getColor(R.color.colorLight1));

            changeLanguage(R.color.colorDark5);

            imgDarkTheme.setImageResource(R.color.colorDark3);
            imgLightTheme.setImageResource(R.color.colorLight1);
        } else {
            imgDarkChecked.setVisibility(View.INVISIBLE);
            imgLightChecked.setVisibility(View.VISIBLE);

            root_view.setBackgroundColor(getResources().getColor(R.color.colorLight1));

            top_frame.setBackgroundColor(getResources().getColor(R.color.colorLight2));
            txtTopBarHeader.setTextColor(getResources().getColor(R.color.colorDark1));
            imgBack.setColorFilter(getResources().getColor(R.color.colorDark1));

            txtThemeTitle.setTextColor(getResources().getColor(R.color.colorDark1));
            txtLanguageTitle.setTextColor(getResources().getColor(R.color.colorDark1));

            changeLanguage(R.color.colorLight5);

            imgDarkTheme.setImageResource(R.color.colorDark1);
            imgLightTheme.setImageResource(R.color.colorLight2);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.animation_finish_enter, R.anim.animation_finish_leave);
    }

    private void changeLanguage(int notSelectId) {
        if (settingManager.isEnglish()) {
            txtEnglish.setTextColor(getResources().getColor(R.color.colorMain3));
            txtVietnamese.setTextColor(getResources().getColor(notSelectId));
        } else {
            txtEnglish.setTextColor(getResources().getColor(notSelectId));
            txtVietnamese.setTextColor(getResources().getColor(R.color.colorMain3));
        }
    }
}
