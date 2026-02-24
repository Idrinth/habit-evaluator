package de.idrinth.habitevaluator.android.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: HabitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: HabitEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNameTranslation(t: HabitNameTranslationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDescTranslation(t: HabitDescriptionTranslationEntity)

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun findById(id: String): HabitEntity?

    @Query("SELECT * FROM habits")
    suspend fun findAll(): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE user_id = :userId ORDER BY name COLLATE NOCASE")
    suspend fun findByUserId(userId: String): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE user_id = :userId ORDER BY name COLLATE NOCASE")
    fun observeByUserId(userId: String): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habit_entries WHERE habit_id = :habitId")
    suspend fun findEntriesByHabitId(habitId: String): List<HabitEntryEntity>

    @Query("SELECT * FROM habit_name_translations WHERE habit_id = :habitId")
    suspend fun findNameTranslations(habitId: String): List<HabitNameTranslationEntity>

    @Query("SELECT * FROM habit_description_translations WHERE habit_id = :habitId")
    suspend fun findDescTranslations(habitId: String): List<HabitDescriptionTranslationEntity>

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM habit_entries WHERE habit_id = :habitId")
    suspend fun deleteEntriesByHabitId(habitId: String)

    @Query("DELETE FROM habit_name_translations WHERE habit_id = :habitId")
    suspend fun deleteNameTranslations(habitId: String)

    @Query("DELETE FROM habit_description_translations WHERE habit_id = :habitId")
    suspend fun deleteDescTranslations(habitId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM habits WHERE id = :id)")
    suspend fun existsById(id: String): Boolean

    @Transaction
    suspend fun saveWithDetails(
        habit: HabitEntity,
        entries: List<HabitEntryEntity>,
        nameTranslations: List<HabitNameTranslationEntity>,
        descTranslations: List<HabitDescriptionTranslationEntity>
    ) {
        insert(habit)
        deleteEntriesByHabitId(habit.id)
        entries.forEach { insertEntry(it) }
        deleteNameTranslations(habit.id)
        nameTranslations.forEach { insertNameTranslation(it) }
        deleteDescTranslations(habit.id)
        descTranslations.forEach { insertDescTranslation(it) }
    }

    @Transaction
    suspend fun deleteWithDetails(id: String) {
        deleteEntriesByHabitId(id)
        deleteNameTranslations(id)
        deleteDescTranslations(id)
        deleteById(id)
    }
}

@Dao
interface HabitCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: HabitCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNameTranslation(t: CategoryNameTranslationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDescTranslation(t: CategoryDescriptionTranslationEntity)

    @Query("SELECT * FROM habit_categories WHERE id = :id")
    suspend fun findById(id: String): HabitCategoryEntity?

    @Query("SELECT * FROM habit_categories")
    suspend fun findAll(): List<HabitCategoryEntity>

    @Query("SELECT * FROM habit_categories WHERE user_id = :userId ORDER BY name COLLATE NOCASE")
    suspend fun findByUserId(userId: String): List<HabitCategoryEntity>

    @Query("SELECT * FROM habit_categories WHERE user_id = :userId ORDER BY name COLLATE NOCASE")
    fun observeByUserId(userId: String): Flow<List<HabitCategoryEntity>>

    @Query("SELECT * FROM category_name_translations WHERE category_id = :categoryId")
    suspend fun findNameTranslations(categoryId: String): List<CategoryNameTranslationEntity>

    @Query("SELECT * FROM category_description_translations WHERE category_id = :categoryId")
    suspend fun findDescTranslations(categoryId: String): List<CategoryDescriptionTranslationEntity>

    @Query("DELETE FROM habit_categories WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM category_name_translations WHERE category_id = :categoryId")
    suspend fun deleteNameTranslations(categoryId: String)

    @Query("DELETE FROM category_description_translations WHERE category_id = :categoryId")
    suspend fun deleteDescTranslations(categoryId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM habit_categories WHERE id = :id)")
    suspend fun existsById(id: String): Boolean

    @Transaction
    suspend fun saveWithDetails(
        category: HabitCategoryEntity,
        nameTranslations: List<CategoryNameTranslationEntity>,
        descTranslations: List<CategoryDescriptionTranslationEntity>
    ) {
        insert(category)
        deleteNameTranslations(category.id)
        nameTranslations.forEach { insertNameTranslation(it) }
        deleteDescTranslations(category.id)
        descTranslations.forEach { insertDescTranslation(it) }
    }

    @Transaction
    suspend fun deleteWithDetails(id: String) {
        deleteNameTranslations(id)
        deleteDescTranslations(id)
        deleteById(id)
    }
}

