package de.idrinth.habitevaluator.shared.persistence;

import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.User;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemEmotionEntryRepositoryTest {

    private File tempDir;
    private FileSystemEmotionPairRepository pairRepository;
    private FileSystemEmotionEntryRepository repository;

    @BeforeEach
    void setUp() {
        tempDir = new File(System.getProperty("java.io.tmpdir"), "emotion-entry-test-" + System.nanoTime());
        tempDir.mkdirs();
        pairRepository = new FileSystemEmotionPairRepository(tempDir);
        repository = new FileSystemEmotionEntryRepository(tempDir, pairRepository);
    }

    @AfterEach
    void tearDown() {
        File[] files = tempDir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
        tempDir.delete();
    }

    @Test
    void testSaveAndFindById() {
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        pairRepository.save(pair);

        EmotionEntry entry = new EmotionEntry(pair, 5, LocalDateTime.of(2024, 6, 15, 10, 0), "Feeling good");
        repository.save(entry);

        Optional<EmotionEntry> found = repository.findById(entry.getId());
        assertTrue(found.isPresent());
        assertEquals(5, found.get().getStrength());
        assertEquals("Feeling good", found.get().getNotes());
    }

    @Test
    void testFindByIdNotFound() {
        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    void testFindAll() {
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        pairRepository.save(pair);

        repository.save(new EmotionEntry(pair, 3, LocalDateTime.now(), "Note 1"));
        repository.save(new EmotionEntry(pair, -2, LocalDateTime.now(), "Note 2"));

        assertEquals(2, repository.findAll().size());
    }

    @Test
    void testDeleteById() {
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        pairRepository.save(pair);

        EmotionEntry entry = new EmotionEntry(pair, 5, LocalDateTime.now(), "Test");
        repository.save(entry);
        assertEquals(1, repository.findAll().size());

        repository.deleteById(entry.getId());
        assertEquals(0, repository.findAll().size());
    }

    @Test
    void testFindByUserId() {
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        pairRepository.save(pair);

        User user = new User("testuser", "password");
        EmotionEntry e1 = new EmotionEntry(pair, 5, LocalDateTime.now(), "Note");
        e1.setUser(user);
        EmotionEntry e2 = new EmotionEntry(pair, -3, LocalDateTime.now(), "Note");

        repository.save(e1);
        repository.save(e2);

        List<EmotionEntry> userEntries = repository.findByUserId(user.getId());
        assertEquals(1, userEntries.size());
    }

    @Test
    void testPersistenceAcrossInstances() {
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        pairRepository.save(pair);

        EmotionEntry entry = new EmotionEntry(pair, 7, LocalDateTime.of(2024, 1, 15, 14, 30), "Persistent");
        repository.save(entry);

        FileSystemEmotionEntryRepository newRepo = new FileSystemEmotionEntryRepository(tempDir, pairRepository);
        Optional<EmotionEntry> found = newRepo.findById(entry.getId());
        assertTrue(found.isPresent());
        assertEquals(7, found.get().getStrength());
        assertEquals("Persistent", found.get().getNotes());
        assertNotNull(found.get().getEmotionPair());
        assertEquals(pair.getId(), found.get().getEmotionPair().getId());
    }

    @Test
    void testEntryWithDeletedPairIsSkipped() {
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        pairRepository.save(pair);

        EmotionEntry entry = new EmotionEntry(pair, 5, LocalDateTime.now(), "Test");
        repository.save(entry);

        pairRepository.deleteById(pair.getId());

        FileSystemEmotionEntryRepository newRepo = new FileSystemEmotionEntryRepository(tempDir, pairRepository);
        assertEquals(0, newRepo.findAll().size());
    }

    @Test
    void testEmptyRepository() {
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void testStrengthClamping() {
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        pairRepository.save(pair);

        EmotionEntry entry = new EmotionEntry(pair, 10, LocalDateTime.now(), "Max");
        repository.save(entry);

        Optional<EmotionEntry> found = repository.findById(entry.getId());
        assertTrue(found.isPresent());
        assertEquals(10, found.get().getStrength());
    }
}
