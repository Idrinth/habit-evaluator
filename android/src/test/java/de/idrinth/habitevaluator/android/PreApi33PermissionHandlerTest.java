package de.idrinth.habitevaluator.android;

import android.content.Context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PreApi33PermissionHandlerTest {

    private PreApi33PermissionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PreApi33PermissionHandler();
    }

    @Test
    void testHasPermissionAlwaysReturnsTrue() {
        Context context = mock(Context.class);
        assertTrue(handler.hasPermission(context));
    }

    @Test
    void testHasPermissionReturnsTrueWithNullContext() {
        assertTrue(handler.hasPermission(null));
    }

    @Test
    void testPermissionNameReturnsNull() {
        assertNull(handler.permissionName());
    }

    @Test
    void testImplementsNotificationPermissionHandler() {
        assertTrue(handler instanceof NotificationPermissionHandler);
    }
}
