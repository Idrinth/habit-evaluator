package de.idrinth.habitevaluator.android

import de.idrinth.habitevaluator.shared.model.Habit
import de.idrinth.habitevaluator.shared.model.HabitEntry
import de.idrinth.habitevaluator.shared.model.FrequencyType
import de.idrinth.habitevaluator.shared.model.User
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Tests for logic extracted from AppViewModel.
 * Since AppViewModel extends AndroidViewModel and requires Application context,
 * we test the extractable business logic separately.
 */
class AppViewModelTest {

    companion object {
        private const val PLACEHOLDER_USERNAME = "android_user"

        /**
         * Replication of copyHabitForBackup logic from AppViewModel.
         * Creates a copy of a habit for backup purposes with a different user.
         */
        fun copyHabitForBackup(source: Habit, backupUser: User): Habit {
            val habit = Habit(source.name, source.description)
            habit.frequencyType = source.frequencyType
            habit.targetFrequency = source.targetFrequency
            habit.maxEntriesPerDay = source.maxEntriesPerDay
            habit.categoryId = source.categoryId
            habit.isPositiveScoring = source.isPositiveScoring
            habit.scoringRule = source.scoringRule
            habit.user = backupUser
            for (sourceEntry in source.entries) {
                val entry = HabitEntry()
                entry.id = sourceEntry.id
                entry.completedAt = sourceEntry.completedAt
                entry.notes = sourceEntry.notes
                entry.value = sourceEntry.value
                habit.addEntry(entry)
            }
            return habit
        }

        /**
         * Replication of isRemoteModeConfigured check pattern from AppViewModel.
         * Validates that remote mode has all required fields.
         */
        fun isRemoteModeConfigured(url: String?, username: String?, password: String?): Boolean {
            return !url.isNullOrEmpty() && !username.isNullOrEmpty() && !password.isNullOrEmpty()
        }

        /**
         * Replication of storage mode detection from AppViewModel.
         */
        fun isRemoteMode(mode: String?): Boolean {
            return SettingsConstants.MODE_REMOTE == mode
        }
    }

    // --- Placeholder username tests ---

    @Test
    fun testPlaceholderUsername() {
        assertEquals("android_user", PLACEHOLDER_USERNAME)
    }

    @Test
    fun testPlaceholderUsernameIsNotEmpty() {
        assertTrue(PLACEHOLDER_USERNAME.isNotEmpty())
    }

    // --- copyHabitForBackup tests ---

    @Test
    fun testCopyHabitForBackupCopiesName() {
        val source = Habit("Morning Jog", "Daily exercise")
        val backupUser = User("backup", "pass")
        val copy = copyHabitForBackup(source, backupUser)
        assertEquals("Morning Jog", copy.name)
    }

    @Test
    fun testCopyHabitForBackupCopiesDescription() {
        val source = Habit("Morning Jog", "Daily exercise")
        val backupUser = User("backup", "pass")
        val copy = copyHabitForBackup(source, backupUser)
        assertEquals("Daily exercise", copy.description)
    }

    @Test
    fun testCopyHabitForBackupSetsBackupUser() {
        val source = Habit("Morning Jog", "Daily exercise")
        val originalUser = User("original", "pass")
        source.user = originalUser
        val backupUser = User("backup", "pass")
        val copy = copyHabitForBackup(source, backupUser)
        assertEquals("backup", copy.user.username)
        assertNotEquals("original", copy.user.username)
    }

    @Test
    fun testCopyHabitForBackupCopiesFrequencyType() {
        val source = Habit("Morning Jog", "Daily exercise")
        source.frequencyType = FrequencyType.WEEKLY
        val backupUser = User("backup", "pass")
        val copy = copyHabitForBackup(source, backupUser)
        assertEquals(FrequencyType.WEEKLY, copy.frequencyType)
    }

    @Test
    fun testCopyHabitForBackupCopiesTargetFrequency() {
        val source = Habit("Morning Jog", "Daily exercise")
        source.targetFrequency = 5
        val backupUser = User("backup", "pass")
        val copy = copyHabitForBackup(source, backupUser)
        assertEquals(5, copy.targetFrequency)
    }

    @Test
    fun testCopyHabitForBackupCopiesMaxEntriesPerDay() {
        val source = Habit("Morning Jog", "Daily exercise")
        source.maxEntriesPerDay = 3
        val backupUser = User("backup", "pass")
        val copy = copyHabitForBackup(source, backupUser)
        assertEquals(3, copy.maxEntriesPerDay)
    }

    @Test
    fun testCopyHabitForBackupCopiesCategoryId() {
        val source = Habit("Morning Jog", "Daily exercise")
        source.categoryId = "cat-123"
        val backupUser = User("backup", "pass")
        val copy = copyHabitForBackup(source, backupUser)
        assertEquals("cat-123", copy.categoryId)
    }

    @Test
    fun testCopyHabitForBackupCopiesPositiveScoring() {
        val source = Habit("Morning Jog", "Daily exercise")
        source.isPositiveScoring = false
        val backupUser = User("backup", "pass")
        val copy = copyHabitForBackup(source, backupUser)
        assertFalse(copy.isPositiveScoring)
    }

