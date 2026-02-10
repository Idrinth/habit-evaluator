package de.idrinth.habitevaluator.webserver.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {

    @Test
    void testDefaultConstructor() {
        LoginRequest request = new LoginRequest();
        assertNull(request.getUsername());
        assertNull(request.getPassword());
    }

    @Test
    void testParameterizedConstructor() {
        LoginRequest request = new LoginRequest("admin", "secret");
        assertEquals("admin", request.getUsername());
        assertEquals("secret", request.getPassword());
    }

    @Test
    void testSetUsername() {
        LoginRequest request = new LoginRequest();
        request.setUsername("user1");
        assertEquals("user1", request.getUsername());
    }

    @Test
    void testSetPassword() {
        LoginRequest request = new LoginRequest();
        request.setPassword("pass123");
        assertEquals("pass123", request.getPassword());
    }
}