@Dao
interface DiaryDao {
    // References
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReference(ref: DiaryReferenceEntity)

    @Query("SELECT * FROM diary_references WHERE id = :id")
    suspend fun findReferenceById(id: String): DiaryReferenceEntity?

    @Query("SELECT * FROM diary_references WHERE user_id = :userId")
    suspend fun findReferencesByUserId(userId: String): List<DiaryReferenceEntity>

    @Query("SELECT * FROM diary_references WHERE user_id = :userId AND description_lower = :descLower LIMIT 1")
    suspend fun findReferenceByUserIdAndDescLower(userId: String, descLower: String): DiaryReferenceEntity?

    @Query("DELETE FROM diary_references WHERE id = :id")
    suspend fun deleteReferenceById(id: String)

    @Query(
        """SELECT DISTINCT description FROM diary_references WHERE user_id = :userId
        UNION SELECT DISTINCT legacy_description FROM diary_entries
        WHERE user_id = :userId AND legacy_description IS NOT NULL AND legacy_description != ''
        ORDER BY description"""
    )
    suspend fun findDistinctDescriptions(userId: String): List<String>

    // Entries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DiaryEntryEntity)

    @Query("SELECT * FROM diary_entries WHERE id = :id")
    suspend fun findEntryById(id: String): DiaryEntryEntity?

    @Query("SELECT * FROM diary_entries")
    suspend fun findAllEntries(): List<DiaryEntryEntity>

    @Query("SELECT * FROM diary_entries WHERE user_id = :userId ORDER BY event_date DESC, start_time DESC")
    suspend fun findEntriesByUserId(userId: String): List<DiaryEntryEntity>

    @Query("SELECT * FROM diary_entries WHERE user_id = :userId ORDER BY event_date DESC, start_time DESC")
    fun observeEntriesByUserId(userId: String): Flow<List<DiaryEntryEntity>>

    @Query("DELETE FROM diary_entries WHERE id = :id")
    suspend fun deleteEntryById(id: String)

    @Query(
        """SELECT * FROM diary_entries WHERE user_id = :userId
        AND legacy_description IS NOT NULL AND legacy_description != ''
        AND diary_reference_id IS NULL"""
    )
    suspend fun findEntriesNeedingMigration(userId: String): List<DiaryEntryEntity>
}

@Dao
interface SleepEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: SleepEntryEntity)

    @Query("SELECT * FROM sleep_entries WHERE id = :id")
    suspend fun findById(id: String): SleepEntryEntity?

    @Query("SELECT * FROM sleep_entries")
    suspend fun findAll(): List<SleepEntryEntity>

    @Query("SELECT * FROM sleep_entries WHERE user_id = :userId ORDER BY date DESC, from_time DESC")
    suspend fun findByUserId(userId: String): List<SleepEntryEntity>

    @Query("SELECT * FROM sleep_entries WHERE user_id = :userId ORDER BY date DESC, from_time DESC")
    fun observeByUserId(userId: String): Flow<List<SleepEntryEntity>>

    @Query("DELETE FROM sleep_entries WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM sleep_entries WHERE id = :id)")
    suspend fun existsById(id: String): Boolean
}

@Dao
interface EmotionDao {
    // Pairs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPair(pair: EmotionPairEntity)

    @Query("SELECT * FROM emotion_pairs WHERE id = :id")
    suspend fun findPairById(id: String): EmotionPairEntity?

    @Query("SELECT * FROM emotion_pairs")
    suspend fun findAllPairs(): List<EmotionPairEntity>

    @Query("SELECT * FROM emotion_pairs WHERE user_id = :userId ORDER BY negative_label COLLATE NOCASE")
    suspend fun findPairsByUserId(userId: String): List<EmotionPairEntity>

    @Query("SELECT * FROM emotion_pairs WHERE user_id = :userId ORDER BY negative_label COLLATE NOCASE")
    fun observePairsByUserId(userId: String): Flow<List<EmotionPairEntity>>

    @Query("DELETE FROM emotion_pairs WHERE id = :id")
    suspend fun deletePairById(id: String)

    // Entries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: EmotionEntryEntity)

    @Query("SELECT * FROM emotion_entries WHERE id = :id")
    suspend fun findEntryById(id: String): EmotionEntryEntity?

