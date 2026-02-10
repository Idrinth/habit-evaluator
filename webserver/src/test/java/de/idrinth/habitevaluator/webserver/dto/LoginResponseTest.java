package de.idrinth.habitevaluator.webserver.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginResponseTest {

    @Test
    void testDefaultConstructor() {
        LoginResponse response = new LoginResponse();
        assertNull(response.getUserId());
        assertNull(response.getUsername());
        assertNull(response.getMessage());
        assertFalse(response.isSuccess());
    }

    @Test
    void testParameterizedConstructor() {
        LoginResponse response = new LoginResponse("uid-1", "admin", "OK", true);
        assertEquals("uid-1", response.getUserId());
        assertEquals("admin", response.getUsername());
        assertEquals("OK", response.getMessage());
        assertTrue(response.isSuccess());
    }

    @Test
    void testSuccessFactory() {
        LoginResponse response = LoginResponse.success("uid-1", "admin");
        assertEquals("uid-1", response.getUserId());
        assertEquals("admin", response.getUsername());
        assertEquals("Login successful", response.getMessage());
        assertTrue(response.isSuccess());
    }

    @Test
    void testFailureFactory() {
        LoginResponse response = LoginResponse.failure("Invalid credentials");
        assertNull(response.getUserId());
        assertNull(response.getUsername());
        assertEquals("Invalid credentials", response.getMessage());
        assertFalse(response.isSuccess());
    }

    @Test
    void testSetUserId() {
        LoginResponse response = new LoginResponse();
        response.setUserId("uid-2");
        assertEquals("uid-2", response.getUserId());
    }

    @Test
    void testSetUsername() {
        LoginResponse response = new LoginResponse();
        response.setUsername("user1");
        assertEquals("user1", response.getUsername());
    }

    @Test
    void testSetMessage() {
        LoginResponse response = new LoginResponse();
        response.setMessage("Welcome");
        assertEquals("Welcome", response.getMessage());
    }

    @Test
    void testSetSuccess() {
        LoginResponse response = new LoginResponse();
        response.setSuccess(true);
        assertTrue(response.isSuccess());
    }
}
