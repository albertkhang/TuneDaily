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
                setTheme(true);
                changeTheme();
            }
        });

        imgLightTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTheme(false);
                changeTheme();
            }
        });

        txtEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLanguage(true);
                changeTheme();
            }
        });

        txtVietnamese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLanguage(false);
                changeTheme();
            }
        });
    }

    private void changeTheme() {
        if (isDarkTheme()) {
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

    private boolean isDarkTheme() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getBoolean(getString(R.string.isDarkTheme), true);
    }

    private void setTheme(boolean isDarkTheme) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        if (isDarkTheme) {
            editor.putBoolean(getString(R.string.isDarkTheme), true);
        } else {
            editor.putBoolean(getString(R.string.isDarkTheme), false);
        }

        editor.apply();
    }

    private void changeLanguage(int notSelectId) {
        if (isEnglish()) {
            txtEnglish.setTextColor(getResources().getColor(R.color.colorMain3));
            txtVietnamese.setTextColor(getResources().getColor(notSelectId));
        } else {
            txtEnglish.setTextColor(getResources().getColor(notSelectId));
            txtVietnamese.setTextColor(getResources().getColor(R.color.colorMain3));
        }
    }

    private boolean isEnglish() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getBoolean(getString(R.string.isEnglish), true);
    }

    private void setLanguage(boolean isEnglish) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        if (isEnglish) {
            editor.putBoolean(getString(R.string.isEnglish), true);
        } else {
            editor.putBoolean(getString(R.string.isEnglish), false);
        }

        editor.apply();
    }
}
