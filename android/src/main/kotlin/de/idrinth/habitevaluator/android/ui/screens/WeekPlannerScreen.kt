package de.idrinth.habitevaluator.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.R
import de.idrinth.habitevaluator.android.ReminderScheduler
import de.idrinth.habitevaluator.shared.model.PlannerGroup
import de.idrinth.habitevaluator.shared.model.SlotConfirmation
import de.idrinth.habitevaluator.shared.model.WeekPlannerSlot
import de.idrinth.habitevaluator.shared.service.DayPlannerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

@Composable
fun WeekPlannerScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val localUser by viewModel.localUser.collectAsState()
    val dayPlannerService = remember { DayPlannerService() }

    var allSlots by remember { mutableStateOf<List<WeekPlannerSlot>>(emptyList()) }
    var allGroups by remember { mutableStateOf<List<PlannerGroup>>(emptyList()) }
    var allConfirmations by remember { mutableStateOf<List<SlotConfirmation>>(emptyList()) }
    var selectedDay by remember { mutableIntStateOf(DayOfWeek.from(java.time.LocalDate.now()).value) }
    var showSlotDialog by remember { mutableStateOf(false) }
    var dialogHour by remember { mutableIntStateOf(0) }
    var dialogDuration by remember { mutableFloatStateOf(1f) }
    var selectedGroupIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showSummary by remember { mutableStateOf(false) }

    fun loadData() {
        scope.launch(Dispatchers.IO) {
            val userId = localUser?.id ?: return@launch
            val loadedSlots = viewModel.weekPlannerSlotRepository.findByUserId(userId)
            val loadedGroups = viewModel.plannerGroupRepository.findByUserId(userId)
            val loadedConfirmations = viewModel.slotConfirmationRepository.findByUserId(userId)
            withContext(Dispatchers.Main) {
                allSlots = loadedSlots
                allGroups = loadedGroups
                allConfirmations = loadedConfirmations
            }
        }
    }

    LaunchedEffect(localUser) { loadData() }

    val daySlots = remember(allSlots, selectedDay) {
        allSlots.filter { it.dayOfWeek == selectedDay }
    }

    val summary = remember(allSlots) {
        dayPlannerService.getWeekSlotSummary(allSlots)
    }

    val confirmationTotal = allConfirmations.size
    val confirmedCount = allConfirmations.count { it.isConfirmed }
    val confirmationRate = remember(allConfirmations) {
        dayPlannerService.calculateConfirmationRate(confirmationTotal, confirmedCount)
    }

    fun setSlotGroups(hour: Int, duration: Int, groupIds: Set<String>) {
        scope.launch(Dispatchers.IO) {
            val userId = localUser?.id ?: return@launch
            val existing = allSlots.find { it.dayOfWeek == selectedDay && it.hour == hour }
            if (groupIds.isEmpty()) {
                existing?.let { viewModel.weekPlannerSlotRepository.deleteById(it.id) }
            } else {
                val groups = allGroups.filter { it.id in groupIds }.toHashSet()
                if (existing != null) {
                    existing.groups = groups
                    existing.duration = duration
                    viewModel.weekPlannerSlotRepository.save(existing)
                } else {
                    val slot = WeekPlannerSlot()
                    slot.id = UUID.randomUUID().toString()
                    slot.dayOfWeek = selectedDay
                    slot.hour = hour
                    slot.duration = duration
                    slot.groups = groups
                    slot.user = localUser
                    viewModel.weekPlannerSlotRepository.save(slot)
                }
            }
            loadData()
            ReminderScheduler.schedulePlannerReminders(context)
        }
    }

    /** Returns the slot that covers a given hour, or null if none. */
    fun slotCoveringHour(hour: Int): WeekPlannerSlot? {
        return daySlots.find { it.coversHour(hour) }
    }

    val dayName = remember(selectedDay) {
        DayOfWeek.of(selectedDay).getDisplayName(TextStyle.FULL, Locale.getDefault())
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(stringResource(R.string.planner_week_planner_title), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))

            // Summary bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showSummary = !showSummary }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        stringResource(R.string.planner_week_summary, summary[0], summary[1]),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (showSummary) {
                        Spacer(Modifier.height(4.dp))
                        for (day in 1..7) {
                            val filled = allSlots
                                .filter { it.dayOfWeek == day && it.groups != null && it.groups.isNotEmpty() }
                                .sumOf { maxOf(1, it.duration) }
                            val dayLabel = DayOfWeek.of(day).getDisplayName(TextStyle.SHORT, Locale.getDefault())
                            Text("$dayLabel: $filled/24", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Confirmation rate
            if (confirmationTotal > 0) {
                Spacer(Modifier.height(4.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            stringResource(R.string.planner_confirmation_rate, confirmationRate * 100),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            stringResource(R.string.planner_confirmations_total, confirmedCount, confirmationTotal),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Day selector with swipe
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            if (dragAmount < -50 && selectedDay < 7) selectedDay++
                            else if (dragAmount > 50 && selectedDay > 1) selectedDay--
                        }
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { if (selectedDay > 1) selectedDay-- },
                    enabled = selectedDay > 1
                ) { Text("<") }
                Text(dayName, style = MaterialTheme.typography.titleMedium)
                TextButton(
                    onClick = { if (selectedDay < 7) selectedDay++ },
                    enabled = selectedDay < 7
                ) { Text(">") }
            }

            Spacer(Modifier.height(8.dp))

            // Hour slots
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items((0..23).toList()) { hour ->
                    val slot = daySlots.find { it.hour == hour }
                    val coveringSlot = slotCoveringHour(hour)
                    // Skip hours that are covered by a multi-hour slot starting earlier
                    if (coveringSlot != null && coveringSlot.hour != hour) {
                        // This hour is part of a multi-hour block; don't render a separate row
                    } else {
                        val displaySlot = slot ?: coveringSlot
                        val groupNames = displaySlot?.groups?.mapNotNull { it.name }?.sorted()
                        val hasGroups = !groupNames.isNullOrEmpty()
                        val duration = displaySlot?.duration ?: 1
                        val heightDp = (40 * duration).dp

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(heightDp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (hasGroups) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    dialogHour = hour
                                    dialogDuration = (displaySlot?.duration ?: 1).toFloat()
                                    selectedGroupIds = displaySlot?.groups?.map { it.id }?.toSet() ?: emptySet()
                                    showSlotDialog = true
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val endHour = hour + duration
                            Text(
                                if (duration > 1) String.format("%02d:00-%02d:00", hour, endHour)
                                else String.format("%02d:00", hour),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.width(if (duration > 1) 100.dp else 52.dp)
                            )
                            Text(
                                if (hasGroups) groupNames!!.joinToString(", ")
                                else stringResource(R.string.planner_unassigned),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSlotDialog) {
        val maxDuration = 24 - dialogHour
        AlertDialog(
            onDismissRequest = { showSlotDialog = false },
            title = { Text(stringResource(R.string.planner_assign_group, String.format("%02d:00", dialogHour))) },
            text = {
                Column {
                    // Duration slider
                    Text(
                        stringResource(R.string.planner_duration, dialogDuration.roundToInt()),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = dialogDuration,
                        onValueChange = { dialogDuration = it },
                        valueRange = 1f..maxDuration.toFloat(),
                        steps = maxOf(0, maxDuration - 2)
                    )
                    Spacer(Modifier.height(8.dp))
                    // Group checkboxes
                    allGroups.forEach { group ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedGroupIds = if (group.id in selectedGroupIds) {
                                        selectedGroupIds - group.id
                                    } else {
                                        selectedGroupIds + group.id
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = group.id in selectedGroupIds,
                                onCheckedChange = { checked ->
                                    selectedGroupIds = if (checked) {
                                        selectedGroupIds + group.id
                                    } else {
                                        selectedGroupIds - group.id
                                    }
                                }
                            )
                            Text(
                                group.name ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    setSlotGroups(dialogHour, dialogDuration.roundToInt(), selectedGroupIds)
                    showSlotDialog = false
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showSlotDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
