package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.PlannerActivity;
import de.idrinth.habitevaluator.shared.model.PlannerGroup;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.model.WeekPlannerSlot;
import de.idrinth.habitevaluator.shared.model.SlotConfirmation;
import de.idrinth.habitevaluator.shared.repository.PlannerActivityRepository;
import de.idrinth.habitevaluator.shared.repository.PlannerGroupRepository;
import de.idrinth.habitevaluator.shared.repository.SlotConfirmationRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.shared.repository.WeekPlannerSlotRepository;
import de.idrinth.habitevaluator.shared.service.DayPlannerService;
import de.idrinth.habitevaluator.webserver.service.StatsCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DayPlannerControllerTest {

    private PlannerActivityRepository activityRepository;
    private PlannerGroupRepository groupRepository;
    private WeekPlannerSlotRepository slotRepository;
    private SlotConfirmationRepository confirmationRepository;
    private UserRepository userRepository;
    private DayPlannerService dayPlannerService;
    private StatsCacheService statsCacheService;
    private DayPlannerController controller;
    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        activityRepository = mock(PlannerActivityRepository.class);
        groupRepository = mock(PlannerGroupRepository.class);
        slotRepository = mock(WeekPlannerSlotRepository.class);
        confirmationRepository = mock(SlotConfirmationRepository.class);
        userRepository = mock(UserRepository.class);
        dayPlannerService = mock(DayPlannerService.class);
        statsCacheService = mock(StatsCacheService.class);
        controller = new DayPlannerController(
                activityRepository, groupRepository, slotRepository,
                confirmationRepository, userRepository, dayPlannerService,
                statsCacheService);
        session = new MockHttpSession();
        testUser = new User("testuser", "password");
        session.setAttribute("userId", testUser.getId());
    }

    // ── Groups ──

    @Test
    void testGetGroupsUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<List<PlannerGroup>> response = controller.getGroups(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetGroupsSuccess() {
        PlannerGroup group = new PlannerGroup("Fitness");
        when(groupRepository.findByUserId(testUser.getId())).thenReturn(List.of(group));

        ResponseEntity<List<PlannerGroup>> response = controller.getGroups(session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals("Fitness", response.getBody().get(0).getName());
    }

    @Test
    void testCreateGroupSuccess() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(groupRepository.save(any(PlannerGroup.class))).thenAnswer(i -> i.getArgument(0));

        PlannerGroup group = new PlannerGroup("Creative");
        ResponseEntity<?> response = controller.createGroup(group, session);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testCreateGroupUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<?> response = controller.createGroup(new PlannerGroup(), unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testCreateGroupUserNotFound() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());
        ResponseEntity<?> response = controller.createGroup(new PlannerGroup(), session);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testDeleteGroupSuccess() {
        PlannerGroup group = new PlannerGroup("Fitness");
        group.setUser(testUser);
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));

        ResponseEntity<Void> response = controller.deleteGroup(group.getId(), session);

        assertEquals(204, response.getStatusCode().value());
        verify(groupRepository).deleteById(group.getId());
    }

    @Test
    void testDeleteGroupNotFound() {
        when(groupRepository.findById("nonexistent")).thenReturn(Optional.empty());
        ResponseEntity<Void> response = controller.deleteGroup("nonexistent", session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteGroupNotOwned() {
        User otherUser = new User("other", "pass");
        PlannerGroup group = new PlannerGroup("Fitness");
        group.setUser(otherUser);
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));

        ResponseEntity<Void> response = controller.deleteGroup(group.getId(), session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteGroupUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<Void> response = controller.deleteGroup("some-id", unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    // ── Activities ──

    @Test
    void testGetActivitiesUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<List<PlannerActivity>> response = controller.getActivities(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetActivitiesSuccess() {
        PlannerActivity activity = new PlannerActivity("Morning Run");
        when(activityRepository.findByUserId(testUser.getId())).thenReturn(List.of(activity));

        ResponseEntity<List<PlannerActivity>> response = controller.getActivities(session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testCreateActivitySuccess() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(activityRepository.save(any(PlannerActivity.class))).thenAnswer(i -> i.getArgument(0));

        PlannerActivity activity = new PlannerActivity("Walk");
        ResponseEntity<?> response = controller.createActivity(activity, session);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testCreateActivityWithGroupsSuccess() {
        PlannerGroup group = new PlannerGroup("Fitness");
        group.setUser(testUser);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(activityRepository.save(any(PlannerActivity.class))).thenAnswer(i -> i.getArgument(0));

        PlannerActivity activity = new PlannerActivity("Morning Run");
        Set<PlannerGroup> groups = new HashSet<>();
        PlannerGroup ref = new PlannerGroup();
        ref.setId(group.getId());
        groups.add(ref);
        activity.setGroups(groups);

        ResponseEntity<?> response = controller.createActivity(activity, session);

        assertEquals(200, response.getStatusCode().value());
        PlannerActivity result = (PlannerActivity) response.getBody();
        assertNotNull(result.getGroups());
        assertEquals(1, result.getGroups().size());
        assertTrue(result.getGroups().contains(group));
    }

    @Test
    void testCreateActivityUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<?> response = controller.createActivity(new PlannerActivity(), unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testCreateActivityUserNotFound() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());
        ResponseEntity<?> response = controller.createActivity(new PlannerActivity(), session);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testUpdateActivitySuccess() {
        PlannerActivity existing = new PlannerActivity("Walk");
        existing.setUser(testUser);
        when(activityRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(activityRepository.save(any(PlannerActivity.class))).thenAnswer(i -> i.getArgument(0));

        PlannerActivity updated = new PlannerActivity("Morning Walk");
        ResponseEntity<?> response = controller.updateActivity(existing.getId(), updated, session);

        assertEquals(200, response.getStatusCode().value());
        PlannerActivity result = (PlannerActivity) response.getBody();
        assertEquals(existing.getId(), result.getId());
        assertEquals("Morning Walk", result.getName());
    }

    @Test
    void testUpdateActivityWithGroupAssignment() {
        PlannerGroup group = new PlannerGroup("Fitness");
        group.setUser(testUser);

        PlannerActivity existing = new PlannerActivity("Walk");
        existing.setUser(testUser);
        when(activityRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(activityRepository.save(any(PlannerActivity.class))).thenAnswer(i -> i.getArgument(0));

        PlannerActivity updated = new PlannerActivity("Morning Walk");
        Set<PlannerGroup> groups = new HashSet<>();
        PlannerGroup ref = new PlannerGroup();
        ref.setId(group.getId());
        groups.add(ref);
        updated.setGroups(groups);

        ResponseEntity<?> response = controller.updateActivity(existing.getId(), updated, session);

        assertEquals(200, response.getStatusCode().value());
        PlannerActivity result = (PlannerActivity) response.getBody();
        assertNotNull(result.getGroups());
        assertEquals(1, result.getGroups().size());
        assertTrue(result.getGroups().contains(group));
    }

    @Test
    void testUpdateActivityNotFound() {
        when(activityRepository.findById("nonexistent")).thenReturn(Optional.empty());
        PlannerActivity updated = new PlannerActivity("Walk");

        ResponseEntity<?> response = controller.updateActivity("nonexistent", updated, session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testUpdateActivityNotOwned() {
        User otherUser = new User("other", "pass");
        PlannerActivity existing = new PlannerActivity("Walk");
        existing.setUser(otherUser);
        when(activityRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

        PlannerActivity updated = new PlannerActivity("Run");
        ResponseEntity<?> response = controller.updateActivity(existing.getId(), updated, session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testUpdateActivityUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<?> response = controller.updateActivity("some-id", new PlannerActivity(), unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testDeleteActivitySuccess() {
        PlannerActivity activity = new PlannerActivity("Walk");
        activity.setUser(testUser);
        when(activityRepository.findById(activity.getId())).thenReturn(Optional.of(activity));

        ResponseEntity<Void> response = controller.deleteActivity(activity.getId(), session);

        assertEquals(204, response.getStatusCode().value());
        verify(activityRepository).deleteById(activity.getId());
    }

    @Test
    void testDeleteActivityNotFound() {
        when(activityRepository.findById("nonexistent")).thenReturn(Optional.empty());
        ResponseEntity<Void> response = controller.deleteActivity("nonexistent", session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteActivityNotOwned() {
        User otherUser = new User("other", "pass");
        PlannerActivity activity = new PlannerActivity("Walk");
        activity.setUser(otherUser);
        when(activityRepository.findById(activity.getId())).thenReturn(Optional.of(activity));

        ResponseEntity<Void> response = controller.deleteActivity(activity.getId(), session);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteActivityUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<Void> response = controller.deleteActivity("some-id", unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    // ── Slots ──

    @Test
    void testGetSlotsUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<List<WeekPlannerSlot>> response = controller.getSlots(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetSlotsSuccess() {
        when(slotRepository.findByUserId(testUser.getId())).thenReturn(List.of());
        ResponseEntity<List<WeekPlannerSlot>> response = controller.getSlots(session);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testCreateSlotUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<?> response = controller.createSlot(new WeekPlannerSlot(), unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testDeleteSlotUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<Void> response = controller.deleteSlot("some-id", unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testDeleteSlotNotFound() {
        when(slotRepository.findById("nonexistent")).thenReturn(Optional.empty());
        ResponseEntity<Void> response = controller.deleteSlot("nonexistent", session);
        assertEquals(404, response.getStatusCode().value());
    }

    // ── Confirmations ──

    @Test
    void testGetConfirmationsUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<List<SlotConfirmation>> response = controller.getConfirmations(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetConfirmationsSuccess() {
        when(confirmationRepository.findByUserId(testUser.getId())).thenReturn(List.of());
        ResponseEntity<List<SlotConfirmation>> response = controller.getConfirmations(session);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testCreateConfirmationUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<?> response = controller.createConfirmation(new SlotConfirmation(), unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    // ── Suggestion ──

    @Test
    void testSuggestActivityUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<?> response = controller.suggestActivity(1, 10, unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testSuggestActivityNoActivities() {
        when(slotRepository.findByUserIdAndDayOfWeek(testUser.getId(), 1)).thenReturn(List.of());
        when(activityRepository.findByUserId(testUser.getId())).thenReturn(List.of());
        when(dayPlannerService.suggestActivity(any(), any())).thenReturn(null);

        ResponseEntity<?> response = controller.suggestActivity(1, 10, session);

        assertEquals(200, response.getStatusCode().value());
    }

    // ── Week Overview ──

    @Test
    void testGetWeekOverviewUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<?> response = controller.getWeekOverview(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetWeekOverviewEmpty() {
        when(slotRepository.findByUserId(testUser.getId())).thenReturn(List.of());

        ResponseEntity<?> response = controller.getWeekOverview(session);

        assertEquals(200, response.getStatusCode().value());
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> body = (java.util.Map<String, Object>) response.getBody();
        assertNotNull(body);
        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Integer>> slots = (java.util.List<java.util.Map<String, Integer>>) body.get("slots");
        assertTrue(slots.isEmpty());
    }

    @Test
    void testGetWeekOverviewWithSlots() {
        PlannerGroup group = new PlannerGroup("Fitness");
        group.setUser(testUser);

        WeekPlannerSlot slot1 = new WeekPlannerSlot(1, 9);
        slot1.setUser(testUser);
        slot1.setGroups(Set.of(group));

        WeekPlannerSlot slot2 = new WeekPlannerSlot(3, 14);
        slot2.setUser(testUser);
        slot2.setGroups(Set.of(group));

        WeekPlannerSlot emptySlot = new WeekPlannerSlot(5, 10);
        emptySlot.setUser(testUser);
        emptySlot.setGroups(new HashSet<>());

        when(slotRepository.findByUserId(testUser.getId())).thenReturn(List.of(slot1, slot2, emptySlot));

        ResponseEntity<?> response = controller.getWeekOverview(session);

        assertEquals(200, response.getStatusCode().value());
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> body = (java.util.Map<String, Object>) response.getBody();
        assertNotNull(body);
        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Integer>> slots = (java.util.List<java.util.Map<String, Integer>>) body.get("slots");
        assertEquals(2, slots.size());
        assertEquals(1, slots.get(0).get("dayOfWeek"));
        assertEquals(9, slots.get(0).get("hour"));
        assertEquals(3, slots.get(1).get("dayOfWeek"));
        assertEquals(14, slots.get(1).get("hour"));
    }

    // ── Summary ──

    @Test
    void testGetWeekSummaryUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<?> response = controller.getWeekSummary(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetWeekSummarySuccess() {
        when(slotRepository.findByUserId(testUser.getId())).thenReturn(List.of());
        when(dayPlannerService.getWeekSlotSummary(any())).thenReturn(new int[]{3, 7});

        ResponseEntity<?> response = controller.getWeekSummary(session);

        assertEquals(200, response.getStatusCode().value());
    }
}
