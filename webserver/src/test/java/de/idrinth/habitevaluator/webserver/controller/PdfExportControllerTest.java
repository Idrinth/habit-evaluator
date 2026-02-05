package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import de.idrinth.habitevaluator.shared.service.DiaryService;
import de.idrinth.habitevaluator.shared.service.EventCorrelationService;
import de.idrinth.habitevaluator.shared.service.HabitScoringService;
import de.idrinth.habitevaluator.shared.service.SleepEvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PdfExportControllerTest {

    private HabitRepository habitRepository;
    private DiaryEntryRepository diaryEntryRepository;
    private SleepEntryRepository sleepEntryRepository;
    private EmotionEntryRepository emotionEntryRepository;
    private HabitScoringService scoringService;
    private DiaryService diaryService;
    private SleepEvaluationService sleepEvaluationService;
    private EventCorrelationService correlationService;
    private PdfExportController controller;
    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        habitRepository = mock(HabitRepository.class);
        diaryEntryRepository = mock(DiaryEntryRepository.class);
        sleepEntryRepository = mock(SleepEntryRepository.class);
        emotionEntryRepository = mock(EmotionEntryRepository.class);
        scoringService = mock(HabitScoringService.class);
        diaryService = mock(DiaryService.class);
        sleepEvaluationService = mock(SleepEvaluationService.class);
        correlationService = mock(EventCorrelationService.class);
        controller = new PdfExportController(habitRepository, diaryEntryRepository, sleepEntryRepository,
                emotionEntryRepository, scoringService, diaryService, sleepEvaluationService, correlationService);
        session = new MockHttpSession();
        testUser = new User("testuser", "password");
        session.setAttribute("userId", testUser.getId());
    }

    @Test
    void testExportPdfUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<byte[]> response = controller.exportPdf(null, null, true, true, true, true, unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testExportPdfNoSectionsSelected() {
        ResponseEntity<byte[]> response = controller.exportPdf(null, null, false, false, false, false, session);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testExportPdfSuccess() {
        when(habitRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(diaryEntryRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(sleepEntryRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(emotionEntryRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(correlationService.calculateCorrelations(any(), any(), any(), any())).thenReturn(new ArrayList<>());
        when(diaryService.getDayPoints(any(), any())).thenReturn(0);
        when(diaryService.getEntriesInRange(any(), any(), any())).thenReturn(new ArrayList<>());

        ResponseEntity<byte[]> response = controller.exportPdf("2025-01-01", "2025-01-31", true, true, true, true, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
        assertEquals("application/pdf", response.getHeaders().getContentType().toString());
        assertTrue(response.getHeaders().getFirst("Content-Disposition").contains("habit_report_"));
    }

    @Test
    void testExportPdfDefaultDates() {
        when(habitRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(diaryEntryRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(sleepEntryRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(emotionEntryRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(correlationService.calculateCorrelations(any(), any(), any(), any())).thenReturn(new ArrayList<>());
        when(diaryService.getDayPoints(any(), any())).thenReturn(0);
        when(diaryService.getEntriesInRange(any(), any(), any())).thenReturn(new ArrayList<>());

        ResponseEntity<byte[]> response = controller.exportPdf(null, null, true, true, true, true, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void testExportPdfHabitsOnlySection() {
        when(habitRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(diaryEntryRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(sleepEntryRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(emotionEntryRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(correlationService.calculateCorrelations(any(), any(), any(), any())).thenReturn(new ArrayList<>());

        ResponseEntity<byte[]> response = controller.exportPdf("2025-01-01", "2025-01-31", true, false, false, false, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }
}
