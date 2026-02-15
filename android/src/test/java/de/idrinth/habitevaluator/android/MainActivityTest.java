package de.idrinth.habitevaluator.android;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import de.idrinth.habitevaluator.shared.persistence.InMemoryHabitRepository;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.User;

import static org.junit.jupiter.api.Assertions.*;

class MainActivityTest {

    @BeforeEach
    void setUp() {
        MainActivity.setEditHabitId(null);
        MainActivity.setPointDevelopmentHabitId(null);
        MainActivity.setRecordEmotionPairId(null);
    }

    @Test
    void testGetEditHabitIdDefaultsToNull() {
        assertNull(MainActivity.getEditHabitId());
    }

    @Test
    void testSetAndGetEditHabitId() {
        MainActivity.setEditHabitId("habit-123");
        assertEquals("habit-123", MainActivity.getEditHabitId());
    }

    @Test
    void testSetEditHabitIdOverwritesPrevious() {
        MainActivity.setEditHabitId("first");
        MainActivity.setEditHabitId("second");
        assertEquals("second", MainActivity.getEditHabitId());
    }

    @Test
    void testSetEditHabitIdToNull() {
        MainActivity.setEditHabitId("some-id");
        MainActivity.setEditHabitId(null);
        assertNull(MainActivity.getEditHabitId());
    }

    @Test
    void testGetPointDevelopmentHabitIdDefaultsToNull() {
        assertNull(MainActivity.getPointDevelopmentHabitId());
    }

    @Test
    void testSetAndGetPointDevelopmentHabitId() {
        MainActivity.setPointDevelopmentHabitId("habit-456");
        assertEquals("habit-456", MainActivity.getPointDevelopmentHabitId());
    }

    @Test
    void testSetPointDevelopmentHabitIdOverwritesPrevious() {
        MainActivity.setPointDevelopmentHabitId("first");
        MainActivity.setPointDevelopmentHabitId("second");
        assertEquals("second", MainActivity.getPointDevelopmentHabitId());
    }

    @Test
    void testSetPointDevelopmentHabitIdToNull() {
        MainActivity.setPointDevelopmentHabitId("some-id");
        MainActivity.setPointDevelopmentHabitId(null);
        assertNull(MainActivity.getPointDevelopmentHabitId());
    }

    @Test
    void testGetRecordEmotionPairIdDefaultsToNull() {
        assertNull(MainActivity.getRecordEmotionPairId());
    }

    @Test
    void testSetAndGetRecordEmotionPairId() {
        MainActivity.setRecordEmotionPairId("pair-789");
        assertEquals("pair-789", MainActivity.getRecordEmotionPairId());
    }

    @Test
    void testSetRecordEmotionPairIdOverwritesPrevious() {
        MainActivity.setRecordEmotionPairId("first");
        MainActivity.setRecordEmotionPairId("second");
        assertEquals("second", MainActivity.getRecordEmotionPairId());
    }

    @Test
    void testSetRecordEmotionPairIdToNull() {
        MainActivity.setRecordEmotionPairId("some-id");
        MainActivity.setRecordEmotionPairId(null);
        assertNull(MainActivity.getRecordEmotionPairId());
    }

    @Test
    void testSaveAllHabitsDoesNotThrowWhenRepositoryIsNull() {
        // Static state has null repository by default — should return early without error
        MainActivity.saveAllHabits();
    }

    @Test
    void testRefreshSharedMedicationsDoesNotThrowWhenRepositoryIsNull() {
        // Static state has null medication repository by default — should handle gracefully
        MainActivity.refreshSharedMedications();
    }

    @Test
    void testSharedCategoriesIsNeverNull() {
        assertNotNull(MainActivity.getSharedCategories());
    }

    @Test
    void testSharedEmotionPairsIsNeverNull() {
        assertNotNull(MainActivity.getSharedEmotionPairs());
    }

    @Test
    void testSharedSleepEntriesIsNeverNull() {
        assertNotNull(MainActivity.getSharedSleepEntries());
    }

    @Test
    void testSharedMedicationsIsNeverNull() {
        assertNotNull(MainActivity.getSharedMedications());
    }

