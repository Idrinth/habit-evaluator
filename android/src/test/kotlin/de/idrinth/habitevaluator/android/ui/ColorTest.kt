package de.idrinth.habitevaluator.android.ui

import androidx.compose.ui.graphics.Color
import de.idrinth.habitevaluator.android.ui.theme.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tests for Color.kt constants. Verifies that all color definitions
 * have correct RGBA channel values and expected properties.
 * Uses channel-based comparisons (red, green, blue, alpha floats)
 * since toArgb() is an extension function not available in unit tests.
 */
class ColorTest {

    private val tolerance = 0.01f

    private fun assertColorChannels(
        expectedRed: Float, expectedGreen: Float, expectedBlue: Float,
        actual: Color, name: String
    ) {
        assertEquals(expectedRed, actual.red, tolerance, "$name red channel mismatch")
        assertEquals(expectedGreen, actual.green, tolerance, "$name green channel mismatch")
        assertEquals(expectedBlue, actual.blue, tolerance, "$name blue channel mismatch")
        assertEquals(1.0f, actual.alpha, tolerance, "$name alpha channel mismatch")
    }

    // --- Light theme color value tests ---

    @Test
    fun testPrimaryColorChannels() {
        // 0xFF4CAF50 -> R=0x4C/255, G=0xAF/255, B=0x50/255
        assertColorChannels(76f / 255f, 175f / 255f, 80f / 255f, Primary, "Primary")
    }

    @Test
    fun testPrimaryDarkColorChannels() {
        // 0xFF388E3C -> R=0x38/255, G=0x8E/255, B=0x3C/255
        assertColorChannels(56f / 255f, 142f / 255f, 60f / 255f, PrimaryDark, "PrimaryDark")
    }

    @Test
    fun testPrimaryLightColorChannels() {
        // 0xFFC8E6C9 -> R=0xC8/255, G=0xE6/255, B=0xC9/255
        assertColorChannels(200f / 255f, 230f / 255f, 201f / 255f, PrimaryLight, "PrimaryLight")
    }

    @Test
    fun testAccentColorChannels() {
        // 0xFFFFC107 -> R=0xFF/255, G=0xC1/255, B=0x07/255
        assertColorChannels(255f / 255f, 193f / 255f, 7f / 255f, Accent, "Accent")
    }

    @Test
    fun testTextPrimaryColorChannels() {
        // 0xFF212121 -> R=G=B=0x21/255
        assertColorChannels(33f / 255f, 33f / 255f, 33f / 255f, TextPrimary, "TextPrimary")
    }

    @Test
    fun testTextSecondaryColorChannels() {
        // 0xFF757575 -> R=G=B=0x75/255
        assertColorChannels(117f / 255f, 117f / 255f, 117f / 255f, TextSecondary, "TextSecondary")
    }

    @Test
    fun testDividerColorChannels() {
        // 0xFFBDBDBD -> R=G=B=0xBD/255
        assertColorChannels(189f / 255f, 189f / 255f, 189f / 255f, Divider, "Divider")
    }

    @Test
    fun testBackgroundColorChannels() {
        // 0xFFFAFAFA -> R=G=B=0xFA/255
        assertColorChannels(250f / 255f, 250f / 255f, 250f / 255f, Background, "Background")
    }

    @Test
    fun testWhiteColorChannels() {
        // 0xFFFFFFFF -> R=G=B=1.0
        assertColorChannels(1.0f, 1.0f, 1.0f, White, "White")
    }

    // --- Dark theme color value tests ---

    @Test
    fun testPrimaryDarkThemeColorChannels() {
        // 0xFF4CAF50 -> same as Primary
        assertColorChannels(76f / 255f, 175f / 255f, 80f / 255f, PrimaryDarkTheme, "PrimaryDarkTheme")
    }

    @Test
    fun testPrimaryDarkDarkColorChannels() {
        // 0xFF388E3C -> same as PrimaryDark
        assertColorChannels(56f / 255f, 142f / 255f, 60f / 255f, PrimaryDarkDark, "PrimaryDarkDark")
    }

    @Test
    fun testAccentDarkColorChannels() {
        // 0xFFFFD54F -> R=0xFF/255, G=0xD5/255, B=0x4F/255
        assertColorChannels(255f / 255f, 213f / 255f, 79f / 255f, AccentDark, "AccentDark")
    }

    @Test
    fun testBackgroundDarkColorChannels() {
        // 0xFF121212 -> R=G=B=0x12/255
        assertColorChannels(18f / 255f, 18f / 255f, 18f / 255f, BackgroundDark, "BackgroundDark")
    }

    @Test
    fun testSurfaceDarkColorChannels() {
        // 0xFF1E1E1E -> R=G=B=0x1E/255
        assertColorChannels(30f / 255f, 30f / 255f, 30f / 255f, SurfaceDark, "SurfaceDark")
    }

