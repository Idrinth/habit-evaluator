package de.idrinth.habitevaluator.android.persistence

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class EntitiesTest {

    // ── HabitEntity ──

    @Test
    fun testHabitEntityConstruction() {
        val entity = HabitEntity(
            id = "h1", name = "Exercise", description = "Daily workout",
            categoryId = "cat1", frequencyType = "DAILY", targetFrequency = 1,
            maxEntriesPerDay = 3, positiveScoring = 1, createdAt = "2024-01-01T00:00:00",
            scoringRuleId = "sr1", scoringRuleName = "Default",
            userId = "u1", userName = "testuser"
        )

        assertEquals("h1", entity.id)
        assertEquals("Exercise", entity.name)
        assertEquals("Daily workout", entity.description)
        assertEquals("cat1", entity.categoryId)
        assertEquals("DAILY", entity.frequencyType)
        assertEquals(1, entity.targetFrequency)
        assertEquals(3, entity.maxEntriesPerDay)
        assertEquals(1, entity.positiveScoring)
        assertEquals("2024-01-01T00:00:00", entity.createdAt)
        assertEquals("sr1", entity.scoringRuleId)
        assertEquals("Default", entity.scoringRuleName)
        assertEquals("u1", entity.userId)
        assertEquals("testuser", entity.userName)
    }

    @Test
    fun testHabitEntityNullableFields() {
        val entity = HabitEntity(
            id = "h1", name = "Test", description = null,
            categoryId = null, frequencyType = "DAILY", targetFrequency = 1,
            maxEntriesPerDay = 1, positiveScoring = 0, createdAt = null,
            scoringRuleId = null, scoringRuleName = null,
            userId = "u1", userName = "user"
        )

        assertNull(entity.description)
        assertNull(entity.categoryId)
        assertNull(entity.createdAt)
        assertNull(entity.scoringRuleId)
        assertNull(entity.scoringRuleName)
    }

    @Test
    fun testHabitEntityEquality() {
        val entity1 = HabitEntity(
            id = "h1", name = "A", description = null,
            categoryId = null, frequencyType = "DAILY", targetFrequency = 1,
            maxEntriesPerDay = 1, positiveScoring = 1, createdAt = null,
            scoringRuleId = null, scoringRuleName = null,
            userId = "u1", userName = "user"
        )
        val entity2 = entity1.copy()

        assertEquals(entity1, entity2)
        assertEquals(entity1.hashCode(), entity2.hashCode())
    }

    @Test
    fun testHabitEntityInequalityByName() {
        val entity1 = HabitEntity(
            id = "h1", name = "A", description = null,
            categoryId = null, frequencyType = "DAILY", targetFrequency = 1,
            maxEntriesPerDay = 1, positiveScoring = 1, createdAt = null,
            scoringRuleId = null, scoringRuleName = null,
            userId = "u1", userName = "user"
        )
        val entity2 = entity1.copy(name = "B")

        assertNotEquals(entity1, entity2)
    }

    // ── HabitEntryEntity ──

    @Test
    fun testHabitEntryEntityConstruction() {
        val entity = HabitEntryEntity(
            id = "e1", habitId = "h1",
            completedAt = "2024-06-15T10:00:00", notes = "Done", value = 1.0
        )

        assertEquals("e1", entity.id)
        assertEquals("h1", entity.habitId)
        assertEquals("2024-06-15T10:00:00", entity.completedAt)
        assertEquals("Done", entity.notes)
        assertEquals(1.0, entity.value, 0.001)
    }

    @Test
    fun testHabitEntryEntityNullNotes() {
        val entity = HabitEntryEntity(
            id = "e1", habitId = "h1",
            completedAt = "2024-06-15T10:00:00", notes = null, value = 2.0
        )

        assertNull(entity.notes)
    }

    // ── HabitNameTranslationEntity ──

    @Test
    fun testHabitNameTranslationEntity() {
        val entity = HabitNameTranslationEntity("h1", "de", "Ubung")

        assertEquals("h1", entity.habitId)
        assertEquals("de", entity.language)
        assertEquals("Ubung", entity.translatedName)
    }

    // ── HabitDescriptionTranslationEntity ──

    @Test
    fun testHabitDescriptionTranslationEntity() {
        val entity = HabitDescriptionTranslationEntity("h1", "de", "Beschreibung")

        assertEquals("h1", entity.habitId)
        assertEquals("de", entity.language)
        assertEquals("Beschreibung", entity.translatedDescription)
    }

    // ── HabitCategoryEntity ──

    @Test
    fun testHabitCategoryEntityConstruction() {
        val entity = HabitCategoryEntity(
            id = "cat1", name = "Health", description = "Health habits",
            color = "#FF0000", userId = "u1", userName = "testuser"
        )

        assertEquals("cat1", entity.id)
        assertEquals("Health", entity.name)
        assertEquals("Health habits", entity.description)
        assertEquals("#FF0000", entity.color)
        assertEquals("u1", entity.userId)
    }

    @Test
    fun testHabitCategoryEntityNullableFields() {
        val entity = HabitCategoryEntity(
            id = "cat1", name = "Test", description = null,
            color = null, userId = "u1", userName = "user"
        )

        assertNull(entity.description)
        assertNull(entity.color)
    }

    // ── CategoryNameTranslationEntity ──

    @Test
    fun testCategoryNameTranslationEntity() {
        val entity = CategoryNameTranslationEntity("cat1", "de", "Gesundheit")

        assertEquals("cat1", entity.categoryId)
        assertEquals("de", entity.language)
        assertEquals("Gesundheit", entity.translatedName)
    }

    // ── CategoryDescriptionTranslationEntity ──

    @Test
    fun testCategoryDescriptionTranslationEntity() {
        val entity = CategoryDescriptionTranslationEntity("cat1", "fr", "Description")

        assertEquals("cat1", entity.categoryId)
        assertEquals("fr", entity.language)
        assertEquals("Description", entity.translatedDescription)
    }

    // ── DiaryReferenceEntity ──

    @Test
    fun testDiaryReferenceEntityConstruction() {
        val entity = DiaryReferenceEntity(
            id = "ref1", description = "Morning Walk",
            descriptionLower = "morning walk", userId = "u1", userName = "testuser"
        )

        assertEquals("ref1", entity.id)
        assertEquals("Morning Walk", entity.description)
        assertEquals("morning walk", entity.descriptionLower)
    }

    // ── DiaryEntryEntity ──

    @Test
    fun testDiaryEntryEntityConstruction() {
        val entity = DiaryEntryEntity(
            id = "de1", legacyDescription = null, diaryReferenceId = "ref1",
            significance = "MAJOR", eventDate = "2024-06-15",
            startTime = "09:00", endTime = "10:00",
            createdAt = "2024-06-15T09:00:00", userId = "u1", userName = "testuser"
        )

        assertEquals("de1", entity.id)
        assertNull(entity.legacyDescription)
        assertEquals("ref1", entity.diaryReferenceId)
        assertEquals("MAJOR", entity.significance)
        assertEquals("2024-06-15", entity.eventDate)
        assertEquals("09:00", entity.startTime)
        assertEquals("10:00", entity.endTime)
    }

    @Test
    fun testDiaryEntryEntityNullableTimeFields() {
        val entity = DiaryEntryEntity(
            id = "de1", legacyDescription = "Old desc", diaryReferenceId = null,
            significance = "NORMAL", eventDate = "2024-06-15",
            startTime = null, endTime = null,
            createdAt = "2024-06-15T09:00:00", userId = "u1", userName = "testuser"
        )

        assertEquals("Old desc", entity.legacyDescription)
        assertNull(entity.diaryReferenceId)
        assertNull(entity.startTime)
        assertNull(entity.endTime)
    }

    // ── SleepEntryEntity ──

    @Test
    fun testSleepEntryEntityConstruction() {
        val entity = SleepEntryEntity(
            id = "se1", fromTime = "23:00", untilTime = "07:00",
            date = "2024-06-15", createdAt = "2024-06-15T23:00:00",
            notes = "Good sleep", userId = "u1", userName = "testuser"
        )

        assertEquals("se1", entity.id)
        assertEquals("23:00", entity.fromTime)
        assertEquals("07:00", entity.untilTime)
        assertEquals("2024-06-15", entity.date)
        assertEquals("Good sleep", entity.notes)
    }

    @Test
    fun testSleepEntryEntityNullNotes() {
        val entity = SleepEntryEntity(
            id = "se1", fromTime = "22:00", untilTime = "06:00",
            date = "2024-06-15", createdAt = "2024-06-15T22:00:00",
            notes = null, userId = "u1", userName = "user"
        )

        assertNull(entity.notes)
    }

    // ── EmotionPairEntity ──

    @Test
    fun testEmotionPairEntityConstruction() {
        val entity = EmotionPairEntity(
            id = "ep1", negativeLabel = "Sad", positiveLabel = "Happy",
            userId = "u1", userName = "testuser"
        )

        assertEquals("ep1", entity.id)
        assertEquals("Sad", entity.negativeLabel)
        assertEquals("Happy", entity.positiveLabel)
    }

    // ── EmotionEntryEntity ──

    @Test
    fun testEmotionEntryEntityConstruction() {
        val entity = EmotionEntryEntity(
            id = "ee1", emotionPairId = "ep1", strength = 7,
            recordedAt = "2024-06-15T14:30:00", notes = "Good mood",
            userId = "u1", userName = "testuser"
        )

        assertEquals("ee1", entity.id)
        assertEquals("ep1", entity.emotionPairId)
        assertEquals(7, entity.strength)
        assertEquals("2024-06-15T14:30:00", entity.recordedAt)
        assertEquals("Good mood", entity.notes)
    }

    @Test
    fun testEmotionEntryEntityNullNotes() {
        val entity = EmotionEntryEntity(
            id = "ee1", emotionPairId = "ep1", strength = -3,
            recordedAt = "2024-06-15T14:30:00", notes = null,
            userId = "u1", userName = "testuser"
        )

        assertNull(entity.notes)
        assertEquals(-3, entity.strength)
    }

    // ── SportLogEntity ──

    @Test
    fun testSportLogEntityConstruction() {
        val entity = SportLogEntity(
            id = "sl1", name = "Running", measurement = 5.5, measurementUnit = "km",
            startTime = "07:00", endTime = "08:00", date = "2024-06-15",
            createdAt = "2024-06-15T08:00:00", notes = "Morning run",
            userId = "u1", userName = "testuser"
        )

        assertEquals("sl1", entity.id)
        assertEquals("Running", entity.name)
        assertEquals(5.5, entity.measurement)
        assertEquals("km", entity.measurementUnit)
        assertEquals("07:00", entity.startTime)
        assertEquals("08:00", entity.endTime)
    }

    @Test
    fun testSportLogEntityNullableFields() {
        val entity = SportLogEntity(
            id = "sl1", name = "Test", measurement = null, measurementUnit = null,
            startTime = null, endTime = null, date = "2024-06-15",
            createdAt = "2024-06-15T08:00:00", notes = null,
            userId = "u1", userName = "user"
        )

        assertNull(entity.measurement)
        assertNull(entity.measurementUnit)
        assertNull(entity.startTime)
        assertNull(entity.endTime)
        assertNull(entity.notes)
    }

    // ── FoodLogEntity ──

    @Test
    fun testFoodLogEntityConstruction() {
        val entity = FoodLogEntity(
            id = "fl1", carbohydrates = 45.5, kcal = 350,
            dateTime = "2024-06-15T12:00:00", foodItems = "Rice, Chicken",
            createdAt = "2024-06-15T12:00:00", notes = "Lunch",
            userId = "u1", userName = "testuser"
        )

        assertEquals("fl1", entity.id)
        assertEquals(45.5, entity.carbohydrates)
        assertEquals(350, entity.kcal)
        assertEquals("Rice, Chicken", entity.foodItems)
        assertEquals("Lunch", entity.notes)
    }

    @Test
    fun testFoodLogEntityNullableFields() {
        val entity = FoodLogEntity(
            id = "fl1", carbohydrates = null, kcal = null,
            dateTime = "2024-06-15T12:00:00", foodItems = null,
            createdAt = "2024-06-15T12:00:00", notes = null,
            userId = "u1", userName = "user"
        )

        assertNull(entity.carbohydrates)
        assertNull(entity.kcal)
        assertNull(entity.foodItems)
        assertNull(entity.notes)
    }

    // ── FoodTagEntity ──

    @Test
    fun testFoodTagEntityConstruction() {
        val entity = FoodTagEntity(
            id = "ft1", name = "Rice", nameLower = "rice",
            userId = "u1", userName = "testuser"
        )

        assertEquals("ft1", entity.id)
        assertEquals("Rice", entity.name)
        assertEquals("rice", entity.nameLower)
    }

    // ── FoodLogTagCrossRef ──

    @Test
    fun testFoodLogTagCrossRefConstruction() {
        val ref = FoodLogTagCrossRef(foodLogId = "fl1", foodTagId = "ft1")

        assertEquals("fl1", ref.foodLogId)
        assertEquals("ft1", ref.foodTagId)
    }

    @Test
    fun testFoodLogTagCrossRefEquality() {
        val ref1 = FoodLogTagCrossRef(foodLogId = "fl1", foodTagId = "ft1")
        val ref2 = FoodLogTagCrossRef(foodLogId = "fl1", foodTagId = "ft1")

        assertEquals(ref1, ref2)
        assertEquals(ref1.hashCode(), ref2.hashCode())
    }

    @Test
    fun testFoodLogTagCrossRefInequality() {
        val ref1 = FoodLogTagCrossRef(foodLogId = "fl1", foodTagId = "ft1")
        val ref2 = FoodLogTagCrossRef(foodLogId = "fl1", foodTagId = "ft2")

        assertNotEquals(ref1, ref2)
    }

    // ── MedicationEntity ──

    @Test
    fun testMedicationEntityConstruction() {
        val entity = MedicationEntity(
            id = "m1", name = "Aspirin",
            wikipediaLink = "https://en.wikipedia.org/wiki/Aspirin",
            provisionType = "PILL", userId = "u1", userName = "testuser"
        )

        assertEquals("m1", entity.id)
        assertEquals("Aspirin", entity.name)
        assertEquals("https://en.wikipedia.org/wiki/Aspirin", entity.wikipediaLink)
        assertEquals("PILL", entity.provisionType)
    }

    @Test
    fun testMedicationEntityNullWikipediaLink() {
        val entity = MedicationEntity(
            id = "m1", name = "Test", wikipediaLink = null,
            provisionType = "LIQUID_DROPS", userId = "u1", userName = "user"
        )

        assertNull(entity.wikipediaLink)
        assertEquals("LIQUID_DROPS", entity.provisionType)
    }

    // ── MedicationLogEntity ──

    @Test
    fun testMedicationLogEntityConstruction() {
        val entity = MedicationLogEntity(
            id = "ml1", medicationId = "m1", amount = 2.5,
            takenAt = "2024-06-15T08:00:00", createdAt = "2024-06-15T08:00:00",
            notes = "After breakfast", userId = "u1", userName = "testuser"
        )

        assertEquals("ml1", entity.id)
        assertEquals("m1", entity.medicationId)
        assertEquals(2.5, entity.amount, 0.001)
        assertEquals("2024-06-15T08:00:00", entity.takenAt)
        assertEquals("After breakfast", entity.notes)
    }

    @Test
    fun testMedicationLogEntityNullNotes() {
        val entity = MedicationLogEntity(
            id = "ml1", medicationId = "m1", amount = 1.0,
            takenAt = "2024-06-15T08:00:00", createdAt = "2024-06-15T08:00:00",
            notes = null, userId = "u1", userName = "user"
        )

        assertNull(entity.notes)
    }

    // ── EmergencyPlanStepEntity ──

    @Test
    fun testEmergencyPlanStepEntityConstruction() {
        val entity = EmergencyPlanStepEntity(
            id = "eps1", question = "Are you safe?", stepOrder = 1,
            userId = "u1", userName = "testuser"
        )

        assertEquals("eps1", entity.id)
        assertEquals("Are you safe?", entity.question)
        assertEquals(1, entity.stepOrder)
    }

    @Test
    fun testEmergencyPlanStepEntityEquality() {
        val entity1 = EmergencyPlanStepEntity(
            id = "eps1", question = "Q?", stepOrder = 1,
            userId = "u1", userName = "user"
        )
        val entity2 = entity1.copy()

        assertEquals(entity1, entity2)
    }

    // ── EmergencyPlanActionEntity ──

    @Test
    fun testEmergencyPlanActionEntityConstruction() {
        val entity = EmergencyPlanActionEntity(
            id = "epa1", actionText = "Call 911",
            phoneNumber = "911", actionOrder = 1, stepId = "eps1"
        )

        assertEquals("epa1", entity.id)
        assertEquals("Call 911", entity.actionText)
        assertEquals("911", entity.phoneNumber)
        assertEquals(1, entity.actionOrder)
        assertEquals("eps1", entity.stepId)
    }

    @Test
    fun testEmergencyPlanActionEntityNullPhone() {
        val entity = EmergencyPlanActionEntity(
            id = "epa1", actionText = "Breathe deeply",
            phoneNumber = null, actionOrder = 1, stepId = "eps1"
        )

        assertNull(entity.phoneNumber)
    }

    // ── ActivityLogEntity ──

    @Test
    fun testActivityLogEntityConstruction() {
        val entity = ActivityLogEntity(
            id = "al1", persons = "Alice, Bob", location = "Park",
            startTime = "10:00", endTime = "11:30", date = "2024-06-15",
            activity = "Walking", createdAt = "2024-06-15T12:00:00",
            userId = "u1", userName = "testuser"
        )

        assertEquals("al1", entity.id)
        assertEquals("Alice, Bob", entity.persons)
        assertEquals("Park", entity.location)
        assertEquals("10:00", entity.startTime)
        assertEquals("11:30", entity.endTime)
        assertEquals("2024-06-15", entity.date)
        assertEquals("Walking", entity.activity)
    }

    @Test
    fun testActivityLogEntityNullActivity() {
        val entity = ActivityLogEntity(
            id = "al1", persons = "Alice", location = "Home",
            startTime = "10:00", endTime = "11:00", date = "2024-06-15",
            activity = null, createdAt = "2024-06-15T12:00:00",
            userId = "u1", userName = "user"
        )

        assertNull(entity.activity)
    }

    @Test
    fun testActivityLogEntityCopy() {
        val entity = ActivityLogEntity(
            id = "al1", persons = "Alice", location = "Park",
            startTime = "10:00", endTime = "11:00", date = "2024-06-15",
            activity = "Walking", createdAt = "2024-06-15T12:00:00",
            userId = "u1", userName = "testuser"
        )
        val copy = entity.copy(location = "Beach")

        assertEquals("Beach", copy.location)
        assertEquals(entity.id, copy.id)
        assertEquals(entity.persons, copy.persons)
    }
}
