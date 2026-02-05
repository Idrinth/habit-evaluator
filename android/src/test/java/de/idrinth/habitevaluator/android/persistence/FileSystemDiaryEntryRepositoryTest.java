package de.idrinth.habitevaluator.android.persistence;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EventSignificance;
import de.idrinth.habitevaluator.shared.model.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class FileSystemDiaryEntryRepositoryTest {

    private File tempDir;
    private FileSystemDiaryEntryRepository repository;

    @Before
    public void setUp() {
        tempDir = new File(System.getProperty("java.io.tmpdir"), "diary-test-" + System.nanoTime());
        tempDir.mkdirs();
        repository = new FileSystemDiaryEntryRepository(tempDir);
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
        DiaryEntry entry = new DiaryEntry("Had a great day", EventSignificance.MAJOR);
        entry.setEventDate(LocalDate.of(2024, 6, 15));
        repository.save(entry);

        Optional<DiaryEntry> found = repository.findById(entry.getId());
        assertTrue(found.isPresent());
        assertEquals("Had a great day", found.get().getDescription());
        assertEquals(EventSignificance.MAJOR, found.get().getSignificance());
    }

    @Test
    public void testFindByIdNotFound() {
        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    public void testFindAll() {
        repository.save(new DiaryEntry("Entry 1", EventSignificance.MINOR));
        repository.save(new DiaryEntry("Entry 2", EventSignificance.NORMAL));
        repository.save(new DiaryEntry("Entry 3", EventSignificance.MAJOR));

        assertEquals(3, repository.findAll().size());
    }

    @Test
    public void testDeleteById() {
        DiaryEntry entry = new DiaryEntry("To delete", EventSignificance.MINOR);
        repository.save(entry);
        assertEquals(1, repository.findAll().size());

        repository.deleteById(entry.getId());
        assertEquals(0, repository.findAll().size());
    }

    @Test
    public void testFindByUserId() {
        User user = new User("testuser", "password");
        DiaryEntry e1 = new DiaryEntry("User entry", EventSignificance.NORMAL);
        e1.setUser(user);
        DiaryEntry e2 = new DiaryEntry("No user entry", EventSignificance.MINOR);

        repository.save(e1);
        repository.save(e2);

        List<DiaryEntry> userEntries = repository.findByUserId(user.getId());
        assertEquals(1, userEntries.size());
        assertEquals("User entry", userEntries.get(0).getDescription());
    }

    @Test
    public void testFindDistinctDescriptionsByUserId() {
        User user = new User("testuser", "password");
        DiaryEntry e1 = new DiaryEntry("Workout", EventSignificance.NORMAL);
        e1.setUser(user);
        DiaryEntry e2 = new DiaryEntry("Workout", EventSignificance.MINOR);
        e2.setUser(user);
        DiaryEntry e3 = new DiaryEntry("Meditation", EventSignificance.MAJOR);
        e3.setUser(user);

        repository.save(e1);
        repository.save(e2);
        repository.save(e3);

        List<String> descriptions = repository.findDistinctDescriptionsByUserId(user.getId());
        assertEquals(2, descriptions.size());
        assertTrue(descriptions.contains("Workout"));
        assertTrue(descriptions.contains("Meditation"));
    }

    @Test
    public void testPersistenceAcrossInstances() {
        DiaryEntry entry = new DiaryEntry("Persistent entry", EventSignificance.MAJOR);
        entry.setEventDate(LocalDate.of(2024, 3, 20));
        repository.save(entry);

        FileSystemDiaryEntryRepository newRepo = new FileSystemDiaryEntryRepository(tempDir);
        Optional<DiaryEntry> found = newRepo.findById(entry.getId());
        assertTrue(found.isPresent());
        assertEquals("Persistent entry", found.get().getDescription());
        assertEquals(EventSignificance.MAJOR, found.get().getSignificance());
        assertEquals(LocalDate.of(2024, 3, 20), found.get().getEventDate());
    }

    @Test
    public void testAllSignificanceLevels() {
        for (EventSignificance sig : EventSignificance.values()) {
            DiaryEntry entry = new DiaryEntry("Entry-" + sig.name(), sig);
            repository.save(entry);
        }

        FileSystemDiaryEntryRepository newRepo = new FileSystemDiaryEntryRepository(tempDir);
        assertEquals(EventSignificance.values().length, newRepo.findAll().size());
    }

    @Test
    public void testEmptyRepository() {
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    public void testSaveWithUser() {
        User user = new User("testuser", "password");
        DiaryEntry entry = new DiaryEntry("User entry", EventSignificance.NORMAL);
        entry.setUser(user);
        repository.save(entry);

        FileSystemDiaryEntryRepository newRepo = new FileSystemDiaryEntryRepository(tempDir);
        Optional<DiaryEntry> found = newRepo.findById(entry.getId());
        assertTrue(found.isPresent());
        assertNotNull(found.get().getUser());
        assertEquals(user.getId(), found.get().getUser().getId());
    }
}
