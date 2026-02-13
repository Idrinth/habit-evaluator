package de.idrinth.habitevaluator.android;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class Api33PermissionHandlerTest {

    private Api33PermissionHandler handler;

    @Before
    public void setUp() {
        handler = new Api33PermissionHandler();
    }

    @Test
    public void testPermissionNameReturnsPostNotifications() {
        assertEquals("android.permission.POST_NOTIFICATIONS", handler.permissionName());
    }

    @Test
    public void testPermissionNameIsNotNull() {
        assertNotNull(handler.permissionName());
    }

    @Test
    public void testImplementsNotificationPermissionHandler() {
        assertTrue(handler instanceof NotificationPermissionHandler);
    }
}
