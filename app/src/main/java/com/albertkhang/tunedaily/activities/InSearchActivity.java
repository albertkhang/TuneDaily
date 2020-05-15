package com.albertkhang.tunedaily.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.utils.SettingManager;

import java.util.Objects;

public class InSearchActivity extends AppCompatActivity {
    private SettingManager settingManager;

    private EditText txtSearchText;
    private ScrollView scroll_view;
    private ImageView imgCollapse;
    private RelativeLayout root_view;
    private ConstraintLayout top_frame;
    private ImageView imgClose;
    private TextView txtArtist;
    private TextView txtSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_search);

        addControl();
        addEvent();
    }

    private void addControl() {
        settingManager = SettingManager.getInstance(this);

        txtSearchText = findViewById(R.id.txtSearchText);
        txtSearchText.requestFocus();
        scroll_view = findViewById(R.id.scroll_view);
        imgCollapse = findViewById(R.id.imgCollapse);
        root_view = findViewById(R.id.root_view);
        top_frame = findViewById(R.id.top_frame);
        imgClose = findViewById(R.id.imgClose);
        txtArtist = findViewById(R.id.txtArtist);
        txtSongs = findViewById(R.id.txtSongs);

        updateTheme();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addEvent() {
        scroll_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideSoftKeyboard();
                txtSearchText.clearFocus();
                scroll_view.requestFocus();
                return false;
            }
        });

        imgCollapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null && this.getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void updateTheme() {
        if (settingManager.isDarkTheme()) {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorDark1));

            top_frame.setBackgroundColor(getResources().getColor(R.color.colorDark2));
            imgCollapse.setColorFilter(getResources().getColor(R.color.colorLight5));
            txtSearchText.setTextColor(getResources().getColor(R.color.colorLight1));
            imgClose.setColorFilter(getResources().getColor(R.color.colorLight5));

            txtArtist.setTextColor(getResources().getColor(R.color.colorLight1));
            txtSongs.setTextColor(getResources().getColor(R.color.colorLight1));
        } else {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorLight1));

            top_frame.setBackgroundColor(getResources().getColor(R.color.colorLight2));
            imgCollapse.setColorFilter(getResources().getColor(R.color.colorDark5));
            txtSearchText.setTextColor(getResources().getColor(R.color.colorDark1));
            imgClose.setColorFilter(getResources().getColor(R.color.colorDark5));

            txtArtist.setTextColor(getResources().getColor(R.color.colorDark1));
            txtSongs.setTextColor(getResources().getColor(R.color.colorDark1));
        }
    }
}
