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
}
