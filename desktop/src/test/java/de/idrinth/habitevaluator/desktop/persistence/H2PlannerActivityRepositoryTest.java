package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.PlannerActivity;
import de.idrinth.habitevaluator.shared.model.PlannerGroup;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class H2PlannerActivityRepositoryTest extends H2RepositoryTestBase {

    private H2PlannerActivityRepository activityRepository;
    private H2PlannerGroupRepository groupRepository;
    private H2UserRepository userRepository;
    private User testUser;

    @BeforeEach
    void setUp() {
        activityRepository = new H2PlannerActivityRepository();
        groupRepository = new H2PlannerGroupRepository();
        userRepository = new H2UserRepository();

        for (PlannerActivity activity : activityRepository.findAll()) {
            activityRepository.deleteById(activity.getId());
        }
        for (PlannerGroup group : groupRepository.findAll()) {
            groupRepository.deleteById(group.getId());
        }
        for (User user : userRepository.findAll()) {
            userRepository.deleteById(user.getId());
        }

        testUser = new User("activityuser", "password123");
        userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        PlannerActivity activity = new PlannerActivity("Morning Run", "30 min jogging");
        activity.setUser(testUser);
        PlannerActivity saved = activityRepository.save(activity);

        assertNotNull(saved);
        Optional<PlannerActivity> found = activityRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Morning Run", found.get().getName());
        assertEquals("30 min jogging", found.get().getDescription());
    }

    @Test
    void testSaveUpdatesExisting() {
        PlannerActivity activity = new PlannerActivity("Morning Run", "30 min jogging");
        activity.setUser(testUser);
        activityRepository.save(activity);

        activity.setDescription("Updated to 45 min");
        activityRepository.save(activity);

        Optional<PlannerActivity> found = activityRepository.findById(activity.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated to 45 min", found.get().getDescription());
    }

    @Test
    void testFindByIdReturnsEmptyForNonExistent() {
        Optional<PlannerActivity> found = activityRepository.findById("nonexistent-id");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        PlannerActivity a1 = new PlannerActivity("Running", "Go for a run");
        a1.setUser(testUser);
        PlannerActivity a2 = new PlannerActivity("Reading", "Read a book");
        a2.setUser(testUser);
        activityRepository.save(a1);
        activityRepository.save(a2);

        List<PlannerActivity> all = activityRepository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testDeleteById() {
        PlannerActivity activity = new PlannerActivity("Running", "Go for a run");
        activity.setUser(testUser);
        activityRepository.save(activity);

        assertTrue(activityRepository.findById(activity.getId()).isPresent());
        activityRepository.deleteById(activity.getId());
        assertFalse(activityRepository.findById(activity.getId()).isPresent());
    }

    @Test
    void testDeleteByIdNonExistentDoesNotThrow() {
        assertDoesNotThrow(() -> activityRepository.deleteById("nonexistent-id"));
    }

    @Test
    void testExistsById() {
        PlannerActivity activity = new PlannerActivity("Running", "Go for a run");
        activity.setUser(testUser);
        activityRepository.save(activity);

        assertTrue(activityRepository.existsById(activity.getId()));
        assertFalse(activityRepository.existsById("nonexistent-id"));
    }

    @Test
    void testFindByUserId() {
        PlannerActivity a1 = new PlannerActivity("Running", "Go for a run");
        a1.setUser(testUser);
        activityRepository.save(a1);

        User otherUser = new User("otheractivityuser", "password123");
        userRepository.save(otherUser);
        PlannerActivity a2 = new PlannerActivity("Reading", "Read a book");
        a2.setUser(otherUser);
        activityRepository.save(a2);

        List<PlannerActivity> userActivities = activityRepository.findByUserId(testUser.getId());
        assertEquals(1, userActivities.size());
        assertEquals("Running", userActivities.get(0).getName());
    }

    @Test
    void testFindByGroupId() {
        PlannerGroup group = new PlannerGroup("Fitness", "Physical activities");
        group.setUser(testUser);
        groupRepository.save(group);

        PlannerActivity a1 = new PlannerActivity("Running", "Go for a run");
        a1.setUser(testUser);
        Set<PlannerGroup> groups = new HashSet<>();
        groups.add(group);
        a1.setGroups(groups);
        activityRepository.save(a1);

        PlannerActivity a2 = new PlannerActivity("Reading", "Read a book");
        a2.setUser(testUser);
        activityRepository.save(a2);

        List<PlannerActivity> groupActivities = activityRepository.findByGroupId(group.getId());
        assertEquals(1, groupActivities.size());
        assertEquals("Running", groupActivities.get(0).getName());
    }

    @Test
    void testFindByUserIdReturnsEmptyForNonExistentUser() {
        List<PlannerActivity> activities = activityRepository.findByUserId("nonexistent-user-id");
        assertTrue(activities.isEmpty());
    }
}
