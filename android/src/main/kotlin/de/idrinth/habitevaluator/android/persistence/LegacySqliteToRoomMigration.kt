package de.idrinth.habitevaluator.android.persistence

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.io.File

class LegacySqliteToRoomMigration(
    private val legacyDbFile: File,
    private val habitDao: HabitDao,
    private val habitCategoryDao: HabitCategoryDao,
    private val diaryDao: DiaryDao,
    private val sleepEntryDao: SleepEntryDao,
    private val emotionDao: EmotionDao,
    private val foodLogDao: FoodLogDao,
    private val sportLogDao: SportLogDao,
    private val medicationDao: MedicationDao,
    private val emergencyPlanDao: EmergencyPlanDao,
    private val activityLogDao: ActivityLogDao
) {

    suspend fun migrate() {
        if (!legacyDbFile.exists()) return

        val db = try {
            SQLiteDatabase.openDatabase(legacyDbFile.path, null, SQLiteDatabase.OPEN_READONLY)
        } catch (_: Exception) {
            deleteLegacyFiles()
            return
        }

        try {
            migrateCategories(db)
            migrateHabits(db)
            migrateDiaryReferences(db)
            migrateDiaryEntries(db)
            migrateSleepEntries(db)
            migrateEmotionPairs(db)
            migrateEmotionEntries(db)
            migrateSportLogs(db)
            migrateFoodTags(db)
            migrateFoodLogs(db)
            migrateFoodLogTags(db)
            migrateMedications(db)
            migrateMedicationLogs(db)
            migrateEmergencyPlanSteps(db)
            migrateEmergencyPlanActions(db)
            migrateActivityLogs(db)
        } finally {
            db.close()
            deleteLegacyFiles()
        }
    }

    private fun deleteLegacyFiles() {
        legacyDbFile.delete()
        File(legacyDbFile.path + "-wal").delete()
        File(legacyDbFile.path + "-shm").delete()
        File(legacyDbFile.path + "-journal").delete()
    }

    private fun tableExists(db: SQLiteDatabase, tableName: String): Boolean {
        val cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
            arrayOf(tableName)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    private suspend fun migrateCategories(db: SQLiteDatabase) {
        if (!tableExists(db, "habit_categories")) return
        val cursor = db.rawQuery("SELECT * FROM habit_categories", null)
        while (cursor.moveToNext()) {
            habitCategoryDao.insert(
                HabitCategoryEntity(
                    id = cursor.getString("id"),
                    name = cursor.getStringOrEmpty("name"),
                    description = cursor.getStringOrNull("description"),
                    color = cursor.getStringOrNull("color"),
                    userId = cursor.getStringOrEmpty("user_id"),
                    userName = cursor.getStringOrEmpty("user_name")
                )
            )
        }
        cursor.close()
        migrateCategoryTranslations(db)
    }

    private suspend fun migrateCategoryTranslations(db: SQLiteDatabase) {
        if (!tableExists(db, "category_name_translations")) return
        val cursor = db.rawQuery("SELECT * FROM category_name_translations", null)
        while (cursor.moveToNext()) {
            habitCategoryDao.insertNameTranslation(
                CategoryNameTranslationEntity(
                    categoryId = cursor.getString("category_id"),
                    language = cursor.getString("language"),
                    translatedName = cursor.getString("translated_name")
                )
            )
        }
        cursor.close()

        if (!tableExists(db, "category_description_translations")) return
        val descCursor = db.rawQuery("SELECT * FROM category_description_translations", null)
        while (descCursor.moveToNext()) {
            habitCategoryDao.insertDescTranslation(
                CategoryDescriptionTranslationEntity(
                    categoryId = descCursor.getString("category_id"),
                    language = descCursor.getString("language"),
                    translatedDescription = descCursor.getString("translated_description")
                )
            )
        }
        descCursor.close()
    }

    private suspend fun migrateHabits(db: SQLiteDatabase) {
        if (!tableExists(db, "habits")) return
        val cursor = db.rawQuery("SELECT * FROM habits", null)
        while (cursor.moveToNext()) {
            habitDao.insert(
                HabitEntity(
                    id = cursor.getString("id"),
                    name = cursor.getStringOrEmpty("name"),
                    description = cursor.getStringOrNull("description"),
                    categoryId = cursor.getStringOrNull("category_id"),
                    frequencyType = cursor.getStringOrEmpty("frequency_type"),
                    targetFrequency = cursor.getInt("target_frequency"),
                    maxEntriesPerDay = cursor.getInt("max_entries_per_day"),
                    positiveScoring = cursor.getInt("positive_scoring"),
                    createdAt = cursor.getStringOrNull("created_at"),
                    scoringRuleId = cursor.getStringOrNull("scoring_rule_id"),
                    scoringRuleName = cursor.getStringOrNull("scoring_rule_name"),
                    userId = cursor.getStringOrEmpty("user_id"),
                    userName = cursor.getStringOrEmpty("user_name")
                )
            )
        }
        cursor.close()
        migrateHabitEntries(db)
        migrateHabitTranslations(db)
    }

    private suspend fun migrateHabitEntries(db: SQLiteDatabase) {
        if (!tableExists(db, "habit_entries")) return
        val cursor = db.rawQuery("SELECT * FROM habit_entries", null)
        while (cursor.moveToNext()) {
            habitDao.insertEntry(
                HabitEntryEntity(
                    id = cursor.getString("id"),
                    habitId = cursor.getString("habit_id"),
                    completedAt = cursor.getString("completed_at"),
                    notes = cursor.getStringOrNull("notes"),
                    value = cursor.getInt("value").toDouble()
                )
            )
        }
        cursor.close()
    }

    private suspend fun migrateHabitTranslations(db: SQLiteDatabase) {
        if (!tableExists(db, "habit_name_translations")) return
        val cursor = db.rawQuery("SELECT * FROM habit_name_translations", null)
        while (cursor.moveToNext()) {
            habitDao.insertNameTranslation(
                HabitNameTranslationEntity(
                    habitId = cursor.getString("habit_id"),
                    language = cursor.getString("language"),
                    translatedName = cursor.getString("translated_name")
                )
            )
        }
        cursor.close()

        if (!tableExists(db, "habit_description_translations")) return
        val descCursor = db.rawQuery("SELECT * FROM habit_description_translations", null)
        while (descCursor.moveToNext()) {
            habitDao.insertDescTranslation(
                HabitDescriptionTranslationEntity(
                    habitId = descCursor.getString("habit_id"),
                    language = descCursor.getString("language"),
                    translatedDescription = descCursor.getString("translated_description")
                )
            )
        }
        descCursor.close()
    }

    private suspend fun migrateDiaryReferences(db: SQLiteDatabase) {
        if (!tableExists(db, "diary_references")) return
        val cursor = db.rawQuery("SELECT * FROM diary_references", null)
        while (cursor.moveToNext()) {
            diaryDao.insertReference(
                DiaryReferenceEntity(
                    id = cursor.getString("id"),
                    description = cursor.getStringOrEmpty("description"),
                    descriptionLower = cursor.getStringOrEmpty("description_lower"),
                    userId = cursor.getStringOrEmpty("user_id"),
                    userName = cursor.getStringOrEmpty("user_name")
                )
            )
        }
        cursor.close()
    }

    private suspend fun migrateDiaryEntries(db: SQLiteDatabase) {
        if (!tableExists(db, "diary_entries")) return
        val cursor = db.rawQuery("SELECT * FROM diary_entries", null)
        while (cursor.moveToNext()) {
            diaryDao.insertEntry(
                DiaryEntryEntity(
                    id = cursor.getString("id"),
                    legacyDescription = cursor.getStringOrNull("legacy_description"),
                    diaryReferenceId = cursor.getStringOrNull("diary_reference_id"),
                    significance = cursor.getStringOrEmpty("significance"),
                    eventDate = cursor.getStringOrEmpty("event_date"),
                    startTime = cursor.getStringOrNull("start_time"),
                    endTime = cursor.getStringOrNull("end_time"),
                    createdAt = cursor.getStringOrEmpty("created_at"),
                    userId = cursor.getStringOrEmpty("user_id"),
                    userName = cursor.getStringOrEmpty("user_name")
                )
            )
        }
        cursor.close()
    }

    private suspend fun migrateSleepEntries(db: SQLiteDatabase) {
        if (!tableExists(db, "sleep_entries")) return
        val cursor = db.rawQuery("SELECT * FROM sleep_entries", null)
        while (cursor.moveToNext()) {
            sleepEntryDao.insert(
                SleepEntryEntity(
                    id = cursor.getString("id"),
                    fromTime = cursor.getStringOrEmpty("from_time"),
                    untilTime = cursor.getStringOrEmpty("until_time"),
                    date = cursor.getStringOrEmpty("date"),
                    createdAt = cursor.getStringOrEmpty("created_at"),
                    notes = cursor.getStringOrNull("notes"),
                    userId = cursor.getStringOrEmpty("user_id"),
                    userName = cursor.getStringOrEmpty("user_name")
                )
            )
        }
        cursor.close()
    }

    private suspend fun migrateEmotionPairs(db: SQLiteDatabase) {
        if (!tableExists(db, "emotion_pairs")) return
        val cursor = db.rawQuery("SELECT * FROM emotion_pairs", null)
        while (cursor.moveToNext()) {
            emotionDao.insertPair(
                EmotionPairEntity(
                    id = cursor.getString("id"),
                    negativeLabel = cursor.getStringOrEmpty("negative_label"),
                    positiveLabel = cursor.getStringOrEmpty("positive_label"),
                    userId = cursor.getStringOrEmpty("user_id"),
                    userName = cursor.getStringOrEmpty("user_name")
                )
            )
        }
        cursor.close()
    }

    private suspend fun migrateEmotionEntries(db: SQLiteDatabase) {
        if (!tableExists(db, "emotion_entries")) return
        val cursor = db.rawQuery("SELECT * FROM emotion_entries", null)
        while (cursor.moveToNext()) {
            emotionDao.insertEntry(
                EmotionEntryEntity(
                    id = cursor.getString("id"),
                    emotionPairId = cursor.getStringOrEmpty("emotion_pair_id"),
                    strength = cursor.getInt("strength"),
                    recordedAt = cursor.getStringOrEmpty("recorded_at"),
                    notes = cursor.getStringOrNull("notes"),
                    userId = cursor.getStringOrEmpty("user_id"),
                    userName = cursor.getStringOrEmpty("user_name")
                )
            )
        }
        cursor.close()
    }

    private suspend fun migrateSportLogs(db: SQLiteDatabase) {
        if (!tableExists(db, "sport_logs")) return
        val cursor = db.rawQuery("SELECT * FROM sport_logs", null)
        while (cursor.moveToNext()) {
            sportLogDao.insert(
                SportLogEntity(
                    id = cursor.getString("id"),
                    name = cursor.getStringOrEmpty("name"),
                    measurement = cursor.getDoubleOrNull("measurement"),
                    measurementUnit = cursor.getStringOrNull("measurement_unit"),
                    startTime = cursor.getStringOrNull("start_time"),
                    endTime = cursor.getStringOrNull("end_time"),
                    date = cursor.getStringOrEmpty("date"),
                    createdAt = cursor.getStringOrEmpty("created_at"),
                    notes = cursor.getStringOrNull("notes"),
                    userId = cursor.getStringOrEmpty("user_id"),
                    userName = cursor.getStringOrEmpty("user_name")
                )
            )
        }
        cursor.close()
    }

    private suspend fun migrateFoodTags(db: SQLiteDatabase) {
        if (!tableExists(db, "food_tags")) return
        val cursor = db.rawQuery("SELECT * FROM food_tags", null)
        while (cursor.moveToNext()) {
            foodLogDao.insertTag(
                FoodTagEntity(
                    id = cursor.getString("id"),
                    name = cursor.getStringOrEmpty("name"),
                    nameLower = cursor.getStringOrEmpty("name_lower"),
                    userId = cursor.getStringOrEmpty("user_id"),
                    userName = cursor.getStringOrEmpty("user_name")
                )
            )
        }
        cursor.close()
    }

    private suspend fun migrateFoodLogs(db: SQLiteDatabase) {
        if (!tableExists(db, "food_logs")) return
        val cursor = db.rawQuery("SELECT * FROM food_logs", null)
        while (cursor.moveToNext()) {
            foodLogDao.insert(
                FoodLogEntity(
                    id = cursor.getString("id"),
                    carbohydrates = cursor.getDoubleOrNull("carbohydrates"),
                    kcal = cursor.getIntOrNull("kcal"),
                    dateTime = cursor.getStringOrEmpty("date_time"),
                    foodItems = cursor.getStringOrNull("food_items"),
                    createdAt = cursor.getStringOrEmpty("created_at"),
                    notes = cursor.getStringOrNull("notes"),
                    userId = cursor.getStringOrEmpty("user_id"),
                    userName = cursor.getStringOrEmpty("user_name")
                )
            )
        }
        cursor.close()
    }

    private suspend fun migrateFoodLogTags(db: SQLiteDatabase) {
        if (!tableExists(db, "food_log_tags")) return
        val cursor = db.rawQuery("SELECT * FROM food_log_tags", null)
        while (cursor.moveToNext()) {
            foodLogDao.linkTagToFoodLog(
                FoodLogTagCrossRef(
                    foodLogId = cursor.getString("food_log_id"),
                    foodTagId = cursor.getString("food_tag_id")
                )
            )
        }
        cursor.close()
    }

    private suspend fun migrateMedications(db: SQLiteDatabase) {
        if (!tableExists(db, "medications")) return
        val cursor = db.rawQuery("SELECT * FROM medications", null)
        while (cursor.moveToNext()) {
            medicationDao.insert(
                MedicationEntity(
                    id = cursor.getString("id"),
                    name = cursor.getStringOrEmpty("name"),
                    wikipediaLink = cursor.getStringOrNull("wikipedia_link"),
                    provisionType = cursor.getStringOrEmpty("provision_type"),
                    userId = cursor.getStringOrEmpty("user_id"),
                    userName = cursor.getStringOrEmpty("user_name")
                )
            )
        }
        cursor.close()
    }

    private suspend fun migrateMedicationLogs(db: SQLiteDatabase) {
        if (!tableExists(db, "medication_logs")) return
        val cursor = db.rawQuery("SELECT * FROM medication_logs", null)
        while (cursor.moveToNext()) {
            medicationDao.insertLog(
                MedicationLogEntity(
                    id = cursor.getString("id"),
                    medicationId = cursor.getStringOrEmpty("medication_id"),
                    amount = cursor.getDouble("amount"),
                    takenAt = cursor.getStringOrEmpty("taken_at"),
                    createdAt = cursor.getStringOrEmpty("created_at"),
                    notes = cursor.getStringOrNull("notes"),
                    userId = cursor.getStringOrEmpty("user_id"),
                    userName = cursor.getStringOrEmpty("user_name")
                )
            )
        }
        cursor.close()
    }

    private suspend fun migrateEmergencyPlanSteps(db: SQLiteDatabase) {
        if (!tableExists(db, "emergency_plan_steps")) return
        val cursor = db.rawQuery("SELECT * FROM emergency_plan_steps", null)
        while (cursor.moveToNext()) {
            emergencyPlanDao.insertStep(
                EmergencyPlanStepEntity(
                    id = cursor.getString("id"),
                    question = cursor.getStringOrEmpty("question"),
                    stepOrder = cursor.getInt("step_order"),
                    userId = cursor.getStringOrEmpty("user_id"),
                    userName = cursor.getStringOrEmpty("user_name")
                )
            )
        }
        cursor.close()
    }

    private suspend fun migrateEmergencyPlanActions(db: SQLiteDatabase) {
        if (!tableExists(db, "emergency_plan_actions")) return
        val cursor = db.rawQuery("SELECT * FROM emergency_plan_actions", null)
        while (cursor.moveToNext()) {
            emergencyPlanDao.insertAction(
                EmergencyPlanActionEntity(
                    id = cursor.getString("id"),
                    actionText = cursor.getStringOrEmpty("action_text"),
                    phoneNumber = cursor.getStringOrNull("phone_number"),
                    actionOrder = cursor.getInt("action_order"),
                    stepId = cursor.getStringOrEmpty("step_id")
                )
            )
        }
        cursor.close()
    }

    private suspend fun migrateActivityLogs(db: SQLiteDatabase) {
        if (!tableExists(db, "activity_logs")) return
        val cursor = db.rawQuery("SELECT * FROM activity_logs", null)
        while (cursor.moveToNext()) {
            activityLogDao.insert(
                ActivityLogEntity(
                    id = cursor.getString("id"),
                    persons = cursor.getStringOrEmpty("persons"),
                    location = cursor.getStringOrEmpty("location"),
                    startTime = cursor.getStringOrEmpty("start_time"),
                    endTime = cursor.getStringOrEmpty("end_time"),
                    date = cursor.getStringOrEmpty("date"),
                    activity = cursor.getStringOrNull("activity"),
                    createdAt = cursor.getStringOrEmpty("created_at"),
                    userId = cursor.getStringOrEmpty("user_id"),
                    userName = cursor.getStringOrEmpty("user_name")
                )
            )
        }
        cursor.close()
    }

    private fun Cursor.getString(columnName: String): String {
        val index = getColumnIndex(columnName)
        return if (index >= 0) getString(index) ?: "" else ""
    }

    private fun Cursor.getStringOrEmpty(columnName: String): String {
        val index = getColumnIndex(columnName)
        return if (index >= 0) getString(index) ?: "" else ""
    }

    private fun Cursor.getStringOrNull(columnName: String): String? {
        val index = getColumnIndex(columnName)
        return if (index >= 0 && !isNull(index)) getString(index) else null
    }

    private fun Cursor.getInt(columnName: String): Int {
        val index = getColumnIndex(columnName)
        return if (index >= 0) getInt(index) else 0
    }

    private fun Cursor.getIntOrNull(columnName: String): Int? {
        val index = getColumnIndex(columnName)
        return if (index >= 0 && !isNull(index)) getInt(index) else null
    }

    private fun Cursor.getDouble(columnName: String): Double {
        val index = getColumnIndex(columnName)
        return if (index >= 0) getDouble(index) else 0.0
    }

    private fun Cursor.getDoubleOrNull(columnName: String): Double? {
        val index = getColumnIndex(columnName)
        return if (index >= 0 && !isNull(index)) getDouble(index) else null
    }
}
