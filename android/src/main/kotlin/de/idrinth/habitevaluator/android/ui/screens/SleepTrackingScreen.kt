package de.idrinth.habitevaluator.android.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.R
import de.idrinth.habitevaluator.shared.model.SleepEntry
import de.idrinth.habitevaluator.shared.service.SleepEvaluationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun SleepTrackingScreen(viewModel: AppViewModel) {
    val sleepEntries by viewModel.sleepEntries.collectAsState()
    val localUser by viewModel.localUser.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val timeFormat = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val sleepService = remember { SleepEvaluationService() }

    var date by remember { mutableStateOf(LocalDate.now()) }
    var fromTime by remember { mutableStateOf(LocalTime.of(23, 0)) }
    var untilTime by remember { mutableStateOf(LocalTime.of(7, 0)) }
    var notes by remember { mutableStateOf("") }
    var formVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.sleep_tracking), style = MaterialTheme.typography.headlineMedium)
        }

        if (formVisible) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            DatePickerDialog(context, { _, y, m, d ->
                                date = LocalDate.of(y, m + 1, d)
                            }, date.year, date.monthValue - 1, date.dayOfMonth).show()
                        }) { Text(date.format(DateTimeFormatter.ISO_LOCAL_DATE)) }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                TimePickerDialog(context, { _, h, m ->
                                    fromTime = LocalTime.of(h, m)
                                }, fromTime.hour, fromTime.minute, true).show()
                            }, modifier = Modifier.weight(1f)) {
                                Text("From: ${fromTime.format(timeFormat)}")
                            }
                            OutlinedButton(onClick = {
                                TimePickerDialog(context, { _, h, m ->
                                    untilTime = LocalTime.of(h, m)
                                }, untilTime.hour, untilTime.minute, true).show()
                            }, modifier = Modifier.weight(1f)) {
                                Text("Until: ${untilTime.format(timeFormat)}")
                            }
                        }

                        OutlinedTextField(
                            value = notes, onValueChange = { notes = it },
                            label = { Text(stringResource(R.string.notes)) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(onClick = {
                            val user = localUser ?: return@Button
                            val entry = SleepEntry()
                            entry.date = date
                            entry.fromTime = fromTime
                            entry.untilTime = untilTime
                            entry.notes = notes.ifBlank { null }
                            entry.user = user
                            if (sleepService.hasOverlap(sleepEntries, date, fromTime, untilTime)) {
                                Toast.makeText(context, R.string.sleep_overlap, Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            scope.launch(Dispatchers.IO) {
                                viewModel.sleepEntryRepository.save(entry)
                                withContext(Dispatchers.Main) {
                                    notes = ""
                                    viewModel.loadSleepEntries()
                                }
                            }
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.add_entry))
                        }
                    }
                }
            }
        }

        item {
            OutlinedButton(onClick = { formVisible = !formVisible }, modifier = Modifier.fillMaxWidth()) {
                Text(if (formVisible) stringResource(R.string.hide_form) else stringResource(R.string.show_form))
            }
        }

        // Stats
        item {
            val weekStats = sleepService.getCurrentWeekStats(sleepEntries)
            val monthStats = sleepService.getCurrentMonthStats(sleepEntries)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.week), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.sleep_avg, weekStats?.averageHours ?: 0.0))
                    Text(stringResource(R.string.sleep_min, weekStats?.minHours ?: 0.0))
                    Text(stringResource(R.string.sleep_max, weekStats?.maxHours ?: 0.0))
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.month), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.sleep_avg, monthStats?.averageHours ?: 0.0))
                    Text(stringResource(R.string.sleep_min, monthStats?.minHours ?: 0.0))
                    Text(stringResource(R.string.sleep_max, monthStats?.maxHours ?: 0.0))
                }
            }
        }

        items(sleepEntries, key = { it.id }) { entry ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${entry.date?.format(DateTimeFormatter.ISO_LOCAL_DATE)}", style = MaterialTheme.typography.bodyLarge)
                        Text("${entry.fromTime?.format(timeFormat)} - ${entry.untilTime?.format(timeFormat)}", style = MaterialTheme.typography.bodySmall)
                        entry.notes?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    }
                    IconButton(onClick = {
                        scope.launch(Dispatchers.IO) {
                            viewModel.sleepEntryRepository.deleteById(entry.id)
                            withContext(Dispatchers.Main) { viewModel.loadSleepEntries() }
                        }
                    }) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
                }
            }
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}
