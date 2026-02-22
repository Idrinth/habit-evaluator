package de.idrinth.habitevaluator.android.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.R
import de.idrinth.habitevaluator.shared.model.EmergencyPlanStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun EmergencyDialogueScreen(viewModel: AppViewModel, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val localUser by viewModel.localUser.collectAsState()

    var steps by remember { mutableStateOf<List<EmergencyPlanStep>>(emptyList()) }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var showActions by remember { mutableStateOf(false) }

    LaunchedEffect(localUser) {
        scope.launch(Dispatchers.IO) {
            val userId = localUser?.id ?: return@launch
            val loaded = viewModel.emergencyPlanStepRepository.findByUserId(userId)
                .sortedBy { it.stepOrder }
            withContext(Dispatchers.Main) {
                steps = loaded
                if (loaded.isEmpty()) navController.popBackStack()
            }
        }
    }

    fun advanceToNext() {
        showActions = false
        currentStepIndex++
    }

    val isComplete = currentStepIndex >= steps.size
    val currentStep = if (!isComplete && steps.isNotEmpty()) steps[currentStepIndex] else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isComplete) {
            // Completion screen
            Text(
                stringResource(R.string.dialogue_complete),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.finish)) }
        } else if (currentStep != null) {
            // Progress
            Text(
                "${stringResource(R.string.step)} ${currentStepIndex + 1} ${stringResource(R.string.of)} ${steps.size}",
                style = MaterialTheme.typography.bodySmall
            )

            // Question
            Text(
                currentStep.question ?: "",
                style = MaterialTheme.typography.headlineSmall
            )

            if (!showActions) {
                // Yes/No buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showActions = true },
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.yes)) }
                    OutlinedButton(
                        onClick = { advanceToNext() },
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.no)) }
                }
            } else {
                // Actions list
                currentStep.actions?.sortedBy { it.actionOrder }?.forEach { action ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(action.actionText ?: "", style = MaterialTheme.typography.bodyLarge)
                            if (!action.phoneNumber.isNullOrBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(action.phoneNumber ?: "", style = MaterialTheme.typography.bodyMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(onClick = {
                                        try {
                                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${action.phoneNumber}")))
                                        } catch (_: Exception) {
                                            Toast.makeText(context, R.string.no_dialer, Toast.LENGTH_SHORT).show()
                                        }
                                    }) { Text(stringResource(R.string.call)) }
                                    TextButton(onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("phone", action.phoneNumber))
                                        Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
                                    }) { Text(stringResource(R.string.copy)) }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                if (currentStepIndex < steps.size - 1) {
                    Button(
                        onClick = { advanceToNext() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.next)) }
                } else {
                    Button(
                        onClick = { advanceToNext() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.finish)) }
                }
            }
        }
    }
}
