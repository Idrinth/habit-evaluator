package de.idrinth.habitevaluator.android.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import de.idrinth.habitevaluator.shared.model.GratitudeEntry
import de.idrinth.habitevaluator.shared.service.GratitudeService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun GratitudeScreen(viewModel: AppViewModel) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val localUser by viewModel.localUser.collectAsState()

    var entries by remember { mutableStateOf<List<GratitudeEntry>>(emptyList()) }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var formVisible by remember { mutableStateOf(false) }

    val gratitudeService = remember { GratitudeService() }

    val prompts = listOf(
        stringResource(R.string.gratitude_prompt_grateful),
        stringResource(R.string.gratitude_prompt_thankful)
    )
    var currentPrompt by remember { mutableStateOf(prompts.random()) }

    fun loadEntries() {
        scope.launch(Dispatchers.IO) {
            val userId = localUser?.id ?: return@launch
            val list = viewModel.gratitudeEntryRepository.findByUserId(userId)
            withContext(Dispatchers.Main) {
                entries = list
            }
        }
    }

    LaunchedEffect(localUser) { loadEntries() }

    fun resetForm() {
        description = ""
        date = LocalDate.now()
        formVisible = false
        currentPrompt = prompts.random()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                resetForm()
                formVisible = true
            }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_gratitude_entry))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.gratitude_diary), style = MaterialTheme.typography.headlineMedium)
            }

            // Stats card
            item {
                val todayCount = gratitudeService.getDayCount(entries, LocalDate.now())
                val weekCount = gratitudeService.getCurrentWeekCount(entries)
                val monthCount = gratitudeService.getCurrentMonthCount(entries)
                val streak = gratitudeService.getCurrentStreak(entries)
                val dailyAvg = gratitudeService.getDailyAverageForMonth(entries)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "${stringResource(R.string.gratitude_today)}: $todayCount | " +
                                "${stringResource(R.string.gratitude_week)}: $weekCount | " +
                                "${stringResource(R.string.gratitude_month)}: $monthCount",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "${stringResource(R.string.gratitude_streak)}: $streak | " +
                                "${stringResource(R.string.gratitude_daily_avg)}: ${"%.1f".format(dailyAvg)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            if (entries.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.gratitude_no_entries),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }

            items(entries, key = { it.id }) { entry ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.description ?: "", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                entry.eventDate?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: "",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(onClick = {
                            scope.launch(Dispatchers.IO) {
                                viewModel.gratitudeEntryRepository.deleteById(entry.id)
                                withContext(Dispatchers.Main) { loadEntries() }
                            }
                        }) { Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete)) }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (formVisible) {
        AlertDialog(
            onDismissRequest = { resetForm() },
            title = { Text(stringResource(R.string.add_gratitude_entry)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        currentPrompt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(stringResource(R.string.gratitude_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    OutlinedButton(onClick = {
                        val dp = DatePickerDialog(context, { _, y, m, d ->
                            date = LocalDate.of(y, m + 1, d)
                        }, date.year, date.monthValue - 1, date.dayOfMonth)
                        dp.datePicker.maxDate = System.currentTimeMillis()
                        dp.show()
                    }) {
                        Text(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (description.isBlank()) {
                        Toast.makeText(context, R.string.description_required, Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }
                    scope.launch(Dispatchers.IO) {
                        val user = localUser ?: return@launch
                        val entry = GratitudeEntry(description.trim(), date)
                        entry.user = user
                        viewModel.gratitudeEntryRepository.save(entry)
                        withContext(Dispatchers.Main) {
                            resetForm()
                            loadEntries()
                        }
                    }
                }) {
                    Text(stringResource(R.string.add_gratitude_entry))
                }
            },
            dismissButton = {
                TextButton(onClick = { resetForm() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
