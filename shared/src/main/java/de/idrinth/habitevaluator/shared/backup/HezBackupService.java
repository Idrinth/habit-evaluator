package de.idrinth.habitevaluator.shared.backup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.ReminderSettingsRepository;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import de.idrinth.habitevaluator.shared.repository.SportLogRepository;
import de.idrinth.habitevaluator.shared.repository.FoodLogRepository;
import de.idrinth.habitevaluator.shared.repository.FoodTagRepository;
import de.idrinth.habitevaluator.shared.repository.MeetingEntryRepository;
import de.idrinth.habitevaluator.shared.repository.ActivityLogRepository;
import de.idrinth.habitevaluator.shared.repository.MedicationRepository;
import de.idrinth.habitevaluator.shared.repository.MedicationLogRepository;
import de.idrinth.habitevaluator.shared.repository.ModuleVisibilityRepository;
import de.idrinth.habitevaluator.shared.repository.EmergencyPlanStepRepository;
import de.idrinth.habitevaluator.shared.repository.EmergencyPlanActionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Service for creating and restoring .hez backup files.
 * A .hez file is a ZIP archive containing an encrypted backup file.
 * This format allows for easy file transfer and identification.
 */
public class HezBackupService {

    private static final Logger logger = LoggerFactory.getLogger(HezBackupService.class);
    public static final String HEZ_FILE_EXTENSION = ".hez";
    private static final String ENCRYPTED_BACKUP_ENTRY_NAME = "backup.encrypted";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final BackupEncryptionService encryptionService;
    private final Gson gson;

    public HezBackupService() {
        this.encryptionService = new BackupEncryptionService();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Creates a .hez backup file containing encrypted user data.
     *
     * @param password             the encryption password
     * @param user                 the current user
     * @param habitRepository      habit data source
     * @param categoryRepository   category data source (may be null)
     * @param diaryEntryRepository diary entry data source (may be null)
     * @param sleepEntryRepository sleep entry data source (may be null)
     * @return the .hez file as a byte array
     * @throws BackupException if backup creation fails
     */
    public byte[] createHezBackup(String password, User user,
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
            byte[] encrypted = encryptionService.encrypt(plaintext, password);

            return createZipArchive(encrypted);
        } catch (BackupException e) {
            throw e;
        } catch (Exception e) {
            throw new BackupException("Failed to create .hez backup", e);
        }
    }

    /**
     * Creates a .hez backup file containing encrypted user data, including emotion and reminder data.
     */
    public byte[] createHezBackup(String password, User user,
                                   HabitRepository habitRepository,
                                   HabitCategoryRepository categoryRepository,
                                   DiaryEntryRepository diaryEntryRepository,
                                   SleepEntryRepository sleepEntryRepository,
                                   EmotionPairRepository emotionPairRepository,
                                   EmotionEntryRepository emotionEntryRepository,
                                   ReminderSettingsRepository reminderSettingsRepository) throws BackupException {
        if (password == null || password.isEmpty()) {
            throw new BackupException("Backup password must not be empty");
        }
        if (user == null) {
            throw new BackupException("User must not be null for backup");
        }

        try {
            BackupService backupService = new BackupService();
            BackupData backupData = backupService.collectBackupData(user, habitRepository,
                    categoryRepository, diaryEntryRepository, sleepEntryRepository,
                    null, null, emotionPairRepository, emotionEntryRepository,
                    reminderSettingsRepository);

            String json = gson.toJson(backupData);
            byte[] plaintext = json.getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = encryptionService.encrypt(plaintext, password);

            return createZipArchive(encrypted);
        } catch (BackupException e) {
            throw e;
        } catch (Exception e) {
            throw new BackupException("Failed to create .hez backup", e);
        }
    }

    /**
     * Creates a .hez backup file containing encrypted user data, including all data types.
     */
    public byte[] createHezBackup(String password, User user,
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
                                   EmergencyPlanActionRepository emergencyPlanActionRepository) throws BackupException {
        if (password == null || password.isEmpty()) {
            throw new BackupException("Backup password must not be empty");
        }
        if (user == null) {
            throw new BackupException("User must not be null for backup");
        }

        try {
            BackupService backupService = new BackupService();
            BackupData backupData = backupService.collectBackupData(user, habitRepository,
                    categoryRepository, diaryEntryRepository, sleepEntryRepository,
                    sportLogRepository, foodLogRepository, emotionPairRepository,
                    emotionEntryRepository, meetingEntryRepository, activityLogRepository,
                    reminderSettingsRepository, medicationRepository, medicationLogRepository,
                    moduleVisibilityRepository, emergencyPlanStepRepository,
                    emergencyPlanActionRepository);

            String json = gson.toJson(backupData);
            byte[] plaintext = json.getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = encryptionService.encrypt(plaintext, password);

            return createZipArchive(encrypted);
        } catch (BackupException e) {
            throw e;
        } catch (Exception e) {
            throw new BackupException("Failed to create .hez backup", e);
        }
    }

