package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class JpaSleepEntryRepositoryTest {

    @Autowired
    private JpaSleepEntryRepository sleepEntryRepository;

    @Autowired
    private JpaUserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("sleepuser", "password123", "sleepuser@example.com");
        testUser = userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        SleepEntry entry = new SleepEntry(LocalTime.of(23, 0), LocalTime.of(7, 0), LocalDate.of(2025, 1, 15));
        entry.setUser(testUser);
        SleepEntry saved = sleepEntryRepository.save(entry);

        Optional<SleepEntry> found = sleepEntryRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(LocalTime.of(23, 0), found.get().getFromTime());
        assertEquals(LocalTime.of(7, 0), found.get().getUntilTime());
        assertEquals(LocalDate.of(2025, 1, 15), found.get().getDate());
    }

    @Test
    void testFindByUserId() {
        SleepEntry entry1 = new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0), LocalDate.of(2025, 1, 15));
        entry1.setUser(testUser);
        sleepEntryRepository.save(entry1);

        SleepEntry entry2 = new SleepEntry(LocalTime.of(23, 30), LocalTime.of(7, 30), LocalDate.of(2025, 1, 16));
        entry2.setUser(testUser);
        sleepEntryRepository.save(entry2);

        List<SleepEntry> entries = sleepEntryRepository.findByUserId(testUser.getId());

        assertEquals(2, entries.size());
    }

    @Test
    void testFindByUserIdOrderedByDateDesc() {
        SleepEntry older = new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0), LocalDate.of(2025, 1, 10));
        older.setUser(testUser);
        sleepEntryRepository.save(older);

        SleepEntry newer = new SleepEntry(LocalTime.of(23, 0), LocalTime.of(7, 0), LocalDate.of(2025, 1, 20));
        newer.setUser(testUser);
        sleepEntryRepository.save(newer);

        List<SleepEntry> entries = sleepEntryRepository.findByUserId(testUser.getId());

        assertEquals(2, entries.size());
        assertEquals(LocalDate.of(2025, 1, 20), entries.get(0).getDate());
        assertEquals(LocalDate.of(2025, 1, 10), entries.get(1).getDate());
    }

    @Test
    void testFindByUserIdEmptyForOtherUser() {
        SleepEntry entry = new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0));
        entry.setUser(testUser);
        sleepEntryRepository.save(entry);

        User otherUser = new User("othersleepuser", "password", "othersleep@example.com");
        otherUser = userRepository.save(otherUser);

        List<SleepEntry> entries = sleepEntryRepository.findByUserId(otherUser.getId());
        assertTrue(entries.isEmpty());
    }

    @Test
    void testDeleteById() {
        SleepEntry entry = new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0));
        entry.setUser(testUser);
        SleepEntry saved = sleepEntryRepository.save(entry);

        sleepEntryRepository.deleteById(saved.getId());

        assertFalse(sleepEntryRepository.findById(saved.getId()).isPresent());
    }
}
