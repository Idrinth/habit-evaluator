package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeekPlannerSlotTest {

    @Test
    void testDefaultConstructor() {
        WeekPlannerSlot slot = new WeekPlannerSlot();
        assertNotNull(slot.getId());
    }

    @Test
    void testParameterizedConstructor() {
        WeekPlannerSlot slot = new WeekPlannerSlot(1, 9);
        assertEquals(1, slot.getDayOfWeek());
        assertEquals(9, slot.getHour());
        assertNotNull(slot.getId());
    }

    @Test
    void testSetDayOfWeek() {
        WeekPlannerSlot slot = new WeekPlannerSlot();
        slot.setDayOfWeek(5);
        assertEquals(5, slot.getDayOfWeek());
    }

    @Test
    void testSetHour() {
        WeekPlannerSlot slot = new WeekPlannerSlot();
        slot.setHour(14);
        assertEquals(14, slot.getHour());
    }

    @Test
    void testSetGroups() {
        WeekPlannerSlot slot = new WeekPlannerSlot();
        PlannerGroup group1 = new PlannerGroup("Fitness");
        PlannerGroup group2 = new PlannerGroup("Creative");
        java.util.Set<PlannerGroup> groups = new java.util.HashSet<>();
        groups.add(group1);
        groups.add(group2);
        slot.setGroups(groups);
        assertEquals(2, slot.getGroups().size());
        assertTrue(slot.getGroups().contains(group1));
        assertTrue(slot.getGroups().contains(group2));
    }

    @Test
    void testDefaultGroupsEmpty() {
        WeekPlannerSlot slot = new WeekPlannerSlot();
        assertNotNull(slot.getGroups());
        assertTrue(slot.getGroups().isEmpty());
    }

    @Test
    void testSetUser() {
        WeekPlannerSlot slot = new WeekPlannerSlot();
        User user = new User("test", "pass");
        slot.setUser(user);
        assertSame(user, slot.getUser());
    }

    @Test
    void testEqualsSameId() {
        WeekPlannerSlot s1 = new WeekPlannerSlot(1, 8);
        WeekPlannerSlot s2 = new WeekPlannerSlot(2, 9);
        s2.setId(s1.getId());
        assertEquals(s1, s2);
    }

    @Test
    void testEqualsDifferentId() {
        WeekPlannerSlot s1 = new WeekPlannerSlot(1, 8);
        WeekPlannerSlot s2 = new WeekPlannerSlot(1, 8);
        assertNotEquals(s1, s2);
    }

    @Test
    void testEqualsNull() {
        WeekPlannerSlot s = new WeekPlannerSlot();
        assertNotEquals(null, s);
    }

    @Test
    void testEqualsSameObject() {
        WeekPlannerSlot s = new WeekPlannerSlot();
        assertEquals(s, s);
    }

    @Test
    void testHashCodeConsistent() {
        WeekPlannerSlot s1 = new WeekPlannerSlot();
        WeekPlannerSlot s2 = new WeekPlannerSlot();
        s2.setId(s1.getId());
        assertEquals(s1.hashCode(), s2.hashCode());
    }
}
