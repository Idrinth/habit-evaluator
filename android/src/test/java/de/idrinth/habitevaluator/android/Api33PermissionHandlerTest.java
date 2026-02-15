package de.idrinth.habitevaluator.android;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @Test
    void testPermissionNameIsNotEmpty() {
        assertFalse(handler.permissionName().isEmpty());
    }

    @Test
    void testPermissionNameMatchesAndroidConstant() {
        // Verify the permission string is the standard Android POST_NOTIFICATIONS permission
        assertEquals("android.permission.POST_NOTIFICATIONS", handler.permissionName());
    }

    @Test
    void testHasPermissionReturnsTrueWhenGranted() {
        Context context = mock(Context.class);
        try (MockedStatic<ContextCompat> contextCompat = mockStatic(ContextCompat.class)) {
            contextCompat.when(() -> ContextCompat.checkSelfPermission(context, "android.permission.POST_NOTIFICATIONS"))
                    .thenReturn(PackageManager.PERMISSION_GRANTED);

            assertTrue(handler.hasPermission(context));
        }
    }

    @Test
    void testHasPermissionReturnsFalseWhenDenied() {
        Context context = mock(Context.class);
        try (MockedStatic<ContextCompat> contextCompat = mockStatic(ContextCompat.class)) {
            contextCompat.when(() -> ContextCompat.checkSelfPermission(context, "android.permission.POST_NOTIFICATIONS"))
                    .thenReturn(PackageManager.PERMISSION_DENIED);

            assertFalse(handler.hasPermission(context));
        }
    }

    @Test
    void testMultipleCallsReturnConsistentPermissionName() {
        String first = handler.permissionName();
        String second = handler.permissionName();
        assertEquals(first, second);
    }
}
