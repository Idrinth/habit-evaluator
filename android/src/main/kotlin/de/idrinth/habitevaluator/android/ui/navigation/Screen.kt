package de.idrinth.habitevaluator.android.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Diary : Screen("diary")
    data object Sleep : Screen("sleep")
    data object EmergencyPlan : Screen("emergency_plan")
    data object EmotionalState : Screen("emotional_state")
    data object EditHabits : Screen("edit_habits/{habitId}") {
        fun createRoute(habitId: String) = "edit_habits/$habitId"
    }
    data object AddHabit : Screen("add_habit")
    data object Stats : Screen("stats")
    data object PointDevelopment : Screen("point_development/{habitId}") {
        fun createRoute(habitId: String) = "point_development/$habitId"
    }
    data object AddEmotionPair : Screen("add_emotion_pair")
    data object RecordEmotionEntry : Screen("record_emotion/{pairId}") {
        fun createRoute(pairId: String) = "record_emotion/$pairId"
    }
    data object Settings : Screen("settings")
    data object Imprint : Screen("imprint")
    data object PositivityDiary : Screen("positivity_diary")
    data object SportLog : Screen("sport_log")
    data object FoodLog : Screen("food_log")
    data object MedicationLog : Screen("medication_log")
    data object MedicationList : Screen("medication_list")
    data object ActivityLog : Screen("activity_log")
    data object PdfExport : Screen("pdf_export")
    data object SleepAnalysis : Screen("sleep_analysis")
    data object Correlations : Screen("correlations")
    data object EmergencyDialogue : Screen("emergency_dialogue")
    data object Planner : Screen("planner")
    data object WeekPlanner : Screen("week_planner")
    data object WeekOverview : Screen("week_overview")
    data object PlannerGroups : Screen("planner_groups")
    data object PlannerActivities : Screen("planner_activities")
    data object Backup : Screen("backup")
    data object Gratitude : Screen("gratitude")
}
