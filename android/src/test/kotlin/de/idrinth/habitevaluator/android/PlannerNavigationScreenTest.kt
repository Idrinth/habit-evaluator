package de.idrinth.habitevaluator.android

import de.idrinth.habitevaluator.android.ui.navigation.Screen
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for PlannerNavigationScreen routes and navigation structure.
 * Verifies that all planner sub-routes are correctly defined.
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
    fun testPlannerRoutesAreUnique() {
        val routes = setOf(
            Screen.Planner.route,
            Screen.WeekPlanner.route,
            Screen.PlannerActivities.route,
            Screen.PlannerGroups.route
        )
        assertEquals(4, routes.size)
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
        assertFalse(Screen.PlannerActivities.route.contains(" "))
        assertFalse(Screen.PlannerGroups.route.contains(" "))
    }

    @Test
    fun testPlannerSubRoutesAreDistinctFromParent() {
        assertNotEquals(Screen.Planner.route, Screen.WeekPlanner.route)
        assertNotEquals(Screen.Planner.route, Screen.PlannerActivities.route)
        assertNotEquals(Screen.Planner.route, Screen.PlannerGroups.route)
    }
}
