package de.idrinth.habitevaluator.desktop.controller;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EventSignificance;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PdfExportControllerTest extends JavaFXControllerTestBase {

    private PdfExportController controller;
    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;
    private CheckBox includeHabitsCheckbox;
    private CheckBox includeSleepCheckbox;
    private CheckBox includeDiaryCheckbox;
    private CheckBox includeEmotionsCheckbox;
    private Label statusLabel;

    private StubHabitRepository habitRepository;
    private StubDiaryEntryRepository diaryEntryRepository;
    private StubSleepEntryRepository sleepEntryRepository;
    private StubEmotionEntryRepository emotionEntryRepository;

    @BeforeEach
    void setUp() throws Exception {
        controller = new PdfExportController();
        habitRepository = new StubHabitRepository();
        diaryEntryRepository = new StubDiaryEntryRepository();
        sleepEntryRepository = new StubSleepEntryRepository();
        emotionEntryRepository = new StubEmotionEntryRepository();

        fromDatePicker = new DatePicker();
        toDatePicker = new DatePicker();
        includeHabitsCheckbox = new CheckBox();
        includeSleepCheckbox = new CheckBox();
        includeDiaryCheckbox = new CheckBox();
        includeEmotionsCheckbox = new CheckBox();
        statusLabel = new Label();

        setField(controller, "fromDatePicker", fromDatePicker);
        setField(controller, "toDatePicker", toDatePicker);
        setField(controller, "includeHabitsCheckbox", includeHabitsCheckbox);
        setField(controller, "includeSleepCheckbox", includeSleepCheckbox);
        setField(controller, "includeDiaryCheckbox", includeDiaryCheckbox);
        setField(controller, "includeEmotionsCheckbox", includeEmotionsCheckbox);
        setField(controller, "statusLabel", statusLabel);

        controller.setHabitRepository(habitRepository);
        controller.setDiaryEntryRepository(diaryEntryRepository);
        controller.setSleepEntryRepository(sleepEntryRepository);
        controller.setEmotionEntryRepository(emotionEntryRepository);
    }

    @Test
    void testInitializeSetsDefaultDates() {
        controller.initialize();

        assertEquals(LocalDate.now(), toDatePicker.getValue());
        assertEquals(LocalDate.now().minusDays(29), fromDatePicker.getValue());
    }

    @Test
    void testHandleExportWithNoSectionsSelectedShowsError() throws Exception {
        controller.initialize();
        includeHabitsCheckbox.setSelected(false);
        includeSleepCheckbox.setSelected(false);
        includeDiaryCheckbox.setSelected(false);
        includeEmotionsCheckbox.setSelected(false);

        Method handleExport = PdfExportController.class.getDeclaredMethod("handleExport");
        handleExport.setAccessible(true);
        handleExport.invoke(controller);

        assertEquals("Select at least one section.", statusLabel.getText());
    }

    @Test
    void testHandleExportWithNullFromDateShowsError() throws Exception {
        controller.initialize();
        includeHabitsCheckbox.setSelected(true);
        fromDatePicker.setValue(null);

        Method handleExport = PdfExportController.class.getDeclaredMethod("handleExport");
        handleExport.setAccessible(true);
        handleExport.invoke(controller);

        assertEquals("Select both dates.", statusLabel.getText());
    }

    @Test
    void testHandleExportWithNullToDateShowsError() throws Exception {
        controller.initialize();
        includeHabitsCheckbox.setSelected(true);
        toDatePicker.setValue(null);

        Method handleExport = PdfExportController.class.getDeclaredMethod("handleExport");
        handleExport.setAccessible(true);
        handleExport.invoke(controller);

        assertEquals("Select both dates.", statusLabel.getText());
    }

    @Test
    void testHandleExportWithBothDatesNullShowsError() throws Exception {
        controller.initialize();
        includeHabitsCheckbox.setSelected(true);
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);

        Method handleExport = PdfExportController.class.getDeclaredMethod("handleExport");
        handleExport.setAccessible(true);
        handleExport.invoke(controller);

        assertEquals("Select both dates.", statusLabel.getText());
    }

    @Test
    void testSetCurrentUserSetsUser() {
        User user = new User("testuser", "password");
        controller.setCurrentUser(user);

        // No direct getter, but we can verify indirectly via export behavior
        assertDoesNotThrow(() -> controller.initialize());
    }

    @Test
    void testSetRepositories() {
        // Verify setters do not throw
        assertDoesNotThrow(() -> controller.setHabitRepository(habitRepository));
        assertDoesNotThrow(() -> controller.setDiaryEntryRepository(diaryEntryRepository));
        assertDoesNotThrow(() -> controller.setSleepEntryRepository(sleepEntryRepository));
        assertDoesNotThrow(() -> controller.setEmotionEntryRepository(emotionEntryRepository));
    }

    @Test
    void testStatusLabelStyleIsRedOnValidationError() throws Exception {
        controller.initialize();
        includeHabitsCheckbox.setSelected(false);
        includeSleepCheckbox.setSelected(false);
        includeDiaryCheckbox.setSelected(false);
        includeEmotionsCheckbox.setSelected(false);

        Method handleExport = PdfExportController.class.getDeclaredMethod("handleExport");
        handleExport.setAccessible(true);
        handleExport.invoke(controller);

        assertTrue(statusLabel.getStyle().contains("-fx-text-fill: red"));
    }

    @Test
    void testHandleExportWithValidDatesPassesValidation() throws Exception {
        controller.initialize();
        includeHabitsCheckbox.setSelected(true);
        includeSleepCheckbox.setSelected(false);
        includeDiaryCheckbox.setSelected(false);
        includeEmotionsCheckbox.setSelected(false);

        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();
        fromDatePicker.setValue(from);
        toDatePicker.setValue(to);

        User user = new User("testuser", "password");
        controller.setCurrentUser(user);

        Method handleExport = PdfExportController.class.getDeclaredMethod("handleExport");
        handleExport.setAccessible(true);

        // This will pass date/section validation but NPE on getScene() for FileChooser
        // We verify it reaches past the validation checks
        try {
            handleExport.invoke(controller);
        } catch (Exception e) {
            // Expected NPE from statusLabel.getScene().getWindow() when not in a scene
        }

        // Assertions must run regardless of whether an exception was thrown
        assertNotEquals("Select at least one section.", statusLabel.getText());
        assertNotEquals("Select both dates.", statusLabel.getText());
    }

    @Test
    void testHandleExportSwapsDatesWhenFromIsAfterTo() throws Exception {
        controller.initialize();
        includeHabitsCheckbox.setSelected(true);

        LocalDate from = LocalDate.now();
        LocalDate to = LocalDate.now().minusDays(10);
        fromDatePicker.setValue(from);
        toDatePicker.setValue(to);

        User user = new User("testuser", "password");
        controller.setCurrentUser(user);

        Method handleExport = PdfExportController.class.getDeclaredMethod("handleExport");
        handleExport.setAccessible(true);

        // The method swaps dates before reaching the FileChooser (which will NPE in test)
        try {
            handleExport.invoke(controller);
        } catch (Exception e) {
            // Expected NPE from statusLabel.getScene().getWindow()
        }

        // Verify the dates were swapped
        assertTrue(fromDatePicker.getValue().isBefore(toDatePicker.getValue()));
    }

    @Test
    void testSetNullRepositoriesDoNotThrow() {
        assertDoesNotThrow(() -> controller.setHabitRepository(null));
        assertDoesNotThrow(() -> controller.setDiaryEntryRepository(null));
        assertDoesNotThrow(() -> controller.setSleepEntryRepository(null));
        assertDoesNotThrow(() -> controller.setEmotionEntryRepository(null));
    }

    @Test
    void testSetCurrentUserWithNullDoesNotThrow() {
        assertDoesNotThrow(() -> controller.setCurrentUser(null));
    }

    @Test
    void testStatusLabelStyleIsRedOnMissingDates() throws Exception {
        controller.initialize();
        includeHabitsCheckbox.setSelected(true);
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);

        Method handleExport = PdfExportController.class.getDeclaredMethod("handleExport");
        handleExport.setAccessible(true);
        handleExport.invoke(controller);

        assertTrue(statusLabel.getStyle().contains("-fx-text-fill: red"));
    }

    private static class StubHabitRepository implements HabitRepository {
        private final List<Habit> habits = new ArrayList<>();

        @Override
        public Habit save(Habit habit) {
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
