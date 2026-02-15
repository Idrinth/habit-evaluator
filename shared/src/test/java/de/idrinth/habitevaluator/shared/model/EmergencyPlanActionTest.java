package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmergencyPlanActionTest {

    @Test
    void testDefaultConstructor() {
        EmergencyPlanAction action = new EmergencyPlanAction();
        assertNotNull(action.getId());
        assertEquals(36, action.getId().length());
    }

    @Test
    void testTwoArgConstructor() {
        EmergencyPlanAction action = new EmergencyPlanAction("Feed Munnin", 0);
        assertEquals("Feed Munnin", action.getActionText());
        assertEquals(0, action.getActionOrder());
        assertNull(action.getPhoneNumber());
        assertNotNull(action.getId());
    }

    @Test
    void testThreeArgConstructor() {
        EmergencyPlanAction action = new EmergencyPlanAction("Call support", "+491234567890", 1);
        assertEquals("Call support", action.getActionText());
        assertEquals("+491234567890", action.getPhoneNumber());
        assertEquals(1, action.getActionOrder());
    }

    @Test
    void testSetId() {
        EmergencyPlanAction action = new EmergencyPlanAction();
        action.setId("custom-id");
        assertEquals("custom-id", action.getId());
    }

    @Test
    void testSetActionText() {
        EmergencyPlanAction action = new EmergencyPlanAction();
        action.setActionText("Take a walk");
        assertEquals("Take a walk", action.getActionText());
    }

    @Test
    void testSetPhoneNumber() {
        EmergencyPlanAction action = new EmergencyPlanAction();
        action.setPhoneNumber("+49123456");
        assertEquals("+49123456", action.getPhoneNumber());
    }

    @Test
    void testSetPhoneNumberNull() {
        EmergencyPlanAction action = new EmergencyPlanAction("Action", "+49123", 0);
        action.setPhoneNumber(null);
        assertNull(action.getPhoneNumber());
    }

    @Test
    void testSetActionOrder() {
        EmergencyPlanAction action = new EmergencyPlanAction();
        action.setActionOrder(3);
        assertEquals(3, action.getActionOrder());
    }

    @Test
    void testSetStep() {
        EmergencyPlanAction action = new EmergencyPlanAction();
        EmergencyPlanStep step = new EmergencyPlanStep("Question?", 0);
        action.setStep(step);
        assertSame(step, action.getStep());
    }

    @Test
    void testStepDefaultNull() {
        EmergencyPlanAction action = new EmergencyPlanAction();
        assertNull(action.getStep());
    }

    @Test
    void testEqualsSameId() {
        EmergencyPlanAction a1 = new EmergencyPlanAction();
        EmergencyPlanAction a2 = new EmergencyPlanAction();
        a2.setId(a1.getId());
        assertEquals(a1, a2);
    }

    @Test
    void testEqualsDifferentId() {
        EmergencyPlanAction a1 = new EmergencyPlanAction();
        EmergencyPlanAction a2 = new EmergencyPlanAction();
        assertNotEquals(a1, a2);
    }

    @Test
    void testEqualsNull() {
        EmergencyPlanAction a = new EmergencyPlanAction();
        assertNotEquals(null, a);
    }

    @Test
    void testEqualsSameObject() {
        EmergencyPlanAction a = new EmergencyPlanAction();
        assertEquals(a, a);
    }

    @Test
    void testHashCodeConsistent() {
        EmergencyPlanAction a1 = new EmergencyPlanAction();
        EmergencyPlanAction a2 = new EmergencyPlanAction();
        a2.setId(a1.getId());
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    @Test
    void testHashCodeDifferentForDifferentIds() {
        EmergencyPlanAction a1 = new EmergencyPlanAction();
        EmergencyPlanAction a2 = new EmergencyPlanAction();
        assertNotEquals(a1.hashCode(), a2.hashCode());
    }

    @Test
    void testUniqueIdsGenerated() {
        EmergencyPlanAction a1 = new EmergencyPlanAction();
        EmergencyPlanAction a2 = new EmergencyPlanAction();
        assertNotEquals(a1.getId(), a2.getId());
    }
}
