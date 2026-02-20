package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.ActivityLog;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class JpaActivityLogRepositoryTest {

    @Autowired
    private JpaActivityLogRepository activityLogRepository;

    @Autowired
    private JpaUserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("activityloguser", "password123", "activityloguser@example.com");
        testUser = userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        ActivityLog entry = new ActivityLog("Alice, Bob", "Office",
                LocalTime.of(9, 0), LocalTime.of(10, 0), LocalDate.of(2025, 1, 15));
        entry.setUser(testUser);
        ActivityLog saved = activityLogRepository.save(entry);

        Optional<ActivityLog> found = activityLogRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Alice, Bob", found.get().getPersons());
        assertEquals("Office", found.get().getLocation());
        assertEquals(LocalTime.of(9, 0), found.get().getStartTime());
        assertEquals(LocalTime.of(10, 0), found.get().getEndTime());
        assertEquals(LocalDate.of(2025, 1, 15), found.get().getDate());
    }

    @Test
    void testFindByUserId() {
        ActivityLog entry1 = new ActivityLog("Alice", "Office",
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        entry1.setUser(testUser);
        activityLogRepository.save(entry1);

        ActivityLog entry2 = new ActivityLog("Bob", "Cafe",
                LocalTime.of(14, 0), LocalTime.of(15, 0));
        entry2.setUser(testUser);
        activityLogRepository.save(entry2);

        List<ActivityLog> entries = activityLogRepository.findByUserId(testUser.getId());

        assertEquals(2, entries.size());
    }

    @Test
    void testFindByUserIdOrderedByDateDesc() {
        ActivityLog older = new ActivityLog("Alice", "Office",
                LocalTime.of(9, 0), LocalTime.of(10, 0), LocalDate.of(2025, 1, 10));
        older.setUser(testUser);
        activityLogRepository.save(older);

        ActivityLog newer = new ActivityLog("Bob", "Cafe",
                LocalTime.of(14, 0), LocalTime.of(15, 0), LocalDate.of(2025, 1, 20));
        newer.setUser(testUser);
        activityLogRepository.save(newer);

        List<ActivityLog> entries = activityLogRepository.findByUserId(testUser.getId());

        assertEquals(2, entries.size());
        assertEquals(LocalDate.of(2025, 1, 20), entries.get(0).getDate());
        assertEquals(LocalDate.of(2025, 1, 10), entries.get(1).getDate());
    }

    @Test
    void testFindByUserIdEmptyForOtherUser() {
        ActivityLog entry = new ActivityLog("Alice", "Office",
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        entry.setUser(testUser);
        activityLogRepository.save(entry);

        User otherUser = new User("otheractivityloguser", "password", "otheractivitylog@example.com");
        otherUser = userRepository.save(otherUser);

        List<ActivityLog> entries = activityLogRepository.findByUserId(otherUser.getId());
        assertTrue(entries.isEmpty());
    }

    @Test
    void testDeleteById() {
        ActivityLog entry = new ActivityLog("Alice", "Office",
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        entry.setUser(testUser);
        ActivityLog saved = activityLogRepository.save(entry);

        activityLogRepository.deleteById(saved.getId());

        assertFalse(activityLogRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void testFindDistinctLocationsByUserId() {
        ActivityLog entry1 = new ActivityLog("Alice", "Office",
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        entry1.setUser(testUser);
        activityLogRepository.save(entry1);

        ActivityLog entry2 = new ActivityLog("Bob", "Cafe",
                LocalTime.of(14, 0), LocalTime.of(15, 0));
        entry2.setUser(testUser);
        activityLogRepository.save(entry2);

        ActivityLog entry3 = new ActivityLog("Charlie", "Office",
                LocalTime.of(16, 0), LocalTime.of(17, 0));
        entry3.setUser(testUser);
        activityLogRepository.save(entry3);

        List<String> locations = activityLogRepository.findDistinctLocationsByUserId(testUser.getId());

        assertEquals(2, locations.size());
        assertTrue(locations.contains("Office"));
        assertTrue(locations.contains("Cafe"));
    }

    @Test
    void testFindDistinctActivitiesByUserId() {
        ActivityLog entry1 = new ActivityLog("Alice", "Office",
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        entry1.setUser(testUser);
        entry1.setActivity("Team meeting");
        activityLogRepository.save(entry1);

        ActivityLog entry2 = new ActivityLog("Bob", "Cafe",
                LocalTime.of(14, 0), LocalTime.of(15, 0));
        entry2.setUser(testUser);
        entry2.setActivity("Lunch");
        activityLogRepository.save(entry2);

        ActivityLog entry3 = new ActivityLog("Charlie", "Office",
                LocalTime.of(16, 0), LocalTime.of(17, 0));
        entry3.setUser(testUser);
        entry3.setActivity("Team meeting");
        activityLogRepository.save(entry3);

        List<String> activities = activityLogRepository.findDistinctActivitiesByUserId(testUser.getId());

        assertEquals(2, activities.size());
        assertTrue(activities.contains("Team meeting"));
        assertTrue(activities.contains("Lunch"));
    }

    @Test
    void testFindDistinctActivitiesByUserIdExcludesNull() {
        ActivityLog entry1 = new ActivityLog("Alice", "Office",
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        entry1.setUser(testUser);
        entry1.setActivity("Team meeting");
        activityLogRepository.save(entry1);

        ActivityLog entry2 = new ActivityLog("Bob", "Cafe",
                LocalTime.of(14, 0), LocalTime.of(15, 0));
        entry2.setUser(testUser);
        activityLogRepository.save(entry2);

        List<String> activities = activityLogRepository.findDistinctActivitiesByUserId(testUser.getId());

        assertEquals(1, activities.size());
        assertEquals("Team meeting", activities.get(0));
    }
}
