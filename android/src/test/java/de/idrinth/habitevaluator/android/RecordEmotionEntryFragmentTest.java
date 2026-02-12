package de.idrinth.habitevaluator.android;

import org.junit.Test;

import static org.junit.Assert.*;

public class RecordEmotionEntryFragmentTest {

    @Test
    public void testSeekBarOffsetValue() {
        assertEquals(10, RecordEmotionEntryFragment.SEEKBAR_OFFSET);
    }

    @Test
    public void testSeekBarProgressToStrengthAtMinimum() {
        assertEquals(-10, RecordEmotionEntryFragment.seekBarProgressToStrength(0));
    }

    @Test
    public void testSeekBarProgressToStrengthAtMaximum() {
        assertEquals(10, RecordEmotionEntryFragment.seekBarProgressToStrength(20));
    }

    @Test
    public void testSeekBarProgressToStrengthAtCenter() {
        assertEquals(0, RecordEmotionEntryFragment.seekBarProgressToStrength(10));
    }

    @Test
    public void testSeekBarProgressToStrengthPositive() {
        assertEquals(5, RecordEmotionEntryFragment.seekBarProgressToStrength(15));
    }

    @Test
    public void testSeekBarProgressToStrengthNegative() {
        assertEquals(-3, RecordEmotionEntryFragment.seekBarProgressToStrength(7));
    }

    @Test
    public void testSeekBarProgressToStrengthAtOne() {
        assertEquals(-9, RecordEmotionEntryFragment.seekBarProgressToStrength(1));
    }

    @Test
    public void testSeekBarProgressToStrengthAtNineteen() {
        assertEquals(9, RecordEmotionEntryFragment.seekBarProgressToStrength(19));
    }
}
