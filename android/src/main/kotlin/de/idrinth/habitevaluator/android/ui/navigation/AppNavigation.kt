package de.idrinth.habitevaluator.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.ui.screens.ActivityLogScreen
import de.idrinth.habitevaluator.android.ui.screens.BackupScreen
import de.idrinth.habitevaluator.android.ui.screens.AddEmotionPairScreen
import de.idrinth.habitevaluator.android.ui.screens.AddHabitScreen
import de.idrinth.habitevaluator.android.ui.screens.CorrelationScreen
import de.idrinth.habitevaluator.android.ui.screens.DiaryNavigationScreen
import de.idrinth.habitevaluator.android.ui.screens.DiaryScreen
import de.idrinth.habitevaluator.android.ui.screens.EditHabitsScreen
import de.idrinth.habitevaluator.android.ui.screens.EmergencyDialogueScreen
import de.idrinth.habitevaluator.android.ui.screens.EmergencyPlanScreen
import de.idrinth.habitevaluator.android.ui.screens.EmotionalStateScreen
import de.idrinth.habitevaluator.android.ui.screens.FoodLogScreen
import de.idrinth.habitevaluator.android.ui.screens.HomeScreen
import de.idrinth.habitevaluator.android.ui.screens.ImprintScreen
import de.idrinth.habitevaluator.android.ui.screens.MedicationListScreen
import de.idrinth.habitevaluator.android.ui.screens.MedicationLogScreen
import de.idrinth.habitevaluator.android.ui.screens.PdfExportScreen
import de.idrinth.habitevaluator.android.ui.screens.PointDevelopmentScreen
import de.idrinth.habitevaluator.android.ui.screens.RecordEmotionEntryScreen
import de.idrinth.habitevaluator.android.ui.screens.SettingsScreen
import de.idrinth.habitevaluator.android.ui.screens.SleepAnalysisScreen
import de.idrinth.habitevaluator.android.ui.screens.SleepTrackingScreen
import de.idrinth.habitevaluator.android.ui.screens.SportLogScreen
import de.idrinth.habitevaluator.android.ui.screens.PlannerActivityScreen
import de.idrinth.habitevaluator.android.ui.screens.PlannerGroupScreen
import de.idrinth.habitevaluator.android.ui.screens.PlannerNavigationScreen
import de.idrinth.habitevaluator.android.ui.screens.StatsScreen
import de.idrinth.habitevaluator.android.ui.screens.WeekPlannerScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = Screen.Home.route, modifier = modifier) {
        composable(Screen.Home.route) {
            HomeScreen(viewModel = viewModel, navController = navController)
        }
        composable(Screen.Diary.route) {
            DiaryNavigationScreen(viewModel = viewModel, navController = navController)
        }
        composable(Screen.Sleep.route) {
            SleepTrackingScreen(viewModel = viewModel)
        }
        composable(Screen.EmergencyPlan.route) {
            EmergencyPlanScreen(viewModel = viewModel, navController = navController)
        }
        composable(Screen.EmotionalState.route) {
            EmotionalStateScreen(viewModel = viewModel, navController = navController)
        }
        composable(
            Screen.EditHabits.route,
            arguments = listOf(navArgument("habitId") { type = NavType.StringType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId") ?: ""
            EditHabitsScreen(viewModel = viewModel, habitId = habitId, navController = navController)
        }
        composable(Screen.AddHabit.route) {
            AddHabitScreen(viewModel = viewModel, navController = navController)
        }
        composable(Screen.Stats.route) {
            StatsScreen(viewModel = viewModel, navController = navController)
        }
        composable(
            Screen.PointDevelopment.route,
            arguments = listOf(navArgument("habitId") { type = NavType.StringType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId") ?: ""
            PointDevelopmentScreen(viewModel = viewModel, habitId = habitId)
        }
        composable(Screen.AddEmotionPair.route) {
            AddEmotionPairScreen(viewModel = viewModel, navController = navController)
        }
        composable(
            Screen.RecordEmotionEntry.route,
            arguments = listOf(navArgument("pairId") { type = NavType.StringType })
        ) { backStackEntry ->
            val pairId = backStackEntry.arguments?.getString("pairId") ?: ""
            RecordEmotionEntryScreen(viewModel = viewModel, pairId = pairId, navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(viewModel = viewModel, navController = navController)
        }
        composable(Screen.Imprint.route) {
            ImprintScreen()
        }
        composable(Screen.PositivityDiary.route) {
            DiaryScreen(viewModel = viewModel)
        }
        composable(Screen.SportLog.route) {
            SportLogScreen(viewModel = viewModel)
        }
        composable(Screen.FoodLog.route) {
            FoodLogScreen(viewModel = viewModel)
        }
        composable(Screen.MedicationLog.route) {
            MedicationLogScreen(viewModel = viewModel)
        }
        composable(Screen.MedicationList.route) {
            MedicationListScreen(viewModel = viewModel)
        }
        composable(Screen.ActivityLog.route) {
            ActivityLogScreen(viewModel = viewModel)
        }
        composable(Screen.PdfExport.route) {
            PdfExportScreen(viewModel = viewModel)
        }
        composable(Screen.SleepAnalysis.route) {
            SleepAnalysisScreen(viewModel = viewModel)
        }
        composable(Screen.Correlations.route) {
            CorrelationScreen(viewModel = viewModel)
        }
        composable(Screen.EmergencyDialogue.route) {
            EmergencyDialogueScreen(viewModel = viewModel, navController = navController)
        }
        composable(Screen.Planner.route) {
            PlannerNavigationScreen(viewModel = viewModel, navController = navController)
        }
        composable(Screen.WeekPlanner.route) {
            WeekPlannerScreen(viewModel = viewModel)
        }
        composable(Screen.PlannerGroups.route) {
            PlannerGroupScreen(viewModel = viewModel)
        }
        composable(Screen.PlannerActivities.route) {
            PlannerActivityScreen(viewModel = viewModel)
        }
        composable(Screen.Backup.route) {
            BackupScreen(viewModel = viewModel)
        }
    }
}
