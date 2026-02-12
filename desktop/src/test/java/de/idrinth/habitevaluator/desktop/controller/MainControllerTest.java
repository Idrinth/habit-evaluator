package de.idrinth.habitevaluator.desktop.controller;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EventSignificance;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MainControllerTest extends JavaFXControllerTestBase {

    private MainController controller;
    private ListView<Habit> habitListView;
    private ComboBox<String> categoryFilterComboBox;
    private Label streakLabel;
    private Label completionRateLabel;
    private ProgressBar completionProgressBar;
    private Button loadDefaultsButton;
    private Button statsButton;
    private Button exportPdfButton;
    private Button exportBackupButton;
    private Button importBackupButton;
    private Button sleepTrackingButton;
    private Button emotionPairsButton;
    private Button recordEmotionButton;
    private Label storageModeLabel;
    private Label dailyPointsLabel;
    private Label weeklyPointsLabel;
    private Label monthlyPointsLabel;
    private VBox editHabitsContainer;
    private Button completeButton;
    private Label editMessage;
    private ToggleButton weekToggle;
    private ToggleButton monthToggle;
    private Label chartTotalLabel;
    private Label chartAverageLabel;
    private BarChart<String, Number> dailyPointsChart;
    private BarChart<String, Number> runningAvgChart;
    private BarChart<String, Number> cumulativeChart;
    private Label diaryTodayPointsLabel;
    private Label diaryWeekPointsLabel;
    private Label diaryMonthPointsLabel;
    private Label diaryWeeklyAvgLabel;
    private Label diaryTrendLabel;
    private TextField diaryDescriptionField;
    private DatePicker diaryDatePicker;
    private ComboBox<String> diarySignificanceComboBox;
    private VBox diaryEntriesContainer;

    private StubHabitRepository habitRepository;
    private StubHabitCategoryRepository categoryRepository;
    private StubDiaryEntryRepository diaryEntryRepository;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        controller = new MainController();
        habitRepository = new StubHabitRepository();
        categoryRepository = new StubHabitCategoryRepository();
        diaryEntryRepository = new StubDiaryEntryRepository();
        testUser = new User("testuser", "password");

        habitListView = new ListView<>();
        categoryFilterComboBox = new ComboBox<>();
        streakLabel = new Label();
        completionRateLabel = new Label();
        completionProgressBar = new ProgressBar();
        loadDefaultsButton = new Button();
        statsButton = new Button();
        exportPdfButton = new Button();
        exportBackupButton = new Button();
        importBackupButton = new Button();
        sleepTrackingButton = new Button();
        emotionPairsButton = new Button();
        recordEmotionButton = new Button();
        storageModeLabel = new Label();
        dailyPointsLabel = new Label();
        weeklyPointsLabel = new Label();
        monthlyPointsLabel = new Label();
        editHabitsContainer = new VBox();
        completeButton = new Button();
        editMessage = new Label();
        weekToggle = new ToggleButton();
        monthToggle = new ToggleButton();
        chartTotalLabel = new Label();
        chartAverageLabel = new Label();

        CategoryAxis dailyXAxis = new CategoryAxis();
        NumberAxis dailyYAxis = new NumberAxis();
        dailyPointsChart = new BarChart<>(dailyXAxis, dailyYAxis);

        CategoryAxis avgXAxis = new CategoryAxis();
        NumberAxis avgYAxis = new NumberAxis();
        runningAvgChart = new BarChart<>(avgXAxis, avgYAxis);

        CategoryAxis cumXAxis = new CategoryAxis();
        NumberAxis cumYAxis = new NumberAxis();
        cumulativeChart = new BarChart<>(cumXAxis, cumYAxis);

        diaryTodayPointsLabel = new Label();
        diaryWeekPointsLabel = new Label();
        diaryMonthPointsLabel = new Label();
        diaryWeeklyAvgLabel = new Label();
        diaryTrendLabel = new Label();
        diaryDescriptionField = new TextField();
        diaryDatePicker = new DatePicker();
        diarySignificanceComboBox = new ComboBox<>();
        diaryEntriesContainer = new VBox();

        setField(controller, "habitListView", habitListView);
        setField(controller, "categoryFilterComboBox", categoryFilterComboBox);
        setField(controller, "streakLabel", streakLabel);
        setField(controller, "completionRateLabel", completionRateLabel);
        setField(controller, "completionProgressBar", completionProgressBar);
        setField(controller, "loadDefaultsButton", loadDefaultsButton);
        setField(controller, "statsButton", statsButton);
        setField(controller, "exportPdfButton", exportPdfButton);
        setField(controller, "exportBackupButton", exportBackupButton);
        setField(controller, "importBackupButton", importBackupButton);
        setField(controller, "sleepTrackingButton", sleepTrackingButton);
        setField(controller, "emotionPairsButton", emotionPairsButton);
        setField(controller, "recordEmotionButton", recordEmotionButton);
        setField(controller, "storageModeLabel", storageModeLabel);
        setField(controller, "dailyPointsLabel", dailyPointsLabel);
        setField(controller, "weeklyPointsLabel", weeklyPointsLabel);
        setField(controller, "monthlyPointsLabel", monthlyPointsLabel);
        setField(controller, "editHabitsContainer", editHabitsContainer);
        setField(controller, "completeButton", completeButton);
        setField(controller, "editMessage", editMessage);
        setField(controller, "weekToggle", weekToggle);
        setField(controller, "monthToggle", monthToggle);
        setField(controller, "chartTotalLabel", chartTotalLabel);
        setField(controller, "chartAverageLabel", chartAverageLabel);
        setField(controller, "dailyPointsChart", dailyPointsChart);
        setField(controller, "runningAvgChart", runningAvgChart);
        setField(controller, "cumulativeChart", cumulativeChart);
        setField(controller, "diaryTodayPointsLabel", diaryTodayPointsLabel);
        setField(controller, "diaryWeekPointsLabel", diaryWeekPointsLabel);
        setField(controller, "diaryMonthPointsLabel", diaryMonthPointsLabel);
        setField(controller, "diaryWeeklyAvgLabel", diaryWeeklyAvgLabel);
        setField(controller, "diaryTrendLabel", diaryTrendLabel);
        setField(controller, "diaryDescriptionField", diaryDescriptionField);
        setField(controller, "diaryDatePicker", diaryDatePicker);
        setField(controller, "diarySignificanceComboBox", diarySignificanceComboBox);
        setField(controller, "diaryEntriesContainer", diaryEntriesContainer);

        // Inject repositories and user directly via reflection
        setField(controller, "habitRepository", habitRepository);
        setField(controller, "categoryRepository", categoryRepository);
        setField(controller, "diaryEntryRepository", diaryEntryRepository);
        setField(controller, "currentUser", testUser);
    }

    @Test
    void testClearEvaluationDisplayResetsLabels() throws Exception {
        streakLabel.setText("something");
        completionRateLabel.setText("something");
        completionProgressBar.setProgress(0.5);
        dailyPointsLabel.setText("something");
        weeklyPointsLabel.setText("something");
        monthlyPointsLabel.setText("something");
        chartTotalLabel.setText("something");
        chartAverageLabel.setText("something");

        Method clearEval = MainController.class.getDeclaredMethod("clearEvaluationDisplay");
        clearEval.setAccessible(true);
        clearEval.invoke(controller);

        assertEquals("Current Streak: -", streakLabel.getText());
        assertEquals("Completion Rate: -", completionRateLabel.getText());
        assertEquals(0, completionProgressBar.getProgress(), 0.001);
        assertEquals("Today: -", dailyPointsLabel.getText());
        assertEquals("This Week: -", weeklyPointsLabel.getText());
        assertEquals("This Month: -", monthlyPointsLabel.getText());
        assertEquals("Total: -", chartTotalLabel.getText());
        assertEquals("Avg: -", chartAverageLabel.getText());
    }

    @Test
    void testDisplayHabitDetailsUpdatesLabels() throws Exception {
        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setUser(testUser);
        weekToggle.setSelected(true);
        setField(controller, "chartShowingWeek", true);

        Method displayDetails = MainController.class.getDeclaredMethod("displayHabitDetails", Habit.class);
        displayDetails.setAccessible(true);
        displayDetails.invoke(controller, habit);

        assertTrue(streakLabel.getText().startsWith("Current Streak: "));
        assertTrue(completionRateLabel.getText().startsWith("Completion Rate: "));
        assertTrue(dailyPointsLabel.getText().startsWith("Today: "));
        assertTrue(weeklyPointsLabel.getText().startsWith("This Week: "));
        assertTrue(monthlyPointsLabel.getText().startsWith("This Month: "));
    }

    @Test
    void testDisplayHabitDetailsShowsCompleteForSingleEntryHabit() throws Exception {
        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setMaxEntriesPerDay(1);
        weekToggle.setSelected(true);
        setField(controller, "chartShowingWeek", true);

        Method displayDetails = MainController.class.getDeclaredMethod("displayHabitDetails", Habit.class);
        displayDetails.setAccessible(true);
        displayDetails.invoke(controller, habit);

        assertEquals("Complete", completeButton.getText());
    }

    @Test
    void testDisplayHabitDetailsShowsAddCompletionForMultiEntryHabit() throws Exception {
        Habit habit = new Habit("Water", "Drink water");
        habit.setMaxEntriesPerDay(8);
        weekToggle.setSelected(true);
        setField(controller, "chartShowingWeek", true);

        Method displayDetails = MainController.class.getDeclaredMethod("displayHabitDetails", Habit.class);
        displayDetails.setAccessible(true);
        displayDetails.invoke(controller, habit);

        assertEquals("Add Completion", completeButton.getText());
    }

    @Test
    void testHandleCompleteHabitWithNoSelectionDoesNothing() throws Exception {
        habitListView.getSelectionModel().clearSelection();

        Method handleComplete = MainController.class.getDeclaredMethod("handleCompleteHabit");
        handleComplete.setAccessible(true);
        handleComplete.invoke(controller);

        assertTrue(habitRepository.findAll().isEmpty());
    }

    @Test
    void testHandleCompleteHabitAddsEntry() throws Exception {
        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setUser(testUser);
        habit.setMaxEntriesPerDay(5);
        habitRepository.save(habit);
        weekToggle.setSelected(true);
        setField(controller, "chartShowingWeek", true);

        habitListView.getItems().add(habit);
        habitListView.getSelectionModel().selectFirst();

        Method handleComplete = MainController.class.getDeclaredMethod("handleCompleteHabit");
        handleComplete.setAccessible(true);
        handleComplete.invoke(controller);

        assertEquals(1, habit.getEntries().size());
    }

    @Test
    void testHandleDeleteHabitRemovesFromList() throws Exception {
        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setUser(testUser);
        habitRepository.save(habit);

        ObservableList<Habit> habits = FXCollections.observableArrayList(habit);
        setField(controller, "habits", habits);

        habitListView.setItems(FXCollections.observableArrayList(habit));
        habitListView.getSelectionModel().selectFirst();
        chartTotalLabel.setText("");
        chartAverageLabel.setText("");

        Method handleDelete = MainController.class.getDeclaredMethod("handleDeleteHabit");
        handleDelete.setAccessible(true);
        handleDelete.invoke(controller);

        assertTrue(habits.isEmpty());
        assertFalse(habitRepository.existsById(habit.getId()));
    }

    @Test
    void testHandleDeleteHabitWithNullSelectionDoesNothing() throws Exception {
        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setUser(testUser);
        habitRepository.save(habit);

        ObservableList<Habit> habits = FXCollections.observableArrayList(habit);
        setField(controller, "habits", habits);

        habitListView.getSelectionModel().clearSelection();

        Method handleDelete = MainController.class.getDeclaredMethod("handleDeleteHabit");
        handleDelete.setAccessible(true);
        handleDelete.invoke(controller);

        assertEquals(1, habits.size());
    }

    @Test
    void testHandleDeleteHabitCleansUpUnusedCategory() throws Exception {
        HabitCategory category = new HabitCategory("Fitness", "Fitness category", "#FF0000");
        category.setUser(testUser);
        categoryRepository.save(category);

        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setUser(testUser);
        habit.setCategoryId(category.getId());
        habitRepository.save(habit);

        ObservableList<Habit> habits = FXCollections.observableArrayList(habit);
        setField(controller, "habits", habits);

        habitListView.setItems(FXCollections.observableArrayList(habit));
        habitListView.getSelectionModel().selectFirst();
        chartTotalLabel.setText("");
        chartAverageLabel.setText("");

        Method handleDelete = MainController.class.getDeclaredMethod("handleDeleteHabit");
        handleDelete.setAccessible(true);
        handleDelete.invoke(controller);

        assertFalse(categoryRepository.existsById(category.getId()));
    }

    @Test
    void testUpdateStorageModeLabel() throws Exception {
        Method updateLabel = MainController.class.getDeclaredMethod("updateStorageModeLabel");
        updateLabel.setAccessible(true);
        updateLabel.invoke(controller);

        assertTrue(storageModeLabel.getText().startsWith("Storage: "));
    }

    @Test
    void testRefreshDiaryStatsWithNoEntries() throws Exception {
        setField(controller, "diaryEntries", new ArrayList<>());

        Method refreshStats = MainController.class.getDeclaredMethod("refreshDiaryStats");
        refreshStats.setAccessible(true);
        refreshStats.invoke(controller);

        assertEquals("Today: 0 pts", diaryTodayPointsLabel.getText());
        assertEquals("This Week: 0 pts", diaryWeekPointsLabel.getText());
        assertEquals("This Month: 0 pts", diaryMonthPointsLabel.getText());
        assertEquals("Weekly Avg: 0.0", diaryWeeklyAvgLabel.getText());
        assertEquals("Trend: -", diaryTrendLabel.getText());
    }

    @Test
    void testRefreshDiaryStatsWithEntries() throws Exception {
        DiaryEntry entry = new DiaryEntry("Test event", EventSignificance.NORMAL, LocalDate.now());
        entry.setUser(testUser);
        List<DiaryEntry> entries = new ArrayList<>();
        entries.add(entry);
        setField(controller, "diaryEntries", entries);

        Method refreshStats = MainController.class.getDeclaredMethod("refreshDiaryStats");
        refreshStats.setAccessible(true);
        refreshStats.invoke(controller);

        assertEquals("Today: 2 pts", diaryTodayPointsLabel.getText());
    }

    @Test
    void testRefreshDiaryEntriesListWithNoEntries() throws Exception {
        setField(controller, "diaryEntries", new ArrayList<>());

        Method refreshList = MainController.class.getDeclaredMethod("refreshDiaryEntriesList");
        refreshList.setAccessible(true);
        refreshList.invoke(controller);

        assertEquals(1, diaryEntriesContainer.getChildren().size());
        assertTrue(diaryEntriesContainer.getChildren().get(0) instanceof Label);
    }

    @Test
    void testRefreshDiaryEntriesListWithEntries() throws Exception {
        DiaryEntry entry = new DiaryEntry("Test event", EventSignificance.NORMAL, LocalDate.now());
        entry.setUser(testUser);
        List<DiaryEntry> entries = new ArrayList<>();
        entries.add(entry);
        setField(controller, "diaryEntries", entries);

        Method refreshList = MainController.class.getDeclaredMethod("refreshDiaryEntriesList");
        refreshList.setAccessible(true);
        refreshList.invoke(controller);

        assertEquals(1, diaryEntriesContainer.getChildren().size());
    }

    @Test
    void testHandleAddDiaryEntryWithEmptyDescriptionDoesNothing() throws Exception {
        diaryDescriptionField.setText("");
        diaryDatePicker.setValue(LocalDate.now());
        diarySignificanceComboBox.setItems(FXCollections.observableArrayList(
                "Minor (1pt)", "Normal (2pt)", "Major (4pt)"));
        diarySignificanceComboBox.getSelectionModel().select(1);

        Method handleAdd = MainController.class.getDeclaredMethod("handleAddDiaryEntry");
        handleAdd.setAccessible(true);
        handleAdd.invoke(controller);

        assertTrue(diaryEntryRepository.findAll().isEmpty());
    }

    @Test
    void testHandleAddDiaryEntryCreatesEntry() throws Exception {
        diaryDescriptionField.setText("Had a productive meeting");
        diaryDatePicker.setValue(LocalDate.now());
        diarySignificanceComboBox.setItems(FXCollections.observableArrayList(
                "Minor (1pt)", "Normal (2pt)", "Major (4pt)"));
        diarySignificanceComboBox.getSelectionModel().select(1);

        Method handleAdd = MainController.class.getDeclaredMethod("handleAddDiaryEntry");
        handleAdd.setAccessible(true);
        handleAdd.invoke(controller);

        assertEquals(1, diaryEntryRepository.findAll().size());
        assertEquals("Had a productive meeting", diaryEntryRepository.findAll().get(0).getDescription());
    }

    @Test
    void testHandleAddDiaryEntryClearsField() throws Exception {
        diaryDescriptionField.setText("Some event");
        diaryDatePicker.setValue(LocalDate.now());
        diarySignificanceComboBox.setItems(FXCollections.observableArrayList(
                "Minor (1pt)", "Normal (2pt)", "Major (4pt)"));
        diarySignificanceComboBox.getSelectionModel().select(1);

        Method handleAdd = MainController.class.getDeclaredMethod("handleAddDiaryEntry");
        handleAdd.setAccessible(true);
        handleAdd.invoke(controller);

        assertEquals("", diaryDescriptionField.getText());
    }

    @Test
    void testHandleAddDiaryEntryWithMinorSignificance() throws Exception {
        diaryDescriptionField.setText("Small event");
        diaryDatePicker.setValue(LocalDate.now());
        diarySignificanceComboBox.setItems(FXCollections.observableArrayList(
                "Minor (1pt)", "Normal (2pt)", "Major (4pt)"));
        diarySignificanceComboBox.getSelectionModel().select(0);

        Method handleAdd = MainController.class.getDeclaredMethod("handleAddDiaryEntry");
        handleAdd.setAccessible(true);
        handleAdd.invoke(controller);

        assertEquals(EventSignificance.MINOR, diaryEntryRepository.findAll().get(0).getSignificance());
    }

    @Test
    void testHandleAddDiaryEntryWithMajorSignificance() throws Exception {
        diaryDescriptionField.setText("Big event");
        diaryDatePicker.setValue(LocalDate.now());
        diarySignificanceComboBox.setItems(FXCollections.observableArrayList(
                "Minor (1pt)", "Normal (2pt)", "Major (4pt)"));
        diarySignificanceComboBox.getSelectionModel().select(2);

        Method handleAdd = MainController.class.getDeclaredMethod("handleAddDiaryEntry");
        handleAdd.setAccessible(true);
        handleAdd.invoke(controller);

        assertEquals(EventSignificance.MAJOR, diaryEntryRepository.findAll().get(0).getSignificance());
    }

    @Test
    void testHandleWeekToggleSetsState() throws Exception {
        setField(controller, "chartShowingWeek", false);
        monthToggle.setSelected(true);
        weekToggle.setSelected(false);

        Method handleWeek = MainController.class.getDeclaredMethod("handleWeekToggle");
        handleWeek.setAccessible(true);
        handleWeek.invoke(controller);

        assertTrue(weekToggle.isSelected());
        assertFalse(monthToggle.isSelected());
    }

    @Test
    void testHandleMonthToggleSetsState() throws Exception {
        setField(controller, "chartShowingWeek", true);
        weekToggle.setSelected(true);
        monthToggle.setSelected(false);

        Method handleMonth = MainController.class.getDeclaredMethod("handleMonthToggle");
        handleMonth.setAccessible(true);
        handleMonth.invoke(controller);

        assertFalse(weekToggle.isSelected());
        assertTrue(monthToggle.isSelected());
    }

    @Test
    void testUpdateLoadDefaultsButtonVisibility() throws Exception {
        ObservableList<Habit> habits = FXCollections.observableArrayList();
        setField(controller, "habits", habits);
        setField(controller, "categoryList", new ArrayList<>());

        Method updateVisibility = MainController.class.getDeclaredMethod("updateLoadDefaultsButtonVisibility");
        updateVisibility.setAccessible(true);
        updateVisibility.invoke(controller);

        assertTrue(loadDefaultsButton.isVisible());
    }

    @Test
    void testUpdateLoadDefaultsButtonHiddenWhenDataExists() throws Exception {
        ObservableList<Habit> habits = FXCollections.observableArrayList();
        habits.add(new Habit("Test", "Test"));
        setField(controller, "habits", habits);
        setField(controller, "categoryList", new ArrayList<>());

        Method updateVisibility = MainController.class.getDeclaredMethod("updateLoadDefaultsButtonVisibility");
        updateVisibility.setAccessible(true);
        updateVisibility.invoke(controller);

        assertFalse(loadDefaultsButton.isVisible());
    }

    @Test
    void testRefreshEditHabitsWithNoHabitsShowsMessage() throws Exception {
        ObservableList<Habit> habits = FXCollections.observableArrayList();
        setField(controller, "habits", habits);

        Method refreshEdit = MainController.class.getDeclaredMethod("refreshEditHabits");
        refreshEdit.setAccessible(true);
        refreshEdit.invoke(controller);

        assertEquals(1, editHabitsContainer.getChildren().size());
        assertTrue(editHabitsContainer.getChildren().get(0) instanceof Label);
    }

    @Test
    void testRefreshEditHabitsWithHabitsPopulatesContainer() throws Exception {
        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setUser(testUser);
        ObservableList<Habit> habits = FXCollections.observableArrayList(habit);
        setField(controller, "habits", habits);

        Method refreshEdit = MainController.class.getDeclaredMethod("refreshEditHabits");
        refreshEdit.setAccessible(true);
        refreshEdit.invoke(controller);

        assertEquals(1, editHabitsContainer.getChildren().size());
        assertTrue(editHabitsContainer.getChildren().get(0) instanceof VBox);
    }

    @Test
    void testCopyHabitForBackup() throws Exception {
        Habit source = new Habit("Exercise", "Daily exercise");
        source.setTargetFrequency(3);
        source.setMaxEntriesPerDay(5);
        source.setPositiveScoring(true);
        HabitEntry entry = new HabitEntry();
        source.addEntry(entry);

        User backupUser = new User("backup", "pass");

        Method copyMethod = MainController.class.getDeclaredMethod("copyHabitForBackup", Habit.class, User.class);
        copyMethod.setAccessible(true);
        Habit copy = (Habit) copyMethod.invoke(controller, source, backupUser);

        assertEquals("Exercise", copy.getName());
        assertEquals("Daily exercise", copy.getDescription());
        assertEquals(3, copy.getTargetFrequency());
        assertEquals(5, copy.getMaxEntriesPerDay());
        assertTrue(copy.isPositiveScoring());
        assertEquals(backupUser, copy.getUser());
        assertEquals(1, copy.getEntries().size());
    }

    private static class StubHabitRepository implements HabitRepository {
        private final List<Habit> habits = new ArrayList<>();

        @Override
        public Habit save(Habit habit) {
            habits.removeIf(h -> h.getId().equals(habit.getId()));
            habits.add(habit);
            return habit;
        }

        @Override
        public Optional<Habit> findById(String id) {
            return habits.stream().filter(h -> h.getId().equals(id)).findFirst();
        }

        @Override
        public List<Habit> findAll() {
            return new ArrayList<>(habits);
        }

        @Override
        public void deleteById(String id) {
            habits.removeIf(h -> h.getId().equals(id));
        }

        @Override
        public boolean existsById(String id) {
            return habits.stream().anyMatch(h -> h.getId().equals(id));
        }

        @Override
        public List<Habit> findByUserId(String userId) {
            return habits.stream().filter(h -> h.getUser() != null
                    && h.getUser().getId().equals(userId)).toList();
        }
    }

    private static class StubHabitCategoryRepository implements HabitCategoryRepository {
        private final List<HabitCategory> categories = new ArrayList<>();

        @Override
        public HabitCategory save(HabitCategory category) {
            categories.add(category);
            return category;
        }

        @Override
        public Optional<HabitCategory> findById(String id) {
            return categories.stream().filter(c -> c.getId().equals(id)).findFirst();
        }

        @Override
        public List<HabitCategory> findAll() {
            return new ArrayList<>(categories);
        }

        @Override
        public void deleteById(String id) {
            categories.removeIf(c -> c.getId().equals(id));
        }

        @Override
        public boolean existsById(String id) {
            return categories.stream().anyMatch(c -> c.getId().equals(id));
        }

        @Override
        public List<HabitCategory> findByUserId(String userId) {
            return new ArrayList<>(categories.stream().filter(c -> c.getUser() != null
                    && c.getUser().getId().equals(userId)).toList());
        }
    }

    private static class StubDiaryEntryRepository implements DiaryEntryRepository {
        private final List<DiaryEntry> entries = new ArrayList<>();

        @Override
        public DiaryEntry save(DiaryEntry entry) {
            entries.add(entry);
            return entry;
        }

        @Override
        public Optional<DiaryEntry> findById(String id) {
            return entries.stream().filter(e -> e.getId().equals(id)).findFirst();
        }

        @Override
        public List<DiaryEntry> findAll() {
            return new ArrayList<>(entries);
        }

        @Override
        public void deleteById(String id) {
            entries.removeIf(e -> e.getId().equals(id));
        }

        @Override
        public List<DiaryEntry> findByUserId(String userId) {
            return entries.stream().filter(e -> e.getUser() != null
                    && e.getUser().getId().equals(userId)).toList();
        }

        @Override
        public List<String> findDistinctDescriptionsByUserId(String userId) {
            return entries.stream()
                    .filter(e -> e.getUser() != null && e.getUser().getId().equals(userId))
                    .map(DiaryEntry::getDescription)
                    .distinct()
                    .toList();
        }

        @Override
        public List<DiaryEntry> findEntriesNeedingMigration(String userId) {
            return new ArrayList<>();
        }
    }
}
