package de.idrinth.habitevaluator.desktop.controller;

import de.idrinth.habitevaluator.shared.api.ApiClient;
import de.idrinth.habitevaluator.shared.api.StorageConfig;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the storage settings dialog.
 * Allows switching between local database and remote API storage.
 */
public class SettingsDialogController {

    @FXML
    private ToggleGroup storageToggleGroup;

    @FXML
    private RadioButton localRadio;

    @FXML
    private RadioButton remoteRadio;

    @FXML
    private VBox remoteSettingsPane;

    @FXML
    private TextField apiUrlField;

    @FXML
    private TextField apiUsernameField;

    @FXML
    private PasswordField apiPasswordField;

    @FXML
    private Label connectionStatusLabel;

    @FXML
    private ToggleGroup themeToggleGroup;

    @FXML
    private RadioButton themeSystemRadio;

    @FXML
    private RadioButton themeLightRadio;

    @FXML
    private RadioButton themeDarkRadio;

    private StorageConfig storageConfig;
    private boolean saved;

    @FXML
    public void initialize() {
        storageToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            remoteSettingsPane.setDisable(localRadio.isSelected());
        });
    }

    public void setStorageConfig(StorageConfig config) {
        this.storageConfig = config;
        if (config.isRemote()) {
            remoteRadio.setSelected(true);
            remoteSettingsPane.setDisable(false);
        } else {
            localRadio.setSelected(true);
            remoteSettingsPane.setDisable(true);
        }
        apiUrlField.setText(config.getApiBaseUrl());
        apiUsernameField.setText(config.getApiUsername());
        apiPasswordField.setText(config.getApiPassword());

        StorageConfig.ThemeMode themeMode = config.getThemeMode();
        if (themeMode == StorageConfig.ThemeMode.LIGHT) {
            themeLightRadio.setSelected(true);
        } else if (themeMode == StorageConfig.ThemeMode.DARK) {
            themeDarkRadio.setSelected(true);
        } else {
            themeSystemRadio.setSelected(true);
        }
    }

    @FXML
    private void handleTestConnection() {
        String url = apiUrlField.getText().trim();
        String username = apiUsernameField.getText().trim();
        String password = apiPasswordField.getText();

        if (url.isEmpty() || username.isEmpty() || password.isEmpty()) {
            connectionStatusLabel.setText("Please fill in all fields");
            connectionStatusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        connectionStatusLabel.setText("Testing connection...");
        connectionStatusLabel.setStyle("-fx-text-fill: grey;");

        try {
            ApiClient client = new ApiClient(url);
            boolean success = client.login(username, password);
            if (success) {
                connectionStatusLabel.setText("Connection successful");
                connectionStatusLabel.setStyle("-fx-text-fill: green;");
            } else {
                connectionStatusLabel.setText("Authentication failed");
                connectionStatusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (IOException e) {
            connectionStatusLabel.setText("Connection failed: " + e.getMessage());
            connectionStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleSave() {
        String url = apiUrlField.getText().trim();
        String username = apiUsernameField.getText().trim();
        String password = apiPasswordField.getText();

        if (remoteRadio.isSelected()) {
            if (url.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Incomplete Settings");
                alert.setHeaderText(null);
                alert.setContentText("Please fill in all remote API fields.");
                alert.showAndWait();
                return;
            }
            storageConfig.setStorageMode(StorageConfig.StorageMode.REMOTE);
        } else {
            storageConfig.setStorageMode(StorageConfig.StorageMode.LOCAL);
        }

        storageConfig.setApiBaseUrl(url);
        storageConfig.setApiUsername(username);
        storageConfig.setApiPassword(password);

        if (themeLightRadio.isSelected()) {
            storageConfig.setThemeMode(StorageConfig.ThemeMode.LIGHT);
        } else if (themeDarkRadio.isSelected()) {
            storageConfig.setThemeMode(StorageConfig.ThemeMode.DARK);
        } else {
            storageConfig.setThemeMode(StorageConfig.ThemeMode.SYSTEM);
        }

        storageConfig.save();
        saved = true;

        Stage stage = (Stage) localRadio.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) localRadio.getScene().getWindow();
        stage.close();
    }

    public boolean isSaved() {
        return saved;
    }
}
