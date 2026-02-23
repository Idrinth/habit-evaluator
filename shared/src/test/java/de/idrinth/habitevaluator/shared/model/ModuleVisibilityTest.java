package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModuleVisibilityTest {

    @Test
    void testDefaultConstructor() {
        ModuleVisibility mv = new ModuleVisibility();
        assertNotNull(mv.getId());
        assertNull(mv.getUser());
    }

    @Test
    void testAllModulesVisibleByDefault() {
        ModuleVisibility mv = new ModuleVisibility();
        assertTrue(mv.isDiaryVisible());
        assertTrue(mv.isSleepVisible());
        assertTrue(mv.isEmotionsVisible());
        assertTrue(mv.isPointsVisible());
        assertTrue(mv.isStatisticsVisible());
        assertTrue(mv.isFoodLogVisible());
        assertTrue(mv.isSportLogVisible());
        assertTrue(mv.isMedicationVisible());
        assertTrue(mv.isBackupVisible());
        assertTrue(mv.isPdfExportVisible());
        assertTrue(mv.isActivityLogVisible());
        assertTrue(mv.isDayPlannerVisible());
    }

    @Test
    void testSetDiaryVisible() {
        ModuleVisibility mv = new ModuleVisibility();
        mv.setDiaryVisible(false);
        assertFalse(mv.isDiaryVisible());
    }

    @Test
    void testSetSleepVisible() {
        ModuleVisibility mv = new ModuleVisibility();
        mv.setSleepVisible(false);
        assertFalse(mv.isSleepVisible());
    }

    @Test
    void testSetEmotionsVisible() {
        ModuleVisibility mv = new ModuleVisibility();
        mv.setEmotionsVisible(false);
        assertFalse(mv.isEmotionsVisible());
    }

    @Test
    void testSetPointsVisible() {
        ModuleVisibility mv = new ModuleVisibility();
        mv.setPointsVisible(false);
        assertFalse(mv.isPointsVisible());
    }

    @Test
    void testSetStatisticsVisible() {
        ModuleVisibility mv = new ModuleVisibility();
        mv.setStatisticsVisible(false);
        assertFalse(mv.isStatisticsVisible());
    }

    @Test
    void testSetFoodLogVisible() {
        ModuleVisibility mv = new ModuleVisibility();
        mv.setFoodLogVisible(false);
        assertFalse(mv.isFoodLogVisible());
    }

    @Test
    void testSetSportLogVisible() {
        ModuleVisibility mv = new ModuleVisibility();
        mv.setSportLogVisible(false);
        assertFalse(mv.isSportLogVisible());
    }

    @Test
    void testSetMedicationVisible() {
        ModuleVisibility mv = new ModuleVisibility();
        mv.setMedicationVisible(false);
        assertFalse(mv.isMedicationVisible());
    }

    @Test
    void testSetBackupVisible() {
        ModuleVisibility mv = new ModuleVisibility();
        mv.setBackupVisible(false);
        assertFalse(mv.isBackupVisible());
    }

    @Test
    void testSetPdfExportVisible() {
        ModuleVisibility mv = new ModuleVisibility();
        mv.setPdfExportVisible(false);
        assertFalse(mv.isPdfExportVisible());
    }

    @Test
    void testSetActivityLogVisible() {
        ModuleVisibility mv = new ModuleVisibility();
        mv.setActivityLogVisible(false);
        assertFalse(mv.isActivityLogVisible());
    }

    @Test
    void testSetDayPlannerVisible() {
        ModuleVisibility mv = new ModuleVisibility();
        mv.setDayPlannerVisible(false);
        assertFalse(mv.isDayPlannerVisible());
    }

    @Test
    void testSetUser() {
        ModuleVisibility mv = new ModuleVisibility();
        User user = new User("test", "pass");
        mv.setUser(user);
        assertSame(user, mv.getUser());
    }

    @Test
    void testSetId() {
        ModuleVisibility mv = new ModuleVisibility();
        mv.setId("custom-id");
        assertEquals("custom-id", mv.getId());
    }

    @Test
    void testEqualsSameId() {
        ModuleVisibility m1 = new ModuleVisibility();
        ModuleVisibility m2 = new ModuleVisibility();
        m2.setId(m1.getId());
        assertEquals(m1, m2);
    }

    @Test
    void testEqualsDifferentId() {
        ModuleVisibility m1 = new ModuleVisibility();
        ModuleVisibility m2 = new ModuleVisibility();
        assertNotEquals(m1, m2);
    }

    @Test
    void testEqualsNull() {
        ModuleVisibility mv = new ModuleVisibility();
        assertNotEquals(null, mv);
    }

    @Test
    void testEqualsSameObject() {
        ModuleVisibility mv = new ModuleVisibility();
        assertEquals(mv, mv);
    }

    @Test
    void testHashCodeConsistent() {
        ModuleVisibility m1 = new ModuleVisibility();
        ModuleVisibility m2 = new ModuleVisibility();
        m2.setId(m1.getId());
        assertEquals(m1.hashCode(), m2.hashCode());
    }
}
