package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.SportLog;
import de.idrinth.habitevaluator.shared.model.SportLogStats;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.SportLogRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.shared.service.SportLogService;
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
import static org.mockito.Mockito.*;

class SportLogControllerTest {

    private SportLogRepository sportLogRepository;
    private UserRepository userRepository;
    private SportLogService sportLogService;
    private SportLogController controller;
    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        sportLogRepository = mock(SportLogRepository.class);
        userRepository = mock(UserRepository.class);
        sportLogService = mock(SportLogService.class);
        controller = new SportLogController(sportLogRepository, userRepository, sportLogService);
        session = new MockHttpSession();
        testUser = new User("testuser", "password");
        session.setAttribute("userId", testUser.getId());
    }

    @Test
    void testGetAllEntriesUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<List<SportLog>> response = controller.getAllEntries(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetAllEntriesSuccess() {
        SportLog entry = new SportLog("Running", 5.0, "km", LocalTime.of(8, 0), LocalTime.of(9, 0));
        when(sportLogRepository.findByUserId(testUser.getId())).thenReturn(List.of(entry));

        ResponseEntity<List<SportLog>> response = controller.getAllEntries(session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testCreateEntrySuccess() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(sportLogRepository.save(any(SportLog.class))).thenAnswer(i -> i.getArgument(0));

        SportLog entry = new SportLog("Running", 5.0, "km", LocalTime.of(8, 0), LocalTime.of(9, 0));
        ResponseEntity<?> response = controller.createEntry(entry, session);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testCreateEntryMissingName() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        SportLog entry = new SportLog(null, 5.0, "km", LocalTime.of(8, 0), LocalTime.of(9, 0));
        ResponseEntity<?> response = controller.createEntry(entry, session);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testCreateEntryMissingUnit() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        SportLog entry = new SportLog("Running", 5.0, null, LocalTime.of(8, 0), LocalTime.of(9, 0));
        ResponseEntity<?> response = controller.createEntry(entry, session);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testCreateEntryUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<?> response = controller.createEntry(new SportLog(), unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testDeleteEntrySuccess() {
        SportLog entry = new SportLog("Running", 5.0, "km", LocalTime.of(8, 0), LocalTime.of(9, 0));
        entry.setUser(testUser);
        when(sportLogRepository.findById(entry.getId())).thenReturn(Optional.of(entry));

        ResponseEntity<Void> response = controller.deleteEntry(entry.getId(), session);

        assertEquals(204, response.getStatusCode().value());
        verify(sportLogRepository).deleteById(entry.getId());
    }

    @Test
    void testDeleteEntryNotFound() {
        when(sportLogRepository.findById("nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<Void> response = controller.deleteEntry("nonexistent", session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteEntryNotOwned() {
        User otherUser = new User("other", "pass");
        SportLog entry = new SportLog("Running", 5.0, "km", LocalTime.of(8, 0), LocalTime.of(9, 0));
        entry.setUser(otherUser);
        when(sportLogRepository.findById(entry.getId())).thenReturn(Optional.of(entry));

        ResponseEntity<Void> response = controller.deleteEntry(entry.getId(), session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testGetSuggestionsUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<Map<String, List<String>>> response = controller.getSuggestions(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetSuggestionsSuccess() {
        when(sportLogRepository.findDistinctNamesByUserId(testUser.getId()))
                .thenReturn(List.of("Running", "Swimming"));
        when(sportLogRepository.findDistinctMeasurementUnitsByUserId(testUser.getId()))
                .thenReturn(List.of("km", "m"));

        ResponseEntity<Map<String, List<String>>> response = controller.getSuggestions(session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().get("names").size());
        assertEquals("Running", response.getBody().get("names").get(0));
        assertEquals("Swimming", response.getBody().get("names").get(1));
        assertEquals(2, response.getBody().get("units").size());
        assertEquals("km", response.getBody().get("units").get(0));
        assertEquals("m", response.getBody().get("units").get(1));
    }

    @Test
    void testGetSuggestionsEmpty() {
        when(sportLogRepository.findDistinctNamesByUserId(testUser.getId()))
                .thenReturn(List.of());
        when(sportLogRepository.findDistinctMeasurementUnitsByUserId(testUser.getId()))
                .thenReturn(List.of());

        ResponseEntity<Map<String, List<String>>> response = controller.getSuggestions(session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("names").isEmpty());
        assertTrue(response.getBody().get("units").isEmpty());
    }

    @Test
    void testGetGraphUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<Map<String, Object>> response = controller.getGraph(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetGraphEmpty() {
        when(sportLogRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());

        ResponseEntity<Map<String, Object>> response = controller.getGraph(session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        List<String> labels = (List<String>) response.getBody().get("labels");
        assertEquals(30, labels.size());
        List<Map<String, Object>> activities = (List<Map<String, Object>>) response.getBody().get("activities");
        assertEquals(0, activities.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetGraphWithData() {
        LocalDate today = LocalDate.now();
        SportLog entry1 = new SportLog("Running", 5.0, "km", LocalTime.of(8, 0), LocalTime.of(9, 0), today);
        SportLog entry2 = new SportLog("Running", 3.0, "km", LocalTime.of(7, 0), LocalTime.of(7, 30), today.minusDays(1));
        SportLog entry3 = new SportLog("Swimming", 1000, "m", LocalTime.of(10, 0), LocalTime.of(11, 0), today);
        when(sportLogRepository.findByUserId(testUser.getId())).thenReturn(List.of(entry1, entry2, entry3));

        ResponseEntity<Map<String, Object>> response = controller.getGraph(session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        List<Map<String, Object>> activities = (List<Map<String, Object>>) response.getBody().get("activities");
        assertEquals(2, activities.size());

        // First activity should be Running
        assertEquals("Running", activities.get(0).get("name"));
        assertEquals("km", activities.get(0).get("unit"));

        // Second activity should be Swimming
        assertEquals("Swimming", activities.get(1).get("name"));
        assertEquals("m", activities.get(1).get("unit"));

        // Each activity should have 30 daily data points
        List<Double> runningDuration = (List<Double>) activities.get(0).get("dailyDuration");
        assertEquals(30, runningDuration.size());
    }

    @Test
    void testGetStatsUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<Map<String, SportLogStats>> response = controller.getStats(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetStatsSuccess() {
        List<SportLog> entries = List.of(
                new SportLog("Running", 5.0, "km", LocalTime.of(8, 0), LocalTime.of(9, 0))
        );
        when(sportLogRepository.findByUserId(testUser.getId())).thenReturn(entries);

        SportLogStats weeklyStats = new SportLogStats(LocalDate.now().minusDays(7), LocalDate.now(), 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 1);
        SportLogStats monthlyStats = new SportLogStats(LocalDate.now().minusDays(30), LocalDate.now(), 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 1);
        when(sportLogService.getCurrentWeekStats(entries)).thenReturn(weeklyStats);
        when(sportLogService.getCurrentMonthStats(entries)).thenReturn(monthlyStats);

        ResponseEntity<Map<String, SportLogStats>> response = controller.getStats(session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody().get("weekly"));
        assertNotNull(response.getBody().get("monthly"));
    }
}
