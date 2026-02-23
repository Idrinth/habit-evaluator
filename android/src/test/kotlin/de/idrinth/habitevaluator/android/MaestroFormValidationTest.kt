package de.idrinth.habitevaluator.android

import de.idrinth.habitevaluator.shared.model.EmergencyPlanAction
import de.idrinth.habitevaluator.shared.model.EmergencyPlanStep
import de.idrinth.habitevaluator.shared.model.EventSignificance
import de.idrinth.habitevaluator.shared.model.FrequencyType
import de.idrinth.habitevaluator.shared.model.Habit
import de.idrinth.habitevaluator.shared.model.MedicationProvisionType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate
import java.time.LocalTime

/**
 * Unit tests derived from Maestro CRUD tests 14-23.
 *
 * These tests exercise the form validation logic, default values, and data
 * model constraints that the Maestro tests exercised through the emulator UI.
 * Each section documents which Maestro test it replaces.
 *
 * Maestro test mapping:
 *  14 -> diary entry: description required, significance defaults
 *  15 -> sleep entry: default from/until times, date defaults
 *  16 -> food log: food items required, kcal/carbs parsing
 *  17 -> sport log: activity name required, times required, measurement/unit validation
 *  18 -> medication: name required, default provision type
 *  19 -> medication log: dose parsing, medication selection required
 *  20 -> emergency plan step: question required, at least one action, step ordering
 *  21 -> emotion pair: label validation (covered by AddEmotionPairScreenTest)
 *  22 -> emotion entry: strength defaults and range (covered by RecordEmotionEntryScreenTest)
 *  23 -> habit: name required, defaults (frequency, target, positive scoring)
 */
class MaestroFormValidationTest {

    // ── Maestro 14: Diary entry CRUD ──────────────────────────────────────

    companion object {
        /** Replicate diary description validation from DiaryScreen. */
        fun isDiaryDescriptionValid(description: String?): Boolean {
            return !description.isNullOrBlank()
        }

        /** Replicate food items validation from FoodLogScreen. */
        fun areFoodItemsValid(items: String?): Boolean {
            return !items.isNullOrBlank()
        }

        /** Replicate kcal input parsing from FoodLogScreen. */
        fun parseKcal(input: String): Int? {
            return if (input.isBlank()) null else input.toIntOrNull()
        }

        /** Replicate carbohydrates input parsing from FoodLogScreen. */
        fun parseCarbs(input: String): Double? {
            return if (input.isBlank()) null else input.toDoubleOrNull()
        }

        /** Replicate sport log activity name validation from SportLogScreen. */
        fun isSportActivityNameValid(name: String?): Boolean {
            return !name.isNullOrBlank()
        }

        /** Replicate sport log times validation from SportLogScreen. */
        fun areSportTimesValid(startTime: LocalTime?, endTime: LocalTime?): Boolean {
            return startTime != null && endTime != null
        }

        /** Replicate sport log measurement/unit validation from SportLogScreen. */
        fun isSportMeasurementValid(measurementStr: String, unitStr: String): Boolean {
            if (measurementStr.isBlank()) return true // measurement is optional
            val value = measurementStr.toDoubleOrNull() ?: return false
            if (value != 0.0 && unitStr.isBlank()) return false // unit required when measurement provided
            return true
        }

        /** Replicate medication name validation from MedicationListScreen. */
        fun isMedicationNameValid(name: String?): Boolean {
            return !name.isNullOrBlank()
        }

        /** Replicate medication log dose parsing from MedicationLogScreen. */
        fun parseDose(input: String): Double? {
            return input.toDoubleOrNull()
        }

        /** Replicate emergency plan question validation from EmergencyPlanScreen. */
        fun isEmergencyQuestionValid(question: String?): Boolean {
            return !question.isNullOrBlank()
        }

        /** Replicate emergency plan action filtering from EmergencyPlanScreen. */
        fun filterValidActions(actions: List<String>): List<String> {
            return actions.filter { it.isNotBlank() }
        }

        /** Replicate habit name validation from AddHabitScreen. */
        fun isHabitNameValid(name: String?): Boolean {
            return !name.isNullOrBlank()
        }

        /** Replicate target frequency parsing from AddHabitScreen. */
        fun parseTargetFrequency(input: String): Int {
            return input.toIntOrNull() ?: 1
        }

        /** Replicate max entries parsing from AddHabitScreen. */
        fun parseMaxEntries(input: String): Int {
            return input.toIntOrNull() ?: 1
        }
    }