    @Query("SELECT * FROM emotion_entries")
    suspend fun findAllEntries(): List<EmotionEntryEntity>

    @Query("SELECT * FROM emotion_entries WHERE user_id = :userId ORDER BY recorded_at DESC")
    suspend fun findEntriesByUserId(userId: String): List<EmotionEntryEntity>

    @Query("SELECT * FROM emotion_entries WHERE user_id = :userId ORDER BY recorded_at DESC")
    fun observeEntriesByUserId(userId: String): Flow<List<EmotionEntryEntity>>

    @Query("DELETE FROM emotion_entries WHERE id = :id")
    suspend fun deleteEntryById(id: String)
}

@Dao
interface FoodLogDao {
    // Food Logs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: FoodLogEntity)

    @Query("SELECT * FROM food_logs WHERE id = :id")
    suspend fun findById(id: String): FoodLogEntity?

    @Query("SELECT * FROM food_logs")
    suspend fun findAll(): List<FoodLogEntity>

    @Query("SELECT * FROM food_logs WHERE user_id = :userId ORDER BY date_time DESC")
    suspend fun findByUserId(userId: String): List<FoodLogEntity>

    @Query("SELECT * FROM food_logs WHERE user_id = :userId ORDER BY date_time DESC")
    fun observeByUserId(userId: String): Flow<List<FoodLogEntity>>

    @Query("DELETE FROM food_logs WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM food_logs WHERE id = :id)")
    suspend fun existsById(id: String): Boolean

    // Food Tags
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: FoodTagEntity)

    @Query("SELECT * FROM food_tags WHERE id = :id")
    suspend fun findTagById(id: String): FoodTagEntity?

    @Query("SELECT * FROM food_tags WHERE user_id = :userId ORDER BY name COLLATE NOCASE")
    suspend fun findTagsByUserId(userId: String): List<FoodTagEntity>

    @Query("SELECT * FROM food_tags WHERE name_lower = :nameLower AND user_id = :userId LIMIT 1")
    suspend fun findTagByNameLowerAndUserId(nameLower: String, userId: String): FoodTagEntity?

    @Query("DELETE FROM food_tags WHERE id = :id")
    suspend fun deleteTagById(id: String)

    @Query("DELETE FROM food_tags WHERE user_id = :userId AND (name IS NULL OR name = '')")
    suspend fun deleteEmptyTags(userId: String)

    // Join table
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun linkTagToFoodLog(ref: FoodLogTagCrossRef)

    @Query("DELETE FROM food_log_tags WHERE food_log_id = :foodLogId")
    suspend fun unlinkAllTagsFromFoodLog(foodLogId: String)

    @Query(
        """SELECT ft.* FROM food_tags ft
        INNER JOIN food_log_tags flt ON ft.id = flt.food_tag_id
        WHERE flt.food_log_id = :foodLogId"""
    )
    suspend fun findTagsByFoodLogId(foodLogId: String): List<FoodTagEntity>
}

@Dao
interface SportLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: SportLogEntity)

    @Query("SELECT * FROM sport_logs WHERE id = :id")
    suspend fun findById(id: String): SportLogEntity?

    @Query("SELECT * FROM sport_logs")
    suspend fun findAll(): List<SportLogEntity>

    @Query("SELECT * FROM sport_logs WHERE user_id = :userId ORDER BY date DESC, start_time DESC")
    suspend fun findByUserId(userId: String): List<SportLogEntity>

    @Query("SELECT * FROM sport_logs WHERE user_id = :userId ORDER BY date DESC, start_time DESC")
    fun observeByUserId(userId: String): Flow<List<SportLogEntity>>

    @Query("DELETE FROM sport_logs WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM sport_logs WHERE id = :id)")
    suspend fun existsById(id: String): Boolean

    @Query("SELECT DISTINCT name FROM sport_logs WHERE user_id = :userId ORDER BY name COLLATE NOCASE")
    suspend fun findDistinctNames(userId: String): List<String>

    @Query("SELECT DISTINCT measurement_unit FROM sport_logs WHERE user_id = :userId AND measurement_unit IS NOT NULL ORDER BY measurement_unit")
    suspend fun findDistinctMeasurementUnits(userId: String): List<String>
}

