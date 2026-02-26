package de.idrinth.habitevaluator.android.ui.screens

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.NavController
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.BuildConfig
import de.idrinth.habitevaluator.android.NotificationHelper
import de.idrinth.habitevaluator.android.R
import de.idrinth.habitevaluator.android.ReminderScheduler
import de.idrinth.habitevaluator.android.SettingsConstants
import de.idrinth.habitevaluator.android.ui.navigation.Screen
import de.idrinth.habitevaluator.shared.util.ReminderScheduleCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
    var activityLogVisible by remember { mutableStateOf(prefs.getBoolean(SettingsConstants.KEY_MODULE_ACTIVITY_LOG_VISIBLE, true)) }
    var dayPlannerVisible by remember { mutableStateOf(prefs.getBoolean(SettingsConstants.KEY_MODULE_DAY_PLANNER_VISIBLE, true)) }

    var backupEnabled by remember { mutableStateOf(prefs.getBoolean(SettingsConstants.KEY_BACKUP_ENABLED, false)) }
    var backupPassword by remember { mutableStateOf(prefs.getString(SettingsConstants.KEY_BACKUP_PASSWORD, "") ?: "") }
    var backupPasswordConfirm by remember { mutableStateOf(prefs.getString(SettingsConstants.KEY_BACKUP_PASSWORD, "") ?: "") }
    var backupLocationUri by remember { mutableStateOf(prefs.getString(SettingsConstants.KEY_BACKUP_LOCATION_URI, "") ?: "") }
    var backupPasswordError by remember { mutableStateOf("") }

    var sleepReminderEnabled by remember { mutableStateOf(prefs.getBoolean(SettingsConstants.KEY_SLEEP_REMINDER_ENABLED, false)) }
    var sleepReminderTime by remember { mutableStateOf(prefs.getString(SettingsConstants.KEY_SLEEP_REMINDER_TIME, SettingsConstants.DEFAULT_SLEEP_REMINDER_TIME) ?: SettingsConstants.DEFAULT_SLEEP_REMINDER_TIME) }
    var diaryReminderEnabled by remember { mutableStateOf(prefs.getBoolean(SettingsConstants.KEY_DIARY_REMINDER_ENABLED, false)) }
    var diaryReminderTime by remember { mutableStateOf(prefs.getString(SettingsConstants.KEY_DIARY_REMINDER_TIME, SettingsConstants.DEFAULT_DIARY_REMINDER_TIME) ?: SettingsConstants.DEFAULT_DIARY_REMINDER_TIME) }
    var gratitudeReminderEnabled by remember { mutableStateOf(prefs.getBoolean(SettingsConstants.KEY_GRATITUDE_REMINDER_ENABLED, false)) }
    var gratitudeReminderTime by remember { mutableStateOf(prefs.getString(SettingsConstants.KEY_GRATITUDE_REMINDER_TIME, SettingsConstants.DEFAULT_GRATITUDE_REMINDER_TIME) ?: SettingsConstants.DEFAULT_GRATITUDE_REMINDER_TIME) }
    var emotionReminderEnabled by remember { mutableStateOf(prefs.getBoolean(SettingsConstants.KEY_EMOTION_REMINDER_ENABLED, false)) }
    var emotionReminderCount by remember { mutableFloatStateOf(prefs.getInt(SettingsConstants.KEY_EMOTION_REMINDER_COUNT, SettingsConstants.DEFAULT_EMOTION_REMINDER_COUNT).toFloat()) }
    var wakingHoursStart by remember { mutableStateOf(prefs.getString(SettingsConstants.KEY_WAKING_HOURS_START, SettingsConstants.DEFAULT_WAKING_HOURS_START) ?: SettingsConstants.DEFAULT_WAKING_HOURS_START) }
    var wakingHoursEnd by remember { mutableStateOf(prefs.getString(SettingsConstants.KEY_WAKING_HOURS_END, SettingsConstants.DEFAULT_WAKING_HOURS_END) ?: SettingsConstants.DEFAULT_WAKING_HOURS_END) }

    val permissionHandler = remember { NotificationHelper.permissionHandler() }
    var pendingReminderToggle by remember { mutableStateOf<String?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            when (pendingReminderToggle) {
                "sleep" -> sleepReminderEnabled = true
                "diary" -> diaryReminderEnabled = true
                "gratitude" -> gratitudeReminderEnabled = true
                "emotion" -> emotionReminderEnabled = true
            }
        } else {
            Toast.makeText(context, R.string.notification_permission_denied, Toast.LENGTH_LONG).show()
        }
        pendingReminderToggle = null
    }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            backupLocationUri = uri.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            "Version ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

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

        // Backup settings
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.backup_settings), style = MaterialTheme.typography.titleMedium)
                LabeledSwitch(stringResource(R.string.backup_enabled_label), backupEnabled) { backupEnabled = it }
                if (backupEnabled) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = backupPassword,
                        onValueChange = {
                            backupPassword = it
                            backupPasswordError = ""
                        },
                        label = { Text(stringResource(R.string.backup_password_hint)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = backupPasswordConfirm,
                        onValueChange = {
                            backupPasswordConfirm = it
                            backupPasswordError = ""
                        },
                        label = { Text(stringResource(R.string.backup_password_confirm_hint)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (backupPasswordError.isNotEmpty()) {
                        Text(
                            backupPasswordError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.backup_location_label), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        if (backupLocationUri.isEmpty()) stringResource(R.string.backup_location_default)
                        else DocumentFile.fromTreeUri(context, android.net.Uri.parse(backupLocationUri))?.name ?: backupLocationUri,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = { folderPickerLauncher.launch(null) }) {
                            Text(stringResource(R.string.backup_location_choose))
                        }
                        if (backupLocationUri.isNotEmpty()) {
                            OutlinedButton(onClick = { backupLocationUri = "" }) {
                                Text(stringResource(R.string.backup_location_default))
                            }
                        }
                    }
                }
            }
        }

        Button(onClick = { navController.navigate(Screen.Backup.route) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.backup_title))
        }

        // Reminder settings
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.reminder_settings), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                LabeledSwitch(stringResource(R.string.reminder_sleep_label), sleepReminderEnabled) {
                    if (it) {
                        val permName = permissionHandler.permissionName()
                        if (permName != null && !permissionHandler.hasPermission(context)) {
                            pendingReminderToggle = "sleep"
                            notificationPermissionLauncher.launch(permName)
                        } else {
                            sleepReminderEnabled = true
                        }
                    } else {
                        sleepReminderEnabled = false
                    }
                }
                if (sleepReminderEnabled) {
                    val sleepHm = ReminderScheduleCalculator.parseTime(sleepReminderTime)
                    OutlinedButton(onClick = {
                        TimePickerDialog(context, { _, h, m ->
                            sleepReminderTime = String.format("%02d:%02d", h, m)
                        }, sleepHm[0], sleepHm[1], true).show()
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.reminder_sleep_time_label) + ": $sleepReminderTime")
                    }
                }

                Spacer(Modifier.height(4.dp))
                LabeledSwitch(stringResource(R.string.reminder_diary_label), diaryReminderEnabled) {
                    if (it) {
                        val permName = permissionHandler.permissionName()
                        if (permName != null && !permissionHandler.hasPermission(context)) {
                            pendingReminderToggle = "diary"
                            notificationPermissionLauncher.launch(permName)
                        } else {
                            diaryReminderEnabled = true
                        }
                    } else {
                        diaryReminderEnabled = false
                    }
                }
                if (diaryReminderEnabled) {
                    val diaryHm = ReminderScheduleCalculator.parseTime(diaryReminderTime)
                    OutlinedButton(onClick = {
                        TimePickerDialog(context, { _, h, m ->
                            diaryReminderTime = String.format("%02d:%02d", h, m)
                        }, diaryHm[0], diaryHm[1], true).show()
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.reminder_diary_time_label) + ": $diaryReminderTime")
                    }
                }

                Spacer(Modifier.height(4.dp))
                LabeledSwitch(stringResource(R.string.reminder_gratitude_label), gratitudeReminderEnabled) {
                    if (it) {
                        val permName = permissionHandler.permissionName()
                        if (permName != null && !permissionHandler.hasPermission(context)) {
                            pendingReminderToggle = "gratitude"
                            notificationPermissionLauncher.launch(permName)
                        } else {
                            gratitudeReminderEnabled = true
                        }
                    } else {
                        gratitudeReminderEnabled = false
                    }
                }
                if (gratitudeReminderEnabled) {
                    val gratitudeHm = ReminderScheduleCalculator.parseTime(gratitudeReminderTime)
                    OutlinedButton(onClick = {
                        TimePickerDialog(context, { _, h, m ->
                            gratitudeReminderTime = String.format("%02d:%02d", h, m)
                        }, gratitudeHm[0], gratitudeHm[1], true).show()
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.reminder_gratitude_time_label) + ": $gratitudeReminderTime")
                    }
                }

                Spacer(Modifier.height(4.dp))
                LabeledSwitch(stringResource(R.string.reminder_emotion_label), emotionReminderEnabled) {
                    if (it) {
                        val permName = permissionHandler.permissionName()
                        if (permName != null && !permissionHandler.hasPermission(context)) {
                            pendingReminderToggle = "emotion"
                            notificationPermissionLauncher.launch(permName)
                        } else {
                            emotionReminderEnabled = true
                        }
                    } else {
                        emotionReminderEnabled = false
                    }
                }
                if (emotionReminderEnabled) {
                    Text(
                        stringResource(R.string.reminder_emotion_count_label, emotionReminderCount.roundToInt()),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Slider(
                        value = emotionReminderCount,
                        onValueChange = { emotionReminderCount = it },
                        valueRange = 1f..10f,
                        steps = 8,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        stringResource(R.string.reminder_waking_hours_label),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val startHm = ReminderScheduleCalculator.parseTime(wakingHoursStart)
                        OutlinedButton(onClick = {
                            TimePickerDialog(context, { _, h, m ->
                                wakingHoursStart = String.format("%02d:%02d", h, m)
                            }, startHm[0], startHm[1], true).show()
                        }, modifier = Modifier.weight(1f)) {
                            Text(wakingHoursStart)
                        }
                        val endHm = ReminderScheduleCalculator.parseTime(wakingHoursEnd)
                        OutlinedButton(onClick = {
                            TimePickerDialog(context, { _, h, m ->
                                wakingHoursEnd = String.format("%02d:%02d", h, m)
                            }, endHm[0], endHm[1], true).show()
                        }, modifier = Modifier.weight(1f)) {
                            Text(wakingHoursEnd)
                        }
                    }
                }
            }
        }

        // Module visibility
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.module_visibility), style = MaterialTheme.typography.titleMedium)
                LabeledSwitch(stringResource(R.string.diary), diaryVisible) { diaryVisible = it }
                LabeledSwitch(stringResource(R.string.sleep_tracking), sleepVisible) { sleepVisible = it }
                LabeledSwitch(stringResource(R.string.emotions), emotionsVisible) { emotionsVisible = it }
                LabeledSwitch(stringResource(R.string.module_points), pointsVisible) { pointsVisible = it }
                LabeledSwitch(stringResource(R.string.statistics), statsVisible) { statsVisible = it }
                LabeledSwitch(stringResource(R.string.module_food_log), foodLogVisible) { foodLogVisible = it }
                LabeledSwitch(stringResource(R.string.module_sport_log), sportLogVisible) { sportLogVisible = it }
                LabeledSwitch(stringResource(R.string.module_medication), medicationVisible) { medicationVisible = it }
                LabeledSwitch(stringResource(R.string.module_backup), backupVisible) { backupVisible = it }
                LabeledSwitch(stringResource(R.string.module_pdf_export), pdfExportVisible) { pdfExportVisible = it }
                LabeledSwitch(stringResource(R.string.module_activity_log), activityLogVisible) { activityLogVisible = it }
                LabeledSwitch(stringResource(R.string.module_day_planner), dayPlannerVisible) { dayPlannerVisible = it }
            }
        }

        // Save
        Button(
            onClick = {
                if (backupEnabled) {
                    if (backupPassword.isEmpty()) {
                        backupPasswordError = context.getString(R.string.backup_password_required)
                        return@Button
                    }
                    if (backupPassword != backupPasswordConfirm) {
                        backupPasswordError = context.getString(R.string.backup_passwords_mismatch)
                        return@Button
                    }
                }
                prefs.edit()
                    .putString(SettingsConstants.KEY_STORAGE_MODE, storageMode)
                    .putString(SettingsConstants.KEY_API_URL, apiUrl)
                    .putString(SettingsConstants.KEY_API_USERNAME, apiUsername)
                    .putString(SettingsConstants.KEY_API_PASSWORD, apiPassword)
                    .putString(SettingsConstants.KEY_THEME_MODE, themeMode)
                    .putString(SettingsConstants.KEY_LANGUAGE, language)
                    .putBoolean(SettingsConstants.KEY_BACKUP_ENABLED, backupEnabled)
                    .putString(SettingsConstants.KEY_BACKUP_PASSWORD, if (backupEnabled) backupPassword else "")
                    .putString(SettingsConstants.KEY_BACKUP_LOCATION_URI, if (backupEnabled) backupLocationUri else "")
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
                    .putBoolean(SettingsConstants.KEY_MODULE_ACTIVITY_LOG_VISIBLE, activityLogVisible)
                    .putBoolean(SettingsConstants.KEY_MODULE_DAY_PLANNER_VISIBLE, dayPlannerVisible)
                    .putBoolean(SettingsConstants.KEY_SLEEP_REMINDER_ENABLED, sleepReminderEnabled)
                    .putString(SettingsConstants.KEY_SLEEP_REMINDER_TIME, sleepReminderTime)
                    .putBoolean(SettingsConstants.KEY_DIARY_REMINDER_ENABLED, diaryReminderEnabled)
                    .putString(SettingsConstants.KEY_DIARY_REMINDER_TIME, diaryReminderTime)
                    .putBoolean(SettingsConstants.KEY_GRATITUDE_REMINDER_ENABLED, gratitudeReminderEnabled)
                    .putString(SettingsConstants.KEY_GRATITUDE_REMINDER_TIME, gratitudeReminderTime)
                    .putBoolean(SettingsConstants.KEY_EMOTION_REMINDER_ENABLED, emotionReminderEnabled)
                    .putInt(SettingsConstants.KEY_EMOTION_REMINDER_COUNT, emotionReminderCount.roundToInt())
                    .putString(SettingsConstants.KEY_WAKING_HOURS_START, wakingHoursStart)
                    .putString(SettingsConstants.KEY_WAKING_HOURS_END, wakingHoursEnd)
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
