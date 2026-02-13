package de.idrinth.habitevaluator.shared.persistence;

import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.User;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemHabitCategoryRepositoryTest {

    private File tempDir;
    private FileSystemHabitCategoryRepository repository;

    @BeforeEach
    void setUp() {
        tempDir = new File(System.getProperty("java.io.tmpdir"), "category-test-" + System.nanoTime());
        tempDir.mkdirs();
        repository = new FileSystemHabitCategoryRepository(tempDir);
    }

    @AfterEach
    void tearDown() {
        File[] files = tempDir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
        tempDir.delete();
    }

    @Test
    void testSaveAndFindById() {
        HabitCategory category = new HabitCategory("Health", "Health related habits", "#FF0000");
        repository.save(category);

        Optional<HabitCategory> found = repository.findById(category.getId());
        assertTrue(found.isPresent());
        assertEquals("Health", found.get().getName());
        assertEquals("Health related habits", found.get().getDescription());
        assertEquals("#FF0000", found.get().getColor());
    }

    @Test
    void testFindByIdNotFound() {
        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    void testFindAll() {
        repository.save(new HabitCategory("Health"));
        repository.save(new HabitCategory("Education"));
        repository.save(new HabitCategory("Fitness"));

        assertEquals(3, repository.findAll().size());
    }

    @Test
    void testDeleteById() {
        HabitCategory category = new HabitCategory("Health");
        repository.save(category);
        assertTrue(repository.existsById(category.getId()));

        repository.deleteById(category.getId());
        assertFalse(repository.existsById(category.getId()));
    }

    @Test
    void testExistsById() {
        HabitCategory category = new HabitCategory("Health");
        assertFalse(repository.existsById(category.getId()));

        repository.save(category);
        assertTrue(repository.existsById(category.getId()));
    }

    @Test
    void testFindByUserId() {
        User user = new User("testuser", "password");
        HabitCategory c1 = new HabitCategory("Health");
        c1.setUser(user);
        HabitCategory c2 = new HabitCategory("Education");

        repository.save(c1);
        repository.save(c2);

        List<HabitCategory> userCategories = repository.findByUserId(user.getId());
        assertEquals(1, userCategories.size());
        assertEquals("Health", userCategories.get(0).getName());
    }

    @Test
    void testPersistenceAcrossInstances() {
        HabitCategory category = new HabitCategory("Health", "Health habits", "#00FF00");
        repository.save(category);

        FileSystemHabitCategoryRepository newRepo = new FileSystemHabitCategoryRepository(tempDir);
        Optional<HabitCategory> found = newRepo.findById(category.getId());
        assertTrue(found.isPresent());
        assertEquals("Health", found.get().getName());
        assertEquals("Health habits", found.get().getDescription());
        assertEquals("#00FF00", found.get().getColor());
    }

    @Test
    void testSaveWithTranslations() {
        HabitCategory category = new HabitCategory("Health");
        Map<String, String> nameTranslations = new HashMap<>();
        nameTranslations.put("de", "Gesundheit");
        nameTranslations.put("es", "Salud");
        category.setNameTranslations(nameTranslations);

        Map<String, String> descTranslations = new HashMap<>();
        descTranslations.put("de", "Gesundheitsbezogene Gewohnheiten");
        category.setDescriptionTranslations(descTranslations);

        repository.save(category);

        FileSystemHabitCategoryRepository newRepo = new FileSystemHabitCategoryRepository(tempDir);
        Optional<HabitCategory> found = newRepo.findById(category.getId());
        assertTrue(found.isPresent());
        assertEquals("Gesundheit", found.get().getNameTranslations().get("de"));
        assertEquals("Salud", found.get().getNameTranslations().get("es"));
        assertEquals("Gesundheitsbezogene Gewohnheiten", found.get().getDescriptionTranslations().get("de"));
    }

    @Test
    void testSaveWithUser() {
        User user = new User("testuser", "password");
        HabitCategory category = new HabitCategory("Health");
        category.setUser(user);
        repository.save(category);

        FileSystemHabitCategoryRepository newRepo = new FileSystemHabitCategoryRepository(tempDir);
        Optional<HabitCategory> found = newRepo.findById(category.getId());
        assertTrue(found.isPresent());
        assertNotNull(found.get().getUser());
        assertEquals(user.getId(), found.get().getUser().getId());
    }

    @Test
    void testEmptyRepository() {
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void testUpdateExistingCategory() {
        HabitCategory category = new HabitCategory("Health");
        repository.save(category);

        category.setName("Updated Health");
        category.setColor("#0000FF");
        repository.save(category);

        assertEquals(1, repository.findAll().size());
        assertEquals("Updated Health", repository.findById(category.getId()).get().getName());
    }
}