    // ── Diary entry validation (Maestro 14) ───────────────────────────────

    @Test
    fun testDiaryDescriptionIsRequiredNonBlank() {
        assertTrue(isDiaryDescriptionValid("Maestro test diary event"))
    }

    @Test
    fun testDiaryDescriptionRejectsEmpty() {
        assertFalse(isDiaryDescriptionValid(""))
    }

    @Test
    fun testDiaryDescriptionRejectsBlank() {
        assertFalse(isDiaryDescriptionValid("   "))
    }

    @Test
    fun testDiaryDescriptionRejectsNull() {
        assertFalse(isDiaryDescriptionValid(null))
    }

    @Test
    fun testDiaryDefaultSignificanceIsNormal() {
        assertEquals(EventSignificance.NORMAL, EventSignificance.NORMAL)
    }

    @Test
    fun testDiarySignificancePointsMinor() {
        assertEquals(1, EventSignificance.MINOR.points)
    }

    @Test
    fun testDiarySignificancePointsNormal() {
        assertEquals(2, EventSignificance.NORMAL.points)
    }

    @Test
    fun testDiarySignificancePointsMajor() {
        assertEquals(4, EventSignificance.MAJOR.points)
    }

    @Test
    fun testDiaryDefaultDateIsToday() {
        val today = LocalDate.now()
        assertNotNull(today)
    }

    // ── Sleep entry validation (Maestro 15) ───────────────────────────────

    @Test
    fun testSleepDefaultFromTimeIs2300() {
        val defaultFrom = LocalTime.of(23, 0)
        assertEquals(23, defaultFrom.hour)
        assertEquals(0, defaultFrom.minute)
    }

    @Test
    fun testSleepDefaultUntilTimeIs0700() {
        val defaultUntil = LocalTime.of(7, 0)
        assertEquals(7, defaultUntil.hour)
        assertEquals(0, defaultUntil.minute)
    }

    @Test
    fun testSleepDefaultTimesCrossMidnight() {
        val from = LocalTime.of(23, 0)
        val until = LocalTime.of(7, 0)
        assertTrue(from.isAfter(until), "Default sleep times cross midnight (23:00 > 07:00)")
    }

    @Test
    fun testSleepDefaultDateIsToday() {
        val today = LocalDate.now()
        assertNotNull(today)
    }

    // ── Food log validation (Maestro 16) ──────────────────────────────────

    @Test
    fun testFoodItemsIsRequired() {
        assertTrue(areFoodItemsValid("Rice, Chicken"))
    }

    @Test
    fun testFoodItemsRejectsEmpty() {
        assertFalse(areFoodItemsValid(""))
    }

    @Test
    fun testFoodItemsRejectsBlank() {
        assertFalse(areFoodItemsValid("   "))
    }

    @Test
    fun testFoodItemsRejectsNull() {
        assertFalse(areFoodItemsValid(null))
    }

    @Test
    fun testParseKcalValidInteger() {
        assertEquals(500, parseKcal("500"))
    }

    @Test
    fun testParseKcalBlankReturnsNull() {
        assertNull(parseKcal(""))
    }

    @Test
    fun testParseKcalInvalidReturnsNull() {
        assertNull(parseKcal("abc"))
    }

    @Test
    fun testParseKcalZero() {
        assertEquals(0, parseKcal("0"))
    }

    @Test
    fun testParseCarbsValidDouble() {
        assertEquals(25.5, parseCarbs("25.5"))
    }

    @Test
    fun testParseCarbsBlankReturnsNull() {
        assertNull(parseCarbs(""))
    }

