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

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.DiaryReference;
import de.idrinth.habitevaluator.shared.model.EventSignificance;
import de.idrinth.habitevaluator.shared.model.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SQLiteDiaryEntryRepositoryTest {

    private SQLiteDiaryEntryRepository repository;
    private SQLiteDiaryReferenceRepository referenceRepository;
    private SQLiteHelper dbHelper;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase("habit_evaluator.db");
        dbHelper = SQLiteHelper.getInstance(context);
        referenceRepository = new SQLiteDiaryReferenceRepository(dbHelper);
        repository = new SQLiteDiaryEntryRepository(dbHelper);
        repository.setDiaryReferenceRepository(referenceRepository);
    }

    @After
    public void tearDown() {
        dbHelper.close();
        context.deleteDatabase("habit_evaluator.db");
    }

    @Test
    public void testSaveAndFindById() {
        DiaryEntry entry = createEntry("de-1", EventSignificance.NORMAL, "u1");
        repository.save(entry);

        Optional<DiaryEntry> found = repository.findById("de-1");
        assertTrue(found.isPresent());
        assertEquals(EventSignificance.NORMAL, found.get().getSignificance());
    }

    @Test
    public void testFindByIdNotFound() {
        Optional<DiaryEntry> found = repository.findById("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindAll() {
        repository.save(createEntry("de-1", EventSignificance.MINOR, "u1"));
        repository.save(createEntry("de-2", EventSignificance.NORMAL, "u1"));
        repository.save(createEntry("de-3", EventSignificance.MAJOR, "u1"));

        List<DiaryEntry> all = repository.findAll();
        assertEquals(3, all.size());
    }

    @Test
    public void testDeleteById() {
        repository.save(createEntry("to-delete", EventSignificance.NORMAL, "u1"));
        assertTrue(repository.findById("to-delete").isPresent());

        repository.deleteById("to-delete");
        assertFalse(repository.findById("to-delete").isPresent());
    }

    @Test
    public void testFindByUserId() {
        repository.save(createEntry("de-1", EventSignificance.NORMAL, "u1"));
        repository.save(createEntry("de-2", EventSignificance.MINOR, "u1"));
        repository.save(createEntry("de-3", EventSignificance.MAJOR, "u2"));

        List<DiaryEntry> user1Entries = repository.findByUserId("u1");
        assertEquals(2, user1Entries.size());

        List<DiaryEntry> user2Entries = repository.findByUserId("u2");
        assertEquals(1, user2Entries.size());
    }

    @Test
    public void testSaveWithStartAndEndTime() {
        DiaryEntry entry = createEntry("de-time", EventSignificance.NORMAL, "u1");
        entry.setStartTime(LocalTime.of(9, 0));
        entry.setEndTime(LocalTime.of(10, 30));
        repository.save(entry);

        Optional<DiaryEntry> found = repository.findById("de-time");
        assertTrue(found.isPresent());
        assertEquals(LocalTime.of(9, 0), found.get().getStartTime());
        assertEquals(LocalTime.of(10, 30), found.get().getEndTime());
    }

    @Test
    public void testSaveWithDiaryReference() {
        DiaryReference ref = new DiaryReference();
        ref.setId("ref-1");
        ref.setDescription("Morning exercise");
        ref.setDescriptionLower("morning exercise");
        User user = createUser("u1");
        ref.setUser(user);
        referenceRepository.save(ref);

        DiaryEntry entry = createEntry("de-ref", EventSignificance.NORMAL, "u1");
        entry.setDiaryReference(ref);
        repository.save(entry);

        Optional<DiaryEntry> found = repository.findById("de-ref");
        assertTrue(found.isPresent());
        assertNotNull(found.get().getDiaryReference());
        assertEquals("Morning exercise", found.get().getDiaryReference().getDescription());
    }

    @Test
    public void testSaveWithLegacyDescription() {
        DiaryEntry entry = createEntry("de-legacy", EventSignificance.MINOR, "u1");
        entry.setLegacyDescription("Legacy entry text");
        repository.save(entry);

        Optional<DiaryEntry> found = repository.findById("de-legacy");
        assertTrue(found.isPresent());
        assertEquals("Legacy entry text", found.get().getLegacyDescription());
    }

    @Test
    public void testFindEntriesNeedingMigration() {
        DiaryEntry legacy = createEntry("de-legacy", EventSignificance.NORMAL, "u1");
        legacy.setLegacyDescription("Old description");
        repository.save(legacy);

        DiaryReference ref = new DiaryReference();
        ref.setId("ref-1");
        ref.setDescription("New style");
        ref.setDescriptionLower("new style");
        User user = createUser("u1");
        ref.setUser(user);
        referenceRepository.save(ref);

        DiaryEntry modern = createEntry("de-modern", EventSignificance.NORMAL, "u1");
        modern.setDiaryReference(ref);
        repository.save(modern);

        List<DiaryEntry> needsMigration = repository.findEntriesNeedingMigration("u1");
        assertEquals(1, needsMigration.size());
        assertEquals("de-legacy", needsMigration.get(0).getId());
    }

    @Test
    public void testSaveAllSignificanceLevels() {
        repository.save(createEntry("de-minor", EventSignificance.MINOR, "u1"));
        repository.save(createEntry("de-normal", EventSignificance.NORMAL, "u1"));
        repository.save(createEntry("de-major", EventSignificance.MAJOR, "u1"));

        assertEquals(EventSignificance.MINOR, repository.findById("de-minor").get().getSignificance());
        assertEquals(EventSignificance.NORMAL, repository.findById("de-normal").get().getSignificance());
        assertEquals(EventSignificance.MAJOR, repository.findById("de-major").get().getSignificance());
    }

    @Test
    public void testUpdateExistingEntry() {
        repository.save(createEntry("de-update", EventSignificance.MINOR, "u1"));

        DiaryEntry updated = createEntry("de-update", EventSignificance.MAJOR, "u1");
        updated.setLegacyDescription("Updated text");
        repository.save(updated);

        Optional<DiaryEntry> found = repository.findById("de-update");
        assertTrue(found.isPresent());
        assertEquals(EventSignificance.MAJOR, found.get().getSignificance());
        assertEquals("Updated text", found.get().getLegacyDescription());
    }

    private DiaryEntry createEntry(String id, EventSignificance significance, String userId) {
        DiaryEntry entry = new DiaryEntry();
        entry.setId(id);
        entry.setSignificance(significance);
        entry.setEventDate(LocalDate.now());
        entry.setCreatedAt(LocalDateTime.now());
        entry.setUser(createUser(userId));
        return entry;
    }

    private User createUser(String id) {
        User user = new User();
        user.setId(id);
        user.setUsername("user_" + id);
        user.setPassword("placeholder");
        return user;
    }
}
