package de.idrinth.habitevaluator.android;

import org.junit.Test;

import static org.junit.Assert.*;

public class SettingsActivityTest {

    @Test
    public void testGetFontScaleSystem() {
        assertEquals(-1f, SettingsActivity.getFontScale(SettingsActivity.FONT_SIZE_SYSTEM), 0.001f);
    }

    @Test
    public void testGetFontScaleNull() {
        assertEquals(-1f, SettingsActivity.getFontScale(null), 0.001f);
    }

    @Test
    public void testGetFontScaleXs() {
        assertEquals(0.8f, SettingsActivity.getFontScale(SettingsActivity.FONT_SIZE_XS), 0.001f);
    }

    @Test
    public void testGetFontScaleSmall() {
        assertEquals(0.9f, SettingsActivity.getFontScale(SettingsActivity.FONT_SIZE_SMALL), 0.001f);
    }

    @Test
    public void testGetFontScaleNormal() {
        assertEquals(1.0f, SettingsActivity.getFontScale(SettingsActivity.FONT_SIZE_NORMAL), 0.001f);
    }

    @Test
    public void testGetFontScaleLarge() {
        assertEquals(1.2f, SettingsActivity.getFontScale(SettingsActivity.FONT_SIZE_LARGE), 0.001f);
    }

    @Test
    public void testGetFontScaleUnknown() {
        assertEquals(-1f, SettingsActivity.getFontScale("UNKNOWN"), 0.001f);
    }

    @Test
    public void testGetEffectiveLanguageNull() {
        String result = SettingsActivity.getEffectiveLanguage(null);
        assertNotNull(result);
    }

    @Test
    public void testGetEffectiveLanguageSystem() {
        String result = SettingsActivity.getEffectiveLanguage(SettingsActivity.LANGUAGE_SYSTEM);
        assertNotNull(result);
        assertTrue(
                result.equals("en") || result.equals("de")
                        || result.equals("es") || result.equals("fr")
        );
    }

    @Test
    public void testGetEffectiveLanguageEn() {
        assertEquals("en", SettingsActivity.getEffectiveLanguage(SettingsActivity.LANGUAGE_EN));
    }

    @Test
    public void testGetEffectiveLanguageDe() {
        assertEquals("de", SettingsActivity.getEffectiveLanguage(SettingsActivity.LANGUAGE_DE));
    }

    @Test
    public void testGetEffectiveLanguageEs() {
        assertEquals("es", SettingsActivity.getEffectiveLanguage(SettingsActivity.LANGUAGE_ES));
    }

    @Test
    public void testGetEffectiveLanguageFr() {
        assertEquals("fr", SettingsActivity.getEffectiveLanguage(SettingsActivity.LANGUAGE_FR));
    }

    @Test
    public void testPrefsNameConstant() {
        assertEquals("habit_evaluator_settings", SettingsActivity.PREFS_NAME);
    }

    @Test
    public void testStorageModeConstants() {
        assertEquals("LOCAL", SettingsActivity.MODE_LOCAL);
        assertEquals("REMOTE", SettingsActivity.MODE_REMOTE);
    }

    @Test
    public void testThemeModeConstants() {
        assertEquals("SYSTEM", SettingsActivity.THEME_SYSTEM);
        assertEquals("LIGHT", SettingsActivity.THEME_LIGHT);
        assertEquals("DARK", SettingsActivity.THEME_DARK);
    }

    @Test
    public void testLanguageConstants() {
        assertEquals("system", SettingsActivity.LANGUAGE_SYSTEM);
        assertEquals("en", SettingsActivity.LANGUAGE_EN);
        assertEquals("de", SettingsActivity.LANGUAGE_DE);
        assertEquals("es", SettingsActivity.LANGUAGE_ES);
        assertEquals("fr", SettingsActivity.LANGUAGE_FR);
    }

    @Test
    public void testFontSizeConstants() {
        assertEquals("SYSTEM", SettingsActivity.FONT_SIZE_SYSTEM);
        assertEquals("XS", SettingsActivity.FONT_SIZE_XS);
        assertEquals("SMALL", SettingsActivity.FONT_SIZE_SMALL);
        assertEquals("NORMAL", SettingsActivity.FONT_SIZE_NORMAL);
        assertEquals("LARGE", SettingsActivity.FONT_SIZE_LARGE);
    }

    @Test
    public void testDefaultReminderValues() {
        assertEquals("08:00", SettingsActivity.DEFAULT_SLEEP_REMINDER_TIME);
        assertEquals("20:00", SettingsActivity.DEFAULT_DIARY_REMINDER_TIME);
        assertEquals(3, SettingsActivity.DEFAULT_EMOTION_REMINDER_COUNT);
        assertEquals("07:00", SettingsActivity.DEFAULT_WAKING_HOURS_START);
        assertEquals("22:00", SettingsActivity.DEFAULT_WAKING_HOURS_END);
    }

