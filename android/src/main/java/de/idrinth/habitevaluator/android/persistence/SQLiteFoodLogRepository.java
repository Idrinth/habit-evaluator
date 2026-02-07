package de.idrinth.habitevaluator.android.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.FoodLog;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.FoodLogRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteFoodLogRepository implements FoodLogRepository {

    private final SQLiteHelper dbHelper;
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public SQLiteFoodLogRepository(SQLiteHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public FoodLog save(FoodLog entry) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", entry.getId());
        values.put("carbohydrates", entry.getCarbohydrates());
        values.put("kcal", entry.getKcal());
        values.put("date_time", entry.getDateTime() != null ? entry.getDateTime().format(DT_FORMAT) : LocalDateTime.now().format(DT_FORMAT));
        values.put("food_items", entry.getFoodItems());
        values.put("created_at", entry.getCreatedAt() != null ? entry.getCreatedAt().format(DT_FORMAT) : LocalDateTime.now().format(DT_FORMAT));
        values.put("notes", entry.getNotes());
        if (entry.getUser() != null) {
            values.put("user_id", entry.getUser().getId());
            values.put("user_name", entry.getUser().getUsername());
        }
        db.insertWithOnConflict("food_logs", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return entry;
    }

    @Override
    public Optional<FoodLog> findById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM food_logs WHERE id = ?", new String[]{id})) {
            if (cursor.moveToFirst()) {
                return Optional.of(readFromCursor(cursor));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<FoodLog> findAll() {
        List<FoodLog> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM food_logs", null)) {
            while (cursor.moveToNext()) {
                result.add(readFromCursor(cursor));
            }
        }
        return result;
    }

    @Override
    public void deleteById(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("food_logs", "id = ?", new String[]{id});
    }

    @Override
    public boolean existsById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT 1 FROM food_logs WHERE id = ?", new String[]{id})) {
            return cursor.moveToFirst();
        }
    }

    @Override
    public List<FoodLog> findByUserId(String userId) {
        List<FoodLog> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM food_logs WHERE user_id = ?", new String[]{userId})) {
            while (cursor.moveToNext()) {
                result.add(readFromCursor(cursor));
            }
        }
        return result;
    }

    private FoodLog readFromCursor(Cursor cursor) {
        FoodLog entry = new FoodLog();
        entry.setId(getString(cursor, "id"));

        int carbIdx = cursor.getColumnIndexOrThrow("carbohydrates");
        if (!cursor.isNull(carbIdx)) {
            entry.setCarbohydrates(cursor.getDouble(carbIdx));
        }

        int kcalIdx = cursor.getColumnIndexOrThrow("kcal");
        if (!cursor.isNull(kcalIdx)) {
            entry.setKcal(cursor.getInt(kcalIdx));
        }

        String dateTime = getString(cursor, "date_time");
        if (dateTime != null) {
            entry.setDateTime(LocalDateTime.parse(dateTime, DT_FORMAT));
        }

        entry.setFoodItems(getString(cursor, "food_items"));

        String createdAt = getString(cursor, "created_at");
        if (createdAt != null) {
            entry.setCreatedAt(LocalDateTime.parse(createdAt, DT_FORMAT));
        }

        entry.setNotes(getString(cursor, "notes"));

        String userId = getString(cursor, "user_id");
        if (userId != null) {
            User user = new User();
            user.setId(userId);
            user.setUsername(getString(cursor, "user_name"));
            user.setPassword("placeholder");
            entry.setUser(user);
        }

        return entry;
    }

    private String getString(Cursor cursor, String column) {
        int idx = cursor.getColumnIndexOrThrow(column);
        return cursor.isNull(idx) ? null : cursor.getString(idx);
    }
}
