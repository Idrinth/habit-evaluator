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

        /**
         * Replication of confidence classification logic from CorrelationScreen.
         * Classifies a correlation value into "strong", "moderate", or "weak"
         * based on absolute strength thresholds.
         */
        fun classifyConfidence(correlation: Double): String {
            val strength = kotlin.math.abs(correlation)
            return when {
                strength >= 0.5 -> "strong"
                strength >= 0.3 -> "moderate"
                else -> "weak"
            }
        }

        /**
         * Replication of isWeak check from CorrelationScreen.
         * Returns true if the absolute correlation strength is below 0.3.
         */
        fun isWeak(correlation: Double): Boolean {
            return kotlin.math.abs(correlation) < 0.3
        }

        /**
         * Replication of event name building logic from CorrelationScreen.
         * Collects unique event names from correlations, sorts them, and prepends "All".
         */
        fun buildEventNames(correlations: List<EventCorrelation>): List<String> {
            val names = mutableSetOf<String>()
            correlations.forEach {
                it.eventA?.let { a -> names.add(a) }
                it.eventB?.let { b -> names.add(b) }
            }
            return listOf(ALL) + names.sorted()
        }

        /**
         * Replication of correlation value formatting from CorrelationScreen.
         * Formats a correlation value with sign and 3 decimal places.
         */
        fun formatCorrelation(value: Double): String {
            return String.format("%+.3f", value)
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

    // --- Confidence classification tests ---

    @Test
    fun testClassifyConfidenceStrongAtThreshold() {
        assertEquals("strong", classifyConfidence(0.5))
    }

    @Test
    fun testClassifyConfidenceStrongAboveThreshold() {
        assertEquals("strong", classifyConfidence(0.8))
    }

    @Test
    fun testClassifyConfidenceModerateAtThreshold() {
        assertEquals("moderate", classifyConfidence(0.3))
    }

    @Test
    fun testClassifyConfidenceModerateJustBelow() {
        assertEquals("moderate", classifyConfidence(0.499))
    }

    @Test
    fun testClassifyConfidenceWeakBelowModerateThreshold() {
        assertEquals("weak", classifyConfidence(0.29))
    }

    @Test
    fun testClassifyConfidenceWeakAtZero() {
        assertEquals("weak", classifyConfidence(0.0))
    }

    @Test
    fun testClassifyConfidenceStrongAtOne() {
        assertEquals("strong", classifyConfidence(1.0))
    }

    @Test
    fun testClassifyConfidenceUsesAbsoluteValue() {
        // Negative correlations should be classified by absolute strength
        assertEquals("strong", classifyConfidence(-0.6))
    }

    @Test
    fun testClassifyConfidenceNegativeModerate() {
        assertEquals("moderate", classifyConfidence(-0.35))
    }

    @Test
    fun testClassifyConfidenceNegativeWeak() {
        assertEquals("weak", classifyConfidence(-0.1))
    }

    // --- isWeak classification tests ---

    @Test
    fun testIsWeakTrueForLowStrength() {
        assertTrue(isWeak(0.1))
    }

    @Test
    fun testIsWeakTrueForZero() {
        assertTrue(isWeak(0.0))
    }

    @Test
    fun testIsWeakFalseAtThreshold() {
        assertFalse(isWeak(0.3))
    }

    @Test
    fun testIsWeakFalseAboveThreshold() {
        assertFalse(isWeak(0.5))
    }

    @Test
    fun testIsWeakUsesAbsoluteValue() {
        assertFalse(isWeak(-0.4))
    }

    @Test
    fun testIsWeakNegativeWeak() {
        assertTrue(isWeak(-0.1))
    }

    // --- Event name building tests ---

    @Test
    fun testBuildEventNamesReturnsAllPlusUnique() {
        val names = buildEventNames(correlations)
        assertTrue(names.first() == "All")
        assertTrue(names.contains("Sleep Hours"))
        assertTrue(names.contains("Habit: Eat Breakfast"))
        assertTrue(names.contains("Diary Points"))
    }

    @Test
    fun testBuildEventNamesNoDuplicates() {
        val names = buildEventNames(correlations)
        // "Sleep Hours" appears as both eventA and eventB, should appear only once
        val withoutAll = names.drop(1)
        assertEquals(withoutAll.size, withoutAll.toSet().size)
    }

    @Test
    fun testBuildEventNamesAreSorted() {
        val names = buildEventNames(correlations)
        val withoutAll = names.drop(1)
        assertEquals(withoutAll.sorted(), withoutAll)
    }

    @Test
    fun testBuildEventNamesEmptyCorrelations() {
        val names = buildEventNames(emptyList())
        assertEquals(1, names.size)
        assertEquals("All", names[0])
    }

    @Test
    fun testBuildEventNamesAlwaysStartsWithAll() {
        val names = buildEventNames(correlations)
        assertEquals("All", names[0])
    }

    // --- Correlation value formatting tests ---

    @Test
    fun testFormatCorrelationPositive() {
        val result = formatCorrelation(0.471)
        assertEquals("+0.471", result)
    }

    @Test
    fun testFormatCorrelationNegative() {
        val result = formatCorrelation(-0.226)
        assertEquals("-0.226", result)
    }

    @Test
    fun testFormatCorrelationZero() {
        val result = formatCorrelation(0.0)
        assertEquals("+0.000", result)
    }

    @Test
    fun testFormatCorrelationMaxPositive() {
        val result = formatCorrelation(1.0)
        assertEquals("+1.000", result)
    }

    @Test
    fun testFormatCorrelationMaxNegative() {
        val result = formatCorrelation(-1.0)
        assertEquals("-1.000", result)
    }

    @Test
    fun testFormatCorrelationRoundsToThreeDecimals() {
        val result = formatCorrelation(0.12345)
        assertEquals("+0.123", result)
    }
}
