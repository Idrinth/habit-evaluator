package de.idrinth.habitevaluator.android;

import org.junit.Test;

import de.idrinth.habitevaluator.shared.model.FrequencyType;

import static org.junit.Assert.*;

public class AddHabitFragmentTest {

    @Test
    public void testMapPositionToFrequencyTypeDaily() {
        assertEquals(FrequencyType.DAILY,
                AddHabitFragment.mapPositionToFrequencyType(0));
    }

    @Test
    public void testMapPositionToFrequencyTypeWeekly() {
        assertEquals(FrequencyType.WEEKLY,
                AddHabitFragment.mapPositionToFrequencyType(1));
    }

    @Test
    public void testMapPositionToFrequencyTypeMonthly() {
        assertEquals(FrequencyType.MONTHLY,
                AddHabitFragment.mapPositionToFrequencyType(2));
    }

    @Test
    public void testMapPositionToFrequencyTypeNegativeReturnsNull() {
        assertNull(AddHabitFragment.mapPositionToFrequencyType(-1));
    }

    @Test
    public void testMapPositionToFrequencyTypeOutOfRangeReturnsNull() {
        assertNull(AddHabitFragment.mapPositionToFrequencyType(99));
    }

    @Test
    public void testMapPositionToFrequencyTypeExactBoundary() {
        assertNull(AddHabitFragment.mapPositionToFrequencyType(
                FrequencyType.values().length));
    }

    @Test
    public void testAllFrequencyTypesAreMapped() {
        for (int i = 0; i < FrequencyType.values().length; i++) {
            assertNotNull(AddHabitFragment.mapPositionToFrequencyType(i));
        }
    }

    @Test
    public void testMapPositionToFrequencyTypePreservesOrder() {
        FrequencyType[] expected = FrequencyType.values();
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], AddHabitFragment.mapPositionToFrequencyType(i));
        }
    }
}
