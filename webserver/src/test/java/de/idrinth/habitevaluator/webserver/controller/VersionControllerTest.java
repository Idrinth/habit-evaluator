package de.idrinth.habitevaluator.webserver.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VersionControllerTest {

    @Test
    void testGetVersionReturnsOk() {
        VersionController controller = new VersionController("1.2.3");
        ResponseEntity<Map<String, String>> response = controller.getVersion();
        assertEquals(200, response.getStatusCode().value());
        assertEquals("1.2.x", response.getBody().get("version"));
    }

    @Test
    void testMaskBugfixVersionWithThreeParts() {
        assertEquals("1.2.x", VersionController.maskBugfixVersion("1.2.3"));
    }

    @Test
    void testMaskBugfixVersionWithSnapshot() {
        assertEquals("0.1.x", VersionController.maskBugfixVersion("0.1.0-SNAPSHOT"));
    }

    @Test
    void testMaskBugfixVersionWithTwoParts() {
        assertEquals("1.2", VersionController.maskBugfixVersion("1.2"));
    }

    @Test
    void testMaskBugfixVersionWithOnePart() {
        assertEquals("1", VersionController.maskBugfixVersion("1"));
    }

    @Test
    void testMaskBugfixVersionWithFourParts() {
        assertEquals("1.2.x", VersionController.maskBugfixVersion("1.2.3.4"));
    }

    @Test
    void testMaskBugfixVersionWithHighNumbers() {
        assertEquals("22.33.x", VersionController.maskBugfixVersion("22.33.44"));
    }
}
