package de.idrinth.habitevaluator.desktop.controller;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.EventCorrelation;
import de.idrinth.habitevaluator.shared.model.EventSignificance;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class StatsControllerTest extends JavaFXControllerTestBase {

    private StatsController controller;
    private BarChart<String, Number> habitPointsChart;
    private CategoryAxis habitXAxis;
    private NumberAxis habitYAxis;
    private BarChart<String, Number> diaryPointsChart;
    private CategoryAxis diaryXAxis;
    private NumberAxis diaryYAxis;
    private BarChart<String, Number> sleepDurationChart;
    private CategoryAxis sleepDurationXAxis;
    private NumberAxis sleepDurationYAxis;
    private BarChart<String, Number> sleepEntriesChart;
    private CategoryAxis sleepEntriesXAxis;
    private NumberAxis sleepEntriesYAxis;
    private LineChart<String, Number> emotionPairsChart;
    private CategoryAxis emotionXAxis;
    private NumberAxis emotionYAxis;
    private TableView<EventCorrelation> correlationTable;
    private TableColumn<EventCorrelation, String> eventAColumn;
    private TableColumn<EventCorrelation, String> eventBColumn;
    private TableColumn<EventCorrelation, Number> correlationColumn;
    private TableColumn<EventCorrelation, Number> sharedDaysColumn;
    private TableColumn<EventCorrelation, String> confidenceColumn;

    private StubSleepEntryRepository sleepEntryRepository;
    private StubDiaryEntryRepository diaryEntryRepository;
    private StubEmotionEntryRepository emotionEntryRepository;

    @BeforeEach
    void setUp() throws Exception {
        controller = new StatsController();
        sleepEntryRepository = new StubSleepEntryRepository();
        diaryEntryRepository = new StubDiaryEntryRepository();
        emotionEntryRepository = new StubEmotionEntryRepository();

        habitXAxis = new CategoryAxis();
        habitYAxis = new NumberAxis();
        habitPointsChart = new BarChart<>(habitXAxis, habitYAxis);

        diaryXAxis = new CategoryAxis();
        diaryYAxis = new NumberAxis();
        diaryPointsChart = new BarChart<>(diaryXAxis, diaryYAxis);

        sleepDurationXAxis = new CategoryAxis();
        sleepDurationYAxis = new NumberAxis();
        sleepDurationChart = new BarChart<>(sleepDurationXAxis, sleepDurationYAxis);

        sleepEntriesXAxis = new CategoryAxis();
        sleepEntriesYAxis = new NumberAxis();
        sleepEntriesChart = new BarChart<>(sleepEntriesXAxis, sleepEntriesYAxis);

        emotionXAxis = new CategoryAxis();
        emotionYAxis = new NumberAxis();
        emotionPairsChart = new LineChart<>(emotionXAxis, emotionYAxis);

        correlationTable = new TableView<>();
        eventAColumn = new TableColumn<>();
        eventBColumn = new TableColumn<>();
        correlationColumn = new TableColumn<>();
        sharedDaysColumn = new TableColumn<>();
        confidenceColumn = new TableColumn<>();

        setField(controller, "habitPointsChart", habitPointsChart);
        setField(controller, "habitXAxis", habitXAxis);
        setField(controller, "habitYAxis", habitYAxis);
        setField(controller, "diaryPointsChart", diaryPointsChart);
        setField(controller, "diaryXAxis", diaryXAxis);
        setField(controller, "diaryYAxis", diaryYAxis);
        setField(controller, "sleepDurationChart", sleepDurationChart);
        setField(controller, "sleepDurationXAxis", sleepDurationXAxis);
        setField(controller, "sleepDurationYAxis", sleepDurationYAxis);
        setField(controller, "sleepEntriesChart", sleepEntriesChart);
        setField(controller, "sleepEntriesXAxis", sleepEntriesXAxis);
        setField(controller, "sleepEntriesYAxis", sleepEntriesYAxis);
        setField(controller, "emotionPairsChart", emotionPairsChart);
        setField(controller, "emotionXAxis", emotionXAxis);
        setField(controller, "emotionYAxis", emotionYAxis);
        setField(controller, "correlationTable", correlationTable);
        setField(controller, "eventAColumn", eventAColumn);
        setField(controller, "eventBColumn", eventBColumn);
        setField(controller, "correlationColumn", correlationColumn);
        setField(controller, "sharedDaysColumn", sharedDaysColumn);
        setField(controller, "confidenceColumn", confidenceColumn);

        controller.setSleepEntryRepository(sleepEntryRepository);
        controller.setDiaryEntryRepository(diaryEntryRepository);
        controller.setEmotionEntryRepository(emotionEntryRepository);
    }

    @Test
    void testLoadDataWithNoDataPopulatesEmptyCharts() {
        User user = new User("testuser", "password");
        controller.setCurrentUser(user);
        controller.setHabits(new ArrayList<>());
        controller.loadData();

        assertFalse(habitPointsChart.getData().isEmpty());
        assertEquals(1, habitPointsChart.getData().size());
    }

    @Test
    void testLoadDataPopulatesHabitChart() {
        User user = new User("testuser", "password");
        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setUser(user);
        HabitEntry entry = new HabitEntry();
        entry.setCompletedAt(LocalDateTime.now());
        habit.addEntry(entry);

        List<Habit> habits = new ArrayList<>();
        habits.add(habit);

        controller.setCurrentUser(user);
        controller.setHabits(habits);
        controller.loadData();

        assertFalse(habitPointsChart.getData().isEmpty());
        assertEquals(30, habitPointsChart.getData().get(0).getData().size());
    }

    @Test
    void testLoadDataPopulatesDiaryChart() {
        User user = new User("testuser", "password");
        DiaryEntry entry = new DiaryEntry("Test event", EventSignificance.NORMAL, LocalDate.now());
        entry.setUser(user);
        diaryEntryRepository.save(entry);

        controller.setCurrentUser(user);
        controller.setHabits(new ArrayList<>());
        controller.loadData();

        assertFalse(diaryPointsChart.getData().isEmpty());
        assertEquals(30, diaryPointsChart.getData().get(0).getData().size());
    }

    @Test
    void testLoadDataPopulatesSleepDurationChart() {
        User user = new User("testuser", "password");
        SleepEntry entry = new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0), LocalDate.now());
        entry.setUser(user);
        sleepEntryRepository.save(entry);

        controller.setCurrentUser(user);
        controller.setHabits(new ArrayList<>());
        controller.loadData();

        assertFalse(sleepDurationChart.getData().isEmpty());
        assertEquals(30, sleepDurationChart.getData().get(0).getData().size());
    }

    @Test
    void testLoadDataPopulatesSleepEntriesChart() {
        User user = new User("testuser", "password");
        SleepEntry entry = new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0), LocalDate.now());
        entry.setUser(user);
        sleepEntryRepository.save(entry);

        controller.setCurrentUser(user);
        controller.setHabits(new ArrayList<>());
        controller.loadData();

        assertFalse(sleepEntriesChart.getData().isEmpty());
        assertEquals(30, sleepEntriesChart.getData().get(0).getData().size());
    }

    @Test
    void testLoadDataPopulatesEmotionChart() {
        User user = new User("testuser", "password");
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        pair.setUser(user);
        EmotionEntry entry = new EmotionEntry(pair, 5, LocalDateTime.now(), "Feeling good");
        entry.setUser(user);
        emotionEntryRepository.save(entry);

        controller.setCurrentUser(user);
        controller.setHabits(new ArrayList<>());
        controller.loadData();

        assertFalse(emotionPairsChart.getData().isEmpty());
    }

    @Test
    void testLoadDataPopulatesCorrelationTable() {
        User user = new User("testuser", "password");
        controller.setCurrentUser(user);
        controller.setHabits(new ArrayList<>());
        controller.loadData();

        // With no data, correlation table should be empty but columns should be configured
        assertNotNull(eventAColumn.getCellValueFactory());
        assertNotNull(eventBColumn.getCellValueFactory());
        assertNotNull(correlationColumn.getCellValueFactory());
        assertNotNull(sharedDaysColumn.getCellValueFactory());
        assertNotNull(confidenceColumn.getCellValueFactory());
    }

    @Test
    void testSetHabitsWithNullDefaultsToEmptyList() {
        controller.setHabits(null);
        User user = new User("testuser", "password");
        controller.setCurrentUser(user);

        // Should not throw when loading data with null habits
        assertDoesNotThrow(() -> controller.loadData());
    }

    @Test
    void testLoadDataWithNullRepositoriesDoesNotThrow() {
        User user = new User("testuser", "password");
        controller.setCurrentUser(user);
        controller.setHabits(new ArrayList<>());
        controller.setSleepEntryRepository(null);
        controller.setDiaryEntryRepository(null);
        controller.setEmotionEntryRepository(null);

        assertDoesNotThrow(() -> controller.loadData());
    }

    @Test
    void testLoadDataWithNullUserDoesNotThrow() {
        controller.setCurrentUser(null);
        controller.setHabits(new ArrayList<>());

        assertDoesNotThrow(() -> controller.loadData());
    }

    @Test
    void testHabitChartHas30DataPoints() {
        User user = new User("testuser", "password");
        controller.setCurrentUser(user);
        controller.setHabits(new ArrayList<>());
        controller.loadData();

        assertEquals(30, habitXAxis.getCategories().size());
    }

    private static class StubSleepEntryRepository implements SleepEntryRepository {
        private final List<SleepEntry> entries = new ArrayList<>();

        @Override
        public SleepEntry save(SleepEntry entry) {
            entries.add(entry);
            return entry;
        }

        @Override
        public Optional<SleepEntry> findById(String id) {
            return entries.stream().filter(e -> e.getId().equals(id)).findFirst();
        }

        @Override
        public List<SleepEntry> findAll() {
            return new ArrayList<>(entries);
        }

        @Override
        public void deleteById(String id) {
            entries.removeIf(e -> e.getId().equals(id));
        }

        @Override
        public boolean existsById(String id) {
            return entries.stream().anyMatch(e -> e.getId().equals(id));
        }

        @Override
        public List<SleepEntry> findByUserId(String userId) {
            return entries.stream().filter(e -> e.getUser() != null
                    && e.getUser().getId().equals(userId)).toList();
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
            return entries.stream()
                    .filter(e -> e.getUser() != null && e.getUser().getId().equals(userId))
                    .filter(DiaryEntry::needsMigration)
                    .toList();
        }
    }

    private static class StubEmotionEntryRepository implements EmotionEntryRepository {
        private final List<EmotionEntry> entries = new ArrayList<>();

        @Override
        public EmotionEntry save(EmotionEntry entry) {
            entries.add(entry);
            return entry;
        }

        @Override
        public Optional<EmotionEntry> findById(String id) {
            return entries.stream().filter(e -> e.getId().equals(id)).findFirst();
        }

        @Override
        public List<EmotionEntry> findAll() {
            return new ArrayList<>(entries);
        }

        @Override
        public void deleteById(String id) {
            entries.removeIf(e -> e.getId().equals(id));
        }

        @Override
        public List<EmotionEntry> findByUserId(String userId) {
            return entries.stream().filter(e -> e.getUser() != null
                    && e.getUser().getId().equals(userId)).toList();
        }
    }
}
