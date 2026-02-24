package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ActivityGroupTest {

    @Test
    void testDefaultConstructor() {
        ActivityGroup group = new ActivityGroup();
        assertNotNull(group.getId());
        assertNotNull(group.getCreatedAt());
    }

    @Test
    void testNameConstructor() {
        ActivityGroup group = new ActivityGroup("Social");
        assertEquals("Social", group.getName());
        assertNotNull(group.getId());
    }

    @Test
    void testNameDescriptionConstructor() {
        ActivityGroup group = new ActivityGroup("Work", "Work-related activities");
        assertEquals("Work", group.getName());
        assertEquals("Work-related activities", group.getDescription());
    }

    @Test
    void testSetName() {
        ActivityGroup group = new ActivityGroup();
        group.setName("Exercise");
        assertEquals("Exercise", group.getName());
    }

    @Test
    void testSetDescription() {
        ActivityGroup group = new ActivityGroup();
        group.setDescription("Physical exercise activities");
        assertEquals("Physical exercise activities", group.getDescription());
    }

    @Test
    void testSetCreatedAt() {
        ActivityGroup group = new ActivityGroup();
        LocalDateTime time = LocalDateTime.of(2026, 2, 1, 12, 0);
        group.setCreatedAt(time);
        assertEquals(time, group.getCreatedAt());
    }

    @Test
    void testSetUser() {
        ActivityGroup group = new ActivityGroup();
        User user = new User("test", "pass");
        group.setUser(user);
        assertSame(user, group.getUser());
    }

    @Test
    void testEqualsSameId() {
        ActivityGroup g1 = new ActivityGroup("A");
        ActivityGroup g2 = new ActivityGroup("B");
        g2.setId(g1.getId());
        assertEquals(g1, g2);
    }

    @Test
    void testEqualsDifferentId() {
        ActivityGroup g1 = new ActivityGroup("A");
        ActivityGroup g2 = new ActivityGroup("A");
        assertNotEquals(g1, g2);
    }

    @Test
    void testEqualsNull() {
        ActivityGroup g = new ActivityGroup();
        assertNotEquals(null, g);
    }

    @Test
    void testEqualsSameObject() {
        ActivityGroup g = new ActivityGroup();
        assertEquals(g, g);
    }

    @Test
    void testHashCodeConsistent() {
        ActivityGroup g1 = new ActivityGroup();
        ActivityGroup g2 = new ActivityGroup();
        g2.setId(g1.getId());
        assertEquals(g1.hashCode(), g2.hashCode());
    }
}
