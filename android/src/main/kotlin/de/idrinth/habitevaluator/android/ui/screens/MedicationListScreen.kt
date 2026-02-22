package de.idrinth.habitevaluator.android.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.R
import de.idrinth.habitevaluator.shared.model.Medication
import de.idrinth.habitevaluator.shared.model.MedicationProvisionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationListScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val localUser by viewModel.localUser.collectAsState()
    val medications by viewModel.medications.collectAsState()

    var formExpanded by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var provisionType by remember { mutableStateOf(MedicationProvisionType.PILL) }
    var wikipediaLink by remember { mutableStateOf("") }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    // Edit link dialog state
    var editingMedication by remember { mutableStateOf<Medication?>(null) }
    var editLinkValue by remember { mutableStateOf("") }

    fun loadMedications() {
        viewModel.loadMedications()
    }

    LaunchedEffect(localUser) { loadMedications() }

    fun resetForm() {
        name = ""
        provisionType = MedicationProvisionType.PILL
        wikipediaLink = ""
        formExpanded = false
    }

    fun addMedication() {
        if (name.isBlank()) {
            Toast.makeText(context, R.string.medication_name_required, Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch(Dispatchers.IO) {
            val med = Medication()
            med.id = UUID.randomUUID().toString()
            med.name = name.trim()
            med.provisionType = provisionType
            med.wikipediaLink = wikipediaLink.ifBlank { null }
            med.user = localUser
            viewModel.medicationRepository.save(med)
            withContext(Dispatchers.Main) { resetForm() }
            loadMedications()
        }
    }

    val provisionTypes = MedicationProvisionType.entries
    val provisionLabels = mapOf(
        MedicationProvisionType.PILL to stringResource(R.string.pill),
        MedicationProvisionType.LIQUID_DROPS to stringResource(R.string.liquid_drops),
        MedicationProvisionType.LIQUID_ML to stringResource(R.string.liquid_ml)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(stringResource(R.string.medication_list), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))

        // Toggle form
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { formExpanded = !formExpanded }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.add_medication), style = MaterialTheme.typography.titleMedium)
                Icon(
                    painter = painterResource(
                        if (formExpanded) android.R.drawable.arrow_up_float
                        else android.R.drawable.arrow_down_float
                    ),
                    contentDescription = null
                )
            }
        }

        AnimatedVisibility(visible = formExpanded) {
            Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.medication_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    ExposedDropdownMenuBox(
                        expanded = typeDropdownExpanded,
                        onExpandedChange = { typeDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = provisionLabels[provisionType] ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.provision_type)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = typeDropdownExpanded,
                            onDismissRequest = { typeDropdownExpanded = false }
                        ) {
                            provisionTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(provisionLabels[type] ?: type.name) },
                                    onClick = {
                                        provisionType = type
                                        typeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = wikipediaLink,
                        onValueChange = { wikipediaLink = it },
                        label = { Text(stringResource(R.string.wikipedia_link)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(onClick = { addMedication() }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.add_medication))
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(medications, key = { it.id }) { med ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(med.name ?: "", style = MaterialTheme.typography.bodyLarge)
                        Text(provisionLabels[med.provisionType] ?: "", style = MaterialTheme.typography.bodySmall)
                        med.wikipediaLink?.let {
                            if (it.isNotBlank()) Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = {
                                editingMedication = med
                                editLinkValue = med.wikipediaLink ?: ""
                            }) { Text(stringResource(R.string.edit_link)) }
                            TextButton(onClick = {
                                scope.launch(Dispatchers.IO) {
                                    viewModel.medicationRepository.deleteById(med.id)
                                    loadMedications()
                                }
                            }) { Text(stringResource(R.string.delete)) }
                        }
                    }
                }
            }
        }
    }

    // Edit Wikipedia link dialog
    if (editingMedication != null) {
        AlertDialog(
            onDismissRequest = { editingMedication = null },
            title = { Text(stringResource(R.string.edit_wikipedia_link)) },
            text = {
                OutlinedTextField(
                    value = editLinkValue,
                    onValueChange = { editLinkValue = it },
                    label = { Text(stringResource(R.string.wikipedia_link)) },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val med = editingMedication ?: return@TextButton
                    scope.launch(Dispatchers.IO) {
                        med.wikipediaLink = editLinkValue.ifBlank { null }
                        viewModel.medicationRepository.save(med)
                        loadMedications()
                    }
                    editingMedication = null
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { editingMedication = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