    @Test
    fun testTextPrimaryDarkColorChannels() {
        // 0xFFE0E0E0 -> R=G=B=0xE0/255
        assertColorChannels(224f / 255f, 224f / 255f, 224f / 255f, TextPrimaryDark, "TextPrimaryDark")
    }

    @Test
    fun testTextSecondaryDarkColorChannels() {
        // 0xFFB0B0B0 -> R=G=B=0xB0/255
        assertColorChannels(176f / 255f, 176f / 255f, 176f / 255f, TextSecondaryDark, "TextSecondaryDark")
    }

    // --- Color property tests ---

    @Test
    fun testAllLightColorsAreFullyOpaque() {
        val lightColors = listOf(Primary, PrimaryDark, PrimaryLight, Accent,
            TextPrimary, TextSecondary, Divider, Background, White)
        lightColors.forEach { color ->
            assertEquals(1.0f, color.alpha, 0.001f, "Light color should be fully opaque")
        }
    }

    @Test
    fun testAllDarkColorsAreFullyOpaque() {
        val darkColors = listOf(PrimaryDarkTheme, PrimaryDarkDark, AccentDark,
            BackgroundDark, SurfaceDark, TextPrimaryDark, TextSecondaryDark)
        darkColors.forEach { color ->
            assertEquals(1.0f, color.alpha, 0.001f, "Dark color should be fully opaque")
        }
    }

    @Test
    fun testLightThemeColorsAreDistinct() {
        val colors = listOf(Primary, PrimaryDark, PrimaryLight, Accent,
            TextPrimary, TextSecondary, Divider, Background, White)
        val uniqueValues = colors.map { it.value }.toSet()
        assertEquals(colors.size, uniqueValues.size, "All light theme colors should have unique values")
    }

    @Test
    fun testDarkThemeColorsAreDistinct() {
        val colors = listOf(AccentDark, BackgroundDark, SurfaceDark,
            TextPrimaryDark, TextSecondaryDark)
        val uniqueValues = colors.map { it.value }.toSet()
        assertEquals(colors.size, uniqueValues.size, "Dark theme accent/bg/surface/text colors should be unique")
    }

    @Test
    fun testLightAndDarkBackgroundsDiffer() {
        assertNotEquals(Background.value, BackgroundDark.value)
    }

    @Test
    fun testLightAndDarkTextPrimaryDiffer() {
        assertNotEquals(TextPrimary.value, TextPrimaryDark.value)
    }

    @Test
    fun testLightAndDarkAccentsDiffer() {
        assertNotEquals(Accent.value, AccentDark.value)
    }

    @Test
    fun testPrimaryGreenChannelIsDominant() {
        assertTrue(Primary.green > Primary.red, "Green channel should dominate for primary color")
        assertTrue(Primary.green > Primary.blue, "Green channel should be higher than blue for primary")
    }

    @Test
    fun testDarkBackgroundIsActuallyDark() {
        assertTrue(BackgroundDark.red < 0.1f, "Dark background red channel should be low")
        assertTrue(BackgroundDark.green < 0.1f, "Dark background green channel should be low")
        assertTrue(BackgroundDark.blue < 0.1f, "Dark background blue channel should be low")
    }

    @Test
    fun testLightBackgroundIsActuallyLight() {
        assertTrue(Background.red > 0.9f, "Light background red channel should be high")
        assertTrue(Background.green > 0.9f, "Light background green channel should be high")
        assertTrue(Background.blue > 0.9f, "Light background blue channel should be high")
    }

    @Test
    fun testWhiteIsFullWhite() {
        assertEquals(1.0f, White.red, 0.001f)
        assertEquals(1.0f, White.green, 0.001f)
        assertEquals(1.0f, White.blue, 0.001f)
    }

    @Test
    fun testDarkTextPrimaryIsReadableOnDarkBackground() {
        assertTrue(
            TextPrimaryDark.red > BackgroundDark.red + 0.5f,
            "Dark theme text should be much lighter than background"
        )
    }

    @Test
    fun testLightTextPrimaryIsReadableOnLightBackground() {
        assertTrue(
            Background.red > TextPrimary.red + 0.5f,
            "Light theme text should be much darker than background"
        )
    }

    @Test
    fun testPrimaryAndPrimaryDarkThemeShareSameValue() {
        assertEquals(Primary.value, PrimaryDarkTheme.value,
            "Primary green should be same in both light and dark themes")
    }

    @Test
    fun testPrimaryDarkAndPrimaryDarkDarkShareSameValue() {
        assertEquals(PrimaryDark.value, PrimaryDarkDark.value,
            "Dark primary variant should be same in both themes")
    }
}
