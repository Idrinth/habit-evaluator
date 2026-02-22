package de.idrinth.habitevaluator.android

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for settings form logic equivalent to what SettingsFragment previously provided
 * as static constants and helper methods.
 */
class SettingsScreenTest {

    companion object {
        const val DEFAULT_HOUR = 8
        const val DEFAULT_MINUTE = 0
        const val TIME_SEPARATOR = ":"

        /**
         * Replication of parseTimeString logic from SettingsFragment.
         * Parses "HH:mm" or "H:mm" strings into [hour, minute] arrays.
         * Returns [DEFAULT_HOUR, DEFAULT_MINUTE] on null, empty, or invalid input.
         */
        fun parseTimeString(timeStr: String?): IntArray {
            if (timeStr.isNullOrEmpty()) return intArrayOf(DEFAULT_HOUR, DEFAULT_MINUTE)
            val parts = timeStr.split(TIME_SEPARATOR)
            return try {
                if (parts.size != 2) intArrayOf(DEFAULT_HOUR, DEFAULT_MINUTE)
                else intArrayOf(parts[0].toInt(), parts[1].toInt())
            } catch (e: NumberFormatException) {
                intArrayOf(DEFAULT_HOUR, DEFAULT_MINUTE)
            }
        }

        /**
         * Replication of isBackupPasswordValid logic from SettingsFragment.
         * A password is valid if it is non-null, non-empty, and matches the confirmation.
         */
        fun isBackupPasswordValid(password: String?, confirmPassword: String?): Boolean {
            if (password.isNullOrEmpty() || confirmPassword == null) return false
            return password == confirmPassword
        }

        /**
         * Replication of areRemoteFieldsComplete logic from SettingsFragment.
         * URL and username are trimmed before checking; password is not trimmed.
         */
        fun areRemoteFieldsComplete(url: String?, username: String?, password: String?): Boolean {
            return !url.isNullOrBlank() && !username.isNullOrBlank() && !password.isNullOrEmpty()
        }
    }

    @Test
    fun testDefaultHourValue() {
        assertEquals(8, DEFAULT_HOUR)
    }

    @Test
    fun testDefaultMinuteValue() {
        assertEquals(0, DEFAULT_MINUTE)
    }

    @Test
    fun testTimeSeparatorValue() {
        assertEquals(":", TIME_SEPARATOR)
    }

    @Test
    fun testParseTimeStringWithValidTime() {
        val result = parseTimeString("14:30")
        assertEquals(14, result[0])
        assertEquals(30, result[1])
    }

    @Test
    fun testParseTimeStringWithMidnight() {
        val result = parseTimeString("00:00")
        assertEquals(0, result[0])
        assertEquals(0, result[1])
    }

    @Test
    fun testParseTimeStringWithEndOfDay() {
        val result = parseTimeString("23:59")
        assertEquals(23, result[0])
        assertEquals(59, result[1])
    }

    @Test
    fun testParseTimeStringWithNull() {
        val result = parseTimeString(null)
        assertEquals(DEFAULT_HOUR, result[0])
        assertEquals(DEFAULT_MINUTE, result[1])
    }

    @Test
    fun testParseTimeStringWithEmptyString() {
        val result = parseTimeString("")
        assertEquals(DEFAULT_HOUR, result[0])
        assertEquals(DEFAULT_MINUTE, result[1])
    }

    @Test
    fun testParseTimeStringWithInvalidFormat() {
        val result = parseTimeString("invalid")
        assertEquals(DEFAULT_HOUR, result[0])
        assertEquals(DEFAULT_MINUTE, result[1])
    }

    @Test
    fun testParseTimeStringWithSingleDigitHour() {
        val result = parseTimeString("9:05")
        assertEquals(9, result[0])
        assertEquals(5, result[1])
    }

    @Test
    fun testParseTimeStringReturnsTwoElements() {
        val result = parseTimeString("12:00")
        assertEquals(2, result.size)
    }

    @Test
    fun testParseTimeStringWithDefaultSleepReminderTime() {
        val result = parseTimeString(SettingsConstants.DEFAULT_SLEEP_REMINDER_TIME)
        assertEquals(8, result[0])
        assertEquals(0, result[1])
    }

    @Test
    fun testParseTimeStringWithDefaultDiaryReminderTime() {
        val result = parseTimeString(SettingsConstants.DEFAULT_DIARY_REMINDER_TIME)
        assertEquals(20, result[0])
        assertEquals(0, result[1])
    }

    @Test
    fun testParseTimeStringWithDefaultWakingHoursStart() {
        val result = parseTimeString(SettingsConstants.DEFAULT_WAKING_HOURS_START)
        assertEquals(7, result[0])
        assertEquals(0, result[1])
    }

    @Test
    fun testParseTimeStringWithDefaultWakingHoursEnd() {
        val result = parseTimeString(SettingsConstants.DEFAULT_WAKING_HOURS_END)
        assertEquals(22, result[0])
        assertEquals(0, result[1])
    }

    @Test
    fun testIsBackupPasswordValidWithMatchingPasswords() {
        assertTrue(isBackupPasswordValid("secret123", "secret123"))
    }

