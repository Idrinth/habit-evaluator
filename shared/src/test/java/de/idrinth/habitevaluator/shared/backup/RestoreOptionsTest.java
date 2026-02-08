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
    }
}
