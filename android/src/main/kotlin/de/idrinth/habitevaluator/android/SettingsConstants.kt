package de.idrinth.habitevaluator.android

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object SettingsConstants {
    const val PREFS_NAME = "habit_evaluator_settings"
    const val KEY_STORAGE_MODE = "storage_mode"
    const val KEY_API_URL = "api_base_url"
    const val KEY_API_USERNAME = "api_username"
    const val KEY_API_PASSWORD = "api_password"
    const val KEY_THEME_MODE = "theme_mode"
    const val KEY_LANGUAGE = "language"
    const val MODE_LOCAL = "LOCAL"
    const val MODE_REMOTE = "REMOTE"
    const val THEME_SYSTEM = "SYSTEM"
    const val THEME_LIGHT = "LIGHT"
    const val THEME_DARK = "DARK"
    const val LANGUAGE_SYSTEM = "system"
    const val LANGUAGE_EN = "en"
    const val LANGUAGE_DE = "de"
    const val LANGUAGE_ES = "es"
    const val LANGUAGE_FR = "fr"
    const val KEY_CUSTOM_TRANSLATIONS = "custom_translations_enabled"
    const val KEY_BACKUP_ENABLED = "backup_enabled"
    const val KEY_BACKUP_PASSWORD = "backup_password"
    const val KEY_BACKUP_LOCATION_URI = "backup_location_uri"
    const val KEY_FIRST_START_COMPLETED = "first_start_completed"
    const val KEY_FONT_SIZE = "font_size"
    const val KEY_SLEEP_REMINDER_ENABLED = "sleep_reminder_enabled"
    const val KEY_SLEEP_REMINDER_TIME = "sleep_reminder_time"
    const val KEY_DIARY_REMINDER_ENABLED = "diary_reminder_enabled"
    const val KEY_DIARY_REMINDER_TIME = "diary_reminder_time"
    const val KEY_EMOTION_REMINDER_ENABLED = "emotion_reminder_enabled"
    const val KEY_EMOTION_REMINDER_COUNT = "emotion_reminder_count"
    const val KEY_GRATITUDE_REMINDER_ENABLED = "gratitude_reminder_enabled"
    const val KEY_GRATITUDE_REMINDER_TIME = "gratitude_reminder_time"
    const val KEY_WAKING_HOURS_START = "waking_hours_start"
    const val KEY_WAKING_HOURS_END = "waking_hours_end"
    const val DEFAULT_SLEEP_REMINDER_TIME = "08:00"
    const val DEFAULT_DIARY_REMINDER_TIME = "20:00"
    const val DEFAULT_GRATITUDE_REMINDER_TIME = "08:00"
    const val DEFAULT_EMOTION_REMINDER_COUNT = 3
    const val DEFAULT_WAKING_HOURS_START = "07:00"
    const val DEFAULT_WAKING_HOURS_END = "22:00"
    const val FONT_SIZE_SYSTEM = "SYSTEM"
    const val FONT_SIZE_XS = "XS"
    const val FONT_SIZE_SMALL = "SMALL"
    const val FONT_SIZE_NORMAL = "NORMAL"
    const val FONT_SIZE_LARGE = "LARGE"
    const val KEY_MODULE_DIARY_VISIBLE = "module_diary_visible"
    const val KEY_MODULE_SLEEP_VISIBLE = "module_sleep_visible"
    const val KEY_MODULE_EMOTIONS_VISIBLE = "module_emotions_visible"
    const val KEY_MODULE_POINTS_VISIBLE = "module_points_visible"
    const val KEY_MODULE_STATISTICS_VISIBLE = "module_statistics_visible"
    const val KEY_MODULE_FOOD_LOG_VISIBLE = "module_food_log_visible"
    const val KEY_MODULE_SPORT_LOG_VISIBLE = "module_sport_log_visible"
    const val KEY_MODULE_MEDICATION_VISIBLE = "module_medication_visible"
    const val KEY_MODULE_BACKUP_VISIBLE = "module_backup_visible"
    const val KEY_MODULE_PDF_EXPORT_VISIBLE = "module_pdf_export_visible"
    const val KEY_MODULE_ACTIVITY_LOG_VISIBLE = "module_activity_log_visible"
    const val KEY_MODULE_DAY_PLANNER_VISIBLE = "module_day_planner_visible"

    fun getFontScale(fontSizeSetting: String?): Float = when (fontSizeSetting) {
        FONT_SIZE_XS -> 0.8f
        FONT_SIZE_SMALL -> 0.9f
        FONT_SIZE_NORMAL -> 1.0f
        FONT_SIZE_LARGE -> 1.2f
        else -> -1f
    }

    fun applyThemeMode(themeMode: String?) {
        when (themeMode) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun applyLanguage(languageSetting: String?) {
        if (languageSetting == null || languageSetting == LANGUAGE_SYSTEM) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageSetting))
        }
    }

    fun getEffectiveLanguage(languageSetting: String?): String {
        if (languageSetting == null || languageSetting == LANGUAGE_SYSTEM) {
            val systemLang = Locale.getDefault().language
            return if (systemLang in listOf(LANGUAGE_EN, LANGUAGE_DE, LANGUAGE_ES, LANGUAGE_FR)) {
                systemLang
            } else {
                LANGUAGE_EN
            }
        }
        return languageSetting
    }
}
