package de.idrinth.habitevaluator.android;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Api33PermissionHandlerTest {

    private Api33PermissionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new Api33PermissionHandler();
    }

    @Test
    void testPermissionNameReturnsPostNotifications() {
        assertEquals("android.permission.POST_NOTIFICATIONS", handler.permissionName());
    }

    @Test
    void testPermissionNameIsNotNull() {
        assertNotNull(handler.permissionName());
    }

    @Test
    void testImplementsNotificationPermissionHandler() {
        assertTrue(handler instanceof NotificationPermissionHandler);
    }
}
