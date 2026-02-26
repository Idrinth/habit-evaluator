package de.idrinth.habitevaluator.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.R
import de.idrinth.habitevaluator.android.ui.navigation.Screen
import de.idrinth.habitevaluator.shared.model.WeekPlannerSlot
import de.idrinth.habitevaluator.shared.service.DayPlannerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun PlannerNavigationScreen(viewModel: AppViewModel, navController: NavController) {
    val scope = rememberCoroutineScope()
    val localUser by viewModel.localUser.collectAsState()
    val dayPlannerService = remember { DayPlannerService() }

    var allSlots by remember { mutableStateOf<List<WeekPlannerSlot>>(emptyList()) }
    var showDailyBreakdown by remember { mutableStateOf(false) }

    LaunchedEffect(localUser) {
        scope.launch(Dispatchers.IO) {
            val userId = localUser?.id ?: return@launch
            val loadedSlots = viewModel.weekPlannerSlotRepository.findByUserId(userId)
            withContext(Dispatchers.Main) {
                allSlots = loadedSlots
            }
        }
    }

    val summary = remember(allSlots) {
        dayPlannerService.getWeekSlotSummary(allSlots)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.planner), style = MaterialTheme.typography.headlineMedium)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDailyBreakdown = !showDailyBreakdown }
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    stringResource(R.string.planner_week_summary, summary[0], summary[1]),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (showDailyBreakdown) {
                    Spacer(Modifier.height(4.dp))
                    for (day in 1..7) {
                        val filled = allSlots
                            .filter { it.dayOfWeek == day && it.groups != null && it.groups.isNotEmpty() }
                            .sumOf { maxOf(1, it.duration) }
                        val dayLabel = DayOfWeek.of(day).getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        Text("$dayLabel: $filled/24", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        NavigationCard(stringResource(R.string.planner_week_planner_title)) {
            navController.navigate(Screen.WeekPlanner.route)
        }
        NavigationCard(stringResource(R.string.planner_week_overview_title)) {
            navController.navigate(Screen.WeekOverview.route)
        }
        NavigationCard(stringResource(R.string.planner_activities_title)) {
            navController.navigate(Screen.PlannerActivities.route)
        }
        NavigationCard(stringResource(R.string.planner_groups_title)) {
            navController.navigate(Screen.PlannerGroups.route)
        }
    }
}
