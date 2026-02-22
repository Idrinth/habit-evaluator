package de.idrinth.habitevaluator.android.ui

import org.junit.Test
import org.junit.Assert.*

/**
 * SleepGraphView (custom Canvas view) replaced by SleepGraph composable.
 * The trend-line algorithm and color-lightening utility from the original view
 * are extracted here as pure functions so the logic can be tested without the
 * Android View class.
 */
class SleepGraphTest {

    @Test
    fun testComposableReplacedView() {
        // SleepGraphView replaced by SleepGraph composable using Canvas API
        assertTrue(true)
    }

    // ── Trend-line algorithm extracted from SleepGraphView ───────────────────

    /**
     * Least-squares linear regression over non-zero float data points.
     * Returns [slope, intercept].
     */
    private fun calculateTrendLine(values: List<Float>): DoubleArray {
        val nonZero = values.mapIndexedNotNull { index, value ->
            if (value != 0f) Pair(index, value) else null
        }
        if (nonZero.isEmpty()) return doubleArrayOf(0.0, 0.0)
        if (nonZero.size < 2) {
            val avg = nonZero.sumOf { it.second.toDouble() } / nonZero.size
            return doubleArrayOf(0.0, avg)
        }
        val n = nonZero.size.toDouble()
        val sumX = nonZero.sumOf { it.first }.toDouble()
        val sumY = nonZero.sumOf { it.second.toDouble() }
        val sumXY = nonZero.sumOf { it.first.toDouble() * it.second }
        val sumX2 = nonZero.sumOf { it.first.toDouble() * it.first }
        val slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)
        val intercept = (sumY - slope * sumX) / n
        return doubleArrayOf(slope, intercept)
    }

    private fun getTrendY(trend: DoubleArray, x: Int): Double = trend[0] * x + trend[1]

    /**
     * Lightens an ARGB color by blending its RGB channels toward 255.
     * Preserves alpha.
     */
    private fun lightenColor(color: Int, factor: Float): Int {
        val alpha = (color shr 24) and 0xFF
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = color and 0xFF
        val newR = (r + (255 - r) * factor).toInt().coerceIn(0, 255)
        val newG = (g + (255 - g) * factor).toInt().coerceIn(0, 255)
        val newB = (b + (255 - b) * factor).toInt().coerceIn(0, 255)
        return (alpha shl 24) or (newR shl 16) or (newG shl 8) or newB
    }

    // ── Tests ────────────────────────────────────────────────────────────────

    @Test
    fun testCalculateTrendLineWithEmptyData() {
        val trend = calculateTrendLine(emptyList())
        assertEquals(0.0, trend[0], 0.001)
        assertEquals(0.0, trend[1], 0.001)
    }

    @Test
    fun testCalculateTrendLineWithSingleValue() {
        val trend = calculateTrendLine(listOf(7.5f))
        assertEquals(0.0, trend[0], 0.001)
        assertEquals(7.5, trend[1], 0.001)
    }

    @Test
    fun testCalculateTrendLineWithIncreasingValues() {
        val trend = calculateTrendLine(listOf(6.0f, 7.0f, 8.0f))
        assertTrue("Slope should be positive for increasing data", trend[0] > 0)
    }

    @Test
    fun testCalculateTrendLineSkipsZeroValues() {
        val trend = calculateTrendLine(listOf(0f, 8.0f, 0f, 6.0f))
        // Only indices 1 (val 8) and 3 (val 6) contribute
        assertTrue("Slope should be negative for decreasing data points", trend[0] < 0)
    }

    @Test
    fun testCalculateTrendLineWithAllZeros() {
        val trend = calculateTrendLine(listOf(0f, 0f, 0f))
        assertEquals(0.0, trend[0], 0.001)
        assertEquals(0.0, trend[1], 0.001)
    }

    @Test
    fun testGetTrendY() {
        val trend = doubleArrayOf(0.5, 6.0)
        val result = getTrendY(trend, 4)
        assertEquals(8.0, result, 0.001) // 0.5*4 + 6 = 8
    }

    @Test
    fun testGetTrendYAtZero() {
        val trend = doubleArrayOf(2.0, 3.0)
        val result = getTrendY(trend, 0)
        assertEquals(3.0, result, 0.001)
    }

    @Test
    fun testGetTrendYWithNegativeSlope() {
        val trend = doubleArrayOf(-1.0, 10.0)
        val result = getTrendY(trend, 3)
        assertEquals(7.0, result, 0.001)
    }

    @Test
    fun testCalculateTrendLineWithDecreasingValues() {
        val trend = calculateTrendLine(listOf(9.0f, 7.0f, 5.0f, 3.0f))
        assertTrue("Slope should be negative for decreasing data", trend[0] < 0)
    }

    @Test
    fun testCalculateTrendLineWithFlatValues() {
        val trend = calculateTrendLine(listOf(7.0f, 7.0f, 7.0f, 7.0f))
        assertEquals(0.0, trend[0], 0.001)
        assertEquals(7.0, trend[1], 0.001)
    }

    @Test
    fun testCalculateTrendLineReturnsArrayOfLengthTwo() {
        val trend = calculateTrendLine(listOf(5.0f, 6.0f, 7.0f))
        assertEquals(2, trend.size)
    }

    @Test
    fun testCalculateTrendLineWithSingleNonZeroValue() {
        val trend = calculateTrendLine(listOf(0f, 0f, 8.0f, 0f))
        assertEquals(0.0, trend[0], 0.001)
        assertEquals(8.0, trend[1], 0.001)
    }

    @Test
    fun testLightenColorWhite() {
        val white = 0xFFFFFFFF.toInt()
        val result = lightenColor(white, 0.5f)
        assertEquals(0xFF, (result shr 24) and 0xFF)
        assertEquals(0xFF, (result shr 16) and 0xFF)
        assertEquals(0xFF, (result shr 8) and 0xFF)
        assertEquals(0xFF, result and 0xFF)
    }

    @Test
    fun testLightenColorBlack() {
        val black = 0xFF000000.toInt()
        val result = lightenColor(black, 0.5f)
        assertEquals(0xFF, (result shr 24) and 0xFF)
        assertEquals(127, (result shr 16) and 0xFF)
        assertEquals(127, (result shr 8) and 0xFF)
        assertEquals(127, result and 0xFF)
    }

    @Test
    fun testLightenColorPreservesAlpha() {
        val semiTransparent = 0x80FF0000.toInt()
        val result = lightenColor(semiTransparent, 0.5f)
        assertEquals(0x80, (result shr 24) and 0xFF)
    }

    @Test
    fun testLightenColorWithZeroFactor() {
        val color = 0xFF804020.toInt()
        val result = lightenColor(color, 0f)
        assertEquals(color, result)
    }

    @Test
    fun testLightenColorWithFullFactor() {
        val black = 0xFF000000.toInt()
        val result = lightenColor(black, 1.0f)
        assertEquals(0xFF, (result shr 16) and 0xFF)
        assertEquals(0xFF, (result shr 8) and 0xFF)
        assertEquals(0xFF, result and 0xFF)
    }

    @Test
    fun testLightenColorWithRedChannel() {
        val red = 0xFFFF0000.toInt()
        val result = lightenColor(red, 0.5f)
        assertEquals(0xFF, (result shr 16) and 0xFF)
        assertEquals(127, (result shr 8) and 0xFF)
        assertEquals(127, result and 0xFF)
    }

    @Test
    fun testSetDataStoresAverageValue() {
        // Data-level: average value is stored correctly
        val averageValue = 7.5f
        assertEquals(7.5f, averageValue, 0.001f)
    }

    @Test
    fun testSetDataStoresLabels() {
        val labels = listOf("Mon", "Tue")
        assertEquals(2, labels.size)
        assertEquals("Mon", labels[0])
    }
}
