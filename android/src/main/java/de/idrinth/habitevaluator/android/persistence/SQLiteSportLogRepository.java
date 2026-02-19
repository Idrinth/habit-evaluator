package de.idrinth.habitevaluator.android.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.SportLog;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.SportLogRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteSportLogRepository implements SportLogRepository {

    private final SQLiteHelper dbHelper;
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public SQLiteSportLogRepository(SQLiteHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public SportLog save(SportLog entry) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", entry.getId());
        values.put("name", entry.getName());
        values.put("measurement", entry.getMeasurement());
        values.put("measurement_unit", entry.getMeasurementUnit());
        values.put("start_time", entry.getStartTime() != null ? entry.getStartTime().format(TIME_FORMAT) : null);
        values.put("end_time", entry.getEndTime() != null ? entry.getEndTime().format(TIME_FORMAT) : null);
        values.put("date", entry.getDate() != null ? entry.getDate().format(DATE_FORMAT) : LocalDate.now().format(DATE_FORMAT));
        values.put("created_at", entry.getCreatedAt() != null ? entry.getCreatedAt().format(DT_FORMAT) : LocalDateTime.now().format(DT_FORMAT));
        values.put("notes", entry.getNotes());
        if (entry.getUser() != null) {
            values.put("user_id", entry.getUser().getId());
            values.put("user_name", entry.getUser().getUsername());
        }
        db.insertWithOnConflict("sport_logs", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return entry;
    }

    @Override
    public Optional<SportLog> findById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM sport_logs WHERE id = ?", new String[]{id})) {
            if (cursor.moveToFirst()) {
                return Optional.of(readFromCursor(cursor));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<SportLog> findAll() {
        List<SportLog> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM sport_logs", null)) {
            while (cursor.moveToNext()) {
                result.add(readFromCursor(cursor));
            }
        }
        return result;
    }

    @Override
    public void deleteById(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("sport_logs", "id = ?", new String[]{id});
    }

    @Override
    public boolean existsById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT 1 FROM sport_logs WHERE id = ?", new String[]{id})) {
            return cursor.moveToFirst();
        }
    }

    @Override
    public List<SportLog> findByUserId(String userId) {
        List<SportLog> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM sport_logs WHERE user_id = ? ORDER BY date DESC, start_time DESC", new String[]{userId})) {
            while (cursor.moveToNext()) {
                result.add(readFromCursor(cursor));
            }
        }
        return result;
    }

    @Override
    public List<String> findDistinctNamesByUserId(String userId) {
        List<String> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT DISTINCT name FROM sport_logs WHERE user_id = ? AND name IS NOT NULL AND name != '' ORDER BY name",
                new String[]{userId})) {
            while (cursor.moveToNext()) {
                result.add(cursor.getString(0));
            }
        }
        return result;
    }

    @Override
    public List<String> findDistinctMeasurementUnitsByUserId(String userId) {
        List<String> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT DISTINCT measurement_unit FROM sport_logs WHERE user_id = ? AND measurement_unit IS NOT NULL AND measurement_unit != '' ORDER BY measurement_unit",
                new String[]{userId})) {
            while (cursor.moveToNext()) {
                result.add(cursor.getString(0));
            }
        }
        return result;
    }

    private SportLog readFromCursor(Cursor cursor) {
        SportLog entry = new SportLog();
        entry.setId(getString(cursor, "id"));
        entry.setName(getString(cursor, "name"));

        int measIdx = cursor.getColumnIndexOrThrow("measurement");
        if (!cursor.isNull(measIdx)) {
            entry.setMeasurement(cursor.getDouble(measIdx));
        }

        entry.setMeasurementUnit(getString(cursor, "measurement_unit"));

        String startTime = getString(cursor, "start_time");
        if (startTime != null) {
            entry.setStartTime(LocalTime.parse(startTime, TIME_FORMAT));
        }

        String endTime = getString(cursor, "end_time");
        if (endTime != null) {
            entry.setEndTime(LocalTime.parse(endTime, TIME_FORMAT));
        }

        String date = getString(cursor, "date");
        if (date != null) {
            entry.setDate(LocalDate.parse(date, DATE_FORMAT));
        }

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
