package com.android.cineflow;

import android.app.Application;

import com.android.cineflow.data.auth.AuthManager;
import com.android.cineflow.data.settings.SettingsManager;

public class CineflowApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AuthManager.init(this);
        SettingsManager.init(this);
    }
}
