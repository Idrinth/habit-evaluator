package de.idrinth.habitevaluator.android.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.R
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

        Spacer(Modifier.height(32.dp))
    }
}
