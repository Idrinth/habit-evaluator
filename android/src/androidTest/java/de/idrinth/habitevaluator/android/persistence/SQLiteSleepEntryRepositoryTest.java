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

import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SQLiteSleepEntryRepositoryTest {

    private SQLiteSleepEntryRepository repository;
    private SQLiteHelper dbHelper;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase("habit_evaluator.db");
        dbHelper = SQLiteHelper.getInstance(context);
        repository = new SQLiteSleepEntryRepository(dbHelper);
    }

    @After
    public void tearDown() {
        dbHelper.close();
        context.deleteDatabase("habit_evaluator.db");
    }

    @Test
    public void testSaveAndFindById() {
        SleepEntry entry = createSleepEntry("se-1", LocalTime.of(23, 0), LocalTime.of(7, 0), LocalDate.now(), "u1");
        repository.save(entry);

        Optional<SleepEntry> found = repository.findById("se-1");
        assertTrue(found.isPresent());
        assertEquals(LocalTime.of(23, 0), found.get().getFromTime());
        assertEquals(LocalTime.of(7, 0), found.get().getUntilTime());
    }

    @Test
    public void testFindByIdNotFound() {
        Optional<SleepEntry> found = repository.findById("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindAll() {
        repository.save(createSleepEntry("se-1", LocalTime.of(22, 0), LocalTime.of(6, 0), LocalDate.now(), "u1"));
        repository.save(createSleepEntry("se-2", LocalTime.of(23, 0), LocalTime.of(7, 0), LocalDate.now().minusDays(1), "u1"));

        List<SleepEntry> all = repository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    public void testDeleteById() {
        repository.save(createSleepEntry("to-delete", LocalTime.of(22, 0), LocalTime.of(6, 0), LocalDate.now(), "u1"));
        assertTrue(repository.existsById("to-delete"));

        repository.deleteById("to-delete");
        assertFalse(repository.existsById("to-delete"));
    }

    @Test
    public void testExistsById() {
        assertFalse(repository.existsById("se-check"));
        repository.save(createSleepEntry("se-check", LocalTime.of(22, 0), LocalTime.of(6, 0), LocalDate.now(), "u1"));
        assertTrue(repository.existsById("se-check"));
    }

    @Test
    public void testFindByUserId() {
        repository.save(createSleepEntry("se-1", LocalTime.of(22, 0), LocalTime.of(6, 0), LocalDate.now(), "u1"));
        repository.save(createSleepEntry("se-2", LocalTime.of(23, 0), LocalTime.of(7, 0), LocalDate.now(), "u1"));
        repository.save(createSleepEntry("se-3", LocalTime.of(21, 0), LocalTime.of(5, 0), LocalDate.now(), "u2"));

        List<SleepEntry> user1Entries = repository.findByUserId("u1");
        assertEquals(2, user1Entries.size());

        List<SleepEntry> user2Entries = repository.findByUserId("u2");
        assertEquals(1, user2Entries.size());
    }

    @Test
    public void testSaveWithNotes() {
        SleepEntry entry = createSleepEntry("se-notes", LocalTime.of(22, 30), LocalTime.of(6, 30), LocalDate.now(), "u1");
        entry.setNotes("Slept well, no interruptions");
        repository.save(entry);

        Optional<SleepEntry> found = repository.findById("se-notes");
        assertTrue(found.isPresent());
        assertEquals("Slept well, no interruptions", found.get().getNotes());
    }

    @Test
    public void testSaveMidnightCrossing() {
        SleepEntry entry = createSleepEntry("se-midnight", LocalTime.of(23, 30), LocalTime.of(7, 0), LocalDate.now(), "u1");
        repository.save(entry);

        Optional<SleepEntry> found = repository.findById("se-midnight");
        assertTrue(found.isPresent());
        assertEquals(LocalTime.of(23, 30), found.get().getFromTime());
        assertEquals(LocalTime.of(7, 0), found.get().getUntilTime());
    }

    @Test
    public void testUpdateExistingEntry() {
        repository.save(createSleepEntry("se-update", LocalTime.of(22, 0), LocalTime.of(6, 0), LocalDate.now(), "u1"));

        SleepEntry updated = createSleepEntry("se-update", LocalTime.of(23, 0), LocalTime.of(7, 30), LocalDate.now(), "u1");
        updated.setNotes("Updated");
        repository.save(updated);

        Optional<SleepEntry> found = repository.findById("se-update");
        assertTrue(found.isPresent());
        assertEquals(LocalTime.of(23, 0), found.get().getFromTime());
        assertEquals(LocalTime.of(7, 30), found.get().getUntilTime());
        assertEquals("Updated", found.get().getNotes());
    }

    @Test
    public void testSavePreservesDate() {
        LocalDate specificDate = LocalDate.of(2025, 1, 15);
        SleepEntry entry = createSleepEntry("se-date", LocalTime.of(22, 0), LocalTime.of(6, 0), specificDate, "u1");
        repository.save(entry);

        Optional<SleepEntry> found = repository.findById("se-date");
        assertTrue(found.isPresent());
        assertEquals(specificDate, found.get().getDate());
    }

    @Test
    public void testSavePreservesUser() {
        SleepEntry entry = createSleepEntry("se-user", LocalTime.of(22, 0), LocalTime.of(6, 0), LocalDate.now(), "u1");
        repository.save(entry);

        Optional<SleepEntry> found = repository.findById("se-user");
        assertTrue(found.isPresent());
        assertNotNull(found.get().getUser());
        assertEquals("u1", found.get().getUser().getId());
    }

    private SleepEntry createSleepEntry(String id, LocalTime from, LocalTime until, LocalDate date, String userId) {
        SleepEntry entry = new SleepEntry();
        entry.setId(id);
        entry.setFromTime(from);
        entry.setUntilTime(until);
        entry.setDate(date);
        entry.setCreatedAt(LocalDateTime.now());
        User user = new User();
        user.setId(userId);
        user.setUsername("user_" + userId);
        user.setPassword("placeholder");
        entry.setUser(user);
        return entry;
    }
}
