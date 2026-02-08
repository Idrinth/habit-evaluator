package de.idrinth.habitevaluator.shared.backup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.ReminderSettings;
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
import de.idrinth.habitevaluator.shared.repository.MeetingEntryRepository;
import de.idrinth.habitevaluator.shared.repository.ReminderSettingsRepository;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import de.idrinth.habitevaluator.shared.repository.SportLogRepository;
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
                foodLogRepository, foodTagRepository, null, null, null, null, options);
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
                emotionEntryRepository, null, reminderSettingsRepository, options);
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
                                ReminderSettingsRepository reminderSettingsRepository,
                                RestoreOptions options) throws BackupException {
        int categoriesAdded = 0;
        int habitsAdded = 0;
        int habitsMerged = 0;
        int entriesAdded = 0;
        int diaryEntriesAdded = 0;
        int sleepEntriesAdded = 0;

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
                            String nameLower = tagName.toLowerCase();
                            java.util.Optional<de.idrinth.habitevaluator.shared.model.FoodTag> existing =
                                    foodTagRepository.findByNameLowerAndUserId(nameLower, user.getId());
                            if (existing.isPresent()) {
                                tags.add(existing.get());
                            } else {
                                de.idrinth.habitevaluator.shared.model.FoodTag newTag =
                                        new de.idrinth.habitevaluator.shared.model.FoodTag(tagName);
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

        MergeResult result = new MergeResult(categoriesAdded, habitsAdded, habitsMerged,
                entriesAdded, diaryEntriesAdded, sleepEntriesAdded, sportLogsAdded, foodLogsAdded,
                emotionPairsAdded, emotionEntriesAdded, meetingEntriesAdded,
                reminderSettingsRestored);
        logger.info("Backup merged: {}", result);
        return result;
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
                                         ReminderSettingsRepository reminderSettingsRepository) {
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
