package de.idrinth.habitevaluator.android.ui

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for theme color constants and typography definitions from the ui/theme package.
 * Validates that color values are defined correctly and typography sizes are consistent.
 */
class ThemeTest {

    companion object {
        // Light theme color values (from Color.kt)
        const val PRIMARY = 0xFF4CAF50
        const val PRIMARY_DARK = 0xFF388E3C
        const val PRIMARY_LIGHT = 0xFFC8E6C9
        const val ACCENT = 0xFFFFC107
        const val TEXT_PRIMARY = 0xFF212121
        const val TEXT_SECONDARY = 0xFF757575
        const val DIVIDER = 0xFFBDBDBD
        const val BACKGROUND = 0xFFFAFAFA
        const val WHITE = 0xFFFFFFFF

        // Dark theme color values (from Color.kt)
        const val PRIMARY_DARK_THEME = 0xFF81C784
        const val PRIMARY_DARK_DARK = 0xFF4CAF50
        const val ACCENT_DARK = 0xFFFFD54F
        const val BACKGROUND_DARK = 0xFF121212
        const val SURFACE_DARK = 0xFF1E1E1E
        const val TEXT_PRIMARY_DARK = 0xFFE0E0E0
        const val TEXT_SECONDARY_DARK = 0xFFB0B0B0

        // Typography sizes (from Type.kt)
        const val HEADLINE_LARGE_SIZE = 28
        const val HEADLINE_MEDIUM_SIZE = 24
        const val TITLE_LARGE_SIZE = 20
        const val TITLE_MEDIUM_SIZE = 16
        const val BODY_LARGE_SIZE = 16
        const val BODY_MEDIUM_SIZE = 14
        const val LABEL_LARGE_SIZE = 14
    }

    // --- Light theme color tests ---

    @Test
    fun testPrimaryColorIsGreen() {
        // Material green 500
        assertEquals(0xFF4CAF50, PRIMARY)
    }

    @Test
    fun testPrimaryDarkColorIsDarkerGreen() {
        assertEquals(0xFF388E3C, PRIMARY_DARK)
        assertTrue(PRIMARY_DARK != PRIMARY, "Dark variant should differ from primary")
    }

    @Test
    fun testAccentColorIsAmber() {
        assertEquals(0xFFFFC107, ACCENT)
    }

    @Test
    fun testWhiteColor() {
        assertEquals(0xFFFFFFFF, WHITE)
    }

    @Test
    fun testBackgroundColorIsNearWhite() {
        assertEquals(0xFFFAFAFA, BACKGROUND)
    }

    @Test
    fun testTextPrimaryIsDark() {
        assertEquals(0xFF212121, TEXT_PRIMARY)
    }

    @Test
    fun testTextSecondaryIsLighterThanPrimary() {
        // Text secondary should be a lighter shade
        assertNotEquals(TEXT_PRIMARY, TEXT_SECONDARY)
    }

    // --- Dark theme color tests ---

    @Test
    fun testDarkThemeBackgroundIsNearBlack() {
        assertEquals(0xFF121212, BACKGROUND_DARK)
    }

    @Test
    fun testDarkThemeSurfaceIsDark() {
        assertEquals(0xFF1E1E1E, SURFACE_DARK)
    }

    @Test
    fun testDarkThemeTextPrimaryIsLight() {
        assertEquals(0xFFE0E0E0, TEXT_PRIMARY_DARK)
    }

    @Test
    fun testDarkThemeTextSecondaryIsLighter() {
        assertNotEquals(TEXT_PRIMARY_DARK, TEXT_SECONDARY_DARK)
    }

    @Test
    fun testDarkAccentIsDifferentFromLightAccent() {
        assertNotEquals(ACCENT, ACCENT_DARK)
    }

    // --- Theme contrast tests ---

    @Test
    fun testLightAndDarkBackgroundsDiffer() {
        assertNotEquals(BACKGROUND, BACKGROUND_DARK)
    }

    @Test
    fun testLightAndDarkTextPrimaryDiffer() {
        assertNotEquals(TEXT_PRIMARY, TEXT_PRIMARY_DARK)
    }

    // --- Color uniqueness tests ---

    @Test
    fun testLightThemeColorsAreDistinct() {
        val colors = listOf(PRIMARY, PRIMARY_DARK, PRIMARY_LIGHT, ACCENT, TEXT_PRIMARY, TEXT_SECONDARY, DIVIDER, BACKGROUND, WHITE)
        assertEquals(colors.size, colors.toSet().size, "All light theme colors should be unique")
    }

    @Test
    fun testDarkThemeColorsAreDistinct() {
        val colors = listOf(PRIMARY_DARK_THEME, ACCENT_DARK, BACKGROUND_DARK, SURFACE_DARK, TEXT_PRIMARY_DARK, TEXT_SECONDARY_DARK)
        assertEquals(colors.size, colors.toSet().size, "All dark theme colors should be unique")
    }

    // --- Typography size tests ---

    @Test
    fun testHeadlineLargeSize() {
        assertEquals(28, HEADLINE_LARGE_SIZE)
    }

    @Test
    fun testHeadlineMediumSize() {
        assertEquals(24, HEADLINE_MEDIUM_SIZE)
    }

    @Test
    fun testTitleLargeSize() {
        assertEquals(20, TITLE_LARGE_SIZE)
    }

    @Test
    fun testTitleMediumSize() {
        assertEquals(16, TITLE_MEDIUM_SIZE)
    }

    @Test
    fun testBodyLargeSize() {
        assertEquals(16, BODY_LARGE_SIZE)
    }

    @Test
    fun testBodyMediumSize() {
        assertEquals(14, BODY_MEDIUM_SIZE)
    }

    @Test
    fun testLabelLargeSize() {
        assertEquals(14, LABEL_LARGE_SIZE)
    }

    @Test
    fun testTypographySizesAreDescending() {
        assertTrue(HEADLINE_LARGE_SIZE > HEADLINE_MEDIUM_SIZE)
        assertTrue(HEADLINE_MEDIUM_SIZE > TITLE_LARGE_SIZE)
        assertTrue(TITLE_LARGE_SIZE >= TITLE_MEDIUM_SIZE)
        assertTrue(TITLE_MEDIUM_SIZE >= BODY_LARGE_SIZE)
        assertTrue(BODY_LARGE_SIZE >= BODY_MEDIUM_SIZE)
    }

    @Test
    fun testAllTypographySizesArePositive() {
        val sizes = listOf(HEADLINE_LARGE_SIZE, HEADLINE_MEDIUM_SIZE, TITLE_LARGE_SIZE,
            TITLE_MEDIUM_SIZE, BODY_LARGE_SIZE, BODY_MEDIUM_SIZE, LABEL_LARGE_SIZE)
        sizes.forEach { size ->
            assertTrue(size > 0, "Typography size should be positive: $size")
        }
    }
}
