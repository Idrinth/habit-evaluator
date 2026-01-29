package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HabitCategoryTest {

    @Test
    void testDefaultConstructor() {
        HabitCategory category = new HabitCategory();
        assertNotNull(category.getId());
        assertNull(category.getName());
    }

    @Test
    void testNameConstructor() {
        HabitCategory category = new HabitCategory("Health");
        assertEquals("Health", category.getName());
        assertNotNull(category.getId());
    }

    @Test
    void testFullConstructor() {
        HabitCategory category = new HabitCategory("Health", "Physical well-being", "#FF0000");
        assertEquals("Health", category.getName());
        assertEquals("Physical well-being", category.getDescription());
        assertEquals("#FF0000", category.getColor());
    }

    @Test
    void testSetName() {
        HabitCategory category = new HabitCategory();
        category.setName("Learning");
        assertEquals("Learning", category.getName());
    }

    @Test
    void testSetDescription() {
        HabitCategory category = new HabitCategory();
        category.setDescription("Growth activities");
        assertEquals("Growth activities", category.getDescription());
    }

    @Test
    void testSetColor() {
        HabitCategory category = new HabitCategory();
        category.setColor("#00FF00");
        assertEquals("#00FF00", category.getColor());
    }

    @Test
    void testEqualsSameId() {
        HabitCategory c1 = new HabitCategory("A");
        HabitCategory c2 = new HabitCategory("B");
        c2.setId(c1.getId());
        assertEquals(c1, c2);
    }

    @Test
    void testEqualsDifferentId() {
        HabitCategory c1 = new HabitCategory("A");
        HabitCategory c2 = new HabitCategory("A");
        assertNotEquals(c1, c2);
    }

    @Test
    void testEqualsNull() {
        HabitCategory c = new HabitCategory();
        assertNotEquals(null, c);
    }

    @Test
    void testEqualsSameObject() {
        HabitCategory c = new HabitCategory();
        assertEquals(c, c);
    }

    @Test
    void testHashCodeConsistent() {
        HabitCategory c1 = new HabitCategory();
        HabitCategory c2 = new HabitCategory();
        c2.setId(c1.getId());
        assertEquals(c1.hashCode(), c2.hashCode());
    }
}