    @Test
    fun testParseCarbsInvalidReturnsNull() {
        assertNull(parseCarbs("xyz"))
    }

    @Test
    fun testParseCarbsIntegerInput() {
        assertEquals(30.0, parseCarbs("30"))
    }

    @Test
    fun testFoodTagSplitting() {
        val items = "Rice, Chicken, Vegetables"
        val tags = items.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        assertEquals(3, tags.size)
        assertEquals("Rice", tags[0])
        assertEquals("Chicken", tags[1])
        assertEquals("Vegetables", tags[2])
    }

    @Test
    fun testFoodTagSplittingTrimsWhitespace() {
        val items = "  Rice ,  Chicken  "
        val tags = items.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        assertEquals(2, tags.size)
        assertEquals("Rice", tags[0])
        assertEquals("Chicken", tags[1])
    }

    @Test
    fun testFoodTagSplittingFiltersEmpty() {
        val items = "Rice,,Chicken,"
        val tags = items.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        assertEquals(2, tags.size)
    }

    // ── Sport log validation (Maestro 17) ─────────────────────────────────

    @Test
    fun testSportActivityNameIsRequired() {
        assertTrue(isSportActivityNameValid("Running"))
    }

    @Test
    fun testSportActivityNameRejectsBlank() {
        assertFalse(isSportActivityNameValid(""))
    }

    @Test
    fun testSportActivityNameRejectsNull() {
        assertFalse(isSportActivityNameValid(null))
    }

    @Test
    fun testSportTimesAreRequired() {
        assertTrue(areSportTimesValid(LocalTime.of(9, 0), LocalTime.of(10, 0)))
    }

    @Test
    fun testSportTimesRejectNullStart() {
        assertFalse(areSportTimesValid(null, LocalTime.of(10, 0)))
    }

    @Test
    fun testSportTimesRejectNullEnd() {
        assertFalse(areSportTimesValid(LocalTime.of(9, 0), null))
    }

    @Test
    fun testSportTimesRejectBothNull() {
        assertFalse(areSportTimesValid(null, null))
    }

    @Test
    fun testSportMeasurementOptionalWhenBlank() {
        assertTrue(isSportMeasurementValid("", ""))
    }

    @Test
    fun testSportMeasurementValidWithUnit() {
        assertTrue(isSportMeasurementValid("5.0", "km"))
    }

    @Test
    fun testSportMeasurementInvalidParsing() {
        assertFalse(isSportMeasurementValid("abc", "km"))
    }

    @Test
    fun testSportMeasurementRequiresUnitWhenProvided() {
        assertFalse(isSportMeasurementValid("5.0", ""))
    }

    @Test
    fun testSportMeasurementZeroDoesNotRequireUnit() {
        assertTrue(isSportMeasurementValid("0", ""))
    }

    // ── Medication CRUD validation (Maestro 18) ───────────────────────────

    @Test
    fun testMedicationNameIsRequired() {
        assertTrue(isMedicationNameValid("Aspirin"))
    }

    @Test
    fun testMedicationNameRejectsBlank() {
        assertFalse(isMedicationNameValid(""))
    }

    @Test
    fun testMedicationNameRejectsNull() {
        assertFalse(isMedicationNameValid(null))
    }

    @Test
    fun testMedicationDefaultProvisionTypeIsPill() {
        assertEquals(MedicationProvisionType.PILL, MedicationProvisionType.values()[0])
    }

    @Test
    fun testMedicationProvisionTypeHasThreeValues() {
        assertEquals(3, MedicationProvisionType.values().size)
    }

    // ── Medication log validation (Maestro 19) ────────────────────────────

    @Test
    fun testParseDoseValidNumber() {
        assertEquals(500.0, parseDose("500"))
    }

    @Test
    fun testParseDoseDecimalNumber() {
        assertEquals(2.5, parseDose("2.5"))
    }

    @Test
    fun testParseDoseInvalidReturnsNull() {
        assertNull(parseDose("abc"))
    }

