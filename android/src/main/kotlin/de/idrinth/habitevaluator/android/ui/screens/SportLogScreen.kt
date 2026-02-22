package de.idrinth.habitevaluator.android.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.R
import de.idrinth.habitevaluator.shared.model.SportLog
import de.idrinth.habitevaluator.shared.service.SportLogService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.UUID

@Composable
fun SportLogScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val localUser by viewModel.localUser.collectAsState()
    val sportLogService = remember { SportLogService() }

    var entries by remember { mutableStateOf<List<SportLog>>(emptyList()) }
    var formExpanded by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var startTime by remember { mutableStateOf<LocalTime?>(null) }
    var endTime by remember { mutableStateOf<LocalTime?>(null) }
    var activityName by remember { mutableStateOf("") }
    var measurement by remember { mutableStateOf("") }
    var measurementUnit by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val dateFormat = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val timeFormat = remember { DateTimeFormatter.ofPattern("HH:mm") }

    fun loadEntries() {
        scope.launch(Dispatchers.IO) {
            val userId = localUser?.id ?: return@launch
            val loaded = viewModel.sportLogRepository.findByUserId(userId)
                .sortedWith(compareByDescending<SportLog> { it.date }.thenByDescending { it.createdAt })
            withContext(Dispatchers.Main) { entries = loaded }
        }
    }

    LaunchedEffect(localUser) { loadEntries() }

    fun resetForm() {
        date = LocalDate.now()
        startTime = null
        endTime = null
        activityName = ""
        measurement = ""
        measurementUnit = ""
        notes = ""
        formExpanded = false
    }

    fun addEntry() {
        if (activityName.isBlank()) {
            Toast.makeText(context, R.string.activity_name_required, Toast.LENGTH_SHORT).show()
            return
        }
        if (startTime == null || endTime == null) {
            Toast.makeText(context, R.string.times_required, Toast.LENGTH_SHORT).show()
            return
        }
        val measureVal = measurement.toDoubleOrNull()
        if (measurement.isNotBlank() && measureVal == null) {
            Toast.makeText(context, R.string.invalid_measurement, Toast.LENGTH_SHORT).show()
            return
        }
        if (measureVal != null && measurementUnit.isBlank()) {
            Toast.makeText(context, R.string.unit_required, Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch(Dispatchers.IO) {
            val log = SportLog()
            log.id = UUID.randomUUID().toString()
            log.name = activityName.trim()
            log.date = date
            log.startTime = startTime
            log.endTime = endTime
            log.measurement = measureVal ?: 0.0
            log.measurementUnit = measurementUnit.ifBlank { null }
            log.notes = notes.ifBlank { null }
            log.user = localUser
            viewModel.sportLogRepository.save(log)

            withContext(Dispatchers.Main) { resetForm() }
            loadEntries()
        }
    }

    val weekStats = remember(entries) { sportLogService.getCurrentWeekStats(entries) }
    val monthStats = remember(entries) { sportLogService.getCurrentMonthStats(entries) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(stringResource(R.string.sport_log), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))

        // Stats
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(stringResource(R.string.weekly_stats), style = MaterialTheme.typography.titleSmall)
                Text("${stringResource(R.string.entries)}: ${weekStats?.totalEntries ?: 0}")
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.monthly_stats), style = MaterialTheme.typography.titleSmall)
                Text("${stringResource(R.string.entries)}: ${monthStats?.totalEntries ?: 0}")
            }
        }

        Spacer(Modifier.height(8.dp))

        // Toggle form
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { formExpanded = !formExpanded }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.add_entry), style = MaterialTheme.typography.titleMedium)
                Icon(
                    painter = painterResource(
                        if (formExpanded) android.R.drawable.arrow_up_float
                        else android.R.drawable.arrow_down_float
                    ),
                    contentDescription = null
                )
            }
        }

        AnimatedVisibility(visible = formExpanded) {
            Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = date.format(dateFormat),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.date)) },
                        modifier = Modifier.fillMaxWidth().clickable {
                            val cal = Calendar.getInstance()
                            DatePickerDialog(context, { _, y, m, d ->
                                date = LocalDate.of(y, m + 1, d)
                            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).apply {
                                datePicker.maxDate = System.currentTimeMillis()
                            }.show()
                        },
                        enabled = false
                    )
                    OutlinedTextField(
                        value = startTime?.format(timeFormat) ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.start_time)) },
                        modifier = Modifier.fillMaxWidth().clickable {
                            val cal = Calendar.getInstance()
                            TimePickerDialog(context, { _, h, m ->
                                startTime = LocalTime.of(h, m)
                            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                        },
                        enabled = false
                    )
                    OutlinedTextField(
                        value = endTime?.format(timeFormat) ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.end_time)) },
                        modifier = Modifier.fillMaxWidth().clickable {
                            val cal = Calendar.getInstance()
                            TimePickerDialog(context, { _, h, m ->
                                endTime = LocalTime.of(h, m)
                            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                        },
                        enabled = false
                    )
                    OutlinedTextField(
                        value = activityName,
                        onValueChange = { activityName = it },
                        label = { Text(stringResource(R.string.activity_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = measurement,
                        onValueChange = { measurement = it },
                        label = { Text(stringResource(R.string.measurement)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = measurementUnit,
                        onValueChange = { measurementUnit = it },
                        label = { Text(stringResource(R.string.measurement_unit)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(stringResource(R.string.notes)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(onClick = { addEntry() }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.add_entry))
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(entries, key = { it.id }) { entry ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(entry.name ?: "", style = MaterialTheme.typography.bodyLarge)
                        entry.date?.let { Text(it.format(dateFormat), style = MaterialTheme.typography.bodySmall) }
                        val st = entry.startTime?.format(timeFormat) ?: ""
                        val et = entry.endTime?.format(timeFormat) ?: ""
                        if (st.isNotEmpty() || et.isNotEmpty()) Text("$st - $et", style = MaterialTheme.typography.bodySmall)
                        if (entry.measurement > 0) {
                            Text("${entry.measurement} ${entry.measurementUnit ?: ""}", style = MaterialTheme.typography.bodySmall)
                        }
                        entry.notes?.let { if (it.isNotBlank()) Text(it, style = MaterialTheme.typography.bodySmall) }
                        TextButton(onClick = {
                            scope.launch(Dispatchers.IO) {
                                viewModel.sportLogRepository.deleteById(entry.id)
                                loadEntries()
                            }
                        }) { Text(stringResource(R.string.delete)) }
                    }
                }
            }
        }
    }
}
