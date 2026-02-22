package de.idrinth.habitevaluator.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import de.idrinth.habitevaluator.shared.model.EmotionEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

@Composable
fun RecordEmotionEntryScreen(viewModel: AppViewModel, pairId: String, navController: NavController) {
    val emotionPairs by viewModel.emotionPairs.collectAsState()
    val localUser by viewModel.localUser.collectAsState()
    val scope = rememberCoroutineScope()

    val pair = emotionPairs.find { it.id == pairId }
    if (pair == null) {
        Text(stringResource(R.string.emotion_pair_not_found), modifier = Modifier.padding(16.dp))
        return
    }

    var strength by remember { mutableFloatStateOf(0f) }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.record_emotion), style = MaterialTheme.typography.headlineMedium)

        Text(
            "${pair.negativeLabel} \u2194 ${pair.positiveLabel}",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            "Strength: ${strength.toInt()}",
            style = MaterialTheme.typography.headlineLarge
        )

        Slider(
            value = strength,
            onValueChange = { strength = it },
            valueRange = -10f..10f,
            steps = 19,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = notes, onValueChange = { notes = it },
            label = { Text(stringResource(R.string.notes)) },
            modifier = Modifier.fillMaxWidth(), maxLines = 3
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val user = localUser ?: return@Button
                val entry = EmotionEntry()
                entry.emotionPair = pair
                entry.strength = strength.toInt()
                entry.recordedAt = LocalDateTime.now()
                entry.notes = notes.ifBlank { null }
                entry.user = user
                scope.launch(Dispatchers.IO) {
                    viewModel.emotionEntryRepository.save(entry)
                    withContext(Dispatchers.Main) {
                        viewModel.loadEmotionPairs()
                        navController.popBackStack()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.record)) }
    }
}
