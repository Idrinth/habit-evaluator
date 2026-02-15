package de.idrinth.habitevaluator.android.persistence;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.EmergencyPlanAction;
import de.idrinth.habitevaluator.shared.model.EmergencyPlanStep;
import de.idrinth.habitevaluator.shared.repository.EmergencyPlanActionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class SQLiteEmergencyPlanStepRepositoryTest {

    private SQLiteHelper dbHelper;
    private SQLiteDatabase db;
    private EmergencyPlanActionRepository actionRepository;
    private SQLiteEmergencyPlanStepRepository repository;

    @BeforeEach
    void setUp() {
        dbHelper = mock(SQLiteHelper.class);
        db = mock(SQLiteDatabase.class);
        actionRepository = mock(EmergencyPlanActionRepository.class);
        when(dbHelper.getReadableDatabase()).thenReturn(db);
        when(dbHelper.getWritableDatabase()).thenReturn(db);
        repository = new SQLiteEmergencyPlanStepRepository(dbHelper, actionRepository);
    }

    @Test
    void testSaveCallsInsert() {
        EmergencyPlanStep step = new EmergencyPlanStep("Question?", 0);

        repository.save(step);

        verify(db).insertWithOnConflict(eq("emergency_plan_steps"), isNull(), any(), eq(5));
    }

    @Test
    void testSaveReturnsStep() {
        EmergencyPlanStep step = new EmergencyPlanStep("Question?", 0);
        EmergencyPlanStep result = repository.save(step);
        assertSame(step, result);
    }

    @Test
    void testSavePersistsActions() {
        EmergencyPlanStep step = new EmergencyPlanStep("Question?", 0);
        EmergencyPlanAction action = new EmergencyPlanAction("Do something", 0);
        step.addAction(action);

        repository.save(step);

        verify(actionRepository).deleteByStepId(step.getId());
        verify(actionRepository).save(action);
    }

    @Test
    void testSaveAllUsesTransaction() {
        EmergencyPlanStep step1 = new EmergencyPlanStep("Q1?", 0);
        EmergencyPlanStep step2 = new EmergencyPlanStep("Q2?", 1);

        repository.saveAll(Arrays.asList(step1, step2));

        verify(db).beginTransaction();
        verify(db, times(2)).insertWithOnConflict(eq("emergency_plan_steps"), isNull(), any(), eq(5));
        verify(db).setTransactionSuccessful();
        verify(db).endTransaction();
    }

    @Test
    void testSaveAllEndsTransactionOnFailure() {
        EmergencyPlanStep step = new EmergencyPlanStep("Q?", 0);
        doThrow(new RuntimeException("DB error")).when(db)
                .insertWithOnConflict(eq("emergency_plan_steps"), isNull(), any(), eq(5));

        assertThrows(RuntimeException.class, () -> repository.saveAll(Arrays.asList(step)));

        verify(db).beginTransaction();
        verify(db, never()).setTransactionSuccessful();
        verify(db).endTransaction();
    }

    @Test
    void testFindByIdNotFound() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(db.rawQuery(eq("SELECT * FROM emergency_plan_steps WHERE id = ?"), eq(new String[]{"nonexistent"}))).thenReturn(cursor);

        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    void testFindByIdFound() {
        Cursor cursor = createStepCursor("s1", "Are you tired?", 0, null, null);
        when(db.rawQuery(eq("SELECT * FROM emergency_plan_steps WHERE id = ?"), eq(new String[]{"s1"}))).thenReturn(cursor);

        List<EmergencyPlanAction> actions = new ArrayList<>();
        actions.add(new EmergencyPlanAction("Take a nap", 0));
        when(actionRepository.findByStepId("s1")).thenReturn(actions);

        Optional<EmergencyPlanStep> result = repository.findById("s1");

        assertTrue(result.isPresent());
        assertEquals("Are you tired?", result.get().getQuestion());
        assertEquals(0, result.get().getStepOrder());
        assertEquals(1, result.get().getActions().size());
        assertEquals("Take a nap", result.get().getActions().get(0).getActionText());
    }

    @Test
    void testFindByIdWithUser() {
        Cursor cursor = createStepCursor("s1", "Q?", 0, "u1", "testuser");
        when(db.rawQuery(eq("SELECT * FROM emergency_plan_steps WHERE id = ?"), eq(new String[]{"s1"}))).thenReturn(cursor);
        when(actionRepository.findByStepId("s1")).thenReturn(Collections.emptyList());

        Optional<EmergencyPlanStep> result = repository.findById("s1");

        assertTrue(result.isPresent());
        assertNotNull(result.get().getUser());
        assertEquals("u1", result.get().getUser().getId());
    }

    @Test
    void testFindAllEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(eq("SELECT * FROM emergency_plan_steps ORDER BY step_order"), isNull())).thenReturn(cursor);

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void testDeleteById() {
        repository.deleteById("s1");

        verify(actionRepository).deleteByStepId("s1");
        verify(db).delete(eq("emergency_plan_steps"), eq("id = ?"), eq(new String[]{"s1"}));
    }

    @Test
    void testFindByUserIdEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(startsWith("SELECT * FROM emergency_plan_steps WHERE user_id"), any())).thenReturn(cursor);

        assertTrue(repository.findByUserId("u1").isEmpty());
    }

    private Cursor createStepCursor(String id, String question, int stepOrder, String userId, String userName) {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.moveToNext()).thenReturn(false);

        when(cursor.getColumnIndexOrThrow("id")).thenReturn(0);
        when(cursor.getColumnIndexOrThrow("question")).thenReturn(1);
        when(cursor.getColumnIndexOrThrow("step_order")).thenReturn(2);
        when(cursor.getColumnIndexOrThrow("user_id")).thenReturn(3);
        when(cursor.getColumnIndexOrThrow("user_name")).thenReturn(4);

        when(cursor.isNull(anyInt())).thenReturn(true);
        when(cursor.isNull(0)).thenReturn(false);
        when(cursor.isNull(1)).thenReturn(false);
        when(cursor.isNull(2)).thenReturn(false);

        when(cursor.getString(0)).thenReturn(id);
        when(cursor.getString(1)).thenReturn(question);
        when(cursor.getInt(2)).thenReturn(stepOrder);

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
