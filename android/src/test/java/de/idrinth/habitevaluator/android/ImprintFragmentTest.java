package de.idrinth.habitevaluator.android;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImprintFragmentTest {

    @Test
    void testEmailAddressIsNotNull() {
        assertNotNull(ImprintFragment.EMAIL_ADDRESS);
    }

    @Test
    void testEmailAddressIsNotEmpty() {
        assertFalse(ImprintFragment.EMAIL_ADDRESS.isEmpty());
    }

    @Test
    void testEmailAddressContainsAtSymbol() {
        assertTrue(ImprintFragment.EMAIL_ADDRESS.contains("@"));
    }

    @Test
    void testEmailAddressValue() {
        assertEquals("self@idrinth.de", ImprintFragment.EMAIL_ADDRESS);
    }

    @Test
    void testMailtoUriIsNotNull() {
        assertNotNull(ImprintFragment.MAILTO_URI);
    }

    @Test
    void testMailtoUriStartsWithMailtoScheme() {
        assertTrue(ImprintFragment.MAILTO_URI.startsWith("mailto:"));
    }

    @Test
    void testMailtoUriContainsEmailAddress() {
        assertTrue(ImprintFragment.MAILTO_URI.contains(ImprintFragment.EMAIL_ADDRESS));
    }

    @Test
    void testMailtoUriValue() {
        assertEquals("mailto:self@idrinth.de", ImprintFragment.MAILTO_URI);
    }

    @Test
    void testMailtoUriMatchesEmailAddress() {
        assertEquals("mailto:" + ImprintFragment.EMAIL_ADDRESS, ImprintFragment.MAILTO_URI);
    }

    @Test
    void testVersionPrefixIsNotNull() {
        assertNotNull(ImprintFragment.VERSION_PREFIX);
    }

    @Test
    void testVersionPrefixValue() {
        assertEquals("Version ", ImprintFragment.VERSION_PREFIX);
    }

    @Test
    void testVersionPrefixEndsWithSpace() {
        assertTrue(ImprintFragment.VERSION_PREFIX.endsWith(" "));
    }

    @Test
    void testFormatVersionLabelWithValidVersion() {
        assertEquals("Version 1.0.0", ImprintFragment.formatVersionLabel("1.0.0"));
    }

    @Test
    void testFormatVersionLabelWithSnapshotVersion() {
        assertEquals("Version 0.1.0-SNAPSHOT", ImprintFragment.formatVersionLabel("0.1.0-SNAPSHOT"));
    }

    @Test
    void testFormatVersionLabelStartsWithPrefix() {
        assertTrue(ImprintFragment.formatVersionLabel("2.3.4").startsWith(ImprintFragment.VERSION_PREFIX));
    }

    @Test
    void testFormatVersionLabelWithNullVersion() {
        assertEquals("Version unknown", ImprintFragment.formatVersionLabel(null));
    }

    @Test
    void testFormatVersionLabelWithEmptyVersion() {
        assertEquals("Version unknown", ImprintFragment.formatVersionLabel(""));
    }

    @Test
    void testFormatVersionLabelWithSingleDigitVersion() {
        assertEquals("Version 1", ImprintFragment.formatVersionLabel("1"));
    }

    @Test
    void testFormatVersionLabelWithSemanticVersion() {
        String result = ImprintFragment.formatVersionLabel("3.2.1");
        assertTrue(result.contains("3.2.1"));
    }

    @Test
    void testFormatVersionLabelResultIsNotNull() {
        assertNotNull(ImprintFragment.formatVersionLabel("1.0.0"));
    }

    @Test
    void testFormatVersionLabelResultIsNotNullForNullInput() {
        assertNotNull(ImprintFragment.formatVersionLabel(null));
    }

    @Test
    void testFormatVersionLabelResultIsNotNullForEmptyInput() {
        assertNotNull(ImprintFragment.formatVersionLabel(""));
    }
}
