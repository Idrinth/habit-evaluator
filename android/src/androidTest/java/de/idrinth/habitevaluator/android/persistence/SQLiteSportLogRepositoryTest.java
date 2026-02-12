package de.idrinth.habitevaluator.android.persistence;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import de.idrinth.habitevaluator.shared.model.SportLog;
import de.idrinth.habitevaluator.shared.model.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SQLiteSportLogRepositoryTest {

    private SQLiteSportLogRepository repository;
    private SQLiteHelper dbHelper;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase("habit_evaluator.db");
        dbHelper = SQLiteHelper.getInstance(context);
        repository = new SQLiteSportLogRepository(dbHelper);
    }

    @After
    public void tearDown() {
        dbHelper.close();
        context.deleteDatabase("habit_evaluator.db");
    }

    @Test
    public void testSaveAndFindById() {
        SportLog log = createSportLog("sl-1", "Running", 5.0, "km", "u1");
        repository.save(log);

        Optional<SportLog> found = repository.findById("sl-1");
        assertTrue(found.isPresent());
        assertEquals("Running", found.get().getName());
        assertEquals(5.0, found.get().getMeasurement(), 0.001);
        assertEquals("km", found.get().getMeasurementUnit());
    }

    @Test
    public void testFindByIdNotFound() {
        Optional<SportLog> found = repository.findById("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindAll() {
        repository.save(createSportLog("sl-1", "Running", 5.0, "km", "u1"));
        repository.save(createSportLog("sl-2", "Swimming", 1.5, "km", "u1"));
        repository.save(createSportLog("sl-3", "Cycling", 20.0, "km", "u1"));

        List<SportLog> all = repository.findAll();
        assertEquals(3, all.size());
    }

    @Test
    public void testDeleteById() {
        repository.save(createSportLog("to-delete", "Running", 5.0, "km", "u1"));
        assertTrue(repository.existsById("to-delete"));

        repository.deleteById("to-delete");
        assertFalse(repository.existsById("to-delete"));
    }

    @Test
    public void testExistsById() {
        assertFalse(repository.existsById("sl-check"));
        repository.save(createSportLog("sl-check", "Running", 5.0, "km", "u1"));
        assertTrue(repository.existsById("sl-check"));
    }

    @Test
    public void testFindByUserId() {
        repository.save(createSportLog("sl-1", "Running", 5.0, "km", "u1"));
        repository.save(createSportLog("sl-2", "Swimming", 1.5, "km", "u1"));
        repository.save(createSportLog("sl-3", "Cycling", 20.0, "km", "u2"));

        List<SportLog> user1Logs = repository.findByUserId("u1");
        assertEquals(2, user1Logs.size());

        List<SportLog> user2Logs = repository.findByUserId("u2");
        assertEquals(1, user2Logs.size());
    }

    @Test
    public void testSaveWithNotes() {
        SportLog log = createSportLog("sl-notes", "Running", 10.0, "km", "u1");
        log.setNotes("Personal best today!");
        repository.save(log);

        Optional<SportLog> found = repository.findById("sl-notes");
        assertTrue(found.isPresent());
        assertEquals("Personal best today!", found.get().getNotes());
    }

    @Test
    public void testSavePreservesTimeFields() {
        SportLog log = createSportLog("sl-time", "Running", 5.0, "km", "u1");
        log.setStartTime(LocalTime.of(8, 0));
        log.setEndTime(LocalTime.of(9, 30));
        repository.save(log);

        Optional<SportLog> found = repository.findById("sl-time");
        assertTrue(found.isPresent());
        assertEquals(LocalTime.of(8, 0), found.get().getStartTime());
        assertEquals(LocalTime.of(9, 30), found.get().getEndTime());
    }

    @Test
    public void testSavePreservesDate() {
        LocalDate specificDate = LocalDate.of(2025, 6, 15);
        SportLog log = createSportLog("sl-date", "Running", 5.0, "km", "u1");
        log.setDate(specificDate);
        repository.save(log);

        Optional<SportLog> found = repository.findById("sl-date");
        assertTrue(found.isPresent());
        assertEquals(specificDate, found.get().getDate());
    }

    @Test
    public void testUpdateExistingLog() {
        repository.save(createSportLog("sl-update", "Running", 5.0, "km", "u1"));

        SportLog updated = createSportLog("sl-update", "Running", 10.0, "km", "u1");
        updated.setNotes("Doubled the distance");
        repository.save(updated);

        Optional<SportLog> found = repository.findById("sl-update");
        assertTrue(found.isPresent());
        assertEquals(10.0, found.get().getMeasurement(), 0.001);
        assertEquals("Doubled the distance", found.get().getNotes());
    }

    @Test
    public void testSavePreservesUser() {
        SportLog log = createSportLog("sl-user", "Running", 5.0, "km", "u1");
        repository.save(log);

        Optional<SportLog> found = repository.findById("sl-user");
        assertTrue(found.isPresent());
        assertNotNull(found.get().getUser());
        assertEquals("u1", found.get().getUser().getId());
    }

    private SportLog createSportLog(String id, String name, double measurement, String unit, String userId) {
        SportLog log = new SportLog();
        log.setId(id);
        log.setName(name);
        log.setMeasurement(measurement);
        log.setMeasurementUnit(unit);
        log.setStartTime(LocalTime.of(8, 0));
        log.setEndTime(LocalTime.of(9, 0));
        log.setDate(LocalDate.now());
        log.setCreatedAt(LocalDateTime.now());
        User user = new User();
        user.setId(userId);
        user.setUsername("user_" + userId);
        user.setPassword("placeholder");
        log.setUser(user);
        return log;
    }
}
