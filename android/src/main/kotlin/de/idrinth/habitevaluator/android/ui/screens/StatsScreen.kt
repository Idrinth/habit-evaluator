package de.idrinth.habitevaluator.android.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.R
import de.idrinth.habitevaluator.android.ui.components.EmotionLineChart
import de.idrinth.habitevaluator.android.ui.components.EmotionScatterChart
import de.idrinth.habitevaluator.android.ui.components.PointChart
import de.idrinth.habitevaluator.android.ui.components.ScatterEntry
import de.idrinth.habitevaluator.android.ui.components.ScatterPair
import de.idrinth.habitevaluator.android.ui.navigation.Screen
import de.idrinth.habitevaluator.shared.model.DiaryEntry
import de.idrinth.habitevaluator.shared.model.EmotionEntry
import de.idrinth.habitevaluator.shared.service.DiaryService
import de.idrinth.habitevaluator.shared.service.HabitScoringService
import de.idrinth.habitevaluator.shared.service.SleepEvaluationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.max

@Composable
fun StatsScreen(viewModel: AppViewModel, navController: NavController) {
    val habits by viewModel.habits.collectAsState()
    val sleepEntries by viewModel.sleepEntries.collectAsState()
    val localUser by viewModel.localUser.collectAsState()
    val emotionPairs by viewModel.emotionPairs.collectAsState()

    var diaryEntries by remember { mutableStateOf<List<DiaryEntry>>(emptyList()) }
    var emotionEntries by remember { mutableStateOf<List<EmotionEntry>>(emptyList()) }
    val diaryService = remember { DiaryService() }
    val scoringService = remember { HabitScoringService() }
    val sleepService = remember { SleepEvaluationService() }

    val today = LocalDate.now()
    val startDate = today.minusDays(29)
    val labelFormat = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

    LaunchedEffect(localUser) {
        val userId = localUser?.id ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            diaryEntries = viewModel.diaryEntryRepository.findByUserId(userId)
            emotionEntries = viewModel.emotionEntryRepository.findByUserId(userId)
        }
    }

    // Build chart data
    val sleepData = remember(sleepEntries) {
        (0L..29L).map { offset ->
            val d = startDate.plusDays(offset)
            val dayEntries = sleepEntries.filter { it.date == d }
            val hours = dayEntries.sumOf { e ->
                val from = e.fromTime ?: return@sumOf 0.0
                val until = e.untilTime ?: return@sumOf 0.0
                var mins = ChronoUnit.MINUTES.between(from, until)
                if (mins < 0) mins += 24 * 60
                mins / 60.0
            }
            d.format(labelFormat) to hours.toFloat()
        }
    }

    val diaryData = remember(diaryEntries) {
        (0L..29L).map { offset ->
            val d = startDate.plusDays(offset)
            val pts = diaryService.getDayPoints(diaryEntries, d)
            d.format(labelFormat) to pts.toFloat()
        }
    }

    val habitData = remember(habits) {
        (0L..29L).map { offset ->
            val d = startDate.plusDays(offset)
            val pts = habits.sumOf { scoringService.calculateHabitScore(it, d, d).score }
            d.format(labelFormat) to pts.toFloat()
        }
    }

    // Emotion scatter chart data (last 30 days)
    val scatterPairs = remember(emotionEntries, emotionPairs) {
        if (emotionEntries.isEmpty() || emotionPairs.isEmpty()) return@remember emptyList()
        val cutoff = today.minusDays(30).atStartOfDay()
        val recentEntries = emotionEntries.filter { it.recordedAt != null && it.recordedAt.isAfter(cutoff) }
        emotionPairs.mapNotNull { pair ->
            val pairEntries = recentEntries.filter { it.emotionPair?.id == pair.id }
            if (pairEntries.isEmpty()) return@mapNotNull null
            ScatterPair(
                pairLabel = "${pair.negativeLabel} \u2194 ${pair.positiveLabel}",
                entries = pairEntries.map { entry ->
                    val hour = entry.recordedAt.hour + entry.recordedAt.minute / 60f
                    ScatterEntry(hourOfDay = hour, strength = entry.strength.toFloat())
                }
            )
        }
    }

    // Emotion line chart data (daily averages)
    val lineChartLabels = remember(emotionEntries) {
        if (emotionEntries.isEmpty()) return@remember emptyList()
        val dates = emotionEntries.mapNotNull { it.recordedAt?.toLocalDate() }
        if (dates.isEmpty()) return@remember emptyList()
        val minDate = dates.min()
        val maxDate = today
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        generateSequence(minDate) { it.plusDays(1) }
            .takeWhile { !it.isAfter(maxDate) }
            .map { it.format(formatter) }
            .toList()
    }

    val lineChartData = remember(emotionEntries, emotionPairs, lineChartLabels) {
        if (emotionEntries.isEmpty() || emotionPairs.isEmpty() || lineChartLabels.isEmpty()) {
            return@remember Pair(emptyList<String>(), emptyList<List<Float?>>())
        }
        val dates = emotionEntries.mapNotNull { it.recordedAt?.toLocalDate() }
        if (dates.isEmpty()) return@remember Pair(emptyList<String>(), emptyList<List<Float?>>())
        val minDate = dates.min()
        val maxDate = today
        val allDates = generateSequence(minDate) { it.plusDays(1) }
            .takeWhile { !it.isAfter(maxDate) }
            .toList()

        val pairNames = mutableListOf<String>()
        val pairValues = mutableListOf<List<Float?>>()

        for (pair in emotionPairs) {
            val pairEntries = emotionEntries.filter { it.emotionPair?.id == pair.id }
            if (pairEntries.isEmpty()) continue
            val entriesByDate = pairEntries.groupBy { it.recordedAt?.toLocalDate() }
            pairNames.add("${pair.negativeLabel} \u2194 ${pair.positiveLabel}")
            pairValues.add(allDates.map { date ->
                val dayEntries = entriesByDate[date]
                if (dayEntries.isNullOrEmpty()) null
                else dayEntries.map { it.strength.toFloat() }.average().toFloat()
            })
        }

        Pair(pairNames, pairValues)
    }

    val (lineChartPairNames, lineChartPairValues) = lineChartData

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.statistics), style = MaterialTheme.typography.headlineMedium)

        if (sleepData.any { it.second > 0 }) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.sleep_duration), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    PointChart(data = sleepData, modifier = Modifier.fillMaxWidth().height(200.dp))
                }
            }
        }

        if (diaryData.any { it.second > 0 }) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.diary_points), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    PointChart(data = diaryData, modifier = Modifier.fillMaxWidth().height(200.dp))
                }
            }
        }

        if (habitData.any { it.second > 0 }) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.habit_points), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    PointChart(data = habitData, modifier = Modifier.fillMaxWidth().height(200.dp))
                }
            }
        }

        // Emotion scatter chart (last 30 days, time-of-day distribution)
        if (scatterPairs.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.emotion_scatter_title), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    val legendRows = kotlin.math.ceil(scatterPairs.size.toFloat() / 2).toInt()
                    val legendHeight = legendRows * 24 + 16
                    EmotionScatterChart(
                        pairs = scatterPairs,
                        modifier = Modifier.fillMaxWidth().height((300 + legendHeight).dp)
                    )
                }
            }
        }

        // Emotion line chart (average emotional development)
        if (lineChartPairNames.isNotEmpty() && lineChartLabels.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.emotion_development_title), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    val legendRows = kotlin.math.ceil(lineChartPairNames.size.toFloat() / 2).toInt()
                    val legendHeight = legendRows * 24 + 16
                    val chartWidth = max(lineChartLabels.size * 12, 300)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        EmotionLineChart(
                            labels = lineChartLabels,
                            pairNames = lineChartPairNames,
                            pairDailyValues = lineChartPairValues,
                            modifier = Modifier
                                .width(chartWidth.dp)
                                .height((300 + legendHeight).dp)
                        )
                    }
                }
            }
        }

        Button(onClick = { navController.navigate(Screen.PdfExport.route) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.export_pdf))
        }
        Button(onClick = { navController.navigate(Screen.Correlations.route) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.view_correlations))
        }
        Button(onClick = { navController.navigate(Screen.Backup.route) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.backup_title))
        }

        Spacer(Modifier.height(32.dp))
    }
}
