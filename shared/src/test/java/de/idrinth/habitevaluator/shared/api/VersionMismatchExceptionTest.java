package de.idrinth.habitevaluator.shared.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class VersionMismatchExceptionTest {

    @Test
    void testIsIOException() {
        VersionMismatchException exception = new VersionMismatchException("0.1.x", "0.2.x");
        assertInstanceOf(IOException.class, exception);
    }

    @Test
    void testMessageContainsBothVersions() {
        VersionMismatchException exception = new VersionMismatchException("0.1.x", "0.2.x");
        assertTrue(exception.getMessage().contains("0.1.x"));
        assertTrue(exception.getMessage().contains("0.2.x"));
    }

    @Test
    void testGetClientVersion() {
        VersionMismatchException exception = new VersionMismatchException("1.0.x", "2.0.x");
        assertEquals("1.0.x", exception.getClientVersion());
    }

    @Test
    void testGetServerVersion() {
        VersionMismatchException exception = new VersionMismatchException("1.0.x", "2.0.x");
        assertEquals("2.0.x", exception.getServerVersion());
    }

    @Test
    void testNullVersions() {
        VersionMismatchException exception = new VersionMismatchException(null, null);
        assertNull(exception.getClientVersion());
        assertNull(exception.getServerVersion());
    }
}
