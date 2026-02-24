package de.idrinth.habitevaluator.android.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import de.idrinth.habitevaluator.shared.model.ActivityGroup
import de.idrinth.habitevaluator.shared.model.ActivityLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivityLogScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val localUser by viewModel.localUser.collectAsState()

    var entries by remember { mutableStateOf<List<ActivityLog>>(emptyList()) }
    var allGroups by remember { mutableStateOf<List<ActivityGroup>>(emptyList()) }
    var formExpanded by remember { mutableStateOf(false) }
    var groupDialogExpanded by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var startTime by remember { mutableStateOf<LocalTime?>(null) }
    var endTime by remember { mutableStateOf<LocalTime?>(null) }
    var persons by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var activity by remember { mutableStateOf("") }
    var selectedGroupIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var editingEntry by remember { mutableStateOf<ActivityLog?>(null) }

    // Group creation form state
    var newGroupName by remember { mutableStateOf("") }
    var newGroupDescription by remember { mutableStateOf("") }

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

    fun loadGroups() {
        scope.launch(Dispatchers.IO) {
            val userId = localUser?.id ?: return@launch
            val loaded = viewModel.activityGroupRepository.findByUserId(userId)
            withContext(Dispatchers.Main) { allGroups = loaded }
        }
    }

    LaunchedEffect(localUser) {
        loadEntries()
        loadGroups()
    }

    fun resetForm() {
        date = LocalDate.now()
        startTime = null
        endTime = null
        persons = ""
        location = ""
        activity = ""
        selectedGroupIds = emptySet()
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
        selectedGroupIds = entry.groups?.map { it.id }?.toSet() ?: emptySet()
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
            entry.groups = allGroups.filter { it.id in selectedGroupIds }.toHashSet()

            viewModel.activityLogRepository.save(entry)
            withContext(Dispatchers.Main) { resetForm() }
            loadEntries()
        }
    }

    fun saveGroup() {
        if (newGroupName.isBlank()) {
            Toast.makeText(context, R.string.activity_group_name_required, Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch(Dispatchers.IO) {
            val group = ActivityGroup()
            group.id = UUID.randomUUID().toString()
            group.name = newGroupName.trim()
            group.description = newGroupDescription.ifBlank { null }
            group.user = localUser
            viewModel.activityGroupRepository.save(group)
            withContext(Dispatchers.Main) {
                newGroupName = ""
                newGroupDescription = ""
            }
            loadGroups()
        }
    }

    fun deleteGroup(group: ActivityGroup) {
        scope.launch(Dispatchers.IO) {
            viewModel.activityGroupRepository.deleteById(group.id)
            withContext(Dispatchers.Main) {
                selectedGroupIds = selectedGroupIds - group.id
            }
            loadGroups()
            loadEntries()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                resetForm()
                formExpanded = true
            }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_entry))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(stringResource(R.string.activity_log), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = { groupDialogExpanded = true }) {
                Text(stringResource(R.string.manage_groups))
            }
            Spacer(Modifier.height(4.dp))

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
                            val entryGroups = entry.groups
                            if (entryGroups != null && entryGroups.isNotEmpty()) {
                                Spacer(Modifier.height(4.dp))
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    entryGroups.sortedBy { it.name }.forEach { group ->
                                        FilterChip(
                                            selected = true,
                                            onClick = {},
                                            label = { Text(group.name ?: "") }
                                        )
                                    }
                                }
                            }
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

    if (formExpanded) {
        AlertDialog(
            onDismissRequest = { resetForm() },
            title = {
                Text(if (editingEntry != null) stringResource(R.string.edit_entry) else stringResource(R.string.add_entry))
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                    if (allGroups.isNotEmpty()) {
                        Text(stringResource(R.string.select_groups), style = MaterialTheme.typography.labelLarge)
                        allGroups.sortedBy { it.name }.forEach { group ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    selectedGroupIds = if (group.id in selectedGroupIds) {
                                        selectedGroupIds - group.id
                                    } else {
                                        selectedGroupIds + group.id
                                    }
                                }
                            ) {
                                Checkbox(
                                    checked = group.id in selectedGroupIds,
                                    onCheckedChange = { checked ->
                                        selectedGroupIds = if (checked) {
                                            selectedGroupIds + group.id
                                        } else {
                                            selectedGroupIds - group.id
                                        }
                                    }
                                )
                                Text(group.name ?: "")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { saveEntry() }) {
                    Text(if (editingEntry != null) stringResource(R.string.save_edit) else stringResource(R.string.add_entry))
                }
            },
            dismissButton = {
                TextButton(onClick = { resetForm() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (groupDialogExpanded) {
        AlertDialog(
            onDismissRequest = { groupDialogExpanded = false },
            title = { Text(stringResource(R.string.activity_groups)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newGroupName,
                        onValueChange = { newGroupName = it },
                        label = { Text(stringResource(R.string.activity_group_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newGroupDescription,
                        onValueChange = { newGroupDescription = it },
                        label = { Text(stringResource(R.string.activity_group_description)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(onClick = { saveGroup() }) {
                        Text(stringResource(R.string.add_activity_group))
                    }
                    Spacer(Modifier.height(8.dp))
                    if (allGroups.isEmpty()) {
                        Text(stringResource(R.string.no_activity_groups), style = MaterialTheme.typography.bodyMedium)
                    } else {
                        allGroups.sortedBy { it.name }.forEach { group ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(group.name ?: "", style = MaterialTheme.typography.bodyLarge)
                                        group.description?.let {
                                            if (it.isNotBlank()) Text(it, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                    TextButton(onClick = { deleteGroup(group) }) {
                                        Text(stringResource(R.string.delete))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { groupDialogExpanded = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
