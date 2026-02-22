package de.idrinth.habitevaluator.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.R
import de.idrinth.habitevaluator.android.ui.components.PointChart
import de.idrinth.habitevaluator.shared.service.HabitScoringService
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Composable
fun PointDevelopmentScreen(viewModel: AppViewModel, habitId: String) {
    val habits by viewModel.habits.collectAsState()
    val habit = habits.find { it.id == habitId }
    val scorer = remember { HabitScoringService() }
    var showMonth by remember { mutableStateOf(false) }
    val today = LocalDate.now()

    if (habit == null) {
        Text(stringResource(R.string.habit_not_found), modifier = Modifier.padding(16.dp))
        return
    }

    val data = remember(habit, habit.entries?.size, showMonth) {
        if (showMonth) {
            val start = today.withDayOfMonth(1)
            val end = today
            (0..end.dayOfMonth - 1).map { offset ->
                val d = start.plusDays(offset.toLong())
                val label = if (d.dayOfMonth % 5 == 1 || d == end) d.dayOfMonth.toString() else ""
                label to scorer.calculateHabitScore(habit, d, d).score.toFloat()
            }
        } else {
            val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val dayFormat = DateTimeFormatter.ofPattern("EEE")
            (0..6).map { offset ->
                val d = weekStart.plusDays(offset.toLong())
                d.format(dayFormat) to scorer.calculateHabitScore(habit, d, d).score.toFloat()
            }
        }
    }

    val totalPts = data.sumOf { it.second.toDouble() }
    val avgPts = if (data.isNotEmpty()) totalPts / data.size else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(habit.name ?: "", style = MaterialTheme.typography.headlineMedium)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { showMonth = false },
                enabled = showMonth,
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.week)) }
            Button(
                onClick = { showMonth = true },
                enabled = !showMonth,
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.month)) }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.daily_points), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                PointChart(data = data, modifier = Modifier.fillMaxWidth().height(200.dp))
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total: ${totalPts.toInt()} pts", style = MaterialTheme.typography.bodyLarge)
                Text("Average: ${String.format("%.1f", avgPts)} pts/day", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
