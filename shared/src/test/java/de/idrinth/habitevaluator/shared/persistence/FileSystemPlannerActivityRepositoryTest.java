package de.idrinth.habitevaluator.shared.persistence;

import de.idrinth.habitevaluator.shared.model.PlannerActivity;
import de.idrinth.habitevaluator.shared.model.PlannerGroup;
import de.idrinth.habitevaluator.shared.model.User;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemPlannerActivityRepositoryTest {

    private File tempDir;
    private FileSystemPlannerGroupRepository groupRepository;
    private FileSystemPlannerActivityRepository repository;

    @BeforeEach
    void setUp() {
        tempDir = new File(System.getProperty("java.io.tmpdir"), "planner-activity-test-" + System.nanoTime());
        tempDir.mkdirs();
        groupRepository = new FileSystemPlannerGroupRepository(tempDir);
        repository = new FileSystemPlannerActivityRepository(tempDir);
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
        PlannerActivity activity = new PlannerActivity("Morning Run", "30 min jogging");
        repository.save(activity);

        Optional<PlannerActivity> found = repository.findById(activity.getId());
        assertTrue(found.isPresent());
        assertEquals("Morning Run", found.get().getName());
        assertEquals("30 min jogging", found.get().getDescription());
    }

    @Test
    void testFindByIdNotFound() {
        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    void testFindAll() {
        repository.save(new PlannerActivity("Running", "Go for a run"));
        repository.save(new PlannerActivity("Reading", "Read a book"));

        assertEquals(2, repository.findAll().size());
    }

    @Test
    void testDeleteById() {
        PlannerActivity activity = new PlannerActivity("Running", "Go for a run");
        repository.save(activity);
        assertEquals(1, repository.findAll().size());

        repository.deleteById(activity.getId());
        assertEquals(0, repository.findAll().size());
    }

    @Test
    void testExistsById() {
        PlannerActivity activity = new PlannerActivity("Running", "Go for a run");
        repository.save(activity);

        assertTrue(repository.existsById(activity.getId()));
        assertFalse(repository.existsById("nonexistent"));
    }

    @Test
    void testFindByUserId() {
        User user = new User("testuser", "password");
        PlannerActivity a1 = new PlannerActivity("Running", "Go for a run");
        a1.setUser(user);
        PlannerActivity a2 = new PlannerActivity("Reading", "Read a book");

        repository.save(a1);
        repository.save(a2);

        List<PlannerActivity> userActivities = repository.findByUserId(user.getId());
        assertEquals(1, userActivities.size());
        assertEquals("Running", userActivities.get(0).getName());
    }

    @Test
    void testFindByGroupId() {
        PlannerGroup group = new PlannerGroup("Fitness", "Physical activities");
        groupRepository.save(group);

        PlannerActivity a1 = new PlannerActivity("Running", "Go for a run");
        Set<PlannerGroup> groups = new HashSet<>();
        groups.add(group);
        a1.setGroups(groups);

        PlannerActivity a2 = new PlannerActivity("Reading", "Read a book");

        repository.save(a1);
        repository.save(a2);

        List<PlannerActivity> groupActivities = repository.findByGroupId(group.getId());
        assertEquals(1, groupActivities.size());
        assertEquals("Running", groupActivities.get(0).getName());
    }

    @Test
    void testPersistenceAcrossInstances() {
        PlannerActivity activity = new PlannerActivity("Running", "Go for a run");
        repository.save(activity);

        FileSystemPlannerActivityRepository newRepo = new FileSystemPlannerActivityRepository(tempDir);
        newRepo.setPlannerGroupRepository(groupRepository);
        Optional<PlannerActivity> found = newRepo.findById(activity.getId());
        assertTrue(found.isPresent());
        assertEquals("Running", found.get().getName());
    }

    @Test
    void testSaveWithUser() {
        User user = new User("testuser", "password");
        PlannerActivity activity = new PlannerActivity("Running", "Go for a run");
        activity.setUser(user);
        repository.save(activity);

        FileSystemPlannerActivityRepository newRepo = new FileSystemPlannerActivityRepository(tempDir);
        newRepo.setPlannerGroupRepository(groupRepository);
        Optional<PlannerActivity> found = newRepo.findById(activity.getId());
        assertTrue(found.isPresent());
        assertNotNull(found.get().getUser());
        assertEquals(user.getId(), found.get().getUser().getId());
    }

    @Test
    void testEmptyRepository() {
        assertTrue(repository.findAll().isEmpty());
    }
}
