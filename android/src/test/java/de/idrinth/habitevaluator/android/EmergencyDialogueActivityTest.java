package de.idrinth.habitevaluator.android;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmergencyDialogueActivityTest {

    @Test
    void testPhoneUriSchemeValue() {
        assertEquals("tel:", EmergencyDialogueActivity.PHONE_URI_SCHEME);
    }

    @Test
    void testPhoneUriSchemeStartsWithTel() {
        assertTrue(EmergencyDialogueActivity.PHONE_URI_SCHEME.startsWith("tel"));
    }

    @Test
    void testPhoneUriSchemeEndsWithColon() {
        assertTrue(EmergencyDialogueActivity.PHONE_URI_SCHEME.endsWith(":"));
    }

    @Test
    void testPhoneUriSchemeIsNotEmpty() {
        assertFalse(EmergencyDialogueActivity.PHONE_URI_SCHEME.isEmpty());
    }

    @Test
    void testPhoneUriSchemeCanBePrependedToNumber() {
        String phoneNumber = "1234567890";
        String uri = EmergencyDialogueActivity.PHONE_URI_SCHEME + phoneNumber;
        assertEquals("tel:1234567890", uri);
    }

    @Test
    void testPhoneUriSchemeWithInternationalNumber() {
        String phoneNumber = "+49123456789";
        String uri = EmergencyDialogueActivity.PHONE_URI_SCHEME + phoneNumber;
        assertTrue(uri.startsWith("tel:+"));
    }

    @Test
    void testPhoneUriSchemeIsLowerCase() {
        assertEquals(EmergencyDialogueActivity.PHONE_URI_SCHEME,
                EmergencyDialogueActivity.PHONE_URI_SCHEME.toLowerCase());
    }

    @Test
    void testPhoneUriSchemeHasNoSpaces() {
        assertFalse(EmergencyDialogueActivity.PHONE_URI_SCHEME.contains(" "));
    }
}
