package de.idrinth.habitevaluator.android;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiaryNavigationFragmentTest {

    @Test
    void testNavigationCardCountValue() {
        assertEquals(6, DiaryNavigationFragment.NAVIGATION_CARD_COUNT);
    }

    @Test
    void testNavigationTargetsNotNull() {
        assertNotNull(DiaryNavigationFragment.NAVIGATION_TARGETS);
    }

    @Test
    void testNavigationTargetsLength() {
        assertEquals(DiaryNavigationFragment.NAVIGATION_CARD_COUNT,
                DiaryNavigationFragment.NAVIGATION_TARGETS.length);
    }

    @Test
    void testNavigationTargetsContainsPositivityDiary() {
        assertTrue(DiaryNavigationFragment.isValidNavigationTarget("positivityDiary"));
    }

    @Test
    void testNavigationTargetsContainsSportLog() {
        assertTrue(DiaryNavigationFragment.isValidNavigationTarget("sportLog"));
    }

    @Test
    void testNavigationTargetsContainsFoodLog() {
        assertTrue(DiaryNavigationFragment.isValidNavigationTarget("foodLog"));
    }

    @Test
    void testNavigationTargetsContainsMedicationLog() {
        assertTrue(DiaryNavigationFragment.isValidNavigationTarget("medicationLog"));
    }

    @Test
    void testNavigationTargetsContainsMedicationList() {
        assertTrue(DiaryNavigationFragment.isValidNavigationTarget("medicationList"));
    }

    @Test
    void testNavigationTargetsContainsEmergencyPlan() {
        assertTrue(DiaryNavigationFragment.isValidNavigationTarget("emergencyPlan"));
    }

    @Test
    void testIsValidNavigationTargetWithInvalidTarget() {
        assertFalse(DiaryNavigationFragment.isValidNavigationTarget("nonExistent"));
    }

    @Test
    void testIsValidNavigationTargetWithNull() {
        assertFalse(DiaryNavigationFragment.isValidNavigationTarget(null));
    }

    @Test
    void testIsValidNavigationTargetWithEmptyString() {
        assertFalse(DiaryNavigationFragment.isValidNavigationTarget(""));
    }

    @Test
    void testIsValidNavigationTargetIsCaseSensitive() {
        assertFalse(DiaryNavigationFragment.isValidNavigationTarget("SPORTLOG"));
    }

    @Test
    void testIsValidNavigationTargetWithPartialMatch() {
        assertFalse(DiaryNavigationFragment.isValidNavigationTarget("sport"));
    }

    @Test
    void testAllNavigationTargetsAreNonNull() {
        for (String target : DiaryNavigationFragment.NAVIGATION_TARGETS) {
            assertNotNull(target);
        }
    }

    @Test
    void testAllNavigationTargetsAreNonEmpty() {
        for (String target : DiaryNavigationFragment.NAVIGATION_TARGETS) {
            assertFalse(target.isEmpty());
        }
    }

    @Test
    void testAllNavigationTargetsAreValidatable() {
        for (String target : DiaryNavigationFragment.NAVIGATION_TARGETS) {
            assertTrue(DiaryNavigationFragment.isValidNavigationTarget(target));
        }
    }
}