    @Test
    public void testModuleVisibilityKeyConstants() {
        assertNotNull(SettingsActivity.KEY_MODULE_DIARY_VISIBLE);
        assertNotNull(SettingsActivity.KEY_MODULE_SLEEP_VISIBLE);
        assertNotNull(SettingsActivity.KEY_MODULE_EMOTIONS_VISIBLE);
        assertNotNull(SettingsActivity.KEY_MODULE_POINTS_VISIBLE);
        assertNotNull(SettingsActivity.KEY_MODULE_STATISTICS_VISIBLE);
        assertNotNull(SettingsActivity.KEY_MODULE_FOOD_LOG_VISIBLE);
        assertNotNull(SettingsActivity.KEY_MODULE_SPORT_LOG_VISIBLE);
        assertNotNull(SettingsActivity.KEY_MODULE_MEDICATION_VISIBLE);
        assertNotNull(SettingsActivity.KEY_MODULE_BACKUP_VISIBLE);
        assertNotNull(SettingsActivity.KEY_MODULE_PDF_EXPORT_VISIBLE);
    }

    @Test
    public void testModuleVisibilityKeysAreUnique() {
        String[] keys = {
                SettingsActivity.KEY_MODULE_DIARY_VISIBLE,
                SettingsActivity.KEY_MODULE_SLEEP_VISIBLE,
                SettingsActivity.KEY_MODULE_EMOTIONS_VISIBLE,
                SettingsActivity.KEY_MODULE_POINTS_VISIBLE,
                SettingsActivity.KEY_MODULE_STATISTICS_VISIBLE,
                SettingsActivity.KEY_MODULE_FOOD_LOG_VISIBLE,
                SettingsActivity.KEY_MODULE_SPORT_LOG_VISIBLE,
                SettingsActivity.KEY_MODULE_MEDICATION_VISIBLE,
                SettingsActivity.KEY_MODULE_BACKUP_VISIBLE,
                SettingsActivity.KEY_MODULE_PDF_EXPORT_VISIBLE
        };
        for (int i = 0; i < keys.length; i++) {
            for (int j = i + 1; j < keys.length; j++) {
                assertNotEquals(
                        "Module visibility keys " + keys[i] + " and " + keys[j] + " should be unique",
                        keys[i], keys[j]);
            }
        }
    }

    @Test
    public void testSettingsKeyConstants() {
        assertEquals("storage_mode", SettingsActivity.KEY_STORAGE_MODE);
        assertEquals("api_base_url", SettingsActivity.KEY_API_URL);
        assertEquals("api_username", SettingsActivity.KEY_API_USERNAME);
        assertEquals("api_password", SettingsActivity.KEY_API_PASSWORD);
        assertEquals("theme_mode", SettingsActivity.KEY_THEME_MODE);
        assertEquals("language", SettingsActivity.KEY_LANGUAGE);
        assertEquals("custom_translations_enabled", SettingsActivity.KEY_CUSTOM_TRANSLATIONS);
        assertEquals("backup_enabled", SettingsActivity.KEY_BACKUP_ENABLED);
        assertEquals("backup_password", SettingsActivity.KEY_BACKUP_PASSWORD);
        assertEquals("first_start_completed", SettingsActivity.KEY_FIRST_START_COMPLETED);
        assertEquals("font_size", SettingsActivity.KEY_FONT_SIZE);
    }

    @Test
    public void testReminderKeyConstants() {
        assertEquals("sleep_reminder_enabled", SettingsActivity.KEY_SLEEP_REMINDER_ENABLED);
        assertEquals("sleep_reminder_time", SettingsActivity.KEY_SLEEP_REMINDER_TIME);
        assertEquals("diary_reminder_enabled", SettingsActivity.KEY_DIARY_REMINDER_ENABLED);
        assertEquals("diary_reminder_time", SettingsActivity.KEY_DIARY_REMINDER_TIME);
        assertEquals("emotion_reminder_enabled", SettingsActivity.KEY_EMOTION_REMINDER_ENABLED);
        assertEquals("emotion_reminder_count", SettingsActivity.KEY_EMOTION_REMINDER_COUNT);
        assertEquals("waking_hours_start", SettingsActivity.KEY_WAKING_HOURS_START);
        assertEquals("waking_hours_end", SettingsActivity.KEY_WAKING_HOURS_END);
    }
}
