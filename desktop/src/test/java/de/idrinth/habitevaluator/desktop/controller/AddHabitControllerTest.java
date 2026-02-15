package de.idrinth.habitevaluator.desktop.controller;

import de.idrinth.habitevaluator.shared.model.FrequencyType;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AddHabitControllerTest extends JavaFXControllerTestBase {

    private AddHabitController controller;
    private StubHabitRepository habitRepository;
    private ComboBox<String> categoryComboBox;
    private ComboBox<String> frequencyTypeComboBox;

    @BeforeEach
    void setUp() throws Exception {
        controller = new AddHabitController();
        habitRepository = new StubHabitRepository();

        setField(controller, "habitNameField", new TextField());
        setField(controller, "habitDescriptionArea", new TextArea());
        categoryComboBox = new ComboBox<>();
        setField(controller, "categoryComboBox", categoryComboBox);
        frequencyTypeComboBox = new ComboBox<>();
        setField(controller, "frequencyTypeComboBox", frequencyTypeComboBox);
        setField(controller, "targetFrequencyField", new TextField("1"));
        setField(controller, "maxEntriesPerDayField", new TextField("1"));
        setField(controller, "positiveScoringCheckBox", new CheckBox());
        setField(controller, "threshold1Field", new TextField("1"));
        setField(controller, "threshold2Field", new TextField("2"));
        setField(controller, "threshold4Field", new TextField("4"));
        setField(controller, "threshold8Field", new TextField("7"));
        setField(controller, "translationsContainer", new VBox());

        controller.setHabitRepository(habitRepository);
        controller.setCurrentUser(new User("testuser", "password"));
    }

    @Test
    void testGetAddedHabitIsNullInitially() {
        assertNull(controller.getAddedHabit());
    }

    @Test
    void testInitializePopulatesFrequencyTypes() {
        controller.initialize();

        assertEquals(FrequencyType.values().length, frequencyTypeComboBox.getItems().size());
        assertEquals(0, frequencyTypeComboBox.getSelectionModel().getSelectedIndex());
    }

    @Test
    void testInitializeFrequencyTypeLabelsAreFormatted() {
        controller.initialize();

        // DAILY -> "Daily", WEEKLY -> "Weekly", MONTHLY -> "Monthly"
        for (int i = 0; i < FrequencyType.values().length; i++) {
            String expected = FrequencyType.values()[i].name().substring(0, 1)
                    + FrequencyType.values()[i].name().substring(1).toLowerCase();
            assertEquals(expected, frequencyTypeComboBox.getItems().get(i));
        }
    }

    @Test
    void testSetCategoryListPopulatesComboBox() {
        HabitCategory cat = new HabitCategory("Health");
        controller.setCategoryList(List.of(cat));

        // First item is "New category", second is our category
        assertEquals(2, categoryComboBox.getItems().size());
        assertEquals("New category", categoryComboBox.getItems().get(0));
        assertEquals("Health", categoryComboBox.getItems().get(1));
    }

    @Test
    void testSetCategoryListSelectsFirstRealCategory() {
        HabitCategory cat = new HabitCategory("Health");
        controller.setCategoryList(List.of(cat));

        assertEquals("Health", categoryComboBox.getSelectionModel().getSelectedItem());
    }

    @Test
    void testSetCategoryListEmptySelectsNewCategory() {
        controller.setCategoryList(Collections.emptyList());

        assertEquals("New category", categoryComboBox.getSelectionModel().getSelectedItem());
    }

    @Test
    void testSetCategoryListWithMultipleCategories() {
        HabitCategory cat1 = new HabitCategory("Health");
        HabitCategory cat2 = new HabitCategory("Learning");
        controller.setCategoryList(List.of(cat1, cat2));

        assertEquals(3, categoryComboBox.getItems().size());
        assertEquals("Health", categoryComboBox.getItems().get(1));
        assertEquals("Learning", categoryComboBox.getItems().get(2));
    }

    @Test
    void testSetCategoryListStoresCategories() {
        HabitCategory cat = new HabitCategory("Health");
        controller.setCategoryList(List.of(cat));

        // Should store the list (verified through combo box content)
        assertTrue(categoryComboBox.getItems().contains("Health"));
    }

    @Test
    void testInitializeSetsDefaultFrequencySelection() {
        controller.initialize();

        // Default selection should be the first frequency type
        assertEquals(0, frequencyTypeComboBox.getSelectionModel().getSelectedIndex());
        assertNotNull(frequencyTypeComboBox.getSelectionModel().getSelectedItem());
    }

    @Test
    void testSetCategoryListWithNewCategoryOption() {
        controller.setCategoryList(Collections.emptyList());

        // Should always have "New category" as first item
        assertEquals("New category", categoryComboBox.getItems().get(0));
    }

    @Test
    void testSetStorageConfigWithNullDoesNotThrow() {
        assertDoesNotThrow(() -> controller.setStorageConfig(null));
    }

    @Test
    void testSetCategoryRepositoryDoesNotThrow() {
        assertDoesNotThrow(() -> controller.setCategoryRepository(null));
    }

    @Test
    void testSetApiClientDoesNotThrow() {
        assertDoesNotThrow(() -> controller.setApiClient(null));
    }

    @Test
    void testGetAddedHabitRemainsNullAfterSetup() {
        controller.initialize();
        HabitCategory cat = new HabitCategory("Health");
        controller.setCategoryList(List.of(cat));

        // Without calling handleAddHabit, addedHabit should remain null
        assertNull(controller.getAddedHabit());
    }

    @Test
    void testInitializeFrequencyTypeContainsDailyWeeklyMonthly() {
        controller.initialize();

        List<String> items = frequencyTypeComboBox.getItems();
        assertTrue(items.contains("Daily"));
        assertTrue(items.contains("Weekly"));
        assertTrue(items.contains("Monthly"));
    }

    @Test
    void testSetStorageConfigWithNullHidesTranslations() {
        controller.setStorageConfig(null);

        VBox translationsContainer = getTranslationsContainer();
        assertFalse(translationsContainer.isVisible());
        assertFalse(translationsContainer.isManaged());
    }

    @Test
    void testCreateCategoryWithLocalRepository() throws Exception {
        StubHabitCategoryRepository categoryRepository = new StubHabitCategoryRepository();
        controller.setCategoryRepository(categoryRepository);

        java.lang.reflect.Method createCategory = AddHabitController.class.getDeclaredMethod("createCategory", String.class);
        createCategory.setAccessible(true);
        createCategory.invoke(controller, "Fitness");

        assertEquals(1, categoryRepository.findAll().size());
        assertEquals("Fitness", categoryRepository.findAll().get(0).getName());
    }

    @Test
    void testCreateCategoryAddsToComboBox() throws Exception {
        StubHabitCategoryRepository categoryRepository = new StubHabitCategoryRepository();
        controller.setCategoryRepository(categoryRepository);
        controller.setCategoryList(Collections.emptyList());

        java.lang.reflect.Method createCategory = AddHabitController.class.getDeclaredMethod("createCategory", String.class);
        createCategory.setAccessible(true);
        createCategory.invoke(controller, "Health");

        assertTrue(categoryComboBox.getItems().contains("Health"));
    }

    @Test
    void testCreateCategoryWithNullRepositoriesDoesNotThrow() throws Exception {
        controller.setCategoryRepository(null);
        controller.setApiClient(null);

        java.lang.reflect.Method createCategory = AddHabitController.class.getDeclaredMethod("createCategory", String.class);
        createCategory.setAccessible(true);
        assertDoesNotThrow(() -> createCategory.invoke(controller, "Test"));
    }

    @Test
    void testPopulateCategoryComboBoxSetsCategoryNameToIdMapping() throws Exception {
        HabitCategory cat = new HabitCategory("Health", "Health desc", "#00FF00");
        controller.setCategoryList(List.of(cat));

        java.lang.reflect.Field field = AddHabitController.class.getDeclaredField("categoryNameToId");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, String> mapping = (Map<String, String>) field.get(controller);
        assertEquals(cat.getId(), mapping.get("Health"));
    }

    private VBox getTranslationsContainer() {
        try {
            java.lang.reflect.Field field = AddHabitController.class.getDeclaredField("translationsContainer");
            field.setAccessible(true);
            return (VBox) field.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Minimal stub for HabitRepository that stores habits in memory.
     */
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
}
