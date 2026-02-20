package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.ActivityLog;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.ActivityLogRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ActivityLogControllerTest {

    private ActivityLogRepository activityLogRepository;
    private UserRepository userRepository;
    private ActivityLogController controller;
    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        activityLogRepository = mock(ActivityLogRepository.class);
        userRepository = mock(UserRepository.class);
        controller = new ActivityLogController(activityLogRepository, userRepository);
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

        ActivityLog entry = new ActivityLog("Alice", "Office",
                LocalTime.of(14, 0), LocalTime.of(15, 0));
        ResponseEntity<?> response = controller.createEntry(entry, session);

        assertEquals(200, response.getStatusCode().value());
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
}
