package de.idrinth.habitevaluator.android

import android.content.Context
import android.content.Intent
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class ReminderReceiverTest {

    private lateinit var receiver: ReminderReceiver

    @BeforeEach
    fun setUp() {
        receiver = ReminderReceiver()
    }

    @Test
    fun testActionReminderConstant() {
        assertEquals("de.idrinth.habitevaluator.REMINDER", ReminderReceiver.ACTION_REMINDER)
    }

    @Test
    fun testExtraTypeConstant() {
        assertEquals("reminder_type", ReminderReceiver.EXTRA_TYPE)
    }

    @Test
    fun testTypeSleepConstant() {
        assertEquals("sleep", ReminderReceiver.TYPE_SLEEP)
    }

    @Test
    fun testTypeDiaryConstant() {
        assertEquals("diary", ReminderReceiver.TYPE_DIARY)
    }

    @Test
    fun testTypeGratitudeConstant() {
        assertEquals("gratitude", ReminderReceiver.TYPE_GRATITUDE)
    }

    @Test
    fun testTypeEmotionConstant() {
        assertEquals("emotion", ReminderReceiver.TYPE_EMOTION)
    }

    @Test
    fun testTypePlannerConstant() {
        assertEquals("planner", ReminderReceiver.TYPE_PLANNER)
    }

    @Test
    fun testExtraPlannerDayConstant() {
        assertEquals("planner_day_of_week", ReminderReceiver.EXTRA_PLANNER_DAY)
    }

    @Test
    fun testExtraPlannerHourConstant() {
        assertEquals("planner_hour", ReminderReceiver.EXTRA_PLANNER_HOUR)
    }

    @Test
    fun testAllTypeConstantsAreUnique() {
        val types = setOf(
            ReminderReceiver.TYPE_SLEEP,
            ReminderReceiver.TYPE_DIARY,
            ReminderReceiver.TYPE_GRATITUDE,
            ReminderReceiver.TYPE_EMOTION,
            ReminderReceiver.TYPE_PLANNER
        )
        assertEquals(5, types.size)
    }

    @Test
    fun testActionReminderIsNotEmpty() {
        assertFalse(ReminderReceiver.ACTION_REMINDER.isEmpty())
    }

    @Test
    fun testExtraTypeIsNotEmpty() {
        assertFalse(ReminderReceiver.EXTRA_TYPE.isEmpty())
    }

    @Test
    fun testCanInstantiate() {
        assertNotNull(receiver)
    }

    @Test
    fun testOnReceiveIgnoresUnknownAction() {
        val context = mock(Context::class.java)
        val intent = mock(Intent::class.java)
        `when`(intent.action).thenReturn("com.example.UNKNOWN_ACTION")

        // Should not throw - unknown actions are silently ignored
        receiver.onReceive(context, intent)
    }

    @Test
    fun testOnReceiveWithNullTypeDoesNothing() {
        val context = mock(Context::class.java)
        val intent = mock(Intent::class.java)
        `when`(intent.action).thenReturn(ReminderReceiver.ACTION_REMINDER)
        `when`(intent.getStringExtra(ReminderReceiver.EXTRA_TYPE)).thenReturn(null)

        // Should not throw - null type is silently handled
        receiver.onReceive(context, intent)
    }

    @Test
    fun testOnReceiveWithEmptyActionDoesNothing() {
        val context = mock(Context::class.java)
        val intent = mock(Intent::class.java)
        `when`(intent.action).thenReturn("")

        // Should not throw
        receiver.onReceive(context, intent)
    }

    @Test
    fun testOnReceiveWithNullActionDoesNothing() {
        val context = mock(Context::class.java)
        val intent = mock(Intent::class.java)
        `when`(intent.action).thenReturn(null)

        // Should not throw
        receiver.onReceive(context, intent)
    }
}
