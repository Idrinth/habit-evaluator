package de.idrinth.habitevaluator.shared.backup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.Medication;
import de.idrinth.habitevaluator.shared.model.MedicationLog;
import de.idrinth.habitevaluator.shared.model.MedicationProvisionType;
import de.idrinth.habitevaluator.shared.model.ReminderSettings;
import de.idrinth.habitevaluator.shared.model.ActivityLog;
import de.idrinth.habitevaluator.shared.model.ScoringRule;
import de.idrinth.habitevaluator.shared.model.MeetingEntry;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.SportLog;
import de.idrinth.habitevaluator.shared.model.FoodLog;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;
import de.idrinth.habitevaluator.shared.repository.FoodLogRepository;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.MedicationLogRepository;
import de.idrinth.habitevaluator.shared.repository.MedicationRepository;
import de.idrinth.habitevaluator.shared.repository.ActivityLogRepository;
import de.idrinth.habitevaluator.shared.repository.MeetingEntryRepository;
import de.idrinth.habitevaluator.shared.repository.ReminderSettingsRepository;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import de.idrinth.habitevaluator.shared.repository.SportLogRepository;
import de.idrinth.habitevaluator.shared.model.ModuleVisibility;
import de.idrinth.habitevaluator.shared.model.EmergencyPlanStep;
import de.idrinth.habitevaluator.shared.model.EmergencyPlanAction;
import de.idrinth.habitevaluator.shared.repository.ModuleVisibilityRepository;
import de.idrinth.habitevaluator.shared.repository.EmergencyPlanStepRepository;
import de.idrinth.habitevaluator.shared.repository.EmergencyPlanActionRepository;
import de.idrinth.habitevaluator.shared.model.PlannerGroup;
import de.idrinth.habitevaluator.shared.model.PlannerActivity;
import de.idrinth.habitevaluator.shared.model.WeekPlannerSlot;
import de.idrinth.habitevaluator.shared.model.SlotConfirmation;
import de.idrinth.habitevaluator.shared.repository.PlannerGroupRepository;
import de.idrinth.habitevaluator.shared.repository.PlannerActivityRepository;
import de.idrinth.habitevaluator.shared.repository.WeekPlannerSlotRepository;
import de.idrinth.habitevaluator.shared.repository.SlotConfirmationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.idrinth.habitevaluator.shared.model.EventSignificance;
import de.idrinth.habitevaluator.shared.model.FrequencyType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service that creates and restores encrypted daily backups of user data.
 * Backups are stored as encrypted files in a configurable directory.
 * Old backups beyond the retention period are automatically cleaned up.
 */
