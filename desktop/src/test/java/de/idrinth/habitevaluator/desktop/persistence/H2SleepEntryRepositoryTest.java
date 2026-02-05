package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class H2SleepEntryRepositoryTest extends H2RepositoryTestBase {

    private H2SleepEntryRepository sleepRepository;
    private H2UserRepository userRepository;
    private User testUser;

    @BeforeEach
    void setUp() {
        sleepRepository = new H2SleepEntryRepository();
        userRepository = new H2UserRepository();

        for (SleepEntry entry : sleepRepository.findAll()) {
            sleepRepository.deleteById(entry.getId());
        }
        for (User user : userRepository.findAll()) {
            userRepository.deleteById(user.getId());
        }

        testUser = new User("sleepuser", "password123");
        userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        SleepEntry entry = new SleepEntry(LocalTime.of(23, 0), LocalTime.of(7, 0));
        entry.setUser(testUser);
        SleepEntry saved = sleepRepository.save(entry);

        assertNotNull(saved);
        Optional<SleepEntry> found = sleepRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(LocalTime.of(23, 0), found.get().getFromTime());
        assertEquals(LocalTime.of(7, 0), found.get().getUntilTime());
    }

    @Test
    void testSaveUpdatesExistingEntry() {
        SleepEntry entry = new SleepEntry(LocalTime.of(23, 0), LocalTime.of(7, 0));
        entry.setUser(testUser);
        sleepRepository.save(entry);

        entry.setNotes("Slept well");
        sleepRepository.save(entry);

        Optional<SleepEntry> found = sleepRepository.findById(entry.getId());
        assertTrue(found.isPresent());
        assertEquals("Slept well", found.get().getNotes());
    }

    @Test
    void testFindByIdReturnsEmptyForNonExistent() {
        Optional<SleepEntry> found = sleepRepository.findById("nonexistent-id");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        SleepEntry entry1 = new SleepEntry(LocalTime.of(23, 0), LocalTime.of(7, 0));
        entry1.setUser(testUser);
        SleepEntry entry2 = new SleepEntry(LocalTime.of(22, 30), LocalTime.of(6, 30));
        entry2.setUser(testUser);
        entry2.setDate(LocalDate.now().minusDays(1));
        sleepRepository.save(entry1);
        sleepRepository.save(entry2);

        List<SleepEntry> all = sleepRepository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testDeleteById() {
        SleepEntry entry = new SleepEntry(LocalTime.of(23, 0), LocalTime.of(7, 0));
        entry.setUser(testUser);
        sleepRepository.save(entry);

        assertTrue(sleepRepository.existsById(entry.getId()));
        sleepRepository.deleteById(entry.getId());
        assertFalse(sleepRepository.existsById(entry.getId()));
    }

    @Test
    void testDeleteByIdNonExistentDoesNotThrow() {
        assertDoesNotThrow(() -> sleepRepository.deleteById("nonexistent-id"));
    }

    @Test
    void testExistsById() {
        SleepEntry entry = new SleepEntry(LocalTime.of(23, 0), LocalTime.of(7, 0));
        entry.setUser(testUser);
        sleepRepository.save(entry);

        assertTrue(sleepRepository.existsById(entry.getId()));
        assertFalse(sleepRepository.existsById("nonexistent-id"));
    }

    @Test
    void testFindByUserId() {
        SleepEntry entry1 = new SleepEntry(LocalTime.of(23, 0), LocalTime.of(7, 0));
        entry1.setUser(testUser);
        sleepRepository.save(entry1);

        User otherUser = new User("othersleepuser", "password123");
        userRepository.save(otherUser);
        SleepEntry entry2 = new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0));
        entry2.setUser(otherUser);
        sleepRepository.save(entry2);

        List<SleepEntry> userEntries = sleepRepository.findByUserId(testUser.getId());
        assertEquals(1, userEntries.size());
    }

    @Test
    void testFindByUserIdReturnsEmptyForNonExistentUser() {
        List<SleepEntry> entries = sleepRepository.findByUserId("nonexistent-user-id");
        assertTrue(entries.isEmpty());
    }

    @Test
    void testDatePersisted() {
        LocalDate specificDate = LocalDate.of(2024, 6, 15);
        SleepEntry entry = new SleepEntry(LocalTime.of(23, 0), LocalTime.of(7, 0), specificDate);
        entry.setUser(testUser);
        sleepRepository.save(entry);

        Optional<SleepEntry> found = sleepRepository.findById(entry.getId());
        assertTrue(found.isPresent());
        assertEquals(specificDate, found.get().getDate());
    }
}
