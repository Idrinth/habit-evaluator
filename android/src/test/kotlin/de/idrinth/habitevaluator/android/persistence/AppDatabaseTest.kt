package de.idrinth.habitevaluator.android.persistence

import androidx.room.RoomDatabase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Tests for AppDatabase abstract DAO accessor declarations using reflection.
 * Note: Room's @Database annotation has CLASS retention and cannot be read
 * at runtime, so annotation-level assertions (version, entities, exportSchema)
 * are not testable via reflection.
 */
class AppDatabaseTest {

    @Test
    fun testExtendsRoomDatabase() {
        assertTrue(RoomDatabase::class.java.isAssignableFrom(AppDatabase::class.java))
    }

    @Test
    fun testHabitDaoMethodExists() {
        val method = AppDatabase::class.java.getDeclaredMethod("habitDao")
        assertNotNull(method)
        assertEquals(HabitDao::class.java, method.returnType)
    }

    @Test
    fun testHabitCategoryDaoMethodExists() {
        val method = AppDatabase::class.java.getDeclaredMethod("habitCategoryDao")
        assertNotNull(method)
        assertEquals(HabitCategoryDao::class.java, method.returnType)
    }

    @Test
    fun testDiaryDaoMethodExists() {
        val method = AppDatabase::class.java.getDeclaredMethod("diaryDao")
        assertNotNull(method)
        assertEquals(DiaryDao::class.java, method.returnType)
    }

    @Test
    fun testSleepEntryDaoMethodExists() {
        val method = AppDatabase::class.java.getDeclaredMethod("sleepEntryDao")
        assertNotNull(method)
        assertEquals(SleepEntryDao::class.java, method.returnType)
    }

    @Test
    fun testEmotionDaoMethodExists() {
        val method = AppDatabase::class.java.getDeclaredMethod("emotionDao")
        assertNotNull(method)
        assertEquals(EmotionDao::class.java, method.returnType)
    }

    @Test
    fun testFoodLogDaoMethodExists() {
        val method = AppDatabase::class.java.getDeclaredMethod("foodLogDao")
        assertNotNull(method)
        assertEquals(FoodLogDao::class.java, method.returnType)
    }

    @Test
    fun testSportLogDaoMethodExists() {
        val method = AppDatabase::class.java.getDeclaredMethod("sportLogDao")
        assertNotNull(method)
        assertEquals(SportLogDao::class.java, method.returnType)
    }

    @Test
    fun testMedicationDaoMethodExists() {
        val method = AppDatabase::class.java.getDeclaredMethod("medicationDao")
        assertNotNull(method)
        assertEquals(MedicationDao::class.java, method.returnType)
    }

    @Test
    fun testEmergencyPlanDaoMethodExists() {
        val method = AppDatabase::class.java.getDeclaredMethod("emergencyPlanDao")
        assertNotNull(method)
        assertEquals(EmergencyPlanDao::class.java, method.returnType)
    }

    @Test
    fun testActivityLogDaoMethodExists() {
        val method = AppDatabase::class.java.getDeclaredMethod("activityLogDao")
        assertNotNull(method)
        assertEquals(ActivityLogDao::class.java, method.returnType)
    }

    @Test
    fun testDatabaseHasTenDaoMethods() {
        val daoMethods = AppDatabase::class.java.declaredMethods.filter {
            it.name.endsWith("Dao")
        }
        assertEquals(10, daoMethods.size)
    }
}
