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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HezBackupServiceTest {

    private HezBackupService hezBackupService;
    private User user;
    private InMemoryHabitRepository habitRepository;
    private InMemoryHabitCategoryRepository categoryRepository;
    private InMemoryDiaryEntryRepository diaryEntryRepository;
    private InMemorySleepEntryRepository sleepEntryRepository;

    @TempDir
    File tempDir;

    @BeforeEach
    void setUp() {
        hezBackupService = new HezBackupService();
        user = new User("testuser", "password", "test@test.com");
        habitRepository = new InMemoryHabitRepository();
        categoryRepository = new InMemoryHabitCategoryRepository();
        diaryEntryRepository = new InMemoryDiaryEntryRepository();
        sleepEntryRepository = new InMemorySleepEntryRepository();
    }

    @Test
    void testCreateHezBackupEmptyPasswordThrows() {
        assertThrows(BackupException.class, () ->
                hezBackupService.createHezBackup("", user, habitRepository,
                        categoryRepository, diaryEntryRepository, sleepEntryRepository));
    }

    @Test
    void testCreateHezBackupNullPasswordThrows() {
        assertThrows(BackupException.class, () ->
                hezBackupService.createHezBackup(null, user, habitRepository,
                        categoryRepository, diaryEntryRepository, sleepEntryRepository));
    }

    @Test
    void testCreateHezBackupNullUserThrows() {
        assertThrows(BackupException.class, () ->
                hezBackupService.createHezBackup("password", null, habitRepository,
                        categoryRepository, diaryEntryRepository, sleepEntryRepository));
    }

    @Test
    void testCreateAndRestoreHezBackup() throws BackupException {
        Habit habit = new Habit("Exercise", "Daily workout");
        habit.setUser(user);
        habitRepository.save(habit);

        byte[] hezData = hezBackupService.createHezBackup("password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        assertNotNull(hezData);
        assertTrue(hezData.length > 0);
        assertTrue(hezBackupService.isValidHezData(hezData));

        BackupData restored = hezBackupService.restoreFromHezBytes(hezData, "password");
        assertNotNull(restored);
        assertNotNull(restored.getUser());
        assertEquals("testuser", restored.getUser().getUsername());
        assertEquals(1, restored.getHabits().size());
        assertEquals("Exercise", restored.getHabits().get(0).getName());
    }

    @Test
    void testCreateHezBackupToFile() throws BackupException {
        Habit habit = new Habit("Exercise", "Daily workout");
        habit.setUser(user);
        habitRepository.save(habit);

        File outputFile = new File(tempDir, "test.hez");
        hezBackupService.createHezBackupToFile(outputFile, "password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
        assertTrue(hezBackupService.isValidHezFile(outputFile));
    }

    @Test
    void testRestoreFromHezFile() throws BackupException {
        Habit habit = new Habit("Exercise", "Daily workout");
        habit.setUser(user);
        habitRepository.save(habit);

        File outputFile = new File(tempDir, "test.hez");
        hezBackupService.createHezBackupToFile(outputFile, "password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        BackupData restored = hezBackupService.restoreFromHezFile(outputFile, "password");
        assertNotNull(restored);
        assertEquals(1, restored.getHabits().size());
        assertEquals("Exercise", restored.getHabits().get(0).getName());
    }

    @Test
    void testRestoreFromHezStream() throws BackupException {
        Habit habit = new Habit("Exercise", "Daily workout");
        habit.setUser(user);
        habitRepository.save(habit);

        byte[] hezData = hezBackupService.createHezBackup("password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(hezData);
        BackupData restored = hezBackupService.restoreFromHezStream(inputStream, "password");
        assertNotNull(restored);
        assertEquals(1, restored.getHabits().size());
    }

    @Test
    void testRestoreHezBackupWrongPassword() throws BackupException {
        habitRepository.save(new Habit("Test", "Test"));
        byte[] hezData = hezBackupService.createHezBackup("password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        assertThrows(BackupException.class, () ->
                hezBackupService.restoreFromHezBytes(hezData, "wrongpassword"));
    }

    @Test
    void testRestoreHezBackupEmptyPasswordThrows() {
        byte[] fakeData = new byte[100];
        assertThrows(BackupException.class, () ->
                hezBackupService.restoreFromHezBytes(fakeData, ""));
    }

    @Test
    void testRestoreFromHezFileNonexistentThrows() {
        File nonExistent = new File(tempDir, "nonexistent.hez");
        assertThrows(BackupException.class, () ->
                hezBackupService.restoreFromHezFile(nonExistent, "password"));
    }

    @Test
    void testHezBackupIncludesDiaryEntries() throws BackupException {
        DiaryEntry entry = new DiaryEntry("Good event", EventSignificance.MAJOR, LocalDate.of(2026, 1, 15));
        entry.setUser(user);
        diaryEntryRepository.save(entry);

        byte[] hezData = hezBackupService.createHezBackup("password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        BackupData restored = hezBackupService.restoreFromHezBytes(hezData, "password");
        assertEquals(1, restored.getDiaryEntries().size());
        assertEquals("Good event", restored.getDiaryEntries().get(0).getDescription());
    }

    @Test
    void testHezBackupIncludesSleepEntries() throws BackupException {
        SleepEntry entry = new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0), LocalDate.of(2026, 1, 15));
        entry.setUser(user);
        sleepEntryRepository.save(entry);

        byte[] hezData = hezBackupService.createHezBackup("password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        BackupData restored = hezBackupService.restoreFromHezBytes(hezData, "password");
        assertEquals(1, restored.getSleepEntries().size());
    }

    @Test
    void testHezBackupIncludesCategories() throws BackupException {
        HabitCategory category = new HabitCategory("Hobbies", "Fun stuff", "#4CAF50");
        category.setUser(user);
        categoryRepository.save(category);

        byte[] hezData = hezBackupService.createHezBackup("password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        BackupData restored = hezBackupService.restoreFromHezBytes(hezData, "password");
        assertEquals(1, restored.getCategories().size());
        assertEquals("Hobbies", restored.getCategories().get(0).getName());
    }

    @Test
    void testMergeFromHezFile() throws BackupException {
        Habit habit = new Habit("Exercise", "Test");
        habit.setUser(user);
        habitRepository.save(habit);

        File outputFile = new File(tempDir, "test.hez");
        hezBackupService.createHezBackupToFile(outputFile, "password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        // Clear local data
        habitRepository.deleteById(habit.getId());
        assertTrue(habitRepository.findByUserId(user.getId()).isEmpty());

        MergeResult result = hezBackupService.mergeFromHezFile(outputFile, "password", user,
                habitRepository, categoryRepository, diaryEntryRepository, sleepEntryRepository);

        assertEquals(1, result.getHabitsAdded());
    }

    @Test
    void testMergeFromHezBytes() throws BackupException {
        Habit habit = new Habit("Exercise", "Test");
        habit.setUser(user);
        habitRepository.save(habit);

        byte[] hezData = hezBackupService.createHezBackup("password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        // Clear local data
        habitRepository.deleteById(habit.getId());
        assertTrue(habitRepository.findByUserId(user.getId()).isEmpty());

        MergeResult result = hezBackupService.mergeFromHezBytes(hezData, "password", user,
                habitRepository, categoryRepository, diaryEntryRepository, sleepEntryRepository);

        assertEquals(1, result.getHabitsAdded());
    }

    @Test
    void testMergeFromHezStream() throws BackupException {
        Habit habit = new Habit("Exercise", "Test");
        habit.setUser(user);
        habitRepository.save(habit);

        byte[] hezData = hezBackupService.createHezBackup("password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);

        // Clear local data
        habitRepository.deleteById(habit.getId());
        assertTrue(habitRepository.findByUserId(user.getId()).isEmpty());

        ByteArrayInputStream inputStream = new ByteArrayInputStream(hezData);
        MergeResult result = hezBackupService.mergeFromHezStream(inputStream, "password", user,
                habitRepository, categoryRepository, diaryEntryRepository, sleepEntryRepository);

        assertEquals(1, result.getHabitsAdded());
    }

    @Test
    void testGenerateDefaultFilename() {
        String filename = hezBackupService.generateDefaultFilename();
        assertNotNull(filename);
        assertTrue(filename.startsWith("habit-evaluator-"));
        assertTrue(filename.endsWith(".hez"));
    }

    @Test
    void testIsValidHezDataWithValidData() throws BackupException {
        byte[] hezData = hezBackupService.createHezBackup("password", user, habitRepository,
                categoryRepository, diaryEntryRepository, sleepEntryRepository);
        assertTrue(hezBackupService.isValidHezData(hezData));
    }

    @Test
    void testIsValidHezDataWithInvalidData() {
        assertFalse(hezBackupService.isValidHezData(null));
        assertFalse(hezBackupService.isValidHezData(new byte[0]));
        assertFalse(hezBackupService.isValidHezData(new byte[]{0, 1, 2, 3}));
    }

    @Test
    void testIsValidHezFileWithInvalidFile() {
        assertFalse(hezBackupService.isValidHezFile(null));
        assertFalse(hezBackupService.isValidHezFile(new File(tempDir, "nonexistent.hez")));
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
}
