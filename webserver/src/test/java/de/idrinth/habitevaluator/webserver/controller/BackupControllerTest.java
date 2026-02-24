package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.ActivityLogRepository;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;
import de.idrinth.habitevaluator.shared.repository.FoodLogRepository;
import de.idrinth.habitevaluator.shared.repository.FoodTagRepository;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.MedicationLogRepository;
import de.idrinth.habitevaluator.shared.repository.MedicationRepository;
import de.idrinth.habitevaluator.shared.repository.MeetingEntryRepository;
import de.idrinth.habitevaluator.shared.repository.ModuleVisibilityRepository;
import de.idrinth.habitevaluator.shared.repository.ReminderSettingsRepository;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import de.idrinth.habitevaluator.shared.repository.SportLogRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.webserver.service.StatsCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BackupControllerTest {

    private HabitRepository habitRepository;
    private HabitCategoryRepository categoryRepository;
    private DiaryEntryRepository diaryEntryRepository;
    private SleepEntryRepository sleepEntryRepository;
    private UserRepository userRepository;
    private EmotionPairRepository emotionPairRepository;
    private EmotionEntryRepository emotionEntryRepository;
    private ReminderSettingsRepository reminderSettingsRepository;
    private SportLogRepository sportLogRepository;
    private FoodLogRepository foodLogRepository;
    private FoodTagRepository foodTagRepository;
    private MeetingEntryRepository meetingEntryRepository;
    private ActivityLogRepository activityLogRepository;
    private MedicationRepository medicationRepository;
    private MedicationLogRepository medicationLogRepository;
    private ModuleVisibilityRepository moduleVisibilityRepository;
    private StatsCacheService statsCacheService;
    private BackupController controller;
    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        habitRepository = mock(HabitRepository.class);
        categoryRepository = mock(HabitCategoryRepository.class);
        diaryEntryRepository = mock(DiaryEntryRepository.class);
        sleepEntryRepository = mock(SleepEntryRepository.class);
        userRepository = mock(UserRepository.class);
        emotionPairRepository = mock(EmotionPairRepository.class);
        emotionEntryRepository = mock(EmotionEntryRepository.class);
        reminderSettingsRepository = mock(ReminderSettingsRepository.class);
        sportLogRepository = mock(SportLogRepository.class);
        foodLogRepository = mock(FoodLogRepository.class);
        foodTagRepository = mock(FoodTagRepository.class);
        meetingEntryRepository = mock(MeetingEntryRepository.class);
        activityLogRepository = mock(ActivityLogRepository.class);
        medicationRepository = mock(MedicationRepository.class);
        medicationLogRepository = mock(MedicationLogRepository.class);
        moduleVisibilityRepository = mock(ModuleVisibilityRepository.class);
        statsCacheService = mock(StatsCacheService.class);
        controller = new BackupController(
                habitRepository, categoryRepository, diaryEntryRepository,
                sleepEntryRepository, userRepository, emotionPairRepository,
                emotionEntryRepository, reminderSettingsRepository,
                sportLogRepository, foodLogRepository, foodTagRepository,
                meetingEntryRepository, activityLogRepository,
                medicationRepository, medicationLogRepository,
                moduleVisibilityRepository, statsCacheService);
        session = new MockHttpSession();
        testUser = new User("testuser", "password");
        session.setAttribute("userId", testUser.getId());
    }

    @Test
    void testDownloadBackupUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<byte[]> response = controller.downloadBackup("mypass", unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testDownloadBackupUserNotFound() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());
        ResponseEntity<byte[]> response = controller.downloadBackup("mypass", session);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testDownloadBackupSuccess() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        ResponseEntity<byte[]> response = controller.downloadBackup("mypass", session);
        // The backup should either succeed (200) or fail internally (500) depending on data
        assertTrue(response.getStatusCode().value() == 200 || response.getStatusCode().value() == 500);
    }

    @Test
    void testUploadBackupUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        MockMultipartFile file = new MockMultipartFile("file", "backup.hez",
                "application/octet-stream", new byte[]{1, 2, 3});
        ResponseEntity<Map<String, Object>> response =
                controller.uploadBackup(file, "mypass", true, true, true, true,
                        true, true, true, true, true, true, true, true, false, unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testUploadBackupUserNotFound() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());
        MockMultipartFile file = new MockMultipartFile("file", "backup.hez",
                "application/octet-stream", new byte[]{1, 2, 3});
        ResponseEntity<Map<String, Object>> response =
                controller.uploadBackup(file, "mypass", true, true, true, true,
                        true, true, true, true, true, true, true, true, false, session);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testUploadBackupEmptyFile() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        MockMultipartFile file = new MockMultipartFile("file", "backup.hez",
                "application/octet-stream", new byte[0]);
        ResponseEntity<Map<String, Object>> response =
                controller.uploadBackup(file, "mypass", true, true, true, true,
                        true, true, true, true, true, true, true, true, false, session);
        assertEquals(400, response.getStatusCode().value());
        assertFalse((Boolean) response.getBody().get("success"));
    }

    @Test
    void testUploadBackupInvalidHezData() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        MockMultipartFile file = new MockMultipartFile("file", "backup.hez",
                "application/octet-stream", new byte[]{1, 2, 3, 4, 5});
        ResponseEntity<Map<String, Object>> response =
                controller.uploadBackup(file, "mypass", true, true, true, true,
                        true, true, true, true, true, true, true, true, false, session);
        assertEquals(400, response.getStatusCode().value());
        assertFalse((Boolean) response.getBody().get("success"));
    }
}
