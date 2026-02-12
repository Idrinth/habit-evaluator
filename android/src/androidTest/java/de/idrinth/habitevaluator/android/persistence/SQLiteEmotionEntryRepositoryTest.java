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

import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SQLiteEmotionEntryRepositoryTest {

    private SQLiteEmotionEntryRepository repository;
    private SQLiteEmotionPairRepository pairRepository;
    private SQLiteHelper dbHelper;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase("habit_evaluator.db");
        dbHelper = SQLiteHelper.getInstance(context);
        pairRepository = new SQLiteEmotionPairRepository(dbHelper);
        repository = new SQLiteEmotionEntryRepository(dbHelper, pairRepository);
    }

    @After
    public void tearDown() {
        dbHelper.close();
        context.deleteDatabase("habit_evaluator.db");
    }

    @Test
    public void testSaveAndFindById() {
        EmotionPair pair = createAndSavePair("pair-1", "Sad", "Happy", "u1");

        EmotionEntry entry = createEntry("ee-1", pair, 5, "u1");
        repository.save(entry);

        Optional<EmotionEntry> found = repository.findById("ee-1");
        assertTrue(found.isPresent());
        assertEquals(5, found.get().getStrength());
        assertNotNull(found.get().getEmotionPair());
        assertEquals("pair-1", found.get().getEmotionPair().getId());
    }

    @Test
    public void testFindByIdNotFound() {
        Optional<EmotionEntry> found = repository.findById("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindAll() {
        EmotionPair pair = createAndSavePair("pair-1", "Sad", "Happy", "u1");

        repository.save(createEntry("ee-1", pair, 3, "u1"));
        repository.save(createEntry("ee-2", pair, -2, "u1"));
        repository.save(createEntry("ee-3", pair, 7, "u1"));

        List<EmotionEntry> all = repository.findAll();
        assertEquals(3, all.size());
    }

    @Test
    public void testDeleteById() {
        EmotionPair pair = createAndSavePair("pair-1", "Sad", "Happy", "u1");
        repository.save(createEntry("to-delete", pair, 5, "u1"));
        assertTrue(repository.findById("to-delete").isPresent());

        repository.deleteById("to-delete");
        assertFalse(repository.findById("to-delete").isPresent());
    }

    @Test
    public void testFindByUserId() {
        EmotionPair pair = createAndSavePair("pair-1", "Sad", "Happy", "u1");
        EmotionPair pair2 = createAndSavePair("pair-2", "Anxious", "Calm", "u2");

        repository.save(createEntry("ee-1", pair, 5, "u1"));
        repository.save(createEntry("ee-2", pair, -3, "u1"));
        repository.save(createEntry("ee-3", pair2, 2, "u2"));

        List<EmotionEntry> user1Entries = repository.findByUserId("u1");
        assertEquals(2, user1Entries.size());

        List<EmotionEntry> user2Entries = repository.findByUserId("u2");
        assertEquals(1, user2Entries.size());
    }

    @Test
    public void testSaveWithNotes() {
        EmotionPair pair = createAndSavePair("pair-1", "Sad", "Happy", "u1");

        EmotionEntry entry = createEntry("ee-notes", pair, 8, "u1");
        entry.setNotes("Feeling great after exercise");
        repository.save(entry);

        Optional<EmotionEntry> found = repository.findById("ee-notes");
        assertTrue(found.isPresent());
        assertEquals("Feeling great after exercise", found.get().getNotes());
    }

    @Test
    public void testSaveNegativeStrength() {
        EmotionPair pair = createAndSavePair("pair-1", "Sad", "Happy", "u1");

        EmotionEntry entry = createEntry("ee-neg", pair, -7, "u1");
        repository.save(entry);

        Optional<EmotionEntry> found = repository.findById("ee-neg");
        assertTrue(found.isPresent());
        assertEquals(-7, found.get().getStrength());
    }

    @Test
    public void testSaveZeroStrength() {
        EmotionPair pair = createAndSavePair("pair-1", "Sad", "Happy", "u1");

        EmotionEntry entry = createEntry("ee-zero", pair, 0, "u1");
        repository.save(entry);

        Optional<EmotionEntry> found = repository.findById("ee-zero");
        assertTrue(found.isPresent());
        assertEquals(0, found.get().getStrength());
    }

    @Test
    public void testUpdateExistingEntry() {
        EmotionPair pair = createAndSavePair("pair-1", "Sad", "Happy", "u1");
        repository.save(createEntry("ee-update", pair, 3, "u1"));

        EmotionEntry updated = createEntry("ee-update", pair, 8, "u1");
        updated.setNotes("Updated note");
        repository.save(updated);

        Optional<EmotionEntry> found = repository.findById("ee-update");
        assertTrue(found.isPresent());
        assertEquals(8, found.get().getStrength());
        assertEquals("Updated note", found.get().getNotes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveWithoutPairThrowsException() {
        EmotionEntry entry = new EmotionEntry();
        entry.setId("ee-no-pair");
        entry.setStrength(5);
        entry.setRecordedAt(LocalDateTime.now());
        repository.save(entry);
    }

    @Test
    public void testFindByIdReturnsEmptyWhenPairDeleted() {
        EmotionPair pair = createAndSavePair("pair-del", "Sad", "Happy", "u1");
        repository.save(createEntry("ee-orphan", pair, 5, "u1"));

        pairRepository.deleteById("pair-del");

        Optional<EmotionEntry> found = repository.findById("ee-orphan");
        assertFalse(found.isPresent());
    }

    private EmotionPair createAndSavePair(String id, String negativeLabel, String positiveLabel, String userId) {
        EmotionPair pair = new EmotionPair();
        pair.setId(id);
        pair.setNegativeLabel(negativeLabel);
        pair.setPositiveLabel(positiveLabel);
        User user = new User();
        user.setId(userId);
        user.setUsername("user_" + userId);
        user.setPassword("placeholder");
        pair.setUser(user);
        pairRepository.save(pair);
        return pair;
    }

    private EmotionEntry createEntry(String id, EmotionPair pair, int strength, String userId) {
        EmotionEntry entry = new EmotionEntry();
        entry.setId(id);
        entry.setEmotionPair(pair);
        entry.setStrength(strength);
        entry.setRecordedAt(LocalDateTime.now());
        User user = new User();
        user.setId(userId);
        user.setUsername("user_" + userId);
        user.setPassword("placeholder");
        entry.setUser(user);
        return entry;
    }
}