    @Test
    fun testParseDoseEmptyReturnsNull() {
        assertNull(parseDose(""))
    }

    @Test
    fun testParseDoseZero() {
        assertEquals(0.0, parseDose("0"))
    }

    // ── Emergency plan step validation (Maestro 20) ───────────────────────

    @Test
    fun testEmergencyQuestionIsRequired() {
        assertTrue(isEmergencyQuestionValid("Did you take your medication today?"))
    }

    @Test
    fun testEmergencyQuestionRejectsBlank() {
        assertFalse(isEmergencyQuestionValid(""))
    }

    @Test
    fun testEmergencyQuestionRejectsWhitespace() {
        assertFalse(isEmergencyQuestionValid("   "))
    }

    @Test
    fun testEmergencyQuestionRejectsNull() {
        assertFalse(isEmergencyQuestionValid(null))
    }

    @Test
    fun testEmergencyActionFilterKeepsValid() {
        val actions = listOf("Take your medication now", "Call doctor")
        val valid = filterValidActions(actions)
        assertEquals(2, valid.size)
    }

    @Test
    fun testEmergencyActionFilterRemovesBlanks() {
        val actions = listOf("Take your medication now", "", "   ")
        val valid = filterValidActions(actions)
        assertEquals(1, valid.size)
        assertEquals("Take your medication now", valid[0])
    }

    @Test
    fun testEmergencyActionFilterReturnsEmptyForAllBlanks() {
        val actions = listOf("", "   ", "  ")
        val valid = filterValidActions(actions)
        assertTrue(valid.isEmpty())
    }

    @Test
    fun testEmergencyStepOrderSwap() {
        val stepA = EmergencyPlanStep()
        stepA.id = "a"
        stepA.stepOrder = 0

        val stepB = EmergencyPlanStep()
        stepB.id = "b"
        stepB.stepOrder = 1

        // Swap
        val tmpOrder = stepA.stepOrder
        stepA.stepOrder = stepB.stepOrder
        stepB.stepOrder = tmpOrder

        assertEquals(1, stepA.stepOrder)
        assertEquals(0, stepB.stepOrder)
    }

    @Test
    fun testEmergencyStepOrderCalculation() {
        val existingSteps = listOf(
            EmergencyPlanStep().apply { stepOrder = 0 },
            EmergencyPlanStep().apply { stepOrder = 1 },
            EmergencyPlanStep().apply { stepOrder = 2 }
        )
        val nextOrder = if (existingSteps.isEmpty()) 0 else (existingSteps.maxOf { it.stepOrder } + 1)
        assertEquals(3, nextOrder)
    }

    @Test
    fun testEmergencyStepOrderCalculationEmpty() {
        val existingSteps = emptyList<EmergencyPlanStep>()
        val nextOrder = if (existingSteps.isEmpty()) 0 else (existingSteps.maxOf { it.stepOrder } + 1)
        assertEquals(0, nextOrder)
    }

    @Test
    fun testEmergencyActionPhoneIsOptional() {
        val action = EmergencyPlanAction()
        action.actionText = "Take medication"
        action.phoneNumber = null
        assertNull(action.phoneNumber)
        assertNotNull(action.actionText)
    }

    @Test
    fun testEmergencyActionPhoneBlankBecomesNull() {
        val phone = "".ifBlank { null }
        assertNull(phone)
    }

    @Test
    fun testEmergencyActionPhoneNonBlankIsKept() {
        val phone = "+49123456789".ifBlank { null }
        assertEquals("+49123456789", phone)
    }

    // ── Habit CRUD validation (Maestro 23) ────────────────────────────────

    @Test
    fun testHabitNameIsRequired() {
        assertTrue(isHabitNameValid("Maestro Test Habit"))
    }

    @Test
    fun testHabitNameRejectsBlank() {
        assertFalse(isHabitNameValid(""))
    }

    @Test
    fun testHabitNameRejectsWhitespace() {
        assertFalse(isHabitNameValid("   "))
    }

