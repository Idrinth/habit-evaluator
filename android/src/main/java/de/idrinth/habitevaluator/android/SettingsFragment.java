package de.idrinth.habitevaluator.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;

import de.idrinth.habitevaluator.android.databinding.FragmentSettingsBinding;
import de.idrinth.habitevaluator.shared.api.ApiClient;
import de.idrinth.habitevaluator.shared.api.StorageConfig;
import de.idrinth.habitevaluator.shared.backup.BackupException;
import de.idrinth.habitevaluator.shared.backup.BackupService;
import de.idrinth.habitevaluator.shared.backup.MergeResult;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

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

        boolean backupEnabled = prefs.getBoolean(SettingsActivity.KEY_BACKUP_ENABLED, false);
        binding.backupEnabledSwitch.setChecked(backupEnabled);
        binding.backupSettingsPanel.setVisibility(backupEnabled ? View.VISIBLE : View.GONE);
        String backupPassword = prefs.getString(SettingsActivity.KEY_BACKUP_PASSWORD, "");
        binding.backupPasswordInput.setText(backupPassword);
        binding.backupPasswordConfirmInput.setText(backupPassword);
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

        binding.testConnectionButton.setOnClickListener(v -> testConnection());
        binding.saveSettingsButton.setOnClickListener(v -> saveSettings());
        binding.restoreBackupButton.setOnClickListener(v -> restoreBackup());
    }

    private void testConnection() {
        String url = binding.apiUrlInput.getText().toString().trim();
        String username = binding.apiUsernameInput.getText().toString().trim();
        String password = binding.apiPasswordInput.getText().toString();

        if (url.isEmpty() || username.isEmpty() || password.isEmpty()) {
            binding.connectionStatusText.setText(R.string.fill_all_fields);
            binding.connectionStatusText.setTextColor(requireContext().getColor(android.R.color.holo_red_dark));
            return;
        }

        if (!StorageConfig.isUrlSecure(url)) {
            binding.connectionStatusText.setText(R.string.https_required);
            binding.connectionStatusText.setTextColor(requireContext().getColor(android.R.color.holo_red_dark));
            return;
        }

        binding.connectionStatusText.setText(R.string.testing_connection);
        binding.connectionStatusText.setTextColor(requireContext().getColor(R.color.text_secondary));

        new Thread(() -> {
            try {
                ApiClient client = new ApiClient(url);
                boolean success = client.login(username, password);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (success) {
                            binding.connectionStatusText.setText(R.string.connection_success);
                            binding.connectionStatusText.setTextColor(requireContext().getColor(android.R.color.holo_green_dark));
                        } else {
                            binding.connectionStatusText.setText(R.string.auth_failed);
                            binding.connectionStatusText.setTextColor(requireContext().getColor(android.R.color.holo_red_dark));
                        }
                    });
                }
            } catch (IOException e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        binding.connectionStatusText.setText(getString(R.string.connection_failed, e.getMessage()));
                        binding.connectionStatusText.setTextColor(requireContext().getColor(android.R.color.holo_red_dark));
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
            if (url.isEmpty() || username.isEmpty() || password.isEmpty()) {
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

        // Handle backup settings
        if (binding.backupEnabledSwitch.isChecked()) {
            String backupPassword = binding.backupPasswordInput.getText().toString();
            String confirmPassword = binding.backupPasswordConfirmInput.getText().toString();
            if (backupPassword.isEmpty()) {
                binding.backupStatusText.setText(R.string.backup_password_required);
                binding.backupStatusText.setTextColor(requireContext().getColor(android.R.color.holo_red_dark));
                return;
            }
            if (!backupPassword.equals(confirmPassword)) {
                binding.backupStatusText.setText(R.string.backup_passwords_mismatch);
                binding.backupStatusText.setTextColor(requireContext().getColor(android.R.color.holo_red_dark));
                return;
            }
            editor.putBoolean(SettingsActivity.KEY_BACKUP_ENABLED, true);
            editor.putString(SettingsActivity.KEY_BACKUP_PASSWORD, backupPassword);
        } else {
            editor.putBoolean(SettingsActivity.KEY_BACKUP_ENABLED, false);
            editor.putString(SettingsActivity.KEY_BACKUP_PASSWORD, "");
        }

        editor.apply();
        SettingsActivity.applyThemeMode(themeMode);
        SettingsActivity.applyLanguage(language);
        Toast.makeText(requireContext(), R.string.settings_saved, Toast.LENGTH_SHORT).show();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onSettingsChanged();
        }
    }

    private void restoreBackup() {
        File backupDir = new File(requireContext().getFilesDir(), "backups");
        BackupService backupService = new BackupService();
        File[] backups = backupService.listBackups(backupDir);

        if (backups.length == 0) {
            binding.restoreStatusText.setText(R.string.restore_no_backups);
            binding.restoreStatusText.setTextColor(requireContext().getColor(android.R.color.holo_red_dark));
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
                        binding.restoreStatusText.setTextColor(requireContext().getColor(android.R.color.holo_red_dark));
                        return;
                    }
                    binding.restoreStatusText.setText(R.string.restore_in_progress);
                    binding.restoreStatusText.setTextColor(requireContext().getColor(R.color.text_secondary));

                    new Thread(() -> {
                        try {
                            MergeResult result = backupService.mergeBackup(
                                    selectedFile, password,
                                    MainActivity.getSharedCurrentUser(),
                                    MainActivity.getSharedHabitRepository(),
                                    MainActivity.getSharedCategoryRepository(),
                                    MainActivity.getSharedDiaryEntryRepository(),
                                    MainActivity.getSharedSleepEntryRepository());
                            if (isAdded()) {
                                requireActivity().runOnUiThread(() -> {
                                    binding.restoreStatusText.setText(getString(R.string.restore_success,
                                            result.getHabitsAdded(), result.getHabitsMerged(),
                                            result.getEntriesAdded(), result.getDiaryEntriesAdded(),
                                            result.getSleepEntriesAdded(), result.getCategoriesAdded()));
                                    binding.restoreStatusText.setTextColor(requireContext().getColor(android.R.color.holo_green_dark));
                                    if (getActivity() instanceof MainActivity) {
                                        ((MainActivity) getActivity()).onSettingsChanged();
                                    }
                                });
                            }
                        } catch (BackupException e) {
                            if (isAdded()) {
                                requireActivity().runOnUiThread(() -> {
                                    binding.restoreStatusText.setText(getString(R.string.restore_failed, e.getMessage()));
                                    binding.restoreStatusText.setTextColor(requireContext().getColor(android.R.color.holo_red_dark));
                                });
                            }
                        }
                    }).start();
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
