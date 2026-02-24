package de.idrinth.habitevaluator.android

import de.idrinth.habitevaluator.shared.model.PlannerGroup
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for logic extracted from PlannerGroupScreen.
 * Validates form validation, group display, and name uniqueness checking.
 */
class PlannerGroupScreenTest {

    companion object {
        /**
         * Replication of the name validation logic from PlannerGroupScreen.
         * A name must be non-blank to be valid.
         */
        fun isGroupNameValid(name: String): Boolean {
            return name.isNotBlank()
        }

        /**
         * Replication of the description trimming logic from PlannerGroupScreen.
         * Returns null for blank descriptions, original value otherwise.
         */
        fun normalizeDescription(description: String): String? {
            return description.ifBlank { null }
        }

        /**
         * Checks if a group name is already used in the existing groups list.
         * Case-insensitive comparison, trims whitespace.
         */
        fun isGroupNameDuplicate(name: String, existingGroups: List<PlannerGroup>): Boolean {
            val trimmed = name.trim().lowercase()
            return existingGroups.any { (it.name ?: "").trim().lowercase() == trimmed }
        }
    }

    // --- Name validation tests ---

    @Test
    fun testIsGroupNameValidWithContent() {
        assertTrue(isGroupNameValid("Exercise"))
    }

    @Test
    fun testIsGroupNameValidEmpty() {
        assertFalse(isGroupNameValid(""))
    }

    @Test
    fun testIsGroupNameValidBlank() {
        assertFalse(isGroupNameValid("   "))
    }

    @Test
    fun testIsGroupNameValidSingleChar() {
        assertTrue(isGroupNameValid("A"))
    }

    @Test
    fun testIsGroupNameValidWithSpacesInName() {
        assertTrue(isGroupNameValid("Morning Routine"))
    }

    @Test
    fun testIsGroupNameValidTab() {
        assertFalse(isGroupNameValid("\t"))
    }

    // --- Description normalization tests ---

    @Test
    fun testNormalizeDescriptionNonBlank() {
        assertEquals("Physical activities", normalizeDescription("Physical activities"))
    }

    @Test
    fun testNormalizeDescriptionEmpty() {
        assertNull(normalizeDescription(""))
    }

    @Test
    fun testNormalizeDescriptionBlank() {
        assertNull(normalizeDescription("   "))
    }

    @Test
    fun testNormalizeDescriptionPreservesWhitespace() {
        assertEquals("  description  ", normalizeDescription("  description  "))
    }

    // --- Group name duplicate tests ---

    @Test
    fun testIsGroupNameDuplicateMatches() {
        val groups = listOf(PlannerGroup("Exercise").apply { id = "g1" })
        assertTrue(isGroupNameDuplicate("Exercise", groups))
    }

    @Test
    fun testIsGroupNameDuplicateCaseInsensitive() {
        val groups = listOf(PlannerGroup("Exercise").apply { id = "g1" })
        assertTrue(isGroupNameDuplicate("exercise", groups))
        assertTrue(isGroupNameDuplicate("EXERCISE", groups))
    }

    @Test
    fun testIsGroupNameDuplicateWithWhitespace() {
        val groups = listOf(PlannerGroup("Exercise").apply { id = "g1" })
        assertTrue(isGroupNameDuplicate("  Exercise  ", groups))
    }

    @Test
    fun testIsGroupNameDuplicateNoMatch() {
        val groups = listOf(PlannerGroup("Exercise").apply { id = "g1" })
        assertFalse(isGroupNameDuplicate("Study", groups))
    }

    @Test
    fun testIsGroupNameDuplicateEmptyList() {
        assertFalse(isGroupNameDuplicate("Exercise", emptyList()))
    }

    @Test
    fun testIsGroupNameDuplicateMultipleGroups() {
        val groups = listOf(
            PlannerGroup("Exercise").apply { id = "g1" },
            PlannerGroup("Study").apply { id = "g2" },
            PlannerGroup("Social").apply { id = "g3" }
        )
        assertTrue(isGroupNameDuplicate("Study", groups))
        assertFalse(isGroupNameDuplicate("Cooking", groups))
    }

    // --- Screen route test ---

    @Test
    fun testPlannerGroupsScreenRoute() {
        assertEquals(
            "planner_groups",
            de.idrinth.habitevaluator.android.ui.navigation.Screen.PlannerGroups.route
        )
    }
}
