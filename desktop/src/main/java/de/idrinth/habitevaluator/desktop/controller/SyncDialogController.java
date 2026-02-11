package de.idrinth.habitevaluator.desktop.controller;

import de.idrinth.habitevaluator.shared.api.SyncService;
import de.idrinth.habitevaluator.shared.api.VersionMismatchException;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Controller for the sync dialog.
 * Prompts for remote server credentials and performs bidirectional sync.
 */
public class SyncDialogController {

    @FXML
    private TextField serverUrlField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    @FXML
    private Button syncButton;

    private HabitRepository habitRepository;
    private User currentUser;
    private boolean synced;

    public void setHabitRepository(HabitRepository habitRepository) {
        this.habitRepository = habitRepository;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    @FXML
    private void handleSync() {
        String url = serverUrlField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (url.isEmpty() || username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please fill in all fields");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        statusLabel.setText("Syncing...");
        statusLabel.setStyle("-fx-text-fill: grey;");
        syncButton.setDisable(true);

        new Thread(() -> {
            try {
                SyncService syncService = new SyncService(habitRepository);
                SyncService.SyncResult result = syncService.sync(url, username, password, currentUser, getClientVersion());

                Platform.runLater(() -> {
                    statusLabel.setText(String.format(
                            "Sync complete: %d pushed, %d pulled, %d merged",
                            result.getPushed(), result.getPulled(), result.getMerged()));
                    statusLabel.setStyle("-fx-text-fill: green;");
                    syncButton.setDisable(false);
                    synced = true;
                });
            } catch (VersionMismatchException e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Version mismatch: your version ("
                            + e.getClientVersion() + ") does not match server ("
                            + e.getServerVersion() + "). Please update before syncing.");
                    statusLabel.setStyle("-fx-text-fill: red;");
                    syncButton.setDisable(false);
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Sync failed: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: red;");
                    syncButton.setDisable(false);
                });
            }
        }).start();
    }

    private String getClientVersion() {
        try (InputStream is = getClass().getResourceAsStream("/version.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                return props.getProperty("version", "unknown");
            }
        } catch (Exception e) {
            // fall through
        }
        return "unknown";
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) serverUrlField.getScene().getWindow();
        stage.close();
    }

    public boolean isSynced() {
        return synced;
    }
}
