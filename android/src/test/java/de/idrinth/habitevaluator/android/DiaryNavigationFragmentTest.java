package de.idrinth.habitevaluator.android;

import org.junit.Test;

import static org.junit.Assert.*;

public class DiaryNavigationFragmentTest {

    @Test
    public void testNavigationCardCountValue() {
        assertEquals(5, DiaryNavigationFragment.NAVIGATION_CARD_COUNT);
    }

    @Test
    public void testNavigationTargetsNotNull() {
        assertNotNull(DiaryNavigationFragment.NAVIGATION_TARGETS);
    }

    @Test
    public void testNavigationTargetsLength() {
        assertEquals(DiaryNavigationFragment.NAVIGATION_CARD_COUNT,
                DiaryNavigationFragment.NAVIGATION_TARGETS.length);
    }

    @Test
    public void testNavigationTargetsContainsPositivityDiary() {
        assertTrue(DiaryNavigationFragment.isValidNavigationTarget("positivityDiary"));
    }

    @Test
    public void testNavigationTargetsContainsSportLog() {
        assertTrue(DiaryNavigationFragment.isValidNavigationTarget("sportLog"));
    }

    @Test
    public void testNavigationTargetsContainsFoodLog() {
        assertTrue(DiaryNavigationFragment.isValidNavigationTarget("foodLog"));
    }

    @Test
    public void testNavigationTargetsContainsMedicationLog() {
        assertTrue(DiaryNavigationFragment.isValidNavigationTarget("medicationLog"));
    }

    @Test
    public void testNavigationTargetsContainsMedicationList() {
        assertTrue(DiaryNavigationFragment.isValidNavigationTarget("medicationList"));
    }

    @Test
    public void testIsValidNavigationTargetWithInvalidTarget() {
        assertFalse(DiaryNavigationFragment.isValidNavigationTarget("nonExistent"));
    }

    @Test
    public void testIsValidNavigationTargetWithNull() {
        assertFalse(DiaryNavigationFragment.isValidNavigationTarget(null));
    }

    @Test
    public void testIsValidNavigationTargetWithEmptyString() {
        assertFalse(DiaryNavigationFragment.isValidNavigationTarget(""));
    }

    @Test
    public void testIsValidNavigationTargetIsCaseSensitive() {
        assertFalse(DiaryNavigationFragment.isValidNavigationTarget("SPORTLOG"));
    }

    @Test
    public void testIsValidNavigationTargetWithPartialMatch() {
        assertFalse(DiaryNavigationFragment.isValidNavigationTarget("sport"));
    }

    @Test
    public void testAllNavigationTargetsAreNonNull() {
        for (String target : DiaryNavigationFragment.NAVIGATION_TARGETS) {
            assertNotNull(target);
        }
    }

    @Test
    public void testAllNavigationTargetsAreNonEmpty() {
        for (String target : DiaryNavigationFragment.NAVIGATION_TARGETS) {
            assertFalse(target.isEmpty());
        }
    }

    @Test
    public void testAllNavigationTargetsAreValidatable() {
        for (String target : DiaryNavigationFragment.NAVIGATION_TARGETS) {
            assertTrue(DiaryNavigationFragment.isValidNavigationTarget(target));
        }
    }
}
