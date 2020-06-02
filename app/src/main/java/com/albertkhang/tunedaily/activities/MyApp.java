package com.albertkhang.tunedaily.activities;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

import io.paperdb.Paper;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Paper.init(this);
    }
}
