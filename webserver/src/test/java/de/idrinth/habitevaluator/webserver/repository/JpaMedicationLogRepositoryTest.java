package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.Medication;
import de.idrinth.habitevaluator.shared.model.MedicationLog;
import de.idrinth.habitevaluator.shared.model.MedicationProvisionType;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class JpaMedicationLogRepositoryTest {

    @Autowired
    private JpaMedicationLogRepository medicationLogRepository;

    @Autowired
    private JpaMedicationRepository medicationRepository;

    @Autowired
    private JpaUserRepository userRepository;

    private User testUser;
    private Medication testMedication;

    @BeforeEach
    void setUp() {
        testUser = new User("medloguser", "password123", "medloguser@example.com");
        testUser = userRepository.save(testUser);

        testMedication = new Medication("Ibuprofen", MedicationProvisionType.PILL);
        testMedication.setUser(testUser);
        testMedication = medicationRepository.save(testMedication);
    }

    @Test
    void testSaveAndFindById() {
        LocalDateTime takenAt = LocalDateTime.of(2025, 1, 15, 8, 0);
        MedicationLog log = new MedicationLog(testMedication, 400.0, takenAt);
        log.setUser(testUser);
        MedicationLog saved = medicationLogRepository.save(log);

        Optional<MedicationLog> found = medicationLogRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(400.0, found.get().getAmount());
        assertEquals(takenAt, found.get().getTakenAt());
    }

    @Test
    void testFindByUserId() {
        MedicationLog log1 = new MedicationLog(testMedication, 400.0, LocalDateTime.of(2025, 1, 15, 8, 0));
        log1.setUser(testUser);
        medicationLogRepository.save(log1);

        MedicationLog log2 = new MedicationLog(testMedication, 200.0, LocalDateTime.of(2025, 1, 15, 20, 0));
        log2.setUser(testUser);
        medicationLogRepository.save(log2);

        List<MedicationLog> logs = medicationLogRepository.findByUserId(testUser.getId());

        assertEquals(2, logs.size());
    }

    @Test
    void testFindByUserIdOrderedByTakenAtDesc() {
        MedicationLog older = new MedicationLog(testMedication, 400.0, LocalDateTime.of(2025, 1, 10, 8, 0));
        older.setUser(testUser);
        medicationLogRepository.save(older);

        MedicationLog newer = new MedicationLog(testMedication, 200.0, LocalDateTime.of(2025, 1, 20, 8, 0));
        newer.setUser(testUser);
        medicationLogRepository.save(newer);

        List<MedicationLog> logs = medicationLogRepository.findByUserId(testUser.getId());

        assertEquals(2, logs.size());
        assertEquals(LocalDateTime.of(2025, 1, 20, 8, 0), logs.get(0).getTakenAt());
        assertEquals(LocalDateTime.of(2025, 1, 10, 8, 0), logs.get(1).getTakenAt());
    }

    @Test
    void testFindByUserIdPaged() {
        for (int i = 1; i <= 5; i++) {
            MedicationLog log = new MedicationLog(testMedication, i * 100.0,
                    LocalDateTime.of(2025, 1, i, 8, 0));
            log.setUser(testUser);
            medicationLogRepository.save(log);
        }

        List<MedicationLog> page1 = medicationLogRepository.findByUserIdPaged(testUser.getId(), PageRequest.of(0, 2));
        assertEquals(2, page1.size());

        List<MedicationLog> page2 = medicationLogRepository.findByUserIdPaged(testUser.getId(), PageRequest.of(1, 2));
        assertEquals(2, page2.size());

        List<MedicationLog> page3 = medicationLogRepository.findByUserIdPaged(testUser.getId(), PageRequest.of(2, 2));
        assertEquals(1, page3.size());
    }

    @Test
    void testCountByUserId() {
        assertEquals(0, medicationLogRepository.countByUserId(testUser.getId()));

        MedicationLog log1 = new MedicationLog(testMedication, 400.0, LocalDateTime.now());
        log1.setUser(testUser);
        medicationLogRepository.save(log1);

        MedicationLog log2 = new MedicationLog(testMedication, 200.0, LocalDateTime.now());
        log2.setUser(testUser);
        medicationLogRepository.save(log2);

        assertEquals(2, medicationLogRepository.countByUserId(testUser.getId()));
    }

    @Test
    void testCountByUserIdIsolation() {
        MedicationLog log = new MedicationLog(testMedication, 400.0, LocalDateTime.now());
        log.setUser(testUser);
        medicationLogRepository.save(log);

        User otherUser = new User("othermedloguser", "password", "othermedlog@example.com");
        otherUser = userRepository.save(otherUser);

        assertEquals(0, medicationLogRepository.countByUserId(otherUser.getId()));
    }

    @Test
    void testFindByUserIdEmptyForOtherUser() {
        MedicationLog log = new MedicationLog(testMedication, 400.0, LocalDateTime.now());
        log.setUser(testUser);
        medicationLogRepository.save(log);

        User otherUser = new User("othermedloguser2", "password", "othermedlog2@example.com");
        otherUser = userRepository.save(otherUser);

        List<MedicationLog> logs = medicationLogRepository.findByUserId(otherUser.getId());
        assertTrue(logs.isEmpty());
    }

    @Test
    void testDeleteById() {
        MedicationLog log = new MedicationLog(testMedication, 400.0, LocalDateTime.now());
        log.setUser(testUser);
        MedicationLog saved = medicationLogRepository.save(log);

        medicationLogRepository.deleteById(saved.getId());

        assertFalse(medicationLogRepository.findById(saved.getId()).isPresent());
    }
}
