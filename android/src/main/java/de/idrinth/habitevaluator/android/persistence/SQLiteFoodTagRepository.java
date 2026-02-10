package de.idrinth.habitevaluator.android.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.FoodTag;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.FoodTagRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteFoodTagRepository implements FoodTagRepository {

    private final SQLiteHelper dbHelper;

    public SQLiteFoodTagRepository(SQLiteHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public FoodTag save(FoodTag tag) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", tag.getId());
        values.put("name", tag.getName());
        values.put("name_lower", tag.getNameLower());
        if (tag.getUser() != null) {
            values.put("user_id", tag.getUser().getId());
            values.put("user_name", tag.getUser().getUsername());
        }
        db.insertWithOnConflict("food_tags", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return tag;
    }

    @Override
    public Optional<FoodTag> findById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM food_tags WHERE id = ?", new String[]{id})) {
            if (cursor.moveToFirst()) {
                return Optional.of(readFromCursor(cursor));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<FoodTag> findByUserId(String userId) {
        List<FoodTag> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM food_tags WHERE user_id = ? ORDER BY name COLLATE NOCASE", new String[]{userId})) {
            while (cursor.moveToNext()) {
                result.add(readFromCursor(cursor));
            }
        }
        return result;
    }

    @Override
    public Optional<FoodTag> findByNameLowerAndUserId(String nameLower, String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT * FROM food_tags WHERE name_lower = ? AND user_id = ?",
                new String[]{nameLower, userId})) {
            if (cursor.moveToFirst()) {
                return Optional.of(readFromCursor(cursor));
            }
        }
        return Optional.empty();
    }

    @Override
    public void deleteById(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("food_tags", "id = ?", new String[]{id});
    }

    public void linkTagToFoodLog(String foodLogId, String foodTagId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("food_log_id", foodLogId);
        values.put("food_tag_id", foodTagId);
        db.insertWithOnConflict("food_log_tags", null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void unlinkAllTagsFromFoodLog(String foodLogId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("food_log_tags", "food_log_id = ?", new String[]{foodLogId});
    }

    public List<FoodTag> findTagsByFoodLogId(String foodLogId) {
        List<FoodTag> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT t.* FROM food_tags t "
                        + "INNER JOIN food_log_tags flt ON t.id = flt.food_tag_id "
                        + "WHERE flt.food_log_id = ?",
                new String[]{foodLogId})) {
            while (cursor.moveToNext()) {
                result.add(readFromCursor(cursor));
            }
        }
        return result;
    }

    private FoodTag readFromCursor(Cursor cursor) {
        FoodTag tag = new FoodTag();
        tag.setId(getString(cursor, "id"));
        tag.setName(getString(cursor, "name"));
        tag.setNameLower(getString(cursor, "name_lower"));

        String userId = getString(cursor, "user_id");
        if (userId != null) {
            User user = new User();
            user.setId(userId);
            user.setUsername(getString(cursor, "user_name"));
            user.setPassword("placeholder");
            tag.setUser(user);
        }

        return tag;
    }

    private String getString(Cursor cursor, String column) {
        int idx = cursor.getColumnIndexOrThrow(column);
        return cursor.isNull(idx) ? null : cursor.getString(idx);
    }
}
