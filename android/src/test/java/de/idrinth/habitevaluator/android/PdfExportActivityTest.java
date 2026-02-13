package de.idrinth.habitevaluator.android;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PdfExportActivityTest {

    @Test
    void testTruncateLineReturnsSameStringWhenWithinLimit() {
        String input = "short line";
        assertEquals(input, PdfExportActivity.truncateLine(input, 80));
    }

    @Test
    void testTruncateLineReturnsExactLengthWhenAtLimit() {
        String input = "12345678901234567890"; // 20 chars
        assertEquals(input, PdfExportActivity.truncateLine(input, 20));
    }

    @Test
    void testTruncateLineTruncatesWhenOverLimit() {
        String input = "This is a very long line that should be truncated because it exceeds the limit set for it here";
        String result = PdfExportActivity.truncateLine(input, 30);
        assertEquals(30, result.length());
        assertTrue(result.endsWith("..."));
    }

    @Test
    void testTruncateLinePreservesContentBeforeEllipsis() {
        String input = "ABCDEFGHIJ"; // 10 chars
        String result = PdfExportActivity.truncateLine(input, 8);
        assertEquals("ABCDE...", result);
        assertEquals(8, result.length());
    }

    @Test
    void testTruncateLineWithEmptyString() {
        assertEquals("", PdfExportActivity.truncateLine("", 80));
    }

    @Test
    void testTruncateLineWithLargeLimit() {
        String input = "hello";
        assertEquals(input, PdfExportActivity.truncateLine(input, 1000));
    }

    @Test
    void testTruncateLineWith80CharLimit() {
        // Simulate the sleep/diary entry truncation behavior
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 90; i++) {
            sb.append("x");
        }
        String result = PdfExportActivity.truncateLine(sb.toString(), 80);
        assertEquals(80, result.length());
        assertTrue(result.endsWith("..."));
        assertEquals(sb.substring(0, 77) + "...", result);
    }

    @Test
    void testTruncateLineWith95CharLimit() {
        // Simulate the correlation line truncation behavior
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("y");
        }
        String result = PdfExportActivity.truncateLine(sb.toString(), 95);
        assertEquals(95, result.length());
        assertTrue(result.endsWith("..."));
    }

    @Test
    void testTruncateLineWith30CharLimit() {
        // Simulate the legend label truncation behavior
        String label = "Very long emotion pair label that exceeds thirty characters";
        String result = PdfExportActivity.truncateLine(label, 30);
        assertEquals(30, result.length());
        assertTrue(result.endsWith("..."));
    }

    @Test
    void testTruncateLineOneOverLimit() {
        String input = "123456"; // 6 chars
        String result = PdfExportActivity.truncateLine(input, 5);
        assertEquals("12...", result);
        assertEquals(5, result.length());
    }
}
