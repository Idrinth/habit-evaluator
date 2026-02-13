package de.idrinth.habitevaluator.android;

import org.junit.jupiter.api.Test;

import de.idrinth.habitevaluator.shared.model.FrequencyType;

import static org.junit.jupiter.api.Assertions.*;

class AddHabitFragmentTest {

    @Test
    void testMapPositionToFrequencyTypeDaily() {
        assertEquals(FrequencyType.DAILY,
                AddHabitFragment.mapPositionToFrequencyType(0));
    }

    @Test
    void testMapPositionToFrequencyTypeWeekly() {
        assertEquals(FrequencyType.WEEKLY,
                AddHabitFragment.mapPositionToFrequencyType(1));
    }

    @Test
    void testMapPositionToFrequencyTypeMonthly() {
        assertEquals(FrequencyType.MONTHLY,
                AddHabitFragment.mapPositionToFrequencyType(2));
    }

    @Test
    void testMapPositionToFrequencyTypeNegativeReturnsNull() {
        assertNull(AddHabitFragment.mapPositionToFrequencyType(-1));
    }

    @Test
    void testMapPositionToFrequencyTypeOutOfRangeReturnsNull() {
        assertNull(AddHabitFragment.mapPositionToFrequencyType(99));
    }

    @Test
    void testMapPositionToFrequencyTypeExactBoundary() {
        assertNull(AddHabitFragment.mapPositionToFrequencyType(
                FrequencyType.values().length));
    }

    @Test
    void testAllFrequencyTypesAreMapped() {
        for (int i = 0; i < FrequencyType.values().length; i++) {
            assertNotNull(AddHabitFragment.mapPositionToFrequencyType(i));
        }
    }

    @Test
    void testMapPositionToFrequencyTypePreservesOrder() {
        FrequencyType[] expected = FrequencyType.values();
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], AddHabitFragment.mapPositionToFrequencyType(i));
        }
    }
}
