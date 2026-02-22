package de.idrinth.habitevaluator.android

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for threshold validation logic equivalent to what EditHabitsFragment provided.
 * The isThresholdOrderValid helper is extracted as a pure function for unit testing.
 * Thresholds must be non-negative and in non-descending order.
 */
class EditHabitsScreenTest {

    /**
     * Replication of isThresholdOrderValid logic from EditHabitsFragment.
     * Returns true if t1 >= 0 and t1 <= t2 <= t3 <= t4.
     */
    private fun isThresholdOrderValid(t1: Int, t2: Int, t3: Int, t4: Int): Boolean {
        return t1 >= 0 && t1 <= t2 && t2 <= t3 && t3 <= t4
    }

    @Test
    fun testIsThresholdOrderValidWithAscendingValues() {
        assertTrue(isThresholdOrderValid(1, 2, 4, 7))
    }

    @Test
    fun testIsThresholdOrderValidWithEqualValues() {
        assertTrue(isThresholdOrderValid(3, 3, 3, 3))
    }

    @Test
    fun testIsThresholdOrderValidWithZeros() {
        assertTrue(isThresholdOrderValid(0, 0, 0, 0))
    }

    @Test
    fun testIsThresholdOrderValidWithZeroStart() {
        assertTrue(isThresholdOrderValid(0, 1, 2, 3))
    }

    @Test
    fun testIsThresholdOrderInvalidWithNegativeFirst() {
        assertFalse(isThresholdOrderValid(-1, 2, 4, 7))
    }

    @Test
    fun testIsThresholdOrderInvalidWithDescendingPair() {
        assertFalse(isThresholdOrderValid(5, 3, 4, 7))
    }

    @Test
    fun testIsThresholdOrderInvalidWithDescendingMiddle() {
        assertFalse(isThresholdOrderValid(1, 5, 3, 7))
    }

    @Test
    fun testIsThresholdOrderInvalidWithDescendingLast() {
        assertFalse(isThresholdOrderValid(1, 2, 8, 5))
    }

    @Test
    fun testIsThresholdOrderValidWithLargeValues() {
        assertTrue(isThresholdOrderValid(10, 20, 50, 100))
    }

    @Test
    fun testIsThresholdOrderValidWithDefaultThresholds() {
        assertTrue(isThresholdOrderValid(1, 2, 4, 7))
    }
}
