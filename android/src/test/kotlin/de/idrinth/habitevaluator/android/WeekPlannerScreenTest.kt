package de.idrinth.habitevaluator.android

import de.idrinth.habitevaluator.shared.model.PlannerGroup
import de.idrinth.habitevaluator.shared.model.WeekPlannerSlot
import de.idrinth.habitevaluator.shared.service.DayPlannerService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

/**
 * Tests for logic extracted from WeekPlannerScreen.
 * Validates day navigation, slot filtering, hour formatting, and summary calculations.
 */
class WeekPlannerScreenTest {

    companion object {
        /**
         * Replication of day slots filtering from WeekPlannerScreen.
         * Filters all slots to only those matching a specific day of week.
         */
        fun filterSlotsByDay(slots: List<WeekPlannerSlot>, dayOfWeek: Int): List<WeekPlannerSlot> {
            return slots.filter { it.dayOfWeek == dayOfWeek }
        }

        /**
         * Replication of hour formatting from WeekPlannerScreen.
         * Formats an hour as "HH:00".
         */
        fun formatHour(hour: Int): String {
            return String.format("%02d:00", hour)
        }

        /**
         * Replication of group names display from WeekPlannerScreen.
         * Sorts group names and joins them with comma separator.
         */
        fun formatSlotGroupNames(slot: WeekPlannerSlot): List<String>? {
            return slot.groups?.mapNotNull { it.name }?.sorted()
        }

        /**
         * Replication of day name display from WeekPlannerScreen.
         * Returns the full display name of a day of week.
         */
        fun getDayName(dayOfWeek: Int): String {
            return DayOfWeek.of(dayOfWeek).getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        }

        /**
         * Checks if the selected day can navigate forward.
         */
        fun canNavigateForward(selectedDay: Int): Boolean {
            return selectedDay < 7
        }

        /**
         * Checks if the selected day can navigate backward.
         */
        fun canNavigateBackward(selectedDay: Int): Boolean {
            return selectedDay > 1
        }

        /**
         * Counts filled slots per day for a given day of week.
         */
        fun countFilledSlotsForDay(allSlots: List<WeekPlannerSlot>, dayOfWeek: Int): Int {
            return allSlots.count {
                it.dayOfWeek == dayOfWeek && it.groups != null && it.groups.isNotEmpty()
            }
        }
    }

    // --- Day slot filtering tests ---

    @Test
    fun testFilterSlotsByDayMatching() {
        val s1 = WeekPlannerSlot(1, 9).apply { id = "s1" }
        val s2 = WeekPlannerSlot(1, 10).apply { id = "s2" }
        val s3 = WeekPlannerSlot(2, 9).apply { id = "s3" }

        val result = filterSlotsByDay(listOf(s1, s2, s3), 1)
        assertEquals(2, result.size)
    }

