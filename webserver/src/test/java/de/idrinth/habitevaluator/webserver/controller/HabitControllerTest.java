package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.Evaluation;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.HabitScore;
import de.idrinth.habitevaluator.shared.model.PredictedWeeklyScore;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.shared.service.HabitEvaluatorService;
import de.idrinth.habitevaluator.shared.service.HabitScoringService;
import de.idrinth.habitevaluator.webserver.service.StatsCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class HabitControllerTest {

    private HabitRepository habitRepository;
    private HabitCategoryRepository habitCategoryRepository;
    private UserRepository userRepository;
    private HabitEvaluatorService evaluatorService;
    private HabitScoringService scoringService;
    private StatsCacheService statsCacheService;
    private HabitController controller;
    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        habitRepository = mock(HabitRepository.class);
        habitCategoryRepository = mock(HabitCategoryRepository.class);
        userRepository = mock(UserRepository.class);
        evaluatorService = mock(HabitEvaluatorService.class);
        scoringService = mock(HabitScoringService.class);
        statsCacheService = mock(StatsCacheService.class);
        controller = new HabitController(habitRepository, habitCategoryRepository, userRepository, evaluatorService, scoringService, statsCacheService);
        session = new MockHttpSession();
        testUser = new User("testuser", "password");
        session.setAttribute("userId", testUser.getId());
    }

    @Test
    void testGetAllHabitsUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<List<Habit>> response = controller.getAllHabits(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetAllHabitsAuthenticated() {
        List<Habit> habits = List.of(new Habit("Exercise", "Daily exercise"));
        when(habitRepository.findByUserId(testUser.getId())).thenReturn(habits);

        ResponseEntity<List<Habit>> response = controller.getAllHabits(session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals("Exercise", response.getBody().get(0).getName());
    }

    @Test
    void testGetHabitByIdOwned() {
        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setUser(testUser);
        when(habitRepository.findById(habit.getId())).thenReturn(Optional.of(habit));

        ResponseEntity<Habit> response = controller.getHabit(habit.getId(), session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Exercise", response.getBody().getName());
    }

    @Test
    void testGetHabitByIdNotOwned() {
        User otherUser = new User("other", "pass");
        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setUser(otherUser);
        when(habitRepository.findById(habit.getId())).thenReturn(Optional.of(habit));

        ResponseEntity<Habit> response = controller.getHabit(habit.getId(), session);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testGetHabitByIdNotFound() {
        when(habitRepository.findById("nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<Habit> response = controller.getHabit("nonexistent", session);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testCreateHabit() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        Habit habit = new Habit("Reading", "Daily reading");
        when(habitRepository.save(any(Habit.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<Habit> response = controller.createHabit(habit, session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Reading", response.getBody().getName());
        assertEquals(testUser, response.getBody().getUser());
    }

    @Test
    void testCreateHabitUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<Habit> response = controller.createHabit(new Habit("Test", "Test"), unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testCreateHabitUserNotFound() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());
        ResponseEntity<Habit> response = controller.createHabit(new Habit("Test", "Test"), session);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testUpdateHabitSuccess() {
        Habit existing = new Habit("Exercise", "Old description");
        existing.setUser(testUser);
        when(habitRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(habitRepository.save(any(Habit.class))).thenAnswer(i -> i.getArgument(0));

        Habit updated = new Habit("Exercise", "New description");
        ResponseEntity<Habit> response = controller.updateHabit(existing.getId(), updated, session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(existing.getId(), response.getBody().getId());
        assertEquals(testUser, response.getBody().getUser());
    }

    @Test
    void testUpdateHabitNotFound() {
        when(habitRepository.findById("nonexistent")).thenReturn(Optional.empty());
        ResponseEntity<Habit> response = controller.updateHabit("nonexistent", new Habit("Test", "Test"), session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testUpdateHabitNotOwned() {
        User otherUser = new User("other", "pass");
        Habit existing = new Habit("Exercise", "desc");
        existing.setUser(otherUser);
        when(habitRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

        ResponseEntity<Habit> response = controller.updateHabit(existing.getId(), new Habit("Test", "Test"), session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteHabitSuccess() {
        Habit habit = new Habit("Exercise", "desc");
        habit.setUser(testUser);
        habit.setCategoryId("cat-1");
        when(habitRepository.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(habitRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());

        ResponseEntity<Void> response = controller.deleteHabit(habit.getId(), session);

        assertEquals(204, response.getStatusCode().value());
        verify(habitRepository).deleteById(habit.getId());
        verify(habitCategoryRepository).deleteById("cat-1");
    }

    @Test
    void testDeleteHabitCategoryStillUsed() {
        Habit habit = new Habit("Exercise", "desc");
        habit.setUser(testUser);
        habit.setCategoryId("cat-1");
        Habit otherHabit = new Habit("Other", "desc");
        otherHabit.setCategoryId("cat-1");
        when(habitRepository.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(habitRepository.findByUserId(testUser.getId())).thenReturn(List.of(otherHabit));

        ResponseEntity<Void> response = controller.deleteHabit(habit.getId(), session);

        assertEquals(204, response.getStatusCode().value());
        verify(habitRepository).deleteById(habit.getId());
        verify(habitCategoryRepository, never()).deleteById(any());
    }

    @Test
    void testDeleteHabitNotOwned() {
        User otherUser = new User("other", "pass");
        Habit habit = new Habit("Exercise", "desc");
        habit.setUser(otherUser);
        when(habitRepository.findById(habit.getId())).thenReturn(Optional.of(habit));

        ResponseEntity<Void> response = controller.deleteHabit(habit.getId(), session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testAddEntrySuccess() {
        Habit habit = new Habit("Exercise", "desc");
        habit.setUser(testUser);
        when(habitRepository.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(habitRepository.save(any(Habit.class))).thenAnswer(i -> i.getArgument(0));

        HabitEntry entry = new HabitEntry();
        ResponseEntity<HabitEntry> response = controller.addEntry(habit.getId(), entry, session);

        assertEquals(200, response.getStatusCode().value());
        verify(habitRepository).save(habit);
    }

    @Test
    void testAddEntryHabitNotFound() {
        when(habitRepository.findById("nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<HabitEntry> response = controller.addEntry("nonexistent", new HabitEntry(), session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testEvaluateHabitSuccess() {
        Habit habit = new Habit("Exercise", "desc");
        habit.setUser(testUser);
        when(habitRepository.findById(habit.getId())).thenReturn(Optional.of(habit));

        Evaluation eval = new Evaluation(habit.getId(), "Exercise");
        when(evaluatorService.evaluate(eq(habit), any(LocalDate.class), any(LocalDate.class))).thenReturn(eval);

        ResponseEntity<Evaluation> response = controller.evaluateHabit(habit.getId(), null, null, session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Exercise", response.getBody().getHabitName());
    }

    @Test
    void testEvaluateHabitWithDateRange() {
        Habit habit = new Habit("Exercise", "desc");
        habit.setUser(testUser);
        when(habitRepository.findById(habit.getId())).thenReturn(Optional.of(habit));

        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);
        Evaluation eval = new Evaluation(habit.getId(), "Exercise");
        when(evaluatorService.evaluate(habit, start, end)).thenReturn(eval);

        ResponseEntity<Evaluation> response = controller.evaluateHabit(habit.getId(), start, end, session);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testPredictWeeklyScore() {
        List<Habit> habits = List.of(new Habit("Exercise", "desc"));
        when(habitRepository.findByUserId(testUser.getId())).thenReturn(habits);

        PredictedWeeklyScore prediction = new PredictedWeeklyScore();
        when(scoringService.predictCurrentWeekScore(habits)).thenReturn(prediction);

        ResponseEntity<PredictedWeeklyScore> response = controller.predictWeeklyScore(session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void testPredictWeeklyScoreUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<PredictedWeeklyScore> response = controller.predictWeeklyScore(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetPointDevelopmentWeek() {
        Habit habit = new Habit("Exercise", "desc");
        habit.setUser(testUser);
        when(habitRepository.findById(habit.getId())).thenReturn(Optional.of(habit));

        HabitScore score = new HabitScore(habit.getId(), "Exercise", 0, 0);
        when(scoringService.calculateHabitScore(eq(habit), any(LocalDate.class), any(LocalDate.class))).thenReturn(score);

        ResponseEntity<Map<String, Object>> response = controller.getPointDevelopment(habit.getId(), "week", session);

        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("dailyPoints"));
        assertTrue(body.containsKey("labels"));
        assertTrue(body.containsKey("runningAverages"));
        assertTrue(body.containsKey("cumulativeTotals"));
        assertTrue(body.containsKey("totalPoints"));
        assertTrue(body.containsKey("average"));
    }

    @Test
    void testGetPointDevelopmentMonth() {
        Habit habit = new Habit("Exercise", "desc");
        habit.setUser(testUser);
        when(habitRepository.findById(habit.getId())).thenReturn(Optional.of(habit));

        HabitScore score = new HabitScore(habit.getId(), "Exercise", 0, 0);
        when(scoringService.calculateHabitScore(eq(habit), any(LocalDate.class), any(LocalDate.class))).thenReturn(score);

        ResponseEntity<Map<String, Object>> response = controller.getPointDevelopment(habit.getId(), "month", session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetPointDevelopmentNotFound() {
        when(habitRepository.findById("nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = controller.getPointDevelopment("nonexistent", "week", session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testRemoveLastEntrySuccess() {
        Habit habit = new Habit("Exercise", "desc");
        habit.setUser(testUser);
        HabitEntry entry = new HabitEntry();
        entry.setCompletedAt(LocalDate.now().atTime(10, 0));
        habit.addEntry(entry);
        when(habitRepository.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(habitRepository.save(any(Habit.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<Void> response = controller.removeLastEntry(habit.getId(), LocalDate.now(), session);

        assertEquals(204, response.getStatusCode().value());
        verify(habitRepository).save(habit);
    }

    @Test
    void testRemoveLastEntryNoEntriesForDate() {
        Habit habit = new Habit("Exercise", "desc");
        habit.setUser(testUser);
        when(habitRepository.findById(habit.getId())).thenReturn(Optional.of(habit));

        ResponseEntity<Void> response = controller.removeLastEntry(habit.getId(), LocalDate.now(), session);

        assertEquals(404, response.getStatusCode().value());
        verify(habitRepository, never()).save(any());
    }

    @Test
    void testRemoveLastEntryHabitNotFound() {
        when(habitRepository.findById("nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<Void> response = controller.removeLastEntry("nonexistent", LocalDate.now(), session);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testRemoveLastEntryNotOwned() {
        User otherUser = new User("other", "pass");
        Habit habit = new Habit("Exercise", "desc");
        habit.setUser(otherUser);
        when(habitRepository.findById(habit.getId())).thenReturn(Optional.of(habit));

        ResponseEntity<Void> response = controller.removeLastEntry(habit.getId(), LocalDate.now(), session);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testRemoveLastEntryUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<Void> response = controller.removeLastEntry("any-id", LocalDate.now(), unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }
}
