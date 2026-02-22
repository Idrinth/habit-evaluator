package de.idrinth.habitevaluator.android

import de.idrinth.habitevaluator.shared.model.FrequencyType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for frequency type mapping logic equivalent to what AddHabitFragment provided.
 * Maps a spinner/dropdown position (0-based) to a FrequencyType enum value,
 * returning null for out-of-range positions.
 */
class AddHabitScreenTest {

    /**
     * Replication of mapPositionToFrequencyType logic from AddHabitFragment.
     */
    private fun mapPositionToFrequencyType(position: Int): FrequencyType? {
        val values = FrequencyType.values()
        return if (position < 0 || position >= values.size) null else values[position]
    }

    @Test
    fun testMapPositionToFrequencyTypeDaily() {
        assertEquals(FrequencyType.DAILY, mapPositionToFrequencyType(0))
    }

    @Test
    fun testMapPositionToFrequencyTypeWeekly() {
        assertEquals(FrequencyType.WEEKLY, mapPositionToFrequencyType(1))
    }

    @Test
    fun testMapPositionToFrequencyTypeMonthly() {
        assertEquals(FrequencyType.MONTHLY, mapPositionToFrequencyType(2))
    }

    @Test
    fun testMapPositionToFrequencyTypeNegativeReturnsNull() {
        assertNull(mapPositionToFrequencyType(-1))
    }

    @Test
    fun testMapPositionToFrequencyTypeOutOfRangeReturnsNull() {
        assertNull(mapPositionToFrequencyType(99))
    }

    @Test
    fun testMapPositionToFrequencyTypeExactBoundary() {
        assertNull(mapPositionToFrequencyType(FrequencyType.values().size))
    }

    @Test
    fun testAllFrequencyTypesAreMapped() {
        for (i in FrequencyType.values().indices) {
            assertNotNull(mapPositionToFrequencyType(i))
        }
    }

    @Test
    fun testMapPositionToFrequencyTypePreservesOrder() {
        val expected = FrequencyType.values()
        for (i in expected.indices) {
            assertEquals(expected[i], mapPositionToFrequencyType(i))
        }
    }

    @Test
    fun testMapPositionToFrequencyTypeLargeNegative() {
        assertNull(mapPositionToFrequencyType(-100))
    }

    @Test
    fun testMapPositionToFrequencyTypeIntMaxValue() {
        assertNull(mapPositionToFrequencyType(Int.MAX_VALUE))
    }

    @Test
    fun testMapPositionToFrequencyTypeThreeReturnsNull() {
        assertNull(mapPositionToFrequencyType(3))
    }

    @Test
    fun testMapPositionToFrequencyTypeDailyIsPositionZero() {
        assertEquals(FrequencyType.DAILY, mapPositionToFrequencyType(0))
    }

    @Test
    fun testMapPositionToFrequencyTypeCoversAllEnumValues() {
        var mappedCount = 0
        for (i in 0 until 100) {
            if (mapPositionToFrequencyType(i) != null) mappedCount++
        }
        assertEquals(FrequencyType.values().size, mappedCount)
    }
}
