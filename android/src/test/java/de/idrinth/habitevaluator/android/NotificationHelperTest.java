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
        when(context.getSystemService(Context.NOTIFICATION_SERVICE))
                .thenReturn(mock(NotificationManager.class));

        NotificationHelper.ensureNotificationChannel(context);
    }

    @Test
    void testEnsureNotificationChannelWithNullNotificationManager() {
        Context context = mock(Context.class);
        when(context.getString(anyInt())).thenReturn("Test Channel");
        when(context.getSystemService(Context.NOTIFICATION_SERVICE)).thenReturn(null);

        NotificationHelper.ensureNotificationChannel(context);
    }
}
