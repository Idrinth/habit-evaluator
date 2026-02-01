package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.api.SyncData;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Handles bidirectional sync requests from remote clients.
 * Accepts incoming habit data, merges with server-side data, and returns the merged result.
 */
@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final HabitRepository habitRepository;
    private final UserRepository userRepository;

    public SyncController(HabitRepository habitRepository, UserRepository userRepository) {
        this.habitRepository = habitRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<SyncData> sync(@RequestBody SyncData incoming, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        User user = userOpt.get();
        List<Habit> serverHabits = habitRepository.findByUserId(userId);
        List<Habit> clientHabits = incoming.getHabits();

        List<Habit> merged = merge(serverHabits, clientHabits, user);

        return ResponseEntity.ok(new SyncData(merged));
    }

    private List<Habit> merge(List<Habit> serverHabits, List<Habit> clientHabits, User user) {
        Map<String, Habit> serverByName = serverHabits.stream()
                .collect(Collectors.toMap(Habit::getName, Function.identity(), (a, b) -> a));

        // Merge client habits into server
        for (Habit clientHabit : clientHabits) {
            Habit serverHabit = serverByName.get(clientHabit.getName());
            if (serverHabit == null) {
                // Client-only habit: create on server
                Habit newHabit = copyHabitForServer(clientHabit, user);
                habitRepository.save(newHabit);
                serverByName.put(newHabit.getName(), newHabit);
            } else {
                // Exists on both: merge entries
                mergeEntries(serverHabit, clientHabit);
                habitRepository.save(serverHabit);
            }
        }

        // Return all server habits (now includes merged data)
        return new ArrayList<>(habitRepository.findByUserId(user.getId()));
    }

    private Habit copyHabitForServer(Habit source, User user) {
        Habit habit = new Habit(source.getName(), source.getDescription());
        habit.setFrequencyType(source.getFrequencyType());
        habit.setTargetFrequency(source.getTargetFrequency());
        habit.setCategoryId(source.getCategoryId());
        habit.setPositiveScoring(source.isPositiveScoring());
        habit.setUser(user);
        for (HabitEntry sourceEntry : source.getEntries()) {
            HabitEntry entry = new HabitEntry();
            entry.setCompletedAt(sourceEntry.getCompletedAt());
            entry.setNotes(sourceEntry.getNotes());
            entry.setValue(sourceEntry.getValue());
            habit.addEntry(entry);
        }
        return habit;
    }

    private void mergeEntries(Habit server, Habit client) {
        Set<String> serverEntryIds = server.getEntries().stream()
                .map(HabitEntry::getId)
                .collect(Collectors.toSet());

        for (HabitEntry clientEntry : client.getEntries()) {
            if (!serverEntryIds.contains(clientEntry.getId())) {
                HabitEntry entry = new HabitEntry();
                entry.setCompletedAt(clientEntry.getCompletedAt());
                entry.setNotes(clientEntry.getNotes());
                entry.setValue(clientEntry.getValue());
                server.addEntry(entry);
            }
        }
    }
}
