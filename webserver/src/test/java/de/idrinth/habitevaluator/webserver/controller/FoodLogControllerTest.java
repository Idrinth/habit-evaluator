package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.FoodLog;
import de.idrinth.habitevaluator.shared.model.FoodTag;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.FoodLogRepository;
import de.idrinth.habitevaluator.shared.repository.FoodTagRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class FoodLogControllerTest {

    private FoodLogRepository foodLogRepository;
    private FoodTagRepository foodTagRepository;
    private UserRepository userRepository;
    private FoodLogController controller;
    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        foodLogRepository = mock(FoodLogRepository.class);
        foodTagRepository = mock(FoodTagRepository.class);
        userRepository = mock(UserRepository.class);
        controller = new FoodLogController(foodLogRepository, foodTagRepository, userRepository);
        session = new MockHttpSession();
        testUser = new User("testuser", "password");
        session.setAttribute("userId", testUser.getId());
    }

    @Test
    void testGetAllEntriesUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<List<FoodLog>> response = controller.getAllEntries(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetAllEntriesSuccess() {
        FoodLog entry = new FoodLog(30.0, 450, LocalDateTime.now(), "Rice, Chicken");
        when(foodLogRepository.findByUserId(testUser.getId())).thenReturn(List.of(entry));

        ResponseEntity<List<FoodLog>> response = controller.getAllEntries(session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testCreateEntryUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<?> response = controller.createEntry(new FoodLog(), unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testCreateEntryUserNotFound() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());
        ResponseEntity<?> response = controller.createEntry(new FoodLog(), session);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testCreateEntrySuccess() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(foodLogRepository.save(any(FoodLog.class))).thenAnswer(i -> i.getArgument(0));
        FoodTag tag = new FoodTag("Rice");
        tag.setUser(testUser);
        when(foodTagRepository.findByNameLowerAndUserId(anyString(), anyString())).thenReturn(Optional.of(tag));

        FoodLog entry = new FoodLog(30.0, 450, LocalDateTime.now(), "Rice");
        ResponseEntity<?> response = controller.createEntry(entry, session);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testCreateEntryCreatesNewTags() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(foodLogRepository.save(any(FoodLog.class))).thenAnswer(i -> i.getArgument(0));
        when(foodTagRepository.findByNameLowerAndUserId(anyString(), anyString())).thenReturn(Optional.empty());
        when(foodTagRepository.save(any(FoodTag.class))).thenAnswer(i -> i.getArgument(0));

        FoodLog entry = new FoodLog(30.0, 450, LocalDateTime.now(), "NewFood");
        ResponseEntity<?> response = controller.createEntry(entry, session);

        assertEquals(200, response.getStatusCode().value());
        verify(foodTagRepository).save(any(FoodTag.class));
    }

    @Test
    void testDeleteEntryUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<Void> response = controller.deleteEntry("some-id", unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testDeleteEntryNotFound() {
        when(foodLogRepository.findById("nonexistent")).thenReturn(Optional.empty());
        ResponseEntity<Void> response = controller.deleteEntry("nonexistent", session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteEntryNotOwned() {
        User otherUser = new User("other", "pass");
        FoodLog entry = new FoodLog(30.0, 450, LocalDateTime.now(), "Rice");
        entry.setUser(otherUser);
        when(foodLogRepository.findById(entry.getId())).thenReturn(Optional.of(entry));

        ResponseEntity<Void> response = controller.deleteEntry(entry.getId(), session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteEntrySuccess() {
        FoodLog entry = new FoodLog(30.0, 450, LocalDateTime.now(), "Rice");
        entry.setUser(testUser);
        when(foodLogRepository.findById(entry.getId())).thenReturn(Optional.of(entry));

        ResponseEntity<Void> response = controller.deleteEntry(entry.getId(), session);

        assertEquals(204, response.getStatusCode().value());
        verify(foodLogRepository).deleteById(entry.getId());
    }

    @Test
    void testGetSuggestionsUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<List<String>> response = controller.getSuggestions(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetSuggestionsSuccess() {
        FoodTag tag1 = new FoodTag("Apple");
        FoodTag tag2 = new FoodTag("Banana");
        when(foodTagRepository.findByUserId(testUser.getId())).thenReturn(List.of(tag2, tag1));

        ResponseEntity<List<String>> response = controller.getSuggestions(session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().size());
        assertEquals("Apple", response.getBody().get(0));
        assertEquals("Banana", response.getBody().get(1));
    }

    @Test
    void testMigrateTagsUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<Void> response = controller.migrateTags(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testMigrateTagsUserNotFound() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());
        ResponseEntity<Void> response = controller.migrateTags(session);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testMigrateTagsSuccess() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        FoodLog entryWithoutTags = new FoodLog(30.0, 450, LocalDateTime.now(), "Rice");
        entryWithoutTags.setUser(testUser);
        when(foodLogRepository.findByUserId(testUser.getId())).thenReturn(List.of(entryWithoutTags));
        when(foodTagRepository.findByNameLowerAndUserId(anyString(), anyString())).thenReturn(Optional.empty());
        when(foodTagRepository.save(any(FoodTag.class))).thenAnswer(i -> i.getArgument(0));
        when(foodLogRepository.save(any(FoodLog.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<Void> response = controller.migrateTags(session);

        assertEquals(204, response.getStatusCode().value());
        verify(foodLogRepository).save(any(FoodLog.class));
    }
}
