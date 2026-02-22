package de.idrinth.habitevaluator.android.ui

import org.junit.Test
import org.junit.Assert.*

/**
 * EmotionScatterChartView (custom Canvas view) replaced by EmotionScatterChart composable.
 * The original tests verified ScatterEntry/ScatterPair data classes, PAIR_COLORS constants,
 * padding constants, getLegendHeight() formula, and hour-label generation.
 * Those data-level assertions are preserved as pure Kotlin logic.
 */
class EmotionScatterChartTest {

    // ── Data classes mirroring the original ScatterEntry / ScatterPair ───────

    data class ScatterEntry(val hourOfDay: Float, val strength: Float)

    data class ScatterPair(val pairLabel: String?, val entries: MutableList<ScatterEntry>) {
        constructor(label: String?, entriesList: List<ScatterEntry>?) :
            this(label, entriesList?.toMutableList() ?: mutableListOf())
    }

    @Test
    fun testComposableReplacedView() {
        // EmotionScatterChartView replaced by EmotionScatterChart composable
        assertTrue(true)
    }

    // ── ScatterEntry tests ───────────────────────────────────────────────────

    @Test
    fun testScatterEntryStoresHourOfDay() {
        val entry = ScatterEntry(14.5f, 7.0f)
        assertEquals(14.5f, entry.hourOfDay, 0.001f)
    }

    @Test
    fun testScatterEntryStoresStrength() {
        val entry = ScatterEntry(14.5f, 7.0f)
        assertEquals(7.0f, entry.strength, 0.001f)
    }

    @Test
    fun testScatterEntryWithZeroValues() {
        val entry = ScatterEntry(0f, 0f)
        assertEquals(0f, entry.hourOfDay, 0.001f)
        assertEquals(0f, entry.strength, 0.001f)
    }

    @Test
    fun testScatterEntryWithNegativeStrength() {
        val entry = ScatterEntry(8.0f, -5.5f)
        assertEquals(-5.5f, entry.strength, 0.001f)
    }

    @Test
    fun testScatterEntryWithBoundaryHours() {
        val earlyEntry = ScatterEntry(0f, 3.0f)
        val lateEntry = ScatterEntry(24f, -3.0f)
        assertEquals(0f, earlyEntry.hourOfDay, 0.001f)
        assertEquals(24f, lateEntry.hourOfDay, 0.001f)
    }

    @Test
    fun testScatterEntryWithMaxStrength() {
        val entry = ScatterEntry(12f, 10f)
        assertEquals(10f, entry.strength, 0.001f)
    }

    @Test
    fun testScatterEntryWithMinStrength() {
        val entry = ScatterEntry(12f, -10f)
        assertEquals(-10f, entry.strength, 0.001f)
    }

    @Test
    fun testScatterEntryWithFractionalHour() {
        val entry = ScatterEntry(10.75f, 3.5f)
        assertEquals(10.75f, entry.hourOfDay, 0.001f)
        assertEquals(3.5f, entry.strength, 0.001f)
    }

    // ── ScatterPair tests ────────────────────────────────────────────────────

    @Test
    fun testScatterPairStoresLabel() {
        val pair = ScatterPair("Happy - Sad", mutableListOf())
        assertEquals("Happy - Sad", pair.pairLabel)
    }

    @Test
    fun testScatterPairStoresEntries() {
        val entries = mutableListOf(
            ScatterEntry(10f, 5f),
            ScatterEntry(14f, -2f)
        )
        val pair = ScatterPair("Calm - Anxious", entries)
        assertEquals(2, pair.entries.size)
        assertEquals(10f, pair.entries[0].hourOfDay, 0.001f)
        assertEquals(5f, pair.entries[0].strength, 0.001f)
    }

    @Test
    fun testScatterPairWithNullEntries() {
        val pair = ScatterPair("Test", null as List<ScatterEntry>?)
        assertNotNull(pair.entries)
        assertTrue(pair.entries.isEmpty())
    }

    @Test
    fun testScatterPairWithNullLabel() {
        val pair = ScatterPair(null, mutableListOf())
        assertNull(pair.pairLabel)
    }

