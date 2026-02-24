package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmotionPairControllerTest {

    private EmotionPairRepository emotionPairRepository;
    private EmotionEntryRepository emotionEntryRepository;
    private EmotionPairController controller;
    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        emotionPairRepository = mock(EmotionPairRepository.class);
        emotionEntryRepository = mock(EmotionEntryRepository.class);
        controller = new EmotionPairController(emotionPairRepository, emotionEntryRepository);
        session = new MockHttpSession();
        testUser = new User("testuser", "password");
        session.setAttribute("userId", testUser.getId());
    }

    @Test
    void testGetPairsUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<List<Map<String, Object>>> response = controller.getPairs(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetPairsSuccess() {
        EmotionPair pair = new EmotionPair("sad", "happy");
        when(emotionPairRepository.findByUserId(testUser.getId())).thenReturn(List.of(pair));

        ResponseEntity<List<Map<String, Object>>> response = controller.getPairs(session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        Map<String, Object> pairMap = response.getBody().get(0);
        assertEquals("sad", pairMap.get("negativeLabel"));
        assertEquals("happy", pairMap.get("positiveLabel"));
        assertEquals(pair.getId(), pairMap.get("id"));
    }

    @Test
    void testGetGraphDataUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<Map<String, Object>> response = controller.getGraphData(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetGraphDataWithNoEntries() {
        when(emotionPairRepository.findByUserId(testUser.getId())).thenReturn(List.of(new EmotionPair("sad", "happy")));
        when(emotionEntryRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());

        ResponseEntity<Map<String, Object>> response = controller.getGraphData(session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody().get("labels"));
        assertNotNull(response.getBody().get("pairs"));
        List<?> pairs = (List<?>) response.getBody().get("pairs");
        assertEquals(1, pairs.size());
        Map<?, ?> pairData = (Map<?, ?>) pairs.get(0);
        List<?> entries = (List<?>) pairData.get("entries");
        assertNotNull(entries);
        assertEquals(0, entries.size());
    }

    @Test
    void testGetGraphDataWithEntries() {
        EmotionPair pair = new EmotionPair("sad", "happy");
        when(emotionPairRepository.findByUserId(testUser.getId())).thenReturn(List.of(pair));

        EmotionEntry entry = new EmotionEntry(pair, 5, LocalDateTime.now(), "feeling good");
        when(emotionEntryRepository.findByUserId(testUser.getId())).thenReturn(List.of(entry));

        ResponseEntity<Map<String, Object>> response = controller.getGraphData(session);

        assertEquals(200, response.getStatusCode().value());
        List<?> pairs = (List<?>) response.getBody().get("pairs");
        assertEquals(1, pairs.size());
        Map<?, ?> pairData = (Map<?, ?>) pairs.get(0);
        assertEquals(pair.getId(), pairData.get("pairId"));
        assertEquals("sad", pairData.get("negativeLabel"));
        assertEquals("happy", pairData.get("positiveLabel"));
        assertEquals(1, pairData.get("totalEntries"));

        List<?> entries = (List<?>) pairData.get("entries");
        assertNotNull(entries);
        assertEquals(1, entries.size());
        Map<?, ?> entryData = (Map<?, ?>) entries.get(0);
        assertEquals(entry.getId(), entryData.get("id"));
        assertEquals(5, entryData.get("strength"));
        assertEquals("feeling good", entryData.get("notes"));
        assertNotNull(entryData.get("recordedAt"));
    }

    @Test
    void testGetGraphDataEntriesSortedDescending() {
        EmotionPair pair = new EmotionPair("sad", "happy");
        when(emotionPairRepository.findByUserId(testUser.getId())).thenReturn(List.of(pair));

        LocalDateTime earlier = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime later = LocalDateTime.of(2025, 1, 2, 14, 0);
        EmotionEntry entry1 = new EmotionEntry(pair, 3, earlier, "first");
        EmotionEntry entry2 = new EmotionEntry(pair, 7, later, "second");
        when(emotionEntryRepository.findByUserId(testUser.getId())).thenReturn(List.of(entry1, entry2));

        ResponseEntity<Map<String, Object>> response = controller.getGraphData(session);

        List<?> pairs = (List<?>) response.getBody().get("pairs");
        Map<?, ?> pairData = (Map<?, ?>) pairs.get(0);
        List<?> entries = (List<?>) pairData.get("entries");
        assertEquals(2, entries.size());
        Map<?, ?> firstEntry = (Map<?, ?>) entries.get(0);
        Map<?, ?> secondEntry = (Map<?, ?>) entries.get(1);
        assertEquals("second", firstEntry.get("notes"));
        assertEquals("first", secondEntry.get("notes"));
    }
}
