package de.idrinth.habitevaluator.android

import android.app.NotificationManager
import android.content.Context
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.*

class NotificationHelperTest {

    @Test
    fun testChannelIdIsNotNull() {
        assertNotNull(NotificationHelper.CHANNEL_ID)
    }

    @Test
    fun testChannelIdIsNotEmpty() {
        assertFalse(NotificationHelper.CHANNEL_ID.isEmpty())
    }

    @Test
    fun testChannelIdValue() {
        assertEquals("habit_evaluator_reminders", NotificationHelper.CHANNEL_ID)
    }

    @Test
    fun testInputRemindersChannelIdIsNotEmpty() {
        assertFalse(NotificationHelper.CHANNEL_ID_INPUT_REMINDERS.isEmpty())
    }

    @Test
    fun testInputRemindersChannelIdValue() {
        assertEquals("habit_evaluator_input_reminders", NotificationHelper.CHANNEL_ID_INPUT_REMINDERS)
    }

    @Test
    fun testEmotionChannelIdIsNotEmpty() {
        assertFalse(NotificationHelper.CHANNEL_ID_EMOTION.isEmpty())
    }

    @Test
    fun testEmotionChannelIdValue() {
        assertEquals("habit_evaluator_emotion", NotificationHelper.CHANNEL_ID_EMOTION)
    }

    @Test
    fun testDayPlannerChannelIdIsNotEmpty() {
        assertFalse(NotificationHelper.CHANNEL_ID_DAY_PLANNER.isEmpty())
    }

    @Test
    fun testDayPlannerChannelIdValue() {
        assertEquals("habit_evaluator_day_planner", NotificationHelper.CHANNEL_ID_DAY_PLANNER)
    }

    @Test
    fun testAllChannelIdsAreUnique() {
        val ids = setOf(
            NotificationHelper.CHANNEL_ID,
            NotificationHelper.CHANNEL_ID_INPUT_REMINDERS,
            NotificationHelper.CHANNEL_ID_EMOTION,
            NotificationHelper.CHANNEL_ID_DAY_PLANNER
        )
        assertEquals(4, ids.size, "All channel IDs should be unique")
    }

    @Test
    fun testChannelIdsDoNotContainSpaces() {
        assertFalse(NotificationHelper.CHANNEL_ID.contains(" "), "CHANNEL_ID should not contain spaces")
        assertFalse(NotificationHelper.CHANNEL_ID_INPUT_REMINDERS.contains(" "), "CHANNEL_ID_INPUT_REMINDERS should not contain spaces")
        assertFalse(NotificationHelper.CHANNEL_ID_EMOTION.contains(" "), "CHANNEL_ID_EMOTION should not contain spaces")
        assertFalse(NotificationHelper.CHANNEL_ID_DAY_PLANNER.contains(" "), "CHANNEL_ID_DAY_PLANNER should not contain spaces")
    }

    @Test
    fun testPermissionHandlerReturnsNonNull() {
        assertNotNull(NotificationHelper.permissionHandler())
    }

    @Test
    fun testPermissionHandlerImplementsInterface() {
        assertTrue(NotificationHelper.permissionHandler() is NotificationPermissionHandler)
    }

    @Test
    fun testPermissionHandlerReturnsFreshInstances() {
        val handler1 = NotificationHelper.permissionHandler()
        val handler2 = NotificationHelper.permissionHandler()
        assertNotNull(handler1)
        assertNotNull(handler2)
    }

    @Test
    fun testEnsureNotificationChannelWithMockedContext() {
        val context = mock(Context::class.java)
        `when`(context.getString(anyInt())).thenReturn("Test Channel")
        val manager = mock(NotificationManager::class.java)
        `when`(context.getSystemService(Context.NOTIFICATION_SERVICE))
            .thenReturn(manager)

        // Should not throw regardless of flavor implementation
        NotificationHelper.ensureNotificationChannel(context)
    }

    @Test
    fun testEnsureNotificationChannelWithNullNotificationManager() {
        val context = mock(Context::class.java)
        `when`(context.getString(anyInt())).thenReturn("Test Channel")
        `when`(context.getSystemService(Context.NOTIFICATION_SERVICE)).thenReturn(null)

        NotificationHelper.ensureNotificationChannel(context)
        // Should not throw when manager is null
    }

    @Test
    fun testEnsureNotificationChannelCallsGetString() {
        val context = mock(Context::class.java)
        `when`(context.getString(anyInt())).thenReturn("Channel Name")
        `when`(context.getSystemService(Context.NOTIFICATION_SERVICE))
            .thenReturn(mock(NotificationManager::class.java))

        // Should not throw regardless of flavor implementation
        NotificationHelper.ensureNotificationChannel(context)
    }

    @Test
    fun testPermissionHandlerReturnsNewInstancesEachTime() {
        val handler1 = NotificationHelper.permissionHandler()
        val handler2 = NotificationHelper.permissionHandler()
        assertNotSame(handler1, handler2)
    }

    @Test
    fun testEnsureNotificationChannelCanBeCalledMultipleTimes() {
        val context = mock(Context::class.java)
        `when`(context.getString(anyInt())).thenReturn("Test Channel")
        `when`(context.getSystemService(Context.NOTIFICATION_SERVICE))
            .thenReturn(mock(NotificationManager::class.java))

        // Calling multiple times should be idempotent
        NotificationHelper.ensureNotificationChannel(context)
        NotificationHelper.ensureNotificationChannel(context)
    }
}
