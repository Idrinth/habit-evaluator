package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.backup.BackupException;
import de.idrinth.habitevaluator.shared.backup.HezBackupService;
import de.idrinth.habitevaluator.shared.backup.MergeResult;
import de.idrinth.habitevaluator.shared.backup.RestoreOptions;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.ReminderSettingsRepository;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Handles .hez backup file download and upload.
 * On upload, all user IDs in the backup data are replaced with the current user's ID.
 */
@RestController
@RequestMapping("/api/backup")
public class BackupController {

    private final HabitRepository habitRepository;
    private final HabitCategoryRepository categoryRepository;
    private final DiaryEntryRepository diaryEntryRepository;
    private final SleepEntryRepository sleepEntryRepository;
    private final UserRepository userRepository;
    private final EmotionPairRepository emotionPairRepository;
    private final EmotionEntryRepository emotionEntryRepository;
    private final ReminderSettingsRepository reminderSettingsRepository;
    private final HezBackupService hezBackupService;

    public BackupController(HabitRepository habitRepository,
                            HabitCategoryRepository categoryRepository,
                            DiaryEntryRepository diaryEntryRepository,
                            SleepEntryRepository sleepEntryRepository,
                            UserRepository userRepository,
                            EmotionPairRepository emotionPairRepository,
                            EmotionEntryRepository emotionEntryRepository,
                            ReminderSettingsRepository reminderSettingsRepository) {
        this.habitRepository = habitRepository;
        this.categoryRepository = categoryRepository;
        this.diaryEntryRepository = diaryEntryRepository;
        this.sleepEntryRepository = sleepEntryRepository;
        this.userRepository = userRepository;
        this.emotionPairRepository = emotionPairRepository;
        this.emotionEntryRepository = emotionEntryRepository;
        this.reminderSettingsRepository = reminderSettingsRepository;
        this.hezBackupService = new HezBackupService();
    }

    @GetMapping
    public ResponseEntity<byte[]> downloadBackup(
            @RequestParam String password,
            HttpSession session) {

        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        User user = userOpt.get();

        try {
            byte[] hezData = hezBackupService.createHezBackup(password, user,
                    habitRepository, categoryRepository, diaryEntryRepository, sleepEntryRepository,
                    emotionPairRepository, emotionEntryRepository, reminderSettingsRepository);

            String fileName = hezBackupService.generateDefaultFilename();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(hezData);
        } catch (BackupException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> uploadBackup(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password,
            @RequestParam(value = "categories", required = false, defaultValue = "true") boolean categories,
            @RequestParam(value = "habits", required = false, defaultValue = "true") boolean habits,
            @RequestParam(value = "diary", required = false, defaultValue = "true") boolean diary,
            @RequestParam(value = "sleep", required = false, defaultValue = "true") boolean sleep,
            @RequestParam(value = "sportLogs", required = false, defaultValue = "true") boolean sportLogs,
            @RequestParam(value = "foodLogs", required = false, defaultValue = "true") boolean foodLogs,
            @RequestParam(value = "emotions", required = false, defaultValue = "true") boolean emotions,
            @RequestParam(value = "reminderSettings", required = false, defaultValue = "true") boolean reminderSettingsParam,
            HttpSession session) {

        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        User user = userOpt.get();

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "No file uploaded"));
        }

        try {
            byte[] hezData = file.getBytes();

            if (!hezBackupService.isValidHezData(hezData)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Invalid .hez file"));
            }

            RestoreOptions options = new RestoreOptions();
            options.setRestoreCategories(categories);
            options.setRestoreHabits(habits);
            options.setRestoreDiaryEntries(diary);
            options.setRestoreSleepEntries(sleep);
            options.setRestoreSportLogs(sportLogs);
            options.setRestoreFoodLogs(foodLogs);
            options.setRestoreEmotionData(emotions);
            options.setRestoreReminderSettings(reminderSettingsParam);

            MergeResult result = hezBackupService.mergeFromHezBytes(hezData, password, user,
                    habitRepository, categoryRepository, diaryEntryRepository, sleepEntryRepository,
                    emotionPairRepository, emotionEntryRepository, reminderSettingsRepository,
                    options);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Backup restored successfully");
            response.put("categoriesAdded", result.getCategoriesAdded());
            response.put("habitsAdded", result.getHabitsAdded());
            response.put("habitsMerged", result.getHabitsMerged());
            response.put("entriesAdded", result.getEntriesAdded());
            response.put("diaryEntriesAdded", result.getDiaryEntriesAdded());
            response.put("sleepEntriesAdded", result.getSleepEntriesAdded());
            response.put("sportLogsAdded", result.getSportLogsAdded());
            response.put("foodLogsAdded", result.getFoodLogsAdded());
            response.put("emotionPairsAdded", result.getEmotionPairsAdded());
            response.put("emotionEntriesAdded", result.getEmotionEntriesAdded());
            response.put("reminderSettingsRestored", result.isReminderSettingsRestored());
            return ResponseEntity.ok(response);
        } catch (BackupException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message",
                            "Restore failed: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "Unexpected error during restore"));
        }
    }
}
