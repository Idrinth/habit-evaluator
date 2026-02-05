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
}
