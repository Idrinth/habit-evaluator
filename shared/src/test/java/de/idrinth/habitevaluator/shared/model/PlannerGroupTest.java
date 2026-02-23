package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PlannerGroupTest {

    @Test
    void testDefaultConstructor() {
        PlannerGroup group = new PlannerGroup();
        assertNotNull(group.getId());
        assertNotNull(group.getCreatedAt());
    }

    @Test
    void testNameConstructor() {
        PlannerGroup group = new PlannerGroup("Fitness");
        assertEquals("Fitness", group.getName());
        assertNotNull(group.getId());
    }

    @Test
    void testNameDescriptionConstructor() {
        PlannerGroup group = new PlannerGroup("Creative", "Art and writing activities");
        assertEquals("Creative", group.getName());
        assertEquals("Art and writing activities", group.getDescription());
    }

    @Test
    void testSetName() {
        PlannerGroup group = new PlannerGroup();
        group.setName("Mindfulness");
        assertEquals("Mindfulness", group.getName());
    }

    @Test
    void testSetDescription() {
        PlannerGroup group = new PlannerGroup();
        group.setDescription("Meditation and breathing exercises");
        assertEquals("Meditation and breathing exercises", group.getDescription());
    }

    @Test
    void testSetCreatedAt() {
        PlannerGroup group = new PlannerGroup();
        LocalDateTime time = LocalDateTime.of(2026, 2, 1, 12, 0);
        group.setCreatedAt(time);
        assertEquals(time, group.getCreatedAt());
    }

    @Test
    void testSetUser() {
        PlannerGroup group = new PlannerGroup();
        User user = new User("test", "pass");
        group.setUser(user);
        assertSame(user, group.getUser());
    }

    @Test
    void testEqualsSameId() {
        PlannerGroup g1 = new PlannerGroup("A");
        PlannerGroup g2 = new PlannerGroup("B");
        g2.setId(g1.getId());
        assertEquals(g1, g2);
    }

    @Test
    void testEqualsDifferentId() {
        PlannerGroup g1 = new PlannerGroup("A");
        PlannerGroup g2 = new PlannerGroup("A");
        assertNotEquals(g1, g2);
    }

    @Test
    void testEqualsNull() {
        PlannerGroup g = new PlannerGroup();
        assertNotEquals(null, g);
    }

    @Test
    void testEqualsSameObject() {
        PlannerGroup g = new PlannerGroup();
        assertEquals(g, g);
    }

    @Test
    void testHashCodeConsistent() {
        PlannerGroup g1 = new PlannerGroup();
        PlannerGroup g2 = new PlannerGroup();
        g2.setId(g1.getId());
        assertEquals(g1.hashCode(), g2.hashCode());
    }
}
