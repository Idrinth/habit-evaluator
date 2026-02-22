package de.idrinth.habitevaluator.android.ui.screens

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
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.R
import de.idrinth.habitevaluator.shared.model.FrequencyType
import de.idrinth.habitevaluator.shared.model.Habit
import de.idrinth.habitevaluator.shared.model.ScoringRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHabitsScreen(viewModel: AppViewModel, habitId: String, navController: NavController) {
    val habits by viewModel.habits.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val scope = rememberCoroutineScope()

    val habit = habits.find { it.id == habitId }
    if (habit == null) {
        Text(stringResource(R.string.habit_not_found), modifier = Modifier.padding(16.dp))
        return
    }

    var name by remember(habitId) { mutableStateOf(habit.name ?: "") }
    var description by remember(habitId) { mutableStateOf(habit.description ?: "") }
    var categoryId by remember(habitId) { mutableStateOf(habit.categoryId) }
    var frequencyType by remember(habitId) { mutableStateOf(habit.frequencyType ?: FrequencyType.DAILY) }
    var targetFrequency by remember(habitId) { mutableStateOf(habit.targetFrequency.toString()) }
    var maxEntries by remember(habitId) { mutableStateOf(habit.maxEntriesPerDay.toString()) }
    var positiveScoring by remember(habitId) { mutableStateOf(habit.isPositiveScoring) }
    var catExpanded by remember { mutableStateOf(false) }
    var freqExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.edit_habit), style = MaterialTheme.typography.headlineMedium)

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

        // Category
        ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
            OutlinedTextField(
                value = categories.find { it.id == categoryId }?.name ?: stringResource(R.string.no_category),
                onValueChange = {}, readOnly = true,
                label = { Text(stringResource(R.string.category)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
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

        // Frequency type
        ExposedDropdownMenuBox(expanded = freqExpanded, onExpandedChange = { freqExpanded = it }) {
            OutlinedTextField(
                value = frequencyType.name, onValueChange = {}, readOnly = true,
                label = { Text(stringResource(R.string.frequency_type)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = freqExpanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
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
                habit.name = name
                habit.description = description
                habit.categoryId = categoryId
                habit.frequencyType = frequencyType
                habit.targetFrequency = targetFrequency.toIntOrNull() ?: 1
                habit.maxEntriesPerDay = maxEntries.toIntOrNull() ?: 1
                habit.isPositiveScoring = positiveScoring
                scope.launch(Dispatchers.IO) {
                    viewModel.habitRepository.value.save(habit)
                    withContext(Dispatchers.Main) {
                        viewModel.loadHabits()
                        navController.popBackStack()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.save)) }
    }
}

@Composable
fun LabeledSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
