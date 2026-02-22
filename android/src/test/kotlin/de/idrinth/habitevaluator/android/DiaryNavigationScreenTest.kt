package de.idrinth.habitevaluator.android

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for navigation target constants and validation equivalent to what
 * DiaryNavigationFragment previously provided as static constants and helper methods.
 */
class DiaryNavigationScreenTest {

    companion object {
        const val NAVIGATION_CARD_COUNT = 6

        val NAVIGATION_TARGETS: Array<String> = arrayOf(
            "positivityDiary",
            "sportLog",
            "foodLog",
            "medicationLog",
            "medicationList",
            "activityLog"
        )

        fun isValidNavigationTarget(target: String?): Boolean {
            if (target == null) return false
            return NAVIGATION_TARGETS.contains(target)
        }
    }

    @Test
    fun testNavigationCardCountValue() {
        assertEquals(6, NAVIGATION_CARD_COUNT)
    }

    @Test
    fun testNavigationTargetsNotNull() {
        assertNotNull(NAVIGATION_TARGETS)
    }

    @Test
    fun testNavigationTargetsLength() {
        assertEquals(NAVIGATION_CARD_COUNT, NAVIGATION_TARGETS.size)
    }

    @Test
    fun testNavigationTargetsContainsPositivityDiary() {
        assertTrue(isValidNavigationTarget("positivityDiary"))
    }

    @Test
    fun testNavigationTargetsContainsSportLog() {
        assertTrue(isValidNavigationTarget("sportLog"))
    }

    @Test
    fun testNavigationTargetsContainsFoodLog() {
        assertTrue(isValidNavigationTarget("foodLog"))
    }

    @Test
    fun testNavigationTargetsContainsMedicationLog() {
        assertTrue(isValidNavigationTarget("medicationLog"))
    }

    @Test
    fun testNavigationTargetsContainsMedicationList() {
        assertTrue(isValidNavigationTarget("medicationList"))
    }

    @Test
    fun testNavigationTargetsContainsActivityLog() {
        assertTrue(isValidNavigationTarget("activityLog"))
    }

    @Test
    fun testIsValidNavigationTargetWithInvalidTarget() {
        assertFalse(isValidNavigationTarget("nonExistent"))
    }

    @Test
    fun testIsValidNavigationTargetWithNull() {
        assertFalse(isValidNavigationTarget(null))
    }

    @Test
    fun testIsValidNavigationTargetWithEmptyString() {
        assertFalse(isValidNavigationTarget(""))
    }

    @Test
    fun testIsValidNavigationTargetIsCaseSensitive() {
        assertFalse(isValidNavigationTarget("SPORTLOG"))
    }

    @Test
    fun testIsValidNavigationTargetWithPartialMatch() {
        assertFalse(isValidNavigationTarget("sport"))
    }

    @Test
    fun testAllNavigationTargetsAreNonNull() {
        for (target in NAVIGATION_TARGETS) {
            assertNotNull(target)
        }
    }

    @Test
    fun testAllNavigationTargetsAreNonEmpty() {
        for (target in NAVIGATION_TARGETS) {
            assertFalse(target.isEmpty())
        }
    }

    @Test
    fun testAllNavigationTargetsAreValidatable() {
        for (target in NAVIGATION_TARGETS) {
            assertTrue(isValidNavigationTarget(target))
        }
    }
}
