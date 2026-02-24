package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.PlannerActivity;
import de.idrinth.habitevaluator.shared.model.PlannerGroup;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class JpaPlannerActivityRepositoryTest {

    @Autowired
    private JpaPlannerActivityRepository activityRepository;

    @Autowired
    private JpaPlannerGroupRepository groupRepository;

    @Autowired
    private JpaUserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("planneractivityuser", "password123", "planneractivityuser@example.com");
        testUser = userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        PlannerActivity activity = new PlannerActivity("Morning Run", "30 min jogging");
        activity.setUser(testUser);
        PlannerActivity saved = activityRepository.save(activity);

        Optional<PlannerActivity> found = activityRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Morning Run", found.get().getName());
        assertEquals("30 min jogging", found.get().getDescription());
    }

    @Test
    void testFindByUserId() {
        PlannerActivity a1 = new PlannerActivity("Running");
        a1.setUser(testUser);
        activityRepository.save(a1);

        PlannerActivity a2 = new PlannerActivity("Reading");
        a2.setUser(testUser);
        activityRepository.save(a2);

        List<PlannerActivity> activities = activityRepository.findByUserId(testUser.getId());

        assertEquals(2, activities.size());
    }

    @Test
    void testFindByUserIdOrderedByName() {
        PlannerActivity a1 = new PlannerActivity("Yoga");
        a1.setUser(testUser);
        activityRepository.save(a1);

        PlannerActivity a2 = new PlannerActivity("Archery");
        a2.setUser(testUser);
        activityRepository.save(a2);

        List<PlannerActivity> activities = activityRepository.findByUserId(testUser.getId());

        assertEquals(2, activities.size());
        assertEquals("Archery", activities.get(0).getName());
        assertEquals("Yoga", activities.get(1).getName());
    }

    @Test
    void testFindByUserIdEmptyForOtherUser() {
        PlannerActivity activity = new PlannerActivity("Running");
        activity.setUser(testUser);
        activityRepository.save(activity);

        User otherUser = new User("otherplanneruser", "password", "otherplanner@example.com");
        otherUser = userRepository.save(otherUser);

        List<PlannerActivity> activities = activityRepository.findByUserId(otherUser.getId());
        assertTrue(activities.isEmpty());
    }

    @Test
    void testSaveActivityWithGroups() {
        PlannerGroup group = new PlannerGroup("Fitness", "Physical activities");
        group.setUser(testUser);
        group = groupRepository.save(group);

        PlannerActivity activity = new PlannerActivity("Morning Run");
        activity.setUser(testUser);
        Set<PlannerGroup> groups = new HashSet<>();
        groups.add(group);
        activity.setGroups(groups);
        activityRepository.save(activity);

        Optional<PlannerActivity> found = activityRepository.findById(activity.getId());

        assertTrue(found.isPresent());
        assertNotNull(found.get().getGroups());
        assertEquals(1, found.get().getGroups().size());
    }

    @Test
    void testFindByUserIdReturnsActivitiesWithGroups() {
        PlannerGroup group = new PlannerGroup("Fitness", "Physical activities");
        group.setUser(testUser);
        group = groupRepository.save(group);

        PlannerActivity activity = new PlannerActivity("Morning Run");
        activity.setUser(testUser);
        Set<PlannerGroup> groups = new HashSet<>();
        groups.add(group);
        activity.setGroups(groups);
        activityRepository.save(activity);

        List<PlannerActivity> activities = activityRepository.findByUserId(testUser.getId());

        assertEquals(1, activities.size());
        assertNotNull(activities.get(0).getGroups());
        assertEquals(1, activities.get(0).getGroups().size());
        assertEquals("Fitness", activities.get(0).getGroups().iterator().next().getName());
    }

    @Test
    void testFindByGroupId() {
        PlannerGroup group = new PlannerGroup("Fitness");
        group.setUser(testUser);
        group = groupRepository.save(group);

        PlannerActivity a1 = new PlannerActivity("Running");
        a1.setUser(testUser);
        Set<PlannerGroup> groups = new HashSet<>();
        groups.add(group);
        a1.setGroups(groups);
        activityRepository.save(a1);

        PlannerActivity a2 = new PlannerActivity("Reading");
        a2.setUser(testUser);
        activityRepository.save(a2);

        List<PlannerActivity> groupActivities = activityRepository.findByGroupId(group.getId());

        assertEquals(1, groupActivities.size());
        assertEquals("Running", groupActivities.get(0).getName());
    }

    @Test
    void testFindByGroupIdReturnsAllGroupsForActivity() {
        PlannerGroup fitness = new PlannerGroup("Fitness");
        fitness.setUser(testUser);
        fitness = groupRepository.save(fitness);

        PlannerGroup outdoor = new PlannerGroup("Outdoor");
        outdoor.setUser(testUser);
        outdoor = groupRepository.save(outdoor);

        PlannerActivity activity = new PlannerActivity("Jogging");
        activity.setUser(testUser);
        Set<PlannerGroup> groups = new HashSet<>();
        groups.add(fitness);
        groups.add(outdoor);
        activity.setGroups(groups);
        activityRepository.save(activity);

        List<PlannerActivity> result = activityRepository.findByGroupId(fitness.getId());

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getGroups().size());
    }

    @Test
    void testDeleteById() {
        PlannerActivity activity = new PlannerActivity("Running");
        activity.setUser(testUser);
        activityRepository.save(activity);

        activityRepository.deleteById(activity.getId());

        assertFalse(activityRepository.findById(activity.getId()).isPresent());
    }

    @Test
    void testSaveActivityWithMultipleGroups() {
        PlannerGroup g1 = new PlannerGroup("Fitness");
        g1.setUser(testUser);
        g1 = groupRepository.save(g1);

        PlannerGroup g2 = new PlannerGroup("Morning Routine");
        g2.setUser(testUser);
        g2 = groupRepository.save(g2);

        PlannerActivity activity = new PlannerActivity("Morning Run");
        activity.setUser(testUser);
        Set<PlannerGroup> groups = new HashSet<>();
        groups.add(g1);
        groups.add(g2);
        activity.setGroups(groups);
        activityRepository.save(activity);

        Optional<PlannerActivity> found = activityRepository.findById(activity.getId());

        assertTrue(found.isPresent());
        assertEquals(2, found.get().getGroups().size());
    }
}
