package de.idrinth.habitevaluator.android.persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.DiaryReference;
import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.DiaryReferenceRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class JsonToSqliteMigrationTest {

    private File tempDir;
    private HabitRepository habitRepository;
    private HabitCategoryRepository categoryRepository;
    private DiaryReferenceRepository diaryReferenceRepository;
    private DiaryEntryRepository diaryEntryRepository;
    private SleepEntryRepository sleepEntryRepository;
    private EmotionPairRepository emotionPairRepository;
    private EmotionEntryRepository emotionEntryRepository;
    private JsonToSqliteMigration migration;

    @Before
    public void setUp() {
        tempDir = new File(System.getProperty("java.io.tmpdir"),
                "migration-test-" + System.nanoTime());
        tempDir.mkdirs();

        habitRepository = mock(HabitRepository.class);
        categoryRepository = mock(HabitCategoryRepository.class);
        diaryReferenceRepository = mock(DiaryReferenceRepository.class);
        diaryEntryRepository = mock(DiaryEntryRepository.class);
        sleepEntryRepository = mock(SleepEntryRepository.class);
        emotionPairRepository = mock(EmotionPairRepository.class);
        emotionEntryRepository = mock(EmotionEntryRepository.class);

        migration = new JsonToSqliteMigration(
                tempDir,
                habitRepository,
                categoryRepository,
                diaryReferenceRepository,
                diaryEntryRepository,
                sleepEntryRepository,
                emotionPairRepository,
                emotionEntryRepository
        );
    }

    @After
    public void tearDown() {
        deleteRecursive(tempDir);
    }

    private void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        file.delete();
    }

    private void createFile(String name, String content) throws IOException {
        File file = new File(tempDir, name);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }

    @Test
    public void testNeedsMigrationReturnsFalseWhenDirectoryDoesNotExist() {
        File nonExistentDir = new File(tempDir, "nonexistent");
        JsonToSqliteMigration migrationWithMissingDir = new JsonToSqliteMigration(
                nonExistentDir,
                habitRepository,
                categoryRepository,
                diaryReferenceRepository,
                diaryEntryRepository,
                sleepEntryRepository,
                emotionPairRepository,
                emotionEntryRepository
        );

        assertFalse(migrationWithMissingDir.needsMigration());
    }

    @Test
    public void testNeedsMigrationReturnsFalseWhenNoJsonFiles() {
        assertFalse(migration.needsMigration());
    }

    @Test
    public void testNeedsMigrationReturnsFalseWhenJsonFilesAreEmpty() throws IOException {
        createFile("habits.json", "");
        createFile("categories.json", "");

        assertFalse(migration.needsMigration());
    }

    @Test
    public void testNeedsMigrationReturnsTrueWhenHabitsJsonHasContent() throws IOException {
        createFile("habits.json", "[]");

        assertTrue(migration.needsMigration());
    }

    @Test
    public void testNeedsMigrationReturnsTrueWhenCategoriesJsonHasContent() throws IOException {
        createFile("categories.json", "[]");

        assertTrue(migration.needsMigration());
    }

    @Test
    public void testNeedsMigrationReturnsTrueWhenSleepEntriesJsonHasContent() throws IOException {
        createFile("sleep_entries.json", "[]");

        assertTrue(migration.needsMigration());
    }

    @Test
    public void testNeedsMigrationReturnsTrueWhenEmotionPairsJsonHasContent() throws IOException {
        createFile("emotion-pairs.json", "[{\"id\":\"1\"}]");

        assertTrue(migration.needsMigration());
    }

    @Test
    public void testNeedsMigrationReturnsTrueWhenDiaryReferencesJsonHasContent() throws IOException {
        createFile("diary_references.json", "[]");

        assertTrue(migration.needsMigration());
    }

    @Test
    public void testNeedsMigrationReturnsTrueWhenDiaryEntriesJsonHasContent() throws IOException {
        createFile("diary_entries.json", "[]");

        assertTrue(migration.needsMigration());
    }

    @Test
    public void testNeedsMigrationReturnsTrueWhenEmotionEntriesJsonHasContent() throws IOException {
        createFile("emotion-entries.json", "[]");

        assertTrue(migration.needsMigration());
    }

    @Test
    public void testMigrateDoesNothingWhenDirectoryDoesNotExist() {
        File nonExistentDir = new File(tempDir, "nonexistent");
        JsonToSqliteMigration migrationWithMissingDir = new JsonToSqliteMigration(
                nonExistentDir,
                habitRepository,
                categoryRepository,
                diaryReferenceRepository,
                diaryEntryRepository,
                sleepEntryRepository,
                emotionPairRepository,
                emotionEntryRepository
        );

        migrationWithMissingDir.migrate();

        verifyNoInteractions(habitRepository, categoryRepository, diaryReferenceRepository,
                diaryEntryRepository, sleepEntryRepository, emotionPairRepository, emotionEntryRepository);
    }

    @Test
    public void testMigrateDoesNothingWhenNoJsonFiles() {
        migration.migrate();

        verifyNoInteractions(habitRepository, categoryRepository, diaryReferenceRepository,
                diaryEntryRepository, sleepEntryRepository, emotionPairRepository, emotionEntryRepository);
    }

    @Test
    public void testMigrateSkipsEmptyFiles() throws IOException {
        createFile("habits.json", "");
        createFile("categories.json", "");

        migration.migrate();

        verifyNoInteractions(habitRepository, categoryRepository);
    }

    @Test
    public void testMigrateDeletesCategoriesJsonAfterMigration() throws IOException {
        createFile("categories.json", "[]");

        migration.migrate();

        assertFalse(new File(tempDir, "categories.json").exists());
    }

    @Test
    public void testMigrateDeletesHabitsJsonAfterMigration() throws IOException {
        createFile("habits.json", "[]");

        migration.migrate();

        assertFalse(new File(tempDir, "habits.json").exists());
    }

    @Test
    public void testMigrateDeletesSleepEntriesJsonAfterMigration() throws IOException {
        createFile("sleep_entries.json", "[]");

        migration.migrate();

        assertFalse(new File(tempDir, "sleep_entries.json").exists());
    }

    @Test
    public void testMigrateDeletesDiaryReferencesJsonAfterMigration() throws IOException {
        createFile("diary_references.json", "[]");

        migration.migrate();

        assertFalse(new File(tempDir, "diary_references.json").exists());
    }

    @Test
    public void testMigrateDeletesDiaryEntriesJsonAfterMigration() throws IOException {
        createFile("diary_entries.json", "[]");

        migration.migrate();

        assertFalse(new File(tempDir, "diary_entries.json").exists());
    }

    @Test
    public void testMigrateDeletesEmotionPairsJsonAfterMigration() throws IOException {
        createFile("emotion-pairs.json", "[]");

        migration.migrate();

        assertFalse(new File(tempDir, "emotion-pairs.json").exists());
    }

    @Test
    public void testMigrateDeletesEmotionEntriesJsonAfterMigration() throws IOException {
        createFile("emotion-entries.json", "[]");

        migration.migrate();

        assertFalse(new File(tempDir, "emotion-entries.json").exists());
    }

    @Test
    public void testNeedsMigrationOnlyChecksKnownFiles() throws IOException {
        createFile("unknown_file.json", "[{\"some\":\"data\"}]");

        assertFalse(migration.needsMigration());
    }
}
