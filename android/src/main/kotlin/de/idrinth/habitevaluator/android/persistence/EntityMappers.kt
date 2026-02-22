package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.ActivityLog
import de.idrinth.habitevaluator.shared.model.DiaryEntry
import de.idrinth.habitevaluator.shared.model.DiaryReference
import de.idrinth.habitevaluator.shared.model.EmergencyPlanAction
import de.idrinth.habitevaluator.shared.model.EmergencyPlanStep
import de.idrinth.habitevaluator.shared.model.EmotionEntry
import de.idrinth.habitevaluator.shared.model.EmotionPair
import de.idrinth.habitevaluator.shared.model.EventSignificance
import de.idrinth.habitevaluator.shared.model.FoodLog
import de.idrinth.habitevaluator.shared.model.FoodTag
import de.idrinth.habitevaluator.shared.model.FrequencyType
import de.idrinth.habitevaluator.shared.model.Habit
import de.idrinth.habitevaluator.shared.model.HabitCategory
import de.idrinth.habitevaluator.shared.model.HabitEntry
import de.idrinth.habitevaluator.shared.model.Medication
import de.idrinth.habitevaluator.shared.model.MedicationLog
import de.idrinth.habitevaluator.shared.model.MedicationProvisionType
import de.idrinth.habitevaluator.shared.model.SleepEntry
import de.idrinth.habitevaluator.shared.model.SportLog
import de.idrinth.habitevaluator.shared.model.User
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val DT_FORMAT: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

// ── Habit ──

fun Habit.toEntity(): HabitEntity = HabitEntity(
    id = id,
    name = name,
    description = description,
    categoryId = categoryId,
    frequencyType = frequencyType?.name ?: FrequencyType.DAILY.name,
    targetFrequency = targetFrequency,
    maxEntriesPerDay = maxEntriesPerDay,
    positiveScoring = if (isPositiveScoring) 1 else 0,
    createdAt = createdAt?.format(DT_FORMAT),
    scoringRuleId = scoringRule?.id,
    scoringRuleName = scoringRule?.name,
    userId = user?.id ?: "",
    userName = user?.username ?: ""
)

fun Habit.toEntryEntities(): List<HabitEntryEntity> = entries.map { it.toEntity(id) }

fun HabitEntry.toEntity(habitId: String) = HabitEntryEntity(
    id = id,
    habitId = habitId,
    completedAt = completedAt.format(DT_FORMAT),
    notes = notes,
    value = value.toDouble()
)

fun Habit.toNameTranslations(): List<HabitNameTranslationEntity> =
    nameTranslations?.map { (lang, name) ->
        HabitNameTranslationEntity(id, lang, name)
    } ?: emptyList()

fun Habit.toDescTranslations(): List<HabitDescriptionTranslationEntity> =
    descriptionTranslations?.map { (lang, desc) ->
        HabitDescriptionTranslationEntity(id, lang, desc)
    } ?: emptyList()

fun HabitEntity.toModel(
    entries: List<HabitEntryEntity>,
    nameTranslations: List<HabitNameTranslationEntity>,
    descTranslations: List<HabitDescriptionTranslationEntity>
): Habit {
    val habit = Habit()
    habit.id = id
    habit.name = name
    habit.description = description
    habit.categoryId = categoryId
    habit.frequencyType = try { FrequencyType.valueOf(frequencyType) } catch (_: Exception) { FrequencyType.DAILY }
    habit.targetFrequency = targetFrequency
    habit.maxEntriesPerDay = maxEntriesPerDay
    habit.isPositiveScoring = positiveScoring == 1
    createdAt?.let { habit.createdAt = LocalDateTime.parse(it, DT_FORMAT) }
    val user = User()
    user.id = userId
    user.username = userName
    habit.user = user
    entries.forEach { e ->
        val entry = HabitEntry(id)
        entry.id = e.id
        entry.completedAt = LocalDateTime.parse(e.completedAt, DT_FORMAT)
        entry.notes = e.notes
        entry.value = e.value.toInt()
        habit.addEntry(entry)
    }
    if (nameTranslations.isNotEmpty()) {
        val map = HashMap<String, String>()
        nameTranslations.forEach { map[it.language] = it.translatedName }
        habit.nameTranslations = map
    }
    if (descTranslations.isNotEmpty()) {
        val map = HashMap<String, String>()
        descTranslations.forEach { map[it.language] = it.translatedDescription }
        habit.descriptionTranslations = map
    }
    return habit
}

