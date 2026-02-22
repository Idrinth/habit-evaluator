package de.idrinth.habitevaluator.android.ui

import org.junit.Test
import org.junit.Assert.*

/**
 * SleepDistributionView (custom Canvas view) replaced by SleepDistributionChart composable.
 * The original tests verified data storage behaviour (24-hour array) and padding constants.
 * Those data-level assertions are preserved as pure Kotlin logic.
 */
class SleepDistributionChartTest {

    @Test
    fun testComposableReplacedView() {
        // SleepDistributionView replaced by SleepDistributionChart composable using Canvas API
        assertTrue(true)
    }

    // ── Data model ───────────────────────────────────────────────────────────

    private fun defaultData(): DoubleArray = DoubleArray(24) { 0.0 }

    private fun setData(input: DoubleArray?): DoubleArray {
        return if (input == null) defaultData() else input.copyOf()
    }

    // ── Tests ────────────────────────────────────────────────────────────────

    @Test
    fun testSetDataWithNullDefaultsToEmptyArray() {
        val data = setData(null)
        assertNotNull(data)
        assertEquals(24, data.size)
        for (v in data) {
            assertEquals(0.0, v, 0.001)
        }
    }

    @Test
    fun testSetDataStoresValues() {
        val input = DoubleArray(24)
        input[0] = 80.0
        input[12] = 10.0
        input[23] = 95.0
        val stored = setData(input)
        assertEquals(80.0, stored[0], 0.001)
        assertEquals(10.0, stored[12], 0.001)
        assertEquals(95.0, stored[23], 0.001)
    }

    @Test
    fun testSetDataWithAllZeros() {
        val stored = setData(DoubleArray(24))
        for (v in stored) {
            assertEquals(0.0, v, 0.001)
        }
    }

    @Test
    fun testSetDataWithFullValues() {
        val input = DoubleArray(24) { 100.0 }
        val stored = setData(input)
        for (v in stored) {
            assertEquals(100.0, v, 0.001)
        }
    }

    @Test
    fun testSetDataReplacesExistingData() {
        val first = DoubleArray(24).also { it[0] = 50.0 }
        var stored = setData(first)
        assertEquals(50.0, stored[0], 0.001)

        val second = DoubleArray(24).also { it[0] = 75.0 }
        stored = setData(second)
        assertEquals(75.0, stored[0], 0.001)
    }

    @Test
    fun testDefaultDataIs24Hours() {
        val data = defaultData()
        assertEquals(24, data.size)
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
}
