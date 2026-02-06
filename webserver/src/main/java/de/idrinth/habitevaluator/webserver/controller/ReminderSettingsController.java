package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.ReminderSettings;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.ReminderSettingsRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/reminder-settings")
public class ReminderSettingsController {

    private final ReminderSettingsRepository reminderSettingsRepository;
    private final UserRepository userRepository;

    public ReminderSettingsController(ReminderSettingsRepository reminderSettingsRepository,
                                      UserRepository userRepository) {
        this.reminderSettingsRepository = reminderSettingsRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<ReminderSettings> getSettings(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        ReminderSettings settings = reminderSettingsRepository.findByUserId(userId)
                .orElseGet(ReminderSettings::new);
        return ResponseEntity.ok(settings);
    }

    @PutMapping
    public ResponseEntity<ReminderSettings> updateSettings(@RequestBody ReminderSettings incoming,
                                                           HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        ReminderSettings settings = reminderSettingsRepository.findByUserId(userId)
                .orElseGet(ReminderSettings::new);
        settings.setUser(userOpt.get());
        settings.setSleepReminderEnabled(incoming.isSleepReminderEnabled());
        settings.setSleepReminderTime(incoming.getSleepReminderTime());
        settings.setDiaryReminderEnabled(incoming.isDiaryReminderEnabled());
        settings.setDiaryReminderTime(incoming.getDiaryReminderTime());
        settings.setEmotionReminderEnabled(incoming.isEmotionReminderEnabled());
        settings.setEmotionReminderCount(incoming.getEmotionReminderCount());
        settings.setWakingHoursStart(incoming.getWakingHoursStart());
        settings.setWakingHoursEnd(incoming.getWakingHoursEnd());
        return ResponseEntity.ok(reminderSettingsRepository.save(settings));
    }
}
