package de.idrinth.habitevaluator.android.ui.screens

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
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.R
import de.idrinth.habitevaluator.shared.model.EventCorrelation
import de.idrinth.habitevaluator.shared.service.EventCorrelationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CorrelationScreen(viewModel: AppViewModel) {
    val scope = rememberCoroutineScope()
    val habits by viewModel.habits.collectAsState()
    val sleepEntries by viewModel.sleepEntries.collectAsState()
    val localUser by viewModel.localUser.collectAsState()

    val correlationService = remember { EventCorrelationService() }
    var allCorrelations by remember { mutableStateOf<List<EventCorrelation>>(emptyList()) }
    var sourceFilter by remember { mutableStateOf("All") }
    var targetFilter by remember { mutableStateOf("All") }
    var sourceExpanded by remember { mutableStateOf(false) }
    var targetExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(localUser, habits, sleepEntries) {
        scope.launch(Dispatchers.IO) {
            val userId = localUser?.id ?: return@launch
            val diary = viewModel.diaryEntryRepository.findByUserId(userId)
            val emotions = viewModel.emotionEntryRepository.findByUserId(userId)
            val sportLogs = viewModel.sportLogRepository.findByUserId(userId)
            val foodLogs = viewModel.foodLogRepository.findByUserId(userId)
            val medicationLogs = viewModel.medicationLogRepository.findByUserId(userId)

            val correlations = correlationService.calculateCorrelations(
                habits, diary, sleepEntries, emotions,
                sportLogs, emptyList(), emptyList(), foodLogs, medicationLogs
            )
            withContext(Dispatchers.Main) { allCorrelations = correlations }
        }
    }

    // Build filter options
    val eventNames = remember(allCorrelations) {
        val names = mutableSetOf<String>()
        allCorrelations.forEach {
            it.eventA?.let { a -> names.add(a) }
            it.eventB?.let { b -> names.add(b) }
        }
        listOf("All") + names.sorted()
    }

    val filteredCorrelations = remember(allCorrelations, sourceFilter, targetFilter) {
        allCorrelations.filter { c ->
            (sourceFilter == "All" || c.eventA == sourceFilter) &&
                    (targetFilter == "All" || c.eventB == targetFilter)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.correlations), style = MaterialTheme.typography.headlineMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = sourceExpanded,
                onExpandedChange = { sourceExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = sourceFilter,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.source)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = sourceExpanded, onDismissRequest = { sourceExpanded = false }) {
                    eventNames.forEach { name ->
                        DropdownMenuItem(text = { Text(name) }, onClick = {
                            sourceFilter = name
                            sourceExpanded = false
                        })
                    }
                }
            }
            ExposedDropdownMenuBox(
                expanded = targetExpanded,
                onExpandedChange = { targetExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = targetFilter,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.target)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = targetExpanded, onDismissRequest = { targetExpanded = false }) {
                    eventNames.forEach { name ->
                        DropdownMenuItem(text = { Text(name) }, onClick = {
                            targetFilter = name
                            targetExpanded = false
                        })
                    }
                }
            }
        }

        if (filteredCorrelations.isEmpty()) {
            Text(stringResource(R.string.no_correlations), style = MaterialTheme.typography.bodyMedium)
        } else {
            Text(
                stringResource(R.string.correlation_disclaimer),
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic)
            )
            Spacer(Modifier.height(4.dp))
            filteredCorrelations.forEach { correlation ->
                val strength = abs(correlation.correlation)
                val isWeak = strength < 0.3
                val confidenceLabel = when {
                    strength >= 0.5 -> stringResource(R.string.strong)
                    strength >= 0.3 -> stringResource(R.string.moderate)
                    else -> stringResource(R.string.weak)
                }
                val confidenceColor = when {
                    strength >= 0.5 -> Color(0xFF4CAF50)
                    strength >= 0.3 -> Color(0xFFFF9800)
                    else -> Color(0xFF9E9E9E)
                }
                val valueColor = if (correlation.correlation >= 0) Color(0xFF4CAF50) else Color(0xFFE91E63)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (isWeak) 0.6f else 1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${correlation.eventA ?: ""} \u2194 ${correlation.eventB ?: ""}",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            confidenceLabel,
                            color = confidenceColor,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            String.format("%+.3f", correlation.correlation),
                            color = valueColor,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}
