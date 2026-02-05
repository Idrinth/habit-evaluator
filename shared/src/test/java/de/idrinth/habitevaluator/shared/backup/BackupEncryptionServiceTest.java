package de.idrinth.habitevaluator.shared.backup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class BackupEncryptionServiceTest {

    private BackupEncryptionService service;

    @BeforeEach
    void setUp() {
        service = new BackupEncryptionService();
    }

    @Test
    void testEncryptDecryptRoundTrip() throws BackupException {
        String original = "Hello, this is test data for backup!";
        byte[] plaintext = original.getBytes(StandardCharsets.UTF_8);
        String password = "securePassword123";

        byte[] encrypted = service.encrypt(plaintext, password);
        byte[] decrypted = service.decrypt(encrypted, password);

        assertEquals(original, new String(decrypted, StandardCharsets.UTF_8));
    }

    @Test
    void testEncryptProducesDifferentOutput() throws BackupException {
        byte[] plaintext = "test data".getBytes(StandardCharsets.UTF_8);
        String password = "password";

        byte[] encrypted1 = service.encrypt(plaintext, password);
        byte[] encrypted2 = service.encrypt(plaintext, password);

        // Due to random salt and IV, encrypted outputs should differ
        assertNotEquals(encrypted1.length == encrypted2.length
                && java.util.Arrays.equals(encrypted1, encrypted2), true);
    }

    @Test
    void testDecryptWithWrongPasswordFails() throws BackupException {
        byte[] plaintext = "sensitive data".getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = service.encrypt(plaintext, "correctPassword");

        assertThrows(BackupException.class, () ->
                service.decrypt(encrypted, "wrongPassword"));
    }

    @Test
    void testDecryptTooShortDataFails() {
        byte[] tooShort = new byte[10]; // Less than salt (16) + IV (12) + tag (16)
        assertThrows(BackupException.class, () ->
                service.decrypt(tooShort, "password"));
    }

    @Test
    void testEncryptDecryptEmptyData() throws BackupException {
        byte[] empty = new byte[0];
        String password = "password";

        byte[] encrypted = service.encrypt(empty, password);
        byte[] decrypted = service.decrypt(encrypted, password);

        assertEquals(0, decrypted.length);
    }

    @Test
    void testEncryptDecryptLargeData() throws BackupException {
        byte[] largeData = new byte[100_000];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }
        String password = "longTestPassword";

        byte[] encrypted = service.encrypt(largeData, password);
        byte[] decrypted = service.decrypt(encrypted, password);

        assertArrayEquals(largeData, decrypted);
    }

    @Test
    void testEncryptedDataLargerThanPlaintext() throws BackupException {
        byte[] plaintext = "test".getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = service.encrypt(plaintext, "password");

        // Encrypted should be larger: salt (16) + IV (12) + ciphertext + GCM tag (16)
        assertTrue(encrypted.length > plaintext.length);
        // At minimum: 16 (salt) + 12 (IV) + plaintext.length + 16 (GCM tag)
        assertTrue(encrypted.length >= 16 + 12 + plaintext.length + 16);
    }

    @Test
    void testDecryptCorruptedDataFails() throws BackupException {
        byte[] plaintext = "test data".getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = service.encrypt(plaintext, "password");

        // Corrupt a byte in the ciphertext area
        encrypted[encrypted.length - 1] ^= 0xFF;

        assertThrows(BackupException.class, () ->
                service.decrypt(encrypted, "password"));
    }
}
