package com.albertkhang.tunedaily.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.preference.PreferenceManager;

import com.albertkhang.tunedaily.R;

import java.util.Locale;

public class SettingManager {
    private static SettingManager instance;
    private Context context;
    private SharedPreferences prefs;

    public interface LANGUAGE {
        String VI = "vi";
        String EN = "en";
    }

    private SettingManager(Context context) {
        this.context = context;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
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

    public void setLanguageSP(boolean isEnglish) {
        SharedPreferences.Editor editor = prefs.edit();

        if (isEnglish) {
            editor.putBoolean(context.getResources().getString(R.string.isEnglish), true);
        } else {
            editor.putBoolean(context.getResources().getString(R.string.isEnglish), false);
        }

        editor.apply();
    }

    public void updateLanguageConfiguration() {
        Locale locale;

        if (isEnglish()) {
            locale = new Locale(LANGUAGE.EN);
            Locale.setDefault(locale);
        } else {
            locale = new Locale(LANGUAGE.VI);
            Locale.setDefault(locale);
        }

        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }
}
