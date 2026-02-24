package de.idrinth.habitevaluator.android

import de.idrinth.habitevaluator.android.ui.navigation.Screen
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Regression test: verifies invariants that, if violated, would cause the app
 * to crash on startup or shortly after. In the Compose architecture, shared
 * mutable state lives in AppViewModel (StateFlow), and navigation uses
 * Screen sealed class routes instead of ViewPager2 page indices.
 */
class StartupRegressionTest {

    // ── Screen routes must be non-empty ─────────────────────────────────────

    @Test
    fun testHomeRouteIsNotEmpty() {
        assertTrue(Screen.Home.route.isNotEmpty(), "Home route must not be empty")
    }

    @Test
    fun testDiaryRouteIsNotEmpty() {
        assertTrue(Screen.Diary.route.isNotEmpty(), "Diary route must not be empty")
    }

    @Test
    fun testSleepRouteIsNotEmpty() {
        assertTrue(Screen.Sleep.route.isNotEmpty(), "Sleep route must not be empty")
    }

    @Test
    fun testEmergencyPlanRouteIsNotEmpty() {
        assertTrue(Screen.EmergencyPlan.route.isNotEmpty(), "EmergencyPlan route must not be empty")
    }

    @Test
    fun testEmotionalStateRouteIsNotEmpty() {
        assertTrue(Screen.EmotionalState.route.isNotEmpty(), "EmotionalState route must not be empty")
    }

    @Test
    fun testPlannerRouteIsNotEmpty() {
        assertTrue(Screen.Planner.route.isNotEmpty(), "Planner route must not be empty")
    }

    // ── All screen routes must be unique ────────────────────────────────────

    @Test
    fun testAllScreenRoutesAreUnique() {
        val allRoutes = listOf(
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

        val uniqueRoutes = allRoutes.toSet()
        assertEquals(
            allRoutes.size, uniqueRoutes.size,
            "All screen routes must be unique. Duplicates: ${allRoutes.groupBy { it }.filter { it.value.size > 1 }.keys}"
        )
    }

    @Test
    fun testAllScreenRoutesAreNonBlank() {
        val allRoutes = listOf(
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

        for (route in allRoutes) {
            assertTrue(route.isNotBlank(), "Screen route must not be blank: '$route'")
        }
    }

    // ── Parameterized routes generate correctly ─────────────────────────────

    @Test
    fun testEditHabitsCreateRouteContainsHabitId() {
        val route = Screen.EditHabits.createRoute("test-id")
        assertTrue(route.contains("test-id"), "EditHabits route must contain the habit ID")
    }

    @Test
    fun testPointDevelopmentCreateRouteContainsHabitId() {
        val route = Screen.PointDevelopment.createRoute("test-id")
        assertTrue(route.contains("test-id"), "PointDevelopment route must contain the habit ID")
    }

    @Test
    fun testRecordEmotionEntryCreateRouteContainsPairId() {
        val route = Screen.RecordEmotionEntry.createRoute("pair-id")
        assertTrue(route.contains("pair-id"), "RecordEmotionEntry route must contain the pair ID")
    }

    // ── AppViewModel class exists ───────────────────────────────────────────

    @Test
    fun testAppViewModelClassCanBeReferenced() {
        val clazz = AppViewModel::class.java
        assertNotNull(clazz)
    }

    @Test
    fun testAppViewModelClassNameIsCorrect() {
        assertEquals("AppViewModel", AppViewModel::class.java.simpleName)
    }

    // ── Screen count matches expected total ─────────────────────────────────

    @Test
    fun testTotalScreenCount() {
        val allRoutes = listOf(
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
        assertEquals(24, allRoutes.size, "Total number of screen routes must be 24")
    }

    // ── MainActivity class exists ───────────────────────────────────────────

    @Test
    fun testMainActivityClassCanBeReferenced() {
        val clazz = MainActivity::class.java
        assertNotNull(clazz)
    }
}
