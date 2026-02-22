package de.idrinth.habitevaluator.android.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

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
        ActivityLogEntity::class
    ],
    version = 10,
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

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_evaluator.db"
                ).build().also { instance = it }
            }
    }
}
