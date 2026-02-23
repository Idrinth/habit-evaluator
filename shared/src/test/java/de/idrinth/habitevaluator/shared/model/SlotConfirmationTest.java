package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SlotConfirmationTest {

    @Test
    void testDefaultConstructor() {
        SlotConfirmation confirmation = new SlotConfirmation();
        assertNotNull(confirmation.getId());
        assertNotNull(confirmation.getCreatedAt());
        assertNotNull(confirmation.getDate());
        assertFalse(confirmation.isConfirmed());
    }

    @Test
    void testSetConfirmed() {
        SlotConfirmation confirmation = new SlotConfirmation();
        confirmation.setConfirmed(true);
        assertTrue(confirmation.isConfirmed());
    }

    @Test
    void testSetDate() {
        SlotConfirmation confirmation = new SlotConfirmation();
        LocalDate date = LocalDate.of(2026, 3, 15);
        confirmation.setDate(date);
        assertEquals(date, confirmation.getDate());
    }

    @Test
    void testSetSlot() {
        SlotConfirmation confirmation = new SlotConfirmation();
        WeekPlannerSlot slot = new WeekPlannerSlot(1, 8);
        confirmation.setSlot(slot);
        assertSame(slot, confirmation.getSlot());
    }

    @Test
    void testSetActivity() {
        SlotConfirmation confirmation = new SlotConfirmation();
        PlannerActivity activity = new PlannerActivity("Walk");
        confirmation.setActivity(activity);
        assertSame(activity, confirmation.getActivity());
    }

    @Test
    void testSetGroup() {
        SlotConfirmation confirmation = new SlotConfirmation();
        PlannerGroup group = new PlannerGroup("Fitness");
        confirmation.setGroup(group);
        assertSame(group, confirmation.getGroup());
    }

    @Test
    void testSetCreatedAt() {
        SlotConfirmation confirmation = new SlotConfirmation();
        LocalDateTime time = LocalDateTime.of(2026, 2, 1, 12, 0);
        confirmation.setCreatedAt(time);
        assertEquals(time, confirmation.getCreatedAt());
    }

    @Test
    void testSetUser() {
        SlotConfirmation confirmation = new SlotConfirmation();
        User user = new User("test", "pass");
        confirmation.setUser(user);
        assertSame(user, confirmation.getUser());
    }

    @Test
    void testEqualsSameId() {
        SlotConfirmation c1 = new SlotConfirmation();
        SlotConfirmation c2 = new SlotConfirmation();
        c2.setId(c1.getId());
        assertEquals(c1, c2);
    }

    @Test
    void testEqualsDifferentId() {
        SlotConfirmation c1 = new SlotConfirmation();
        SlotConfirmation c2 = new SlotConfirmation();
        assertNotEquals(c1, c2);
    }

    @Test
    void testEqualsNull() {
        SlotConfirmation c = new SlotConfirmation();
        assertNotEquals(null, c);
    }

    @Test
    void testEqualsSameObject() {
        SlotConfirmation c = new SlotConfirmation();
        assertEquals(c, c);
    }

    @Test
    void testHashCodeConsistent() {
        SlotConfirmation c1 = new SlotConfirmation();
        SlotConfirmation c2 = new SlotConfirmation();
        c2.setId(c1.getId());
        assertEquals(c1.hashCode(), c2.hashCode());
    }
}
