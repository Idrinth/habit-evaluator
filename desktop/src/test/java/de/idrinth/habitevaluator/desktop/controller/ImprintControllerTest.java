package de.idrinth.habitevaluator.desktop.controller;

import javafx.scene.control.Hyperlink;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class ImprintControllerTest extends JavaFXControllerTestBase {

    private ImprintController controller;
    private Hyperlink emailLink;

    @BeforeEach
    void setUp() throws Exception {
        controller = new ImprintController();
        emailLink = new Hyperlink();

        setField(controller, "emailLink", emailLink);
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
