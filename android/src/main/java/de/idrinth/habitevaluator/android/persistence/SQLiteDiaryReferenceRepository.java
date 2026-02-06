package de.idrinth.habitevaluator.android.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.DiaryReference;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.DiaryReferenceRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteDiaryReferenceRepository implements DiaryReferenceRepository {

    private final SQLiteHelper dbHelper;

    public SQLiteDiaryReferenceRepository(SQLiteHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public DiaryReference save(DiaryReference reference) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", reference.getId());
        values.put("description", reference.getDescription());
        values.put("description_lower", reference.getDescriptionLower());
        if (reference.getUser() != null) {
            values.put("user_id", reference.getUser().getId());
            values.put("user_name", reference.getUser().getUsername());
        }
        db.insertWithOnConflict("diary_references", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return reference;
    }

    @Override
    public Optional<DiaryReference> findById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM diary_references WHERE id = ?", new String[]{id})) {
            if (cursor.moveToFirst()) {
                return Optional.of(readFromCursor(cursor));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<DiaryReference> findAll() {
        List<DiaryReference> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM diary_references", null)) {
            while (cursor.moveToNext()) {
                result.add(readFromCursor(cursor));
            }
        }
        return result;
    }

    @Override
    public void deleteById(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("diary_references", "id = ?", new String[]{id});
    }

    @Override
    public List<DiaryReference> findByUserId(String userId) {
        List<DiaryReference> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM diary_references WHERE user_id = ?", new String[]{userId})) {
            while (cursor.moveToNext()) {
                result.add(readFromCursor(cursor));
            }
        }
        return result;
    }

    @Override
    public Optional<DiaryReference> findByUserIdAndDescriptionIgnoreCase(String userId, String description) {
        if (description == null) {
            return Optional.empty();
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT * FROM diary_references WHERE user_id = ? AND description_lower = ?",
                new String[]{userId, description.toLowerCase()})) {
            if (cursor.moveToFirst()) {
                return Optional.of(readFromCursor(cursor));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<String> findDistinctDescriptionsByUserId(String userId) {
        List<String> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT DISTINCT description FROM diary_references WHERE user_id = ? AND description IS NOT NULL AND description != '' ORDER BY description",
                new String[]{userId})) {
            while (cursor.moveToNext()) {
                result.add(cursor.getString(0));
            }
        }
        return result;
    }

    private DiaryReference readFromCursor(Cursor cursor) {
        DiaryReference ref = new DiaryReference();
        ref.setId(getString(cursor, "id"));
        ref.setDescription(getString(cursor, "description"));
        ref.setDescriptionLower(getString(cursor, "description_lower"));

        String userId = getString(cursor, "user_id");
        if (userId != null) {
            User user = new User();
            user.setId(userId);
            user.setUsername(getString(cursor, "user_name"));
            user.setPassword("placeholder");
            ref.setUser(user);
        }

        return ref;
    }

    private String getString(Cursor cursor, String column) {
        int idx = cursor.getColumnIndexOrThrow(column);
        return cursor.isNull(idx) ? null : cursor.getString(idx);
    }
}
