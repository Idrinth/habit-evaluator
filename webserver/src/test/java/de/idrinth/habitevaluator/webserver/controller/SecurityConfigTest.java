package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.webserver.config.RequestIdFilter;
import de.idrinth.habitevaluator.webserver.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    private final SecurityConfig config = new SecurityConfig(new RequestIdFilter());

    @Test
    void testPasswordEncoderIsBCrypt() {
        PasswordEncoder encoder = config.passwordEncoder();
        assertNotNull(encoder);
        assertTrue(encoder instanceof BCryptPasswordEncoder);
    }

    @Test
    void testPasswordEncoderEncodesAndMatches() {
        PasswordEncoder encoder = config.passwordEncoder();

        String rawPassword = "testPassword123";
        String encoded = encoder.encode(rawPassword);

        assertNotEquals(rawPassword, encoded);
        assertTrue(encoder.matches(rawPassword, encoded));
        assertFalse(encoder.matches("wrongPassword", encoded));
    }
}
