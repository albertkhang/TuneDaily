package com.albertkhang.tunedaily.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.albertkhang.tunedaily.R;

public class SettingManager {
    private static SettingManager instance;
    private Context context;
    private SharedPreferences prefs;

    public SettingManager(Context context) {
        this.context = context;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public SettingManager() {
    }

    public static SettingManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SettingManager.class) {
                if (instance == null) {
                    instance = new SettingManager(context);
                }
            }
        }
        return instance;
    }

    public boolean isDarkTheme() {
        return prefs.getBoolean(context.getResources().getString(R.string.isDarkTheme), true);
    }

    public void setTheme(boolean isDarkTheme) {
        SharedPreferences.Editor editor = prefs.edit();

        if (isDarkTheme) {
            editor.putBoolean(context.getResources().getString(R.string.isDarkTheme), true);
        } else {
            editor.putBoolean(context.getResources().getString(R.string.isDarkTheme), false);
        }

        editor.apply();
    }

    public boolean isEnglish() {
        return prefs.getBoolean(context.getResources().getString(R.string.isEnglish), true);
    }

    public void setLanguage(boolean isEnglish) {
        SharedPreferences.Editor editor = prefs.edit();

        if (isEnglish) {
            editor.putBoolean(context.getResources().getString(R.string.isEnglish), true);
        } else {
            editor.putBoolean(context.getResources().getString(R.string.isEnglish), false);
        }

        editor.apply();
    }
}
