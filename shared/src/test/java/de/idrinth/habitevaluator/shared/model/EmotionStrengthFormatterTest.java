package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmotionStrengthFormatterTest {

    @Test
    void testFormatPositiveStrength() {
        assertEquals("50% energetic", EmotionStrengthFormatter.format(5, "fatigued", "energetic"));
    }

    @Test
    void testFormatNegativeStrength() {
        assertEquals("30% fatigued", EmotionStrengthFormatter.format(-3, "fatigued", "energetic"));
    }

    @Test
    void testFormatZeroStrength() {
        assertEquals("0%", EmotionStrengthFormatter.format(0, "fatigued", "energetic"));
    }

    @Test
    void testFormatMaxPositive() {
        assertEquals("100% happy", EmotionStrengthFormatter.format(10, "sad", "happy"));
    }

    @Test
    void testFormatMaxNegative() {
        assertEquals("100% sad", EmotionStrengthFormatter.format(-10, "sad", "happy"));
    }

    @Test
    void testFormatWithPair() {
        EmotionPair pair = new EmotionPair("angry", "calm");
        assertEquals("70% calm", EmotionStrengthFormatter.format(7, pair));
    }

    @Test
    void testFormatWithPairNegative() {
        EmotionPair pair = new EmotionPair("angry", "calm");
        assertEquals("40% angry", EmotionStrengthFormatter.format(-4, pair));
    }

    @Test
    void testFormatWithNullPair() {
        assertEquals("50%", EmotionStrengthFormatter.format(5, (EmotionPair) null));
    }

    @Test
    void testFormatWithNullPairZero() {
        assertEquals("0%", EmotionStrengthFormatter.format(0, (EmotionPair) null));
    }

    @Test
    void testFormatDouble() {
        assertEquals("50% happy", EmotionStrengthFormatter.formatDouble(5.0, "sad", "happy"));
    }

    @Test
    void testFormatDoubleNegative() {
        assertEquals("30% sad", EmotionStrengthFormatter.formatDouble(-3.0, "sad", "happy"));
    }

    @Test
    void testFormatDoubleZero() {
        assertEquals("0%", EmotionStrengthFormatter.formatDouble(0.0, "sad", "happy"));
    }

    @Test
    void testFormatDoubleRounding() {
        assertEquals("35% happy", EmotionStrengthFormatter.formatDouble(3.5, "sad", "happy"));
    }
}
