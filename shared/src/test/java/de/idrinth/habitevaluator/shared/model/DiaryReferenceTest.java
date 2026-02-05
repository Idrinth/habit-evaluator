package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiaryReferenceTest {

    @Test
    void testDefaultConstructor() {
        DiaryReference ref = new DiaryReference();
        assertNotNull(ref.getId());
        assertEquals(36, ref.getId().length());
    }

    @Test
    void testDescriptionConstructor() {
        DiaryReference ref = new DiaryReference("Test Event");
        assertEquals("Test Event", ref.getDescription());
        assertEquals("test event", ref.getDescriptionLower());
        assertNotNull(ref.getId());
    }

    @Test
    void testSetDescriptionUpdatesLower() {
        DiaryReference ref = new DiaryReference();
        ref.setDescription("Mixed CASE Description");
        assertEquals("Mixed CASE Description", ref.getDescription());
        assertEquals("mixed case description", ref.getDescriptionLower());
    }

    @Test
    void testSetDescriptionWithNull() {
        DiaryReference ref = new DiaryReference("Initial");
        ref.setDescription(null);
        assertNull(ref.getDescription());
        assertNull(ref.getDescriptionLower());
    }

    @Test
    void testSetUser() {
        DiaryReference ref = new DiaryReference("Test");
        User user = new User("test", "pass");
        ref.setUser(user);
        assertSame(user, ref.getUser());
    }

    @Test
    void testEqualsSameId() {
        DiaryReference r1 = new DiaryReference("One");
        DiaryReference r2 = new DiaryReference("Two");
        r2.setId(r1.getId());
        assertEquals(r1, r2);
    }

    @Test
    void testEqualsDifferentId() {
        DiaryReference r1 = new DiaryReference("Same");
        DiaryReference r2 = new DiaryReference("Same");
        assertNotEquals(r1, r2);
    }

    @Test
    void testEqualsNull() {
        DiaryReference r = new DiaryReference("Test");
        assertNotEquals(null, r);
    }

    @Test
    void testEqualsSameObject() {
        DiaryReference r = new DiaryReference("Test");
        assertEquals(r, r);
    }

    @Test
    void testHashCodeConsistent() {
        DiaryReference r1 = new DiaryReference("One");
        DiaryReference r2 = new DiaryReference("Two");
        r2.setId(r1.getId());
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString() {
        DiaryReference ref = new DiaryReference("My Event");
        assertEquals("My Event", ref.toString());
    }

    @Test
    void testCaseInsensitiveMatching() {
        DiaryReference ref = new DiaryReference("Got Promoted");
        assertEquals("got promoted", ref.getDescriptionLower());
        // This simulates how case-insensitive lookup would work
        String userInput = "GOT PROMOTED";
        assertEquals(ref.getDescriptionLower(), userInput.toLowerCase());
    }
}
