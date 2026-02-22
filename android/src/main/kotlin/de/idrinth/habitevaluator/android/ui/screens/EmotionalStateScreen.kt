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
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.R
import de.idrinth.habitevaluator.android.ui.navigation.Screen
import de.idrinth.habitevaluator.shared.model.EmotionEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun EmotionalStateScreen(viewModel: AppViewModel, navController: NavController) {
    val emotionPairs by viewModel.emotionPairs.collectAsState()
    val localUser by viewModel.localUser.collectAsState()
    val scope = rememberCoroutineScope()
    var entries by remember { mutableStateOf<List<EmotionEntry>>(emptyList()) }

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

                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        navController.navigate(Screen.RecordEmotionEntry.createRoute(pair.id))
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                            Text("${pairEntries.size} entries, latest: ${pairEntries.first().strength}",
                                style = MaterialTheme.typography.bodySmall)
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
