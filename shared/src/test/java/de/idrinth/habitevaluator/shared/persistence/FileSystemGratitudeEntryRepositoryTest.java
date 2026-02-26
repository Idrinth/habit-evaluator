package de.idrinth.habitevaluator.shared.persistence;

import de.idrinth.habitevaluator.shared.model.GratitudeEntry;
import de.idrinth.habitevaluator.shared.model.User;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemGratitudeEntryRepositoryTest {

    private File tempDir;
    private FileSystemGratitudeEntryRepository repository;

    @BeforeEach
    void setUp() {
        tempDir = new File(System.getProperty("java.io.tmpdir"), "gratitude-test-" + System.nanoTime());
        tempDir.mkdirs();
        repository = new FileSystemGratitudeEntryRepository(tempDir);
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
        GratitudeEntry entry = new GratitudeEntry("I'm grateful for my family");
        entry.setEventDate(LocalDate.of(2026, 2, 15));
        repository.save(entry);

        Optional<GratitudeEntry> found = repository.findById(entry.getId());
        assertTrue(found.isPresent());
        assertEquals("I'm grateful for my family", found.get().getDescription());
    }

    @Test
    void testFindByIdNotFound() {
        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    void testFindAll() {
        repository.save(new GratitudeEntry("Entry 1"));
        repository.save(new GratitudeEntry("Entry 2"));
        repository.save(new GratitudeEntry("Entry 3"));

        assertEquals(3, repository.findAll().size());
    }

    @Test
    void testDeleteById() {
        GratitudeEntry entry = new GratitudeEntry("To delete");
        repository.save(entry);
        assertEquals(1, repository.findAll().size());

        repository.deleteById(entry.getId());
        assertEquals(0, repository.findAll().size());
    }

    @Test
    void testFindByUserId() {
        User user = new User("testuser", "password");
        GratitudeEntry e1 = new GratitudeEntry("User entry");
        e1.setUser(user);
        repository.save(e1);

        GratitudeEntry e2 = new GratitudeEntry("Other entry");
        User other = new User("other", "pass");
        e2.setUser(other);
        repository.save(e2);

        List<GratitudeEntry> found = repository.findByUserId(user.getId());
        assertEquals(1, found.size());
        assertEquals("User entry", found.get(0).getDescription());
    }

    @Test
    void testPersistenceAcrossInstances() {
        GratitudeEntry entry = new GratitudeEntry("Persisted gratitude");
        entry.setEventDate(LocalDate.of(2026, 1, 10));
        repository.save(entry);

        // Create a new repository instance from the same directory
        FileSystemGratitudeEntryRepository repo2 = new FileSystemGratitudeEntryRepository(tempDir);
        Optional<GratitudeEntry> found = repo2.findById(entry.getId());
        assertTrue(found.isPresent());
        assertEquals("Persisted gratitude", found.get().getDescription());
        assertEquals(LocalDate.of(2026, 1, 10), found.get().getEventDate());
    }

    @Test
    void testSaveUpdatesExisting() {
        GratitudeEntry entry = new GratitudeEntry("Original");
        repository.save(entry);

        entry.setDescription("Updated");
        repository.save(entry);

        assertEquals(1, repository.findAll().size());
        assertEquals("Updated", repository.findById(entry.getId()).get().getDescription());
    }

    @Test
    void testSaveAndFindWithReason() {
        GratitudeEntry entry = new GratitudeEntry("the sunny weather");
        entry.setReason("it allowed for a nice walk");
        repository.save(entry);

        Optional<GratitudeEntry> found = repository.findById(entry.getId());
        assertTrue(found.isPresent());
        assertEquals("the sunny weather", found.get().getDescription());
        assertEquals("it allowed for a nice walk", found.get().getReason());
    }

    @Test
    void testSaveAndFindWithNullReason() {
        GratitudeEntry entry = new GratitudeEntry("good weather");
        repository.save(entry);

        Optional<GratitudeEntry> found = repository.findById(entry.getId());
        assertTrue(found.isPresent());
        assertNull(found.get().getReason());
    }

    @Test
    void testReasonPersistsAcrossInstances() {
        GratitudeEntry entry = new GratitudeEntry("sunny weather");
        entry.setReason("it made me happy");
        repository.save(entry);

        FileSystemGratitudeEntryRepository repo2 = new FileSystemGratitudeEntryRepository(tempDir);
        Optional<GratitudeEntry> found = repo2.findById(entry.getId());
        assertTrue(found.isPresent());
        assertEquals("it made me happy", found.get().getReason());
    }
}
