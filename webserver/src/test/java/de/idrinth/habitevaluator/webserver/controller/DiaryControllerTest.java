package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EventSignificance;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.DiaryReferenceRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.shared.service.DiaryService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DiaryControllerTest {

    private DiaryEntryRepository diaryEntryRepository;
    private DiaryReferenceRepository diaryReferenceRepository;
    private UserRepository userRepository;
    private DiaryService diaryService;
    private DiaryController controller;
    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        diaryEntryRepository = mock(DiaryEntryRepository.class);
        diaryReferenceRepository = mock(DiaryReferenceRepository.class);
        userRepository = mock(UserRepository.class);
        diaryService = mock(DiaryService.class);
        controller = new DiaryController(diaryEntryRepository, diaryReferenceRepository, userRepository, diaryService);
        session = new MockHttpSession();
        testUser = new User("testuser", "password");
        session.setAttribute("userId", testUser.getId());
    }

    @Test
    void testGetAllEntriesUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<List<DiaryEntry>> response = controller.getAllEntries(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetAllEntriesSuccess() {
        DiaryEntry entry = new DiaryEntry("Good day", EventSignificance.NORMAL);
        when(diaryEntryRepository.findByUserId(testUser.getId())).thenReturn(List.of(entry));

        ResponseEntity<List<DiaryEntry>> response = controller.getAllEntries(session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testCreateEntrySuccess() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(diaryEntryRepository.save(any(DiaryEntry.class))).thenAnswer(i -> i.getArgument(0));

        DiaryEntry entry = new DiaryEntry("Great meeting", EventSignificance.MAJOR);
        ResponseEntity<DiaryEntry> response = controller.createEntry(entry, session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Great meeting", response.getBody().getDescription());
        assertEquals(testUser, response.getBody().getUser());
    }

    @Test
    void testCreateEntryUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<DiaryEntry> response = controller.createEntry(new DiaryEntry(), unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testDeleteEntrySuccess() {
        DiaryEntry entry = new DiaryEntry("Test", EventSignificance.MINOR);
        entry.setUser(testUser);
        when(diaryEntryRepository.findById(entry.getId())).thenReturn(Optional.of(entry));

        ResponseEntity<Void> response = controller.deleteEntry(entry.getId(), session);

        assertEquals(204, response.getStatusCode().value());
        verify(diaryEntryRepository).deleteById(entry.getId());
    }

    @Test
    void testDeleteEntryNotFound() {
        when(diaryEntryRepository.findById("nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<Void> response = controller.deleteEntry("nonexistent", session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteEntryNotOwned() {
        User otherUser = new User("other", "pass");
        DiaryEntry entry = new DiaryEntry("Test", EventSignificance.MINOR);
        entry.setUser(otherUser);
        when(diaryEntryRepository.findById(entry.getId())).thenReturn(Optional.of(entry));

        ResponseEntity<Void> response = controller.deleteEntry(entry.getId(), session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testGetSuggestionsSuccess() {
        when(diaryReferenceRepository.findDistinctDescriptionsByUserId(testUser.getId()))
                .thenReturn(List.of("Meeting", "Exercise"));

        ResponseEntity<List<String>> response = controller.getSuggestions(session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void testGetStatsSuccess() {
        List<DiaryEntry> entries = List.of(new DiaryEntry("Test", EventSignificance.NORMAL));
        when(diaryEntryRepository.findByUserId(testUser.getId())).thenReturn(entries);
        when(diaryService.getDayPoints(eq(entries), any(LocalDate.class))).thenReturn(2);
        when(diaryService.getCurrentWeekPoints(entries)).thenReturn(10);
        when(diaryService.getCurrentMonthPoints(entries)).thenReturn(40);
        when(diaryService.getWeeklyAverageForMonth(entries)).thenReturn(10.0);
        when(diaryService.getMonthlyTrend(entries)).thenReturn(0.5);

        ResponseEntity<Map<String, Object>> response = controller.getStats(session);

        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> stats = response.getBody();
        assertEquals(2, stats.get("todayPoints"));
        assertEquals(10, stats.get("weekPoints"));
        assertEquals(40, stats.get("monthPoints"));
        assertEquals(10.0, stats.get("weeklyAverage"));
        assertEquals(0.5, stats.get("monthlyTrend"));
    }
}
