package de.idrinth.habitevaluator.android;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EditHabitsFragmentTest {

    @Test
    void testIsThresholdOrderValidWithAscendingValues() {
        assertTrue(EditHabitsFragment.isThresholdOrderValid(1, 2, 4, 7));
    }

    @Test
    void testIsThresholdOrderValidWithEqualValues() {
        assertTrue(EditHabitsFragment.isThresholdOrderValid(3, 3, 3, 3));
    }

    @Test
    void testIsThresholdOrderValidWithZeros() {
        assertTrue(EditHabitsFragment.isThresholdOrderValid(0, 0, 0, 0));
    }

    @Test
    void testIsThresholdOrderValidWithZeroStart() {
        assertTrue(EditHabitsFragment.isThresholdOrderValid(0, 1, 2, 3));
    }

    @Test
    void testIsThresholdOrderInvalidWithNegativeFirst() {
        assertFalse(EditHabitsFragment.isThresholdOrderValid(-1, 2, 4, 7));
    }

    @Test
    void testIsThresholdOrderInvalidWithDescendingPair() {
        assertFalse(EditHabitsFragment.isThresholdOrderValid(5, 3, 4, 7));
    }

    @Test
    void testIsThresholdOrderInvalidWithDescendingMiddle() {
        assertFalse(EditHabitsFragment.isThresholdOrderValid(1, 5, 3, 7));
    }

    @Test
    void testIsThresholdOrderInvalidWithDescendingLast() {
        assertFalse(EditHabitsFragment.isThresholdOrderValid(1, 2, 8, 5));
    }

    @Test
    void testIsThresholdOrderValidWithLargeValues() {
        assertTrue(EditHabitsFragment.isThresholdOrderValid(10, 20, 50, 100));
    }

    @Test
    void testIsThresholdOrderValidWithDefaultThresholds() {
        assertTrue(EditHabitsFragment.isThresholdOrderValid(1, 2, 4, 7));
    }
}
