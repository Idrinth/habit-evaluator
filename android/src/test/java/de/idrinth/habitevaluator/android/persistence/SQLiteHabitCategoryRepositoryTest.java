package de.idrinth.habitevaluator.android.persistence;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.User;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

public class SQLiteHabitCategoryRepositoryTest {

    private SQLiteHelper dbHelper;
    private SQLiteDatabase db;
    private SQLiteHabitCategoryRepository repository;

    @Before
    public void setUp() {
        dbHelper = mock(SQLiteHelper.class);
        db = mock(SQLiteDatabase.class);
        when(dbHelper.getReadableDatabase()).thenReturn(db);
        when(dbHelper.getWritableDatabase()).thenReturn(db);
        repository = new SQLiteHabitCategoryRepository(dbHelper);
    }

    @Test
    public void testSaveCallsInsertWithTransaction() {
        HabitCategory category = new HabitCategory();
        category.setName("Health");
        category.setDescription("Health-related habits");
        category.setColor("#FF0000");

        repository.save(category);

        verify(db).beginTransaction();
        verify(db).insertWithOnConflict(eq("habit_categories"), isNull(), any(), eq(5));
        verify(db).setTransactionSuccessful();
        verify(db).endTransaction();
    }

    @Test
    public void testSaveWithTranslations() {
        HabitCategory category = new HabitCategory();
        category.setName("Health");
        Map<String, String> nameTranslations = new HashMap<>();
        nameTranslations.put("de", "Gesundheit");
        category.setNameTranslations(nameTranslations);
        Map<String, String> descTranslations = new HashMap<>();
        descTranslations.put("de", "Gesundheitsbezogene Gewohnheiten");
        category.setDescriptionTranslations(descTranslations);

        repository.save(category);

        verify(db).delete(eq("category_name_translations"), eq("category_id = ?"), eq(new String[]{category.getId()}));
        verify(db).delete(eq("category_description_translations"), eq("category_id = ?"), eq(new String[]{category.getId()}));
        verify(db).insert(eq("category_name_translations"), isNull(), any());
        verify(db).insert(eq("category_description_translations"), isNull(), any());
    }

    @Test
    public void testFindByIdNotFound() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(db.rawQuery(eq("SELECT * FROM habit_categories WHERE id = ?"), eq(new String[]{"nonexistent"}))).thenReturn(cursor);

        Optional<HabitCategory> result = repository.findById("nonexistent");

        assertFalse(result.isPresent());
    }

    @Test
    public void testFindByIdFound() {
        Cursor cursor = createCategoryCursor("c1", "Health", "Health habits", "#FF0000", null, null);
        when(db.rawQuery(eq("SELECT * FROM habit_categories WHERE id = ?"), eq(new String[]{"c1"}))).thenReturn(cursor);

        Cursor nameTransCursor = createEmptyCursor();
        Cursor descTransCursor = createEmptyCursor();
        when(db.rawQuery(contains("category_name_translations"), any())).thenReturn(nameTransCursor);
        when(db.rawQuery(contains("category_description_translations"), any())).thenReturn(descTransCursor);

        Optional<HabitCategory> result = repository.findById("c1");

        assertTrue(result.isPresent());
        assertEquals("Health", result.get().getName());
        assertEquals("Health habits", result.get().getDescription());
        assertEquals("#FF0000", result.get().getColor());
    }

    @Test
    public void testFindByIdWithUser() {
        Cursor cursor = createCategoryCursor("c1", "Health", "Health habits", "#FF0000", "u1", "testuser");
        when(db.rawQuery(eq("SELECT * FROM habit_categories WHERE id = ?"), eq(new String[]{"c1"}))).thenReturn(cursor);

        Cursor nameTransCursor = createEmptyCursor();
        Cursor descTransCursor = createEmptyCursor();
        when(db.rawQuery(contains("category_name_translations"), any())).thenReturn(nameTransCursor);
        when(db.rawQuery(contains("category_description_translations"), any())).thenReturn(descTransCursor);

        Optional<HabitCategory> result = repository.findById("c1");

        assertTrue(result.isPresent());
        assertNotNull(result.get().getUser());
        assertEquals("u1", result.get().getUser().getId());
        assertEquals("testuser", result.get().getUser().getUsername());
    }

    @Test
    public void testFindAllEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(eq("SELECT * FROM habit_categories"), isNull())).thenReturn(cursor);

        List<HabitCategory> result = repository.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    public void testDeleteByIdWithTransaction() {
        repository.deleteById("c1");

        verify(db).beginTransaction();
        verify(db).delete(eq("category_name_translations"), eq("category_id = ?"), eq(new String[]{"c1"}));
        verify(db).delete(eq("category_description_translations"), eq("category_id = ?"), eq(new String[]{"c1"}));
        verify(db).delete(eq("habit_categories"), eq("id = ?"), eq(new String[]{"c1"}));
        verify(db).setTransactionSuccessful();
        verify(db).endTransaction();
    }

    @Test
    public void testExistsByIdTrue() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(db.rawQuery(eq("SELECT 1 FROM habit_categories WHERE id = ?"), eq(new String[]{"c1"}))).thenReturn(cursor);

        assertTrue(repository.existsById("c1"));
    }

    @Test
    public void testExistsByIdFalse() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(db.rawQuery(eq("SELECT 1 FROM habit_categories WHERE id = ?"), eq(new String[]{"c1"}))).thenReturn(cursor);

        assertFalse(repository.existsById("c1"));
    }

    @Test
    public void testFindByUserIdEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(startsWith("SELECT * FROM habit_categories WHERE user_id"), any())).thenReturn(cursor);

        List<HabitCategory> result = repository.findByUserId("u1");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testSaveReturnsCategory() {
        HabitCategory category = new HabitCategory();
        category.setName("Health");
        HabitCategory result = repository.save(category);
        assertSame(category, result);
    }

    private Cursor createCategoryCursor(String id, String name, String description, String color, String userId, String userName) {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.moveToNext()).thenReturn(false);

        when(cursor.getColumnIndexOrThrow("id")).thenReturn(0);
        when(cursor.getColumnIndexOrThrow("name")).thenReturn(1);
        when(cursor.getColumnIndexOrThrow("description")).thenReturn(2);
        when(cursor.getColumnIndexOrThrow("color")).thenReturn(3);
        when(cursor.getColumnIndexOrThrow("user_id")).thenReturn(4);
        when(cursor.getColumnIndexOrThrow("user_name")).thenReturn(5);

        when(cursor.isNull(anyInt())).thenReturn(true);
        when(cursor.isNull(0)).thenReturn(false);
        when(cursor.isNull(1)).thenReturn(false);
        when(cursor.isNull(2)).thenReturn(false);
        when(cursor.isNull(3)).thenReturn(false);

        when(cursor.getString(0)).thenReturn(id);
        when(cursor.getString(1)).thenReturn(name);
        when(cursor.getString(2)).thenReturn(description);
        when(cursor.getString(3)).thenReturn(color);

        if (userId != null) {
            when(cursor.isNull(4)).thenReturn(false);
            when(cursor.isNull(5)).thenReturn(false);
            when(cursor.getString(4)).thenReturn(userId);
            when(cursor.getString(5)).thenReturn(userName);
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
