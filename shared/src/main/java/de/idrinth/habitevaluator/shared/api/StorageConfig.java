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

    public static final String DEFAULT_API_BASE_URL = "https://localhost:8080";

    /**
     * Checks whether the given URL is acceptable for remote connections.
     * Remote servers must use HTTPS. Only localhost URLs are allowed over plain HTTP.
     *
     * @param url the URL to validate
     * @return true if the URL uses HTTPS or targets localhost over HTTP
     */
    public static boolean isUrlSecure(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        String lower = url.toLowerCase(Locale.ROOT);
        if (lower.startsWith("https://")) {
            return true;
        }
        if (lower.startsWith("http://")) {
            String hostPart = lower.substring("http://".length());
            return hostPart.startsWith("localhost") || hostPart.startsWith("127.0.0.1")
                    || hostPart.startsWith("[::1]");
        }
        return false;
    }

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
    private boolean sleepReminderEnabled;
    private String sleepReminderTime;
    private boolean diaryReminderEnabled;
    private String diaryReminderTime;
    private boolean emotionReminderEnabled;
    private int emotionReminderCount;
    private String wakingHoursStart;
    private String wakingHoursEnd;
    private boolean diaryVisible;
    private boolean sleepVisible;
    private boolean emotionsVisible;
    private boolean pointsVisible;
    private boolean statisticsVisible;
    private boolean foodLogVisible;
    private boolean sportLogVisible;
    private boolean medicationVisible;
    private boolean backupVisible;
    private boolean pdfExportVisible;

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
        this.sleepReminderEnabled = false;
        this.sleepReminderTime = "08:00";
        this.diaryReminderEnabled = false;
        this.diaryReminderTime = "20:00";
        this.emotionReminderEnabled = false;
        this.emotionReminderCount = 3;
        this.wakingHoursStart = "07:00";
        this.wakingHoursEnd = "22:00";
        this.diaryVisible = true;
        this.sleepVisible = true;
        this.emotionsVisible = true;
        this.pointsVisible = true;
        this.statisticsVisible = true;
        this.foodLogVisible = true;
        this.sportLogVisible = true;
        this.medicationVisible = true;
        this.backupVisible = true;
        this.pdfExportVisible = true;
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
            sleepReminderEnabled = Boolean.parseBoolean(
                    props.getProperty("reminder.sleep.enabled", "false"));
            sleepReminderTime = props.getProperty("reminder.sleep.time", "08:00");
            diaryReminderEnabled = Boolean.parseBoolean(
                    props.getProperty("reminder.diary.enabled", "false"));
            diaryReminderTime = props.getProperty("reminder.diary.time", "20:00");
            emotionReminderEnabled = Boolean.parseBoolean(
                    props.getProperty("reminder.emotion.enabled", "false"));
            try {
                emotionReminderCount = Integer.parseInt(
                        props.getProperty("reminder.emotion.count", "3"));
            } catch (NumberFormatException e2) {
                emotionReminderCount = 3;
            }
            wakingHoursStart = props.getProperty("reminder.waking.start", "07:00");
            wakingHoursEnd = props.getProperty("reminder.waking.end", "22:00");
            diaryVisible = Boolean.parseBoolean(
                    props.getProperty("module.diary.visible", "true"));
            sleepVisible = Boolean.parseBoolean(
                    props.getProperty("module.sleep.visible", "true"));
            emotionsVisible = Boolean.parseBoolean(
                    props.getProperty("module.emotions.visible", "true"));
            pointsVisible = Boolean.parseBoolean(
                    props.getProperty("module.points.visible", "true"));
            statisticsVisible = Boolean.parseBoolean(
                    props.getProperty("module.statistics.visible", "true"));
            foodLogVisible = Boolean.parseBoolean(
                    props.getProperty("module.food_log.visible", "true"));
            sportLogVisible = Boolean.parseBoolean(
                    props.getProperty("module.sport_log.visible", "true"));
            medicationVisible = Boolean.parseBoolean(
                    props.getProperty("module.medication.visible", "true"));
            backupVisible = Boolean.parseBoolean(
                    props.getProperty("module.backup.visible", "true"));
            pdfExportVisible = Boolean.parseBoolean(
                    props.getProperty("module.pdf_export.visible", "true"));
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
        props.setProperty("reminder.sleep.enabled", String.valueOf(sleepReminderEnabled));
        props.setProperty("reminder.sleep.time", sleepReminderTime != null ? sleepReminderTime : "08:00");
        props.setProperty("reminder.diary.enabled", String.valueOf(diaryReminderEnabled));
        props.setProperty("reminder.diary.time", diaryReminderTime != null ? diaryReminderTime : "20:00");
        props.setProperty("reminder.emotion.enabled", String.valueOf(emotionReminderEnabled));
        props.setProperty("reminder.emotion.count", String.valueOf(emotionReminderCount));
        props.setProperty("reminder.waking.start", wakingHoursStart != null ? wakingHoursStart : "07:00");
        props.setProperty("reminder.waking.end", wakingHoursEnd != null ? wakingHoursEnd : "22:00");
        props.setProperty("module.diary.visible", String.valueOf(diaryVisible));
        props.setProperty("module.sleep.visible", String.valueOf(sleepVisible));
        props.setProperty("module.emotions.visible", String.valueOf(emotionsVisible));
        props.setProperty("module.points.visible", String.valueOf(pointsVisible));
        props.setProperty("module.statistics.visible", String.valueOf(statisticsVisible));
        props.setProperty("module.food_log.visible", String.valueOf(foodLogVisible));
        props.setProperty("module.sport_log.visible", String.valueOf(sportLogVisible));
        props.setProperty("module.medication.visible", String.valueOf(medicationVisible));
        props.setProperty("module.backup.visible", String.valueOf(backupVisible));
        props.setProperty("module.pdf_export.visible", String.valueOf(pdfExportVisible));
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

    public boolean isSleepReminderEnabled() {
        return sleepReminderEnabled;
    }

    public void setSleepReminderEnabled(boolean sleepReminderEnabled) {
        this.sleepReminderEnabled = sleepReminderEnabled;
    }

    public String getSleepReminderTime() {
        return sleepReminderTime;
    }

    public void setSleepReminderTime(String sleepReminderTime) {
        this.sleepReminderTime = sleepReminderTime;
    }

    public boolean isDiaryReminderEnabled() {
        return diaryReminderEnabled;
    }

    public void setDiaryReminderEnabled(boolean diaryReminderEnabled) {
        this.diaryReminderEnabled = diaryReminderEnabled;
    }

    public String getDiaryReminderTime() {
        return diaryReminderTime;
    }

    public void setDiaryReminderTime(String diaryReminderTime) {
        this.diaryReminderTime = diaryReminderTime;
    }

    public boolean isEmotionReminderEnabled() {
        return emotionReminderEnabled;
    }

    public void setEmotionReminderEnabled(boolean emotionReminderEnabled) {
        this.emotionReminderEnabled = emotionReminderEnabled;
    }

    public int getEmotionReminderCount() {
        return emotionReminderCount;
    }

    public void setEmotionReminderCount(int emotionReminderCount) {
        this.emotionReminderCount = emotionReminderCount;
    }

    public String getWakingHoursStart() {
        return wakingHoursStart;
    }

    public void setWakingHoursStart(String wakingHoursStart) {
        this.wakingHoursStart = wakingHoursStart;
    }

    public String getWakingHoursEnd() {
        return wakingHoursEnd;
    }

    public void setWakingHoursEnd(String wakingHoursEnd) {
        this.wakingHoursEnd = wakingHoursEnd;
    }

    public boolean isDiaryVisible() {
        return diaryVisible;
    }

    public void setDiaryVisible(boolean diaryVisible) {
        this.diaryVisible = diaryVisible;
    }

    public boolean isSleepVisible() {
        return sleepVisible;
    }

    public void setSleepVisible(boolean sleepVisible) {
        this.sleepVisible = sleepVisible;
    }

    public boolean isEmotionsVisible() {
        return emotionsVisible;
    }

    public void setEmotionsVisible(boolean emotionsVisible) {
        this.emotionsVisible = emotionsVisible;
    }

    public boolean isPointsVisible() {
        return pointsVisible;
    }

    public void setPointsVisible(boolean pointsVisible) {
        this.pointsVisible = pointsVisible;
    }

    public boolean isStatisticsVisible() {
        return statisticsVisible;
    }

    public void setStatisticsVisible(boolean statisticsVisible) {
        this.statisticsVisible = statisticsVisible;
    }

    public boolean isFoodLogVisible() {
        return foodLogVisible;
    }

    public void setFoodLogVisible(boolean foodLogVisible) {
        this.foodLogVisible = foodLogVisible;
    }

    public boolean isSportLogVisible() {
        return sportLogVisible;
    }

    public void setSportLogVisible(boolean sportLogVisible) {
        this.sportLogVisible = sportLogVisible;
    }

    public boolean isMedicationVisible() {
        return medicationVisible;
    }

    public void setMedicationVisible(boolean medicationVisible) {
        this.medicationVisible = medicationVisible;
    }

    public boolean isBackupVisible() {
        return backupVisible;
    }

    public void setBackupVisible(boolean backupVisible) {
        this.backupVisible = backupVisible;
    }

    public boolean isPdfExportVisible() {
        return pdfExportVisible;
    }

    public void setPdfExportVisible(boolean pdfExportVisible) {
        this.pdfExportVisible = pdfExportVisible;
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
