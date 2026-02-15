package de.idrinth.habitevaluator.android;

import android.content.Context;
import android.content.Intent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReminderReceiverTest {

    private ReminderReceiver receiver;

    @BeforeEach
    void setUp() {
        receiver = new ReminderReceiver();
    }

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
        assertNotNull(receiver);
    }

    @Test
    void testOnReceiveIgnoresUnknownAction() {
        Context context = mock(Context.class);
        Intent intent = mock(Intent.class);
        when(intent.getAction()).thenReturn("com.example.UNKNOWN_ACTION");

        // Should not throw - unknown actions are silently ignored
        receiver.onReceive(context, intent);
    }

    @Test
    void testOnReceiveWithNullTypeDoesNothing() {
        Context context = mock(Context.class);
        Intent intent = mock(Intent.class);
        when(intent.getAction()).thenReturn(ReminderReceiver.ACTION_REMINDER);
        when(intent.getStringExtra(ReminderReceiver.EXTRA_TYPE)).thenReturn(null);

        // Should not throw - null type is silently handled
        receiver.onReceive(context, intent);
    }

    @Test
    void testOnReceiveWithEmptyActionDoesNothing() {
        Context context = mock(Context.class);
        Intent intent = mock(Intent.class);
        when(intent.getAction()).thenReturn("");

        // Should not throw
        receiver.onReceive(context, intent);
    }

    @Test
    void testOnReceiveWithNullActionDoesNothing() {
        Context context = mock(Context.class);
        Intent intent = mock(Intent.class);
        when(intent.getAction()).thenReturn(null);

        // Should not throw
        receiver.onReceive(context, intent);
    }
}
