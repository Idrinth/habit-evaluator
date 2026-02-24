package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.PlannerGroup;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class H2PlannerGroupRepositoryTest extends H2RepositoryTestBase {

    private H2PlannerGroupRepository groupRepository;
    private H2UserRepository userRepository;
    private User testUser;

    @BeforeEach
    void setUp() {
        groupRepository = new H2PlannerGroupRepository();
        userRepository = new H2UserRepository();

        for (PlannerGroup group : groupRepository.findAll()) {
            groupRepository.deleteById(group.getId());
        }
        for (User user : userRepository.findAll()) {
            userRepository.deleteById(user.getId());
        }

        testUser = new User("planneruser", "password123");
        userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        PlannerGroup group = new PlannerGroup("Fitness", "Physical activities");
        group.setUser(testUser);
        PlannerGroup saved = groupRepository.save(group);

        assertNotNull(saved);
        Optional<PlannerGroup> found = groupRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Fitness", found.get().getName());
        assertEquals("Physical activities", found.get().getDescription());
    }

    @Test
    void testSaveUpdatesExisting() {
        PlannerGroup group = new PlannerGroup("Fitness", "Physical activities");
        group.setUser(testUser);
        groupRepository.save(group);

        group.setDescription("Updated description");
        groupRepository.save(group);

        Optional<PlannerGroup> found = groupRepository.findById(group.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated description", found.get().getDescription());
    }

    @Test
    void testFindByIdReturnsEmptyForNonExistent() {
        Optional<PlannerGroup> found = groupRepository.findById("nonexistent-id");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        PlannerGroup g1 = new PlannerGroup("Fitness", "Physical activities");
        g1.setUser(testUser);
        PlannerGroup g2 = new PlannerGroup("Creative", "Creative projects");
        g2.setUser(testUser);
        groupRepository.save(g1);
        groupRepository.save(g2);

        List<PlannerGroup> all = groupRepository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testDeleteById() {
        PlannerGroup group = new PlannerGroup("Fitness", "Physical activities");
        group.setUser(testUser);
        groupRepository.save(group);

        assertTrue(groupRepository.findById(group.getId()).isPresent());
        groupRepository.deleteById(group.getId());
        assertFalse(groupRepository.findById(group.getId()).isPresent());
    }

    @Test
    void testDeleteByIdNonExistentDoesNotThrow() {
        assertDoesNotThrow(() -> groupRepository.deleteById("nonexistent-id"));
    }

    @Test
    void testExistsById() {
        PlannerGroup group = new PlannerGroup("Fitness", "Physical activities");
        group.setUser(testUser);
        groupRepository.save(group);

        assertTrue(groupRepository.existsById(group.getId()));
        assertFalse(groupRepository.existsById("nonexistent-id"));
    }

    @Test
    void testFindByUserId() {
        PlannerGroup g1 = new PlannerGroup("Fitness", "Physical activities");
        g1.setUser(testUser);
        groupRepository.save(g1);

        User otherUser = new User("otherplanneruser", "password123");
        userRepository.save(otherUser);
        PlannerGroup g2 = new PlannerGroup("Creative", "Creative projects");
        g2.setUser(otherUser);
        groupRepository.save(g2);

        List<PlannerGroup> userGroups = groupRepository.findByUserId(testUser.getId());
        assertEquals(1, userGroups.size());
        assertEquals("Fitness", userGroups.get(0).getName());
    }

    @Test
    void testFindByUserIdReturnsEmptyForNonExistentUser() {
        List<PlannerGroup> groups = groupRepository.findByUserId("nonexistent-user-id");
        assertTrue(groups.isEmpty());
    }
}
