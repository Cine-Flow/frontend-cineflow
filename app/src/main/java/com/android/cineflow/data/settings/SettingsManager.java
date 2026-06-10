package com.android.cineflow.data.settings;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {
    private static final String PREFS_NAME = "cineflow_settings";
    
    private static final String KEY_QUALITY = "pref_video_quality";
    private static final String KEY_AUTOPLAY = "pref_autoplay_next";
    private static final String KEY_NOTIFICATIONS = "pref_push_notifications";
    
    public static final String QUALITY_AUTO = "Tự động";
    public static final String QUALITY_FHD = "Full HD (1080p)";
    public static final String QUALITY_HD = "HD (720p)";
    public static final String QUALITY_SD = "SD (480p)";

    private static volatile SettingsManager instance;
    private final SharedPreferences prefs;

    public static void init(Context context) {
        if (instance == null) {
            synchronized (SettingsManager.class) {
                if (instance == null) {
                    instance = new SettingsManager(context.getApplicationContext());
                }
            }
        }
    }

    public static SettingsManager getInstance() {
        return instance;
    }

    private SettingsManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getVideoQuality() {
        return prefs.getString(KEY_QUALITY, QUALITY_AUTO);
    }

    public void setVideoQuality(String quality) {
        prefs.edit().putString(KEY_QUALITY, quality).apply();
    }

    public boolean isAutoplayEnabled() {
        return prefs.getBoolean(KEY_AUTOPLAY, true);
    }

    public void setAutoplayEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_AUTOPLAY, enabled).apply();
    }

    public boolean isNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATIONS, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
    }

    public int getThemeMode() {
        return prefs.getInt("pref_theme_mode", androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
    }

    public void setThemeMode(int mode) {
        prefs.edit().putInt("pref_theme_mode", mode).apply();
    }
}