@Dao
interface MedicationDao {
    // Medications
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: MedicationEntity)

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun findById(id: String): MedicationEntity?

    @Query("SELECT * FROM medications")
    suspend fun findAll(): List<MedicationEntity>

    @Query("SELECT * FROM medications WHERE user_id = :userId ORDER BY name COLLATE NOCASE")
    suspend fun findByUserId(userId: String): List<MedicationEntity>

    @Query("SELECT * FROM medications WHERE user_id = :userId ORDER BY name COLLATE NOCASE")
    fun observeByUserId(userId: String): Flow<List<MedicationEntity>>

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteById(id: String)

    // Medication Logs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MedicationLogEntity)

    @Query("SELECT * FROM medication_logs WHERE id = :id")
    suspend fun findLogById(id: String): MedicationLogEntity?

    @Query("SELECT * FROM medication_logs")
    suspend fun findAllLogs(): List<MedicationLogEntity>

    @Query("SELECT * FROM medication_logs WHERE user_id = :userId ORDER BY taken_at DESC, created_at DESC")
    suspend fun findLogsByUserId(userId: String): List<MedicationLogEntity>

    @Query("SELECT * FROM medication_logs WHERE user_id = :userId ORDER BY taken_at DESC, created_at DESC")
    fun observeLogsByUserId(userId: String): Flow<List<MedicationLogEntity>>

    @Query("DELETE FROM medication_logs WHERE id = :id")
    suspend fun deleteLogById(id: String)

    @Query("SELECT * FROM medication_logs WHERE user_id = :userId ORDER BY taken_at DESC LIMIT :limit OFFSET :offset")
    suspend fun findLogsByUserIdPaged(userId: String, limit: Int, offset: Int): List<MedicationLogEntity>

    @Query("SELECT COUNT(*) FROM medication_logs WHERE user_id = :userId")
    suspend fun countLogsByUserId(userId: String): Int
}

@Dao
interface EmergencyPlanDao {
    // Steps
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStep(step: EmergencyPlanStepEntity)

    @Query("SELECT * FROM emergency_plan_steps WHERE id = :id")
    suspend fun findStepById(id: String): EmergencyPlanStepEntity?

    @Query("SELECT * FROM emergency_plan_steps ORDER BY step_order")
    suspend fun findAllSteps(): List<EmergencyPlanStepEntity>

    @Query("SELECT * FROM emergency_plan_steps WHERE user_id = :userId ORDER BY step_order")
    suspend fun findStepsByUserId(userId: String): List<EmergencyPlanStepEntity>

    @Query("SELECT * FROM emergency_plan_steps WHERE user_id = :userId ORDER BY step_order")
    fun observeStepsByUserId(userId: String): Flow<List<EmergencyPlanStepEntity>>

    @Query("DELETE FROM emergency_plan_steps WHERE id = :id")
    suspend fun deleteStepById(id: String)

    // Actions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: EmergencyPlanActionEntity)

    @Query("SELECT * FROM emergency_plan_actions WHERE id = :id")
    suspend fun findActionById(id: String): EmergencyPlanActionEntity?

    @Query("SELECT * FROM emergency_plan_actions WHERE step_id = :stepId ORDER BY action_order")
    suspend fun findActionsByStepId(stepId: String): List<EmergencyPlanActionEntity>

    @Query("DELETE FROM emergency_plan_actions WHERE id = :id")
    suspend fun deleteActionById(id: String)

    @Query("DELETE FROM emergency_plan_actions WHERE step_id = :stepId")
    suspend fun deleteActionsByStepId(stepId: String)

    @Transaction
    suspend fun saveStepWithActions(step: EmergencyPlanStepEntity, actions: List<EmergencyPlanActionEntity>) {
        insertStep(step)
        deleteActionsByStepId(step.id)
        actions.forEach { insertAction(it) }
    }

    @Transaction
    suspend fun deleteStepWithActions(id: String) {
        deleteActionsByStepId(id)
        deleteStepById(id)
    }
}

