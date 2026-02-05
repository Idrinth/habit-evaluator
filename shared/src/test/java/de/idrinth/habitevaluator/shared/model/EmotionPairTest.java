package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmotionPairTest {

    @Test
    void testDefaultConstructor() {
        EmotionPair pair = new EmotionPair();
        assertNotNull(pair.getId());
    }

    @Test
    void testLabelConstructor() {
        EmotionPair pair = new EmotionPair("sad", "happy");
        assertEquals("sad", pair.getNegativeLabel());
        assertEquals("happy", pair.getPositiveLabel());
        assertNotNull(pair.getId());
    }

    @Test
    void testSetNegativeLabel() {
        EmotionPair pair = new EmotionPair();
        pair.setNegativeLabel("angry");
        assertEquals("angry", pair.getNegativeLabel());
    }

    @Test
    void testSetPositiveLabel() {
        EmotionPair pair = new EmotionPair();
        pair.setPositiveLabel("calm");
        assertEquals("calm", pair.getPositiveLabel());
    }

    @Test
    void testSetUser() {
        EmotionPair pair = new EmotionPair();
        User user = new User("test", "pass");
        pair.setUser(user);
        assertSame(user, pair.getUser());
    }

    @Test
    void testToString() {
        EmotionPair pair = new EmotionPair("fatigued", "energetic");
        assertEquals("fatigued \u2014 energetic", pair.toString());
    }

    @Test
    void testEqualsSameId() {
        EmotionPair p1 = new EmotionPair("a", "b");
        EmotionPair p2 = new EmotionPair("c", "d");
        p2.setId(p1.getId());
        assertEquals(p1, p2);
    }

    @Test
    void testEqualsDifferentId() {
        EmotionPair p1 = new EmotionPair("a", "b");
        EmotionPair p2 = new EmotionPair("a", "b");
        assertNotEquals(p1, p2);
    }

    @Test
    void testEqualsNull() {
        EmotionPair p = new EmotionPair();
        assertNotEquals(null, p);
    }

    @Test
    void testEqualsSameObject() {
        EmotionPair p = new EmotionPair();
        assertEquals(p, p);
    }

    @Test
    void testHashCodeConsistent() {
        EmotionPair p1 = new EmotionPair();
        EmotionPair p2 = new EmotionPair();
        p2.setId(p1.getId());
        assertEquals(p1.hashCode(), p2.hashCode());
    }
}
