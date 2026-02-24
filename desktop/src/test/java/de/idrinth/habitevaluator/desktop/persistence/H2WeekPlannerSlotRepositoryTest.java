package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.PlannerGroup;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.model.WeekPlannerSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class H2WeekPlannerSlotRepositoryTest extends H2RepositoryTestBase {

    private H2WeekPlannerSlotRepository slotRepository;
    private H2PlannerGroupRepository groupRepository;
    private H2UserRepository userRepository;
    private User testUser;

    @BeforeEach
    void setUp() {
        slotRepository = new H2WeekPlannerSlotRepository();
        groupRepository = new H2PlannerGroupRepository();
        userRepository = new H2UserRepository();

        for (WeekPlannerSlot slot : slotRepository.findAll()) {
            slotRepository.deleteById(slot.getId());
        }
        for (PlannerGroup group : groupRepository.findAll()) {
            groupRepository.deleteById(group.getId());
        }
        for (User user : userRepository.findAll()) {
            userRepository.deleteById(user.getId());
        }

        testUser = new User("slotuser", "password123");
        userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        WeekPlannerSlot slot = new WeekPlannerSlot(1, 9);
        slot.setUser(testUser);
        WeekPlannerSlot saved = slotRepository.save(slot);

        assertNotNull(saved);
        Optional<WeekPlannerSlot> found = slotRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(1, found.get().getDayOfWeek());
        assertEquals(9, found.get().getHour());
    }

    @Test
    void testSaveUpdatesExisting() {
        WeekPlannerSlot slot = new WeekPlannerSlot(1, 9);
        slot.setUser(testUser);
        slotRepository.save(slot);

        slot.setHour(10);
        slotRepository.save(slot);

        Optional<WeekPlannerSlot> found = slotRepository.findById(slot.getId());
        assertTrue(found.isPresent());
        assertEquals(10, found.get().getHour());
    }

    @Test
    void testFindByIdReturnsEmptyForNonExistent() {
        Optional<WeekPlannerSlot> found = slotRepository.findById("nonexistent-id");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        WeekPlannerSlot s1 = new WeekPlannerSlot(1, 9);
        s1.setUser(testUser);
        WeekPlannerSlot s2 = new WeekPlannerSlot(1, 10);
        s2.setUser(testUser);
        slotRepository.save(s1);
        slotRepository.save(s2);

        List<WeekPlannerSlot> all = slotRepository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testDeleteById() {
        WeekPlannerSlot slot = new WeekPlannerSlot(1, 9);
        slot.setUser(testUser);
        slotRepository.save(slot);

        assertTrue(slotRepository.findById(slot.getId()).isPresent());
        slotRepository.deleteById(slot.getId());
        assertFalse(slotRepository.findById(slot.getId()).isPresent());
    }

    @Test
    void testDeleteByIdNonExistentDoesNotThrow() {
        assertDoesNotThrow(() -> slotRepository.deleteById("nonexistent-id"));
    }

    @Test
    void testExistsById() {
        WeekPlannerSlot slot = new WeekPlannerSlot(1, 9);
        slot.setUser(testUser);
        slotRepository.save(slot);

        assertTrue(slotRepository.existsById(slot.getId()));
        assertFalse(slotRepository.existsById("nonexistent-id"));
    }

    @Test
    void testFindByUserId() {
        WeekPlannerSlot s1 = new WeekPlannerSlot(1, 9);
        s1.setUser(testUser);
        slotRepository.save(s1);

        User otherUser = new User("otherslotuser", "password123");
        userRepository.save(otherUser);
        WeekPlannerSlot s2 = new WeekPlannerSlot(2, 10);
        s2.setUser(otherUser);
        slotRepository.save(s2);

        List<WeekPlannerSlot> userSlots = slotRepository.findByUserId(testUser.getId());
        assertEquals(1, userSlots.size());
        assertEquals(1, userSlots.get(0).getDayOfWeek());
    }

    @Test
    void testFindByUserIdAndDayOfWeek() {
        WeekPlannerSlot s1 = new WeekPlannerSlot(1, 9);
        s1.setUser(testUser);
        WeekPlannerSlot s2 = new WeekPlannerSlot(1, 14);
        s2.setUser(testUser);
        WeekPlannerSlot s3 = new WeekPlannerSlot(2, 9);
        s3.setUser(testUser);
        slotRepository.save(s1);
        slotRepository.save(s2);
        slotRepository.save(s3);

        List<WeekPlannerSlot> mondaySlots = slotRepository.findByUserIdAndDayOfWeek(testUser.getId(), 1);
        assertEquals(2, mondaySlots.size());

        List<WeekPlannerSlot> tuesdaySlots = slotRepository.findByUserIdAndDayOfWeek(testUser.getId(), 2);
        assertEquals(1, tuesdaySlots.size());
    }

    @Test
    void testSaveWithGroups() {
        PlannerGroup group = new PlannerGroup("Fitness", "Physical activities");
        group.setUser(testUser);
        groupRepository.save(group);

        WeekPlannerSlot slot = new WeekPlannerSlot(1, 9);
        slot.setUser(testUser);
        java.util.Set<PlannerGroup> groups = new java.util.HashSet<>();
        groups.add(group);
        slot.setGroups(groups);
        slotRepository.save(slot);

        Optional<WeekPlannerSlot> found = slotRepository.findById(slot.getId());
        assertTrue(found.isPresent());
        assertNotNull(found.get().getGroups());
        assertFalse(found.get().getGroups().isEmpty());
        assertTrue(found.get().getGroups().stream().anyMatch(g -> g.getId().equals(group.getId())));
    }

    @Test
    void testFindByUserIdReturnsEmptyForNonExistentUser() {
        List<WeekPlannerSlot> slots = slotRepository.findByUserId("nonexistent-user-id");
        assertTrue(slots.isEmpty());
    }

    @Test
    void testFindByUserIdAndDayOfWeekReturnsEmptyForNonExistent() {
        List<WeekPlannerSlot> slots = slotRepository.findByUserIdAndDayOfWeek("nonexistent-user-id", 1);
        assertTrue(slots.isEmpty());
    }

    @Test
    void testSaveAndFindByIdWithDuration() {
        WeekPlannerSlot slot = new WeekPlannerSlot(1, 9, 3);
        slot.setUser(testUser);
        slotRepository.save(slot);

        Optional<WeekPlannerSlot> found = slotRepository.findById(slot.getId());
        assertTrue(found.isPresent());
        assertEquals(3, found.get().getDuration());
    }

    @Test
    void testDefaultDuration() {
        WeekPlannerSlot slot = new WeekPlannerSlot(1, 9);
        slot.setUser(testUser);
        slotRepository.save(slot);

        Optional<WeekPlannerSlot> found = slotRepository.findById(slot.getId());
        assertTrue(found.isPresent());
        assertEquals(1, found.get().getDuration());
    }
}
