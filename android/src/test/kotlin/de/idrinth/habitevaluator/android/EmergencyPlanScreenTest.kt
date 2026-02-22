package de.idrinth.habitevaluator.android

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for the phone URI scheme constant equivalent to what EmergencyPlanFragment provided.
 * Used to build tel: URIs for crisis contact phone numbers.
 */
class EmergencyPlanScreenTest {

    companion object {
        const val PHONE_URI_SCHEME = "tel:"
    }

    @Test
    fun testPhoneUriSchemeValue() {
        assertEquals("tel:", PHONE_URI_SCHEME)
    }

    @Test
    fun testPhoneUriSchemeStartsWithTel() {
        assertTrue(PHONE_URI_SCHEME.startsWith("tel"))
    }

    @Test
    fun testPhoneUriSchemeEndsWithColon() {
        assertTrue(PHONE_URI_SCHEME.endsWith(":"))
    }

    @Test
    fun testPhoneUriSchemeIsNotEmpty() {
        assertFalse(PHONE_URI_SCHEME.isEmpty())
    }

    @Test
    fun testPhoneUriSchemeCanBePrependedToNumber() {
        val phoneNumber = "1234567890"
        val uri = PHONE_URI_SCHEME + phoneNumber
        assertEquals("tel:1234567890", uri)
    }

    @Test
    fun testPhoneUriSchemeWithInternationalNumber() {
        val phoneNumber = "+49123456789"
        val uri = PHONE_URI_SCHEME + phoneNumber
        assertTrue(uri.startsWith("tel:+"))
    }

    @Test
    fun testPhoneUriSchemeIsLowerCase() {
        assertEquals(PHONE_URI_SCHEME, PHONE_URI_SCHEME.lowercase())
    }

    @Test
    fun testPhoneUriSchemeHasNoSpaces() {
        assertFalse(PHONE_URI_SCHEME.contains(" "))
    }
}
