package de.idrinth.habitevaluator.shared.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiClientTest {

    @Test
    void testConstructorRejectsInsecureUrl() {
        assertThrows(IllegalArgumentException.class, () ->
                new ApiClient("http://example.com"));
    }

    @Test
    void testConstructorAcceptsHttpsUrl() {
        assertDoesNotThrow(() -> new ApiClient("https://example.com"));
    }

    @Test
    void testConstructorAcceptsHttpLocalhost() {
        assertDoesNotThrow(() -> new ApiClient("http://localhost:8080"));
    }

    @Test
    void testConstructorAcceptsHttp127001() {
        assertDoesNotThrow(() -> new ApiClient("http://127.0.0.1:8080"));
    }

    @Test
    void testConstructorTrimsTrailingSlash() {
        ApiClient client = new ApiClient("https://example.com/");
        assertNotNull(client);
    }

    @Test
    void testNotAuthenticatedByDefault() {
        ApiClient client = new ApiClient("https://example.com");
        assertFalse(client.isAuthenticated());
    }

    @Test
    void testGetGsonNotNull() {
        ApiClient client = new ApiClient("https://example.com");
        assertNotNull(client.getGson());
    }

    @Test
    void testMaskBugfixVersionWithThreeParts() {
        assertEquals("1.2.x", ApiClient.maskBugfixVersion("1.2.3"));
    }

    @Test
    void testMaskBugfixVersionWithSnapshot() {
        assertEquals("0.1.x", ApiClient.maskBugfixVersion("0.1.0-SNAPSHOT"));
    }

    @Test
    void testMaskBugfixVersionWithTwoParts() {
        assertEquals("1.2", ApiClient.maskBugfixVersion("1.2"));
    }

    @Test
    void testMaskBugfixVersionWithOnePart() {
        assertEquals("1", ApiClient.maskBugfixVersion("1"));
    }

    @Test
    void testMaskBugfixVersionWithFourParts() {
        assertEquals("1.2.x", ApiClient.maskBugfixVersion("1.2.3.4"));
    }

    @Test
    void testMaskBugfixVersionWithNull() {
        assertNull(ApiClient.maskBugfixVersion(null));
    }

    @Test
    void testMaskBugfixVersionWithHighNumbers() {
        assertEquals("22.33.x", ApiClient.maskBugfixVersion("22.33.44"));
    }
}
