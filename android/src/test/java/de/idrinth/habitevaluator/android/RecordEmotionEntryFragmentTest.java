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
}
