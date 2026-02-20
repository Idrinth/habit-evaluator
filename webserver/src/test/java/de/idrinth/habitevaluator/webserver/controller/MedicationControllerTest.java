package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.Medication;
import de.idrinth.habitevaluator.shared.model.MedicationLog;
import de.idrinth.habitevaluator.shared.model.MedicationProvisionType;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.MedicationLogRepository;
import de.idrinth.habitevaluator.shared.repository.MedicationRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.webserver.service.StatsCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MedicationControllerTest {

    private MedicationRepository medicationRepository;
    private MedicationLogRepository medicationLogRepository;
    private UserRepository userRepository;
    private StatsCacheService statsCacheService;
    private MedicationController controller;
    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        medicationRepository = mock(MedicationRepository.class);
        medicationLogRepository = mock(MedicationLogRepository.class);
        userRepository = mock(UserRepository.class);
        statsCacheService = mock(StatsCacheService.class);
        controller = new MedicationController(medicationRepository, medicationLogRepository, userRepository, statsCacheService);
        session = new MockHttpSession();
        testUser = new User("testuser", "password");
        session.setAttribute("userId", testUser.getId());
    }

    // --- getAllMedications ---

    @Test
    void testGetAllMedicationsUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<List<Medication>> response = controller.getAllMedications(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetAllMedicationsSuccess() {
        Medication med = new Medication("Aspirin", MedicationProvisionType.PILL);
        when(medicationRepository.findByUserId(testUser.getId())).thenReturn(List.of(med));

        ResponseEntity<List<Medication>> response = controller.getAllMedications(session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals("Aspirin", response.getBody().get(0).getName());
    }

    // --- createMedication ---

    @Test
    void testCreateMedicationUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<?> response = controller.createMedication(new Medication(), unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testCreateMedicationUserNotFound() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());
        ResponseEntity<?> response = controller.createMedication(new Medication(), session);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testCreateMedicationSuccess() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(medicationRepository.save(any(Medication.class))).thenAnswer(i -> i.getArgument(0));

        Medication medication = new Medication("Ibuprofen", MedicationProvisionType.PILL);
        ResponseEntity<?> response = controller.createMedication(medication, session);

        assertEquals(200, response.getStatusCode().value());
        Medication saved = (Medication) response.getBody();
        assertEquals("Ibuprofen", saved.getName());
        assertEquals(testUser, saved.getUser());
    }

    // --- updateMedication ---

    @Test
    void testUpdateMedicationUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<?> response = controller.updateMedication("some-id", new Medication(), unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testUpdateMedicationNotFound() {
        when(medicationRepository.findById("nonexistent")).thenReturn(Optional.empty());
        ResponseEntity<?> response = controller.updateMedication("nonexistent", new Medication(), session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testUpdateMedicationNotOwned() {
        User otherUser = new User("other", "pass");
        Medication existing = new Medication("Aspirin", MedicationProvisionType.PILL);
        existing.setUser(otherUser);
        when(medicationRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

        ResponseEntity<?> response = controller.updateMedication(existing.getId(), new Medication(), session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testUpdateMedicationNullUser() {
        Medication existing = new Medication("Aspirin", MedicationProvisionType.PILL);
        when(medicationRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

        ResponseEntity<?> response = controller.updateMedication(existing.getId(), new Medication(), session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testUpdateMedicationSuccess() {
        Medication existing = new Medication("Aspirin", MedicationProvisionType.PILL);
        existing.setUser(testUser);
        when(medicationRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(medicationRepository.save(any(Medication.class))).thenAnswer(i -> i.getArgument(0));

        Medication update = new Medication();
        update.setWikipediaLink("https://en.wikipedia.org/wiki/Aspirin");
        ResponseEntity<?> response = controller.updateMedication(existing.getId(), update, session);

        assertEquals(200, response.getStatusCode().value());
        Medication saved = (Medication) response.getBody();
        assertEquals("https://en.wikipedia.org/wiki/Aspirin", saved.getWikipediaLink());
    }

    // --- deleteMedication ---

    @Test
    void testDeleteMedicationUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<Void> response = controller.deleteMedication("some-id", unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testDeleteMedicationNotFound() {
        when(medicationRepository.findById("nonexistent")).thenReturn(Optional.empty());
        ResponseEntity<Void> response = controller.deleteMedication("nonexistent", session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteMedicationNotOwned() {
        User otherUser = new User("other", "pass");
        Medication medication = new Medication("Aspirin", MedicationProvisionType.PILL);
        medication.setUser(otherUser);
        when(medicationRepository.findById(medication.getId())).thenReturn(Optional.of(medication));

        ResponseEntity<Void> response = controller.deleteMedication(medication.getId(), session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteMedicationNullUser() {
        Medication medication = new Medication("Aspirin", MedicationProvisionType.PILL);
        when(medicationRepository.findById(medication.getId())).thenReturn(Optional.of(medication));

        ResponseEntity<Void> response = controller.deleteMedication(medication.getId(), session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteMedicationSuccess() {
        Medication medication = new Medication("Aspirin", MedicationProvisionType.PILL);
        medication.setUser(testUser);
        when(medicationRepository.findById(medication.getId())).thenReturn(Optional.of(medication));

        ResponseEntity<Void> response = controller.deleteMedication(medication.getId(), session);

        assertEquals(204, response.getStatusCode().value());
        verify(medicationRepository).deleteById(medication.getId());
    }

    // --- getAllLogs ---

    @Test
    void testGetAllLogsUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<List<MedicationLog>> response = controller.getAllLogs(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetAllLogsSuccess() {
        Medication med = new Medication("Aspirin", MedicationProvisionType.PILL);
        MedicationLog log = new MedicationLog(med, 500.0, LocalDateTime.now());
        when(medicationLogRepository.findByUserId(testUser.getId())).thenReturn(List.of(log));

        ResponseEntity<List<MedicationLog>> response = controller.getAllLogs(session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    // --- createLog ---

    @Test
    void testCreateLogUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<?> response = controller.createLog(new MedicationLog(), unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testCreateLogUserNotFound() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());
        ResponseEntity<?> response = controller.createLog(new MedicationLog(), session);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testCreateLogNullMedication() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        MedicationLog log = new MedicationLog();
        // medication is null by default
        ResponseEntity<?> response = controller.createLog(log, session);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testCreateLogMedicationWithNullId() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        MedicationLog log = new MedicationLog();
        Medication med = new Medication();
        med.setId(null);
        log.setMedication(med);
        ResponseEntity<?> response = controller.createLog(log, session);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testCreateLogMedicationNotFound() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        Medication med = new Medication("Aspirin", MedicationProvisionType.PILL);
        MedicationLog log = new MedicationLog(med, 500.0, LocalDateTime.now());
        when(medicationRepository.findById(med.getId())).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.createLog(log, session);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testCreateLogMedicationNotOwnedByUser() {
        User otherUser = new User("other", "pass");
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        Medication med = new Medication("Aspirin", MedicationProvisionType.PILL);
        med.setUser(otherUser);
        when(medicationRepository.findById(med.getId())).thenReturn(Optional.of(med));

        MedicationLog log = new MedicationLog(med, 500.0, LocalDateTime.now());
        ResponseEntity<?> response = controller.createLog(log, session);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testCreateLogMedicationNullUser() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        Medication med = new Medication("Aspirin", MedicationProvisionType.PILL);
        // user is null
        when(medicationRepository.findById(med.getId())).thenReturn(Optional.of(med));

        MedicationLog log = new MedicationLog(med, 500.0, LocalDateTime.now());
        ResponseEntity<?> response = controller.createLog(log, session);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testCreateLogSuccess() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        Medication med = new Medication("Aspirin", MedicationProvisionType.PILL);
        med.setUser(testUser);
        when(medicationRepository.findById(med.getId())).thenReturn(Optional.of(med));
        when(medicationLogRepository.save(any(MedicationLog.class))).thenAnswer(i -> i.getArgument(0));

        MedicationLog log = new MedicationLog(med, 500.0, LocalDateTime.now());
        ResponseEntity<?> response = controller.createLog(log, session);

        assertEquals(200, response.getStatusCode().value());
        MedicationLog saved = (MedicationLog) response.getBody();
        assertSame(med, saved.getMedication());
        assertEquals(testUser, saved.getUser());
    }

    // --- deleteLog ---

    @Test
    void testDeleteLogUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<Void> response = controller.deleteLog("some-id", unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testDeleteLogNotFound() {
        when(medicationLogRepository.findById("nonexistent")).thenReturn(Optional.empty());
        ResponseEntity<Void> response = controller.deleteLog("nonexistent", session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteLogNotOwned() {
        User otherUser = new User("other", "pass");
        Medication med = new Medication("Aspirin", MedicationProvisionType.PILL);
        MedicationLog log = new MedicationLog(med, 500.0, LocalDateTime.now());
        log.setUser(otherUser);
        when(medicationLogRepository.findById(log.getId())).thenReturn(Optional.of(log));

        ResponseEntity<Void> response = controller.deleteLog(log.getId(), session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteLogNullUser() {
        Medication med = new Medication("Aspirin", MedicationProvisionType.PILL);
        MedicationLog log = new MedicationLog(med, 500.0, LocalDateTime.now());
        when(medicationLogRepository.findById(log.getId())).thenReturn(Optional.of(log));

        ResponseEntity<Void> response = controller.deleteLog(log.getId(), session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteLogSuccess() {
        Medication med = new Medication("Aspirin", MedicationProvisionType.PILL);
        MedicationLog log = new MedicationLog(med, 500.0, LocalDateTime.now());
        log.setUser(testUser);
        when(medicationLogRepository.findById(log.getId())).thenReturn(Optional.of(log));

        ResponseEntity<Void> response = controller.deleteLog(log.getId(), session);

        assertEquals(204, response.getStatusCode().value());
        verify(medicationLogRepository).deleteById(log.getId());
    }
}
