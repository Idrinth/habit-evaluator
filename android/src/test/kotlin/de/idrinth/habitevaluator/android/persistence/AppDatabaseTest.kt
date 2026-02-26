package de.idrinth.habitevaluator.android.persistence

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
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
    fun testDayPlannerDaoMethodExists() {
        val method = AppDatabase::class.java.getDeclaredMethod("dayPlannerDao")
        assertNotNull(method)
        assertEquals(DayPlannerDao::class.java, method.returnType)
    }

    @Test
    fun testGratitudeDaoMethodExists() {
        val method = AppDatabase::class.java.getDeclaredMethod("gratitudeDao")
        assertNotNull(method)
        assertEquals(GratitudeDao::class.java, method.returnType)
    }

    @Test
    fun testDatabaseHasTwelveDaoMethods() {
        val daoMethods = AppDatabase::class.java.declaredMethods.filter {
            it.name.endsWith("Dao")
        }
        assertEquals(12, daoMethods.size)
    }

    @Test
    fun testMigration10To11Exists() {
        val migration = AppDatabase.MIGRATION_10_11
        assertNotNull(migration)
        assertTrue(migration is Migration)
        assertEquals(10, migration.startVersion)
        assertEquals(11, migration.endVersion)
    }

    @Test
    fun testMigration11To12Exists() {
        val migration = AppDatabase.MIGRATION_11_12
        assertNotNull(migration)
        assertTrue(migration is Migration)
        assertEquals(11, migration.startVersion)
        assertEquals(12, migration.endVersion)
    }

    @Test
    fun testDatabaseIsAbstract() {
        assertTrue(
            java.lang.reflect.Modifier.isAbstract(AppDatabase::class.java.modifiers),
            "AppDatabase should be abstract"
        )
    }

    @Test
    fun testAllDaoMethodsAreAbstract() {
        val daoMethods = AppDatabase::class.java.declaredMethods.filter {
            it.name.endsWith("Dao")
        }
        daoMethods.forEach { method ->
            assertTrue(
                java.lang.reflect.Modifier.isAbstract(method.modifiers),
                "${method.name} should be abstract"
            )
        }
    }

    @Test
    fun testAllDaoMethodsTakeNoParameters() {
        val daoMethods = AppDatabase::class.java.declaredMethods.filter {
            it.name.endsWith("Dao")
        }
        daoMethods.forEach { method ->
            assertEquals(
                0,
                method.parameterCount,
                "${method.name} should take no parameters"
            )
        }
    }

    @Test
    fun testCompanionObjectExists() {
        val companion = AppDatabase::class.java.declaredClasses.find {
            it.simpleName == "Companion"
        }
        assertNotNull(companion, "AppDatabase should have a companion object")
    }

    @Test
    fun testGetInstanceMethodExists() {
        val companion = AppDatabase::class.java.declaredClasses.find {
            it.simpleName == "Companion"
        }
        assertNotNull(companion)
        val method = companion!!.declaredMethods.find { it.name == "getInstance" }
        assertNotNull(method, "Companion should have getInstance method")
    }

    @Test
    fun testHasLegacyDatabaseMethodExists() {
        val companion = AppDatabase::class.java.declaredClasses.find {
            it.simpleName == "Companion"
        }
        assertNotNull(companion)
        val method = companion!!.declaredMethods.find { it.name == "hasLegacyDatabase" }
        assertNotNull(method, "Companion should have hasLegacyDatabase method")
    }

    @Test
    fun testGetLegacyDatabasePathMethodExists() {
        val companion = AppDatabase::class.java.declaredClasses.find {
            it.simpleName == "Companion"
        }
        assertNotNull(companion)
        val method = companion!!.declaredMethods.find { it.name == "getLegacyDatabasePath" }
        assertNotNull(method, "Companion should have getLegacyDatabasePath method")
    }

    @Test
    fun testMigration10To11StartVersionIsCorrect() {
        assertEquals(10, AppDatabase.MIGRATION_10_11.startVersion)
    }

    @Test
    fun testMigration10To11EndVersionIsCorrect() {
        assertEquals(11, AppDatabase.MIGRATION_10_11.endVersion)
    }

    @Test
    fun testMigration11To12StartVersionIsCorrect() {
        assertEquals(11, AppDatabase.MIGRATION_11_12.startVersion)
    }

    @Test
    fun testMigration11To12EndVersionIsCorrect() {
        assertEquals(12, AppDatabase.MIGRATION_11_12.endVersion)
    }

    @Test
    fun testMigration12To13Exists() {
        val migration = AppDatabase.MIGRATION_12_13
        assertNotNull(migration)
        assertTrue(migration is Migration)
        assertEquals(12, migration.startVersion)
        assertEquals(13, migration.endVersion)
    }

    @Test
    fun testMigration12To13StartVersionIsCorrect() {
        assertEquals(12, AppDatabase.MIGRATION_12_13.startVersion)
    }

    @Test
    fun testMigration12To13EndVersionIsCorrect() {
        assertEquals(13, AppDatabase.MIGRATION_12_13.endVersion)
    }

    @Test
    fun testMigration13To14Exists() {
        val migration = AppDatabase.MIGRATION_13_14
        assertNotNull(migration)
        assertTrue(migration is Migration)
        assertEquals(13, migration.startVersion)
        assertEquals(14, migration.endVersion)
    }

    @Test
    fun testMigration13To14StartVersionIsCorrect() {
        assertEquals(13, AppDatabase.MIGRATION_13_14.startVersion)
    }

    @Test
    fun testMigration13To14EndVersionIsCorrect() {
        assertEquals(14, AppDatabase.MIGRATION_13_14.endVersion)
    }

    @Test
    fun testMigrationsAreSequential() {
        assertEquals(
            AppDatabase.MIGRATION_10_11.endVersion,
            AppDatabase.MIGRATION_11_12.startVersion,
            "Migration 10->11 end should match migration 11->12 start"
        )
        assertEquals(
            AppDatabase.MIGRATION_11_12.endVersion,
            AppDatabase.MIGRATION_12_13.startVersion,
            "Migration 11->12 end should match migration 12->13 start"
        )
        assertEquals(
            AppDatabase.MIGRATION_12_13.endVersion,
            AppDatabase.MIGRATION_13_14.startVersion,
            "Migration 12->13 end should match migration 13->14 start"
        )
    }

    @Test
    fun testDaoMethodReturnTypes() {
        val expectedDaos = mapOf(
            "habitDao" to HabitDao::class.java,
            "habitCategoryDao" to HabitCategoryDao::class.java,
            "diaryDao" to DiaryDao::class.java,
            "sleepEntryDao" to SleepEntryDao::class.java,
            "emotionDao" to EmotionDao::class.java,
            "foodLogDao" to FoodLogDao::class.java,
            "sportLogDao" to SportLogDao::class.java,
            "medicationDao" to MedicationDao::class.java,
            "emergencyPlanDao" to EmergencyPlanDao::class.java,
            "activityLogDao" to ActivityLogDao::class.java,
            "dayPlannerDao" to DayPlannerDao::class.java,
            "gratitudeDao" to GratitudeDao::class.java
        )
        expectedDaos.forEach { (name, expectedType) ->
            val method = AppDatabase::class.java.getDeclaredMethod(name)
            assertEquals(expectedType, method.returnType, "$name should return $expectedType")
        }
    }
}