public class BackupService {

    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);
    private static final String BACKUP_FILE_EXTENSION = ".backup";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int MAX_BACKUPS = 30;

    private final BackupEncryptionService encryptionService;
    private final Gson gson;

    public BackupService() {
        this.encryptionService = new BackupEncryptionService();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Creates an encrypted backup of all user data and writes it to the backup directory.
     * Only one backup per day is kept; calling this again on the same day overwrites the previous.
     *
     * @param backupDir           the directory to store backups
     * @param password            the encryption password
     * @param user                the current user
     * @param habitRepository     habit data source
     * @param categoryRepository  category data source (may be null)
     * @param diaryEntryRepository diary entry data source (may be null)
     * @param sleepEntryRepository sleep entry data source (may be null)
     * @throws BackupException if backup creation fails
     */
    public void createBackup(File backupDir, String password, User user,
                             HabitRepository habitRepository,
                             HabitCategoryRepository categoryRepository,
                             DiaryEntryRepository diaryEntryRepository,
                             SleepEntryRepository sleepEntryRepository) throws BackupException {
        if (password == null || password.isEmpty()) {
            throw new BackupException("Backup password must not be empty");
        }
        if (user == null) {
            throw new BackupException("User must not be null for backup");
        }

        try {
            if (!backupDir.exists() && !backupDir.mkdirs()) {
                throw new BackupException("Failed to create backup directory: " + backupDir.getAbsolutePath());
            }

            BackupData backupData = collectBackupData(user, habitRepository,
                    categoryRepository, diaryEntryRepository, sleepEntryRepository);

            String json = gson.toJson(backupData);
            byte[] plaintext = json.getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = encryptionService.encrypt(plaintext, password);

            String fileName = LocalDate.now().format(DATE_FORMAT) + BACKUP_FILE_EXTENSION;
            File backupFile = new File(backupDir, fileName);
            writeBytes(backupFile, encrypted);

            logger.info("Backup created successfully: {}", backupFile.getName());

            cleanupOldBackups(backupDir);
        } catch (BackupException e) {
            throw e;
        } catch (Exception e) {
            throw new BackupException("Failed to create backup", e);
        }
    }

    /**
     * Creates an encrypted backup of all user data and returns it as a byte array.
     * This can be used to write the backup to non-file destinations (e.g. SAF URIs).
     *
     * @param password            the encryption password
     * @param user                the current user
     * @param habitRepository     habit data source
     * @param categoryRepository  category data source (may be null)
     * @param diaryEntryRepository diary entry data source (may be null)
     * @param sleepEntryRepository sleep entry data source (may be null)
     * @return the encrypted backup data
     * @throws BackupException if backup creation fails
     */
    public byte[] createBackupBytes(String password, User user,
                                    HabitRepository habitRepository,
                                    HabitCategoryRepository categoryRepository,
                                    DiaryEntryRepository diaryEntryRepository,
                                    SleepEntryRepository sleepEntryRepository) throws BackupException {
        if (password == null || password.isEmpty()) {
            throw new BackupException("Backup password must not be empty");
        }
        if (user == null) {
            throw new BackupException("User must not be null for backup");
        }

        try {
            BackupData backupData = collectBackupData(user, habitRepository,
                    categoryRepository, diaryEntryRepository, sleepEntryRepository);

            String json = gson.toJson(backupData);
            byte[] plaintext = json.getBytes(StandardCharsets.UTF_8);
            return encryptionService.encrypt(plaintext, password);
        } catch (BackupException e) {
            throw e;
        } catch (Exception e) {
            throw new BackupException("Failed to create backup", e);
        }
    }

    /**
     * Restores user data from an encrypted backup file.
     *
     * @param backupFile the encrypted backup file
     * @param password   the encryption password
     * @return the deserialized backup data
     * @throws BackupException if restore fails
     */
    public BackupData restoreBackup(File backupFile, String password) throws BackupException {
        if (password == null || password.isEmpty()) {
            throw new BackupException("Backup password must not be empty");
        }
        if (!backupFile.exists()) {
            throw new BackupException("Backup file does not exist: " + backupFile.getAbsolutePath());
        }

        try {
            byte[] encrypted = readBytes(backupFile);
            byte[] plaintext = encryptionService.decrypt(encrypted, password);
            String json = new String(plaintext, StandardCharsets.UTF_8);
            return gson.fromJson(json, BackupData.class);
        } catch (BackupException e) {
            throw e;
        } catch (Exception e) {
            throw new BackupException("Failed to restore backup", e);
        }
    }

    /**
     * Restores user data from an encrypted backup provided as an InputStream.
     * This can be used to restore from non-file sources (e.g. SAF URIs).
     *
     * @param inputStream the input stream containing encrypted backup data
     * @param password    the encryption password
     * @return the deserialized backup data
     * @throws BackupException if restore fails
     */
    public BackupData restoreBackupFromStream(InputStream inputStream, String password) throws BackupException {
        if (password == null || password.isEmpty()) {
            throw new BackupException("Backup password must not be empty");
        }
        if (inputStream == null) {
            throw new BackupException("Input stream must not be null");
        }

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(chunk)) != -1) {
                buffer.write(chunk, 0, bytesRead);
            }
            byte[] encrypted = buffer.toByteArray();
            byte[] plaintext = encryptionService.decrypt(encrypted, password);
            String json = new String(plaintext, StandardCharsets.UTF_8);
            return gson.fromJson(json, BackupData.class);
        } catch (BackupException e) {
            throw e;
        } catch (Exception e) {
            throw new BackupException("Failed to restore backup from stream", e);
        }
    }

    /**
     * Merges data from a backup stream into the current user's existing data.
     *
     * @param inputStream          the input stream containing encrypted backup data
     * @param password             the encryption password
     * @param user                 the current user
     * @param habitRepository      habit data source
     * @param categoryRepository   category data source (may be null)
     * @param diaryEntryRepository diary entry data source (may be null)
     * @param sleepEntryRepository sleep entry data source (may be null)
     * @param options              selective restore options
     * @return a summary of what was merged
     * @throws BackupException if merge fails
     */
    public MergeResult mergeBackupFromStream(InputStream inputStream, String password, User user,
                                             HabitRepository habitRepository,
                                             HabitCategoryRepository categoryRepository,
                                             DiaryEntryRepository diaryEntryRepository,
                                             SleepEntryRepository sleepEntryRepository,
                                             RestoreOptions options) throws BackupException {
        BackupData backupData = restoreBackupFromStream(inputStream, password);
        return mergeBackupData(backupData, user, habitRepository, categoryRepository,
                diaryEntryRepository, sleepEntryRepository, null, null, options);
    }

    /**
     * Returns the expected backup filename for today.
     *
     * @return the backup filename for today (e.g. "2026-02-19.backup")
     */
    public String getTodaysBackupFilename() {
        return LocalDate.now().format(DATE_FORMAT) + BACKUP_FILE_EXTENSION;
    }

    /**
     * Merges data from a backup into the current user's existing data.
     * Categories are matched by name; new ones are created.
     * Habits are matched by name and category; new ones are created, existing ones get missing entries merged.
     * Diary entries and sleep entries are matched by ID; new ones are added.
     *
     * @param backupFile           the encrypted backup file
     * @param password             the encryption password
     * @param user                 the current user
     * @param habitRepository      habit data source
     * @param categoryRepository   category data source (may be null)
     * @param diaryEntryRepository diary entry data source (may be null)
     * @param sleepEntryRepository sleep entry data source (may be null)
     * @return a summary of what was merged
     * @throws BackupException if merge fails
     */
    public MergeResult mergeBackup(File backupFile, String password, User user,
                                   HabitRepository habitRepository,
                                   HabitCategoryRepository categoryRepository,
                                   DiaryEntryRepository diaryEntryRepository,
                                   SleepEntryRepository sleepEntryRepository) throws BackupException {
        return mergeBackup(backupFile, password, user, habitRepository, categoryRepository,
                diaryEntryRepository, sleepEntryRepository, RestoreOptions.all());
    }

    public MergeResult mergeBackup(File backupFile, String password, User user,
                                   HabitRepository habitRepository,
                                   HabitCategoryRepository categoryRepository,
                                   DiaryEntryRepository diaryEntryRepository,
                                   SleepEntryRepository sleepEntryRepository,
                                   RestoreOptions options) throws BackupException {
        BackupData backupData = restoreBackup(backupFile, password);
        return mergeBackupData(backupData, user, habitRepository, categoryRepository,
                diaryEntryRepository, sleepEntryRepository, null, null, options);
    }

    /**
     * Merges data from a BackupData object into the current user's existing data.
     * Categories are matched by name; new ones are created.
     * Habits are matched by name and category; new ones are created, existing ones get missing entries merged.
     * Diary entries and sleep entries are matched by ID; new ones are added.
     *
     * @param backupData           the backup data to merge
     * @param user                 the current user
     * @param habitRepository      habit data source
     * @param categoryRepository   category data source (may be null)
     * @param diaryEntryRepository diary entry data source (may be null)
     * @param sleepEntryRepository sleep entry data source (may be null)
     * @return a summary of what was merged
     * @throws BackupException if merge fails
     */
    public MergeResult mergeBackupData(BackupData backupData, User user,
                                       HabitRepository habitRepository,
                                       HabitCategoryRepository categoryRepository,
                                       DiaryEntryRepository diaryEntryRepository,
                                       SleepEntryRepository sleepEntryRepository) throws BackupException {
        return mergeBackupData(backupData, user, habitRepository, categoryRepository,
                diaryEntryRepository, sleepEntryRepository, null);
    }

    public MergeResult mergeBackupData(BackupData backupData, User user,
                                       HabitRepository habitRepository,
                                       HabitCategoryRepository categoryRepository,
                                       DiaryEntryRepository diaryEntryRepository,
                                       SleepEntryRepository sleepEntryRepository,
                                       SportLogRepository sportLogRepository) throws BackupException {
        return mergeBackupData(backupData, user, habitRepository, categoryRepository,
                diaryEntryRepository, sleepEntryRepository, sportLogRepository, null);
    }

    public MergeResult mergeBackupData(BackupData backupData, User user,
                                       HabitRepository habitRepository,
                                       HabitCategoryRepository categoryRepository,
                                       DiaryEntryRepository diaryEntryRepository,
                                       SleepEntryRepository sleepEntryRepository,
                                       SportLogRepository sportLogRepository,
                                       FoodLogRepository foodLogRepository) throws BackupException {
        return mergeBackupData(backupData, user, habitRepository, categoryRepository,
                diaryEntryRepository, sleepEntryRepository, sportLogRepository, foodLogRepository,
                null, RestoreOptions.all());
    }

    public MergeResult mergeBackupData(BackupData backupData, User user,
                                       HabitRepository habitRepository,
                                       HabitCategoryRepository categoryRepository,
                                       DiaryEntryRepository diaryEntryRepository,
                                       SleepEntryRepository sleepEntryRepository,
                                       SportLogRepository sportLogRepository,
                                       FoodLogRepository foodLogRepository,
                                       RestoreOptions options) throws BackupException {
        return mergeBackupData(backupData, user, habitRepository, categoryRepository,
                diaryEntryRepository, sleepEntryRepository, sportLogRepository, foodLogRepository,
                null, options);
    }

    public MergeResult mergeBackupData(BackupData backupData, User user,
                                       HabitRepository habitRepository,
                                       HabitCategoryRepository categoryRepository,
                                       DiaryEntryRepository diaryEntryRepository,
                                       SleepEntryRepository sleepEntryRepository,
                                       SportLogRepository sportLogRepository,
                                       FoodLogRepository foodLogRepository,
                                       de.idrinth.habitevaluator.shared.repository.FoodTagRepository foodTagRepository,
                                       RestoreOptions options) throws BackupException {
        if (user == null) {
            throw new BackupException("User must not be null for merge");
        }
        if (options == null) {
            options = RestoreOptions.all();
        }

        try {
            return doMerge(backupData, user, habitRepository, categoryRepository,
                    diaryEntryRepository, sleepEntryRepository, sportLogRepository,
                    foodLogRepository, foodTagRepository, options);
        } catch (BackupException e) {
            throw e;
        } catch (Exception e) {
            throw new BackupException("Failed to merge backup data", e);
        }
    }

    public MergeResult mergeBackupData(BackupData backupData, User user,
                                       HabitRepository habitRepository,
                                       HabitCategoryRepository categoryRepository,
                                       DiaryEntryRepository diaryEntryRepository,
                                       SleepEntryRepository sleepEntryRepository,
                                       SportLogRepository sportLogRepository,
                                       FoodLogRepository foodLogRepository,
                                       de.idrinth.habitevaluator.shared.repository.FoodTagRepository foodTagRepository,
                                       EmotionPairRepository emotionPairRepository,
                                       EmotionEntryRepository emotionEntryRepository,
                                       ReminderSettingsRepository reminderSettingsRepository,
                                       RestoreOptions options) throws BackupException {
        if (user == null) {
            throw new BackupException("User must not be null for merge");
        }
        if (options == null) {
            options = RestoreOptions.all();
        }

        try {
            return doMerge(backupData, user, habitRepository, categoryRepository,
                    diaryEntryRepository, sleepEntryRepository, sportLogRepository,
                    foodLogRepository, foodTagRepository, emotionPairRepository,
                    emotionEntryRepository, reminderSettingsRepository, options);
        } catch (BackupException e) {
            throw e;
        } catch (Exception e) {
            throw new BackupException("Failed to merge backup data", e);
        }
    }

    public MergeResult mergeBackupData(BackupData backupData, User user,
                                       HabitRepository habitRepository,
                                       HabitCategoryRepository categoryRepository,
                                       DiaryEntryRepository diaryEntryRepository,
                                       SleepEntryRepository sleepEntryRepository,
                                       SportLogRepository sportLogRepository,
                                       FoodLogRepository foodLogRepository,
                                       de.idrinth.habitevaluator.shared.repository.FoodTagRepository foodTagRepository,
                                       EmotionPairRepository emotionPairRepository,
                                       EmotionEntryRepository emotionEntryRepository,
                                       ReminderSettingsRepository reminderSettingsRepository,
                                       MedicationRepository medicationRepository,
                                       MedicationLogRepository medicationLogRepository,
                                       RestoreOptions options) throws BackupException {
        return mergeBackupData(backupData, user, habitRepository, categoryRepository,
                diaryEntryRepository, sleepEntryRepository, sportLogRepository,
                foodLogRepository, foodTagRepository, emotionPairRepository,
                emotionEntryRepository, null, null, reminderSettingsRepository,
                medicationRepository, medicationLogRepository, null, null, null, options);
    }

    public MergeResult mergeBackupData(BackupData backupData, User user,
                                       HabitRepository habitRepository,
                                       HabitCategoryRepository categoryRepository,
                                       DiaryEntryRepository diaryEntryRepository,
                                       SleepEntryRepository sleepEntryRepository,
                                       SportLogRepository sportLogRepository,
                                       FoodLogRepository foodLogRepository,
                                       de.idrinth.habitevaluator.shared.repository.FoodTagRepository foodTagRepository,
                                       EmotionPairRepository emotionPairRepository,
                                       EmotionEntryRepository emotionEntryRepository,
                                       MeetingEntryRepository meetingEntryRepository,
                                       ActivityLogRepository activityLogRepository,
                                       ReminderSettingsRepository reminderSettingsRepository,
                                       MedicationRepository medicationRepository,
                                       MedicationLogRepository medicationLogRepository,
                                       ModuleVisibilityRepository moduleVisibilityRepository,
                                       EmergencyPlanStepRepository emergencyPlanStepRepository,
                                       EmergencyPlanActionRepository emergencyPlanActionRepository,
                                       RestoreOptions options) throws BackupException {
        if (user == null) {
            throw new BackupException("User must not be null for merge");
        }
        if (options == null) {
            options = RestoreOptions.all();
        }

        try {
            return doMerge(backupData, user, habitRepository, categoryRepository,
                    diaryEntryRepository, sleepEntryRepository, sportLogRepository,
                    foodLogRepository, foodTagRepository, emotionPairRepository,
                    emotionEntryRepository, meetingEntryRepository, activityLogRepository,
                    reminderSettingsRepository, medicationRepository, medicationLogRepository,
                    moduleVisibilityRepository, emergencyPlanStepRepository,
                    emergencyPlanActionRepository, options);
        } catch (BackupException e) {
            throw e;
        } catch (Exception e) {
            throw new BackupException("Failed to merge backup data", e);
        }
    }

    private MergeResult doMerge(BackupData backupData, User user,
                                HabitRepository habitRepository,
                                HabitCategoryRepository categoryRepository,
                                DiaryEntryRepository diaryEntryRepository,
                                SleepEntryRepository sleepEntryRepository,
                                SportLogRepository sportLogRepository,
                                FoodLogRepository foodLogRepository,
                                de.idrinth.habitevaluator.shared.repository.FoodTagRepository foodTagRepository,
                                RestoreOptions options) throws BackupException {
        return doMerge(backupData, user, habitRepository, categoryRepository,
                diaryEntryRepository, sleepEntryRepository, sportLogRepository,
                foodLogRepository, foodTagRepository, null, null, null, null, null, null, null, null, null, null, options);
    }

    private MergeResult doMerge(BackupData backupData, User user,
                                HabitRepository habitRepository,
                                HabitCategoryRepository categoryRepository,
                                DiaryEntryRepository diaryEntryRepository,
                                SleepEntryRepository sleepEntryRepository,
                                SportLogRepository sportLogRepository,
                                FoodLogRepository foodLogRepository,
                                de.idrinth.habitevaluator.shared.repository.FoodTagRepository foodTagRepository,
                                EmotionPairRepository emotionPairRepository,
                                EmotionEntryRepository emotionEntryRepository,
                                ReminderSettingsRepository reminderSettingsRepository,
                                RestoreOptions options) throws BackupException {
        return doMerge(backupData, user, habitRepository, categoryRepository,
                diaryEntryRepository, sleepEntryRepository, sportLogRepository,
                foodLogRepository, foodTagRepository, emotionPairRepository,
                emotionEntryRepository, null, null, reminderSettingsRepository, null, null, null, null, null, options);
    }

    private MergeResult doMerge(BackupData backupData, User user,
                                HabitRepository habitRepository,
                                HabitCategoryRepository categoryRepository,
                                DiaryEntryRepository diaryEntryRepository,
                                SleepEntryRepository sleepEntryRepository,
                                SportLogRepository sportLogRepository,
                                FoodLogRepository foodLogRepository,
                                de.idrinth.habitevaluator.shared.repository.FoodTagRepository foodTagRepository,
                                EmotionPairRepository emotionPairRepository,
                                EmotionEntryRepository emotionEntryRepository,
                                MeetingEntryRepository meetingEntryRepository,
                                ActivityLogRepository activityLogRepository,
                                ReminderSettingsRepository reminderSettingsRepository,
                                MedicationRepository medicationRepository,
                                MedicationLogRepository medicationLogRepository,
                                RestoreOptions options) throws BackupException {
        return doMerge(backupData, user, habitRepository, categoryRepository,
                diaryEntryRepository, sleepEntryRepository, sportLogRepository,
                foodLogRepository, foodTagRepository, emotionPairRepository,
                emotionEntryRepository, meetingEntryRepository, activityLogRepository,
                reminderSettingsRepository, medicationRepository, medicationLogRepository,
                null, null, null, options);
    }

    private MergeResult doMerge(BackupData backupData, User user,
                                HabitRepository habitRepository,
                                HabitCategoryRepository categoryRepository,
                                DiaryEntryRepository diaryEntryRepository,
                                SleepEntryRepository sleepEntryRepository,
                                SportLogRepository sportLogRepository,
                                FoodLogRepository foodLogRepository,
                                de.idrinth.habitevaluator.shared.repository.FoodTagRepository foodTagRepository,
                                EmotionPairRepository emotionPairRepository,
                                EmotionEntryRepository emotionEntryRepository,
                                MeetingEntryRepository meetingEntryRepository,
                                ActivityLogRepository activityLogRepository,
                                ReminderSettingsRepository reminderSettingsRepository,
                                MedicationRepository medicationRepository,
                                MedicationLogRepository medicationLogRepository,
                                ModuleVisibilityRepository moduleVisibilityRepository,
                                EmergencyPlanStepRepository emergencyPlanStepRepository,
                                EmergencyPlanActionRepository emergencyPlanActionRepository,
                                RestoreOptions options) throws BackupException {
        int categoriesAdded = 0;
        int habitsAdded = 0;
        int habitsMerged = 0;
        int entriesAdded = 0;
        int diaryEntriesAdded = 0;
        int sleepEntriesAdded = 0;

        // When overwrite is enabled, clear existing data before restoring.
        // Deletion order respects referential integrity (children before parents).
        if (options.isOverwrite()) {
            String userId = user.getId();
            if (options.isRestoreEmergencyPlan() && emergencyPlanStepRepository != null
                    && emergencyPlanActionRepository != null) {
                List<EmergencyPlanStep> steps = emergencyPlanStepRepository.findByUserId(userId);
                for (EmergencyPlanStep step : steps) {
                    List<EmergencyPlanAction> actions = emergencyPlanActionRepository.findByStepId(step.getId());
                    for (EmergencyPlanAction action : actions) {
                        emergencyPlanActionRepository.deleteById(action.getId());
                    }
                    emergencyPlanStepRepository.deleteById(step.getId());
                }
            }
            if (options.isRestoreMedicationData() && medicationLogRepository != null) {
                for (MedicationLog log : medicationLogRepository.findByUserId(userId)) {
                    medicationLogRepository.deleteById(log.getId());
                }
            }
            if (options.isRestoreMedicationData() && medicationRepository != null) {
                for (Medication med : medicationRepository.findByUserId(userId)) {
                    medicationRepository.deleteById(med.getId());
                }
            }
            if (options.isRestoreEmotionData() && emotionEntryRepository != null) {
                for (EmotionEntry entry : emotionEntryRepository.findByUserId(userId)) {
                    emotionEntryRepository.deleteById(entry.getId());
                }
            }
            if (options.isRestoreEmotionData() && emotionPairRepository != null) {
                for (EmotionPair pair : emotionPairRepository.findByUserId(userId)) {
                    emotionPairRepository.deleteById(pair.getId());
                }
            }
            if (options.isRestoreHabits() && habitRepository != null) {
                for (Habit habit : habitRepository.findByUserId(userId)) {
                    habitRepository.deleteById(habit.getId());
                }
            }
            if (options.isRestoreCategories() && categoryRepository != null) {
                for (HabitCategory cat : categoryRepository.findByUserId(userId)) {
                    categoryRepository.deleteById(cat.getId());
                }
            }
            if (options.isRestoreDiaryEntries() && diaryEntryRepository != null) {
                for (DiaryEntry entry : diaryEntryRepository.findByUserId(userId)) {
                    diaryEntryRepository.deleteById(entry.getId());
                }
            }
            if (options.isRestoreSleepEntries() && sleepEntryRepository != null) {
                for (SleepEntry entry : sleepEntryRepository.findByUserId(userId)) {
                    sleepEntryRepository.deleteById(entry.getId());
                }
            }
            if (options.isRestoreSportLogs() && sportLogRepository != null) {
                for (SportLog entry : sportLogRepository.findByUserId(userId)) {
                    sportLogRepository.deleteById(entry.getId());
                }
            }
            if (options.isRestoreFoodLogs() && foodLogRepository != null) {
                for (FoodLog entry : foodLogRepository.findByUserId(userId)) {
                    foodLogRepository.deleteById(entry.getId());
                }
            }
            if (options.isRestoreFoodLogs() && foodTagRepository != null) {
                for (de.idrinth.habitevaluator.shared.model.FoodTag tag : foodTagRepository.findByUserId(userId)) {
                    foodTagRepository.deleteById(tag.getId());
                }
            }
            if (options.isRestoreMeetingEntries() && meetingEntryRepository != null) {
                for (MeetingEntry entry : meetingEntryRepository.findByUserId(userId)) {
                    meetingEntryRepository.deleteById(entry.getId());
                }
            }
            if (options.isRestoreActivityLogs() && activityLogRepository != null) {
                for (ActivityLog entry : activityLogRepository.findByUserId(userId)) {
                    activityLogRepository.deleteById(entry.getId());
                }
            }
            if (options.isRestoreReminderSettings() && reminderSettingsRepository != null) {
                reminderSettingsRepository.deleteByUserId(userId);
            }
            if (options.isRestoreModuleVisibility() && moduleVisibilityRepository != null) {
                moduleVisibilityRepository.deleteByUserId(userId);
            }
            logger.info("Overwrite mode: cleared existing data for user {}", userId);
        }

        // Build a mapping from backup category IDs to local category IDs.
        // Categories are processed when restoring categories or habits (habits need category mapping).
        Map<String, String> categoryIdMapping = new HashMap<>();
        if ((options.isRestoreCategories() || options.isRestoreHabits())
                && categoryRepository != null && backupData.getCategories() != null) {
            List<HabitCategory> existingCategories = categoryRepository.findByUserId(user.getId());
            Map<String, HabitCategory> existingByName = new HashMap<>();
            for (HabitCategory cat : existingCategories) {
                existingByName.put(cat.getName(), cat);
            }

            for (BackupData.CategoryData catData : backupData.getCategories()) {
                HabitCategory existing = existingByName.get(catData.getName());
                if (existing != null) {
                    categoryIdMapping.put(catData.getId(), existing.getId());
                } else {
                    HabitCategory newCat = new HabitCategory(catData.getName(),
                            catData.getDescription(), catData.getColor());
                    newCat.setUser(user);
                    newCat = categoryRepository.save(newCat);
                    categoryIdMapping.put(catData.getId(), newCat.getId());
                    categoriesAdded++;
                }
            }
        }

        // Merge habits
        if (options.isRestoreHabits() && habitRepository != null && backupData.getHabits() != null) {
            List<Habit> existingHabits = habitRepository.findByUserId(user.getId());
            Map<String, Habit> existingByNameAndCategory = new HashMap<>();
            for (Habit habit : existingHabits) {
                String key = habit.getName() + "|" + (habit.getCategoryId() != null ? habit.getCategoryId() : "");
                existingByNameAndCategory.put(key, habit);
            }

            for (BackupData.HabitData habitData : backupData.getHabits()) {
                String mappedCategoryId = categoryIdMapping.get(habitData.getCategoryId());
                if (mappedCategoryId == null) {
                    mappedCategoryId = habitData.getCategoryId();
                }
                String key = habitData.getName() + "|" + (mappedCategoryId != null ? mappedCategoryId : "");
                Habit existingHabit = existingByNameAndCategory.get(key);

                if (existingHabit != null) {
                    // Merge entries into existing habit
                    Set<String> existingEntryIds = new HashSet<>();
                    List<HabitEntry> existingEntries = existingHabit.getEntries();
                    if (existingEntries != null) {
                        for (HabitEntry entry : existingEntries) {
                            existingEntryIds.add(entry.getId());
                        }
                    }
                    int addedForThisHabit = 0;
                    List<BackupData.HabitEntryData> backupEntries = habitData.getEntries();
                    if (backupEntries != null) {
                        for (BackupData.HabitEntryData entryData : backupEntries) {
                            if (!existingEntryIds.contains(entryData.getId())) {
                                HabitEntry newEntry = new HabitEntry();
                                newEntry.setId(entryData.getId());
                                if (entryData.getCompletedAt() != null) {
                                    newEntry.setCompletedAt(LocalDateTime.parse(entryData.getCompletedAt()));
                                }
                                newEntry.setNotes(entryData.getNotes());
                                newEntry.setValue(entryData.getValue());
                                existingHabit.addEntry(newEntry);
                                addedForThisHabit++;
                            }
                        }
                    }
                    if (addedForThisHabit > 0) {
                        habitRepository.save(existingHabit);
                        entriesAdded += addedForThisHabit;
                        habitsMerged++;
                    }
                } else {
                    // Create new habit from backup
                    Habit newHabit = new Habit(habitData.getName(), habitData.getDescription());
                    newHabit.setCategoryId(mappedCategoryId);
                    newHabit.setUser(user);
                    if (habitData.getFrequencyType() != null) {
                        newHabit.setFrequencyType(FrequencyType.valueOf(habitData.getFrequencyType()));
                    }
                    newHabit.setTargetFrequency(habitData.getTargetFrequency());
                    newHabit.setMaxEntriesPerDay(habitData.getMaxEntriesPerDay());
                    newHabit.setPositiveScoring(habitData.isPositiveScoring());
                    if (habitData.getCreatedAt() != null) {
                        newHabit.setCreatedAt(LocalDateTime.parse(habitData.getCreatedAt()));
                    }
                    if (habitData.getNameTranslations() != null) {
                        newHabit.setNameTranslations(habitData.getNameTranslations());
                    }
                    if (habitData.getDescriptionTranslations() != null) {
                        newHabit.setDescriptionTranslations(habitData.getDescriptionTranslations());
                    }

                    BackupData.ScoringRuleData ruleData = habitData.getScoringRule();
                    if (ruleData != null) {
                        ScoringRule rule = new ScoringRule(
                                ruleData.getName() != null ? ruleData.getName() : "custom",
                                ruleData.getThresholdFor1Point(),
                                ruleData.getThresholdFor2Points(),
                                ruleData.getThresholdFor4Points(),
                                ruleData.getThresholdFor8Points()
                        );
                        rule.setUser(user);
                        newHabit.setScoringRule(rule);
                    }

                    List<BackupData.HabitEntryData> newHabitEntries = habitData.getEntries();
                    if (newHabitEntries != null) {
                        for (BackupData.HabitEntryData entryData : newHabitEntries) {
                            HabitEntry newEntry = new HabitEntry();
                            newEntry.setId(entryData.getId());
                            if (entryData.getCompletedAt() != null) {
                                newEntry.setCompletedAt(LocalDateTime.parse(entryData.getCompletedAt()));
                            }
                            newEntry.setNotes(entryData.getNotes());
                            newEntry.setValue(entryData.getValue());
                            newHabit.addEntry(newEntry);
                            entriesAdded++;
                        }
                    }

                    habitRepository.save(newHabit);
                    habitsAdded++;
                }
            }
        }

        // Merge diary entries
        if (options.isRestoreDiaryEntries() && diaryEntryRepository != null && backupData.getDiaryEntries() != null) {
            List<DiaryEntry> existingDiaryEntries = diaryEntryRepository.findByUserId(user.getId());
            Set<String> existingDiaryIds = new HashSet<>();
            for (DiaryEntry entry : existingDiaryEntries) {
                existingDiaryIds.add(entry.getId());
            }

            for (BackupData.DiaryEntryData entryData : backupData.getDiaryEntries()) {
                if (!existingDiaryIds.contains(entryData.getId())) {
                    EventSignificance significance = EventSignificance.NORMAL;
                    if (entryData.getSignificance() != null) {
                        significance = EventSignificance.valueOf(entryData.getSignificance());
                    }
                    LocalDate eventDate = LocalDate.now();
                    if (entryData.getEventDate() != null) {
                        eventDate = LocalDate.parse(entryData.getEventDate());
                    }
                    DiaryEntry newEntry = new DiaryEntry(entryData.getDescription(), significance, eventDate);
                    newEntry.setId(entryData.getId());
                    if (entryData.getCreatedAt() != null) {
                        newEntry.setCreatedAt(LocalDateTime.parse(entryData.getCreatedAt()));
                    }
                    if (entryData.getStartTime() != null) {
                        newEntry.setStartTime(LocalTime.parse(entryData.getStartTime()));
                    }
                    if (entryData.getEndTime() != null) {
                        newEntry.setEndTime(LocalTime.parse(entryData.getEndTime()));
                    }
                    newEntry.setUser(user);
                    diaryEntryRepository.save(newEntry);
                    diaryEntriesAdded++;
                }
            }
        }

        // Merge sleep entries
        if (options.isRestoreSleepEntries() && sleepEntryRepository != null && backupData.getSleepEntries() != null) {
            List<SleepEntry> existingSleepEntries = sleepEntryRepository.findByUserId(user.getId());
            Set<String> existingSleepIds = new HashSet<>();
            for (SleepEntry entry : existingSleepEntries) {
                existingSleepIds.add(entry.getId());
            }

            for (BackupData.SleepEntryData entryData : backupData.getSleepEntries()) {
                if (!existingSleepIds.contains(entryData.getId())) {
                    SleepEntry newEntry = new SleepEntry();
                    newEntry.setId(entryData.getId());
                    if (entryData.getFromTime() != null) {
                        newEntry.setFromTime(LocalTime.parse(entryData.getFromTime()));
                    }
                    if (entryData.getUntilTime() != null) {
                        newEntry.setUntilTime(LocalTime.parse(entryData.getUntilTime()));
                    }
                    if (entryData.getDate() != null) {
                        newEntry.setDate(LocalDate.parse(entryData.getDate()));
                    }
                    newEntry.setNotes(entryData.getNotes());
                    if (entryData.getCreatedAt() != null) {
                        newEntry.setCreatedAt(LocalDateTime.parse(entryData.getCreatedAt()));
                    }
                    newEntry.setUser(user);
                    sleepEntryRepository.save(newEntry);
                    sleepEntriesAdded++;
                }
            }
        }

        // Merge sport logs
        int sportLogsAdded = 0;
        if (options.isRestoreSportLogs() && sportLogRepository != null && backupData.getSportLogs() != null) {
            List<SportLog> existingSportLogs = sportLogRepository.findByUserId(user.getId());
            Set<String> existingSportLogIds = new HashSet<>();
            for (SportLog entry : existingSportLogs) {
                existingSportLogIds.add(entry.getId());
            }

            for (BackupData.SportLogData entryData : backupData.getSportLogs()) {
                if (!existingSportLogIds.contains(entryData.getId())) {
                    SportLog newEntry = new SportLog();
                    newEntry.setId(entryData.getId());
                    newEntry.setName(entryData.getName());
                    newEntry.setMeasurement(entryData.getMeasurement());
                    newEntry.setMeasurementUnit(entryData.getMeasurementUnit());
                    if (entryData.getStartTime() != null) {
                        newEntry.setStartTime(LocalTime.parse(entryData.getStartTime()));
                    }
                    if (entryData.getEndTime() != null) {
                        newEntry.setEndTime(LocalTime.parse(entryData.getEndTime()));
                    }
                    if (entryData.getDate() != null) {
                        newEntry.setDate(LocalDate.parse(entryData.getDate()));
                    }
                    newEntry.setNotes(entryData.getNotes());
                    if (entryData.getCreatedAt() != null) {
                        newEntry.setCreatedAt(LocalDateTime.parse(entryData.getCreatedAt()));
                    }
                    newEntry.setUser(user);
                    sportLogRepository.save(newEntry);
                    sportLogsAdded++;
                }
            }
        }

        // Merge food logs
        int foodLogsAdded = 0;
        if (options.isRestoreFoodLogs() && foodLogRepository != null && backupData.getFoodLogs() != null) {
            List<FoodLog> existingFoodLogs = foodLogRepository.findByUserId(user.getId());
            Set<String> existingFoodLogIds = new HashSet<>();
            for (FoodLog entry : existingFoodLogs) {
                existingFoodLogIds.add(entry.getId());
            }

            for (BackupData.FoodLogData entryData : backupData.getFoodLogs()) {
                if (!existingFoodLogIds.contains(entryData.getId())) {
                    FoodLog newEntry = new FoodLog();
                    newEntry.setId(entryData.getId());
                    newEntry.setCarbohydrates(entryData.getCarbohydrates());
                    newEntry.setKcal(entryData.getKcal());
                    if (entryData.getDateTime() != null) {
                        newEntry.setDateTime(LocalDateTime.parse(entryData.getDateTime()));
                    }
                    newEntry.setFoodItems(entryData.getFoodItems());
                    newEntry.setNotes(entryData.getNotes());
                    if (entryData.getCreatedAt() != null) {
                        newEntry.setCreatedAt(LocalDateTime.parse(entryData.getCreatedAt()));
                    }
                    newEntry.setUser(user);
                    if (foodTagRepository != null && entryData.getTagNames() != null) {
                        Set<de.idrinth.habitevaluator.shared.model.FoodTag> tags = new HashSet<>();
                        for (String tagName : entryData.getTagNames()) {
                            String trimmed = tagName.trim();
                            if (trimmed.isEmpty()) {
                                continue;
                            }
                            String nameLower = trimmed.toLowerCase();
                            java.util.Optional<de.idrinth.habitevaluator.shared.model.FoodTag> existing =
                                    foodTagRepository.findByNameLowerAndUserId(nameLower, user.getId());
                            if (existing.isPresent()) {
                                tags.add(existing.get());
                            } else {
                                de.idrinth.habitevaluator.shared.model.FoodTag newTag =
                                        new de.idrinth.habitevaluator.shared.model.FoodTag(trimmed);
                                newTag.setUser(user);
                                tags.add(foodTagRepository.save(newTag));
                            }
                        }
                        newEntry.setTags(tags);
                    }
                    foodLogRepository.save(newEntry);
                    foodLogsAdded++;
                }
            }
        }

        // Merge emotion pairs
        int emotionPairsAdded = 0;
        Map<String, String> emotionPairIdMapping = new HashMap<>();
        if (options.isRestoreEmotionData() && emotionPairRepository != null
                && backupData.getEmotionPairs() != null) {
            List<EmotionPair> existingPairs = emotionPairRepository.findByUserId(user.getId());
            Map<String, EmotionPair> existingByLabels = new HashMap<>();
            for (EmotionPair pair : existingPairs) {
                String key = pair.getNegativeLabel() + "|" + pair.getPositiveLabel();
                existingByLabels.put(key, pair);
            }

            for (BackupData.EmotionPairData pairData : backupData.getEmotionPairs()) {
                String key = pairData.getNegativeLabel() + "|" + pairData.getPositiveLabel();
                EmotionPair existing = existingByLabels.get(key);
                if (existing != null) {
                    emotionPairIdMapping.put(pairData.getId(), existing.getId());
                } else {
                    EmotionPair newPair = new EmotionPair(pairData.getNegativeLabel(),
                            pairData.getPositiveLabel());
                    newPair.setUser(user);
                    newPair = emotionPairRepository.save(newPair);
                    emotionPairIdMapping.put(pairData.getId(), newPair.getId());
                    emotionPairsAdded++;
                }
            }
        }

        // Merge emotion entries
        int emotionEntriesAdded = 0;
        if (options.isRestoreEmotionData() && emotionEntryRepository != null
                && emotionPairRepository != null && backupData.getEmotionEntries() != null) {
            List<EmotionEntry> existingEmotionEntries = emotionEntryRepository.findByUserId(user.getId());
            Set<String> existingEmotionEntryIds = new HashSet<>();
            for (EmotionEntry entry : existingEmotionEntries) {
                existingEmotionEntryIds.add(entry.getId());
            }

            for (BackupData.EmotionEntryData entryData : backupData.getEmotionEntries()) {
                if (!existingEmotionEntryIds.contains(entryData.getId())) {
                    String mappedPairId = emotionPairIdMapping.get(entryData.getEmotionPairId());
                    if (mappedPairId == null) {
                        mappedPairId = entryData.getEmotionPairId();
                    }
                    java.util.Optional<EmotionPair> pairOpt = emotionPairRepository.findById(mappedPairId);
                    if (pairOpt.isPresent()) {
                        EmotionEntry newEntry = new EmotionEntry();
                        newEntry.setId(entryData.getId());
                        newEntry.setEmotionPair(pairOpt.get());
                        newEntry.setStrength(entryData.getStrength());
                        if (entryData.getRecordedAt() != null) {
                            newEntry.setRecordedAt(LocalDateTime.parse(entryData.getRecordedAt()));
                        }
                        newEntry.setNotes(entryData.getNotes());
                        newEntry.setUser(user);
                        emotionEntryRepository.save(newEntry);
                        emotionEntriesAdded++;
                    }
                }
            }
        }

        // Merge meeting entries
        int meetingEntriesAdded = 0;
        if (options.isRestoreMeetingEntries() && meetingEntryRepository != null
                && backupData.getMeetingEntries() != null) {
            List<MeetingEntry> existingMeetingEntries = meetingEntryRepository.findByUserId(user.getId());
            Set<String> existingMeetingIds = new HashSet<>();
            for (MeetingEntry entry : existingMeetingEntries) {
                existingMeetingIds.add(entry.getId());
            }

            for (BackupData.MeetingEntryData entryData : backupData.getMeetingEntries()) {
                if (!existingMeetingIds.contains(entryData.getId())) {
                    MeetingEntry newEntry = new MeetingEntry();
                    newEntry.setId(entryData.getId());
                    newEntry.setPlace(entryData.getPlace());
                    newEntry.setAttendants(entryData.getAttendants());
                    if (entryData.getStartTime() != null) {
                        newEntry.setStartTime(LocalTime.parse(entryData.getStartTime()));
                    }
                    if (entryData.getEndTime() != null) {
                        newEntry.setEndTime(LocalTime.parse(entryData.getEndTime()));
                    }
                    if (entryData.getDate() != null) {
                        newEntry.setDate(LocalDate.parse(entryData.getDate()));
                    }
                    if (entryData.getCreatedAt() != null) {
                        newEntry.setCreatedAt(LocalDateTime.parse(entryData.getCreatedAt()));
                    }
                    newEntry.setUser(user);
                    meetingEntryRepository.save(newEntry);
                    meetingEntriesAdded++;
                }
            }
        }

        // Merge activity logs
        int activityLogsAdded = 0;
        if (options.isRestoreActivityLogs() && activityLogRepository != null
                && backupData.getActivityLogs() != null) {
            List<ActivityLog> existingActivityLogs = activityLogRepository.findByUserId(user.getId());
            Set<String> existingActivityLogIds = new HashSet<>();
            for (ActivityLog entry : existingActivityLogs) {
                existingActivityLogIds.add(entry.getId());
            }
            for (BackupData.ActivityLogData entryData : backupData.getActivityLogs()) {
                if (!existingActivityLogIds.contains(entryData.getId())) {
                    ActivityLog newEntry = new ActivityLog();
                    newEntry.setId(entryData.getId());
                    newEntry.setPersons(entryData.getPersons());
                    newEntry.setLocation(entryData.getLocation());
                    if (entryData.getStartTime() != null) {
                        newEntry.setStartTime(LocalTime.parse(entryData.getStartTime()));
                    }
                    if (entryData.getEndTime() != null) {
                        newEntry.setEndTime(LocalTime.parse(entryData.getEndTime()));
                    }
                    if (entryData.getDate() != null) {
                        newEntry.setDate(LocalDate.parse(entryData.getDate()));
                    }
                    newEntry.setActivity(entryData.getActivity());
                    if (entryData.getCreatedAt() != null) {
                        newEntry.setCreatedAt(LocalDateTime.parse(entryData.getCreatedAt()));
                    }
                    newEntry.setUser(user);
                    activityLogRepository.save(newEntry);
                    activityLogsAdded++;
                }
            }
        }

        // Merge medications
        int medicationsAdded = 0;
        Map<String, String> medicationIdMapping = new HashMap<>();
        if (options.isRestoreMedicationData() && medicationRepository != null
                && backupData.getMedications() != null) {
            List<Medication> existingMedications = medicationRepository.findByUserId(user.getId());
            Map<String, Medication> existingByNameAndType = new HashMap<>();
            for (Medication med : existingMedications) {
                String key = med.getName() + "|" + (med.getProvisionType() != null ? med.getProvisionType().name() : "");
                existingByNameAndType.put(key, med);
            }

            for (BackupData.MedicationData medData : backupData.getMedications()) {
                String key = medData.getName() + "|" + (medData.getProvisionType() != null ? medData.getProvisionType() : "");
                Medication existing = existingByNameAndType.get(key);
                if (existing != null) {
                    medicationIdMapping.put(medData.getId(), existing.getId());
                } else {
                    MedicationProvisionType provisionType = null;
                    if (medData.getProvisionType() != null) {
                        provisionType = MedicationProvisionType.valueOf(medData.getProvisionType());
                    }
                    Medication newMed = new Medication(medData.getName(), provisionType);
                    newMed.setWikipediaLink(medData.getWikipediaLink());
                    newMed.setUser(user);
                    newMed = medicationRepository.save(newMed);
                    medicationIdMapping.put(medData.getId(), newMed.getId());
                    medicationsAdded++;
                }
            }
        }

        // Merge medication logs
        int medicationLogsAdded = 0;
        if (options.isRestoreMedicationData() && medicationLogRepository != null
                && medicationRepository != null && backupData.getMedicationLogs() != null) {
            List<MedicationLog> existingMedicationLogs = medicationLogRepository.findByUserId(user.getId());
            Set<String> existingMedicationLogIds = new HashSet<>();
            for (MedicationLog entry : existingMedicationLogs) {
                existingMedicationLogIds.add(entry.getId());
            }

            for (BackupData.MedicationLogData logData : backupData.getMedicationLogs()) {
                if (!existingMedicationLogIds.contains(logData.getId())) {
                    String mappedMedId = medicationIdMapping.get(logData.getMedicationId());
                    if (mappedMedId == null) {
                        mappedMedId = logData.getMedicationId();
                    }
                    java.util.Optional<Medication> medOpt = medicationRepository.findById(mappedMedId);
                    if (medOpt.isPresent()) {
                        MedicationLog newLog = new MedicationLog();
                        newLog.setId(logData.getId());
                        newLog.setMedication(medOpt.get());
                        newLog.setAmount(logData.getAmount());
                        if (logData.getTakenAt() != null) {
                            newLog.setTakenAt(LocalDateTime.parse(logData.getTakenAt()));
                        }
                        newLog.setNotes(logData.getNotes());
                        if (logData.getCreatedAt() != null) {
                            newLog.setCreatedAt(LocalDateTime.parse(logData.getCreatedAt()));
                        }
                        newLog.setUser(user);
                        medicationLogRepository.save(newLog);
                        medicationLogsAdded++;
                    }
                }
            }
        }

        // Merge reminder settings
        boolean reminderSettingsRestored = false;
        if (options.isRestoreReminderSettings() && reminderSettingsRepository != null
                && backupData.getReminderSettings() != null) {
            BackupData.ReminderSettingsData settingsData = backupData.getReminderSettings();
            java.util.Optional<ReminderSettings> existingOpt =
                    reminderSettingsRepository.findByUserId(user.getId());
            ReminderSettings settings = existingOpt.orElseGet(ReminderSettings::new);
            settings.setUser(user);
            settings.setSleepReminderEnabled(settingsData.isSleepReminderEnabled());
            if (settingsData.getSleepReminderTime() != null) {
                settings.setSleepReminderTime(LocalTime.parse(settingsData.getSleepReminderTime()));
            }
            settings.setDiaryReminderEnabled(settingsData.isDiaryReminderEnabled());
            if (settingsData.getDiaryReminderTime() != null) {
                settings.setDiaryReminderTime(LocalTime.parse(settingsData.getDiaryReminderTime()));
            }
            settings.setEmotionReminderEnabled(settingsData.isEmotionReminderEnabled());
            settings.setEmotionReminderCount(settingsData.getEmotionReminderCount());
            if (settingsData.getWakingHoursStart() != null) {
                settings.setWakingHoursStart(LocalTime.parse(settingsData.getWakingHoursStart()));
            }
            if (settingsData.getWakingHoursEnd() != null) {
                settings.setWakingHoursEnd(LocalTime.parse(settingsData.getWakingHoursEnd()));
            }
            reminderSettingsRepository.save(settings);
            reminderSettingsRestored = true;
        }

        // Merge module visibility
        boolean moduleVisibilityRestored = false;
        if (options.isRestoreModuleVisibility() && moduleVisibilityRepository != null
                && backupData.getModuleVisibility() != null) {
            BackupData.ModuleVisibilityData visData = backupData.getModuleVisibility();
            java.util.Optional<ModuleVisibility> existingOpt =
                    moduleVisibilityRepository.findByUserId(user.getId());
            ModuleVisibility visibility = existingOpt.orElseGet(ModuleVisibility::new);
            visibility.setUser(user);
            visibility.setDiaryVisible(visData.isDiaryVisible());
            visibility.setSleepVisible(visData.isSleepVisible());
            visibility.setEmotionsVisible(visData.isEmotionsVisible());
            visibility.setPointsVisible(visData.isPointsVisible());
            visibility.setStatisticsVisible(visData.isStatisticsVisible());
            visibility.setFoodLogVisible(visData.isFoodLogVisible());
            visibility.setSportLogVisible(visData.isSportLogVisible());
            visibility.setMedicationVisible(visData.isMedicationVisible());
            visibility.setBackupVisible(visData.isBackupVisible());
            visibility.setPdfExportVisible(visData.isPdfExportVisible());
            visibility.setActivityLogVisible(visData.isActivityLogVisible());
            moduleVisibilityRepository.save(visibility);
            moduleVisibilityRestored = true;
        }

        // Merge emergency plan steps and actions
        int emergencyPlanStepsAdded = 0;
        if (options.isRestoreEmergencyPlan() && emergencyPlanStepRepository != null
                && backupData.getEmergencyPlanSteps() != null) {
            List<EmergencyPlanStep> existingSteps = emergencyPlanStepRepository.findByUserId(user.getId());
            Set<String> existingStepIds = new HashSet<>();
            for (EmergencyPlanStep step : existingSteps) {
                existingStepIds.add(step.getId());
            }

            for (BackupData.EmergencyPlanStepData stepData : backupData.getEmergencyPlanSteps()) {
                if (!existingStepIds.contains(stepData.getId())) {
                    EmergencyPlanStep newStep = new EmergencyPlanStep(stepData.getQuestion(), stepData.getStepOrder());
                    newStep.setId(stepData.getId());
                    newStep.setUser(user);
                    newStep = emergencyPlanStepRepository.save(newStep);

                    if (emergencyPlanActionRepository != null && stepData.getActions() != null) {
                        for (BackupData.EmergencyPlanActionData actionData : stepData.getActions()) {
                            EmergencyPlanAction newAction = new EmergencyPlanAction(
                                    actionData.getActionText(), actionData.getPhoneNumber(), actionData.getActionOrder());
                            newAction.setId(actionData.getId());
                            newAction.setStep(newStep);
                            emergencyPlanActionRepository.save(newAction);
                        }
                    }

                    emergencyPlanStepsAdded++;
                }
            }
        }

        MergeResult result = new MergeResult(categoriesAdded, habitsAdded, habitsMerged,
                entriesAdded, diaryEntriesAdded, sleepEntriesAdded, sportLogsAdded, foodLogsAdded,
                emotionPairsAdded, emotionEntriesAdded, meetingEntriesAdded, activityLogsAdded,
                medicationsAdded, medicationLogsAdded, reminderSettingsRestored,
                moduleVisibilityRestored, emergencyPlanStepsAdded);
        logger.info("Backup merged: {}", result);
        return result;
    }

    public MergeResult mergeBackupData(BackupData backupData, User user,
                                       HabitRepository habitRepository,
                                       HabitCategoryRepository categoryRepository,
                                       DiaryEntryRepository diaryEntryRepository,
                                       SleepEntryRepository sleepEntryRepository,
                                       SportLogRepository sportLogRepository,
                                       FoodLogRepository foodLogRepository,
                                       de.idrinth.habitevaluator.shared.repository.FoodTagRepository foodTagRepository,
                                       EmotionPairRepository emotionPairRepository,
                                       EmotionEntryRepository emotionEntryRepository,
                                       MeetingEntryRepository meetingEntryRepository,
                                       ActivityLogRepository activityLogRepository,
                                       ReminderSettingsRepository reminderSettingsRepository,
                                       MedicationRepository medicationRepository,
                                       MedicationLogRepository medicationLogRepository,
                                       ModuleVisibilityRepository moduleVisibilityRepository,
                                       EmergencyPlanStepRepository emergencyPlanStepRepository,
                                       EmergencyPlanActionRepository emergencyPlanActionRepository,
                                       PlannerGroupRepository plannerGroupRepository,
                                       PlannerActivityRepository plannerActivityRepository,
                                       WeekPlannerSlotRepository weekPlannerSlotRepository,
                                       SlotConfirmationRepository slotConfirmationRepository,
                                       RestoreOptions options) throws BackupException {
        if (user == null) {
            throw new BackupException("User must not be null for merge");
        }
        if (options == null) {
            options = RestoreOptions.all();
        }

        try {
            // Clear day planner data before doMerge when overwrite is enabled
            if (options.isOverwrite() && options.isRestoreDayPlanner()) {
                clearDayPlannerData(user, plannerGroupRepository, plannerActivityRepository,
                        weekPlannerSlotRepository, slotConfirmationRepository);
            }

            MergeResult baseResult = doMerge(backupData, user, habitRepository, categoryRepository,
                    diaryEntryRepository, sleepEntryRepository, sportLogRepository,
                    foodLogRepository, foodTagRepository, emotionPairRepository,
                    emotionEntryRepository, meetingEntryRepository, activityLogRepository,
                    reminderSettingsRepository, medicationRepository, medicationLogRepository,
                    moduleVisibilityRepository, emergencyPlanStepRepository,
                    emergencyPlanActionRepository, options);

            int dayPlannerItemsAdded = 0;
            if (options.isRestoreDayPlanner()) {
                dayPlannerItemsAdded = mergeDayPlannerData(backupData, user,
                        plannerGroupRepository, plannerActivityRepository,
                        weekPlannerSlotRepository, slotConfirmationRepository);
            }

            return new MergeResult(
                    baseResult.getCategoriesAdded(), baseResult.getHabitsAdded(),
                    baseResult.getHabitsMerged(), baseResult.getEntriesAdded(),
                    baseResult.getDiaryEntriesAdded(), baseResult.getSleepEntriesAdded(),
                    baseResult.getSportLogsAdded(), baseResult.getFoodLogsAdded(),
                    baseResult.getEmotionPairsAdded(), baseResult.getEmotionEntriesAdded(),
                    baseResult.getMeetingEntriesAdded(), baseResult.getActivityLogsAdded(),
                    baseResult.getMedicationsAdded(), baseResult.getMedicationLogsAdded(),
                    baseResult.isReminderSettingsRestored(),
                    baseResult.isModuleVisibilityRestored(),
                    baseResult.getEmergencyPlanStepsAdded(),
                    dayPlannerItemsAdded);
        } catch (BackupException e) {
            throw e;
        } catch (Exception e) {
            throw new BackupException("Failed to merge backup data", e);
        }
    }

    private void clearDayPlannerData(User user,
                                    PlannerGroupRepository plannerGroupRepository,
                                    PlannerActivityRepository plannerActivityRepository,
                                    WeekPlannerSlotRepository weekPlannerSlotRepository,
                                    SlotConfirmationRepository slotConfirmationRepository) {
        String userId = user.getId();
        if (slotConfirmationRepository != null) {
            for (SlotConfirmation confirmation : slotConfirmationRepository.findByUserId(userId)) {
                slotConfirmationRepository.deleteById(confirmation.getId());
            }
        }
        if (weekPlannerSlotRepository != null) {
            for (WeekPlannerSlot slot : weekPlannerSlotRepository.findByUserId(userId)) {
                weekPlannerSlotRepository.deleteById(slot.getId());
            }
        }
        if (plannerActivityRepository != null) {
            for (PlannerActivity activity : plannerActivityRepository.findByUserId(userId)) {
                plannerActivityRepository.deleteById(activity.getId());
            }
        }
        if (plannerGroupRepository != null) {
            for (PlannerGroup group : plannerGroupRepository.findByUserId(userId)) {
                plannerGroupRepository.deleteById(group.getId());
            }
        }
        logger.info("Overwrite mode: cleared day planner data for user {}", userId);
    }

    private int mergeDayPlannerData(BackupData backupData, User user,
                                    PlannerGroupRepository plannerGroupRepository,
                                    PlannerActivityRepository plannerActivityRepository,
                                    WeekPlannerSlotRepository weekPlannerSlotRepository,
                                    SlotConfirmationRepository slotConfirmationRepository) {
        int itemsAdded = 0;
        Map<String, String> groupIdMapping = new HashMap<>();

        // Merge planner groups
        if (plannerGroupRepository != null && backupData.getPlannerGroups() != null) {
            List<PlannerGroup> existingGroups = plannerGroupRepository.findByUserId(user.getId());
            Map<String, PlannerGroup> existingByName = new HashMap<>();
            for (PlannerGroup group : existingGroups) {
                existingByName.put(group.getName(), group);
            }

            for (BackupData.PlannerGroupData groupData : backupData.getPlannerGroups()) {
                PlannerGroup existing = existingByName.get(groupData.getName());
                if (existing != null) {
                    groupIdMapping.put(groupData.getId(), existing.getId());
                } else {
                    PlannerGroup newGroup = new PlannerGroup(groupData.getName(), groupData.getDescription());
                    newGroup.setUser(user);
                    if (groupData.getCreatedAt() != null) {
                        newGroup.setCreatedAt(LocalDateTime.parse(groupData.getCreatedAt()));
                    }
                    newGroup = plannerGroupRepository.save(newGroup);
                    groupIdMapping.put(groupData.getId(), newGroup.getId());
                    itemsAdded++;
                }
            }
        }

        // Merge planner activities
        Map<String, String> activityIdMapping = new HashMap<>();
        if (plannerActivityRepository != null && backupData.getPlannerActivities() != null) {
            List<PlannerActivity> existingActivities = plannerActivityRepository.findByUserId(user.getId());
            Map<String, PlannerActivity> existingByName = new HashMap<>();
            for (PlannerActivity activity : existingActivities) {
                existingByName.put(activity.getName(), activity);
            }

            for (BackupData.PlannerActivityData actData : backupData.getPlannerActivities()) {
                PlannerActivity existing = existingByName.get(actData.getName());
                if (existing != null) {
                    activityIdMapping.put(actData.getId(), existing.getId());
                } else {
                    PlannerActivity newActivity = new PlannerActivity(actData.getName(), actData.getDescription());
                    newActivity.setUser(user);
                    if (actData.getCreatedAt() != null) {
                        newActivity.setCreatedAt(LocalDateTime.parse(actData.getCreatedAt()));
                    }
                    // Resolve group references
                    if (actData.getGroupIds() != null && plannerGroupRepository != null) {
                        Set<PlannerGroup> groups = new HashSet<>();
                        for (String oldGroupId : actData.getGroupIds()) {
                            String mappedId = groupIdMapping.getOrDefault(oldGroupId, oldGroupId);
                            plannerGroupRepository.findById(mappedId).ifPresent(groups::add);
                        }
                        newActivity.setGroups(groups);
                    }
                    newActivity = plannerActivityRepository.save(newActivity);
                    activityIdMapping.put(actData.getId(), newActivity.getId());
                    itemsAdded++;
                }
            }
        }

        // Merge week planner slots
        if (weekPlannerSlotRepository != null && backupData.getWeekPlannerSlots() != null) {
            List<WeekPlannerSlot> existingSlots = weekPlannerSlotRepository.findByUserId(user.getId());
            Set<String> existingSlotKeys = new HashSet<>();
            for (WeekPlannerSlot slot : existingSlots) {
                existingSlotKeys.add(slot.getDayOfWeek() + "|" + slot.getHour() + "|" + slot.getDuration());
            }

            for (BackupData.WeekPlannerSlotData slotData : backupData.getWeekPlannerSlots()) {
                int duration = Math.max(1, slotData.getDuration());
                String key = slotData.getDayOfWeek() + "|" + slotData.getHour() + "|" + duration;
                if (!existingSlotKeys.contains(key)) {
                    WeekPlannerSlot newSlot = new WeekPlannerSlot(slotData.getDayOfWeek(), slotData.getHour(), duration);
                    newSlot.setUser(user);
                    if (slotData.getGroupIds() != null && plannerGroupRepository != null) {
                        Set<PlannerGroup> groups = new HashSet<>();
                        for (String gid : slotData.getGroupIds()) {
                            String mappedGroupId = groupIdMapping.getOrDefault(gid, gid);
                            plannerGroupRepository.findById(mappedGroupId).ifPresent(groups::add);
                        }
                        newSlot.setGroups(groups);
                    }
                    weekPlannerSlotRepository.save(newSlot);
                    itemsAdded++;
                }
            }
        }

        // Merge slot confirmations
        if (slotConfirmationRepository != null && backupData.getSlotConfirmations() != null) {
            List<SlotConfirmation> existingConfirmations = slotConfirmationRepository.findByUserId(user.getId());
            Set<String> existingConfirmationIds = new HashSet<>();
            for (SlotConfirmation confirmation : existingConfirmations) {
                existingConfirmationIds.add(confirmation.getId());
            }

            for (BackupData.SlotConfirmationData confData : backupData.getSlotConfirmations()) {
                if (!existingConfirmationIds.contains(confData.getId())) {
                    SlotConfirmation newConf = new SlotConfirmation();
                    newConf.setId(confData.getId());
                    newConf.setConfirmed(confData.isConfirmed());
                    newConf.setUser(user);
                    if (confData.getDate() != null) {
                        newConf.setDate(LocalDate.parse(confData.getDate()));
                    }
                    if (confData.getCreatedAt() != null) {
                        newConf.setCreatedAt(LocalDateTime.parse(confData.getCreatedAt()));
                    }
                    if (confData.getSlotId() != null && weekPlannerSlotRepository != null) {
                        weekPlannerSlotRepository.findById(confData.getSlotId()).ifPresent(newConf::setSlot);
                    }
                    if (confData.getActivityId() != null && plannerActivityRepository != null) {
                        String mappedActivityId = activityIdMapping.getOrDefault(confData.getActivityId(), confData.getActivityId());
                        plannerActivityRepository.findById(mappedActivityId).ifPresent(newConf::setActivity);
                    }
                    if (confData.getGroupId() != null && plannerGroupRepository != null) {
                        String mappedGroupId = groupIdMapping.getOrDefault(confData.getGroupId(), confData.getGroupId());
                        plannerGroupRepository.findById(mappedGroupId).ifPresent(newConf::setGroup);
                    }
                    slotConfirmationRepository.save(newConf);
                    itemsAdded++;
                }
            }
        }

        return itemsAdded;
    }

    /**
     * Lists available backup files in the backup directory, sorted newest first.
     *
     * @param backupDir the backup directory
     * @return sorted array of backup files
     */
    public File[] listBackups(File backupDir) {
        if (!backupDir.exists() || !backupDir.isDirectory()) {
            return new File[0];
        }
        File[] backups = backupDir.listFiles((dir, name) -> name.endsWith(BACKUP_FILE_EXTENSION));
        if (backups == null) {
            return new File[0];
        }
        Arrays.sort(backups, Comparator.comparing(File::getName).reversed());
        return backups;
    }

    /**
     * Checks if a backup already exists for today.
     *
     * @param backupDir the backup directory
     * @return true if today's backup exists
     */
    public boolean hasTodaysBackup(File backupDir) {
        String fileName = LocalDate.now().format(DATE_FORMAT) + BACKUP_FILE_EXTENSION;
        return new File(backupDir, fileName).exists();
    }

    private BackupData collectBackupData(User user,
                                         HabitRepository habitRepository,
                                         HabitCategoryRepository categoryRepository,
                                         DiaryEntryRepository diaryEntryRepository,
                                         SleepEntryRepository sleepEntryRepository) {
        return collectBackupData(user, habitRepository, categoryRepository,
                diaryEntryRepository, sleepEntryRepository, null);
    }

    BackupData collectBackupData(User user,
                                         HabitRepository habitRepository,
                                         HabitCategoryRepository categoryRepository,
                                         DiaryEntryRepository diaryEntryRepository,
                                         SleepEntryRepository sleepEntryRepository,
                                         SportLogRepository sportLogRepository) {
        BackupData data = new BackupData();
        data.setBackupDate(LocalDateTime.now().toString());

        BackupData.UserData userData = new BackupData.UserData();
        userData.setId(user.getId());
        userData.setUsername(user.getUsername());
        userData.setEmail(user.getEmail());
        if (user.getCreatedAt() != null) {
            userData.setCreatedAt(user.getCreatedAt().toString());
        }
        data.setUser(userData);

        if (habitRepository != null) {
            List<Habit> habits = habitRepository.findByUserId(user.getId());
            for (Habit habit : habits) {
                data.getHabits().add(convertHabit(habit));
            }
        }

        if (categoryRepository != null) {
            List<HabitCategory> categories = categoryRepository.findByUserId(user.getId());
            for (HabitCategory cat : categories) {
                BackupData.CategoryData catData = new BackupData.CategoryData();
                catData.setId(cat.getId());
                catData.setName(cat.getName());
                catData.setDescription(cat.getDescription());
                catData.setColor(cat.getColor());
                data.getCategories().add(catData);
            }
        }

        if (diaryEntryRepository != null) {
            List<DiaryEntry> diaryEntries = diaryEntryRepository.findByUserId(user.getId());
            for (DiaryEntry entry : diaryEntries) {
                BackupData.DiaryEntryData entryData = new BackupData.DiaryEntryData();
                entryData.setId(entry.getId());
                entryData.setDescription(entry.getDescription());
                if (entry.getSignificance() != null) {
                    entryData.setSignificance(entry.getSignificance().name());
                }
                if (entry.getEventDate() != null) {
                    entryData.setEventDate(entry.getEventDate().toString());
                }
                if (entry.getCreatedAt() != null) {
                    entryData.setCreatedAt(entry.getCreatedAt().toString());
                }
                if (entry.getStartTime() != null) {
                    entryData.setStartTime(entry.getStartTime().toString());
                }
                if (entry.getEndTime() != null) {
                    entryData.setEndTime(entry.getEndTime().toString());
                }
                data.getDiaryEntries().add(entryData);
            }
        }

        if (sleepEntryRepository != null) {
            List<SleepEntry> sleepEntries = sleepEntryRepository.findByUserId(user.getId());
            for (SleepEntry entry : sleepEntries) {
                BackupData.SleepEntryData entryData = new BackupData.SleepEntryData();
                entryData.setId(entry.getId());
                if (entry.getFromTime() != null) {
                    entryData.setFromTime(entry.getFromTime().toString());
                }
                if (entry.getUntilTime() != null) {
                    entryData.setUntilTime(entry.getUntilTime().toString());
                }
                if (entry.getDate() != null) {
                    entryData.setDate(entry.getDate().toString());
                }
                entryData.setNotes(entry.getNotes());
                if (entry.getCreatedAt() != null) {
                    entryData.setCreatedAt(entry.getCreatedAt().toString());
                }
                data.getSleepEntries().add(entryData);
            }
        }

        if (sportLogRepository != null) {
            List<SportLog> sportLogs = sportLogRepository.findByUserId(user.getId());
            for (SportLog entry : sportLogs) {
                BackupData.SportLogData entryData = new BackupData.SportLogData();
                entryData.setId(entry.getId());
                entryData.setName(entry.getName());
                entryData.setMeasurement(entry.getMeasurement());
                entryData.setMeasurementUnit(entry.getMeasurementUnit());
                if (entry.getStartTime() != null) {
                    entryData.setStartTime(entry.getStartTime().toString());
                }
                if (entry.getEndTime() != null) {
                    entryData.setEndTime(entry.getEndTime().toString());
                }
                if (entry.getDate() != null) {
                    entryData.setDate(entry.getDate().toString());
                }
                entryData.setNotes(entry.getNotes());
                if (entry.getCreatedAt() != null) {
                    entryData.setCreatedAt(entry.getCreatedAt().toString());
                }
                data.getSportLogs().add(entryData);
            }
        }

        return data;
    }

    BackupData collectBackupData(User user,
                                         HabitRepository habitRepository,
                                         HabitCategoryRepository categoryRepository,
                                         DiaryEntryRepository diaryEntryRepository,
                                         SleepEntryRepository sleepEntryRepository,
                                         SportLogRepository sportLogRepository,
                                         FoodLogRepository foodLogRepository) {
        BackupData data = collectBackupData(user, habitRepository, categoryRepository,
                diaryEntryRepository, sleepEntryRepository, sportLogRepository);

        if (foodLogRepository != null) {
            List<FoodLog> foodLogs = foodLogRepository.findByUserId(user.getId());
            Set<String> exportedTagIds = new HashSet<>();
            for (FoodLog entry : foodLogs) {
                BackupData.FoodLogData entryData = new BackupData.FoodLogData();
                entryData.setId(entry.getId());
                entryData.setCarbohydrates(entry.getCarbohydrates());
                entryData.setKcal(entry.getKcal());
                if (entry.getDateTime() != null) {
                    entryData.setDateTime(entry.getDateTime().toString());
                }
                entryData.setFoodItems(entry.getFoodItems());
                entryData.setNotes(entry.getNotes());
                if (entry.getCreatedAt() != null) {
                    entryData.setCreatedAt(entry.getCreatedAt().toString());
                }
                if (entry.getTags() != null) {
                    List<String> tagNames = new java.util.ArrayList<>();
                    for (de.idrinth.habitevaluator.shared.model.FoodTag tag : entry.getTags()) {
                        if (tag.getName() == null || tag.getName().trim().isEmpty()) {
                            continue;
                        }
                        tagNames.add(tag.getName());
                        if (exportedTagIds.add(tag.getId())) {
                            BackupData.FoodTagData tagData = new BackupData.FoodTagData();
                            tagData.setId(tag.getId());
                            tagData.setName(tag.getName());
                            data.getFoodTags().add(tagData);
                        }
                    }
                    entryData.setTagNames(tagNames);
                }
                data.getFoodLogs().add(entryData);
            }
        }

        return data;
    }

    BackupData collectBackupData(User user,
                                         HabitRepository habitRepository,
                                         HabitCategoryRepository categoryRepository,
                                         DiaryEntryRepository diaryEntryRepository,
                                         SleepEntryRepository sleepEntryRepository,
                                         SportLogRepository sportLogRepository,
                                         FoodLogRepository foodLogRepository,
                                         EmotionPairRepository emotionPairRepository,
                                         EmotionEntryRepository emotionEntryRepository,
                                         ReminderSettingsRepository reminderSettingsRepository) {
        BackupData data = collectBackupData(user, habitRepository, categoryRepository,
                diaryEntryRepository, sleepEntryRepository, sportLogRepository, foodLogRepository);

        if (emotionPairRepository != null) {
            List<EmotionPair> emotionPairs = emotionPairRepository.findByUserId(user.getId());
            for (EmotionPair pair : emotionPairs) {
                BackupData.EmotionPairData pairData = new BackupData.EmotionPairData();
                pairData.setId(pair.getId());
                pairData.setNegativeLabel(pair.getNegativeLabel());
                pairData.setPositiveLabel(pair.getPositiveLabel());
                data.getEmotionPairs().add(pairData);
            }
        }

        if (emotionEntryRepository != null) {
            List<EmotionEntry> emotionEntries = emotionEntryRepository.findByUserId(user.getId());
            for (EmotionEntry entry : emotionEntries) {
                BackupData.EmotionEntryData entryData = new BackupData.EmotionEntryData();
                entryData.setId(entry.getId());
                if (entry.getEmotionPair() != null) {
                    entryData.setEmotionPairId(entry.getEmotionPair().getId());
                }
                entryData.setStrength(entry.getStrength());
                if (entry.getRecordedAt() != null) {
                    entryData.setRecordedAt(entry.getRecordedAt().toString());
                }
                entryData.setNotes(entry.getNotes());
                data.getEmotionEntries().add(entryData);
            }
        }

        if (reminderSettingsRepository != null) {
            java.util.Optional<ReminderSettings> settingsOpt =
                    reminderSettingsRepository.findByUserId(user.getId());
            if (settingsOpt.isPresent()) {
                ReminderSettings settings = settingsOpt.get();
                BackupData.ReminderSettingsData settingsData = new BackupData.ReminderSettingsData();
                settingsData.setId(settings.getId());
                settingsData.setSleepReminderEnabled(settings.isSleepReminderEnabled());
                if (settings.getSleepReminderTime() != null) {
                    settingsData.setSleepReminderTime(settings.getSleepReminderTime().toString());
                }
                settingsData.setDiaryReminderEnabled(settings.isDiaryReminderEnabled());
                if (settings.getDiaryReminderTime() != null) {
                    settingsData.setDiaryReminderTime(settings.getDiaryReminderTime().toString());
                }
                settingsData.setEmotionReminderEnabled(settings.isEmotionReminderEnabled());
                settingsData.setEmotionReminderCount(settings.getEmotionReminderCount());
                if (settings.getWakingHoursStart() != null) {
                    settingsData.setWakingHoursStart(settings.getWakingHoursStart().toString());
                }
                if (settings.getWakingHoursEnd() != null) {
                    settingsData.setWakingHoursEnd(settings.getWakingHoursEnd().toString());
                }
                data.setReminderSettings(settingsData);
            }
        }

        return data;
    }

    BackupData collectBackupData(User user,
                                         HabitRepository habitRepository,
                                         HabitCategoryRepository categoryRepository,
                                         DiaryEntryRepository diaryEntryRepository,
                                         SleepEntryRepository sleepEntryRepository,
                                         SportLogRepository sportLogRepository,
                                         FoodLogRepository foodLogRepository,
                                         EmotionPairRepository emotionPairRepository,
                                         EmotionEntryRepository emotionEntryRepository,
                                         MeetingEntryRepository meetingEntryRepository,
                                         ActivityLogRepository activityLogRepository,
                                         ReminderSettingsRepository reminderSettingsRepository,
                                         MedicationRepository medicationRepository,
                                         MedicationLogRepository medicationLogRepository) {
        BackupData data = collectBackupData(user, habitRepository, categoryRepository,
                diaryEntryRepository, sleepEntryRepository, sportLogRepository, foodLogRepository,
                emotionPairRepository, emotionEntryRepository, reminderSettingsRepository);

        if (meetingEntryRepository != null) {
            List<MeetingEntry> meetingEntries = meetingEntryRepository.findByUserId(user.getId());
            for (MeetingEntry entry : meetingEntries) {
                BackupData.MeetingEntryData entryData = new BackupData.MeetingEntryData();
                entryData.setId(entry.getId());
                entryData.setPlace(entry.getPlace());
                entryData.setAttendants(entry.getAttendants());
                if (entry.getStartTime() != null) {
                    entryData.setStartTime(entry.getStartTime().toString());
                }
                if (entry.getEndTime() != null) {
                    entryData.setEndTime(entry.getEndTime().toString());
                }
                if (entry.getDate() != null) {
                    entryData.setDate(entry.getDate().toString());
                }
                if (entry.getCreatedAt() != null) {
                    entryData.setCreatedAt(entry.getCreatedAt().toString());
                }
                data.getMeetingEntries().add(entryData);
            }
        }

        if (activityLogRepository != null) {
            List<ActivityLog> activityLogs = activityLogRepository.findByUserId(user.getId());
            for (ActivityLog entry : activityLogs) {
                BackupData.ActivityLogData entryData = new BackupData.ActivityLogData();
                entryData.setId(entry.getId());
                entryData.setPersons(entry.getPersons());
                entryData.setLocation(entry.getLocation());
                if (entry.getStartTime() != null) {
                    entryData.setStartTime(entry.getStartTime().toString());
                }
                if (entry.getEndTime() != null) {
                    entryData.setEndTime(entry.getEndTime().toString());
                }
                if (entry.getDate() != null) {
                    entryData.setDate(entry.getDate().toString());
                }
                entryData.setActivity(entry.getActivity());
                if (entry.getCreatedAt() != null) {
                    entryData.setCreatedAt(entry.getCreatedAt().toString());
                }
                data.getActivityLogs().add(entryData);
            }
        }

        if (medicationRepository != null) {
            List<Medication> medications = medicationRepository.findByUserId(user.getId());
            for (Medication medication : medications) {
                BackupData.MedicationData medData = new BackupData.MedicationData();
                medData.setId(medication.getId());
                medData.setName(medication.getName());
                medData.setWikipediaLink(medication.getWikipediaLink());
                if (medication.getProvisionType() != null) {
                    medData.setProvisionType(medication.getProvisionType().name());
                }
                data.getMedications().add(medData);
            }
        }

        if (medicationLogRepository != null) {
            List<MedicationLog> medicationLogs = medicationLogRepository.findByUserId(user.getId());
            for (MedicationLog log : medicationLogs) {
                BackupData.MedicationLogData logData = new BackupData.MedicationLogData();
                logData.setId(log.getId());
                if (log.getMedication() != null) {
                    logData.setMedicationId(log.getMedication().getId());
                }
                logData.setAmount(log.getAmount());
                if (log.getTakenAt() != null) {
                    logData.setTakenAt(log.getTakenAt().toString());
                }
                logData.setNotes(log.getNotes());
                if (log.getCreatedAt() != null) {
                    logData.setCreatedAt(log.getCreatedAt().toString());
                }
                data.getMedicationLogs().add(logData);
            }
        }

        return data;
    }

    BackupData collectBackupData(User user,
                                         HabitRepository habitRepository,
                                         HabitCategoryRepository categoryRepository,
                                         DiaryEntryRepository diaryEntryRepository,
                                         SleepEntryRepository sleepEntryRepository,
                                         SportLogRepository sportLogRepository,
                                         FoodLogRepository foodLogRepository,
                                         EmotionPairRepository emotionPairRepository,
                                         EmotionEntryRepository emotionEntryRepository,
                                         MeetingEntryRepository meetingEntryRepository,
                                         ActivityLogRepository activityLogRepository,
                                         ReminderSettingsRepository reminderSettingsRepository,
                                         MedicationRepository medicationRepository,
                                         MedicationLogRepository medicationLogRepository,
                                         ModuleVisibilityRepository moduleVisibilityRepository,
                                         EmergencyPlanStepRepository emergencyPlanStepRepository,
                                         EmergencyPlanActionRepository emergencyPlanActionRepository) {
        BackupData data = collectBackupData(user, habitRepository, categoryRepository,
                diaryEntryRepository, sleepEntryRepository, sportLogRepository, foodLogRepository,
                emotionPairRepository, emotionEntryRepository, meetingEntryRepository,
                activityLogRepository, reminderSettingsRepository, medicationRepository,
                medicationLogRepository);

        if (moduleVisibilityRepository != null) {
            java.util.Optional<ModuleVisibility> visOpt = moduleVisibilityRepository.findByUserId(user.getId());
            if (visOpt.isPresent()) {
                ModuleVisibility vis = visOpt.get();
                BackupData.ModuleVisibilityData visData = new BackupData.ModuleVisibilityData();
                visData.setId(vis.getId());
                visData.setDiaryVisible(vis.isDiaryVisible());
                visData.setSleepVisible(vis.isSleepVisible());
                visData.setEmotionsVisible(vis.isEmotionsVisible());
                visData.setPointsVisible(vis.isPointsVisible());
                visData.setStatisticsVisible(vis.isStatisticsVisible());
                visData.setFoodLogVisible(vis.isFoodLogVisible());
                visData.setSportLogVisible(vis.isSportLogVisible());
                visData.setMedicationVisible(vis.isMedicationVisible());
                visData.setBackupVisible(vis.isBackupVisible());
                visData.setPdfExportVisible(vis.isPdfExportVisible());
                visData.setActivityLogVisible(vis.isActivityLogVisible());
                data.setModuleVisibility(visData);
            }
        }

        if (emergencyPlanStepRepository != null) {
            List<EmergencyPlanStep> steps = emergencyPlanStepRepository.findByUserId(user.getId());
            for (EmergencyPlanStep step : steps) {
                BackupData.EmergencyPlanStepData stepData = new BackupData.EmergencyPlanStepData();
                stepData.setId(step.getId());
                stepData.setQuestion(step.getQuestion());
                stepData.setStepOrder(step.getStepOrder());

                if (emergencyPlanActionRepository != null) {
                    List<EmergencyPlanAction> actions = emergencyPlanActionRepository.findByStepId(step.getId());
                    for (EmergencyPlanAction action : actions) {
                        BackupData.EmergencyPlanActionData actionData = new BackupData.EmergencyPlanActionData();
                        actionData.setId(action.getId());
                        actionData.setActionText(action.getActionText());
                        actionData.setPhoneNumber(action.getPhoneNumber());
                        actionData.setActionOrder(action.getActionOrder());
                        stepData.getActions().add(actionData);
                    }
                }

                data.getEmergencyPlanSteps().add(stepData);
            }
        }

        return data;
    }

    BackupData collectBackupData(User user,
                                         HabitRepository habitRepository,
                                         HabitCategoryRepository categoryRepository,
                                         DiaryEntryRepository diaryEntryRepository,
                                         SleepEntryRepository sleepEntryRepository,
                                         SportLogRepository sportLogRepository,
                                         FoodLogRepository foodLogRepository,
                                         EmotionPairRepository emotionPairRepository,
                                         EmotionEntryRepository emotionEntryRepository,
                                         MeetingEntryRepository meetingEntryRepository,
                                         ActivityLogRepository activityLogRepository,
                                         ReminderSettingsRepository reminderSettingsRepository,
                                         MedicationRepository medicationRepository,
                                         MedicationLogRepository medicationLogRepository,
                                         ModuleVisibilityRepository moduleVisibilityRepository,
                                         EmergencyPlanStepRepository emergencyPlanStepRepository,
                                         EmergencyPlanActionRepository emergencyPlanActionRepository,
                                         PlannerGroupRepository plannerGroupRepository,
                                         PlannerActivityRepository plannerActivityRepository,
                                         WeekPlannerSlotRepository weekPlannerSlotRepository,
                                         SlotConfirmationRepository slotConfirmationRepository) {
        BackupData data = collectBackupData(user, habitRepository, categoryRepository,
                diaryEntryRepository, sleepEntryRepository, sportLogRepository, foodLogRepository,
                emotionPairRepository, emotionEntryRepository, meetingEntryRepository,
                activityLogRepository, reminderSettingsRepository, medicationRepository,
                medicationLogRepository, moduleVisibilityRepository, emergencyPlanStepRepository,
                emergencyPlanActionRepository);

        if (plannerGroupRepository != null) {
            List<PlannerGroup> groups = plannerGroupRepository.findByUserId(user.getId());
            for (PlannerGroup group : groups) {
                BackupData.PlannerGroupData groupData = new BackupData.PlannerGroupData();
                groupData.setId(group.getId());
                groupData.setName(group.getName());
                groupData.setDescription(group.getDescription());
                if (group.getCreatedAt() != null) {
                    groupData.setCreatedAt(group.getCreatedAt().toString());
                }
                data.getPlannerGroups().add(groupData);
            }
        }

        if (plannerActivityRepository != null) {
            List<PlannerActivity> activities = plannerActivityRepository.findByUserId(user.getId());
            for (PlannerActivity activity : activities) {
                BackupData.PlannerActivityData activityData = new BackupData.PlannerActivityData();
                activityData.setId(activity.getId());
                activityData.setName(activity.getName());
                activityData.setDescription(activity.getDescription());
                if (activity.getCreatedAt() != null) {
                    activityData.setCreatedAt(activity.getCreatedAt().toString());
                }
                List<String> groupIds = new java.util.ArrayList<>();
                if (activity.getGroups() != null) {
                    for (PlannerGroup group : activity.getGroups()) {
                        groupIds.add(group.getId());
                    }
                }
                activityData.setGroupIds(groupIds);
                data.getPlannerActivities().add(activityData);
            }
        }

        if (weekPlannerSlotRepository != null) {
            List<WeekPlannerSlot> slots = weekPlannerSlotRepository.findByUserId(user.getId());
            for (WeekPlannerSlot slot : slots) {
                BackupData.WeekPlannerSlotData slotData = new BackupData.WeekPlannerSlotData();
                slotData.setId(slot.getId());
                slotData.setDayOfWeek(slot.getDayOfWeek());
                slotData.setHour(slot.getHour());
                slotData.setDuration(slot.getDuration());
                List<String> groupIds = new ArrayList<>();
                if (slot.getGroups() != null) {
                    for (PlannerGroup group : slot.getGroups()) {
                        groupIds.add(group.getId());
                    }
                }
                slotData.setGroupIds(groupIds);
                data.getWeekPlannerSlots().add(slotData);
            }
        }

        if (slotConfirmationRepository != null) {
            List<SlotConfirmation> confirmations = slotConfirmationRepository.findByUserId(user.getId());
            for (SlotConfirmation confirmation : confirmations) {
                BackupData.SlotConfirmationData confData = new BackupData.SlotConfirmationData();
                confData.setId(confirmation.getId());
                confData.setConfirmed(confirmation.isConfirmed());
                if (confirmation.getDate() != null) {
                    confData.setDate(confirmation.getDate().toString());
                }
                if (confirmation.getCreatedAt() != null) {
                    confData.setCreatedAt(confirmation.getCreatedAt().toString());
                }
                if (confirmation.getSlot() != null) {
                    confData.setSlotId(confirmation.getSlot().getId());
                }
                if (confirmation.getActivity() != null) {
                    confData.setActivityId(confirmation.getActivity().getId());
                }
                if (confirmation.getGroup() != null) {
                    confData.setGroupId(confirmation.getGroup().getId());
                }
                data.getSlotConfirmations().add(confData);
            }
        }

        return data;
    }

    private BackupData.HabitData convertHabit(Habit habit) {
        BackupData.HabitData habitData = new BackupData.HabitData();
        habitData.setId(habit.getId());
        habitData.setName(habit.getName());
        habitData.setDescription(habit.getDescription());
        habitData.setCategoryId(habit.getCategoryId());
        if (habit.getFrequencyType() != null) {
            habitData.setFrequencyType(habit.getFrequencyType().name());
        }
        habitData.setTargetFrequency(habit.getTargetFrequency());
        habitData.setMaxEntriesPerDay(habit.getMaxEntriesPerDay());
        habitData.setPositiveScoring(habit.isPositiveScoring());
        if (habit.getCreatedAt() != null) {
            habitData.setCreatedAt(habit.getCreatedAt().toString());
        }
        habitData.setNameTranslations(habit.getNameTranslations());
        habitData.setDescriptionTranslations(habit.getDescriptionTranslations());

        ScoringRule rule = habit.getScoringRule();
        if (rule != null) {
            BackupData.ScoringRuleData ruleData = new BackupData.ScoringRuleData();
            ruleData.setId(rule.getId());
            ruleData.setName(rule.getName());
            ruleData.setThresholdFor1Point(rule.getThresholdFor1Point());
            ruleData.setThresholdFor2Points(rule.getThresholdFor2Points());
            ruleData.setThresholdFor4Points(rule.getThresholdFor4Points());
            ruleData.setThresholdFor8Points(rule.getThresholdFor8Points());
            habitData.setScoringRule(ruleData);
        }

        if (habit.getEntries() != null) {
            for (HabitEntry entry : habit.getEntries()) {
                BackupData.HabitEntryData entryData = new BackupData.HabitEntryData();
                entryData.setId(entry.getId());
                if (entry.getCompletedAt() != null) {
                    entryData.setCompletedAt(entry.getCompletedAt().toString());
                }
                entryData.setNotes(entry.getNotes());
                entryData.setValue(entry.getValue());
                habitData.getEntries().add(entryData);
            }
        }

        return habitData;
    }

    private void cleanupOldBackups(File backupDir) {
        File[] backups = listBackups(backupDir);
        if (backups.length > MAX_BACKUPS) {
            for (int i = MAX_BACKUPS; i < backups.length; i++) {
                if (backups[i].delete()) {
                    logger.info("Deleted old backup: {}", backups[i].getName());
                }
            }
        }
    }

    private static void writeBytes(File file, byte[] data) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        }
    }

    private static byte[] readBytes(File file) throws IOException {
        try (InputStream is = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            return bos.toByteArray();
        }
    }
}
