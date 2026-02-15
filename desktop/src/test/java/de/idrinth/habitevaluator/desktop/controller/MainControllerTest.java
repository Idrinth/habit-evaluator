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

    @Test
    void testApplyFilterShowsAllHabitsWhenAllCategoriesSelected() throws Exception {
        Habit habit1 = new Habit("Exercise", "Daily exercise");
        habit1.setUser(testUser);
        Habit habit2 = new Habit("Read", "Daily reading");
        habit2.setUser(testUser);

        ObservableList<Habit> habits = FXCollections.observableArrayList(habit1, habit2);
        setField(controller, "habits", habits);

        categoryFilterComboBox.setItems(FXCollections.observableArrayList("All categories", "Fitness"));
        categoryFilterComboBox.getSelectionModel().selectFirst();

        Method applyFilter = MainController.class.getDeclaredMethod("applyFilter");
        applyFilter.setAccessible(true);
        applyFilter.invoke(controller);

        ObservableList<Habit> filteredHabits = (ObservableList<Habit>) getFieldValue(controller, "filteredHabits");
        assertEquals(2, filteredHabits.size());
    }

    @Test
    void testApplyFilterShowsAllHabitsWhenNullSelection() throws Exception {
        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setUser(testUser);

        ObservableList<Habit> habits = FXCollections.observableArrayList(habit);
        setField(controller, "habits", habits);

        categoryFilterComboBox.setItems(FXCollections.observableArrayList("All categories"));
        categoryFilterComboBox.getSelectionModel().clearSelection();

        Method applyFilter = MainController.class.getDeclaredMethod("applyFilter");
        applyFilter.setAccessible(true);
        applyFilter.invoke(controller);

        ObservableList<Habit> filteredHabits = (ObservableList<Habit>) getFieldValue(controller, "filteredHabits");
        assertEquals(1, filteredHabits.size());
    }

    @Test
    void testApplyFilterShowsOnlyUncategorizedHabits() throws Exception {
        Habit categorized = new Habit("Exercise", "Daily exercise");
        categorized.setUser(testUser);
        categorized.setCategoryId("some-category-id");

        Habit uncategorized = new Habit("Read", "Daily reading");
        uncategorized.setUser(testUser);
        uncategorized.setCategoryId(null);

        Habit emptyCategory = new Habit("Walk", "Walking");
        emptyCategory.setUser(testUser);
        emptyCategory.setCategoryId("");

        ObservableList<Habit> habits = FXCollections.observableArrayList(categorized, uncategorized, emptyCategory);
        setField(controller, "habits", habits);

        categoryFilterComboBox.setItems(FXCollections.observableArrayList("All categories", "Fitness", "Uncategorized"));
        categoryFilterComboBox.getSelectionModel().select("Uncategorized");

        Method applyFilter = MainController.class.getDeclaredMethod("applyFilter");
        applyFilter.setAccessible(true);
        applyFilter.invoke(controller);

        ObservableList<Habit> filteredHabits = (ObservableList<Habit>) getFieldValue(controller, "filteredHabits");
        assertEquals(2, filteredHabits.size());
    }

    @Test
    void testApplyFilterShowsOnlyMatchingCategory() throws Exception {
        HabitCategory category = new HabitCategory("Fitness", "Fitness category", "#FF0000");
        category.setUser(testUser);

        Habit matching = new Habit("Exercise", "Daily exercise");
        matching.setUser(testUser);
        matching.setCategoryId(category.getId());

        Habit nonMatching = new Habit("Read", "Daily reading");
        nonMatching.setUser(testUser);
        nonMatching.setCategoryId("other-id");

        ObservableList<Habit> habits = FXCollections.observableArrayList(matching, nonMatching);
        setField(controller, "habits", habits);

        java.util.Map<String, String> categoryNameToId = new java.util.LinkedHashMap<>();
        categoryNameToId.put("Fitness", category.getId());
        setField(controller, "categoryNameToId", categoryNameToId);

        categoryFilterComboBox.setItems(FXCollections.observableArrayList("All categories", "Fitness", "Uncategorized"));
        categoryFilterComboBox.getSelectionModel().select("Fitness");

        Method applyFilter = MainController.class.getDeclaredMethod("applyFilter");
        applyFilter.setAccessible(true);
        applyFilter.invoke(controller);

        ObservableList<Habit> filteredHabits = (ObservableList<Habit>) getFieldValue(controller, "filteredHabits");
        assertEquals(1, filteredHabits.size());
        assertEquals("Exercise", filteredHabits.get(0).getName());
    }

    @Test
    void testPopulateCategoryComboBoxes() throws Exception {
        HabitCategory cat1 = new HabitCategory("Fitness", "Fitness desc", "#FF0000");
        cat1.setUser(testUser);
        HabitCategory cat2 = new HabitCategory("Health", "Health desc", "#00FF00");
        cat2.setUser(testUser);

        List<HabitCategory> categoryList = new ArrayList<>();
        categoryList.add(cat1);
        categoryList.add(cat2);
        setField(controller, "categoryList", categoryList);

        Method populate = MainController.class.getDeclaredMethod("populateCategoryComboBoxes");
        populate.setAccessible(true);
        populate.invoke(controller);

        ObservableList<String> items = categoryFilterComboBox.getItems();
        assertEquals(4, items.size());
        assertEquals("All categories", items.get(0));
        assertEquals("Fitness", items.get(1));
        assertEquals("Health", items.get(2));
        assertEquals("Uncategorized", items.get(3));
    }

    @Test
    void testPopulateCategoryComboBoxesWithEmptyList() throws Exception {
        setField(controller, "categoryList", new ArrayList<>());

        Method populate = MainController.class.getDeclaredMethod("populateCategoryComboBoxes");
        populate.setAccessible(true);
        populate.invoke(controller);

        ObservableList<String> items = categoryFilterComboBox.getItems();
        assertEquals(2, items.size());
        assertEquals("All categories", items.get(0));
        assertEquals("Uncategorized", items.get(1));
    }

    @Test
    void testClearPointCharts() throws Exception {
        dailyPointsChart.getData().add(new javafx.scene.chart.XYChart.Series<>());
        runningAvgChart.getData().add(new javafx.scene.chart.XYChart.Series<>());
        cumulativeChart.getData().add(new javafx.scene.chart.XYChart.Series<>());
        chartTotalLabel.setText("Total: 42 pts");
        chartAverageLabel.setText("Avg: 6.0 pts");

        Method clearCharts = MainController.class.getDeclaredMethod("clearPointCharts");
        clearCharts.setAccessible(true);
        clearCharts.invoke(controller);

        assertEquals("Total: -", chartTotalLabel.getText());
        assertEquals("Avg: -", chartAverageLabel.getText());
        assertTrue(dailyPointsChart.getData().isEmpty());
        assertTrue(runningAvgChart.getData().isEmpty());
        assertTrue(cumulativeChart.getData().isEmpty());
    }

    @Test
    void testUpdatePointChartsForWeekView() throws Exception {
        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setUser(testUser);
        setField(controller, "chartShowingWeek", true);

        Method updateCharts = MainController.class.getDeclaredMethod("updatePointCharts", Habit.class);
        updateCharts.setAccessible(true);
        updateCharts.invoke(controller, habit);

        assertTrue(chartTotalLabel.getText().startsWith("Total: "));
        assertTrue(chartAverageLabel.getText().startsWith("Avg: "));
        assertEquals(1, dailyPointsChart.getData().size());
        assertEquals(1, runningAvgChart.getData().size());
        assertEquals(1, cumulativeChart.getData().size());
        assertEquals(7, dailyPointsChart.getData().get(0).getData().size());
    }

    @Test
    void testUpdatePointChartsForMonthView() throws Exception {
        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setUser(testUser);
        setField(controller, "chartShowingWeek", false);

        Method updateCharts = MainController.class.getDeclaredMethod("updatePointCharts", Habit.class);
        updateCharts.setAccessible(true);
        updateCharts.invoke(controller, habit);

        assertTrue(chartTotalLabel.getText().startsWith("Total: "));
        assertTrue(chartAverageLabel.getText().startsWith("Avg: "));
        assertEquals(1, dailyPointsChart.getData().size());
        assertTrue(dailyPointsChart.getData().get(0).getData().size() >= 28);
    }

    @Test
    void testGetClientVersion() throws Exception {
        Method getVersion = MainController.class.getDeclaredMethod("getClientVersion");
        getVersion.setAccessible(true);
        String version = (String) getVersion.invoke(controller);

        assertNotNull(version);
        assertFalse(version.isEmpty());
    }

    @Test
    void testHandleSaveEditedHabitsUpdatesTarget() throws Exception {
        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setUser(testUser);
        habit.setTargetFrequency(3);
        habitRepository.save(habit);

        ObservableList<Habit> habits = FXCollections.observableArrayList(habit);
        setField(controller, "habits", habits);

        java.util.Map<String, TextField> editTargetFields = new java.util.HashMap<>();
        TextField targetField = new TextField("5");
        editTargetFields.put(habit.getId(), targetField);
        setField(controller, "editTargetFields", editTargetFields);
        setField(controller, "editMaxEntriesFields", new java.util.HashMap<>());
        setField(controller, "editPositiveScoringBoxes", new java.util.HashMap<>());
        setField(controller, "editThreshold1Fields", new java.util.HashMap<>());
        setField(controller, "editThreshold2Fields", new java.util.HashMap<>());
        setField(controller, "editThreshold4Fields", new java.util.HashMap<>());
        setField(controller, "editThreshold8Fields", new java.util.HashMap<>());
        setField(controller, "editNameTranslationFields", new java.util.HashMap<>());
        setField(controller, "editDescTranslationFields", new java.util.HashMap<>());

        Method handleSave = MainController.class.getDeclaredMethod("handleSaveEditedHabits");
        handleSave.setAccessible(true);
        handleSave.invoke(controller);

        assertEquals(5, habit.getTargetFrequency());
        assertTrue(editMessage.getText().contains("1 habit(s) saved"));
        assertTrue(editMessage.getStyle().contains("green"));
    }

    @Test
    void testHandleSaveEditedHabitsIgnoresInvalidTarget() throws Exception {
        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setUser(testUser);
        habit.setTargetFrequency(3);
        habitRepository.save(habit);

        ObservableList<Habit> habits = FXCollections.observableArrayList(habit);
        setField(controller, "habits", habits);

        java.util.Map<String, TextField> editTargetFields = new java.util.HashMap<>();
        TextField targetField = new TextField("abc");
        editTargetFields.put(habit.getId(), targetField);
        setField(controller, "editTargetFields", editTargetFields);
        setField(controller, "editMaxEntriesFields", new java.util.HashMap<>());
        setField(controller, "editPositiveScoringBoxes", new java.util.HashMap<>());
        setField(controller, "editThreshold1Fields", new java.util.HashMap<>());
        setField(controller, "editThreshold2Fields", new java.util.HashMap<>());
        setField(controller, "editThreshold4Fields", new java.util.HashMap<>());
        setField(controller, "editThreshold8Fields", new java.util.HashMap<>());
        setField(controller, "editNameTranslationFields", new java.util.HashMap<>());
        setField(controller, "editDescTranslationFields", new java.util.HashMap<>());

        Method handleSave = MainController.class.getDeclaredMethod("handleSaveEditedHabits");
        handleSave.setAccessible(true);
        handleSave.invoke(controller);

        assertEquals(3, habit.getTargetFrequency());
        assertTrue(editMessage.getText().contains("0 habit(s) saved"));
    }

    @Test
    void testHandleSaveEditedHabitsUpdatesMaxEntriesPerDay() throws Exception {
        Habit habit = new Habit("Water", "Drink water");
        habit.setUser(testUser);
        habit.setMaxEntriesPerDay(5);
        habitRepository.save(habit);

        ObservableList<Habit> habits = FXCollections.observableArrayList(habit);
        setField(controller, "habits", habits);

        java.util.Map<String, TextField> editMaxEntriesFields = new java.util.HashMap<>();
        editMaxEntriesFields.put(habit.getId(), new TextField("10"));
        setField(controller, "editTargetFields", new java.util.HashMap<>());
        setField(controller, "editMaxEntriesFields", editMaxEntriesFields);
        setField(controller, "editPositiveScoringBoxes", new java.util.HashMap<>());
        setField(controller, "editThreshold1Fields", new java.util.HashMap<>());
        setField(controller, "editThreshold2Fields", new java.util.HashMap<>());
        setField(controller, "editThreshold4Fields", new java.util.HashMap<>());
        setField(controller, "editThreshold8Fields", new java.util.HashMap<>());
        setField(controller, "editNameTranslationFields", new java.util.HashMap<>());
        setField(controller, "editDescTranslationFields", new java.util.HashMap<>());

        Method handleSave = MainController.class.getDeclaredMethod("handleSaveEditedHabits");
        handleSave.setAccessible(true);
        handleSave.invoke(controller);

        assertEquals(10, habit.getMaxEntriesPerDay());
    }

    @Test
    void testHandleSaveEditedHabitsUpdatesPositiveScoring() throws Exception {
        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setUser(testUser);
        habit.setPositiveScoring(true);
        habitRepository.save(habit);

        ObservableList<Habit> habits = FXCollections.observableArrayList(habit);
        setField(controller, "habits", habits);

        java.util.Map<String, javafx.scene.control.CheckBox> editPositiveScoringBoxes = new java.util.HashMap<>();
        javafx.scene.control.CheckBox posBox = new javafx.scene.control.CheckBox();
        posBox.setSelected(false);
        editPositiveScoringBoxes.put(habit.getId(), posBox);
        setField(controller, "editTargetFields", new java.util.HashMap<>());
        setField(controller, "editMaxEntriesFields", new java.util.HashMap<>());
        setField(controller, "editPositiveScoringBoxes", editPositiveScoringBoxes);
        setField(controller, "editThreshold1Fields", new java.util.HashMap<>());
        setField(controller, "editThreshold2Fields", new java.util.HashMap<>());
        setField(controller, "editThreshold4Fields", new java.util.HashMap<>());
        setField(controller, "editThreshold8Fields", new java.util.HashMap<>());
        setField(controller, "editNameTranslationFields", new java.util.HashMap<>());
        setField(controller, "editDescTranslationFields", new java.util.HashMap<>());

        Method handleSave = MainController.class.getDeclaredMethod("handleSaveEditedHabits");
        handleSave.setAccessible(true);
        handleSave.invoke(controller);

        assertFalse(habit.isPositiveScoring());
    }

    @Test
    void testHandleSaveEditedHabitsUpdatesScoringThresholds() throws Exception {
        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setUser(testUser);
        habitRepository.save(habit);

        ObservableList<Habit> habits = FXCollections.observableArrayList(habit);
        setField(controller, "habits", habits);

        java.util.Map<String, TextField> t1Fields = new java.util.HashMap<>();
        java.util.Map<String, TextField> t2Fields = new java.util.HashMap<>();
        java.util.Map<String, TextField> t4Fields = new java.util.HashMap<>();
        java.util.Map<String, TextField> t8Fields = new java.util.HashMap<>();
        t1Fields.put(habit.getId(), new TextField("2"));
        t2Fields.put(habit.getId(), new TextField("3"));
        t4Fields.put(habit.getId(), new TextField("5"));
        t8Fields.put(habit.getId(), new TextField("10"));
        setField(controller, "editTargetFields", new java.util.HashMap<>());
        setField(controller, "editMaxEntriesFields", new java.util.HashMap<>());
        setField(controller, "editPositiveScoringBoxes", new java.util.HashMap<>());
        setField(controller, "editThreshold1Fields", t1Fields);
        setField(controller, "editThreshold2Fields", t2Fields);
        setField(controller, "editThreshold4Fields", t4Fields);
        setField(controller, "editThreshold8Fields", t8Fields);
        setField(controller, "editNameTranslationFields", new java.util.HashMap<>());
        setField(controller, "editDescTranslationFields", new java.util.HashMap<>());

        Method handleSave = MainController.class.getDeclaredMethod("handleSaveEditedHabits");
        handleSave.setAccessible(true);
        handleSave.invoke(controller);

        assertNotNull(habit.getScoringRule());
        assertEquals(2, habit.getScoringRule().getThresholdFor1Point());
        assertEquals(3, habit.getScoringRule().getThresholdFor2Points());
        assertEquals(5, habit.getScoringRule().getThresholdFor4Points());
        assertEquals(10, habit.getScoringRule().getThresholdFor8Points());
    }

    @Test
    void testHandleSaveEditedHabitsRejectsInvalidThresholdOrder() throws Exception {
        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setUser(testUser);
        habitRepository.save(habit);

        ObservableList<Habit> habits = FXCollections.observableArrayList(habit);
        setField(controller, "habits", habits);

        java.util.Map<String, TextField> t1Fields = new java.util.HashMap<>();
        java.util.Map<String, TextField> t2Fields = new java.util.HashMap<>();
        java.util.Map<String, TextField> t4Fields = new java.util.HashMap<>();
        java.util.Map<String, TextField> t8Fields = new java.util.HashMap<>();
        t1Fields.put(habit.getId(), new TextField("5"));
        t2Fields.put(habit.getId(), new TextField("3"));
        t4Fields.put(habit.getId(), new TextField("2"));
        t8Fields.put(habit.getId(), new TextField("1"));
        setField(controller, "editTargetFields", new java.util.HashMap<>());
        setField(controller, "editMaxEntriesFields", new java.util.HashMap<>());
        setField(controller, "editPositiveScoringBoxes", new java.util.HashMap<>());
        setField(controller, "editThreshold1Fields", t1Fields);
        setField(controller, "editThreshold2Fields", t2Fields);
        setField(controller, "editThreshold4Fields", t4Fields);
        setField(controller, "editThreshold8Fields", t8Fields);
        setField(controller, "editNameTranslationFields", new java.util.HashMap<>());
        setField(controller, "editDescTranslationFields", new java.util.HashMap<>());

        Method handleSave = MainController.class.getDeclaredMethod("handleSaveEditedHabits");
        handleSave.setAccessible(true);
        handleSave.invoke(controller);

        assertNull(habit.getScoringRule());
        assertTrue(editMessage.getText().contains("0 habit(s) saved"));
    }

    @Test
    void testUpdateDiarySuggestions() throws Exception {
        DiaryEntry entry1 = new DiaryEntry("Morning run", EventSignificance.NORMAL);
        entry1.setUser(testUser);
        DiaryEntry entry2 = new DiaryEntry("Team meeting", EventSignificance.MINOR);
        entry2.setUser(testUser);
        diaryEntryRepository.save(entry1);
        diaryEntryRepository.save(entry2);

        Method updateSuggestions = MainController.class.getDeclaredMethod("updateDiarySuggestions");
        updateSuggestions.setAccessible(true);
        updateSuggestions.invoke(controller);

        List<String> diarySuggestions = (List<String>) getFieldValue(controller, "diarySuggestions");
        assertEquals(2, diarySuggestions.size());
    }

    @Test
    void testUpdateDiarySuggestionsWithNullRepository() throws Exception {
        setField(controller, "diaryEntryRepository", null);

        Method updateSuggestions = MainController.class.getDeclaredMethod("updateDiarySuggestions");
        updateSuggestions.setAccessible(true);
        updateSuggestions.invoke(controller);

        List<String> diarySuggestions = (List<String>) getFieldValue(controller, "diarySuggestions");
        assertTrue(diarySuggestions.isEmpty());
    }

    @Test
    void testLoadDiaryEntries() throws Exception {
        DiaryEntry entry = new DiaryEntry("Morning run", EventSignificance.NORMAL);
        entry.setUser(testUser);
        diaryEntryRepository.save(entry);

        Method loadDiary = MainController.class.getDeclaredMethod("loadDiaryEntries");
        loadDiary.setAccessible(true);
        loadDiary.invoke(controller);

        List<DiaryEntry> diaryEntries = (List<DiaryEntry>) getFieldValue(controller, "diaryEntries");
        assertEquals(1, diaryEntries.size());
    }

    @Test
    void testLoadDiaryEntriesWithNullRepository() throws Exception {
        setField(controller, "diaryEntryRepository", null);

        Method loadDiary = MainController.class.getDeclaredMethod("loadDiaryEntries");
        loadDiary.setAccessible(true);
        loadDiary.invoke(controller);

        List<DiaryEntry> diaryEntries = (List<DiaryEntry>) getFieldValue(controller, "diaryEntries");
        assertTrue(diaryEntries.isEmpty());
    }

    @Test
    void testHandleAddDiaryEntryWithNullDateUsesToday() throws Exception {
        diaryDescriptionField.setText("Test event");
        diaryDatePicker.setValue(null);
        diarySignificanceComboBox.setItems(FXCollections.observableArrayList(
                "Minor (1pt)", "Normal (2pt)", "Major (4pt)"));
        diarySignificanceComboBox.getSelectionModel().select(1);

        Method handleAdd = MainController.class.getDeclaredMethod("handleAddDiaryEntry");
        handleAdd.setAccessible(true);
        handleAdd.invoke(controller);

        assertEquals(1, diaryEntryRepository.findAll().size());
        assertEquals(LocalDate.now(), diaryEntryRepository.findAll().get(0).getEventDate());
    }

    @Test
    void testHandleCompleteHabitDoesNotExceedDailyLimit() throws Exception {
        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setUser(testUser);
        habit.setMaxEntriesPerDay(1);

        HabitEntry existingEntry = new HabitEntry(habit.getId());
        habit.addEntry(existingEntry);
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
    void testShutdownInLocalModeDoesNotThrow() throws Exception {
        setField(controller, "localBackupRepository", null);
        setField(controller, "localBackupUser", null);

        assertDoesNotThrow(() -> controller.shutdown());
    }

    @Test
    void testLoadCategoriesWithRepositoryAndUser() throws Exception {
        HabitCategory cat = new HabitCategory("Fitness", "Fitness desc", "#FF0000");
        cat.setUser(testUser);
        categoryRepository.save(cat);

        Method loadCategories = MainController.class.getDeclaredMethod("loadCategories");
        loadCategories.setAccessible(true);
        loadCategories.invoke(controller);

        List<HabitCategory> categoryList = (List<HabitCategory>) getFieldValue(controller, "categoryList");
        assertEquals(1, categoryList.size());
        assertEquals("Fitness", categoryList.get(0).getName());
    }

    @Test
    void testLoadCategoriesSortsAlphabetically() throws Exception {
        HabitCategory catB = new HabitCategory("Zzz", "Zzz desc", "#FF0000");
        catB.setUser(testUser);
        HabitCategory catA = new HabitCategory("Aaa", "Aaa desc", "#00FF00");
        catA.setUser(testUser);
        categoryRepository.save(catB);
        categoryRepository.save(catA);

        Method loadCategories = MainController.class.getDeclaredMethod("loadCategories");
        loadCategories.setAccessible(true);
        loadCategories.invoke(controller);

        List<HabitCategory> categoryList = (List<HabitCategory>) getFieldValue(controller, "categoryList");
        assertEquals(2, categoryList.size());
        assertEquals("Aaa", categoryList.get(0).getName());
        assertEquals("Zzz", categoryList.get(1).getName());
    }

    @Test
    void testDisplayHabitDetailsForNegativeHabit() throws Exception {
        Habit habit = new Habit("Smoking", "Avoid smoking");
        habit.setUser(testUser);
        habit.setPositiveScoring(false);
        weekToggle.setSelected(true);
        setField(controller, "chartShowingWeek", true);

        Method displayDetails = MainController.class.getDeclaredMethod("displayHabitDetails", Habit.class);
        displayDetails.setAccessible(true);
        displayDetails.invoke(controller, habit);

        assertTrue(streakLabel.getText().startsWith("Current Streak: "));
        assertTrue(completionRateLabel.getText().startsWith("Completion Rate: "));
    }

    @Test
    void testRefreshEditHabitsWithHabitHavingDescription() throws Exception {
        Habit habit = new Habit("Exercise", "A detailed description");
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
    void testRefreshEditHabitsWithHabitWithoutDescription() throws Exception {
        Habit habit = new Habit("Exercise", null);
        habit.setUser(testUser);
        ObservableList<Habit> habits = FXCollections.observableArrayList(habit);
        setField(controller, "habits", habits);

        Method refreshEdit = MainController.class.getDeclaredMethod("refreshEditHabits");
        refreshEdit.setAccessible(true);
        refreshEdit.invoke(controller);

        assertEquals(1, editHabitsContainer.getChildren().size());
    }

    @SuppressWarnings("unchecked")
    private Object getFieldValue(Object target, String fieldName) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
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