    /**
     * Writes a .hez backup file to a specific location.
     *
     * @param outputFile           the file to write to
     * @param password             the encryption password
     * @param user                 the current user
     * @param habitRepository      habit data source
     * @param categoryRepository   category data source (may be null)
     * @param diaryEntryRepository diary entry data source (may be null)
     * @param sleepEntryRepository sleep entry data source (may be null)
     * @throws BackupException if backup creation fails
     */
    public void createHezBackupToFile(File outputFile, String password, User user,
                                       HabitRepository habitRepository,
                                       HabitCategoryRepository categoryRepository,
                                       DiaryEntryRepository diaryEntryRepository,
                                       SleepEntryRepository sleepEntryRepository) throws BackupException {
        byte[] hezData = createHezBackup(password, user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);
        try {
            writeBytes(outputFile, hezData);
            logger.info("Hez backup created successfully: {}", outputFile.getName());
        } catch (IOException e) {
            throw new BackupException("Failed to write .hez backup file", e);
        }
    }

    /**
     * Restores backup data from a .hez file.
     *
     * @param hezFile  the .hez file
     * @param password the decryption password
     * @return the deserialized backup data
     * @throws BackupException if restore fails
     */
    public BackupData restoreFromHezFile(File hezFile, String password) throws BackupException {
        if (password == null || password.isEmpty()) {
            throw new BackupException("Backup password must not be empty");
        }
        if (!hezFile.exists()) {
            throw new BackupException("Hez file does not exist: " + hezFile.getAbsolutePath());
        }

        try {
            byte[] hezData = readBytes(hezFile);
            return restoreFromHezBytes(hezData, password);
        } catch (BackupException e) {
            throw e;
        } catch (Exception e) {
            throw new BackupException("Failed to restore from .hez file", e);
        }
    }

    /**
     * Restores backup data from .hez bytes.
     *
     * @param hezData  the .hez file content as bytes
     * @param password the decryption password
     * @return the deserialized backup data
     * @throws BackupException if restore fails
     */
    public BackupData restoreFromHezBytes(byte[] hezData, String password) throws BackupException {
        if (password == null || password.isEmpty()) {
            throw new BackupException("Backup password must not be empty");
        }

        try {
            byte[] encrypted = extractFromZipArchive(hezData);
            byte[] plaintext = encryptionService.decrypt(encrypted, password);
            String json = new String(plaintext, StandardCharsets.UTF_8);
            return gson.fromJson(json, BackupData.class);
        } catch (BackupException e) {
            throw e;
        } catch (Exception e) {
            throw new BackupException("Failed to restore from .hez data. Wrong password or corrupted file.", e);
        }
    }

    /**
     * Restores backup data from an InputStream containing .hez data.
     *
     * @param inputStream the input stream containing .hez data
     * @param password    the decryption password
     * @return the deserialized backup data
     * @throws BackupException if restore fails
     */
    public BackupData restoreFromHezStream(InputStream inputStream, String password) throws BackupException {
        try {
            byte[] hezData = readAllBytes(inputStream);
            return restoreFromHezBytes(hezData, password);
        } catch (IOException e) {
            throw new BackupException("Failed to read from input stream", e);
        }
    }

    /**
     * Merges data from a .hez file into the current user's existing data.
     *
     * @param hezFile              the .hez file
     * @param password             the decryption password
     * @param user                 the current user
     * @param habitRepository      habit data source
     * @param categoryRepository   category data source (may be null)
     * @param diaryEntryRepository diary entry data source (may be null)
     * @param sleepEntryRepository sleep entry data source (may be null)
     * @return a summary of what was merged
     * @throws BackupException if merge fails
     */
    public MergeResult mergeFromHezFile(File hezFile, String password, User user,
                                        HabitRepository habitRepository,
                                        HabitCategoryRepository categoryRepository,
                                        DiaryEntryRepository diaryEntryRepository,
                                        SleepEntryRepository sleepEntryRepository) throws BackupException {
        return mergeFromHezFile(hezFile, password, user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository, RestoreOptions.all());
    }

