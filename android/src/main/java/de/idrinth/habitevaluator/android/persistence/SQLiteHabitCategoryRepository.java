package de.idrinth.habitevaluator.android.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SQLiteHabitCategoryRepository implements HabitCategoryRepository {

    private final SQLiteHelper dbHelper;

    public SQLiteHabitCategoryRepository(SQLiteHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public HabitCategory save(HabitCategory category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("id", category.getId());
            values.put("name", category.getName());
            values.put("description", category.getDescription());
            values.put("color", category.getColor());
            if (category.getUser() != null) {
                values.put("user_id", category.getUser().getId());
                values.put("user_name", category.getUser().getUsername());
            }
            db.insertWithOnConflict("habit_categories", null, values, SQLiteDatabase.CONFLICT_REPLACE);

            db.delete("category_name_translations", "category_id = ?", new String[]{category.getId()});
            if (category.getNameTranslations() != null) {
                for (Map.Entry<String, String> e : category.getNameTranslations().entrySet()) {
                    ContentValues tv = new ContentValues();
                    tv.put("category_id", category.getId());
                    tv.put("language", e.getKey());
                    tv.put("translated_name", e.getValue());
                    db.insert("category_name_translations", null, tv);
                }
            }
            db.delete("category_description_translations", "category_id = ?", new String[]{category.getId()});
            if (category.getDescriptionTranslations() != null) {
                for (Map.Entry<String, String> e : category.getDescriptionTranslations().entrySet()) {
                    ContentValues tv = new ContentValues();
                    tv.put("category_id", category.getId());
                    tv.put("language", e.getKey());
                    tv.put("translated_description", e.getValue());
                    db.insert("category_description_translations", null, tv);
                }
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return category;
    }

    @Override
    public Optional<HabitCategory> findById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM habit_categories WHERE id = ?", new String[]{id})) {
            if (cursor.moveToFirst()) {
                return Optional.of(readCategoryFromCursor(cursor, db));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<HabitCategory> findAll() {
        List<HabitCategory> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM habit_categories", null)) {
            while (cursor.moveToNext()) {
                result.add(readCategoryFromCursor(cursor, db));
            }
        }
        return result;
    }

    @Override
    public void deleteById(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("category_name_translations", "category_id = ?", new String[]{id});
            db.delete("category_description_translations", "category_id = ?", new String[]{id});
            db.delete("habit_categories", "id = ?", new String[]{id});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public boolean existsById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT 1 FROM habit_categories WHERE id = ?", new String[]{id})) {
            return cursor.moveToFirst();
        }
    }

    @Override
    public List<HabitCategory> findByUserId(String userId) {
        List<HabitCategory> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM habit_categories WHERE user_id = ?", new String[]{userId})) {
            while (cursor.moveToNext()) {
                result.add(readCategoryFromCursor(cursor, db));
            }
        }
        return result;
    }

    private HabitCategory readCategoryFromCursor(Cursor cursor, SQLiteDatabase db) {
        HabitCategory category = new HabitCategory();
        category.setId(getString(cursor, "id"));
        category.setName(getString(cursor, "name"));
        category.setDescription(getString(cursor, "description"));
        category.setColor(getString(cursor, "color"));

        String userId = getString(cursor, "user_id");
        if (userId != null) {
            User user = new User();
            user.setId(userId);
            user.setUsername(getString(cursor, "user_name"));
            user.setPassword("placeholder");
            category.setUser(user);
        }

        category.setNameTranslations(loadTranslations(db, "category_name_translations", "category_id", category.getId(), "translated_name"));
        category.setDescriptionTranslations(loadTranslations(db, "category_description_translations", "category_id", category.getId(), "translated_description"));

        return category;
    }

    private Map<String, String> loadTranslations(SQLiteDatabase db, String table, String fkColumn, String fkValue, String valueColumn) {
        Map<String, String> translations = new HashMap<>();
        try (Cursor cursor = db.rawQuery("SELECT language, " + valueColumn + " FROM " + table + " WHERE " + fkColumn + " = ?", new String[]{fkValue})) {
            while (cursor.moveToNext()) {
                String lang = cursor.getString(cursor.getColumnIndexOrThrow("language"));
                String val = cursor.getString(cursor.getColumnIndexOrThrow(valueColumn));
                if (lang != null && val != null) {
                    translations.put(lang, val);
                }
            }
        }
        return translations;
    }

    private String getString(Cursor cursor, String column) {
        int idx = cursor.getColumnIndexOrThrow(column);
        return cursor.isNull(idx) ? null : cursor.getString(idx);
    }
}
