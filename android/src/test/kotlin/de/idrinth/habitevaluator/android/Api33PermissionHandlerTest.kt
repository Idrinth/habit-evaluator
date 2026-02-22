package de.idrinth.habitevaluator.android

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class Api33PermissionHandlerTest {

    private lateinit var handler: Api33PermissionHandler

    @BeforeEach
    fun setUp() {
        handler = Api33PermissionHandler()
    }

    @Test
    fun testPermissionNameReturnsPostNotifications() {
        assertEquals("android.permission.POST_NOTIFICATIONS", handler.permissionName())
    }

    @Test
    fun testPermissionNameIsNotNull() {
        assertNotNull(handler.permissionName())
    }

    @Test
    fun testImplementsNotificationPermissionHandler() {
        assertTrue(handler is NotificationPermissionHandler)
    }

    @Test
    fun testPermissionNameIsNotEmpty() {
        assertFalse(handler.permissionName().isEmpty())
    }

    @Test
    fun testPermissionNameMatchesAndroidConstant() {
        // Verify the permission string is the standard Android POST_NOTIFICATIONS permission
        assertEquals("android.permission.POST_NOTIFICATIONS", handler.permissionName())
    }

    @Test
    fun testHasPermissionReturnsTrueWhenGranted() {
        val context = mock(Context::class.java)
        mockStatic(ContextCompat::class.java).use { contextCompat ->
            contextCompat.`when`<Int> {
                ContextCompat.checkSelfPermission(context, "android.permission.POST_NOTIFICATIONS")
            }.thenReturn(PackageManager.PERMISSION_GRANTED)

            assertTrue(handler.hasPermission(context))
        }
    }

    @Test
    fun testHasPermissionReturnsFalseWhenDenied() {
        val context = mock(Context::class.java)
        mockStatic(ContextCompat::class.java).use { contextCompat ->
            contextCompat.`when`<Int> {
                ContextCompat.checkSelfPermission(context, "android.permission.POST_NOTIFICATIONS")
            }.thenReturn(PackageManager.PERMISSION_DENIED)

            assertFalse(handler.hasPermission(context))
        }
    }

    @Test
    fun testMultipleCallsReturnConsistentPermissionName() {
        val first = handler.permissionName()
        val second = handler.permissionName()
        assertEquals(first, second)
    }
}
