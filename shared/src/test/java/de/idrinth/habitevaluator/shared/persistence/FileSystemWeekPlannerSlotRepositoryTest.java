package de.idrinth.habitevaluator.shared.persistence;

import de.idrinth.habitevaluator.shared.model.PlannerGroup;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.model.WeekPlannerSlot;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemWeekPlannerSlotRepositoryTest {

    private File tempDir;
    private FileSystemPlannerGroupRepository groupRepository;
    private FileSystemWeekPlannerSlotRepository repository;

    @BeforeEach
    void setUp() {
        tempDir = new File(System.getProperty("java.io.tmpdir"), "week-planner-slot-test-" + System.nanoTime());
        tempDir.mkdirs();
        groupRepository = new FileSystemPlannerGroupRepository(tempDir);
        repository = new FileSystemWeekPlannerSlotRepository(tempDir);
        repository.setPlannerGroupRepository(groupRepository);
    }

    @AfterEach
    void tearDown() {
        File[] files = tempDir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
        tempDir.delete();
    }

    @Test
    void testSaveAndFindById() {
        WeekPlannerSlot slot = new WeekPlannerSlot(1, 9);
        repository.save(slot);

        Optional<WeekPlannerSlot> found = repository.findById(slot.getId());
        assertTrue(found.isPresent());
        assertEquals(1, found.get().getDayOfWeek());
        assertEquals(9, found.get().getHour());
    }

    @Test
    void testFindByIdNotFound() {
        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    void testFindAll() {
        repository.save(new WeekPlannerSlot(1, 9));
        repository.save(new WeekPlannerSlot(1, 10));
        repository.save(new WeekPlannerSlot(2, 9));

        assertEquals(3, repository.findAll().size());
    }

    @Test
    void testDeleteById() {
        WeekPlannerSlot slot = new WeekPlannerSlot(1, 9);
        repository.save(slot);
        assertEquals(1, repository.findAll().size());

        repository.deleteById(slot.getId());
        assertEquals(0, repository.findAll().size());
    }

    @Test
    void testExistsById() {
        WeekPlannerSlot slot = new WeekPlannerSlot(1, 9);
        repository.save(slot);

        assertTrue(repository.existsById(slot.getId()));
        assertFalse(repository.existsById("nonexistent"));
    }

    @Test
    void testFindByUserId() {
        User user = new User("testuser", "password");
        WeekPlannerSlot s1 = new WeekPlannerSlot(1, 9);
        s1.setUser(user);
        WeekPlannerSlot s2 = new WeekPlannerSlot(2, 10);

        repository.save(s1);
        repository.save(s2);

        List<WeekPlannerSlot> userSlots = repository.findByUserId(user.getId());
        assertEquals(1, userSlots.size());
        assertEquals(1, userSlots.get(0).getDayOfWeek());
        assertEquals(9, userSlots.get(0).getHour());
    }

    @Test
    void testFindByUserIdAndDayOfWeek() {
        User user = new User("testuser", "password");
        WeekPlannerSlot s1 = new WeekPlannerSlot(1, 9);
        s1.setUser(user);
        WeekPlannerSlot s2 = new WeekPlannerSlot(1, 14);
        s2.setUser(user);
        WeekPlannerSlot s3 = new WeekPlannerSlot(2, 9);
        s3.setUser(user);

        repository.save(s1);
        repository.save(s2);
        repository.save(s3);

        List<WeekPlannerSlot> mondaySlots = repository.findByUserIdAndDayOfWeek(user.getId(), 1);
        assertEquals(2, mondaySlots.size());

        List<WeekPlannerSlot> tuesdaySlots = repository.findByUserIdAndDayOfWeek(user.getId(), 2);
        assertEquals(1, tuesdaySlots.size());
    }

    @Test
    void testSaveWithGroups() {
        PlannerGroup group1 = new PlannerGroup("Fitness", "Physical activities");
        PlannerGroup group2 = new PlannerGroup("Creative", "Creative activities");
        groupRepository.save(group1);
        groupRepository.save(group2);

        WeekPlannerSlot slot = new WeekPlannerSlot(1, 9);
        Set<PlannerGroup> groups = new HashSet<>();
        groups.add(group1);
        groups.add(group2);
        slot.setGroups(groups);
        repository.save(slot);

        FileSystemWeekPlannerSlotRepository newRepo = new FileSystemWeekPlannerSlotRepository(tempDir);
        newRepo.setPlannerGroupRepository(groupRepository);
        Optional<WeekPlannerSlot> found = newRepo.findById(slot.getId());
        assertTrue(found.isPresent());
        assertNotNull(found.get().getGroups());
        assertEquals(2, found.get().getGroups().size());
    }

    @Test
    void testSaveWithSingleGroup() {
        PlannerGroup group = new PlannerGroup("Fitness", "Physical activities");
        groupRepository.save(group);

        WeekPlannerSlot slot = new WeekPlannerSlot(1, 9);
        Set<PlannerGroup> groups = new HashSet<>();
        groups.add(group);
        slot.setGroups(groups);
        repository.save(slot);

        FileSystemWeekPlannerSlotRepository newRepo = new FileSystemWeekPlannerSlotRepository(tempDir);
        newRepo.setPlannerGroupRepository(groupRepository);
        Optional<WeekPlannerSlot> found = newRepo.findById(slot.getId());
        assertTrue(found.isPresent());
        assertEquals(1, found.get().getGroups().size());
        assertTrue(found.get().getGroups().stream().anyMatch(g -> g.getId().equals(group.getId())));
    }

    @Test
    void testPersistenceAcrossInstances() {
        WeekPlannerSlot slot = new WeekPlannerSlot(3, 15);
        repository.save(slot);

        FileSystemWeekPlannerSlotRepository newRepo = new FileSystemWeekPlannerSlotRepository(tempDir);
        newRepo.setPlannerGroupRepository(groupRepository);
        Optional<WeekPlannerSlot> found = newRepo.findById(slot.getId());
        assertTrue(found.isPresent());
        assertEquals(3, found.get().getDayOfWeek());
        assertEquals(15, found.get().getHour());
    }

    @Test
    void testEmptyRepository() {
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void testSaveAndFindByIdWithDuration() {
        WeekPlannerSlot slot = new WeekPlannerSlot(1, 9, 3);
        repository.save(slot);

        Optional<WeekPlannerSlot> found = repository.findById(slot.getId());
        assertTrue(found.isPresent());
        assertEquals(3, found.get().getDuration());
    }

    @Test
    void testDurationPersistenceAcrossInstances() {
        WeekPlannerSlot slot = new WeekPlannerSlot(2, 10, 4);
        repository.save(slot);

        FileSystemWeekPlannerSlotRepository newRepo = new FileSystemWeekPlannerSlotRepository(tempDir);
        newRepo.setPlannerGroupRepository(groupRepository);
        Optional<WeekPlannerSlot> found = newRepo.findById(slot.getId());
        assertTrue(found.isPresent());
        assertEquals(4, found.get().getDuration());
    }

    @Test
    void testDefaultDurationForOldData() {
        WeekPlannerSlot slot = new WeekPlannerSlot(1, 9);
        repository.save(slot);

        Optional<WeekPlannerSlot> found = repository.findById(slot.getId());
        assertTrue(found.isPresent());
        assertEquals(1, found.get().getDuration());
    }
}
