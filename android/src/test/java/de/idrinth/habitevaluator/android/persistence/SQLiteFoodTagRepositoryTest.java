package de.idrinth.habitevaluator.android.persistence;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.FoodTag;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

public class SQLiteFoodTagRepositoryTest {

    private SQLiteHelper dbHelper;
    private SQLiteDatabase db;
    private SQLiteFoodTagRepository repository;

    @Before
    public void setUp() {
        dbHelper = mock(SQLiteHelper.class);
        db = mock(SQLiteDatabase.class);
        when(dbHelper.getReadableDatabase()).thenReturn(db);
        when(dbHelper.getWritableDatabase()).thenReturn(db);
        repository = new SQLiteFoodTagRepository(dbHelper);
    }

    @Test
    public void testSaveCallsInsert() {
        FoodTag tag = new FoodTag("Vegetarian");

        repository.save(tag);

        verify(db).insertWithOnConflict(eq("food_tags"), isNull(), any(), eq(5));
    }

    @Test
    public void testFindByIdNotFound() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(db.rawQuery(eq("SELECT * FROM food_tags WHERE id = ?"), eq(new String[]{"nonexistent"}))).thenReturn(cursor);

        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    public void testFindByIdFound() {
        Cursor cursor = createTagCursor("t1", "Vegetarian", "vegetarian", null, null);
        when(db.rawQuery(eq("SELECT * FROM food_tags WHERE id = ?"), eq(new String[]{"t1"}))).thenReturn(cursor);

        Optional<FoodTag> result = repository.findById("t1");

        assertTrue(result.isPresent());
        assertEquals("Vegetarian", result.get().getName());
        assertEquals("vegetarian", result.get().getNameLower());
    }

    @Test
    public void testFindByIdWithUser() {
        Cursor cursor = createTagCursor("t1", "Vegetarian", "vegetarian", "u1", "testuser");
        when(db.rawQuery(eq("SELECT * FROM food_tags WHERE id = ?"), eq(new String[]{"t1"}))).thenReturn(cursor);

        Optional<FoodTag> result = repository.findById("t1");

        assertTrue(result.isPresent());
        assertNotNull(result.get().getUser());
        assertEquals("u1", result.get().getUser().getId());
    }

    @Test
    public void testFindByUserIdEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(startsWith("SELECT * FROM food_tags WHERE user_id"), any())).thenReturn(cursor);

        assertTrue(repository.findByUserId("u1").isEmpty());
    }

    @Test
    public void testFindByNameLowerAndUserIdFound() {
        Cursor cursor = createTagCursor("t1", "Vegetarian", "vegetarian", "u1", "testuser");
        when(db.rawQuery(
                eq("SELECT * FROM food_tags WHERE name_lower = ? AND user_id = ?"),
                eq(new String[]{"vegetarian", "u1"})
        )).thenReturn(cursor);

        Optional<FoodTag> result = repository.findByNameLowerAndUserId("vegetarian", "u1");

        assertTrue(result.isPresent());
        assertEquals("Vegetarian", result.get().getName());
    }

    @Test
    public void testFindByNameLowerAndUserIdNotFound() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(db.rawQuery(
                eq("SELECT * FROM food_tags WHERE name_lower = ? AND user_id = ?"),
                eq(new String[]{"nonexistent", "u1"})
        )).thenReturn(cursor);

        assertFalse(repository.findByNameLowerAndUserId("nonexistent", "u1").isPresent());
    }

    @Test
    public void testDeleteById() {
        repository.deleteById("t1");

        verify(db).delete(eq("food_tags"), eq("id = ?"), eq(new String[]{"t1"}));
    }

    @Test
    public void testLinkTagToFoodLog() {
        repository.linkTagToFoodLog("fl1", "t1");

        verify(db).insertWithOnConflict(eq("food_log_tags"), isNull(), any(), eq(4));
    }

    @Test
    public void testUnlinkAllTagsFromFoodLog() {
        repository.unlinkAllTagsFromFoodLog("fl1");

        verify(db).delete(eq("food_log_tags"), eq("food_log_id = ?"), eq(new String[]{"fl1"}));
    }

    @Test
    public void testFindTagsByFoodLogIdEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(startsWith("SELECT t.* FROM food_tags"), any())).thenReturn(cursor);

        assertTrue(repository.findTagsByFoodLogId("fl1").isEmpty());
    }

    @Test
    public void testSaveReturnsTag() {
        FoodTag tag = new FoodTag("Vegan");
        FoodTag result = repository.save(tag);
        assertSame(tag, result);
    }

    private Cursor createTagCursor(String id, String name, String nameLower, String userId, String userName) {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.moveToNext()).thenReturn(false);

        when(cursor.getColumnIndexOrThrow("id")).thenReturn(0);
        when(cursor.getColumnIndexOrThrow("name")).thenReturn(1);
        when(cursor.getColumnIndexOrThrow("name_lower")).thenReturn(2);
        when(cursor.getColumnIndexOrThrow("user_id")).thenReturn(3);
        when(cursor.getColumnIndexOrThrow("user_name")).thenReturn(4);

        when(cursor.isNull(anyInt())).thenReturn(true);
        when(cursor.isNull(0)).thenReturn(false);
        when(cursor.isNull(1)).thenReturn(false);
        when(cursor.isNull(2)).thenReturn(false);

        when(cursor.getString(0)).thenReturn(id);
        when(cursor.getString(1)).thenReturn(name);
        when(cursor.getString(2)).thenReturn(nameLower);

        if (userId != null) {
            when(cursor.isNull(3)).thenReturn(false);
            when(cursor.isNull(4)).thenReturn(false);
            when(cursor.getString(3)).thenReturn(userId);
            when(cursor.getString(4)).thenReturn(userName);
        }

        return cursor;
    }

    private Cursor createEmptyCursor() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(cursor.moveToNext()).thenReturn(false);
        return cursor;
    }
}
