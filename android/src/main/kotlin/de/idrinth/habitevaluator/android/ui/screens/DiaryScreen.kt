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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import de.idrinth.habitevaluator.shared.model.DiaryEntry
import de.idrinth.habitevaluator.shared.model.EventSignificance
import de.idrinth.habitevaluator.shared.service.DiaryService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(viewModel: AppViewModel) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val localUser by viewModel.localUser.collectAsState()

    var entries by remember { mutableStateOf<List<DiaryEntry>>(emptyList()) }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var significance by remember { mutableStateOf(EventSignificance.NORMAL) }
    var sigExpanded by remember { mutableStateOf(false) }
    var formVisible by remember { mutableStateOf(true) }

    val diaryService = remember { DiaryService() }

    fun loadEntries() {
        scope.launch(Dispatchers.IO) {
            val userId = localUser?.id ?: return@launch
            val list = viewModel.diaryEntryRepository.findByUserId(userId)
            withContext(Dispatchers.Main) { entries = list }
        }
    }

    LaunchedEffect(localUser) { loadEntries() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.positivity_diary), style = MaterialTheme.typography.headlineMedium)
        }

        if (formVisible) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = description, onValueChange = { description = it },
                            label = { Text(stringResource(R.string.description)) },
                            modifier = Modifier.fillMaxWidth()
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
                        ExposedDropdownMenuBox(expanded = sigExpanded, onExpandedChange = { sigExpanded = it }) {
                            OutlinedTextField(
                                value = significance.name, onValueChange = {}, readOnly = true,
                                label = { Text(stringResource(R.string.significance)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sigExpanded) },
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = sigExpanded, onDismissRequest = { sigExpanded = false }) {
                                EventSignificance.entries.forEach { sig ->
                                    DropdownMenuItem(text = { Text(sig.name) },
                                        onClick = { significance = sig; sigExpanded = false })
                                }
                            }
                        }
                        Button(
                            onClick = {
                                if (description.isBlank()) {
                                    Toast.makeText(context, R.string.description_required, Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                scope.launch(Dispatchers.IO) {
                                    val user = localUser ?: return@launch
                                    val ref = viewModel.diaryReferenceRepository.findOrCreate(
                                        user.id, description.trim()
                                    ) { user }
                                    val entry = DiaryEntry()
                                    entry.diaryReference = ref
                                    entry.significance = significance
                                    entry.eventDate = date
                                    entry.user = user
                                    viewModel.diaryEntryRepository.save(entry)
                                    withContext(Dispatchers.Main) {
                                        description = ""
                                        loadEntries()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.add_entry)) }
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
            val todayPts = diaryService.getDayPoints(entries, LocalDate.now())
            val weekPts = diaryService.getCurrentWeekPoints(entries)
            val monthPts = diaryService.getCurrentMonthPoints(entries)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Today: $todayPts pts | Week: $weekPts pts | Month: $monthPts pts")
                }
            }
        }

        items(entries, key = { it.id }) { entry ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entry.description ?: "", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${entry.eventDate?.format(DateTimeFormatter.ISO_LOCAL_DATE)} - ${entry.significance?.name}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    IconButton(onClick = {
                        scope.launch(Dispatchers.IO) {
                            viewModel.diaryEntryRepository.deleteById(entry.id)
                            withContext(Dispatchers.Main) { loadEntries() }
                        }
                    }) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
                }
            }
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}

