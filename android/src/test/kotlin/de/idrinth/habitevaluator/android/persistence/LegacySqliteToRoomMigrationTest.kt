package de.idrinth.habitevaluator.android.persistence

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import java.io.File

class LegacySqliteToRoomMigrationTest {

    private lateinit var tempDir: File
    private lateinit var habitDao: HabitDao
    private lateinit var habitCategoryDao: HabitCategoryDao
    private lateinit var diaryDao: DiaryDao
    private lateinit var sleepEntryDao: SleepEntryDao
    private lateinit var emotionDao: EmotionDao
    private lateinit var foodLogDao: FoodLogDao
    private lateinit var sportLogDao: SportLogDao
    private lateinit var medicationDao: MedicationDao
    private lateinit var emergencyPlanDao: EmergencyPlanDao
    private lateinit var activityLogDao: ActivityLogDao

    @BeforeEach
    fun setUp() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "legacy-migration-test-${System.nanoTime()}")
        tempDir.mkdirs()

        habitDao = mock(HabitDao::class.java)
        habitCategoryDao = mock(HabitCategoryDao::class.java)
        diaryDao = mock(DiaryDao::class.java)
        sleepEntryDao = mock(SleepEntryDao::class.java)
        emotionDao = mock(EmotionDao::class.java)
        foodLogDao = mock(FoodLogDao::class.java)
        sportLogDao = mock(SportLogDao::class.java)
        medicationDao = mock(MedicationDao::class.java)
        emergencyPlanDao = mock(EmergencyPlanDao::class.java)
        activityLogDao = mock(ActivityLogDao::class.java)
    }

    @AfterEach
    fun tearDown() {
        tempDir.listFiles()?.forEach { it.delete() }
        tempDir.delete()
    }

    private fun createMigration(dbFile: File) = LegacySqliteToRoomMigration(
        dbFile, habitDao, habitCategoryDao, diaryDao, sleepEntryDao,
        emotionDao, foodLogDao, sportLogDao, medicationDao,
        emergencyPlanDao, activityLogDao
    )

    @Test
    fun testMigrateDoesNothingWhenFileDoesNotExist() = runTest {
        val nonExistentFile = File(tempDir, "nonexistent.db")
        val migration = createMigration(nonExistentFile)

        migration.migrate()

        verifyNoInteractions(
            habitDao, habitCategoryDao, diaryDao, sleepEntryDao,
            emotionDao, foodLogDao, sportLogDao, medicationDao,
            emergencyPlanDao, activityLogDao
        )
    }

    @Test
    fun testConstructorAcceptsAllDaoParameters() {
        val dbFile = File(tempDir, "test.db")
        val migration = createMigration(dbFile)
        assertNotNull(migration)
    }
}
