package de.idrinth.habitevaluator.android.persistence;

import android.database.sqlite.SQLiteDatabase;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SQLiteHelperTest {

    @Test
    void testGetInstanceReturnsNonNull() {
        // With returnDefaultValues = true, Context methods return defaults
        android.content.Context context = mock(android.content.Context.class);
        when(context.getApplicationContext()).thenReturn(context);

        SQLiteHelper instance = SQLiteHelper.getInstance(context);
        assertNotNull(instance);
    }

    @Test
    void testGetInstanceReturnsSameInstance() {
        android.content.Context context = mock(android.content.Context.class);
        when(context.getApplicationContext()).thenReturn(context);

        SQLiteHelper instance1 = SQLiteHelper.getInstance(context);
        SQLiteHelper instance2 = SQLiteHelper.getInstance(context);
        assertSame(instance1, instance2);
    }

    @Test
    void testOnConfigureEnablesForeignKeys() {
        android.content.Context context = mock(android.content.Context.class);
        when(context.getApplicationContext()).thenReturn(context);

        SQLiteHelper helper = SQLiteHelper.getInstance(context);
        SQLiteDatabase db = mock(SQLiteDatabase.class);

        helper.onConfigure(db);

        verify(db).setForeignKeyConstraintsEnabled(true);
    }

    @Test
    void testOnCreateExecutesSqlStatements() {
        android.content.Context context = mock(android.content.Context.class);
        when(context.getApplicationContext()).thenReturn(context);

        SQLiteHelper helper = SQLiteHelper.getInstance(context);
        SQLiteDatabase db = mock(SQLiteDatabase.class);

        helper.onCreate(db);

        // Verify core tables are created
        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE habits"));
        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE habit_entries"));
        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE habit_categories"));
        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE diary_entries"));
        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE diary_references"));
        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE sleep_entries"));
        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE emotion_pairs"));
        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE emotion_entries"));
        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE sport_logs"));
        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE food_logs"));
        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE food_tags"));
        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE food_log_tags"));
        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE medications"));
        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE medication_logs"));
        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE emergency_plan_steps"));
        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE emergency_plan_actions"));
    }

    @Test
    void testOnCreateCreatesTranslationTables() {
        android.content.Context context = mock(android.content.Context.class);
        when(context.getApplicationContext()).thenReturn(context);

        SQLiteHelper helper = SQLiteHelper.getInstance(context);
        SQLiteDatabase db = mock(SQLiteDatabase.class);

        helper.onCreate(db);

        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE habit_name_translations"));
        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE habit_description_translations"));
        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE category_name_translations"));
        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE category_description_translations"));
    }

    @Test
    void testOnCreateCreatesIndexes() {
        android.content.Context context = mock(android.content.Context.class);
        when(context.getApplicationContext()).thenReturn(context);

        SQLiteHelper helper = SQLiteHelper.getInstance(context);
        SQLiteDatabase db = mock(SQLiteDatabase.class);

        helper.onCreate(db);

        verify(db, atLeastOnce()).execSQL(contains("CREATE INDEX idx_habit_entries_habit_id"));
        verify(db, atLeastOnce()).execSQL(contains("CREATE INDEX idx_habits_user_id"));
        verify(db, atLeastOnce()).execSQL(contains("CREATE INDEX idx_diary_entries_user_id"));
        verify(db, atLeastOnce()).execSQL(contains("CREATE INDEX idx_sleep_entries_user_id"));
        verify(db, atLeastOnce()).execSQL(contains("CREATE INDEX idx_emotion_pairs_user_id"));
        verify(db, atLeastOnce()).execSQL(contains("CREATE INDEX idx_emergency_plan_steps_user_id"));
        verify(db, atLeastOnce()).execSQL(contains("CREATE INDEX idx_emergency_plan_actions_step_id"));
    }

    @Test
    void testOnUpgradeFromVersion1To9() {
        android.content.Context context = mock(android.content.Context.class);
        when(context.getApplicationContext()).thenReturn(context);

        SQLiteHelper helper = SQLiteHelper.getInstance(context);
        SQLiteDatabase db = mock(SQLiteDatabase.class);

        helper.onUpgrade(db, 1, 9);

        // Version 2: diary_entries gets start_time and end_time
        verify(db).execSQL(contains("ALTER TABLE diary_entries ADD COLUMN start_time"));
        verify(db).execSQL(contains("ALTER TABLE diary_entries ADD COLUMN end_time"));
        // Version 3: sport_logs table
        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE IF NOT EXISTS sport_logs"));
        // Version 4+5: food_logs table (created in v4, recreated via tmp in v5)
        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE IF NOT EXISTS food_logs"));
        // Version 6: food_tags
        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE IF NOT EXISTS food_tags"));
        // Version 7: medications
        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE IF NOT EXISTS medications"));
        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE IF NOT EXISTS medication_logs"));
        // Version 8: emergency_plan_steps
        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE IF NOT EXISTS emergency_plan_steps"));
        // Version 9: emergency_plan_actions
        verify(db, atLeastOnce()).execSQL(contains("CREATE TABLE IF NOT EXISTS emergency_plan_actions"));
    }

    @Test
    void testOnUpgradeFromVersion7To8() {
        android.content.Context context = mock(android.content.Context.class);
        when(context.getApplicationContext()).thenReturn(context);

        SQLiteHelper helper = SQLiteHelper.getInstance(context);
        SQLiteDatabase db = mock(SQLiteDatabase.class);

        helper.onUpgrade(db, 7, 8);

        // Should NOT run migrations for versions <= 7
        verify(db, never()).execSQL(contains("ALTER TABLE diary_entries ADD COLUMN start_time"));
        verify(db, never()).execSQL(contains("CREATE TABLE IF NOT EXISTS sport_logs"));
        verify(db, never()).execSQL(contains("CREATE TABLE IF NOT EXISTS medications"));

        // Should run version 8 migration
        verify(db).execSQL(contains("CREATE TABLE IF NOT EXISTS emergency_plan_steps"));
        verify(db).execSQL(contains("CREATE INDEX IF NOT EXISTS idx_emergency_plan_steps_user_id"));
    }

    @Test
    void testOnUpgradeFromVersion8To9() {
        android.content.Context context = mock(android.content.Context.class);
        when(context.getApplicationContext()).thenReturn(context);

        SQLiteHelper helper = SQLiteHelper.getInstance(context);
        SQLiteDatabase db = mock(SQLiteDatabase.class);

        helper.onUpgrade(db, 8, 9);

        // Should NOT run earlier migrations
        verify(db, never()).execSQL(contains("ALTER TABLE diary_entries ADD COLUMN start_time"));
        verify(db, never()).execSQL(contains("CREATE TABLE IF NOT EXISTS sport_logs"));
        verify(db, never()).execSQL(contains("CREATE TABLE IF NOT EXISTS medications"));

        // Should run version 9 migration: create actions table, migrate data, recreate steps table
        verify(db).execSQL(contains("CREATE TABLE IF NOT EXISTS emergency_plan_actions"));
        verify(db).execSQL(contains("CREATE INDEX IF NOT EXISTS idx_emergency_plan_actions_step_id"));
        verify(db).execSQL(contains("INSERT INTO emergency_plan_actions"));
        verify(db).execSQL(contains("emergency_plan_steps_new"));
    }

    @Test
    void testOnUpgradeFromVersion6To7() {
        android.content.Context context = mock(android.content.Context.class);
        when(context.getApplicationContext()).thenReturn(context);

        SQLiteHelper helper = SQLiteHelper.getInstance(context);
        SQLiteDatabase db = mock(SQLiteDatabase.class);

        helper.onUpgrade(db, 6, 7);

        // Should NOT run migrations for versions <= 6
        verify(db, never()).execSQL(contains("ALTER TABLE diary_entries ADD COLUMN start_time"));
        verify(db, never()).execSQL(contains("CREATE TABLE IF NOT EXISTS sport_logs"));

        // Should run version 7 migration
        verify(db).execSQL(contains("CREATE TABLE IF NOT EXISTS medications"));
        verify(db).execSQL(contains("CREATE TABLE IF NOT EXISTS medication_logs"));
    }

    @Test
    void testOnUpgradeFromVersion2To3() {
        android.content.Context context = mock(android.content.Context.class);
        when(context.getApplicationContext()).thenReturn(context);

        SQLiteHelper helper = SQLiteHelper.getInstance(context);
        SQLiteDatabase db = mock(SQLiteDatabase.class);

        helper.onUpgrade(db, 2, 3);

        // Should NOT run version 2 migration (already at v2)
        verify(db, never()).execSQL(contains("ALTER TABLE diary_entries ADD COLUMN start_time"));

        // Should run version 3 migration
        verify(db).execSQL(contains("CREATE TABLE IF NOT EXISTS sport_logs"));
    }

    @Test
    void testOnUpgradeFromVersion4To5() {
        android.content.Context context = mock(android.content.Context.class);
        when(context.getApplicationContext()).thenReturn(context);

        SQLiteHelper helper = SQLiteHelper.getInstance(context);
        SQLiteDatabase db = mock(SQLiteDatabase.class);

        helper.onUpgrade(db, 4, 5);

        // Version 5 recreates food_logs table
        verify(db).execSQL(contains("CREATE TABLE IF NOT EXISTS food_logs_tmp"));
        verify(db).execSQL(contains("INSERT INTO food_logs_tmp SELECT * FROM food_logs"));
        verify(db).execSQL(contains("DROP TABLE food_logs"));
        verify(db).execSQL(contains("ALTER TABLE food_logs_tmp RENAME TO food_logs"));
    }

    @Test
    void testOnUpgradeFromCurrentVersionDoesNothing() {
        android.content.Context context = mock(android.content.Context.class);
        when(context.getApplicationContext()).thenReturn(context);

        SQLiteHelper helper = SQLiteHelper.getInstance(context);
        SQLiteDatabase db = mock(SQLiteDatabase.class);

        helper.onUpgrade(db, 9, 9);

        verify(db, never()).execSQL(anyString());
    }
}
