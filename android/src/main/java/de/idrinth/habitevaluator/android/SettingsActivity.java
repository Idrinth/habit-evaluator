package de.idrinth.habitevaluator.android;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

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
    public static final String KEY_CUSTOM_TRANSLATIONS = "custom_translations_enabled";
    public static final String KEY_BACKUP_ENABLED = "backup_enabled";
    public static final String KEY_BACKUP_PASSWORD = "backup_password";
    public static final String KEY_FIRST_START_COMPLETED = "first_start_completed";

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
     * Applies the given language setting to the app using per-app language support.
     * For "system", clears the override so the system locale is used.
     * For a specific language code, sets that locale as the app locale.
     */
    public static void applyLanguage(String languageSetting) {
        if (languageSetting == null || LANGUAGE_SYSTEM.equals(languageSetting)) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList());
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageSetting));
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
