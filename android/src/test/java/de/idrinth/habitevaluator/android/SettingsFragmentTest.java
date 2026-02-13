package de.idrinth.habitevaluator.android;

import org.junit.Test;

import static org.junit.Assert.*;

public class SettingsFragmentTest {

    @Test
    public void testDefaultHourValue() {
        assertEquals(8, SettingsFragment.DEFAULT_HOUR);
    }

    @Test
    public void testDefaultMinuteValue() {
        assertEquals(0, SettingsFragment.DEFAULT_MINUTE);
    }

    @Test
    public void testTimeSeparatorValue() {
        assertEquals(":", SettingsFragment.TIME_SEPARATOR);
    }

    // parseTimeString tests

    @Test
    public void testParseTimeStringWithValidTime() {
        int[] result = SettingsFragment.parseTimeString("14:30");
        assertEquals(14, result[0]);
        assertEquals(30, result[1]);
    }

    @Test
    public void testParseTimeStringWithMidnight() {
        int[] result = SettingsFragment.parseTimeString("00:00");
        assertEquals(0, result[0]);
        assertEquals(0, result[1]);
    }

    @Test
    public void testParseTimeStringWithEndOfDay() {
        int[] result = SettingsFragment.parseTimeString("23:59");
        assertEquals(23, result[0]);
        assertEquals(59, result[1]);
    }

    @Test
    public void testParseTimeStringWithNull() {
        int[] result = SettingsFragment.parseTimeString(null);
        assertEquals(SettingsFragment.DEFAULT_HOUR, result[0]);
        assertEquals(SettingsFragment.DEFAULT_MINUTE, result[1]);
    }

    @Test
    public void testParseTimeStringWithEmptyString() {
        int[] result = SettingsFragment.parseTimeString("");
        assertEquals(SettingsFragment.DEFAULT_HOUR, result[0]);
        assertEquals(SettingsFragment.DEFAULT_MINUTE, result[1]);
    }

    @Test
    public void testParseTimeStringWithInvalidFormat() {
        int[] result = SettingsFragment.parseTimeString("invalid");
        assertEquals(SettingsFragment.DEFAULT_HOUR, result[0]);
        assertEquals(SettingsFragment.DEFAULT_MINUTE, result[1]);
    }

    @Test
    public void testParseTimeStringWithSingleDigitHour() {
        int[] result = SettingsFragment.parseTimeString("9:05");
        assertEquals(9, result[0]);
        assertEquals(5, result[1]);
    }

    @Test
    public void testParseTimeStringReturnsTwoElements() {
        int[] result = SettingsFragment.parseTimeString("12:00");
        assertEquals(2, result.length);
    }

    @Test
    public void testParseTimeStringWithDefaultSleepReminderTime() {
        int[] result = SettingsFragment.parseTimeString(SettingsActivity.DEFAULT_SLEEP_REMINDER_TIME);
        assertEquals(8, result[0]);
        assertEquals(0, result[1]);
    }

    @Test
    public void testParseTimeStringWithDefaultDiaryReminderTime() {
        int[] result = SettingsFragment.parseTimeString(SettingsActivity.DEFAULT_DIARY_REMINDER_TIME);
        assertEquals(20, result[0]);
        assertEquals(0, result[1]);
    }

    @Test
    public void testParseTimeStringWithDefaultWakingHoursStart() {
        int[] result = SettingsFragment.parseTimeString(SettingsActivity.DEFAULT_WAKING_HOURS_START);
        assertEquals(7, result[0]);
        assertEquals(0, result[1]);
    }

    @Test
    public void testParseTimeStringWithDefaultWakingHoursEnd() {
        int[] result = SettingsFragment.parseTimeString(SettingsActivity.DEFAULT_WAKING_HOURS_END);
        assertEquals(22, result[0]);
        assertEquals(0, result[1]);
    }

    // isBackupPasswordValid tests

    @Test
    public void testIsBackupPasswordValidWithMatchingPasswords() {
        assertTrue(SettingsFragment.isBackupPasswordValid("secret123", "secret123"));
    }

    @Test
    public void testIsBackupPasswordValidWithMismatchedPasswords() {
        assertFalse(SettingsFragment.isBackupPasswordValid("secret123", "different"));
    }

    @Test
    public void testIsBackupPasswordValidWithEmptyPassword() {
        assertFalse(SettingsFragment.isBackupPasswordValid("", ""));
    }

    @Test
    public void testIsBackupPasswordValidWithNullPassword() {
        assertFalse(SettingsFragment.isBackupPasswordValid(null, "confirm"));
    }

    @Test
    public void testIsBackupPasswordValidWithNullConfirmPassword() {
        assertFalse(SettingsFragment.isBackupPasswordValid("secret", null));
    }

    @Test
    public void testIsBackupPasswordValidWithBothNull() {
        assertFalse(SettingsFragment.isBackupPasswordValid(null, null));
    }

    @Test
    public void testIsBackupPasswordValidWithSingleCharacter() {
        assertTrue(SettingsFragment.isBackupPasswordValid("a", "a"));
    }

    @Test
    public void testIsBackupPasswordValidWithEmptyPasswordNonEmptyConfirm() {
        assertFalse(SettingsFragment.isBackupPasswordValid("", "confirm"));
    }

    // areRemoteFieldsComplete tests

    @Test
    public void testAreRemoteFieldsCompleteWithAllFilled() {
        assertTrue(SettingsFragment.areRemoteFieldsComplete(
                "https://example.com", "user", "pass"));
    }

    @Test
    public void testAreRemoteFieldsCompleteWithEmptyUrl() {
        assertFalse(SettingsFragment.areRemoteFieldsComplete("", "user", "pass"));
    }

    @Test
    public void testAreRemoteFieldsCompleteWithEmptyUsername() {
        assertFalse(SettingsFragment.areRemoteFieldsComplete(
                "https://example.com", "", "pass"));
    }

    @Test
    public void testAreRemoteFieldsCompleteWithEmptyPassword() {
        assertFalse(SettingsFragment.areRemoteFieldsComplete(
                "https://example.com", "user", ""));
    }

    @Test
    public void testAreRemoteFieldsCompleteWithNullUrl() {
        assertFalse(SettingsFragment.areRemoteFieldsComplete(null, "user", "pass"));
    }

    @Test
    public void testAreRemoteFieldsCompleteWithNullUsername() {
        assertFalse(SettingsFragment.areRemoteFieldsComplete(
                "https://example.com", null, "pass"));
    }

    @Test
    public void testAreRemoteFieldsCompleteWithNullPassword() {
        assertFalse(SettingsFragment.areRemoteFieldsComplete(
                "https://example.com", "user", null));
    }

    @Test
    public void testAreRemoteFieldsCompleteWithAllNull() {
        assertFalse(SettingsFragment.areRemoteFieldsComplete(null, null, null));
    }

    @Test
    public void testAreRemoteFieldsCompleteWithAllEmpty() {
        assertFalse(SettingsFragment.areRemoteFieldsComplete("", "", ""));
    }

    @Test
    public void testAreRemoteFieldsCompleteWithWhitespaceUrl() {
        assertFalse(SettingsFragment.areRemoteFieldsComplete("   ", "user", "pass"));
    }

    @Test
    public void testAreRemoteFieldsCompleteWithWhitespaceUsername() {
        assertFalse(SettingsFragment.areRemoteFieldsComplete(
                "https://example.com", "   ", "pass"));
    }
}
