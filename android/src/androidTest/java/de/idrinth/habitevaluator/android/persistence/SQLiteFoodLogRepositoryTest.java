package de.idrinth.habitevaluator.android.persistence;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import de.idrinth.habitevaluator.shared.model.FoodLog;
import de.idrinth.habitevaluator.shared.model.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SQLiteFoodLogRepositoryTest {

    private SQLiteFoodLogRepository repository;
    private SQLiteHelper dbHelper;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase("habit_evaluator.db");
        dbHelper = SQLiteHelper.getInstance(context);
        repository = new SQLiteFoodLogRepository(dbHelper);
    }

    @After
    public void tearDown() {
        dbHelper.close();
        context.deleteDatabase("habit_evaluator.db");
    }

    @Test
    public void testSaveAndFindById() {
        FoodLog log = createFoodLog("fl-1", "Oatmeal, Banana", 45.0, 350, "u1");
        repository.save(log);

        Optional<FoodLog> found = repository.findById("fl-1");
        assertTrue(found.isPresent());
        assertEquals("Oatmeal, Banana", found.get().getFoodItems());
        assertEquals(45.0, found.get().getCarbohydrates(), 0.001);
        assertEquals(Integer.valueOf(350), found.get().getKcal());
    }

    @Test
    public void testFindByIdNotFound() {
        Optional<FoodLog> found = repository.findById("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindAll() {
        repository.save(createFoodLog("fl-1", "Oatmeal", 30.0, 200, "u1"));
        repository.save(createFoodLog("fl-2", "Salad", 10.0, 150, "u1"));

        List<FoodLog> all = repository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    public void testDeleteById() {
        repository.save(createFoodLog("to-delete", "Temp", 0.0, 0, "u1"));
        assertTrue(repository.existsById("to-delete"));

        repository.deleteById("to-delete");
        assertFalse(repository.existsById("to-delete"));
    }

    @Test
    public void testExistsById() {
        assertFalse(repository.existsById("fl-check"));
        repository.save(createFoodLog("fl-check", "Food", 10.0, 100, "u1"));
        assertTrue(repository.existsById("fl-check"));
    }

    @Test
    public void testFindByUserId() {
        repository.save(createFoodLog("fl-1", "Oatmeal", 30.0, 200, "u1"));
        repository.save(createFoodLog("fl-2", "Salad", 10.0, 150, "u1"));
        repository.save(createFoodLog("fl-3", "Pizza", 60.0, 800, "u2"));

        List<FoodLog> user1Logs = repository.findByUserId("u1");
        assertEquals(2, user1Logs.size());

        List<FoodLog> user2Logs = repository.findByUserId("u2");
        assertEquals(1, user2Logs.size());
    }

    @Test
    public void testSaveWithNullCarbohydrates() {
        FoodLog log = createFoodLog("fl-no-carb", "Water", null, null, "u1");
        repository.save(log);

        Optional<FoodLog> found = repository.findById("fl-no-carb");
        assertTrue(found.isPresent());
        assertNull(found.get().getCarbohydrates());
        assertNull(found.get().getKcal());
    }

    @Test
    public void testSaveWithNotes() {
        FoodLog log = createFoodLog("fl-notes", "Lunch", 40.0, 500, "u1");
        log.setNotes("Had a big lunch today");
        repository.save(log);

        Optional<FoodLog> found = repository.findById("fl-notes");
        assertTrue(found.isPresent());
        assertEquals("Had a big lunch today", found.get().getNotes());
    }

    @Test
    public void testUpdateExistingLog() {
        repository.save(createFoodLog("fl-update", "Oatmeal", 30.0, 200, "u1"));

        FoodLog updated = createFoodLog("fl-update", "Oatmeal with fruit", 45.0, 300, "u1");
        repository.save(updated);

        Optional<FoodLog> found = repository.findById("fl-update");
        assertTrue(found.isPresent());
        assertEquals("Oatmeal with fruit", found.get().getFoodItems());
        assertEquals(45.0, found.get().getCarbohydrates(), 0.001);
        assertEquals(Integer.valueOf(300), found.get().getKcal());
    }

    @Test
    public void testSavePreservesUser() {
        FoodLog log = createFoodLog("fl-user", "Food", 10.0, 100, "u1");
        repository.save(log);

        Optional<FoodLog> found = repository.findById("fl-user");
        assertTrue(found.isPresent());
        assertNotNull(found.get().getUser());
        assertEquals("u1", found.get().getUser().getId());
    }

    @Test
    public void testSavePreservesDateTime() {
        LocalDateTime specificTime = LocalDateTime.of(2025, 3, 15, 12, 30, 0);
        FoodLog log = createFoodLog("fl-time", "Lunch", 40.0, 500, "u1");
        log.setDateTime(specificTime);
        repository.save(log);

        Optional<FoodLog> found = repository.findById("fl-time");
        assertTrue(found.isPresent());
        assertEquals(specificTime, found.get().getDateTime());
    }

    private FoodLog createFoodLog(String id, String foodItems, Double carbs, Integer kcal, String userId) {
        FoodLog log = new FoodLog();
        log.setId(id);
        log.setFoodItems(foodItems);
        log.setCarbohydrates(carbs);
        log.setKcal(kcal);
        log.setDateTime(LocalDateTime.now());
        log.setCreatedAt(LocalDateTime.now());
        User user = new User();
        user.setId(userId);
        user.setUsername("user_" + userId);
        user.setPassword("placeholder");
        log.setUser(user);
        return log;
    }
}
