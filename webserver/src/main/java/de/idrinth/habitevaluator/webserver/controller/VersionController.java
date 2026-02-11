package de.idrinth.habitevaluator.webserver.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/version")
public class VersionController {

    private final String version;

    public VersionController(@Value("${app.version}") String fullVersion) {
        this.version = maskBugfixVersion(fullVersion);
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> getVersion() {
        return ResponseEntity.ok(Collections.singletonMap("version", version));
    }

    static String maskBugfixVersion(String fullVersion) {
        int firstDot = fullVersion.indexOf('.');
        if (firstDot < 0) {
            return fullVersion;
        }
        int secondDot = fullVersion.indexOf('.', firstDot + 1);
        if (secondDot < 0) {
            return fullVersion;
        }
        return fullVersion.substring(0, secondDot) + ".x";
    }
}
