package de.idrinth.habitevaluator.android;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecordEmotionEntryFragmentTest {

    @Test
    void testSeekBarOffsetValue() {
        assertEquals(10, RecordEmotionEntryFragment.SEEKBAR_OFFSET);
    }

    @Test
    void testSeekBarProgressToStrengthAtMinimum() {
        assertEquals(-10, RecordEmotionEntryFragment.seekBarProgressToStrength(0));
    }

    @Test
    void testSeekBarProgressToStrengthAtMaximum() {
        assertEquals(10, RecordEmotionEntryFragment.seekBarProgressToStrength(20));
    }

    @Test
    void testSeekBarProgressToStrengthAtCenter() {
        assertEquals(0, RecordEmotionEntryFragment.seekBarProgressToStrength(10));
    }

    @Test
    void testSeekBarProgressToStrengthPositive() {
        assertEquals(5, RecordEmotionEntryFragment.seekBarProgressToStrength(15));
    }

    @Test
    void testSeekBarProgressToStrengthNegative() {
        assertEquals(-3, RecordEmotionEntryFragment.seekBarProgressToStrength(7));
    }

    @Test
    void testSeekBarProgressToStrengthAtOne() {
        assertEquals(-9, RecordEmotionEntryFragment.seekBarProgressToStrength(1));
    }

    @Test
    void testSeekBarProgressToStrengthAtNineteen() {
        assertEquals(9, RecordEmotionEntryFragment.seekBarProgressToStrength(19));
    }

    @Test
    void testSeekBarProgressToStrengthIsLinear() {
        for (int progress = 0; progress <= 20; progress++) {
            int expected = progress - RecordEmotionEntryFragment.SEEKBAR_OFFSET;
            assertEquals(expected, RecordEmotionEntryFragment.seekBarProgressToStrength(progress));
        }
    }

    @Test
    void testSeekBarProgressToStrengthRangeCoversFullEmotionSpectrum() {
        assertEquals(-10, RecordEmotionEntryFragment.seekBarProgressToStrength(0));
        assertEquals(10, RecordEmotionEntryFragment.seekBarProgressToStrength(20));
    }

    @Test
    void testSeekBarOffsetIsZeroPoint() {
        assertEquals(0, RecordEmotionEntryFragment.seekBarProgressToStrength(
                RecordEmotionEntryFragment.SEEKBAR_OFFSET));
    }

    @Test
    void testSeekBarProgressSymmetry() {
        for (int n = 0; n <= 10; n++) {
            int negativeStrength = RecordEmotionEntryFragment.seekBarProgressToStrength(
                    RecordEmotionEntryFragment.SEEKBAR_OFFSET - n);
            int positiveStrength = RecordEmotionEntryFragment.seekBarProgressToStrength(
                    RecordEmotionEntryFragment.SEEKBAR_OFFSET + n);
            assertEquals(-n, negativeStrength);
            assertEquals(n, positiveStrength);
        }
    }

    @Test
    void testSeekBarProgressToStrengthAtTwo() {
        assertEquals(-8, RecordEmotionEntryFragment.seekBarProgressToStrength(2));
    }

    @Test
    void testSeekBarProgressToStrengthAtEighteen() {
        assertEquals(8, RecordEmotionEntryFragment.seekBarProgressToStrength(18));
    }
}
