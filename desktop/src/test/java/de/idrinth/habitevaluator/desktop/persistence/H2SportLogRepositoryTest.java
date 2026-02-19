package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.SportLog;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class H2SportLogRepositoryTest extends H2RepositoryTestBase {

    private H2SportLogRepository sportLogRepository;
    private H2UserRepository userRepository;
    private User testUser;

    @BeforeEach
    void setUp() {
        sportLogRepository = new H2SportLogRepository();
        userRepository = new H2UserRepository();

        for (SportLog entry : sportLogRepository.findAll()) {
            sportLogRepository.deleteById(entry.getId());
        }
        for (User user : userRepository.findAll()) {
            userRepository.deleteById(user.getId());
        }

        testUser = new User("sportuser", "password123");
        userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        SportLog entry = new SportLog("Running", 5.0, "km",
                LocalTime.of(8, 0), LocalTime.of(9, 0));
        entry.setUser(testUser);
        SportLog saved = sportLogRepository.save(entry);

        assertNotNull(saved);
        Optional<SportLog> found = sportLogRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Running", found.get().getName());
        assertEquals(5.0, found.get().getMeasurement());
        assertEquals("km", found.get().getMeasurementUnit());
    }

    @Test
    void testSaveUpdatesExisting() {
        SportLog entry = new SportLog("Running", 5.0, "km",
                LocalTime.of(8, 0), LocalTime.of(9, 0));
        entry.setUser(testUser);
        sportLogRepository.save(entry);

        entry.setMeasurement(10.0);
        entry.setNotes("Personal best");
        sportLogRepository.save(entry);

        Optional<SportLog> found = sportLogRepository.findById(entry.getId());
        assertTrue(found.isPresent());
        assertEquals(10.0, found.get().getMeasurement());
        assertEquals("Personal best", found.get().getNotes());
    }

    @Test
    void testFindByIdReturnsEmptyForNonExistent() {
        Optional<SportLog> found = sportLogRepository.findById("nonexistent-id");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        SportLog entry1 = new SportLog("Running", 5.0, "km",
                LocalTime.of(8, 0), LocalTime.of(9, 0));
        entry1.setUser(testUser);
        SportLog entry2 = new SportLog("Swimming", 2.0, "km",
                LocalTime.of(10, 0), LocalTime.of(11, 0));
        entry2.setUser(testUser);
        sportLogRepository.save(entry1);
        sportLogRepository.save(entry2);

        List<SportLog> all = sportLogRepository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testDeleteById() {
        SportLog entry = new SportLog("Running", 5.0, "km",
                LocalTime.of(8, 0), LocalTime.of(9, 0));
        entry.setUser(testUser);
        sportLogRepository.save(entry);

        assertTrue(sportLogRepository.findById(entry.getId()).isPresent());
        sportLogRepository.deleteById(entry.getId());
        assertFalse(sportLogRepository.findById(entry.getId()).isPresent());
    }

    @Test
    void testDeleteByIdNonExistentDoesNotThrow() {
        assertDoesNotThrow(() -> sportLogRepository.deleteById("nonexistent-id"));
    }

    @Test
    void testExistsById() {
        SportLog entry = new SportLog("Running", 5.0, "km",
                LocalTime.of(8, 0), LocalTime.of(9, 0));
        entry.setUser(testUser);
        sportLogRepository.save(entry);

        assertTrue(sportLogRepository.existsById(entry.getId()));
        assertFalse(sportLogRepository.existsById("nonexistent-id"));
    }

    @Test
    void testFindByUserId() {
        SportLog entry1 = new SportLog("Running", 5.0, "km",
                LocalTime.of(8, 0), LocalTime.of(9, 0));
        entry1.setUser(testUser);
        sportLogRepository.save(entry1);

        User otherUser = new User("othersportuser", "password123");
        userRepository.save(otherUser);
        SportLog entry2 = new SportLog("Swimming", 2.0, "km",
                LocalTime.of(10, 0), LocalTime.of(11, 0));
        entry2.setUser(otherUser);
        sportLogRepository.save(entry2);

        List<SportLog> userEntries = sportLogRepository.findByUserId(testUser.getId());
        assertEquals(1, userEntries.size());
        assertEquals("Running", userEntries.get(0).getName());
    }

    @Test
    void testFindByUserIdReturnsEmptyForNonExistentUser() {
        List<SportLog> entries = sportLogRepository.findByUserId("nonexistent-user-id");
        assertTrue(entries.isEmpty());
    }

    @Test
    void testDatePersisted() {
        LocalDate specificDate = LocalDate.of(2024, 6, 15);
        SportLog entry = new SportLog("Running", 5.0, "km",
                LocalTime.of(8, 0), LocalTime.of(9, 0), specificDate);
        entry.setUser(testUser);
        sportLogRepository.save(entry);

        Optional<SportLog> found = sportLogRepository.findById(entry.getId());
        assertTrue(found.isPresent());
        assertEquals(specificDate, found.get().getDate());
    }

    @Test
    void testFindDistinctNamesByUserId() {
        SportLog entry1 = new SportLog("Running", 5.0, "km",
                LocalTime.of(8, 0), LocalTime.of(9, 0));
        entry1.setUser(testUser);
        sportLogRepository.save(entry1);

        SportLog entry2 = new SportLog("Swimming", 2.0, "km",
                LocalTime.of(10, 0), LocalTime.of(11, 0));
        entry2.setUser(testUser);
        sportLogRepository.save(entry2);

        SportLog entry3 = new SportLog("Running", 3.0, "km",
                LocalTime.of(6, 0), LocalTime.of(6, 30));
        entry3.setUser(testUser);
        sportLogRepository.save(entry3);

        List<String> names = sportLogRepository.findDistinctNamesByUserId(testUser.getId());
        assertEquals(2, names.size());
        assertTrue(names.contains("Running"));
        assertTrue(names.contains("Swimming"));
    }

    @Test
    void testFindDistinctMeasurementUnitsByUserId() {
        SportLog entry1 = new SportLog("Running", 5.0, "km",
                LocalTime.of(8, 0), LocalTime.of(9, 0));
        entry1.setUser(testUser);
        sportLogRepository.save(entry1);

        SportLog entry2 = new SportLog("Swimming", 1000, "m",
                LocalTime.of(10, 0), LocalTime.of(11, 0));
        entry2.setUser(testUser);
        sportLogRepository.save(entry2);

        SportLog entry3 = new SportLog("Cycling", 20.0, "km",
                LocalTime.of(6, 0), LocalTime.of(7, 0));
        entry3.setUser(testUser);
        sportLogRepository.save(entry3);

        List<String> units = sportLogRepository.findDistinctMeasurementUnitsByUserId(testUser.getId());
        assertEquals(2, units.size());
        assertTrue(units.contains("km"));
        assertTrue(units.contains("m"));
    }

    @Test
    void testFindDistinctNamesByUserIdEmpty() {
        List<String> names = sportLogRepository.findDistinctNamesByUserId("nonexistent-user-id");
        assertTrue(names.isEmpty());
    }

    @Test
    void testFindDistinctNamesByUserIdIsolatedPerUser() {
        SportLog entry1 = new SportLog("Running", 5.0, "km",
                LocalTime.of(8, 0), LocalTime.of(9, 0));
        entry1.setUser(testUser);
        sportLogRepository.save(entry1);

        User otherUser = new User("othersportuser2", "password123");
        userRepository.save(otherUser);
        SportLog entry2 = new SportLog("Swimming", 2.0, "km",
                LocalTime.of(10, 0), LocalTime.of(11, 0));
        entry2.setUser(otherUser);
        sportLogRepository.save(entry2);

        List<String> names = sportLogRepository.findDistinctNamesByUserId(testUser.getId());
        assertEquals(1, names.size());
        assertEquals("Running", names.get(0));
    }
}
