package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.EventCorrelation;
import de.idrinth.habitevaluator.shared.model.FoodLog;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.HabitScore;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.ActivityLogRepository;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import de.idrinth.habitevaluator.shared.repository.FoodLogRepository;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.MeetingEntryRepository;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import de.idrinth.habitevaluator.shared.repository.SportLogRepository;
import de.idrinth.habitevaluator.shared.service.DiaryService;
import de.idrinth.habitevaluator.shared.service.EventCorrelationService;
import de.idrinth.habitevaluator.shared.service.HabitScoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class StatsControllerTest {

    private HabitRepository habitRepository;
    private SleepEntryRepository sleepEntryRepository;
    private DiaryEntryRepository diaryEntryRepository;
    private EmotionEntryRepository emotionEntryRepository;
    private HabitCategoryRepository habitCategoryRepository;
    private SportLogRepository sportLogRepository;
    private FoodLogRepository foodLogRepository;
    private MeetingEntryRepository meetingEntryRepository;
    private ActivityLogRepository activityLogRepository;
    private HabitScoringService scoringService;
    private DiaryService diaryService;
    private EventCorrelationService correlationService;
    private StatsController controller;
    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        habitRepository = mock(HabitRepository.class);
        sleepEntryRepository = mock(SleepEntryRepository.class);
        diaryEntryRepository = mock(DiaryEntryRepository.class);
        emotionEntryRepository = mock(EmotionEntryRepository.class);
        habitCategoryRepository = mock(HabitCategoryRepository.class);
        sportLogRepository = mock(SportLogRepository.class);
        foodLogRepository = mock(FoodLogRepository.class);
        meetingEntryRepository = mock(MeetingEntryRepository.class);
        activityLogRepository = mock(ActivityLogRepository.class);
        scoringService = mock(HabitScoringService.class);
        diaryService = mock(DiaryService.class);
        correlationService = mock(EventCorrelationService.class);
        controller = new StatsController(habitRepository, sleepEntryRepository, diaryEntryRepository,
                emotionEntryRepository, habitCategoryRepository, sportLogRepository, foodLogRepository,
                meetingEntryRepository, activityLogRepository, scoringService, diaryService, correlationService);
        session = new MockHttpSession();
        testUser = new User("testuser", "password");
        session.setAttribute("userId", testUser.getId());
    }

    @Test
    void testGetDashboardUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<Map<String, Object>> response = controller.getDashboard(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetDashboardSuccess() {
        when(habitRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(diaryEntryRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(sleepEntryRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(diaryService.getDayPoints(any(), any(LocalDate.class))).thenReturn(0);

        ResponseEntity<Map<String, Object>> response = controller.getDashboard(session);

        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("labels"));
        assertTrue(body.containsKey("habitPoints"));
        assertTrue(body.containsKey("diaryPoints"));
        assertTrue(body.containsKey("sleepDuration"));
        assertTrue(body.containsKey("sleepEntries"));

        List<?> labels = (List<?>) body.get("labels");
        assertEquals(30, labels.size());
    }

    @Test
    void testGetDailyTimelineUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<Map<String, Object>> response = controller.getDailyTimeline(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetDailyTimelineSuccess() {
        when(habitRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(habitCategoryRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());

        ResponseEntity<Map<String, Object>> response = controller.getDailyTimeline(session);

        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("labels"));
        assertTrue(body.containsKey("habits"));

        List<?> labels = (List<?>) body.get("labels");
        assertEquals(30, labels.size());
    }

    @Test
    void testGetCorrelationsUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<List<Map<String, Object>>> response = controller.getCorrelations(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetCorrelationsSuccess() {
        when(habitRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(diaryEntryRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(sleepEntryRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(emotionEntryRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(sportLogRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(meetingEntryRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(activityLogRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());

        EventCorrelation correlation = new EventCorrelation("Habit: Exercise", "Sleep Hours", 0.75, 30);
        when(correlationService.calculateCorrelations(any(), any(), any(), any(), any(), any(), any())).thenReturn(List.of(correlation));

        ResponseEntity<List<Map<String, Object>>> response = controller.getCorrelations(session);

        assertEquals(200, response.getStatusCode().value());
        List<Map<String, Object>> body = response.getBody();
        assertEquals(1, body.size());
        assertEquals("Habit: Exercise", body.get(0).get("eventA"));
        assertEquals("Sleep Hours", body.get(0).get("eventB"));
        assertEquals(0.75, body.get(0).get("correlation"));
        assertEquals(30, body.get(0).get("sharedDays"));
    }

    @Test
    void testGetFoodDistributionUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<Map<String, Object>> response = controller.getFoodDistribution(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetFoodDistributionSuccess() {
        when(foodLogRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());

        ResponseEntity<Map<String, Object>> response = controller.getFoodDistribution(session);

        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("labels"));
        assertTrue(body.containsKey("mealCounts"));
        assertTrue(body.containsKey("avgKcal"));
        assertTrue(body.containsKey("avgCarbs"));

        List<?> labels = (List<?>) body.get("labels");
        assertEquals(24, labels.size());
        assertEquals("00:00", labels.get(0));
        assertEquals("23:00", labels.get(23));
    }

    @Test
    void testGetFoodDistributionWithEntries() {
        FoodLog entry1 = new FoodLog(30.0, 500, LocalDateTime.now().withHour(12).withMinute(0), "Rice, Chicken");
        FoodLog entry2 = new FoodLog(20.0, 300, LocalDateTime.now().withHour(12).withMinute(30), "Salad");
        FoodLog entry3 = new FoodLog(50.0, 800, LocalDateTime.now().withHour(19).withMinute(0), "Pasta");

        when(foodLogRepository.findByUserId(testUser.getId())).thenReturn(List.of(entry1, entry2, entry3));

        ResponseEntity<Map<String, Object>> response = controller.getFoodDistribution(session);

        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        List<Integer> counts = (List<Integer>) body.get("mealCounts");
        assertEquals(2, counts.get(12));
        assertEquals(1, counts.get(19));
        assertEquals(0, counts.get(0));

        List<Double> avgKcal = (List<Double>) body.get("avgKcal");
        assertEquals(400.0, avgKcal.get(12));
        assertEquals(800.0, avgKcal.get(19));
    }

    @Test
    void testGetEmotionScatterUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<Map<String, Object>> response = controller.getEmotionScatter(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetEmotionScatterSuccess() {
        when(emotionEntryRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());

        ResponseEntity<Map<String, Object>> response = controller.getEmotionScatter(session);

        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("labels"));
        assertTrue(body.containsKey("pairs"));

        List<?> labels = (List<?>) body.get("labels");
        assertEquals(5, labels.size());
        assertEquals("00:00", labels.get(0));
        assertEquals("24:00", labels.get(4));

        List<?> pairs = (List<?>) body.get("pairs");
        assertEquals(0, pairs.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetEmotionScatterWithEntries() {
        EmotionPair pair = new EmotionPair("Sad", "Happy");

        EmotionEntry entry1 = new EmotionEntry(pair, 5, LocalDateTime.now().withHour(10).withMinute(30), "Good morning");
        EmotionEntry entry2 = new EmotionEntry(pair, -3, LocalDateTime.now().withHour(22).withMinute(0), "Tired");

        when(emotionEntryRepository.findByUserId(testUser.getId())).thenReturn(List.of(entry1, entry2));

        ResponseEntity<Map<String, Object>> response = controller.getEmotionScatter(session);

        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        List<Map<String, Object>> pairs = (List<Map<String, Object>>) body.get("pairs");
        assertEquals(1, pairs.size());

        Map<String, Object> pairScatter = pairs.get(0);
        assertEquals(pair.getId(), pairScatter.get("pairId"));
        assertNotNull(pairScatter.get("pairLabel"));
        assertNotNull(pairScatter.get("color"));

        List<Map<String, Object>> entries = (List<Map<String, Object>>) pairScatter.get("entries");
        assertEquals(2, entries.size());

        assertEquals(5, entries.get(0).get("strength"));
        assertEquals(-3, entries.get(1).get("strength"));
    }
}
