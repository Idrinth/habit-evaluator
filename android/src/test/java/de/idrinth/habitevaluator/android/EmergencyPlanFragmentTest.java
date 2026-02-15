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
}
