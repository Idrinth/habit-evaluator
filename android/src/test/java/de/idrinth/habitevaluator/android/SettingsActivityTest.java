package de.idrinth.habitevaluator.android;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SettingsActivityTest {

    @Test
    void testGetFontScaleSystem() {
        assertEquals(-1f, SettingsActivity.getFontScale(SettingsActivity.FONT_SIZE_SYSTEM), 0.001f);
    }

    @Test
    void testGetFontScaleNull() {
        assertEquals(-1f, SettingsActivity.getFontScale(null), 0.001f);
    }

    @Test
    void testGetFontScaleXs() {
        assertEquals(0.8f, SettingsActivity.getFontScale(SettingsActivity.FONT_SIZE_XS), 0.001f);
    }

    @Test
    void testGetFontScaleSmall() {
        assertEquals(0.9f, SettingsActivity.getFontScale(SettingsActivity.FONT_SIZE_SMALL), 0.001f);
    }

    @Test
    void testGetFontScaleNormal() {
        assertEquals(1.0f, SettingsActivity.getFontScale(SettingsActivity.FONT_SIZE_NORMAL), 0.001f);
    }

    @Test
    void testGetFontScaleLarge() {
        assertEquals(1.2f, SettingsActivity.getFontScale(SettingsActivity.FONT_SIZE_LARGE), 0.001f);
    }

    @Test
    void testGetFontScaleUnknown() {
        assertEquals(-1f, SettingsActivity.getFontScale("UNKNOWN"), 0.001f);
    }

    @Test
    void testGetEffectiveLanguageNull() {
        String result = SettingsActivity.getEffectiveLanguage(null);
        assertNotNull(result);
    }

    @Test
    void testGetEffectiveLanguageSystem() {
        String result = SettingsActivity.getEffectiveLanguage(SettingsActivity.LANGUAGE_SYSTEM);
        assertNotNull(result);
        assertTrue(
                result.equals("en") || result.equals("de")
                        || result.equals("es") || result.equals("fr")
        );
    }

    @Test
    void testGetEffectiveLanguageEn() {
        assertEquals("en", SettingsActivity.getEffectiveLanguage(SettingsActivity.LANGUAGE_EN));
    }

    @Test
    void testGetEffectiveLanguageDe() {
        assertEquals("de", SettingsActivity.getEffectiveLanguage(SettingsActivity.LANGUAGE_DE));
    }

    @Test
    void testGetEffectiveLanguageEs() {
        assertEquals("es", SettingsActivity.getEffectiveLanguage(SettingsActivity.LANGUAGE_ES));
    }

    @Test
    void testGetEffectiveLanguageFr() {
        assertEquals("fr", SettingsActivity.getEffectiveLanguage(SettingsActivity.LANGUAGE_FR));
    }

    @Test
    void testPrefsNameConstant() {
        assertEquals("habit_evaluator_settings", SettingsActivity.PREFS_NAME);
    }

    @Test
    void testStorageModeConstants() {
        assertEquals("LOCAL", SettingsActivity.MODE_LOCAL);
        assertEquals("REMOTE", SettingsActivity.MODE_REMOTE);
    }

    @Test
    void testThemeModeConstants() {
        assertEquals("SYSTEM", SettingsActivity.THEME_SYSTEM);
        assertEquals("LIGHT", SettingsActivity.THEME_LIGHT);
        assertEquals("DARK", SettingsActivity.THEME_DARK);
    }

    @Test
    void testLanguageConstants() {
        assertEquals("system", SettingsActivity.LANGUAGE_SYSTEM);
        assertEquals("en", SettingsActivity.LANGUAGE_EN);
        assertEquals("de", SettingsActivity.LANGUAGE_DE);
        assertEquals("es", SettingsActivity.LANGUAGE_ES);
        assertEquals("fr", SettingsActivity.LANGUAGE_FR);
    }

    @Test
    void testFontSizeConstants() {
        assertEquals("SYSTEM", SettingsActivity.FONT_SIZE_SYSTEM);
        assertEquals("XS", SettingsActivity.FONT_SIZE_XS);
        assertEquals("SMALL", SettingsActivity.FONT_SIZE_SMALL);
        assertEquals("NORMAL", SettingsActivity.FONT_SIZE_NORMAL);
        assertEquals("LARGE", SettingsActivity.FONT_SIZE_LARGE);
    }

    @Test
    void testDefaultReminderValues() {
        assertEquals("08:00", SettingsActivity.DEFAULT_SLEEP_REMINDER_TIME);
        assertEquals("20:00", SettingsActivity.DEFAULT_DIARY_REMINDER_TIME);
        assertEquals(3, SettingsActivity.DEFAULT_EMOTION_REMINDER_COUNT);
        assertEquals("07:00", SettingsActivity.DEFAULT_WAKING_HOURS_START);
        assertEquals("22:00", SettingsActivity.DEFAULT_WAKING_HOURS_END);
    }

    @Test
    void testModuleVisibilityKeyConstants() {
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
    void testModuleVisibilityKeysAreUnique() {
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
                        keys[i], keys[j],
                        "Module visibility keys " + keys[i] + " and " + keys[j] + " should be unique");
            }
        }
    }

    @Test
    void testSettingsKeyConstants() {
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
    void testReminderKeyConstants() {
        assertEquals("sleep_reminder_enabled", SettingsActivity.KEY_SLEEP_REMINDER_ENABLED);
        assertEquals("sleep_reminder_time", SettingsActivity.KEY_SLEEP_REMINDER_TIME);
        assertEquals("diary_reminder_enabled", SettingsActivity.KEY_DIARY_REMINDER_ENABLED);
        assertEquals("diary_reminder_time", SettingsActivity.KEY_DIARY_REMINDER_TIME);
        assertEquals("emotion_reminder_enabled", SettingsActivity.KEY_EMOTION_REMINDER_ENABLED);
        assertEquals("emotion_reminder_count", SettingsActivity.KEY_EMOTION_REMINDER_COUNT);
        assertEquals("waking_hours_start", SettingsActivity.KEY_WAKING_HOURS_START);
        assertEquals("waking_hours_end", SettingsActivity.KEY_WAKING_HOURS_END);
    }

    @Test
    void testGetFontScaleEmptyString() {
        assertEquals(-1f, SettingsActivity.getFontScale(""), 0.001f);
    }

    @Test
    void testGetFontScaleCaseSensitive() {
        // "xs" is not the same as "XS"
        assertEquals(-1f, SettingsActivity.getFontScale("xs"), 0.001f);
    }

    @Test
    void testGetEffectiveLanguageEmptyString() {
        String result = SettingsActivity.getEffectiveLanguage("");
        assertNotNull(result);
    }

    @Test
    void testGetEffectiveLanguageUnknown() {
        String result = SettingsActivity.getEffectiveLanguage("xx");
        assertNotNull(result);
    }

    @Test
    void testGetEffectiveLanguageSupportedLanguagesAreConsistent() {
        assertEquals("en", SettingsActivity.getEffectiveLanguage(SettingsActivity.LANGUAGE_EN));
        assertEquals("de", SettingsActivity.getEffectiveLanguage(SettingsActivity.LANGUAGE_DE));
        assertEquals("es", SettingsActivity.getEffectiveLanguage(SettingsActivity.LANGUAGE_ES));
        assertEquals("fr", SettingsActivity.getEffectiveLanguage(SettingsActivity.LANGUAGE_FR));
    }

    @Test
    void testGetFontScaleReturnsDistinctValuesForEachSize() {
        float xs = SettingsActivity.getFontScale(SettingsActivity.FONT_SIZE_XS);
        float small = SettingsActivity.getFontScale(SettingsActivity.FONT_SIZE_SMALL);
        float normal = SettingsActivity.getFontScale(SettingsActivity.FONT_SIZE_NORMAL);
        float large = SettingsActivity.getFontScale(SettingsActivity.FONT_SIZE_LARGE);
        assertTrue(xs < small);
        assertTrue(small < normal);
        assertTrue(normal < large);
    }

    @Test
    void testGetFontScaleNormalIsOne() {
        assertEquals(1.0f, SettingsActivity.getFontScale(SettingsActivity.FONT_SIZE_NORMAL), 0.001f);
    }

    @Test
    void testDefaultReminderTimesAreParseable() {
        // Verify default reminder times can be parsed by SettingsFragment.parseTimeString
        int[] sleepTime = SettingsFragment.parseTimeString(SettingsActivity.DEFAULT_SLEEP_REMINDER_TIME);
        assertTrue(sleepTime[0] >= 0 && sleepTime[0] <= 23);
        assertTrue(sleepTime[1] >= 0 && sleepTime[1] <= 59);

        int[] diaryTime = SettingsFragment.parseTimeString(SettingsActivity.DEFAULT_DIARY_REMINDER_TIME);
        assertTrue(diaryTime[0] >= 0 && diaryTime[0] <= 23);
        assertTrue(diaryTime[1] >= 0 && diaryTime[1] <= 59);
    }

    @Test
    void testDefaultEmotionReminderCountIsPositive() {
        assertTrue(SettingsActivity.DEFAULT_EMOTION_REMINDER_COUNT > 0);
    }

    @Test
    void testWakingHoursStartIsBeforeEnd() {
        int[] start = SettingsFragment.parseTimeString(SettingsActivity.DEFAULT_WAKING_HOURS_START);
        int[] end = SettingsFragment.parseTimeString(SettingsActivity.DEFAULT_WAKING_HOURS_END);
        assertTrue(start[0] < end[0], "Waking hours start should be before end");
    }

    @Test
    void testAllSettingsKeysAreNonEmpty() {
        assertFalse(SettingsActivity.KEY_STORAGE_MODE.isEmpty());
        assertFalse(SettingsActivity.KEY_API_URL.isEmpty());
        assertFalse(SettingsActivity.KEY_API_USERNAME.isEmpty());
        assertFalse(SettingsActivity.KEY_API_PASSWORD.isEmpty());
        assertFalse(SettingsActivity.KEY_THEME_MODE.isEmpty());
        assertFalse(SettingsActivity.KEY_LANGUAGE.isEmpty());
        assertFalse(SettingsActivity.KEY_FONT_SIZE.isEmpty());
    }
}
