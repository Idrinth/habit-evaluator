package de.idrinth.habitevaluator.android.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.R
import de.idrinth.habitevaluator.shared.model.Medication
import de.idrinth.habitevaluator.shared.model.MedicationLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationLogScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val localUser by viewModel.localUser.collectAsState()
    val medications by viewModel.medications.collectAsState()

    var entries by remember { mutableStateOf<List<MedicationLog>>(emptyList()) }
    var formExpanded by remember { mutableStateOf(false) }
    var dateTime by remember { mutableStateOf(LocalDateTime.now().withSecond(0).withNano(0)) }
    var selectedMedication by remember { mutableStateOf<Medication?>(null) }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var medDropdownExpanded by remember { mutableStateOf(false) }

    val dateTimeFormat = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") }

    fun loadEntries() {
        scope.launch(Dispatchers.IO) {
            val userId = localUser?.id ?: return@launch
            val loaded = viewModel.medicationLogRepository.findByUserId(userId)
                .sortedByDescending { it.takenAt }
            withContext(Dispatchers.Main) { entries = loaded }
        }
    }

    LaunchedEffect(localUser) {
        viewModel.loadMedications()
        loadEntries()
    }

    fun resetForm() {
        dateTime = LocalDateTime.now().withSecond(0).withNano(0)
        selectedMedication = null
        amount = ""
        notes = ""
        formExpanded = false
    }

    fun pickDateTime() {
        val cal = Calendar.getInstance()
        DatePickerDialog(context, { _, y, m, d ->
            TimePickerDialog(context, { _, h, min ->
                dateTime = LocalDateTime.of(y, m + 1, d, h, min)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    fun addEntry() {
        if (medications.isEmpty()) {
            Toast.makeText(context, R.string.no_medications_defined, Toast.LENGTH_SHORT).show()
            return
        }
        val med = selectedMedication
        if (med == null) {
            Toast.makeText(context, R.string.select_medication, Toast.LENGTH_SHORT).show()
            return
        }
        val dose = amount.toDoubleOrNull()
        if (dose == null) {
            Toast.makeText(context, R.string.invalid_amount, Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch(Dispatchers.IO) {
            val log = MedicationLog()
            log.id = UUID.randomUUID().toString()
            log.medication = med
            log.amount = dose
            log.takenAt = dateTime
            log.notes = notes.ifBlank { null }
            log.user = localUser
            viewModel.medicationLogRepository.save(log)
            withContext(Dispatchers.Main) { resetForm() }
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
            Text(stringResource(R.string.medication_log), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(entry.medication?.name ?: "", style = MaterialTheme.typography.bodyLarge)
                            Text("${entry.amount} ${entry.medication?.unit ?: ""}", style = MaterialTheme.typography.bodyMedium)
                            entry.takenAt?.let { Text(it.format(dateTimeFormat), style = MaterialTheme.typography.bodySmall) }
                            entry.notes?.let { if (it.isNotBlank()) Text(it, style = MaterialTheme.typography.bodySmall) }
                            TextButton(onClick = {
                                scope.launch(Dispatchers.IO) {
                                    viewModel.medicationLogRepository.deleteById(entry.id)
                                    loadEntries()
                                }
                            }) { Text(stringResource(R.string.delete)) }
                        }
                    }
                }
            }
        }
    }

    if (formExpanded) {
        AlertDialog(
            onDismissRequest = { resetForm() },
            title = { Text(stringResource(R.string.add_entry)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dateTime.format(dateTimeFormat),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.date_time)) },
                        modifier = Modifier.fillMaxWidth().clickable { pickDateTime() },
                        enabled = false
                    )
                    ExposedDropdownMenuBox(
                        expanded = medDropdownExpanded,
                        onExpandedChange = { medDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedMedication?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.medication)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = medDropdownExpanded) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = medDropdownExpanded,
                            onDismissRequest = { medDropdownExpanded = false }
                        ) {
                            medications.forEach { med ->
                                DropdownMenuItem(
                                    text = { Text(med.name ?: "") },
                                    onClick = {
                                        selectedMedication = med
                                        medDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text(stringResource(R.string.dose_amount)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(stringResource(R.string.notes)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { addEntry() }) {
                    Text(stringResource(R.string.add_entry))
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
