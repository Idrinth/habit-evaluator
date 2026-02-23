package de.idrinth.habitevaluator.android.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.R
import de.idrinth.habitevaluator.shared.model.FoodLog
import de.idrinth.habitevaluator.shared.model.FoodTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.UUID

@Composable
fun FoodLogScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val localUser by viewModel.localUser.collectAsState()

    var entries by remember { mutableStateOf<List<FoodLog>>(emptyList()) }
    var formExpanded by remember { mutableStateOf(false) }
    var dateTime by remember { mutableStateOf(LocalDateTime.now().withSecond(0).withNano(0)) }
    var foodItems by remember { mutableStateOf("") }
    var kcal by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val dateTimeFormat = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") }

    fun loadEntries() {
        scope.launch(Dispatchers.IO) {
            val userId = localUser?.id ?: return@launch
            viewModel.foodTagRepository.deleteEmptyTags(userId)
            val loaded = viewModel.foodLogRepository.findByUserId(userId)
                .sortedWith(compareByDescending<FoodLog> { it.dateTime }.thenByDescending { it.createdAt })
            withContext(Dispatchers.Main) { entries = loaded }
        }
    }

    LaunchedEffect(localUser) { loadEntries() }

    fun resetForm() {
        dateTime = LocalDateTime.now().withSecond(0).withNano(0)
        foodItems = ""
        kcal = ""
        carbs = ""
        notes = ""
        formExpanded = false
    }

    fun pickDateTime() {
        val cal = Calendar.getInstance()
        DatePickerDialog(context, { _, y, m, d ->
            TimePickerDialog(context, { _, h, min ->
                dateTime = LocalDateTime.of(y, m + 1, d, h, min)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    fun addEntry() {
        val userId = localUser?.id ?: return
        val items = foodItems.trim()
        if (items.isEmpty()) {
            Toast.makeText(context, R.string.food_items_required, Toast.LENGTH_SHORT).show()
            return
        }
        val kcalVal = if (kcal.isNotBlank()) {
            kcal.toIntOrNull() ?: run {
                Toast.makeText(context, R.string.invalid_kcal, Toast.LENGTH_SHORT).show()
                return
            }
        } else null
        val carbsVal = if (carbs.isNotBlank()) {
            carbs.toDoubleOrNull() ?: run {
                Toast.makeText(context, R.string.invalid_carbs, Toast.LENGTH_SHORT).show()
                return
            }
        } else null

        scope.launch(Dispatchers.IO) {
            val log = FoodLog()
            log.id = UUID.randomUUID().toString()
            log.foodItems = items
            log.kcal = kcalVal
            log.carbohydrates = carbsVal
            log.notes = notes.ifBlank { null }
            log.dateTime = dateTime
            log.user = localUser
            viewModel.foodLogRepository.save(log)

            // Resolve/create tags
            items.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { tagName ->
                val lower = tagName.lowercase()
                val existingTag = viewModel.foodTagRepository.findByNameLowerAndUserId(lower, userId)
                val tag = if (existingTag.isPresent) {
                    existingTag.get()
                } else {
                    val newTag = FoodTag()
                    newTag.id = UUID.randomUUID().toString()
                    newTag.name = tagName
                    newTag.user = localUser
                    viewModel.foodTagRepository.save(newTag)
                    newTag
                }
                viewModel.roomFoodTagRepository.linkTagToFoodLog(tag.id, log.id)
            }

            withContext(Dispatchers.Main) { resetForm() }
            loadEntries()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                resetForm()
                formExpanded = true
            }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_entry))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(stringResource(R.string.food_log), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(entry.foodItems ?: "", style = MaterialTheme.typography.bodyLarge)
                            entry.dateTime?.let { Text(it.format(dateTimeFormat), style = MaterialTheme.typography.bodySmall) }
                            entry.kcal?.let { Text("$it kcal", style = MaterialTheme.typography.bodySmall) }
                            entry.carbohydrates?.let { Text("${it}g carbs", style = MaterialTheme.typography.bodySmall) }
                            entry.notes?.let { if (it.isNotBlank()) Text(it, style = MaterialTheme.typography.bodySmall) }
                            TextButton(onClick = {
                                scope.launch(Dispatchers.IO) {
                                    viewModel.foodLogRepository.deleteById(entry.id)
                                    loadEntries()
                                }
                            }) { Text(stringResource(R.string.delete)) }
                        }
                    }
                }
            }
        }
    }

    if (formExpanded) {
        AlertDialog(
            onDismissRequest = { resetForm() },
            title = { Text(stringResource(R.string.add_entry)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dateTime.format(dateTimeFormat),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.date_time)) },
                        modifier = Modifier.fillMaxWidth().clickable { pickDateTime() },
                        enabled = false
                    )
                    OutlinedTextField(
                        value = foodItems,
                        onValueChange = { foodItems = it },
                        label = { Text(stringResource(R.string.food_items)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = kcal,
                        onValueChange = { kcal = it },
                        label = { Text(stringResource(R.string.kcal)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text(stringResource(R.string.carbohydrates)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(stringResource(R.string.notes)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { addEntry() }) {
                    Text(stringResource(R.string.add_entry))
                }
            },
            dismissButton = {
                TextButton(onClick = { resetForm() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
