package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.SportLog;
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
class JpaSportLogRepositoryTest {

    @Autowired
    private JpaSportLogRepository sportLogRepository;

    @Autowired
    private JpaUserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("sportuser", "password123", "sportuser@example.com");
        testUser = userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        SportLog log = new SportLog("Running", 5.0, "km",
                LocalTime.of(7, 0), LocalTime.of(7, 45), LocalDate.of(2025, 1, 15));
        log.setUser(testUser);
        SportLog saved = sportLogRepository.save(log);

        Optional<SportLog> found = sportLogRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Running", found.get().getName());
        assertEquals(5.0, found.get().getMeasurement());
        assertEquals("km", found.get().getMeasurementUnit());
    }

    @Test
    void testFindByUserId() {
        SportLog log1 = new SportLog("Running", 5.0, "km",
                LocalTime.of(7, 0), LocalTime.of(7, 45));
        log1.setUser(testUser);
        sportLogRepository.save(log1);

        SportLog log2 = new SportLog("Swimming", 1.0, "km",
                LocalTime.of(18, 0), LocalTime.of(19, 0));
        log2.setUser(testUser);
        sportLogRepository.save(log2);

        List<SportLog> logs = sportLogRepository.findByUserId(testUser.getId());

        assertEquals(2, logs.size());
    }

    @Test
    void testFindByUserIdOrderedByDateDesc() {
        SportLog older = new SportLog("Running", 5.0, "km",
                LocalTime.of(7, 0), LocalTime.of(7, 45), LocalDate.of(2025, 1, 10));
        older.setUser(testUser);
        sportLogRepository.save(older);

        SportLog newer = new SportLog("Swimming", 1.0, "km",
                LocalTime.of(18, 0), LocalTime.of(19, 0), LocalDate.of(2025, 1, 20));
        newer.setUser(testUser);
        sportLogRepository.save(newer);

        List<SportLog> logs = sportLogRepository.findByUserId(testUser.getId());

        assertEquals(2, logs.size());
        assertEquals(LocalDate.of(2025, 1, 20), logs.get(0).getDate());
        assertEquals(LocalDate.of(2025, 1, 10), logs.get(1).getDate());
    }

    @Test
    void testFindByUserIdEmptyForOtherUser() {
        SportLog log = new SportLog("Running", 5.0, "km",
                LocalTime.of(7, 0), LocalTime.of(7, 45));
        log.setUser(testUser);
        sportLogRepository.save(log);

        User otherUser = new User("othersportuser", "password", "othersport@example.com");
        otherUser = userRepository.save(otherUser);

        List<SportLog> logs = sportLogRepository.findByUserId(otherUser.getId());
        assertTrue(logs.isEmpty());
    }

    @Test
    void testDeleteById() {
        SportLog log = new SportLog("Running", 5.0, "km",
                LocalTime.of(7, 0), LocalTime.of(7, 45));
        log.setUser(testUser);
        SportLog saved = sportLogRepository.save(log);

        sportLogRepository.deleteById(saved.getId());

        assertFalse(sportLogRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void testFindDistinctNamesByUserId() {
        SportLog log1 = new SportLog("Running", 5.0, "km",
                LocalTime.of(7, 0), LocalTime.of(7, 45));
        log1.setUser(testUser);
        sportLogRepository.save(log1);

        SportLog log2 = new SportLog("Swimming", 1.0, "km",
                LocalTime.of(18, 0), LocalTime.of(19, 0));
        log2.setUser(testUser);
        sportLogRepository.save(log2);

        SportLog log3 = new SportLog("Running", 3.0, "km",
                LocalTime.of(8, 0), LocalTime.of(8, 30));
        log3.setUser(testUser);
        sportLogRepository.save(log3);

        List<String> names = sportLogRepository.findDistinctNamesByUserId(testUser.getId());

        assertEquals(2, names.size());
        assertEquals("Running", names.get(0));
        assertEquals("Swimming", names.get(1));
    }

    @Test
    void testFindDistinctMeasurementUnitsByUserId() {
        SportLog log1 = new SportLog("Running", 5.0, "km",
                LocalTime.of(7, 0), LocalTime.of(7, 45));
        log1.setUser(testUser);
        sportLogRepository.save(log1);

        SportLog log2 = new SportLog("Swimming", 1000, "m",
                LocalTime.of(18, 0), LocalTime.of(19, 0));
        log2.setUser(testUser);
        sportLogRepository.save(log2);

        SportLog log3 = new SportLog("Cycling", 20.0, "km",
                LocalTime.of(8, 0), LocalTime.of(9, 0));
        log3.setUser(testUser);
        sportLogRepository.save(log3);

        List<String> units = sportLogRepository.findDistinctMeasurementUnitsByUserId(testUser.getId());

        assertEquals(2, units.size());
        assertEquals("km", units.get(0));
        assertEquals("m", units.get(1));
    }

    @Test
    void testFindDistinctNamesByUserIdEmptyForOtherUser() {
        SportLog log = new SportLog("Running", 5.0, "km",
                LocalTime.of(7, 0), LocalTime.of(7, 45));
        log.setUser(testUser);
        sportLogRepository.save(log);

        User otherUser = new User("othersportuser2", "password", "othersport2@example.com");
        otherUser = userRepository.save(otherUser);

        List<String> names = sportLogRepository.findDistinctNamesByUserId(otherUser.getId());
        assertTrue(names.isEmpty());
    }
}
