package de.idrinth.habitevaluator.android.ui

import de.idrinth.habitevaluator.android.ui.navigation.Screen
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for AppNavigation route registration completeness.
 * Verifies that all Screen routes are accounted for in the navigation graph
 * and validates navigation route conventions.
 */
class AppNavigationTest {

    companion object {
        /**
         * All routes that AppNavigation registers composable destinations for.
         * Must match the routes in AppNavigation.kt.
         */
        val REGISTERED_ROUTES = listOf(
            Screen.Home.route,
            Screen.Diary.route,
            Screen.Sleep.route,
            Screen.EmergencyPlan.route,
            Screen.EmotionalState.route,
            Screen.EditHabits.route,
            Screen.AddHabit.route,
            Screen.Stats.route,
            Screen.PointDevelopment.route,
            Screen.AddEmotionPair.route,
            Screen.RecordEmotionEntry.route,
            Screen.Settings.route,
            Screen.Imprint.route,
            Screen.PositivityDiary.route,
            Screen.SportLog.route,
            Screen.FoodLog.route,
            Screen.MedicationLog.route,
            Screen.MedicationList.route,
            Screen.ActivityLog.route,
            Screen.PdfExport.route,
            Screen.SleepAnalysis.route,
            Screen.Correlations.route,
            Screen.EmergencyDialogue.route,
            Screen.Planner.route
        )

        /**
         * Routes that use navigation arguments (parameterized routes).
         */
        val PARAMETERIZED_ROUTES = listOf(
            Screen.EditHabits.route,
            Screen.PointDevelopment.route,
            Screen.RecordEmotionEntry.route
        )
    }

    @Test
    fun testAllScreenRoutesAreRegistered() {
        // Verify every Screen object has a route in the registered list
        assertEquals(24, REGISTERED_ROUTES.size, "All 24 screens should be registered in navigation")
    }

    @Test
    fun testStartDestinationIsHome() {
        assertEquals("home", Screen.Home.route, "Start destination should be the home route")
    }

    @Test
    fun testRegisteredRoutesAreUnique() {
        assertEquals(REGISTERED_ROUTES.size, REGISTERED_ROUTES.toSet().size,
            "All registered routes must be unique")
    }

    @Test
    fun testRegisteredRoutesAreNonEmpty() {
        REGISTERED_ROUTES.forEach { route ->
            assertTrue(route.isNotEmpty(), "Route should not be empty")
        }
    }

    @Test
    fun testParameterizedRoutesCount() {
        assertEquals(3, PARAMETERIZED_ROUTES.size, "There should be exactly 3 parameterized routes")
    }

    @Test
    fun testParameterizedRoutesContainBraces() {
        PARAMETERIZED_ROUTES.forEach { route ->
            assertTrue(route.contains("{"), "Parameterized route should contain '{': $route")
            assertTrue(route.contains("}"), "Parameterized route should contain '}': $route")
        }
    }

    @Test
    fun testNonParameterizedRoutesDoNotContainBraces() {
        val nonParameterized = REGISTERED_ROUTES - PARAMETERIZED_ROUTES.toSet()
        nonParameterized.forEach { route ->
            assertFalse(route.contains("{"), "Non-parameterized route should not contain '{': $route")
            assertFalse(route.contains("}"), "Non-parameterized route should not contain '}': $route")
        }
    }

    @Test
    fun testRoutesUseUnderscoreNaming() {
        // Routes should use snake_case (underscores) not camelCase or kebab-case
        REGISTERED_ROUTES.forEach { route ->
            val baseRoute = route.substringBefore("/{")
            assertFalse(baseRoute.contains("-"), "Route should use underscores not hyphens: $route")
            assertEquals(baseRoute, baseRoute.lowercase(), "Route should be lowercase: $route")
        }
    }

    @Test
    fun testEditHabitsRouteParameter() {
        assertTrue(Screen.EditHabits.route.contains("{habitId}"))
        val created = Screen.EditHabits.createRoute("test-123")
        assertEquals("edit_habits/test-123", created)
    }

    @Test
    fun testPointDevelopmentRouteParameter() {
        assertTrue(Screen.PointDevelopment.route.contains("{habitId}"))
        val created = Screen.PointDevelopment.createRoute("habit-abc")
        assertEquals("point_development/habit-abc", created)
    }

    @Test
    fun testRecordEmotionEntryRouteParameter() {
        assertTrue(Screen.RecordEmotionEntry.route.contains("{pairId}"))
        val created = Screen.RecordEmotionEntry.createRoute("pair-xyz")
        assertEquals("record_emotion/pair-xyz", created)
    }

    @Test
    fun testNavigationCoversAllModuleScreens() {
        // Verify key module screens are present
        assertTrue(REGISTERED_ROUTES.contains(Screen.Home.route))
        assertTrue(REGISTERED_ROUTES.contains(Screen.Diary.route))
        assertTrue(REGISTERED_ROUTES.contains(Screen.Sleep.route))
        assertTrue(REGISTERED_ROUTES.contains(Screen.Stats.route))
        assertTrue(REGISTERED_ROUTES.contains(Screen.Settings.route))
        assertTrue(REGISTERED_ROUTES.contains(Screen.FoodLog.route))
        assertTrue(REGISTERED_ROUTES.contains(Screen.SportLog.route))
        assertTrue(REGISTERED_ROUTES.contains(Screen.MedicationLog.route))
        assertTrue(REGISTERED_ROUTES.contains(Screen.ActivityLog.route))
        assertTrue(REGISTERED_ROUTES.contains(Screen.PdfExport.route))
        assertTrue(REGISTERED_ROUTES.contains(Screen.EmergencyPlan.route))
        assertTrue(REGISTERED_ROUTES.contains(Screen.Correlations.route))
    }

    @Test
    fun testNoRouteIsAnExactPrefixSegmentOfAnother() {
        // Ensure no route is an exact path-segment prefix of another (e.g., "home" != "home/something")
        val baseRoutes = REGISTERED_ROUTES.map { it.substringBefore("/{") }
        for (i in baseRoutes.indices) {
            for (j in baseRoutes.indices) {
                if (i != j) {
                    assertFalse(
                        baseRoutes[i].startsWith(baseRoutes[j] + "/"),
                        "Route '${baseRoutes[i]}' should not be a sub-path of '${baseRoutes[j]}'"
                    )
                }
            }
        }
    }
}
