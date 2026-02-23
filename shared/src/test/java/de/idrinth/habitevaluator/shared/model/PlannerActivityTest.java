package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PlannerActivityTest {

    @Test
    void testDefaultConstructor() {
        PlannerActivity activity = new PlannerActivity();
        assertNotNull(activity.getId());
        assertNotNull(activity.getCreatedAt());
        assertNotNull(activity.getGroups());
        assertTrue(activity.getGroups().isEmpty());
    }

    @Test
    void testNameConstructor() {
        PlannerActivity activity = new PlannerActivity("Go for a walk");
        assertEquals("Go for a walk", activity.getName());
        assertNotNull(activity.getId());
    }

    @Test
    void testNameDescriptionConstructor() {
        PlannerActivity activity = new PlannerActivity("Read a book", "At least 30 minutes");
        assertEquals("Read a book", activity.getName());
        assertEquals("At least 30 minutes", activity.getDescription());
    }

    @Test
    void testSetName() {
        PlannerActivity activity = new PlannerActivity();
        activity.setName("Meditate");
        assertEquals("Meditate", activity.getName());
    }

    @Test
    void testSetDescription() {
        PlannerActivity activity = new PlannerActivity();
        activity.setDescription("10 min session");
        assertEquals("10 min session", activity.getDescription());
    }

    @Test
    void testSetCreatedAt() {
        PlannerActivity activity = new PlannerActivity();
        LocalDateTime time = LocalDateTime.of(2026, 2, 1, 12, 0);
        activity.setCreatedAt(time);
        assertEquals(time, activity.getCreatedAt());
    }

    @Test
    void testSetUser() {
        PlannerActivity activity = new PlannerActivity();
        User user = new User("test", "pass");
        activity.setUser(user);
        assertSame(user, activity.getUser());
    }

    @Test
    void testSetGroups() {
        PlannerActivity activity = new PlannerActivity();
        Set<PlannerGroup> groups = new HashSet<>();
        groups.add(new PlannerGroup("Fitness"));
        activity.setGroups(groups);
        assertEquals(1, activity.getGroups().size());
    }

    @Test
    void testEqualsSameId() {
        PlannerActivity a1 = new PlannerActivity("Walk");
        PlannerActivity a2 = new PlannerActivity("Run");
        a2.setId(a1.getId());
        assertEquals(a1, a2);
    }

    @Test
    void testEqualsDifferentId() {
        PlannerActivity a1 = new PlannerActivity("Walk");
        PlannerActivity a2 = new PlannerActivity("Walk");
        assertNotEquals(a1, a2);
    }

    @Test
    void testEqualsNull() {
        PlannerActivity a = new PlannerActivity();
        assertNotEquals(null, a);
    }

    @Test
    void testEqualsSameObject() {
        PlannerActivity a = new PlannerActivity();
        assertEquals(a, a);
    }

    @Test
    void testHashCodeConsistent() {
        PlannerActivity a1 = new PlannerActivity();
        PlannerActivity a2 = new PlannerActivity();
        a2.setId(a1.getId());
        assertEquals(a1.hashCode(), a2.hashCode());
    }
}
