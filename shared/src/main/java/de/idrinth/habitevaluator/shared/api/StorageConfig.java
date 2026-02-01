package de.idrinth.habitevaluator.shared.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

    private StorageMode storageMode;
    private String apiBaseUrl;
    private String apiUsername;
    private String apiPassword;

    private final File configFile;

    public StorageConfig(File configFile) {
        this.configFile = configFile;
        this.storageMode = StorageMode.LOCAL;
        this.apiBaseUrl = DEFAULT_API_BASE_URL;
        this.apiUsername = "";
        this.apiPassword = "";
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
            apiBaseUrl = props.getProperty("api.baseUrl", DEFAULT_API_BASE_URL);
            apiUsername = props.getProperty("api.username", "");
            apiPassword = props.getProperty("api.password", "");
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
        props.setProperty("api.baseUrl", apiBaseUrl != null ? apiBaseUrl : "");
        props.setProperty("api.username", apiUsername != null ? apiUsername : "");
        props.setProperty("api.password", apiPassword != null ? apiPassword : "");
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
}
