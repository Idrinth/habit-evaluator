package de.idrinth.habitevaluator.android

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for the truncateLine utility logic equivalent to what PdfExportActivity provided.
 * Truncates a string to a maximum length, appending "..." when truncation occurs.
 */
class PdfExportScreenTest {

    companion object {
        /**
         * Replication of truncateLine logic from PdfExportActivity.
         * If the string length exceeds maxLength, truncates to (maxLength - 3) chars and appends "...".
         */
        fun truncateLine(line: String, maxLength: Int): String {
            return if (line.length <= maxLength) line
            else line.substring(0, maxLength - 3) + "..."
        }
    }

    @Test
    fun testTruncateLineReturnsSameStringWhenWithinLimit() {
        val input = "short line"
        assertEquals(input, truncateLine(input, 80))
    }

    @Test
    fun testTruncateLineReturnsExactLengthWhenAtLimit() {
        val input = "12345678901234567890" // 20 chars
        assertEquals(input, truncateLine(input, 20))
    }

    @Test
    fun testTruncateLineTruncatesWhenOverLimit() {
        val input = "This is a very long line that should be truncated because it exceeds the limit set for it here"
        val result = truncateLine(input, 30)
        assertEquals(30, result.length)
        assertTrue(result.endsWith("..."))
    }

    @Test
    fun testTruncateLinePreservesContentBeforeEllipsis() {
        val input = "ABCDEFGHIJ" // 10 chars
        val result = truncateLine(input, 8)
        assertEquals("ABCDE...", result)
        assertEquals(8, result.length)
    }

    @Test
    fun testTruncateLineWithEmptyString() {
        assertEquals("", truncateLine("", 80))
    }

    @Test
    fun testTruncateLineWithLargeLimit() {
        val input = "hello"
        assertEquals(input, truncateLine(input, 1000))
    }

    @Test
    fun testTruncateLineWith80CharLimit() {
        val input = "x".repeat(90)
        val result = truncateLine(input, 80)
        assertEquals(80, result.length)
        assertTrue(result.endsWith("..."))
        assertEquals(input.substring(0, 77) + "...", result)
    }

    @Test
    fun testTruncateLineWith95CharLimit() {
        val input = "y".repeat(100)
        val result = truncateLine(input, 95)
        assertEquals(95, result.length)
        assertTrue(result.endsWith("..."))
    }

    @Test
    fun testTruncateLineWith30CharLimit() {
        val label = "Very long emotion pair label that exceeds thirty characters"
        val result = truncateLine(label, 30)
        assertEquals(30, result.length)
        assertTrue(result.endsWith("..."))
    }

    @Test
    fun testTruncateLineOneOverLimit() {
        val input = "123456" // 6 chars
        val result = truncateLine(input, 5)
        assertEquals("12...", result)
        assertEquals(5, result.length)
    }

    @Test
    fun testTruncateLineWithUnicodeCharacters() {
        val input = "Tägliche Gewohnheit bewerten und überprüfen"
        val result = truncateLine(input, 20)
        assertEquals(20, result.length)
        assertTrue(result.endsWith("..."))
    }

    @Test
    fun testTruncateLineWithExactlyThreeCharsLimit() {
        val input = "ABCDEF"
        val result = truncateLine(input, 3)
        assertEquals("...", result)
        assertEquals(3, result.length)
    }

    @Test
    fun testTruncateLinePreservesOriginalWhenShorter() {
        val input = "Hi"
        assertSame(input, truncateLine(input, 100))
    }

    @Test
    fun testTruncateLineWithSpacesInContent() {
        val input = "This is a test string with spaces that should be truncated"
        val result = truncateLine(input, 25)
        assertEquals(25, result.length)
        assertTrue(result.endsWith("..."))
    }

    @Test
    fun testTruncateLineConsistentBehavior() {
        val input = "This is a string that exceeds the limit"
        val result1 = truncateLine(input, 20)
        val result2 = truncateLine(input, 20)
        assertEquals(result1, result2)
    }

    @Test
    fun testTruncateLineEllipsisIsExactlyThreeDots() {
        val input = "ABCDEFGHIJKLMNOP"
        val result = truncateLine(input, 10)
        assertTrue(result.endsWith("..."))
        assertFalse(result.endsWith("...."))
    }

    @Test
    fun testTruncateLineTwoOverLimit() {
        val input = "1234567" // 7 chars
        val result = truncateLine(input, 5)
        assertEquals("12...", result)
    }
}
