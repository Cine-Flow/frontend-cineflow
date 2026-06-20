package com.android.cineflow;

import android.app.Application;
import android.content.Context;

import com.android.cineflow.data.auth.AuthManager;
import com.android.cineflow.data.download.OfflineDownloadManager;
import com.android.cineflow.data.settings.SettingsManager;

public class CineflowApp extends Application {
    private static CineflowApp instance;

    public static CineflowApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        AuthManager.init(this);
        SettingsManager.init(this);
        OfflineDownloadManager.init(this);
        com.android.cineflow.data.network.ApiClient.init(this);
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(SettingsManager.getInstance().getThemeMode());
    }

    @Override
    protected void attachBaseContext(Context base) {
        // Initialize SettingsManager early so we can read saved language
        SettingsManager.init(base);
        super.attachBaseContext(SettingsManager.getInstance().applyLocale(base));
    }
}
