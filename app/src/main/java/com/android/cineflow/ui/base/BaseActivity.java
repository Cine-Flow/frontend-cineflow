package com.android.cineflow.ui.base;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.android.cineflow.data.settings.SettingsManager;

public abstract class BaseActivity extends AppCompatActivity {
    private String currentLanguage;

    @Override
    protected void attachBaseContext(Context newBase) {
        SettingsManager.init(newBase);
        super.attachBaseContext(SettingsManager.getInstance().applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentLanguage = SettingsManager.getInstance().getLanguage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String lang = SettingsManager.getInstance().getLanguage();
        if (currentLanguage != null && !lang.equals(currentLanguage)) {
            currentLanguage = lang;
            recreate();
        }
    }
}
