package de.idrinth.habitevaluator.shared.persistence;

import de.idrinth.habitevaluator.shared.model.PlannerGroup;
import de.idrinth.habitevaluator.shared.model.User;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemPlannerGroupRepositoryTest {

    private File tempDir;
    private FileSystemPlannerGroupRepository repository;

    @BeforeEach
    void setUp() {
        tempDir = new File(System.getProperty("java.io.tmpdir"), "planner-group-test-" + System.nanoTime());
        tempDir.mkdirs();
        repository = new FileSystemPlannerGroupRepository(tempDir);
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
        PlannerGroup group = new PlannerGroup("Fitness", "Physical activities");
        repository.save(group);

        Optional<PlannerGroup> found = repository.findById(group.getId());
        assertTrue(found.isPresent());
        assertEquals("Fitness", found.get().getName());
        assertEquals("Physical activities", found.get().getDescription());
    }

    @Test
    void testFindByIdNotFound() {
        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    void testFindAll() {
        repository.save(new PlannerGroup("Fitness", "Physical activities"));
        repository.save(new PlannerGroup("Creative", "Creative projects"));
        repository.save(new PlannerGroup("Learning", "Education"));

        assertEquals(3, repository.findAll().size());
    }

    @Test
    void testDeleteById() {
        PlannerGroup group = new PlannerGroup("Fitness", "Physical activities");
        repository.save(group);
        assertEquals(1, repository.findAll().size());

        repository.deleteById(group.getId());
        assertEquals(0, repository.findAll().size());
    }

    @Test
    void testExistsById() {
        PlannerGroup group = new PlannerGroup("Fitness", "Physical activities");
        repository.save(group);

        assertTrue(repository.existsById(group.getId()));
        assertFalse(repository.existsById("nonexistent"));
    }

    @Test
    void testFindByUserId() {
        User user = new User("testuser", "password");
        PlannerGroup g1 = new PlannerGroup("Fitness", "Physical activities");
        g1.setUser(user);
        PlannerGroup g2 = new PlannerGroup("Creative", "Creative projects");

        repository.save(g1);
        repository.save(g2);

        List<PlannerGroup> userGroups = repository.findByUserId(user.getId());
        assertEquals(1, userGroups.size());
        assertEquals("Fitness", userGroups.get(0).getName());
    }

    @Test
    void testPersistenceAcrossInstances() {
        PlannerGroup group = new PlannerGroup("Fitness", "Physical activities");
        repository.save(group);

        FileSystemPlannerGroupRepository newRepo = new FileSystemPlannerGroupRepository(tempDir);
        Optional<PlannerGroup> found = newRepo.findById(group.getId());
        assertTrue(found.isPresent());
        assertEquals("Fitness", found.get().getName());
        assertEquals("Physical activities", found.get().getDescription());
    }

    @Test
    void testSaveWithUser() {
        User user = new User("testuser", "password");
        PlannerGroup group = new PlannerGroup("Fitness", "Physical activities");
        group.setUser(user);
        repository.save(group);

        FileSystemPlannerGroupRepository newRepo = new FileSystemPlannerGroupRepository(tempDir);
        Optional<PlannerGroup> found = newRepo.findById(group.getId());
        assertTrue(found.isPresent());
        assertNotNull(found.get().getUser());
        assertEquals(user.getId(), found.get().getUser().getId());
    }

    @Test
    void testEmptyRepository() {
        assertTrue(repository.findAll().isEmpty());
    }
}
