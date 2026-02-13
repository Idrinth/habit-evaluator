package de.idrinth.habitevaluator.android;

import org.junit.Test;

import static org.junit.Assert.*;

public class AddEmotionPairFragmentTest {

    @Test
    public void testRequiredLabelCountValue() {
        assertEquals(2, AddEmotionPairFragment.REQUIRED_LABEL_COUNT);
    }

    @Test
    public void testAreLabelsValidWithBothLabels() {
        assertTrue(AddEmotionPairFragment.areLabelsValid("Sad", "Happy"));
    }

    @Test
    public void testAreLabelsValidWithEmptyNegativeLabel() {
        assertFalse(AddEmotionPairFragment.areLabelsValid("", "Happy"));
    }

    @Test
    public void testAreLabelsValidWithEmptyPositiveLabel() {
        assertFalse(AddEmotionPairFragment.areLabelsValid("Sad", ""));
    }

    @Test
    public void testAreLabelsValidWithBothEmpty() {
        assertFalse(AddEmotionPairFragment.areLabelsValid("", ""));
    }

    @Test
    public void testAreLabelsValidWithNullNegativeLabel() {
        assertFalse(AddEmotionPairFragment.areLabelsValid(null, "Happy"));
    }

    @Test
    public void testAreLabelsValidWithNullPositiveLabel() {
        assertFalse(AddEmotionPairFragment.areLabelsValid("Sad", null));
    }

    @Test
    public void testAreLabelsValidWithBothNull() {
        assertFalse(AddEmotionPairFragment.areLabelsValid(null, null));
    }

    @Test
    public void testAreLabelsValidWithWhitespaceNegativeLabel() {
        assertFalse(AddEmotionPairFragment.areLabelsValid("   ", "Happy"));
    }

    @Test
    public void testAreLabelsValidWithWhitespacePositiveLabel() {
        assertFalse(AddEmotionPairFragment.areLabelsValid("Sad", "   "));
    }

    @Test
    public void testAreLabelsValidWithBothWhitespace() {
        assertFalse(AddEmotionPairFragment.areLabelsValid("   ", "   "));
    }

    @Test
    public void testAreLabelsValidWithSingleCharacterLabels() {
        assertTrue(AddEmotionPairFragment.areLabelsValid("A", "B"));
    }

    @Test
    public void testAreLabelsValidWithLeadingTrailingSpaces() {
        assertTrue(AddEmotionPairFragment.areLabelsValid(" Sad ", " Happy "));
    }

    @Test
    public void testAreLabelsValidWithIdenticalLabels() {
        assertTrue(AddEmotionPairFragment.areLabelsValid("Same", "Same"));
    }
}
