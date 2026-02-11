package de.idrinth.habitevaluator.desktop.controller;

import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EmotionPairControllerTest extends JavaFXControllerTestBase {

    private EmotionPairController controller;
    private TextField negativeLabelField;
    private TextField positiveLabelField;
    private Label statusLabel;
    private VBox pairsContainer;
    private StubEmotionPairRepository pairRepository;
    private StubEmotionEntryRepository entryRepository;

    @BeforeEach
    void setUp() throws Exception {
        controller = new EmotionPairController();
        pairRepository = new StubEmotionPairRepository();
        entryRepository = new StubEmotionEntryRepository();

        negativeLabelField = new TextField();
        positiveLabelField = new TextField();
        statusLabel = new Label();
        pairsContainer = new VBox();

        setField(controller, "negativeLabelField", negativeLabelField);
        setField(controller, "positiveLabelField", positiveLabelField);
        setField(controller, "statusLabel", statusLabel);
        setField(controller, "pairsContainer", pairsContainer);

        controller.setEmotionPairRepository(pairRepository);
        controller.setEmotionEntryRepository(entryRepository);
    }

    @Test
    void testLoadDataWithNoUser() {
        controller.loadData();

        assertEquals(1, pairsContainer.getChildren().size());
        assertTrue(pairsContainer.getChildren().get(0) instanceof Label);
    }

    @Test
    void testLoadDataWithUserShowsPairs() {
        User user = new User("testuser", "password");
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        pair.setUser(user);
        pairRepository.save(pair);

        controller.setCurrentUser(user);
        controller.loadData();

        assertFalse(pairsContainer.getChildren().isEmpty());
    }

    @Test
    void testLoadDataWithNoPairsShowsEmptyMessage() {
        User user = new User("testuser", "password");
        controller.setCurrentUser(user);
        controller.loadData();

        assertEquals(1, pairsContainer.getChildren().size());
        assertTrue(pairsContainer.getChildren().get(0) instanceof Label);
        assertEquals("No emotion pairs defined yet.",
                ((Label) pairsContainer.getChildren().get(0)).getText());
    }

    @Test
    void testHandleAddPairWithEmptyNegativeShowsError() throws Exception {
        User user = new User("testuser", "password");
        controller.setCurrentUser(user);

        positiveLabelField.setText("Happy");

        Method handleAddPair = EmotionPairController.class.getDeclaredMethod("handleAddPair");
        handleAddPair.setAccessible(true);
        handleAddPair.invoke(controller);

        assertEquals("Both labels are required.", statusLabel.getText());
        assertTrue(pairRepository.findAll().isEmpty());
    }

    @Test
    void testHandleAddPairWithEmptyPositiveShowsError() throws Exception {
        User user = new User("testuser", "password");
        controller.setCurrentUser(user);

        negativeLabelField.setText("Sad");

        Method handleAddPair = EmotionPairController.class.getDeclaredMethod("handleAddPair");
        handleAddPair.setAccessible(true);
        handleAddPair.invoke(controller);

        assertEquals("Both labels are required.", statusLabel.getText());
        assertTrue(pairRepository.findAll().isEmpty());
    }

    @Test
    void testHandleAddPairWithBothEmptyShowsError() throws Exception {
        User user = new User("testuser", "password");
        controller.setCurrentUser(user);

        Method handleAddPair = EmotionPairController.class.getDeclaredMethod("handleAddPair");
        handleAddPair.setAccessible(true);
        handleAddPair.invoke(controller);

        assertEquals("Both labels are required.", statusLabel.getText());
    }

    @Test
    void testHandleAddPairSuccessful() throws Exception {
        User user = new User("testuser", "password");
        controller.setCurrentUser(user);

        negativeLabelField.setText("Sad");
        positiveLabelField.setText("Happy");

        Method handleAddPair = EmotionPairController.class.getDeclaredMethod("handleAddPair");
        handleAddPair.setAccessible(true);
        handleAddPair.invoke(controller);

        assertEquals("Emotion pair added.", statusLabel.getText());
        assertEquals(1, pairRepository.findAll().size());
        assertEquals("Sad", pairRepository.findAll().get(0).getNegativeLabel());
        assertEquals("Happy", pairRepository.findAll().get(0).getPositiveLabel());
    }

    @Test
    void testHandleAddPairClearsFields() throws Exception {
        User user = new User("testuser", "password");
        controller.setCurrentUser(user);

        negativeLabelField.setText("Sad");
        positiveLabelField.setText("Happy");

        Method handleAddPair = EmotionPairController.class.getDeclaredMethod("handleAddPair");
        handleAddPair.setAccessible(true);
        handleAddPair.invoke(controller);

        assertEquals("", negativeLabelField.getText());
        assertEquals("", positiveLabelField.getText());
    }

    @Test
    void testHandleAddPairTrimsWhitespace() throws Exception {
        User user = new User("testuser", "password");
        controller.setCurrentUser(user);

        negativeLabelField.setText("  Sad  ");
        positiveLabelField.setText("  Happy  ");

        Method handleAddPair = EmotionPairController.class.getDeclaredMethod("handleAddPair");
        handleAddPair.setAccessible(true);
        handleAddPair.invoke(controller);

        assertEquals("Sad", pairRepository.findAll().get(0).getNegativeLabel());
        assertEquals("Happy", pairRepository.findAll().get(0).getPositiveLabel());
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
