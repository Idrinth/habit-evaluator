package de.idrinth.habitevaluator.shared.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Configuration for storage mode selection.
 * Supports switching between local database and remote API storage.
 * Settings are persisted to a properties file.
 */
public class StorageConfig {

    private static final Logger logger = LoggerFactory.getLogger(StorageConfig.class);

    public static final String DEFAULT_API_BASE_URL = "http://localhost:8080";

    public enum StorageMode {
        LOCAL, REMOTE
    }

    public enum ThemeMode {
        SYSTEM, LIGHT, DARK
    }

    public static final String LANGUAGE_SYSTEM = "system";

    private StorageMode storageMode;
    private ThemeMode themeMode;
    private String language;
    private boolean customTranslationsEnabled;
    private String apiBaseUrl;
    private String apiUsername;
    private String apiPassword;
    private boolean backupEnabled;
    private String backupPassword;
    private boolean firstStartCompleted;

    private final File configFile;

    public StorageConfig(File configFile) {
        this.configFile = configFile;
        this.storageMode = StorageMode.LOCAL;
        this.themeMode = ThemeMode.SYSTEM;
        this.language = LANGUAGE_SYSTEM;
        this.apiBaseUrl = DEFAULT_API_BASE_URL;
        this.apiUsername = "";
        this.apiPassword = "";
        this.backupEnabled = false;
        this.backupPassword = "";
        load();
    }

    public void load() {
        if (configFile == null || !configFile.exists()) {
            return;
        }
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(configFile)) {
            props.load(fis);
            String mode = props.getProperty("storage.mode", "LOCAL");
            storageMode = StorageMode.valueOf(mode);
            String theme = props.getProperty("theme.mode", "SYSTEM");
            try {
                themeMode = ThemeMode.valueOf(theme);
            } catch (IllegalArgumentException e) {
                themeMode = ThemeMode.SYSTEM;
            }
            language = props.getProperty("language", LANGUAGE_SYSTEM);
            customTranslationsEnabled = Boolean.parseBoolean(
                    props.getProperty("custom.translations.enabled", "false"));
            apiBaseUrl = props.getProperty("api.baseUrl", DEFAULT_API_BASE_URL);
            apiUsername = props.getProperty("api.username", "");
            apiPassword = props.getProperty("api.password", "");
            backupEnabled = Boolean.parseBoolean(
                    props.getProperty("backup.enabled", "false"));
            backupPassword = props.getProperty("backup.password", "");
            firstStartCompleted = Boolean.parseBoolean(
                    props.getProperty("first.start.completed", "false"));
        } catch (IOException e) {
            logger.warn("Failed to load storage config, using defaults", e);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid storage mode in config, using LOCAL", e);
            storageMode = StorageMode.LOCAL;
        }
    }

    public void save() {
        if (configFile == null) {
            return;
        }
        File parentDir = configFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        Properties props = new Properties();
        props.setProperty("storage.mode", storageMode.name());
        props.setProperty("theme.mode", themeMode != null ? themeMode.name() : "SYSTEM");
        props.setProperty("language", language != null ? language : LANGUAGE_SYSTEM);
        props.setProperty("custom.translations.enabled", String.valueOf(customTranslationsEnabled));
        props.setProperty("api.baseUrl", apiBaseUrl != null ? apiBaseUrl : "");
        props.setProperty("api.username", apiUsername != null ? apiUsername : "");
        props.setProperty("api.password", apiPassword != null ? apiPassword : "");
        props.setProperty("backup.enabled", String.valueOf(backupEnabled));
        props.setProperty("backup.password", backupPassword != null ? backupPassword : "");
        props.setProperty("first.start.completed", String.valueOf(firstStartCompleted));
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            props.store(fos, "Habit Evaluator Storage Configuration");
        } catch (IOException e) {
            logger.error("Failed to save storage config", e);
        }
    }

    public StorageMode getStorageMode() {
        return storageMode;
    }

    public void setStorageMode(StorageMode storageMode) {
        this.storageMode = storageMode;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getApiUsername() {
        return apiUsername;
    }

    public void setApiUsername(String apiUsername) {
        this.apiUsername = apiUsername;
    }

    public String getApiPassword() {
        return apiPassword;
    }

    public void setApiPassword(String apiPassword) {
        this.apiPassword = apiPassword;
    }

    public boolean isRemote() {
        return storageMode == StorageMode.REMOTE;
    }

    public ThemeMode getThemeMode() {
        return themeMode;
    }

    public void setThemeMode(ThemeMode themeMode) {
        this.themeMode = themeMode;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isCustomTranslationsEnabled() {
        return customTranslationsEnabled;
    }

    public void setCustomTranslationsEnabled(boolean customTranslationsEnabled) {
        this.customTranslationsEnabled = customTranslationsEnabled;
    }

    public boolean isBackupEnabled() {
        return backupEnabled;
    }

    public void setBackupEnabled(boolean backupEnabled) {
        this.backupEnabled = backupEnabled;
    }

    public String getBackupPassword() {
        return backupPassword;
    }

    public void setBackupPassword(String backupPassword) {
        this.backupPassword = backupPassword;
    }

    public boolean isFirstStartCompleted() {
        return firstStartCompleted;
    }

    public void setFirstStartCompleted(boolean firstStartCompleted) {
        this.firstStartCompleted = firstStartCompleted;
    }

    private static final List<String> SUPPORTED_LANGUAGES = Arrays.asList("en", "de", "es", "fr");

    /**
     * Resolves the effective language code. If set to "system", uses the system locale,
     * falling back to "en" if the system language is not supported.
     */
    public String getEffectiveLanguage() {
        if (language == null || LANGUAGE_SYSTEM.equals(language)) {
            String systemLang = Locale.getDefault().getLanguage();
            if (SUPPORTED_LANGUAGES.contains(systemLang)) {
                return systemLang;
            }
            return "en";
        }
        return language;
    }
}
