package de.idrinth.habitevaluator.shared.backup;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EventSignificance;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.time.LocalDate;
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
}
