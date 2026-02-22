package de.idrinth.habitevaluator.android.persistence

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for AppDatabase creation, schema version, and DAO access.
 * Replaces the old SQLiteHelperTest which tested the raw SQLite helper.
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testDatabaseCreation() {
        assertNotNull(database)
    }

    @Test
    fun testCurrentDatabaseVersion() {
        // The current Room database version; update this when DATABASE_VERSION changes in AppDatabase
        assertEquals(10, AppDatabase::class.java.getAnnotation(androidx.room.Database::class.java)?.version)
    }

    @Test
    fun testHabitDaoIsAccessible() {
        val dao = database.habitDao()
        assertNotNull(dao)
    }

    @Test
    fun testHabitCategoryDaoIsAccessible() {
        val dao = database.habitCategoryDao()
        assertNotNull(dao)
    }

    @Test
    fun testDiaryDaoIsAccessible() {
        val dao = database.diaryDao()
        assertNotNull(dao)
    }

    @Test
    fun testSleepEntryDaoIsAccessible() {
        val dao = database.sleepEntryDao()
        assertNotNull(dao)
    }

    @Test
    fun testEmotionDaoIsAccessible() {
        val dao = database.emotionDao()
        assertNotNull(dao)
    }

    @Test
    fun testFoodLogDaoIsAccessible() {
        val dao = database.foodLogDao()
        assertNotNull(dao)
    }

    @Test
    fun testSportLogDaoIsAccessible() {
        val dao = database.sportLogDao()
        assertNotNull(dao)
    }

    @Test
    fun testMedicationDaoIsAccessible() {
        val dao = database.medicationDao()
        assertNotNull(dao)
    }

    @Test
    fun testEmergencyPlanDaoIsAccessible() {
        val dao = database.emergencyPlanDao()
        assertNotNull(dao)
    }

    @Test
    fun testActivityLogDaoIsAccessible() {
        val dao = database.activityLogDao()
        assertNotNull(dao)
    }

    @Test
    fun testDatabaseIsOpenAfterCreation() {
        assertTrue(database.isOpen)
    }

    @Test
    fun testDatabaseAllTablesAccessible() {
        // Verify all DAOs return non-null results (database schema is intact)
        assertNotNull(database.habitDao())
        assertNotNull(database.habitCategoryDao())
        assertNotNull(database.diaryDao())
        assertNotNull(database.sleepEntryDao())
        assertNotNull(database.emotionDao())
        assertNotNull(database.foodLogDao())
        assertNotNull(database.sportLogDao())
        assertNotNull(database.medicationDao())
        assertNotNull(database.emergencyPlanDao())
        assertNotNull(database.activityLogDao())
    }

    private fun assertTrue(condition: Boolean) {
        org.junit.Assert.assertTrue(condition)
    }
}
