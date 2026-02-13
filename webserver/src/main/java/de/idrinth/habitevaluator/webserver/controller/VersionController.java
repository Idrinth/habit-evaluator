package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.api.ApiClient;
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
        this.version = ApiClient.maskBugfixVersion(fullVersion);
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> getVersion() {
        return ResponseEntity.ok(Collections.singletonMap("version", version));
    }
}
