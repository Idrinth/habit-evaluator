package de.idrinth.habitevaluator.android

import de.idrinth.habitevaluator.shared.model.PlannerActivity
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

        /**
         * Replication of the activity filtering logic from PlannerGroupScreen.
         * Returns activities whose groups set contains the given group ID.
         */
        fun activitiesForGroup(
            groupId: String,
            activities: List<PlannerActivity>
        ): List<PlannerActivity> {
            return activities.filter { activity ->
                activity.groups.any { it.id == groupId }
            }
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

    // --- Activity filtering tests ---

    @Test
    fun testActivitiesForGroupReturnsMatchingActivities() {
        val group1 = PlannerGroup("Exercise").apply { id = "g1" }
        val group2 = PlannerGroup("Study").apply { id = "g2" }
        val activity1 = PlannerActivity("Running").apply {
            id = "a1"
            groups = setOf(group1)
        }
        val activity2 = PlannerActivity("Reading").apply {
            id = "a2"
            groups = setOf(group2)
        }
        val activity3 = PlannerActivity("Yoga").apply {
            id = "a3"
            groups = setOf(group1, group2)
        }
        val result = activitiesForGroup("g1", listOf(activity1, activity2, activity3))
        assertEquals(2, result.size)
        assertTrue(result.any { it.id == "a1" })
        assertTrue(result.any { it.id == "a3" })
    }

    @Test
    fun testActivitiesForGroupReturnsEmptyForNoMatch() {
        val group1 = PlannerGroup("Exercise").apply { id = "g1" }
        val activity1 = PlannerActivity("Running").apply {
            id = "a1"
            groups = setOf(group1)
        }
        val result = activitiesForGroup("g999", listOf(activity1))
        assertTrue(result.isEmpty())
    }

    @Test
    fun testActivitiesForGroupEmptyActivitiesList() {
        val result = activitiesForGroup("g1", emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun testActivitiesForGroupActivityInMultipleGroups() {
        val group1 = PlannerGroup("Exercise").apply { id = "g1" }
        val group2 = PlannerGroup("Outdoor").apply { id = "g2" }
        val activity = PlannerActivity("Hiking").apply {
            id = "a1"
            groups = setOf(group1, group2)
        }
        val resultG1 = activitiesForGroup("g1", listOf(activity))
        val resultG2 = activitiesForGroup("g2", listOf(activity))
        assertEquals(1, resultG1.size)
        assertEquals(1, resultG2.size)
        assertEquals("a1", resultG1[0].id)
        assertEquals("a1", resultG2[0].id)
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
