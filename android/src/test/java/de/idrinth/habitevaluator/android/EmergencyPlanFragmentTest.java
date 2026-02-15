package de.idrinth.habitevaluator.android;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmergencyPlanFragmentTest {

    @Test
    void testPhoneUriSchemeValue() {
        assertEquals("tel:", EmergencyPlanFragment.PHONE_URI_SCHEME);
    }

    @Test
    void testPhoneUriSchemeStartsWithTel() {
        assertTrue(EmergencyPlanFragment.PHONE_URI_SCHEME.startsWith("tel"));
    }

    @Test
    void testPhoneUriSchemeEndsWithColon() {
        assertTrue(EmergencyPlanFragment.PHONE_URI_SCHEME.endsWith(":"));
    }

    @Test
    void testPhoneUriSchemeIsNotEmpty() {
        assertFalse(EmergencyPlanFragment.PHONE_URI_SCHEME.isEmpty());
    }

    @Test
    void testPhoneUriSchemeCanBePrependedToNumber() {
        String phoneNumber = "1234567890";
        String uri = EmergencyPlanFragment.PHONE_URI_SCHEME + phoneNumber;
        assertEquals("tel:1234567890", uri);
    }

    @Test
    void testPhoneUriSchemeWithInternationalNumber() {
        String phoneNumber = "+49123456789";
        String uri = EmergencyPlanFragment.PHONE_URI_SCHEME + phoneNumber;
        assertTrue(uri.startsWith("tel:+"));
    }

    @Test
    void testPhoneUriSchemeIsLowerCase() {
        assertEquals(EmergencyPlanFragment.PHONE_URI_SCHEME,
                EmergencyPlanFragment.PHONE_URI_SCHEME.toLowerCase());
    }

    @Test
    void testPhoneUriSchemeHasNoSpaces() {
        assertFalse(EmergencyPlanFragment.PHONE_URI_SCHEME.contains(" "));
    }
}