    public MergeResult mergeFromHezFile(File hezFile, String password, User user,
                                        HabitRepository habitRepository,
                                        HabitCategoryRepository categoryRepository,
                                        DiaryEntryRepository diaryEntryRepository,
                                        SleepEntryRepository sleepEntryRepository,
                                        RestoreOptions options) throws BackupException {
        BackupData backupData = restoreFromHezFile(hezFile, password);
        BackupService backupService = new BackupService();
        return backupService.mergeBackupData(backupData, user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository,
                null, null, options);
    }

    /**
     * Merges data from .hez bytes into the current user's existing data.
     *
     * @param hezData              the .hez file content as bytes
     * @param password             the decryption password
     * @param user                 the current user
     * @param habitRepository      habit data source
     * @param categoryRepository   category data source (may be null)
     * @param diaryEntryRepository diary entry data source (may be null)
     * @param sleepEntryRepository sleep entry data source (may be null)
     * @return a summary of what was merged
     * @throws BackupException if merge fails
     */
    public MergeResult mergeFromHezBytes(byte[] hezData, String password, User user,
                                         HabitRepository habitRepository,
                                         HabitCategoryRepository categoryRepository,
                                         DiaryEntryRepository diaryEntryRepository,
                                         SleepEntryRepository sleepEntryRepository) throws BackupException {
        return mergeFromHezBytes(hezData, password, user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository, RestoreOptions.all());
    }

    public MergeResult mergeFromHezBytes(byte[] hezData, String password, User user,
                                         HabitRepository habitRepository,
                                         HabitCategoryRepository categoryRepository,
                                         DiaryEntryRepository diaryEntryRepository,
                                         SleepEntryRepository sleepEntryRepository,
                                         RestoreOptions options) throws BackupException {
        BackupData backupData = restoreFromHezBytes(hezData, password);
        BackupService backupService = new BackupService();
        return backupService.mergeBackupData(backupData, user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository,
                null, null, options);
    }

    /**
     * Merges data from .hez bytes into the current user's existing data,
     * including emotion and reminder data.
     */
    public MergeResult mergeFromHezBytes(byte[] hezData, String password, User user,
                                         HabitRepository habitRepository,
                                         HabitCategoryRepository categoryRepository,
                                         DiaryEntryRepository diaryEntryRepository,
                                         SleepEntryRepository sleepEntryRepository,
                                         EmotionPairRepository emotionPairRepository,
                                         EmotionEntryRepository emotionEntryRepository,
                                         ReminderSettingsRepository reminderSettingsRepository,
                                         RestoreOptions options) throws BackupException {
        BackupData backupData = restoreFromHezBytes(hezData, password);
        BackupService backupService = new BackupService();
        return backupService.mergeBackupData(backupData, user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository,
                null, null, null, emotionPairRepository, emotionEntryRepository,
                reminderSettingsRepository, options);
    }

    /**
     * Merges data from .hez bytes into the current user's existing data,
     * including all data types.
     */
    public MergeResult mergeFromHezBytes(byte[] hezData, String password, User user,
                                         HabitRepository habitRepository,
                                         HabitCategoryRepository categoryRepository,
                                         DiaryEntryRepository diaryEntryRepository,
                                         SleepEntryRepository sleepEntryRepository,
                                         SportLogRepository sportLogRepository,
                                         FoodLogRepository foodLogRepository,
                                         FoodTagRepository foodTagRepository,
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
        BackupData backupData = restoreFromHezBytes(hezData, password);
        BackupService backupService = new BackupService();
        return backupService.mergeBackupData(backupData, user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository,
                sportLogRepository, foodLogRepository, foodTagRepository,
                emotionPairRepository, emotionEntryRepository, meetingEntryRepository,
                activityLogRepository, reminderSettingsRepository, medicationRepository,
                medicationLogRepository, moduleVisibilityRepository,
                emergencyPlanStepRepository, emergencyPlanActionRepository, options);
    }

    /**
     * Merges data from a .hez input stream into the current user's existing data.
     *
     * @param inputStream          the input stream containing .hez data
     * @param password             the decryption password
     * @param user                 the current user
     * @param habitRepository      habit data source
     * @param categoryRepository   category data source (may be null)
     * @param diaryEntryRepository diary entry data source (may be null)
     * @param sleepEntryRepository sleep entry data source (may be null)
     * @return a summary of what was merged
     * @throws BackupException if merge fails
     */
    public MergeResult mergeFromHezStream(InputStream inputStream, String password, User user,
                                          HabitRepository habitRepository,
                                          HabitCategoryRepository categoryRepository,
                                          DiaryEntryRepository diaryEntryRepository,
                                          SleepEntryRepository sleepEntryRepository) throws BackupException {
        return mergeFromHezStream(inputStream, password, user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository, RestoreOptions.all());
    }

