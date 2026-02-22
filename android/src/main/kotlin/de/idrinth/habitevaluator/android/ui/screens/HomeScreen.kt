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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.R
import de.idrinth.habitevaluator.android.ui.navigation.Screen
import de.idrinth.habitevaluator.shared.model.Habit
import de.idrinth.habitevaluator.shared.model.HabitEntry
import de.idrinth.habitevaluator.shared.service.HabitEvaluatorService
import de.idrinth.habitevaluator.shared.service.HabitScoringService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: AppViewModel, navController: NavController) {
    val habits by viewModel.habits.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedHabit by remember { mutableStateOf<Habit?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val filteredHabits = if (selectedCategoryId == null) habits
    else habits.filter { it.categoryId == selectedCategoryId }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AddHabit.route) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_habit))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                // Category filter
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.id == selectedCategoryId }?.name
                            ?: stringResource(R.string.all_habits),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.all_habits)) },
                            onClick = { selectedCategoryId = null; categoryExpanded = false }
                        )
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name ?: "") },
                                onClick = { selectedCategoryId = cat.id; categoryExpanded = false }
                            )
                        }
                    }
                }
            }

            // Evaluation card for selected habit
            selectedHabit?.let { habit ->
                item {
                    HabitEvaluationCard(
                        habit = habit,
                        onComplete = {
                            scope.launch(Dispatchers.IO) {
                                val entry = HabitEntry()
                                habit.addEntry(entry)
                                viewModel.habitRepository.value.save(habit)
                                withContext(Dispatchers.Main) { viewModel.loadHabits() }
                            }
                        },
                        onRemoveCompletion = {
                            scope.launch(Dispatchers.IO) {
                                habit.removeLastEntryForDate(LocalDate.now())
                                viewModel.habitRepository.value.save(habit)
                                withContext(Dispatchers.Main) { viewModel.loadHabits() }
                            }
                        },
                        onViewPoints = {
                            navController.navigate(Screen.PointDevelopment.createRoute(habit.id))
                        }
                    )
                }
            }

            // Habit list
            items(filteredHabits, key = { it.id }) { habit ->
                HabitListItem(
                    habit = habit,
                    isSelected = habit.id == selectedHabit?.id,
                    onClick = { selectedHabit = if (selectedHabit?.id == habit.id) null else habit },
                    onEdit = { navController.navigate(Screen.EditHabits.createRoute(habit.id)) },
                    onDelete = {
                        scope.launch(Dispatchers.IO) {
                            viewModel.habitRepository.value.deleteById(habit.id)
                            withContext(Dispatchers.Main) {
                                if (selectedHabit?.id == habit.id) selectedHabit = null
                                viewModel.loadHabits()
                                viewModel.loadCategories()
                            }
                        }
                    }
                )
            }

            if (filteredHabits.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.no_habits_yet), style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadDefaults() }) {
                            Text(stringResource(R.string.load_defaults))
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun HabitListItem(
    habit: Habit,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                habit.description?.takeIf { it.isNotEmpty() }?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
private fun HabitEvaluationCard(
    habit: Habit,
    onComplete: () -> Unit,
    onRemoveCompletion: () -> Unit,
    onViewPoints: () -> Unit
) {
    val evaluator = remember { HabitEvaluatorService() }
    val scorer = remember { HabitScoringService() }
    val eval = remember(habit, habit.entries?.size) {
        evaluator.evaluate(habit, LocalDate.now().minusDays(29), LocalDate.now())
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(habit.name ?: "", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))

            if (eval != null) {
                Text(
                    if (habit.isPositiveScoring) "Streak: ${eval.currentStreak} days"
                    else "Avoidance: ${eval.currentStreak} days"
                )
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { eval.completionRate.toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "${(eval.completionRate * 100).toInt()}% (${eval.totalEntries}/${eval.targetEntries})",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onComplete, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.complete))
                }
                Button(onClick = onRemoveCompletion, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.remove_completion))
                }
            }
            Spacer(Modifier.height(4.dp))
            Button(onClick = onViewPoints, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.view_points))
            }
        }
    }
}
