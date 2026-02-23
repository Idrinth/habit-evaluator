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
    void testSetGroup() {
        WeekPlannerSlot slot = new WeekPlannerSlot();
        PlannerGroup group = new PlannerGroup("Fitness");
        slot.setGroup(group);
        assertSame(group, slot.getGroup());
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
