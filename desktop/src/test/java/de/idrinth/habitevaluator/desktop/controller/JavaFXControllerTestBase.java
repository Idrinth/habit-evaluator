package de.idrinth.habitevaluator.desktop.controller;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Base class for JavaFX controller tests.
 * Initializes the JavaFX toolkit so that JavaFX controls can be instantiated
 * in tests without a running application.
 */
public abstract class JavaFXControllerTestBase {

    private static boolean toolkitInitialized = false;

    @BeforeAll
    static void initToolkit() throws Exception {
        if (!toolkitInitialized) {
            System.setProperty("glass.platform", "Monocle");
            System.setProperty("monocle.platform", "Headless");
            System.setProperty("prism.order", "sw");
            CountDownLatch latch = new CountDownLatch(1);
            try {
                Platform.startup(latch::countDown);
            } catch (IllegalStateException e) {
                // Toolkit already initialized
                latch.countDown();
            }
            latch.await(5, TimeUnit.SECONDS);
            toolkitInitialized = true;
        }
    }

    /**
     * Sets a field value on the target object by field name, bypassing access control.
     * Used to inject @FXML fields for testing without loading FXML files.
     */
    protected static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
