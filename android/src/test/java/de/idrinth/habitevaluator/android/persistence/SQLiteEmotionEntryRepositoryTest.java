package de.idrinth.habitevaluator.android.persistence;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class SQLiteEmotionEntryRepositoryTest {

    private SQLiteHelper dbHelper;
    private SQLiteDatabase db;
    private EmotionPairRepository emotionPairRepository;
    private SQLiteEmotionEntryRepository repository;

    @BeforeEach
    void setUp() {
        dbHelper = mock(SQLiteHelper.class);
        db = mock(SQLiteDatabase.class);
        emotionPairRepository = mock(EmotionPairRepository.class);
        when(dbHelper.getReadableDatabase()).thenReturn(db);
        when(dbHelper.getWritableDatabase()).thenReturn(db);
        repository = new SQLiteEmotionEntryRepository(dbHelper, emotionPairRepository);
    }

    @Test
    void testSaveCallsInsert() {
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        EmotionEntry entry = new EmotionEntry(pair, 5, LocalDateTime.of(2024, 6, 15, 10, 0), "Feeling good");

        repository.save(entry);

        verify(db).insertWithOnConflict(eq("emotion_entries"), isNull(), any(), eq(5));
    }

    @Test
    void testSaveThrowsWhenPairIsNull() {
        EmotionEntry entry = new EmotionEntry();

        try {
            repository.save(entry);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("EmotionEntry requires a non-null EmotionPair", e.getMessage());
        }
    }

    @Test
    void testFindByIdNotFound() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(db.rawQuery(eq("SELECT * FROM emotion_entries WHERE id = ?"), eq(new String[]{"nonexistent"}))).thenReturn(cursor);

        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    void testFindByIdFoundWithPair() {
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        pair.setId("p1");
        when(emotionPairRepository.findById("p1")).thenReturn(Optional.of(pair));

        Cursor cursor = createEntryCursor("e1", "p1", 7, "2024-06-15T10:00:00", "Feeling great", null, null);
        when(db.rawQuery(eq("SELECT * FROM emotion_entries WHERE id = ?"), eq(new String[]{"e1"}))).thenReturn(cursor);

        Optional<EmotionEntry> result = repository.findById("e1");

        assertTrue(result.isPresent());
        assertEquals(7, result.get().getStrength());
        assertEquals("Feeling great", result.get().getNotes());
        assertNotNull(result.get().getEmotionPair());
        assertEquals("p1", result.get().getEmotionPair().getId());
    }

    @Test
    void testFindByIdReturnsEmptyWhenPairNotFound() {
        when(emotionPairRepository.findById("missing-pair")).thenReturn(Optional.empty());

        Cursor cursor = createEntryCursor("e1", "missing-pair", 5, "2024-06-15T10:00:00", null, null, null);
        when(db.rawQuery(eq("SELECT * FROM emotion_entries WHERE id = ?"), eq(new String[]{"e1"}))).thenReturn(cursor);

        Optional<EmotionEntry> result = repository.findById("e1");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByIdWithUser() {
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        pair.setId("p1");
        when(emotionPairRepository.findById("p1")).thenReturn(Optional.of(pair));

        Cursor cursor = createEntryCursor("e1", "p1", 3, "2024-06-15T10:00:00", null, "u1", "testuser");
        when(db.rawQuery(eq("SELECT * FROM emotion_entries WHERE id = ?"), eq(new String[]{"e1"}))).thenReturn(cursor);

        Optional<EmotionEntry> result = repository.findById("e1");

        assertTrue(result.isPresent());
        assertNotNull(result.get().getUser());
        assertEquals("u1", result.get().getUser().getId());
    }

    @Test
    void testFindAllEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(eq("SELECT * FROM emotion_entries"), isNull())).thenReturn(cursor);

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void testDeleteById() {
        repository.deleteById("e1");

        verify(db).delete(eq("emotion_entries"), eq("id = ?"), eq(new String[]{"e1"}));
    }

    @Test
    void testFindByUserIdEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(startsWith("SELECT * FROM emotion_entries WHERE user_id"), any())).thenReturn(cursor);

        assertTrue(repository.findByUserId("u1").isEmpty());
    }

    @Test
    void testSaveReturnsEntry() {
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        EmotionEntry entry = new EmotionEntry(pair, 5, LocalDateTime.now(), null);
        EmotionEntry result = repository.save(entry);
        assertSame(entry, result);
    }

    private Cursor createEntryCursor(String id, String pairId, int strength, String recordedAt, String notes, String userId, String userName) {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.moveToNext()).thenReturn(false);

        when(cursor.getColumnIndexOrThrow("id")).thenReturn(0);
        when(cursor.getColumnIndexOrThrow("emotion_pair_id")).thenReturn(1);
        when(cursor.getColumnIndexOrThrow("strength")).thenReturn(2);
        when(cursor.getColumnIndexOrThrow("recorded_at")).thenReturn(3);
        when(cursor.getColumnIndexOrThrow("notes")).thenReturn(4);
        when(cursor.getColumnIndexOrThrow("user_id")).thenReturn(5);
        when(cursor.getColumnIndexOrThrow("user_name")).thenReturn(6);

        when(cursor.isNull(anyInt())).thenReturn(true);
        when(cursor.isNull(0)).thenReturn(false);
        when(cursor.isNull(1)).thenReturn(false);
        when(cursor.isNull(2)).thenReturn(false);
        when(cursor.isNull(3)).thenReturn(false);

        when(cursor.getString(0)).thenReturn(id);
        when(cursor.getString(1)).thenReturn(pairId);
        when(cursor.getInt(2)).thenReturn(strength);
        when(cursor.getString(3)).thenReturn(recordedAt);

        if (notes != null) {
            when(cursor.isNull(4)).thenReturn(false);
            when(cursor.getString(4)).thenReturn(notes);
        }
        if (userId != null) {
            when(cursor.isNull(5)).thenReturn(false);
            when(cursor.isNull(6)).thenReturn(false);
            when(cursor.getString(5)).thenReturn(userId);
            when(cursor.getString(6)).thenReturn(userName);
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
