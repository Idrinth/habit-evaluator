package de.idrinth.habitevaluator.android.persistence

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.io.File

@Database(
    entities = [
        HabitEntity::class,
        HabitEntryEntity::class,
        HabitNameTranslationEntity::class,
        HabitDescriptionTranslationEntity::class,
        HabitCategoryEntity::class,
        CategoryNameTranslationEntity::class,
        CategoryDescriptionTranslationEntity::class,
        DiaryReferenceEntity::class,
        DiaryEntryEntity::class,
        SleepEntryEntity::class,
        EmotionPairEntity::class,
        EmotionEntryEntity::class,
        SportLogEntity::class,
        FoodLogEntity::class,
        FoodTagEntity::class,
        FoodLogTagCrossRef::class,
        MedicationEntity::class,
        MedicationLogEntity::class,
        EmergencyPlanStepEntity::class,
        EmergencyPlanActionEntity::class,
        ActivityLogEntity::class,
        PlannerActivityEntity::class,
        PlannerGroupEntity::class,
        PlannerActivityGroupLinkEntity::class,
        WeekPlannerSlotEntity::class,
        SlotConfirmationEntity::class
    ],
    version = 11,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitCategoryDao(): HabitCategoryDao
    abstract fun diaryDao(): DiaryDao
    abstract fun sleepEntryDao(): SleepEntryDao
    abstract fun emotionDao(): EmotionDao
    abstract fun foodLogDao(): FoodLogDao
    abstract fun sportLogDao(): SportLogDao
    abstract fun medicationDao(): MedicationDao
    abstract fun emergencyPlanDao(): EmergencyPlanDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun dayPlannerDao(): DayPlannerDao

    companion object {
        private const val DATABASE_NAME = "habit_evaluator.db"
        private const val LEGACY_DATABASE_NAME = "habit_evaluator_legacy.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }

        fun hasLegacyDatabase(context: Context): Boolean {
            return context.getDatabasePath(LEGACY_DATABASE_NAME).exists()
        }

        fun getLegacyDatabasePath(context: Context): File {
            return context.getDatabasePath(LEGACY_DATABASE_NAME)
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `planner_activities` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT,
                        `created_at` TEXT NOT NULL,
                        `user_id` TEXT NOT NULL,
                        `user_name` TEXT NOT NULL,
                        PRIMARY KEY(`id`))"""
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_planner_activities_user_id` ON `planner_activities` (`user_id`)")

                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `planner_groups` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT,
                        `created_at` TEXT NOT NULL,
                        `user_id` TEXT NOT NULL,
                        `user_name` TEXT NOT NULL,
                        PRIMARY KEY(`id`))"""
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_planner_groups_user_id` ON `planner_groups` (`user_id`)")

                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `planner_activity_group_links` (
                        `activity_id` TEXT NOT NULL,
                        `group_id` TEXT NOT NULL,
                        PRIMARY KEY(`activity_id`, `group_id`))"""
                )

                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `week_planner_slots` (
                        `id` TEXT NOT NULL,
                        `day_of_week` INTEGER NOT NULL,
                        `hour` INTEGER NOT NULL,
                        `group_id` TEXT,
                        `user_id` TEXT NOT NULL,
                        `user_name` TEXT NOT NULL,
                        PRIMARY KEY(`id`))"""
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_week_planner_slots_user_id` ON `week_planner_slots` (`user_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_week_planner_slots_group_id` ON `week_planner_slots` (`group_id`)")

                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `slot_confirmations` (
                        `id` TEXT NOT NULL,
                        `slot_id` TEXT,
                        `activity_id` TEXT,
                        `group_id` TEXT,
                        `confirmed` INTEGER NOT NULL,
                        `date` TEXT NOT NULL,
                        `created_at` TEXT NOT NULL,
                        `user_id` TEXT NOT NULL,
                        `user_name` TEXT NOT NULL,
                        PRIMARY KEY(`id`))"""
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_slot_confirmations_user_id` ON `slot_confirmations` (`user_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_slot_confirmations_slot_id` ON `slot_confirmations` (`slot_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_slot_confirmations_activity_id` ON `slot_confirmations` (`activity_id`)")
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            handlePreRoomDatabase(context)
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            ).addMigrations(MIGRATION_10_11).build()
        }

        private fun handlePreRoomDatabase(context: Context) {
            val dbFile = context.getDatabasePath(DATABASE_NAME)
            if (!dbFile.exists()) return

            if (!isRoomDatabase(dbFile)) {
                val legacyFile = context.getDatabasePath(LEGACY_DATABASE_NAME)
                dbFile.renameTo(legacyFile)
                File(dbFile.path + "-wal").let { wal ->
                    if (wal.exists()) wal.renameTo(File(legacyFile.path + "-wal"))
                }
                File(dbFile.path + "-shm").let { shm ->
                    if (shm.exists()) shm.renameTo(File(legacyFile.path + "-shm"))
                }
                File(dbFile.path + "-journal").let { journal ->
                    if (journal.exists()) journal.renameTo(File(legacyFile.path + "-journal"))
                }
            }
        }

        private fun isRoomDatabase(dbFile: File): Boolean {
            return try {
                val db = SQLiteDatabase.openDatabase(
                    dbFile.path, null, SQLiteDatabase.OPEN_READONLY
                )
                val cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='room_master_table'",
                    null
                )
                val hasRoomTable = cursor.count > 0
                cursor.close()
                db.close()
                hasRoomTable
            } catch (_: Exception) {
                false
            }
        }
    }
}
