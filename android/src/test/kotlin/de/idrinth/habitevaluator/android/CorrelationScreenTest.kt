package de.idrinth.habitevaluator.android

import de.idrinth.habitevaluator.shared.model.EventCorrelation
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for correlation filtering logic equivalent to what CorrelationActivity provided.
 * Filters EventCorrelation entries by source (eventA) and/or target (eventB).
 */
class CorrelationScreenTest {

    companion object {
        private const val ALL = "All"

        /**
         * Replication of filterCorrelations logic from CorrelationActivity.
         * Filters correlations by type (ignored here — passed as ALL), source (eventA),
         * and target (eventB). "All" means no filter is applied for that dimension.
         */
        fun filterCorrelations(
            correlations: List<EventCorrelation>,
            type: String,
            source: String,
            target: String
        ): List<EventCorrelation> {
            return correlations.filter { corr ->
                (source == ALL || corr.eventA == source) &&
                    (target == ALL || corr.eventB == target)
            }
        }
    }

    private lateinit var correlations: MutableList<EventCorrelation>

    @BeforeEach
    fun setUp() {
        correlations = mutableListOf(
            EventCorrelation("Habit: Eat Breakfast", "Sleep Hours", 0.471, 30),
            EventCorrelation("Sleep Hours", "Emotion: fatigued \u2014 energetic", -0.226, 20),
            EventCorrelation("Diary Points", "Sleep Hours", 0.387, 25),
            EventCorrelation("Habit: Walking", "Diary Points", 0.35, 15)
        )
    }

    @Test
    fun testAllFiltersReturnEverything() {
        val result = filterCorrelations(correlations, ALL, ALL, ALL)
        assertEquals(4, result.size)
    }

    @Test
    fun testSourceFilterMatchesOnlyEventA() {
        val result = filterCorrelations(correlations, ALL, "Sleep Hours", ALL)
        assertEquals(1, result.size)
        assertEquals("Sleep Hours", result[0].eventA)
        assertEquals("Emotion: fatigued \u2014 energetic", result[0].eventB)
    }

    @Test
    fun testTargetFilterMatchesOnlyEventB() {
        val result = filterCorrelations(correlations, ALL, ALL, "Sleep Hours")
        assertEquals(2, result.size)
        for (corr in result) {
            assertEquals("Sleep Hours", corr.eventB)
        }
    }

    @Test
    fun testTargetFilterDoesNotMatchEventA() {
        val result = filterCorrelations(correlations, ALL, ALL, "Habit: Eat Breakfast")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testSourceFilterDoesNotMatchEventB() {
        val result = filterCorrelations(correlations, ALL, "Emotion: fatigued \u2014 energetic", ALL)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testBothFiltersApplied() {
        val result = filterCorrelations(correlations, ALL, "Diary Points", "Sleep Hours")
        assertEquals(1, result.size)
        assertEquals("Diary Points", result[0].eventA)
        assertEquals("Sleep Hours", result[0].eventB)
    }

    @Test
    fun testNoMatchesReturnsEmpty() {
        val result = filterCorrelations(correlations, ALL, "Nonexistent", "Also Missing")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testEmptyCorrelationsReturnsEmpty() {
        val result = filterCorrelations(emptyList(), ALL, ALL, "Sleep Hours")
        assertTrue(result.isEmpty())
    }
}
