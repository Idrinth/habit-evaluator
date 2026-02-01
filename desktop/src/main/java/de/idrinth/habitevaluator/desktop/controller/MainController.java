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
import java.util.Optional;

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
    private static final String NEW_CATEGORY = "New category";

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
    private Label editMessage;

    private final Map<String, CheckBox> trackCheckBoxes = new HashMap<>();
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
        refreshEditHabits();
        habits.addListener((javafx.collections.ListChangeListener<Habit>) change -> {
            applyFilter();
            refreshTrackHabits();
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
        // Populate the creation combo box
        ObservableList<String> categoryNames = FXCollections.observableArrayList();
        categoryNames.add(NEW_CATEGORY);
        categoryNameToId.clear();
        for (HabitCategory cat : categoryList) {
            categoryNames.add(cat.getName());
            categoryNameToId.put(cat.getName(), cat.getId());
        }
        categoryComboBox.setItems(categoryNames);
        // Select first actual category if available
        if (categoryList.size() > 0) {
            categoryComboBox.getSelectionModel().select(1);
        } else {
            categoryComboBox.getSelectionModel().selectFirst();
        }
        categoryComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (NEW_CATEGORY.equals(newValue)) {
                        showNewCategoryDialog();
                    }
                });

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

    private void showNewCategoryDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Category");
        dialog.setHeaderText(null);
        dialog.setContentText("Category name:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            createCategory(result.get().trim());
        } else {
            // Revert selection to first actual category or stay on New category
            if (categoryList.size() > 0) {
                categoryComboBox.getSelectionModel().select(1);
            }
        }
    }

    private void createCategory(String name) {
        HabitCategory category = new HabitCategory(name);
        category.setUser(currentUser);
        if (categoryRepository != null) {
            categoryRepository.save(category);
            categoryList.add(category);
            populateCategoryComboBoxes();
            // Select the newly created category
            categoryComboBox.getSelectionModel().select(name);
        } else if (storageConfig.isRemote() && apiClient != null) {
            new Thread(() -> {
                try {
                    Map<String, String> body = new LinkedHashMap<>();
                    body.put("name", name);
                    HabitCategory created = apiClient.post("/api/categories", body, HabitCategory.class);
                    Platform.runLater(() -> {
                        categoryList.add(created);
                        populateCategoryComboBoxes();
                        categoryComboBox.getSelectionModel().select(created.getName());
                    });
                } catch (IOException e) {
                    Platform.runLater(() -> showAlert("Error", "Failed to create category: " + e.getMessage()));
                }
            }).start();
        }
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

        // Build info label with habit parameters
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

        HBox row = new HBox(8, checkBox, infoLabel);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        if (atLimit) {
            row.setOpacity(0.5);
        }

        trackCheckBoxes.put(habit.getId(), checkBox);
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
            if (selectedCategory == null || NEW_CATEGORY.equals(selectedCategory)) {
                showAlert("Category Required", "Please select or create a category before adding a habit.");
                return;
            }
            String catId = categoryNameToId.get(selectedCategory);
            if (catId != null) {
                habit.setCategoryId(catId);
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
        refreshTrackHabits();
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
