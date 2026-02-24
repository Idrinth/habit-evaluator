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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class EntityMappersTest {

    private fun createUser(id: String = "u1", username: String = "testuser"): User {
        val user = User()
        user.id = id
        user.username = username
        return user
    }

    // ── Habit toEntity ──

    @Test
    fun testHabitToEntityMapsBasicFields() {
        val habit = Habit("Exercise", "Daily workout")
        habit.id = "h1"
        habit.user = createUser()
        habit.frequencyType = FrequencyType.DAILY
        habit.targetFrequency = 2
        habit.maxEntriesPerDay = 3
        habit.isPositiveScoring = true

        val entity = habit.toEntity()

        assertEquals("h1", entity.id)
        assertEquals("Exercise", entity.name)
        assertEquals("Daily workout", entity.description)
        assertEquals("DAILY", entity.frequencyType)
        assertEquals(2, entity.targetFrequency)
        assertEquals(3, entity.maxEntriesPerDay)
        assertEquals(1, entity.positiveScoring)
        assertEquals("u1", entity.userId)
        assertEquals("testuser", entity.userName)
    }

    @Test
    fun testHabitToEntityNegativeScoring() {
        val habit = Habit("Smoking", "Avoid")
        habit.user = createUser()
        habit.isPositiveScoring = false

        val entity = habit.toEntity()

        assertEquals(0, entity.positiveScoring)
    }

    @Test
    fun testHabitToEntityNullCategoryId() {
        val habit = Habit("Test", "")
        habit.user = createUser()

        val entity = habit.toEntity()

        assertNull(entity.categoryId)
    }

    @Test
    fun testHabitToEntityWithCategoryId() {
        val habit = Habit("Test", "")
        habit.user = createUser()
        habit.categoryId = "cat1"

        val entity = habit.toEntity()

        assertEquals("cat1", entity.categoryId)
    }

    @Test
    fun testHabitToEntityNullUser() {
        val habit = Habit("Test", "")

        val entity = habit.toEntity()

        assertEquals("", entity.userId)
        assertEquals("", entity.userName)
    }

    @Test
    fun testHabitToEntityDefaultFrequencyWhenNull() {
        val habit = Habit("Test", "")
        habit.user = createUser()
        habit.frequencyType = null

        val entity = habit.toEntity()

        assertEquals("DAILY", entity.frequencyType)
    }

    @Test
    fun testHabitToEntityWeeklyFrequency() {
        val habit = Habit("Test", "")
        habit.user = createUser()
        habit.frequencyType = FrequencyType.WEEKLY

        val entity = habit.toEntity()

        assertEquals("WEEKLY", entity.frequencyType)
    }

    @Test
    fun testHabitToEntityMonthlyFrequency() {
        val habit = Habit("Test", "")
        habit.user = createUser()
        habit.frequencyType = FrequencyType.MONTHLY

        val entity = habit.toEntity()

        assertEquals("MONTHLY", entity.frequencyType)
    }

    // ── HabitEntry toEntity ──

    @Test
    fun testHabitEntryToEntity() {
        val entry = HabitEntry("h1")
        entry.id = "e1"
        val now = LocalDateTime.of(2024, 6, 15, 10, 30, 0)
        entry.completedAt = now
        entry.notes = "Test note"
        entry.value = 3

        val entity = entry.toEntity("h1")

        assertEquals("e1", entity.id)
        assertEquals("h1", entity.habitId)
        assertEquals("2024-06-15T10:30:00", entity.completedAt)
        assertEquals("Test note", entity.notes)
        assertEquals(3.0, entity.value, 0.001)
    }

    @Test
    fun testHabitToEntryEntities() {
        val habit = Habit("Test", "")
        habit.id = "h1"
        habit.user = createUser()
        val entry1 = HabitEntry("h1")
        entry1.completedAt = LocalDateTime.of(2024, 1, 1, 10, 0, 0)
        val entry2 = HabitEntry("h1")
        entry2.completedAt = LocalDateTime.of(2024, 1, 2, 10, 0, 0)
        habit.addEntry(entry1)
        habit.addEntry(entry2)

        val entities = habit.toEntryEntities()

        assertEquals(2, entities.size)
    }

    // ── Habit toModel ──

    @Test
    fun testHabitEntityToModel() {
        val entity = HabitEntity(
            id = "h1", name = "Running", description = "Morning run",
            categoryId = "cat1", frequencyType = "WEEKLY", targetFrequency = 3,
            maxEntriesPerDay = 2, positiveScoring = 1, createdAt = "2024-01-15T10:00:00",
            scoringRuleId = "sr1", scoringRuleName = "Default",
            userId = "u1", userName = "testuser"
        )

        val habit = entity.toModel(emptyList(), emptyList(), emptyList())

        assertEquals("h1", habit.id)
        assertEquals("Running", habit.name)
        assertEquals("Morning run", habit.description)
        assertEquals("cat1", habit.categoryId)
        assertEquals(FrequencyType.WEEKLY, habit.frequencyType)
        assertEquals(3, habit.targetFrequency)
        assertEquals(2, habit.maxEntriesPerDay)
        assertTrue(habit.isPositiveScoring)
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0, 0), habit.createdAt)
        assertEquals("u1", habit.user.id)
        assertEquals("testuser", habit.user.username)
    }

    @Test
    fun testHabitEntityToModelNegativeScoring() {
        val entity = HabitEntity(
            id = "h1", name = "Test", description = null,
            categoryId = null, frequencyType = "DAILY", targetFrequency = 1,
            maxEntriesPerDay = 1, positiveScoring = 0, createdAt = null,
            scoringRuleId = null, scoringRuleName = null,
            userId = "u1", userName = "testuser"
        )

        val habit = entity.toModel(emptyList(), emptyList(), emptyList())

        assertFalse(habit.isPositiveScoring)
    }

    @Test
    fun testHabitEntityToModelInvalidFrequencyDefaultsToDaily() {
        val entity = HabitEntity(
            id = "h1", name = "Test", description = null,
            categoryId = null, frequencyType = "INVALID", targetFrequency = 1,
            maxEntriesPerDay = 1, positiveScoring = 1, createdAt = null,
            scoringRuleId = null, scoringRuleName = null,
            userId = "u1", userName = "testuser"
        )

        val habit = entity.toModel(emptyList(), emptyList(), emptyList())

        assertEquals(FrequencyType.DAILY, habit.frequencyType)
    }

    @Test
    fun testHabitEntityToModelNullCreatedAt() {
        val entity = HabitEntity(
            id = "h1", name = "Test", description = null,
            categoryId = null, frequencyType = "DAILY", targetFrequency = 1,
            maxEntriesPerDay = 1, positiveScoring = 1, createdAt = null,
            scoringRuleId = null, scoringRuleName = null,
            userId = "u1", userName = "testuser"
        )

        val habit = entity.toModel(emptyList(), emptyList(), emptyList())

        assertNotNull(habit)
    }

    @Test
    fun testHabitEntityToModelWithEntries() {
        val entity = HabitEntity(
            id = "h1", name = "Test", description = null,
            categoryId = null, frequencyType = "DAILY", targetFrequency = 1,
            maxEntriesPerDay = 1, positiveScoring = 1, createdAt = null,
            scoringRuleId = null, scoringRuleName = null,
            userId = "u1", userName = "testuser"
        )
        val entryEntities = listOf(
            HabitEntryEntity("e1", "h1", "2024-06-15T10:00:00", "note", 1.0),
            HabitEntryEntity("e2", "h1", "2024-06-16T10:00:00", null, 2.0)
        )

        val habit = entity.toModel(entryEntities, emptyList(), emptyList())

        assertEquals(2, habit.entries.size)
        assertEquals("e1", habit.entries[0].id)
        assertEquals("note", habit.entries[0].notes)
        assertEquals(1, habit.entries[0].value)
        assertEquals("e2", habit.entries[1].id)
        assertEquals(2, habit.entries[1].value)
    }

    @Test
    fun testHabitEntityToModelWithNameTranslations() {
        val entity = HabitEntity(
            id = "h1", name = "Exercise", description = null,
            categoryId = null, frequencyType = "DAILY", targetFrequency = 1,
            maxEntriesPerDay = 1, positiveScoring = 1, createdAt = null,
            scoringRuleId = null, scoringRuleName = null,
            userId = "u1", userName = "testuser"
        )
        val nameTranslations = listOf(
            HabitNameTranslationEntity("h1", "de", "Ubung"),
            HabitNameTranslationEntity("h1", "fr", "Exercice")
        )

        val habit = entity.toModel(emptyList(), nameTranslations, emptyList())

        assertNotNull(habit.nameTranslations)
        assertEquals("Ubung", habit.nameTranslations["de"])
        assertEquals("Exercice", habit.nameTranslations["fr"])
    }

    @Test
    fun testHabitEntityToModelWithDescTranslations() {
        val entity = HabitEntity(
            id = "h1", name = "Exercise", description = "Desc",
            categoryId = null, frequencyType = "DAILY", targetFrequency = 1,
            maxEntriesPerDay = 1, positiveScoring = 1, createdAt = null,
            scoringRuleId = null, scoringRuleName = null,
            userId = "u1", userName = "testuser"
        )
        val descTranslations = listOf(
            HabitDescriptionTranslationEntity("h1", "de", "Beschreibung")
        )

        val habit = entity.toModel(emptyList(), emptyList(), descTranslations)

        assertNotNull(habit.descriptionTranslations)
        assertEquals("Beschreibung", habit.descriptionTranslations["de"])
    }

    @Test
    fun testHabitEntityToModelEmptyTranslationsAreNotPopulated() {
        val entity = HabitEntity(
            id = "h1", name = "Exercise", description = null,
            categoryId = null, frequencyType = "DAILY", targetFrequency = 1,
            maxEntriesPerDay = 1, positiveScoring = 1, createdAt = null,
            scoringRuleId = null, scoringRuleName = null,
            userId = "u1", userName = "testuser"
        )

        val habit = entity.toModel(emptyList(), emptyList(), emptyList())

        // Empty translation lists should not add any translation entries
        val nameTranslations = habit.nameTranslations
        val descTranslations = habit.descriptionTranslations
        assertTrue(nameTranslations == null || nameTranslations.isEmpty())
        assertTrue(descTranslations == null || descTranslations.isEmpty())
    }

    // ── Habit Name/Desc Translations ──

    @Test
    fun testHabitToNameTranslations() {
        val habit = Habit("Test", "")
        habit.id = "h1"
        habit.nameTranslations = hashMapOf("de" to "Test-DE", "fr" to "Test-FR")

        val translations = habit.toNameTranslations()

        assertEquals(2, translations.size)
        assertTrue(translations.any { it.language == "de" && it.translatedName == "Test-DE" })
        assertTrue(translations.any { it.language == "fr" && it.translatedName == "Test-FR" })
    }

    @Test
    fun testHabitToNameTranslationsNull() {
        val habit = Habit("Test", "")
        habit.id = "h1"
        habit.nameTranslations = null

        val translations = habit.toNameTranslations()

        assertTrue(translations.isEmpty())
    }

    @Test
    fun testHabitToDescTranslations() {
        val habit = Habit("Test", "")
        habit.id = "h1"
        habit.descriptionTranslations = hashMapOf("de" to "Beschreibung")

        val translations = habit.toDescTranslations()

        assertEquals(1, translations.size)
        assertEquals("de", translations[0].language)
        assertEquals("Beschreibung", translations[0].translatedDescription)
    }

    @Test
    fun testHabitToDescTranslationsNull() {
        val habit = Habit("Test", "")
        habit.id = "h1"
        habit.descriptionTranslations = null

        val translations = habit.toDescTranslations()

        assertTrue(translations.isEmpty())
    }

    // ── HabitCategory ──

    @Test
    fun testHabitCategoryToEntity() {
        val cat = HabitCategory("Health", "Health habits", "#FF0000")
        cat.id = "cat1"
        cat.user = createUser()

        val entity = cat.toEntity()

        assertEquals("cat1", entity.id)
        assertEquals("Health", entity.name)
        assertEquals("Health habits", entity.description)
        assertEquals("#FF0000", entity.color)
        assertEquals("u1", entity.userId)
        assertEquals("testuser", entity.userName)
    }

    @Test
    fun testHabitCategoryToEntityNullUser() {
        val cat = HabitCategory("Health")

        val entity = cat.toEntity()

        assertEquals("", entity.userId)
        assertEquals("", entity.userName)
    }

    @Test
    fun testHabitCategoryEntityToModel() {
        val entity = HabitCategoryEntity(
            id = "cat1", name = "Health", description = "Healthy habits",
            color = "#00FF00", userId = "u1", userName = "testuser"
        )

        val cat = entity.toModel(emptyList(), emptyList())

        assertEquals("cat1", cat.id)
        assertEquals("Health", cat.name)
        assertEquals("Healthy habits", cat.description)
        assertEquals("#00FF00", cat.color)
        assertEquals("u1", cat.user.id)
        assertEquals("testuser", cat.user.username)
    }

    @Test
    fun testHabitCategoryEntityToModelWithTranslations() {
        val entity = HabitCategoryEntity(
            id = "cat1", name = "Health", description = null,
            color = null, userId = "u1", userName = "testuser"
        )
        val nameTranslations = listOf(CategoryNameTranslationEntity("cat1", "de", "Gesundheit"))
        val descTranslations = listOf(CategoryDescriptionTranslationEntity("cat1", "de", "Gesunde Gewohnheiten"))

        val cat = entity.toModel(nameTranslations, descTranslations)

        assertNotNull(cat.nameTranslations)
        assertEquals("Gesundheit", cat.nameTranslations["de"])
        assertNotNull(cat.descriptionTranslations)
        assertEquals("Gesunde Gewohnheiten", cat.descriptionTranslations["de"])
    }

    @Test
    fun testHabitCategoryToNameTranslations() {
        val cat = HabitCategory("Health")
        cat.id = "cat1"
        cat.nameTranslations = hashMapOf("de" to "Gesundheit")

        val translations = cat.toNameTranslations()

        assertEquals(1, translations.size)
        assertEquals("cat1", translations[0].categoryId)
        assertEquals("de", translations[0].language)
    }

    @Test
    fun testHabitCategoryToDescTranslations() {
        val cat = HabitCategory("Health")
        cat.id = "cat1"
        cat.descriptionTranslations = hashMapOf("de" to "Desc-DE")

        val translations = cat.toDescTranslations()

        assertEquals(1, translations.size)
        assertEquals("Desc-DE", translations[0].translatedDescription)
    }

    // ── DiaryReference ──

    @Test
    fun testDiaryReferenceToEntity() {
        val ref = DiaryReference("Morning walk")
        ref.id = "ref1"
        ref.user = createUser()

        val entity = ref.toEntity()

        assertEquals("ref1", entity.id)
        assertEquals("Morning walk", entity.description)
        assertEquals("morning walk", entity.descriptionLower)
        assertEquals("u1", entity.userId)
        assertEquals("testuser", entity.userName)
    }

    @Test
    fun testDiaryReferenceToEntityEmptyDescription() {
        val ref = DiaryReference("")
        ref.id = "ref1"
        ref.user = createUser()

        val entity = ref.toEntity()

        assertEquals("", entity.description)
        assertEquals("", entity.descriptionLower)
    }

    @Test
    fun testDiaryReferenceEntityToModel() {
        val entity = DiaryReferenceEntity(
            id = "ref1", description = "Morning walk",
            descriptionLower = "morning walk", userId = "u1", userName = "testuser"
        )

        val ref = entity.toModel()

        assertEquals("ref1", ref.id)
        assertEquals("Morning walk", ref.description)
        assertEquals("u1", ref.user.id)
        assertEquals("testuser", ref.user.username)
    }

    // ── DiaryEntry ──

    @Test
    fun testDiaryEntryToEntity() {
        val ref = DiaryReference("Test event")
        ref.id = "ref1"
        val entry = DiaryEntry()
        entry.id = "de1"
        entry.diaryReference = ref
        entry.significance = EventSignificance.MAJOR
        entry.eventDate = LocalDate.of(2024, 6, 15)
        entry.startTime = LocalTime.of(9, 0)
        entry.endTime = LocalTime.of(10, 30)
        entry.createdAt = LocalDateTime.of(2024, 6, 15, 9, 0, 0)
        entry.user = createUser()

        val entity = entry.toEntity()

        assertEquals("de1", entity.id)
        assertNull(entity.legacyDescription)
        assertEquals("ref1", entity.diaryReferenceId)
        assertEquals("MAJOR", entity.significance)
        assertEquals("2024-06-15", entity.eventDate)
        assertEquals("09:00", entity.startTime)
        assertEquals("10:30", entity.endTime)
        assertEquals("u1", entity.userId)
    }

    @Test
    fun testDiaryEntryToEntityNullTimes() {
        val entry = DiaryEntry()
        entry.id = "de1"
        entry.user = createUser()

        val entity = entry.toEntity()

        assertNull(entity.startTime)
        assertNull(entity.endTime)
    }

    @Test
    fun testDiaryEntryToEntityDefaultSignificance() {
        val entry = DiaryEntry()
        entry.user = createUser()

        val entity = entry.toEntity()

        assertEquals("NORMAL", entity.significance)
    }

    @Test
    fun testDiaryEntryEntityToModel() {
        val entity = DiaryEntryEntity(
            id = "de1", legacyDescription = null, diaryReferenceId = "ref1",
            significance = "MAJOR", eventDate = "2024-06-15",
            startTime = "09:00", endTime = "10:30",
            createdAt = "2024-06-15T09:00:00", userId = "u1", userName = "testuser"
        )
        val ref = DiaryReference("Test event")
        ref.id = "ref1"

        val entry = entity.toModel(ref)

        assertEquals("de1", entry.id)
        assertEquals(EventSignificance.MAJOR, entry.significance)
        assertEquals(LocalDate.of(2024, 6, 15), entry.eventDate)
        assertEquals(LocalTime.of(9, 0), entry.startTime)
        assertEquals(LocalTime.of(10, 30), entry.endTime)
        assertEquals(ref, entry.diaryReference)
        assertEquals("u1", entry.user.id)
    }

    @Test
    fun testDiaryEntryEntityToModelNullTimes() {
        val entity = DiaryEntryEntity(
            id = "de1", legacyDescription = null, diaryReferenceId = null,
            significance = "NORMAL", eventDate = "2024-06-15",
            startTime = null, endTime = null,
            createdAt = "2024-06-15T09:00:00", userId = "u1", userName = "testuser"
        )

        val entry = entity.toModel(null)

        assertNull(entry.startTime)
        assertNull(entry.endTime)
        assertNull(entry.diaryReference)
    }

    @Test
    fun testDiaryEntryEntityToModelInvalidSignificanceDefaultsToNormal() {
        val entity = DiaryEntryEntity(
            id = "de1", legacyDescription = null, diaryReferenceId = null,
            significance = "INVALID", eventDate = "2024-06-15",
            startTime = null, endTime = null,
            createdAt = "2024-06-15T09:00:00", userId = "u1", userName = "testuser"
        )

        val entry = entity.toModel(null)

        assertEquals(EventSignificance.NORMAL, entry.significance)
    }

    // ── SleepEntry ──

    @Test
    fun testSleepEntryToEntity() {
        val sleepEntry = SleepEntry(LocalTime.of(23, 0), LocalTime.of(7, 0))
        sleepEntry.id = "se1"
        sleepEntry.date = LocalDate.of(2024, 6, 15)
        sleepEntry.createdAt = LocalDateTime.of(2024, 6, 15, 23, 0, 0)
        sleepEntry.notes = "Good sleep"
        sleepEntry.user = createUser()

        val entity = sleepEntry.toEntity()

        assertEquals("se1", entity.id)
        assertEquals("23:00", entity.fromTime)
        assertEquals("07:00", entity.untilTime)
        assertEquals("2024-06-15", entity.date)
        assertEquals("Good sleep", entity.notes)
        assertEquals("u1", entity.userId)
    }

    @Test
    fun testSleepEntryEntityToModel() {
        val entity = SleepEntryEntity(
            id = "se1", fromTime = "23:00", untilTime = "07:00",
            date = "2024-06-15", createdAt = "2024-06-15T23:00:00",
            notes = "Good sleep", userId = "u1", userName = "testuser"
        )

        val sleepEntry = entity.toModel()

        assertEquals("se1", sleepEntry.id)
        assertEquals(LocalTime.of(23, 0), sleepEntry.fromTime)
        assertEquals(LocalTime.of(7, 0), sleepEntry.untilTime)
        assertEquals(LocalDate.of(2024, 6, 15), sleepEntry.date)
        assertEquals("Good sleep", sleepEntry.notes)
        assertEquals("u1", sleepEntry.user.id)
    }

    // ── EmotionPair ──

    @Test
    fun testEmotionPairToEntity() {
        val pair = EmotionPair("Sad", "Happy")
        pair.id = "ep1"
        pair.user = createUser()

        val entity = pair.toEntity()

        assertEquals("ep1", entity.id)
        assertEquals("Sad", entity.negativeLabel)
        assertEquals("Happy", entity.positiveLabel)
        assertEquals("u1", entity.userId)
    }

    @Test
    fun testEmotionPairEntityToModel() {
        val entity = EmotionPairEntity(
            id = "ep1", negativeLabel = "Sad", positiveLabel = "Happy",
            userId = "u1", userName = "testuser"
        )

        val pair = entity.toModel()

        assertEquals("ep1", pair.id)
        assertEquals("Sad", pair.negativeLabel)
        assertEquals("Happy", pair.positiveLabel)
        assertEquals("u1", pair.user.id)
    }

    // ── EmotionEntry ──

    @Test
    fun testEmotionEntryToEntity() {
        val pair = EmotionPair("Sad", "Happy")
        pair.id = "ep1"
        val entry = EmotionEntry()
        entry.id = "ee1"
        entry.emotionPair = pair
        entry.strength = 5
        entry.recordedAt = LocalDateTime.of(2024, 6, 15, 14, 30, 0)
        entry.notes = "Feeling good"
        entry.user = createUser()

        val entity = entry.toEntity()

        assertEquals("ee1", entity.id)
        assertEquals("ep1", entity.emotionPairId)
        assertEquals(5, entity.strength)
        assertEquals("2024-06-15T14:30:00", entity.recordedAt)
        assertEquals("Feeling good", entity.notes)
        assertEquals("u1", entity.userId)
    }

    @Test
    fun testEmotionEntryToEntityNullPair() {
        val entry = EmotionEntry()
        entry.id = "ee1"
        entry.user = createUser()

        val entity = entry.toEntity()

        assertEquals("", entity.emotionPairId)
    }

    @Test
    fun testEmotionEntryEntityToModel() {
        val entity = EmotionEntryEntity(
            id = "ee1", emotionPairId = "ep1", strength = 7,
            recordedAt = "2024-06-15T14:30:00", notes = "Great day",
            userId = "u1", userName = "testuser"
        )
        val pair = EmotionPair("Sad", "Happy")
        pair.id = "ep1"

        val entry = entity.toModel(pair)

        assertNotNull(entry)
        assertEquals("ee1", entry!!.id)
        assertEquals(pair, entry.emotionPair)
        assertEquals(7, entry.strength)
        assertEquals(LocalDateTime.of(2024, 6, 15, 14, 30, 0), entry.recordedAt)
        assertEquals("Great day", entry.notes)
    }

    @Test
    fun testEmotionEntryEntityToModelNullPairReturnsNull() {
        val entity = EmotionEntryEntity(
            id = "ee1", emotionPairId = "ep1", strength = 5,
            recordedAt = "2024-06-15T14:30:00", notes = null,
            userId = "u1", userName = "testuser"
        )

        val entry = entity.toModel(null)

        assertNull(entry)
    }

    // ── SportLog ──

    @Test
    fun testSportLogToEntity() {
        val log = SportLog()
        log.id = "sl1"
        log.name = "Running"
        log.measurement = 5.5
        log.measurementUnit = "km"
        log.startTime = LocalTime.of(7, 0)
        log.endTime = LocalTime.of(8, 0)
        log.date = LocalDate.of(2024, 6, 15)
        log.createdAt = LocalDateTime.of(2024, 6, 15, 8, 0, 0)
        log.notes = "Morning run"
        log.user = createUser()

        val entity = log.toEntity()

        assertEquals("sl1", entity.id)
        assertEquals("Running", entity.name)
        assertEquals(5.5, entity.measurement)
        assertEquals("km", entity.measurementUnit)
        assertEquals("07:00", entity.startTime)
        assertEquals("08:00", entity.endTime)
        assertEquals("2024-06-15", entity.date)
        assertEquals("Morning run", entity.notes)
    }

    @Test
    fun testSportLogToEntityNullTimes() {
        val log = SportLog()
        log.id = "sl1"
        log.name = "Test"
        log.user = createUser()

        val entity = log.toEntity()

        assertNull(entity.startTime)
        assertNull(entity.endTime)
    }

    @Test
    fun testSportLogEntityToModel() {
        val entity = SportLogEntity(
            id = "sl1", name = "Running", measurement = 5.5, measurementUnit = "km",
            startTime = "07:00", endTime = "08:00", date = "2024-06-15",
            createdAt = "2024-06-15T08:00:00", notes = "Morning run",
            userId = "u1", userName = "testuser"
        )

        val log = entity.toModel()

        assertEquals("sl1", log.id)
        assertEquals("Running", log.name)
        assertEquals(5.5, log.measurement, 0.001)
        assertEquals("km", log.measurementUnit)
        assertEquals(LocalTime.of(7, 0), log.startTime)
        assertEquals(LocalTime.of(8, 0), log.endTime)
        assertEquals(LocalDate.of(2024, 6, 15), log.date)
        assertEquals("Morning run", log.notes)
    }

    @Test
    fun testSportLogEntityToModelNullMeasurement() {
        val entity = SportLogEntity(
            id = "sl1", name = "Test", measurement = null, measurementUnit = null,
            startTime = null, endTime = null, date = "2024-06-15",
            createdAt = "2024-06-15T08:00:00", notes = null,
            userId = "u1", userName = "testuser"
        )

        val log = entity.toModel()

        assertEquals(0.0, log.measurement, 0.001)
        assertNull(log.startTime)
        assertNull(log.endTime)
    }

    // ── FoodLog ──

    @Test
    fun testFoodLogToEntity() {
        val log = FoodLog()
        log.id = "fl1"
        log.carbohydrates = 45.5
        log.kcal = 350
        log.dateTime = LocalDateTime.of(2024, 6, 15, 12, 0, 0)
        log.foodItems = "Rice, Chicken"
        log.createdAt = LocalDateTime.of(2024, 6, 15, 12, 0, 0)
        log.notes = "Lunch"
        log.user = createUser()

        val entity = log.toEntity()

        assertEquals("fl1", entity.id)
        assertEquals(45.5, entity.carbohydrates)
        assertEquals(350, entity.kcal)
        assertEquals("Rice, Chicken", entity.foodItems)
        assertEquals("Lunch", entity.notes)
    }

    @Test
    fun testFoodLogEntityToModel() {
        val entity = FoodLogEntity(
            id = "fl1", carbohydrates = 45.5, kcal = 350,
            dateTime = "2024-06-15T12:00:00", foodItems = "Rice, Chicken",
            createdAt = "2024-06-15T12:00:00", notes = "Lunch",
            userId = "u1", userName = "testuser"
        )

        val log = entity.toModel()

        assertEquals("fl1", log.id)
        assertEquals(45.5, log.carbohydrates)
        assertEquals(350, log.kcal)
        assertEquals("Rice, Chicken", log.foodItems)
        assertEquals("Lunch", log.notes)
        assertEquals("u1", log.user.id)
        assertTrue(log.tags.isEmpty())
    }

    @Test
    fun testFoodLogEntityToModelWithTags() {
        val entity = FoodLogEntity(
            id = "fl1", carbohydrates = 45.5, kcal = 350,
            dateTime = "2024-06-15T12:00:00", foodItems = "Rice, Chicken",
            createdAt = "2024-06-15T12:00:00", notes = "Lunch",
            userId = "u1", userName = "testuser"
        )
        val tag1 = FoodTag("Rice")
        tag1.id = "t1"
        val tag2 = FoodTag("Chicken")
        tag2.id = "t2"

        val log = entity.toModel(setOf(tag1, tag2))

        assertEquals("fl1", log.id)
        assertEquals(2, log.tags.size)
        assertTrue(log.tags.any { it.name == "Rice" })
        assertTrue(log.tags.any { it.name == "Chicken" })
    }

    // ── FoodTag ──

    @Test
    fun testFoodTagToEntity() {
        val tag = FoodTag("Rice")
        tag.id = "ft1"
        tag.user = createUser()

        val entity = tag.toEntity()

        assertEquals("ft1", entity.id)
        assertEquals("Rice", entity.name)
        assertEquals("rice", entity.nameLower)
        assertEquals("u1", entity.userId)
    }

    @Test
    fun testFoodTagToEntityEmptyName() {
        val tag = FoodTag("")
        tag.id = "ft1"
        tag.user = createUser()

        val entity = tag.toEntity()

        assertEquals("", entity.name)
        assertEquals("", entity.nameLower)
    }

    @Test
    fun testFoodTagEntityToModel() {
        val entity = FoodTagEntity(
            id = "ft1", name = "Rice", nameLower = "rice",
            userId = "u1", userName = "testuser"
        )

        val tag = entity.toModel()

        assertEquals("ft1", tag.id)
        assertEquals("Rice", tag.name)
        assertEquals("u1", tag.user.id)
    }

    // ── Medication ──

    @Test
    fun testMedicationToEntity() {
        val med = Medication("Aspirin", MedicationProvisionType.PILL)
        med.id = "m1"
        med.wikipediaLink = "https://en.wikipedia.org/wiki/Aspirin"
        med.user = createUser()

        val entity = med.toEntity()

        assertEquals("m1", entity.id)
        assertEquals("Aspirin", entity.name)
        assertEquals("https://en.wikipedia.org/wiki/Aspirin", entity.wikipediaLink)
        assertEquals("PILL", entity.provisionType)
        assertEquals("u1", entity.userId)
    }

    @Test
    fun testMedicationToEntityNullProvisionType() {
        val med = Medication()
        med.id = "m1"
        med.name = "Test"
        med.provisionType = null
        med.user = createUser()

        val entity = med.toEntity()

        assertEquals("PILL", entity.provisionType)
    }

    @Test
    fun testMedicationToEntityLiquidDrops() {
        val med = Medication("Drops", MedicationProvisionType.LIQUID_DROPS)
        med.id = "m1"
        med.user = createUser()

        val entity = med.toEntity()

        assertEquals("LIQUID_DROPS", entity.provisionType)
    }

    @Test
    fun testMedicationEntityToModel() {
        val entity = MedicationEntity(
            id = "m1", name = "Aspirin",
            wikipediaLink = "https://en.wikipedia.org/wiki/Aspirin",
            provisionType = "PILL", userId = "u1", userName = "testuser"
        )

        val med = entity.toModel()

        assertEquals("m1", med.id)
        assertEquals("Aspirin", med.name)
        assertEquals("https://en.wikipedia.org/wiki/Aspirin", med.wikipediaLink)
        assertEquals(MedicationProvisionType.PILL, med.provisionType)
        assertEquals("u1", med.user.id)
    }

    @Test
    fun testMedicationEntityToModelInvalidProvisionTypeDefaultsToPill() {
        val entity = MedicationEntity(
            id = "m1", name = "Test", wikipediaLink = null,
            provisionType = "INVALID", userId = "u1", userName = "testuser"
        )

        val med = entity.toModel()

        assertEquals(MedicationProvisionType.PILL, med.provisionType)
    }

    @Test
    fun testMedicationEntityToModelLiquidMl() {
        val entity = MedicationEntity(
            id = "m1", name = "Syrup", wikipediaLink = null,
            provisionType = "LIQUID_ML", userId = "u1", userName = "testuser"
        )

        val med = entity.toModel()

        assertEquals(MedicationProvisionType.LIQUID_ML, med.provisionType)
    }

    // ── MedicationLog ──

    @Test
    fun testMedicationLogToEntity() {
        val med = Medication("Aspirin", MedicationProvisionType.PILL)
        med.id = "m1"
        val log = MedicationLog()
        log.id = "ml1"
        log.medication = med
        log.amount = 2.5
        log.takenAt = LocalDateTime.of(2024, 6, 15, 8, 0, 0)
        log.createdAt = LocalDateTime.of(2024, 6, 15, 8, 0, 0)
        log.notes = "After breakfast"
        log.user = createUser()

        val entity = log.toEntity()

        assertEquals("ml1", entity.id)
        assertEquals("m1", entity.medicationId)
        assertEquals(2.5, entity.amount, 0.001)
        assertEquals("2024-06-15T08:00:00", entity.takenAt)
        assertEquals("After breakfast", entity.notes)
    }

    @Test
    fun testMedicationLogToEntityNullMedication() {
        val log = MedicationLog()
        log.id = "ml1"
        log.user = createUser()

        val entity = log.toEntity()

        assertEquals("", entity.medicationId)
    }

    @Test
    fun testMedicationLogEntityToModel() {
        val entity = MedicationLogEntity(
            id = "ml1", medicationId = "m1", amount = 2.5,
            takenAt = "2024-06-15T08:00:00", createdAt = "2024-06-15T08:00:00",
            notes = "After breakfast", userId = "u1", userName = "testuser"
        )
        val med = Medication("Aspirin", MedicationProvisionType.PILL)
        med.id = "m1"

        val log = entity.toModel(med)

        assertEquals("ml1", log.id)
        assertEquals(med, log.medication)
        assertEquals(2.5, log.amount, 0.001)
        assertEquals(LocalDateTime.of(2024, 6, 15, 8, 0, 0), log.takenAt)
        assertEquals("After breakfast", log.notes)
    }

    @Test
    fun testMedicationLogEntityToModelNullMedication() {
        val entity = MedicationLogEntity(
            id = "ml1", medicationId = "m1", amount = 1.0,
            takenAt = "2024-06-15T08:00:00", createdAt = "2024-06-15T08:00:00",
            notes = null, userId = "u1", userName = "testuser"
        )

        val log = entity.toModel(null)

        assertNull(log.medication)
    }

    // ── EmergencyPlanStep ──

    @Test
    fun testEmergencyPlanStepToEntity() {
        val step = EmergencyPlanStep("Are you safe?", 1)
        step.id = "eps1"
        step.user = createUser()

        val entity = step.toEntity()

        assertEquals("eps1", entity.id)
        assertEquals("Are you safe?", entity.question)
        assertEquals(1, entity.stepOrder)
        assertEquals("u1", entity.userId)
    }

    @Test
    fun testEmergencyPlanStepEntityToModel() {
        val entity = EmergencyPlanStepEntity(
            id = "eps1", question = "Are you safe?", stepOrder = 1,
            userId = "u1", userName = "testuser"
        )

        val step = entity.toModel()

        assertEquals("eps1", step.id)
        assertEquals("Are you safe?", step.question)
        assertEquals(1, step.stepOrder)
        assertEquals("u1", step.user.id)
    }

    @Test
    fun testEmergencyPlanStepEntityToModelWithActions() {
        val entity = EmergencyPlanStepEntity(
            id = "eps1", question = "Are you safe?", stepOrder = 1,
            userId = "u1", userName = "testuser"
        )
        val action = EmergencyPlanAction("Call 911", "911", 1)

        val step = entity.toModel(listOf(action))

        assertEquals(1, step.actions.size)
        assertEquals("Call 911", step.actions[0].actionText)
    }

    @Test
    fun testEmergencyPlanStepEntityToModelEmptyActions() {
        val entity = EmergencyPlanStepEntity(
            id = "eps1", question = "Question?", stepOrder = 0,
            userId = "u1", userName = "testuser"
        )

        val step = entity.toModel(emptyList())

        assertTrue(step.actions.isEmpty())
    }

    // ── EmergencyPlanAction ──

    @Test
    fun testEmergencyPlanActionToEntity() {
        val step = EmergencyPlanStep("Q?", 1)
        step.id = "eps1"
        val action = EmergencyPlanAction("Call help", "555-1234", 1)
        action.id = "epa1"
        action.step = step

        val entity = action.toEntity()

        assertEquals("epa1", entity.id)
        assertEquals("Call help", entity.actionText)
        assertEquals("555-1234", entity.phoneNumber)
        assertEquals(1, entity.actionOrder)
        assertEquals("eps1", entity.stepId)
    }

    @Test
    fun testEmergencyPlanActionToEntityNullStep() {
        val action = EmergencyPlanAction("Call help", 1)
        action.id = "epa1"

        val entity = action.toEntity()

        assertEquals("", entity.stepId)
    }

    @Test
    fun testEmergencyPlanActionToEntityNullPhone() {
        val action = EmergencyPlanAction("Breathe deeply", 1)
        action.id = "epa1"

        val entity = action.toEntity()

        assertNull(entity.phoneNumber)
    }

    @Test
    fun testEmergencyPlanActionEntityToModel() {
        val entity = EmergencyPlanActionEntity(
            id = "epa1", actionText = "Call help",
            phoneNumber = "555-1234", actionOrder = 1, stepId = "eps1"
        )

        val action = entity.toModel()

        assertEquals("epa1", action.id)
        assertEquals("Call help", action.actionText)
        assertEquals("555-1234", action.phoneNumber)
        assertEquals(1, action.actionOrder)
        assertNull(action.step)
    }

    @Test
    fun testEmergencyPlanActionEntityToModelWithStep() {
        val entity = EmergencyPlanActionEntity(
            id = "epa1", actionText = "Call help",
            phoneNumber = null, actionOrder = 2, stepId = "eps1"
        )
        val step = EmergencyPlanStep("Q?", 1)
        step.id = "eps1"

        val action = entity.toModel(step)

        assertEquals(step, action.step)
        assertNull(action.phoneNumber)
    }

    // ── ActivityLog ──

    @Test
    fun testActivityLogToEntity() {
        val log = ActivityLog("Alice, Bob", "Park", LocalTime.of(10, 0), LocalTime.of(11, 30))
        log.id = "al1"
        log.date = LocalDate.of(2024, 6, 15)
        log.activity = "Walking"
        log.createdAt = LocalDateTime.of(2024, 6, 15, 12, 0, 0)
        log.user = createUser()

        val entity = log.toEntity()

        assertEquals("al1", entity.id)
        assertEquals("Alice, Bob", entity.persons)
        assertEquals("Park", entity.location)
        assertEquals("10:00", entity.startTime)
        assertEquals("11:30", entity.endTime)
        assertEquals("2024-06-15", entity.date)
        assertEquals("Walking", entity.activity)
        assertEquals("u1", entity.userId)
    }

    @Test
    fun testActivityLogToEntityNullFields() {
        val log = ActivityLog()
        log.id = "al1"
        log.user = createUser()

        val entity = log.toEntity()

        assertEquals("", entity.persons)
        assertEquals("", entity.location)
        assertEquals("", entity.startTime)
        assertEquals("", entity.endTime)
    }

    @Test
    fun testActivityLogEntityToModel() {
        val entity = ActivityLogEntity(
            id = "al1", persons = "Alice, Bob", location = "Park",
            startTime = "10:00", endTime = "11:30", date = "2024-06-15",
            activity = "Walking", createdAt = "2024-06-15T12:00:00",
            userId = "u1", userName = "testuser"
        )

        val log = entity.toModel()

        assertEquals("al1", log.id)
        assertEquals("Alice, Bob", log.persons)
        assertEquals("Park", log.location)
        assertEquals(LocalTime.of(10, 0), log.startTime)
        assertEquals(LocalTime.of(11, 30), log.endTime)
        assertEquals(LocalDate.of(2024, 6, 15), log.date)
        assertEquals("Walking", log.activity)
        assertEquals("u1", log.user.id)
    }

    // ── Round-trip tests ──

    @Test
    fun testHabitRoundTrip() {
        val original = Habit("Exercise", "Daily workout")
        original.id = "h1"
        original.user = createUser()
        original.frequencyType = FrequencyType.WEEKLY
        original.targetFrequency = 3
        original.maxEntriesPerDay = 2
        original.isPositiveScoring = false
        original.createdAt = LocalDateTime.of(2024, 1, 1, 0, 0, 0)
        original.nameTranslations = hashMapOf("de" to "Ubung")
        original.descriptionTranslations = hashMapOf("de" to "Tagliches Training")

        val entity = original.toEntity()
        val entryEntities = original.toEntryEntities()
        val nameTranslations = original.toNameTranslations()
        val descTranslations = original.toDescTranslations()
        val restored = entity.toModel(entryEntities, nameTranslations, descTranslations)

        assertEquals(original.id, restored.id)
        assertEquals(original.name, restored.name)
        assertEquals(original.description, restored.description)
        assertEquals(original.frequencyType, restored.frequencyType)
        assertEquals(original.targetFrequency, restored.targetFrequency)
        assertEquals(original.maxEntriesPerDay, restored.maxEntriesPerDay)
        assertEquals(original.isPositiveScoring, restored.isPositiveScoring)
        assertEquals("Ubung", restored.nameTranslations["de"])
        assertEquals("Tagliches Training", restored.descriptionTranslations["de"])
    }

    @Test
    fun testSleepEntryRoundTrip() {
        val original = SleepEntry(LocalTime.of(22, 30), LocalTime.of(6, 45))
        original.id = "se1"
        original.date = LocalDate.of(2024, 6, 15)
        original.createdAt = LocalDateTime.of(2024, 6, 15, 22, 30, 0)
        original.notes = "Deep sleep"
        original.user = createUser()

        val entity = original.toEntity()
        val restored = entity.toModel()

        assertEquals(original.id, restored.id)
        assertEquals(original.fromTime, restored.fromTime)
        assertEquals(original.untilTime, restored.untilTime)
        assertEquals(original.date, restored.date)
        assertEquals(original.notes, restored.notes)
    }

    @Test
    fun testEmotionPairRoundTrip() {
        val original = EmotionPair("Anxious", "Calm")
        original.id = "ep1"
        original.user = createUser()

        val entity = original.toEntity()
        val restored = entity.toModel()

        assertEquals(original.id, restored.id)
        assertEquals(original.negativeLabel, restored.negativeLabel)
        assertEquals(original.positiveLabel, restored.positiveLabel)
    }

    @Test
    fun testFoodTagRoundTrip() {
        val original = FoodTag("Broccoli")
        original.id = "ft1"
        original.user = createUser()

        val entity = original.toEntity()
        val restored = entity.toModel()

        assertEquals(original.id, restored.id)
        assertEquals(original.name, restored.name)
    }

    @Test
    fun testMedicationRoundTrip() {
        val original = Medication("Ibuprofen", MedicationProvisionType.LIQUID_ML)
        original.id = "m1"
        original.wikipediaLink = "https://example.com"
        original.user = createUser()

        val entity = original.toEntity()
        val restored = entity.toModel()

        assertEquals(original.id, restored.id)
        assertEquals(original.name, restored.name)
        assertEquals(original.wikipediaLink, restored.wikipediaLink)
        assertEquals(original.provisionType, restored.provisionType)
    }

    // ── Empty string edge case tests ──

    @Test
    fun testSleepEntryToModelWithEmptyTimesDoesNotCrash() {
        val entity = SleepEntryEntity(
            id = "s1", fromTime = "", untilTime = "",
            date = "2024-06-15", createdAt = "2024-06-15T22:00:00",
            notes = null, userId = "u1", userName = "test"
        )
        val model = entity.toModel()
        assertNull(model.fromTime)
        assertNull(model.untilTime)
    }

    @Test
    fun testActivityLogToModelWithEmptyTimesDoesNotCrash() {
        val entity = ActivityLogEntity(
            id = "al1", persons = "Alice", location = "Office",
            startTime = "", endTime = "",
            date = "2024-06-15", activity = null,
            createdAt = "2024-06-15T09:00:00",
            userId = "u1", userName = "test"
        )
        val model = entity.toModel()
        assertNull(model.startTime)
        assertNull(model.endTime)
    }

    @Test
    fun testActivityLogToModelWithEmptyDateDoesNotCrash() {
        val entity = ActivityLogEntity(
            id = "al1", persons = "Alice", location = "Office",
            startTime = "09:00", endTime = "10:00",
            date = "", activity = null,
            createdAt = "",
            userId = "u1", userName = "test"
        )
        val model = entity.toModel()
        assertNull(model.date)
        assertNull(model.createdAt)
    }

    @Test
    fun testFoodLogToModelWithEmptyDateTimeDoesNotCrash() {
        val entity = FoodLogEntity(
            id = "f1", carbohydrates = null, kcal = null,
            dateTime = "", foodItems = null,
            createdAt = "", notes = null,
            userId = "u1", userName = "test"
        )
        val model = entity.toModel()
        assertNull(model.dateTime)
        assertNull(model.createdAt)
    }

    @Test
    fun testMedicationLogToModelWithEmptyDateTimesDoesNotCrash() {
        val entity = MedicationLogEntity(
            id = "ml1", medicationId = "m1", amount = 1.0,
            takenAt = "", createdAt = "",
            notes = null, userId = "u1", userName = "test"
        )
        val model = entity.toModel(null)
        assertNull(model.takenAt)
        assertNull(model.createdAt)
    }

    @Test
    fun testHabitEntryWithInvalidCompletedAtUsesDefault() {
        val entries = listOf(
            HabitEntryEntity(
                id = "he1", habitId = "h1",
                completedAt = "", notes = null, value = 1.0
            )
        )
        val habitEntity = HabitEntity(
            id = "h1", name = "Test", description = null,
            categoryId = null, frequencyType = "DAILY",
            targetFrequency = 1, maxEntriesPerDay = 1,
            positiveScoring = 1, createdAt = null,
            scoringRuleId = null, scoringRuleName = null,
            userId = "u1", userName = "test"
        )
        val model = habitEntity.toModel(entries, emptyList(), emptyList())
        assertEquals(1, model.entries.size)
        assertNotNull(model.entries[0].completedAt)
    }

    @Test
    fun testEmotionEntryToModelWithEmptyRecordedAtDoesNotCrash() {
        val pair = EmotionPairEntity(
            id = "ep1", negativeLabel = "Sad", positiveLabel = "Happy",
            userId = "u1", userName = "test"
        )
        val entry = EmotionEntryEntity(
            id = "ee1", emotionPairId = "ep1", strength = 5,
            recordedAt = "", notes = null, userId = "u1", userName = "test"
        )
        val model = entry.toModel(pair.toModel())
        assertNotNull(model)
        assertNull(model!!.recordedAt)
    }
}
