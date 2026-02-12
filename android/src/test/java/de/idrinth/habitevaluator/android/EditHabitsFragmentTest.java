package de.idrinth.habitevaluator.android;

import org.junit.Test;

import static org.junit.Assert.*;

public class EditHabitsFragmentTest {

    @Test
    public void testIsThresholdOrderValidWithAscendingValues() {
        assertTrue(EditHabitsFragment.isThresholdOrderValid(1, 2, 4, 7));
    }

    @Test
    public void testIsThresholdOrderValidWithEqualValues() {
        assertTrue(EditHabitsFragment.isThresholdOrderValid(3, 3, 3, 3));
    }

    @Test
    public void testIsThresholdOrderValidWithZeros() {
        assertTrue(EditHabitsFragment.isThresholdOrderValid(0, 0, 0, 0));
    }

    @Test
    public void testIsThresholdOrderValidWithZeroStart() {
        assertTrue(EditHabitsFragment.isThresholdOrderValid(0, 1, 2, 3));
    }

    @Test
    public void testIsThresholdOrderInvalidWithNegativeFirst() {
        assertFalse(EditHabitsFragment.isThresholdOrderValid(-1, 2, 4, 7));
    }

    @Test
    public void testIsThresholdOrderInvalidWithDescendingPair() {
        assertFalse(EditHabitsFragment.isThresholdOrderValid(5, 3, 4, 7));
    }

    @Test
    public void testIsThresholdOrderInvalidWithDescendingMiddle() {
        assertFalse(EditHabitsFragment.isThresholdOrderValid(1, 5, 3, 7));
    }

    @Test
    public void testIsThresholdOrderInvalidWithDescendingLast() {
        assertFalse(EditHabitsFragment.isThresholdOrderValid(1, 2, 8, 5));
    }

    @Test
    public void testIsThresholdOrderValidWithLargeValues() {
        assertTrue(EditHabitsFragment.isThresholdOrderValid(10, 20, 50, 100));
    }

    @Test
    public void testIsThresholdOrderValidWithDefaultThresholds() {
        assertTrue(EditHabitsFragment.isThresholdOrderValid(1, 2, 4, 7));
    }
}
