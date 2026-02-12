package de.idrinth.habitevaluator.android.persistence;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.Medication;
import de.idrinth.habitevaluator.shared.model.MedicationProvisionType;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

public class SQLiteMedicationRepositoryTest {

    private SQLiteHelper dbHelper;
    private SQLiteDatabase db;
    private SQLiteMedicationRepository repository;

    @Before
    public void setUp() {
        dbHelper = mock(SQLiteHelper.class);
        db = mock(SQLiteDatabase.class);
        when(dbHelper.getReadableDatabase()).thenReturn(db);
        when(dbHelper.getWritableDatabase()).thenReturn(db);
        repository = new SQLiteMedicationRepository(dbHelper);
    }

    @Test
    public void testSaveCallsInsert() {
        Medication medication = new Medication("Ibuprofen", MedicationProvisionType.PILL);

        repository.save(medication);

        verify(db).insertWithOnConflict(eq("medications"), isNull(), any(), eq(5));
    }

    @Test
    public void testFindByIdNotFound() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(db.rawQuery(eq("SELECT * FROM medications WHERE id = ?"), eq(new String[]{"nonexistent"}))).thenReturn(cursor);

        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    public void testFindByIdFound() {
        Cursor cursor = createMedicationCursor("m1", "Ibuprofen", "https://en.wikipedia.org/wiki/Ibuprofen", "PILL", null, null);
        when(db.rawQuery(eq("SELECT * FROM medications WHERE id = ?"), eq(new String[]{"m1"}))).thenReturn(cursor);

        Optional<Medication> result = repository.findById("m1");

        assertTrue(result.isPresent());
        assertEquals("Ibuprofen", result.get().getName());
        assertEquals("https://en.wikipedia.org/wiki/Ibuprofen", result.get().getWikipediaLink());
        assertEquals(MedicationProvisionType.PILL, result.get().getProvisionType());
    }

    @Test
    public void testFindByIdWithNullProvisionType() {
        Cursor cursor = createMedicationCursor("m1", "Unknown Med", null, null, null, null);
        when(db.rawQuery(eq("SELECT * FROM medications WHERE id = ?"), eq(new String[]{"m1"}))).thenReturn(cursor);

        Optional<Medication> result = repository.findById("m1");

        assertTrue(result.isPresent());
        assertNull(result.get().getProvisionType());
    }

    @Test
    public void testFindByIdWithUser() {
        Cursor cursor = createMedicationCursor("m1", "Ibuprofen", null, "PILL", "u1", "testuser");
        when(db.rawQuery(eq("SELECT * FROM medications WHERE id = ?"), eq(new String[]{"m1"}))).thenReturn(cursor);

        Optional<Medication> result = repository.findById("m1");

        assertTrue(result.isPresent());
        assertNotNull(result.get().getUser());
        assertEquals("u1", result.get().getUser().getId());
    }

    @Test
    public void testFindAllEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(eq("SELECT * FROM medications"), isNull())).thenReturn(cursor);

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    public void testDeleteById() {
        repository.deleteById("m1");

        verify(db).delete(eq("medications"), eq("id = ?"), eq(new String[]{"m1"}));
    }

    @Test
    public void testFindByUserIdEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(startsWith("SELECT * FROM medications WHERE user_id"), any())).thenReturn(cursor);

        assertTrue(repository.findByUserId("u1").isEmpty());
    }

    @Test
    public void testFindByIdWithLiquidDrops() {
        Cursor cursor = createMedicationCursor("m1", "Eye Drops", null, "LIQUID_DROPS", null, null);
        when(db.rawQuery(eq("SELECT * FROM medications WHERE id = ?"), eq(new String[]{"m1"}))).thenReturn(cursor);

        Optional<Medication> result = repository.findById("m1");

        assertTrue(result.isPresent());
        assertEquals(MedicationProvisionType.LIQUID_DROPS, result.get().getProvisionType());
    }

    @Test
    public void testSaveReturnsMedication() {
        Medication medication = new Medication("Test", MedicationProvisionType.PILL);
        Medication result = repository.save(medication);
        assertSame(medication, result);
    }

    private Cursor createMedicationCursor(String id, String name, String wikipediaLink, String provisionType, String userId, String userName) {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.moveToNext()).thenReturn(false);

        when(cursor.getColumnIndexOrThrow("id")).thenReturn(0);
        when(cursor.getColumnIndexOrThrow("name")).thenReturn(1);
        when(cursor.getColumnIndexOrThrow("wikipedia_link")).thenReturn(2);
        when(cursor.getColumnIndexOrThrow("provision_type")).thenReturn(3);
        when(cursor.getColumnIndexOrThrow("user_id")).thenReturn(4);
        when(cursor.getColumnIndexOrThrow("user_name")).thenReturn(5);

        when(cursor.isNull(anyInt())).thenReturn(true);
        when(cursor.isNull(0)).thenReturn(false);
        when(cursor.isNull(1)).thenReturn(false);

        when(cursor.getString(0)).thenReturn(id);
        when(cursor.getString(1)).thenReturn(name);

        if (wikipediaLink != null) {
            when(cursor.isNull(2)).thenReturn(false);
            when(cursor.getString(2)).thenReturn(wikipediaLink);
        }
        if (provisionType != null) {
            when(cursor.isNull(3)).thenReturn(false);
            when(cursor.getString(3)).thenReturn(provisionType);
        }
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
