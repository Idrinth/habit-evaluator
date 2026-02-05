package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class H2HabitCategoryRepositoryTest extends H2RepositoryTestBase {

    private H2HabitCategoryRepository categoryRepository;
    private H2UserRepository userRepository;
    private User testUser;

    @BeforeEach
    void setUp() {
        categoryRepository = new H2HabitCategoryRepository();
        userRepository = new H2UserRepository();

        for (HabitCategory category : categoryRepository.findAll()) {
            categoryRepository.deleteById(category.getId());
        }
        for (User user : userRepository.findAll()) {
            userRepository.deleteById(user.getId());
        }

        testUser = new User("catuser", "password123");
        userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        HabitCategory category = new HabitCategory("Health", "Health related habits", "#00FF00");
        category.setUser(testUser);
        HabitCategory saved = categoryRepository.save(category);

        assertNotNull(saved);
        Optional<HabitCategory> found = categoryRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Health", found.get().getName());
        assertEquals("#00FF00", found.get().getColor());
    }

    @Test
    void testSaveUpdatesExistingCategory() {
        HabitCategory category = new HabitCategory("Health");
        category.setUser(testUser);
        categoryRepository.save(category);

        category.setDescription("Updated description");
        categoryRepository.save(category);

        Optional<HabitCategory> found = categoryRepository.findById(category.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated description", found.get().getDescription());
    }

    @Test
    void testFindByIdReturnsEmptyForNonExistent() {
        Optional<HabitCategory> found = categoryRepository.findById("nonexistent-id");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        HabitCategory cat1 = new HabitCategory("Health");
        cat1.setUser(testUser);
        HabitCategory cat2 = new HabitCategory("Productivity");
        cat2.setUser(testUser);
        categoryRepository.save(cat1);
        categoryRepository.save(cat2);

        List<HabitCategory> all = categoryRepository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testDeleteById() {
        HabitCategory category = new HabitCategory("ToDelete");
        category.setUser(testUser);
        categoryRepository.save(category);

        assertTrue(categoryRepository.existsById(category.getId()));
        categoryRepository.deleteById(category.getId());
        assertFalse(categoryRepository.existsById(category.getId()));
    }

    @Test
    void testDeleteByIdNonExistentDoesNotThrow() {
        assertDoesNotThrow(() -> categoryRepository.deleteById("nonexistent-id"));
    }

    @Test
    void testExistsById() {
        HabitCategory category = new HabitCategory("Exists");
        category.setUser(testUser);
        categoryRepository.save(category);

        assertTrue(categoryRepository.existsById(category.getId()));
        assertFalse(categoryRepository.existsById("nonexistent-id"));
    }

    @Test
    void testFindByUserId() {
        HabitCategory cat1 = new HabitCategory("Health");
        cat1.setUser(testUser);
        categoryRepository.save(cat1);

        User otherUser = new User("othercat", "password123");
        userRepository.save(otherUser);
        HabitCategory cat2 = new HabitCategory("Fitness");
        cat2.setUser(otherUser);
        categoryRepository.save(cat2);

        List<HabitCategory> userCategories = categoryRepository.findByUserId(testUser.getId());
        assertEquals(1, userCategories.size());
        assertEquals("Health", userCategories.get(0).getName());
    }

    @Test
    void testFindByUserIdReturnsEmptyForNonExistentUser() {
        List<HabitCategory> categories = categoryRepository.findByUserId("nonexistent-user-id");
        assertTrue(categories.isEmpty());
    }
}
