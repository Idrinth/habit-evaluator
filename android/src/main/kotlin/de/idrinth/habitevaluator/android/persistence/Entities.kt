package de.idrinth.habitevaluator.android.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "habits", indices = [Index("user_id")])
data class HabitEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "category_id") val categoryId: String?,
    @ColumnInfo(name = "frequency_type") val frequencyType: String,
    @ColumnInfo(name = "target_frequency") val targetFrequency: Int,
    @ColumnInfo(name = "max_entries_per_day") val maxEntriesPerDay: Int,
    @ColumnInfo(name = "positive_scoring") val positiveScoring: Int,
    @ColumnInfo(name = "created_at") val createdAt: String?,
    @ColumnInfo(name = "scoring_rule_id") val scoringRuleId: String?,
    @ColumnInfo(name = "scoring_rule_name") val scoringRuleName: String?,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val userName: String
)

@Entity(tableName = "habit_entries", indices = [Index("habit_id")])
data class HabitEntryEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "habit_id") val habitId: String,
    @ColumnInfo(name = "completed_at") val completedAt: String,
    @ColumnInfo(name = "notes") val notes: String?,
    @ColumnInfo(name = "value") val value: Double
)

@Entity(tableName = "habit_name_translations", primaryKeys = ["habit_id", "language"])
data class HabitNameTranslationEntity(
    @ColumnInfo(name = "habit_id") val habitId: String,
    @ColumnInfo(name = "language") val language: String,
    @ColumnInfo(name = "translated_name") val translatedName: String
)

@Entity(tableName = "habit_description_translations", primaryKeys = ["habit_id", "language"])
data class HabitDescriptionTranslationEntity(
    @ColumnInfo(name = "habit_id") val habitId: String,
    @ColumnInfo(name = "language") val language: String,
    @ColumnInfo(name = "translated_description") val translatedDescription: String
)

@Entity(tableName = "habit_categories", indices = [Index("user_id")])
data class HabitCategoryEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "color") val color: String?,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val userName: String
)

@Entity(tableName = "category_name_translations", primaryKeys = ["category_id", "language"])
data class CategoryNameTranslationEntity(
    @ColumnInfo(name = "category_id") val categoryId: String,
    @ColumnInfo(name = "language") val language: String,
    @ColumnInfo(name = "translated_name") val translatedName: String
)

@Entity(tableName = "category_description_translations", primaryKeys = ["category_id", "language"])
data class CategoryDescriptionTranslationEntity(
    @ColumnInfo(name = "category_id") val categoryId: String,
    @ColumnInfo(name = "language") val language: String,
    @ColumnInfo(name = "translated_description") val translatedDescription: String
)

@Entity(tableName = "diary_references", indices = [Index("user_id"), Index("description_lower")])
data class DiaryReferenceEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "description_lower") val descriptionLower: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val userName: String
)

@Entity(tableName = "diary_entries", indices = [Index("user_id")])
data class DiaryEntryEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "legacy_description") val legacyDescription: String?,
    @ColumnInfo(name = "diary_reference_id") val diaryReferenceId: String?,
    @ColumnInfo(name = "significance") val significance: String,
    @ColumnInfo(name = "event_date") val eventDate: String,
    @ColumnInfo(name = "start_time") val startTime: String?,
    @ColumnInfo(name = "end_time") val endTime: String?,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val userName: String
)

@Entity(tableName = "sleep_entries", indices = [Index("user_id")])
data class SleepEntryEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "from_time") val fromTime: String,
    @ColumnInfo(name = "until_time") val untilTime: String,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "notes") val notes: String?,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val userName: String
)

@Entity(tableName = "emotion_pairs", indices = [Index("user_id")])
data class EmotionPairEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "negative_label") val negativeLabel: String,
    @ColumnInfo(name = "positive_label") val positiveLabel: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val userName: String
)

@Entity(tableName = "emotion_entries", indices = [Index("user_id"), Index("emotion_pair_id")])
data class EmotionEntryEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "emotion_pair_id") val emotionPairId: String,
    @ColumnInfo(name = "strength") val strength: Int,
    @ColumnInfo(name = "recorded_at") val recordedAt: String,
    @ColumnInfo(name = "notes") val notes: String?,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val userName: String
)

@Entity(tableName = "sport_logs", indices = [Index("user_id")])
data class SportLogEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "measurement") val measurement: Double?,
    @ColumnInfo(name = "measurement_unit") val measurementUnit: String?,
    @ColumnInfo(name = "start_time") val startTime: String?,
    @ColumnInfo(name = "end_time") val endTime: String?,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "notes") val notes: String?,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val userName: String
)

