package de.idrinth.habitevaluator.desktop.controller;

import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SleepTrackingControllerTest extends JavaFXControllerTestBase {

    private SleepTrackingController controller;
    private DatePicker datePicker;
    private TextField fromTimeField;
    private TextField untilTimeField;
    private TextField notesField;
    private Label messageLabel;
    private Label weeklyAvgLabel;
    private Label weeklyMinLabel;
    private Label weeklyMaxLabel;
    private Label weeklyCountLabel;
    private Label monthlyAvgLabel;
    private Label monthlyMinLabel;
    private Label monthlyMaxLabel;
    private Label monthlyCountLabel;
    private VBox entriesContainer;
    private StubSleepEntryRepository sleepEntryRepository;

    @BeforeEach
    void setUp() throws Exception {
        controller = new SleepTrackingController();
        sleepEntryRepository = new StubSleepEntryRepository();

        datePicker = new DatePicker();
        fromTimeField = new TextField();
        untilTimeField = new TextField();
        notesField = new TextField();
        messageLabel = new Label();
        weeklyAvgLabel = new Label();
        weeklyMinLabel = new Label();
        weeklyMaxLabel = new Label();
        weeklyCountLabel = new Label();
        monthlyAvgLabel = new Label();
        monthlyMinLabel = new Label();
        monthlyMaxLabel = new Label();
        monthlyCountLabel = new Label();
        entriesContainer = new VBox();

        setField(controller, "datePicker", datePicker);
        setField(controller, "fromTimeField", fromTimeField);
        setField(controller, "untilTimeField", untilTimeField);
        setField(controller, "notesField", notesField);
        setField(controller, "messageLabel", messageLabel);
        setField(controller, "weeklyAvgLabel", weeklyAvgLabel);
        setField(controller, "weeklyMinLabel", weeklyMinLabel);
        setField(controller, "weeklyMaxLabel", weeklyMaxLabel);
        setField(controller, "weeklyCountLabel", weeklyCountLabel);
        setField(controller, "monthlyAvgLabel", monthlyAvgLabel);
        setField(controller, "monthlyMinLabel", monthlyMinLabel);
        setField(controller, "monthlyMaxLabel", monthlyMaxLabel);
        setField(controller, "monthlyCountLabel", monthlyCountLabel);
        setField(controller, "entriesContainer", entriesContainer);

        controller.setSleepEntryRepository(sleepEntryRepository);
    }

    @Test
    void testInitializeSetsDefaults() {
        controller.initialize();

        assertEquals(LocalDate.now(), datePicker.getValue());
        assertEquals("22:00", fromTimeField.getText());
        assertEquals("06:00", untilTimeField.getText());
    }

    @Test
    void testSetCurrentUserLoadsData() {
        User user = new User("testuser", "password");
        SleepEntry entry = new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0), LocalDate.now());
        entry.setUser(user);
        sleepEntryRepository.save(entry);

        controller.setCurrentUser(user);

        assertFalse(entriesContainer.getChildren().isEmpty());
    }

    @Test
    void testSetCurrentUserWithNoEntriesShowsEmptyMessage() {
        User user = new User("testuser", "password");
        controller.setCurrentUser(user);

        assertEquals(1, entriesContainer.getChildren().size());
        assertTrue(entriesContainer.getChildren().get(0) instanceof Label);
    }

    @Test
    void testStatsShowDashWhenNoEntries() {
        User user = new User("testuser", "password");
        controller.setCurrentUser(user);

        assertEquals("Avg: -", weeklyAvgLabel.getText());
        assertEquals("Min: -", weeklyMinLabel.getText());
        assertEquals("Max: -", weeklyMaxLabel.getText());
        assertEquals("Entries: 0", weeklyCountLabel.getText());
        assertEquals("Avg: -", monthlyAvgLabel.getText());
        assertEquals("Min: -", monthlyMinLabel.getText());
        assertEquals("Max: -", monthlyMaxLabel.getText());
        assertEquals("Entries: 0", monthlyCountLabel.getText());
    }

    @Test
    void testHandleAddEntryWithNullDateShowsError() throws Exception {
        controller.initialize();
        datePicker.setValue(null);
        User user = new User("testuser", "password");
        controller.setCurrentUser(user);

        Method handleAddEntry = SleepTrackingController.class.getDeclaredMethod("handleAddEntry");
        handleAddEntry.setAccessible(true);
        handleAddEntry.invoke(controller);

        assertEquals("Please select a date", messageLabel.getText());
    }

    @Test
    void testHandleAddEntryWithInvalidTimeShowsError() throws Exception {
        controller.initialize();
        fromTimeField.setText("invalid");
        User user = new User("testuser", "password");
        controller.setCurrentUser(user);

        Method handleAddEntry = SleepTrackingController.class.getDeclaredMethod("handleAddEntry");
        handleAddEntry.setAccessible(true);
        handleAddEntry.invoke(controller);

        assertEquals("Invalid time format. Use HH:mm", messageLabel.getText());
    }

    @Test
    void testHandleAddEntrySuccessful() throws Exception {
        controller.initialize();
        User user = new User("testuser", "password");
        controller.setCurrentUser(user);

        Method handleAddEntry = SleepTrackingController.class.getDeclaredMethod("handleAddEntry");
        handleAddEntry.setAccessible(true);
        handleAddEntry.invoke(controller);

        assertEquals("Sleep entry added", messageLabel.getText());
        assertEquals(1, sleepEntryRepository.findAll().size());
    }

    @Test
    void testHandleAddEntryWithNotesPreservesNotes() throws Exception {
        controller.initialize();
        notesField.setText("Good sleep");
        User user = new User("testuser", "password");
        controller.setCurrentUser(user);

        Method handleAddEntry = SleepTrackingController.class.getDeclaredMethod("handleAddEntry");
        handleAddEntry.setAccessible(true);
        handleAddEntry.invoke(controller);

        SleepEntry saved = sleepEntryRepository.findAll().get(0);
        assertEquals("Good sleep", saved.getNotes());
    }

    @Test
    void testHandleAddEntryClearsNotesField() throws Exception {
        controller.initialize();
        notesField.setText("Good sleep");
        User user = new User("testuser", "password");
        controller.setCurrentUser(user);

        Method handleAddEntry = SleepTrackingController.class.getDeclaredMethod("handleAddEntry");
        handleAddEntry.setAccessible(true);
        handleAddEntry.invoke(controller);

        assertEquals("", notesField.getText());
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
}
