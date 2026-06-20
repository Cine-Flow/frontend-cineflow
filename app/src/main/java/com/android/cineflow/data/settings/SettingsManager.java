package com.android.cineflow.data.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

public class SettingsManager {
    private static final String PREFS_NAME = "cineflow_settings";
    
    private static final String KEY_QUALITY = "pref_video_quality";
    private static final String KEY_AUTOPLAY = "pref_autoplay_next";
    private static final String KEY_NOTIFICATIONS = "pref_push_notifications";
    private static final String KEY_LANGUAGE = "pref_language";
    
    public static final String QUALITY_AUTO = "Tự động";
    public static final String QUALITY_FHD = "Full HD (1080p)";
    public static final String QUALITY_HD = "HD (720p)";
    public static final String QUALITY_SD = "SD (480p)";

    public static final String LANG_VIETNAMESE = "vi";
    public static final String LANG_ENGLISH = "en";

    private static volatile SettingsManager instance;
    private final SharedPreferences prefs;

    public static void init(Context context) {
        if (instance == null) {
            synchronized (SettingsManager.class) {
                if (instance == null) {
                    // getApplicationContext() may return null during attachBaseContext(),
                    // so fall back to using the provided context directly.
                    Context appContext = context.getApplicationContext();
                    instance = new SettingsManager(appContext != null ? appContext : context);
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

    // --- Language / Locale ---

    public String getLanguage() {
        return prefs.getString(KEY_LANGUAGE, LANG_VIETNAMESE);
    }

    public void setLanguage(String langCode) {
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply();
    }

    /**
     * Applies the saved locale to the given context.
     * Call this in Application.onCreate() and Activity.attachBaseContext()
     * to ensure the locale is consistently applied.
     */
    public Context applyLocale(Context context) {
        String lang = getLanguage();
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(new LocaleList(locale));
        } else {
            config.setLocale(locale);
        }

        return context.createConfigurationContext(config);
    }
}
