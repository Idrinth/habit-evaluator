package de.idrinth.habitevaluator.android

import de.idrinth.habitevaluator.android.ui.navigation.Screen
import de.idrinth.habitevaluator.shared.model.PlannerGroup
import de.idrinth.habitevaluator.shared.model.WeekPlannerSlot
import de.idrinth.habitevaluator.shared.service.DayPlannerService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

/**
 * Tests for PlannerNavigationScreen routes, navigation structure,
 * and week plan overview logic.
 */
class PlannerNavigationScreenTest {

    @Test
    fun testPlannerRouteExists() {
        assertEquals("planner", Screen.Planner.route)
    }

    @Test
    fun testWeekPlannerRouteExists() {
        assertEquals("week_planner", Screen.WeekPlanner.route)
    }

    @Test
    fun testPlannerActivitiesRouteExists() {
        assertEquals("planner_activities", Screen.PlannerActivities.route)
    }

    @Test
    fun testPlannerGroupsRouteExists() {
        assertEquals("planner_groups", Screen.PlannerGroups.route)
    }

    @Test
    fun testWeekOverviewRouteExists() {
        assertEquals("week_overview", Screen.WeekOverview.route)
    }

    @Test
    fun testPlannerRoutesAreUnique() {
        val routes = setOf(
            Screen.Planner.route,
            Screen.WeekPlanner.route,
            Screen.WeekOverview.route,
            Screen.PlannerActivities.route,
            Screen.PlannerGroups.route
        )
        assertEquals(5, routes.size)
    }

    @Test
    fun testPlannerRouteIsNotEmpty() {
        assertTrue(Screen.Planner.route.isNotEmpty())
    }

    @Test
    fun testWeekPlannerRouteIsNotEmpty() {
        assertTrue(Screen.WeekPlanner.route.isNotEmpty())
    }

    @Test
    fun testPlannerActivitiesRouteIsNotEmpty() {
        assertTrue(Screen.PlannerActivities.route.isNotEmpty())
    }

    @Test
    fun testPlannerGroupsRouteIsNotEmpty() {
        assertTrue(Screen.PlannerGroups.route.isNotEmpty())
    }

    @Test
    fun testPlannerRoutesDoNotContainSpaces() {
        assertFalse(Screen.Planner.route.contains(" "))
        assertFalse(Screen.WeekPlanner.route.contains(" "))
        assertFalse(Screen.WeekOverview.route.contains(" "))
        assertFalse(Screen.PlannerActivities.route.contains(" "))
        assertFalse(Screen.PlannerGroups.route.contains(" "))
    }

    @Test
    fun testPlannerSubRoutesAreDistinctFromParent() {
        assertNotEquals(Screen.Planner.route, Screen.WeekPlanner.route)
        assertNotEquals(Screen.Planner.route, Screen.WeekOverview.route)
        assertNotEquals(Screen.Planner.route, Screen.PlannerActivities.route)
        assertNotEquals(Screen.Planner.route, Screen.PlannerGroups.route)
    }

    // --- Week plan overview tests (logic used by PlannerNavigationScreen) ---

    companion object {
        fun countFilledHoursForDay(allSlots: List<WeekPlannerSlot>, dayOfWeek: Int): Int {
            return allSlots
                .filter { it.dayOfWeek == dayOfWeek && it.groups != null && it.groups.isNotEmpty() }
                .sumOf { maxOf(1, it.duration) }
        }
    }

    @Test
    fun testWeekOverviewSummaryEmpty() {
        val service = DayPlannerService()
        val summary = service.getWeekSlotSummary(emptyList())
        assertEquals(0, summary[0])
        assertEquals(168, summary[1])
    }

    @Test
    fun testWeekOverviewSummaryWithSlots() {
        val group = PlannerGroup("Exercise")
        group.id = "g1"

        val slots = listOf(
            WeekPlannerSlot(1, 9).apply { id = "s1"; groups = hashSetOf(group) },
            WeekPlannerSlot(2, 10).apply { id = "s2"; groups = hashSetOf(group) },
            WeekPlannerSlot(3, 14).apply { id = "s3"; groups = hashSetOf(group) }
        )

        val service = DayPlannerService()
        val summary = service.getWeekSlotSummary(slots)
        assertEquals(3, summary[0])
        assertEquals(168, summary[1])
    }

    @Test
    fun testWeekOverviewSummaryWithMultiHourSlots() {
        val group = PlannerGroup("Work")
        group.id = "g1"

        val slots = listOf(
            WeekPlannerSlot(1, 9, 3).apply { id = "s1"; groups = hashSetOf(group) },
            WeekPlannerSlot(2, 14, 2).apply { id = "s2"; groups = hashSetOf(group) }
        )

        val service = DayPlannerService()
        val summary = service.getWeekSlotSummary(slots)
        assertEquals(5, summary[0])
        assertEquals(168, summary[1])
    }

    @Test
    fun testDailyBreakdownFilledHours() {
        val group = PlannerGroup("Exercise")
        group.id = "g1"

        val slots = listOf(
            WeekPlannerSlot(1, 9).apply { id = "s1"; groups = hashSetOf(group) },
            WeekPlannerSlot(1, 10).apply { id = "s2"; groups = hashSetOf(group) },
            WeekPlannerSlot(2, 14).apply { id = "s3"; groups = hashSetOf(group) }
        )

        assertEquals(2, countFilledHoursForDay(slots, 1))
        assertEquals(1, countFilledHoursForDay(slots, 2))
        assertEquals(0, countFilledHoursForDay(slots, 3))
    }

    @Test
    fun testDailyBreakdownMultiHourSlots() {
        val group = PlannerGroup("Work")
        group.id = "g1"

        val slots = listOf(
            WeekPlannerSlot(1, 9, 3).apply { id = "s1"; groups = hashSetOf(group) }
        )

        assertEquals(3, countFilledHoursForDay(slots, 1))
        assertEquals(0, countFilledHoursForDay(slots, 2))
    }

    @Test
    fun testDailyBreakdownEmptySlots() {
        for (day in 1..7) {
            assertEquals(0, countFilledHoursForDay(emptyList(), day))
        }
    }

    @Test
    fun testDailyBreakdownIgnoresSlotsWithoutGroups() {
        val group = PlannerGroup("Exercise")
        group.id = "g1"

        val slots = listOf(
            WeekPlannerSlot(1, 9).apply { id = "s1"; groups = hashSetOf(group) },
            WeekPlannerSlot(1, 10).apply { id = "s2"; groups = hashSetOf() },
            WeekPlannerSlot(1, 11).apply { id = "s3"; groups = null }
        )

        assertEquals(1, countFilledHoursForDay(slots, 1))
    }

    @Test
    fun testDailyBreakdownDayLabelsAreValid() {
        for (day in 1..7) {
            val label = DayOfWeek.of(day).getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
            assertTrue(label.isNotEmpty())
        }
    }
}
