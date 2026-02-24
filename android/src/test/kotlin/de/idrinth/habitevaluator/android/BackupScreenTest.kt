package de.idrinth.habitevaluator.android

import de.idrinth.habitevaluator.shared.backup.RestoreOptions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for backup screen logic: RestoreOptions configuration,
 * status message detection, and validation helpers.
 */
class BackupScreenTest {

    companion object {
        /**
         * Replication of error detection logic from BackupScreen.
         * Returns true if the status message indicates a failure.
         */
        fun isFailureStatus(status: String): Boolean {
            return status.contains("failed", ignoreCase = true)
        }

        /**
         * Validates that a password is non-empty for backup operations.
         */
        fun isPasswordValid(password: String): Boolean {
            return password.isNotEmpty()
        }

        /**
         * Validates that file bytes represent a potentially valid .hez file.
         * Checks for ZIP magic number (PK\x03\x04).
         */
        fun isValidHezData(data: ByteArray?): Boolean {
            if (data == null || data.size < 4) return false
            return data[0] == 0x50.toByte() && data[1] == 0x4B.toByte() &&
                    data[2] == 0x03.toByte() && data[3] == 0x04.toByte()
        }
    }

    // --- RestoreOptions configuration tests ---

    @Test
    fun testRestoreOptionsAllEnabledByDefault() {
        val options = RestoreOptions()
        assertTrue(options.isRestoreCategories)
        assertTrue(options.isRestoreHabits)
        assertTrue(options.isRestoreDiaryEntries)
        assertTrue(options.isRestoreSleepEntries)
        assertTrue(options.isRestoreSportLogs)
        assertTrue(options.isRestoreFoodLogs)
        assertTrue(options.isRestoreEmotionData)
        assertTrue(options.isRestoreMeetingEntries)
        assertTrue(options.isRestoreActivityLogs)
        assertTrue(options.isRestoreMedicationData)
        assertTrue(options.isRestoreReminderSettings)
        assertTrue(options.isRestoreModuleVisibility)
        assertTrue(options.isRestoreEmergencyPlan)
        assertTrue(options.isRestoreDayPlanner)
        assertFalse(options.isOverwrite)
    }

    @Test
    fun testRestoreOptionsAllFactoryEnabledAll() {
        val options = RestoreOptions.all()
        assertTrue(options.isRestoreCategories)
        assertTrue(options.isRestoreHabits)
        assertTrue(options.isRestoreDiaryEntries)
        assertTrue(options.isRestoreSleepEntries)
    }

    @Test
    fun testRestoreOptionsNoneFactoryDisablesAll() {
        val options = RestoreOptions.none()
        assertFalse(options.isRestoreCategories)
        assertFalse(options.isRestoreHabits)
        assertFalse(options.isRestoreDiaryEntries)
        assertFalse(options.isRestoreSleepEntries)
        assertFalse(options.isRestoreSportLogs)
        assertFalse(options.isRestoreFoodLogs)
        assertFalse(options.isRestoreEmotionData)
        assertFalse(options.isRestoreMeetingEntries)
        assertFalse(options.isRestoreActivityLogs)
        assertFalse(options.isRestoreMedicationData)
        assertFalse(options.isRestoreReminderSettings)
        assertFalse(options.isRestoreModuleVisibility)
        assertFalse(options.isRestoreEmergencyPlan)
        assertFalse(options.isRestoreDayPlanner)
        assertFalse(options.isOverwrite)
    }

    @Test
    fun testRestoreOptionsSelectiveConfiguration() {
        val options = RestoreOptions()
        options.isRestoreCategories = true
        options.isRestoreHabits = true
        options.isRestoreDiaryEntries = false
        options.isRestoreSleepEntries = false
        options.isRestoreSportLogs = false
        options.isRestoreFoodLogs = false
        options.isRestoreEmotionData = false
        options.isRestoreMeetingEntries = false
        options.isRestoreActivityLogs = false
        options.isRestoreMedicationData = false
        options.isRestoreReminderSettings = false
        options.isRestoreModuleVisibility = false
        options.isRestoreEmergencyPlan = false
        options.isRestoreDayPlanner = false

        assertTrue(options.isRestoreCategories)
        assertTrue(options.isRestoreHabits)
        assertFalse(options.isRestoreDiaryEntries)
        assertFalse(options.isRestoreSleepEntries)
        assertFalse(options.isRestoreEmotionData)
    }

