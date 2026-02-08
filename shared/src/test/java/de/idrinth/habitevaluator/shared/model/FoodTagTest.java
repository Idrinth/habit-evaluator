package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FoodTagTest {

    @Test
    void testDefaultConstructor() {
        FoodTag tag = new FoodTag();
        assertNotNull(tag.getId());
        assertEquals(36, tag.getId().length());
    }

    @Test
    void testNameConstructor() {
        FoodTag tag = new FoodTag("Chicken");
        assertEquals("Chicken", tag.getName());
        assertEquals("chicken", tag.getNameLower());
        assertNotNull(tag.getId());
    }

    @Test
    void testSetNameUpdatesLower() {
        FoodTag tag = new FoodTag();
        tag.setName("Mixed CASE Food");
        assertEquals("Mixed CASE Food", tag.getName());
        assertEquals("mixed case food", tag.getNameLower());
    }

    @Test
    void testSetNameWithNull() {
        FoodTag tag = new FoodTag("Initial");
        tag.setName(null);
        assertNull(tag.getName());
        assertNull(tag.getNameLower());
    }

    @Test
    void testSetUser() {
        FoodTag tag = new FoodTag("Rice");
        User user = new User("test", "pass");
        tag.setUser(user);
        assertSame(user, tag.getUser());
    }

    @Test
    void testEqualsSameId() {
        FoodTag t1 = new FoodTag("One");
        FoodTag t2 = new FoodTag("Two");
        t2.setId(t1.getId());
        assertEquals(t1, t2);
    }

    @Test
    void testEqualsDifferentId() {
        FoodTag t1 = new FoodTag("Same");
        FoodTag t2 = new FoodTag("Same");
        assertNotEquals(t1, t2);
    }

    @Test
    void testEqualsNull() {
        FoodTag t = new FoodTag("Test");
        assertNotEquals(null, t);
    }

    @Test
    void testEqualsSameObject() {
        FoodTag t = new FoodTag("Test");
        assertEquals(t, t);
    }

    @Test
    void testHashCodeConsistent() {
        FoodTag t1 = new FoodTag("One");
        FoodTag t2 = new FoodTag("Two");
        t2.setId(t1.getId());
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    void testToString() {
        FoodTag tag = new FoodTag("Pasta");
        assertEquals("Pasta", tag.toString());
    }

    @Test
    void testCaseInsensitiveMatching() {
        FoodTag tag = new FoodTag("Brown Rice");
        assertEquals("brown rice", tag.getNameLower());
        String userInput = "BROWN RICE";
        assertEquals(tag.getNameLower(), userInput.toLowerCase());
    }

    @Test
    void testSetId() {
        FoodTag tag = new FoodTag("Test");
        String newId = "custom-id-12345";
        tag.setId(newId);
        assertEquals(newId, tag.getId());
    }

    @Test
    void testSetNameLowerDirectly() {
        FoodTag tag = new FoodTag();
        tag.setNameLower("custom_lower");
        assertEquals("custom_lower", tag.getNameLower());
    }
}