// ── HabitCategory ──

fun HabitCategory.toEntity(): HabitCategoryEntity = HabitCategoryEntity(
    id = id,
    name = name,
    description = description,
    color = color,
    userId = user?.id ?: "",
    userName = user?.username ?: ""
)

fun HabitCategory.toNameTranslations(): List<CategoryNameTranslationEntity> =
    nameTranslations?.map { (lang, n) ->
        CategoryNameTranslationEntity(id, lang, n)
    } ?: emptyList()

fun HabitCategory.toDescTranslations(): List<CategoryDescriptionTranslationEntity> =
    descriptionTranslations?.map { (lang, d) ->
        CategoryDescriptionTranslationEntity(id, lang, d)
    } ?: emptyList()

fun HabitCategoryEntity.toModel(
    nameTranslations: List<CategoryNameTranslationEntity>,
    descTranslations: List<CategoryDescriptionTranslationEntity>
): HabitCategory {
    val cat = HabitCategory()
    cat.id = id
    cat.name = name
    cat.description = description
    cat.color = color
    val user = User()
    user.id = userId
    user.username = userName
    cat.user = user
    if (nameTranslations.isNotEmpty()) {
        val map = HashMap<String, String>()
        nameTranslations.forEach { map[it.language] = it.translatedName }
        cat.nameTranslations = map
    }
    if (descTranslations.isNotEmpty()) {
        val map = HashMap<String, String>()
        descTranslations.forEach { map[it.language] = it.translatedDescription }
        cat.descriptionTranslations = map
    }
    return cat
}

// ── DiaryReference ──

fun DiaryReference.toEntity(): DiaryReferenceEntity = DiaryReferenceEntity(
    id = id,
    description = description,
    descriptionLower = description?.lowercase() ?: "",
    userId = user?.id ?: "",
    userName = user?.username ?: ""
)

fun DiaryReferenceEntity.toModel(): DiaryReference {
    val ref = DiaryReference()
    ref.id = id
    ref.description = description
    val u = User()
    u.id = userId
    u.username = userName
    ref.user = u
    return ref
}

// ── DiaryEntry ──

fun DiaryEntry.toEntity(): DiaryEntryEntity = DiaryEntryEntity(
    id = id,
    legacyDescription = null,
    diaryReferenceId = diaryReference?.id,
    significance = significance?.name ?: EventSignificance.NORMAL.name,
    eventDate = eventDate?.format(DATE_FORMAT) ?: LocalDate.now().format(DATE_FORMAT),
    startTime = startTime?.format(TIME_FORMAT),
    endTime = endTime?.format(TIME_FORMAT),
    createdAt = createdAt?.format(DT_FORMAT) ?: LocalDateTime.now().format(DT_FORMAT),
    userId = user?.id ?: "",
    userName = user?.username ?: ""
)

fun DiaryEntryEntity.toModel(reference: DiaryReference?): DiaryEntry {
    val entry = DiaryEntry()
    entry.id = id
    entry.significance = try { EventSignificance.valueOf(significance) } catch (_: Exception) { EventSignificance.NORMAL }
    entry.eventDate = LocalDate.parse(eventDate, DATE_FORMAT)
    startTime?.let { entry.startTime = LocalTime.parse(it, TIME_FORMAT) }
    endTime?.let { entry.endTime = LocalTime.parse(it, TIME_FORMAT) }
    entry.createdAt = LocalDateTime.parse(createdAt, DT_FORMAT)
    entry.diaryReference = reference
    val u = User()
    u.id = userId
    u.username = userName
    entry.user = u
    return entry
}

// ── SleepEntry ──

fun SleepEntry.toEntity(): SleepEntryEntity = SleepEntryEntity(
    id = id,
    fromTime = fromTime?.format(TIME_FORMAT) ?: "",
    untilTime = untilTime?.format(TIME_FORMAT) ?: "",
    date = date?.format(DATE_FORMAT) ?: LocalDate.now().format(DATE_FORMAT),
    createdAt = createdAt?.format(DT_FORMAT) ?: LocalDateTime.now().format(DT_FORMAT),
    notes = notes,
    userId = user?.id ?: "",
    userName = user?.username ?: ""
)

