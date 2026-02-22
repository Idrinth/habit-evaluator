package de.idrinth.habitevaluator.android

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for the SettingsConstants object that replaced SettingsActivity.
 * Verifies all constant values are correct and stable.
 * Migrated from SettingsActivityTest.java.
 */
class SettingsConstantsTest {

    @Test
    fun testGetFontScaleSystem() {
        assertEquals(-1f, SettingsConstants.getFontScale(SettingsConstants.FONT_SIZE_SYSTEM), 0.001f)
    }

    @Test
    fun testGetFontScaleNull() {
        assertEquals(-1f, SettingsConstants.getFontScale(null), 0.001f)
    }

    @Test
    fun testGetFontScaleXs() {
        assertEquals(0.8f, SettingsConstants.getFontScale(SettingsConstants.FONT_SIZE_XS), 0.001f)
    }

    @Test
    fun testGetFontScaleSmall() {
        assertEquals(0.9f, SettingsConstants.getFontScale(SettingsConstants.FONT_SIZE_SMALL), 0.001f)
    }

    @Test
    fun testGetFontScaleNormal() {
        assertEquals(1.0f, SettingsConstants.getFontScale(SettingsConstants.FONT_SIZE_NORMAL), 0.001f)
    }

    @Test
    fun testGetFontScaleLarge() {
        assertEquals(1.2f, SettingsConstants.getFontScale(SettingsConstants.FONT_SIZE_LARGE), 0.001f)
    }

    @Test
    fun testGetFontScaleUnknown() {
        assertEquals(-1f, SettingsConstants.getFontScale("UNKNOWN"), 0.001f)
    }

    @Test
    fun testGetEffectiveLanguageNull() {
        val result = SettingsConstants.getEffectiveLanguage(null)
        assertNotNull(result)
    }

    @Test
    fun testGetEffectiveLanguageSystem() {
        val result = SettingsConstants.getEffectiveLanguage(SettingsConstants.LANGUAGE_SYSTEM)
        assertNotNull(result)
        assertTrue(
            result == "en" || result == "de" || result == "es" || result == "fr"
        )
    }

    @Test
    fun testGetEffectiveLanguageEn() {
        assertEquals("en", SettingsConstants.getEffectiveLanguage(SettingsConstants.LANGUAGE_EN))
    }

    @Test
    fun testGetEffectiveLanguageDe() {
        assertEquals("de", SettingsConstants.getEffectiveLanguage(SettingsConstants.LANGUAGE_DE))
    }

    @Test
    fun testGetEffectiveLanguageEs() {
        assertEquals("es", SettingsConstants.getEffectiveLanguage(SettingsConstants.LANGUAGE_ES))
    }

    @Test
    fun testGetEffectiveLanguageFr() {
        assertEquals("fr", SettingsConstants.getEffectiveLanguage(SettingsConstants.LANGUAGE_FR))
    }

    @Test
    fun testPrefsNameConstant() {
        assertEquals("habit_evaluator_settings", SettingsConstants.PREFS_NAME)
    }

    @Test
    fun testStorageModeConstants() {
        assertEquals("LOCAL", SettingsConstants.MODE_LOCAL)
        assertEquals("REMOTE", SettingsConstants.MODE_REMOTE)
    }

    @Test
    fun testThemeModeConstants() {
        assertEquals("SYSTEM", SettingsConstants.THEME_SYSTEM)
        assertEquals("LIGHT", SettingsConstants.THEME_LIGHT)
        assertEquals("DARK", SettingsConstants.THEME_DARK)
    }

    @Test
    fun testLanguageConstants() {
        assertEquals("system", SettingsConstants.LANGUAGE_SYSTEM)
        assertEquals("en", SettingsConstants.LANGUAGE_EN)
        assertEquals("de", SettingsConstants.LANGUAGE_DE)
        assertEquals("es", SettingsConstants.LANGUAGE_ES)
        assertEquals("fr", SettingsConstants.LANGUAGE_FR)
    }

    @Test
    fun testFontSizeConstants() {
        assertEquals("SYSTEM", SettingsConstants.FONT_SIZE_SYSTEM)
        assertEquals("XS", SettingsConstants.FONT_SIZE_XS)
        assertEquals("SMALL", SettingsConstants.FONT_SIZE_SMALL)
        assertEquals("NORMAL", SettingsConstants.FONT_SIZE_NORMAL)
        assertEquals("LARGE", SettingsConstants.FONT_SIZE_LARGE)
    }

