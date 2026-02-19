package de.idrinth.habitevaluator.shared.backup;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.EventSignificance;
import de.idrinth.habitevaluator.shared.model.FoodLog;
import de.idrinth.habitevaluator.shared.model.FoodTag;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.Medication;
import de.idrinth.habitevaluator.shared.model.MedicationLog;
import de.idrinth.habitevaluator.shared.model.MedicationProvisionType;
import de.idrinth.habitevaluator.shared.model.MeetingEntry;
import de.idrinth.habitevaluator.shared.model.ReminderSettings;
import de.idrinth.habitevaluator.shared.model.ScoringRule;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.SportLog;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;
import de.idrinth.habitevaluator.shared.repository.FoodLogRepository;
import de.idrinth.habitevaluator.shared.repository.FoodTagRepository;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.MedicationLogRepository;
import de.idrinth.habitevaluator.shared.repository.MedicationRepository;
import de.idrinth.habitevaluator.shared.repository.MeetingEntryRepository;
import de.idrinth.habitevaluator.shared.repository.ReminderSettingsRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import de.idrinth.habitevaluator.shared.repository.SportLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BackupServiceTest {

    private BackupService backupService;
    private User user;
    private InMemoryHabitRepository habitRepository;
    private InMemoryHabitCategoryRepository categoryRepository;
    private InMemoryDiaryEntryRepository diaryEntryRepository;
    private InMemorySleepEntryRepository sleepEntryRepository;

    @TempDir
    File tempDir;

    @BeforeEach
    void setUp() {
        backupService = new BackupService();
        user = new User("testuser", "password", "test@test.com");
        habitRepository = new InMemoryHabitRepository();
        categoryRepository = new InMemoryHabitCategoryRepository();
        diaryEntryRepository = new InMemoryDiaryEntryRepository();
        sleepEntryRepository = new InMemorySleepEntryRepository();
    }

    @Test
    void testCreateBackupEmptyPasswordThrows() {
        assertThrows(BackupException.class, () ->
                backupService.createBackup(tempDir, "", user, habitRepository,
                        categoryRepository, diaryEntryRepository, sleepEntryRepository));
    }

    @Test
    void testCreateBackupNullPasswordThrows() {
        assertThrows(BackupException.class, () ->
                backupService.createBackup(tempDir, null, user, habitRepository,
                        categoryRepository, diaryEntryRepository, sleepEntryRepository));
    }

    @Test
    void testCreateBackupNullUserThrows() {
        assertThrows(BackupException.class, () ->
                backupService.createBackup(tempDir, "password", null, habitRepository,
                        categoryRepository, diaryEntryRepository, sleepEntryRepository));
    }

    @Test
    void testCreateAndRestoreBackup() throws BackupException {
        Habit habit = new Habit("Exercise", "Daily workout");
        habit.setUser(user);
        habitRepository.save(habit);

        backupService.createBackup(tempDir, "password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        File[] backups = backupService.listBackups(tempDir);
        assertEquals(1, backups.length);

        BackupData restored = backupService.restoreBackup(backups[0], "password");
        assertNotNull(restored);
        assertNotNull(restored.getUser());
        assertEquals("testuser", restored.getUser().getUsername());
        assertEquals(1, restored.getHabits().size());
        assertEquals("Exercise", restored.getHabits().get(0).getName());
    }

    @Test
    void testRestoreBackupWrongPassword() throws BackupException {
        habitRepository.save(new Habit("Test", "Test"));
        backupService.createBackup(tempDir, "password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        File[] backups = backupService.listBackups(tempDir);
        assertThrows(BackupException.class, () ->
                backupService.restoreBackup(backups[0], "wrongpassword"));
    }

    @Test
    void testRestoreBackupEmptyPasswordThrows() {
        File fakeFile = new File(tempDir, "test.backup");
        assertThrows(BackupException.class, () ->
                backupService.restoreBackup(fakeFile, ""));
    }

    @Test
    void testRestoreBackupNonexistentFileThrows() {
        File nonExistent = new File(tempDir, "nonexistent.backup");
        assertThrows(BackupException.class, () ->
                backupService.restoreBackup(nonExistent, "password"));
    }

    @Test
    void testHasTodaysBackup() throws BackupException {
        assertFalse(backupService.hasTodaysBackup(tempDir));

        backupService.createBackup(tempDir, "password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        assertTrue(backupService.hasTodaysBackup(tempDir));
    }

    @Test
    void testListBackupsEmptyDir() {
        File[] backups = backupService.listBackups(tempDir);
        assertEquals(0, backups.length);
    }

    @Test
    void testListBackupsNonexistentDir() {
        File nonExistent = new File(tempDir, "nonexistent");
        File[] backups = backupService.listBackups(nonExistent);
        assertEquals(0, backups.length);
    }

    @Test
    void testBackupIncludesDiaryEntries() throws BackupException {
        DiaryEntry entry = new DiaryEntry("Good event", EventSignificance.MAJOR, LocalDate.of(2026, 1, 15));
        entry.setUser(user);
        diaryEntryRepository.save(entry);

        backupService.createBackup(tempDir, "password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        File[] backups = backupService.listBackups(tempDir);
        BackupData restored = backupService.restoreBackup(backups[0], "password");

        assertEquals(1, restored.getDiaryEntries().size());
        assertEquals("Good event", restored.getDiaryEntries().get(0).getDescription());
    }

    @Test
    void testBackupIncludesSleepEntries() throws BackupException {
        SleepEntry entry = new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0), LocalDate.of(2026, 1, 15));
        entry.setUser(user);
        sleepEntryRepository.save(entry);

        backupService.createBackup(tempDir, "password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        File[] backups = backupService.listBackups(tempDir);
        BackupData restored = backupService.restoreBackup(backups[0], "password");

        assertEquals(1, restored.getSleepEntries().size());
    }

    @Test
    void testBackupIncludesCategories() throws BackupException {
        HabitCategory category = new HabitCategory("Hobbies", "Fun stuff", "#4CAF50");
        category.setUser(user);
        categoryRepository.save(category);

        backupService.createBackup(tempDir, "password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        File[] backups = backupService.listBackups(tempDir);
        BackupData restored = backupService.restoreBackup(backups[0], "password");

        assertEquals(1, restored.getCategories().size());
        assertEquals("Hobbies", restored.getCategories().get(0).getName());
    }

    @Test
    void testMergeBackupNullUserThrows() throws BackupException {
        backupService.createBackup(tempDir, "password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        File[] backups = backupService.listBackups(tempDir);
        assertThrows(BackupException.class, () ->
                backupService.mergeBackup(backups[0], "password", null, habitRepository,
                        categoryRepository, diaryEntryRepository, sleepEntryRepository));
    }

    @Test
    void testMergeBackupAddsNewHabits() throws BackupException {
        Habit habit = new Habit("Exercise", "Test");
        habit.setUser(user);
        habitRepository.save(habit);

        backupService.createBackup(tempDir, "password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        // Clear local data
        habitRepository.deleteById(habit.getId());
        assertTrue(habitRepository.findByUserId(user.getId()).isEmpty());

        File[] backups = backupService.listBackups(tempDir);
        MergeResult result = backupService.mergeBackup(backups[0], "password", user,
                habitRepository, categoryRepository, diaryEntryRepository, sleepEntryRepository);

        assertEquals(1, result.getHabitsAdded());
    }

    @Test
    void testPartialMergeHabitsOnly() throws BackupException {
        Habit habit = new Habit("Exercise", "Test");
        habit.setUser(user);
        habitRepository.save(habit);

        DiaryEntry diary = new DiaryEntry("Test event", EventSignificance.NORMAL, LocalDate.of(2026, 1, 15));
        diary.setUser(user);
        diaryEntryRepository.save(diary);

        SleepEntry sleep = new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0), LocalDate.of(2026, 1, 15));
        sleep.setUser(user);
        sleepEntryRepository.save(sleep);

        backupService.createBackup(tempDir, "password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        // Clear local data
        habitRepository.deleteById(habit.getId());
        diaryEntryRepository.deleteById(diary.getId());
        sleepEntryRepository.deleteById(sleep.getId());

        // Restore only habits
        RestoreOptions options = RestoreOptions.none();
        options.setRestoreHabits(true);

        File[] backups = backupService.listBackups(tempDir);
        MergeResult result = backupService.mergeBackup(backups[0], "password", user,
                habitRepository, categoryRepository, diaryEntryRepository, sleepEntryRepository,
                options);

        assertEquals(1, result.getHabitsAdded());
        assertEquals(0, result.getDiaryEntriesAdded());
        assertEquals(0, result.getSleepEntriesAdded());
        assertTrue(diaryEntryRepository.findByUserId(user.getId()).isEmpty());
        assertTrue(sleepEntryRepository.findByUserId(user.getId()).isEmpty());
    }

    @Test
    void testPartialMergeDiaryOnly() throws BackupException {
        Habit habit = new Habit("Exercise", "Test");
        habit.setUser(user);
        habitRepository.save(habit);

        DiaryEntry diary = new DiaryEntry("Test event", EventSignificance.NORMAL, LocalDate.of(2026, 1, 15));
        diary.setUser(user);
        diaryEntryRepository.save(diary);

        backupService.createBackup(tempDir, "password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        habitRepository.deleteById(habit.getId());
        diaryEntryRepository.deleteById(diary.getId());

        RestoreOptions options = RestoreOptions.none();
        options.setRestoreDiaryEntries(true);

        File[] backups = backupService.listBackups(tempDir);
        MergeResult result = backupService.mergeBackup(backups[0], "password", user,
                habitRepository, categoryRepository, diaryEntryRepository, sleepEntryRepository,
                options);

        assertEquals(0, result.getHabitsAdded());
        assertEquals(1, result.getDiaryEntriesAdded());
        assertTrue(habitRepository.findByUserId(user.getId()).isEmpty());
    }

    @Test
    void testPartialMergeSleepOnly() throws BackupException {
        SleepEntry sleep = new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0), LocalDate.of(2026, 1, 15));
        sleep.setUser(user);
        sleepEntryRepository.save(sleep);

        Habit habit = new Habit("Exercise", "Test");
        habit.setUser(user);
        habitRepository.save(habit);

        backupService.createBackup(tempDir, "password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        sleepEntryRepository.deleteById(sleep.getId());
        habitRepository.deleteById(habit.getId());

        RestoreOptions options = RestoreOptions.none();
        options.setRestoreSleepEntries(true);

        File[] backups = backupService.listBackups(tempDir);
        MergeResult result = backupService.mergeBackup(backups[0], "password", user,
                habitRepository, categoryRepository, diaryEntryRepository, sleepEntryRepository,
                options);

        assertEquals(0, result.getHabitsAdded());
        assertEquals(1, result.getSleepEntriesAdded());
        assertTrue(habitRepository.findByUserId(user.getId()).isEmpty());
    }

    @Test
    void testPartialMergeNoneRestoresNothing() throws BackupException {
        Habit habit = new Habit("Exercise", "Test");
        habit.setUser(user);
        habitRepository.save(habit);

        DiaryEntry diary = new DiaryEntry("Test event", EventSignificance.NORMAL, LocalDate.of(2026, 1, 15));
        diary.setUser(user);
        diaryEntryRepository.save(diary);

        SleepEntry sleep = new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0), LocalDate.of(2026, 1, 15));
        sleep.setUser(user);
        sleepEntryRepository.save(sleep);

        HabitCategory category = new HabitCategory("Health", "Stuff", "#FF0000");
        category.setUser(user);
        categoryRepository.save(category);

        backupService.createBackup(tempDir, "password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        habitRepository.deleteById(habit.getId());
        diaryEntryRepository.deleteById(diary.getId());
        sleepEntryRepository.deleteById(sleep.getId());
        categoryRepository.deleteById(category.getId());

        RestoreOptions options = RestoreOptions.none();

        File[] backups = backupService.listBackups(tempDir);
        MergeResult result = backupService.mergeBackup(backups[0], "password", user,
                habitRepository, categoryRepository, diaryEntryRepository, sleepEntryRepository,
                options);

        assertEquals(0, result.getTotalChanges());
        assertTrue(habitRepository.findByUserId(user.getId()).isEmpty());
        assertTrue(diaryEntryRepository.findByUserId(user.getId()).isEmpty());
        assertTrue(sleepEntryRepository.findByUserId(user.getId()).isEmpty());
        assertTrue(categoryRepository.findByUserId(user.getId()).isEmpty());
    }

    @Test
    void testPartialMergeHabitsAutoIncludesCategories() throws BackupException {
        HabitCategory category = new HabitCategory("Health", "Healthy stuff", "#00FF00");
        category.setUser(user);
        categoryRepository.save(category);

        Habit habit = new Habit("Exercise", "Test");
        habit.setUser(user);
        habit.setCategoryId(category.getId());
        habitRepository.save(habit);

        backupService.createBackup(tempDir, "password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        habitRepository.deleteById(habit.getId());
        categoryRepository.deleteById(category.getId());

        // Restore only habits (not categories explicitly)
        RestoreOptions options = RestoreOptions.none();
        options.setRestoreHabits(true);

        File[] backups = backupService.listBackups(tempDir);
        MergeResult result = backupService.mergeBackup(backups[0], "password", user,
                habitRepository, categoryRepository, diaryEntryRepository, sleepEntryRepository,
                options);

        assertEquals(1, result.getHabitsAdded());
        assertEquals(1, result.getCategoriesAdded());
        assertFalse(categoryRepository.findByUserId(user.getId()).isEmpty());
    }

    @Test
    void testBackupIncludesEmotionData() {
        InMemoryEmotionPairRepository emotionPairRepository = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository emotionEntryRepository = new InMemoryEmotionEntryRepository();

        EmotionPair pair = new EmotionPair("Sad", "Happy");
        pair.setUser(user);
        emotionPairRepository.save(pair);

        EmotionEntry entry = new EmotionEntry(pair, 7,
                LocalDateTime.of(2026, 1, 15, 14, 30), "Feeling good");
        entry.setUser(user);
        emotionEntryRepository.save(entry);

        BackupData data = backupService.collectBackupData(user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository,
                null, null, emotionPairRepository, emotionEntryRepository, null);

        assertEquals(1, data.getEmotionPairs().size());
        assertEquals("Sad", data.getEmotionPairs().get(0).getNegativeLabel());
        assertEquals("Happy", data.getEmotionPairs().get(0).getPositiveLabel());
        assertEquals(1, data.getEmotionEntries().size());
        assertEquals(7, data.getEmotionEntries().get(0).getStrength());
        assertEquals(pair.getId(), data.getEmotionEntries().get(0).getEmotionPairId());
    }

    @Test
    void testBackupIncludesReminderSettings() {
        InMemoryReminderSettingsRepository reminderSettingsRepository =
                new InMemoryReminderSettingsRepository();

        ReminderSettings settings = new ReminderSettings();
        settings.setUser(user);
        settings.setSleepReminderEnabled(true);
        settings.setSleepReminderTime(LocalTime.of(8, 0));
        settings.setDiaryReminderEnabled(true);
        settings.setDiaryReminderTime(LocalTime.of(20, 0));
        settings.setEmotionReminderEnabled(true);
        settings.setEmotionReminderCount(5);
        reminderSettingsRepository.save(settings);

        BackupData data = backupService.collectBackupData(user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository,
                null, null, null, null, reminderSettingsRepository);

        assertNotNull(data.getReminderSettings());
        assertTrue(data.getReminderSettings().isSleepReminderEnabled());
        assertEquals("08:00", data.getReminderSettings().getSleepReminderTime());
        assertTrue(data.getReminderSettings().isDiaryReminderEnabled());
        assertTrue(data.getReminderSettings().isEmotionReminderEnabled());
        assertEquals(5, data.getReminderSettings().getEmotionReminderCount());
    }

    @Test
    void testBackupIncludesDiaryStartEndTime() throws BackupException {
        DiaryEntry entry = new DiaryEntry("Meeting", EventSignificance.NORMAL, LocalDate.of(2026, 1, 15));
        entry.setStartTime(LocalTime.of(9, 0));
        entry.setEndTime(LocalTime.of(10, 30));
        entry.setUser(user);
        diaryEntryRepository.save(entry);

        backupService.createBackup(tempDir, "password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        File[] backups = backupService.listBackups(tempDir);
        BackupData restored = backupService.restoreBackup(backups[0], "password");

        assertEquals(1, restored.getDiaryEntries().size());
        assertEquals("09:00", restored.getDiaryEntries().get(0).getStartTime());
        assertEquals("10:30", restored.getDiaryEntries().get(0).getEndTime());
    }

    @Test
    void testMergeEmotionData() throws BackupException {
        InMemoryEmotionPairRepository emotionPairRepository = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository emotionEntryRepository = new InMemoryEmotionEntryRepository();

        // Create backup data with emotion information
        BackupData backupData = new BackupData();
        BackupData.UserData userData = new BackupData.UserData();
        userData.setId(user.getId());
        userData.setUsername(user.getUsername());
        backupData.setUser(userData);

        BackupData.EmotionPairData pairData = new BackupData.EmotionPairData();
        pairData.setId("ep-1");
        pairData.setNegativeLabel("Tired");
        pairData.setPositiveLabel("Energetic");
        backupData.getEmotionPairs().add(pairData);

        BackupData.EmotionEntryData entryData = new BackupData.EmotionEntryData();
        entryData.setId("ee-1");
        entryData.setEmotionPairId("ep-1");
        entryData.setStrength(5);
        entryData.setRecordedAt("2026-01-15T14:30:00");
        entryData.setNotes("Feeling energetic");
        backupData.getEmotionEntries().add(entryData);

        MergeResult result = backupService.mergeBackupData(backupData, user,
                habitRepository, categoryRepository, diaryEntryRepository,
                sleepEntryRepository, null, null, null,
                emotionPairRepository, emotionEntryRepository, null,
                RestoreOptions.all());

        assertEquals(1, result.getEmotionPairsAdded());
        assertEquals(1, result.getEmotionEntriesAdded());
        assertEquals(1, emotionPairRepository.findByUserId(user.getId()).size());
        assertEquals(1, emotionEntryRepository.findByUserId(user.getId()).size());
    }

    @Test
    void testMergeReminderSettings() throws BackupException {
        InMemoryReminderSettingsRepository reminderSettingsRepository =
                new InMemoryReminderSettingsRepository();

        BackupData backupData = new BackupData();
        BackupData.UserData userData = new BackupData.UserData();
        userData.setId(user.getId());
        userData.setUsername(user.getUsername());
        backupData.setUser(userData);

        BackupData.ReminderSettingsData settingsData = new BackupData.ReminderSettingsData();
        settingsData.setId("rs-1");
        settingsData.setSleepReminderEnabled(true);
        settingsData.setSleepReminderTime("08:30");
        settingsData.setDiaryReminderEnabled(true);
        settingsData.setDiaryReminderTime("21:00");
        settingsData.setEmotionReminderEnabled(false);
        settingsData.setEmotionReminderCount(3);
        settingsData.setWakingHoursStart("07:00");
        settingsData.setWakingHoursEnd("23:00");
        backupData.setReminderSettings(settingsData);

        MergeResult result = backupService.mergeBackupData(backupData, user,
                habitRepository, categoryRepository, diaryEntryRepository,
                sleepEntryRepository, null, null, null,
                null, null, reminderSettingsRepository,
                RestoreOptions.all());

        assertTrue(result.isReminderSettingsRestored());
        Optional<ReminderSettings> restored = reminderSettingsRepository.findByUserId(user.getId());
        assertTrue(restored.isPresent());
        assertTrue(restored.get().isSleepReminderEnabled());
        assertEquals(LocalTime.of(8, 30), restored.get().getSleepReminderTime());
        assertTrue(restored.get().isDiaryReminderEnabled());
        assertFalse(restored.get().isEmotionReminderEnabled());
    }

    @Test
    void testMergeDiaryEntryPreservesStartEndTime() throws BackupException {
        BackupData backupData = new BackupData();
        BackupData.UserData userData = new BackupData.UserData();
        userData.setId(user.getId());
        userData.setUsername(user.getUsername());
        backupData.setUser(userData);

        BackupData.DiaryEntryData diaryData = new BackupData.DiaryEntryData();
        diaryData.setId("de-1");
        diaryData.setDescription("Team meeting");
        diaryData.setSignificance("NORMAL");
        diaryData.setEventDate("2026-01-15");
        diaryData.setStartTime("09:00");
        diaryData.setEndTime("10:30");
        backupData.getDiaryEntries().add(diaryData);

        MergeResult result = backupService.mergeBackupData(backupData, user,
                habitRepository, categoryRepository, diaryEntryRepository,
                sleepEntryRepository);

        assertEquals(1, result.getDiaryEntriesAdded());
        List<DiaryEntry> restored = diaryEntryRepository.findByUserId(user.getId());
        assertEquals(1, restored.size());
        assertEquals(LocalTime.of(9, 0), restored.get(0).getStartTime());
        assertEquals(LocalTime.of(10, 30), restored.get(0).getEndTime());
    }

    @Test
    void testPartialMergeEmotionDataSkippedWhenDisabled() throws BackupException {
        InMemoryEmotionPairRepository emotionPairRepository = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository emotionEntryRepository = new InMemoryEmotionEntryRepository();

        BackupData backupData = new BackupData();
        BackupData.UserData userData = new BackupData.UserData();
        userData.setId(user.getId());
        userData.setUsername(user.getUsername());
        backupData.setUser(userData);

        BackupData.EmotionPairData pairData = new BackupData.EmotionPairData();
        pairData.setId("ep-1");
        pairData.setNegativeLabel("Tired");
        pairData.setPositiveLabel("Energetic");
        backupData.getEmotionPairs().add(pairData);

        RestoreOptions options = RestoreOptions.none();

        MergeResult result = backupService.mergeBackupData(backupData, user,
                habitRepository, categoryRepository, diaryEntryRepository,
                sleepEntryRepository, null, null, null,
                emotionPairRepository, emotionEntryRepository, null,
                options);

        assertEquals(0, result.getEmotionPairsAdded());
        assertTrue(emotionPairRepository.findByUserId(user.getId()).isEmpty());
    }

    @Test
    void testCollectBackupDataIncludesSportLogs() {
        InMemorySportLogRepository sportLogRepository = new InMemorySportLogRepository();

        SportLog sportLog = new SportLog("Running", 5.0, "km",
                LocalTime.of(7, 0), LocalTime.of(7, 45));
        sportLog.setUser(user);
        sportLog.setNotes("Morning run");
        sportLogRepository.save(sportLog);

        BackupData data = backupService.collectBackupData(user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository,
                sportLogRepository);

        assertEquals(1, data.getSportLogs().size());
        assertEquals("Running", data.getSportLogs().get(0).getName());
        assertEquals(5.0, data.getSportLogs().get(0).getMeasurement());
        assertEquals("km", data.getSportLogs().get(0).getMeasurementUnit());
        assertEquals("Morning run", data.getSportLogs().get(0).getNotes());
    }

    @Test
    void testCollectBackupDataIncludesFoodLogs() {
        InMemorySportLogRepository sportLogRepository = new InMemorySportLogRepository();
        InMemoryFoodLogRepository foodLogRepository = new InMemoryFoodLogRepository();

        FoodLog foodLog = new FoodLog(30.5, 450, LocalDateTime.of(2026, 1, 15, 12, 0), "Rice, Chicken");
        foodLog.setUser(user);
        foodLog.setNotes("Lunch");
        foodLogRepository.save(foodLog);

        BackupData data = backupService.collectBackupData(user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository,
                sportLogRepository, foodLogRepository);

        assertEquals(1, data.getFoodLogs().size());
        assertEquals(30.5, data.getFoodLogs().get(0).getCarbohydrates());
        assertEquals(450, data.getFoodLogs().get(0).getKcal());
        assertEquals("Lunch", data.getFoodLogs().get(0).getNotes());
    }

    @Test
    void testCollectBackupDataIncludesMeetingEntries() {
        InMemoryMeetingEntryRepository meetingEntryRepository = new InMemoryMeetingEntryRepository();
        InMemoryMedicationRepository medicationRepository = new InMemoryMedicationRepository();
        InMemoryMedicationLogRepository medicationLogRepository = new InMemoryMedicationLogRepository();

        MeetingEntry meeting = new MeetingEntry("Office", "Alice, Bob",
                LocalTime.of(14, 0), LocalTime.of(15, 30));
        meeting.setUser(user);
        meetingEntryRepository.save(meeting);

        BackupData data = backupService.collectBackupData(user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository,
                null, null, null, null,
                meetingEntryRepository, null,
                medicationRepository, medicationLogRepository);

        assertEquals(1, data.getMeetingEntries().size());
        assertEquals("Office", data.getMeetingEntries().get(0).getPlace());
        assertEquals("Alice, Bob", data.getMeetingEntries().get(0).getAttendants());
    }

    @Test
    void testCollectBackupDataIncludesMedications() {
        InMemoryMeetingEntryRepository meetingEntryRepository = new InMemoryMeetingEntryRepository();
        InMemoryMedicationRepository medicationRepository = new InMemoryMedicationRepository();
        InMemoryMedicationLogRepository medicationLogRepository = new InMemoryMedicationLogRepository();

        Medication medication = new Medication("Ibuprofen", MedicationProvisionType.PILL);
        medication.setWikipediaLink("https://en.wikipedia.org/wiki/Ibuprofen");
        medication.setUser(user);
        medicationRepository.save(medication);

        MedicationLog log = new MedicationLog(medication, 400.0, LocalDateTime.of(2026, 1, 15, 8, 0));
        log.setNotes("After breakfast");
        log.setUser(user);
        medicationLogRepository.save(log);

        BackupData data = backupService.collectBackupData(user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository,
                null, null, null, null,
                meetingEntryRepository, null,
                medicationRepository, medicationLogRepository);

        assertEquals(1, data.getMedications().size());
        assertEquals("Ibuprofen", data.getMedications().get(0).getName());
        assertEquals("PILL", data.getMedications().get(0).getProvisionType());
        assertEquals("https://en.wikipedia.org/wiki/Ibuprofen", data.getMedications().get(0).getWikipediaLink());

        assertEquals(1, data.getMedicationLogs().size());
        assertEquals(400.0, data.getMedicationLogs().get(0).getAmount());
        assertEquals("After breakfast", data.getMedicationLogs().get(0).getNotes());
    }

    @Test
    void testMergeSportLogs() throws BackupException {
        InMemorySportLogRepository sportLogRepository = new InMemorySportLogRepository();

        BackupData backupData = new BackupData();
        BackupData.UserData userData = new BackupData.UserData();
        userData.setId(user.getId());
        userData.setUsername(user.getUsername());
        backupData.setUser(userData);

        BackupData.SportLogData sportData = new BackupData.SportLogData();
        sportData.setId("sl-1");
        sportData.setName("Running");
        sportData.setMeasurement(5.0);
        sportData.setMeasurementUnit("km");
        sportData.setStartTime("07:00");
        sportData.setEndTime("07:45");
        sportData.setDate("2026-01-15");
        sportData.setNotes("Park run");
        sportData.setCreatedAt("2026-01-15T07:00:00");
        backupData.getSportLogs().add(sportData);

        MergeResult result = backupService.mergeBackupData(backupData, user,
                habitRepository, categoryRepository, diaryEntryRepository,
                sleepEntryRepository, sportLogRepository);

        assertEquals(1, result.getSportLogsAdded());
        assertEquals(1, sportLogRepository.findByUserId(user.getId()).size());
        SportLog restored = sportLogRepository.findByUserId(user.getId()).get(0);
        assertEquals("Running", restored.getName());
        assertEquals(5.0, restored.getMeasurement());
        assertEquals("km", restored.getMeasurementUnit());
        assertEquals("Park run", restored.getNotes());
    }

    @Test
    void testMergeSportLogsDeduplicate() throws BackupException {
        InMemorySportLogRepository sportLogRepository = new InMemorySportLogRepository();

        // Pre-existing sport log
        SportLog existing = new SportLog("Running", 5.0, "km",
                LocalTime.of(7, 0), LocalTime.of(7, 45));
        existing.setUser(user);
        sportLogRepository.save(existing);

        BackupData backupData = new BackupData();
        BackupData.UserData userData = new BackupData.UserData();
        userData.setId(user.getId());
        userData.setUsername(user.getUsername());
        backupData.setUser(userData);

        // Same ID - should be skipped
        BackupData.SportLogData sameId = new BackupData.SportLogData();
        sameId.setId(existing.getId());
        sameId.setName("Running");
        backupData.getSportLogs().add(sameId);

        // Different ID - should be added
        BackupData.SportLogData newLog = new BackupData.SportLogData();
        newLog.setId("sl-new");
        newLog.setName("Swimming");
        newLog.setMeasurement(1.0);
        newLog.setMeasurementUnit("km");
        newLog.setStartTime("18:00");
        newLog.setEndTime("19:00");
        newLog.setDate("2026-01-15");
        backupData.getSportLogs().add(newLog);

        MergeResult result = backupService.mergeBackupData(backupData, user,
                habitRepository, categoryRepository, diaryEntryRepository,
                sleepEntryRepository, sportLogRepository);

        assertEquals(1, result.getSportLogsAdded());
        assertEquals(2, sportLogRepository.findByUserId(user.getId()).size());
    }

    @Test
    void testMergeFoodLogs() throws BackupException {
        InMemoryFoodLogRepository foodLogRepository = new InMemoryFoodLogRepository();

        BackupData backupData = new BackupData();
        BackupData.UserData userData = new BackupData.UserData();
        userData.setId(user.getId());
        userData.setUsername(user.getUsername());
        backupData.setUser(userData);

        BackupData.FoodLogData foodData = new BackupData.FoodLogData();
        foodData.setId("fl-1");
        foodData.setCarbohydrates(45.5);
        foodData.setKcal(350);
        foodData.setDateTime("2026-01-15T12:30:00");
        foodData.setFoodItems("Rice, Chicken");
        foodData.setNotes("Lunch");
        foodData.setCreatedAt("2026-01-15T12:30:00");
        backupData.getFoodLogs().add(foodData);

        MergeResult result = backupService.mergeBackupData(backupData, user,
                habitRepository, categoryRepository, diaryEntryRepository,
                sleepEntryRepository, null, foodLogRepository);

        assertEquals(1, result.getFoodLogsAdded());
        assertEquals(1, foodLogRepository.findByUserId(user.getId()).size());
    }

    @Test
    void testMergeMedications() throws BackupException {
        InMemoryMedicationRepository medicationRepository = new InMemoryMedicationRepository();
        InMemoryMedicationLogRepository medicationLogRepository = new InMemoryMedicationLogRepository();

        BackupData backupData = new BackupData();
        BackupData.UserData userData = new BackupData.UserData();
        userData.setId(user.getId());
        userData.setUsername(user.getUsername());
        backupData.setUser(userData);

        BackupData.MedicationData medData = new BackupData.MedicationData();
        medData.setId("med-1");
        medData.setName("Ibuprofen");
        medData.setProvisionType("PILL");
        medData.setWikipediaLink("https://en.wikipedia.org/wiki/Ibuprofen");
        backupData.getMedications().add(medData);

        BackupData.MedicationLogData logData = new BackupData.MedicationLogData();
        logData.setId("ml-1");
        logData.setMedicationId("med-1");
        logData.setAmount(400.0);
        logData.setTakenAt("2026-01-15T08:00:00");
        logData.setNotes("After breakfast");
        logData.setCreatedAt("2026-01-15T08:00:00");
        backupData.getMedicationLogs().add(logData);

        MergeResult result = backupService.mergeBackupData(backupData, user,
                habitRepository, categoryRepository, diaryEntryRepository,
                sleepEntryRepository, null, null, null,
                null, null,
                null, medicationRepository,
                medicationLogRepository, RestoreOptions.all());

        assertEquals(1, result.getMedicationsAdded());
        assertEquals(1, result.getMedicationLogsAdded());
        assertEquals(1, medicationRepository.findByUserId(user.getId()).size());
        assertEquals(1, medicationLogRepository.findByUserId(user.getId()).size());
    }

    @Test
    void testMergeMedicationsDeduplicate() throws BackupException {
        InMemoryMedicationRepository medicationRepository = new InMemoryMedicationRepository();
        InMemoryMedicationLogRepository medicationLogRepository = new InMemoryMedicationLogRepository();

        Medication existing = new Medication("Ibuprofen", MedicationProvisionType.PILL);
        existing.setUser(user);
        medicationRepository.save(existing);

        BackupData backupData = new BackupData();
        BackupData.UserData userData = new BackupData.UserData();
        userData.setId(user.getId());
        userData.setUsername(user.getUsername());
        backupData.setUser(userData);

        // Same name + type - should be matched, not duplicated
        BackupData.MedicationData medData = new BackupData.MedicationData();
        medData.setId("med-backup");
        medData.setName("Ibuprofen");
        medData.setProvisionType("PILL");
        backupData.getMedications().add(medData);

        // Log referencing the backup medication ID
        BackupData.MedicationLogData logData = new BackupData.MedicationLogData();
        logData.setId("ml-1");
        logData.setMedicationId("med-backup");
        logData.setAmount(200.0);
        logData.setTakenAt("2026-01-15T08:00:00");
        backupData.getMedicationLogs().add(logData);

        MergeResult result = backupService.mergeBackupData(backupData, user,
                habitRepository, categoryRepository, diaryEntryRepository,
                sleepEntryRepository, null, null, null,
                null, null,
                null, medicationRepository,
                medicationLogRepository, RestoreOptions.all());

        assertEquals(0, result.getMedicationsAdded());
        assertEquals(1, result.getMedicationLogsAdded());
        assertEquals(1, medicationRepository.findByUserId(user.getId()).size());
    }

    @Test
    void testMergeMedicationDataSkippedWhenDisabled() throws BackupException {
        InMemoryMedicationRepository medicationRepository = new InMemoryMedicationRepository();
        InMemoryMedicationLogRepository medicationLogRepository = new InMemoryMedicationLogRepository();

        BackupData backupData = new BackupData();
        BackupData.UserData userData = new BackupData.UserData();
        userData.setId(user.getId());
        userData.setUsername(user.getUsername());
        backupData.setUser(userData);

        BackupData.MedicationData medData = new BackupData.MedicationData();
        medData.setId("med-1");
        medData.setName("Aspirin");
        medData.setProvisionType("PILL");
        backupData.getMedications().add(medData);

        RestoreOptions options = RestoreOptions.none();

        MergeResult result = backupService.mergeBackupData(backupData, user,
                habitRepository, categoryRepository, diaryEntryRepository,
                sleepEntryRepository, null, null, null,
                null, null,
                null, medicationRepository,
                medicationLogRepository, options);

        assertEquals(0, result.getMedicationsAdded());
        assertTrue(medicationRepository.findByUserId(user.getId()).isEmpty());
    }

    @Test
    void testMergeExistingCategoryMatchesByName() throws BackupException {
        // Pre-existing category
        HabitCategory category = new HabitCategory("Health", "Healthy habits", "#00FF00");
        category.setUser(user);
        categoryRepository.save(category);

        BackupData backupData = new BackupData();
        BackupData.UserData userData = new BackupData.UserData();
        userData.setId(user.getId());
        userData.setUsername(user.getUsername());
        backupData.setUser(userData);

        // Backup has same category name with different ID
        BackupData.CategoryData catData = new BackupData.CategoryData();
        catData.setId("different-id");
        catData.setName("Health");
        catData.setDescription("Backup health");
        catData.setColor("#FF0000");
        backupData.getCategories().add(catData);

        MergeResult result = backupService.mergeBackupData(backupData, user,
                habitRepository, categoryRepository, diaryEntryRepository,
                sleepEntryRepository);

        assertEquals(0, result.getCategoriesAdded());
        assertEquals(1, categoryRepository.findByUserId(user.getId()).size());
    }

    @Test
    void testMergeExistingEmotionPairMatchesByLabels() throws BackupException {
        InMemoryEmotionPairRepository emotionPairRepository = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository emotionEntryRepository = new InMemoryEmotionEntryRepository();

        // Pre-existing pair
        EmotionPair existingPair = new EmotionPair("Sad", "Happy");
        existingPair.setUser(user);
        emotionPairRepository.save(existingPair);

        BackupData backupData = new BackupData();
        BackupData.UserData userData = new BackupData.UserData();
        userData.setId(user.getId());
        userData.setUsername(user.getUsername());
        backupData.setUser(userData);

        // Same labels, different ID
        BackupData.EmotionPairData pairData = new BackupData.EmotionPairData();
        pairData.setId("different-pair-id");
        pairData.setNegativeLabel("Sad");
        pairData.setPositiveLabel("Happy");
        backupData.getEmotionPairs().add(pairData);

        // Entry referencing the backup pair ID
        BackupData.EmotionEntryData entryData = new BackupData.EmotionEntryData();
        entryData.setId("ee-1");
        entryData.setEmotionPairId("different-pair-id");
        entryData.setStrength(5);
        entryData.setRecordedAt("2026-01-15T14:30:00");
        backupData.getEmotionEntries().add(entryData);

        MergeResult result = backupService.mergeBackupData(backupData, user,
                habitRepository, categoryRepository, diaryEntryRepository,
                sleepEntryRepository, null, null, null,
                emotionPairRepository, emotionEntryRepository, null,
                RestoreOptions.all());

        assertEquals(0, result.getEmotionPairsAdded());
        assertEquals(1, result.getEmotionEntriesAdded());
        assertEquals(1, emotionPairRepository.findByUserId(user.getId()).size());
        // Entry should be linked to the existing pair via ID mapping
        assertEquals(1, emotionEntryRepository.findByUserId(user.getId()).size());
    }

    @Test
    void testMergeHabitEntryDeduplication() throws BackupException {
        // Habit with an existing entry
        Habit habit = new Habit("Exercise", "Test");
        habit.setUser(user);
        HabitEntry existingEntry = new HabitEntry();
        existingEntry.setCompletedAt(LocalDateTime.of(2026, 1, 15, 10, 0));
        existingEntry.setNotes("Morning session");
        existingEntry.setValue(1);
        habit.addEntry(existingEntry);
        habitRepository.save(habit);

        BackupData backupData = new BackupData();
        BackupData.UserData userData = new BackupData.UserData();
        userData.setId(user.getId());
        userData.setUsername(user.getUsername());
        backupData.setUser(userData);

        BackupData.HabitData habitData = new BackupData.HabitData();
        habitData.setName("Exercise");
        habitData.setDescription("Test");

        // Entry with same ID - should be skipped
        BackupData.HabitEntryData duplicateEntry = new BackupData.HabitEntryData();
        duplicateEntry.setId(existingEntry.getId());
        duplicateEntry.setCompletedAt("2026-01-15T10:00:00");
        duplicateEntry.setValue(1);
        habitData.getEntries().add(duplicateEntry);

        // Entry with new ID - should be added
        BackupData.HabitEntryData newEntry = new BackupData.HabitEntryData();
        newEntry.setId("new-entry-id");
        newEntry.setCompletedAt("2026-01-16T10:00:00");
        newEntry.setValue(1);
        habitData.getEntries().add(newEntry);

        backupData.getHabits().add(habitData);

        MergeResult result = backupService.mergeBackupData(backupData, user,
                habitRepository, categoryRepository, diaryEntryRepository,
                sleepEntryRepository);

        assertEquals(0, result.getHabitsAdded());
        assertEquals(1, result.getHabitsMerged());
        assertEquals(1, result.getEntriesAdded());
        Habit merged = habitRepository.findByUserId(user.getId()).get(0);
        assertEquals(2, merged.getEntries().size());
    }

    @Test
    void testMergeNewHabitWithScoringRule() throws BackupException {
        BackupData backupData = new BackupData();
        BackupData.UserData userData = new BackupData.UserData();
        userData.setId(user.getId());
        userData.setUsername(user.getUsername());
        backupData.setUser(userData);

        BackupData.HabitData habitData = new BackupData.HabitData();
        habitData.setName("Read");
        habitData.setDescription("Daily reading");
        habitData.setFrequencyType("DAILY");
        habitData.setTargetFrequency(1);
        habitData.setMaxEntriesPerDay(1);
        habitData.setPositiveScoring(true);
        habitData.setCreatedAt("2026-01-01T00:00:00");

        BackupData.ScoringRuleData ruleData = new BackupData.ScoringRuleData();
        ruleData.setName("custom-reading");
        ruleData.setThresholdFor1Point(1);
        ruleData.setThresholdFor2Points(3);
        ruleData.setThresholdFor4Points(5);
        ruleData.setThresholdFor8Points(7);
        habitData.setScoringRule(ruleData);

        backupData.getHabits().add(habitData);

        MergeResult result = backupService.mergeBackupData(backupData, user,
                habitRepository, categoryRepository, diaryEntryRepository,
                sleepEntryRepository);

        assertEquals(1, result.getHabitsAdded());
        Habit restoredHabit = habitRepository.findByUserId(user.getId()).get(0);
        assertNotNull(restoredHabit.getScoringRule());
        assertEquals("custom-reading", restoredHabit.getScoringRule().getName());
        assertEquals(1, restoredHabit.getScoringRule().getThresholdFor1Point());
        assertEquals(3, restoredHabit.getScoringRule().getThresholdFor2Points());
        assertEquals(5, restoredHabit.getScoringRule().getThresholdFor4Points());
        assertEquals(7, restoredHabit.getScoringRule().getThresholdFor8Points());
    }

    @Test
    void testMergeNewHabitWithTranslations() throws BackupException {
        BackupData backupData = new BackupData();
        BackupData.UserData userData = new BackupData.UserData();
        userData.setId(user.getId());
        userData.setUsername(user.getUsername());
        backupData.setUser(userData);

        BackupData.HabitData habitData = new BackupData.HabitData();
        habitData.setName("Exercise");
        habitData.setDescription("Daily workout");
        habitData.setFrequencyType("DAILY");
        habitData.setTargetFrequency(1);

        java.util.Map<String, String> nameTranslations = new java.util.HashMap<>();
        nameTranslations.put("de", "Sport");
        nameTranslations.put("fr", "Exercice");
        habitData.setNameTranslations(nameTranslations);

        java.util.Map<String, String> descTranslations = new java.util.HashMap<>();
        descTranslations.put("de", "Tägliches Training");
        habitData.setDescriptionTranslations(descTranslations);

        backupData.getHabits().add(habitData);

        MergeResult result = backupService.mergeBackupData(backupData, user,
                habitRepository, categoryRepository, diaryEntryRepository,
                sleepEntryRepository);

        assertEquals(1, result.getHabitsAdded());
        Habit restoredHabit = habitRepository.findByUserId(user.getId()).get(0);
        assertEquals("Sport", restoredHabit.getNameTranslations().get("de"));
        assertEquals("Exercice", restoredHabit.getNameTranslations().get("fr"));
        assertEquals("Tägliches Training", restoredHabit.getDescriptionTranslations().get("de"));
    }

    @Test
    void testMergeDiaryEntryWithNullSignificanceDefaultsToNormal() throws BackupException {
        BackupData backupData = new BackupData();
        BackupData.UserData userData = new BackupData.UserData();
        userData.setId(user.getId());
        userData.setUsername(user.getUsername());
        backupData.setUser(userData);

        BackupData.DiaryEntryData diaryData = new BackupData.DiaryEntryData();
        diaryData.setId("de-1");
        diaryData.setDescription("Something happened");
        diaryData.setSignificance(null);
        diaryData.setEventDate("2026-01-15");
        backupData.getDiaryEntries().add(diaryData);

        MergeResult result = backupService.mergeBackupData(backupData, user,
                habitRepository, categoryRepository, diaryEntryRepository,
                sleepEntryRepository);

        assertEquals(1, result.getDiaryEntriesAdded());
        DiaryEntry restored = diaryEntryRepository.findByUserId(user.getId()).get(0);
        assertEquals(EventSignificance.NORMAL, restored.getSignificance());
    }

    @Test
    void testMergeDiaryEntryDeduplicate() throws BackupException {
        DiaryEntry existing = new DiaryEntry("Existing event", EventSignificance.MINOR, LocalDate.of(2026, 1, 15));
        existing.setUser(user);
        diaryEntryRepository.save(existing);

        BackupData backupData = new BackupData();
        BackupData.UserData userData = new BackupData.UserData();
        userData.setId(user.getId());
        userData.setUsername(user.getUsername());
        backupData.setUser(userData);

        // Same ID - should be skipped
        BackupData.DiaryEntryData sameId = new BackupData.DiaryEntryData();
        sameId.setId(existing.getId());
        sameId.setDescription("Existing event");
        sameId.setEventDate("2026-01-15");
        backupData.getDiaryEntries().add(sameId);

        // Different ID - should be added
        BackupData.DiaryEntryData newEntry = new BackupData.DiaryEntryData();
        newEntry.setId("de-new");
        newEntry.setDescription("New event");
        newEntry.setEventDate("2026-01-16");
        backupData.getDiaryEntries().add(newEntry);

        MergeResult result = backupService.mergeBackupData(backupData, user,
                habitRepository, categoryRepository, diaryEntryRepository,
                sleepEntryRepository);

        assertEquals(1, result.getDiaryEntriesAdded());
        assertEquals(2, diaryEntryRepository.findByUserId(user.getId()).size());
    }

    @Test
    void testMergeSleepEntryDeduplicate() throws BackupException {
        SleepEntry existing = new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0), LocalDate.of(2026, 1, 15));
        existing.setUser(user);
        sleepEntryRepository.save(existing);

        BackupData backupData = new BackupData();
        BackupData.UserData userData = new BackupData.UserData();
        userData.setId(user.getId());
        userData.setUsername(user.getUsername());
        backupData.setUser(userData);

        // Same ID - should be skipped
        BackupData.SleepEntryData sameId = new BackupData.SleepEntryData();
        sameId.setId(existing.getId());
        sameId.setFromTime("22:00");
        sameId.setUntilTime("06:00");
        sameId.setDate("2026-01-15");
        backupData.getSleepEntries().add(sameId);

        MergeResult result = backupService.mergeBackupData(backupData, user,
                habitRepository, categoryRepository, diaryEntryRepository,
                sleepEntryRepository);

        assertEquals(0, result.getSleepEntriesAdded());
        assertEquals(1, sleepEntryRepository.findByUserId(user.getId()).size());
    }

    @Test
    void testMergeReminderSettingsOverwritesExisting() throws BackupException {
        InMemoryReminderSettingsRepository reminderSettingsRepository =
                new InMemoryReminderSettingsRepository();

        // Pre-existing settings
        ReminderSettings existing = new ReminderSettings();
        existing.setUser(user);
        existing.setSleepReminderEnabled(false);
        existing.setSleepReminderTime(LocalTime.of(22, 0));
        reminderSettingsRepository.save(existing);

        BackupData backupData = new BackupData();
        BackupData.UserData userData = new BackupData.UserData();
        userData.setId(user.getId());
        userData.setUsername(user.getUsername());
        backupData.setUser(userData);

        BackupData.ReminderSettingsData settingsData = new BackupData.ReminderSettingsData();
        settingsData.setSleepReminderEnabled(true);
        settingsData.setSleepReminderTime("09:00");
        settingsData.setDiaryReminderEnabled(true);
        settingsData.setDiaryReminderTime("20:00");
        backupData.setReminderSettings(settingsData);

        MergeResult result = backupService.mergeBackupData(backupData, user,
                habitRepository, categoryRepository, diaryEntryRepository,
                sleepEntryRepository, null, null, null,
                null, null, reminderSettingsRepository,
                RestoreOptions.all());

        assertTrue(result.isReminderSettingsRestored());
        ReminderSettings restored = reminderSettingsRepository.findByUserId(user.getId()).get();
        assertTrue(restored.isSleepReminderEnabled());
        assertEquals(LocalTime.of(9, 0), restored.getSleepReminderTime());
        assertTrue(restored.isDiaryReminderEnabled());
    }

    @Test
    void testMergeWithNullOptionsDefaultsToAll() throws BackupException {
        BackupData backupData = new BackupData();
        BackupData.UserData userData = new BackupData.UserData();
        userData.setId(user.getId());
        userData.setUsername(user.getUsername());
        backupData.setUser(userData);

        BackupData.DiaryEntryData diaryData = new BackupData.DiaryEntryData();
        diaryData.setId("de-1");
        diaryData.setDescription("Test");
        diaryData.setEventDate("2026-01-15");
        backupData.getDiaryEntries().add(diaryData);

        // Passing null options should default to RestoreOptions.all()
        MergeResult result = backupService.mergeBackupData(backupData, user,
                habitRepository, categoryRepository, diaryEntryRepository,
                sleepEntryRepository, null, null, null, null);

        assertEquals(1, result.getDiaryEntriesAdded());
    }

    @Test
    void testMergePartialSportLogsOnly() throws BackupException {
        InMemorySportLogRepository sportLogRepository = new InMemorySportLogRepository();
        InMemoryFoodLogRepository foodLogRepository = new InMemoryFoodLogRepository();

        BackupData backupData = new BackupData();
        BackupData.UserData userData = new BackupData.UserData();
        userData.setId(user.getId());
        userData.setUsername(user.getUsername());
        backupData.setUser(userData);

        BackupData.SportLogData sportData = new BackupData.SportLogData();
        sportData.setId("sl-1");
        sportData.setName("Cycling");
        sportData.setStartTime("08:00");
        sportData.setEndTime("09:00");
        sportData.setDate("2026-01-15");
        backupData.getSportLogs().add(sportData);

        BackupData.FoodLogData foodData = new BackupData.FoodLogData();
        foodData.setId("fl-1");
        foodData.setFoodItems("Oats");
        foodData.setDateTime("2026-01-15T07:00:00");
        backupData.getFoodLogs().add(foodData);

        RestoreOptions options = RestoreOptions.none();
        options.setRestoreSportLogs(true);

        MergeResult result = backupService.mergeBackupData(backupData, user,
                habitRepository, categoryRepository, diaryEntryRepository,
                sleepEntryRepository, sportLogRepository, foodLogRepository,
                options);

        assertEquals(1, result.getSportLogsAdded());
        assertEquals(0, result.getFoodLogsAdded());
    }

    @Test
    void testBackupScoringRuleInHabitConvert() throws BackupException {
        ScoringRule rule = new ScoringRule("custom", 1, 2, 4, 7);
        rule.setUser(user);

        Habit habit = new Habit("Exercise", "Test");
        habit.setUser(user);
        habit.setScoringRule(rule);
        habitRepository.save(habit);

        backupService.createBackup(tempDir, "password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        File[] backups = backupService.listBackups(tempDir);
        BackupData restored = backupService.restoreBackup(backups[0], "password");

        assertEquals(1, restored.getHabits().size());
        BackupData.ScoringRuleData restoredRule = restored.getHabits().get(0).getScoringRule();
        assertNotNull(restoredRule);
        assertEquals("custom", restoredRule.getName());
        assertEquals(1, restoredRule.getThresholdFor1Point());
        assertEquals(2, restoredRule.getThresholdFor2Points());
        assertEquals(4, restoredRule.getThresholdFor4Points());
        assertEquals(7, restoredRule.getThresholdFor8Points());
    }

    @Test
    void testBackupHabitEntriesRoundTrip() throws BackupException {
        Habit habit = new Habit("Exercise", "Test");
        habit.setUser(user);

        HabitEntry entry1 = new HabitEntry();
        entry1.setCompletedAt(LocalDateTime.of(2026, 1, 15, 10, 0));
        entry1.setNotes("Morning");
        entry1.setValue(1);
        habit.addEntry(entry1);

        HabitEntry entry2 = new HabitEntry();
        entry2.setCompletedAt(LocalDateTime.of(2026, 1, 16, 18, 0));
        entry2.setNotes("Evening");
        entry2.setValue(2);
        habit.addEntry(entry2);

        habitRepository.save(habit);

        backupService.createBackup(tempDir, "password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        File[] backups = backupService.listBackups(tempDir);
        BackupData restored = backupService.restoreBackup(backups[0], "password");

        assertEquals(2, restored.getHabits().get(0).getEntries().size());
    }

    // Simple in-memory repositories for testing

    private static class InMemoryHabitRepository implements HabitRepository {
        private final List<Habit> habits = new ArrayList<>();

        @Override
        public Habit save(Habit habit) {
            habits.removeIf(h -> h.getId().equals(habit.getId()));
            habits.add(habit);
            return habit;
        }

        @Override
        public Optional<Habit> findById(String id) {
            return habits.stream().filter(h -> h.getId().equals(id)).findFirst();
        }

        @Override
        public List<Habit> findAll() {
            return new ArrayList<>(habits);
        }

        @Override
        public void deleteById(String id) {
            habits.removeIf(h -> h.getId().equals(id));
        }

        @Override
        public boolean existsById(String id) {
            return habits.stream().anyMatch(h -> h.getId().equals(id));
        }

        @Override
        public List<Habit> findByUserId(String userId) {
            List<Habit> result = new ArrayList<>();
            for (Habit h : habits) {
                if (h.getUser() != null && h.getUser().getId().equals(userId)) {
                    result.add(h);
                }
            }
            return result;
        }
    }

    private static class InMemoryHabitCategoryRepository implements HabitCategoryRepository {
        private final List<HabitCategory> categories = new ArrayList<>();

        @Override
        public HabitCategory save(HabitCategory category) {
            categories.removeIf(c -> c.getId().equals(category.getId()));
            categories.add(category);
            return category;
        }

        @Override
        public Optional<HabitCategory> findById(String id) {
            return categories.stream().filter(c -> c.getId().equals(id)).findFirst();
        }

        @Override
        public List<HabitCategory> findAll() {
            return new ArrayList<>(categories);
        }

        @Override
        public void deleteById(String id) {
            categories.removeIf(c -> c.getId().equals(id));
        }

        @Override
        public boolean existsById(String id) {
            return categories.stream().anyMatch(c -> c.getId().equals(id));
        }

        @Override
        public List<HabitCategory> findByUserId(String userId) {
            List<HabitCategory> result = new ArrayList<>();
            for (HabitCategory c : categories) {
                if (c.getUser() != null && c.getUser().getId().equals(userId)) {
                    result.add(c);
                }
            }
            return result;
        }
    }

    private static class InMemoryDiaryEntryRepository implements DiaryEntryRepository {
        private final List<DiaryEntry> entries = new ArrayList<>();

        @Override
        public DiaryEntry save(DiaryEntry entry) {
            entries.removeIf(e -> e.getId().equals(entry.getId()));
            entries.add(entry);
            return entry;
        }

        @Override
        public Optional<DiaryEntry> findById(String id) {
            return entries.stream().filter(e -> e.getId().equals(id)).findFirst();
        }

        @Override
        public List<DiaryEntry> findAll() {
            return new ArrayList<>(entries);
        }

        @Override
        public void deleteById(String id) {
            entries.removeIf(e -> e.getId().equals(id));
        }

        @Override
        public List<DiaryEntry> findByUserId(String userId) {
            List<DiaryEntry> result = new ArrayList<>();
            for (DiaryEntry e : entries) {
                if (e.getUser() != null && e.getUser().getId().equals(userId)) {
                    result.add(e);
                }
            }
            return result;
        }

        @Override
        public List<String> findDistinctDescriptionsByUserId(String userId) {
            List<String> result = new ArrayList<>();
            for (DiaryEntry e : entries) {
                if (e.getUser() != null && e.getUser().getId().equals(userId)
                        && !result.contains(e.getDescription())) {
                    result.add(e.getDescription());
                }
            }
            return result;
        }

        @Override
        public List<DiaryEntry> findEntriesNeedingMigration(String userId) {
            return new ArrayList<>();
        }
    }

    private static class InMemorySleepEntryRepository implements SleepEntryRepository {
        private final List<SleepEntry> entries = new ArrayList<>();

        @Override
        public SleepEntry save(SleepEntry entry) {
            entries.removeIf(e -> e.getId().equals(entry.getId()));
            entries.add(entry);
            return entry;
        }

        @Override
        public Optional<SleepEntry> findById(String id) {
            return entries.stream().filter(e -> e.getId().equals(id)).findFirst();
        }

        @Override
        public List<SleepEntry> findAll() {
            return new ArrayList<>(entries);
        }

        @Override
        public void deleteById(String id) {
            entries.removeIf(e -> e.getId().equals(id));
        }

        @Override
        public boolean existsById(String id) {
            return entries.stream().anyMatch(e -> e.getId().equals(id));
        }

        @Override
        public List<SleepEntry> findByUserId(String userId) {
            List<SleepEntry> result = new ArrayList<>();
            for (SleepEntry e : entries) {
                if (e.getUser() != null && e.getUser().getId().equals(userId)) {
                    result.add(e);
                }
            }
            return result;
        }
    }

    private static class InMemoryEmotionPairRepository implements EmotionPairRepository {
        private final List<EmotionPair> pairs = new ArrayList<>();

        @Override
        public EmotionPair save(EmotionPair pair) {
            pairs.removeIf(p -> p.getId().equals(pair.getId()));
            pairs.add(pair);
            return pair;
        }

        @Override
        public Optional<EmotionPair> findById(String id) {
            return pairs.stream().filter(p -> p.getId().equals(id)).findFirst();
        }

        @Override
        public List<EmotionPair> findAll() {
            return new ArrayList<>(pairs);
        }

        @Override
        public void deleteById(String id) {
            pairs.removeIf(p -> p.getId().equals(id));
        }

        @Override
        public List<EmotionPair> findByUserId(String userId) {
            List<EmotionPair> result = new ArrayList<>();
            for (EmotionPair p : pairs) {
                if (p.getUser() != null && p.getUser().getId().equals(userId)) {
                    result.add(p);
                }
            }
            return result;
        }
    }

    private static class InMemoryEmotionEntryRepository implements EmotionEntryRepository {
        private final List<EmotionEntry> entries = new ArrayList<>();

        @Override
        public EmotionEntry save(EmotionEntry entry) {
            entries.removeIf(e -> e.getId().equals(entry.getId()));
            entries.add(entry);
            return entry;
        }

        @Override
        public Optional<EmotionEntry> findById(String id) {
            return entries.stream().filter(e -> e.getId().equals(id)).findFirst();
        }

        @Override
        public List<EmotionEntry> findAll() {
            return new ArrayList<>(entries);
        }

        @Override
        public void deleteById(String id) {
            entries.removeIf(e -> e.getId().equals(id));
        }

        @Override
        public List<EmotionEntry> findByUserId(String userId) {
            List<EmotionEntry> result = new ArrayList<>();
            for (EmotionEntry e : entries) {
                if (e.getUser() != null && e.getUser().getId().equals(userId)) {
                    result.add(e);
                }
            }
            return result;
        }
    }

    private static class InMemoryReminderSettingsRepository implements ReminderSettingsRepository {
        private ReminderSettings settings;

        @Override
        public ReminderSettings save(ReminderSettings s) {
            this.settings = s;
            return s;
        }

        @Override
        public Optional<ReminderSettings> findByUserId(String userId) {
            if (settings != null && settings.getUser() != null
                    && settings.getUser().getId().equals(userId)) {
                return Optional.of(settings);
            }
            return Optional.empty();
        }

        @Override
        public void deleteByUserId(String userId) {
            if (settings != null && settings.getUser() != null
                    && settings.getUser().getId().equals(userId)) {
                settings = null;
            }
        }
    }

    private static class InMemorySportLogRepository implements SportLogRepository {
        private final List<SportLog> entries = new ArrayList<>();

        @Override
        public SportLog save(SportLog entry) {
            entries.removeIf(e -> e.getId().equals(entry.getId()));
            entries.add(entry);
            return entry;
        }

        @Override
        public Optional<SportLog> findById(String id) {
            return entries.stream().filter(e -> e.getId().equals(id)).findFirst();
        }

        @Override
        public List<SportLog> findAll() {
            return new ArrayList<>(entries);
        }

        @Override
        public void deleteById(String id) {
            entries.removeIf(e -> e.getId().equals(id));
        }

        @Override
        public boolean existsById(String id) {
            return entries.stream().anyMatch(e -> e.getId().equals(id));
        }

        @Override
        public List<SportLog> findByUserId(String userId) {
            List<SportLog> result = new ArrayList<>();
            for (SportLog e : entries) {
                if (e.getUser() != null && e.getUser().getId().equals(userId)) {
                    result.add(e);
                }
            }
            return result;
        }

        @Override
        public List<String> findDistinctNamesByUserId(String userId) {
            return entries.stream()
                    .filter(e -> e.getUser() != null && e.getUser().getId().equals(userId))
                    .map(SportLog::getName)
                    .distinct()
                    .sorted()
                    .collect(java.util.stream.Collectors.toList());
        }

        @Override
        public List<String> findDistinctMeasurementUnitsByUserId(String userId) {
            return entries.stream()
                    .filter(e -> e.getUser() != null && e.getUser().getId().equals(userId))
                    .map(SportLog::getMeasurementUnit)
                    .distinct()
                    .sorted()
                    .collect(java.util.stream.Collectors.toList());
        }
    }

    private static class InMemoryFoodLogRepository implements FoodLogRepository {
        private final List<FoodLog> entries = new ArrayList<>();

        @Override
        public FoodLog save(FoodLog entry) {
            entries.removeIf(e -> e.getId().equals(entry.getId()));
            entries.add(entry);
            return entry;
        }

        @Override
        public Optional<FoodLog> findById(String id) {
            return entries.stream().filter(e -> e.getId().equals(id)).findFirst();
        }

        @Override
        public List<FoodLog> findAll() {
            return new ArrayList<>(entries);
        }

        @Override
        public void deleteById(String id) {
            entries.removeIf(e -> e.getId().equals(id));
        }

        @Override
        public boolean existsById(String id) {
            return entries.stream().anyMatch(e -> e.getId().equals(id));
        }

        @Override
        public List<FoodLog> findByUserId(String userId) {
            List<FoodLog> result = new ArrayList<>();
            for (FoodLog e : entries) {
                if (e.getUser() != null && e.getUser().getId().equals(userId)) {
                    result.add(e);
                }
            }
            return result;
        }
    }

    private static class InMemoryMeetingEntryRepository implements MeetingEntryRepository {
        private final List<MeetingEntry> entries = new ArrayList<>();

        @Override
        public MeetingEntry save(MeetingEntry entry) {
            entries.removeIf(e -> e.getId().equals(entry.getId()));
            entries.add(entry);
            return entry;
        }

        @Override
        public Optional<MeetingEntry> findById(String id) {
            return entries.stream().filter(e -> e.getId().equals(id)).findFirst();
        }

        @Override
        public List<MeetingEntry> findAll() {
            return new ArrayList<>(entries);
        }

        @Override
        public void deleteById(String id) {
            entries.removeIf(e -> e.getId().equals(id));
        }

        @Override
        public boolean existsById(String id) {
            return entries.stream().anyMatch(e -> e.getId().equals(id));
        }

        @Override
        public List<MeetingEntry> findByUserId(String userId) {
            List<MeetingEntry> result = new ArrayList<>();
            for (MeetingEntry e : entries) {
                if (e.getUser() != null && e.getUser().getId().equals(userId)) {
                    result.add(e);
                }
            }
            return result;
        }
    }

    private static class InMemoryMedicationRepository implements MedicationRepository {
        private final List<Medication> medications = new ArrayList<>();

        @Override
        public Medication save(Medication medication) {
            medications.removeIf(m -> m.getId().equals(medication.getId()));
            medications.add(medication);
            return medication;
        }

        @Override
        public Optional<Medication> findById(String id) {
            return medications.stream().filter(m -> m.getId().equals(id)).findFirst();
        }

        @Override
        public List<Medication> findAll() {
            return new ArrayList<>(medications);
        }

        @Override
        public void deleteById(String id) {
            medications.removeIf(m -> m.getId().equals(id));
        }

        @Override
        public List<Medication> findByUserId(String userId) {
            List<Medication> result = new ArrayList<>();
            for (Medication m : medications) {
                if (m.getUser() != null && m.getUser().getId().equals(userId)) {
                    result.add(m);
                }
            }
            return result;
        }
    }

    private static class InMemoryMedicationLogRepository implements MedicationLogRepository {
        private final List<MedicationLog> logs = new ArrayList<>();

        @Override
        public MedicationLog save(MedicationLog log) {
            logs.removeIf(l -> l.getId().equals(log.getId()));
            logs.add(log);
            return log;
        }

        @Override
        public Optional<MedicationLog> findById(String id) {
            return logs.stream().filter(l -> l.getId().equals(id)).findFirst();
        }

        @Override
        public List<MedicationLog> findAll() {
            return new ArrayList<>(logs);
        }

        @Override
        public void deleteById(String id) {
            logs.removeIf(l -> l.getId().equals(id));
        }

        @Override
        public List<MedicationLog> findByUserId(String userId) {
            List<MedicationLog> result = new ArrayList<>();
            for (MedicationLog l : logs) {
                if (l.getUser() != null && l.getUser().getId().equals(userId)) {
                    result.add(l);
                }
            }
            return result;
        }

        @Override
        public List<MedicationLog> findByUserIdPaged(String userId, int limit, int offset) {
            List<MedicationLog> all = findByUserId(userId);
            int end = Math.min(offset + limit, all.size());
            if (offset >= all.size()) {
                return new ArrayList<>();
            }
            return new ArrayList<>(all.subList(offset, end));
        }

        @Override
        public int countByUserId(String userId) {
            return findByUserId(userId).size();
        }
    }
}
