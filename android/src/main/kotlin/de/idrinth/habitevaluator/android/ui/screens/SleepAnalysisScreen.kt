package de.idrinth.habitevaluator.android.ui.screens

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.R
import de.idrinth.habitevaluator.android.ui.components.SleepDistributionChart
import de.idrinth.habitevaluator.android.ui.components.SleepGraph
import de.idrinth.habitevaluator.shared.service.SleepEvaluationService
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.TreeMap

@Composable
fun SleepAnalysisScreen(viewModel: AppViewModel) {
    val sleepEntries by viewModel.sleepEntries.collectAsState()
    val sleepService = remember { SleepEvaluationService() }
    val today = LocalDate.now()
    val startDate = today.minusDays(29)
    val labelFormat = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

    // Build duration and interruptions data
    val (durationData, interruptionData) = remember(sleepEntries) {
        val buckets = TreeMap<LocalDate, MutableList<de.idrinth.habitevaluator.shared.model.SleepEntry>>()
        (0L..29L).forEach { offset -> buckets[startDate.plusDays(offset)] = mutableListOf() }
        sleepEntries.forEach { entry ->
            entry.date?.let { d -> buckets[d]?.add(entry) }
        }

        val durations = buckets.map { (date, dayEntries) ->
            val hours = dayEntries.sumOf { e ->
                val from = e.fromTime ?: return@sumOf 0.0
                val until = e.untilTime ?: return@sumOf 0.0
                var mins = ChronoUnit.MINUTES.between(from, until)
                if (mins < 0) mins += 24 * 60
                mins / 60.0
            }
            date.format(labelFormat) to hours.toFloat()
        }

        val interruptions = buckets.map { (date, dayEntries) ->
            val count = if (dayEntries.isEmpty()) 0 else maxOf(0, dayEntries.size - 1)
            date.format(labelFormat) to count.toFloat()
        }

        durations to interruptions
    }

    val distribution = remember(sleepEntries) {
        sleepService.calculateSleepDistribution(sleepEntries)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.sleep_analysis), style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.sleep_duration), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                SleepGraph(
                    data = durationData,
                    formatLabel = "%.1f",
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.interruptions), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                SleepGraph(
                    data = interruptionData,
                    formatLabel = "%.0f",
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            }
        }

        if (distribution != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.sleep_distribution), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    SleepDistributionChart(
                        percentAsleep = distribution.percentAsleep,
                        modifier = Modifier.fillMaxWidth().height(250.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}