fun SleepEntryEntity.toModel(): SleepEntry {
    val entry = SleepEntry()
    entry.id = id
    entry.fromTime = LocalTime.parse(fromTime, TIME_FORMAT)
    entry.untilTime = LocalTime.parse(untilTime, TIME_FORMAT)
    entry.date = LocalDate.parse(date, DATE_FORMAT)
    entry.createdAt = LocalDateTime.parse(createdAt, DT_FORMAT)
    entry.notes = notes
    val u = User()
    u.id = userId
    u.username = userName
    entry.user = u
    return entry
}

// ── EmotionPair ──

fun EmotionPair.toEntity(): EmotionPairEntity = EmotionPairEntity(
    id = id,
    negativeLabel = negativeLabel,
    positiveLabel = positiveLabel,
    userId = user?.id ?: "",
    userName = user?.username ?: ""
)

fun EmotionPairEntity.toModel(): EmotionPair {
    val pair = EmotionPair()
    pair.id = id
    pair.negativeLabel = negativeLabel
    pair.positiveLabel = positiveLabel
    val u = User()
    u.id = userId
    u.username = userName
    pair.user = u
    return pair
}

// ── EmotionEntry ──

fun EmotionEntry.toEntity(): EmotionEntryEntity = EmotionEntryEntity(
    id = id,
    emotionPairId = emotionPair?.id ?: "",
    strength = strength,
    recordedAt = recordedAt?.format(DT_FORMAT) ?: LocalDateTime.now().format(DT_FORMAT),
    notes = notes,
    userId = user?.id ?: "",
    userName = user?.username ?: ""
)

fun EmotionEntryEntity.toModel(pair: EmotionPair?): EmotionEntry? {
    if (pair == null) return null
    val entry = EmotionEntry()
    entry.id = id
    entry.emotionPair = pair
    entry.strength = strength
    entry.recordedAt = LocalDateTime.parse(recordedAt, DT_FORMAT)
    entry.notes = notes
    val u = User()
    u.id = userId
    u.username = userName
    entry.user = u
    return entry
}

// ── SportLog ──

fun SportLog.toEntity(): SportLogEntity = SportLogEntity(
    id = id,
    name = name,
    measurement = measurement,
    measurementUnit = measurementUnit,
    startTime = startTime?.format(TIME_FORMAT),
    endTime = endTime?.format(TIME_FORMAT),
    date = date?.format(DATE_FORMAT) ?: LocalDate.now().format(DATE_FORMAT),
    createdAt = createdAt?.format(DT_FORMAT) ?: LocalDateTime.now().format(DT_FORMAT),
    notes = notes,
    userId = user?.id ?: "",
    userName = user?.username ?: ""
)

fun SportLogEntity.toModel(): SportLog {
    val log = SportLog()
    log.id = id
    log.name = name
    log.measurement = measurement ?: 0.0
    log.measurementUnit = measurementUnit
    startTime?.let { log.startTime = LocalTime.parse(it, TIME_FORMAT) }
    endTime?.let { log.endTime = LocalTime.parse(it, TIME_FORMAT) }
    log.date = LocalDate.parse(date, DATE_FORMAT)
    log.createdAt = LocalDateTime.parse(createdAt, DT_FORMAT)
    log.notes = notes
    val u = User()
    u.id = userId
    u.username = userName
    log.user = u
    return log
}

// ── FoodLog ──

fun FoodLog.toEntity(): FoodLogEntity = FoodLogEntity(
    id = id,
    carbohydrates = carbohydrates,
    kcal = kcal,
    dateTime = dateTime?.format(DT_FORMAT) ?: LocalDateTime.now().format(DT_FORMAT),
    foodItems = foodItems,
    createdAt = createdAt?.format(DT_FORMAT) ?: LocalDateTime.now().format(DT_FORMAT),
    notes = notes,
    userId = user?.id ?: "",
    userName = user?.username ?: ""
)

fun FoodLogEntity.toModel(): FoodLog {
    val log = FoodLog()
    log.id = id
    log.carbohydrates = carbohydrates
    log.kcal = kcal
    log.dateTime = LocalDateTime.parse(dateTime, DT_FORMAT)
    log.foodItems = foodItems
    log.createdAt = LocalDateTime.parse(createdAt, DT_FORMAT)
    log.notes = notes
    val u = User()
    u.id = userId
    u.username = userName
    log.user = u
    return log
}

// ── FoodTag ──

fun FoodTag.toEntity(): FoodTagEntity = FoodTagEntity(
    id = id,
    name = name,
    nameLower = name?.lowercase() ?: "",
    userId = user?.id ?: "",
    userName = user?.username ?: ""
)

