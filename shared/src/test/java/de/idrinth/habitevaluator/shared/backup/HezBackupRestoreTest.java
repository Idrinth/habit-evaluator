package de.idrinth.habitevaluator.shared.backup;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.EventSignificance;
import de.idrinth.habitevaluator.shared.model.FrequencyType;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.ReminderSettings;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.ReminderSettingsRepository;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HezBackupRestoreTest {

    private HezBackupService hezBackupService;
    private User user;
    private static final String PASSWORD = "testPassword123";

    @BeforeEach
    void setUp() {
        hezBackupService = new HezBackupService();
        user = new User("testuser", "password", "test@test.com");
    }

    // --- Empty backup, empty local ---

    @Test
    void testCreateAndRestoreEmptyBackupIntoEmptyLocal() throws BackupException {
        InMemoryHabitRepository sourceHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository sourceCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository sourceDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository sourceSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository sourceEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository sourceEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository sourceReminderRepo = new InMemoryReminderSettingsRepository();

        byte[] hezData = hezBackupService.createHezBackup(PASSWORD, user,
                sourceHabitRepo, sourceCategoryRepo, sourceDiaryRepo, sourceSleepRepo,
                sourceEmotionPairRepo, sourceEmotionEntryRepo, sourceReminderRepo);

        assertNotNull(hezData);
        assertTrue(hezBackupService.isValidHezData(hezData));

        InMemoryHabitRepository targetHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository targetCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository targetDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository targetSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository targetEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository targetEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository targetReminderRepo = new InMemoryReminderSettingsRepository();

        MergeResult result = hezBackupService.mergeFromHezBytes(hezData, PASSWORD, user,
                targetHabitRepo, targetCategoryRepo, targetDiaryRepo, targetSleepRepo,
                targetEmotionPairRepo, targetEmotionEntryRepo, targetReminderRepo,
                RestoreOptions.all());

        assertEquals(0, result.getCategoriesAdded());
        assertEquals(0, result.getHabitsAdded());
        assertEquals(0, result.getHabitsMerged());
        assertEquals(0, result.getEntriesAdded());
        assertEquals(0, result.getDiaryEntriesAdded());
        assertEquals(0, result.getSleepEntriesAdded());
        assertEquals(0, result.getEmotionPairsAdded());
        assertEquals(0, result.getEmotionEntriesAdded());
        assertFalse(result.isReminderSettingsRestored());
        assertEquals(0, result.getTotalChanges());

        assertTrue(targetHabitRepo.findByUserId(user.getId()).isEmpty());
        assertTrue(targetCategoryRepo.findByUserId(user.getId()).isEmpty());
        assertTrue(targetDiaryRepo.findByUserId(user.getId()).isEmpty());
        assertTrue(targetSleepRepo.findByUserId(user.getId()).isEmpty());
        assertTrue(targetEmotionPairRepo.findByUserId(user.getId()).isEmpty());
        assertTrue(targetEmotionEntryRepo.findByUserId(user.getId()).isEmpty());
        assertFalse(targetReminderRepo.findByUserId(user.getId()).isPresent());
    }

    @Test
    void testEmptyBackupDeserialization() throws BackupException {
        InMemoryHabitRepository sourceHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository sourceCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository sourceDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository sourceSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository sourceEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository sourceEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository sourceReminderRepo = new InMemoryReminderSettingsRepository();

        byte[] hezData = hezBackupService.createHezBackup(PASSWORD, user,
                sourceHabitRepo, sourceCategoryRepo, sourceDiaryRepo, sourceSleepRepo,
                sourceEmotionPairRepo, sourceEmotionEntryRepo, sourceReminderRepo);

        BackupData restored = hezBackupService.restoreFromHezBytes(hezData, PASSWORD);

        assertNotNull(restored);
        assertNotNull(restored.getUser());
        assertEquals("testuser", restored.getUser().getUsername());
        assertTrue(restored.getHabits().isEmpty());
        assertTrue(restored.getCategories().isEmpty());
        assertTrue(restored.getDiaryEntries().isEmpty());
        assertTrue(restored.getSleepEntries().isEmpty());
        assertTrue(restored.getEmotionPairs().isEmpty());
        assertTrue(restored.getEmotionEntries().isEmpty());
        assertNull(restored.getReminderSettings());
    }

    // --- Filled backup, empty local ---

    @Test
    void testCreateFilledBackupAndRestoreIntoEmptyLocal() throws BackupException {
        InMemoryHabitRepository sourceHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository sourceCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository sourceDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository sourceSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository sourceEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository sourceEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository sourceReminderRepo = new InMemoryReminderSettingsRepository();

        populateSourceData(user, sourceCategoryRepo, sourceHabitRepo, sourceDiaryRepo,
                sourceSleepRepo, sourceEmotionPairRepo, sourceEmotionEntryRepo, sourceReminderRepo);

        byte[] hezData = hezBackupService.createHezBackup(PASSWORD, user,
                sourceHabitRepo, sourceCategoryRepo, sourceDiaryRepo, sourceSleepRepo,
                sourceEmotionPairRepo, sourceEmotionEntryRepo, sourceReminderRepo);

        assertNotNull(hezData);
        assertTrue(hezBackupService.isValidHezData(hezData));

        InMemoryHabitRepository targetHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository targetCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository targetDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository targetSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository targetEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository targetEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository targetReminderRepo = new InMemoryReminderSettingsRepository();

        MergeResult result = hezBackupService.mergeFromHezBytes(hezData, PASSWORD, user,
                targetHabitRepo, targetCategoryRepo, targetDiaryRepo, targetSleepRepo,
                targetEmotionPairRepo, targetEmotionEntryRepo, targetReminderRepo,
                RestoreOptions.all());

        assertEquals(2, result.getCategoriesAdded());
        assertEquals(2, result.getHabitsAdded());
        assertEquals(0, result.getHabitsMerged());
        assertEquals(2, result.getEntriesAdded());
        assertEquals(2, result.getDiaryEntriesAdded());
        assertEquals(1, result.getSleepEntriesAdded());
        assertEquals(1, result.getEmotionPairsAdded());
        assertEquals(1, result.getEmotionEntriesAdded());
        assertTrue(result.isReminderSettingsRestored());

        assertEquals(2, targetHabitRepo.findByUserId(user.getId()).size());
        assertEquals(2, targetCategoryRepo.findByUserId(user.getId()).size());
        assertEquals(2, targetDiaryRepo.findByUserId(user.getId()).size());
        assertEquals(1, targetSleepRepo.findByUserId(user.getId()).size());
        assertEquals(1, targetEmotionPairRepo.findByUserId(user.getId()).size());
        assertEquals(1, targetEmotionEntryRepo.findByUserId(user.getId()).size());
        assertTrue(targetReminderRepo.findByUserId(user.getId()).isPresent());
    }

    @Test
    void testFilledBackupDataPreservesHabitDetails() throws BackupException {
        InMemoryHabitRepository sourceHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository sourceCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository sourceDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository sourceSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository sourceEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository sourceEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository sourceReminderRepo = new InMemoryReminderSettingsRepository();

        HabitCategory category = new HabitCategory("Health", "Health habits", "#FF0000");
        category.setUser(user);
        sourceCategoryRepo.save(category);

        Habit habit = new Habit("Exercise", "Daily workout");
        habit.setUser(user);
        habit.setCategoryId(category.getId());
        habit.setFrequencyType(FrequencyType.WEEKLY);
        habit.setTargetFrequency(3);
        habit.setMaxEntriesPerDay(2);
        habit.setPositiveScoring(true);
        HabitEntry entry = new HabitEntry();
        entry.setCompletedAt(LocalDateTime.of(2026, 1, 15, 10, 0));
        entry.setNotes("Morning run");
        entry.setValue(1);
        habit.addEntry(entry);
        sourceHabitRepo.save(habit);

        byte[] hezData = hezBackupService.createHezBackup(PASSWORD, user,
                sourceHabitRepo, sourceCategoryRepo, sourceDiaryRepo, sourceSleepRepo,
                sourceEmotionPairRepo, sourceEmotionEntryRepo, sourceReminderRepo);

        BackupData restored = hezBackupService.restoreFromHezBytes(hezData, PASSWORD);

        assertEquals(1, restored.getCategories().size());
        assertEquals("Health", restored.getCategories().get(0).getName());
        assertEquals("#FF0000", restored.getCategories().get(0).getColor());

        assertEquals(1, restored.getHabits().size());
        BackupData.HabitData habitData = restored.getHabits().get(0);
        assertEquals("Exercise", habitData.getName());
        assertEquals("Daily workout", habitData.getDescription());
        assertEquals("WEEKLY", habitData.getFrequencyType());
        assertEquals(3, habitData.getTargetFrequency());
        assertEquals(2, habitData.getMaxEntriesPerDay());
        assertTrue(habitData.isPositiveScoring());
        assertEquals(1, habitData.getEntries().size());
        assertEquals("Morning run", habitData.getEntries().get(0).getNotes());
        assertEquals(1, habitData.getEntries().get(0).getValue());
    }

    @Test
    void testFilledBackupDataPreservesDiaryDetails() throws BackupException {
        InMemoryHabitRepository sourceHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository sourceCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository sourceDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository sourceSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository sourceEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository sourceEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository sourceReminderRepo = new InMemoryReminderSettingsRepository();

        DiaryEntry diary = new DiaryEntry("Important meeting", EventSignificance.MAJOR,
                LocalDate.of(2026, 2, 10));
        diary.setStartTime(LocalTime.of(9, 0));
        diary.setEndTime(LocalTime.of(11, 30));
        diary.setUser(user);
        sourceDiaryRepo.save(diary);

        byte[] hezData = hezBackupService.createHezBackup(PASSWORD, user,
                sourceHabitRepo, sourceCategoryRepo, sourceDiaryRepo, sourceSleepRepo,
                sourceEmotionPairRepo, sourceEmotionEntryRepo, sourceReminderRepo);

        BackupData restored = hezBackupService.restoreFromHezBytes(hezData, PASSWORD);

        assertEquals(1, restored.getDiaryEntries().size());
        BackupData.DiaryEntryData diaryData = restored.getDiaryEntries().get(0);
        assertEquals("Important meeting", diaryData.getDescription());
        assertEquals("MAJOR", diaryData.getSignificance());
        assertEquals("2026-02-10", diaryData.getEventDate());
        assertEquals("09:00", diaryData.getStartTime());
        assertEquals("11:30", diaryData.getEndTime());
    }

    @Test
    void testFilledBackupDataPreservesSleepDetails() throws BackupException {
        InMemoryHabitRepository sourceHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository sourceCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository sourceDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository sourceSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository sourceEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository sourceEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository sourceReminderRepo = new InMemoryReminderSettingsRepository();

        SleepEntry sleep = new SleepEntry(LocalTime.of(23, 0), LocalTime.of(7, 0),
                LocalDate.of(2026, 2, 10));
        sleep.setNotes("Slept well");
        sleep.setUser(user);
        sourceSleepRepo.save(sleep);

        byte[] hezData = hezBackupService.createHezBackup(PASSWORD, user,
                sourceHabitRepo, sourceCategoryRepo, sourceDiaryRepo, sourceSleepRepo,
                sourceEmotionPairRepo, sourceEmotionEntryRepo, sourceReminderRepo);

        BackupData restored = hezBackupService.restoreFromHezBytes(hezData, PASSWORD);

        assertEquals(1, restored.getSleepEntries().size());
        BackupData.SleepEntryData sleepData = restored.getSleepEntries().get(0);
        assertEquals("23:00", sleepData.getFromTime());
        assertEquals("07:00", sleepData.getUntilTime());
        assertEquals("2026-02-10", sleepData.getDate());
        assertEquals("Slept well", sleepData.getNotes());
    }

    @Test
    void testFilledBackupDataPreservesEmotionDetails() throws BackupException {
        InMemoryHabitRepository sourceHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository sourceCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository sourceDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository sourceSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository sourceEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository sourceEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository sourceReminderRepo = new InMemoryReminderSettingsRepository();

        EmotionPair pair = new EmotionPair("Anxious", "Calm");
        pair.setUser(user);
        sourceEmotionPairRepo.save(pair);

        EmotionEntry emotionEntry = new EmotionEntry(pair, -5,
                LocalDateTime.of(2026, 2, 10, 14, 30), "Before presentation");
        emotionEntry.setUser(user);
        sourceEmotionEntryRepo.save(emotionEntry);

        byte[] hezData = hezBackupService.createHezBackup(PASSWORD, user,
                sourceHabitRepo, sourceCategoryRepo, sourceDiaryRepo, sourceSleepRepo,
                sourceEmotionPairRepo, sourceEmotionEntryRepo, sourceReminderRepo);

        BackupData restored = hezBackupService.restoreFromHezBytes(hezData, PASSWORD);

        assertEquals(1, restored.getEmotionPairs().size());
        assertEquals("Anxious", restored.getEmotionPairs().get(0).getNegativeLabel());
        assertEquals("Calm", restored.getEmotionPairs().get(0).getPositiveLabel());

        assertEquals(1, restored.getEmotionEntries().size());
        assertEquals(-5, restored.getEmotionEntries().get(0).getStrength());
        assertEquals("Before presentation", restored.getEmotionEntries().get(0).getNotes());
    }

    @Test
    void testFilledBackupDataPreservesReminderSettings() throws BackupException {
        InMemoryHabitRepository sourceHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository sourceCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository sourceDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository sourceSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository sourceEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository sourceEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository sourceReminderRepo = new InMemoryReminderSettingsRepository();

        ReminderSettings settings = new ReminderSettings();
        settings.setUser(user);
        settings.setSleepReminderEnabled(true);
        settings.setSleepReminderTime(LocalTime.of(22, 30));
        settings.setDiaryReminderEnabled(true);
        settings.setDiaryReminderTime(LocalTime.of(20, 0));
        settings.setEmotionReminderEnabled(true);
        settings.setEmotionReminderCount(5);
        settings.setWakingHoursStart(LocalTime.of(7, 0));
        settings.setWakingHoursEnd(LocalTime.of(23, 0));
        sourceReminderRepo.save(settings);

        byte[] hezData = hezBackupService.createHezBackup(PASSWORD, user,
                sourceHabitRepo, sourceCategoryRepo, sourceDiaryRepo, sourceSleepRepo,
                sourceEmotionPairRepo, sourceEmotionEntryRepo, sourceReminderRepo);

        BackupData restored = hezBackupService.restoreFromHezBytes(hezData, PASSWORD);

        assertNotNull(restored.getReminderSettings());
        assertTrue(restored.getReminderSettings().isSleepReminderEnabled());
        assertEquals("22:30", restored.getReminderSettings().getSleepReminderTime());
        assertTrue(restored.getReminderSettings().isDiaryReminderEnabled());
        assertEquals("20:00", restored.getReminderSettings().getDiaryReminderTime());
        assertTrue(restored.getReminderSettings().isEmotionReminderEnabled());
        assertEquals(5, restored.getReminderSettings().getEmotionReminderCount());
        assertEquals("07:00", restored.getReminderSettings().getWakingHoursStart());
        assertEquals("23:00", restored.getReminderSettings().getWakingHoursEnd());
    }

    @Test
    void testFilledBackupMergeIntoEmptyLocalCreatesAllEntities() throws BackupException {
        InMemoryHabitRepository sourceHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository sourceCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository sourceDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository sourceSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository sourceEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository sourceEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository sourceReminderRepo = new InMemoryReminderSettingsRepository();

        populateSourceData(user, sourceCategoryRepo, sourceHabitRepo, sourceDiaryRepo,
                sourceSleepRepo, sourceEmotionPairRepo, sourceEmotionEntryRepo, sourceReminderRepo);

        byte[] hezData = hezBackupService.createHezBackup(PASSWORD, user,
                sourceHabitRepo, sourceCategoryRepo, sourceDiaryRepo, sourceSleepRepo,
                sourceEmotionPairRepo, sourceEmotionEntryRepo, sourceReminderRepo);

        InMemoryHabitRepository targetHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository targetCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository targetDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository targetSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository targetEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository targetEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository targetReminderRepo = new InMemoryReminderSettingsRepository();

        hezBackupService.mergeFromHezBytes(hezData, PASSWORD, user,
                targetHabitRepo, targetCategoryRepo, targetDiaryRepo, targetSleepRepo,
                targetEmotionPairRepo, targetEmotionEntryRepo, targetReminderRepo,
                RestoreOptions.all());

        List<Habit> habits = targetHabitRepo.findByUserId(user.getId());
        assertEquals(2, habits.size());
        for (Habit h : habits) {
            assertNotNull(h.getName());
            assertEquals(1, h.getEntries().size());
        }

        List<HabitCategory> categories = targetCategoryRepo.findByUserId(user.getId());
        assertEquals(2, categories.size());

        List<DiaryEntry> diaryEntries = targetDiaryRepo.findByUserId(user.getId());
        assertEquals(2, diaryEntries.size());

        List<SleepEntry> sleepEntries = targetSleepRepo.findByUserId(user.getId());
        assertEquals(1, sleepEntries.size());

        List<EmotionPair> emotionPairs = targetEmotionPairRepo.findByUserId(user.getId());
        assertEquals(1, emotionPairs.size());

        List<EmotionEntry> emotionEntries = targetEmotionEntryRepo.findByUserId(user.getId());
        assertEquals(1, emotionEntries.size());

        assertTrue(targetReminderRepo.findByUserId(user.getId()).isPresent());
        ReminderSettings restoredSettings = targetReminderRepo.findByUserId(user.getId()).get();
        assertTrue(restoredSettings.isSleepReminderEnabled());
    }

    // --- Filled backup, filled local ---

    @Test
    void testFilledBackupMergeIntoFilledLocalAddsNewCategories() throws BackupException {
        InMemoryHabitRepository sourceHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository sourceCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository sourceDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository sourceSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository sourceEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository sourceEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository sourceReminderRepo = new InMemoryReminderSettingsRepository();

        HabitCategory sourceCategory = new HabitCategory("Fitness", "Fitness habits", "#00FF00");
        sourceCategory.setUser(user);
        sourceCategoryRepo.save(sourceCategory);

        Habit sourceHabit = new Habit("Running", "Go for a run");
        sourceHabit.setUser(user);
        sourceHabit.setCategoryId(sourceCategory.getId());
        HabitEntry sourceEntry = new HabitEntry();
        sourceEntry.setCompletedAt(LocalDateTime.of(2026, 1, 20, 8, 0));
        sourceHabit.addEntry(sourceEntry);
        sourceHabitRepo.save(sourceHabit);

        byte[] hezData = hezBackupService.createHezBackup(PASSWORD, user,
                sourceHabitRepo, sourceCategoryRepo, sourceDiaryRepo, sourceSleepRepo,
                sourceEmotionPairRepo, sourceEmotionEntryRepo, sourceReminderRepo);

        InMemoryHabitRepository targetHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository targetCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository targetDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository targetSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository targetEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository targetEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository targetReminderRepo = new InMemoryReminderSettingsRepository();

        HabitCategory existingCategory = new HabitCategory("Health", "Health habits", "#FF0000");
        existingCategory.setUser(user);
        targetCategoryRepo.save(existingCategory);

        Habit existingHabit = new Habit("Meditation", "Daily meditation");
        existingHabit.setUser(user);
        existingHabit.setCategoryId(existingCategory.getId());
        targetHabitRepo.save(existingHabit);

        MergeResult result = hezBackupService.mergeFromHezBytes(hezData, PASSWORD, user,
                targetHabitRepo, targetCategoryRepo, targetDiaryRepo, targetSleepRepo,
                targetEmotionPairRepo, targetEmotionEntryRepo, targetReminderRepo,
                RestoreOptions.all());

        assertEquals(1, result.getCategoriesAdded());
        assertEquals(1, result.getHabitsAdded());
        assertEquals(0, result.getHabitsMerged());
        assertEquals(1, result.getEntriesAdded());

        assertEquals(2, targetCategoryRepo.findByUserId(user.getId()).size());
        assertEquals(2, targetHabitRepo.findByUserId(user.getId()).size());
    }

    @Test
    void testFilledBackupMergeIntoFilledLocalMatchesCategoriesByName() throws BackupException {
        InMemoryHabitRepository sourceHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository sourceCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository sourceDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository sourceSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository sourceEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository sourceEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository sourceReminderRepo = new InMemoryReminderSettingsRepository();

        HabitCategory sourceCategory = new HabitCategory("Health", "Source description", "#00FF00");
        sourceCategory.setUser(user);
        sourceCategoryRepo.save(sourceCategory);

        Habit sourceHabit = new Habit("NewHabit", "A new habit");
        sourceHabit.setUser(user);
        sourceHabit.setCategoryId(sourceCategory.getId());
        sourceHabitRepo.save(sourceHabit);

        byte[] hezData = hezBackupService.createHezBackup(PASSWORD, user,
                sourceHabitRepo, sourceCategoryRepo, sourceDiaryRepo, sourceSleepRepo,
                sourceEmotionPairRepo, sourceEmotionEntryRepo, sourceReminderRepo);

        InMemoryHabitRepository targetHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository targetCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository targetDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository targetSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository targetEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository targetEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository targetReminderRepo = new InMemoryReminderSettingsRepository();

        HabitCategory existingCategory = new HabitCategory("Health", "Existing description", "#FF0000");
        existingCategory.setUser(user);
        targetCategoryRepo.save(existingCategory);

        MergeResult result = hezBackupService.mergeFromHezBytes(hezData, PASSWORD, user,
                targetHabitRepo, targetCategoryRepo, targetDiaryRepo, targetSleepRepo,
                targetEmotionPairRepo, targetEmotionEntryRepo, targetReminderRepo,
                RestoreOptions.all());

        assertEquals(0, result.getCategoriesAdded());
        assertEquals(1, result.getHabitsAdded());
        assertEquals(1, targetCategoryRepo.findByUserId(user.getId()).size());

        Habit restoredHabit = targetHabitRepo.findByUserId(user.getId()).get(0);
        assertEquals(existingCategory.getId(), restoredHabit.getCategoryId());
    }

    @Test
    void testFilledBackupMergeIntoFilledLocalMergesHabitEntries() throws BackupException {
        InMemoryHabitRepository sourceHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository sourceCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository sourceDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository sourceSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository sourceEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository sourceEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository sourceReminderRepo = new InMemoryReminderSettingsRepository();

        Habit sourceHabit = new Habit("Exercise", "Daily workout");
        sourceHabit.setUser(user);
        HabitEntry sourceEntry1 = new HabitEntry();
        sourceEntry1.setCompletedAt(LocalDateTime.of(2026, 1, 15, 10, 0));
        sourceEntry1.setNotes("Source entry 1");
        sourceHabit.addEntry(sourceEntry1);
        HabitEntry sourceEntry2 = new HabitEntry();
        sourceEntry2.setCompletedAt(LocalDateTime.of(2026, 1, 16, 10, 0));
        sourceEntry2.setNotes("Source entry 2");
        sourceHabit.addEntry(sourceEntry2);
        sourceHabitRepo.save(sourceHabit);

        byte[] hezData = hezBackupService.createHezBackup(PASSWORD, user,
                sourceHabitRepo, sourceCategoryRepo, sourceDiaryRepo, sourceSleepRepo,
                sourceEmotionPairRepo, sourceEmotionEntryRepo, sourceReminderRepo);

        InMemoryHabitRepository targetHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository targetCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository targetDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository targetSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository targetEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository targetEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository targetReminderRepo = new InMemoryReminderSettingsRepository();

        Habit existingHabit = new Habit("Exercise", "Daily workout");
        existingHabit.setUser(user);
        HabitEntry existingEntry = new HabitEntry();
        existingEntry.setId(sourceEntry1.getId());
        existingEntry.setCompletedAt(LocalDateTime.of(2026, 1, 15, 10, 0));
        existingEntry.setNotes("Source entry 1");
        existingHabit.addEntry(existingEntry);
        targetHabitRepo.save(existingHabit);

        MergeResult result = hezBackupService.mergeFromHezBytes(hezData, PASSWORD, user,
                targetHabitRepo, targetCategoryRepo, targetDiaryRepo, targetSleepRepo,
                targetEmotionPairRepo, targetEmotionEntryRepo, targetReminderRepo,
                RestoreOptions.all());

        assertEquals(0, result.getHabitsAdded());
        assertEquals(1, result.getHabitsMerged());
        assertEquals(1, result.getEntriesAdded());

        List<Habit> habits = targetHabitRepo.findByUserId(user.getId());
        assertEquals(1, habits.size());
        assertEquals(2, habits.get(0).getEntries().size());
    }

    @Test
    void testFilledBackupMergeIntoFilledLocalSkipsDuplicateDiaryEntries() throws BackupException {
        InMemoryHabitRepository sourceHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository sourceCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository sourceDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository sourceSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository sourceEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository sourceEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository sourceReminderRepo = new InMemoryReminderSettingsRepository();

        DiaryEntry sourceDiary1 = new DiaryEntry("Event A", EventSignificance.NORMAL,
                LocalDate.of(2026, 1, 10));
        sourceDiary1.setUser(user);
        sourceDiaryRepo.save(sourceDiary1);

        DiaryEntry sourceDiary2 = new DiaryEntry("Event B", EventSignificance.MAJOR,
                LocalDate.of(2026, 1, 11));
        sourceDiary2.setUser(user);
        sourceDiaryRepo.save(sourceDiary2);

        byte[] hezData = hezBackupService.createHezBackup(PASSWORD, user,
                sourceHabitRepo, sourceCategoryRepo, sourceDiaryRepo, sourceSleepRepo,
                sourceEmotionPairRepo, sourceEmotionEntryRepo, sourceReminderRepo);

        InMemoryHabitRepository targetHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository targetCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository targetDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository targetSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository targetEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository targetEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository targetReminderRepo = new InMemoryReminderSettingsRepository();

        DiaryEntry existingDiary = new DiaryEntry("Event A", EventSignificance.NORMAL,
                LocalDate.of(2026, 1, 10));
        existingDiary.setId(sourceDiary1.getId());
        existingDiary.setUser(user);
        targetDiaryRepo.save(existingDiary);

        MergeResult result = hezBackupService.mergeFromHezBytes(hezData, PASSWORD, user,
                targetHabitRepo, targetCategoryRepo, targetDiaryRepo, targetSleepRepo,
                targetEmotionPairRepo, targetEmotionEntryRepo, targetReminderRepo,
                RestoreOptions.all());

        assertEquals(1, result.getDiaryEntriesAdded());
        assertEquals(2, targetDiaryRepo.findByUserId(user.getId()).size());
    }

    @Test
    void testFilledBackupMergeIntoFilledLocalSkipsDuplicateSleepEntries() throws BackupException {
        InMemoryHabitRepository sourceHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository sourceCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository sourceDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository sourceSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository sourceEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository sourceEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository sourceReminderRepo = new InMemoryReminderSettingsRepository();

        SleepEntry sourceSleep1 = new SleepEntry(LocalTime.of(23, 0), LocalTime.of(7, 0),
                LocalDate.of(2026, 1, 10));
        sourceSleep1.setUser(user);
        sourceSleepRepo.save(sourceSleep1);

        SleepEntry sourceSleep2 = new SleepEntry(LocalTime.of(22, 30), LocalTime.of(6, 30),
                LocalDate.of(2026, 1, 11));
        sourceSleep2.setUser(user);
        sourceSleepRepo.save(sourceSleep2);

        byte[] hezData = hezBackupService.createHezBackup(PASSWORD, user,
                sourceHabitRepo, sourceCategoryRepo, sourceDiaryRepo, sourceSleepRepo,
                sourceEmotionPairRepo, sourceEmotionEntryRepo, sourceReminderRepo);

        InMemoryHabitRepository targetHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository targetCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository targetDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository targetSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository targetEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository targetEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository targetReminderRepo = new InMemoryReminderSettingsRepository();

        SleepEntry existingSleep = new SleepEntry(LocalTime.of(23, 0), LocalTime.of(7, 0),
                LocalDate.of(2026, 1, 10));
        existingSleep.setId(sourceSleep1.getId());
        existingSleep.setUser(user);
        targetSleepRepo.save(existingSleep);

        MergeResult result = hezBackupService.mergeFromHezBytes(hezData, PASSWORD, user,
                targetHabitRepo, targetCategoryRepo, targetDiaryRepo, targetSleepRepo,
                targetEmotionPairRepo, targetEmotionEntryRepo, targetReminderRepo,
                RestoreOptions.all());

        assertEquals(1, result.getSleepEntriesAdded());
        assertEquals(2, targetSleepRepo.findByUserId(user.getId()).size());
    }

    @Test
    void testFilledBackupMergeIntoFilledLocalMatchesEmotionPairsByLabel() throws BackupException {
        InMemoryHabitRepository sourceHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository sourceCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository sourceDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository sourceSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository sourceEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository sourceEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository sourceReminderRepo = new InMemoryReminderSettingsRepository();

        EmotionPair sourcePair = new EmotionPair("Sad", "Happy");
        sourcePair.setUser(user);
        sourceEmotionPairRepo.save(sourcePair);

        EmotionEntry sourceEmotionEntry = new EmotionEntry(sourcePair, 3,
                LocalDateTime.of(2026, 2, 10, 12, 0), "Feeling ok");
        sourceEmotionEntry.setUser(user);
        sourceEmotionEntryRepo.save(sourceEmotionEntry);

        byte[] hezData = hezBackupService.createHezBackup(PASSWORD, user,
                sourceHabitRepo, sourceCategoryRepo, sourceDiaryRepo, sourceSleepRepo,
                sourceEmotionPairRepo, sourceEmotionEntryRepo, sourceReminderRepo);

        InMemoryHabitRepository targetHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository targetCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository targetDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository targetSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository targetEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository targetEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository targetReminderRepo = new InMemoryReminderSettingsRepository();

        EmotionPair existingPair = new EmotionPair("Sad", "Happy");
        existingPair.setUser(user);
        targetEmotionPairRepo.save(existingPair);

        MergeResult result = hezBackupService.mergeFromHezBytes(hezData, PASSWORD, user,
                targetHabitRepo, targetCategoryRepo, targetDiaryRepo, targetSleepRepo,
                targetEmotionPairRepo, targetEmotionEntryRepo, targetReminderRepo,
                RestoreOptions.all());

        assertEquals(0, result.getEmotionPairsAdded());
        assertEquals(1, result.getEmotionEntriesAdded());
        assertEquals(1, targetEmotionPairRepo.findByUserId(user.getId()).size());
        assertEquals(1, targetEmotionEntryRepo.findByUserId(user.getId()).size());
    }

    @Test
    void testFilledBackupMergeIntoFilledLocalUpdatesReminderSettings() throws BackupException {
        InMemoryHabitRepository sourceHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository sourceCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository sourceDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository sourceSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository sourceEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository sourceEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository sourceReminderRepo = new InMemoryReminderSettingsRepository();

        ReminderSettings sourceSettings = new ReminderSettings();
        sourceSettings.setUser(user);
        sourceSettings.setSleepReminderEnabled(true);
        sourceSettings.setSleepReminderTime(LocalTime.of(23, 0));
        sourceSettings.setEmotionReminderEnabled(true);
        sourceSettings.setEmotionReminderCount(7);
        sourceReminderRepo.save(sourceSettings);

        byte[] hezData = hezBackupService.createHezBackup(PASSWORD, user,
                sourceHabitRepo, sourceCategoryRepo, sourceDiaryRepo, sourceSleepRepo,
                sourceEmotionPairRepo, sourceEmotionEntryRepo, sourceReminderRepo);

        InMemoryHabitRepository targetHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository targetCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository targetDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository targetSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository targetEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository targetEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository targetReminderRepo = new InMemoryReminderSettingsRepository();

        ReminderSettings existingSettings = new ReminderSettings();
        existingSettings.setUser(user);
        existingSettings.setSleepReminderEnabled(false);
        existingSettings.setEmotionReminderCount(2);
        targetReminderRepo.save(existingSettings);

        MergeResult result = hezBackupService.mergeFromHezBytes(hezData, PASSWORD, user,
                targetHabitRepo, targetCategoryRepo, targetDiaryRepo, targetSleepRepo,
                targetEmotionPairRepo, targetEmotionEntryRepo, targetReminderRepo,
                RestoreOptions.all());

        assertTrue(result.isReminderSettingsRestored());
        ReminderSettings updatedSettings = targetReminderRepo.findByUserId(user.getId()).get();
        assertTrue(updatedSettings.isSleepReminderEnabled());
        assertTrue(updatedSettings.isEmotionReminderEnabled());
        assertEquals(7, updatedSettings.getEmotionReminderCount());
    }

    @Test
    void testFilledBackupMergeIntoFilledLocalFullScenario() throws BackupException {
        InMemoryHabitRepository sourceHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository sourceCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository sourceDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository sourceSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository sourceEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository sourceEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository sourceReminderRepo = new InMemoryReminderSettingsRepository();

        populateSourceData(user, sourceCategoryRepo, sourceHabitRepo, sourceDiaryRepo,
                sourceSleepRepo, sourceEmotionPairRepo, sourceEmotionEntryRepo, sourceReminderRepo);

        byte[] hezData = hezBackupService.createHezBackup(PASSWORD, user,
                sourceHabitRepo, sourceCategoryRepo, sourceDiaryRepo, sourceSleepRepo,
                sourceEmotionPairRepo, sourceEmotionEntryRepo, sourceReminderRepo);

        InMemoryHabitRepository targetHabitRepo = new InMemoryHabitRepository();
        InMemoryHabitCategoryRepository targetCategoryRepo = new InMemoryHabitCategoryRepository();
        InMemoryDiaryEntryRepository targetDiaryRepo = new InMemoryDiaryEntryRepository();
        InMemorySleepEntryRepository targetSleepRepo = new InMemorySleepEntryRepository();
        InMemoryEmotionPairRepository targetEmotionPairRepo = new InMemoryEmotionPairRepository();
        InMemoryEmotionEntryRepository targetEmotionEntryRepo = new InMemoryEmotionEntryRepository();
        InMemoryReminderSettingsRepository targetReminderRepo = new InMemoryReminderSettingsRepository();

        HabitCategory existingCategory = new HabitCategory("Health", "Already here", "#AAAAAA");
        existingCategory.setUser(user);
        targetCategoryRepo.save(existingCategory);

        Habit existingHabit = new Habit("Reading", "Read books");
        existingHabit.setUser(user);
        existingHabit.setCategoryId(existingCategory.getId());
        HabitEntry existingEntry = new HabitEntry();
        existingEntry.setCompletedAt(LocalDateTime.of(2026, 2, 1, 20, 0));
        existingHabit.addEntry(existingEntry);
        targetHabitRepo.save(existingHabit);

        DiaryEntry existingDiary = new DiaryEntry("Local event", EventSignificance.MINOR,
                LocalDate.of(2026, 2, 5));
        existingDiary.setUser(user);
        targetDiaryRepo.save(existingDiary);

        EmotionPair existingPair = new EmotionPair("Sad", "Happy");
        existingPair.setUser(user);
        targetEmotionPairRepo.save(existingPair);

        MergeResult result = hezBackupService.mergeFromHezBytes(hezData, PASSWORD, user,
                targetHabitRepo, targetCategoryRepo, targetDiaryRepo, targetSleepRepo,
                targetEmotionPairRepo, targetEmotionEntryRepo, targetReminderRepo,
                RestoreOptions.all());

        assertTrue(result.getTotalChanges() > 0);

        assertEquals(2, targetCategoryRepo.findByUserId(user.getId()).size());

        List<Habit> allHabits = targetHabitRepo.findByUserId(user.getId());
        assertTrue(allHabits.size() >= 2);

        List<DiaryEntry> allDiary = targetDiaryRepo.findByUserId(user.getId());
        assertTrue(allDiary.size() >= 3);

        assertEquals(1, targetSleepRepo.findByUserId(user.getId()).size());
        assertEquals(1, targetEmotionPairRepo.findByUserId(user.getId()).size());
        assertTrue(targetEmotionEntryRepo.findByUserId(user.getId()).size() >= 1);
        assertTrue(targetReminderRepo.findByUserId(user.getId()).isPresent());
    }

    // --- Helper methods ---

    private void populateSourceData(User user,
                                     InMemoryHabitCategoryRepository categoryRepo,
                                     InMemoryHabitRepository habitRepo,
                                     InMemoryDiaryEntryRepository diaryRepo,
                                     InMemorySleepEntryRepository sleepRepo,
                                     InMemoryEmotionPairRepository emotionPairRepo,
                                     InMemoryEmotionEntryRepository emotionEntryRepo,
                                     InMemoryReminderSettingsRepository reminderRepo) {
        HabitCategory healthCategory = new HabitCategory("Health", "Health habits", "#FF0000");
        healthCategory.setUser(user);
        categoryRepo.save(healthCategory);

        HabitCategory productivityCategory = new HabitCategory("Productivity", "Work habits", "#0000FF");
        productivityCategory.setUser(user);
        categoryRepo.save(productivityCategory);

        Habit exerciseHabit = new Habit("Exercise", "Daily workout");
        exerciseHabit.setUser(user);
        exerciseHabit.setCategoryId(healthCategory.getId());
        exerciseHabit.setFrequencyType(FrequencyType.DAILY);
        exerciseHabit.setTargetFrequency(1);
        HabitEntry exerciseEntry = new HabitEntry();
        exerciseEntry.setCompletedAt(LocalDateTime.of(2026, 1, 15, 8, 0));
        exerciseEntry.setNotes("Morning run");
        exerciseHabit.addEntry(exerciseEntry);
        habitRepo.save(exerciseHabit);

        Habit readingHabit = new Habit("Reading", "Read books");
        readingHabit.setUser(user);
        readingHabit.setCategoryId(productivityCategory.getId());
        readingHabit.setFrequencyType(FrequencyType.WEEKLY);
        readingHabit.setTargetFrequency(3);
        HabitEntry readingEntry = new HabitEntry();
        readingEntry.setCompletedAt(LocalDateTime.of(2026, 1, 15, 20, 0));
        readingEntry.setNotes("Chapter 5");
        readingHabit.addEntry(readingEntry);
        habitRepo.save(readingHabit);

        DiaryEntry diary1 = new DiaryEntry("Had a productive day", EventSignificance.NORMAL,
                LocalDate.of(2026, 1, 15));
        diary1.setStartTime(LocalTime.of(9, 0));
        diary1.setEndTime(LocalTime.of(17, 0));
        diary1.setUser(user);
        diaryRepo.save(diary1);

        DiaryEntry diary2 = new DiaryEntry("Got promoted", EventSignificance.MAJOR,
                LocalDate.of(2026, 1, 20));
        diary2.setUser(user);
        diaryRepo.save(diary2);

        SleepEntry sleep = new SleepEntry(LocalTime.of(23, 0), LocalTime.of(7, 0),
                LocalDate.of(2026, 1, 15));
        sleep.setNotes("Good sleep");
        sleep.setUser(user);
        sleepRepo.save(sleep);

        EmotionPair pair = new EmotionPair("Sad", "Happy");
        pair.setUser(user);
        emotionPairRepo.save(pair);

        EmotionEntry emotionEntry = new EmotionEntry(pair, 5,
                LocalDateTime.of(2026, 1, 15, 12, 0), "Feeling good after exercise");
        emotionEntry.setUser(user);
        emotionEntryRepo.save(emotionEntry);

        ReminderSettings settings = new ReminderSettings();
        settings.setUser(user);
        settings.setSleepReminderEnabled(true);
        settings.setSleepReminderTime(LocalTime.of(22, 0));
        settings.setDiaryReminderEnabled(true);
        settings.setDiaryReminderTime(LocalTime.of(20, 0));
        settings.setEmotionReminderEnabled(false);
        settings.setEmotionReminderCount(3);
        settings.setWakingHoursStart(LocalTime.of(7, 0));
        settings.setWakingHoursEnd(LocalTime.of(22, 0));
        reminderRepo.save(settings);
    }

    // --- In-memory repository implementations ---

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
}
