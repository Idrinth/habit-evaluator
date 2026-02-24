package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.PlannerActivity;
import de.idrinth.habitevaluator.shared.model.PlannerGroup;
import de.idrinth.habitevaluator.shared.model.SlotConfirmation;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.model.WeekPlannerSlot;
import de.idrinth.habitevaluator.shared.repository.PlannerActivityRepository;
import de.idrinth.habitevaluator.shared.repository.PlannerGroupRepository;
import de.idrinth.habitevaluator.shared.repository.SlotConfirmationRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.shared.repository.WeekPlannerSlotRepository;
import de.idrinth.habitevaluator.shared.service.DayPlannerService;
import de.idrinth.habitevaluator.webserver.service.StatsCacheService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/day-planner")
public class DayPlannerController {

    private final PlannerActivityRepository activityRepository;
    private final PlannerGroupRepository groupRepository;
    private final WeekPlannerSlotRepository slotRepository;
    private final SlotConfirmationRepository confirmationRepository;
    private final UserRepository userRepository;
    private final DayPlannerService dayPlannerService;
    private final StatsCacheService statsCacheService;

    public DayPlannerController(
            PlannerActivityRepository activityRepository,
            PlannerGroupRepository groupRepository,
            WeekPlannerSlotRepository slotRepository,
            SlotConfirmationRepository confirmationRepository,
            UserRepository userRepository,
            DayPlannerService dayPlannerService,
            StatsCacheService statsCacheService
    ) {
        this.activityRepository = activityRepository;
        this.groupRepository = groupRepository;
        this.slotRepository = slotRepository;
        this.confirmationRepository = confirmationRepository;
        this.userRepository = userRepository;
        this.dayPlannerService = dayPlannerService;
        this.statsCacheService = statsCacheService;
    }

    // ── Groups ──

    @GetMapping("/groups")
    public ResponseEntity<List<PlannerGroup>> getGroups(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(groupRepository.findByUserId(userId));
    }

    @PostMapping("/groups")
    public ResponseEntity<?> createGroup(@RequestBody PlannerGroup group, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        group.setUser(userOpt.get());
        return ResponseEntity.ok(groupRepository.save(group));
    }

