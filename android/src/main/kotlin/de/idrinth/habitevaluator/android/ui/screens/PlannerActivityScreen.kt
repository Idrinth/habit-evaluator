package de.idrinth.habitevaluator.android.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.FilterChip
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
import de.idrinth.habitevaluator.shared.model.PlannerActivity
import de.idrinth.habitevaluator.shared.model.PlannerGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlannerActivityScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val localUser by viewModel.localUser.collectAsState()

    var activities by remember { mutableStateOf<List<PlannerActivity>>(emptyList()) }
    var allGroups by remember { mutableStateOf<List<PlannerGroup>>(emptyList()) }
    var formExpanded by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedGroupIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    fun loadData() {
        scope.launch(Dispatchers.IO) {
            val userId = localUser?.id ?: return@launch
            val loadedActivities = viewModel.plannerActivityRepository.findByUserId(userId)
            val loadedGroups = viewModel.plannerGroupRepository.findByUserId(userId)
            withContext(Dispatchers.Main) {
                activities = loadedActivities
                allGroups = loadedGroups
            }
        }
    }

    LaunchedEffect(localUser) { loadData() }

    fun resetForm() {
        name = ""
        description = ""
        selectedGroupIds = emptySet()
        formExpanded = false
    }

    fun addActivity() {
        if (name.isBlank()) {
            Toast.makeText(context, R.string.planner_activity_name_required, Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch(Dispatchers.IO) {
            val activity = PlannerActivity()
            activity.id = UUID.randomUUID().toString()
            activity.name = name.trim()
            activity.description = description.ifBlank { null }
            activity.user = localUser
            val groups = allGroups.filter { selectedGroupIds.contains(it.id) }.toHashSet()
            activity.groups = groups
            viewModel.plannerActivityRepository.save(activity)
            withContext(Dispatchers.Main) { resetForm() }
            loadData()
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
            Text(stringResource(R.string.planner_activities_title), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))

            if (activities.isEmpty()) {
                Text(stringResource(R.string.planner_no_activities))
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activities, key = { it.id }) { activity ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(activity.name ?: "", style = MaterialTheme.typography.bodyLarge)
                            activity.description?.let {
                                if (it.isNotBlank()) Text(it, style = MaterialTheme.typography.bodySmall)
                            }
                            val groupNames = activity.groups?.joinToString(", ") { it.name ?: "" } ?: ""
                            if (groupNames.isNotBlank()) {
                                Text(
                                    "${stringResource(R.string.planner_groups_label)}: $groupNames",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            TextButton(onClick = {
                                scope.launch(Dispatchers.IO) {
                                    viewModel.plannerActivityRepository.deleteById(activity.id)
                                    loadData()
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
            title = { Text(stringResource(R.string.planner_add_activity)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.planner_activity_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(stringResource(R.string.description)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (allGroups.isNotEmpty()) {
                        Text(
                            stringResource(R.string.planner_select_groups),
                            style = MaterialTheme.typography.labelMedium
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            allGroups.forEach { group ->
                                FilterChip(
                                    selected = selectedGroupIds.contains(group.id),
                                    onClick = {
                                        selectedGroupIds = if (selectedGroupIds.contains(group.id)) {
                                            selectedGroupIds - group.id
                                        } else {
                                            selectedGroupIds + group.id
                                        }
                                    },
                                    label = { Text(group.name ?: "") }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { addActivity() }) {
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
