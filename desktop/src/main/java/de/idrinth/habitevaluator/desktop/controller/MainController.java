package de.idrinth.habitevaluator.desktop.controller;

import de.idrinth.habitevaluator.desktop.persistence.H2DiaryEntryRepository;
import de.idrinth.habitevaluator.desktop.persistence.H2EmotionEntryRepository;
import de.idrinth.habitevaluator.desktop.persistence.H2EmotionPairRepository;
import de.idrinth.habitevaluator.desktop.persistence.H2HabitCategoryRepository;
import de.idrinth.habitevaluator.desktop.persistence.H2HabitRepository;
import de.idrinth.habitevaluator.desktop.persistence.H2SleepEntryRepository;
import de.idrinth.habitevaluator.desktop.persistence.H2UserRepository;
import de.idrinth.habitevaluator.shared.api.ApiClient;
import de.idrinth.habitevaluator.shared.api.RemoteHabitRepository;
import de.idrinth.habitevaluator.shared.api.RemoteUserRepository;
import de.idrinth.habitevaluator.shared.api.StorageConfig;
import de.idrinth.habitevaluator.shared.api.SyncService;
import de.idrinth.habitevaluator.shared.backup.BackupException;
import de.idrinth.habitevaluator.shared.backup.BackupService;
import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.Evaluation;
import de.idrinth.habitevaluator.shared.model.EventSignificance;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.ScoringRule;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.service.DefaultDataInitializer;
import de.idrinth.habitevaluator.shared.service.DiaryService;
import de.idrinth.habitevaluator.shared.service.HabitEvaluatorService;
import de.idrinth.habitevaluator.shared.service.HabitScoringService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainController {

    private static final String PLACEHOLDER_USERNAME = "desktop_user";
    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.habit-evaluator";
    private static final String CONFIG_FILE = CONFIG_DIR + "/storage.properties";
    private static final String BACKUP_DIR = CONFIG_DIR + "/backups";
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

    @FXML
    private ToggleButton weekToggle;

    @FXML
    private ToggleButton monthToggle;

    @FXML
    private Label chartTotalLabel;

    @FXML
    private Label chartAverageLabel;

    @FXML
    private BarChart<String, Number> dailyPointsChart;

    @FXML
    private BarChart<String, Number> runningAvgChart;

    @FXML
    private BarChart<String, Number> cumulativeChart;

    private boolean chartShowingWeek = true;

    @FXML
    private Label diaryTodayPointsLabel;

    @FXML
    private Label diaryWeekPointsLabel;

    @FXML
    private Label diaryMonthPointsLabel;

    @FXML
    private Label diaryWeeklyAvgLabel;

    @FXML
    private Label diaryTrendLabel;

    @FXML
    private TextField diaryDescriptionField;

    @FXML
    private DatePicker diaryDatePicker;

    @FXML
    private ComboBox<String> diarySignificanceComboBox;

    @FXML
    private VBox diaryEntriesContainer;

    private final Map<String, TextField> editTargetFields = new HashMap<>();
    private final Map<String, TextField> editMaxEntriesFields = new HashMap<>();
    private final Map<String, CheckBox> editPositiveScoringBoxes = new HashMap<>();
    private final Map<String, TextField> editThreshold1Fields = new HashMap<>();
    private final Map<String, TextField> editThreshold2Fields = new HashMap<>();
    private final Map<String, TextField> editThreshold4Fields = new HashMap<>();
    private final Map<String, TextField> editThreshold8Fields = new HashMap<>();
    private final Map<String, Map<String, TextField>> editNameTranslationFields = new HashMap<>();
    private final Map<String, Map<String, TextField>> editDescTranslationFields = new HashMap<>();
    private static final String[] TRANSLATION_LANGUAGES = {"en", "de", "es", "fr"};
    private static final String[] TRANSLATION_LANGUAGE_LABELS = {"English", "Deutsch", "Español", "Français"};
    private final ObservableList<Habit> habits = FXCollections.observableArrayList();
    private final ObservableList<Habit> filteredHabits = FXCollections.observableArrayList();
    private final HabitEvaluatorService evaluatorService = new HabitEvaluatorService();
    private final HabitScoringService scoringService = new HabitScoringService();
    private final DiaryService diaryService = new DiaryService();
    private final BackupService backupService = new BackupService();
    private final StorageConfig storageConfig = new StorageConfig(new File(CONFIG_FILE));

    private HabitRepository habitRepository;
    private HabitCategoryRepository categoryRepository;
    private UserRepository userRepository;
    private DiaryEntryRepository diaryEntryRepository;
    private SleepEntryRepository sleepEntryRepository;
    private EmotionPairRepository emotionPairRepository;
    private EmotionEntryRepository emotionEntryRepository;
    private ApiClient apiClient;
    private User currentUser;
    private List<DiaryEntry> diaryEntries = new ArrayList<>();
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

        weekToggle.setSelected(true);
        monthToggle.setSelected(false);

        refreshEditHabits();
        habits.addListener((javafx.collections.ListChangeListener<Habit>) change -> {
            applyFilter();
            refreshEditHabits();
        });

        // Initialize diary UI
        diaryDatePicker.setValue(LocalDate.now());
        diarySignificanceComboBox.setItems(FXCollections.observableArrayList("Minor (1pt)", "Normal (2pt)", "Major (4pt)"));
        diarySignificanceComboBox.getSelectionModel().select(1);
        loadDiaryEntries();

        performDailyBackupIfEnabled();
        showFirstStartDialogIfNeeded();
    }

    private void showFirstStartDialogIfNeeded() {
        if (storageConfig.isFirstStartCompleted()) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Welcome to Habit Evaluator");
        alert.setHeaderText("Welcome to Habit Evaluator");
        alert.setContentText(
                "This app is a self-tracking tool and does not replace professional medical "
                + "or psychological help. If you are struggling, please reach out to a qualified professional."
                + "\n\n"
                + "By default, all your data stays on your device and is not shared with anyone. "
                + "You can optionally configure remote storage in the settings.");
        ButtonType acknowledgeButton = new ButtonType("I understand", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(acknowledgeButton);
        alert.showAndWait();
        storageConfig.setFirstStartCompleted(true);
        storageConfig.save();
    }

    private void performDailyBackupIfEnabled() {
        if (!storageConfig.isBackupEnabled()) {
            return;
        }
        String password = storageConfig.getBackupPassword();
        if (password == null || password.isEmpty()) {
            return;
        }
        File backupDir = new File(BACKUP_DIR);
        if (backupService.hasTodaysBackup(backupDir)) {
            return;
        }
        new Thread(() -> {
            try {
                backupService.createBackup(backupDir, password, currentUser,
                        habitRepository, categoryRepository,
                        diaryEntryRepository, sleepEntryRepository);
            } catch (BackupException e) {
                Platform.runLater(() ->
                        showAlert("Backup Error", "Daily backup failed: " + e.getMessage()));
            }
        }).start();
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
        diaryEntryRepository = new H2DiaryEntryRepository();
        sleepEntryRepository = new H2SleepEntryRepository();
        emotionPairRepository = new H2EmotionPairRepository();
        emotionEntryRepository = new H2EmotionEntryRepository();
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

                // Diary and emotions always use local storage on desktop
                diaryEntryRepository = new H2DiaryEntryRepository();
                emotionPairRepository = new H2EmotionPairRepository();
                emotionEntryRepository = new H2EmotionEntryRepository();

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
        // Create encrypted backup on shutdown if enabled and not yet done today
        performDailyBackupIfEnabled();

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
    private void handleOpenImprint() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/imprint.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Project legal");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(habitListView.getScene().getWindow());

            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(habitListView.getScene().getStylesheets());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (IOException e) {
            showAlert("Error", "Failed to open imprint: " + e.getMessage());
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
            controller.setEmotionEntryRepository(new H2EmotionEntryRepository());
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
            controller.setStorageConfig(storageConfig);
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
    private void handleOpenSleepTracking() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/sleep-tracking.fxml"));
            Parent root = loader.load();

            SleepTrackingController controller = loader.getController();
            controller.setSleepEntryRepository(new H2SleepEntryRepository());
            controller.setCurrentUser(currentUser);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Sleep Tracking");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(habitListView.getScene().getWindow());

            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(habitListView.getScene().getStylesheets());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (IOException e) {
            showAlert("Error", "Failed to open sleep tracking: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenEmotionPairs() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/emotion-pairs.fxml"));
            Parent root = loader.load();

            EmotionPairController controller = loader.getController();
            controller.setEmotionPairRepository(emotionPairRepository);
            controller.setEmotionEntryRepository(emotionEntryRepository);
            controller.setCurrentUser(currentUser);
            controller.loadData();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Emotion Pairs");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(habitListView.getScene().getWindow());

            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(habitListView.getScene().getStylesheets());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (IOException e) {
            showAlert("Error", "Failed to open emotion pairs: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenEmotionEntry() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/emotion-entry.fxml"));
            Parent root = loader.load();

            EmotionEntryController controller = loader.getController();
            controller.setEmotionPairRepository(emotionPairRepository);
            controller.setEmotionEntryRepository(emotionEntryRepository);
            controller.setCurrentUser(currentUser);
            controller.loadData();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Record Emotion");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(habitListView.getScene().getWindow());

            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(habitListView.getScene().getStylesheets());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (IOException e) {
            showAlert("Error", "Failed to open emotion entry: " + e.getMessage());
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

        updatePointCharts(habit);

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
        clearPointCharts();
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
    private void handleExportPdf() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/pdf-export.fxml"));
            Parent root = loader.load();

            PdfExportController controller = loader.getController();
            controller.setHabitRepository(habitRepository);
            controller.setDiaryEntryRepository(diaryEntryRepository);
            controller.setSleepEntryRepository(sleepEntryRepository);
            controller.setEmotionEntryRepository(emotionEntryRepository);
            controller.setCurrentUser(currentUser);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Export PDF");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(habitListView.getScene().getWindow());

            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(habitListView.getScene().getStylesheets());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (IOException e) {
            showAlert("Error", "Failed to open PDF export dialog: " + e.getMessage());
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
        editNameTranslationFields.clear();
        editDescTranslationFields.clear();
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

            if (storageConfig.isCustomTranslationsEnabled()) {
                Label translationsLabel = new Label("Translations:");
                translationsLabel.setStyle("-fx-font-size: 11px; -fx-padding: 4 0 0 0;");
                habitBox.getChildren().add(translationsLabel);

                Map<String, TextField> nameFields = new HashMap<>();
                Map<String, TextField> descFields = new HashMap<>();

                for (int i = 0; i < TRANSLATION_LANGUAGES.length; i++) {
                    String lang = TRANSLATION_LANGUAGES[i];
                    String label = TRANSLATION_LANGUAGE_LABELS[i];

                    TextField nameField = new TextField(
                            habit.getNameTranslations().getOrDefault(lang, ""));
                    nameField.setPrefWidth(150);
                    nameField.setPromptText("Name (" + label + ")");

                    TextField descField = new TextField(
                            habit.getDescriptionTranslations().getOrDefault(lang, ""));
                    descField.setPrefWidth(200);
                    descField.setPromptText("Description (" + label + ")");

                    HBox translationRow = new HBox(6,
                            new Label(label + ":"), nameField, descField);
                    translationRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    habitBox.getChildren().add(translationRow);

                    nameFields.put(lang, nameField);
                    descFields.put(lang, descField);
                }

                editNameTranslationFields.put(habit.getId(), nameFields);
                editDescTranslationFields.put(habit.getId(), descFields);
            }

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

            Map<String, TextField> nameTransFields = editNameTranslationFields.get(habit.getId());
            Map<String, TextField> descTransFields = editDescTranslationFields.get(habit.getId());
            if (nameTransFields != null && descTransFields != null) {
                Map<String, String> nameTranslations = new HashMap<>();
                Map<String, String> descTranslations = new HashMap<>();
                for (String lang : TRANSLATION_LANGUAGES) {
                    TextField nf = nameTransFields.get(lang);
                    if (nf != null && !nf.getText().trim().isEmpty()) {
                        nameTranslations.put(lang, nf.getText().trim());
                    }
                    TextField df = descTransFields.get(lang);
                    if (df != null && !df.getText().trim().isEmpty()) {
                        descTranslations.put(lang, df.getText().trim());
                    }
                }
                if (!nameTranslations.equals(habit.getNameTranslations())
                        || !descTranslations.equals(habit.getDescriptionTranslations())) {
                    habit.setNameTranslations(nameTranslations);
                    habit.setDescriptionTranslations(descTranslations);
                    changed = true;
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

    @FXML
    private void handleWeekToggle() {
        chartShowingWeek = true;
        weekToggle.setSelected(true);
        monthToggle.setSelected(false);
        Habit selectedHabit = habitListView.getSelectionModel().getSelectedItem();
        if (selectedHabit != null) {
            updatePointCharts(selectedHabit);
        }
    }

    @FXML
    private void handleMonthToggle() {
        chartShowingWeek = false;
        weekToggle.setSelected(false);
        monthToggle.setSelected(true);
        Habit selectedHabit = habitListView.getSelectionModel().getSelectedItem();
        if (selectedHabit != null) {
            updatePointCharts(selectedHabit);
        }
    }

    private void updatePointCharts(Habit habit) {
        LocalDate today = LocalDate.now();
        LocalDate start;
        LocalDate end;

        if (chartShowingWeek) {
            start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            end = start.plusDays(6);
        } else {
            start = today.with(TemporalAdjusters.firstDayOfMonth());
            end = today.with(TemporalAdjusters.lastDayOfMonth());
        }

        DateTimeFormatter dayFormat = chartShowingWeek
                ? DateTimeFormatter.ofPattern("EEE")
                : DateTimeFormatter.ofPattern("d");

        int daysInPeriod = (int) (start.until(end, java.time.temporal.ChronoUnit.DAYS)) + 1;

        List<Integer> dailyPoints = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int totalPoints = 0;

        for (int i = 0; i < daysInPeriod; i++) {
            LocalDate date = start.plusDays(i);
            int dayScore = scoringService.calculateHabitScore(habit, date, date).getScore();
            dailyPoints.add(dayScore);
            totalPoints += dayScore;

            if (!chartShowingWeek) {
                if (i == 0 || i == daysInPeriod - 1 || (i + 1) % 5 == 0) {
                    labels.add(date.format(dayFormat));
                } else {
                    labels.add("");
                }
            } else {
                labels.add(date.format(dayFormat));
            }
        }

        double average = totalPoints / (double) daysInPeriod;

        chartTotalLabel.setText("Total: " + totalPoints + " pts");
        chartAverageLabel.setText(String.format("Avg: %.1f pts", average));

        // Daily points chart
        XYChart.Series<String, Number> dailySeries = new XYChart.Series<>();
        for (int i = 0; i < dailyPoints.size(); i++) {
            dailySeries.getData().add(new XYChart.Data<>(labels.get(i).isEmpty() ? String.valueOf(i + 1) : labels.get(i), dailyPoints.get(i)));
        }
        dailyPointsChart.getData().clear();
        dailyPointsChart.getData().add(dailySeries);

        // Running average chart
        XYChart.Series<String, Number> avgSeries = new XYChart.Series<>();
        int runningTotal = 0;
        for (int i = 0; i < dailyPoints.size(); i++) {
            runningTotal += dailyPoints.get(i);
            int runningAvg = Math.round((float) runningTotal / (i + 1));
            avgSeries.getData().add(new XYChart.Data<>(labels.get(i).isEmpty() ? String.valueOf(i + 1) : labels.get(i), runningAvg));
        }
        runningAvgChart.getData().clear();
        runningAvgChart.getData().add(avgSeries);

        // Cumulative total chart
        XYChart.Series<String, Number> cumSeries = new XYChart.Series<>();
        int cumulative = 0;
        for (int i = 0; i < dailyPoints.size(); i++) {
            cumulative += dailyPoints.get(i);
            cumSeries.getData().add(new XYChart.Data<>(labels.get(i).isEmpty() ? String.valueOf(i + 1) : labels.get(i), cumulative));
        }
        cumulativeChart.getData().clear();
        cumulativeChart.getData().add(cumSeries);
    }

    private void clearPointCharts() {
        chartTotalLabel.setText("Total: -");
        chartAverageLabel.setText("Avg: -");
        dailyPointsChart.getData().clear();
        runningAvgChart.getData().clear();
        cumulativeChart.getData().clear();
    }

    private void loadDiaryEntries() {
        diaryEntries.clear();
        if (diaryEntryRepository != null && currentUser != null) {
            diaryEntries = new ArrayList<>(diaryEntryRepository.findByUserId(currentUser.getId()));
            diaryEntries.sort(Comparator.comparing(DiaryEntry::getEventDate)
                    .thenComparing(DiaryEntry::getCreatedAt).reversed());
        }
        refreshDiaryStats();
        refreshDiaryEntriesList();
    }

    private void refreshDiaryStats() {
        if (diaryEntries.isEmpty()) {
            diaryTodayPointsLabel.setText("Today: 0 pts");
            diaryWeekPointsLabel.setText("This Week: 0 pts");
            diaryMonthPointsLabel.setText("This Month: 0 pts");
            diaryWeeklyAvgLabel.setText("Weekly Avg: 0.0");
            diaryTrendLabel.setText("Trend: -");
            return;
        }
        int todayPts = diaryService.getDayPoints(diaryEntries, LocalDate.now());
        int weekPts = diaryService.getCurrentWeekPoints(diaryEntries);
        int monthPts = diaryService.getCurrentMonthPoints(diaryEntries);
        double weeklyAvg = diaryService.getWeeklyAverageForMonth(diaryEntries);
        double trend = diaryService.getMonthlyTrend(diaryEntries);

        diaryTodayPointsLabel.setText("Today: " + todayPts + " pts");
        diaryWeekPointsLabel.setText("This Week: " + weekPts + " pts");
        diaryMonthPointsLabel.setText("This Month: " + monthPts + " pts");
        diaryWeeklyAvgLabel.setText(String.format("Weekly Avg: %.1f", weeklyAvg));

        if (trend > 0.01) {
            diaryTrendLabel.setText(String.format("Trend: +%.0f%%", trend * 100));
            diaryTrendLabel.setStyle("-fx-text-fill: green;");
        } else if (trend < -0.01) {
            diaryTrendLabel.setText(String.format("Trend: %.0f%%", trend * 100));
            diaryTrendLabel.setStyle("-fx-text-fill: red;");
        } else {
            diaryTrendLabel.setText("Trend: Stable");
            diaryTrendLabel.setStyle("");
        }
    }

    private void refreshDiaryEntriesList() {
        diaryEntriesContainer.getChildren().clear();
        if (diaryEntries.isEmpty()) {
            diaryEntriesContainer.getChildren().add(new Label("No diary entries yet."));
            return;
        }
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (DiaryEntry entry : diaryEntries) {
            HBox row = new HBox(10);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setStyle("-fx-border-color: #444; -fx-border-radius: 4; -fx-padding: 6; -fx-background-color: #333; -fx-background-radius: 4;");

            Label dateLabel = new Label(entry.getEventDate().format(dateFmt));
            dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
            dateLabel.setPrefWidth(80);

            Label sigLabel = new Label(entry.getSignificance().name() + " (" + entry.getPoints() + "pt)");
            sigLabel.setStyle("-fx-font-size: 11px;");
            sigLabel.setPrefWidth(100);

            Label descLabel = new Label(entry.getDescription());
            descLabel.setStyle("-fx-font-size: 12px;");
            HBox.setHgrow(descLabel, javafx.scene.layout.Priority.ALWAYS);

            Button deleteBtn = new Button("X");
            deleteBtn.setStyle("-fx-font-size: 10px; -fx-padding: 2 6;");
            deleteBtn.setOnAction(e -> {
                diaryEntryRepository.deleteById(entry.getId());
                loadDiaryEntries();
            });

            row.getChildren().addAll(dateLabel, sigLabel, descLabel, deleteBtn);
            diaryEntriesContainer.getChildren().add(row);
        }
    }

    @FXML
    private void handleAddDiaryEntry() {
        String description = diaryDescriptionField.getText();
        if (description == null || description.trim().isEmpty()) {
            return;
        }
        LocalDate date = diaryDatePicker.getValue();
        if (date == null) {
            date = LocalDate.now();
        }
        int selectedIndex = diarySignificanceComboBox.getSelectionModel().getSelectedIndex();
        EventSignificance significance;
        switch (selectedIndex) {
            case 0:
                significance = EventSignificance.MINOR;
                break;
            case 2:
                significance = EventSignificance.MAJOR;
                break;
            default:
                significance = EventSignificance.NORMAL;
                break;
        }
        DiaryEntry entry = new DiaryEntry(description.trim(), significance, date);
        entry.setUser(currentUser);
        diaryEntryRepository.save(entry);
        diaryDescriptionField.clear();
        diaryDatePicker.setValue(LocalDate.now());
        diarySignificanceComboBox.getSelectionModel().select(1);
        loadDiaryEntries();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
