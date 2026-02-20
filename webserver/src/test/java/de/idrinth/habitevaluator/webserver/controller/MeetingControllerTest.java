package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.MeetingEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.MeetingEntryRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.webserver.service.StatsCacheService;
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

class MeetingControllerTest {

    private MeetingEntryRepository meetingEntryRepository;
    private UserRepository userRepository;
    private StatsCacheService statsCacheService;
    private MeetingController controller;
    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        meetingEntryRepository = mock(MeetingEntryRepository.class);
        userRepository = mock(UserRepository.class);
        statsCacheService = mock(StatsCacheService.class);
        controller = new MeetingController(meetingEntryRepository, userRepository, statsCacheService);
        session = new MockHttpSession();
        testUser = new User("testuser", "password");
        session.setAttribute("userId", testUser.getId());
    }

    @Test
    void testGetAllEntriesUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<List<MeetingEntry>> response = controller.getAllEntries(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetAllEntriesSuccess() {
        MeetingEntry entry = new MeetingEntry("Office", "Alice, Bob",
                LocalTime.of(10, 0), LocalTime.of(11, 0));
        when(meetingEntryRepository.findByUserId(testUser.getId())).thenReturn(List.of(entry));

        ResponseEntity<List<MeetingEntry>> response = controller.getAllEntries(session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testCreateEntrySuccess() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(meetingEntryRepository.save(any(MeetingEntry.class))).thenAnswer(i -> i.getArgument(0));

        MeetingEntry entry = new MeetingEntry("Office", "Alice",
                LocalTime.of(14, 0), LocalTime.of(15, 0));
        ResponseEntity<?> response = controller.createEntry(entry, session);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testCreateEntryUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<?> response = controller.createEntry(new MeetingEntry(), unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testCreateEntryUserNotFound() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());
        ResponseEntity<?> response = controller.createEntry(new MeetingEntry(), session);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testDeleteEntrySuccess() {
        MeetingEntry entry = new MeetingEntry("Office", "Alice",
                LocalTime.of(10, 0), LocalTime.of(11, 0));
        entry.setUser(testUser);
        when(meetingEntryRepository.findById(entry.getId())).thenReturn(Optional.of(entry));

        ResponseEntity<Void> response = controller.deleteEntry(entry.getId(), session);

        assertEquals(204, response.getStatusCode().value());
        verify(meetingEntryRepository).deleteById(entry.getId());
    }

    @Test
    void testDeleteEntryNotFound() {
        when(meetingEntryRepository.findById("nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<Void> response = controller.deleteEntry("nonexistent", session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteEntryNotOwned() {
        User otherUser = new User("other", "pass");
        MeetingEntry entry = new MeetingEntry("Office", "Alice",
                LocalTime.of(10, 0), LocalTime.of(11, 0));
        entry.setUser(otherUser);
        when(meetingEntryRepository.findById(entry.getId())).thenReturn(Optional.of(entry));

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
