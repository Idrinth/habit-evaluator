package de.idrinth.habitevaluator.android

import android.content.Context
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class PreApi33PermissionHandlerTest {

    private lateinit var handler: PreApi33PermissionHandler

    @BeforeEach
    fun setUp() {
        handler = PreApi33PermissionHandler()
    }

    @Test
    fun testHasPermissionAlwaysReturnsTrue() {
        val context = mock(Context::class.java)
        assertTrue(handler.hasPermission(context))
    }

    @Test
    fun testPermissionNameReturnsNull() {
        assertNull(handler.permissionName())
    }

    @Test
    fun testImplementsNotificationPermissionHandler() {
        assertTrue(handler is NotificationPermissionHandler)
    }
}
