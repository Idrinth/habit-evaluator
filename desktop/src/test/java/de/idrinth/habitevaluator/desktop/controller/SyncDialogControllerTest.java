package de.idrinth.habitevaluator.desktop.controller;

import de.idrinth.habitevaluator.shared.model.User;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class SyncDialogControllerTest extends JavaFXControllerTestBase {

    private SyncDialogController controller;

    @BeforeEach
    void setUp() throws Exception {
        controller = new SyncDialogController();

        setField(controller, "serverUrlField", new TextField());
        setField(controller, "usernameField", new TextField());
        setField(controller, "passwordField", new PasswordField());
        setField(controller, "statusLabel", new Label());
        setField(controller, "syncButton", new Button());
    }

    @Test
    void testInitialSyncedStateIsFalse() {
        assertFalse(controller.isSynced());
    }

    @Test
    void testSetCurrentUser() {
        User user = new User("testuser", "password");
        controller.setCurrentUser(user);
        // No exception means the setter works correctly
        assertFalse(controller.isSynced());
    }

    @Test
    void testHandleSyncWithEmptyFieldsShowsError() throws Exception {
        controller.setCurrentUser(new User("testuser", "password"));

        Method handleSync = SyncDialogController.class.getDeclaredMethod("handleSync");
        handleSync.setAccessible(true);
        handleSync.invoke(controller);

        Label statusLabel = getStatusLabel();
        assertEquals("Please fill in all fields", statusLabel.getText());
        assertFalse(controller.isSynced());
    }

    @Test
    void testHandleSyncWithEmptyUrlShowsError() throws Exception {
        controller.setCurrentUser(new User("testuser", "password"));
        getField("usernameField", TextField.class).setText("user");
        getField("passwordField", PasswordField.class).setText("pass");

        Method handleSync = SyncDialogController.class.getDeclaredMethod("handleSync");
        handleSync.setAccessible(true);
        handleSync.invoke(controller);

        assertEquals("Please fill in all fields", getStatusLabel().getText());
    }

    @Test
    void testHandleSyncWithEmptyUsernameShowsError() throws Exception {
        controller.setCurrentUser(new User("testuser", "password"));
        getField("serverUrlField", TextField.class).setText("https://example.com");
        getField("passwordField", PasswordField.class).setText("pass");

        Method handleSync = SyncDialogController.class.getDeclaredMethod("handleSync");
        handleSync.setAccessible(true);
        handleSync.invoke(controller);

        assertEquals("Please fill in all fields", getStatusLabel().getText());
    }

    @Test
    void testHandleSyncWithEmptyPasswordShowsError() throws Exception {
        controller.setCurrentUser(new User("testuser", "password"));
        getField("serverUrlField", TextField.class).setText("https://example.com");
        getField("usernameField", TextField.class).setText("user");

        Method handleSync = SyncDialogController.class.getDeclaredMethod("handleSync");
        handleSync.setAccessible(true);
        handleSync.invoke(controller);

        assertEquals("Please fill in all fields", getStatusLabel().getText());
    }

    @Test
    void testHandleSyncWithAllFieldsFilledStartsSync() throws Exception {
        controller.setCurrentUser(new User("testuser", "password"));
        getField("serverUrlField", TextField.class).setText("https://example.com");
        getField("usernameField", TextField.class).setText("user");
        getField("passwordField", PasswordField.class).setText("pass");

        Method handleSync = SyncDialogController.class.getDeclaredMethod("handleSync");
        handleSync.setAccessible(true);
        handleSync.invoke(controller);

        Label statusLabel = getStatusLabel();
        // When fields are filled, status should change from empty (either "Syncing..." or a failure message
        // since there is no real server, but it should NOT be the validation error)
        assertNotEquals("Please fill in all fields", statusLabel.getText());
    }

    @Test
    void testGetClientVersionReturnsValue() throws Exception {
        Method getClientVersion = SyncDialogController.class.getDeclaredMethod("getClientVersion");
        getClientVersion.setAccessible(true);

        String version = (String) getClientVersion.invoke(controller);
        assertNotNull(version);
        // Should return "unknown" or an actual version string
        assertFalse(version.isEmpty());
    }

    @Test
    void testHandleSyncDisablesSyncButton() throws Exception {
        controller.setCurrentUser(new User("testuser", "password"));
        getField("serverUrlField", TextField.class).setText("https://example.com");
        getField("usernameField", TextField.class).setText("user");
        getField("passwordField", PasswordField.class).setText("pass");

        Button syncButton = getField("syncButton", Button.class);
        assertFalse(syncButton.isDisable());

        Method handleSync = SyncDialogController.class.getDeclaredMethod("handleSync");
        handleSync.setAccessible(true);
        handleSync.invoke(controller);

        // After starting sync, button should be disabled
        assertTrue(syncButton.isDisable());
    }

    @Test
    void testHandleSyncWithWhitespaceOnlyFieldsShowsError() throws Exception {
        controller.setCurrentUser(new User("testuser", "password"));
        getField("serverUrlField", TextField.class).setText("   ");
        getField("usernameField", TextField.class).setText("user");
        getField("passwordField", PasswordField.class).setText("pass");

        Method handleSync = SyncDialogController.class.getDeclaredMethod("handleSync");
        handleSync.setAccessible(true);
        handleSync.invoke(controller);

        assertEquals("Please fill in all fields", getStatusLabel().getText());
    }

    @Test
    void testSetHabitRepository() {
        // Just verify setter doesn't throw
        assertDoesNotThrow(() -> controller.setHabitRepository(null));
    }

    private Label getStatusLabel() throws Exception {
        return getField("statusLabel", Label.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T getField(String name, Class<T> type) throws Exception {
        java.lang.reflect.Field field = SyncDialogController.class.getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(controller);
    }
}
