package de.idrinth.habitevaluator.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
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
fun DiaryNavigationScreen(viewModel: AppViewModel, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.diary), style = MaterialTheme.typography.headlineMedium)

        NavigationCard(stringResource(R.string.positivity_diary)) {
            navController.navigate(Screen.PositivityDiary.route)
        }
        NavigationCard(stringResource(R.string.sleep_tracking)) {
            navController.navigate(Screen.Sleep.route)
        }
        NavigationCard(stringResource(R.string.sport_log)) {
            navController.navigate(Screen.SportLog.route)
        }
        NavigationCard(stringResource(R.string.food_log)) {
            navController.navigate(Screen.FoodLog.route)
        }
        NavigationCard(stringResource(R.string.medication_log)) {
            navController.navigate(Screen.MedicationLog.route)
        }
        NavigationCard(stringResource(R.string.medication_list)) {
            navController.navigate(Screen.MedicationList.route)
        }
        NavigationCard(stringResource(R.string.activity_log)) {
            navController.navigate(Screen.ActivityLog.route)
        }
    }
}

@Composable
private fun NavigationCard(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(20.dp)
        )
    }
}
