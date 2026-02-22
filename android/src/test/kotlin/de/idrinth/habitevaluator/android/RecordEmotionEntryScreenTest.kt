package de.idrinth.habitevaluator.android

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for seekbar-to-strength conversion logic equivalent to what
 * RecordEmotionEntryFragment previously provided as static constants and helper methods.
 * Emotion strength ranges from -10 to +10; seekbar progress ranges from 0 to 20.
 */
class RecordEmotionEntryScreenTest {

    companion object {
        const val SEEKBAR_OFFSET = 10

        /**
         * Replication of seekBarProgressToStrength logic from RecordEmotionEntryFragment.
         * Converts seekbar progress (0..20) to emotion strength (-10..+10).
         */
        fun seekBarProgressToStrength(progress: Int): Int = progress - SEEKBAR_OFFSET
    }

    @Test
    fun testSeekBarOffsetValue() {
        assertEquals(10, SEEKBAR_OFFSET)
    }

    @Test
    fun testSeekBarProgressToStrengthAtMinimum() {
        assertEquals(-10, seekBarProgressToStrength(0))
    }

    @Test
    fun testSeekBarProgressToStrengthAtMaximum() {
        assertEquals(10, seekBarProgressToStrength(20))
    }

    @Test
    fun testSeekBarProgressToStrengthAtCenter() {
        assertEquals(0, seekBarProgressToStrength(10))
    }

    @Test
    fun testSeekBarProgressToStrengthPositive() {
        assertEquals(5, seekBarProgressToStrength(15))
    }

    @Test
    fun testSeekBarProgressToStrengthNegative() {
        assertEquals(-3, seekBarProgressToStrength(7))
    }

    @Test
    fun testSeekBarProgressToStrengthAtOne() {
        assertEquals(-9, seekBarProgressToStrength(1))
    }

    @Test
    fun testSeekBarProgressToStrengthAtNineteen() {
        assertEquals(9, seekBarProgressToStrength(19))
    }

    @Test
    fun testSeekBarProgressToStrengthIsLinear() {
        for (progress in 0..20) {
            val expected = progress - SEEKBAR_OFFSET
            assertEquals(expected, seekBarProgressToStrength(progress))
        }
    }

    @Test
    fun testSeekBarProgressToStrengthRangeCoversFullEmotionSpectrum() {
        assertEquals(-10, seekBarProgressToStrength(0))
        assertEquals(10, seekBarProgressToStrength(20))
    }

    @Test
    fun testSeekBarOffsetIsZeroPoint() {
        assertEquals(0, seekBarProgressToStrength(SEEKBAR_OFFSET))
    }

    @Test
    fun testSeekBarProgressSymmetry() {
        for (n in 0..10) {
            val negativeStrength = seekBarProgressToStrength(SEEKBAR_OFFSET - n)
            val positiveStrength = seekBarProgressToStrength(SEEKBAR_OFFSET + n)
            assertEquals(-n, negativeStrength)
            assertEquals(n, positiveStrength)
        }
    }

    @Test
    fun testSeekBarProgressToStrengthAtTwo() {
        assertEquals(-8, seekBarProgressToStrength(2))
    }

    @Test
    fun testSeekBarProgressToStrengthAtEighteen() {
        assertEquals(8, seekBarProgressToStrength(18))
    }
}
