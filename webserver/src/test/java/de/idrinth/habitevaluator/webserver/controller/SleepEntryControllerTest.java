package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.SleepDistribution;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.SleepStats;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.shared.service.SleepEvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SleepEntryControllerTest {

    private SleepEntryRepository sleepEntryRepository;
    private UserRepository userRepository;
    private SleepEvaluationService sleepEvaluationService;
    private SleepEntryController controller;
    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        sleepEntryRepository = mock(SleepEntryRepository.class);
        userRepository = mock(UserRepository.class);
        sleepEvaluationService = mock(SleepEvaluationService.class);
        controller = new SleepEntryController(sleepEntryRepository, userRepository, sleepEvaluationService);
        session = new MockHttpSession();
        testUser = new User("testuser", "password");
        session.setAttribute("userId", testUser.getId());
    }

    @Test
    void testGetAllEntriesUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<List<SleepEntry>> response = controller.getAllEntries(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetAllEntriesSuccess() {
        SleepEntry entry = new SleepEntry(LocalTime.of(23, 0), LocalTime.of(7, 0));
        when(sleepEntryRepository.findByUserId(testUser.getId())).thenReturn(List.of(entry));

        ResponseEntity<List<SleepEntry>> response = controller.getAllEntries(session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testCreateEntrySuccess() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(sleepEntryRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(sleepEvaluationService.hasOverlap(any(), any(), any(), any())).thenReturn(false);
        when(sleepEntryRepository.save(any(SleepEntry.class))).thenAnswer(i -> i.getArgument(0));

        SleepEntry entry = new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0));
        ResponseEntity<?> response = controller.createEntry(entry, session);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testCreateEntryOverlap() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(sleepEntryRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(sleepEvaluationService.hasOverlap(any(), any(), any(), any())).thenReturn(true);

        SleepEntry entry = new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0));
        ResponseEntity<?> response = controller.createEntry(entry, session);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testCreateEntryUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<?> response = controller.createEntry(new SleepEntry(), unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testDeleteEntrySuccess() {
        SleepEntry entry = new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0));
        entry.setUser(testUser);
        when(sleepEntryRepository.findById(entry.getId())).thenReturn(Optional.of(entry));

        ResponseEntity<Void> response = controller.deleteEntry(entry.getId(), session);

        assertEquals(204, response.getStatusCode().value());
        verify(sleepEntryRepository).deleteById(entry.getId());
    }

    @Test
    void testDeleteEntryNotFound() {
        when(sleepEntryRepository.findById("nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<Void> response = controller.deleteEntry("nonexistent", session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteEntryNotOwned() {
        User otherUser = new User("other", "pass");
        SleepEntry entry = new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0));
        entry.setUser(otherUser);
        when(sleepEntryRepository.findById(entry.getId())).thenReturn(Optional.of(entry));

        ResponseEntity<Void> response = controller.deleteEntry(entry.getId(), session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testGetStatsSuccess() {
        List<SleepEntry> entries = List.of(new SleepEntry(LocalTime.of(23, 0), LocalTime.of(7, 0)));
        when(sleepEntryRepository.findByUserId(testUser.getId())).thenReturn(entries);

        SleepStats weeklyStats = new SleepStats(LocalDate.now().minusDays(7), LocalDate.now(), 7.5, 6.0, 9.0, 7);
        SleepStats monthlyStats = new SleepStats(LocalDate.now().minusDays(30), LocalDate.now(), 7.2, 5.5, 9.5, 28);
        when(sleepEvaluationService.getCurrentWeekStats(entries)).thenReturn(weeklyStats);
        when(sleepEvaluationService.getCurrentMonthStats(entries)).thenReturn(monthlyStats);

        ResponseEntity<Map<String, SleepStats>> response = controller.getStats(session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody().get("weekly"));
        assertNotNull(response.getBody().get("monthly"));
        assertEquals(7.5, response.getBody().get("weekly").getAverageHours());
    }

    @Test
    void testGetDistributionUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<SleepDistribution> response = controller.getDistribution(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetDistributionSuccess() {
        List<SleepEntry> entries = List.of(new SleepEntry(LocalTime.of(23, 0), LocalTime.of(7, 0)));
        when(sleepEntryRepository.findByUserId(testUser.getId())).thenReturn(entries);

        double[] percentages = new double[24];
        percentages[23] = 100.0;
        percentages[0] = 100.0;
        percentages[6] = 50.0;
        SleepDistribution distribution = new SleepDistribution(percentages);
        when(sleepEvaluationService.calculateSleepDistribution(entries)).thenReturn(distribution);

        ResponseEntity<SleepDistribution> response = controller.getDistribution(session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(100.0, response.getBody().getPercentAsleep()[23]);
        assertEquals(100.0, response.getBody().getPercentAsleep()[0]);
        assertEquals(50.0, response.getBody().getPercentAsleep()[6]);
    }

    @Test
    void testGetDistributionEmpty() {
        when(sleepEntryRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());

        SleepDistribution distribution = new SleepDistribution();
        when(sleepEvaluationService.calculateSleepDistribution(any())).thenReturn(distribution);

        ResponseEntity<SleepDistribution> response = controller.getDistribution(session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(0.0, response.getBody().getPercentAsleep()[0]);
    }
}
