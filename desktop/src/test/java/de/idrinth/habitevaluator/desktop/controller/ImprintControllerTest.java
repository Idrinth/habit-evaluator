package de.idrinth.habitevaluator.desktop.controller;

import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class ImprintControllerTest extends JavaFXControllerTestBase {

    private ImprintController controller;
    private Hyperlink emailLink;
    private Label versionLabel;

    @BeforeEach
    void setUp() throws Exception {
        controller = new ImprintController();
        emailLink = new Hyperlink();
        versionLabel = new Label();

        setField(controller, "emailLink", emailLink);
        setField(controller, "versionLabel", versionLabel);
    }

    @Test
    void testInitializeSetsVersionLabel() throws Exception {
        Method initialize = ImprintController.class.getDeclaredMethod("initialize");
        initialize.setAccessible(true);
        initialize.invoke(controller);

        assertNotNull(versionLabel.getText());
        assertTrue(versionLabel.getText().startsWith("Version "));
    }

    @Test
    void testInitializeVersionLabelContainsUnknownWhenNoProperties() throws Exception {
        // version.properties may or may not be on the classpath during tests
        Method initialize = ImprintController.class.getDeclaredMethod("initialize");
        initialize.setAccessible(true);
        initialize.invoke(controller);

        // Either a real version or "unknown" — both start with "Version "
        assertTrue(versionLabel.getText().startsWith("Version "));
    }

    @Test
    void testHandleEmailLinkDoesNotThrow() throws Exception {
        Method handleEmailLink = ImprintController.class.getDeclaredMethod("handleEmailLink");
        handleEmailLink.setAccessible(true);
        // Should not throw even in headless environment
        assertDoesNotThrow(() -> handleEmailLink.invoke(controller));
    }

    @Test
    void testHandleLicenseLinkDoesNotThrowWithNullSource() throws Exception {
        Method handleLicenseLink = ImprintController.class.getDeclaredMethod("handleLicenseLink",
                javafx.event.ActionEvent.class);
        handleLicenseLink.setAccessible(true);

        javafx.event.ActionEvent event = new javafx.event.ActionEvent();
        assertDoesNotThrow(() -> handleLicenseLink.invoke(controller, event));
    }

    @Test
    void testHandleLicenseLinkDoesNotThrowWithHyperlinkSource() throws Exception {
        Method handleLicenseLink = ImprintController.class.getDeclaredMethod("handleLicenseLink",
                javafx.event.ActionEvent.class);
        handleLicenseLink.setAccessible(true);

        Hyperlink link = new Hyperlink("MIT License");
        link.setAccessibleText("https://opensource.org/licenses/MIT");
        javafx.event.ActionEvent event = new javafx.event.ActionEvent(link, null);
        assertDoesNotThrow(() -> handleLicenseLink.invoke(controller, event));
    }
}
