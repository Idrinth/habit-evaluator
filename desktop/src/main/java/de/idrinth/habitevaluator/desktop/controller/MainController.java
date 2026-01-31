package de.idrinth.habitevaluator.desktop.controller;

import de.idrinth.habitevaluator.desktop.persistence.H2HabitCategoryRepository;
import de.idrinth.habitevaluator.desktop.persistence.H2HabitRepository;
import de.idrinth.habitevaluator.desktop.persistence.H2UserRepository;
import de.idrinth.habitevaluator.shared.api.ApiClient;
import de.idrinth.habitevaluator.shared.api.RemoteHabitRepository;
import de.idrinth.habitevaluator.shared.api.RemoteUserRepository;
import de.idrinth.habitevaluator.shared.api.StorageConfig;
import de.idrinth.habitevaluator.shared.model.Evaluation;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.service.DefaultDataInitializer;
import de.idrinth.habitevaluator.shared.service.HabitEvaluatorService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainController {

    private static final String PLACEHOLDER_USERNAME = "desktop_user";
    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.habit-evaluator";
    private static final String CONFIG_FILE = CONFIG_DIR + "/storage.properties";

    @FXML
    private ListView<Habit> habitListView;

    @FXML
    private TextField habitNameField;

    @FXML
    private TextArea habitDescriptionArea;

    @FXML
    private Label streakLabel;

    @FXML
    private Label completionRateLabel;

    @FXML
    private ProgressBar completionProgressBar;

    @FXML
    private VBox trackHabitsContainer;

    @FXML
    private Label trackMessage;

    @FXML
    private Label storageModeLabel;

    private final Map<String, CheckBox> trackCheckBoxes = new HashMap<>();
    private final ObservableList<Habit> habits = FXCollections.observableArrayList();
    private final HabitEvaluatorService evaluatorService = new HabitEvaluatorService();
    private final StorageConfig storageConfig = new StorageConfig(new File(CONFIG_FILE));

    private HabitRepository habitRepository;
    private HabitCategoryRepository categoryRepository;
    private UserRepository userRepository;
    private ApiClient apiClient;
    private User currentUser;

    @FXML
    public void initialize() {
        initializeStorage();
        loadHabits();

        habitListView.setItems(habits);
        habitListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Habit item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        habitListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        displayHabitDetails(newValue);
                    }
                });

        refreshTrackHabits();
        habits.addListener((javafx.collections.ListChangeListener<Habit>) change -> refreshTrackHabits());
    }

    private void initializeStorage() {
        if (storageConfig.isRemote()) {
            initializeRemoteStorage();
        } else {
            initializeLocalStorage();
        }
        updateStorageModeLabel();
    }

    private void initializeLocalStorage() {
        habitRepository = new H2HabitRepository();
        userRepository = new H2UserRepository();
        categoryRepository = new H2HabitCategoryRepository();
        apiClient = null;
        currentUser = userRepository.findByUsername(PLACEHOLDER_USERNAME)
                .orElseGet(() -> {
                    User user = new User(PLACEHOLDER_USERNAME, "placeholder");
                    return userRepository.save(user);
                });
    }

    private void initializeRemoteStorage() {
        try {
            apiClient = new ApiClient(storageConfig.getApiBaseUrl());
            boolean loggedIn = apiClient.login(
                    storageConfig.getApiUsername(),
                    storageConfig.getApiPassword()
            );
            if (loggedIn) {
                habitRepository = new RemoteHabitRepository(apiClient);
                userRepository = new RemoteUserRepository(apiClient);
                categoryRepository = null;
                currentUser = userRepository.findAll().stream().findFirst().orElse(null);
                return;
            }
        } catch (IOException e) {
            showAlert("Connection Error",
                    "Failed to connect to remote API: " + e.getMessage() + "\nFalling back to local storage.");
        }
        // Fall back to local
        storageConfig.setStorageMode(StorageConfig.StorageMode.LOCAL);
        initializeLocalStorage();
    }

    private void loadHabits() {
        habits.clear();
        if (currentUser != null) {
            habits.addAll(habitRepository.findByUserId(currentUser.getId()));
        }
    }

    private void updateStorageModeLabel() {
        if (storageModeLabel != null) {
            storageModeLabel.setText("Storage: " + (storageConfig.isRemote() ? "Remote API" : "Local"));
        }
    }

    @FXML
    private void handleOpenSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/settings.fxml"));
            Parent root = loader.load();

            SettingsDialogController controller = loader.getController();
            controller.setStorageConfig(storageConfig);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Storage Settings");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(habitListView.getScene().getWindow());

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            if (controller.isSaved()) {
                initializeStorage();
                loadHabits();
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to open settings: " + e.getMessage());
        }
    }

    private void refreshTrackHabits() {
        trackHabitsContainer.getChildren().clear();
        trackCheckBoxes.clear();
        trackMessage.setText("");
        if (habits.isEmpty()) {
            trackHabitsContainer.getChildren().add(new Label("No habits found. Add a habit first."));
            return;
        }
        for (Habit habit : habits) {
            CheckBox checkBox = new CheckBox(habit.getName());
            if (habit.getDescription() != null && !habit.getDescription().isEmpty()) {
                checkBox.setTooltip(new Tooltip(habit.getDescription()));
            }
            trackCheckBoxes.put(habit.getId(), checkBox);
            trackHabitsContainer.getChildren().add(checkBox);
        }
    }

    @FXML
    private void handleAddHabit() {
        String name = habitNameField.getText().trim();
        String description = habitDescriptionArea.getText().trim();

        if (!name.isEmpty()) {
            Habit habit = new Habit(name, description);
            habit.setUser(currentUser);
            habitRepository.save(habit);
            habits.add(habit);
            clearInputFields();
        }
    }

    @FXML
    private void handleCompleteHabit() {
        Habit selectedHabit = habitListView.getSelectionModel().getSelectedItem();
        if (selectedHabit != null) {
            HabitEntry entry = new HabitEntry(selectedHabit.getId());
            selectedHabit.addEntry(entry);
            habitRepository.save(selectedHabit);
            displayHabitDetails(selectedHabit);
        }
    }

    @FXML
    private void handleDeleteHabit() {
        Habit selectedHabit = habitListView.getSelectionModel().getSelectedItem();
        if (selectedHabit != null) {
            habitRepository.deleteById(selectedHabit.getId());
            habits.remove(selectedHabit);
            clearEvaluationDisplay();
        }
    }

    @FXML
    private void handleTrackHabits() {
        List<Habit> checkedHabits = new ArrayList<>();
        for (Habit habit : habits) {
            CheckBox checkBox = trackCheckBoxes.get(habit.getId());
            if (checkBox != null && checkBox.isSelected()) {
                checkedHabits.add(habit);
            }
        }
        if (checkedHabits.isEmpty()) {
            trackMessage.setText("No habits were selected");
            trackMessage.setStyle("-fx-text-fill: red;");
            return;
        }
        int count = 0;
        for (Habit habit : checkedHabits) {
            HabitEntry entry = new HabitEntry(habit.getId());
            habit.addEntry(entry);
            habitRepository.save(habit);
            count++;
        }
        trackMessage.setText(count + " habit(s) tracked successfully");
        trackMessage.setStyle("-fx-text-fill: green;");

        // Uncheck all boxes
        for (CheckBox checkBox : trackCheckBoxes.values()) {
            checkBox.setSelected(false);
        }
    }

    private void displayHabitDetails(Habit habit) {
        Evaluation evaluation = evaluatorService.evaluate(
                habit,
                LocalDate.now().minusDays(30),
                LocalDate.now()
        );

        streakLabel.setText("Current Streak: " + evaluation.getCurrentStreak() + " days");
        completionRateLabel.setText(String.format("Completion Rate: %.1f%%", evaluation.getCompletionRate() * 100));
        completionProgressBar.setProgress(evaluation.getCompletionRate());
    }

    private void clearInputFields() {
        habitNameField.clear();
        habitDescriptionArea.clear();
    }

    private void clearEvaluationDisplay() {
        streakLabel.setText("Current Streak: -");
        completionRateLabel.setText("Completion Rate: -");
        completionProgressBar.setProgress(0);
    }

    @FXML
    private void handleLoadDefaults() {
        new Thread(() -> {
            try {
                if (storageConfig.isRemote() && apiClient != null) {
                    apiClient.post("/api/init-defaults", Collections.emptyMap(),
                            new TypeToken<Map<String, Object>>() {}.getType());
                } else if (categoryRepository != null && currentUser != null) {
                    DefaultDataInitializer initializer = new DefaultDataInitializer(categoryRepository, habitRepository);
                    initializer.initializeDefaults(currentUser);
                }
                Platform.runLater(this::loadHabits);
            } catch (IOException e) {
                Platform.runLater(() -> showAlert("Error", "Failed to load default habits: " + e.getMessage()));
            }
        }).start();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
