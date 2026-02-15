package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmergencyPlanStepTest {

    @Test
    void testDefaultConstructor() {
        EmergencyPlanStep step = new EmergencyPlanStep();
        assertNotNull(step.getId());
        assertEquals(36, step.getId().length());
    }

    @Test
    void testTwoArgConstructor() {
        EmergencyPlanStep step = new EmergencyPlanStep("Are you tired?", 0);
        assertEquals("Are you tired?", step.getQuestion());
        assertEquals(0, step.getStepOrder());
        assertNotNull(step.getId());
        assertNotNull(step.getActions());
        assertTrue(step.getActions().isEmpty());
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
    void testActionsDefaultEmpty() {
        EmergencyPlanStep step = new EmergencyPlanStep();
        assertNotNull(step.getActions());
        assertTrue(step.getActions().isEmpty());
    }

    @Test
    void testSetActions() {
        EmergencyPlanStep step = new EmergencyPlanStep();
        List<EmergencyPlanAction> actions = new ArrayList<>();
        actions.add(new EmergencyPlanAction("Action 1", 0));
        actions.add(new EmergencyPlanAction("Action 2", 1));
        step.setActions(actions);
        assertEquals(2, step.getActions().size());
    }

    @Test
    void testAddAction() {
        EmergencyPlanStep step = new EmergencyPlanStep("Question?", 0);
        EmergencyPlanAction action = new EmergencyPlanAction("Feed Munnin", 0);
        step.addAction(action);

        assertEquals(1, step.getActions().size());
        assertSame(action, step.getActions().get(0));
        assertSame(step, action.getStep());
    }

    @Test
    void testAddMultipleActions() {
        EmergencyPlanStep step = new EmergencyPlanStep("Question?", 0);
        EmergencyPlanAction action1 = new EmergencyPlanAction("Action 1", 0);
        EmergencyPlanAction action2 = new EmergencyPlanAction("Action 2", "+49123", 1);
        step.addAction(action1);
        step.addAction(action2);

        assertEquals(2, step.getActions().size());
        assertSame(step, action1.getStep());
        assertSame(step, action2.getStep());
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
