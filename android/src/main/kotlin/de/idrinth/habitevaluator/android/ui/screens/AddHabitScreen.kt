package de.idrinth.habitevaluator.android.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.R
import de.idrinth.habitevaluator.shared.model.FrequencyType
import de.idrinth.habitevaluator.shared.model.Habit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(viewModel: AppViewModel, navController: NavController) {
    val categories by viewModel.categories.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf<String?>(null) }
    var frequencyType by remember { mutableStateOf(FrequencyType.DAILY) }
    var targetFrequency by remember { mutableStateOf("1") }
    var maxEntries by remember { mutableStateOf("1") }
    var positiveScoring by remember { mutableStateOf(true) }
    var catExpanded by remember { mutableStateOf(false) }
    var freqExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.add_habit), style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = name, onValueChange = { name = it },
            label = { Text(stringResource(R.string.habit_name)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = description, onValueChange = { description = it },
            label = { Text(stringResource(R.string.description)) },
            modifier = Modifier.fillMaxWidth(), maxLines = 3
        )

        ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
            OutlinedTextField(
                value = categories.find { it.id == categoryId }?.name ?: stringResource(R.string.no_category),
                onValueChange = {}, readOnly = true,
                label = { Text(stringResource(R.string.category)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                DropdownMenuItem(text = { Text(stringResource(R.string.no_category)) },
                    onClick = { categoryId = null; catExpanded = false })
                categories.forEach { cat ->
                    DropdownMenuItem(text = { Text(cat.name ?: "") },
                        onClick = { categoryId = cat.id; catExpanded = false })
                }
            }
        }

        ExposedDropdownMenuBox(expanded = freqExpanded, onExpandedChange = { freqExpanded = it }) {
            OutlinedTextField(
                value = frequencyType.name, onValueChange = {}, readOnly = true,
                label = { Text(stringResource(R.string.frequency_type)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = freqExpanded) },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = freqExpanded, onDismissRequest = { freqExpanded = false }) {
                FrequencyType.entries.forEach { ft ->
                    DropdownMenuItem(text = { Text(ft.name) },
                        onClick = { frequencyType = ft; freqExpanded = false })
                }
            }
        }

        OutlinedTextField(
            value = targetFrequency, onValueChange = { targetFrequency = it },
            label = { Text(stringResource(R.string.target_frequency)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = maxEntries, onValueChange = { maxEntries = it },
            label = { Text(stringResource(R.string.max_entries_per_day)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        LabeledSwitch(label = stringResource(R.string.positive_scoring), checked = positiveScoring,
            onCheckedChange = { positiveScoring = it })

        Button(
            onClick = {
                if (name.isBlank()) {
                    Toast.makeText(context, R.string.habit_name_required, Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val habit = Habit(name.trim(), description.trim())
                habit.categoryId = categoryId
                habit.frequencyType = frequencyType
                habit.targetFrequency = targetFrequency.toIntOrNull() ?: 1
                habit.maxEntriesPerDay = maxEntries.toIntOrNull() ?: 1
                habit.isPositiveScoring = positiveScoring
                habit.user = currentUser
                scope.launch(Dispatchers.IO) {
                    viewModel.habitRepository.value.save(habit)
                    withContext(Dispatchers.Main) {
                        viewModel.loadHabits()
                        navController.popBackStack()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.submit)) }

        Spacer(Modifier.height(32.dp))
    }
}