    @Test
    void testIsSharedUsingRemoteStorageDefaultsFalse() {
        // Before any initialization, remote storage should be false
        assertFalse(MainActivity.isSharedUsingRemoteStorage());
    }

    @Test
    void testEditHabitIdIsIndependentOfPointDevelopmentHabitId() {
        MainActivity.setEditHabitId("edit-id");
        MainActivity.setPointDevelopmentHabitId("point-id");
        assertEquals("edit-id", MainActivity.getEditHabitId());
        assertEquals("point-id", MainActivity.getPointDevelopmentHabitId());
    }

    @Test
    void testRecordEmotionPairIdIsIndependentOfOtherIds() {
        MainActivity.setEditHabitId("edit-id");
        MainActivity.setPointDevelopmentHabitId("point-id");
        MainActivity.setRecordEmotionPairId("emotion-id");
        assertEquals("edit-id", MainActivity.getEditHabitId());
        assertEquals("point-id", MainActivity.getPointDevelopmentHabitId());
        assertEquals("emotion-id", MainActivity.getRecordEmotionPairId());
    }

    @Test
    void testSetEditHabitIdAcceptsEmptyString() {
        MainActivity.setEditHabitId("");
        assertEquals("", MainActivity.getEditHabitId());
    }

    @Test
    void testSetPointDevelopmentHabitIdAcceptsEmptyString() {
        MainActivity.setPointDevelopmentHabitId("");
        assertEquals("", MainActivity.getPointDevelopmentHabitId());
    }

    @Test
    void testSetRecordEmotionPairIdAcceptsEmptyString() {
        MainActivity.setRecordEmotionPairId("");
        assertEquals("", MainActivity.getRecordEmotionPairId());
    }

    @Test
    void testSetEditHabitIdAcceptsLongString() {
        String longId = "a".repeat(200);
        MainActivity.setEditHabitId(longId);
        assertEquals(longId, MainActivity.getEditHabitId());
    }

    @Test
    void testSetPointDevelopmentHabitIdAcceptsUUID() {
        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        MainActivity.setPointDevelopmentHabitId(uuid);
        assertEquals(uuid, MainActivity.getPointDevelopmentHabitId());
    }

    @Test
    void testSetRecordEmotionPairIdAcceptsUUID() {
        String uuid = "123e4567-e89b-12d3-a456-426614174000";
        MainActivity.setRecordEmotionPairId(uuid);
        assertEquals(uuid, MainActivity.getRecordEmotionPairId());
    }

    @Test
    void testRapidEditHabitIdChanges() {
        for (int i = 0; i < 50; i++) {
            String id = "id-" + i;
            MainActivity.setEditHabitId(id);
            assertEquals(id, MainActivity.getEditHabitId());
        }
    }

    @Test
    void testNullResetForAllIds() {
        MainActivity.setEditHabitId("test");
        MainActivity.setPointDevelopmentHabitId("test");
        MainActivity.setRecordEmotionPairId("test");

        MainActivity.setEditHabitId(null);
        MainActivity.setPointDevelopmentHabitId(null);
        MainActivity.setRecordEmotionPairId(null);

        assertNull(MainActivity.getEditHabitId());
        assertNull(MainActivity.getPointDevelopmentHabitId());
        assertNull(MainActivity.getRecordEmotionPairId());
    }

    @Test
    void testSaveAllHabitsMultipleCallsDoNotThrow() {
        MainActivity.saveAllHabits();
        MainActivity.saveAllHabits();
        MainActivity.saveAllHabits();
    }

    @Test
    void testRefreshSharedMedicationsMultipleCallsDoNotThrow() {
        MainActivity.refreshSharedMedications();
        MainActivity.refreshSharedMedications();
        MainActivity.refreshSharedMedications();
    }

    @Test
    void testSetEditHabitIdWithSpecialCharacters() {
        String specialId = "habit-!@#$%^&*()";
        MainActivity.setEditHabitId(specialId);
        assertEquals(specialId, MainActivity.getEditHabitId());
    }
}
