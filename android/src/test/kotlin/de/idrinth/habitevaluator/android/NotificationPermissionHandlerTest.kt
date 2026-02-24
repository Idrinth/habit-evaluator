package de.idrinth.habitevaluator.android

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for the NotificationPermissionHandler interface contract.
 * Since NotificationPermissionHandler is an interface requiring Android Context,
 * we test the concrete implementations Api33PermissionHandler and PreApi33PermissionHandler,
 * along with the interface contract validation.
 */
class NotificationPermissionHandlerTest {

    /**
     * A test-only implementation of NotificationPermissionHandler that always grants permission.
     */
    private class AlwaysGrantedHandler : NotificationPermissionHandler {
        override fun hasPermission(context: android.content.Context): Boolean = true
        override fun permissionName(): String? = null
    }

    /**
     * A test-only implementation that always denies permission and requires a specific permission name.
     */
    private class AlwaysDeniedHandler(private val permission: String) : NotificationPermissionHandler {
        override fun hasPermission(context: android.content.Context): Boolean = false
        override fun permissionName(): String = permission
    }

    // --- Interface contract tests ---

    @Test
    fun testAlwaysGrantedHandlerReturnsNullPermission() {
        val handler = AlwaysGrantedHandler()
        assertNull(handler.permissionName())
    }

    @Test
    fun testAlwaysDeniedHandlerReturnsPermissionName() {
        val handler = AlwaysDeniedHandler("android.permission.POST_NOTIFICATIONS")
        assertEquals("android.permission.POST_NOTIFICATIONS", handler.permissionName())
    }

    @Test
    fun testAlwaysDeniedHandlerPermissionIsNotNull() {
        val handler = AlwaysDeniedHandler("android.permission.POST_NOTIFICATIONS")
        assertNotNull(handler.permissionName())
    }

    @Test
    fun testAlwaysDeniedHandlerPermissionIsNotEmpty() {
        val handler = AlwaysDeniedHandler("android.permission.POST_NOTIFICATIONS")
        assertTrue(handler.permissionName()!!.isNotEmpty())
    }

    // --- Api33PermissionHandler tests ---

    @Test
    fun testApi33PermissionHandlerReturnsPermissionName() {
        val handler = Api33PermissionHandler()
        val permission = handler.permissionName()
        assertNotNull(permission)
        assertEquals("android.permission.POST_NOTIFICATIONS", permission)
    }

    @Test
    fun testApi33PermissionHandlerPermissionNameIsConsistent() {
        val handler = Api33PermissionHandler()
        assertEquals(handler.permissionName(), handler.permissionName())
    }

    // --- PreApi33PermissionHandler tests ---

    @Test
    fun testPreApi33PermissionHandlerReturnsNullPermission() {
        val handler = PreApi33PermissionHandler()
        assertNull(handler.permissionName())
    }

    // --- Cross-implementation tests ---

    @Test
    fun testApi33AndPreApi33HaveDifferentPermissionBehavior() {
        val api33 = Api33PermissionHandler()
        val preApi33 = PreApi33PermissionHandler()
        assertNotEquals(api33.permissionName(), preApi33.permissionName())
    }

    @Test
    fun testBothImplementationsAreNotificationPermissionHandlers() {
        val api33: NotificationPermissionHandler = Api33PermissionHandler()
        val preApi33: NotificationPermissionHandler = PreApi33PermissionHandler()
        assertNotNull(api33)
        assertNotNull(preApi33)
    }
}
