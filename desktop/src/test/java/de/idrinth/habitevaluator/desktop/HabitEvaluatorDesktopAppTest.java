package de.idrinth.habitevaluator.desktop;

import de.idrinth.habitevaluator.desktop.controller.JavaFXControllerTestBase;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class HabitEvaluatorDesktopAppTest extends JavaFXControllerTestBase {

    @Test
    void testShouldUseDarkModeReturnsBooleanWithoutException() throws Exception {
        HabitEvaluatorDesktopApp app = new HabitEvaluatorDesktopApp();

        Method shouldUseDarkMode = HabitEvaluatorDesktopApp.class.getDeclaredMethod("shouldUseDarkMode");
        shouldUseDarkMode.setAccessible(true);

        // Should return a boolean without throwing, regardless of configuration
        Object result = shouldUseDarkMode.invoke(app);
        assertNotNull(result);
        assertTrue(result instanceof Boolean);
    }

    @Test
    void testIsSystemDarkModeReturnsBooleanWithoutException() throws Exception {
        HabitEvaluatorDesktopApp app = new HabitEvaluatorDesktopApp();

        Method isSystemDarkMode = HabitEvaluatorDesktopApp.class.getDeclaredMethod("isSystemDarkMode");
        isSystemDarkMode.setAccessible(true);

        Object result = isSystemDarkMode.invoke(app);
        assertNotNull(result);
        assertTrue(result instanceof Boolean);
    }

    @Test
    void testStopWithNullServicesDoesNotThrow() {
        HabitEvaluatorDesktopApp app = new HabitEvaluatorDesktopApp();

        // When both reminderService and mainController are null (not initialized),
        // stop should not throw
        assertDoesNotThrow(app::stop);
    }

    @Test
    void testStopWithReminderServiceStopsIt() throws Exception {
        HabitEvaluatorDesktopApp app = new HabitEvaluatorDesktopApp();

        java.io.File tempFile = java.io.File.createTempFile("test-config", ".properties");
        tempFile.deleteOnExit();
        de.idrinth.habitevaluator.shared.api.StorageConfig config =
                new de.idrinth.habitevaluator.shared.api.StorageConfig(tempFile);
        ReminderService reminderService = new ReminderService(config);

        java.lang.reflect.Field reminderField = HabitEvaluatorDesktopApp.class.getDeclaredField("reminderService");
        reminderField.setAccessible(true);
        reminderField.set(app, reminderService);

        // Stop should gracefully stop the reminder service
        assertDoesNotThrow(app::stop);
    }

    @Test
    void testConfigFilePathContainsHabitEvaluator() throws Exception {
        java.lang.reflect.Field configField = HabitEvaluatorDesktopApp.class.getDeclaredField("CONFIG_FILE");
        configField.setAccessible(true);
        String configPath = (String) configField.get(null);

        assertTrue(configPath.contains(".habit-evaluator"));
        assertTrue(configPath.contains("storage.properties"));
    }
}
