package de.idrinth.habitevaluator.desktop.controller;

import de.idrinth.habitevaluator.shared.api.ApiClient;
import de.idrinth.habitevaluator.shared.api.StorageConfig;
import de.idrinth.habitevaluator.shared.backup.BackupException;
import de.idrinth.habitevaluator.shared.backup.BackupService;
import de.idrinth.habitevaluator.shared.backup.HezBackupService;
import de.idrinth.habitevaluator.shared.backup.MergeResult;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

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

    @FXML
    private ToggleGroup languageToggleGroup;

    @FXML
    private RadioButton languageSystemRadio;

    @FXML
    private RadioButton languageEnRadio;

    @FXML
    private RadioButton languageDeRadio;

    @FXML
    private RadioButton languageEsRadio;

    @FXML
    private RadioButton languageFrRadio;

    @FXML
    private CheckBox customTranslationsCheckBox;

    @FXML
    private CheckBox backupEnabledCheckBox;

    @FXML
    private VBox backupSettingsPane;

    @FXML
    private PasswordField backupPasswordField;

    @FXML
    private PasswordField backupPasswordConfirmField;

    @FXML
    private Label backupStatusLabel;

    @FXML
    private Label restoreStatusLabel;

    @FXML
    private Label downloadStatusLabel;

    @FXML
    private Label restoreFromFileStatusLabel;

    private StorageConfig storageConfig;
    private boolean saved;
    private final BackupService backupService = new BackupService();
    private final HezBackupService hezBackupService = new HezBackupService();
    private User currentUser;
    private HabitRepository habitRepository;
    private HabitCategoryRepository categoryRepository;
    private DiaryEntryRepository diaryEntryRepository;
    private SleepEntryRepository sleepEntryRepository;

    @FXML
    public void initialize() {
        storageToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            remoteSettingsPane.setDisable(localRadio.isSelected());
        });
        backupEnabledCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            backupSettingsPane.setDisable(!newVal);
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

        String language = config.getLanguage();
        if ("en".equals(language)) {
            languageEnRadio.setSelected(true);
        } else if ("de".equals(language)) {
            languageDeRadio.setSelected(true);
        } else if ("es".equals(language)) {
            languageEsRadio.setSelected(true);
        } else if ("fr".equals(language)) {
            languageFrRadio.setSelected(true);
        } else {
            languageSystemRadio.setSelected(true);
        }

        customTranslationsCheckBox.setSelected(config.isCustomTranslationsEnabled());

        backupEnabledCheckBox.setSelected(config.isBackupEnabled());
        backupSettingsPane.setDisable(!config.isBackupEnabled());
        backupPasswordField.setText(config.getBackupPassword());
        backupPasswordConfirmField.setText(config.getBackupPassword());
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

        if (!StorageConfig.isUrlSecure(url)) {
            connectionStatusLabel.setText("Remote server URL must use HTTPS. Only localhost is allowed over plain HTTP.");
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
            if (!StorageConfig.isUrlSecure(url)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("HTTPS Required");
                alert.setHeaderText(null);
                alert.setContentText("Remote server URL must use HTTPS. Only localhost is allowed over plain HTTP.");
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

        if (languageEnRadio.isSelected()) {
            storageConfig.setLanguage("en");
        } else if (languageDeRadio.isSelected()) {
            storageConfig.setLanguage("de");
        } else if (languageEsRadio.isSelected()) {
            storageConfig.setLanguage("es");
        } else if (languageFrRadio.isSelected()) {
            storageConfig.setLanguage("fr");
        } else {
            storageConfig.setLanguage(StorageConfig.LANGUAGE_SYSTEM);
        }

        storageConfig.setCustomTranslationsEnabled(customTranslationsCheckBox.isSelected());

        // Handle backup settings
        if (backupEnabledCheckBox.isSelected()) {
            String backupPassword = backupPasswordField.getText();
            String confirmPassword = backupPasswordConfirmField.getText();
            if (backupPassword == null || backupPassword.isEmpty()) {
                backupStatusLabel.setText("Backup password is required when backups are enabled.");
                backupStatusLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            if (!backupPassword.equals(confirmPassword)) {
                backupStatusLabel.setText("Passwords do not match.");
                backupStatusLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            storageConfig.setBackupEnabled(true);
            storageConfig.setBackupPassword(backupPassword);
        } else {
            storageConfig.setBackupEnabled(false);
            storageConfig.setBackupPassword("");
        }

        storageConfig.save();
        saved = true;

        Stage stage = (Stage) localRadio.getScene().getWindow();
        stage.close();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setHabitRepository(HabitRepository habitRepository) {
        this.habitRepository = habitRepository;
    }

    public void setCategoryRepository(HabitCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public void setDiaryEntryRepository(DiaryEntryRepository diaryEntryRepository) {
        this.diaryEntryRepository = diaryEntryRepository;
    }

    public void setSleepEntryRepository(SleepEntryRepository sleepEntryRepository) {
        this.sleepEntryRepository = sleepEntryRepository;
    }

    @FXML
    private void handleRestoreBackup() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Backup File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Backup Files", "*.backup"));

        String backupDir = System.getProperty("user.home") + "/.habit-evaluator/backups";
        File backupDirFile = new File(backupDir);
        if (backupDirFile.exists() && backupDirFile.isDirectory()) {
            fileChooser.setInitialDirectory(backupDirFile);
        }

        Stage stage = (Stage) localRadio.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile == null) {
            return;
        }

        TextInputDialog passwordDialog = new TextInputDialog();
        passwordDialog.setTitle("Backup Password");
        passwordDialog.setHeaderText("Enter the password for this backup");
        passwordDialog.setContentText("Password:");
        Optional<String> passwordResult = passwordDialog.showAndWait();
        if (passwordResult.isEmpty() || passwordResult.get().isEmpty()) {
            restoreStatusLabel.setText("Restore cancelled: no password provided.");
            restoreStatusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        String password = passwordResult.get();
        restoreStatusLabel.setText("Restoring backup...");
        restoreStatusLabel.setStyle("-fx-text-fill: grey;");

        new Thread(() -> {
            try {
                MergeResult result = backupService.mergeBackup(selectedFile, password,
                        currentUser, habitRepository, categoryRepository,
                        diaryEntryRepository, sleepEntryRepository);
                Platform.runLater(() -> {
                    restoreStatusLabel.setText(
                            "Restore complete: " + result.getHabitsAdded() + " habits added, "
                            + result.getHabitsMerged() + " habits merged, "
                            + result.getEntriesAdded() + " entries added, "
                            + result.getDiaryEntriesAdded() + " diary entries added, "
                            + result.getSleepEntriesAdded() + " sleep entries added, "
                            + result.getCategoriesAdded() + " categories added.");
                    restoreStatusLabel.setStyle("-fx-text-fill: green;");
                    saved = true;
                });
            } catch (BackupException e) {
                Platform.runLater(() -> {
                    restoreStatusLabel.setText("Restore failed: " + e.getMessage());
                    restoreStatusLabel.setStyle("-fx-text-fill: red;");
                });
            }
        }).start();
    }

    @FXML
    private void handleDownloadBackup() {
        String backupPassword = backupPasswordField.getText();
        if (backupPassword == null || backupPassword.isEmpty()) {
            downloadStatusLabel.setText("Backup password is required");
            downloadStatusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Backup File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Hez Backup Files", "*.hez"));
        fileChooser.setInitialFileName(hezBackupService.generateDefaultFilename());

        Stage stage = (Stage) localRadio.getScene().getWindow();
        File selectedFile = fileChooser.showSaveDialog(stage);
        if (selectedFile == null) {
            return;
        }

        downloadStatusLabel.setText("Creating backup...");
        downloadStatusLabel.setStyle("-fx-text-fill: grey;");

        new Thread(() -> {
            try {
                hezBackupService.createHezBackupToFile(selectedFile, backupPassword,
                        currentUser, habitRepository, categoryRepository,
                        diaryEntryRepository, sleepEntryRepository);
                Platform.runLater(() -> {
                    downloadStatusLabel.setText("Backup saved successfully: " + selectedFile.getName());
                    downloadStatusLabel.setStyle("-fx-text-fill: green;");
                });
            } catch (BackupException e) {
                Platform.runLater(() -> {
                    downloadStatusLabel.setText("Backup failed: " + e.getMessage());
                    downloadStatusLabel.setStyle("-fx-text-fill: red;");
                });
            }
        }).start();
    }

    @FXML
    private void handleRestoreFromFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Backup File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Hez Backup Files", "*.hez"));

        Stage stage = (Stage) localRadio.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile == null) {
            return;
        }

        TextInputDialog passwordDialog = new TextInputDialog();
        passwordDialog.setTitle("Backup Password");
        passwordDialog.setHeaderText("Enter the password for this backup");
        passwordDialog.setContentText("Password:");
        Optional<String> passwordResult = passwordDialog.showAndWait();
        if (passwordResult.isEmpty() || passwordResult.get().isEmpty()) {
            restoreFromFileStatusLabel.setText("Restore cancelled: no password provided.");
            restoreFromFileStatusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        String password = passwordResult.get();
        restoreFromFileStatusLabel.setText("Restoring from file...");
        restoreFromFileStatusLabel.setStyle("-fx-text-fill: grey;");

        new Thread(() -> {
            try {
                MergeResult result = hezBackupService.mergeFromHezFile(selectedFile, password,
                        currentUser, habitRepository, categoryRepository,
                        diaryEntryRepository, sleepEntryRepository);
                Platform.runLater(() -> {
                    restoreFromFileStatusLabel.setText(
                            "Restore complete: " + result.getHabitsAdded() + " habits added, "
                            + result.getHabitsMerged() + " habits merged, "
                            + result.getEntriesAdded() + " entries added, "
                            + result.getDiaryEntriesAdded() + " diary entries added, "
                            + result.getSleepEntriesAdded() + " sleep entries added, "
                            + result.getCategoriesAdded() + " categories added.");
                    restoreFromFileStatusLabel.setStyle("-fx-text-fill: green;");
                    saved = true;
                });
            } catch (BackupException e) {
                Platform.runLater(() -> {
                    restoreFromFileStatusLabel.setText("Restore failed: " + e.getMessage());
                    restoreFromFileStatusLabel.setStyle("-fx-text-fill: red;");
                });
            }
        }).start();
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
