package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.backup.BackupException;
import de.idrinth.habitevaluator.shared.backup.HezBackupService;
import de.idrinth.habitevaluator.shared.backup.MergeResult;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
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
    private final HezBackupService hezBackupService;

    public BackupController(HabitRepository habitRepository,
                            HabitCategoryRepository categoryRepository,
                            DiaryEntryRepository diaryEntryRepository,
                            SleepEntryRepository sleepEntryRepository,
                            UserRepository userRepository) {
        this.habitRepository = habitRepository;
        this.categoryRepository = categoryRepository;
        this.diaryEntryRepository = diaryEntryRepository;
        this.sleepEntryRepository = sleepEntryRepository;
        this.userRepository = userRepository;
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
                    habitRepository, categoryRepository, diaryEntryRepository, sleepEntryRepository);

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

            MergeResult result = hezBackupService.mergeFromHezBytes(hezData, password, user,
                    habitRepository, categoryRepository, diaryEntryRepository, sleepEntryRepository);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Backup restored successfully",
                    "categoriesAdded", result.getCategoriesAdded(),
                    "habitsAdded", result.getHabitsAdded(),
                    "habitsMerged", result.getHabitsMerged(),
                    "entriesAdded", result.getEntriesAdded(),
                    "diaryEntriesAdded", result.getDiaryEntriesAdded(),
                    "sleepEntriesAdded", result.getSleepEntriesAdded()
            ));
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
