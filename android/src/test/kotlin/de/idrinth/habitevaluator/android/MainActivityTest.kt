package de.idrinth.habitevaluator.android

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for MainActivity. The old static state (editHabitId, pointDevelopmentHabitId,
 * recordEmotionPairId, sharedCategories, etc.) has been migrated to AppViewModel
 * with StateFlow. This test class verifies that MainActivity still exists and can
 * be referenced without crashing at the class-loading level.
 */
class MainActivityTest {

    @Test
    fun testMainActivityClassCanBeReferenced() {
        // Verify the class exists and can be referenced without error.
        // All shared mutable state previously on MainActivity static fields
        // now lives in AppViewModel; see AppViewModelTest for those assertions.
        val clazz = MainActivity::class.java
        assertNotNull(clazz)
    }

    @Test
    fun testMainActivityClassNameIsCorrect() {
        assertEquals("MainActivity", MainActivity::class.java.simpleName)
    }

    @Test
    fun testMainActivityPackageIsCorrect() {
        assertEquals(
            "de.idrinth.habitevaluator.android",
            MainActivity::class.java.packageName
        )
    }
}
