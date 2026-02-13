package de.idrinth.habitevaluator.android;

import org.junit.Test;

import static org.junit.Assert.*;

public class ReminderReceiverTest {

    @Test
    public void testActionReminderConstant() {
        assertEquals("de.idrinth.habitevaluator.REMINDER", ReminderReceiver.ACTION_REMINDER);
    }

    @Test
    public void testExtraTypeConstant() {
        assertEquals("reminder_type", ReminderReceiver.EXTRA_TYPE);
    }

    @Test
    public void testTypeSleepConstant() {
        assertEquals("sleep", ReminderReceiver.TYPE_SLEEP);
    }

    @Test
    public void testTypeDiaryConstant() {
        assertEquals("diary", ReminderReceiver.TYPE_DIARY);
    }

    @Test
    public void testTypeEmotionConstant() {
        assertEquals("emotion", ReminderReceiver.TYPE_EMOTION);
    }

    @Test
    public void testAllTypeConstantsAreUnique() {
        assertNotEquals(ReminderReceiver.TYPE_SLEEP, ReminderReceiver.TYPE_DIARY);
        assertNotEquals(ReminderReceiver.TYPE_SLEEP, ReminderReceiver.TYPE_EMOTION);
        assertNotEquals(ReminderReceiver.TYPE_DIARY, ReminderReceiver.TYPE_EMOTION);
    }

    @Test
    public void testActionReminderIsNotEmpty() {
        assertFalse(ReminderReceiver.ACTION_REMINDER.isEmpty());
    }

    @Test
    public void testExtraTypeIsNotEmpty() {
        assertFalse(ReminderReceiver.EXTRA_TYPE.isEmpty());
    }

    @Test
    public void testCanInstantiate() {
        ReminderReceiver receiver = new ReminderReceiver();
        assertNotNull(receiver);
    }
}
