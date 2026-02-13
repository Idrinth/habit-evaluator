package de.idrinth.habitevaluator.android;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddEmotionPairFragmentTest {

    @Test
    void testRequiredLabelCountValue() {
        assertEquals(2, AddEmotionPairFragment.REQUIRED_LABEL_COUNT);
    }

    @Test
    void testAreLabelsValidWithBothLabels() {
        assertTrue(AddEmotionPairFragment.areLabelsValid("Sad", "Happy"));
    }

    @Test
    void testAreLabelsValidWithEmptyNegativeLabel() {
        assertFalse(AddEmotionPairFragment.areLabelsValid("", "Happy"));
    }

    @Test
    void testAreLabelsValidWithEmptyPositiveLabel() {
        assertFalse(AddEmotionPairFragment.areLabelsValid("Sad", ""));
    }

    @Test
    void testAreLabelsValidWithBothEmpty() {
        assertFalse(AddEmotionPairFragment.areLabelsValid("", ""));
    }

    @Test
    void testAreLabelsValidWithNullNegativeLabel() {
        assertFalse(AddEmotionPairFragment.areLabelsValid(null, "Happy"));
    }

    @Test
    void testAreLabelsValidWithNullPositiveLabel() {
        assertFalse(AddEmotionPairFragment.areLabelsValid("Sad", null));
    }

    @Test
    void testAreLabelsValidWithBothNull() {
        assertFalse(AddEmotionPairFragment.areLabelsValid(null, null));
    }

    @Test
    void testAreLabelsValidWithWhitespaceNegativeLabel() {
        assertFalse(AddEmotionPairFragment.areLabelsValid("   ", "Happy"));
    }

    @Test
    void testAreLabelsValidWithWhitespacePositiveLabel() {
        assertFalse(AddEmotionPairFragment.areLabelsValid("Sad", "   "));
    }

    @Test
    void testAreLabelsValidWithBothWhitespace() {
        assertFalse(AddEmotionPairFragment.areLabelsValid("   ", "   "));
    }

    @Test
    void testAreLabelsValidWithSingleCharacterLabels() {
        assertTrue(AddEmotionPairFragment.areLabelsValid("A", "B"));
    }

    @Test
    void testAreLabelsValidWithLeadingTrailingSpaces() {
        assertTrue(AddEmotionPairFragment.areLabelsValid(" Sad ", " Happy "));
    }

    @Test
    void testAreLabelsValidWithIdenticalLabels() {
        assertTrue(AddEmotionPairFragment.areLabelsValid("Same", "Same"));
    }
}
