package de.idrinth.habitevaluator.android;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Constants and utilities for storage and theme settings.
 */
public final class SettingsActivity {

    public static final String PREFS_NAME = "habit_evaluator_settings";
    public static final String KEY_STORAGE_MODE = "storage_mode";
    public static final String KEY_API_URL = "api_base_url";
    public static final String KEY_API_USERNAME = "api_username";
    public static final String KEY_API_PASSWORD = "api_password";
    public static final String KEY_THEME_MODE = "theme_mode";
    public static final String MODE_LOCAL = "LOCAL";
    public static final String MODE_REMOTE = "REMOTE";
    public static final String THEME_SYSTEM = "SYSTEM";
    public static final String THEME_LIGHT = "LIGHT";
    public static final String THEME_DARK = "DARK";

    private SettingsActivity() {
    }

    public static void applyThemeMode(String themeMode) {
        if (THEME_LIGHT.equals(themeMode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (THEME_DARK.equals(themeMode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }
}