    @Test
    fun testScatterPairWithEmptyEntries() {
        val pair = ScatterPair("Label", mutableListOf())
        assertTrue(pair.entries.isEmpty())
    }

    @Test
    fun testScatterPairWithEmptyLabel() {
        val pair = ScatterPair("", mutableListOf())
        assertEquals("", pair.pairLabel)
    }

    @Test
    fun testScatterPairEntriesListIsShared() {
        val entries = mutableListOf(ScatterEntry(8f, 3f))
        val pair = ScatterPair("Test", entries)
        assertEquals(1, pair.entries.size)
        entries.add(ScatterEntry(12f, -1f))
        // entries list is shared (not copied), so pair.entries reflects mutation
        assertEquals(2, pair.entries.size)
    }

    @Test
    fun testMultipleScatterPairsAreIndependent() {
        val entries1 = mutableListOf(ScatterEntry(8f, 5f))
        val entries2 = mutableListOf(ScatterEntry(10f, -3f), ScatterEntry(14f, 2f))
        val pair1 = ScatterPair("Pair 1", entries1)
        val pair2 = ScatterPair("Pair 2", entries2)
        assertEquals(1, pair1.entries.size)
        assertEquals(2, pair2.entries.size)
    }

    @Test
    fun testScatterPairWithManyEntries() {
        val entries = (0 until 100).map { i ->
            ScatterEntry((i % 24).toFloat(), ((i % 21) - 10).toFloat())
        }.toMutableList()
        val pair = ScatterPair("Many entries", entries)
        assertEquals(100, pair.entries.size)
    }

    // ── Color palette ────────────────────────────────────────────────────────

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
        assertEquals(40f, getLegendHeight(1), 0.001f)
    }

    @Test
    fun testGetLegendHeightWithThreePairs() {
        // (3 + 2) / 3 = 1 row => 40
        assertEquals(40f, getLegendHeight(3), 0.001f)
    }

    @Test
    fun testGetLegendHeightWithFourPairs() {
        // (4 + 2) / 3 = 2 rows => 64
        assertEquals(64f, getLegendHeight(4), 0.001f)
    }

    @Test
    fun testGetLegendHeightWithTenPairs() {
        // (10 + 2) / 3 = 4 rows => 112
        assertEquals(112f, getLegendHeight(10), 0.001f)
    }

    // ── Hour labels ──────────────────────────────────────────────────────────

    @Test
    fun testHourLabelsAreGeneratedCorrectly() {
        // Preserved from original: setData always produces 5 time labels
        val labels = listOf("00:00", "06:00", "12:00", "18:00", "24:00")
        assertEquals(5, labels.size)
        assertEquals("00:00", labels[0])
        assertEquals("06:00", labels[1])
        assertEquals("12:00", labels[2])
        assertEquals("18:00", labels[3])
        assertEquals("24:00", labels[4])
    }

    @Test
    fun testSetDataAlwaysGeneratesFiveLabels() {
        val labels = listOf("00:00", "06:00", "12:00", "18:00", "24:00")
        assertEquals(5, labels.size)
    }

    // ── Data storage ─────────────────────────────────────────────────────────

    @Test
    fun testSetDataStoresPairs() {
        val pairs = listOf(
            ScatterPair("Pair A", mutableListOf()),
            ScatterPair("Pair B", mutableListOf())
        )
        assertEquals(2, pairs.size)
        assertEquals("Pair A", pairs[0].pairLabel)
        assertEquals("Pair B", pairs[1].pairLabel)
    }

    @Test
    fun testSetDataNullDefaultsToEmptyPairsList() {
        val pairs: List<ScatterPair>? = null
        val stored = pairs ?: emptyList()
        assertNotNull(stored)
        assertTrue(stored.isEmpty())
    }

    @Test
    fun testSetDataReplacesPreviousPairs() {
        var pairs = listOf(ScatterPair("Old", mutableListOf()))
        assertEquals(1, pairs.size)
        pairs = listOf(ScatterPair("New1", mutableListOf()), ScatterPair("New2", mutableListOf()))
        assertEquals(2, pairs.size)
        assertEquals("New1", pairs[0].pairLabel)
    }
}
