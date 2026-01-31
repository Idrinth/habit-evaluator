package de.idrinth.habitevaluator.android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import de.idrinth.habitevaluator.android.databinding.ActivitySettingsBinding;
import de.idrinth.habitevaluator.shared.api.ApiClient;

import java.io.IOException;

/**
 * Activity for configuring storage settings.
 * Allows switching between local in-memory storage and remote API storage.
 */
public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "habit_evaluator_settings";
    public static final String KEY_STORAGE_MODE = "storage_mode";
    public static final String KEY_API_URL = "api_base_url";
    public static final String KEY_API_USERNAME = "api_username";
    public static final String KEY_API_PASSWORD = "api_password";
    public static final String MODE_LOCAL = "LOCAL";
    public static final String MODE_REMOTE = "REMOTE";

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadSettings();
        setupListeners();
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String mode = prefs.getString(KEY_STORAGE_MODE, MODE_LOCAL);
        String url = prefs.getString(KEY_API_URL, "http://localhost:8080");
        String username = prefs.getString(KEY_API_USERNAME, "");
        String password = prefs.getString(KEY_API_PASSWORD, "");

        if (MODE_REMOTE.equals(mode)) {
            binding.remoteRadio.setChecked(true);
            binding.remoteSettingsPanel.setVisibility(View.VISIBLE);
        } else {
            binding.localRadio.setChecked(true);
            binding.remoteSettingsPanel.setVisibility(View.GONE);
        }

        binding.apiUrlInput.setText(url);
        binding.apiUsernameInput.setText(username);
        binding.apiPasswordInput.setText(password);
    }

    private void setupListeners() {
        binding.storageModeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.remoteRadio) {
                binding.remoteSettingsPanel.setVisibility(View.VISIBLE);
            } else {
                binding.remoteSettingsPanel.setVisibility(View.GONE);
            }
        });

        binding.testConnectionButton.setOnClickListener(v -> testConnection());
        binding.saveSettingsButton.setOnClickListener(v -> saveSettings());
    }

    private void testConnection() {
        String url = binding.apiUrlInput.getText().toString().trim();
        String username = binding.apiUsernameInput.getText().toString().trim();
        String password = binding.apiPasswordInput.getText().toString();

        if (url.isEmpty() || username.isEmpty() || password.isEmpty()) {
            binding.connectionStatusText.setText(R.string.fill_all_fields);
            binding.connectionStatusText.setTextColor(getColor(android.R.color.holo_red_dark));
            return;
        }

        binding.connectionStatusText.setText(R.string.testing_connection);
        binding.connectionStatusText.setTextColor(getColor(android.R.color.darker_gray));

        new Thread(() -> {
            try {
                ApiClient client = new ApiClient(url);
                boolean success = client.login(username, password);
                runOnUiThread(() -> {
                    if (success) {
                        binding.connectionStatusText.setText(R.string.connection_success);
                        binding.connectionStatusText.setTextColor(getColor(android.R.color.holo_green_dark));
                    } else {
                        binding.connectionStatusText.setText(R.string.auth_failed);
                        binding.connectionStatusText.setTextColor(getColor(android.R.color.holo_red_dark));
                    }
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    binding.connectionStatusText.setText(getString(R.string.connection_failed, e.getMessage()));
                    binding.connectionStatusText.setTextColor(getColor(android.R.color.holo_red_dark));
                });
            }
        }).start();
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();

        if (binding.remoteRadio.isChecked()) {
            String url = binding.apiUrlInput.getText().toString().trim();
            String username = binding.apiUsernameInput.getText().toString().trim();
            String password = binding.apiPasswordInput.getText().toString();

            if (url.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            editor.putString(KEY_STORAGE_MODE, MODE_REMOTE);
            editor.putString(KEY_API_URL, url);
            editor.putString(KEY_API_USERNAME, username);
            editor.putString(KEY_API_PASSWORD, password);
        } else {
            editor.putString(KEY_STORAGE_MODE, MODE_LOCAL);
        }

        editor.apply();
        setResult(RESULT_OK);
        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
        finish();
    }
}
