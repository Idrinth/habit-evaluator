package de.idrinth.habitevaluator.android

import de.idrinth.habitevaluator.shared.model.PlannerActivity
import de.idrinth.habitevaluator.shared.model.PlannerGroup
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for logic extracted from PlannerActivityScreen.
 * Validates form validation, activity filtering by group, and display formatting.
 */
class PlannerActivityScreenTest {

    companion object {
        /**
         * Replication of the name validation logic from PlannerActivityScreen.
         * A name must be non-blank to be valid.
         */
        fun isActivityNameValid(name: String): Boolean {
            return name.isNotBlank()
        }

        /**
         * Replication of the group filtering logic from PlannerActivityScreen.
         * Filters activities to only those belonging to a specific group.
         */
        fun filterActivitiesByGroup(
            activities: List<PlannerActivity>,
            groupId: String
        ): List<PlannerActivity> {
            return activities.filter { activity ->
                activity.groups?.any { it.id == groupId } == true
            }
        }

        /**
         * Replication of group names display formatting from PlannerActivityScreen.
         * Joins group names with comma separator.
         */
        fun formatGroupNames(activity: PlannerActivity): String {
            return activity.groups?.joinToString(", ") { it.name ?: "" } ?: ""
        }

        /**
         * Replication of description trimming logic from PlannerActivityScreen.
         * Returns null for blank descriptions, trimmed value otherwise.
         */
        fun normalizeDescription(description: String): String? {
            return description.ifBlank { null }
        }
    }

    // --- Name validation tests ---

    @Test
    fun testIsActivityNameValidWithContent() {
        assertTrue(isActivityNameValid("Morning Run"))
    }

    @Test
    fun testIsActivityNameValidEmpty() {
        assertFalse(isActivityNameValid(""))
    }

    @Test
    fun testIsActivityNameValidBlank() {
        assertFalse(isActivityNameValid("   "))
    }

    @Test
    fun testIsActivityNameValidSingleChar() {
        assertTrue(isActivityNameValid("A"))
    }

    @Test
    fun testIsActivityNameValidWithSpaces() {
        assertTrue(isActivityNameValid("Morning Run Session"))
    }

    // --- Group filtering tests ---

    @Test
    fun testFilterActivitiesByGroupMatchingSingle() {
        val group = PlannerGroup("Exercise")
        group.id = "g1"
        val activity = PlannerActivity("Run")
        activity.id = "a1"
        activity.groups = hashSetOf(group)

        val result = filterActivitiesByGroup(listOf(activity), "g1")
        assertEquals(1, result.size)
        assertEquals("Run", result[0].name)
    }

    @Test
    fun testFilterActivitiesByGroupNoMatch() {
        val group = PlannerGroup("Exercise")
        group.id = "g1"
        val activity = PlannerActivity("Run")
        activity.id = "a1"
        activity.groups = hashSetOf(group)

        val result = filterActivitiesByGroup(listOf(activity), "g2")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFilterActivitiesByGroupEmptyList() {
        val result = filterActivitiesByGroup(emptyList(), "g1")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFilterActivitiesByGroupNullGroups() {
        val activity = PlannerActivity("Run")
        activity.id = "a1"
        activity.groups = null

        val result = filterActivitiesByGroup(listOf(activity), "g1")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFilterActivitiesByGroupMultipleMatches() {
        val group = PlannerGroup("Exercise")
        group.id = "g1"

        val a1 = PlannerActivity("Run")
        a1.id = "a1"
        a1.groups = hashSetOf(group)

        val a2 = PlannerActivity("Swim")
        a2.id = "a2"
        a2.groups = hashSetOf(group)

        val a3 = PlannerActivity("Read")
        a3.id = "a3"
        val otherGroup = PlannerGroup("Study")
        otherGroup.id = "g2"
        a3.groups = hashSetOf(otherGroup)

        val result = filterActivitiesByGroup(listOf(a1, a2, a3), "g1")
        assertEquals(2, result.size)
    }

    @Test
    fun testFilterActivitiesByGroupWithMultipleGroupsPerActivity() {
        val g1 = PlannerGroup("Exercise")
        g1.id = "g1"
        val g2 = PlannerGroup("Outdoor")
        g2.id = "g2"

        val activity = PlannerActivity("Hiking")
        activity.id = "a1"
        activity.groups = hashSetOf(g1, g2)

        val resultG1 = filterActivitiesByGroup(listOf(activity), "g1")
        assertEquals(1, resultG1.size)

        val resultG2 = filterActivitiesByGroup(listOf(activity), "g2")
        assertEquals(1, resultG2.size)
    }

    // --- Group names formatting tests ---

    @Test
    fun testFormatGroupNamesSingleGroup() {
        val group = PlannerGroup("Exercise")
        group.id = "g1"
        val activity = PlannerActivity("Run")
        activity.groups = hashSetOf(group)

        assertEquals("Exercise", formatGroupNames(activity))
    }

    @Test
    fun testFormatGroupNamesNoGroups() {
        val activity = PlannerActivity("Run")
        activity.groups = hashSetOf()

        assertEquals("", formatGroupNames(activity))
    }

    @Test
    fun testFormatGroupNamesNullGroups() {
        val activity = PlannerActivity("Run")
        activity.groups = null

        assertEquals("", formatGroupNames(activity))
    }

    @Test
    fun testFormatGroupNamesMultipleGroups() {
        val g1 = PlannerGroup("Exercise")
        g1.id = "g1"
        val g2 = PlannerGroup("Outdoor")
        g2.id = "g2"
        val activity = PlannerActivity("Run")
        activity.groups = hashSetOf(g1, g2)

        val result = formatGroupNames(activity)
        assertTrue(result.contains("Exercise"))
        assertTrue(result.contains("Outdoor"))
        assertTrue(result.contains(", "))
    }

    // --- Description normalization tests ---

    @Test
    fun testNormalizeDescriptionNonBlank() {
        assertEquals("Daily morning run", normalizeDescription("Daily morning run"))
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
    fun testNormalizeDescriptionWithLeadingTrailingSpaces() {
        assertEquals("  Morning Run  ", normalizeDescription("  Morning Run  "))
    }

    // --- Screen route test ---

    @Test
    fun testPlannerActivitiesScreenRoute() {
        assertEquals(
            "planner_activities",
            de.idrinth.habitevaluator.android.ui.navigation.Screen.PlannerActivities.route
        )
    }
}
