package de.idrinth.habitevaluator.android.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.R
import de.idrinth.habitevaluator.shared.backup.RestoreOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun BackupScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val storageInitialized by viewModel.storageInitialized.collectAsState()

    var downloadPassword by remember { mutableStateOf("") }
    var downloadStatus by remember { mutableStateOf("") }
    var isDownloading by remember { mutableStateOf(false) }

    var restorePassword by remember { mutableStateOf("") }
    var restoreStatus by remember { mutableStateOf("") }
    var isRestoring by remember { mutableStateOf(false) }
    var selectedFileBytes by remember { mutableStateOf<ByteArray?>(null) }
    var selectedFileName by remember { mutableStateOf("") }

    // Restore options
    var restoreCategories by remember { mutableStateOf(true) }
    var restoreHabits by remember { mutableStateOf(true) }
    var restoreDiary by remember { mutableStateOf(true) }
    var restoreSleep by remember { mutableStateOf(true) }
    var restoreSport by remember { mutableStateOf(true) }
    var restoreFood by remember { mutableStateOf(true) }
    var restoreEmotions by remember { mutableStateOf(true) }
    var restoreMeetings by remember { mutableStateOf(true) }
    var restoreActivity by remember { mutableStateOf(true) }
    var restoreMedication by remember { mutableStateOf(true) }
    var restoreReminders by remember { mutableStateOf(true) }
    var restoreVisibility by remember { mutableStateOf(true) }
    var restoreEmergency by remember { mutableStateOf(true) }
    var restorePlanner by remember { mutableStateOf(true) }

    val createDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        isDownloading = true
        downloadStatus = context.getString(R.string.download_backup_in_progress)
        scope.launch(Dispatchers.IO) {
            try {
                val bytes = viewModel.createDownloadableHezBackup(downloadPassword)
                context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                withContext(Dispatchers.Main) {
                    downloadStatus = context.getString(R.string.download_backup_success)
                    isDownloading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    downloadStatus = context.getString(R.string.download_backup_failed, e.message ?: "Unknown error")
                    isDownloading = false
                }
            }
        }
    }

    val openDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null && bytes.size >= 4) {
                selectedFileBytes = bytes
                val segments = uri.lastPathSegment ?: ""
                selectedFileName = segments.substringAfterLast('/')
            } else {
                Toast.makeText(context, R.string.restore_from_file_invalid, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, R.string.restore_from_file_invalid, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.backup_title), style = MaterialTheme.typography.headlineMedium)

        // Download backup section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.download_backup_title), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = downloadPassword,
                    onValueChange = { downloadPassword = it },
                    label = { Text(stringResource(R.string.backup_password_hint)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (!storageInitialized) {
                            Toast.makeText(context, R.string.restore_failed_not_initialized, Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (downloadPassword.isEmpty()) {
                            Toast.makeText(context, R.string.backup_password_empty, Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        createDocument.launch(viewModel.generateHezFilename())
                    },
                    enabled = !isDownloading,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.download_backup_button)) }

                if (downloadStatus.isNotEmpty()) {
                    Text(
                        downloadStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (downloadStatus.contains("failed", ignoreCase = true))
                            MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Restore from file section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.restore_from_file_title), style = MaterialTheme.typography.titleMedium)

                OutlinedButton(
                    onClick = { openDocument.launch(arrayOf("*/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (selectedFileName.isEmpty()) stringResource(R.string.restore_from_file_select)
                        else selectedFileName
                    )
                }

                OutlinedTextField(
                    value = restorePassword,
                    onValueChange = { restorePassword = it },
                    label = { Text(stringResource(R.string.restore_enter_password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(stringResource(R.string.restore_select_types), style = MaterialTheme.typography.bodyMedium)

                RestoreCheckbox(stringResource(R.string.restore_type_categories), restoreCategories) { restoreCategories = it }
                RestoreCheckbox(stringResource(R.string.restore_type_habits), restoreHabits) { restoreHabits = it }
                RestoreCheckbox(stringResource(R.string.restore_type_diary), restoreDiary) { restoreDiary = it }
                RestoreCheckbox(stringResource(R.string.restore_type_sleep), restoreSleep) { restoreSleep = it }
                RestoreCheckbox(stringResource(R.string.restore_type_sport), restoreSport) { restoreSport = it }
                RestoreCheckbox(stringResource(R.string.restore_type_food), restoreFood) { restoreFood = it }
                RestoreCheckbox(stringResource(R.string.restore_type_emotions), restoreEmotions) { restoreEmotions = it }
                RestoreCheckbox(stringResource(R.string.restore_type_activity), restoreActivity) { restoreActivity = it }
                RestoreCheckbox(stringResource(R.string.restore_type_medication), restoreMedication) { restoreMedication = it }
                RestoreCheckbox(stringResource(R.string.restore_type_emergency), restoreEmergency) { restoreEmergency = it }
                RestoreCheckbox(stringResource(R.string.restore_type_planner), restorePlanner) { restorePlanner = it }

                Button(
                    onClick = {
                        if (!storageInitialized) {
                            Toast.makeText(context, R.string.restore_failed_not_initialized, Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val fileBytes = selectedFileBytes
                        if (fileBytes == null) {
                            Toast.makeText(context, R.string.restore_from_file_select, Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (restorePassword.isEmpty()) {
                            Toast.makeText(context, R.string.backup_password_empty, Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isRestoring = true
                        restoreStatus = context.getString(R.string.restore_in_progress)
                        scope.launch(Dispatchers.IO) {
                            try {
                                val options = RestoreOptions()
                                options.isRestoreCategories = restoreCategories
                                options.isRestoreHabits = restoreHabits
                                options.isRestoreDiaryEntries = restoreDiary
                                options.isRestoreSleepEntries = restoreSleep
                                options.isRestoreSportLogs = restoreSport
                                options.isRestoreFoodLogs = restoreFood
                                options.isRestoreEmotionData = restoreEmotions
                                options.isRestoreMeetingEntries = restoreMeetings
                                options.isRestoreActivityLogs = restoreActivity
                                options.isRestoreMedicationData = restoreMedication
                                options.isRestoreReminderSettings = restoreReminders
                                options.isRestoreModuleVisibility = restoreVisibility
                                options.isRestoreEmergencyPlan = restoreEmergency
                                options.isRestoreDayPlanner = restorePlanner

                                val result = viewModel.restoreFromHezBytes(fileBytes, restorePassword, options)

                                withContext(Dispatchers.Main) {
                                    restoreStatus = if (result.totalChanges > 0) {
                                        context.getString(
                                            R.string.restore_success_detailed,
                                            result.categoriesAdded,
                                            result.habitsAdded,
                                            result.habitsMerged,
                                            result.entriesAdded,
                                            result.diaryEntriesAdded,
                                            result.sleepEntriesAdded
                                        )
                                    } else {
                                        context.getString(R.string.restore_no_changes)
                                    }
                                    isRestoring = false
                                    viewModel.loadCategories()
                                    viewModel.loadHabits()
                                    viewModel.loadSleepEntries()
                                    viewModel.loadEmotionPairs()
                                    viewModel.loadMedications()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    restoreStatus = context.getString(R.string.restore_failed, e.message ?: "Unknown error")
                                    isRestoring = false
                                }
                            }
                        }
                    },
                    enabled = !isRestoring && selectedFileBytes != null,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.restore_from_file_button)) }

                if (restoreStatus.isNotEmpty()) {
                    Text(
                        restoreStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (restoreStatus.contains("failed", ignoreCase = true))
                            MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun RestoreCheckbox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label)
    }
}
