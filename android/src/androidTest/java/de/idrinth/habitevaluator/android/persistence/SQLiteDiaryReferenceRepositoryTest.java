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

import de.idrinth.habitevaluator.shared.model.DiaryReference;
import de.idrinth.habitevaluator.shared.model.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SQLiteDiaryReferenceRepositoryTest {

    private SQLiteDiaryReferenceRepository repository;
    private SQLiteHelper dbHelper;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase("habit_evaluator.db");
        dbHelper = SQLiteHelper.getInstance(context);
        repository = new SQLiteDiaryReferenceRepository(dbHelper);
    }

    @After
    public void tearDown() {
        dbHelper.close();
        context.deleteDatabase("habit_evaluator.db");
    }

    @Test
    public void testSaveAndFindById() {
        DiaryReference ref = createReference("ref-1", "Morning walk", "u1");
        repository.save(ref);

        Optional<DiaryReference> found = repository.findById("ref-1");
        assertTrue(found.isPresent());
        assertEquals("Morning walk", found.get().getDescription());
        assertEquals("morning walk", found.get().getDescriptionLower());
    }

    @Test
    public void testFindByIdNotFound() {
        Optional<DiaryReference> found = repository.findById("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindAll() {
        repository.save(createReference("r1", "Walk", "u1"));
        repository.save(createReference("r2", "Run", "u1"));
        repository.save(createReference("r3", "Swim", "u1"));

        List<DiaryReference> all = repository.findAll();
        assertEquals(3, all.size());
    }

    @Test
    public void testDeleteById() {
        repository.save(createReference("to-delete", "Temp", "u1"));
        assertTrue(repository.findById("to-delete").isPresent());

        repository.deleteById("to-delete");
        assertFalse(repository.findById("to-delete").isPresent());
    }

    @Test
    public void testFindByUserId() {
        repository.save(createReference("r1", "Walk", "u1"));
        repository.save(createReference("r2", "Run", "u1"));
        repository.save(createReference("r3", "Swim", "u2"));

        List<DiaryReference> user1Refs = repository.findByUserId("u1");
        assertEquals(2, user1Refs.size());

        List<DiaryReference> user2Refs = repository.findByUserId("u2");
        assertEquals(1, user2Refs.size());
    }

    @Test
    public void testFindByUserIdAndDescriptionIgnoreCase() {
        repository.save(createReference("r1", "Morning Walk", "u1"));

        Optional<DiaryReference> found = repository.findByUserIdAndDescriptionIgnoreCase("u1", "morning walk");
        assertTrue(found.isPresent());
        assertEquals("Morning Walk", found.get().getDescription());

        Optional<DiaryReference> upper = repository.findByUserIdAndDescriptionIgnoreCase("u1", "MORNING WALK");
        assertTrue(upper.isPresent());
    }

    @Test
    public void testFindByUserIdAndDescriptionIgnoreCaseNotFound() {
        repository.save(createReference("r1", "Morning Walk", "u1"));

        Optional<DiaryReference> found = repository.findByUserIdAndDescriptionIgnoreCase("u1", "evening walk");
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindByUserIdAndDescriptionIgnoreCaseNullDescription() {
        Optional<DiaryReference> found = repository.findByUserIdAndDescriptionIgnoreCase("u1", null);
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindDistinctDescriptionsByUserId() {
        repository.save(createReference("r1", "Walk", "u1"));
        repository.save(createReference("r2", "Run", "u1"));
        repository.save(createReference("r3", "Swim", "u2"));

        List<String> descriptions = repository.findDistinctDescriptionsByUserId("u1");
        assertEquals(2, descriptions.size());
        assertTrue(descriptions.contains("Walk"));
        assertTrue(descriptions.contains("Run"));
    }

    @Test
    public void testUpdateExistingReference() {
        repository.save(createReference("r-update", "Walk", "u1"));

        DiaryReference updated = createReference("r-update", "Morning Jog", "u1");
        repository.save(updated);

        Optional<DiaryReference> found = repository.findById("r-update");
        assertTrue(found.isPresent());
        assertEquals("Morning Jog", found.get().getDescription());
        assertEquals("morning jog", found.get().getDescriptionLower());
    }

    private DiaryReference createReference(String id, String description, String userId) {
        DiaryReference ref = new DiaryReference();
        ref.setId(id);
        ref.setDescription(description);
        ref.setDescriptionLower(description.toLowerCase());
        User user = new User();
        user.setId(userId);
        user.setUsername("user_" + userId);
        user.setPassword("placeholder");
        ref.setUser(user);
        return ref;
    }
}
