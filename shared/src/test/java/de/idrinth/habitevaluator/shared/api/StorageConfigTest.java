package de.idrinth.habitevaluator.shared.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class StorageConfigTest {

    @TempDir
    File tempDir;

    @Test
    void testIsUrlSecureHttps() {
        assertTrue(StorageConfig.isUrlSecure("https://example.com"));
    }

    @Test
    void testIsUrlSecureHttpLocalhost() {
        assertTrue(StorageConfig.isUrlSecure("http://localhost:8080"));
    }

    @Test
    void testIsUrlSecureHttp127001() {
        assertTrue(StorageConfig.isUrlSecure("http://127.0.0.1:8080"));
    }

    @Test
    void testIsUrlSecureHttpIpv6Loopback() {
        assertTrue(StorageConfig.isUrlSecure("http://[::1]:8080"));
    }

    @Test
    void testIsUrlSecureHttpRemoteFails() {
        assertFalse(StorageConfig.isUrlSecure("http://example.com"));
    }

    @Test
    void testIsUrlSecureNullFails() {
        assertFalse(StorageConfig.isUrlSecure(null));
    }

    @Test
    void testIsUrlSecureEmptyFails() {
        assertFalse(StorageConfig.isUrlSecure(""));
    }

    @Test
    void testIsUrlSecureCaseInsensitive() {
        assertTrue(StorageConfig.isUrlSecure("HTTPS://EXAMPLE.COM"));
        assertTrue(StorageConfig.isUrlSecure("HTTP://LOCALHOST:8080"));
    }

    @Test
    void testDefaultValues() {
        File configFile = new File(tempDir, "nonexistent.properties");
        StorageConfig config = new StorageConfig(configFile);

        assertEquals(StorageConfig.StorageMode.LOCAL, config.getStorageMode());
        assertEquals(StorageConfig.ThemeMode.SYSTEM, config.getThemeMode());
        assertEquals("system", config.getLanguage());
        assertEquals(StorageConfig.DEFAULT_API_BASE_URL, config.getApiBaseUrl());
        assertEquals("", config.getApiUsername());
        assertEquals("", config.getApiPassword());
        assertFalse(config.isBackupEnabled());
        assertEquals("", config.getBackupPassword());
        assertFalse(config.isFirstStartCompleted());
        assertFalse(config.isRemote());
        assertFalse(config.isCustomTranslationsEnabled());
        assertFalse(config.isGratitudeReminderEnabled());
        assertEquals("08:00", config.getGratitudeReminderTime());
    }

    @Test
    void testSaveAndLoad() throws IOException {
        File configFile = new File(tempDir, "config.properties");
        StorageConfig config = new StorageConfig(configFile);

        config.setStorageMode(StorageConfig.StorageMode.REMOTE);
        config.setThemeMode(StorageConfig.ThemeMode.DARK);
        config.setLanguage("de");
        config.setApiBaseUrl("https://api.example.com");
        config.setApiUsername("user1");
        config.setApiPassword("pass1");
        config.setBackupEnabled(true);
        config.setBackupPassword("backuppass");
        config.setFirstStartCompleted(true);
        config.setCustomTranslationsEnabled(true);
        config.save();

        // Load in a new instance
        StorageConfig loaded = new StorageConfig(configFile);
        assertEquals(StorageConfig.StorageMode.REMOTE, loaded.getStorageMode());
        assertEquals(StorageConfig.ThemeMode.DARK, loaded.getThemeMode());
        assertEquals("de", loaded.getLanguage());
        assertEquals("https://api.example.com", loaded.getApiBaseUrl());
        assertEquals("user1", loaded.getApiUsername());
        assertEquals("pass1", loaded.getApiPassword());
        assertTrue(loaded.isBackupEnabled());
        assertEquals("backuppass", loaded.getBackupPassword());
        assertTrue(loaded.isFirstStartCompleted());
        assertTrue(loaded.isRemote());
        assertTrue(loaded.isCustomTranslationsEnabled());
    }

    @Test
    void testIsRemote() {
        File configFile = new File(tempDir, "test.properties");
        StorageConfig config = new StorageConfig(configFile);

        assertFalse(config.isRemote());
        config.setStorageMode(StorageConfig.StorageMode.REMOTE);
        assertTrue(config.isRemote());
    }

    @Test
    void testGetEffectiveLanguageExplicit() {
        File configFile = new File(tempDir, "test.properties");
        StorageConfig config = new StorageConfig(configFile);
        config.setLanguage("fr");
        assertEquals("fr", config.getEffectiveLanguage());
    }

    @Test
    void testGetEffectiveLanguageSystemFallback() {
        File configFile = new File(tempDir, "test.properties");
        StorageConfig config = new StorageConfig(configFile);
        config.setLanguage("system");
        // Should return some language (depends on system locale, but shouldn't crash)
        assertNotNull(config.getEffectiveLanguage());
    }

    @Test
    void testGetEffectiveLanguageNullFallback() {
        File configFile = new File(tempDir, "test.properties");
        StorageConfig config = new StorageConfig(configFile);
        config.setLanguage(null);
        assertNotNull(config.getEffectiveLanguage());
    }

    @Test
    void testNullConfigFile() {
        StorageConfig config = new StorageConfig(null);
        // Should use defaults and not crash
        assertEquals(StorageConfig.StorageMode.LOCAL, config.getStorageMode());
        // Save should be a no-op
        config.save();
    }

    @Test
    void testLoadInvalidStorageMode() throws IOException {
        File configFile = new File(tempDir, "invalid.properties");
        Properties props = new Properties();
        props.setProperty("storage.mode", "INVALID_MODE");
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            props.store(fos, "test");
        }

        StorageConfig config = new StorageConfig(configFile);
        // Should fall back to LOCAL
        assertEquals(StorageConfig.StorageMode.LOCAL, config.getStorageMode());
    }

    @Test
    void testStorageModeEnum() {
        assertEquals(2, StorageConfig.StorageMode.values().length);
        assertEquals(StorageConfig.StorageMode.LOCAL, StorageConfig.StorageMode.valueOf("LOCAL"));
        assertEquals(StorageConfig.StorageMode.REMOTE, StorageConfig.StorageMode.valueOf("REMOTE"));
    }

    @Test
    void testGratitudeReminderSaveAndLoad() throws IOException {
        File configFile = new File(tempDir, "gratitude-config.properties");
        StorageConfig config = new StorageConfig(configFile);

        config.setGratitudeReminderEnabled(true);
        config.setGratitudeReminderTime("07:30");
        config.save();

        StorageConfig loaded = new StorageConfig(configFile);
        assertTrue(loaded.isGratitudeReminderEnabled());
        assertEquals("07:30", loaded.getGratitudeReminderTime());
    }

    @Test
    void testThemeModeEnum() {
        assertEquals(3, StorageConfig.ThemeMode.values().length);
        assertEquals(StorageConfig.ThemeMode.SYSTEM, StorageConfig.ThemeMode.valueOf("SYSTEM"));
        assertEquals(StorageConfig.ThemeMode.LIGHT, StorageConfig.ThemeMode.valueOf("LIGHT"));
        assertEquals(StorageConfig.ThemeMode.DARK, StorageConfig.ThemeMode.valueOf("DARK"));
    }
}
