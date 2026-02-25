package de.idrinth.habitevaluator.android

import androidx.appcompat.app.AppCompatActivity
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for MainActivity. The old static state (editHabitId, pointDevelopmentHabitId,
 * recordEmotionPairId, sharedCategories, etc.) has been migrated to AppViewModel
 * with StateFlow. This test class verifies that MainActivity still exists and can
 * be referenced without crashing at the class-loading level.
 */
class MainActivityTest {

    private val clazz = MainActivity::class.java

    @Test
    fun testMainActivityClassCanBeReferenced() {
        assertNotNull(clazz)
    }

    @Test
    fun testMainActivityClassNameIsCorrect() {
        assertEquals("MainActivity", clazz.simpleName)
    }

    @Test
    fun testMainActivityPackageIsCorrect() {
        assertEquals("de.idrinth.habitevaluator.android", clazz.packageName)
    }

    @Test
    fun testExtendsAppCompatActivity() {
        assertTrue(
            AppCompatActivity::class.java.isAssignableFrom(clazz),
            "MainActivity should extend AppCompatActivity"
        )
    }

    @Test
    fun testOnCreateMethodExists() {
        val method = clazz.getDeclaredMethod("onCreate", android.os.Bundle::class.java)
        assertNotNull(method)
    }

    @Test
    fun testOnStopMethodExists() {
        val method = clazz.getDeclaredMethod("onStop")
        assertNotNull(method)
    }

    @Test
    fun testAttachBaseContextMethodExists() {
        val method = clazz.getDeclaredMethod("attachBaseContext", android.content.Context::class.java)
        assertNotNull(method)
    }

    @Test
    fun testViewModelFieldExists() {
        val field = clazz.getDeclaredField("viewModel")
        assertNotNull(field)
        assertEquals(AppViewModel::class.java, field.type)
    }

    @Test
    fun testIsNotAbstract() {
        assertFalse(
            java.lang.reflect.Modifier.isAbstract(clazz.modifiers),
            "MainActivity should not be abstract"
        )
    }

    @Test
    fun testHasNoStaticStateFields() {
        // Verify that old static mutable state has been fully migrated to AppViewModel.
        // No companion object fields should hold shared mutable state.
        val companionFields = clazz.declaredClasses
            .filter { it.simpleName == "Companion" }
            .flatMap { it.declaredFields.toList() }
            .filter { !it.name.startsWith("\$") } // exclude synthetic fields
        assertTrue(
            companionFields.isEmpty(),
            "MainActivity should have no companion object state fields; state lives in AppViewModel"
        )
    }
}
