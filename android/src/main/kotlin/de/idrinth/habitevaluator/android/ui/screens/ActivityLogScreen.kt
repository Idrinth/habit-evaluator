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
import de.idrinth.habitevaluator.shared.model.ActivityLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.UUID

@Composable
fun ActivityLogScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val localUser by viewModel.localUser.collectAsState()

    var entries by remember { mutableStateOf<List<ActivityLog>>(emptyList()) }
    var formExpanded by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var startTime by remember { mutableStateOf<LocalTime?>(null) }
    var endTime by remember { mutableStateOf<LocalTime?>(null) }
    var persons by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var activity by remember { mutableStateOf("") }
    var editingEntry by remember { mutableStateOf<ActivityLog?>(null) }

    val dateFormat = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val timeFormat = remember { DateTimeFormatter.ofPattern("HH:mm") }

    fun loadEntries() {
        scope.launch(Dispatchers.IO) {
            val userId = localUser?.id ?: return@launch
            val loaded = viewModel.activityLogRepository.findByUserId(userId)
                .sortedWith(compareByDescending<ActivityLog> { it.date }.thenByDescending { it.createdAt })
            withContext(Dispatchers.Main) { entries = loaded }
        }
    }

    LaunchedEffect(localUser) { loadEntries() }

    fun resetForm() {
        date = LocalDate.now()
        startTime = null
        endTime = null
        persons = ""
        location = ""
        activity = ""
        editingEntry = null
        formExpanded = false
    }

    fun startEdit(entry: ActivityLog) {
        editingEntry = entry
        date = entry.date ?: LocalDate.now()
        startTime = entry.startTime
        endTime = entry.endTime
        persons = entry.persons ?: ""
        location = entry.location ?: ""
        activity = entry.activity ?: ""
        formExpanded = true
    }

    fun saveEntry() {
        if (persons.isBlank()) {
            Toast.makeText(context, R.string.persons_required, Toast.LENGTH_SHORT).show()
            return
        }
        if (location.isBlank()) {
            Toast.makeText(context, R.string.location_required, Toast.LENGTH_SHORT).show()
            return
        }
        if (startTime == null || endTime == null) {
            Toast.makeText(context, R.string.times_required, Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch(Dispatchers.IO) {
            val entry = editingEntry ?: ActivityLog().also {
                it.id = UUID.randomUUID().toString()
                it.user = localUser
            }
            entry.persons = persons.trim()
            entry.location = location.trim()
            entry.startTime = startTime
            entry.endTime = endTime
            entry.date = date
            entry.activity = activity.ifBlank { null }

            viewModel.activityLogRepository.save(entry)
            withContext(Dispatchers.Main) { resetForm() }
            loadEntries()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(stringResource(R.string.activity_log), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))

        // Toggle form
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (formExpanded && editingEntry != null) resetForm()
                    else formExpanded = !formExpanded
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (editingEntry != null) stringResource(R.string.edit_entry) else stringResource(R.string.add_entry),
                    style = MaterialTheme.typography.titleMedium
                )
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
                        value = persons,
                        onValueChange = { persons = it },
                        label = { Text(stringResource(R.string.persons)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text(stringResource(R.string.location)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = activity,
                        onValueChange = { activity = it },
                        label = { Text(stringResource(R.string.activity_description)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(onClick = { saveEntry() }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (editingEntry != null) stringResource(R.string.save_edit) else stringResource(R.string.add_entry))
                    }
                    if (editingEntry != null) {
                        TextButton(onClick = { resetForm() }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.cancel))
                        }
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
                        Text(entry.persons ?: "", style = MaterialTheme.typography.bodyLarge)
                        Text(entry.location ?: "", style = MaterialTheme.typography.bodyMedium)
                        entry.date?.let { Text(it.format(dateFormat), style = MaterialTheme.typography.bodySmall) }
                        val st = entry.startTime?.format(timeFormat) ?: ""
                        val et = entry.endTime?.format(timeFormat) ?: ""
                        if (st.isNotEmpty() || et.isNotEmpty()) Text("$st - $et", style = MaterialTheme.typography.bodySmall)
                        entry.activity?.let { if (it.isNotBlank()) Text(it, style = MaterialTheme.typography.bodySmall) }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { startEdit(entry) }) { Text(stringResource(R.string.edit)) }
                            TextButton(onClick = {
                                scope.launch(Dispatchers.IO) {
                                    if (editingEntry?.id == entry.id) {
                                        withContext(Dispatchers.Main) { resetForm() }
                                    }
                                    viewModel.activityLogRepository.deleteById(entry.id)
                                    loadEntries()
                                }
                            }) { Text(stringResource(R.string.delete)) }
                        }
                    }
                }
            }
        }
    }
}
