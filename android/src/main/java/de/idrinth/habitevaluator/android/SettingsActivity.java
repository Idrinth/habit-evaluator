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
    public static final String KEY_BACKUP_LOCATION_URI = "backup_location_uri";
    public static final String KEY_FIRST_START_COMPLETED = "first_start_completed";
    public static final String KEY_FONT_SIZE = "font_size";
    public static final String KEY_SLEEP_REMINDER_ENABLED = "sleep_reminder_enabled";
    public static final String KEY_SLEEP_REMINDER_TIME = "sleep_reminder_time";
    public static final String KEY_DIARY_REMINDER_ENABLED = "diary_reminder_enabled";
    public static final String KEY_DIARY_REMINDER_TIME = "diary_reminder_time";
    public static final String KEY_EMOTION_REMINDER_ENABLED = "emotion_reminder_enabled";
    public static final String KEY_EMOTION_REMINDER_COUNT = "emotion_reminder_count";
    public static final String KEY_WAKING_HOURS_START = "waking_hours_start";
    public static final String KEY_WAKING_HOURS_END = "waking_hours_end";
    public static final String DEFAULT_SLEEP_REMINDER_TIME = "08:00";
    public static final String DEFAULT_DIARY_REMINDER_TIME = "20:00";
    public static final int DEFAULT_EMOTION_REMINDER_COUNT = 3;
    public static final String DEFAULT_WAKING_HOURS_START = "07:00";
    public static final String DEFAULT_WAKING_HOURS_END = "22:00";
    public static final String FONT_SIZE_SYSTEM = "SYSTEM";
    public static final String FONT_SIZE_XS = "XS";
    public static final String FONT_SIZE_SMALL = "SMALL";
    public static final String FONT_SIZE_NORMAL = "NORMAL";
    public static final String FONT_SIZE_LARGE = "LARGE";
    public static final String KEY_MODULE_DIARY_VISIBLE = "module_diary_visible";
    public static final String KEY_MODULE_SLEEP_VISIBLE = "module_sleep_visible";
    public static final String KEY_MODULE_EMOTIONS_VISIBLE = "module_emotions_visible";
    public static final String KEY_MODULE_POINTS_VISIBLE = "module_points_visible";
    public static final String KEY_MODULE_STATISTICS_VISIBLE = "module_statistics_visible";
    public static final String KEY_MODULE_FOOD_LOG_VISIBLE = "module_food_log_visible";
    public static final String KEY_MODULE_SPORT_LOG_VISIBLE = "module_sport_log_visible";
    public static final String KEY_MODULE_MEDICATION_VISIBLE = "module_medication_visible";
    public static final String KEY_MODULE_BACKUP_VISIBLE = "module_backup_visible";
    public static final String KEY_MODULE_PDF_EXPORT_VISIBLE = "module_pdf_export_visible";

    /**
     * Returns the font scale multiplier for the given font size setting.
     * SYSTEM = -1 (don't override), XS = 0.8, SMALL = 0.9, NORMAL = 1.0, LARGE = 1.2
     * A return value of -1 indicates the system font scale should be used.
     */
    public static float getFontScale(String fontSizeSetting) {
        if (fontSizeSetting == null || FONT_SIZE_SYSTEM.equals(fontSizeSetting)) {
            return -1f;
        }
        switch (fontSizeSetting) {
            case FONT_SIZE_XS:
                return 0.8f;
            case FONT_SIZE_SMALL:
                return 0.9f;
            case FONT_SIZE_LARGE:
                return 1.2f;
            case FONT_SIZE_NORMAL:
                return 1.0f;
            default:
                return -1f;
        }
    }

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
