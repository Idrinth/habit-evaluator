package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.ActivityLog;
import de.idrinth.habitevaluator.shared.model.PersonTag;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.ActivityLogRepository;
import de.idrinth.habitevaluator.shared.repository.PersonTagRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.webserver.service.StatsCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ActivityLogControllerTest {

    private ActivityLogRepository activityLogRepository;
    private PersonTagRepository personTagRepository;
    private UserRepository userRepository;
    private StatsCacheService statsCacheService;
    private ActivityLogController controller;
    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        activityLogRepository = mock(ActivityLogRepository.class);
        personTagRepository = mock(PersonTagRepository.class);
        userRepository = mock(UserRepository.class);
        statsCacheService = mock(StatsCacheService.class);
        controller = new ActivityLogController(activityLogRepository, personTagRepository, userRepository, statsCacheService);
        session = new MockHttpSession();
        testUser = new User("testuser", "password");
        session.setAttribute("userId", testUser.getId());
    }

    @Test
    void testGetAllEntriesUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<List<ActivityLog>> response = controller.getAllEntries(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetAllEntriesSuccess() {
        ActivityLog entry = new ActivityLog("Alice, Bob", "Office",
                LocalTime.of(10, 0), LocalTime.of(11, 0));
        when(activityLogRepository.findByUserId(testUser.getId())).thenReturn(List.of(entry));

        ResponseEntity<List<ActivityLog>> response = controller.getAllEntries(session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testCreateEntrySuccess() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(activityLogRepository.save(any(ActivityLog.class))).thenAnswer(i -> i.getArgument(0));
        PersonTag tag = new PersonTag("Alice");
        tag.setUser(testUser);
        when(personTagRepository.findByNameLowerAndUserId(anyString(), anyString())).thenReturn(Optional.of(tag));

        ActivityLog entry = new ActivityLog("Alice", "Office",
                LocalTime.of(14, 0), LocalTime.of(15, 0));
        ResponseEntity<?> response = controller.createEntry(entry, session);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testCreateEntryCreatesNewPersonTags() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(activityLogRepository.save(any(ActivityLog.class))).thenAnswer(i -> i.getArgument(0));
        when(personTagRepository.findByNameLowerAndUserId(anyString(), anyString())).thenReturn(Optional.empty());
        when(personTagRepository.save(any(PersonTag.class))).thenAnswer(i -> i.getArgument(0));

        ActivityLog entry = new ActivityLog("NewPerson", "Office",
                LocalTime.of(14, 0), LocalTime.of(15, 0));
        ResponseEntity<?> response = controller.createEntry(entry, session);

        assertEquals(200, response.getStatusCode().value());
        verify(personTagRepository).save(any(PersonTag.class));
    }

    @Test
    void testCreateEntryUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<?> response = controller.createEntry(new ActivityLog(), unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testCreateEntryUserNotFound() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());
        ResponseEntity<?> response = controller.createEntry(new ActivityLog(), session);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testUpdateEntrySuccess() {
        ActivityLog existing = new ActivityLog("Alice", "Office",
                LocalTime.of(10, 0), LocalTime.of(11, 0));
        existing.setUser(testUser);
        when(activityLogRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(activityLogRepository.save(any(ActivityLog.class))).thenAnswer(i -> i.getArgument(0));
        PersonTag tag = new PersonTag("Alice");
        tag.setUser(testUser);
        when(personTagRepository.findByNameLowerAndUserId(anyString(), anyString())).thenReturn(Optional.of(tag));

        ActivityLog updated = new ActivityLog("Alice, Bob", "Park",
                LocalTime.of(14, 0), LocalTime.of(16, 0));
        ResponseEntity<?> response = controller.updateEntry(existing.getId(), updated, session);

        assertEquals(200, response.getStatusCode().value());
        ActivityLog result = (ActivityLog) response.getBody();
        assertEquals(existing.getId(), result.getId());
        assertEquals("Alice, Bob", result.getPersons());
        assertEquals("Park", result.getLocation());
        assertEquals(testUser, result.getUser());
        assertEquals(existing.getCreatedAt(), result.getCreatedAt());
    }

    @Test
    void testUpdateEntryNotFound() {
        when(activityLogRepository.findById("nonexistent")).thenReturn(Optional.empty());
        ActivityLog updated = new ActivityLog("Alice", "Office",
                LocalTime.of(10, 0), LocalTime.of(11, 0));

        ResponseEntity<?> response = controller.updateEntry("nonexistent", updated, session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testUpdateEntryNotOwned() {
        User otherUser = new User("other", "pass");
        ActivityLog existing = new ActivityLog("Alice", "Office",
                LocalTime.of(10, 0), LocalTime.of(11, 0));
        existing.setUser(otherUser);
        when(activityLogRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

        ActivityLog updated = new ActivityLog("Bob", "Park",
                LocalTime.of(14, 0), LocalTime.of(16, 0));
        ResponseEntity<?> response = controller.updateEntry(existing.getId(), updated, session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testUpdateEntryUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<?> response = controller.updateEntry("some-id", new ActivityLog(), unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testDeleteEntrySuccess() {
        ActivityLog entry = new ActivityLog("Alice", "Office",
                LocalTime.of(10, 0), LocalTime.of(11, 0));
        entry.setUser(testUser);
        when(activityLogRepository.findById(entry.getId())).thenReturn(Optional.of(entry));

        ResponseEntity<Void> response = controller.deleteEntry(entry.getId(), session);

        assertEquals(204, response.getStatusCode().value());
        verify(activityLogRepository).deleteById(entry.getId());
    }

    @Test
    void testDeleteEntryNotFound() {
        when(activityLogRepository.findById("nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<Void> response = controller.deleteEntry("nonexistent", session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteEntryNotOwned() {
        User otherUser = new User("other", "pass");
        ActivityLog entry = new ActivityLog("Alice", "Office",
                LocalTime.of(10, 0), LocalTime.of(11, 0));
        entry.setUser(otherUser);
        when(activityLogRepository.findById(entry.getId())).thenReturn(Optional.of(entry));

        ResponseEntity<Void> response = controller.deleteEntry(entry.getId(), session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteEntryUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<Void> response = controller.deleteEntry("some-id", unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetSuggestionsUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<Map<String, List<String>>> response = controller.getSuggestions(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetSuggestionsSuccess() {
        // No entries needing migration (empty list)
        when(activityLogRepository.findByUserId(testUser.getId())).thenReturn(Collections.emptyList());

        PersonTag tag1 = new PersonTag("Alice");
        PersonTag tag2 = new PersonTag("Bob");
        PersonTag tag3 = new PersonTag("Charlie");
        when(personTagRepository.findByUserId(testUser.getId())).thenReturn(List.of(tag1, tag2, tag3));
        when(activityLogRepository.findDistinctLocationsByUserId(testUser.getId())).thenReturn(List.of("Office", "Park"));
        when(activityLogRepository.findDistinctActivitiesByUserId(testUser.getId())).thenReturn(List.of("Meeting"));

        ResponseEntity<Map<String, List<String>>> response = controller.getSuggestions(session);

        assertEquals(200, response.getStatusCode().value());
        Map<String, List<String>> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.get("persons").contains("Alice"));
        assertTrue(body.get("persons").contains("Bob"));
        assertTrue(body.get("persons").contains("Charlie"));
        assertEquals(List.of("Office", "Park"), body.get("locations"));
        assertEquals(List.of("Meeting"), body.get("activities"));
    }

    @Test
    void testGetSuggestionsEmpty() {
        when(activityLogRepository.findByUserId(testUser.getId())).thenReturn(Collections.emptyList());
        when(personTagRepository.findByUserId(testUser.getId())).thenReturn(Collections.emptyList());
        when(activityLogRepository.findDistinctLocationsByUserId(testUser.getId())).thenReturn(Collections.emptyList());
        when(activityLogRepository.findDistinctActivitiesByUserId(testUser.getId())).thenReturn(Collections.emptyList());

        ResponseEntity<Map<String, List<String>>> response = controller.getSuggestions(session);

        assertEquals(200, response.getStatusCode().value());
        Map<String, List<String>> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.get("persons").isEmpty());
        assertTrue(body.get("locations").isEmpty());
        assertTrue(body.get("activities").isEmpty());
    }

    @Test
    void testGetSuggestionsMigratesExistingEntries() {
        ActivityLog entryWithoutTags = new ActivityLog("Alice, Bob", "Office",
                LocalTime.of(10, 0), LocalTime.of(11, 0));
        entryWithoutTags.setUser(testUser);
        when(activityLogRepository.findByUserId(testUser.getId())).thenReturn(List.of(entryWithoutTags));
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(personTagRepository.findByNameLowerAndUserId(anyString(), anyString())).thenReturn(Optional.empty());
        when(personTagRepository.save(any(PersonTag.class))).thenAnswer(i -> i.getArgument(0));
        when(activityLogRepository.save(any(ActivityLog.class))).thenAnswer(i -> i.getArgument(0));

        PersonTag tag1 = new PersonTag("Alice");
        PersonTag tag2 = new PersonTag("Bob");
        when(personTagRepository.findByUserId(testUser.getId())).thenReturn(List.of(tag1, tag2));
        when(activityLogRepository.findDistinctLocationsByUserId(testUser.getId())).thenReturn(Collections.emptyList());
        when(activityLogRepository.findDistinctActivitiesByUserId(testUser.getId())).thenReturn(Collections.emptyList());

        ResponseEntity<Map<String, List<String>>> response = controller.getSuggestions(session);

        assertEquals(200, response.getStatusCode().value());
        verify(personTagRepository, atLeastOnce()).save(any(PersonTag.class));
        verify(activityLogRepository).save(any(ActivityLog.class));
    }
}
