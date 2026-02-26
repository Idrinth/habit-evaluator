package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.GratitudeEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.GratitudeEntryRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.shared.service.GratitudeService;
import de.idrinth.habitevaluator.webserver.service.StatsCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GratitudeControllerTest {

    private GratitudeEntryRepository gratitudeEntryRepository;
    private UserRepository userRepository;
    private GratitudeService gratitudeService;
    private StatsCacheService statsCacheService;
    private GratitudeController controller;
    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        gratitudeEntryRepository = mock(GratitudeEntryRepository.class);
        userRepository = mock(UserRepository.class);
        gratitudeService = mock(GratitudeService.class);
        statsCacheService = mock(StatsCacheService.class);
        controller = new GratitudeController(gratitudeEntryRepository, userRepository, gratitudeService, statsCacheService);
        session = new MockHttpSession();
        testUser = new User("testuser", "password");
        session.setAttribute("userId", testUser.getId());
    }

    @Test
    void testGetAllEntriesUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<List<GratitudeEntry>> response = controller.getAllEntries(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetAllEntriesSuccess() {
        GratitudeEntry entry = new GratitudeEntry("Grateful for sunshine");
        when(gratitudeEntryRepository.findByUserId(testUser.getId())).thenReturn(List.of(entry));

        ResponseEntity<List<GratitudeEntry>> response = controller.getAllEntries(session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testCreateEntrySuccess() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(gratitudeEntryRepository.save(any(GratitudeEntry.class))).thenAnswer(i -> i.getArgument(0));

        GratitudeEntry entry = new GratitudeEntry("I'm grateful for my health");
        ResponseEntity<GratitudeEntry> response = controller.createEntry(entry, session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("I'm grateful for my health", response.getBody().getDescription());
        assertEquals(testUser, response.getBody().getUser());
        verify(statsCacheService).invalidateUser(testUser.getId());
    }

    @Test
    void testCreateEntryUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<GratitudeEntry> response = controller.createEntry(new GratitudeEntry(), unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testCreateEntryUserNotFound() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        ResponseEntity<GratitudeEntry> response = controller.createEntry(new GratitudeEntry(), session);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testDeleteEntrySuccess() {
        GratitudeEntry entry = new GratitudeEntry("Test");
        entry.setUser(testUser);
        when(gratitudeEntryRepository.findById(entry.getId())).thenReturn(Optional.of(entry));

        ResponseEntity<Void> response = controller.deleteEntry(entry.getId(), session);

        assertEquals(204, response.getStatusCode().value());
        verify(gratitudeEntryRepository).deleteById(entry.getId());
        verify(statsCacheService).invalidateUser(testUser.getId());
    }

    @Test
    void testDeleteEntryUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<Void> response = controller.deleteEntry("some-id", unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testDeleteEntryNotFound() {
        when(gratitudeEntryRepository.findById("missing")).thenReturn(Optional.empty());
        ResponseEntity<Void> response = controller.deleteEntry("missing", session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteEntryWrongUser() {
        GratitudeEntry entry = new GratitudeEntry("Other user entry");
        User otherUser = new User("other", "pass");
        entry.setUser(otherUser);
        when(gratitudeEntryRepository.findById(entry.getId())).thenReturn(Optional.of(entry));

        ResponseEntity<Void> response = controller.deleteEntry(entry.getId(), session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testGetStatsUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<Map<String, Object>> response = controller.getStats(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetStatsSuccess() {
        List<GratitudeEntry> entries = List.of(new GratitudeEntry("Test"));
        when(gratitudeEntryRepository.findByUserId(testUser.getId())).thenReturn(entries);
        when(gratitudeService.getDayCount(any(), any())).thenReturn(1);
        when(gratitudeService.getCurrentWeekCount(any())).thenReturn(5);
        when(gratitudeService.getCurrentMonthCount(any())).thenReturn(15);
        when(gratitudeService.getDailyAverageForMonth(any())).thenReturn(1.5);
        when(gratitudeService.getCurrentStreak(any())).thenReturn(3);

        ResponseEntity<Map<String, Object>> response = controller.getStats(session);

        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> stats = response.getBody();
        assertNotNull(stats);
        assertEquals(1, stats.get("todayCount"));
        assertEquals(5, stats.get("weekCount"));
        assertEquals(15, stats.get("monthCount"));
        assertEquals(1.5, stats.get("dailyAverage"));
        assertEquals(3, stats.get("currentStreak"));
    }
}
