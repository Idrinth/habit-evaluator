package de.idrinth.habitevaluator.android.persistence;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SQLiteHelperTest {

    private static final String TEST_DB_NAME = "test_helper.db";
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase(TEST_DB_NAME);
    }

    @After
    public void tearDown() {
        context.deleteDatabase(TEST_DB_NAME);
    }

    @Test
    public void testDatabaseCreation() {
        SQLiteHelper helper = SQLiteHelper.getInstance(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        assertNotNull(db);
        assertTrue(db.isOpen());
    }

    @Test
    public void testAllTablesCreated() {
        SQLiteHelper helper = SQLiteHelper.getInstance(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        List<String> tables = getTableNames(db);
        assertTrue(tables.contains("habits"));
        assertTrue(tables.contains("habit_entries"));
        assertTrue(tables.contains("habit_name_translations"));
        assertTrue(tables.contains("habit_description_translations"));
        assertTrue(tables.contains("habit_categories"));
        assertTrue(tables.contains("category_name_translations"));
        assertTrue(tables.contains("category_description_translations"));
        assertTrue(tables.contains("diary_references"));
        assertTrue(tables.contains("diary_entries"));
        assertTrue(tables.contains("sleep_entries"));
        assertTrue(tables.contains("emotion_pairs"));
        assertTrue(tables.contains("emotion_entries"));
        assertTrue(tables.contains("sport_logs"));
        assertTrue(tables.contains("food_logs"));
        assertTrue(tables.contains("food_tags"));
        assertTrue(tables.contains("food_log_tags"));
        assertTrue(tables.contains("medications"));
        assertTrue(tables.contains("medication_logs"));
    }

    @Test
    public void testForeignKeysEnabled() {
        SQLiteHelper helper = SQLiteHelper.getInstance(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("PRAGMA foreign_keys", null)) {
            assertTrue(cursor.moveToFirst());
            assertTrue(cursor.getInt(0) == 1);
        }
    }

    @Test
    public void testHabitsTableColumns() {
        SQLiteHelper helper = SQLiteHelper.getInstance(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        List<String> columns = getColumnNames(db, "habits");
        assertTrue(columns.contains("id"));
        assertTrue(columns.contains("name"));
        assertTrue(columns.contains("description"));
        assertTrue(columns.contains("category_id"));
        assertTrue(columns.contains("frequency_type"));
        assertTrue(columns.contains("target_frequency"));
        assertTrue(columns.contains("max_entries_per_day"));
        assertTrue(columns.contains("positive_scoring"));
        assertTrue(columns.contains("created_at"));
        assertTrue(columns.contains("scoring_rule_id"));
        assertTrue(columns.contains("user_id"));
    }

    @Test
    public void testSleepEntriesTableColumns() {
        SQLiteHelper helper = SQLiteHelper.getInstance(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        List<String> columns = getColumnNames(db, "sleep_entries");
        assertTrue(columns.contains("id"));
        assertTrue(columns.contains("from_time"));
        assertTrue(columns.contains("until_time"));
        assertTrue(columns.contains("date"));
        assertTrue(columns.contains("created_at"));
        assertTrue(columns.contains("notes"));
        assertTrue(columns.contains("user_id"));
    }

    @Test
    public void testMedicationsTableColumns() {
        SQLiteHelper helper = SQLiteHelper.getInstance(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        List<String> columns = getColumnNames(db, "medications");
        assertTrue(columns.contains("id"));
        assertTrue(columns.contains("name"));
        assertTrue(columns.contains("wikipedia_link"));
        assertTrue(columns.contains("provision_type"));
        assertTrue(columns.contains("user_id"));
    }

    @Test
    public void testIndicesCreated() {
        SQLiteHelper helper = SQLiteHelper.getInstance(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        List<String> indices = getIndexNames(db);
        assertTrue(indices.contains("idx_habit_entries_habit_id"));
        assertTrue(indices.contains("idx_habits_user_id"));
        assertTrue(indices.contains("idx_habit_categories_user_id"));
        assertTrue(indices.contains("idx_diary_entries_user_id"));
        assertTrue(indices.contains("idx_sleep_entries_user_id"));
        assertTrue(indices.contains("idx_emotion_pairs_user_id"));
        assertTrue(indices.contains("idx_emotion_entries_user_id"));
        assertTrue(indices.contains("idx_sport_logs_user_id"));
        assertTrue(indices.contains("idx_food_logs_user_id"));
        assertTrue(indices.contains("idx_medications_user_id"));
        assertTrue(indices.contains("idx_medication_logs_user_id"));
    }

    @Test
    public void testGetInstanceReturnsSameInstance() {
        SQLiteHelper first = SQLiteHelper.getInstance(context);
        SQLiteHelper second = SQLiteHelper.getInstance(context);
        assertTrue(first == second);
    }

    private List<String> getTableNames(SQLiteDatabase db) {
        List<String> tables = new ArrayList<>();
        try (Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%'",
                null)) {
            while (cursor.moveToNext()) {
                tables.add(cursor.getString(0));
            }
        }
        return tables;
    }

    private List<String> getColumnNames(SQLiteDatabase db, String table) {
        List<String> columns = new ArrayList<>();
        try (Cursor cursor = db.rawQuery("PRAGMA table_info(" + table + ")", null)) {
            while (cursor.moveToNext()) {
                columns.add(cursor.getString(1));
            }
        }
        return columns;
    }

    private List<String> getIndexNames(SQLiteDatabase db) {
        List<String> indices = new ArrayList<>();
        try (Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='index' AND name NOT LIKE 'sqlite_%'",
                null)) {
            while (cursor.moveToNext()) {
                indices.add(cursor.getString(0));
            }
        }
        return indices;
    }
}
