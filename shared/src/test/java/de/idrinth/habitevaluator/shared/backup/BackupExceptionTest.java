package de.idrinth.habitevaluator.shared.backup;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BackupExceptionTest {

    @Test
    void testMessageConstructor() {
        BackupException ex = new BackupException("backup failed");
        assertEquals("backup failed", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void testMessageCauseConstructor() {
        RuntimeException cause = new RuntimeException("disk full");
        BackupException ex = new BackupException("backup failed", cause);
        assertEquals("backup failed", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void testIsException() {
        BackupException ex = new BackupException("test");
        assertInstanceOf(Exception.class, ex);
    }
}