    @Test
    fun testDefaultReminderValues() {
        assertEquals("08:00", SettingsConstants.DEFAULT_SLEEP_REMINDER_TIME)
        assertEquals("20:00", SettingsConstants.DEFAULT_DIARY_REMINDER_TIME)
        assertEquals(3, SettingsConstants.DEFAULT_EMOTION_REMINDER_COUNT)
        assertEquals("07:00", SettingsConstants.DEFAULT_WAKING_HOURS_START)
        assertEquals("22:00", SettingsConstants.DEFAULT_WAKING_HOURS_END)
    }

    @Test
    fun testModuleVisibilityKeyConstants() {
        assertNotNull(SettingsConstants.KEY_MODULE_DIARY_VISIBLE)
        assertNotNull(SettingsConstants.KEY_MODULE_SLEEP_VISIBLE)
        assertNotNull(SettingsConstants.KEY_MODULE_EMOTIONS_VISIBLE)
        assertNotNull(SettingsConstants.KEY_MODULE_POINTS_VISIBLE)
        assertNotNull(SettingsConstants.KEY_MODULE_STATISTICS_VISIBLE)
        assertNotNull(SettingsConstants.KEY_MODULE_FOOD_LOG_VISIBLE)
        assertNotNull(SettingsConstants.KEY_MODULE_SPORT_LOG_VISIBLE)
        assertNotNull(SettingsConstants.KEY_MODULE_MEDICATION_VISIBLE)
        assertNotNull(SettingsConstants.KEY_MODULE_BACKUP_VISIBLE)
        assertNotNull(SettingsConstants.KEY_MODULE_PDF_EXPORT_VISIBLE)
    }

    @Test
    fun testModuleVisibilityKeysAreUnique() {
        val keys = listOf(
            SettingsConstants.KEY_MODULE_DIARY_VISIBLE,
            SettingsConstants.KEY_MODULE_SLEEP_VISIBLE,
            SettingsConstants.KEY_MODULE_EMOTIONS_VISIBLE,
            SettingsConstants.KEY_MODULE_POINTS_VISIBLE,
            SettingsConstants.KEY_MODULE_STATISTICS_VISIBLE,
            SettingsConstants.KEY_MODULE_FOOD_LOG_VISIBLE,
            SettingsConstants.KEY_MODULE_SPORT_LOG_VISIBLE,
            SettingsConstants.KEY_MODULE_MEDICATION_VISIBLE,
            SettingsConstants.KEY_MODULE_BACKUP_VISIBLE,
            SettingsConstants.KEY_MODULE_PDF_EXPORT_VISIBLE
        )
        assertEquals(keys.size, keys.toSet().size, "All module visibility keys must be unique")
    }

    @Test
    fun testSettingsKeyConstants() {
        assertEquals("storage_mode", SettingsConstants.KEY_STORAGE_MODE)
        assertEquals("api_base_url", SettingsConstants.KEY_API_URL)
        assertEquals("api_username", SettingsConstants.KEY_API_USERNAME)
        assertEquals("api_password", SettingsConstants.KEY_API_PASSWORD)
        assertEquals("theme_mode", SettingsConstants.KEY_THEME_MODE)
        assertEquals("language", SettingsConstants.KEY_LANGUAGE)
        assertEquals("custom_translations_enabled", SettingsConstants.KEY_CUSTOM_TRANSLATIONS)
        assertEquals("backup_enabled", SettingsConstants.KEY_BACKUP_ENABLED)
        assertEquals("backup_password", SettingsConstants.KEY_BACKUP_PASSWORD)
        assertEquals("backup_location_uri", SettingsConstants.KEY_BACKUP_LOCATION_URI)
        assertEquals("first_start_completed", SettingsConstants.KEY_FIRST_START_COMPLETED)
        assertEquals("font_size", SettingsConstants.KEY_FONT_SIZE)
    }

    @Test
    fun testReminderKeyConstants() {
        assertEquals("sleep_reminder_enabled", SettingsConstants.KEY_SLEEP_REMINDER_ENABLED)
        assertEquals("sleep_reminder_time", SettingsConstants.KEY_SLEEP_REMINDER_TIME)
        assertEquals("diary_reminder_enabled", SettingsConstants.KEY_DIARY_REMINDER_ENABLED)
        assertEquals("diary_reminder_time", SettingsConstants.KEY_DIARY_REMINDER_TIME)
        assertEquals("emotion_reminder_enabled", SettingsConstants.KEY_EMOTION_REMINDER_ENABLED)
        assertEquals("emotion_reminder_count", SettingsConstants.KEY_EMOTION_REMINDER_COUNT)
        assertEquals("waking_hours_start", SettingsConstants.KEY_WAKING_HOURS_START)
        assertEquals("waking_hours_end", SettingsConstants.KEY_WAKING_HOURS_END)
    }

