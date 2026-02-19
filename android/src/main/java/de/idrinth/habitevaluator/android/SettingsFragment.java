package de.idrinth.habitevaluator.android;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.idrinth.habitevaluator.android.databinding.FragmentSettingsBinding;
import de.idrinth.habitevaluator.shared.api.ApiClient;
import de.idrinth.habitevaluator.shared.api.StorageConfig;
import de.idrinth.habitevaluator.shared.backup.BackupException;
import de.idrinth.habitevaluator.shared.backup.BackupService;
import de.idrinth.habitevaluator.shared.backup.HezBackupService;
import de.idrinth.habitevaluator.shared.backup.MergeResult;
import de.idrinth.habitevaluator.shared.backup.RestoreOptions;

public class SettingsFragment extends Fragment {

    static final int DEFAULT_HOUR = 8;
    static final int DEFAULT_MINUTE = 0;
    static final String TIME_SEPARATOR = ":";

    private FragmentSettingsBinding binding;
    private final HezBackupService hezBackupService = new HezBackupService();
    private byte[] pendingHezBackupData;
    private ActivityResultLauncher<Intent> createDocumentLauncher;
    private ActivityResultLauncher<Intent> openDocumentLauncher;
    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null && pendingHezBackupData != null) {
                            saveHezBackupToUri(uri, pendingHezBackupData);
                        }
                    }
                    pendingHezBackupData = null;
                });

        openDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            showPasswordDialogAndRestoreFromFile(uri);
                        }
                    }
                });

        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (!isGranted) {
                        Toast.makeText(requireContext(),
                                R.string.notification_permission_denied,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadSettings();
        setupListeners();
    }

    private void loadSettings() {
        SharedPreferences prefs = requireContext().getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        String mode = prefs.getString(SettingsActivity.KEY_STORAGE_MODE, SettingsActivity.MODE_LOCAL);
        String url = prefs.getString(SettingsActivity.KEY_API_URL, StorageConfig.DEFAULT_API_BASE_URL);
        String username = prefs.getString(SettingsActivity.KEY_API_USERNAME, "");
        String password = prefs.getString(SettingsActivity.KEY_API_PASSWORD, "");
        String themeMode = prefs.getString(SettingsActivity.KEY_THEME_MODE, SettingsActivity.THEME_SYSTEM);
        String language = prefs.getString(SettingsActivity.KEY_LANGUAGE, SettingsActivity.LANGUAGE_SYSTEM);

        if (SettingsActivity.MODE_REMOTE.equals(mode)) {
            binding.remoteRadio.setChecked(true);
            binding.remoteSettingsPanel.setVisibility(View.VISIBLE);
        } else {
            binding.localRadio.setChecked(true);
            binding.remoteSettingsPanel.setVisibility(View.GONE);
        }

        if (SettingsActivity.THEME_LIGHT.equals(themeMode)) {
            binding.themeLightRadio.setChecked(true);
        } else if (SettingsActivity.THEME_DARK.equals(themeMode)) {
            binding.themeDarkRadio.setChecked(true);
        } else {
            binding.themeSystemRadio.setChecked(true);
        }

        if (SettingsActivity.LANGUAGE_EN.equals(language)) {
            binding.languageEnRadio.setChecked(true);
        } else if (SettingsActivity.LANGUAGE_DE.equals(language)) {
            binding.languageDeRadio.setChecked(true);
        } else if (SettingsActivity.LANGUAGE_ES.equals(language)) {
            binding.languageEsRadio.setChecked(true);
        } else if (SettingsActivity.LANGUAGE_FR.equals(language)) {
            binding.languageFrRadio.setChecked(true);
        } else {
            binding.languageSystemRadio.setChecked(true);
        }

        String fontSize = prefs.getString(SettingsActivity.KEY_FONT_SIZE, SettingsActivity.FONT_SIZE_SYSTEM);
        if (SettingsActivity.FONT_SIZE_XS.equals(fontSize)) {
            binding.fontSizeXsRadio.setChecked(true);
        } else if (SettingsActivity.FONT_SIZE_SMALL.equals(fontSize)) {
            binding.fontSizeSmallRadio.setChecked(true);
        } else if (SettingsActivity.FONT_SIZE_NORMAL.equals(fontSize)) {
            binding.fontSizeNormalRadio.setChecked(true);
        } else if (SettingsActivity.FONT_SIZE_LARGE.equals(fontSize)) {
            binding.fontSizeLargeRadio.setChecked(true);
        } else {
            binding.fontSizeSystemRadio.setChecked(true);
        }

        binding.apiUrlInput.setText(url);
        binding.apiUsernameInput.setText(username);
        binding.apiPasswordInput.setText(password);

        boolean customTranslations = prefs.getBoolean(SettingsActivity.KEY_CUSTOM_TRANSLATIONS, false);
        binding.customTranslationsSwitch.setChecked(customTranslations);

        // Reminder settings
        boolean sleepReminder = prefs.getBoolean(SettingsActivity.KEY_SLEEP_REMINDER_ENABLED, false);
        binding.sleepReminderSwitch.setChecked(sleepReminder);
        binding.sleepReminderPanel.setVisibility(sleepReminder ? View.VISIBLE : View.GONE);
        String sleepTime = prefs.getString(SettingsActivity.KEY_SLEEP_REMINDER_TIME,
                SettingsActivity.DEFAULT_SLEEP_REMINDER_TIME);
        binding.sleepReminderTimeButton.setText(sleepTime);

        boolean diaryReminder = prefs.getBoolean(SettingsActivity.KEY_DIARY_REMINDER_ENABLED, false);
        binding.diaryReminderSwitch.setChecked(diaryReminder);
        binding.diaryReminderPanel.setVisibility(diaryReminder ? View.VISIBLE : View.GONE);
        String diaryTime = prefs.getString(SettingsActivity.KEY_DIARY_REMINDER_TIME,
                SettingsActivity.DEFAULT_DIARY_REMINDER_TIME);
        binding.diaryReminderTimeButton.setText(diaryTime);

        boolean emotionReminder = prefs.getBoolean(SettingsActivity.KEY_EMOTION_REMINDER_ENABLED, false);
        binding.emotionReminderSwitch.setChecked(emotionReminder);
        binding.emotionReminderPanel.setVisibility(emotionReminder ? View.VISIBLE : View.GONE);
        int emotionCount = prefs.getInt(SettingsActivity.KEY_EMOTION_REMINDER_COUNT,
                SettingsActivity.DEFAULT_EMOTION_REMINDER_COUNT);
        binding.emotionReminderCountSeekBar.setProgress(emotionCount);
        binding.emotionReminderCountLabel.setText(
                getString(R.string.reminder_emotion_count_label, emotionCount));
        String wakingStart = prefs.getString(SettingsActivity.KEY_WAKING_HOURS_START,
                SettingsActivity.DEFAULT_WAKING_HOURS_START);
        String wakingEnd = prefs.getString(SettingsActivity.KEY_WAKING_HOURS_END,
                SettingsActivity.DEFAULT_WAKING_HOURS_END);
        binding.wakingHoursStartButton.setText(wakingStart);
        binding.wakingHoursEndButton.setText(wakingEnd);

        boolean backupEnabled = prefs.getBoolean(SettingsActivity.KEY_BACKUP_ENABLED, false);
        binding.backupEnabledSwitch.setChecked(backupEnabled);
        binding.backupSettingsPanel.setVisibility(backupEnabled ? View.VISIBLE : View.GONE);
        String backupPassword = prefs.getString(SettingsActivity.KEY_BACKUP_PASSWORD, "");
        binding.backupPasswordInput.setText(backupPassword);
        binding.backupPasswordConfirmInput.setText(backupPassword);

        // Module visibility settings
        binding.diaryVisibleSwitch.setChecked(
                prefs.getBoolean(SettingsActivity.KEY_MODULE_DIARY_VISIBLE, true));
        binding.sleepVisibleSwitch.setChecked(
                prefs.getBoolean(SettingsActivity.KEY_MODULE_SLEEP_VISIBLE, true));
        binding.emotionsVisibleSwitch.setChecked(
                prefs.getBoolean(SettingsActivity.KEY_MODULE_EMOTIONS_VISIBLE, true));
        binding.pointsVisibleSwitch.setChecked(
                prefs.getBoolean(SettingsActivity.KEY_MODULE_POINTS_VISIBLE, true));
        binding.statisticsVisibleSwitch.setChecked(
                prefs.getBoolean(SettingsActivity.KEY_MODULE_STATISTICS_VISIBLE, true));
        binding.foodLogVisibleSwitch.setChecked(
                prefs.getBoolean(SettingsActivity.KEY_MODULE_FOOD_LOG_VISIBLE, true));
        binding.sportLogVisibleSwitch.setChecked(
                prefs.getBoolean(SettingsActivity.KEY_MODULE_SPORT_LOG_VISIBLE, true));
        binding.medicationVisibleSwitch.setChecked(
                prefs.getBoolean(SettingsActivity.KEY_MODULE_MEDICATION_VISIBLE, true));
        binding.backupVisibleSwitch.setChecked(
                prefs.getBoolean(SettingsActivity.KEY_MODULE_BACKUP_VISIBLE, true));
        binding.pdfExportVisibleSwitch.setChecked(
                prefs.getBoolean(SettingsActivity.KEY_MODULE_PDF_EXPORT_VISIBLE, true));
    }

    private void setupListeners() {
        binding.storageModeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.remoteRadio) {
                binding.remoteSettingsPanel.setVisibility(View.VISIBLE);
            } else {
                binding.remoteSettingsPanel.setVisibility(View.GONE);
            }
        });

        binding.backupEnabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.backupSettingsPanel.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        binding.sleepReminderSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                binding.sleepReminderPanel.setVisibility(isChecked ? View.VISIBLE : View.GONE));
        binding.diaryReminderSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                binding.diaryReminderPanel.setVisibility(isChecked ? View.VISIBLE : View.GONE));
        binding.emotionReminderSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                binding.emotionReminderPanel.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        binding.sleepReminderTimeButton.setOnClickListener(v ->
                showTimePicker(binding.sleepReminderTimeButton.getText().toString(),
                        (h, m) -> binding.sleepReminderTimeButton.setText(
                                String.format(java.util.Locale.US, "%02d:%02d", h, m))));
        binding.diaryReminderTimeButton.setOnClickListener(v ->
                showTimePicker(binding.diaryReminderTimeButton.getText().toString(),
                        (h, m) -> binding.diaryReminderTimeButton.setText(
                                String.format(java.util.Locale.US, "%02d:%02d", h, m))));
        binding.wakingHoursStartButton.setOnClickListener(v ->
                showTimePicker(binding.wakingHoursStartButton.getText().toString(),
                        (h, m) -> binding.wakingHoursStartButton.setText(
                                String.format(java.util.Locale.US, "%02d:%02d", h, m))));
        binding.wakingHoursEndButton.setOnClickListener(v ->
                showTimePicker(binding.wakingHoursEndButton.getText().toString(),
                        (h, m) -> binding.wakingHoursEndButton.setText(
                                String.format(java.util.Locale.US, "%02d:%02d", h, m))));

        binding.emotionReminderCountSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        binding.emotionReminderCountLabel.setText(
                                getString(R.string.reminder_emotion_count_label, progress));
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) { }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) { }
                });

        binding.testConnectionButton.setOnClickListener(v -> testConnection());
        binding.saveSettingsButton.setOnClickListener(v -> saveSettings());
        binding.restoreBackupButton.setOnClickListener(v -> restoreBackup());
        binding.downloadBackupButton.setOnClickListener(v -> downloadBackup());
        binding.restoreFromFileButton.setOnClickListener(v -> restoreFromFile());
    }

    private void testConnection() {
        String url = binding.apiUrlInput.getText().toString().trim();
        String username = binding.apiUsernameInput.getText().toString().trim();
        String password = binding.apiPasswordInput.getText().toString();

        if (!areRemoteFieldsComplete(url, username, password)) {
            binding.connectionStatusText.setText(R.string.fill_all_fields);
            binding.connectionStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
            return;
        }

        if (!StorageConfig.isUrlSecure(url)) {
            binding.connectionStatusText.setText(R.string.https_required);
            binding.connectionStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
            return;
        }

        binding.connectionStatusText.setText(R.string.testing_connection);
        binding.connectionStatusText.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

        new Thread(() -> {
            try {
                ApiClient client = new ApiClient(url);
                boolean success = client.login(username, password);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (success) {
                            binding.connectionStatusText.setText(R.string.connection_success);
                            binding.connectionStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
                        } else {
                            binding.connectionStatusText.setText(R.string.auth_failed);
                            binding.connectionStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                        }
                    });
                }
            } catch (IOException e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        binding.connectionStatusText.setText(getString(R.string.connection_failed, e.getMessage()));
                        binding.connectionStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                    });
                }
            }
        }).start();
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = requireContext().getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE).edit();

        String url = binding.apiUrlInput.getText().toString().trim();
        String username = binding.apiUsernameInput.getText().toString().trim();
        String password = binding.apiPasswordInput.getText().toString();

        if (binding.remoteRadio.isChecked()) {
            if (!areRemoteFieldsComplete(url, username, password)) {
                Toast.makeText(requireContext(), R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!StorageConfig.isUrlSecure(url)) {
                Toast.makeText(requireContext(), R.string.https_required, Toast.LENGTH_LONG).show();
                return;
            }
            editor.putString(SettingsActivity.KEY_STORAGE_MODE, SettingsActivity.MODE_REMOTE);
        } else {
            editor.putString(SettingsActivity.KEY_STORAGE_MODE, SettingsActivity.MODE_LOCAL);
        }

        String themeMode;
        if (binding.themeLightRadio.isChecked()) {
            themeMode = SettingsActivity.THEME_LIGHT;
        } else if (binding.themeDarkRadio.isChecked()) {
            themeMode = SettingsActivity.THEME_DARK;
        } else {
            themeMode = SettingsActivity.THEME_SYSTEM;
        }
        editor.putString(SettingsActivity.KEY_THEME_MODE, themeMode);

        String language;
        if (binding.languageEnRadio.isChecked()) {
            language = SettingsActivity.LANGUAGE_EN;
        } else if (binding.languageDeRadio.isChecked()) {
            language = SettingsActivity.LANGUAGE_DE;
        } else if (binding.languageEsRadio.isChecked()) {
            language = SettingsActivity.LANGUAGE_ES;
        } else if (binding.languageFrRadio.isChecked()) {
            language = SettingsActivity.LANGUAGE_FR;
        } else {
            language = SettingsActivity.LANGUAGE_SYSTEM;
        }
        editor.putString(SettingsActivity.KEY_LANGUAGE, language);

        String fontSize;
        if (binding.fontSizeXsRadio.isChecked()) {
            fontSize = SettingsActivity.FONT_SIZE_XS;
        } else if (binding.fontSizeSmallRadio.isChecked()) {
            fontSize = SettingsActivity.FONT_SIZE_SMALL;
        } else if (binding.fontSizeNormalRadio.isChecked()) {
            fontSize = SettingsActivity.FONT_SIZE_NORMAL;
        } else if (binding.fontSizeLargeRadio.isChecked()) {
            fontSize = SettingsActivity.FONT_SIZE_LARGE;
        } else {
            fontSize = SettingsActivity.FONT_SIZE_SYSTEM;
        }
        editor.putString(SettingsActivity.KEY_FONT_SIZE, fontSize);

        editor.putString(SettingsActivity.KEY_API_URL, url);
        editor.putString(SettingsActivity.KEY_API_USERNAME, username);
        editor.putString(SettingsActivity.KEY_API_PASSWORD, password);
        editor.putBoolean(SettingsActivity.KEY_CUSTOM_TRANSLATIONS, binding.customTranslationsSwitch.isChecked());

        // Handle reminder settings
        editor.putBoolean(SettingsActivity.KEY_SLEEP_REMINDER_ENABLED,
                binding.sleepReminderSwitch.isChecked());
        editor.putString(SettingsActivity.KEY_SLEEP_REMINDER_TIME,
                binding.sleepReminderTimeButton.getText().toString());
        editor.putBoolean(SettingsActivity.KEY_DIARY_REMINDER_ENABLED,
                binding.diaryReminderSwitch.isChecked());
        editor.putString(SettingsActivity.KEY_DIARY_REMINDER_TIME,
                binding.diaryReminderTimeButton.getText().toString());
        editor.putBoolean(SettingsActivity.KEY_EMOTION_REMINDER_ENABLED,
                binding.emotionReminderSwitch.isChecked());
        editor.putInt(SettingsActivity.KEY_EMOTION_REMINDER_COUNT,
                binding.emotionReminderCountSeekBar.getProgress());
        editor.putString(SettingsActivity.KEY_WAKING_HOURS_START,
                binding.wakingHoursStartButton.getText().toString());
        editor.putString(SettingsActivity.KEY_WAKING_HOURS_END,
                binding.wakingHoursEndButton.getText().toString());

        // Handle backup settings
        if (binding.backupEnabledSwitch.isChecked()) {
            String backupPassword = binding.backupPasswordInput.getText().toString();
            String confirmPassword = binding.backupPasswordConfirmInput.getText().toString();
            if (backupPassword.isEmpty()) {
                binding.backupStatusText.setText(R.string.backup_password_required);
                binding.backupStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                return;
            }
            if (!isBackupPasswordValid(backupPassword, confirmPassword)) {
                binding.backupStatusText.setText(R.string.backup_passwords_mismatch);
                binding.backupStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                return;
            }
            editor.putBoolean(SettingsActivity.KEY_BACKUP_ENABLED, true);
            editor.putString(SettingsActivity.KEY_BACKUP_PASSWORD, backupPassword);
        } else {
            editor.putBoolean(SettingsActivity.KEY_BACKUP_ENABLED, false);
            editor.putString(SettingsActivity.KEY_BACKUP_PASSWORD, "");
        }

        // Module visibility settings
        editor.putBoolean(SettingsActivity.KEY_MODULE_DIARY_VISIBLE,
                binding.diaryVisibleSwitch.isChecked());
        editor.putBoolean(SettingsActivity.KEY_MODULE_SLEEP_VISIBLE,
                binding.sleepVisibleSwitch.isChecked());
        editor.putBoolean(SettingsActivity.KEY_MODULE_EMOTIONS_VISIBLE,
                binding.emotionsVisibleSwitch.isChecked());
        editor.putBoolean(SettingsActivity.KEY_MODULE_POINTS_VISIBLE,
                binding.pointsVisibleSwitch.isChecked());
        editor.putBoolean(SettingsActivity.KEY_MODULE_STATISTICS_VISIBLE,
                binding.statisticsVisibleSwitch.isChecked());
        editor.putBoolean(SettingsActivity.KEY_MODULE_FOOD_LOG_VISIBLE,
                binding.foodLogVisibleSwitch.isChecked());
        editor.putBoolean(SettingsActivity.KEY_MODULE_SPORT_LOG_VISIBLE,
                binding.sportLogVisibleSwitch.isChecked());
        editor.putBoolean(SettingsActivity.KEY_MODULE_MEDICATION_VISIBLE,
                binding.medicationVisibleSwitch.isChecked());
        editor.putBoolean(SettingsActivity.KEY_MODULE_BACKUP_VISIBLE,
                binding.backupVisibleSwitch.isChecked());
        editor.putBoolean(SettingsActivity.KEY_MODULE_PDF_EXPORT_VISIBLE,
                binding.pdfExportVisibleSwitch.isChecked());

        editor.apply();
        SettingsActivity.applyThemeMode(themeMode);
        SettingsActivity.applyLanguage(language);
        ReminderScheduler.rescheduleAll(requireContext());

        boolean anyReminderEnabled = binding.sleepReminderSwitch.isChecked()
                || binding.diaryReminderSwitch.isChecked()
                || binding.emotionReminderSwitch.isChecked();
        NotificationPermissionHandler permHandler = NotificationHelper.permissionHandler();
        if (anyReminderEnabled && !permHandler.hasPermission(requireContext())) {
            String permission = permHandler.permissionName();
            if (permission != null) {
                notificationPermissionLauncher.launch(permission);
            }
        }

        Toast.makeText(requireContext(), R.string.settings_saved, Toast.LENGTH_SHORT).show();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onSettingsChanged();
        }
    }

    static int[] parseTimeString(String timeString) {
        int hour = DEFAULT_HOUR;
        int minute = DEFAULT_MINUTE;
        if (timeString == null) {
            return new int[]{hour, minute};
        }
        try {
            String[] parts = timeString.split(TIME_SEPARATOR);
            hour = Integer.parseInt(parts[0]);
            minute = Integer.parseInt(parts[1]);
        } catch (Exception ignored) {
        }
        return new int[]{hour, minute};
    }

    static boolean isBackupPasswordValid(String password, String confirmPassword) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return password.equals(confirmPassword);
    }

    static boolean areRemoteFieldsComplete(String url, String username, String password) {
        return url != null && !url.trim().isEmpty()
                && username != null && !username.trim().isEmpty()
                && password != null && !password.isEmpty();
    }

    private void showTimePicker(String currentTime, TimePickerCallback callback) {
        int[] parsed = parseTimeString(currentTime);
        new TimePickerDialog(requireContext(),
                (view, hourOfDay, minuteOfHour) -> callback.onTimeSet(hourOfDay, minuteOfHour),
                parsed[0], parsed[1], true).show();
    }

    private interface TimePickerCallback {
        void onTimeSet(int hour, int minute);
    }

    private void restoreBackup() {
        File backupDir = new File(requireContext().getFilesDir(), "backups");
        BackupService backupService = new BackupService();
        File[] backups = backupService.listBackups(backupDir);

        if (backups.length == 0) {
            binding.restoreStatusText.setText(R.string.restore_no_backups);
            binding.restoreStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
            return;
        }

        String[] backupNames = new String[backups.length];
        for (int i = 0; i < backups.length; i++) {
            backupNames[i] = backups[i].getName().replace(".backup", "");
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.restore_select_backup)
                .setItems(backupNames, (dialog, which) -> {
                    File selectedFile = backups[which];
                    showPasswordDialogAndRestore(backupService, selectedFile);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private interface RestoreOptionsCallback {
        void onOptionsSelected(RestoreOptions options);
    }

    private void showRestoreOptionsDialog(RestoreOptionsCallback callback) {
        String[] items = {"Categories", "Habits", "Diary entries", "Sleep entries", "Sport logs", "Food logs"};
        boolean[] checked = {true, true, true, true, true, true};

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.restore_select_types)
                .setMultiChoiceItems(items, checked, (dialog, which, isChecked) -> checked[which] = isChecked)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    RestoreOptions options = new RestoreOptions();
                    options.setRestoreCategories(checked[0]);
                    options.setRestoreHabits(checked[1]);
                    options.setRestoreDiaryEntries(checked[2]);
                    options.setRestoreSleepEntries(checked[3]);
                    options.setRestoreSportLogs(checked[4]);
                    options.setRestoreFoodLogs(checked[5]);
                    callback.onOptionsSelected(options);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showPasswordDialogAndRestore(BackupService backupService, File selectedFile) {
        EditText passwordInput = new EditText(requireContext());
        passwordInput.setHint(R.string.backup_password_hint);
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.restore_enter_password)
                .setView(passwordInput)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String password = passwordInput.getText().toString();
                    if (password.isEmpty()) {
                        binding.restoreStatusText.setText(R.string.backup_password_required);
                        binding.restoreStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                        return;
                    }
                    if (MainActivity.getSharedLocalUser() == null || MainActivity.getSharedHabitRepository() == null) {
                        binding.restoreStatusText.setText(R.string.restore_failed_not_initialized);
                        binding.restoreStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                        return;
                    }
                    showRestoreOptionsDialog(options -> {
                        binding.restoreStatusText.setText(R.string.restore_in_progress);
                        binding.restoreStatusText.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

                        new Thread(() -> {
                            try {
                                MergeResult result = backupService.mergeBackup(
                                        selectedFile, password,
                                        MainActivity.getSharedLocalUser(),
                                        MainActivity.getSharedHabitRepository(),
                                        MainActivity.getSharedCategoryRepository(),
                                        MainActivity.getSharedDiaryEntryRepository(),
                                        MainActivity.getSharedSleepEntryRepository(),
                                        options);
                                if (isAdded()) {
                                    requireActivity().runOnUiThread(() -> {
                                        binding.restoreStatusText.setText(getString(R.string.restore_success,
                                                result.getHabitsAdded(), result.getHabitsMerged(),
                                                result.getEntriesAdded(), result.getDiaryEntriesAdded(),
                                                result.getSleepEntriesAdded(), result.getCategoriesAdded()));
                                        binding.restoreStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
                                        if (getActivity() instanceof MainActivity) {
                                            ((MainActivity) getActivity()).onSettingsChanged();
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                if (isAdded()) {
                                    requireActivity().runOnUiThread(() -> {
                                        binding.restoreStatusText.setText(getString(R.string.restore_failed, e.getMessage()));
                                        binding.restoreStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                                    });
                                }
                            }
                        }).start();
                    });
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void downloadBackup() {
        String backupPassword = binding.backupPasswordInput.getText().toString();
        if (backupPassword.isEmpty()) {
            binding.downloadStatusText.setText(R.string.backup_password_required);
            binding.downloadStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
            return;
        }

        if (MainActivity.getSharedLocalUser() == null || MainActivity.getSharedHabitRepository() == null) {
            binding.downloadStatusText.setText(R.string.restore_failed_not_initialized);
            binding.downloadStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
            return;
        }

        binding.downloadStatusText.setText(R.string.download_backup_in_progress);
        binding.downloadStatusText.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

        new Thread(() -> {
            try {
                byte[] hezData = hezBackupService.createHezBackup(
                        backupPassword,
                        MainActivity.getSharedLocalUser(),
                        MainActivity.getSharedHabitRepository(),
                        MainActivity.getSharedCategoryRepository(),
                        MainActivity.getSharedDiaryEntryRepository(),
                        MainActivity.getSharedSleepEntryRepository());

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        pendingHezBackupData = hezData;
                        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("application/octet-stream");
                        intent.putExtra(Intent.EXTRA_TITLE, hezBackupService.generateDefaultFilename());
                        createDocumentLauncher.launch(intent);
                    });
                }
            } catch (Exception e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        binding.downloadStatusText.setText(getString(R.string.download_backup_failed, e.getMessage()));
                        binding.downloadStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                    });
                }
            }
        }).start();
    }

    private void saveHezBackupToUri(Uri uri, byte[] data) {
        new Thread(() -> {
            try {
                OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    outputStream.write(data);
                    outputStream.close();
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            binding.downloadStatusText.setText(R.string.download_backup_success);
                            binding.downloadStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
                        });
                    }
                }
            } catch (IOException e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        binding.downloadStatusText.setText(getString(R.string.download_backup_failed, e.getMessage()));
                        binding.downloadStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                    });
                }
            }
        }).start();
    }

    private void restoreFromFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {"application/octet-stream", "application/zip"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        openDocumentLauncher.launch(intent);
    }

    private void showPasswordDialogAndRestoreFromFile(Uri uri) {
        EditText passwordInput = new EditText(requireContext());
        passwordInput.setHint(R.string.backup_password_hint);
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.restore_enter_password)
                .setView(passwordInput)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String password = passwordInput.getText().toString();
                    if (password.isEmpty()) {
                        binding.restoreFromFileStatusText.setText(R.string.backup_password_required);
                        binding.restoreFromFileStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                        return;
                    }
                    if (MainActivity.getSharedLocalUser() == null || MainActivity.getSharedHabitRepository() == null) {
                        binding.restoreFromFileStatusText.setText(R.string.restore_failed_not_initialized);
                        binding.restoreFromFileStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                        return;
                    }
                    showRestoreOptionsDialog(options -> {
                        binding.restoreFromFileStatusText.setText(R.string.restore_in_progress);
                        binding.restoreFromFileStatusText.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

                        new Thread(() -> {
                            try {
                                InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                                if (inputStream == null) {
                                    throw new BackupException("Could not open file");
                                }
                                MergeResult result = hezBackupService.mergeFromHezStream(
                                        inputStream, password,
                                        MainActivity.getSharedLocalUser(),
                                        MainActivity.getSharedHabitRepository(),
                                        MainActivity.getSharedCategoryRepository(),
                                        MainActivity.getSharedDiaryEntryRepository(),
                                        MainActivity.getSharedSleepEntryRepository(),
                                        options);
                                inputStream.close();

                                if (isAdded()) {
                                    requireActivity().runOnUiThread(() -> {
                                        binding.restoreFromFileStatusText.setText(getString(R.string.restore_success,
                                                result.getHabitsAdded(), result.getHabitsMerged(),
                                                result.getEntriesAdded(), result.getDiaryEntriesAdded(),
                                                result.getSleepEntriesAdded(), result.getCategoriesAdded()));
                                        binding.restoreFromFileStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
                                        if (getActivity() instanceof MainActivity) {
                                            ((MainActivity) getActivity()).onSettingsChanged();
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                if (isAdded()) {
                                    requireActivity().runOnUiThread(() -> {
                                        binding.restoreFromFileStatusText.setText(getString(R.string.restore_failed, e.getMessage()));
                                        binding.restoreFromFileStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                                    });
                                }
                            }
                        }).start();
                    });
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
