package de.idrinth.habitevaluator.shared.backup;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.EventSignificance;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.ReminderSettings;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.ReminderSettingsRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
}
