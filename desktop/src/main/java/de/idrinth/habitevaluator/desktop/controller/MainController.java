package de.idrinth.habitevaluator.desktop.controller;

import de.idrinth.habitevaluator.desktop.persistence.H2HabitCategoryRepository;
import de.idrinth.habitevaluator.desktop.persistence.H2HabitRepository;
import de.idrinth.habitevaluator.desktop.persistence.H2UserRepository;
import de.idrinth.habitevaluator.shared.api.ApiClient;
import de.idrinth.habitevaluator.shared.api.RemoteHabitRepository;
import de.idrinth.habitevaluator.shared.api.RemoteUserRepository;
import de.idrinth.habitevaluator.shared.api.StorageConfig;
import de.idrinth.habitevaluator.shared.model.Evaluation;
import de.idrinth.habitevaluator.shared.model.FrequencyType;
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
    private TextField habitNameField;

    @FXML
    private TextArea habitDescriptionArea;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private ComboBox<String> categoryFilterComboBox;

    @FXML
    private ComboBox<String> frequencyTypeComboBox;

    @FXML
    private TextField targetFrequencyField;

    @FXML
    private TextField maxEntriesPerDayField;

    @FXML
    private CheckBox positiveScoringCheckBox;

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

    @FXML
    private Label dailyPointsLabel;

    @FXML
    private Label weeklyPointsLabel;

    @FXML
    private Label monthlyPointsLabel;

    private final Map<String, CheckBox> trackCheckBoxes = new HashMap<>();
    private final Map<String, TextField> trackValueFields = new HashMap<>();
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
    private List<HabitCategory> categoryList = new ArrayList<>();
    private final Map<String, String> categoryNameToId = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        initializeStorage();
        loadCategories();
        loadHabits();

        // Populate frequency type combo box
        ObservableList<String> frequencyTypes = FXCollections.observableArrayList();
        for (FrequencyType ft : FrequencyType.values()) {
            frequencyTypes.add(ft.name().substring(0, 1) + ft.name().substring(1).toLowerCase());
        }
        frequencyTypeComboBox.setItems(frequencyTypes);
        frequencyTypeComboBox.getSelectionModel().selectFirst();

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

        refreshTrackHabits();
        habits.addListener((javafx.collections.ListChangeListener<Habit>) change -> {
            applyFilter();
            refreshTrackHabits();
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
    }

    private void populateCategoryComboBoxes() {
        // Populate the creation combo box
        ObservableList<String> categoryNames = FXCollections.observableArrayList();
        categoryNames.add("No category");
        categoryNameToId.clear();
        for (HabitCategory cat : categoryList) {
            categoryNames.add(cat.getName());
            categoryNameToId.put(cat.getName(), cat.getId());
        }
        categoryComboBox.setItems(categoryNames);
        categoryComboBox.getSelectionModel().selectFirst();

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
            scene.getStylesheets().addAll(habitListView.getScene().getStylesheets());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            if (controller.isSaved()) {
                initializeStorage();
                loadCategories();
                loadHabits();
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to open settings: " + e.getMessage());
        }
    }

    private void refreshTrackHabits() {
        trackHabitsContainer.getChildren().clear();
        trackCheckBoxes.clear();
        trackValueFields.clear();
        trackMessage.setText("");
        if (habits.isEmpty()) {
            trackHabitsContainer.getChildren().add(new Label("No habits found. Add a habit first."));
            return;
        }

        // Group habits by category
        Map<String, List<Habit>> grouped = new LinkedHashMap<>();
        List<Habit> uncategorized = new ArrayList<>();
        Map<String, HabitCategory> catMap = new HashMap<>();
        for (HabitCategory cat : categoryList) {
            catMap.put(cat.getId(), cat);
        }

        for (Habit habit : habits) {
            if (habit.getCategoryId() != null && catMap.containsKey(habit.getCategoryId())) {
                grouped.computeIfAbsent(habit.getCategoryId(), k -> new ArrayList<>()).add(habit);
            } else {
                uncategorized.add(habit);
            }
        }

        for (Map.Entry<String, List<Habit>> entry : grouped.entrySet()) {
            HabitCategory cat = catMap.get(entry.getKey());
            Label categoryLabel = new Label(cat.getName());
            categoryLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5 0 2 0;" +
                    (isValidColor(cat.getColor()) ? " -fx-text-fill: " + cat.getColor() + ";" : ""));
            trackHabitsContainer.getChildren().add(categoryLabel);
            for (Habit habit : entry.getValue()) {
                addTrackCheckBox(habit);
            }
        }

        if (!uncategorized.isEmpty()) {
            if (!grouped.isEmpty()) {
                Label uncatLabel = new Label("Uncategorized");
                uncatLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5 0 2 0;");
                trackHabitsContainer.getChildren().add(uncatLabel);
            }
            for (Habit habit : uncategorized) {
                addTrackCheckBox(habit);
            }
        }
    }

    private void addTrackCheckBox(Habit habit) {
        boolean atLimit = habit.hasReachedDailyLimit(LocalDate.now());

        CheckBox checkBox = new CheckBox(habit.getName());
        if (atLimit) {
            checkBox.setDisable(true);
            checkBox.setTooltip(new Tooltip("Daily limit reached"));
        } else if (habit.getDescription() != null && !habit.getDescription().isEmpty()) {
            checkBox.setTooltip(new Tooltip(habit.getDescription()));
        }

        // Build info tooltip with habit parameters
        StringBuilder info = new StringBuilder();
        info.append(habit.getFrequencyType().name().toLowerCase());
        info.append(", target: ").append(habit.getTargetFrequency());
        if (habit.getMaxEntriesPerDay() > 0) {
            info.append(", max/day: ").append(habit.getMaxEntriesPerDay());
        }
        info.append(", scoring: ").append(habit.isPositiveScoring() ? "+" : "-");
        ScoringRule rule = habit.getScoringRule();
        if (rule != null) {
            info.append(" (").append(rule.getThresholdFor1Point())
                .append("/").append(rule.getThresholdFor2Points())
                .append("/").append(rule.getThresholdFor4Points())
                .append("/").append(rule.getThresholdFor8Points())
                .append(")");
        }

        Label infoLabel = new Label(info.toString());
        infoLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888;");

        TextField valueField = new TextField("1");
        valueField.setPrefWidth(50);
        valueField.setPromptText("Val");
        valueField.setDisable(atLimit);

        HBox row = new HBox(8, checkBox, valueField, infoLabel);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        if (atLimit) {
            row.setOpacity(0.5);
        }

        trackCheckBoxes.put(habit.getId(), checkBox);
        trackValueFields.put(habit.getId(), valueField);
        trackHabitsContainer.getChildren().add(row);
    }

    @FXML
    private void handleAddHabit() {
        String name = habitNameField.getText().trim();
        String description = habitDescriptionArea.getText().trim();

        if (!name.isEmpty()) {
            Habit habit = new Habit(name, description);
            habit.setUser(currentUser);

            String selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
            if (selectedCategory != null && !"No category".equals(selectedCategory)) {
                String catId = categoryNameToId.get(selectedCategory);
                if (catId != null) {
                    habit.setCategoryId(catId);
                }
            }

            // Set frequency type
            int freqIdx = frequencyTypeComboBox.getSelectionModel().getSelectedIndex();
            if (freqIdx >= 0 && freqIdx < FrequencyType.values().length) {
                habit.setFrequencyType(FrequencyType.values()[freqIdx]);
            }

            // Set target frequency
            try {
                int targetFreq = Integer.parseInt(targetFrequencyField.getText().trim());
                if (targetFreq > 0) {
                    habit.setTargetFrequency(targetFreq);
                }
            } catch (NumberFormatException e) {
                // keep default
            }

            // Set max entries per day
            try {
                int maxEntries = Integer.parseInt(maxEntriesPerDayField.getText().trim());
                if (maxEntries > 0) {
                    habit.setMaxEntriesPerDay(maxEntries);
                }
            } catch (NumberFormatException e) {
                // keep default
            }

            // Set positive/negative scoring
            habit.setPositiveScoring(positiveScoringCheckBox.isSelected());

            habitRepository.save(habit);
            habits.add(habit);
            clearInputFields();
        }
    }

    @FXML
    private void handleCompleteHabit() {
        Habit selectedHabit = habitListView.getSelectionModel().getSelectedItem();
        if (selectedHabit != null) {
            if (selectedHabit.hasReachedDailyLimit(LocalDate.now())) {
                trackMessage.setText("Daily limit reached for this habit");
                trackMessage.setStyle("-fx-text-fill: red;");
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
        int skipped = 0;
        for (Habit habit : checkedHabits) {
            if (habit.hasReachedDailyLimit(LocalDate.now())) {
                skipped++;
                continue;
            }
            HabitEntry entry = new HabitEntry(habit.getId());
            // Read value from input field
            TextField valueField = trackValueFields.get(habit.getId());
            if (valueField != null) {
                try {
                    int val = Integer.parseInt(valueField.getText().trim());
                    if (val > 0) {
                        entry.setValue(val);
                    }
                } catch (NumberFormatException e) {
                    // keep default value of 1
                }
            }
            habit.addEntry(entry);
            habitRepository.save(habit);
            count++;
        }
        String message = count + " habit(s) tracked successfully";
        if (skipped > 0) {
            message += " (" + skipped + " skipped - daily limit reached)";
        }
        trackMessage.setText(message);
        trackMessage.setStyle("-fx-text-fill: green;");

        // Uncheck all boxes and reset values
        for (CheckBox checkBox : trackCheckBoxes.values()) {
            checkBox.setSelected(false);
        }
        for (TextField valueField : trackValueFields.values()) {
            valueField.setText("1");
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
    }

    private void clearInputFields() {
        habitNameField.clear();
        habitDescriptionArea.clear();
        categoryComboBox.getSelectionModel().selectFirst();
        frequencyTypeComboBox.getSelectionModel().selectFirst();
        targetFrequencyField.setText("1");
        maxEntriesPerDayField.setText("1");
        positiveScoringCheckBox.setSelected(true);
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

    private static boolean isValidColor(String color) {
        if (color == null || color.isEmpty()) {
            return false;
        }
        return color.matches("^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$")
                || color.matches("^[a-zA-Z]{1,20}$");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
