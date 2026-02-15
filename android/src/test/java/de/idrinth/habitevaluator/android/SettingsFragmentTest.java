package de.idrinth.habitevaluator.android;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SettingsFragmentTest {

    @Test
    void testDefaultHourValue() {
        assertEquals(8, SettingsFragment.DEFAULT_HOUR);
    }

    @Test
    void testDefaultMinuteValue() {
        assertEquals(0, SettingsFragment.DEFAULT_MINUTE);
    }

    @Test
    void testTimeSeparatorValue() {
        assertEquals(":", SettingsFragment.TIME_SEPARATOR);
    }

    // parseTimeString tests

    @Test
    void testParseTimeStringWithValidTime() {
        int[] result = SettingsFragment.parseTimeString("14:30");
        assertEquals(14, result[0]);
        assertEquals(30, result[1]);
    }

    @Test
    void testParseTimeStringWithMidnight() {
        int[] result = SettingsFragment.parseTimeString("00:00");
        assertEquals(0, result[0]);
        assertEquals(0, result[1]);
    }

    @Test
    void testParseTimeStringWithEndOfDay() {
        int[] result = SettingsFragment.parseTimeString("23:59");
        assertEquals(23, result[0]);
        assertEquals(59, result[1]);
    }

    @Test
    void testParseTimeStringWithNull() {
        int[] result = SettingsFragment.parseTimeString(null);
        assertEquals(SettingsFragment.DEFAULT_HOUR, result[0]);
        assertEquals(SettingsFragment.DEFAULT_MINUTE, result[1]);
    }

    @Test
    void testParseTimeStringWithEmptyString() {
        int[] result = SettingsFragment.parseTimeString("");
        assertEquals(SettingsFragment.DEFAULT_HOUR, result[0]);
        assertEquals(SettingsFragment.DEFAULT_MINUTE, result[1]);
    }

    @Test
    void testParseTimeStringWithInvalidFormat() {
        int[] result = SettingsFragment.parseTimeString("invalid");
        assertEquals(SettingsFragment.DEFAULT_HOUR, result[0]);
        assertEquals(SettingsFragment.DEFAULT_MINUTE, result[1]);
    }

    @Test
    void testParseTimeStringWithSingleDigitHour() {
        int[] result = SettingsFragment.parseTimeString("9:05");
        assertEquals(9, result[0]);
        assertEquals(5, result[1]);
    }

    @Test
    void testParseTimeStringReturnsTwoElements() {
        int[] result = SettingsFragment.parseTimeString("12:00");
        assertEquals(2, result.length);
    }

    @Test
    void testParseTimeStringWithDefaultSleepReminderTime() {
        int[] result = SettingsFragment.parseTimeString(SettingsActivity.DEFAULT_SLEEP_REMINDER_TIME);
        assertEquals(8, result[0]);
        assertEquals(0, result[1]);
    }

    @Test
    void testParseTimeStringWithDefaultDiaryReminderTime() {
        int[] result = SettingsFragment.parseTimeString(SettingsActivity.DEFAULT_DIARY_REMINDER_TIME);
        assertEquals(20, result[0]);
        assertEquals(0, result[1]);
    }

    @Test
    void testParseTimeStringWithDefaultWakingHoursStart() {
        int[] result = SettingsFragment.parseTimeString(SettingsActivity.DEFAULT_WAKING_HOURS_START);
        assertEquals(7, result[0]);
        assertEquals(0, result[1]);
    }

    @Test
    void testParseTimeStringWithDefaultWakingHoursEnd() {
        int[] result = SettingsFragment.parseTimeString(SettingsActivity.DEFAULT_WAKING_HOURS_END);
        assertEquals(22, result[0]);
        assertEquals(0, result[1]);
    }

    // isBackupPasswordValid tests

    @Test
    void testIsBackupPasswordValidWithMatchingPasswords() {
        assertTrue(SettingsFragment.isBackupPasswordValid("secret123", "secret123"));
    }

    @Test
    void testIsBackupPasswordValidWithMismatchedPasswords() {
        assertFalse(SettingsFragment.isBackupPasswordValid("secret123", "different"));
    }

    @Test
    void testIsBackupPasswordValidWithEmptyPassword() {
        assertFalse(SettingsFragment.isBackupPasswordValid("", ""));
    }

    @Test
    void testIsBackupPasswordValidWithNullPassword() {
        assertFalse(SettingsFragment.isBackupPasswordValid(null, "confirm"));
    }

    @Test
    void testIsBackupPasswordValidWithNullConfirmPassword() {
        assertFalse(SettingsFragment.isBackupPasswordValid("secret", null));
    }

    @Test
    void testIsBackupPasswordValidWithBothNull() {
        assertFalse(SettingsFragment.isBackupPasswordValid(null, null));
    }

    @Test
    void testIsBackupPasswordValidWithSingleCharacter() {
        assertTrue(SettingsFragment.isBackupPasswordValid("a", "a"));
    }

    @Test
    void testIsBackupPasswordValidWithEmptyPasswordNonEmptyConfirm() {
        assertFalse(SettingsFragment.isBackupPasswordValid("", "confirm"));
    }

    // areRemoteFieldsComplete tests

    @Test
    void testAreRemoteFieldsCompleteWithAllFilled() {
        assertTrue(SettingsFragment.areRemoteFieldsComplete(
                "https://example.com", "user", "pass"));
    }

    @Test
    void testAreRemoteFieldsCompleteWithEmptyUrl() {
        assertFalse(SettingsFragment.areRemoteFieldsComplete("", "user", "pass"));
    }

    @Test
    void testAreRemoteFieldsCompleteWithEmptyUsername() {
        assertFalse(SettingsFragment.areRemoteFieldsComplete(
                "https://example.com", "", "pass"));
    }

    @Test
    void testAreRemoteFieldsCompleteWithEmptyPassword() {
        assertFalse(SettingsFragment.areRemoteFieldsComplete(
                "https://example.com", "user", ""));
    }

    @Test
    void testAreRemoteFieldsCompleteWithNullUrl() {
        assertFalse(SettingsFragment.areRemoteFieldsComplete(null, "user", "pass"));
    }

    @Test
    void testAreRemoteFieldsCompleteWithNullUsername() {
        assertFalse(SettingsFragment.areRemoteFieldsComplete(
                "https://example.com", null, "pass"));
    }

    @Test
    void testAreRemoteFieldsCompleteWithNullPassword() {
        assertFalse(SettingsFragment.areRemoteFieldsComplete(
                "https://example.com", "user", null));
    }

    @Test
    void testAreRemoteFieldsCompleteWithAllNull() {
        assertFalse(SettingsFragment.areRemoteFieldsComplete(null, null, null));
    }

    @Test
    void testAreRemoteFieldsCompleteWithAllEmpty() {
        assertFalse(SettingsFragment.areRemoteFieldsComplete("", "", ""));
    }

    @Test
    void testAreRemoteFieldsCompleteWithWhitespaceUrl() {
        assertFalse(SettingsFragment.areRemoteFieldsComplete("   ", "user", "pass"));
    }

    @Test
    void testAreRemoteFieldsCompleteWithWhitespaceUsername() {
        assertFalse(SettingsFragment.areRemoteFieldsComplete(
                "https://example.com", "   ", "pass"));
    }

    @Test
    void testAreRemoteFieldsCompleteWithWhitespacePassword() {
        // Password is not trimmed in the implementation, so whitespace is accepted
        assertTrue(SettingsFragment.areRemoteFieldsComplete(
                "https://example.com", "user", "   "));
    }

    @Test
    void testAreRemoteFieldsCompleteWithAllWhitespace() {
        // URL and username are trimmed, so whitespace-only fails for them
        assertFalse(SettingsFragment.areRemoteFieldsComplete("   ", "   ", "   "));
    }

    @Test
    void testParseTimeStringWithLeadingZeros() {
        int[] result = SettingsFragment.parseTimeString("08:05");
        assertEquals(8, result[0]);
        assertEquals(5, result[1]);
    }

    @Test
    void testIsBackupPasswordValidWithLongPassword() {
        String longPassword = "a".repeat(100);
        assertTrue(SettingsFragment.isBackupPasswordValid(longPassword, longPassword));
    }

    @Test
    void testIsBackupPasswordValidCaseSensitive() {
        assertFalse(SettingsFragment.isBackupPasswordValid("Password", "password"));
    }

    @Test
    void testIsBackupPasswordValidWithSpecialCharacters() {
        String special = "p@$$w0rd!#%";
        assertTrue(SettingsFragment.isBackupPasswordValid(special, special));
    }

    @Test
    void testParseTimeStringInvalidAlwaysReturnsTwoElements() {
        int[] result = SettingsFragment.parseTimeString("invalid");
        assertEquals(2, result.length);
    }

    @Test
    void testParseTimeStringNullAlwaysReturnsTwoElements() {
        int[] result = SettingsFragment.parseTimeString(null);
        assertEquals(2, result.length);
    }

    @Test
    void testAreRemoteFieldsCompleteWithMinimalValidValues() {
        assertTrue(SettingsFragment.areRemoteFieldsComplete("h", "u", "p"));
    }

    @Test
    void testParseTimeStringNoonTime() {
        int[] result = SettingsFragment.parseTimeString("12:00");
        assertEquals(12, result[0]);
        assertEquals(0, result[1]);
    }

    @Test
    void testIsBackupPasswordValidWithUnicodeCharacters() {
        String unicode = "пароль123";
        assertTrue(SettingsFragment.isBackupPasswordValid(unicode, unicode));
    }
}
