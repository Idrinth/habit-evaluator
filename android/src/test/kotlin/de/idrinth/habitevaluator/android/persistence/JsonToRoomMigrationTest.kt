package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository
import de.idrinth.habitevaluator.shared.repository.DiaryReferenceRepository
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository
import de.idrinth.habitevaluator.shared.repository.HabitRepository
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import java.io.File
import java.io.FileWriter

class JsonToRoomMigrationTest {

    private lateinit var tempDir: File
    private lateinit var habitRepository: HabitRepository
    private lateinit var categoryRepository: HabitCategoryRepository
    private lateinit var diaryReferenceRepository: DiaryReferenceRepository
    private lateinit var diaryEntryRepository: DiaryEntryRepository
    private lateinit var sleepEntryRepository: SleepEntryRepository
    private lateinit var emotionPairRepository: EmotionPairRepository
    private lateinit var emotionEntryRepository: EmotionEntryRepository
    private lateinit var migration: JsonToRoomMigration

    @Before
    fun setUp() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "migration-test-${System.nanoTime()}")
        tempDir.mkdirs()

        habitRepository = mock(HabitRepository::class.java)
        categoryRepository = mock(HabitCategoryRepository::class.java)
        diaryReferenceRepository = mock(DiaryReferenceRepository::class.java)
        diaryEntryRepository = mock(DiaryEntryRepository::class.java)
        sleepEntryRepository = mock(SleepEntryRepository::class.java)
        emotionPairRepository = mock(EmotionPairRepository::class.java)
        emotionEntryRepository = mock(EmotionEntryRepository::class.java)

        migration = JsonToRoomMigration(
            tempDir,
            habitRepository,
            categoryRepository,
            diaryReferenceRepository,
            diaryEntryRepository,
            sleepEntryRepository,
            emotionPairRepository,
            emotionEntryRepository
        )
    }

    @After
    fun tearDown() {
        deleteRecursive(tempDir)
    }

    private fun deleteRecursive(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach { deleteRecursive(it) }
        }
        file.delete()
    }

    private fun createFile(name: String, content: String) {
        val file = File(tempDir, name)
        FileWriter(file).use { it.write(content) }
    }

    @Test
    fun testNeedsMigrationReturnsFalseWhenDirectoryDoesNotExist() {
        val nonExistentDir = File(tempDir, "nonexistent")
        val migrationWithMissingDir = JsonToRoomMigration(
            nonExistentDir,
            habitRepository,
            categoryRepository,
            diaryReferenceRepository,
            diaryEntryRepository,
            sleepEntryRepository,
            emotionPairRepository,
            emotionEntryRepository
        )

        assertFalse(migrationWithMissingDir.needsMigration())
    }

    @Test
    fun testNeedsMigrationReturnsFalseWhenNoJsonFiles() {
        assertFalse(migration.needsMigration())
    }

    @Test
    fun testNeedsMigrationReturnsFalseWhenJsonFilesAreEmpty() {
        createFile("habits.json", "")
        createFile("categories.json", "")

        assertFalse(migration.needsMigration())
    }

    @Test
    fun testNeedsMigrationReturnsTrueWhenHabitsJsonHasContent() {
        createFile("habits.json", "[]")

        assertTrue(migration.needsMigration())
    }

    @Test
    fun testNeedsMigrationReturnsTrueWhenCategoriesJsonHasContent() {
        createFile("categories.json", "[]")

        assertTrue(migration.needsMigration())
    }

    @Test
    fun testNeedsMigrationReturnsTrueWhenSleepEntriesJsonHasContent() {
        createFile("sleep_entries.json", "[]")

        assertTrue(migration.needsMigration())
    }

    @Test
    fun testNeedsMigrationReturnsTrueWhenEmotionPairsJsonHasContent() {
        createFile("emotion-pairs.json", "[{\"id\":\"1\"}]")

        assertTrue(migration.needsMigration())
    }

    @Test
    fun testNeedsMigrationReturnsTrueWhenDiaryReferencesJsonHasContent() {
        createFile("diary_references.json", "[]")

        assertTrue(migration.needsMigration())
    }

    @Test
    fun testNeedsMigrationReturnsTrueWhenDiaryEntriesJsonHasContent() {
        createFile("diary_entries.json", "[]")

        assertTrue(migration.needsMigration())
    }

    @Test
    fun testNeedsMigrationReturnsTrueWhenEmotionEntriesJsonHasContent() {
        createFile("emotion-entries.json", "[]")

        assertTrue(migration.needsMigration())
    }

    @Test
    fun testMigrateDoesNothingWhenDirectoryDoesNotExist() {
        val nonExistentDir = File(tempDir, "nonexistent")
        val migrationWithMissingDir = JsonToRoomMigration(
            nonExistentDir,
            habitRepository,
            categoryRepository,
            diaryReferenceRepository,
            diaryEntryRepository,
            sleepEntryRepository,
            emotionPairRepository,
            emotionEntryRepository
        )

        migrationWithMissingDir.migrate()

        verifyNoInteractions(
            habitRepository,
            categoryRepository,
            diaryReferenceRepository,
            diaryEntryRepository,
            sleepEntryRepository,
            emotionPairRepository,
            emotionEntryRepository
        )
    }

    @Test
    fun testMigrateDoesNothingWhenNoJsonFiles() {
        migration.migrate()

        verifyNoInteractions(
            habitRepository,
            categoryRepository,
            diaryReferenceRepository,
            diaryEntryRepository,
            sleepEntryRepository,
            emotionPairRepository,
            emotionEntryRepository
        )
    }

    @Test
    fun testMigrateSkipsEmptyFiles() {
        createFile("habits.json", "")
        createFile("categories.json", "")

        migration.migrate()

        verifyNoInteractions(habitRepository, categoryRepository)
    }

    @Test
    fun testMigrateDeletesCategoriesJsonAfterMigration() {
        createFile("categories.json", "[]")

        migration.migrate()

        assertFalse(File(tempDir, "categories.json").exists())
    }

    @Test
    fun testMigrateDeletesHabitsJsonAfterMigration() {
        createFile("habits.json", "[]")

        migration.migrate()

        assertFalse(File(tempDir, "habits.json").exists())
    }

    @Test
    fun testMigrateDeletesSleepEntriesJsonAfterMigration() {
        createFile("sleep_entries.json", "[]")

        migration.migrate()

        assertFalse(File(tempDir, "sleep_entries.json").exists())
    }

    @Test
    fun testMigrateDeletesDiaryReferencesJsonAfterMigration() {
        createFile("diary_references.json", "[]")

        migration.migrate()

        assertFalse(File(tempDir, "diary_references.json").exists())
    }

    @Test
    fun testMigrateDeletesDiaryEntriesJsonAfterMigration() {
        createFile("diary_entries.json", "[]")

        migration.migrate()

        assertFalse(File(tempDir, "diary_entries.json").exists())
    }

    @Test
    fun testMigrateDeletesEmotionPairsJsonAfterMigration() {
        createFile("emotion-pairs.json", "[]")

        migration.migrate()

        assertFalse(File(tempDir, "emotion-pairs.json").exists())
    }

    @Test
    fun testMigrateDeletesEmotionEntriesJsonAfterMigration() {
        createFile("emotion-entries.json", "[]")

        migration.migrate()

        assertFalse(File(tempDir, "emotion-entries.json").exists())
    }

    @Test
    fun testNeedsMigrationOnlyChecksKnownFiles() {
        createFile("unknown_file.json", "[{\"some\":\"data\"}]")

        assertFalse(migration.needsMigration())
    }
}
