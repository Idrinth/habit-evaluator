package de.idrinth.habitevaluator.android.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.R
import de.idrinth.habitevaluator.android.ReminderScheduler
import de.idrinth.habitevaluator.android.SettingsConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: AppViewModel, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences(SettingsConstants.PREFS_NAME, Context.MODE_PRIVATE) }

    var storageMode by remember { mutableStateOf(prefs.getString(SettingsConstants.KEY_STORAGE_MODE, SettingsConstants.MODE_LOCAL) ?: SettingsConstants.MODE_LOCAL) }
    var apiUrl by remember { mutableStateOf(prefs.getString(SettingsConstants.KEY_API_URL, "") ?: "") }
    var apiUsername by remember { mutableStateOf(prefs.getString(SettingsConstants.KEY_API_USERNAME, "") ?: "") }
    var apiPassword by remember { mutableStateOf(prefs.getString(SettingsConstants.KEY_API_PASSWORD, "") ?: "") }
    var themeMode by remember { mutableStateOf(prefs.getString(SettingsConstants.KEY_THEME_MODE, SettingsConstants.THEME_SYSTEM) ?: SettingsConstants.THEME_SYSTEM) }
    var language by remember { mutableStateOf(prefs.getString(SettingsConstants.KEY_LANGUAGE, SettingsConstants.LANGUAGE_SYSTEM) ?: SettingsConstants.LANGUAGE_SYSTEM) }
    var diaryVisible by remember { mutableStateOf(prefs.getBoolean(SettingsConstants.KEY_MODULE_DIARY_VISIBLE, true)) }
    var sleepVisible by remember { mutableStateOf(prefs.getBoolean(SettingsConstants.KEY_MODULE_SLEEP_VISIBLE, true)) }
    var emotionsVisible by remember { mutableStateOf(prefs.getBoolean(SettingsConstants.KEY_MODULE_EMOTIONS_VISIBLE, true)) }
    var pointsVisible by remember { mutableStateOf(prefs.getBoolean(SettingsConstants.KEY_MODULE_POINTS_VISIBLE, true)) }
    var statsVisible by remember { mutableStateOf(prefs.getBoolean(SettingsConstants.KEY_MODULE_STATISTICS_VISIBLE, true)) }
    var foodLogVisible by remember { mutableStateOf(prefs.getBoolean(SettingsConstants.KEY_MODULE_FOOD_LOG_VISIBLE, true)) }
    var sportLogVisible by remember { mutableStateOf(prefs.getBoolean(SettingsConstants.KEY_MODULE_SPORT_LOG_VISIBLE, true)) }
    var medicationVisible by remember { mutableStateOf(prefs.getBoolean(SettingsConstants.KEY_MODULE_MEDICATION_VISIBLE, true)) }
    var backupVisible by remember { mutableStateOf(prefs.getBoolean(SettingsConstants.KEY_MODULE_BACKUP_VISIBLE, true)) }
    var pdfExportVisible by remember { mutableStateOf(prefs.getBoolean(SettingsConstants.KEY_MODULE_PDF_EXPORT_VISIBLE, true)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineMedium)

        // Storage mode
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.storage_mode), style = MaterialTheme.typography.titleMedium)
                RadioOption(SettingsConstants.MODE_LOCAL, stringResource(R.string.local_storage), storageMode) { storageMode = it }
                RadioOption(SettingsConstants.MODE_REMOTE, stringResource(R.string.remote_storage), storageMode) { storageMode = it }
                if (storageMode == SettingsConstants.MODE_REMOTE) {
                    OutlinedTextField(value = apiUrl, onValueChange = { apiUrl = it },
                        label = { Text(stringResource(R.string.api_url)) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = apiUsername, onValueChange = { apiUsername = it },
                        label = { Text(stringResource(R.string.username)) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = apiPassword, onValueChange = { apiPassword = it },
                        label = { Text(stringResource(R.string.password)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth())
                }
            }
        }

        // Theme
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.theme), style = MaterialTheme.typography.titleMedium)
                RadioOption(SettingsConstants.THEME_SYSTEM, stringResource(R.string.system_default), themeMode) { themeMode = it }
                RadioOption(SettingsConstants.THEME_LIGHT, stringResource(R.string.light), themeMode) { themeMode = it }
                RadioOption(SettingsConstants.THEME_DARK, stringResource(R.string.dark), themeMode) { themeMode = it }
            }
        }

        // Language
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.language), style = MaterialTheme.typography.titleMedium)
                RadioOption(SettingsConstants.LANGUAGE_SYSTEM, stringResource(R.string.system_default), language) { language = it }
                RadioOption(SettingsConstants.LANGUAGE_EN, "English", language) { language = it }
                RadioOption(SettingsConstants.LANGUAGE_DE, "Deutsch", language) { language = it }
                RadioOption(SettingsConstants.LANGUAGE_ES, "Español", language) { language = it }
                RadioOption(SettingsConstants.LANGUAGE_FR, "Français", language) { language = it }
            }
        }

        // Module visibility
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.module_visibility), style = MaterialTheme.typography.titleMedium)
                LabeledSwitch(stringResource(R.string.module_diary), diaryVisible) { diaryVisible = it }
                LabeledSwitch(stringResource(R.string.module_sleep), sleepVisible) { sleepVisible = it }
                LabeledSwitch(stringResource(R.string.module_emotions), emotionsVisible) { emotionsVisible = it }
                LabeledSwitch(stringResource(R.string.module_points), pointsVisible) { pointsVisible = it }
                LabeledSwitch(stringResource(R.string.module_statistics), statsVisible) { statsVisible = it }
                LabeledSwitch(stringResource(R.string.module_food_log), foodLogVisible) { foodLogVisible = it }
                LabeledSwitch(stringResource(R.string.module_sport_log), sportLogVisible) { sportLogVisible = it }
                LabeledSwitch(stringResource(R.string.module_medication), medicationVisible) { medicationVisible = it }
                LabeledSwitch(stringResource(R.string.module_backup), backupVisible) { backupVisible = it }
                LabeledSwitch(stringResource(R.string.module_pdf_export), pdfExportVisible) { pdfExportVisible = it }
            }
        }

        // Save
        Button(
            onClick = {
                prefs.edit()
                    .putString(SettingsConstants.KEY_STORAGE_MODE, storageMode)
                    .putString(SettingsConstants.KEY_API_URL, apiUrl)
                    .putString(SettingsConstants.KEY_API_USERNAME, apiUsername)
                    .putString(SettingsConstants.KEY_API_PASSWORD, apiPassword)
                    .putString(SettingsConstants.KEY_THEME_MODE, themeMode)
                    .putString(SettingsConstants.KEY_LANGUAGE, language)
                    .putBoolean(SettingsConstants.KEY_MODULE_DIARY_VISIBLE, diaryVisible)
                    .putBoolean(SettingsConstants.KEY_MODULE_SLEEP_VISIBLE, sleepVisible)
                    .putBoolean(SettingsConstants.KEY_MODULE_EMOTIONS_VISIBLE, emotionsVisible)
                    .putBoolean(SettingsConstants.KEY_MODULE_POINTS_VISIBLE, pointsVisible)
                    .putBoolean(SettingsConstants.KEY_MODULE_STATISTICS_VISIBLE, statsVisible)
                    .putBoolean(SettingsConstants.KEY_MODULE_FOOD_LOG_VISIBLE, foodLogVisible)
                    .putBoolean(SettingsConstants.KEY_MODULE_SPORT_LOG_VISIBLE, sportLogVisible)
                    .putBoolean(SettingsConstants.KEY_MODULE_MEDICATION_VISIBLE, medicationVisible)
                    .putBoolean(SettingsConstants.KEY_MODULE_BACKUP_VISIBLE, backupVisible)
                    .putBoolean(SettingsConstants.KEY_MODULE_PDF_EXPORT_VISIBLE, pdfExportVisible)
                    .apply()
                SettingsConstants.applyThemeMode(themeMode)
                SettingsConstants.applyLanguage(language)
                ReminderScheduler.rescheduleAll(context)
                viewModel.initializeStorage()
                Toast.makeText(context, R.string.settings_saved, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.save)) }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun RadioOption(value: String, label: String, selected: String, onSelect: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = value == selected, onClick = { onSelect(value) })
        Text(label, modifier = Modifier.padding(start = 4.dp))
    }
}
