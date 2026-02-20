package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.Medication;
import de.idrinth.habitevaluator.shared.model.MedicationLog;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.MedicationLogRepository;
import de.idrinth.habitevaluator.shared.repository.MedicationRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.webserver.service.StatsCacheService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/medications")
public class MedicationController {

    private final MedicationRepository medicationRepository;
    private final MedicationLogRepository medicationLogRepository;
    private final UserRepository userRepository;
    private final StatsCacheService statsCacheService;

    public MedicationController(MedicationRepository medicationRepository,
                                MedicationLogRepository medicationLogRepository,
                                UserRepository userRepository,
                                StatsCacheService statsCacheService) {
        this.medicationRepository = medicationRepository;
        this.medicationLogRepository = medicationLogRepository;
        this.userRepository = userRepository;
        this.statsCacheService = statsCacheService;
    }

    @GetMapping
    public ResponseEntity<List<Medication>> getAllMedications(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(medicationRepository.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<?> createMedication(@RequestBody Medication medication, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        medication.setUser(userOpt.get());
        return ResponseEntity.ok(medicationRepository.save(medication));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMedication(@PathVariable String id, @RequestBody Medication medication, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<Medication> existingOpt = medicationRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Medication existing = existingOpt.get();
        if (existing.getUser() == null || !userId.equals(existing.getUser().getId())) {
            return ResponseEntity.notFound().build();
        }
        existing.setWikipediaLink(medication.getWikipediaLink());
        return ResponseEntity.ok(medicationRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedication(@PathVariable String id, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<Medication> medicationOpt = medicationRepository.findById(id);
        if (medicationOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Medication medication = medicationOpt.get();
        if (medication.getUser() == null || !userId.equals(medication.getUser().getId())) {
            return ResponseEntity.notFound().build();
        }
        medicationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/logs")
    public ResponseEntity<List<MedicationLog>> getAllLogs(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(medicationLogRepository.findByUserId(userId));
    }

    @PostMapping("/logs")
    public ResponseEntity<?> createLog(@RequestBody MedicationLog log, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        if (log.getMedication() == null || log.getMedication().getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        Optional<Medication> medicationOpt = medicationRepository.findById(log.getMedication().getId());
        if (medicationOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Medication medication = medicationOpt.get();
        if (medication.getUser() == null || !userId.equals(medication.getUser().getId())) {
            return ResponseEntity.badRequest().build();
        }
        log.setMedication(medication);
        log.setUser(userOpt.get());
        MedicationLog saved = medicationLogRepository.save(log);
        statsCacheService.invalidateUser(userId);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/logs/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable String id, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<MedicationLog> logOpt = medicationLogRepository.findById(id);
        if (logOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        MedicationLog log = logOpt.get();
        if (log.getUser() == null || !userId.equals(log.getUser().getId())) {
            return ResponseEntity.notFound().build();
        }
        medicationLogRepository.deleteById(id);
        statsCacheService.invalidateUser(userId);
        return ResponseEntity.noContent().build();
    }
}