@Dao
interface ActivityLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ActivityLogEntity)

    @Query("SELECT * FROM activity_logs WHERE id = :id")
    suspend fun findById(id: String): ActivityLogEntity?

    @Query("SELECT * FROM activity_logs")
    suspend fun findAll(): List<ActivityLogEntity>

    @Query("SELECT * FROM activity_logs WHERE user_id = :userId ORDER BY date DESC, start_time DESC")
    suspend fun findByUserId(userId: String): List<ActivityLogEntity>

    @Query("SELECT * FROM activity_logs WHERE user_id = :userId ORDER BY date DESC, start_time DESC")
    fun observeByUserId(userId: String): Flow<List<ActivityLogEntity>>

    @Query("DELETE FROM activity_logs WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM activity_logs WHERE id = :id)")
    suspend fun existsById(id: String): Boolean

    @Query("SELECT DISTINCT location FROM activity_logs WHERE user_id = :userId ORDER BY location COLLATE NOCASE")
    suspend fun findDistinctLocations(userId: String): List<String>

    @Query("SELECT DISTINCT activity FROM activity_logs WHERE user_id = :userId AND activity IS NOT NULL ORDER BY activity COLLATE NOCASE")
    suspend fun findDistinctActivities(userId: String): List<String>

    // Activity Groups
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: ActivityGroupEntity)

    @Query("SELECT * FROM activity_groups WHERE id = :id")
    suspend fun findGroupById(id: String): ActivityGroupEntity?

    @Query("SELECT * FROM activity_groups")
    suspend fun findAllGroups(): List<ActivityGroupEntity>

    @Query("SELECT * FROM activity_groups WHERE user_id = :userId ORDER BY name COLLATE NOCASE")
    suspend fun findGroupsByUserId(userId: String): List<ActivityGroupEntity>

    @Query("SELECT * FROM activity_groups WHERE user_id = :userId ORDER BY name COLLATE NOCASE")
    fun observeGroupsByUserId(userId: String): Flow<List<ActivityGroupEntity>>

    @Query("DELETE FROM activity_groups WHERE id = :id")
    suspend fun deleteGroupById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM activity_groups WHERE id = :id)")
    suspend fun groupExistsById(id: String): Boolean

    // Activity Log - Group Links
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLogGroupLink(link: ActivityLogGroupLinkEntity)

    @Query("DELETE FROM activity_log_group_links WHERE activity_log_id = :activityLogId")
    suspend fun deleteLinksForActivityLog(activityLogId: String)

    @Query("DELETE FROM activity_log_group_links WHERE activity_group_id = :groupId")
    suspend fun deleteLinksForGroup(groupId: String)

    @Query("SELECT activity_group_id FROM activity_log_group_links WHERE activity_log_id = :activityLogId")
    suspend fun findGroupIdsForActivityLog(activityLogId: String): List<String>

    @Query(
        """SELECT ag.* FROM activity_groups ag
        INNER JOIN activity_log_group_links algl ON ag.id = algl.activity_group_id
        WHERE algl.activity_log_id = :activityLogId ORDER BY ag.name COLLATE NOCASE"""
    )
    suspend fun findGroupsByActivityLogId(activityLogId: String): List<ActivityGroupEntity>

    @Transaction
    suspend fun saveActivityLogWithLinks(entry: ActivityLogEntity, groupIds: List<String>) {
        insert(entry)
        deleteLinksForActivityLog(entry.id)
        groupIds.forEach { insertLogGroupLink(ActivityLogGroupLinkEntity(entry.id, it)) }
    }

    @Transaction
    suspend fun deleteActivityLogWithLinks(id: String) {
        deleteLinksForActivityLog(id)
        deleteById(id)
    }

    @Transaction
    suspend fun deleteGroupWithLinks(id: String) {
        deleteLinksForGroup(id)
        deleteGroupById(id)
    }
}

