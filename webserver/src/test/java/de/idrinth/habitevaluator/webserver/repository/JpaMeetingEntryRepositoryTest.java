package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.MeetingEntry;
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
class JpaMeetingEntryRepositoryTest {

    @Autowired
    private JpaMeetingEntryRepository meetingEntryRepository;

    @Autowired
    private JpaUserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("meetinguser", "password123", "meetinguser@example.com");
        testUser = userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        MeetingEntry entry = new MeetingEntry("Office", "Alice, Bob",
                LocalTime.of(9, 0), LocalTime.of(10, 0), LocalDate.of(2025, 1, 15));
        entry.setUser(testUser);
        MeetingEntry saved = meetingEntryRepository.save(entry);

        Optional<MeetingEntry> found = meetingEntryRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Office", found.get().getPlace());
        assertEquals("Alice, Bob", found.get().getAttendants());
        assertEquals(LocalTime.of(9, 0), found.get().getStartTime());
        assertEquals(LocalTime.of(10, 0), found.get().getEndTime());
        assertEquals(LocalDate.of(2025, 1, 15), found.get().getDate());
    }

    @Test
    void testFindByUserId() {
        MeetingEntry entry1 = new MeetingEntry("Office", "Alice",
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        entry1.setUser(testUser);
        meetingEntryRepository.save(entry1);

        MeetingEntry entry2 = new MeetingEntry("Cafe", "Bob",
                LocalTime.of(14, 0), LocalTime.of(15, 0));
        entry2.setUser(testUser);
        meetingEntryRepository.save(entry2);

        List<MeetingEntry> entries = meetingEntryRepository.findByUserId(testUser.getId());

        assertEquals(2, entries.size());
    }

    @Test
    void testFindByUserIdOrderedByDateDesc() {
        MeetingEntry older = new MeetingEntry("Office", "Alice",
                LocalTime.of(9, 0), LocalTime.of(10, 0), LocalDate.of(2025, 1, 10));
        older.setUser(testUser);
        meetingEntryRepository.save(older);

        MeetingEntry newer = new MeetingEntry("Cafe", "Bob",
                LocalTime.of(14, 0), LocalTime.of(15, 0), LocalDate.of(2025, 1, 20));
        newer.setUser(testUser);
        meetingEntryRepository.save(newer);

        List<MeetingEntry> entries = meetingEntryRepository.findByUserId(testUser.getId());

        assertEquals(2, entries.size());
        assertEquals(LocalDate.of(2025, 1, 20), entries.get(0).getDate());
        assertEquals(LocalDate.of(2025, 1, 10), entries.get(1).getDate());
    }

    @Test
    void testFindByUserIdEmptyForOtherUser() {
        MeetingEntry entry = new MeetingEntry("Office", "Alice",
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        entry.setUser(testUser);
        meetingEntryRepository.save(entry);

        User otherUser = new User("othermeetinguser", "password", "othermeeting@example.com");
        otherUser = userRepository.save(otherUser);

        List<MeetingEntry> entries = meetingEntryRepository.findByUserId(otherUser.getId());
        assertTrue(entries.isEmpty());
    }

    @Test
    void testDeleteById() {
        MeetingEntry entry = new MeetingEntry("Office", "Alice",
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        entry.setUser(testUser);
        MeetingEntry saved = meetingEntryRepository.save(entry);

        meetingEntryRepository.deleteById(saved.getId());

        assertFalse(meetingEntryRepository.findById(saved.getId()).isPresent());
    }
}