    @Test
    fun testCopyHabitForBackupCopiesPositiveScoringTrue() {
        val source = Habit("Morning Jog", "Daily exercise")
        source.isPositiveScoring = true
        val backupUser = User("backup", "pass")
        val copy = copyHabitForBackup(source, backupUser)
        assertTrue(copy.isPositiveScoring)
    }

    @Test
    fun testCopyHabitForBackupCopiesEntries() {
        val source = Habit("Morning Jog", "Daily exercise")
        val entry1 = HabitEntry()
        entry1.id = "e1"
        entry1.completedAt = LocalDateTime.of(2024, 6, 15, 8, 0)
        entry1.notes = "Felt great"
        entry1.value = 1
        source.addEntry(entry1)

        val entry2 = HabitEntry()
        entry2.id = "e2"
        entry2.completedAt = LocalDateTime.of(2024, 6, 16, 8, 0)
        entry2.notes = null
        entry2.value = 2
        source.addEntry(entry2)

        val backupUser = User("backup", "pass")
        val copy = copyHabitForBackup(source, backupUser)
        assertEquals(2, copy.entries.size)
    }

    @Test
    fun testCopyHabitForBackupPreservesEntryIds() {
        val source = Habit("Morning Jog", "Daily exercise")
        val entry = HabitEntry()
        entry.id = "entry-123"
        entry.completedAt = LocalDateTime.now()
        entry.value = 1
        source.addEntry(entry)

        val backupUser = User("backup", "pass")
        val copy = copyHabitForBackup(source, backupUser)
        assertEquals("entry-123", copy.entries.first().id)
    }

    @Test
    fun testCopyHabitForBackupPreservesEntryNotes() {
        val source = Habit("Morning Jog", "Daily exercise")
        val entry = HabitEntry()
        entry.id = "e1"
        entry.completedAt = LocalDateTime.now()
        entry.notes = "test notes"
        entry.value = 1
        source.addEntry(entry)

        val backupUser = User("backup", "pass")
        val copy = copyHabitForBackup(source, backupUser)
        assertEquals("test notes", copy.entries.first().notes)
    }

    @Test
    fun testCopyHabitForBackupWithNoEntries() {
        val source = Habit("Morning Jog", "Daily exercise")
        val backupUser = User("backup", "pass")
        val copy = copyHabitForBackup(source, backupUser)
        assertTrue(copy.entries.isEmpty())
    }

    @Test
    fun testCopyHabitForBackupWithNullDescription() {
        val source = Habit("Morning Jog", null)
        val backupUser = User("backup", "pass")
        val copy = copyHabitForBackup(source, backupUser)
        assertNull(copy.description)
    }

    @Test
    fun testCopyHabitForBackupWithNullCategoryId() {
        val source = Habit("Morning Jog", "desc")
        val backupUser = User("backup", "pass")
        val copy = copyHabitForBackup(source, backupUser)
        assertNull(copy.categoryId)
    }

    // --- Remote mode configuration tests ---

    @Test
    fun testIsRemoteModeConfiguredAllPresent() {
        assertTrue(isRemoteModeConfigured("https://api.example.com", "user", "pass"))
    }

    @Test
    fun testIsRemoteModeConfiguredNullUrl() {
        assertFalse(isRemoteModeConfigured(null, "user", "pass"))
    }

    @Test
    fun testIsRemoteModeConfiguredEmptyUrl() {
        assertFalse(isRemoteModeConfigured("", "user", "pass"))
    }

    @Test
    fun testIsRemoteModeConfiguredNullUsername() {
        assertFalse(isRemoteModeConfigured("https://api.example.com", null, "pass"))
    }

    @Test
    fun testIsRemoteModeConfiguredEmptyUsername() {
        assertFalse(isRemoteModeConfigured("https://api.example.com", "", "pass"))
    }

    @Test
    fun testIsRemoteModeConfiguredNullPassword() {
        assertFalse(isRemoteModeConfigured("https://api.example.com", "user", null))
    }

    @Test
    fun testIsRemoteModeConfiguredEmptyPassword() {
        assertFalse(isRemoteModeConfigured("https://api.example.com", "user", ""))
    }

    @Test
    fun testIsRemoteModeConfiguredAllNull() {
        assertFalse(isRemoteModeConfigured(null, null, null))
    }

    @Test
    fun testIsRemoteModeConfiguredAllEmpty() {
        assertFalse(isRemoteModeConfigured("", "", ""))
    }

    // --- Storage mode detection tests ---

    @Test
    fun testIsRemoteModeTrue() {
        assertTrue(isRemoteMode("REMOTE"))
    }

    @Test
    fun testIsRemoteModeFalseLocal() {
        assertFalse(isRemoteMode("LOCAL"))
    }

    @Test
    fun testIsRemoteModeNull() {
        assertFalse(isRemoteMode(null))
    }

    @Test
    fun testIsRemoteModeCaseSensitive() {
        assertFalse(isRemoteMode("remote"))
    }

    @Test
    fun testIsRemoteModeEmpty() {
        assertFalse(isRemoteMode(""))
    }

