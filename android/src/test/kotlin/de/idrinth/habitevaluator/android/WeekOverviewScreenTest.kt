package de.idrinth.habitevaluator.android

import de.idrinth.habitevaluator.android.ui.navigation.Screen
import de.idrinth.habitevaluator.android.ui.screens.isHourOccupied
import de.idrinth.habitevaluator.shared.model.PlannerGroup
import de.idrinth.habitevaluator.shared.model.WeekPlannerSlot
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for logic used by WeekOverviewScreen.
 * Validates the 7x24 grid cell occupation detection and routing.
 */
class WeekOverviewScreenTest {

    private fun createGroup(id: String = "g1", name: String = "Exercise"): PlannerGroup {
        val group = PlannerGroup(name)
        group.id = id
        return group
    }

    // --- Route tests ---

    @Test
    fun testWeekOverviewRouteExists() {
        assertEquals("week_overview", Screen.WeekOverview.route)
    }

    @Test
    fun testWeekOverviewRouteIsNotEmpty() {
        assertTrue(Screen.WeekOverview.route.isNotEmpty())
    }

    @Test
    fun testWeekOverviewRouteIsDistinctFromWeekPlanner() {
        assertNotEquals(Screen.WeekPlanner.route, Screen.WeekOverview.route)
    }

    @Test
    fun testWeekOverviewRouteIsDistinctFromPlanner() {
        assertNotEquals(Screen.Planner.route, Screen.WeekOverview.route)
    }

    @Test
    fun testWeekOverviewRouteDoesNotContainSpaces() {
        assertFalse(Screen.WeekOverview.route.contains(" "))
    }

    // --- isHourOccupied tests ---

    @Test
    fun testOccupiedWithMatchingSlot() {
        val group = createGroup()
        val slots = listOf(
            WeekPlannerSlot(1, 9).apply { id = "s1"; groups = hashSetOf(group) }
        )
        assertTrue(isHourOccupied(slots, 1, 9))
    }

    @Test
    fun testNotOccupiedDifferentDay() {
        val group = createGroup()
        val slots = listOf(
            WeekPlannerSlot(1, 9).apply { id = "s1"; groups = hashSetOf(group) }
        )
        assertFalse(isHourOccupied(slots, 2, 9))
    }

    @Test
    fun testNotOccupiedDifferentHour() {
        val group = createGroup()
        val slots = listOf(
            WeekPlannerSlot(1, 9).apply { id = "s1"; groups = hashSetOf(group) }
        )
        assertFalse(isHourOccupied(slots, 1, 10))
    }

    @Test
    fun testNotOccupiedEmptySlots() {
        assertFalse(isHourOccupied(emptyList(), 1, 9))
    }

    @Test
    fun testNotOccupiedNullGroups() {
        val slots = listOf(
            WeekPlannerSlot(1, 9).apply { id = "s1"; groups = null }
        )
        assertFalse(isHourOccupied(slots, 1, 9))
    }

    @Test
    fun testNotOccupiedEmptyGroups() {
        val slots = listOf(
            WeekPlannerSlot(1, 9).apply { id = "s1"; groups = hashSetOf() }
        )
        assertFalse(isHourOccupied(slots, 1, 9))
    }

    @Test
    fun testOccupiedMultiHourSlotCoversLaterHour() {
        val group = createGroup()
        val slots = listOf(
            WeekPlannerSlot(1, 9, 3).apply { id = "s1"; groups = hashSetOf(group) }
        )
        assertTrue(isHourOccupied(slots, 1, 9))
        assertTrue(isHourOccupied(slots, 1, 10))
        assertTrue(isHourOccupied(slots, 1, 11))
        assertFalse(isHourOccupied(slots, 1, 12))
    }

    @Test
    fun testOccupiedMultiHourSlotDoesNotCoverEarlierHour() {
        val group = createGroup()
        val slots = listOf(
            WeekPlannerSlot(1, 9, 3).apply { id = "s1"; groups = hashSetOf(group) }
        )
        assertFalse(isHourOccupied(slots, 1, 8))
    }

    @Test
    fun testAllDaysCanBeOccupied() {
        val group = createGroup()
        val slots = (1..7).map { day ->
            WeekPlannerSlot(day, 12).apply { id = "s$day"; groups = hashSetOf(group) }
        }
        for (day in 1..7) {
            assertTrue(isHourOccupied(slots, day, 12))
        }
    }

    @Test
    fun testAllHoursCanBeOccupied() {
        val group = createGroup()
        val slots = (0..23).map { hour ->
            WeekPlannerSlot(1, hour).apply { id = "s$hour"; groups = hashSetOf(group) }
        }
        for (hour in 0..23) {
            assertTrue(isHourOccupied(slots, 1, hour))
        }
    }

    @Test
    fun testMultipleSlotsOnSameDay() {
        val group = createGroup()
        val slots = listOf(
            WeekPlannerSlot(1, 9).apply { id = "s1"; groups = hashSetOf(group) },
            WeekPlannerSlot(1, 14).apply { id = "s2"; groups = hashSetOf(group) }
        )
        assertTrue(isHourOccupied(slots, 1, 9))
        assertFalse(isHourOccupied(slots, 1, 10))
        assertTrue(isHourOccupied(slots, 1, 14))
    }

    @Test
    fun testGridCoversFullWeek() {
        val group = createGroup()
        val slots = listOf(
            WeekPlannerSlot(1, 0).apply { id = "s1"; groups = hashSetOf(group) },
            WeekPlannerSlot(7, 23).apply { id = "s2"; groups = hashSetOf(group) }
        )
        assertTrue(isHourOccupied(slots, 1, 0))
        assertTrue(isHourOccupied(slots, 7, 23))
        assertFalse(isHourOccupied(slots, 4, 12))
    }
}
