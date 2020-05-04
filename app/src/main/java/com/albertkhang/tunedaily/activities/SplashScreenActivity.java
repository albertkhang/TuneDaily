package com.albertkhang.tunedaily.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.albertkhang.tunedaily.R;

public class SplashScreenActivity extends AppCompatActivity {
    private String LOG_TAG = "SplashScreenActivity";
    private static final int SHOWING_INTERVAL = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        openMainActivity();
    }

    private void openMainActivity() {
        Log.d(LOG_TAG, "openMainActivity");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, "run");
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, SHOWING_INTERVAL);
    }
}
