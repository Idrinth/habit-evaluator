package de.idrinth.habitevaluator.shared.backup;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RestoreOptionsTest {

    @Test
    void testDefaultConstructorAllTrue() {
        RestoreOptions options = new RestoreOptions();
        assertTrue(options.isRestoreCategories());
        assertTrue(options.isRestoreHabits());
        assertTrue(options.isRestoreDiaryEntries());
        assertTrue(options.isRestoreSleepEntries());
        assertTrue(options.isRestoreSportLogs());
        assertTrue(options.isRestoreFoodLogs());
        assertTrue(options.isRestoreEmotionData());
        assertTrue(options.isRestoreMeetingEntries());
        assertTrue(options.isRestoreActivityLogs());
        assertTrue(options.isRestoreMedicationData());
        assertTrue(options.isRestoreReminderSettings());
        assertTrue(options.isRestoreModuleVisibility());
        assertTrue(options.isRestoreEmergencyPlan());
    }

    @Test
    void testAllFactoryMethod() {
        RestoreOptions options = RestoreOptions.all();
        assertTrue(options.isRestoreCategories());
        assertTrue(options.isRestoreHabits());
        assertTrue(options.isRestoreDiaryEntries());
        assertTrue(options.isRestoreSleepEntries());
        assertTrue(options.isRestoreSportLogs());
        assertTrue(options.isRestoreFoodLogs());
        assertTrue(options.isRestoreEmotionData());
        assertTrue(options.isRestoreMeetingEntries());
        assertTrue(options.isRestoreActivityLogs());
        assertTrue(options.isRestoreMedicationData());
        assertTrue(options.isRestoreReminderSettings());
        assertTrue(options.isRestoreModuleVisibility());
        assertTrue(options.isRestoreEmergencyPlan());
    }

    @Test
    void testNoneFactoryMethod() {
        RestoreOptions options = RestoreOptions.none();
        assertFalse(options.isRestoreCategories());
        assertFalse(options.isRestoreHabits());
        assertFalse(options.isRestoreDiaryEntries());
        assertFalse(options.isRestoreSleepEntries());
        assertFalse(options.isRestoreSportLogs());
        assertFalse(options.isRestoreFoodLogs());
        assertFalse(options.isRestoreEmotionData());
        assertFalse(options.isRestoreMeetingEntries());
        assertFalse(options.isRestoreActivityLogs());
        assertFalse(options.isRestoreMedicationData());
        assertFalse(options.isRestoreReminderSettings());
        assertFalse(options.isRestoreModuleVisibility());
        assertFalse(options.isRestoreEmergencyPlan());
    }

    @Test
    void testSetters() {
        RestoreOptions options = RestoreOptions.all();

        options.setRestoreCategories(false);
        assertFalse(options.isRestoreCategories());

        options.setRestoreHabits(false);
        assertFalse(options.isRestoreHabits());

        options.setRestoreDiaryEntries(false);
        assertFalse(options.isRestoreDiaryEntries());

        options.setRestoreSleepEntries(false);
        assertFalse(options.isRestoreSleepEntries());

        options.setRestoreSportLogs(false);
        assertFalse(options.isRestoreSportLogs());

        options.setRestoreFoodLogs(false);
        assertFalse(options.isRestoreFoodLogs());

        options.setRestoreEmotionData(false);
        assertFalse(options.isRestoreEmotionData());

        options.setRestoreMeetingEntries(false);
        assertFalse(options.isRestoreMeetingEntries());

        options.setRestoreActivityLogs(false);
        assertFalse(options.isRestoreActivityLogs());

        options.setRestoreMedicationData(false);
        assertFalse(options.isRestoreMedicationData());

        options.setRestoreReminderSettings(false);
        assertFalse(options.isRestoreReminderSettings());

        options.setRestoreModuleVisibility(false);
        assertFalse(options.isRestoreModuleVisibility());

        options.setRestoreEmergencyPlan(false);
        assertFalse(options.isRestoreEmergencyPlan());
    }

    @Test
    void testIndividualFlags() {
        RestoreOptions options = RestoreOptions.none();
        options.setRestoreHabits(true);

        assertFalse(options.isRestoreCategories());
        assertTrue(options.isRestoreHabits());
        assertFalse(options.isRestoreDiaryEntries());
        assertFalse(options.isRestoreSleepEntries());
        assertFalse(options.isRestoreSportLogs());
        assertFalse(options.isRestoreFoodLogs());
        assertFalse(options.isRestoreEmotionData());
        assertFalse(options.isRestoreMeetingEntries());
        assertFalse(options.isRestoreActivityLogs());
        assertFalse(options.isRestoreMedicationData());
        assertFalse(options.isRestoreReminderSettings());
    }

    @Test
    void testIndividualEmotionFlag() {
        RestoreOptions options = RestoreOptions.none();
        options.setRestoreEmotionData(true);

        assertFalse(options.isRestoreCategories());
        assertFalse(options.isRestoreHabits());
        assertTrue(options.isRestoreEmotionData());
        assertFalse(options.isRestoreMeetingEntries());
        assertFalse(options.isRestoreMedicationData());
        assertFalse(options.isRestoreReminderSettings());
    }

    @Test
    void testIndividualMeetingFlag() {
        RestoreOptions options = RestoreOptions.none();
        options.setRestoreMeetingEntries(true);

        assertFalse(options.isRestoreCategories());
        assertTrue(options.isRestoreMeetingEntries());
        assertFalse(options.isRestoreMedicationData());
    }

    @Test
    void testIndividualMedicationFlag() {
        RestoreOptions options = RestoreOptions.none();
        options.setRestoreMedicationData(true);

        assertFalse(options.isRestoreCategories());
        assertFalse(options.isRestoreMeetingEntries());
        assertTrue(options.isRestoreMedicationData());
    }

    @Test
    void testIndividualActivityLogFlag() {
        RestoreOptions options = RestoreOptions.none();
        options.setRestoreActivityLogs(true);

        assertFalse(options.isRestoreCategories());
        assertFalse(options.isRestoreMeetingEntries());
        assertTrue(options.isRestoreActivityLogs());
        assertFalse(options.isRestoreMedicationData());
    }

    @Test
    void testIndividualReminderFlag() {
        RestoreOptions options = RestoreOptions.none();
        options.setRestoreReminderSettings(true);

        assertFalse(options.isRestoreCategories());
        assertTrue(options.isRestoreReminderSettings());
    }

    @Test
    void testIndividualModuleVisibilityFlag() {
        RestoreOptions options = RestoreOptions.none();
        options.setRestoreModuleVisibility(true);

        assertFalse(options.isRestoreCategories());
        assertFalse(options.isRestoreMeetingEntries());
        assertTrue(options.isRestoreModuleVisibility());
        assertFalse(options.isRestoreEmergencyPlan());
    }

    @Test
    void testIndividualEmergencyPlanFlag() {
        RestoreOptions options = RestoreOptions.none();
        options.setRestoreEmergencyPlan(true);

        assertFalse(options.isRestoreCategories());
        assertFalse(options.isRestoreModuleVisibility());
        assertTrue(options.isRestoreEmergencyPlan());
    }
}
