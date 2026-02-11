package de.idrinth.habitevaluator.desktop.controller;

import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EmotionEntryControllerTest extends JavaFXControllerTestBase {

    private EmotionEntryController controller;
    private ComboBox<EmotionPair> emotionPairComboBox;
    private Slider strengthSlider;
    private Label strengthLabel;
    private Label negativeEndLabel;
    private Label positiveEndLabel;
    private DatePicker datePicker;
    private Spinner<Integer> hourSpinner;
    private Spinner<Integer> minuteSpinner;
    private TextArea notesArea;
    private Label statusLabel;
    private VBox entriesContainer;
    private StubEmotionPairRepository pairRepository;
    private StubEmotionEntryRepository entryRepository;

    @BeforeEach
    void setUp() throws Exception {
        controller = new EmotionEntryController();
        pairRepository = new StubEmotionPairRepository();
        entryRepository = new StubEmotionEntryRepository();

        emotionPairComboBox = new ComboBox<>();
        strengthSlider = new Slider(-10, 10, 0);
        strengthLabel = new Label();
        negativeEndLabel = new Label();
        positiveEndLabel = new Label();
        datePicker = new DatePicker();
        hourSpinner = new Spinner<>();
        hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12));
        minuteSpinner = new Spinner<>();
        minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        notesArea = new TextArea();
        statusLabel = new Label();
        entriesContainer = new VBox();

        setField(controller, "emotionPairComboBox", emotionPairComboBox);
        setField(controller, "strengthSlider", strengthSlider);
        setField(controller, "strengthLabel", strengthLabel);
        setField(controller, "negativeEndLabel", negativeEndLabel);
        setField(controller, "positiveEndLabel", positiveEndLabel);
        setField(controller, "datePicker", datePicker);
        setField(controller, "hourSpinner", hourSpinner);
        setField(controller, "minuteSpinner", minuteSpinner);
        setField(controller, "notesArea", notesArea);
        setField(controller, "statusLabel", statusLabel);
        setField(controller, "entriesContainer", entriesContainer);

        controller.setEmotionPairRepository(pairRepository);
        controller.setEmotionEntryRepository(entryRepository);
    }

    @Test
    void testInitializeSetsDateToToday() {
        controller.initialize();

        assertEquals(LocalDate.now(), datePicker.getValue());
    }

    @Test
    void testInitializeSetsUpStrengthSliderListener() {
        controller.initialize();

        // Changing the slider should update the strength label
        strengthSlider.setValue(5);
        assertNotNull(strengthLabel.getText());
        assertFalse(strengthLabel.getText().isEmpty());
    }

    @Test
    void testLoadDataWithNoPairsShowsEmptyComboBox() {
        User user = new User("testuser", "password");
        controller.setCurrentUser(user);
        controller.initialize();
        controller.loadData();

        assertTrue(emotionPairComboBox.getItems().isEmpty());
    }

    @Test
    void testLoadDataPopulatesPairsComboBox() {
        User user = new User("testuser", "password");
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        pair.setUser(user);
        pairRepository.save(pair);

        controller.setCurrentUser(user);
        controller.initialize();
        controller.loadData();

        assertEquals(1, emotionPairComboBox.getItems().size());
        assertEquals("Sad", emotionPairComboBox.getItems().get(0).getNegativeLabel());
    }

    @Test
    void testLoadDataSelectsFirstPair() {
        User user = new User("testuser", "password");
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        pair.setUser(user);
        pairRepository.save(pair);

        controller.setCurrentUser(user);
        controller.initialize();
        controller.loadData();

        assertNotNull(emotionPairComboBox.getSelectionModel().getSelectedItem());
    }

    @Test
    void testLoadDataShowsEmptyMessageWhenNoEntries() {
        User user = new User("testuser", "password");
        controller.setCurrentUser(user);
        controller.initialize();
        controller.loadData();

        assertEquals(1, entriesContainer.getChildren().size());
        assertTrue(entriesContainer.getChildren().get(0) instanceof Label);
    }

    @Test
    void testHandleRecordEmotionWithNoPairSelectedShowsError() throws Exception {
        User user = new User("testuser", "password");
        controller.setCurrentUser(user);
        controller.initialize();

        Method handleRecord = EmotionEntryController.class.getDeclaredMethod("handleRecordEmotion");
        handleRecord.setAccessible(true);
        handleRecord.invoke(controller);

        assertEquals("Select an emotion pair first.", statusLabel.getText());
        assertTrue(entryRepository.findAll().isEmpty());
    }

    @Test
    void testHandleRecordEmotionSuccessful() throws Exception {
        User user = new User("testuser", "password");
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        pair.setUser(user);
        pairRepository.save(pair);

        controller.setCurrentUser(user);
        controller.initialize();
        controller.loadData();

        strengthSlider.setValue(3);
        notesArea.setText("Feeling good");

        Method handleRecord = EmotionEntryController.class.getDeclaredMethod("handleRecordEmotion");
        handleRecord.setAccessible(true);
        handleRecord.invoke(controller);

        assertEquals("Emotion recorded.", statusLabel.getText());
        assertEquals(1, entryRepository.findAll().size());
    }

    @Test
    void testHandleRecordEmotionClearsNotesAndResetsSlider() throws Exception {
        User user = new User("testuser", "password");
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        pair.setUser(user);
        pairRepository.save(pair);

        controller.setCurrentUser(user);
        controller.initialize();
        controller.loadData();

        strengthSlider.setValue(5);
        notesArea.setText("Some notes");

        Method handleRecord = EmotionEntryController.class.getDeclaredMethod("handleRecordEmotion");
        handleRecord.setAccessible(true);
        handleRecord.invoke(controller);

        assertEquals(0, (int) strengthSlider.getValue());
        assertEquals("", notesArea.getText());
    }

    @Test
    void testEmotionPairSelectionUpdatesEndLabels() {
        controller.initialize();

        EmotionPair pair = new EmotionPair("Anxious", "Calm");
        emotionPairComboBox.getItems().add(pair);
        emotionPairComboBox.getSelectionModel().selectFirst();

        assertEquals("Anxious", negativeEndLabel.getText());
        assertEquals("Calm", positiveEndLabel.getText());
    }

    private static class StubEmotionPairRepository implements EmotionPairRepository {
        private final List<EmotionPair> pairs = new ArrayList<>();

        @Override
        public EmotionPair save(EmotionPair pair) {
            pairs.add(pair);
            return pair;
        }

        @Override
        public Optional<EmotionPair> findById(String id) {
            return pairs.stream().filter(p -> p.getId().equals(id)).findFirst();
        }

        @Override
        public List<EmotionPair> findAll() {
            return new ArrayList<>(pairs);
        }

        @Override
        public void deleteById(String id) {
            pairs.removeIf(p -> p.getId().equals(id));
        }

        @Override
        public List<EmotionPair> findByUserId(String userId) {
            return pairs.stream().filter(p -> p.getUser() != null
                    && p.getUser().getId().equals(userId)).toList();
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
