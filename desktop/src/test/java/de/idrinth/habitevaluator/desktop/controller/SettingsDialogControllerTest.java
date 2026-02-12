package de.idrinth.habitevaluator.desktop.controller;

import de.idrinth.habitevaluator.shared.api.StorageConfig;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class SettingsDialogControllerTest extends JavaFXControllerTestBase {

    private SettingsDialogController controller;
    private ToggleGroup storageToggleGroup;
    private RadioButton localRadio;
    private RadioButton remoteRadio;
    private VBox remoteSettingsPane;
    private TextField apiUrlField;
    private TextField apiUsernameField;
    private PasswordField apiPasswordField;
    private Label connectionStatusLabel;
    private ToggleGroup themeToggleGroup;
    private RadioButton themeSystemRadio;
    private RadioButton themeLightRadio;
    private RadioButton themeDarkRadio;
    private ToggleGroup languageToggleGroup;
    private RadioButton languageSystemRadio;
    private RadioButton languageEnRadio;
    private RadioButton languageDeRadio;
    private RadioButton languageEsRadio;
    private RadioButton languageFrRadio;
    private CheckBox customTranslationsCheckBox;
    private CheckBox backupEnabledCheckBox;
    private VBox backupSettingsPane;
    private PasswordField backupPasswordField;
    private PasswordField backupPasswordConfirmField;
    private Label backupStatusLabel;
    private Label restoreStatusLabel;
    private Label downloadStatusLabel;
    private Label restoreFromFileStatusLabel;
    private CheckBox sleepReminderCheckBox;
    private HBox sleepReminderTimePane;
    private TextField sleepReminderTimeField;
    private CheckBox diaryReminderCheckBox;
    private HBox diaryReminderTimePane;
    private TextField diaryReminderTimeField;
    private CheckBox emotionReminderCheckBox;
    private VBox emotionReminderPane;
    private Spinner<Integer> emotionReminderCountSpinner;
    private TextField wakingHoursStartField;
    private TextField wakingHoursEndField;
    private CheckBox diaryVisibleCheckBox;
    private CheckBox sleepVisibleCheckBox;
    private CheckBox emotionsVisibleCheckBox;
    private CheckBox pointsVisibleCheckBox;
    private CheckBox statisticsVisibleCheckBox;
    private CheckBox foodLogVisibleCheckBox;
    private CheckBox sportLogVisibleCheckBox;
    private CheckBox medicationVisibleCheckBox;
    private CheckBox backupVisibleCheckBox;
    private CheckBox pdfExportVisibleCheckBox;

    @TempDir
    File tempDir;

    @BeforeEach
    void setUp() throws Exception {
        controller = new SettingsDialogController();

        storageToggleGroup = new ToggleGroup();
        localRadio = new RadioButton("Local");
        localRadio.setToggleGroup(storageToggleGroup);
        remoteRadio = new RadioButton("Remote");
        remoteRadio.setToggleGroup(storageToggleGroup);
        remoteSettingsPane = new VBox();
        apiUrlField = new TextField();
        apiUsernameField = new TextField();
        apiPasswordField = new PasswordField();
        connectionStatusLabel = new Label();

        themeToggleGroup = new ToggleGroup();
        themeSystemRadio = new RadioButton("System");
        themeSystemRadio.setToggleGroup(themeToggleGroup);
        themeLightRadio = new RadioButton("Light");
        themeLightRadio.setToggleGroup(themeToggleGroup);
        themeDarkRadio = new RadioButton("Dark");
        themeDarkRadio.setToggleGroup(themeToggleGroup);

        languageToggleGroup = new ToggleGroup();
        languageSystemRadio = new RadioButton("System");
        languageSystemRadio.setToggleGroup(languageToggleGroup);
        languageEnRadio = new RadioButton("English");
        languageEnRadio.setToggleGroup(languageToggleGroup);
        languageDeRadio = new RadioButton("German");
        languageDeRadio.setToggleGroup(languageToggleGroup);
        languageEsRadio = new RadioButton("Spanish");
        languageEsRadio.setToggleGroup(languageToggleGroup);
        languageFrRadio = new RadioButton("French");
        languageFrRadio.setToggleGroup(languageToggleGroup);

        customTranslationsCheckBox = new CheckBox();
        backupEnabledCheckBox = new CheckBox();
        backupSettingsPane = new VBox();
        backupPasswordField = new PasswordField();
        backupPasswordConfirmField = new PasswordField();
        backupStatusLabel = new Label();
        restoreStatusLabel = new Label();
        downloadStatusLabel = new Label();
        restoreFromFileStatusLabel = new Label();
        sleepReminderCheckBox = new CheckBox();
        sleepReminderTimePane = new HBox();
        sleepReminderTimeField = new TextField();
        diaryReminderCheckBox = new CheckBox();
        diaryReminderTimePane = new HBox();
        diaryReminderTimeField = new TextField();
        emotionReminderCheckBox = new CheckBox();
        emotionReminderPane = new VBox();
        emotionReminderCountSpinner = new Spinner<>();
        emotionReminderCountSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 3));
        wakingHoursStartField = new TextField();
        wakingHoursEndField = new TextField();
        diaryVisibleCheckBox = new CheckBox();
        sleepVisibleCheckBox = new CheckBox();
        emotionsVisibleCheckBox = new CheckBox();
        pointsVisibleCheckBox = new CheckBox();
        statisticsVisibleCheckBox = new CheckBox();
        foodLogVisibleCheckBox = new CheckBox();
        sportLogVisibleCheckBox = new CheckBox();
        medicationVisibleCheckBox = new CheckBox();
        backupVisibleCheckBox = new CheckBox();
        pdfExportVisibleCheckBox = new CheckBox();

        setField(controller, "storageToggleGroup", storageToggleGroup);
        setField(controller, "localRadio", localRadio);
        setField(controller, "remoteRadio", remoteRadio);
        setField(controller, "remoteSettingsPane", remoteSettingsPane);
        setField(controller, "apiUrlField", apiUrlField);
        setField(controller, "apiUsernameField", apiUsernameField);
        setField(controller, "apiPasswordField", apiPasswordField);
        setField(controller, "connectionStatusLabel", connectionStatusLabel);
        setField(controller, "themeToggleGroup", themeToggleGroup);
        setField(controller, "themeSystemRadio", themeSystemRadio);
        setField(controller, "themeLightRadio", themeLightRadio);
        setField(controller, "themeDarkRadio", themeDarkRadio);
        setField(controller, "languageToggleGroup", languageToggleGroup);
        setField(controller, "languageSystemRadio", languageSystemRadio);
        setField(controller, "languageEnRadio", languageEnRadio);
        setField(controller, "languageDeRadio", languageDeRadio);
        setField(controller, "languageEsRadio", languageEsRadio);
        setField(controller, "languageFrRadio", languageFrRadio);
        setField(controller, "customTranslationsCheckBox", customTranslationsCheckBox);
        setField(controller, "backupEnabledCheckBox", backupEnabledCheckBox);
        setField(controller, "backupSettingsPane", backupSettingsPane);
        setField(controller, "backupPasswordField", backupPasswordField);
        setField(controller, "backupPasswordConfirmField", backupPasswordConfirmField);
        setField(controller, "backupStatusLabel", backupStatusLabel);
        setField(controller, "restoreStatusLabel", restoreStatusLabel);
        setField(controller, "downloadStatusLabel", downloadStatusLabel);
        setField(controller, "restoreFromFileStatusLabel", restoreFromFileStatusLabel);
        setField(controller, "sleepReminderCheckBox", sleepReminderCheckBox);
        setField(controller, "sleepReminderTimePane", sleepReminderTimePane);
        setField(controller, "sleepReminderTimeField", sleepReminderTimeField);
        setField(controller, "diaryReminderCheckBox", diaryReminderCheckBox);
        setField(controller, "diaryReminderTimePane", diaryReminderTimePane);
        setField(controller, "diaryReminderTimeField", diaryReminderTimeField);
        setField(controller, "emotionReminderCheckBox", emotionReminderCheckBox);
        setField(controller, "emotionReminderPane", emotionReminderPane);
        setField(controller, "emotionReminderCountSpinner", emotionReminderCountSpinner);
        setField(controller, "wakingHoursStartField", wakingHoursStartField);
        setField(controller, "wakingHoursEndField", wakingHoursEndField);
        setField(controller, "diaryVisibleCheckBox", diaryVisibleCheckBox);
        setField(controller, "sleepVisibleCheckBox", sleepVisibleCheckBox);
        setField(controller, "emotionsVisibleCheckBox", emotionsVisibleCheckBox);
        setField(controller, "pointsVisibleCheckBox", pointsVisibleCheckBox);
        setField(controller, "statisticsVisibleCheckBox", statisticsVisibleCheckBox);
        setField(controller, "foodLogVisibleCheckBox", foodLogVisibleCheckBox);
        setField(controller, "sportLogVisibleCheckBox", sportLogVisibleCheckBox);
        setField(controller, "medicationVisibleCheckBox", medicationVisibleCheckBox);
        setField(controller, "backupVisibleCheckBox", backupVisibleCheckBox);
        setField(controller, "pdfExportVisibleCheckBox", pdfExportVisibleCheckBox);
    }

    private StorageConfig createStorageConfig() {
        File configFile = new File(tempDir, "config.properties");
        StorageConfig config = new StorageConfig(configFile);
        return config;
    }

    @Test
    void testIsSavedInitiallyFalse() {
        assertFalse(controller.isSaved());
    }

    @Test
    void testInitializeRegistersListeners() {
        controller.initialize();
        // Should not throw
        assertNotNull(controller);
    }

    @Test
    void testSetStorageConfigLocalMode() {
        controller.initialize();
        StorageConfig config = createStorageConfig();
        config.setStorageMode(StorageConfig.StorageMode.LOCAL);
        config.setApiBaseUrl("https://example.com");
        config.setApiUsername("user");
        config.setApiPassword("pass");

        controller.setStorageConfig(config);

        assertTrue(localRadio.isSelected());
        assertFalse(remoteRadio.isSelected());
        assertTrue(remoteSettingsPane.isDisable());
        assertEquals("https://example.com", apiUrlField.getText());
        assertEquals("user", apiUsernameField.getText());
        assertEquals("pass", apiPasswordField.getText());
    }

    @Test
    void testSetStorageConfigRemoteMode() {
        controller.initialize();
        StorageConfig config = createStorageConfig();
        config.setStorageMode(StorageConfig.StorageMode.REMOTE);
        config.setApiBaseUrl("https://api.example.com");
        config.setApiUsername("remoteuser");
        config.setApiPassword("remotepass");

        controller.setStorageConfig(config);

        assertFalse(localRadio.isSelected());
        assertTrue(remoteRadio.isSelected());
        assertFalse(remoteSettingsPane.isDisable());
        assertEquals("https://api.example.com", apiUrlField.getText());
    }

    @Test
    void testSetStorageConfigThemeLight() {
        controller.initialize();
        StorageConfig config = createStorageConfig();
        config.setThemeMode(StorageConfig.ThemeMode.LIGHT);

        controller.setStorageConfig(config);

        assertTrue(themeLightRadio.isSelected());
        assertFalse(themeDarkRadio.isSelected());
        assertFalse(themeSystemRadio.isSelected());
    }

    @Test
    void testSetStorageConfigThemeDark() {
        controller.initialize();
        StorageConfig config = createStorageConfig();
        config.setThemeMode(StorageConfig.ThemeMode.DARK);

        controller.setStorageConfig(config);

        assertTrue(themeDarkRadio.isSelected());
        assertFalse(themeLightRadio.isSelected());
    }

    @Test
    void testSetStorageConfigThemeSystem() {
        controller.initialize();
        StorageConfig config = createStorageConfig();
        config.setThemeMode(StorageConfig.ThemeMode.SYSTEM);

        controller.setStorageConfig(config);

        assertTrue(themeSystemRadio.isSelected());
    }

    @Test
    void testSetStorageConfigLanguageEnglish() {
        controller.initialize();
        StorageConfig config = createStorageConfig();
        config.setLanguage("en");

        controller.setStorageConfig(config);

        assertTrue(languageEnRadio.isSelected());
    }

    @Test
    void testSetStorageConfigLanguageGerman() {
        controller.initialize();
        StorageConfig config = createStorageConfig();
        config.setLanguage("de");

        controller.setStorageConfig(config);

        assertTrue(languageDeRadio.isSelected());
    }

    @Test
    void testSetStorageConfigLanguageSpanish() {
        controller.initialize();
        StorageConfig config = createStorageConfig();
        config.setLanguage("es");

        controller.setStorageConfig(config);

        assertTrue(languageEsRadio.isSelected());
    }

    @Test
    void testSetStorageConfigLanguageFrench() {
        controller.initialize();
        StorageConfig config = createStorageConfig();
        config.setLanguage("fr");

        controller.setStorageConfig(config);

        assertTrue(languageFrRadio.isSelected());
    }

    @Test
    void testSetStorageConfigLanguageSystem() {
        controller.initialize();
        StorageConfig config = createStorageConfig();
        config.setLanguage(StorageConfig.LANGUAGE_SYSTEM);

        controller.setStorageConfig(config);

        assertTrue(languageSystemRadio.isSelected());
    }

    @Test
    void testSetStorageConfigBackupEnabled() {
        controller.initialize();
        StorageConfig config = createStorageConfig();
        config.setBackupEnabled(true);
        config.setBackupPassword("secret");

        controller.setStorageConfig(config);

        assertTrue(backupEnabledCheckBox.isSelected());
        assertFalse(backupSettingsPane.isDisable());
        assertEquals("secret", backupPasswordField.getText());
        assertEquals("secret", backupPasswordConfirmField.getText());
    }

    @Test
    void testSetStorageConfigBackupDisabled() {
        controller.initialize();
        StorageConfig config = createStorageConfig();
        config.setBackupEnabled(false);

        controller.setStorageConfig(config);

        assertFalse(backupEnabledCheckBox.isSelected());
        assertTrue(backupSettingsPane.isDisable());
    }

    @Test
    void testSetStorageConfigReminderSettings() {
        controller.initialize();
        StorageConfig config = createStorageConfig();
        config.setSleepReminderEnabled(true);
        config.setSleepReminderTime("22:00");
        config.setDiaryReminderEnabled(true);
        config.setDiaryReminderTime("20:00");
        config.setEmotionReminderEnabled(true);
        config.setEmotionReminderCount(5);
        config.setWakingHoursStart("07:00");
        config.setWakingHoursEnd("23:00");

        controller.setStorageConfig(config);

        assertTrue(sleepReminderCheckBox.isSelected());
        assertEquals("22:00", sleepReminderTimeField.getText());
        assertTrue(diaryReminderCheckBox.isSelected());
        assertEquals("20:00", diaryReminderTimeField.getText());
        assertTrue(emotionReminderCheckBox.isSelected());
        assertEquals(5, emotionReminderCountSpinner.getValue());
        assertEquals("07:00", wakingHoursStartField.getText());
        assertEquals("23:00", wakingHoursEndField.getText());
    }

    @Test
    void testHandleTestConnectionWithEmptyFieldsShowsError() throws Exception {
        controller.initialize();

        java.lang.reflect.Method handleTestConnection =
                SettingsDialogController.class.getDeclaredMethod("handleTestConnection");
        handleTestConnection.setAccessible(true);
        handleTestConnection.invoke(controller);

        assertEquals("Please fill in all fields", connectionStatusLabel.getText());
    }

    @Test
    void testHandleTestConnectionWithInsecureUrlShowsError() throws Exception {
        controller.initialize();
        apiUrlField.setText("http://example.com");
        apiUsernameField.setText("user");
        apiPasswordField.setText("pass");

        java.lang.reflect.Method handleTestConnection =
                SettingsDialogController.class.getDeclaredMethod("handleTestConnection");
        handleTestConnection.setAccessible(true);
        handleTestConnection.invoke(controller);

        assertTrue(connectionStatusLabel.getText().contains("HTTPS"));
    }
}
