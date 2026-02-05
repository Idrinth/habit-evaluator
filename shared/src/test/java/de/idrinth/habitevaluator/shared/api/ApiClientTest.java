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
}
