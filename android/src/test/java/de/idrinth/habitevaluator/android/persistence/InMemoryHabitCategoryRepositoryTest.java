package de.idrinth.habitevaluator.android.persistence;

import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.User;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class InMemoryHabitCategoryRepositoryTest {

    private InMemoryHabitCategoryRepository repository;

    @Before
    public void setUp() {
        repository = new InMemoryHabitCategoryRepository();
    }

    @Test
    public void testSaveAndFindById() {
        HabitCategory category = new HabitCategory("Health");
        repository.save(category);

        Optional<HabitCategory> found = repository.findById(category.getId());
        assertTrue(found.isPresent());
        assertEquals("Health", found.get().getName());
    }

    @Test
    public void testFindByIdNotFound() {
        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    public void testFindAll() {
        repository.save(new HabitCategory("Health"));
        repository.save(new HabitCategory("Education"));

        assertEquals(2, repository.findAll().size());
    }

    @Test
    public void testDeleteById() {
        HabitCategory category = new HabitCategory("Health");
        repository.save(category);
        assertTrue(repository.existsById(category.getId()));

        repository.deleteById(category.getId());
        assertFalse(repository.existsById(category.getId()));
    }

    @Test
    public void testExistsById() {
        HabitCategory category = new HabitCategory("Health");
        assertFalse(repository.existsById(category.getId()));

        repository.save(category);
        assertTrue(repository.existsById(category.getId()));
    }

    @Test
    public void testFindByUserId() {
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
    public void testUpdateExisting() {
        HabitCategory category = new HabitCategory("Health");
        repository.save(category);

        category.setName("Updated");
        repository.save(category);

        assertEquals(1, repository.findAll().size());
        assertEquals("Updated", repository.findById(category.getId()).get().getName());
    }

    @Test
    public void testEmptyRepository() {
        assertTrue(repository.findAll().isEmpty());
    }
}
