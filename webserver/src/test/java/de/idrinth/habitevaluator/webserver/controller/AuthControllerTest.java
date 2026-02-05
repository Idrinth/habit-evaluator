package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.webserver.dto.LoginRequest;
import de.idrinth.habitevaluator.webserver.dto.LoginResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthController controller;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        controller = new AuthController(userRepository, passwordEncoder);
        session = new MockHttpSession();
    }

    @Test
    void testLoginSuccess() {
        User user = new User("testuser", "encodedPassword");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        LoginRequest request = new LoginRequest("testuser", "password123");
        ResponseEntity<LoginResponse> response = controller.login(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("testuser", response.getBody().getUsername());
        assertEquals(user.getId(), response.getBody().getUserId());
        assertEquals(user.getId(), session.getAttribute("userId"));
        assertEquals("testuser", session.getAttribute("username"));
    }

    @Test
    void testLoginUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("unknown", "password123");
        ResponseEntity<LoginResponse> response = controller.login(request, session);

        assertEquals(400, response.getStatusCode().value());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid username or password", response.getBody().getMessage());
    }

    @Test
    void testLoginWrongPassword() {
        User user = new User("testuser", "encodedPassword");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "encodedPassword")).thenReturn(false);

        LoginRequest request = new LoginRequest("testuser", "wrongpass");
        ResponseEntity<LoginResponse> response = controller.login(request, session);

        assertEquals(400, response.getStatusCode().value());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid username or password", response.getBody().getMessage());
    }

    @Test
    void testLogout() {
        session.setAttribute("userId", "user-id");
        session.setAttribute("username", "testuser");

        ResponseEntity<LoginResponse> response = controller.logout(session);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Logged out successfully", response.getBody().getMessage());
        assertTrue(session.isInvalid());
    }

    @Test
    void testGetCurrentUserWhenLoggedIn() {
        session.setAttribute("userId", "user-id-123");
        session.setAttribute("username", "testuser");

        ResponseEntity<LoginResponse> response = controller.getCurrentUser(session);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("user-id-123", response.getBody().getUserId());
        assertEquals("testuser", response.getBody().getUsername());
    }

    @Test
    void testGetCurrentUserWhenNotLoggedIn() {
        ResponseEntity<LoginResponse> response = controller.getCurrentUser(session);

        assertEquals(400, response.getStatusCode().value());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Not logged in", response.getBody().getMessage());
    }
}
