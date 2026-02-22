package de.idrinth.habitevaluator.android.persistence

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class EntityMappersTest {

    @Test
    fun testSleepEntryToModelWithValidTimes() {
        val entity = SleepEntryEntity(
            id = "s1", fromTime = "22:00", untilTime = "07:00",
            date = "2024-06-15", createdAt = "2024-06-15T22:00:00",
            notes = null, userId = "u1", userName = "test"
        )
        val model = entity.toModel()
        assertNotNull(model.fromTime)
        assertNotNull(model.untilTime)
        assertEquals("22:00", model.fromTime.toString())
        assertEquals("07:00", model.untilTime.toString())
    }

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
    fun testActivityLogToModelWithValidTimes() {
        val entity = ActivityLogEntity(
            id = "al1", persons = "Alice", location = "Office",
            startTime = "09:00", endTime = "10:00",
            date = "2024-06-15", activity = null,
            createdAt = "2024-06-15T09:00:00",
            userId = "u1", userName = "test"
        )
        val model = entity.toModel()
        assertNotNull(model.startTime)
        assertNotNull(model.endTime)
        assertEquals("09:00", model.startTime.toString())
        assertEquals("10:00", model.endTime.toString())
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
    fun testDiaryEntryToModelWithNullTimesDoesNotCrash() {
        val entity = DiaryEntryEntity(
            id = "d1", legacyDescription = null, diaryReferenceId = null,
            significance = "NORMAL", eventDate = "2024-06-15",
            startTime = null, endTime = null,
            createdAt = "2024-06-15T12:00:00",
            userId = "u1", userName = "test"
        )
        val model = entity.toModel(null)
        assertNull(model.startTime)
        assertNull(model.endTime)
    }

    @Test
    fun testSportLogToModelWithNullTimesDoesNotCrash() {
        val entity = SportLogEntity(
            id = "sp1", name = "Running", measurement = 5.0,
            measurementUnit = "km", startTime = null, endTime = null,
            date = "2024-06-15", createdAt = "2024-06-15T10:00:00",
            notes = null, userId = "u1", userName = "test"
        )
        val model = entity.toModel()
        assertNull(model.startTime)
        assertNull(model.endTime)
        assertNotNull(model.date)
    }

    @Test
    fun testFoodLogToModelWithValidDateTime() {
        val entity = FoodLogEntity(
            id = "f1", carbohydrates = 50.0, kcal = 400,
            dateTime = "2024-06-15T12:00:00", foodItems = "Pasta",
            createdAt = "2024-06-15T12:00:00", notes = null,
            userId = "u1", userName = "test"
        )
        val model = entity.toModel()
        assertNotNull(model.dateTime)
        assertNotNull(model.createdAt)
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