    public MergeResult mergeFromHezStream(InputStream inputStream, String password, User user,
                                          HabitRepository habitRepository,
                                          HabitCategoryRepository categoryRepository,
                                          DiaryEntryRepository diaryEntryRepository,
                                          SleepEntryRepository sleepEntryRepository,
                                          RestoreOptions options) throws BackupException {
        BackupData backupData = restoreFromHezStream(inputStream, password);
        BackupService backupService = new BackupService();
        return backupService.mergeBackupData(backupData, user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository,
                null, null, options);
    }

    /**
     * Generates a default filename for a .hez backup file.
     *
     * @return filename in format "habit-evaluator-YYYY-MM-DD.hez"
     */
    public String generateDefaultFilename() {
        return "habit-evaluator-" + LocalDate.now().format(DATE_FORMAT) + HEZ_FILE_EXTENSION;
    }

    /**
     * Validates whether a file appears to be a valid .hez file.
     *
     * @param file the file to check
     * @return true if the file appears to be a valid .hez archive
     */
    public boolean isValidHezFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }
        if (!file.getName().toLowerCase().endsWith(HEZ_FILE_EXTENSION)) {
            return false;
        }
        try {
            byte[] data = readBytes(file);
            return isValidHezData(data);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Validates whether bytes appear to be valid .hez data.
     *
     * @param data the data to check
     * @return true if the data appears to be a valid .hez archive
     */
    public boolean isValidHezData(byte[] data) {
        if (data == null || data.length < 4) {
            return false;
        }
        // Check for ZIP magic number (PK\x03\x04)
        return data[0] == 0x50 && data[1] == 0x4B && data[2] == 0x03 && data[3] == 0x04;
    }

    private byte[] createZipArchive(byte[] encryptedData) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry(ENCRYPTED_BACKUP_ENTRY_NAME);
            zos.putNextEntry(entry);
            zos.write(encryptedData);
            zos.closeEntry();
            zos.finish();
            return baos.toByteArray();
        }
    }

    private byte[] extractFromZipArchive(byte[] zipData) throws BackupException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(zipData);
             ZipInputStream zis = new ZipInputStream(bais)) {
            ZipEntry entry = zis.getNextEntry();
            if (entry == null) {
                throw new BackupException("Invalid .hez file: no entries found");
            }
            if (!ENCRYPTED_BACKUP_ENTRY_NAME.equals(entry.getName())) {
                throw new BackupException("Invalid .hez file: unexpected entry name");
            }
            return readAllBytes(zis);
        } catch (BackupException e) {
            throw e;
        } catch (IOException e) {
            throw new BackupException("Failed to extract from .hez archive", e);
        }
    }

    private BackupData collectBackupData(User user,
                                         HabitRepository habitRepository,
                                         HabitCategoryRepository categoryRepository,
                                         DiaryEntryRepository diaryEntryRepository,
                                         SleepEntryRepository sleepEntryRepository) {
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
            var habits = habitRepository.findByUserId(user.getId());
            for (var habit : habits) {
                data.getHabits().add(convertHabit(habit));
            }
        }

        if (categoryRepository != null) {
            var categories = categoryRepository.findByUserId(user.getId());
            for (var cat : categories) {
                BackupData.CategoryData catData = new BackupData.CategoryData();
                catData.setId(cat.getId());
                catData.setName(cat.getName());
                catData.setDescription(cat.getDescription());
                catData.setColor(cat.getColor());
                data.getCategories().add(catData);
            }
        }

        if (diaryEntryRepository != null) {
            var diaryEntries = diaryEntryRepository.findByUserId(user.getId());
            for (var entry : diaryEntries) {
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
            var sleepEntries = sleepEntryRepository.findByUserId(user.getId());
            for (var entry : sleepEntries) {
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

        return data;
    }

    private BackupData.HabitData convertHabit(de.idrinth.habitevaluator.shared.model.Habit habit) {
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

        var rule = habit.getScoringRule();
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
            for (var entry : habit.getEntries()) {
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

    private static void writeBytes(File file, byte[] data) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        }
    }

    private static byte[] readBytes(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            return readAllBytes(is);
        }
    }

    private static byte[] readAllBytes(InputStream is) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            return bos.toByteArray();
        }
    }
}