fun FoodTagEntity.toModel(): FoodTag {
    val tag = FoodTag()
    tag.id = id
    tag.name = name
    val u = User()
    u.id = userId
    u.username = userName
    tag.user = u
    return tag
}

// ── Medication ──

fun Medication.toEntity(): MedicationEntity = MedicationEntity(
    id = id,
    name = name,
    wikipediaLink = wikipediaLink,
    provisionType = provisionType?.name ?: MedicationProvisionType.PILL.name,
    userId = user?.id ?: "",
    userName = user?.username ?: ""
)

fun MedicationEntity.toModel(): Medication {
    val med = Medication()
    med.id = id
    med.name = name
    med.wikipediaLink = wikipediaLink
    med.provisionType = try { MedicationProvisionType.valueOf(provisionType) } catch (_: Exception) { MedicationProvisionType.PILL }
    val u = User()
    u.id = userId
    u.username = userName
    med.user = u
    return med
}

// ── MedicationLog ──

fun MedicationLog.toEntity(): MedicationLogEntity = MedicationLogEntity(
    id = id,
    medicationId = medication?.id ?: "",
    amount = amount,
    takenAt = takenAt?.format(DT_FORMAT) ?: LocalDateTime.now().format(DT_FORMAT),
    createdAt = createdAt?.format(DT_FORMAT) ?: LocalDateTime.now().format(DT_FORMAT),
    notes = notes,
    userId = user?.id ?: "",
    userName = user?.username ?: ""
)

fun MedicationLogEntity.toModel(medication: Medication?): MedicationLog {
    val log = MedicationLog()
    log.id = id
    log.medication = medication
    log.amount = amount
    log.takenAt = LocalDateTime.parse(takenAt, DT_FORMAT)
    log.createdAt = LocalDateTime.parse(createdAt, DT_FORMAT)
    log.notes = notes
    val u = User()
    u.id = userId
    u.username = userName
    log.user = u
    return log
}

// ── EmergencyPlanStep ──

fun EmergencyPlanStep.toEntity(): EmergencyPlanStepEntity = EmergencyPlanStepEntity(
    id = id,
    question = question,
    stepOrder = stepOrder,
    userId = user?.id ?: "",
    userName = user?.username ?: ""
)

fun EmergencyPlanStepEntity.toModel(actions: List<EmergencyPlanAction> = emptyList()): EmergencyPlanStep {
    val step = EmergencyPlanStep()
    step.id = id
    step.question = question
    step.stepOrder = stepOrder
    step.actions = actions
    val u = User()
    u.id = userId
    u.username = userName
    step.user = u
    return step
}

// ── EmergencyPlanAction ──

fun EmergencyPlanAction.toEntity(): EmergencyPlanActionEntity = EmergencyPlanActionEntity(
    id = id,
    actionText = actionText,
    phoneNumber = phoneNumber,
    actionOrder = actionOrder,
    stepId = step?.id ?: ""
)

fun EmergencyPlanActionEntity.toModel(step: EmergencyPlanStep? = null): EmergencyPlanAction {
    val action = EmergencyPlanAction()
    action.id = id
    action.actionText = actionText
    action.phoneNumber = phoneNumber
    action.actionOrder = actionOrder
    action.step = step
    return action
}

// ── ActivityLog ──

fun ActivityLog.toEntity(): ActivityLogEntity = ActivityLogEntity(
    id = id,
    persons = persons ?: "",
    location = location ?: "",
    startTime = startTime?.format(TIME_FORMAT) ?: "",
    endTime = endTime?.format(TIME_FORMAT) ?: "",
    date = date?.format(DATE_FORMAT) ?: LocalDate.now().format(DATE_FORMAT),
    activity = activity,
    createdAt = createdAt?.format(DT_FORMAT) ?: LocalDateTime.now().format(DT_FORMAT),
    userId = user?.id ?: "",
    userName = user?.username ?: ""
)

fun ActivityLogEntity.toModel(): ActivityLog {
    val log = ActivityLog()
    log.id = id
    log.persons = persons
    log.location = location
    log.startTime = LocalTime.parse(startTime, TIME_FORMAT)
    log.endTime = LocalTime.parse(endTime, TIME_FORMAT)
    log.date = LocalDate.parse(date, DATE_FORMAT)
    log.activity = activity
    log.createdAt = LocalDateTime.parse(createdAt, DT_FORMAT)
    val u = User()
    u.id = userId
    u.username = userName
    log.user = u
    return log
}