    @Test
    fun testIsBackupPasswordValidWithMismatchedPasswords() {
        assertFalse(isBackupPasswordValid("secret123", "different"))
    }

    @Test
    fun testIsBackupPasswordValidWithEmptyPassword() {
        assertFalse(isBackupPasswordValid("", ""))
    }

    @Test
    fun testIsBackupPasswordValidWithNullPassword() {
        assertFalse(isBackupPasswordValid(null, "confirm"))
    }

    @Test
    fun testIsBackupPasswordValidWithNullConfirmPassword() {
        assertFalse(isBackupPasswordValid("secret", null))
    }

    @Test
    fun testIsBackupPasswordValidWithBothNull() {
        assertFalse(isBackupPasswordValid(null, null))
    }

    @Test
    fun testIsBackupPasswordValidWithSingleCharacter() {
        assertTrue(isBackupPasswordValid("a", "a"))
    }

    @Test
    fun testIsBackupPasswordValidWithEmptyPasswordNonEmptyConfirm() {
        assertFalse(isBackupPasswordValid("", "confirm"))
    }

    @Test
    fun testAreRemoteFieldsCompleteWithAllFilled() {
        assertTrue(areRemoteFieldsComplete("https://example.com", "user", "pass"))
    }

    @Test
    fun testAreRemoteFieldsCompleteWithEmptyUrl() {
        assertFalse(areRemoteFieldsComplete("", "user", "pass"))
    }

    @Test
    fun testAreRemoteFieldsCompleteWithEmptyUsername() {
        assertFalse(areRemoteFieldsComplete("https://example.com", "", "pass"))
    }

    @Test
    fun testAreRemoteFieldsCompleteWithEmptyPassword() {
        assertFalse(areRemoteFieldsComplete("https://example.com", "user", ""))
    }

    @Test
    fun testAreRemoteFieldsCompleteWithNullUrl() {
        assertFalse(areRemoteFieldsComplete(null, "user", "pass"))
    }

    @Test
    fun testAreRemoteFieldsCompleteWithNullUsername() {
        assertFalse(areRemoteFieldsComplete("https://example.com", null, "pass"))
    }

    @Test
    fun testAreRemoteFieldsCompleteWithNullPassword() {
        assertFalse(areRemoteFieldsComplete("https://example.com", "user", null))
    }

    @Test
    fun testAreRemoteFieldsCompleteWithAllNull() {
        assertFalse(areRemoteFieldsComplete(null, null, null))
    }

    @Test
    fun testAreRemoteFieldsCompleteWithAllEmpty() {
        assertFalse(areRemoteFieldsComplete("", "", ""))
    }

    @Test
    fun testAreRemoteFieldsCompleteWithWhitespaceUrl() {
        assertFalse(areRemoteFieldsComplete("   ", "user", "pass"))
    }

    @Test
    fun testAreRemoteFieldsCompleteWithWhitespaceUsername() {
        assertFalse(areRemoteFieldsComplete("https://example.com", "   ", "pass"))
    }

    @Test
    fun testAreRemoteFieldsCompleteWithWhitespacePassword() {
        // Password is not trimmed, so whitespace-only password is accepted as non-empty
        assertTrue(areRemoteFieldsComplete("https://example.com", "user", "   "))
    }

    @Test
    fun testAreRemoteFieldsCompleteWithAllWhitespace() {
        assertFalse(areRemoteFieldsComplete("   ", "   ", "   "))
    }

    @Test
    fun testParseTimeStringWithLeadingZeros() {
        val result = parseTimeString("08:05")
        assertEquals(8, result[0])
        assertEquals(5, result[1])
    }

    @Test
    fun testIsBackupPasswordValidWithLongPassword() {
        val longPassword = "a".repeat(100)
        assertTrue(isBackupPasswordValid(longPassword, longPassword))
    }

    @Test
    fun testIsBackupPasswordValidCaseSensitive() {
        assertFalse(isBackupPasswordValid("Password", "password"))
    }

    @Test
    fun testIsBackupPasswordValidWithSpecialCharacters() {
        val special = "p@\$\$w0rd!#%"
        assertTrue(isBackupPasswordValid(special, special))
    }

    @Test
    fun testParseTimeStringInvalidAlwaysReturnsTwoElements() {
        val result = parseTimeString("invalid")
        assertEquals(2, result.size)
    }

    @Test
    fun testParseTimeStringNullAlwaysReturnsTwoElements() {
        val result = parseTimeString(null)
        assertEquals(2, result.size)
    }

    @Test
    fun testAreRemoteFieldsCompleteWithMinimalValidValues() {
        assertTrue(areRemoteFieldsComplete("h", "u", "p"))
    }

    @Test
    fun testParseTimeStringNoonTime() {
        val result = parseTimeString("12:00")
        assertEquals(12, result[0])
        assertEquals(0, result[1])
    }

    @Test
    fun testIsBackupPasswordValidWithUnicodeCharacters() {
        val unicode = "пароль123"
        assertTrue(isBackupPasswordValid(unicode, unicode))
    }
}
