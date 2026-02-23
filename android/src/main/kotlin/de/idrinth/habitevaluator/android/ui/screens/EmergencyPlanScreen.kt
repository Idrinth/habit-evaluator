package de.idrinth.habitevaluator.android.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.R
import de.idrinth.habitevaluator.android.ui.navigation.Screen
import de.idrinth.habitevaluator.shared.model.EmergencyPlanAction
import de.idrinth.habitevaluator.shared.model.EmergencyPlanStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

private data class ActionInput(var text: String = "", var phone: String = "")

@Composable
fun EmergencyPlanScreen(viewModel: AppViewModel, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val localUser by viewModel.localUser.collectAsState()

    var steps by remember { mutableStateOf<List<EmergencyPlanStep>>(emptyList()) }
    var formExpanded by remember { mutableStateOf(false) }
    var question by remember { mutableStateOf("") }
    val actionInputs = remember { mutableStateListOf(ActionInput()) }
    var editingStep by remember { mutableStateOf<EmergencyPlanStep?>(null) }

    fun loadSteps() {
        scope.launch(Dispatchers.IO) {
            val userId = localUser?.id ?: return@launch
            val loaded = viewModel.emergencyPlanStepRepository.findByUserId(userId)
                .sortedBy { it.stepOrder }
            withContext(Dispatchers.Main) { steps = loaded }
        }
    }

    LaunchedEffect(localUser) { loadSteps() }

    fun resetForm() {
        question = ""
        actionInputs.clear()
        actionInputs.add(ActionInput())
        editingStep = null
        formExpanded = false
    }

    fun saveStep() {
        if (question.isBlank()) {
            Toast.makeText(context, R.string.question_required, Toast.LENGTH_SHORT).show()
            return
        }
        val validActions = actionInputs.filter { it.text.isNotBlank() }
        if (validActions.isEmpty()) {
            Toast.makeText(context, R.string.action_required, Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch(Dispatchers.IO) {
            val step = editingStep ?: EmergencyPlanStep().also {
                it.id = UUID.randomUUID().toString()
                it.stepOrder = if (steps.isEmpty()) 0 else (steps.maxOf { s -> s.stepOrder } + 1)
                it.user = localUser
            }
            step.question = question.trim()

            val actions = validActions.mapIndexed { idx, input ->
                EmergencyPlanAction().also { a ->
                    a.id = UUID.randomUUID().toString()
                    a.actionText = input.text.trim()
                    a.phoneNumber = input.phone.ifBlank { null }
                    a.actionOrder = idx
                    a.step = step
                }
            }
            step.actions = actions

            viewModel.emergencyPlanStepRepository.save(step)

            withContext(Dispatchers.Main) { resetForm() }
            loadSteps()
        }
    }

    fun startEdit(step: EmergencyPlanStep) {
        editingStep = step
        question = step.question ?: ""
        actionInputs.clear()
        step.actions?.sortedBy { it.actionOrder }?.forEach { action ->
            actionInputs.add(ActionInput(action.actionText ?: "", action.phoneNumber ?: ""))
        }
        if (actionInputs.isEmpty()) actionInputs.add(ActionInput())
        formExpanded = true
    }

    fun swapOrder(a: EmergencyPlanStep, b: EmergencyPlanStep) {
        scope.launch(Dispatchers.IO) {
            val tmpOrder = a.stepOrder
            a.stepOrder = b.stepOrder
            b.stepOrder = tmpOrder
            viewModel.emergencyPlanStepRepository.save(a)
            viewModel.emergencyPlanStepRepository.save(b)
            loadSteps()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(stringResource(R.string.emergency_plan), style = MaterialTheme.typography.headlineMedium)
        }

        if (steps.isNotEmpty()) {
            item {
                Button(
                    onClick = { navController.navigate(Screen.EmergencyDialogue.route) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.start_dialogue)) }
            }
        }

        // Toggle form
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (formExpanded && editingStep != null) resetForm()
                        else formExpanded = !formExpanded
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (editingStep != null) stringResource(R.string.edit_step) else stringResource(R.string.add_step),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Icon(
                        painter = painterResource(
                            if (formExpanded) android.R.drawable.arrow_up_float
                            else android.R.drawable.arrow_down_float
                        ),
                        contentDescription = null
                    )
                }
            }
        }

        if (formExpanded) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = question,
                            onValueChange = { question = it },
                            label = { Text(stringResource(R.string.question)) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(stringResource(R.string.actions), style = MaterialTheme.typography.titleSmall)

                        actionInputs.forEachIndexed { idx, input ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = input.text,
                                        onValueChange = { actionInputs[idx] = input.copy(text = it) },
                                        label = { Text(stringResource(R.string.action_text)) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = input.phone,
                                        onValueChange = { actionInputs[idx] = input.copy(phone = it) },
                                        label = { Text(stringResource(R.string.phone_number)) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                if (actionInputs.size > 1) {
                                    IconButton(onClick = { actionInputs.removeAt(idx) }) {
                                        Icon(painterResource(android.R.drawable.ic_delete), contentDescription = stringResource(R.string.remove))
                                    }
                                }
                            }
                        }

                        OutlinedButton(onClick = { actionInputs.add(ActionInput()) }) {
                            Text(stringResource(R.string.add_action))
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { saveStep() }, modifier = Modifier.weight(1f)) {
                                Text(if (editingStep != null) stringResource(R.string.update_step) else stringResource(R.string.add_step))
                            }
                            if (editingStep != null) {
                                OutlinedButton(onClick = { resetForm() }, modifier = Modifier.weight(1f)) {
                                    Text(stringResource(R.string.cancel))
                                }
                            }
                        }
                    }
                }
            }
        }

        itemsIndexed(steps, key = { _, step -> step.id }) { index, step ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(step.question ?: "", style = MaterialTheme.typography.bodyLarge)
                    step.actions?.sortedBy { it.actionOrder }?.forEach { action ->
                        Row(
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("• ${action.actionText ?: ""}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            if (!action.phoneNumber.isNullOrBlank()) {
                                TextButton(onClick = {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${action.phoneNumber}")))
                                    } catch (_: Exception) {
                                        Toast.makeText(context, R.string.no_dialer, Toast.LENGTH_SHORT).show()
                                    }
                                }) { Text(action.phoneNumber ?: "") }
                                IconButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("phone", action.phoneNumber))
                                    Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(painterResource(android.R.drawable.ic_menu_share), contentDescription = stringResource(R.string.copy))
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (index > 0) {
                            TextButton(onClick = { swapOrder(steps[index], steps[index - 1]) }) {
                                Text(stringResource(R.string.move_up))
                            }
                        }
                        if (index < steps.size - 1) {
                            TextButton(onClick = { swapOrder(steps[index], steps[index + 1]) }) {
                                Text(stringResource(R.string.move_down))
                            }
                        }
                        TextButton(onClick = { startEdit(step) }) {
                            Text(stringResource(R.string.edit))
                        }
                        TextButton(onClick = {
                            scope.launch(Dispatchers.IO) {
                                viewModel.emergencyPlanStepRepository.deleteById(step.id)
                                loadSteps()
                            }
                        }) { Text(stringResource(R.string.delete)) }
                    }
                }
            }
        }
    }
}
