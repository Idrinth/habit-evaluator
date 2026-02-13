package de.idrinth.habitevaluator.shared.persistence;

import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.User;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemSleepEntryRepositoryTest {

    private File tempDir;
    private FileSystemSleepEntryRepository repository;

    @BeforeEach
    void setUp() {
        tempDir = new File(System.getProperty("java.io.tmpdir"), "sleep-test-" + System.nanoTime());
        tempDir.mkdirs();
        repository = new FileSystemSleepEntryRepository(tempDir);
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
        SleepEntry entry = new SleepEntry(LocalTime.of(23, 0), LocalTime.of(7, 0));
        entry.setDate(LocalDate.of(2024, 6, 15));
        entry.setNotes("Good sleep");
        repository.save(entry);

        Optional<SleepEntry> found = repository.findById(entry.getId());
        assertTrue(found.isPresent());
        assertEquals(LocalTime.of(23, 0), found.get().getFromTime());
        assertEquals(LocalTime.of(7, 0), found.get().getUntilTime());
        assertEquals("Good sleep", found.get().getNotes());
    }

    @Test
    void testFindByIdNotFound() {
        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    void testFindAll() {
        repository.save(new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0)));
        repository.save(new SleepEntry(LocalTime.of(23, 0), LocalTime.of(7, 0)));

        assertEquals(2, repository.findAll().size());
    }

    @Test
    void testDeleteById() {
        SleepEntry entry = new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0));
        repository.save(entry);
        assertTrue(repository.existsById(entry.getId()));

        repository.deleteById(entry.getId());
        assertFalse(repository.existsById(entry.getId()));
    }

    @Test
    void testExistsById() {
        SleepEntry entry = new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0));
        assertFalse(repository.existsById(entry.getId()));

        repository.save(entry);
        assertTrue(repository.existsById(entry.getId()));
    }

    @Test
    void testFindByUserId() {
        User user = new User("testuser", "password");
        SleepEntry e1 = new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0));
        e1.setUser(user);
        SleepEntry e2 = new SleepEntry(LocalTime.of(23, 0), LocalTime.of(7, 0));

        repository.save(e1);
        repository.save(e2);

        List<SleepEntry> userEntries = repository.findByUserId(user.getId());
        assertEquals(1, userEntries.size());
    }

    @Test
    void testPersistenceAcrossInstances() {
        SleepEntry entry = new SleepEntry(LocalTime.of(22, 30), LocalTime.of(6, 45));
        entry.setDate(LocalDate.of(2024, 3, 20));
        entry.setNotes("Persistent sleep");
        repository.save(entry);

        FileSystemSleepEntryRepository newRepo = new FileSystemSleepEntryRepository(tempDir);
        Optional<SleepEntry> found = newRepo.findById(entry.getId());
        assertTrue(found.isPresent());
        assertEquals(LocalTime.of(22, 30), found.get().getFromTime());
        assertEquals(LocalTime.of(6, 45), found.get().getUntilTime());
        assertEquals(LocalDate.of(2024, 3, 20), found.get().getDate());
        assertEquals("Persistent sleep", found.get().getNotes());
    }

    @Test
    void testSaveWithUser() {
        User user = new User("testuser", "password");
        SleepEntry entry = new SleepEntry(LocalTime.of(23, 0), LocalTime.of(7, 0));
        entry.setUser(user);
        repository.save(entry);

        FileSystemSleepEntryRepository newRepo = new FileSystemSleepEntryRepository(tempDir);
        Optional<SleepEntry> found = newRepo.findById(entry.getId());
        assertTrue(found.isPresent());
        assertNotNull(found.get().getUser());
        assertEquals(user.getId(), found.get().getUser().getId());
    }

    @Test
    void testEmptyRepository() {
        assertTrue(repository.findAll().isEmpty());
    }
}
