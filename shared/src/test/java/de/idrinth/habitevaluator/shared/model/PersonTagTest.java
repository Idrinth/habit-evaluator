package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersonTagTest {

    @Test
    void testDefaultConstructor() {
        PersonTag tag = new PersonTag();
        assertNotNull(tag.getId());
        assertEquals(36, tag.getId().length());
    }

    @Test
    void testNameConstructor() {
        PersonTag tag = new PersonTag("Alice");
        assertEquals("Alice", tag.getName());
        assertEquals("alice", tag.getNameLower());
        assertNotNull(tag.getId());
    }

    @Test
    void testSetNameUpdatesLower() {
        PersonTag tag = new PersonTag();
        tag.setName("Bob Smith");
        assertEquals("Bob Smith", tag.getName());
        assertEquals("bob smith", tag.getNameLower());
    }

    @Test
    void testSetNameWithNull() {
        PersonTag tag = new PersonTag("Initial");
        tag.setName(null);
        assertNull(tag.getName());
        assertNull(tag.getNameLower());
    }

    @Test
    void testSetUser() {
        PersonTag tag = new PersonTag("Alice");
        User user = new User("test", "pass");
        tag.setUser(user);
        assertSame(user, tag.getUser());
    }

    @Test
    void testEqualsSameId() {
        PersonTag t1 = new PersonTag("One");
        PersonTag t2 = new PersonTag("Two");
        t2.setId(t1.getId());
        assertEquals(t1, t2);
    }

    @Test
    void testEqualsDifferentId() {
        PersonTag t1 = new PersonTag("Same");
        PersonTag t2 = new PersonTag("Same");
        assertNotEquals(t1, t2);
    }

    @Test
    void testEqualsNull() {
        PersonTag t = new PersonTag("Test");
        assertNotEquals(null, t);
    }

    @Test
    void testEqualsSameObject() {
        PersonTag t = new PersonTag("Test");
        assertEquals(t, t);
    }

    @Test
    void testHashCodeConsistent() {
        PersonTag t1 = new PersonTag("One");
        PersonTag t2 = new PersonTag("Two");
        t2.setId(t1.getId());
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    void testToString() {
        PersonTag tag = new PersonTag("Charlie");
        assertEquals("Charlie", tag.toString());
    }

    @Test
    void testCaseInsensitiveMatching() {
        PersonTag tag = new PersonTag("Alice Smith");
        assertEquals("alice smith", tag.getNameLower());
        String userInput = "ALICE SMITH";
        assertEquals(tag.getNameLower(), userInput.toLowerCase());
    }

    @Test
    void testSetId() {
        PersonTag tag = new PersonTag("Test");
        String newId = "custom-id-12345";
        tag.setId(newId);
        assertEquals(newId, tag.getId());
    }

    @Test
    void testSetNameLowerDirectly() {
        PersonTag tag = new PersonTag();
        tag.setNameLower("custom_lower");
        assertEquals("custom_lower", tag.getNameLower());
    }
}
