package de.idrinth.habitevaluator.android.persistence

import androidx.room.Database
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Tests for AppDatabase annotation metadata and abstract DAO accessor declarations.
 * Uses reflection instead of an in-memory Room database.
 */
class AppDatabaseTest {

    @Test
    fun testCurrentDatabaseVersion() {
        val annotation = AppDatabase::class.java.getAnnotation(Database::class.java)
        assertNotNull(annotation)
        assertEquals(10, annotation!!.version)
    }

    @Test
    fun testExportSchemaEnabled() {
        val annotation = AppDatabase::class.java.getAnnotation(Database::class.java)
        assertNotNull(annotation)
        assertTrue(annotation!!.exportSchema)
    }

    @Test
    fun testDatabaseEntityCount() {
        val annotation = AppDatabase::class.java.getAnnotation(Database::class.java)
        assertNotNull(annotation)
        assertEquals(21, annotation!!.entities.size)
    }

    @Test
    fun testDatabaseEntitiesContainExpectedClasses() {
        val annotation = AppDatabase::class.java.getAnnotation(Database::class.java)!!
        val entityNames = annotation.entities.map { it.simpleName }
        assertTrue(entityNames.contains("HabitEntity"))
        assertTrue(entityNames.contains("HabitEntryEntity"))
        assertTrue(entityNames.contains("HabitNameTranslationEntity"))
        assertTrue(entityNames.contains("HabitDescriptionTranslationEntity"))
        assertTrue(entityNames.contains("HabitCategoryEntity"))
        assertTrue(entityNames.contains("CategoryNameTranslationEntity"))
        assertTrue(entityNames.contains("CategoryDescriptionTranslationEntity"))
        assertTrue(entityNames.contains("DiaryReferenceEntity"))
        assertTrue(entityNames.contains("DiaryEntryEntity"))
        assertTrue(entityNames.contains("SleepEntryEntity"))
        assertTrue(entityNames.contains("EmotionPairEntity"))
        assertTrue(entityNames.contains("EmotionEntryEntity"))
        assertTrue(entityNames.contains("SportLogEntity"))
        assertTrue(entityNames.contains("FoodLogEntity"))
        assertTrue(entityNames.contains("FoodTagEntity"))
        assertTrue(entityNames.contains("FoodLogTagCrossRef"))
        assertTrue(entityNames.contains("MedicationEntity"))
        assertTrue(entityNames.contains("MedicationLogEntity"))
        assertTrue(entityNames.contains("EmergencyPlanStepEntity"))
        assertTrue(entityNames.contains("EmergencyPlanActionEntity"))
        assertTrue(entityNames.contains("ActivityLogEntity"))
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
