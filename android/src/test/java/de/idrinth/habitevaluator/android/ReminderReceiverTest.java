package de.idrinth.habitevaluator.android;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReminderReceiverTest {

    @Test
    void testActionReminderConstant() {
        assertEquals("de.idrinth.habitevaluator.REMINDER", ReminderReceiver.ACTION_REMINDER);
    }

    @Test
    void testExtraTypeConstant() {
        assertEquals("reminder_type", ReminderReceiver.EXTRA_TYPE);
    }

    @Test
    void testTypeSleepConstant() {
        assertEquals("sleep", ReminderReceiver.TYPE_SLEEP);
    }

    @Test
    void testTypeDiaryConstant() {
        assertEquals("diary", ReminderReceiver.TYPE_DIARY);
    }

    @Test
    void testTypeEmotionConstant() {
        assertEquals("emotion", ReminderReceiver.TYPE_EMOTION);
    }

    @Test
    void testAllTypeConstantsAreUnique() {
        assertNotEquals(ReminderReceiver.TYPE_SLEEP, ReminderReceiver.TYPE_DIARY);
        assertNotEquals(ReminderReceiver.TYPE_SLEEP, ReminderReceiver.TYPE_EMOTION);
        assertNotEquals(ReminderReceiver.TYPE_DIARY, ReminderReceiver.TYPE_EMOTION);
    }

    @Test
    void testActionReminderIsNotEmpty() {
        assertFalse(ReminderReceiver.ACTION_REMINDER.isEmpty());
    }

    @Test
    void testExtraTypeIsNotEmpty() {
        assertFalse(ReminderReceiver.EXTRA_TYPE.isEmpty());
    }

    @Test
    void testCanInstantiate() {
        ReminderReceiver receiver = new ReminderReceiver();
        assertNotNull(receiver);
    }
}
