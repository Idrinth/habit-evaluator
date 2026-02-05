package de.idrinth.habitevaluator.android.persistence;

import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class FileSystemEmotionPairRepositoryTest {

    private File tempDir;
    private FileSystemEmotionPairRepository repository;

    @Before
    public void setUp() {
        tempDir = new File(System.getProperty("java.io.tmpdir"), "emotion-pair-test-" + System.nanoTime());
        tempDir.mkdirs();
        repository = new FileSystemEmotionPairRepository(tempDir);
    }

    @After
    public void tearDown() {
        File[] files = tempDir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
        tempDir.delete();
    }

    @Test
    public void testSaveAndFindById() {
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        repository.save(pair);

        Optional<EmotionPair> found = repository.findById(pair.getId());
        assertTrue(found.isPresent());
        assertEquals("Sad", found.get().getNegativeLabel());
        assertEquals("Happy", found.get().getPositiveLabel());
    }

    @Test
    public void testFindByIdNotFound() {
        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    public void testFindAll() {
        repository.save(new EmotionPair("Sad", "Happy"));
        repository.save(new EmotionPair("Anxious", "Calm"));
        repository.save(new EmotionPair("Tired", "Energetic"));

        assertEquals(3, repository.findAll().size());
    }

    @Test
    public void testDeleteById() {
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        repository.save(pair);
        assertEquals(1, repository.findAll().size());

        repository.deleteById(pair.getId());
        assertEquals(0, repository.findAll().size());
    }

    @Test
    public void testFindByUserId() {
        User user = new User("testuser", "password");
        EmotionPair p1 = new EmotionPair("Sad", "Happy");
        p1.setUser(user);
        EmotionPair p2 = new EmotionPair("Anxious", "Calm");

        repository.save(p1);
        repository.save(p2);

        List<EmotionPair> userPairs = repository.findByUserId(user.getId());
        assertEquals(1, userPairs.size());
        assertEquals("Sad", userPairs.get(0).getNegativeLabel());
    }

    @Test
    public void testPersistenceAcrossInstances() {
        EmotionPair pair = new EmotionPair("Listless", "Active");
        repository.save(pair);

        FileSystemEmotionPairRepository newRepo = new FileSystemEmotionPairRepository(tempDir);
        Optional<EmotionPair> found = newRepo.findById(pair.getId());
        assertTrue(found.isPresent());
        assertEquals("Listless", found.get().getNegativeLabel());
        assertEquals("Active", found.get().getPositiveLabel());
    }

    @Test
    public void testSaveWithUser() {
        User user = new User("testuser", "password");
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        pair.setUser(user);
        repository.save(pair);

        FileSystemEmotionPairRepository newRepo = new FileSystemEmotionPairRepository(tempDir);
        Optional<EmotionPair> found = newRepo.findById(pair.getId());
        assertTrue(found.isPresent());
        assertNotNull(found.get().getUser());
        assertEquals(user.getId(), found.get().getUser().getId());
    }

    @Test
    public void testEmptyRepository() {
        assertTrue(repository.findAll().isEmpty());
    }
}