    @Test
    fun testFilterSlotsByDayNoMatch() {
        val s1 = WeekPlannerSlot(1, 9).apply { id = "s1" }

        val result = filterSlotsByDay(listOf(s1), 3)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFilterSlotsByDayEmpty() {
        val result = filterSlotsByDay(emptyList(), 1)
        assertTrue(result.isEmpty())
    }

    // --- Hour formatting tests ---

    @Test
    fun testFormatHourMidnight() {
        assertEquals("00:00", formatHour(0))
    }

    @Test
    fun testFormatHourSingleDigit() {
        assertEquals("09:00", formatHour(9))
    }

    @Test
    fun testFormatHourDoubleDigit() {
        assertEquals("14:00", formatHour(14))
    }

    @Test
    fun testFormatHourEndOfDay() {
        assertEquals("23:00", formatHour(23))
    }

    @Test
    fun testFormatHourNoon() {
        assertEquals("12:00", formatHour(12))
    }

    // --- Group names formatting tests ---

    @Test
    fun testFormatSlotGroupNamesSingleGroup() {
        val group = PlannerGroup("Exercise")
        group.id = "g1"
        val slot = WeekPlannerSlot(1, 9)
        slot.groups = hashSetOf(group)

        val result = formatSlotGroupNames(slot)
        assertNotNull(result)
        assertEquals(1, result!!.size)
        assertEquals("Exercise", result[0])
    }

    @Test
    fun testFormatSlotGroupNamesMultipleGroupsSorted() {
        val g1 = PlannerGroup("Yoga")
        g1.id = "g1"
        val g2 = PlannerGroup("Cardio")
        g2.id = "g2"
        val slot = WeekPlannerSlot(1, 9)
        slot.groups = hashSetOf(g1, g2)

        val result = formatSlotGroupNames(slot)
        assertNotNull(result)
        assertEquals(2, result!!.size)
        assertEquals("Cardio", result[0])
        assertEquals("Yoga", result[1])
    }

    @Test
    fun testFormatSlotGroupNamesNullGroups() {
        val slot = WeekPlannerSlot(1, 9)
        slot.groups = null

        val result = formatSlotGroupNames(slot)
        assertNull(result)
    }

    @Test
    fun testFormatSlotGroupNamesEmptyGroups() {
        val slot = WeekPlannerSlot(1, 9)
        slot.groups = hashSetOf()

        val result = formatSlotGroupNames(slot)
        assertNotNull(result)
        assertTrue(result!!.isEmpty())
    }

    // --- Day name tests ---

    @Test
    fun testGetDayNameMonday() {
        assertEquals("Monday", getDayName(1))
    }

    @Test
    fun testGetDayNameSunday() {
        assertEquals("Sunday", getDayName(7))
    }

    @Test
    fun testGetDayNameWednesday() {
        assertEquals("Wednesday", getDayName(3))
    }

    @Test
    fun testGetDayNameFriday() {
        assertEquals("Friday", getDayName(5))
    }

    // --- Day navigation tests ---

    @Test
    fun testCanNavigateForwardFromMonday() {
        assertTrue(canNavigateForward(1))
    }

    @Test
    fun testCanNavigateForwardFromSaturday() {
        assertTrue(canNavigateForward(6))
    }

    @Test
    fun testCannotNavigateForwardFromSunday() {
        assertFalse(canNavigateForward(7))
    }

    @Test
    fun testCanNavigateBackwardFromTuesday() {
        assertTrue(canNavigateBackward(2))
    }

    @Test
    fun testCanNavigateBackwardFromSunday() {
        assertTrue(canNavigateBackward(7))
    }

    @Test
    fun testCannotNavigateBackwardFromMonday() {
        assertFalse(canNavigateBackward(1))
    }

    // --- Filled slots counting tests ---

    @Test
    fun testCountFilledSlotsForDayWithGroups() {
        val group = PlannerGroup("Exercise")
        group.id = "g1"

        val s1 = WeekPlannerSlot(1, 9).apply { id = "s1"; groups = hashSetOf(group) }
        val s2 = WeekPlannerSlot(1, 10).apply { id = "s2"; groups = hashSetOf(group) }
        val s3 = WeekPlannerSlot(1, 11).apply { id = "s3"; groups = hashSetOf() }

        assertEquals(2, countFilledSlotsForDay(listOf(s1, s2, s3), 1))
    }

    @Test
    fun testCountFilledSlotsForDayNoSlots() {
        assertEquals(0, countFilledSlotsForDay(emptyList(), 1))
    }

    @Test
    fun testCountFilledSlotsForDayNullGroups() {
        val s1 = WeekPlannerSlot(1, 9).apply { id = "s1"; groups = null }

        assertEquals(0, countFilledSlotsForDay(listOf(s1), 1))
    }

    @Test
    fun testCountFilledSlotsForDayDifferentDays() {
        val group = PlannerGroup("Exercise")
        group.id = "g1"

        val s1 = WeekPlannerSlot(1, 9).apply { id = "s1"; groups = hashSetOf(group) }
        val s2 = WeekPlannerSlot(2, 9).apply { id = "s2"; groups = hashSetOf(group) }

        assertEquals(1, countFilledSlotsForDay(listOf(s1, s2), 1))
        assertEquals(1, countFilledSlotsForDay(listOf(s1, s2), 2))
    }

    // --- DayPlannerService integration tests ---

    @Test
    fun testWeekSlotSummaryEmpty() {
        val service = DayPlannerService()
        val summary = service.getWeekSlotSummary(emptyList())
        assertEquals(0, summary[0])
        assertEquals(168, summary[1])
    }

    @Test
    fun testWeekSlotSummaryWithSlots() {
        val group = PlannerGroup("Exercise")
        group.id = "g1"

        val slots = listOf(
            WeekPlannerSlot(1, 9).apply { id = "s1"; groups = hashSetOf(group) },
            WeekPlannerSlot(1, 10).apply { id = "s2"; groups = hashSetOf(group) },
            WeekPlannerSlot(2, 14).apply { id = "s3"; groups = hashSetOf(group) }
        )

        val service = DayPlannerService()
        val summary = service.getWeekSlotSummary(slots)
        assertEquals(3, summary[0])
        assertEquals(168, summary[1])
    }

    @Test
    fun testWeekSlotSummaryTotalIsAlways168() {
        val service = DayPlannerService()
        val summary = service.getWeekSlotSummary(emptyList())
        assertEquals(168, summary[1])
    }

    // --- Screen route test ---

    @Test
    fun testWeekPlannerScreenRoute() {
        assertEquals(
            "week_planner",
            de.idrinth.habitevaluator.android.ui.navigation.Screen.WeekPlanner.route
        )
    }
}
