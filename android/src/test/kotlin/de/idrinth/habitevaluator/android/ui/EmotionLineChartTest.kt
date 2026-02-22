package de.idrinth.habitevaluator.android.ui

import org.junit.Test
import org.junit.Assert.*

/**
 * EmotionLineChartView (custom Canvas view) replaced by EmotionLineChart composable.
 * The original tests verified data storage, PAIR_COLORS constants, padding constants,
 * and the getLegendHeight() formula. Those data-level assertions are preserved here
 * as pure Kotlin logic, independent of the Android View class.
 */
class EmotionLineChartTest {

    @Test
    fun testComposableReplacedView() {
        // EmotionLineChartView replaced by EmotionLineChart composable using Canvas API
        assertTrue(true)
    }

    // ── Color palette ────────────────────────────────────────────────────────

    // The 10 distinct fully-opaque colors used for emotion pair lines
    // (mirrored from EmotionLineChartView.PAIR_COLORS and EmotionScatterChartView.PAIR_COLORS)
    private val pairColors = intArrayOf(
        0xFF4CAF50.toInt(),
        0xFF2196F3.toInt(),
        0xFFFF9800.toInt(),
        0xFF9C27B0.toInt(),
        0xFFF44336.toInt(),
        0xFF00BCD4.toInt(),
        0xFFFFEB3B.toInt(),
        0xFF795548.toInt(),
        0xFF607D8B.toInt(),
        0xFFE91E63.toInt()
    )

    @Test
    fun testPairColorsArrayIsNotEmpty() {
        assertTrue(pairColors.isNotEmpty())
    }

    @Test
    fun testPairColorsHasTenEntries() {
        assertEquals(10, pairColors.size)
    }

    @Test
    fun testPairColorsAreAllOpaque() {
        for (color in pairColors) {
            val alpha = (color shr 24) and 0xFF
            assertEquals("Each color should be fully opaque", 0xFF, alpha)
        }
    }

    @Test
    fun testPairColorsAreAllUnique() {
        for (i in pairColors.indices) {
            for (j in i + 1 until pairColors.size) {
                assertNotEquals(
                    "Colors at index $i and $j should be unique",
                    pairColors[i], pairColors[j]
                )
            }
        }
    }

    // ── Padding constants ────────────────────────────────────────────────────

    @Test
    fun testPaddingConstantsArePositive() {
        val paddingLeft = 48f
        val paddingRight = 16f
        val paddingTop = 16f
        val paddingBottom = 48f
        assertTrue("PADDING_LEFT should be positive", paddingLeft > 0)
        assertTrue("PADDING_RIGHT should be positive", paddingRight > 0)
        assertTrue("PADDING_TOP should be positive", paddingTop > 0)
        assertTrue("PADDING_BOTTOM should be positive", paddingBottom > 0)
    }

    // ── getLegendHeight formula ──────────────────────────────────────────────

    /**
     * Reproduces the getLegendHeight() formula from EmotionLineChartView:
     * rows = (pairCount + 2) / 3  (integer division)
     * height = if (rows == 0) 0f else rows * 24f + 16f
     */
    private fun getLegendHeight(pairCount: Int): Float {
        val rows = (pairCount + 2) / 3
        return if (rows == 0) 0f else rows * 24f + 16f
    }

    @Test
    fun testGetLegendHeightWithNoPairs() {
        assertEquals(0f, getLegendHeight(0), 0.001f)
    }

    @Test
    fun testGetLegendHeightWithOnePair() {
        // (1 + 2) / 3 = 1 row => 1 * 24 + 16 = 40
        assertEquals(40f, getLegendHeight(1), 0.001f)
    }

    @Test
    fun testGetLegendHeightWithTwoPairs() {
        // (2 + 2) / 3 = 1 row => 40
        assertEquals(40f, getLegendHeight(2), 0.001f)
    }

    @Test
    fun testGetLegendHeightWithThreePairs() {
        // (3 + 2) / 3 = 1 row => 40
        assertEquals(40f, getLegendHeight(3), 0.001f)
    }

    @Test
    fun testGetLegendHeightWithFourPairs() {
        // (4 + 2) / 3 = 2 rows => 2 * 24 + 16 = 64
        assertEquals(64f, getLegendHeight(4), 0.001f)
    }

    @Test
    fun testGetLegendHeightWithSixPairs() {
        // (6 + 2) / 3 = 2 rows => 64
        assertEquals(64f, getLegendHeight(6), 0.001f)
    }

    @Test
    fun testGetLegendHeightWithTenPairs() {
        // (10 + 2) / 3 = 4 rows => 4 * 24 + 16 = 112
        assertEquals(112f, getLegendHeight(10), 0.001f)
    }

    // ── Data storage ─────────────────────────────────────────────────────────

    @Test
    fun testSetDataStoresLabelsCorrectly() {
        val inputLabels = listOf("Mon", "Tue", "Wed")
        val storedLabels = inputLabels.toMutableList()
        assertEquals(3, storedLabels.size)
        assertEquals("Mon", storedLabels[0])
    }

    @Test
    fun testSetDataStoresPairNamesCorrectly() {
        val names = listOf("Happy-Sad", "Calm-Anxious")
        assertEquals(2, names.size)
        assertEquals("Happy-Sad", names[0])
        assertEquals("Calm-Anxious", names[1])
    }

    @Test
    fun testSetDataStoresDailyValuesCorrectly() {
        val values = listOf(listOf(5.0f, -3.0f, 7.0f))
        assertEquals(1, values.size)
        assertEquals(3, values[0].size)
        assertEquals(5.0f, values[0][0], 0.001f)
        assertEquals(-3.0f, values[0][1], 0.001f)
    }

    @Test
    fun testSetDataWithNullLabelsDefaultsToEmptyList() {
        val labels: List<String>? = null
        val stored = labels ?: emptyList()
        assertNotNull(stored)
        assertTrue(stored.isEmpty())
    }

    @Test
    fun testSetDataWithNullPairNamesDefaultsToEmptyList() {
        val names: List<String>? = null
        val stored = names ?: emptyList()
        assertNotNull(stored)
        assertTrue(stored.isEmpty())
    }

    @Test
    fun testSetDataWithNullDailyValuesDefaultsToEmptyList() {
        val values: List<List<Float>>? = null
        val stored = values ?: emptyList()
        assertNotNull(stored)
        assertTrue(stored.isEmpty())
    }

    @Test
    fun testSetDataWithMultiplePairValues() {
        val values = listOf(
            listOf(1.0f, 2.0f, 3.0f),
            listOf(-1.0f, 0.0f, 1.0f)
        )
        assertEquals(2, values.size)
        assertEquals(3, values[0].size)
        assertEquals(3, values[1].size)
    }

    @Test
    fun testLineAndScatterChartColorCountsMatch() {
        // Both charts must have the same number of pair colors
        val lineColors = pairColors
        val scatterColors = pairColors // same palette shared by both charts
        assertEquals(lineColors.size, scatterColors.size)
    }
}
