package de.idrinth.habitevaluator.android;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PreApi33PermissionHandlerTest {

    private PreApi33PermissionHandler handler;

    @Before
    public void setUp() {
        handler = new PreApi33PermissionHandler();
    }

    @Test
    public void testHasPermissionAlwaysReturnsTrue() {
        Context context = mock(Context.class);
        assertTrue(handler.hasPermission(context));
    }

    @Test
    public void testHasPermissionReturnsTrueWithNullContext() {
        assertTrue(handler.hasPermission(null));
    }

    @Test
    public void testPermissionNameReturnsNull() {
        assertNull(handler.permissionName());
    }

    @Test
    public void testImplementsNotificationPermissionHandler() {
        assertTrue(handler instanceof NotificationPermissionHandler);
    }
}
