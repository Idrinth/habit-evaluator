package de.idrinth.habitevaluator.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.R
import de.idrinth.habitevaluator.android.ui.navigation.Screen

@Composable
fun PlannerNavigationScreen(viewModel: AppViewModel, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.planner), style = MaterialTheme.typography.headlineMedium)

        NavigationCard(stringResource(R.string.planner_week_planner_title)) {
            navController.navigate(Screen.WeekPlanner.route)
        }
        NavigationCard(stringResource(R.string.planner_activities_title)) {
            navController.navigate(Screen.PlannerActivities.route)
        }
        NavigationCard(stringResource(R.string.planner_groups_title)) {
            navController.navigate(Screen.PlannerGroups.route)
        }
    }
}
