package de.idrinth.habitevaluator.shared.backup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.ScoringRule;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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
