package com.albertkhang.tunedaily.activities;

import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.ScrollView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.utils.SettingManager;

import java.util.Objects;

public class InSearchActivity extends AppCompatActivity {
    private SettingManager settingManager;

    private EditText txtSearchText;
    private ScrollView scroll_view;
    private ImageView imgCollapse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_search);

        addControl();
        addEvent();
    }

    private void addControl() {
        txtSearchText = findViewById(R.id.txtSearchText);
        txtSearchText.requestFocus();
        scroll_view = findViewById(R.id.scroll_view);
        imgCollapse = findViewById(R.id.imgCollapse);
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
}
