package de.idrinth.habitevaluator.shared.backup;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MergeResultTest {

    @Test
    void testConstructorAndGetters() {
        MergeResult result = new MergeResult(2, 3, 1, 5, 4, 6);
        assertEquals(2, result.getCategoriesAdded());
        assertEquals(3, result.getHabitsAdded());
        assertEquals(1, result.getHabitsMerged());
        assertEquals(5, result.getEntriesAdded());
        assertEquals(4, result.getDiaryEntriesAdded());
        assertEquals(6, result.getSleepEntriesAdded());
    }

    @Test
    void testGetTotalChanges() {
        MergeResult result = new MergeResult(2, 3, 1, 5, 4, 6);
        // totalChanges = categoriesAdded + habitsAdded + habitsMerged + diaryEntriesAdded + sleepEntriesAdded
        assertEquals(2 + 3 + 1 + 4 + 6, result.getTotalChanges());
    }

    @Test
    void testGetTotalChangesAllZeros() {
        MergeResult result = new MergeResult(0, 0, 0, 0, 0, 0);
        assertEquals(0, result.getTotalChanges());
    }

    @Test
    void testToString() {
        MergeResult result = new MergeResult(1, 2, 3, 4, 5, 6);
        String str = result.toString();
        assertTrue(str.contains("categories added=1"));
        assertTrue(str.contains("habits added=2"));
        assertTrue(str.contains("habits merged=3"));
        assertTrue(str.contains("entries added=4"));
        assertTrue(str.contains("diary entries added=5"));
        assertTrue(str.contains("sleep entries added=6"));
    }

    @Test
    void testFullConstructorWithEmotionAndReminder() {
        MergeResult result = new MergeResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, true);
        assertEquals(1, result.getCategoriesAdded());
        assertEquals(2, result.getHabitsAdded());
        assertEquals(3, result.getHabitsMerged());
        assertEquals(4, result.getEntriesAdded());
        assertEquals(5, result.getDiaryEntriesAdded());
        assertEquals(6, result.getSleepEntriesAdded());
        assertEquals(7, result.getSportLogsAdded());
        assertEquals(8, result.getFoodLogsAdded());
        assertEquals(9, result.getEmotionPairsAdded());
        assertEquals(10, result.getEmotionEntriesAdded());
        assertTrue(result.isReminderSettingsRestored());
    }

    @Test
    void testTotalChangesIncludesEmotionAndReminder() {
        MergeResult result = new MergeResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, true);
        // totalChanges = categories(1) + habits(2) + merged(3) + diary(5) + sleep(6)
        //   + sport(7) + food(8) + emotionPairs(9) + emotionEntries(10) + reminder(1)
        assertEquals(1 + 2 + 3 + 5 + 6 + 7 + 8 + 9 + 10 + 1, result.getTotalChanges());
    }

    @Test
    void testToStringIncludesEmotionAndReminder() {
        MergeResult result = new MergeResult(0, 0, 0, 0, 0, 0, 0, 0, 3, 7, true);
        String str = result.toString();
        assertTrue(str.contains("emotion pairs added=3"));
        assertTrue(str.contains("emotion entries added=7"));
        assertTrue(str.contains("reminder settings restored=true"));
    }

    @Test
    void testSportLogConstructor() {
        MergeResult result = new MergeResult(1, 2, 3, 4, 5, 6, 7);
        assertEquals(7, result.getSportLogsAdded());
        assertEquals(0, result.getFoodLogsAdded());
    }

    @Test
    void testSportAndFoodLogConstructor() {
        MergeResult result = new MergeResult(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(7, result.getSportLogsAdded());
        assertEquals(8, result.getFoodLogsAdded());
        assertEquals(0, result.getEmotionPairsAdded());
        assertFalse(result.isReminderSettingsRestored());
    }

    @Test
    void testFullConstructorWithMeetingAndMedication() {
        MergeResult result = new MergeResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, true);
        assertEquals(1, result.getCategoriesAdded());
        assertEquals(2, result.getHabitsAdded());
        assertEquals(3, result.getHabitsMerged());
        assertEquals(4, result.getEntriesAdded());
        assertEquals(5, result.getDiaryEntriesAdded());
        assertEquals(6, result.getSleepEntriesAdded());
        assertEquals(7, result.getSportLogsAdded());
        assertEquals(8, result.getFoodLogsAdded());
        assertEquals(9, result.getEmotionPairsAdded());
        assertEquals(10, result.getEmotionEntriesAdded());
        assertEquals(11, result.getMeetingEntriesAdded());
        assertEquals(12, result.getActivityLogsAdded());
        assertEquals(13, result.getMedicationsAdded());
        assertEquals(14, result.getMedicationLogsAdded());
        assertTrue(result.isReminderSettingsRestored());
    }

    @Test
    void testTotalChangesIncludesMeetingAndMedication() {
        MergeResult result = new MergeResult(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 3, 7, false);
        // meeting(5) + medications(3) + medicationLogs(7) = 15
        assertEquals(15, result.getTotalChanges());
    }

    @Test
    void testTotalChangesExcludesEntriesAdded() {
        // entriesAdded (habit entries) is deliberately excluded from getTotalChanges()
        MergeResult result = new MergeResult(0, 0, 0, 100, 0, 0);
        assertEquals(0, result.getTotalChanges());
    }

    @Test
    void testToStringIncludesMeetingAndMedication() {
        MergeResult result = new MergeResult(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 2, 6, false);
        String str = result.toString();
        assertTrue(str.contains("meeting entries added=4"));
        assertTrue(str.contains("activity logs added=0"));
        assertTrue(str.contains("medications added=2"));
        assertTrue(str.contains("medication logs added=6"));
        assertTrue(str.contains("reminder settings restored=false"));
    }

    @Test
    void testToStringIncludesSportAndFoodLogs() {
        MergeResult result = new MergeResult(0, 0, 0, 0, 0, 0, 3, 5, 0, 0, false);
        String str = result.toString();
        assertTrue(str.contains("sport logs added=3"));
        assertTrue(str.contains("food logs added=5"));
    }

    @Test
    void testReminderSettingsNotRestoredDefault() {
        MergeResult result = new MergeResult(1, 2, 3, 4, 5, 6);
        assertFalse(result.isReminderSettingsRestored());
    }

    @Test
    void testMeetingEntriesDefaultZero() {
        MergeResult result = new MergeResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, true);
        assertEquals(0, result.getMeetingEntriesAdded());
        assertEquals(0, result.getActivityLogsAdded());
        assertEquals(0, result.getMedicationsAdded());
        assertEquals(0, result.getMedicationLogsAdded());
    }
}
