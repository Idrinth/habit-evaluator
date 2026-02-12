package de.idrinth.habitevaluator.android.persistence;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.DiaryReference;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

public class SQLiteDiaryReferenceRepositoryTest {

    private SQLiteHelper dbHelper;
    private SQLiteDatabase db;
    private SQLiteDiaryReferenceRepository repository;

    @Before
    public void setUp() {
        dbHelper = mock(SQLiteHelper.class);
        db = mock(SQLiteDatabase.class);
        when(dbHelper.getReadableDatabase()).thenReturn(db);
        when(dbHelper.getWritableDatabase()).thenReturn(db);
        repository = new SQLiteDiaryReferenceRepository(dbHelper);
    }

    @Test
    public void testSaveCallsInsert() {
        DiaryReference reference = new DiaryReference("Morning workout");

        repository.save(reference);

        verify(db).insertWithOnConflict(eq("diary_references"), isNull(), any(), eq(5));
    }

    @Test
    public void testFindByIdNotFound() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(db.rawQuery(eq("SELECT * FROM diary_references WHERE id = ?"), eq(new String[]{"nonexistent"}))).thenReturn(cursor);

        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    public void testFindByIdFound() {
        Cursor cursor = createReferenceCursor("r1", "Morning workout", "morning workout", null, null);
        when(db.rawQuery(eq("SELECT * FROM diary_references WHERE id = ?"), eq(new String[]{"r1"}))).thenReturn(cursor);

        Optional<DiaryReference> result = repository.findById("r1");

        assertTrue(result.isPresent());
        assertEquals("Morning workout", result.get().getDescription());
        assertEquals("morning workout", result.get().getDescriptionLower());
    }

    @Test
    public void testFindByIdWithUser() {
        Cursor cursor = createReferenceCursor("r1", "Morning workout", "morning workout", "u1", "testuser");
        when(db.rawQuery(eq("SELECT * FROM diary_references WHERE id = ?"), eq(new String[]{"r1"}))).thenReturn(cursor);

        Optional<DiaryReference> result = repository.findById("r1");

        assertTrue(result.isPresent());
        assertNotNull(result.get().getUser());
        assertEquals("u1", result.get().getUser().getId());
    }

    @Test
    public void testFindAllEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(eq("SELECT * FROM diary_references"), isNull())).thenReturn(cursor);

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    public void testDeleteById() {
        repository.deleteById("r1");

        verify(db).delete(eq("diary_references"), eq("id = ?"), eq(new String[]{"r1"}));
    }

    @Test
    public void testFindByUserIdEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(eq("SELECT * FROM diary_references WHERE user_id = ?"), eq(new String[]{"u1"}))).thenReturn(cursor);

        assertTrue(repository.findByUserId("u1").isEmpty());
    }

    @Test
    public void testFindByUserIdAndDescriptionIgnoreCaseWithNullDescription() {
        Optional<DiaryReference> result = repository.findByUserIdAndDescriptionIgnoreCase("u1", null);

        assertFalse(result.isPresent());
        verifyNoInteractions(db);
    }

    @Test
    public void testFindByUserIdAndDescriptionIgnoreCaseFound() {
        Cursor cursor = createReferenceCursor("r1", "Morning Workout", "morning workout", "u1", "testuser");
        when(db.rawQuery(
                eq("SELECT * FROM diary_references WHERE user_id = ? AND description_lower = ?"),
                eq(new String[]{"u1", "morning workout"})
        )).thenReturn(cursor);

        Optional<DiaryReference> result = repository.findByUserIdAndDescriptionIgnoreCase("u1", "Morning Workout");

        assertTrue(result.isPresent());
        assertEquals("Morning Workout", result.get().getDescription());
    }

    @Test
    public void testFindByUserIdAndDescriptionIgnoreCaseNotFound() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(db.rawQuery(
                eq("SELECT * FROM diary_references WHERE user_id = ? AND description_lower = ?"),
                eq(new String[]{"u1", "nonexistent"})
        )).thenReturn(cursor);

        assertFalse(repository.findByUserIdAndDescriptionIgnoreCase("u1", "nonexistent").isPresent());
    }

    @Test
    public void testFindDistinctDescriptionsByUserId() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToNext()).thenReturn(true, true, false);
        when(cursor.getString(0)).thenReturn("Morning workout", "Evening walk");
        when(db.rawQuery(startsWith("SELECT DISTINCT description FROM diary_references"), eq(new String[]{"u1"}))).thenReturn(cursor);

        List<String> result = repository.findDistinctDescriptionsByUserId("u1");

        assertEquals(2, result.size());
        assertEquals("Morning workout", result.get(0));
        assertEquals("Evening walk", result.get(1));
    }

    @Test
    public void testSaveReturnsReference() {
        DiaryReference reference = new DiaryReference("Test");
        DiaryReference result = repository.save(reference);
        assertSame(reference, result);
    }

    private Cursor createReferenceCursor(String id, String description, String descriptionLower, String userId, String userName) {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.moveToNext()).thenReturn(false);

        when(cursor.getColumnIndexOrThrow("id")).thenReturn(0);
        when(cursor.getColumnIndexOrThrow("description")).thenReturn(1);
        when(cursor.getColumnIndexOrThrow("description_lower")).thenReturn(2);
        when(cursor.getColumnIndexOrThrow("user_id")).thenReturn(3);
        when(cursor.getColumnIndexOrThrow("user_name")).thenReturn(4);

        when(cursor.isNull(anyInt())).thenReturn(true);
        when(cursor.isNull(0)).thenReturn(false);
        when(cursor.isNull(1)).thenReturn(false);
        when(cursor.isNull(2)).thenReturn(false);

        when(cursor.getString(0)).thenReturn(id);
        when(cursor.getString(1)).thenReturn(description);
        when(cursor.getString(2)).thenReturn(descriptionLower);

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