    @Test
    fun testRestoreOptionsToggleIndividualFields() {
        val options = RestoreOptions.all()
        options.isRestoreCategories = false
        assertFalse(options.isRestoreCategories)
        assertTrue(options.isRestoreHabits)
        options.isRestoreCategories = true
        assertTrue(options.isRestoreCategories)
    }

    // --- Failure status detection tests ---

    @Test
    fun testIsFailureStatusWithFailedMessage() {
        assertTrue(isFailureStatus("Restore failed: Wrong password"))
    }

    @Test
    fun testIsFailureStatusCaseInsensitive() {
        assertTrue(isFailureStatus("Backup FAILED: some error"))
    }

    @Test
    fun testIsFailureStatusSuccess() {
        assertFalse(isFailureStatus("Backup saved successfully"))
    }

    @Test
    fun testIsFailureStatusEmpty() {
        assertFalse(isFailureStatus(""))
    }

    @Test
    fun testIsFailureStatusInProgress() {
        assertFalse(isFailureStatus("Restoring backup..."))
    }

    @Test
    fun testIsFailureStatusRestoreSuccess() {
        assertFalse(isFailureStatus("Restored: 5 categories, 10 habits added"))
    }

    @Test
    fun testIsFailureStatusNoChanges() {
        assertFalse(isFailureStatus("Backup restored, but no new data was found to add."))
    }

    // --- Password validation tests ---

    @Test
    fun testIsPasswordValidWithContent() {
        assertTrue(isPasswordValid("mySecretPassword"))
    }

    @Test
    fun testIsPasswordValidEmpty() {
        assertFalse(isPasswordValid(""))
    }

    @Test
    fun testIsPasswordValidSingleChar() {
        assertTrue(isPasswordValid("a"))
    }

    @Test
    fun testIsPasswordValidWithSpaces() {
        assertTrue(isPasswordValid("  "))
    }

    // --- Hez data validation tests ---

    @Test
    fun testIsValidHezDataWithZipMagicNumber() {
        val data = byteArrayOf(0x50, 0x4B, 0x03, 0x04, 0x00, 0x00)
        assertTrue(isValidHezData(data))
    }

    @Test
    fun testIsValidHezDataWithNull() {
        assertFalse(isValidHezData(null))
    }

    @Test
    fun testIsValidHezDataTooShort() {
        assertFalse(isValidHezData(byteArrayOf(0x50, 0x4B, 0x03)))
    }

    @Test
    fun testIsValidHezDataEmpty() {
        assertFalse(isValidHezData(byteArrayOf()))
    }

    @Test
    fun testIsValidHezDataWrongMagicNumber() {
        assertFalse(isValidHezData(byteArrayOf(0x00, 0x00, 0x00, 0x00)))
    }

    @Test
    fun testIsValidHezDataExactlyFourBytesValid() {
        assertTrue(isValidHezData(byteArrayOf(0x50, 0x4B, 0x03, 0x04)))
    }

    @Test
    fun testIsValidHezDataPartialMagicNumber() {
        assertFalse(isValidHezData(byteArrayOf(0x50, 0x4B, 0x00, 0x04)))
    }

    // --- Overwrite option tests ---

    @Test
    fun testOverwriteDefaultsFalse() {
        val options = RestoreOptions()
        assertFalse(options.isOverwrite)
    }

    @Test
    fun testOverwriteCanBeEnabled() {
        val options = RestoreOptions()
        options.isOverwrite = true
        assertTrue(options.isOverwrite)
    }

    @Test
    fun testOverwriteIndependentOfRestoreFlags() {
        val options = RestoreOptions.all()
        options.isOverwrite = true
        assertTrue(options.isRestoreCategories)
        assertTrue(options.isRestoreHabits)
        assertTrue(options.isOverwrite)
    }

    // --- Screen route test ---

    @Test
    fun testBackupScreenRoute() {
        assertEquals("backup", de.idrinth.habitevaluator.android.ui.navigation.Screen.Backup.route)
    }
}
