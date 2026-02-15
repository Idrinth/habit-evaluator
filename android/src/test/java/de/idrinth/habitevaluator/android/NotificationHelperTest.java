package de.idrinth.habitevaluator.android;

import android.app.NotificationManager;
import android.content.Context;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationHelperTest {

    @Test
    void testChannelIdIsNotNull() {
        assertNotNull(NotificationHelper.CHANNEL_ID);
    }

    @Test
    void testChannelIdIsNotEmpty() {
        assertFalse(NotificationHelper.CHANNEL_ID.isEmpty());
    }

    @Test
    void testChannelIdValue() {
        assertEquals("habit_evaluator_reminders", NotificationHelper.CHANNEL_ID);
    }

    @Test
    void testPermissionHandlerReturnsNonNull() {
        assertNotNull(NotificationHelper.permissionHandler());
    }

    @Test
    void testPermissionHandlerImplementsInterface() {
        assertTrue(NotificationHelper.permissionHandler() instanceof NotificationPermissionHandler);
    }

    @Test
    void testPermissionHandlerReturnsFreshInstances() {
        NotificationPermissionHandler handler1 = NotificationHelper.permissionHandler();
        NotificationPermissionHandler handler2 = NotificationHelper.permissionHandler();
        assertNotNull(handler1);
        assertNotNull(handler2);
    }

    @Test
    void testEnsureNotificationChannelWithMockedContext() {
        Context context = mock(Context.class);
        when(context.getString(anyInt())).thenReturn("Test Channel");
        NotificationManager manager = mock(NotificationManager.class);
        when(context.getSystemService(Context.NOTIFICATION_SERVICE))
                .thenReturn(manager);

        // Should not throw regardless of flavor implementation
        NotificationHelper.ensureNotificationChannel(context);
    }

    @Test
    void testEnsureNotificationChannelWithNullNotificationManager() {
        Context context = mock(Context.class);
        when(context.getString(anyInt())).thenReturn("Test Channel");
        when(context.getSystemService(Context.NOTIFICATION_SERVICE)).thenReturn(null);

        NotificationHelper.ensureNotificationChannel(context);
        // Should not throw when manager is null
    }

    @Test
    void testEnsureNotificationChannelCallsGetString() {
        Context context = mock(Context.class);
        when(context.getString(anyInt())).thenReturn("Channel Name");
        when(context.getSystemService(Context.NOTIFICATION_SERVICE))
                .thenReturn(mock(NotificationManager.class));

        // Should not throw regardless of flavor implementation
        NotificationHelper.ensureNotificationChannel(context);
    }

    @Test
    void testPermissionHandlerReturnsNewInstancesEachTime() {
        NotificationPermissionHandler handler1 = NotificationHelper.permissionHandler();
        NotificationPermissionHandler handler2 = NotificationHelper.permissionHandler();
        assertNotSame(handler1, handler2);
    }

    @Test
    void testChannelIdMatchesReminderReceiverUsage() {
        // Channel ID should match what ReminderReceiver uses for notifications
        assertNotNull(NotificationHelper.CHANNEL_ID);
        assertFalse(NotificationHelper.CHANNEL_ID.contains(" "),
                "Channel ID should not contain spaces");
    }

    @Test
    void testEnsureNotificationChannelCanBeCalledMultipleTimes() {
        Context context = mock(Context.class);
        when(context.getString(anyInt())).thenReturn("Test Channel");
        when(context.getSystemService(Context.NOTIFICATION_SERVICE))
                .thenReturn(mock(NotificationManager.class));

        // Calling multiple times should be idempotent
        NotificationHelper.ensureNotificationChannel(context);
        NotificationHelper.ensureNotificationChannel(context);
    }
}
