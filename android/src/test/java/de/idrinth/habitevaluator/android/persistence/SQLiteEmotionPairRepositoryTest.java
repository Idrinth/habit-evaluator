package de.idrinth.habitevaluator.android.persistence;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.EmotionPair;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

public class SQLiteEmotionPairRepositoryTest {

    private SQLiteHelper dbHelper;
    private SQLiteDatabase db;
    private SQLiteEmotionPairRepository repository;

    @Before
    public void setUp() {
        dbHelper = mock(SQLiteHelper.class);
        db = mock(SQLiteDatabase.class);
        when(dbHelper.getReadableDatabase()).thenReturn(db);
        when(dbHelper.getWritableDatabase()).thenReturn(db);
        repository = new SQLiteEmotionPairRepository(dbHelper);
    }

    @Test
    public void testSaveCallsInsert() {
        EmotionPair pair = new EmotionPair("Sad", "Happy");

        repository.save(pair);

        verify(db).insertWithOnConflict(eq("emotion_pairs"), isNull(), any(), eq(5));
    }

    @Test
    public void testFindByIdNotFound() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(db.rawQuery(eq("SELECT * FROM emotion_pairs WHERE id = ?"), eq(new String[]{"nonexistent"}))).thenReturn(cursor);

        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    public void testFindByIdFound() {
        Cursor cursor = createPairCursor("p1", "Sad", "Happy", null, null);
        when(db.rawQuery(eq("SELECT * FROM emotion_pairs WHERE id = ?"), eq(new String[]{"p1"}))).thenReturn(cursor);

        Optional<EmotionPair> result = repository.findById("p1");

        assertTrue(result.isPresent());
        assertEquals("Sad", result.get().getNegativeLabel());
        assertEquals("Happy", result.get().getPositiveLabel());
    }

    @Test
    public void testFindByIdWithUser() {
        Cursor cursor = createPairCursor("p1", "Sad", "Happy", "u1", "testuser");
        when(db.rawQuery(eq("SELECT * FROM emotion_pairs WHERE id = ?"), eq(new String[]{"p1"}))).thenReturn(cursor);

        Optional<EmotionPair> result = repository.findById("p1");

        assertTrue(result.isPresent());
        assertNotNull(result.get().getUser());
        assertEquals("u1", result.get().getUser().getId());
        assertEquals("testuser", result.get().getUser().getUsername());
    }

    @Test
    public void testFindAllEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(eq("SELECT * FROM emotion_pairs"), isNull())).thenReturn(cursor);

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    public void testDeleteById() {
        repository.deleteById("p1");

        verify(db).delete(eq("emotion_pairs"), eq("id = ?"), eq(new String[]{"p1"}));
    }

    @Test
    public void testFindByUserIdEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(startsWith("SELECT * FROM emotion_pairs WHERE user_id"), any())).thenReturn(cursor);

        assertTrue(repository.findByUserId("u1").isEmpty());
    }

    @Test
    public void testSaveReturnsEmotionPair() {
        EmotionPair pair = new EmotionPair("Anxious", "Calm");
        EmotionPair result = repository.save(pair);
        assertSame(pair, result);
    }

    private Cursor createPairCursor(String id, String negativeLabel, String positiveLabel, String userId, String userName) {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.moveToNext()).thenReturn(false);

        when(cursor.getColumnIndexOrThrow("id")).thenReturn(0);
        when(cursor.getColumnIndexOrThrow("negative_label")).thenReturn(1);
        when(cursor.getColumnIndexOrThrow("positive_label")).thenReturn(2);
        when(cursor.getColumnIndexOrThrow("user_id")).thenReturn(3);
        when(cursor.getColumnIndexOrThrow("user_name")).thenReturn(4);

        when(cursor.isNull(anyInt())).thenReturn(true);
        when(cursor.isNull(0)).thenReturn(false);
        when(cursor.isNull(1)).thenReturn(false);
        when(cursor.isNull(2)).thenReturn(false);

        when(cursor.getString(0)).thenReturn(id);
        when(cursor.getString(1)).thenReturn(negativeLabel);
        when(cursor.getString(2)).thenReturn(positiveLabel);

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
