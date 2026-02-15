package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmergencyPlanStepTest {

    @Test
    void testDefaultConstructor() {
        EmergencyPlanStep step = new EmergencyPlanStep();
        assertNotNull(step.getId());
        assertEquals(36, step.getId().length());
    }

    @Test
    void testThreeArgConstructor() {
        EmergencyPlanStep step = new EmergencyPlanStep("Are you tired?", "Take a nap", 0);
        assertEquals("Are you tired?", step.getQuestion());
        assertEquals("Take a nap", step.getAction());
        assertEquals(0, step.getStepOrder());
        assertNull(step.getPhoneNumber());
        assertNotNull(step.getId());
    }

    @Test
    void testFourArgConstructor() {
        EmergencyPlanStep step = new EmergencyPlanStep("Need help?", "Call support", "+491234567890", 1);
        assertEquals("Need help?", step.getQuestion());
        assertEquals("Call support", step.getAction());
        assertEquals("+491234567890", step.getPhoneNumber());
        assertEquals(1, step.getStepOrder());
    }

    @Test
    void testSetId() {
        EmergencyPlanStep step = new EmergencyPlanStep();
        step.setId("custom-id");
        assertEquals("custom-id", step.getId());
    }

    @Test
    void testSetQuestion() {
        EmergencyPlanStep step = new EmergencyPlanStep();
        step.setQuestion("Did it take more than 30min to get out of bed?");
        assertEquals("Did it take more than 30min to get out of bed?", step.getQuestion());
    }

    @Test
    void testSetAction() {
        EmergencyPlanStep step = new EmergencyPlanStep();
        step.setAction("Feed Munnin");
        assertEquals("Feed Munnin", step.getAction());
    }

    @Test
    void testSetPhoneNumber() {
        EmergencyPlanStep step = new EmergencyPlanStep();
        step.setPhoneNumber("+49123456");
        assertEquals("+49123456", step.getPhoneNumber());
    }

    @Test
    void testSetPhoneNumberNull() {
        EmergencyPlanStep step = new EmergencyPlanStep("Q", "A", "+49123", 0);
        step.setPhoneNumber(null);
        assertNull(step.getPhoneNumber());
    }

    @Test
    void testSetStepOrder() {
        EmergencyPlanStep step = new EmergencyPlanStep();
        step.setStepOrder(5);
        assertEquals(5, step.getStepOrder());
    }

    @Test
    void testSetUser() {
        EmergencyPlanStep step = new EmergencyPlanStep();
        User user = new User("test", "pass");
        step.setUser(user);
        assertSame(user, step.getUser());
    }

    @Test
    void testUserDefaultNull() {
        EmergencyPlanStep step = new EmergencyPlanStep();
        assertNull(step.getUser());
    }

    @Test
    void testEqualsSameId() {
        EmergencyPlanStep s1 = new EmergencyPlanStep();
        EmergencyPlanStep s2 = new EmergencyPlanStep();
        s2.setId(s1.getId());
        assertEquals(s1, s2);
    }

    @Test
    void testEqualsDifferentId() {
        EmergencyPlanStep s1 = new EmergencyPlanStep();
        EmergencyPlanStep s2 = new EmergencyPlanStep();
        assertNotEquals(s1, s2);
    }

    @Test
    void testEqualsNull() {
        EmergencyPlanStep s = new EmergencyPlanStep();
        assertNotEquals(null, s);
    }

    @Test
    void testEqualsSameObject() {
        EmergencyPlanStep s = new EmergencyPlanStep();
        assertEquals(s, s);
    }

    @Test
    void testHashCodeConsistent() {
        EmergencyPlanStep s1 = new EmergencyPlanStep();
        EmergencyPlanStep s2 = new EmergencyPlanStep();
        s2.setId(s1.getId());
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    void testHashCodeDifferentForDifferentIds() {
        EmergencyPlanStep s1 = new EmergencyPlanStep();
        EmergencyPlanStep s2 = new EmergencyPlanStep();
        assertNotEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    void testUniqueIdsGenerated() {
        EmergencyPlanStep s1 = new EmergencyPlanStep();
        EmergencyPlanStep s2 = new EmergencyPlanStep();
        assertNotEquals(s1.getId(), s2.getId());
    }
}
