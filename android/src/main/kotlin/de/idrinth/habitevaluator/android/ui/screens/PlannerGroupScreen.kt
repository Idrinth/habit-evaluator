package de.idrinth.habitevaluator.android.ui.screens

import android.widget.Toast
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
import de.idrinth.habitevaluator.shared.model.PlannerGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@Composable
fun PlannerGroupScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val localUser by viewModel.localUser.collectAsState()

    var groups by remember { mutableStateOf<List<PlannerGroup>>(emptyList()) }
    var formExpanded by remember { mutableStateOf(false) }
    var editingGroup by remember { mutableStateOf<PlannerGroup?>(null) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    fun loadGroups() {
        scope.launch(Dispatchers.IO) {
            val userId = localUser?.id ?: return@launch
            val loaded = viewModel.plannerGroupRepository.findByUserId(userId)
            withContext(Dispatchers.Main) { groups = loaded }
        }
    }

    LaunchedEffect(localUser) { loadGroups() }

    fun resetForm() {
        name = ""
        description = ""
        formExpanded = false
        editingGroup = null
    }

    fun addGroup() {
        if (name.isBlank()) {
            Toast.makeText(context, R.string.planner_group_name_required, Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch(Dispatchers.IO) {
            val group = PlannerGroup()
            group.id = UUID.randomUUID().toString()
            group.name = name.trim()
            group.description = description.ifBlank { null }
            group.user = localUser
            viewModel.plannerGroupRepository.save(group)
            withContext(Dispatchers.Main) { resetForm() }
            loadGroups()
        }
    }

    fun updateGroup() {
        if (name.isBlank()) {
            Toast.makeText(context, R.string.planner_group_name_required, Toast.LENGTH_SHORT).show()
            return
        }
        val existing = editingGroup ?: return
        scope.launch(Dispatchers.IO) {
            existing.name = name.trim()
            existing.description = description.ifBlank { null }
            viewModel.plannerGroupRepository.save(existing)
            withContext(Dispatchers.Main) { resetForm() }
            loadGroups()
        }
    }

    fun startEdit(group: PlannerGroup) {
        editingGroup = group
        name = group.name ?: ""
        description = group.description ?: ""
        formExpanded = true
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
            Text(stringResource(R.string.planner_groups_title), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))

            if (groups.isEmpty()) {
                Text(stringResource(R.string.planner_no_groups))
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(groups, key = { it.id }) { group ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(group.name ?: "", style = MaterialTheme.typography.bodyLarge)
                            group.description?.let {
                                if (it.isNotBlank()) Text(it, style = MaterialTheme.typography.bodySmall)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { startEdit(group) }) {
                                    Text(stringResource(R.string.edit))
                                }
                                TextButton(onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        viewModel.plannerGroupRepository.deleteById(group.id)
                                        loadGroups()
                                    }
                                }) { Text(stringResource(R.string.delete)) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (formExpanded) {
        val isEditing = editingGroup != null
        AlertDialog(
            onDismissRequest = { resetForm() },
            title = {
                Text(stringResource(if (isEditing) R.string.planner_edit_group else R.string.planner_add_group))
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.planner_group_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(stringResource(R.string.description)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { if (isEditing) updateGroup() else addGroup() }) {
                    Text(stringResource(if (isEditing) R.string.save else R.string.add_entry))
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
