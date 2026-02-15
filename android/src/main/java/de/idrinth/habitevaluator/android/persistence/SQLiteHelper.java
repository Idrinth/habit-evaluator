package de.idrinth.habitevaluator.android.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "habit_evaluator.db";
    private static final int DATABASE_VERSION = 9;
    private static SQLiteHelper instance;

    private SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized SQLiteHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SQLiteHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE habits ("
                + "id TEXT PRIMARY KEY,"
                + "name TEXT NOT NULL,"
                + "description TEXT,"
                + "category_id TEXT,"
                + "frequency_type TEXT NOT NULL,"
                + "target_frequency INTEGER NOT NULL,"
                + "max_entries_per_day INTEGER NOT NULL,"
                + "positive_scoring INTEGER NOT NULL DEFAULT 1,"
                + "created_at TEXT NOT NULL,"
                + "scoring_rule_id TEXT,"
                + "scoring_rule_name TEXT,"
                + "user_id TEXT,"
                + "user_name TEXT"
                + ")");

        db.execSQL("CREATE TABLE habit_entries ("
                + "id TEXT PRIMARY KEY,"
                + "habit_id TEXT NOT NULL,"
                + "completed_at TEXT NOT NULL,"
                + "notes TEXT,"
                + "value INTEGER NOT NULL DEFAULT 1,"
                + "FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE"
                + ")");

        db.execSQL("CREATE TABLE habit_name_translations ("
                + "habit_id TEXT NOT NULL,"
                + "language TEXT NOT NULL,"
                + "translated_name TEXT NOT NULL,"
                + "PRIMARY KEY (habit_id, language),"
                + "FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE"
                + ")");

        db.execSQL("CREATE TABLE habit_description_translations ("
                + "habit_id TEXT NOT NULL,"
                + "language TEXT NOT NULL,"
                + "translated_description TEXT NOT NULL,"
                + "PRIMARY KEY (habit_id, language),"
                + "FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE"
                + ")");

        db.execSQL("CREATE TABLE habit_categories ("
                + "id TEXT PRIMARY KEY,"
                + "name TEXT,"
                + "description TEXT,"
                + "color TEXT,"
                + "user_id TEXT,"
                + "user_name TEXT"
                + ")");

        db.execSQL("CREATE TABLE category_name_translations ("
                + "category_id TEXT NOT NULL,"
                + "language TEXT NOT NULL,"
                + "translated_name TEXT NOT NULL,"
                + "PRIMARY KEY (category_id, language),"
                + "FOREIGN KEY (category_id) REFERENCES habit_categories(id) ON DELETE CASCADE"
                + ")");

        db.execSQL("CREATE TABLE category_description_translations ("
                + "category_id TEXT NOT NULL,"
                + "language TEXT NOT NULL,"
                + "translated_description TEXT NOT NULL,"
                + "PRIMARY KEY (category_id, language),"
                + "FOREIGN KEY (category_id) REFERENCES habit_categories(id) ON DELETE CASCADE"
                + ")");

        db.execSQL("CREATE TABLE diary_references ("
                + "id TEXT PRIMARY KEY,"
                + "description TEXT NOT NULL,"
                + "description_lower TEXT NOT NULL,"
                + "user_id TEXT,"
                + "user_name TEXT"
                + ")");

        db.execSQL("CREATE TABLE diary_entries ("
                + "id TEXT PRIMARY KEY,"
                + "legacy_description TEXT,"
                + "diary_reference_id TEXT,"
                + "significance TEXT NOT NULL,"
                + "event_date TEXT NOT NULL,"
                + "created_at TEXT NOT NULL,"
                + "user_id TEXT,"
                + "user_name TEXT,"
                + "start_time TEXT,"
                + "end_time TEXT,"
                + "FOREIGN KEY (diary_reference_id) REFERENCES diary_references(id)"
                + ")");

        db.execSQL("CREATE TABLE sleep_entries ("
                + "id TEXT PRIMARY KEY,"
                + "from_time TEXT NOT NULL,"
                + "until_time TEXT NOT NULL,"
                + "date TEXT NOT NULL,"
                + "created_at TEXT NOT NULL,"
                + "notes TEXT,"
                + "user_id TEXT,"
                + "user_name TEXT"
                + ")");

        db.execSQL("CREATE TABLE emotion_pairs ("
                + "id TEXT PRIMARY KEY,"
                + "negative_label TEXT NOT NULL,"
                + "positive_label TEXT NOT NULL,"
                + "user_id TEXT,"
                + "user_name TEXT"
                + ")");

        db.execSQL("CREATE TABLE emotion_entries ("
                + "id TEXT PRIMARY KEY,"
                + "emotion_pair_id TEXT NOT NULL,"
                + "strength INTEGER NOT NULL,"
                + "recorded_at TEXT NOT NULL,"
                + "notes TEXT,"
                + "user_id TEXT,"
                + "user_name TEXT,"
                + "FOREIGN KEY (emotion_pair_id) REFERENCES emotion_pairs(id)"
                + ")");

        db.execSQL("CREATE TABLE sport_logs ("
                + "id TEXT PRIMARY KEY,"
                + "name TEXT NOT NULL,"
                + "measurement REAL NOT NULL,"
                + "measurement_unit TEXT NOT NULL,"
                + "start_time TEXT NOT NULL,"
                + "end_time TEXT NOT NULL,"
                + "date TEXT NOT NULL,"
                + "created_at TEXT NOT NULL,"
                + "notes TEXT,"
                + "user_id TEXT,"
                + "user_name TEXT"
                + ")");

        db.execSQL("CREATE INDEX idx_habit_entries_habit_id ON habit_entries(habit_id)");
        db.execSQL("CREATE INDEX idx_habits_user_id ON habits(user_id)");
        db.execSQL("CREATE INDEX idx_habit_categories_user_id ON habit_categories(user_id)");
        db.execSQL("CREATE INDEX idx_diary_entries_user_id ON diary_entries(user_id)");
        db.execSQL("CREATE INDEX idx_diary_references_user_id ON diary_references(user_id)");
        db.execSQL("CREATE INDEX idx_diary_references_description_lower ON diary_references(description_lower)");
        db.execSQL("CREATE INDEX idx_sleep_entries_user_id ON sleep_entries(user_id)");
        db.execSQL("CREATE INDEX idx_emotion_pairs_user_id ON emotion_pairs(user_id)");
        db.execSQL("CREATE INDEX idx_emotion_entries_user_id ON emotion_entries(user_id)");
        db.execSQL("CREATE INDEX idx_emotion_entries_pair_id ON emotion_entries(emotion_pair_id)");
        db.execSQL("CREATE INDEX idx_sport_logs_user_id ON sport_logs(user_id)");

        db.execSQL("CREATE TABLE food_logs ("
                + "id TEXT PRIMARY KEY,"
                + "carbohydrates REAL,"
                + "kcal INTEGER,"
                + "date_time TEXT NOT NULL,"
                + "food_items TEXT NOT NULL,"
                + "created_at TEXT NOT NULL,"
                + "notes TEXT,"
                + "user_id TEXT,"
                + "user_name TEXT"
                + ")");
        db.execSQL("CREATE INDEX idx_food_logs_user_id ON food_logs(user_id)");

        db.execSQL("CREATE TABLE food_tags ("
                + "id TEXT PRIMARY KEY,"
                + "name TEXT NOT NULL,"
                + "name_lower TEXT NOT NULL,"
                + "user_id TEXT,"
                + "user_name TEXT,"
                + "UNIQUE(name_lower, user_id)"
                + ")");
        db.execSQL("CREATE INDEX idx_food_tags_user_id ON food_tags(user_id)");
        db.execSQL("CREATE INDEX idx_food_tags_name_lower ON food_tags(name_lower)");

        db.execSQL("CREATE TABLE food_log_tags ("
                + "food_log_id TEXT NOT NULL,"
                + "food_tag_id TEXT NOT NULL,"
                + "PRIMARY KEY (food_log_id, food_tag_id),"
                + "FOREIGN KEY (food_log_id) REFERENCES food_logs(id) ON DELETE CASCADE,"
                + "FOREIGN KEY (food_tag_id) REFERENCES food_tags(id) ON DELETE CASCADE"
                + ")");

        db.execSQL("CREATE TABLE medications ("
                + "id TEXT PRIMARY KEY,"
                + "name TEXT NOT NULL,"
                + "wikipedia_link TEXT,"
                + "provision_type TEXT NOT NULL,"
                + "user_id TEXT,"
                + "user_name TEXT"
                + ")");
        db.execSQL("CREATE INDEX idx_medications_user_id ON medications(user_id)");

        db.execSQL("CREATE TABLE medication_logs ("
                + "id TEXT PRIMARY KEY,"
                + "medication_id TEXT NOT NULL,"
                + "amount REAL NOT NULL,"
                + "taken_at TEXT NOT NULL,"
                + "created_at TEXT NOT NULL,"
                + "notes TEXT,"
                + "user_id TEXT,"
                + "user_name TEXT,"
                + "FOREIGN KEY (medication_id) REFERENCES medications(id)"
                + ")");
        db.execSQL("CREATE INDEX idx_medication_logs_user_id ON medication_logs(user_id)");
        db.execSQL("CREATE INDEX idx_medication_logs_medication_id ON medication_logs(medication_id)");

        db.execSQL("CREATE TABLE emergency_plan_steps ("
                + "id TEXT PRIMARY KEY,"
                + "question TEXT NOT NULL,"
                + "step_order INTEGER NOT NULL,"
                + "user_id TEXT,"
                + "user_name TEXT"
                + ")");
        db.execSQL("CREATE INDEX idx_emergency_plan_steps_user_id ON emergency_plan_steps(user_id)");

        db.execSQL("CREATE TABLE emergency_plan_actions ("
                + "id TEXT PRIMARY KEY,"
                + "action_text TEXT NOT NULL,"
                + "phone_number TEXT,"
                + "action_order INTEGER NOT NULL,"
                + "step_id TEXT,"
                + "FOREIGN KEY (step_id) REFERENCES emergency_plan_steps(id) ON DELETE CASCADE"
                + ")");
        db.execSQL("CREATE INDEX idx_emergency_plan_actions_step_id ON emergency_plan_actions(step_id)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE diary_entries ADD COLUMN start_time TEXT");
            db.execSQL("ALTER TABLE diary_entries ADD COLUMN end_time TEXT");
        }
        if (oldVersion < 3) {
            db.execSQL("CREATE TABLE IF NOT EXISTS sport_logs ("
                    + "id TEXT PRIMARY KEY,"
                    + "name TEXT NOT NULL,"
                    + "measurement REAL NOT NULL,"
                    + "measurement_unit TEXT NOT NULL,"
                    + "start_time TEXT NOT NULL,"
                    + "end_time TEXT NOT NULL,"
                    + "date TEXT NOT NULL,"
                    + "created_at TEXT NOT NULL,"
                    + "notes TEXT,"
                    + "user_id TEXT,"
                    + "user_name TEXT"
                    + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_sport_logs_user_id ON sport_logs(user_id)");
        }
        if (oldVersion < 4) {
            db.execSQL("CREATE TABLE IF NOT EXISTS food_logs ("
                    + "id TEXT PRIMARY KEY,"
                    + "carbohydrates REAL,"
                    + "kcal INTEGER,"
                    + "date_time TEXT NOT NULL,"
                    + "food_items TEXT NOT NULL,"
                    + "created_at TEXT NOT NULL,"
                    + "notes TEXT,"
                    + "user_id TEXT,"
                    + "user_name TEXT"
                    + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_food_logs_user_id ON food_logs(user_id)");
        }
        if (oldVersion < 5) {
            db.execSQL("CREATE TABLE IF NOT EXISTS food_logs_tmp ("
                    + "id TEXT PRIMARY KEY,"
                    + "carbohydrates REAL,"
                    + "kcal INTEGER,"
                    + "date_time TEXT NOT NULL,"
                    + "food_items TEXT NOT NULL,"
                    + "created_at TEXT NOT NULL,"
                    + "notes TEXT,"
                    + "user_id TEXT,"
                    + "user_name TEXT"
                    + ")");
            db.execSQL("INSERT INTO food_logs_tmp SELECT * FROM food_logs");
            db.execSQL("DROP TABLE food_logs");
            db.execSQL("ALTER TABLE food_logs_tmp RENAME TO food_logs");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_food_logs_user_id ON food_logs(user_id)");
        }
        if (oldVersion < 6) {
            db.execSQL("CREATE TABLE IF NOT EXISTS food_tags ("
                    + "id TEXT PRIMARY KEY,"
                    + "name TEXT NOT NULL,"
                    + "name_lower TEXT NOT NULL,"
                    + "user_id TEXT,"
                    + "user_name TEXT,"
                    + "UNIQUE(name_lower, user_id)"
                    + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_food_tags_user_id ON food_tags(user_id)");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_food_tags_name_lower ON food_tags(name_lower)");
            db.execSQL("CREATE TABLE IF NOT EXISTS food_log_tags ("
                    + "food_log_id TEXT NOT NULL,"
                    + "food_tag_id TEXT NOT NULL,"
                    + "PRIMARY KEY (food_log_id, food_tag_id),"
                    + "FOREIGN KEY (food_log_id) REFERENCES food_logs(id) ON DELETE CASCADE,"
                    + "FOREIGN KEY (food_tag_id) REFERENCES food_tags(id) ON DELETE CASCADE"
                    + ")");
        }
        if (oldVersion < 7) {
            db.execSQL("CREATE TABLE IF NOT EXISTS medications ("
                    + "id TEXT PRIMARY KEY,"
                    + "name TEXT NOT NULL,"
                    + "wikipedia_link TEXT,"
                    + "provision_type TEXT NOT NULL,"
                    + "user_id TEXT,"
                    + "user_name TEXT"
                    + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_medications_user_id ON medications(user_id)");
            db.execSQL("CREATE TABLE IF NOT EXISTS medication_logs ("
                    + "id TEXT PRIMARY KEY,"
                    + "medication_id TEXT NOT NULL,"
                    + "amount REAL NOT NULL,"
                    + "taken_at TEXT NOT NULL,"
                    + "created_at TEXT NOT NULL,"
                    + "notes TEXT,"
                    + "user_id TEXT,"
                    + "user_name TEXT,"
                    + "FOREIGN KEY (medication_id) REFERENCES medications(id)"
                    + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_medication_logs_user_id ON medication_logs(user_id)");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_medication_logs_medication_id ON medication_logs(medication_id)");
        }
        if (oldVersion < 8) {
            db.execSQL("CREATE TABLE IF NOT EXISTS emergency_plan_steps ("
                    + "id TEXT PRIMARY KEY,"
                    + "question TEXT NOT NULL,"
                    + "action TEXT NOT NULL,"
                    + "phone_number TEXT,"
                    + "step_order INTEGER NOT NULL,"
                    + "user_id TEXT,"
                    + "user_name TEXT"
                    + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_emergency_plan_steps_user_id ON emergency_plan_steps(user_id)");
        }
        if (oldVersion < 9) {
            // Create the new actions table
            db.execSQL("CREATE TABLE IF NOT EXISTS emergency_plan_actions ("
                    + "id TEXT PRIMARY KEY,"
                    + "action_text TEXT NOT NULL,"
                    + "phone_number TEXT,"
                    + "action_order INTEGER NOT NULL,"
                    + "step_id TEXT,"
                    + "FOREIGN KEY (step_id) REFERENCES emergency_plan_steps(id) ON DELETE CASCADE"
                    + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_emergency_plan_actions_step_id ON emergency_plan_actions(step_id)");
            // Migrate existing action/phone_number data from steps into the new actions table
            db.execSQL("INSERT INTO emergency_plan_actions (id, action_text, phone_number, action_order, step_id) "
                    + "SELECT lower(hex(randomblob(4)) || '-' || hex(randomblob(2)) || '-' || hex(randomblob(2)) || '-' || hex(randomblob(2)) || '-' || hex(randomblob(6))), "
                    + "action, phone_number, 0, id FROM emergency_plan_steps WHERE action IS NOT NULL");
            // Recreate emergency_plan_steps without the action and phone_number columns
            db.execSQL("CREATE TABLE IF NOT EXISTS emergency_plan_steps_new ("
                    + "id TEXT PRIMARY KEY,"
                    + "question TEXT NOT NULL,"
                    + "step_order INTEGER NOT NULL,"
                    + "user_id TEXT,"
                    + "user_name TEXT"
                    + ")");
            db.execSQL("INSERT INTO emergency_plan_steps_new (id, question, step_order, user_id, user_name) "
                    + "SELECT id, question, step_order, user_id, user_name FROM emergency_plan_steps");
            db.execSQL("DROP TABLE emergency_plan_steps");
            db.execSQL("ALTER TABLE emergency_plan_steps_new RENAME TO emergency_plan_steps");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_emergency_plan_steps_user_id ON emergency_plan_steps(user_id)");
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}
