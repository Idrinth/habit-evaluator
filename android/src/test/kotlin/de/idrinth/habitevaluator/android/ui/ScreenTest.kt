package de.idrinth.habitevaluator.android.ui

import de.idrinth.habitevaluator.android.ui.navigation.Screen
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScreenTest {

    @Test
    fun testHomeRoute() {
        assertEquals("home", Screen.Home.route)
    }

    @Test
    fun testDiaryRoute() {
        assertEquals("diary", Screen.Diary.route)
    }

    @Test
    fun testSleepRoute() {
        assertEquals("sleep", Screen.Sleep.route)
    }

    @Test
    fun testEmergencyPlanRoute() {
        assertEquals("emergency_plan", Screen.EmergencyPlan.route)
    }

    @Test
    fun testEmotionalStateRoute() {
        assertEquals("emotional_state", Screen.EmotionalState.route)
    }

    @Test
    fun testEditHabitsRoute() {
        assertEquals("edit_habits/{habitId}", Screen.EditHabits.route)
    }

    @Test
    fun testEditHabitsCreateRoute() {
        assertEquals("edit_habits/abc123", Screen.EditHabits.createRoute("abc123"))
    }

    @Test
    fun testEditHabitsCreateRouteWithSpecialChars() {
        assertEquals("edit_habits/id-with-dashes", Screen.EditHabits.createRoute("id-with-dashes"))
    }

    @Test
    fun testAddHabitRoute() {
        assertEquals("add_habit", Screen.AddHabit.route)
    }

    @Test
    fun testStatsRoute() {
        assertEquals("stats", Screen.Stats.route)
    }

    @Test
    fun testPointDevelopmentRoute() {
        assertEquals("point_development/{habitId}", Screen.PointDevelopment.route)
    }

    @Test
    fun testPointDevelopmentCreateRoute() {
        assertEquals("point_development/habit-1", Screen.PointDevelopment.createRoute("habit-1"))
    }

    @Test
    fun testAddEmotionPairRoute() {
        assertEquals("add_emotion_pair", Screen.AddEmotionPair.route)
    }

    @Test
    fun testRecordEmotionEntryRoute() {
        assertEquals("record_emotion/{pairId}", Screen.RecordEmotionEntry.route)
    }

    @Test
    fun testRecordEmotionEntryCreateRoute() {
        assertEquals("record_emotion/pair-1", Screen.RecordEmotionEntry.createRoute("pair-1"))
    }

    @Test
    fun testSettingsRoute() {
        assertEquals("settings", Screen.Settings.route)
    }

    @Test
    fun testImprintRoute() {
        assertEquals("imprint", Screen.Imprint.route)
    }

    @Test
    fun testPositivityDiaryRoute() {
        assertEquals("positivity_diary", Screen.PositivityDiary.route)
    }

    @Test
    fun testSportLogRoute() {
        assertEquals("sport_log", Screen.SportLog.route)
    }

    @Test
    fun testFoodLogRoute() {
        assertEquals("food_log", Screen.FoodLog.route)
    }

    @Test
    fun testMedicationLogRoute() {
        assertEquals("medication_log", Screen.MedicationLog.route)
    }

    @Test
    fun testMedicationListRoute() {
        assertEquals("medication_list", Screen.MedicationList.route)
    }

    @Test
    fun testActivityLogRoute() {
        assertEquals("activity_log", Screen.ActivityLog.route)
    }

    @Test
    fun testPdfExportRoute() {
        assertEquals("pdf_export", Screen.PdfExport.route)
    }

    @Test
    fun testSleepAnalysisRoute() {
        assertEquals("sleep_analysis", Screen.SleepAnalysis.route)
    }

    @Test
    fun testCorrelationsRoute() {
        assertEquals("correlations", Screen.Correlations.route)
    }

    @Test
    fun testEmergencyDialogueRoute() {
        assertEquals("emergency_dialogue", Screen.EmergencyDialogue.route)
    }

    @Test
    fun testPlannerRoute() {
        assertEquals("planner", Screen.Planner.route)
    }

    @Test
    fun testAllRoutesAreUnique() {
        val routes = listOf(
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
        assertEquals(routes.size, routes.toSet().size, "All screen routes must be unique")
    }

    @Test
    fun testAllRoutesAreNonEmpty() {
        val routes = listOf(
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
        routes.forEach { route ->
            assertTrue(route.isNotEmpty(), "Route should not be empty: $route")
        }
    }

    @Test
    fun testParameterizedRoutesContainPlaceholder() {
        assertTrue(Screen.EditHabits.route.contains("{habitId}"))
        assertTrue(Screen.PointDevelopment.route.contains("{habitId}"))
        assertTrue(Screen.RecordEmotionEntry.route.contains("{pairId}"))
    }

    @Test
    fun testCreateRouteReplacesPlaceholder() {
        val editRoute = Screen.EditHabits.createRoute("test-id")
        assertTrue(!editRoute.contains("{"))
        assertTrue(!editRoute.contains("}"))

        val pointRoute = Screen.PointDevelopment.createRoute("test-id")
        assertTrue(!pointRoute.contains("{"))
        assertTrue(!pointRoute.contains("}"))

        val emotionRoute = Screen.RecordEmotionEntry.createRoute("test-id")
        assertTrue(!emotionRoute.contains("{"))
        assertTrue(!emotionRoute.contains("}"))
    }

    @Test
    fun testCreateRoutePreservesPrefix() {
        assertTrue(Screen.EditHabits.createRoute("x").startsWith("edit_habits/"))
        assertTrue(Screen.PointDevelopment.createRoute("x").startsWith("point_development/"))
        assertTrue(Screen.RecordEmotionEntry.createRoute("x").startsWith("record_emotion/"))
    }

    @Test
    fun testCreateRouteWithEmptyId() {
        assertEquals("edit_habits/", Screen.EditHabits.createRoute(""))
        assertEquals("point_development/", Screen.PointDevelopment.createRoute(""))
        assertEquals("record_emotion/", Screen.RecordEmotionEntry.createRoute(""))
    }

    @Test
    fun testCreateRouteWithUuidStyleId() {
        val uuid = "550e8400-e29b-41d4-a716-446655440000"
        assertEquals("edit_habits/$uuid", Screen.EditHabits.createRoute(uuid))
        assertEquals("point_development/$uuid", Screen.PointDevelopment.createRoute(uuid))
        assertEquals("record_emotion/$uuid", Screen.RecordEmotionEntry.createRoute(uuid))
    }
}