    @DeleteMapping("/groups/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable String id, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<PlannerGroup> groupOpt = groupRepository.findById(id);
        if (groupOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (groupOpt.get().getUser() == null || !userId.equals(groupOpt.get().getUser().getId())) {
            return ResponseEntity.notFound().build();
        }
        groupRepository.deleteById(id);
        statsCacheService.invalidateUser(userId);
        return ResponseEntity.noContent().build();
    }

    // ── Activities ──

    @GetMapping("/activities")
    public ResponseEntity<List<PlannerActivity>> getActivities(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(activityRepository.findByUserId(userId));
    }

    @PostMapping("/activities")
    public ResponseEntity<?> createActivity(@RequestBody PlannerActivity activity, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        activity.setUser(userOpt.get());
        // Resolve group references
        if (activity.getGroups() != null && !activity.getGroups().isEmpty()) {
            Set<PlannerGroup> resolved = new java.util.HashSet<>();
            for (PlannerGroup g : activity.getGroups()) {
                groupRepository.findById(g.getId()).ifPresent(resolved::add);
            }
            activity.setGroups(resolved);
        }
        return ResponseEntity.ok(activityRepository.save(activity));
    }

    @PutMapping("/activities/{id}")
    public ResponseEntity<?> updateActivity(@PathVariable String id, @RequestBody PlannerActivity activity, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<PlannerActivity> existingOpt = activityRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        PlannerActivity existing = existingOpt.get();
        if (existing.getUser() == null || !userId.equals(existing.getUser().getId())) {
            return ResponseEntity.notFound().build();
        }
        activity.setId(id);
        activity.setUser(existing.getUser());
        activity.setCreatedAt(existing.getCreatedAt());
        if (activity.getGroups() != null && !activity.getGroups().isEmpty()) {
            Set<PlannerGroup> resolved = new java.util.HashSet<>();
            for (PlannerGroup g : activity.getGroups()) {
                groupRepository.findById(g.getId()).ifPresent(resolved::add);
            }
            activity.setGroups(resolved);
        }
        return ResponseEntity.ok(activityRepository.save(activity));
    }

    @DeleteMapping("/activities/{id}")
    public ResponseEntity<Void> deleteActivity(@PathVariable String id, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<PlannerActivity> activityOpt = activityRepository.findById(id);
        if (activityOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (activityOpt.get().getUser() == null || !userId.equals(activityOpt.get().getUser().getId())) {
            return ResponseEntity.notFound().build();
        }
        activityRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── Slots ──

    @GetMapping("/slots")
    public ResponseEntity<List<WeekPlannerSlot>> getSlots(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(slotRepository.findByUserId(userId));
    }

    @PostMapping("/slots")
    public ResponseEntity<?> createSlot(@RequestBody WeekPlannerSlot slot, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        slot.setUser(userOpt.get());
        if (slot.getGroups() != null && !slot.getGroups().isEmpty()) {
            Set<PlannerGroup> resolved = new java.util.HashSet<>();
            for (PlannerGroup g : slot.getGroups()) {
                groupRepository.findById(g.getId()).ifPresent(resolved::add);
            }
            slot.setGroups(resolved);
        }
        return ResponseEntity.ok(slotRepository.save(slot));
    }

    @DeleteMapping("/slots/{id}")
    public ResponseEntity<Void> deleteSlot(@PathVariable String id, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<WeekPlannerSlot> slotOpt = slotRepository.findById(id);
        if (slotOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (slotOpt.get().getUser() == null || !userId.equals(slotOpt.get().getUser().getId())) {
            return ResponseEntity.notFound().build();
        }
        slotRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── Confirmations ──

    @GetMapping("/confirmations")
    public ResponseEntity<List<SlotConfirmation>> getConfirmations(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(confirmationRepository.findByUserId(userId));
    }

    @PostMapping("/confirmations")
    public ResponseEntity<?> createConfirmation(@RequestBody SlotConfirmation confirmation, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        confirmation.setUser(userOpt.get());
        if (confirmation.getSlot() != null && confirmation.getSlot().getId() != null) {
            slotRepository.findById(confirmation.getSlot().getId()).ifPresent(confirmation::setSlot);
        }
        if (confirmation.getActivity() != null && confirmation.getActivity().getId() != null) {
            activityRepository.findById(confirmation.getActivity().getId()).ifPresent(confirmation::setActivity);
        }
        if (confirmation.getGroup() != null && confirmation.getGroup().getId() != null) {
            groupRepository.findById(confirmation.getGroup().getId()).ifPresent(confirmation::setGroup);
        }
        SlotConfirmation saved = confirmationRepository.save(confirmation);
        statsCacheService.invalidateUser(userId);
        return ResponseEntity.ok(saved);
    }

    // ── Suggestion ──

    @GetMapping("/suggest")
    public ResponseEntity<?> suggestActivity(
            @RequestParam int dayOfWeek,
            @RequestParam int hour,
            HttpSession session
    ) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        List<WeekPlannerSlot> slots = slotRepository.findByUserIdAndDayOfWeek(userId, dayOfWeek);
        List<WeekPlannerSlot> slotsForHour = slots.stream()
                .filter(s -> s.getHour() == hour)
                .toList();
        List<PlannerActivity> allActivities = activityRepository.findByUserId(userId);
        PlannerActivity suggested = dayPlannerService.suggestActivity(slotsForHour, allActivities);
        if (suggested == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "No activities available for this time slot");
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.ok(suggested);
    }

    // ── Summary ──

    @GetMapping("/summary")
    public ResponseEntity<?> getWeekSummary(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        List<WeekPlannerSlot> slots = slotRepository.findByUserId(userId);
        int[] summary = dayPlannerService.getWeekSlotSummary(slots);
        Map<String, Integer> response = new HashMap<>();
        response.put("filledSlots", summary[0]);
        response.put("totalSlots", summary[1]);
        return ResponseEntity.ok(response);
    }
}
