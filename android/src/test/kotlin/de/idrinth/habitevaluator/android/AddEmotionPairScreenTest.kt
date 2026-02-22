package de.idrinth.habitevaluator.android

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for emotion pair label validation logic equivalent to what
 * AddEmotionPairFragment previously provided as static constants and helper methods.
 * Both a negative and a positive label are required; they must be non-null and non-blank.
 */
class AddEmotionPairScreenTest {

    companion object {
        const val REQUIRED_LABEL_COUNT = 2

        /**
         * Replication of areLabelsValid logic from AddEmotionPairFragment.
         * Both labels must be non-null and non-blank (after trimming) to be valid.
         */
        fun areLabelsValid(negativeLabel: String?, positiveLabel: String?): Boolean {
            return !negativeLabel.isNullOrBlank() && !positiveLabel.isNullOrBlank()
        }
    }

    @Test
    fun testRequiredLabelCountValue() {
        assertEquals(2, REQUIRED_LABEL_COUNT)
    }

    @Test
    fun testAreLabelsValidWithBothLabels() {
        assertTrue(areLabelsValid("Sad", "Happy"))
    }

    @Test
    fun testAreLabelsValidWithEmptyNegativeLabel() {
        assertFalse(areLabelsValid("", "Happy"))
    }

    @Test
    fun testAreLabelsValidWithEmptyPositiveLabel() {
        assertFalse(areLabelsValid("Sad", ""))
    }

    @Test
    fun testAreLabelsValidWithBothEmpty() {
        assertFalse(areLabelsValid("", ""))
    }

    @Test
    fun testAreLabelsValidWithNullNegativeLabel() {
        assertFalse(areLabelsValid(null, "Happy"))
    }

    @Test
    fun testAreLabelsValidWithNullPositiveLabel() {
        assertFalse(areLabelsValid("Sad", null))
    }

    @Test
    fun testAreLabelsValidWithBothNull() {
        assertFalse(areLabelsValid(null, null))
    }

    @Test
    fun testAreLabelsValidWithWhitespaceNegativeLabel() {
        assertFalse(areLabelsValid("   ", "Happy"))
    }

    @Test
    fun testAreLabelsValidWithWhitespacePositiveLabel() {
        assertFalse(areLabelsValid("Sad", "   "))
    }

    @Test
    fun testAreLabelsValidWithBothWhitespace() {
        assertFalse(areLabelsValid("   ", "   "))
    }

    @Test
    fun testAreLabelsValidWithSingleCharacterLabels() {
        assertTrue(areLabelsValid("A", "B"))
    }

    @Test
    fun testAreLabelsValidWithLeadingTrailingSpaces() {
        // Non-blank strings with surrounding spaces are still valid
        assertTrue(areLabelsValid(" Sad ", " Happy "))
    }

    @Test
    fun testAreLabelsValidWithIdenticalLabels() {
        assertTrue(areLabelsValid("Same", "Same"))
    }
}