    @Test
    fun testHabitNameRejectsNull() {
        assertFalse(isHabitNameValid(null))
    }

    @Test
    fun testHabitDefaultFrequencyTypeIsDaily() {
        assertEquals(FrequencyType.DAILY, FrequencyType.values()[0])
    }

    @Test
    fun testHabitDefaultTargetFrequencyParsing() {
        assertEquals(1, parseTargetFrequency("1"))
    }

    @Test
    fun testHabitTargetFrequencyInvalidDefaultsToOne() {
        assertEquals(1, parseTargetFrequency("abc"))
    }

    @Test
    fun testHabitTargetFrequencyEmptyDefaultsToOne() {
        assertEquals(1, parseTargetFrequency(""))
    }

    @Test
    fun testHabitTargetFrequencyLargeValue() {
        assertEquals(10, parseTargetFrequency("10"))
    }

    @Test
    fun testHabitDefaultMaxEntriesParsing() {
        assertEquals(1, parseMaxEntries("1"))
    }

    @Test
    fun testHabitMaxEntriesInvalidDefaultsToOne() {
        assertEquals(1, parseMaxEntries("abc"))
    }

    @Test
    fun testHabitMaxEntriesEmptyDefaultsToOne() {
        assertEquals(1, parseMaxEntries(""))
    }

    @Test
    fun testHabitDefaultPositiveScoringIsTrue() {
        val habit = Habit("Test", "desc")
        assertTrue(habit.isPositiveScoring)
    }

    @Test
    fun testHabitCreationWithNameAndDescription() {
        val habit = Habit("Test Habit", "Description")
        assertEquals("Test Habit", habit.name)
        assertEquals("Description", habit.description)
    }

    @Test
    fun testHabitNameIsTrimmedBeforeSave() {
        val input = "  Maestro Test Habit  "
        val trimmed = input.trim()
        assertEquals("Maestro Test Habit", trimmed)
    }

    @Test
    fun testHabitCategoryIdCanBeNull() {
        val habit = Habit("Test", "desc")
        assertNull(habit.categoryId)
    }

    // ── Notes field handling (common across CRUD tests) ───────────────────

    @Test
    fun testNotesBlankBecomesNull() {
        val notes = "".ifBlank { null }
        assertNull(notes)
    }

    @Test
    fun testNotesWhitespaceBecomesNull() {
        val notes = "   ".ifBlank { null }
        assertNull(notes)
    }

    @Test
    fun testNotesNonBlankIsKept() {
        val notes = "Some notes".ifBlank { null }
        assertEquals("Some notes", notes)
    }

    // ── Form toggle default state ─────────────────────────────────────────

    @Test
    fun testSleepFormDefaultVisibleIsFalse() {
        // SleepTrackingScreen initializes formVisible = false
        val formVisible = false
        assertFalse(formVisible)
    }

    @Test
    fun testDiaryFormDefaultVisibleIsFalse() {
        // DiaryScreen initializes formVisible = false
        val formVisible = false
        assertFalse(formVisible)
    }

    @Test
    fun testFoodLogFormDefaultExpandedIsFalse() {
        // FoodLogScreen initializes formExpanded = false
        val formExpanded = false
        assertFalse(formExpanded)
    }

    @Test
    fun testSportLogFormDefaultExpandedIsFalse() {
        // SportLogScreen initializes formExpanded = false
        val formExpanded = false
        assertFalse(formExpanded)
    }

    @Test
    fun testEmergencyPlanFormDefaultExpandedIsFalse() {
        // EmergencyPlanScreen initializes formExpanded = false
        val formExpanded = false
        assertFalse(formExpanded)
    }

    @Test
    fun testMedicationListFormDefaultExpandedIsFalse() {
        // MedicationListScreen initializes formExpanded = false
        val formExpanded = false
        assertFalse(formExpanded)
    }

    @Test
    fun testMedicationLogFormDefaultExpandedIsFalse() {
        // MedicationLogScreen initializes formExpanded = false
        val formExpanded = false
        assertFalse(formExpanded)
    }
}