    @Test
    fun testGetFontScaleEmptyString() {
        assertEquals(-1f, SettingsConstants.getFontScale(""), 0.001f)
    }

    @Test
    fun testGetFontScaleCaseSensitive() {
        // "xs" is not the same as "XS"
        assertEquals(-1f, SettingsConstants.getFontScale("xs"), 0.001f)
    }

    @Test
    fun testGetEffectiveLanguageEmptyString() {
        val result = SettingsConstants.getEffectiveLanguage("")
        assertNotNull(result)
    }

    @Test
    fun testGetEffectiveLanguageUnknown() {
        val result = SettingsConstants.getEffectiveLanguage("xx")
        assertNotNull(result)
    }

    @Test
    fun testGetEffectiveLanguageSupportedLanguagesAreConsistent() {
        assertEquals("en", SettingsConstants.getEffectiveLanguage(SettingsConstants.LANGUAGE_EN))
        assertEquals("de", SettingsConstants.getEffectiveLanguage(SettingsConstants.LANGUAGE_DE))
        assertEquals("es", SettingsConstants.getEffectiveLanguage(SettingsConstants.LANGUAGE_ES))
        assertEquals("fr", SettingsConstants.getEffectiveLanguage(SettingsConstants.LANGUAGE_FR))
    }

    @Test
    fun testGetFontScaleReturnsDistinctValuesForEachSize() {
        val xs = SettingsConstants.getFontScale(SettingsConstants.FONT_SIZE_XS)
        val small = SettingsConstants.getFontScale(SettingsConstants.FONT_SIZE_SMALL)
        val normal = SettingsConstants.getFontScale(SettingsConstants.FONT_SIZE_NORMAL)
        val large = SettingsConstants.getFontScale(SettingsConstants.FONT_SIZE_LARGE)
        assertTrue(xs < small)
        assertTrue(small < normal)
        assertTrue(normal < large)
    }

    @Test
    fun testGetFontScaleNormalIsOne() {
        assertEquals(1.0f, SettingsConstants.getFontScale(SettingsConstants.FONT_SIZE_NORMAL), 0.001f)
    }

    @Test
    fun testDefaultReminderTimesAreParseable() {
        // Verify default reminder times follow HH:mm format and parse to valid hour/minute ranges
        val sleepParts = SettingsConstants.DEFAULT_SLEEP_REMINDER_TIME.split(":")
        assertEquals(2, sleepParts.size)
        val sleepHour = sleepParts[0].toInt()
        val sleepMinute = sleepParts[1].toInt()
        assertTrue(sleepHour in 0..23)
        assertTrue(sleepMinute in 0..59)

        val diaryParts = SettingsConstants.DEFAULT_DIARY_REMINDER_TIME.split(":")
        assertEquals(2, diaryParts.size)
        val diaryHour = diaryParts[0].toInt()
        val diaryMinute = diaryParts[1].toInt()
        assertTrue(diaryHour in 0..23)
        assertTrue(diaryMinute in 0..59)
    }

    @Test
    fun testDefaultEmotionReminderCountIsPositive() {
        assertTrue(SettingsConstants.DEFAULT_EMOTION_REMINDER_COUNT > 0)
    }

    @Test
    fun testWakingHoursStartIsBeforeEnd() {
        val startParts = SettingsConstants.DEFAULT_WAKING_HOURS_START.split(":")
        val endParts = SettingsConstants.DEFAULT_WAKING_HOURS_END.split(":")
        val startHour = startParts[0].toInt()
        val endHour = endParts[0].toInt()
        assertTrue(startHour < endHour, "Waking hours start should be before end")
    }

    @Test
    fun testAllSettingsKeysAreNonEmpty() {
        assertFalse(SettingsConstants.KEY_STORAGE_MODE.isEmpty())
        assertFalse(SettingsConstants.KEY_API_URL.isEmpty())
        assertFalse(SettingsConstants.KEY_API_USERNAME.isEmpty())
        assertFalse(SettingsConstants.KEY_API_PASSWORD.isEmpty())
        assertFalse(SettingsConstants.KEY_THEME_MODE.isEmpty())
        assertFalse(SettingsConstants.KEY_LANGUAGE.isEmpty())
        assertFalse(SettingsConstants.KEY_FONT_SIZE.isEmpty())
    }
}
