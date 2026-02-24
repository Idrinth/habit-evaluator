package de.idrinth.habitevaluator.shared.persistence;

import de.idrinth.habitevaluator.shared.model.PlannerGroup;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.model.WeekPlannerSlot;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Optional;

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
    void testSaveWithGroup() {
        PlannerGroup group = new PlannerGroup("Fitness", "Physical activities");
        groupRepository.save(group);

        WeekPlannerSlot slot = new WeekPlannerSlot(1, 9);
        slot.setGroup(group);
        repository.save(slot);

        FileSystemWeekPlannerSlotRepository newRepo = new FileSystemWeekPlannerSlotRepository(tempDir);
        newRepo.setPlannerGroupRepository(groupRepository);
        Optional<WeekPlannerSlot> found = newRepo.findById(slot.getId());
        assertTrue(found.isPresent());
        assertNotNull(found.get().getGroup());
        assertEquals(group.getId(), found.get().getGroup().getId());
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
}
