package de.idrinth.habitevaluator.android.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.EmergencyPlanAction;
import de.idrinth.habitevaluator.shared.model.EmergencyPlanStep;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.EmergencyPlanActionRepository;
import de.idrinth.habitevaluator.shared.repository.EmergencyPlanStepRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteEmergencyPlanStepRepository implements EmergencyPlanStepRepository {

    private final SQLiteHelper dbHelper;
    private final EmergencyPlanActionRepository actionRepository;

    public SQLiteEmergencyPlanStepRepository(SQLiteHelper dbHelper, EmergencyPlanActionRepository actionRepository) {
        this.dbHelper = dbHelper;
        this.actionRepository = actionRepository;
    }

    @Override
    public EmergencyPlanStep save(EmergencyPlanStep step) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", step.getId());
        values.put("question", step.getQuestion());
        values.put("step_order", step.getStepOrder());
        if (step.getUser() != null) {
            values.put("user_id", step.getUser().getId());
            values.put("user_name", step.getUser().getUsername());
        }
        db.insertWithOnConflict("emergency_plan_steps", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        // Save actions: delete existing then re-save
        actionRepository.deleteByStepId(step.getId());
        for (EmergencyPlanAction action : step.getActions()) {
            action.setStep(step);
            actionRepository.save(action);
        }

        return step;
    }

    @Override
    public void saveAll(List<EmergencyPlanStep> steps) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (EmergencyPlanStep step : steps) {
                ContentValues values = new ContentValues();
                values.put("id", step.getId());
                values.put("question", step.getQuestion());
                values.put("step_order", step.getStepOrder());
                if (step.getUser() != null) {
                    values.put("user_id", step.getUser().getId());
                    values.put("user_name", step.getUser().getUsername());
                }
                db.insertWithOnConflict("emergency_plan_steps", null, values, SQLiteDatabase.CONFLICT_REPLACE);

                actionRepository.deleteByStepId(step.getId());
                for (EmergencyPlanAction action : step.getActions()) {
                    action.setStep(step);
                    actionRepository.save(action);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public Optional<EmergencyPlanStep> findById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM emergency_plan_steps WHERE id = ?", new String[]{id})) {
            if (cursor.moveToFirst()) {
                EmergencyPlanStep step = readFromCursor(cursor);
                loadActions(step);
                return Optional.of(step);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<EmergencyPlanStep> findAll() {
        List<EmergencyPlanStep> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM emergency_plan_steps ORDER BY step_order", null)) {
            while (cursor.moveToNext()) {
                EmergencyPlanStep step = readFromCursor(cursor);
                loadActions(step);
                result.add(step);
            }
        }
        return result;
    }

    @Override
    public void deleteById(String id) {
        actionRepository.deleteByStepId(id);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("emergency_plan_steps", "id = ?", new String[]{id});
    }

    @Override
    public List<EmergencyPlanStep> findByUserId(String userId) {
        List<EmergencyPlanStep> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM emergency_plan_steps WHERE user_id = ? ORDER BY step_order", new String[]{userId})) {
            while (cursor.moveToNext()) {
                EmergencyPlanStep step = readFromCursor(cursor);
                loadActions(step);
                result.add(step);
            }
        }
        return result;
    }

    private void loadActions(EmergencyPlanStep step) {
        List<EmergencyPlanAction> actions = actionRepository.findByStepId(step.getId());
        for (EmergencyPlanAction action : actions) {
            action.setStep(step);
        }
        step.setActions(actions);
    }

    private EmergencyPlanStep readFromCursor(Cursor cursor) {
        EmergencyPlanStep step = new EmergencyPlanStep();
        step.setId(getString(cursor, "id"));
        step.setQuestion(getString(cursor, "question"));

        int orderIdx = cursor.getColumnIndexOrThrow("step_order");
        step.setStepOrder(cursor.getInt(orderIdx));

        String userId = getString(cursor, "user_id");
        if (userId != null) {
            User user = new User();
            user.setId(userId);
            user.setUsername(getString(cursor, "user_name"));
            user.setPassword("placeholder");
            step.setUser(user);
        }

        return step;
    }

    private String getString(Cursor cursor, String column) {
        int idx = cursor.getColumnIndexOrThrow(column);
        return cursor.isNull(idx) ? null : cursor.getString(idx);
    }
}
