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
    public static final String KEY_LANGUAGE = "language";
    public static final String MODE_LOCAL = "LOCAL";
    public static final String MODE_REMOTE = "REMOTE";
    public static final String THEME_SYSTEM = "SYSTEM";
    public static final String THEME_LIGHT = "LIGHT";
    public static final String THEME_DARK = "DARK";
    public static final String LANGUAGE_SYSTEM = "system";
    public static final String LANGUAGE_EN = "en";
    public static final String LANGUAGE_DE = "de";
    public static final String LANGUAGE_ES = "es";
    public static final String LANGUAGE_FR = "fr";

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

    /**
     * Resolves the effective language code. If set to "system", uses the device locale,
     * falling back to "en" if the system language is not supported.
     */
    public static String getEffectiveLanguage(String languageSetting) {
        if (languageSetting == null || LANGUAGE_SYSTEM.equals(languageSetting)) {
            String systemLang = java.util.Locale.getDefault().getLanguage();
            if (LANGUAGE_EN.equals(systemLang) || LANGUAGE_DE.equals(systemLang)
                    || LANGUAGE_ES.equals(systemLang) || LANGUAGE_FR.equals(systemLang)) {
                return systemLang;
            }
            return LANGUAGE_EN;
        }
        return languageSetting;
    }
}
