package de.idrinth.habitevaluator.android.persistence;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.DiaryReference;
import de.idrinth.habitevaluator.shared.model.EventSignificance;
import de.idrinth.habitevaluator.shared.repository.DiaryReferenceRepository;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

public class SQLiteDiaryEntryRepositoryTest {

    private SQLiteHelper dbHelper;
    private SQLiteDatabase db;
    private SQLiteDiaryEntryRepository repository;
    private DiaryReferenceRepository diaryReferenceRepository;

    @Before
    public void setUp() {
        dbHelper = mock(SQLiteHelper.class);
        db = mock(SQLiteDatabase.class);
        diaryReferenceRepository = mock(DiaryReferenceRepository.class);
        when(dbHelper.getReadableDatabase()).thenReturn(db);
        when(dbHelper.getWritableDatabase()).thenReturn(db);
        repository = new SQLiteDiaryEntryRepository(dbHelper);
        repository.setDiaryReferenceRepository(diaryReferenceRepository);
    }

    @Test
    public void testSaveCallsInsert() {
        DiaryEntry entry = new DiaryEntry("Had a great day", EventSignificance.MAJOR);
        entry.setEventDate(LocalDate.of(2024, 6, 15));

        repository.save(entry);

        verify(db).insertWithOnConflict(eq("diary_entries"), isNull(), any(), eq(5));
    }

    @Test
    public void testFindByIdNotFound() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(db.rawQuery(eq("SELECT * FROM diary_entries WHERE id = ?"), eq(new String[]{"nonexistent"}))).thenReturn(cursor);

        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    public void testFindByIdFound() {
        Cursor cursor = createEntryCursor("e1", "Great day", null, "MAJOR", "2024-06-15", "10:00", "11:00", null, null);
        when(db.rawQuery(eq("SELECT * FROM diary_entries WHERE id = ?"), eq(new String[]{"e1"}))).thenReturn(cursor);

        Optional<DiaryEntry> result = repository.findById("e1");

        assertTrue(result.isPresent());
        assertEquals("Great day", result.get().getLegacyDescription());
        assertEquals(EventSignificance.MAJOR, result.get().getSignificance());
        assertEquals(LocalDate.of(2024, 6, 15), result.get().getEventDate());
        assertEquals(LocalTime.of(10, 0), result.get().getStartTime());
        assertEquals(LocalTime.of(11, 0), result.get().getEndTime());
    }

    @Test
    public void testFindByIdWithDiaryReference() {
        DiaryReference ref = new DiaryReference("Morning workout");
        when(diaryReferenceRepository.findById("ref1")).thenReturn(Optional.of(ref));

        Cursor cursor = createEntryCursor("e1", null, "ref1", "NORMAL", "2024-06-15", null, null, null, null);
        when(db.rawQuery(eq("SELECT * FROM diary_entries WHERE id = ?"), eq(new String[]{"e1"}))).thenReturn(cursor);

        Optional<DiaryEntry> result = repository.findById("e1");

        assertTrue(result.isPresent());
        assertNotNull(result.get().getDiaryReference());
        assertEquals("Morning workout", result.get().getDiaryReference().getDescription());
    }

    @Test
    public void testFindByIdWithUser() {
        Cursor cursor = createEntryCursor("e1", "Great day", null, "NORMAL", "2024-06-15", null, null, "u1", "testuser");
        when(db.rawQuery(eq("SELECT * FROM diary_entries WHERE id = ?"), eq(new String[]{"e1"}))).thenReturn(cursor);

        Optional<DiaryEntry> result = repository.findById("e1");

        assertTrue(result.isPresent());
        assertNotNull(result.get().getUser());
        assertEquals("u1", result.get().getUser().getId());
    }

    @Test
    public void testFindAllEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(eq("SELECT * FROM diary_entries"), isNull())).thenReturn(cursor);

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    public void testDeleteById() {
        repository.deleteById("e1");

        verify(db).delete(eq("diary_entries"), eq("id = ?"), eq(new String[]{"e1"}));
    }

    @Test
    public void testFindByUserIdEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(startsWith("SELECT * FROM diary_entries WHERE user_id"), any())).thenReturn(cursor);

        assertTrue(repository.findByUserId("u1").isEmpty());
    }

    @Test
    public void testFindDistinctDescriptionsByUserId() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToNext()).thenReturn(true, true, false);
        when(cursor.getString(0)).thenReturn("Workout", "Meditation");
        when(db.rawQuery(startsWith("SELECT DISTINCT"), any())).thenReturn(cursor);

        List<String> result = repository.findDistinctDescriptionsByUserId("u1");

        assertEquals(2, result.size());
    }

    @Test
    public void testFindEntriesNeedingMigration() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(startsWith("SELECT * FROM diary_entries WHERE user_id"), any())).thenReturn(cursor);

        List<DiaryEntry> result = repository.findEntriesNeedingMigration("u1");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testSaveReturnsEntry() {
        DiaryEntry entry = new DiaryEntry("Test", EventSignificance.MINOR);
        DiaryEntry result = repository.save(entry);
        assertSame(entry, result);
    }

    private Cursor createEntryCursor(String id, String legacyDesc, String refId, String significance, String eventDate, String startTime, String endTime, String userId, String userName) {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.moveToNext()).thenReturn(false);

        when(cursor.getColumnIndexOrThrow("id")).thenReturn(0);
        when(cursor.getColumnIndexOrThrow("legacy_description")).thenReturn(1);
        when(cursor.getColumnIndexOrThrow("diary_reference_id")).thenReturn(2);
        when(cursor.getColumnIndexOrThrow("significance")).thenReturn(3);
        when(cursor.getColumnIndexOrThrow("event_date")).thenReturn(4);
        when(cursor.getColumnIndexOrThrow("created_at")).thenReturn(5);
        when(cursor.getColumnIndexOrThrow("start_time")).thenReturn(6);
        when(cursor.getColumnIndexOrThrow("end_time")).thenReturn(7);
        when(cursor.getColumnIndexOrThrow("user_id")).thenReturn(8);
        when(cursor.getColumnIndexOrThrow("user_name")).thenReturn(9);

        when(cursor.isNull(anyInt())).thenReturn(true);
        when(cursor.isNull(0)).thenReturn(false);

        when(cursor.getString(0)).thenReturn(id);

        if (legacyDesc != null) {
            when(cursor.isNull(1)).thenReturn(false);
            when(cursor.getString(1)).thenReturn(legacyDesc);
        }
        if (refId != null) {
            when(cursor.isNull(2)).thenReturn(false);
            when(cursor.getString(2)).thenReturn(refId);
        }
        if (significance != null) {
            when(cursor.isNull(3)).thenReturn(false);
            when(cursor.getString(3)).thenReturn(significance);
        }
        if (eventDate != null) {
            when(cursor.isNull(4)).thenReturn(false);
            when(cursor.getString(4)).thenReturn(eventDate);
        }
        if (startTime != null) {
            when(cursor.isNull(6)).thenReturn(false);
            when(cursor.getString(6)).thenReturn(startTime);
        }
        if (endTime != null) {
            when(cursor.isNull(7)).thenReturn(false);
            when(cursor.getString(7)).thenReturn(endTime);
        }
        if (userId != null) {
            when(cursor.isNull(8)).thenReturn(false);
            when(cursor.isNull(9)).thenReturn(false);
            when(cursor.getString(8)).thenReturn(userId);
            when(cursor.getString(9)).thenReturn(userName);
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