@Dao
interface DayPlannerDao {
    // Planner Activities
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: PlannerActivityEntity)

    @Query("SELECT * FROM planner_activities WHERE id = :id")
    suspend fun findActivityById(id: String): PlannerActivityEntity?

    @Query("SELECT * FROM planner_activities")
    suspend fun findAllActivities(): List<PlannerActivityEntity>

    @Query("SELECT * FROM planner_activities WHERE user_id = :userId ORDER BY name COLLATE NOCASE")
    suspend fun findActivitiesByUserId(userId: String): List<PlannerActivityEntity>

    @Query("SELECT * FROM planner_activities WHERE user_id = :userId ORDER BY name COLLATE NOCASE")
    fun observeActivitiesByUserId(userId: String): Flow<List<PlannerActivityEntity>>

    @Query("DELETE FROM planner_activities WHERE id = :id")
    suspend fun deleteActivityById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM planner_activities WHERE id = :id)")
    suspend fun activityExistsById(id: String): Boolean

    // Activity-Group Links
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertActivityGroupLink(link: PlannerActivityGroupLinkEntity)

    @Query("DELETE FROM planner_activity_group_links WHERE activity_id = :activityId")
    suspend fun deleteLinksForActivity(activityId: String)

    @Query("DELETE FROM planner_activity_group_links WHERE group_id = :groupId")
    suspend fun deleteLinksForGroup(groupId: String)

    @Query("SELECT group_id FROM planner_activity_group_links WHERE activity_id = :activityId")
    suspend fun findGroupIdsForActivity(activityId: String): List<String>

    @Query(
        """SELECT pa.* FROM planner_activities pa
        INNER JOIN planner_activity_group_links pagl ON pa.id = pagl.activity_id
        WHERE pagl.group_id = :groupId ORDER BY pa.name COLLATE NOCASE"""
    )
    suspend fun findActivitiesByGroupId(groupId: String): List<PlannerActivityEntity>

    @Transaction
    suspend fun saveActivityWithLinks(activity: PlannerActivityEntity, groupIds: List<String>) {
        insertActivity(activity)
        deleteLinksForActivity(activity.id)
        groupIds.forEach { insertActivityGroupLink(PlannerActivityGroupLinkEntity(activity.id, it)) }
    }

    @Transaction
    suspend fun deleteActivityWithLinks(id: String) {
        deleteLinksForActivity(id)
        deleteActivityById(id)
    }

    // Planner Groups
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: PlannerGroupEntity)

    @Query("SELECT * FROM planner_groups WHERE id = :id")
    suspend fun findGroupById(id: String): PlannerGroupEntity?

    @Query("SELECT * FROM planner_groups")
    suspend fun findAllGroups(): List<PlannerGroupEntity>

    @Query("SELECT * FROM planner_groups WHERE user_id = :userId ORDER BY name COLLATE NOCASE")
    suspend fun findGroupsByUserId(userId: String): List<PlannerGroupEntity>

    @Query("SELECT * FROM planner_groups WHERE user_id = :userId ORDER BY name COLLATE NOCASE")
    fun observeGroupsByUserId(userId: String): Flow<List<PlannerGroupEntity>>

    @Query("DELETE FROM planner_groups WHERE id = :id")
    suspend fun deleteGroupById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM planner_groups WHERE id = :id)")
    suspend fun groupExistsById(id: String): Boolean

    @Transaction
    suspend fun deleteGroupWithLinks(id: String) {
        deleteLinksForGroup(id)
        deleteGroupById(id)
    }

    // Week Planner Slots
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: WeekPlannerSlotEntity)

    @Query("SELECT * FROM week_planner_slots WHERE id = :id")
    suspend fun findSlotById(id: String): WeekPlannerSlotEntity?

    @Query("SELECT * FROM week_planner_slots")
    suspend fun findAllSlots(): List<WeekPlannerSlotEntity>

    @Query("SELECT * FROM week_planner_slots WHERE user_id = :userId ORDER BY day_of_week, hour")
    suspend fun findSlotsByUserId(userId: String): List<WeekPlannerSlotEntity>

    @Query("SELECT * FROM week_planner_slots WHERE user_id = :userId ORDER BY day_of_week, hour")
    fun observeSlotsByUserId(userId: String): Flow<List<WeekPlannerSlotEntity>>

    @Query("SELECT * FROM week_planner_slots WHERE user_id = :userId AND day_of_week = :dayOfWeek ORDER BY hour")
    suspend fun findSlotsByUserIdAndDayOfWeek(userId: String, dayOfWeek: Int): List<WeekPlannerSlotEntity>

    @Query("DELETE FROM week_planner_slots WHERE id = :id")
    suspend fun deleteSlotById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM week_planner_slots WHERE id = :id)")
    suspend fun slotExistsById(id: String): Boolean

    // Slot Confirmations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfirmation(confirmation: SlotConfirmationEntity)

    @Query("SELECT * FROM slot_confirmations WHERE id = :id")
    suspend fun findConfirmationById(id: String): SlotConfirmationEntity?

    @Query("SELECT * FROM slot_confirmations")
    suspend fun findAllConfirmations(): List<SlotConfirmationEntity>

    @Query("SELECT * FROM slot_confirmations WHERE user_id = :userId ORDER BY date DESC, created_at DESC")
    suspend fun findConfirmationsByUserId(userId: String): List<SlotConfirmationEntity>

    @Query("DELETE FROM slot_confirmations WHERE id = :id")
    suspend fun deleteConfirmationById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM slot_confirmations WHERE id = :id)")
    suspend fun confirmationExistsById(id: String): Boolean
}
