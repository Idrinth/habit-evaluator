package de.idrinth.habitevaluator.android.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.EmergencyPlanStep;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.EmergencyPlanStepRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteEmergencyPlanStepRepository implements EmergencyPlanStepRepository {

    private final SQLiteHelper dbHelper;

    public SQLiteEmergencyPlanStepRepository(SQLiteHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public EmergencyPlanStep save(EmergencyPlanStep step) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", step.getId());
        values.put("question", step.getQuestion());
        values.put("action", step.getAction());
        values.put("phone_number", step.getPhoneNumber());
        values.put("step_order", step.getStepOrder());
        if (step.getUser() != null) {
            values.put("user_id", step.getUser().getId());
            values.put("user_name", step.getUser().getUsername());
        }
        db.insertWithOnConflict("emergency_plan_steps", null, values, SQLiteDatabase.CONFLICT_REPLACE);
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
                values.put("action", step.getAction());
                values.put("phone_number", step.getPhoneNumber());
                values.put("step_order", step.getStepOrder());
                if (step.getUser() != null) {
                    values.put("user_id", step.getUser().getId());
                    values.put("user_name", step.getUser().getUsername());
                }
                db.insertWithOnConflict("emergency_plan_steps", null, values, SQLiteDatabase.CONFLICT_REPLACE);
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
                return Optional.of(readFromCursor(cursor));
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
                result.add(readFromCursor(cursor));
            }
        }
        return result;
    }

    @Override
    public void deleteById(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("emergency_plan_steps", "id = ?", new String[]{id});
    }

    @Override
    public List<EmergencyPlanStep> findByUserId(String userId) {
        List<EmergencyPlanStep> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM emergency_plan_steps WHERE user_id = ? ORDER BY step_order", new String[]{userId})) {
            while (cursor.moveToNext()) {
                result.add(readFromCursor(cursor));
            }
        }
        return result;
    }

    private EmergencyPlanStep readFromCursor(Cursor cursor) {
        EmergencyPlanStep step = new EmergencyPlanStep();
        step.setId(getString(cursor, "id"));
        step.setQuestion(getString(cursor, "question"));
        step.setAction(getString(cursor, "action"));
        step.setPhoneNumber(getString(cursor, "phone_number"));

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
