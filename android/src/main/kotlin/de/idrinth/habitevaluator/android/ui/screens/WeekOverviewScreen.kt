package de.idrinth.habitevaluator.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.R
import de.idrinth.habitevaluator.shared.model.WeekPlannerSlot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeekOverviewScreen(viewModel: AppViewModel) {
    val scope = rememberCoroutineScope()
    val localUser by viewModel.localUser.collectAsState()

    var allSlots by remember { mutableStateOf<List<WeekPlannerSlot>>(emptyList()) }

    LaunchedEffect(localUser) {
        scope.launch(Dispatchers.IO) {
            val userId = localUser?.id ?: return@launch
            val loadedSlots = viewModel.weekPlannerSlotRepository.findByUserId(userId)
            withContext(Dispatchers.Main) {
                allSlots = loadedSlots
            }
        }
    }

    val cellWidth = 40.dp
    val cellHeight = 28.dp
    val hourLabelWidth = 52.dp

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                stringResource(R.string.planner_week_overview_title),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(8.dp))

            if (allSlots.isEmpty()) {
                Text(
                    stringResource(R.string.planner_week_overview_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header row with day labels
                    Row {
                        // Empty corner cell for hour labels column
                        Box(modifier = Modifier.width(hourLabelWidth))
                        for (day in 1..7) {
                            val dayLabel = DayOfWeek.of(day)
                                .getDisplayName(TextStyle.SHORT, Locale.getDefault())
                            Box(
                                modifier = Modifier.width(cellWidth),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    dayLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))

                    // 24 hour rows
                    for (hour in 0..23) {
                        Row {
                            // Hour label
                            Box(
                                modifier = Modifier
                                    .width(hourLabelWidth)
                                    .height(cellHeight),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    String.format("%02d:00", hour),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                            // 7 day cells
                            for (day in 1..7) {
                                val occupied = isHourOccupied(allSlots, day, hour)
                                Box(
                                    modifier = Modifier
                                        .width(cellWidth)
                                        .height(cellHeight)
                                        .padding(1.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            if (occupied) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (occupied) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        stringResource(R.string.planner_week_overview_occupied),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, end = 16.dp)
                    )
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                    Text(
                        stringResource(R.string.planner_week_overview_free),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

internal fun isHourOccupied(slots: List<WeekPlannerSlot>, dayOfWeek: Int, hour: Int): Boolean {
    return slots.any { slot ->
        slot.dayOfWeek == dayOfWeek
                && slot.groups != null
                && slot.groups.isNotEmpty()
                && slot.coversHour(hour)
    }
}
