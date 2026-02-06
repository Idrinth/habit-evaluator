package de.idrinth.habitevaluator.android.persistence;

import java.io.File;
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

/**
 * Migrates data from legacy JSON file-based storage to SQLite.
 * Checks for existing JSON files on every startup and migrates any found,
 * deleting the files after successful migration.
 */
public class JsonToSqliteMigration {

    private static final String[] JSON_FILES = {
            "habits.json",
            "categories.json",
            "diary_references.json",
            "diary_entries.json",
            "sleep_entries.json",
            "emotion-pairs.json",
            "emotion-entries.json"
    };

    private final File storageDir;
    private final HabitRepository sqliteHabitRepository;
    private final HabitCategoryRepository sqliteCategoryRepository;
    private final DiaryReferenceRepository sqliteDiaryReferenceRepository;
    private final DiaryEntryRepository sqliteDiaryEntryRepository;
    private final SleepEntryRepository sqliteSleepEntryRepository;
    private final EmotionPairRepository sqliteEmotionPairRepository;
    private final EmotionEntryRepository sqliteEmotionEntryRepository;

    public JsonToSqliteMigration(
            File storageDir,
            HabitRepository sqliteHabitRepository,
            HabitCategoryRepository sqliteCategoryRepository,
            DiaryReferenceRepository sqliteDiaryReferenceRepository,
            DiaryEntryRepository sqliteDiaryEntryRepository,
            SleepEntryRepository sqliteSleepEntryRepository,
            EmotionPairRepository sqliteEmotionPairRepository,
            EmotionEntryRepository sqliteEmotionEntryRepository) {
        this.storageDir = storageDir;
        this.sqliteHabitRepository = sqliteHabitRepository;
        this.sqliteCategoryRepository = sqliteCategoryRepository;
        this.sqliteDiaryReferenceRepository = sqliteDiaryReferenceRepository;
        this.sqliteDiaryEntryRepository = sqliteDiaryEntryRepository;
        this.sqliteSleepEntryRepository = sqliteSleepEntryRepository;
        this.sqliteEmotionPairRepository = sqliteEmotionPairRepository;
        this.sqliteEmotionEntryRepository = sqliteEmotionEntryRepository;
    }

    /**
     * Checks whether any legacy JSON files exist that need migration.
     */
    public boolean needsMigration() {
        if (!storageDir.exists()) {
            return false;
        }
        for (String fileName : JSON_FILES) {
            File file = new File(storageDir, fileName);
            if (file.exists() && file.length() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Performs the migration from JSON files to SQLite.
     * Each file type is read via the old FileSystem repositories, saved into the
     * SQLite repositories, and then the JSON file is deleted.
     * Order matters: categories and emotion pairs before habits and emotion entries,
     * diary references before diary entries.
     */
    public void migrate() {
        if (!storageDir.exists()) {
            return;
        }

        migrateCategories();
        migrateHabits();
        migrateDiaryReferences();
        migrateDiaryEntries();
        migrateSleepEntries();
        migrateEmotionPairs();
        migrateEmotionEntries();
    }

    private void migrateCategories() {
        File file = new File(storageDir, "categories.json");
        if (!file.exists() || file.length() == 0) {
            return;
        }
        FileSystemHabitCategoryRepository jsonRepo = new FileSystemHabitCategoryRepository(storageDir);
        List<HabitCategory> categories = jsonRepo.findAll();
        for (HabitCategory category : categories) {
            sqliteCategoryRepository.save(category);
        }
        file.delete();
    }

    private void migrateHabits() {
        File file = new File(storageDir, "habits.json");
        if (!file.exists() || file.length() == 0) {
            return;
        }
        FileSystemHabitRepository jsonRepo = new FileSystemHabitRepository(storageDir);
        List<Habit> habits = jsonRepo.findAll();
        for (Habit habit : habits) {
            sqliteHabitRepository.save(habit);
        }
        file.delete();
    }

    private void migrateDiaryReferences() {
        File file = new File(storageDir, "diary_references.json");
        if (!file.exists() || file.length() == 0) {
            return;
        }
        FileSystemDiaryReferenceRepository jsonRepo = new FileSystemDiaryReferenceRepository(storageDir);
        List<DiaryReference> references = jsonRepo.findAll();
        for (DiaryReference reference : references) {
            sqliteDiaryReferenceRepository.save(reference);
        }
        file.delete();
    }

    private void migrateDiaryEntries() {
        File file = new File(storageDir, "diary_entries.json");
        if (!file.exists() || file.length() == 0) {
            return;
        }
        FileSystemDiaryEntryRepository jsonRepo = new FileSystemDiaryEntryRepository(storageDir);
        // Use the SQLite reference repo for FK resolution since diary_references.json
        // may already be deleted by migrateDiaryReferences()
        jsonRepo.setDiaryReferenceRepository(sqliteDiaryReferenceRepository);
        List<DiaryEntry> entries = jsonRepo.findAll();
        for (DiaryEntry entry : entries) {
            sqliteDiaryEntryRepository.save(entry);
        }
        file.delete();
    }

    private void migrateSleepEntries() {
        File file = new File(storageDir, "sleep_entries.json");
        if (!file.exists() || file.length() == 0) {
            return;
        }
        FileSystemSleepEntryRepository jsonRepo = new FileSystemSleepEntryRepository(storageDir);
        List<SleepEntry> entries = jsonRepo.findAll();
        for (SleepEntry entry : entries) {
            sqliteSleepEntryRepository.save(entry);
        }
        file.delete();
    }

    private void migrateEmotionPairs() {
        File file = new File(storageDir, "emotion-pairs.json");
        if (!file.exists() || file.length() == 0) {
            return;
        }
        FileSystemEmotionPairRepository jsonRepo = new FileSystemEmotionPairRepository(storageDir);
        List<EmotionPair> pairs = jsonRepo.findAll();
        for (EmotionPair pair : pairs) {
            sqliteEmotionPairRepository.save(pair);
        }
        file.delete();
    }

    private void migrateEmotionEntries() {
        File file = new File(storageDir, "emotion-entries.json");
        if (!file.exists() || file.length() == 0) {
            return;
        }
        // Use the SQLite emotion pair repo for FK resolution since emotion-pairs.json
        // may already be deleted by migrateEmotionPairs()
        FileSystemEmotionEntryRepository jsonRepo = new FileSystemEmotionEntryRepository(storageDir, sqliteEmotionPairRepository);
        List<EmotionEntry> entries = jsonRepo.findAll();
        for (EmotionEntry entry : entries) {
            sqliteEmotionEntryRepository.save(entry);
        }
        file.delete();
    }
}
