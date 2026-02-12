package de.idrinth.habitevaluator.android.persistence;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SQLiteHabitCategoryRepositoryTest {

    private SQLiteHabitCategoryRepository repository;
    private SQLiteHelper dbHelper;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase("habit_evaluator.db");
        dbHelper = SQLiteHelper.getInstance(context);
        repository = new SQLiteHabitCategoryRepository(dbHelper);
    }

    @After
    public void tearDown() {
        dbHelper.close();
        context.deleteDatabase("habit_evaluator.db");
    }

    @Test
    public void testSaveAndFindById() {
        HabitCategory category = createCategory("cat-1", "Health", "Health related habits", "#FF0000");
        repository.save(category);

        Optional<HabitCategory> found = repository.findById("cat-1");
        assertTrue(found.isPresent());
        assertEquals("Health", found.get().getName());
        assertEquals("Health related habits", found.get().getDescription());
        assertEquals("#FF0000", found.get().getColor());
    }

    @Test
    public void testFindByIdNotFound() {
        Optional<HabitCategory> found = repository.findById("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindAll() {
        repository.save(createCategory("c1", "Health", "Desc", "#FF0000"));
        repository.save(createCategory("c2", "Fitness", "Desc", "#00FF00"));
        repository.save(createCategory("c3", "Mental", "Desc", "#0000FF"));

        List<HabitCategory> all = repository.findAll();
        assertEquals(3, all.size());
    }

    @Test
    public void testFindAllEmpty() {
        List<HabitCategory> all = repository.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    public void testDeleteById() {
        repository.save(createCategory("to-delete", "Temp", "Desc", "#000000"));
        assertTrue(repository.existsById("to-delete"));

        repository.deleteById("to-delete");
        assertFalse(repository.existsById("to-delete"));
    }

    @Test
    public void testExistsById() {
        assertFalse(repository.existsById("cat-check"));
        repository.save(createCategory("cat-check", "Health", "Desc", "#FF0000"));
        assertTrue(repository.existsById("cat-check"));
    }

    @Test
    public void testFindByUserId() {
        User user1 = createUser("u1", "alice");
        User user2 = createUser("u2", "bob");

        HabitCategory c1 = createCategory("c1", "Health", "Desc", "#FF0000");
        c1.setUser(user1);
        HabitCategory c2 = createCategory("c2", "Fitness", "Desc", "#00FF00");
        c2.setUser(user1);
        HabitCategory c3 = createCategory("c3", "Mental", "Desc", "#0000FF");
        c3.setUser(user2);

        repository.save(c1);
        repository.save(c2);
        repository.save(c3);

        List<HabitCategory> aliceCategories = repository.findByUserId("u1");
        assertEquals(2, aliceCategories.size());

        List<HabitCategory> bobCategories = repository.findByUserId("u2");
        assertEquals(1, bobCategories.size());
    }

    @Test
    public void testSaveWithTranslations() {
        HabitCategory category = createCategory("cat-trans", "Health", "Health desc", "#FF0000");
        Map<String, String> nameTranslations = new HashMap<>();
        nameTranslations.put("de", "Gesundheit");
        nameTranslations.put("es", "Salud");
        category.setNameTranslations(nameTranslations);

        Map<String, String> descTranslations = new HashMap<>();
        descTranslations.put("de", "Gesundheitsbeschreibung");
        category.setDescriptionTranslations(descTranslations);

        repository.save(category);

        Optional<HabitCategory> found = repository.findById("cat-trans");
        assertTrue(found.isPresent());
        assertEquals("Gesundheit", found.get().getNameTranslations().get("de"));
        assertEquals("Salud", found.get().getNameTranslations().get("es"));
        assertEquals("Gesundheitsbeschreibung", found.get().getDescriptionTranslations().get("de"));
    }

    @Test
    public void testUpdateExistingCategory() {
        repository.save(createCategory("cat-update", "Health", "Initial", "#FF0000"));

        HabitCategory updated = createCategory("cat-update", "Wellness", "Updated", "#00FF00");
        repository.save(updated);

        Optional<HabitCategory> found = repository.findById("cat-update");
        assertTrue(found.isPresent());
        assertEquals("Wellness", found.get().getName());
        assertEquals("Updated", found.get().getDescription());
        assertEquals("#00FF00", found.get().getColor());
    }

    @Test
    public void testSaveWithUser() {
        HabitCategory category = createCategory("cat-user", "Health", "Desc", "#FF0000");
        User user = createUser("u1", "testuser");
        category.setUser(user);

        repository.save(category);

        Optional<HabitCategory> found = repository.findById("cat-user");
        assertTrue(found.isPresent());
        assertNotNull(found.get().getUser());
        assertEquals("u1", found.get().getUser().getId());
        assertEquals("testuser", found.get().getUser().getUsername());
    }

    private HabitCategory createCategory(String id, String name, String description, String color) {
        HabitCategory category = new HabitCategory();
        category.setId(id);
        category.setName(name);
        category.setDescription(description);
        category.setColor(color);
        return category;
    }

    private User createUser(String id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword("placeholder");
        return user;
    }
}