@Entity(tableName = "food_logs", indices = [Index("user_id")])
data class FoodLogEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "carbohydrates") val carbohydrates: Double?,
    @ColumnInfo(name = "kcal") val kcal: Int?,
    @ColumnInfo(name = "date_time") val dateTime: String,
    @ColumnInfo(name = "food_items") val foodItems: String?,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "notes") val notes: String?,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val userName: String
)

@Entity(
    tableName = "food_tags",
    indices = [Index("user_id"), Index("name_lower")]
)
data class FoodTagEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "name_lower") val nameLower: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val userName: String
)

@Entity(tableName = "food_log_tags", primaryKeys = ["food_log_id", "food_tag_id"])
data class FoodLogTagCrossRef(
    @ColumnInfo(name = "food_log_id") val foodLogId: String,
    @ColumnInfo(name = "food_tag_id") val foodTagId: String
)

@Entity(tableName = "medications", indices = [Index("user_id")])
data class MedicationEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "wikipedia_link") val wikipediaLink: String?,
    @ColumnInfo(name = "provision_type") val provisionType: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val userName: String
)

@Entity(tableName = "medication_logs", indices = [Index("user_id"), Index("medication_id")])
data class MedicationLogEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "medication_id") val medicationId: String,
    @ColumnInfo(name = "amount") val amount: Double,
    @ColumnInfo(name = "taken_at") val takenAt: String,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "notes") val notes: String?,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val userName: String
)

@Entity(tableName = "emergency_plan_steps", indices = [Index("user_id")])
data class EmergencyPlanStepEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "question") val question: String,
    @ColumnInfo(name = "step_order") val stepOrder: Int,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val userName: String
)

@Entity(tableName = "emergency_plan_actions", indices = [Index("step_id")])
data class EmergencyPlanActionEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "action_text") val actionText: String,
    @ColumnInfo(name = "phone_number") val phoneNumber: String?,
    @ColumnInfo(name = "action_order") val actionOrder: Int,
    @ColumnInfo(name = "step_id") val stepId: String
)

@Entity(tableName = "activity_logs", indices = [Index("user_id")])
data class ActivityLogEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "persons") val persons: String,
    @ColumnInfo(name = "location") val location: String,
    @ColumnInfo(name = "start_time") val startTime: String,
    @ColumnInfo(name = "end_time") val endTime: String,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "activity") val activity: String?,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val userName: String
)

@Entity(tableName = "activity_groups", indices = [Index("user_id")])
data class ActivityGroupEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val userName: String
)

@Entity(tableName = "activity_log_group_links", primaryKeys = ["activity_log_id", "activity_group_id"])
data class ActivityLogGroupLinkEntity(
    @ColumnInfo(name = "activity_log_id") val activityLogId: String,
    @ColumnInfo(name = "activity_group_id") val activityGroupId: String
)

@Entity(tableName = "planner_activities", indices = [Index("user_id")])
data class PlannerActivityEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val userName: String
)

@Entity(tableName = "planner_groups", indices = [Index("user_id")])
data class PlannerGroupEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val userName: String
)

@Entity(tableName = "planner_activity_group_links", primaryKeys = ["activity_id", "group_id"])
data class PlannerActivityGroupLinkEntity(
    @ColumnInfo(name = "activity_id") val activityId: String,
    @ColumnInfo(name = "group_id") val groupId: String
)

@Entity(tableName = "week_planner_slots", indices = [Index("user_id")])
data class WeekPlannerSlotEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "day_of_week") val dayOfWeek: Int,
    @ColumnInfo(name = "hour") val hour: Int,
    @ColumnInfo(name = "duration", defaultValue = "1") val duration: Int = 1,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val userName: String
)

@Entity(tableName = "week_planner_slot_group_links", primaryKeys = ["slot_id", "group_id"])
data class WeekPlannerSlotGroupLinkEntity(
    @ColumnInfo(name = "slot_id") val slotId: String,
    @ColumnInfo(name = "group_id") val groupId: String
)

@Entity(tableName = "slot_confirmations", indices = [Index("user_id"), Index("slot_id"), Index("activity_id")])
data class SlotConfirmationEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "slot_id") val slotId: String?,
    @ColumnInfo(name = "activity_id") val activityId: String?,
    @ColumnInfo(name = "group_id") val groupId: String?,
    @ColumnInfo(name = "confirmed") val confirmed: Boolean,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val userName: String
)

@Entity(tableName = "gratitude_entries", indices = [Index("user_id")])
data class GratitudeEntryEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "reason") val reason: String?,
    @ColumnInfo(name = "event_date") val eventDate: String,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val userName: String
)
