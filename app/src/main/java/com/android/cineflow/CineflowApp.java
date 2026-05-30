package com.android.cineflow;

import android.app.Application;

import com.android.cineflow.data.auth.AuthManager;

public class CineflowApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AuthManager.init(this);
    }
}
