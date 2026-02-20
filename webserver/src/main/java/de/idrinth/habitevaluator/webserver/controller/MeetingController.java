package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.MeetingEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.MeetingEntryRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.webserver.service.StatsCacheService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    private final MeetingEntryRepository meetingEntryRepository;
    private final UserRepository userRepository;
    private final StatsCacheService statsCacheService;

    public MeetingController(MeetingEntryRepository meetingEntryRepository,
                             UserRepository userRepository,
                             StatsCacheService statsCacheService) {
        this.meetingEntryRepository = meetingEntryRepository;
        this.userRepository = userRepository;
        this.statsCacheService = statsCacheService;
    }

    @GetMapping
    public ResponseEntity<List<MeetingEntry>> getAllEntries(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(meetingEntryRepository.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<?> createEntry(@RequestBody MeetingEntry entry, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        entry.setUser(userOpt.get());
        MeetingEntry saved = meetingEntryRepository.save(entry);
        statsCacheService.invalidateUser(userId);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable String id, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<MeetingEntry> entryOpt = meetingEntryRepository.findById(id);
        if (entryOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        MeetingEntry entry = entryOpt.get();
        if (entry.getUser() == null || !userId.equals(entry.getUser().getId())) {
            return ResponseEntity.notFound().build();
        }
        meetingEntryRepository.deleteById(id);
        statsCacheService.invalidateUser(userId);
        return ResponseEntity.noContent().build();
    }
}
