package de.idrinth.habitevaluator.android

import android.content.Context
import android.content.Intent
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class PlannerConfirmationReceiverTest {

    private lateinit var receiver: PlannerConfirmationReceiver

    @BeforeEach
    fun setUp() {
        receiver = PlannerConfirmationReceiver()
    }

    @Test
    fun testActionConfirmConstant() {
        assertEquals(
            "de.idrinth.habitevaluator.PLANNER_CONFIRM",
            PlannerConfirmationReceiver.ACTION_CONFIRM
        )
    }

    @Test
    fun testActionDenyConstant() {
        assertEquals(
            "de.idrinth.habitevaluator.PLANNER_DENY",
            PlannerConfirmationReceiver.ACTION_DENY
        )
    }

    @Test
    fun testActionTimeoutConstant() {
        assertEquals(
            "de.idrinth.habitevaluator.PLANNER_TIMEOUT",
            PlannerConfirmationReceiver.ACTION_TIMEOUT
        )
    }

    @Test
    fun testExtraSlotIdConstant() {
        assertEquals("planner_slot_id", PlannerConfirmationReceiver.EXTRA_SLOT_ID)
    }

    @Test
    fun testExtraActivityIdConstant() {
        assertEquals("planner_activity_id", PlannerConfirmationReceiver.EXTRA_ACTIVITY_ID)
    }

    @Test
    fun testExtraGroupIdConstant() {
        assertEquals("planner_group_id", PlannerConfirmationReceiver.EXTRA_GROUP_ID)
    }

    @Test
    fun testExtraUserIdConstant() {
        assertEquals("planner_user_id", PlannerConfirmationReceiver.EXTRA_USER_ID)
    }

    @Test
    fun testExtraNotificationIdConstant() {
        assertEquals("planner_notification_id", PlannerConfirmationReceiver.EXTRA_NOTIFICATION_ID)
    }

    @Test
    fun testAllActionConstantsAreUnique() {
        val actions = setOf(
            PlannerConfirmationReceiver.ACTION_CONFIRM,
            PlannerConfirmationReceiver.ACTION_DENY,
            PlannerConfirmationReceiver.ACTION_TIMEOUT
        )
        assertEquals(3, actions.size)
    }

    @Test
    fun testAllExtraConstantsAreUnique() {
        val extras = setOf(
            PlannerConfirmationReceiver.EXTRA_SLOT_ID,
            PlannerConfirmationReceiver.EXTRA_ACTIVITY_ID,
            PlannerConfirmationReceiver.EXTRA_GROUP_ID,
            PlannerConfirmationReceiver.EXTRA_USER_ID,
            PlannerConfirmationReceiver.EXTRA_NOTIFICATION_ID
        )
        assertEquals(5, extras.size)
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
    fun testOnReceiveIgnoresNullAction() {
        val context = mock(Context::class.java)
        val intent = mock(Intent::class.java)
        `when`(intent.action).thenReturn(null)

        // Should not throw
        receiver.onReceive(context, intent)
    }

    @Test
    fun testOnReceiveWithMissingSlotIdDoesNothing() {
        val context = mock(Context::class.java)
        val intent = mock(Intent::class.java)
        `when`(intent.action).thenReturn(PlannerConfirmationReceiver.ACTION_CONFIRM)
        `when`(intent.getStringExtra(PlannerConfirmationReceiver.EXTRA_SLOT_ID)).thenReturn(null)

        // Should not throw - missing slot ID is silently handled
        receiver.onReceive(context, intent)
    }

    @Test
    fun testOnReceiveWithMissingActivityIdDoesNothing() {
        val context = mock(Context::class.java)
        val intent = mock(Intent::class.java)
        `when`(intent.action).thenReturn(PlannerConfirmationReceiver.ACTION_DENY)
        `when`(intent.getStringExtra(PlannerConfirmationReceiver.EXTRA_SLOT_ID)).thenReturn("slot-1")
        `when`(intent.getStringExtra(PlannerConfirmationReceiver.EXTRA_ACTIVITY_ID)).thenReturn(null)

        // Should not throw - missing activity ID is silently handled
        receiver.onReceive(context, intent)
    }

    @Test
    fun testOnReceiveWithMissingGroupIdDoesNothing() {
        val context = mock(Context::class.java)
        val intent = mock(Intent::class.java)
        `when`(intent.action).thenReturn(PlannerConfirmationReceiver.ACTION_TIMEOUT)
        `when`(intent.getStringExtra(PlannerConfirmationReceiver.EXTRA_SLOT_ID)).thenReturn("slot-1")
        `when`(intent.getStringExtra(PlannerConfirmationReceiver.EXTRA_ACTIVITY_ID)).thenReturn("act-1")
        `when`(intent.getStringExtra(PlannerConfirmationReceiver.EXTRA_GROUP_ID)).thenReturn(null)

        // Should not throw - missing group ID is silently handled
        receiver.onReceive(context, intent)
    }

    @Test
    fun testOnReceiveWithMissingUserIdDoesNothing() {
        val context = mock(Context::class.java)
        val intent = mock(Intent::class.java)
        `when`(intent.action).thenReturn(PlannerConfirmationReceiver.ACTION_CONFIRM)
        `when`(intent.getStringExtra(PlannerConfirmationReceiver.EXTRA_SLOT_ID)).thenReturn("slot-1")
        `when`(intent.getStringExtra(PlannerConfirmationReceiver.EXTRA_ACTIVITY_ID)).thenReturn("act-1")
        `when`(intent.getStringExtra(PlannerConfirmationReceiver.EXTRA_GROUP_ID)).thenReturn("group-1")
        `when`(intent.getStringExtra(PlannerConfirmationReceiver.EXTRA_USER_ID)).thenReturn(null)

        // Should not throw - missing user ID is silently handled
        receiver.onReceive(context, intent)
    }

    @Test
    fun testActionsDoNotOverlapWithReminderReceiver() {
        assertNotEquals(PlannerConfirmationReceiver.ACTION_CONFIRM, ReminderReceiver.ACTION_REMINDER)
        assertNotEquals(PlannerConfirmationReceiver.ACTION_DENY, ReminderReceiver.ACTION_REMINDER)
        assertNotEquals(PlannerConfirmationReceiver.ACTION_TIMEOUT, ReminderReceiver.ACTION_REMINDER)
    }
}
