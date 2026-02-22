package de.idrinth.habitevaluator.android.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.R
import de.idrinth.habitevaluator.shared.model.EmotionPair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AddEmotionPairScreen(viewModel: AppViewModel, navController: NavController) {
    val localUser by viewModel.localUser.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var negative by remember { mutableStateOf("") }
    var positive by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.add_emotion_pair), style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = negative, onValueChange = { negative = it },
            label = { Text(stringResource(R.string.negative_label)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = positive, onValueChange = { positive = it },
            label = { Text(stringResource(R.string.positive_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (negative.isBlank() || positive.isBlank()) {
                    Toast.makeText(context, R.string.labels_required, Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val user = localUser ?: return@Button
                val pair = EmotionPair()
                pair.negativeLabel = negative.trim()
                pair.positiveLabel = positive.trim()
                pair.user = user
                scope.launch(Dispatchers.IO) {
                    viewModel.emotionPairRepository.save(pair)
                    withContext(Dispatchers.Main) {
                        viewModel.loadEmotionPairs()
                        navController.popBackStack()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.save)) }
    }
}