    // --- Habit completion logic tests ---
    // These replicate the core logic from completeHabit() to verify
    // that entry creation and daily limit checking work correctly.

    @Test
    fun testCompleteHabitAddsEntry() {
        val habit = Habit("Exercise", "Daily exercise")
        habit.maxEntriesPerDay = 1
        val sizeBefore = habit.entries.size
        if (!habit.hasReachedDailyLimit(LocalDate.now())) {
            val entry = HabitEntry()
            habit.addEntry(entry)
        }
        assertEquals(sizeBefore + 1, habit.entries.size)
    }

    @Test
    fun testCompleteHabitEntryHasCorrectTimestamp() {
        val habit = Habit("Exercise", "Daily exercise")
        val before = LocalDateTime.now()
        val entry = HabitEntry()
        habit.addEntry(entry)
        val after = LocalDateTime.now()
        assertNotNull(entry.completedAt)
        assertFalse(entry.completedAt.isBefore(before))
        assertFalse(entry.completedAt.isAfter(after))
    }

    @Test
    fun testCompleteHabitEntryHasUniqueId() {
        val habit = Habit("Exercise", "Daily exercise")
        val entry1 = HabitEntry()
        val entry2 = HabitEntry()
        habit.addEntry(entry1)
        habit.addEntry(entry2)
        assertNotEquals(entry1.id, entry2.id)
    }

    @Test
    fun testCompleteHabitRespectsMaxEntriesPerDay() {
        val habit = Habit("Exercise", "Daily exercise")
        habit.maxEntriesPerDay = 1
        // Add first entry for today
        val entry = HabitEntry()
        habit.addEntry(entry)
        // Now daily limit should be reached
        assertTrue(habit.hasReachedDailyLimit(LocalDate.now()))
    }

    @Test
    fun testCompleteHabitAllowsMultipleWhenMaxIsHigher() {
        val habit = Habit("Water", "Drink water")
        habit.maxEntriesPerDay = 3
        habit.addEntry(HabitEntry())
        habit.addEntry(HabitEntry())
        assertFalse(habit.hasReachedDailyLimit(LocalDate.now()))
        habit.addEntry(HabitEntry())
        assertTrue(habit.hasReachedDailyLimit(LocalDate.now()))
    }

    @Test
    fun testCompleteHabitNoLimitWhenMaxIsZero() {
        val habit = Habit("Exercise", "Daily exercise")
        habit.maxEntriesPerDay = 0
        habit.addEntry(HabitEntry())
        habit.addEntry(HabitEntry())
        assertFalse(habit.hasReachedDailyLimit(LocalDate.now()))
    }

    @Test
    fun testCompleteHabitEntrySizeIncrementsCorrectly() {
        val habit = Habit("Exercise", "Daily exercise")
        habit.maxEntriesPerDay = 5
        assertEquals(0, habit.entries.size)
        for (i in 1..3) {
            habit.addEntry(HabitEntry())
            assertEquals(i, habit.entries.size)
        }
    }

    @Test
    fun testCompleteHabitSetsHabitBackReference() {
        val habit = Habit("Exercise", "Daily exercise")
        val entry = HabitEntry()
        habit.addEntry(entry)
        assertSame(habit, entry.habit)
    }

    @Test
    fun testRemoveHabitCompletionRemovesLastEntry() {
        val habit = Habit("Exercise", "Daily exercise")
        habit.maxEntriesPerDay = 3
        habit.addEntry(HabitEntry())
        habit.addEntry(HabitEntry())
        assertEquals(2, habit.entries.size)
        habit.removeLastEntryForDate(LocalDate.now())
        assertEquals(1, habit.entries.size)
    }

    @Test
    fun testRemoveHabitCompletionDoesNothingWhenEmpty() {
        val habit = Habit("Exercise", "Daily exercise")
        val removed = habit.removeLastEntryForDate(LocalDate.now())
        assertFalse(removed)
        assertEquals(0, habit.entries.size)
    }

    @Test
    fun testRemoveHabitCompletionOnlyRemovesTodaysEntries() {
        val habit = Habit("Exercise", "Daily exercise")
        // Add an entry for yesterday
        val yesterdayEntry = HabitEntry()
        yesterdayEntry.completedAt = LocalDateTime.now().minusDays(1)
        habit.addEntry(yesterdayEntry)
        // Add an entry for today
        habit.addEntry(HabitEntry())
        assertEquals(2, habit.entries.size)
        // Remove today's entry
        habit.removeLastEntryForDate(LocalDate.now())
        assertEquals(1, habit.entries.size)
        // Yesterday's entry remains
        assertEquals(yesterdayEntry.id, habit.entries.first().id)
    }

    @Test
    fun testDailyLimitNotAffectedByYesterdaysEntries() {
        val habit = Habit("Exercise", "Daily exercise")
        habit.maxEntriesPerDay = 1
        val yesterdayEntry = HabitEntry()
        yesterdayEntry.completedAt = LocalDateTime.now().minusDays(1)
        habit.addEntry(yesterdayEntry)
        assertFalse(habit.hasReachedDailyLimit(LocalDate.now()))
    }
}
