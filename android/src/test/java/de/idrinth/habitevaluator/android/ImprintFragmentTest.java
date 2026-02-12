package de.idrinth.habitevaluator.android;

import org.junit.Test;

import static org.junit.Assert.*;

public class ImprintFragmentTest {

    @Test
    public void testEmailAddressIsNotNull() {
        assertNotNull(ImprintFragment.EMAIL_ADDRESS);
    }

    @Test
    public void testEmailAddressIsNotEmpty() {
        assertFalse(ImprintFragment.EMAIL_ADDRESS.isEmpty());
    }

    @Test
    public void testEmailAddressContainsAtSymbol() {
        assertTrue(ImprintFragment.EMAIL_ADDRESS.contains("@"));
    }

    @Test
    public void testEmailAddressValue() {
        assertEquals("self@idrinth.de", ImprintFragment.EMAIL_ADDRESS);
    }

    @Test
    public void testMailtoUriIsNotNull() {
        assertNotNull(ImprintFragment.MAILTO_URI);
    }

    @Test
    public void testMailtoUriStartsWithMailtoScheme() {
        assertTrue(ImprintFragment.MAILTO_URI.startsWith("mailto:"));
    }

    @Test
    public void testMailtoUriContainsEmailAddress() {
        assertTrue(ImprintFragment.MAILTO_URI.contains(ImprintFragment.EMAIL_ADDRESS));
    }

    @Test
    public void testMailtoUriValue() {
        assertEquals("mailto:self@idrinth.de", ImprintFragment.MAILTO_URI);
    }

    @Test
    public void testMailtoUriMatchesEmailAddress() {
        assertEquals("mailto:" + ImprintFragment.EMAIL_ADDRESS, ImprintFragment.MAILTO_URI);
    }

    @Test
    public void testVersionPrefixIsNotNull() {
        assertNotNull(ImprintFragment.VERSION_PREFIX);
    }

    @Test
    public void testVersionPrefixValue() {
        assertEquals("Version ", ImprintFragment.VERSION_PREFIX);
    }

    @Test
    public void testVersionPrefixEndsWithSpace() {
        assertTrue(ImprintFragment.VERSION_PREFIX.endsWith(" "));
    }

    @Test
    public void testFormatVersionLabelWithValidVersion() {
        assertEquals("Version 1.0.0", ImprintFragment.formatVersionLabel("1.0.0"));
    }

    @Test
    public void testFormatVersionLabelWithSnapshotVersion() {
        assertEquals("Version 0.1.0-SNAPSHOT", ImprintFragment.formatVersionLabel("0.1.0-SNAPSHOT"));
    }

    @Test
    public void testFormatVersionLabelStartsWithPrefix() {
        assertTrue(ImprintFragment.formatVersionLabel("2.3.4").startsWith(ImprintFragment.VERSION_PREFIX));
    }

    @Test
    public void testFormatVersionLabelWithNullVersion() {
        assertEquals("Version unknown", ImprintFragment.formatVersionLabel(null));
    }

    @Test
    public void testFormatVersionLabelWithEmptyVersion() {
        assertEquals("Version unknown", ImprintFragment.formatVersionLabel(""));
    }

    @Test
    public void testFormatVersionLabelWithSingleDigitVersion() {
        assertEquals("Version 1", ImprintFragment.formatVersionLabel("1"));
    }

    @Test
    public void testFormatVersionLabelWithSemanticVersion() {
        String result = ImprintFragment.formatVersionLabel("3.2.1");
        assertTrue(result.contains("3.2.1"));
    }

    @Test
    public void testFormatVersionLabelResultIsNotNull() {
        assertNotNull(ImprintFragment.formatVersionLabel("1.0.0"));
    }

    @Test
    public void testFormatVersionLabelResultIsNotNullForNullInput() {
        assertNotNull(ImprintFragment.formatVersionLabel(null));
    }

    @Test
    public void testFormatVersionLabelResultIsNotNullForEmptyInput() {
        assertNotNull(ImprintFragment.formatVersionLabel(""));
    }
}
