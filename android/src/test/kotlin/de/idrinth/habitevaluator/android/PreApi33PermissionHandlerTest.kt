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

    @Test
    fun testHasPermissionReturnsTrueWithDifferentContextInstances() {
        val context1 = mock(Context::class.java)
        val context2 = mock(Context::class.java)
        assertTrue(handler.hasPermission(context1))
        assertTrue(handler.hasPermission(context2))
    }

    @Test
    fun testMultipleCallsReturnConsistentResults() {
        val context = mock(Context::class.java)
        val first = handler.hasPermission(context)
        val second = handler.hasPermission(context)
        assertEquals(first, second)
    }

    @Test
    fun testPermissionNameConsistentlyReturnsNull() {
        val first = handler.permissionName()
        val second = handler.permissionName()
        assertEquals(first, second)
    }

    @Test
    fun testNewInstanceAlsoReturnsTrue() {
        val anotherHandler = PreApi33PermissionHandler()
        val context = mock(Context::class.java)
        assertTrue(anotherHandler.hasPermission(context))
    }

    @Test
    fun testNewInstanceAlsoReturnsNullPermissionName() {
        val anotherHandler = PreApi33PermissionHandler()
        assertNull(anotherHandler.permissionName())
    }

    @Test
    fun testClassIsNotAbstract() {
        assertFalse(
            java.lang.reflect.Modifier.isAbstract(PreApi33PermissionHandler::class.java.modifiers)
        )
    }

    @Test
    fun testImplementsHasPermissionMethod() {
        val method = PreApi33PermissionHandler::class.java.getMethod("hasPermission", Context::class.java)
        assertNotNull(method)
    }

    @Test
    fun testImplementsPermissionNameMethod() {
        val method = PreApi33PermissionHandler::class.java.getMethod("permissionName")
        assertNotNull(method)
    }
}
