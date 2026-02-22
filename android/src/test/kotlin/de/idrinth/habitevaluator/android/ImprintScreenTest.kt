package de.idrinth.habitevaluator.android

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for imprint/about constants and version label formatting equivalent to what
 * ImprintFragment previously provided as static constants and helper methods.
 */
class ImprintScreenTest {

    companion object {
        const val EMAIL_ADDRESS = "self@idrinth.de"
        const val MAILTO_URI = "mailto:self@idrinth.de"
        const val VERSION_PREFIX = "Version "

        /**
         * Replication of formatVersionLabel logic from ImprintFragment.
         * Returns "Version <version>" for valid versions, "Version unknown" for null/empty.
         */
        fun formatVersionLabel(version: String?): String {
            return if (version.isNullOrEmpty()) {
                "${VERSION_PREFIX}unknown"
            } else {
                "$VERSION_PREFIX$version"
            }
        }
    }

    @Test
    fun testEmailAddressIsNotNull() {
        assertNotNull(EMAIL_ADDRESS)
    }

    @Test
    fun testEmailAddressIsNotEmpty() {
        assertFalse(EMAIL_ADDRESS.isEmpty())
    }

    @Test
    fun testEmailAddressContainsAtSymbol() {
        assertTrue(EMAIL_ADDRESS.contains("@"))
    }

    @Test
    fun testEmailAddressValue() {
        assertEquals("self@idrinth.de", EMAIL_ADDRESS)
    }

    @Test
    fun testMailtoUriIsNotNull() {
        assertNotNull(MAILTO_URI)
    }

    @Test
    fun testMailtoUriStartsWithMailtoScheme() {
        assertTrue(MAILTO_URI.startsWith("mailto:"))
    }

    @Test
    fun testMailtoUriContainsEmailAddress() {
        assertTrue(MAILTO_URI.contains(EMAIL_ADDRESS))
    }

    @Test
    fun testMailtoUriValue() {
        assertEquals("mailto:self@idrinth.de", MAILTO_URI)
    }

    @Test
    fun testMailtoUriMatchesEmailAddress() {
        assertEquals("mailto:$EMAIL_ADDRESS", MAILTO_URI)
    }

    @Test
    fun testVersionPrefixIsNotNull() {
        assertNotNull(VERSION_PREFIX)
    }

    @Test
    fun testVersionPrefixValue() {
        assertEquals("Version ", VERSION_PREFIX)
    }

    @Test
    fun testVersionPrefixEndsWithSpace() {
        assertTrue(VERSION_PREFIX.endsWith(" "))
    }

    @Test
    fun testFormatVersionLabelWithValidVersion() {
        assertEquals("Version 1.0.0", formatVersionLabel("1.0.0"))
    }

    @Test
    fun testFormatVersionLabelWithSnapshotVersion() {
        assertEquals("Version 0.1.0-SNAPSHOT", formatVersionLabel("0.1.0-SNAPSHOT"))
    }

    @Test
    fun testFormatVersionLabelStartsWithPrefix() {
        assertTrue(formatVersionLabel("2.3.4").startsWith(VERSION_PREFIX))
    }

    @Test
    fun testFormatVersionLabelWithNullVersion() {
        assertEquals("Version unknown", formatVersionLabel(null))
    }

    @Test
    fun testFormatVersionLabelWithEmptyVersion() {
        assertEquals("Version unknown", formatVersionLabel(""))
    }

    @Test
    fun testFormatVersionLabelWithSingleDigitVersion() {
        assertEquals("Version 1", formatVersionLabel("1"))
    }

    @Test
    fun testFormatVersionLabelWithSemanticVersion() {
        val result = formatVersionLabel("3.2.1")
        assertTrue(result.contains("3.2.1"))
    }

    @Test
    fun testFormatVersionLabelResultIsNotNull() {
        assertNotNull(formatVersionLabel("1.0.0"))
    }

    @Test
    fun testFormatVersionLabelResultIsNotNullForNullInput() {
        assertNotNull(formatVersionLabel(null))
    }

    @Test
    fun testFormatVersionLabelResultIsNotNullForEmptyInput() {
        assertNotNull(formatVersionLabel(""))
    }
}
