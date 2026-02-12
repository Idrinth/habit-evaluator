package de.idrinth.habitevaluator.android.persistence;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.Medication;
import de.idrinth.habitevaluator.shared.model.MedicationLog;
import de.idrinth.habitevaluator.shared.model.MedicationProvisionType;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

public class SQLiteMedicationLogRepositoryTest {

    private SQLiteHelper dbHelper;
    private SQLiteDatabase db;
    private SQLiteMedicationRepository medicationRepository;
    private SQLiteMedicationLogRepository repository;

    @Before
    public void setUp() {
        dbHelper = mock(SQLiteHelper.class);
        db = mock(SQLiteDatabase.class);
        medicationRepository = mock(SQLiteMedicationRepository.class);
        when(dbHelper.getReadableDatabase()).thenReturn(db);
        when(dbHelper.getWritableDatabase()).thenReturn(db);
        repository = new SQLiteMedicationLogRepository(dbHelper, medicationRepository);
    }

    @Test
    public void testSaveCallsInsert() {
        Medication medication = new Medication("Ibuprofen", MedicationProvisionType.PILL);
        MedicationLog log = new MedicationLog(medication, 400.0, LocalDateTime.of(2024, 6, 15, 8, 0));

        repository.save(log);

        verify(db).insertWithOnConflict(eq("medication_logs"), isNull(), any(), eq(5));
    }

    @Test
    public void testFindByIdNotFound() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(db.rawQuery(eq("SELECT * FROM medication_logs WHERE id = ?"), eq(new String[]{"nonexistent"}))).thenReturn(cursor);

        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    public void testFindByIdFoundWithMedication() {
        Medication medication = new Medication("Ibuprofen", MedicationProvisionType.PILL);
        medication.setId("med1");
        when(medicationRepository.findById("med1")).thenReturn(Optional.of(medication));

        Cursor cursor = createLogCursor("l1", "med1", 400.0, "2024-06-15T08:00:00", "Before meal", null, null);
        when(db.rawQuery(eq("SELECT * FROM medication_logs WHERE id = ?"), eq(new String[]{"l1"}))).thenReturn(cursor);

        Optional<MedicationLog> result = repository.findById("l1");

        assertTrue(result.isPresent());
        assertEquals(400.0, result.get().getAmount(), 0.01);
        assertEquals("Before meal", result.get().getNotes());
        assertNotNull(result.get().getMedication());
        assertEquals("med1", result.get().getMedication().getId());
    }

    @Test
    public void testFindByIdWithoutMedication() {
        when(medicationRepository.findById("missing")).thenReturn(Optional.empty());

        Cursor cursor = createLogCursor("l1", "missing", 200.0, "2024-06-15T08:00:00", null, null, null);
        when(db.rawQuery(eq("SELECT * FROM medication_logs WHERE id = ?"), eq(new String[]{"l1"}))).thenReturn(cursor);

        Optional<MedicationLog> result = repository.findById("l1");

        assertTrue(result.isPresent());
        assertNull(result.get().getMedication());
    }

    @Test
    public void testFindByIdWithUser() {
        when(medicationRepository.findById(anyString())).thenReturn(Optional.empty());

        Cursor cursor = createLogCursor("l1", "med1", 400.0, "2024-06-15T08:00:00", null, "u1", "testuser");
        when(db.rawQuery(eq("SELECT * FROM medication_logs WHERE id = ?"), eq(new String[]{"l1"}))).thenReturn(cursor);

        Optional<MedicationLog> result = repository.findById("l1");

        assertTrue(result.isPresent());
        assertNotNull(result.get().getUser());
        assertEquals("u1", result.get().getUser().getId());
    }

    @Test
    public void testFindAllEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(eq("SELECT * FROM medication_logs"), isNull())).thenReturn(cursor);

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    public void testDeleteById() {
        repository.deleteById("l1");

        verify(db).delete(eq("medication_logs"), eq("id = ?"), eq(new String[]{"l1"}));
    }

    @Test
    public void testFindByUserIdEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(startsWith("SELECT * FROM medication_logs WHERE user_id = ?"), any())).thenReturn(cursor);

        assertTrue(repository.findByUserId("u1").isEmpty());
    }

    @Test
    public void testFindByUserIdPagedEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(contains("LIMIT"), any())).thenReturn(cursor);

        List<MedicationLog> result = repository.findByUserIdPaged("u1", 10, 0);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testCountByUserIdZero() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.getInt(0)).thenReturn(0);
        when(db.rawQuery(startsWith("SELECT COUNT(*)"), any())).thenReturn(cursor);

        assertEquals(0, repository.countByUserId("u1"));
    }

    @Test
    public void testCountByUserIdWithResults() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.getInt(0)).thenReturn(5);
        when(db.rawQuery(startsWith("SELECT COUNT(*)"), any())).thenReturn(cursor);

        assertEquals(5, repository.countByUserId("u1"));
    }

    @Test
    public void testCountByUserIdNoRows() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(db.rawQuery(startsWith("SELECT COUNT(*)"), any())).thenReturn(cursor);

        assertEquals(0, repository.countByUserId("u1"));
    }

    @Test
    public void testSaveReturnsLog() {
        Medication medication = new Medication("Test", MedicationProvisionType.PILL);
        MedicationLog log = new MedicationLog(medication, 100.0, LocalDateTime.now());
        MedicationLog result = repository.save(log);
        assertSame(log, result);
    }

    private Cursor createLogCursor(String id, String medicationId, double amount, String takenAt, String notes, String userId, String userName) {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.moveToNext()).thenReturn(false);

        when(cursor.getColumnIndexOrThrow("id")).thenReturn(0);
        when(cursor.getColumnIndexOrThrow("medication_id")).thenReturn(1);
        when(cursor.getColumnIndexOrThrow("amount")).thenReturn(2);
        when(cursor.getColumnIndexOrThrow("taken_at")).thenReturn(3);
        when(cursor.getColumnIndexOrThrow("notes")).thenReturn(4);
        when(cursor.getColumnIndexOrThrow("created_at")).thenReturn(5);
        when(cursor.getColumnIndexOrThrow("user_id")).thenReturn(6);
        when(cursor.getColumnIndexOrThrow("user_name")).thenReturn(7);

        when(cursor.isNull(anyInt())).thenReturn(true);
        when(cursor.isNull(0)).thenReturn(false);
        when(cursor.isNull(2)).thenReturn(false);
        when(cursor.isNull(3)).thenReturn(false);
        when(cursor.isNull(5)).thenReturn(false);

        when(cursor.getString(0)).thenReturn(id);
        when(cursor.getDouble(2)).thenReturn(amount);
        when(cursor.getString(3)).thenReturn(takenAt);
        when(cursor.getString(5)).thenReturn("2024-06-15T10:00:00");

        if (medicationId != null) {
            when(cursor.isNull(1)).thenReturn(false);
            when(cursor.getString(1)).thenReturn(medicationId);
        }
        if (notes != null) {
            when(cursor.isNull(4)).thenReturn(false);
            when(cursor.getString(4)).thenReturn(notes);
        }
        if (userId != null) {
            when(cursor.isNull(6)).thenReturn(false);
            when(cursor.isNull(7)).thenReturn(false);
            when(cursor.getString(6)).thenReturn(userId);
            when(cursor.getString(7)).thenReturn(userName);
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
