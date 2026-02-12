package de.idrinth.habitevaluator.android.persistence;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Optional;

import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SQLiteEmotionPairRepositoryTest {

    private SQLiteEmotionPairRepository repository;
    private SQLiteHelper dbHelper;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase("habit_evaluator.db");
        dbHelper = SQLiteHelper.getInstance(context);
        repository = new SQLiteEmotionPairRepository(dbHelper);
    }

    @After
    public void tearDown() {
        dbHelper.close();
        context.deleteDatabase("habit_evaluator.db");
    }

    @Test
    public void testSaveAndFindById() {
        EmotionPair pair = createPair("ep-1", "Sad", "Happy", "u1");
        repository.save(pair);

        Optional<EmotionPair> found = repository.findById("ep-1");
        assertTrue(found.isPresent());
        assertEquals("Sad", found.get().getNegativeLabel());
        assertEquals("Happy", found.get().getPositiveLabel());
    }

    @Test
    public void testFindByIdNotFound() {
        Optional<EmotionPair> found = repository.findById("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindAll() {
        repository.save(createPair("ep-1", "Sad", "Happy", "u1"));
        repository.save(createPair("ep-2", "Anxious", "Calm", "u1"));
        repository.save(createPair("ep-3", "Tired", "Energetic", "u1"));

        List<EmotionPair> all = repository.findAll();
        assertEquals(3, all.size());
    }

    @Test
    public void testFindAllEmpty() {
        List<EmotionPair> all = repository.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    public void testDeleteById() {
        repository.save(createPair("to-delete", "Negative", "Positive", "u1"));
        assertTrue(repository.findById("to-delete").isPresent());

        repository.deleteById("to-delete");
        assertFalse(repository.findById("to-delete").isPresent());
    }

    @Test
    public void testFindByUserId() {
        repository.save(createPair("ep-1", "Sad", "Happy", "u1"));
        repository.save(createPair("ep-2", "Anxious", "Calm", "u1"));
        repository.save(createPair("ep-3", "Tired", "Energetic", "u2"));

        List<EmotionPair> user1Pairs = repository.findByUserId("u1");
        assertEquals(2, user1Pairs.size());

        List<EmotionPair> user2Pairs = repository.findByUserId("u2");
        assertEquals(1, user2Pairs.size());
    }

    @Test
    public void testUpdateExistingPair() {
        repository.save(createPair("ep-update", "Sad", "Happy", "u1"));

        EmotionPair updated = createPair("ep-update", "Depressed", "Joyful", "u1");
        repository.save(updated);

        Optional<EmotionPair> found = repository.findById("ep-update");
        assertTrue(found.isPresent());
        assertEquals("Depressed", found.get().getNegativeLabel());
        assertEquals("Joyful", found.get().getPositiveLabel());
    }

    @Test
    public void testSavePreservesUser() {
        EmotionPair pair = createPair("ep-user", "Sad", "Happy", "u1");
        repository.save(pair);

        Optional<EmotionPair> found = repository.findById("ep-user");
        assertTrue(found.isPresent());
        assertNotNull(found.get().getUser());
        assertEquals("u1", found.get().getUser().getId());
        assertEquals("user_u1", found.get().getUser().getUsername());
    }

    @Test
    public void testFindByUserIdOrderedByNegativeLabel() {
        repository.save(createPair("ep-1", "Tired", "Energetic", "u1"));
        repository.save(createPair("ep-2", "Anxious", "Calm", "u1"));
        repository.save(createPair("ep-3", "Sad", "Happy", "u1"));

        List<EmotionPair> pairs = repository.findByUserId("u1");
        assertEquals(3, pairs.size());
        assertEquals("Anxious", pairs.get(0).getNegativeLabel());
        assertEquals("Sad", pairs.get(1).getNegativeLabel());
        assertEquals("Tired", pairs.get(2).getNegativeLabel());
    }

    private EmotionPair createPair(String id, String negativeLabel, String positiveLabel, String userId) {
        EmotionPair pair = new EmotionPair();
        pair.setId(id);
        pair.setNegativeLabel(negativeLabel);
        pair.setPositiveLabel(positiveLabel);
        User user = new User();
        user.setId(userId);
        user.setUsername("user_" + userId);
        user.setPassword("placeholder");
        pair.setUser(user);
        return pair;
    }
}
