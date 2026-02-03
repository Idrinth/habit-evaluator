package de.idrinth.habitevaluator.desktop.controller;

import de.idrinth.habitevaluator.desktop.persistence.H2DiaryEntryRepository;
import de.idrinth.habitevaluator.desktop.persistence.H2HabitCategoryRepository;
import de.idrinth.habitevaluator.desktop.persistence.H2HabitRepository;
import de.idrinth.habitevaluator.desktop.persistence.H2SleepEntryRepository;
import de.idrinth.habitevaluator.desktop.persistence.H2UserRepository;
import de.idrinth.habitevaluator.shared.api.ApiClient;
import de.idrinth.habitevaluator.shared.api.RemoteHabitRepository;
import de.idrinth.habitevaluator.shared.api.RemoteUserRepository;
import de.idrinth.habitevaluator.shared.api.StorageConfig;
import de.idrinth.habitevaluator.shared.api.SyncService;
import de.idrinth.habitevaluator.shared.model.Evaluation;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.ScoringRule;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.service.DefaultDataInitializer;
import de.idrinth.habitevaluator.shared.service.HabitEvaluatorService;
import de.idrinth.habitevaluator.shared.service.HabitScoringService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainController {

    private static final String PLACEHOLDER_USERNAME = "desktop_user";
    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.habit-evaluator";
    private static final String CONFIG_FILE = CONFIG_DIR + "/storage.properties";
    private static final String ALL_CATEGORIES = "All categories";

    @FXML
    private ListView<Habit> habitListView;

    @FXML
    private ComboBox<String> categoryFilterComboBox;

    @FXML
    private Label streakLabel;

    @FXML
    private Label completionRateLabel;

    @FXML
    private ProgressBar completionProgressBar;

    @FXML
    private Button loadDefaultsButton;

    @FXML
    private Label storageModeLabel;

    @FXML
    private Label dailyPointsLabel;

    @FXML
    private Label weeklyPointsLabel;

    @FXML
    private Label monthlyPointsLabel;

    @FXML
    private VBox editHabitsContainer;

    @FXML
    private Button completeButton;

    @FXML
    private Label editMessage;

    private final Map<String, TextField> editTargetFields = new HashMap<>();
    private final Map<String, TextField> editMaxEntriesFields = new HashMap<>();
    private final Map<String, CheckBox> editPositiveScoringBoxes = new HashMap<>();
    private final Map<String, TextField> editThreshold1Fields = new HashMap<>();
    private final Map<String, TextField> editThreshold2Fields = new HashMap<>();
    private final Map<String, TextField> editThreshold4Fields = new HashMap<>();
    private final Map<String, TextField> editThreshold8Fields = new HashMap<>();
    private final ObservableList<Habit> habits = FXCollections.observableArrayList();
    private final ObservableList<Habit> filteredHabits = FXCollections.observableArrayList();
    private final HabitEvaluatorService evaluatorService = new HabitEvaluatorService();
    private final HabitScoringService scoringService = new HabitScoringService();
    private final StorageConfig storageConfig = new StorageConfig(new File(CONFIG_FILE));

    private HabitRepository habitRepository;
    private HabitCategoryRepository categoryRepository;
    private UserRepository userRepository;
    private ApiClient apiClient;
    private User currentUser;
    private HabitRepository localBackupRepository;
    private User localBackupUser;
    private List<HabitCategory> categoryList = new ArrayList<>();
    private final Map<String, String> categoryNameToId = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        initializeStorage();
        loadCategories();
        loadHabits();

        habitListView.setItems(filteredHabits);
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

        categoryFilterComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> applyFilter());

        refreshEditHabits();
        habits.addListener((javafx.collections.ListChangeListener<Habit>) change -> {
            applyFilter();
            refreshEditHabits();
        });
    }

    private void loadCategories() {
        categoryList.clear();
        categoryNameToId.clear();
        if (categoryRepository != null && currentUser != null) {
            categoryList = categoryRepository.findByUserId(currentUser.getId());
        } else if (storageConfig.isRemote() && apiClient != null) {
            try {
                List<HabitCategory> remoteCats = apiClient.get("/api/categories",
                        new TypeToken<List<HabitCategory>>() {}.getType());
                if (remoteCats != null) {
                    categoryList = remoteCats;
                }
            } catch (IOException e) {
                // categories are optional, continue without them
            }
        }
        populateCategoryComboBoxes();
        updateLoadDefaultsButtonVisibility();
    }

    private void populateCategoryComboBoxes() {
        categoryNameToId.clear();
        for (HabitCategory cat : categoryList) {
            categoryNameToId.put(cat.getName(), cat.getId());
        }

        // Populate the filter combo box
        ObservableList<String> filterNames = FXCollections.observableArrayList();
        filterNames.add(ALL_CATEGORIES);
        for (HabitCategory cat : categoryList) {
            filterNames.add(cat.getName());
        }
        filterNames.add("Uncategorized");
        categoryFilterComboBox.setItems(filterNames);
        categoryFilterComboBox.getSelectionModel().selectFirst();
    }

    private void applyFilter() {
        filteredHabits.clear();
        String selected = categoryFilterComboBox.getSelectionModel().getSelectedItem();
        if (selected == null || ALL_CATEGORIES.equals(selected)) {
            filteredHabits.addAll(habits);
        } else if ("Uncategorized".equals(selected)) {
            for (Habit h : habits) {
                if (h.getCategoryId() == null || h.getCategoryId().isEmpty()) {
                    filteredHabits.add(h);
                }
            }
        } else {
            String categoryId = categoryNameToId.get(selected);
            if (categoryId != null) {
                for (Habit h : habits) {
                    if (categoryId.equals(h.getCategoryId())) {
                        filteredHabits.add(h);
                    }
                }
            }
        }
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
        localBackupRepository = null;
        localBackupUser = null;
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

                // Initialize local backup for data safety
                H2HabitRepository h2Backup = new H2HabitRepository();
                H2UserRepository h2UserRepo = new H2UserRepository();
                localBackupRepository = h2Backup;
                localBackupUser = h2UserRepo.findByUsername(PLACEHOLDER_USERNAME)
                        .orElseGet(() -> {
                            User user = new User(PLACEHOLDER_USERNAME, "placeholder");
                            return h2UserRepo.save(user);
                        });

                // Sync remote data to local backup on start
                syncOnStart();
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

    private void syncOnStart() {
        if (localBackupRepository == null || localBackupUser == null) {
            return;
        }
        try {
            SyncService syncService = new SyncService(localBackupRepository);
            syncService.sync(
                    storageConfig.getApiBaseUrl(),
                    storageConfig.getApiUsername(),
                    storageConfig.getApiPassword(),
                    localBackupUser
            );
        } catch (IOException e) {
            // Sync failure on start is non-fatal; remote data will still be used
        }
    }

    /**
     * Called on application shutdown to sync data and save a local backup.
     */
    public void shutdown() {
        if (!storageConfig.isRemote() || localBackupRepository == null || localBackupUser == null) {
            return;
        }
        // Save current habits to local backup
        for (Habit habit : habits) {
            Habit backupCopy = copyHabitForBackup(habit, localBackupUser);
            localBackupRepository.save(backupCopy);
        }
        // Sync local backup with remote to push any final changes
        try {
            SyncService syncService = new SyncService(localBackupRepository);
            syncService.sync(
                    storageConfig.getApiBaseUrl(),
                    storageConfig.getApiUsername(),
                    storageConfig.getApiPassword(),
                    localBackupUser
            );
        } catch (IOException e) {
            // Best effort sync on shutdown; local backup is already saved
        }
    }

    private Habit copyHabitForBackup(Habit source, User backupUser) {
        Habit habit = new Habit(source.getName(), source.getDescription());
        habit.setFrequencyType(source.getFrequencyType());
        habit.setTargetFrequency(source.getTargetFrequency());
        habit.setMaxEntriesPerDay(source.getMaxEntriesPerDay());
        habit.setCategoryId(source.getCategoryId());
        habit.setPositiveScoring(source.isPositiveScoring());
        habit.setScoringRule(source.getScoringRule());
        habit.setUser(backupUser);
        for (HabitEntry sourceEntry : source.getEntries()) {
            HabitEntry entry = new HabitEntry();
            entry.setId(sourceEntry.getId());
            entry.setCompletedAt(sourceEntry.getCompletedAt());
            entry.setNotes(sourceEntry.getNotes());
            entry.setValue(sourceEntry.getValue());
            habit.addEntry(entry);
        }
        return habit;
    }

    private void loadHabits() {
        habits.clear();
        if (currentUser != null) {
            habits.addAll(habitRepository.findByUserId(currentUser.getId()));
        }
        updateLoadDefaultsButtonVisibility();
    }

    private void updateLoadDefaultsButtonVisibility() {
        if (loadDefaultsButton != null) {
            boolean hasData = !categoryList.isEmpty() || !habits.isEmpty();
            loadDefaultsButton.setVisible(!hasData);
            loadDefaultsButton.setManaged(!hasData);
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
            scene.getStylesheets().addAll(habitListView.getScene().getStylesheets());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            if (controller.isSaved()) {
                applyTheme();
                initializeStorage();
                loadCategories();
                loadHabits();
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to open settings: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenStats() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/stats.fxml"));
            Parent root = loader.load();

            StatsController controller = loader.getController();
            controller.setHabits(new ArrayList<>(habits));
            controller.setSleepEntryRepository(new H2SleepEntryRepository());
            controller.setDiaryEntryRepository(new H2DiaryEntryRepository());
            controller.setCurrentUser(currentUser);
            controller.loadData();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Statistics Dashboard");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(habitListView.getScene().getWindow());

            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(habitListView.getScene().getStylesheets());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (IOException e) {
            showAlert("Error", "Failed to open statistics: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenAddHabit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add-habit.fxml"));
            Parent root = loader.load();

            AddHabitController controller = loader.getController();
            controller.setHabitRepository(habitRepository);
            controller.setCategoryRepository(categoryRepository);
            controller.setApiClient(apiClient);
            controller.setCurrentUser(currentUser);
            controller.setCategoryList(categoryList);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add Habit");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(habitListView.getScene().getWindow());

            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(habitListView.getScene().getStylesheets());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            if (controller.getAddedHabit() != null) {
                habits.add(controller.getAddedHabit());
                loadCategories();
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to open add habit dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleCompleteHabit() {
        Habit selectedHabit = habitListView.getSelectionModel().getSelectedItem();
        if (selectedHabit != null) {
            if (selectedHabit.hasReachedDailyLimit(LocalDate.now())) {
                return;
            }
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
            String categoryId = selectedHabit.getCategoryId();
            habitRepository.deleteById(selectedHabit.getId());
            habits.remove(selectedHabit);
            if (categoryId != null && !categoryId.isEmpty() && categoryRepository != null && currentUser != null) {
                boolean categoryStillUsed = habits.stream()
                        .anyMatch(h -> categoryId.equals(h.getCategoryId()));
                if (!categoryStillUsed) {
                    categoryRepository.deleteById(categoryId);
                    loadCategories();
                }
            }
            clearEvaluationDisplay();
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

        dailyPointsLabel.setText("Today: " + scoringService.getCurrentDayScore(habit) + " pts");
        weeklyPointsLabel.setText("This Week: " + scoringService.getCurrentWeekScore(habit) + " pts");
        monthlyPointsLabel.setText("This Month: " + scoringService.getCurrentMonthScore(habit) + " pts");

        if (habit.getMaxEntriesPerDay() != 1) {
            completeButton.setText("Add Completion");
        } else {
            completeButton.setText("Complete");
        }
    }

    private void clearEvaluationDisplay() {
        streakLabel.setText("Current Streak: -");
        completionRateLabel.setText("Completion Rate: -");
        completionProgressBar.setProgress(0);
        dailyPointsLabel.setText("Today: -");
        weeklyPointsLabel.setText("This Week: -");
        monthlyPointsLabel.setText("This Month: -");
    }

    @FXML
    private void handleSync() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/sync.fxml"));
            Parent root = loader.load();

            SyncDialogController controller = loader.getController();
            controller.setHabitRepository(habitRepository);
            controller.setCurrentUser(currentUser);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Sync with Remote Server");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(habitListView.getScene().getWindow());

            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(habitListView.getScene().getStylesheets());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            if (controller.isSynced()) {
                loadHabits();
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to open sync dialog: " + e.getMessage());
        }
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
                Platform.runLater(() -> {
                    loadCategories();
                    loadHabits();
                });
            } catch (IOException e) {
                Platform.runLater(() -> showAlert("Error", "Failed to load default habits: " + e.getMessage()));
            }
        }).start();
    }

    private void refreshEditHabits() {
        editHabitsContainer.getChildren().clear();
        editTargetFields.clear();
        editMaxEntriesFields.clear();
        editPositiveScoringBoxes.clear();
        editThreshold1Fields.clear();
        editThreshold2Fields.clear();
        editThreshold4Fields.clear();
        editThreshold8Fields.clear();
        editMessage.setText("");

        if (habits.isEmpty()) {
            editHabitsContainer.getChildren().add(new Label("No habits found. Add a habit first."));
            return;
        }

        for (Habit habit : habits) {
            VBox habitBox = new VBox(4);
            habitBox.setStyle("-fx-border-color: #444; -fx-border-radius: 4; -fx-padding: 8; -fx-background-color: #333; -fx-background-radius: 4;");

            Label nameLabel = new Label(habit.getName());
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

            if (habit.getDescription() != null && !habit.getDescription().isEmpty()) {
                Label descLabel = new Label(habit.getDescription());
                descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
                habitBox.getChildren().addAll(nameLabel, descLabel);
            } else {
                habitBox.getChildren().add(nameLabel);
            }

            // Target and max/day row
            TextField targetField = new TextField(String.valueOf(habit.getTargetFrequency()));
            targetField.setPrefWidth(60);
            targetField.setPromptText("Target");

            TextField maxEntriesField = new TextField(String.valueOf(habit.getMaxEntriesPerDay()));
            maxEntriesField.setPrefWidth(60);
            maxEntriesField.setPromptText("Max/day");

            CheckBox positiveScoringBox = new CheckBox("Positive scoring");
            positiveScoringBox.setSelected(habit.isPositiveScoring());

            HBox paramsRow = new HBox(8,
                    new Label("Target:"), targetField,
                    new Label("Max/day:"), maxEntriesField,
                    positiveScoringBox);
            paramsRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            habitBox.getChildren().add(paramsRow);

            // Scoring thresholds row
            ScoringRule rule = habit.getScoringRule();
            TextField t1 = new TextField(String.valueOf(rule != null ? rule.getThresholdFor1Point() : 1));
            t1.setPrefWidth(45);
            t1.setPromptText("1pt");
            TextField t2 = new TextField(String.valueOf(rule != null ? rule.getThresholdFor2Points() : 2));
            t2.setPrefWidth(45);
            t2.setPromptText("2pt");
            TextField t4 = new TextField(String.valueOf(rule != null ? rule.getThresholdFor4Points() : 4));
            t4.setPrefWidth(45);
            t4.setPromptText("4pt");
            TextField t8 = new TextField(String.valueOf(rule != null ? rule.getThresholdFor8Points() : 7));
            t8.setPrefWidth(45);
            t8.setPromptText("8pt");

            Label thresholdLabel = new Label("Scoring thresholds:");
            thresholdLabel.setStyle("-fx-font-size: 11px;");

            HBox thresholdRow = new HBox(6,
                    thresholdLabel,
                    new Label("1pt:"), t1,
                    new Label("2pt:"), t2,
                    new Label("4pt:"), t4,
                    new Label("8pt:"), t8);
            thresholdRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            habitBox.getChildren().add(thresholdRow);

            editTargetFields.put(habit.getId(), targetField);
            editMaxEntriesFields.put(habit.getId(), maxEntriesField);
            editPositiveScoringBoxes.put(habit.getId(), positiveScoringBox);
            editThreshold1Fields.put(habit.getId(), t1);
            editThreshold2Fields.put(habit.getId(), t2);
            editThreshold4Fields.put(habit.getId(), t4);
            editThreshold8Fields.put(habit.getId(), t8);

            editHabitsContainer.getChildren().add(habitBox);
        }
    }

    @FXML
    private void handleSaveEditedHabits() {
        int count = 0;
        for (Habit habit : habits) {
            boolean changed = false;

            TextField targetField = editTargetFields.get(habit.getId());
            if (targetField != null) {
                try {
                    int val = Integer.parseInt(targetField.getText().trim());
                    if (val > 0 && val != habit.getTargetFrequency()) {
                        habit.setTargetFrequency(val);
                        changed = true;
                    }
                } catch (NumberFormatException e) {
                    // ignore
                }
            }

            TextField maxField = editMaxEntriesFields.get(habit.getId());
            if (maxField != null) {
                try {
                    int val = Integer.parseInt(maxField.getText().trim());
                    if (val >= 0 && val != habit.getMaxEntriesPerDay()) {
                        habit.setMaxEntriesPerDay(val);
                        changed = true;
                    }
                } catch (NumberFormatException e) {
                    // ignore
                }
            }

            CheckBox posBox = editPositiveScoringBoxes.get(habit.getId());
            if (posBox != null && posBox.isSelected() != habit.isPositiveScoring()) {
                habit.setPositiveScoring(posBox.isSelected());
                changed = true;
            }

            TextField t1 = editThreshold1Fields.get(habit.getId());
            TextField t2 = editThreshold2Fields.get(habit.getId());
            TextField t4 = editThreshold4Fields.get(habit.getId());
            TextField t8 = editThreshold8Fields.get(habit.getId());
            if (t1 != null && t2 != null && t4 != null && t8 != null) {
                try {
                    int v1 = Integer.parseInt(t1.getText().trim());
                    int v2 = Integer.parseInt(t2.getText().trim());
                    int v4 = Integer.parseInt(t4.getText().trim());
                    int v8 = Integer.parseInt(t8.getText().trim());
                    if (v1 >= 0 && v2 >= v1 && v4 >= v2 && v8 >= v4) {
                        ScoringRule rule = habit.getScoringRule();
                        if (rule == null || rule.getThresholdFor1Point() != v1
                                || rule.getThresholdFor2Points() != v2
                                || rule.getThresholdFor4Points() != v4
                                || rule.getThresholdFor8Points() != v8) {
                            String ruleName = rule != null ? rule.getName() : "custom";
                            habit.setScoringRule(new ScoringRule(ruleName, v1, v2, v4, v8));
                            changed = true;
                        }
                    }
                } catch (NumberFormatException e) {
                    // ignore invalid thresholds
                }
            }

            if (changed) {
                habitRepository.save(habit);
                count++;
            }
        }

        editMessage.setText(count + " habit(s) saved successfully");
        editMessage.setStyle("-fx-text-fill: green;");
    }

    private void applyTheme() {
        Scene scene = habitListView.getScene();
        String darkCss = getClass().getResource("/css/dark.css").toExternalForm();
        boolean hasDark = scene.getStylesheets().contains(darkCss);
        boolean wantDark;

        StorageConfig.ThemeMode themeMode = storageConfig.getThemeMode();
        if (themeMode == StorageConfig.ThemeMode.DARK) {
            wantDark = true;
        } else if (themeMode == StorageConfig.ThemeMode.LIGHT) {
            wantDark = false;
        } else {
            // SYSTEM: keep current state (would need app restart for OS detection)
            return;
        }

        if (wantDark && !hasDark) {
            scene.getStylesheets().add(darkCss);
        } else if (!wantDark && hasDark) {
            scene.getStylesheets().remove(darkCss);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
