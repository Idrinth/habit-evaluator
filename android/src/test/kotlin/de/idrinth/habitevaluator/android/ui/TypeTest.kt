package de.idrinth.habitevaluator.android.ui

import androidx.compose.ui.text.font.FontWeight
import de.idrinth.habitevaluator.android.ui.theme.Typography
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tests for Type.kt Typography definitions.
 * Verifies font sizes, weights, and line heights are correctly configured.
 */
class TypeTest {

    @Test
    fun testTypographyObjectIsNotNull() {
        assertNotNull(Typography)
    }

    // --- Font size tests ---

    @Test
    fun testHeadlineLargeFontSize() {
        assertEquals(28.0f, Typography.headlineLarge.fontSize.value)
    }

    @Test
    fun testHeadlineMediumFontSize() {
        assertEquals(24.0f, Typography.headlineMedium.fontSize.value)
    }

    @Test
    fun testTitleLargeFontSize() {
        assertEquals(20.0f, Typography.titleLarge.fontSize.value)
    }

    @Test
    fun testTitleMediumFontSize() {
        assertEquals(16.0f, Typography.titleMedium.fontSize.value)
    }

    @Test
    fun testBodyLargeFontSize() {
        assertEquals(16.0f, Typography.bodyLarge.fontSize.value)
    }

    @Test
    fun testBodyMediumFontSize() {
        assertEquals(14.0f, Typography.bodyMedium.fontSize.value)
    }

    @Test
    fun testLabelLargeFontSize() {
        assertEquals(14.0f, Typography.labelLarge.fontSize.value)
    }

    // --- Font weight tests ---

    @Test
    fun testHeadlineLargeFontWeight() {
        assertEquals(FontWeight.Bold, Typography.headlineLarge.fontWeight)
    }

    @Test
    fun testHeadlineMediumFontWeight() {
        assertEquals(FontWeight.Bold, Typography.headlineMedium.fontWeight)
    }

    @Test
    fun testTitleLargeFontWeight() {
        assertEquals(FontWeight.SemiBold, Typography.titleLarge.fontWeight)
    }

    @Test
    fun testTitleMediumFontWeight() {
        assertEquals(FontWeight.SemiBold, Typography.titleMedium.fontWeight)
    }

    @Test
    fun testBodyLargeFontWeight() {
        assertEquals(FontWeight.Normal, Typography.bodyLarge.fontWeight)
    }

    @Test
    fun testBodyMediumFontWeight() {
        assertEquals(FontWeight.Normal, Typography.bodyMedium.fontWeight)
    }

    @Test
    fun testLabelLargeFontWeight() {
        assertEquals(FontWeight.Medium, Typography.labelLarge.fontWeight)
    }

    // --- Line height tests ---

    @Test
    fun testHeadlineLargeLineHeight() {
        assertEquals(36.0f, Typography.headlineLarge.lineHeight.value)
    }

    @Test
    fun testHeadlineMediumLineHeight() {
        assertEquals(32.0f, Typography.headlineMedium.lineHeight.value)
    }

    @Test
    fun testTitleLargeLineHeight() {
        assertEquals(28.0f, Typography.titleLarge.lineHeight.value)
    }

    @Test
    fun testTitleMediumLineHeight() {
        assertEquals(24.0f, Typography.titleMedium.lineHeight.value)
    }

    @Test
    fun testBodyLargeLineHeight() {
        assertEquals(24.0f, Typography.bodyLarge.lineHeight.value)
    }

    @Test
    fun testBodyMediumLineHeight() {
        assertEquals(20.0f, Typography.bodyMedium.lineHeight.value)
    }

    @Test
    fun testLabelLargeLineHeight() {
        assertEquals(20.0f, Typography.labelLarge.lineHeight.value)
    }

    // --- Hierarchy tests ---

    @Test
    fun testFontSizeHierarchyIsDescending() {
        val sizes = listOf(
            Typography.headlineLarge.fontSize.value,
            Typography.headlineMedium.fontSize.value,
            Typography.titleLarge.fontSize.value,
            Typography.titleMedium.fontSize.value,
            Typography.bodyLarge.fontSize.value,
            Typography.bodyMedium.fontSize.value
        )
        for (i in 0 until sizes.size - 1) {
            assertTrue(
                sizes[i] >= sizes[i + 1],
                "Font size at index $i (${sizes[i]}) should be >= size at index ${i + 1} (${sizes[i + 1]})"
            )
        }
    }

    @Test
    fun testLineHeightHierarchyIsDescending() {
        val lineHeights = listOf(
            Typography.headlineLarge.lineHeight.value,
            Typography.headlineMedium.lineHeight.value,
            Typography.titleLarge.lineHeight.value,
            Typography.titleMedium.lineHeight.value,
            Typography.bodyLarge.lineHeight.value,
            Typography.bodyMedium.lineHeight.value
        )
        for (i in 0 until lineHeights.size - 1) {
            assertTrue(
                lineHeights[i] >= lineHeights[i + 1],
                "Line height at index $i (${lineHeights[i]}) should be >= line height at index ${i + 1} (${lineHeights[i + 1]})"
            )
        }
    }

    @Test
    fun testLineHeightIsAlwaysGreaterThanFontSize() {
        val styles = listOf(
            Typography.headlineLarge, Typography.headlineMedium,
            Typography.titleLarge, Typography.titleMedium,
            Typography.bodyLarge, Typography.bodyMedium,
            Typography.labelLarge
        )
        styles.forEach { style ->
            assertTrue(
                style.lineHeight.value > style.fontSize.value,
                "Line height (${style.lineHeight.value}) should be > font size (${style.fontSize.value})"
            )
        }
    }

    @Test
    fun testAllFontSizesArePositive() {
        val styles = listOf(
            Typography.headlineLarge, Typography.headlineMedium,
            Typography.titleLarge, Typography.titleMedium,
            Typography.bodyLarge, Typography.bodyMedium,
            Typography.labelLarge
        )
        styles.forEach { style ->
            assertTrue(style.fontSize.value > 0, "Font size should be positive")
        }
    }

    @Test
    fun testHeadlinesAreBold() {
        assertEquals(FontWeight.Bold, Typography.headlineLarge.fontWeight)
        assertEquals(FontWeight.Bold, Typography.headlineMedium.fontWeight)
    }

    @Test
    fun testTitlesAreSemiBold() {
        assertEquals(FontWeight.SemiBold, Typography.titleLarge.fontWeight)
        assertEquals(FontWeight.SemiBold, Typography.titleMedium.fontWeight)
    }

    @Test
    fun testBodiesAreNormalWeight() {
        assertEquals(FontWeight.Normal, Typography.bodyLarge.fontWeight)
        assertEquals(FontWeight.Normal, Typography.bodyMedium.fontWeight)
    }

    @Test
    fun testFontWeightHierarchy() {
        // Headlines should be bolder than titles, which should be bolder than body
        assertTrue(Typography.headlineLarge.fontWeight!! >= Typography.titleLarge.fontWeight!!)
        assertTrue(Typography.titleLarge.fontWeight!! >= Typography.bodyLarge.fontWeight!!)
    }
}
