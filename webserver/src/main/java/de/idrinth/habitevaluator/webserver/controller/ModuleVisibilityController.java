package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.ModuleVisibility;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.ModuleVisibilityRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/module-visibility")
public class ModuleVisibilityController {

    private final ModuleVisibilityRepository moduleVisibilityRepository;
    private final UserRepository userRepository;

    public ModuleVisibilityController(ModuleVisibilityRepository moduleVisibilityRepository,
                                       UserRepository userRepository) {
        this.moduleVisibilityRepository = moduleVisibilityRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<ModuleVisibility> getSettings(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        ModuleVisibility settings = moduleVisibilityRepository.findByUserId(userId)
                .orElseGet(ModuleVisibility::new);
        return ResponseEntity.ok(settings);
    }

    @PutMapping
    public ResponseEntity<ModuleVisibility> updateSettings(@RequestBody ModuleVisibility incoming,
                                                            HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        ModuleVisibility settings = moduleVisibilityRepository.findByUserId(userId)
                .orElseGet(ModuleVisibility::new);
        settings.setUser(userOpt.get());
        settings.setDiaryVisible(incoming.isDiaryVisible());
        settings.setSleepVisible(incoming.isSleepVisible());
        settings.setEmotionsVisible(incoming.isEmotionsVisible());
        settings.setPointsVisible(incoming.isPointsVisible());
        settings.setStatisticsVisible(incoming.isStatisticsVisible());
        settings.setFoodLogVisible(incoming.isFoodLogVisible());
        settings.setSportLogVisible(incoming.isSportLogVisible());
        settings.setMedicationVisible(incoming.isMedicationVisible());
        settings.setBackupVisible(incoming.isBackupVisible());
        settings.setPdfExportVisible(incoming.isPdfExportVisible());
        return ResponseEntity.ok(moduleVisibilityRepository.save(settings));
    }
}
