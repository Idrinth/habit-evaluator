package de.idrinth.habitevaluator.android;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import de.idrinth.habitevaluator.android.persistence.InMemoryHabitRepository;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.User;

import static org.junit.Assert.*;

public class MainActivityTest {

    @Before
    public void setUp() {
        MainActivity.setEditHabitId(null);
        MainActivity.setPointDevelopmentHabitId(null);
        MainActivity.setRecordEmotionPairId(null);
    }

    @Test
    public void testGetEditHabitIdDefaultsToNull() {
        assertNull(MainActivity.getEditHabitId());
    }

    @Test
    public void testSetAndGetEditHabitId() {
        MainActivity.setEditHabitId("habit-123");
        assertEquals("habit-123", MainActivity.getEditHabitId());
    }

    @Test
    public void testSetEditHabitIdOverwritesPrevious() {
        MainActivity.setEditHabitId("first");
        MainActivity.setEditHabitId("second");
        assertEquals("second", MainActivity.getEditHabitId());
    }

    @Test
    public void testSetEditHabitIdToNull() {
        MainActivity.setEditHabitId("some-id");
        MainActivity.setEditHabitId(null);
        assertNull(MainActivity.getEditHabitId());
    }

    @Test
    public void testGetPointDevelopmentHabitIdDefaultsToNull() {
        assertNull(MainActivity.getPointDevelopmentHabitId());
    }

    @Test
    public void testSetAndGetPointDevelopmentHabitId() {
        MainActivity.setPointDevelopmentHabitId("habit-456");
        assertEquals("habit-456", MainActivity.getPointDevelopmentHabitId());
    }

    @Test
    public void testSetPointDevelopmentHabitIdOverwritesPrevious() {
        MainActivity.setPointDevelopmentHabitId("first");
        MainActivity.setPointDevelopmentHabitId("second");
        assertEquals("second", MainActivity.getPointDevelopmentHabitId());
    }

    @Test
    public void testSetPointDevelopmentHabitIdToNull() {
        MainActivity.setPointDevelopmentHabitId("some-id");
        MainActivity.setPointDevelopmentHabitId(null);
        assertNull(MainActivity.getPointDevelopmentHabitId());
    }

    @Test
    public void testGetRecordEmotionPairIdDefaultsToNull() {
        assertNull(MainActivity.getRecordEmotionPairId());
    }

    @Test
    public void testSetAndGetRecordEmotionPairId() {
        MainActivity.setRecordEmotionPairId("pair-789");
        assertEquals("pair-789", MainActivity.getRecordEmotionPairId());
    }

    @Test
    public void testSetRecordEmotionPairIdOverwritesPrevious() {
        MainActivity.setRecordEmotionPairId("first");
        MainActivity.setRecordEmotionPairId("second");
        assertEquals("second", MainActivity.getRecordEmotionPairId());
    }

    @Test
    public void testSetRecordEmotionPairIdToNull() {
        MainActivity.setRecordEmotionPairId("some-id");
        MainActivity.setRecordEmotionPairId(null);
        assertNull(MainActivity.getRecordEmotionPairId());
    }

    @Test
    public void testSaveAllHabitsDoesNotThrowWhenRepositoryIsNull() {
        // Static state has null repository by default — should return early without error
        MainActivity.saveAllHabits();
    }

    @Test
    public void testRefreshSharedMedicationsDoesNotThrowWhenRepositoryIsNull() {
        // Static state has null medication repository by default — should handle gracefully
        MainActivity.refreshSharedMedications();
    }

    @Test
    public void testSharedCategoriesIsNeverNull() {
        assertNotNull(MainActivity.getSharedCategories());
    }

    @Test
    public void testSharedEmotionPairsIsNeverNull() {
        assertNotNull(MainActivity.getSharedEmotionPairs());
    }

    @Test
    public void testSharedSleepEntriesIsNeverNull() {
        assertNotNull(MainActivity.getSharedSleepEntries());
    }

    @Test
    public void testSharedMedicationsIsNeverNull() {
        assertNotNull(MainActivity.getSharedMedications());
    }

    @Test
    public void testIsSharedUsingRemoteStorageDefaultsFalse() {
        // Before any initialization, remote storage should be false
        assertFalse(MainActivity.isSharedUsingRemoteStorage());
    }

    @Test
    public void testEditHabitIdIsIndependentOfPointDevelopmentHabitId() {
        MainActivity.setEditHabitId("edit-id");
        MainActivity.setPointDevelopmentHabitId("point-id");
        assertEquals("edit-id", MainActivity.getEditHabitId());
        assertEquals("point-id", MainActivity.getPointDevelopmentHabitId());
    }

    @Test
    public void testRecordEmotionPairIdIsIndependentOfOtherIds() {
        MainActivity.setEditHabitId("edit-id");
        MainActivity.setPointDevelopmentHabitId("point-id");
        MainActivity.setRecordEmotionPairId("emotion-id");
        assertEquals("edit-id", MainActivity.getEditHabitId());
        assertEquals("point-id", MainActivity.getPointDevelopmentHabitId());
        assertEquals("emotion-id", MainActivity.getRecordEmotionPairId());
    }

    @Test
    public void testSetEditHabitIdAcceptsEmptyString() {
        MainActivity.setEditHabitId("");
        assertEquals("", MainActivity.getEditHabitId());
    }

    @Test
    public void testSetPointDevelopmentHabitIdAcceptsEmptyString() {
        MainActivity.setPointDevelopmentHabitId("");
        assertEquals("", MainActivity.getPointDevelopmentHabitId());
    }

    @Test
    public void testSetRecordEmotionPairIdAcceptsEmptyString() {
        MainActivity.setRecordEmotionPairId("");
        assertEquals("", MainActivity.getRecordEmotionPairId());
    }
}
