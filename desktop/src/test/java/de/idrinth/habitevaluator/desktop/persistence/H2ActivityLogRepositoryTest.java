package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.ActivityLog;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class H2ActivityLogRepositoryTest extends H2RepositoryTestBase {

    private H2ActivityLogRepository activityLogRepository;
    private H2UserRepository userRepository;
    private User testUser;

    @BeforeEach
    void setUp() {
        activityLogRepository = new H2ActivityLogRepository();
        userRepository = new H2UserRepository();

        for (ActivityLog entry : activityLogRepository.findAll()) {
            activityLogRepository.deleteById(entry.getId());
        }
        for (User user : userRepository.findAll()) {
            userRepository.deleteById(user.getId());
        }

        testUser = new User("activityuser", "password123");
        userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        ActivityLog entry = new ActivityLog("Alice, Bob", "Office",
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        entry.setUser(testUser);
        ActivityLog saved = activityLogRepository.save(entry);

        assertNotNull(saved);
        Optional<ActivityLog> found = activityLogRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Alice, Bob", found.get().getPersons());
        assertEquals("Office", found.get().getLocation());
        assertEquals(LocalTime.of(9, 0), found.get().getStartTime());
        assertEquals(LocalTime.of(10, 0), found.get().getEndTime());
    }

    @Test
    void testSaveUpdatesExisting() {
        ActivityLog entry = new ActivityLog("Alice, Bob", "Office",
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        entry.setUser(testUser);
        activityLogRepository.save(entry);

        entry.setLocation("Conference Room");
        entry.setActivity("Sprint planning");
        activityLogRepository.save(entry);

        Optional<ActivityLog> found = activityLogRepository.findById(entry.getId());
        assertTrue(found.isPresent());
        assertEquals("Conference Room", found.get().getLocation());
        assertEquals("Sprint planning", found.get().getActivity());
    }

    @Test
    void testFindByIdReturnsEmptyForNonExistent() {
        Optional<ActivityLog> found = activityLogRepository.findById("nonexistent-id");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        ActivityLog entry1 = new ActivityLog("Alice, Bob", "Office",
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        entry1.setUser(testUser);
        ActivityLog entry2 = new ActivityLog("Charlie", "Park",
                LocalTime.of(14, 0), LocalTime.of(15, 0));
        entry2.setUser(testUser);
        activityLogRepository.save(entry1);
        activityLogRepository.save(entry2);

        List<ActivityLog> all = activityLogRepository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testDeleteById() {
        ActivityLog entry = new ActivityLog("Alice, Bob", "Office",
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        entry.setUser(testUser);
        activityLogRepository.save(entry);

        assertTrue(activityLogRepository.findById(entry.getId()).isPresent());
        activityLogRepository.deleteById(entry.getId());
        assertFalse(activityLogRepository.findById(entry.getId()).isPresent());
    }

    @Test
    void testDeleteByIdNonExistentDoesNotThrow() {
        assertDoesNotThrow(() -> activityLogRepository.deleteById("nonexistent-id"));
    }

    @Test
    void testExistsById() {
        ActivityLog entry = new ActivityLog("Alice, Bob", "Office",
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        entry.setUser(testUser);
        activityLogRepository.save(entry);

        assertTrue(activityLogRepository.existsById(entry.getId()));
        assertFalse(activityLogRepository.existsById("nonexistent-id"));
    }

    @Test
    void testFindByUserId() {
        ActivityLog entry1 = new ActivityLog("Alice, Bob", "Office",
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        entry1.setUser(testUser);
        activityLogRepository.save(entry1);

        User otherUser = new User("otheractivityuser", "password123");
        userRepository.save(otherUser);
        ActivityLog entry2 = new ActivityLog("Charlie", "Park",
                LocalTime.of(14, 0), LocalTime.of(15, 0));
        entry2.setUser(otherUser);
        activityLogRepository.save(entry2);

        List<ActivityLog> userEntries = activityLogRepository.findByUserId(testUser.getId());
        assertEquals(1, userEntries.size());
        assertEquals("Alice, Bob", userEntries.get(0).getPersons());
    }

    @Test
    void testFindByUserIdReturnsEmptyForNonExistentUser() {
        List<ActivityLog> entries = activityLogRepository.findByUserId("nonexistent-user-id");
        assertTrue(entries.isEmpty());
    }

    @Test
    void testDatePersisted() {
        LocalDate specificDate = LocalDate.of(2024, 6, 15);
        ActivityLog entry = new ActivityLog("Alice, Bob", "Office",
                LocalTime.of(9, 0), LocalTime.of(10, 0), specificDate);
        entry.setUser(testUser);
        activityLogRepository.save(entry);

        Optional<ActivityLog> found = activityLogRepository.findById(entry.getId());
        assertTrue(found.isPresent());
        assertEquals(specificDate, found.get().getDate());
    }

    @Test
    void testActivityPersisted() {
        ActivityLog entry = new ActivityLog("Alice, Bob", "Office",
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        entry.setActivity("Code review session");
        entry.setUser(testUser);
        activityLogRepository.save(entry);

        Optional<ActivityLog> found = activityLogRepository.findById(entry.getId());
        assertTrue(found.isPresent());
        assertEquals("Code review session", found.get().getActivity());
    }
}
