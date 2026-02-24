package de.idrinth.habitevaluator.android.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.R
import de.idrinth.habitevaluator.android.ui.navigation.Screen
import de.idrinth.habitevaluator.shared.model.EmotionEntry
import de.idrinth.habitevaluator.shared.model.EmotionStrengthFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun EmotionalStateScreen(viewModel: AppViewModel, navController: NavController) {
    val emotionPairs by viewModel.emotionPairs.collectAsState()
    val localUser by viewModel.localUser.collectAsState()
    val scope = rememberCoroutineScope()
    var entries by remember { mutableStateOf<List<EmotionEntry>>(emptyList()) }
    val expandedPairs = remember { mutableStateMapOf<String, Boolean>() }
    val dateTimeFormatter = remember { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT) }

    LaunchedEffect(localUser, emotionPairs) {
        val userId = localUser?.id ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            entries = viewModel.emotionEntryRepository.findByUserId(userId)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AddEmotionPair.route) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_emotion_pair))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.emotional_state), style = MaterialTheme.typography.headlineMedium)
            }

            items(emotionPairs, key = { it.id }) { pair ->
                val pairEntries = entries.filter { it.emotionPair?.id == pair.id }
                    .sortedByDescending { it.recordedAt }
                val isExpanded = expandedPairs[pair.id] == true

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable {
                                navController.navigate(Screen.RecordEmotionEntry.createRoute(pair.id))
                            }
                        ) {
                            Text(
                                "${pair.negativeLabel} \u2194 ${pair.positiveLabel}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                scope.launch(Dispatchers.IO) {
                                    pairEntries.forEach { viewModel.emotionEntryRepository.deleteById(it.id) }
                                    viewModel.emotionPairRepository.deleteById(pair.id)
                                    withContext(Dispatchers.Main) { viewModel.loadEmotionPairs() }
                                }
                            }) { Icon(Icons.Default.Delete, contentDescription = "Delete pair") }
                        }
                        if (pairEntries.isNotEmpty()) {
                            Text(
                                "${pairEntries.size} entries, latest: ${pairEntries.first().strength}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            TextButton(
                                onClick = { expandedPairs[pair.id] = !isExpanded }
                            ) {
                                Icon(
                                    if (isExpanded) Icons.Default.KeyboardArrowUp
                                    else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                                Text(
                                    stringResource(
                                        if (isExpanded) R.string.collapse_emotion_entries
                                        else R.string.expand_emotion_entries
                                    )
                                )
                            }
                            if (isExpanded) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                pairEntries.forEach { entry ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                EmotionStrengthFormatter.format(
                                                    entry.strength,
                                                    pair.negativeLabel,
                                                    pair.positiveLabel
                                                ),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            entry.recordedAt?.let { recordedAt ->
                                                Text(
                                                    dateTimeFormatter.format(recordedAt),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            entry.notes?.let { notes ->
                                                Text(
                                                    notes,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        IconButton(onClick = {
                                            scope.launch(Dispatchers.IO) {
                                                viewModel.emotionEntryRepository.deleteById(entry.id)
                                                val userId = localUser?.id ?: return@launch
                                                val updated = viewModel.emotionEntryRepository.findByUserId(userId)
                                                withContext(Dispatchers.Main) {
                                                    entries = updated
                                                }
                                            }
                                        }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = stringResource(R.string.delete_emotion_entry)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                stringResource(R.string.no_entries_yet),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            if (emotionPairs.isEmpty()) {
                item {
                    Text(stringResource(R.string.no_emotion_pairs), modifier = Modifier.padding(16.dp))
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}
