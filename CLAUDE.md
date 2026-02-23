# Habit Evaluator

A privacy-focused, multi-platform habit tracker for analyzing and evaluating habit performance data without leaking it anywhere.

**Group ID:** `de.idrinth.habitevaluator`
**Version:** 0.1.0-SNAPSHOT
**License:** MIT (Bjorn Buttner)
**Java:** 17
**Build System:** Gradle 9.3.1 (wrapper)

## Project Structure

```text
habit-evaluator/
├── shared/              # Core models, services, repositories, localization, backup, persistence
├── webserver/           # Spring Boot 4.0.2 web application (REST API)
├── desktop/             # JavaFX 21.0.2 desktop application
├── android/             # Android (compileSdk 36, min 23/26/33) Kotlin/Compose application
├── website/             # SvelteKit 2.0 frontend application
├── homepage/            # SvelteKit 2.0 static homepage/documentation site
├── api-bench/           # API performance benchmarking (idrinth-api-bench)
├── .github/workflows/   # CI/CD (GitHub Actions)
├── build.gradle         # Root build configuration (JaCoCo coverage)
├── settings.gradle      # Module definitions (6 Gradle modules)
├── gradle.properties    # Version pins (Java 17, Spring Boot 4.0.2, JavaFX 21.0.2)
├── compose.yml          # Docker Compose (MariaDB + webserver + website + homepage + nginx)
├── scripts/             # Utility scripts (local proxy)
├── crowdin.yml          # Crowdin localization configuration
├── CONTRIBUTING.md      # Contribution guidelines
└── gradle/wrapper/      # Gradle 9.3.1 wrapper
```

## Modules

### shared

Core library consumed by all platform modules. Uses JPMS (`module-info.java`) and opens model package to Hibernate for reflection and backup package to GSON for serialization.

- **Models:** `Habit`, `HabitEntry`, `User`, `HabitCategory`, `Evaluation`, `ScoringRule`, `WeeklyScore`, `HabitScore`, `PredictedWeeklyScore`, `PredictedHabitScore`, `MagicLink`, `FrequencyType` (enum: DAILY/WEEKLY/MONTHLY), `DiaryEntry`, `DiaryReference`, `EventSignificance` (enum: MINOR/NORMAL/MAJOR), `SleepEntry`, `SleepStats`, `SleepDistribution`, `EmotionEntry`, `EmotionPair`, `EmotionStrengthFormatter`, `EventCorrelation`, `ReminderSettings`, `FoodLog`, `FoodTag`, `SportLog`, `SportLogStats`, `Medication`, `MedicationLog`, `MedicationProvisionType` (enum: PILL/LIQUID_DROPS/LIQUID_ML), `MeetingEntry`, `ModuleVisibility`, `EmergencyPlanStep` (question-based steps with ordering), `EmergencyPlanAction` (actions with optional phone numbers), `ActivityLog` (activity tracking with persons, location, time range, optional activity description)
- **Services:** `HabitEvaluatorService` (streaks, completion rates, on-track detection, avoidance streaks for negative habits), `HabitScoringService` (point scoring with thresholds 0/1/2/4/8, weekly aggregation, predicted scores), `DiaryService` (day/week/month points, weekly averages, monthly trends, daily averages), `SleepEvaluationService` (sleep stats: avg/min/max hours, weekly/monthly aggregation, overlap detection), `EventCorrelationService` (time-weighted Pearson correlations with proximity boost across habits, diary, sleep, and emotions over past year; returns top 10 by absolute strength), `SportLogService` (sport log statistics and analysis), `DefaultDataInitializer` (42 default habits, 10 categories, 10 emotion pairs with i18n)
- **Repositories (interfaces):** `HabitRepository`, `UserRepository`, `HabitCategoryRepository`, `ScoringRuleRepository`, `MagicLinkRepository`, `DiaryEntryRepository`, `DiaryReferenceRepository`, `SleepEntryRepository`, `EmotionEntryRepository`, `EmotionPairRepository`, `ReminderSettingsRepository`, `FoodLogRepository`, `FoodTagRepository`, `SportLogRepository`, `MedicationRepository`, `MedicationLogRepository`, `MeetingEntryRepository`, `ModuleVisibilityRepository`, `EmergencyPlanStepRepository`, `EmergencyPlanActionRepository`, `ActivityLogRepository`
- **Persistence (shared implementations):** FileSystem repositories (JSON-based via GSON): `FileSystemHabitRepository`, `FileSystemHabitCategoryRepository`, `FileSystemDiaryEntryRepository`, `FileSystemDiaryReferenceRepository`, `FileSystemSleepEntryRepository`, `FileSystemEmotionPairRepository`, `FileSystemEmotionEntryRepository`; In-memory: `InMemoryHabitRepository`, `InMemoryHabitCategoryRepository`
- **Localization:** `Localizer` service — YAML-based i18n loaded from classpath `localization/{lang}.yml`. Resolution: module.key (requested lang) -> module.key (English) -> general.key (requested lang) -> general.key (English) -> literal fallback. Uses concurrent caching. `ResourceStreamProvider` for classpath resource loading.
- **API client:** `ApiClient` (with `maskBugfixVersion()` for version compatibility), `RemoteHabitRepository`, `RemoteUserRepository`, `StorageConfig` (with ThemeMode enum: SYSTEM/LIGHT/DARK), `SyncData`, `SyncService`, `CircuitBreaker` for resilient client-server communication and bidirectional sync, `VersionMismatchException`
- **Backup:** `BackupService`, `HezBackupService` (.hcz/.hez backup format), `BackupEncryptionService` (password-based encryption), `BackupData` (nested data structures for serialization), `BackupException`, `MergeResult`, `RestoreOptions` (selective restoration: categories, habits, diary, sleep, sport, food, emotions, meetings, medication, reminders)
- **GSON:** `GsonSerializers` (custom serialization helpers)
- **Utilities:** `DateRangeUtils` (date range calculations), `PdfDataAggregator` (PDF report data assembly), `ReminderScheduleCalculator` (reminder schedule computation)
- **Dependencies:** GSON 2.10.1, SLF4J 2.0.11, SnakeYAML 2.5, Jakarta Persistence API 3.1.0 (compile-only), Jackson Annotations 2.21 (compile-only)

### webserver

Spring Boot 4.0.2 application with Spring Security, Spring Data JPA, Spring Dependency Management 1.1.7.

- **Entry point:** `de.idrinth.habitevaluator.webserver.HabitEvaluatorWebApplication`
- **Controllers:** `HabitController`, `AuthController`, `CategoryController`, `ScoringRuleController`, `MagicLinkController`, `DefaultDataController`, `SyncController`, `DiaryController`, `SleepEntryController`, `EmotionPairController`, `ReminderSettingsController`, `StatsController`, `PdfExportController`, `BackupController`, `FoodLogController`, `SportLogController`, `MedicationController`, `MeetingController`, `ModuleVisibilityController`, `VersionController`, `ActivityLogController`
- **Services:** `StatsCacheService` (in-memory per-user cache for expensive stats endpoints with configurable TTL and explicit invalidation)
- **Repositories:** 19 `Database*Repository` wrappers (`DatabaseHabitRepository`, `DatabaseUserRepository`, `DatabaseHabitCategoryRepository`, `DatabaseScoringRuleRepository`, `DatabaseMagicLinkRepository`, `DatabaseDiaryEntryRepository`, `DatabaseDiaryReferenceRepository`, `DatabaseSleepEntryRepository`, `DatabaseEmotionEntryRepository`, `DatabaseEmotionPairRepository`, `DatabaseReminderSettingsRepository`, `DatabaseFoodLogRepository`, `DatabaseFoodTagRepository`, `DatabaseSportLogRepository`, `DatabaseMedicationRepository`, `DatabaseMedicationLogRepository`, `DatabaseMeetingEntryRepository`, `DatabaseModuleVisibilityRepository`, `DatabaseActivityLogRepository`) over 20 Spring Data JPA interfaces (`JpaHabitRepository`, `JpaHabitEntryRepository`, `JpaHabitCategoryRepository`, `JpaUserRepository`, `JpaScoringRuleRepository`, `JpaMagicLinkRepository`, `JpaDiaryEntryRepository`, `JpaDiaryReferenceRepository`, `JpaSleepEntryRepository`, `JpaEmotionEntryRepository`, `JpaEmotionPairRepository`, `JpaReminderSettingsRepository`, `JpaFoodLogRepository`, `JpaFoodTagRepository`, `JpaSportLogRepository`, `JpaMedicationRepository`, `JpaMedicationLogRepository`, `JpaMeetingEntryRepository`, `JpaModuleVisibilityRepository`, `JpaActivityLogRepository`)
- **Configuration:** `SecurityConfig` (BCrypt, session-based auth, CSRF disabled), `AppConfig` (service beans), `DataInitializer` (demo user: username "demo", password "demo123"), `RequestIdFilter` (CSRF & replay attack prevention requiring X-Request-ID header on state-changing requests)
- **DTOs:** `LoginRequest`, `LoginResponse` (standalone); `CreateCategoryRequest`, `CreateScoringRuleRequest`, `MagicLinkRequest` (inner classes in controllers)
- **Database:** H2 (dev, console at `/h2-console`) or MariaDB (production via `mariadb` profile)
- **Artifact:** `habit-evaluator-web.jar`
- **Additional dependencies:** OpenPDF 1.3.35 (PDF generation), springdoc-openapi 2.8.6 (test-only, OpenAPI spec generation)
- **Docker:** `webserver/Dockerfile` — multi-target build: `prebuilt` target (JRE 25, copies pre-built JAR) and default target (JDK 25 build, JRE 25 runtime); source compiled to Java 17 bytecode, exposes port 8080

### desktop

JavaFX 21.0.2 application with JPMS module system.

- **Entry point:** `de.idrinth.habitevaluator.desktop.HabitEvaluatorDesktopApp`
- **Controllers:** `MainController`, `AddHabitController`, `SettingsDialogController`, `SyncDialogController`, `StatsController`, `SleepTrackingController`, `EmotionEntryController`, `EmotionPairController`, `PdfExportController`, `ImprintController`
- **Services:** `ReminderService` (reminder and notification system)
- **Persistence:** `PersistenceManager`, `JpaTransactionHelper`, `H2HabitRepository`, `H2HabitCategoryRepository`, `H2UserRepository`, `H2DiaryEntryRepository`, `H2DiaryReferenceRepository`, `H2SleepEntryRepository`, `H2EmotionPairRepository`, `H2EmotionEntryRepository`, `H2FoodLogRepository`, `H2FoodTagRepository`, `H2SportLogRepository`, `H2ActivityLogRepository`
- **UI:** FXML layouts (`main.fxml`, `add-habit.fxml`, `settings.fxml`, `sync.fxml`, `stats.fxml`, `sleep-tracking.fxml`, `emotion-entry.fxml`, `emotion-pairs.fxml`, `pdf-export.fxml`, `imprint.fxml`), CSS (`styles.css`, `dark.css`)
- **Database:** H2 file-based at `~/.habit-evaluator/data`, configured via `persistence.xml`
- **Dependencies:** JavaFX (controls, FXML), Hibernate Core 6.4.2, H2 2.2.224, Logback 1.4.14, OpenPDF 1.3.35, TestFX Monocle 17.0.10 (test)
- **Packaging:** `jpackageDeb` (Linux .deb), `jpackageExe` (Windows .exe) via jpackage tasks
- **Version properties:** Auto-generated `version.properties` from `project.version` during build

### android

Kotlin/Jetpack Compose Android application (compileSdk 36, targetSdk 35, min 23/26/33). Fully rewritten from Java/XML to Kotlin/Compose.

- **Language:** Kotlin with Jetpack Compose UI framework
- **Namespace:** `de.idrinth.habitevaluator.android`
- **Source directory:** `android/src/main/kotlin/`
- **Architecture:** Single-Activity (`MainActivity`) with Navigation Compose; `AppViewModel` (AndroidViewModel) as central state holder managing all repositories, services, sync, backup, and data operations
- **Screens (Compose):** `HomeScreen`, `DiaryScreen`, `DiaryNavigationScreen`, `SleepTrackingScreen`, `EditHabitsScreen`, `AddHabitScreen`, `StatsScreen`, `PointDevelopmentScreen`, `EmotionalStateScreen`, `RecordEmotionEntryScreen`, `AddEmotionPairScreen`, `SettingsScreen`, `ImprintScreen`, `FoodLogScreen`, `SportLogScreen`, `MedicationListScreen`, `MedicationLogScreen`, `ActivityLogScreen`, `PdfExportScreen`, `SleepAnalysisScreen`, `CorrelationScreen`, `EmergencyPlanScreen`, `EmergencyDialogueScreen`
- **Navigation:** `AppNavigation` (NavHost setup), `Screen` sealed class defining all routes with type-safe navigation
- **UI Components (Compose):** `PointChart`, `SleepGraph`, `EmotionLineChart`, `EmotionScatterChart`, `SleepDistributionChart` (custom Compose chart components)
- **UI Theme:** `Color.kt`, `Theme.kt`, `Type.kt` (Material 3 theming)
- **Utilities:** `FontSizeHelper`, `SettingsConstants` (SharedPreferences keys)
- **Reminders:** `ReminderScheduler`, `ReminderReceiver` (broadcast receiver for boot-completed and reminder intents)
- **Permission Handlers:** `Api33PermissionHandler` (Android 13+), `PreApi33PermissionHandler`, `NotificationPermissionHandler`
- **Persistence (Room):** `AppDatabase` (Room database, version 10, schema export enabled), `Entities.kt` (Room entity classes), `Daos.kt` (Room DAO interfaces: `HabitDao`, `HabitCategoryDao`, `DiaryDao`, `SleepEntryDao`, `EmotionDao`, `FoodLogDao`, `SportLogDao`, `MedicationDao`, `EmergencyPlanDao`, `ActivityLogDao`), `EntityMappers.kt` (Room entity to shared model mapping)
- **Room Repositories:** `RoomHabitRepository`, `RoomHabitCategoryRepository`, `RoomDiaryEntryRepository`, `RoomDiaryReferenceRepository`, `RoomSleepEntryRepository`, `RoomEmotionPairRepository`, `RoomEmotionEntryRepository`, `RoomFoodLogRepository`, `RoomFoodTagRepository`, `RoomSportLogRepository`, `RoomMedicationRepository`, `RoomMedicationLogRepository`, `RoomEmergencyPlanStepRepository`, `RoomEmergencyPlanActionRepository`, `RoomActivityLogRepository`
- **Migration:** `JsonToRoomMigration` (migrates from legacy FileSystem JSON to Room), `LegacySqliteToRoomMigration` (migrates from pre-Room SQLite database to Room)
- **Room schemas:** Exported to `android/schemas/` for migration testing
- **Permissions:** INTERNET, POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED, SCHEDULE_EXACT_ALARM (API ≤32)
- **Build Flavors:** `lollipop` (minSdk 23), `oreo` (minSdk 26), `tiramisu` (minSdk 33) with Core Library Desugaring
- **Dependencies:** Jetpack Compose BOM 2026.01.01 (UI, Graphics, Material 3, Icons Extended), Navigation Compose 2.9.7, Activity Compose 1.12.4, Lifecycle (ViewModel Compose 2.10.0, Runtime Compose 2.10.0), Room 2.8.4 (with KSP compiler), Kotlin Coroutines 1.10.2, AndroidX AppCompat 1.7.1, Material 1.11.0, DocumentFile 1.0.1, Core Library Desugaring 2.0.4, SLF4J no-op 2.0.17, OpenPDF 1.3.35
- **Test dependencies:** JUnit Jupiter 5.10.2, Mockito 5.11.0, Kotlin Coroutines Test 1.10.2, Room Testing 2.8.4
- **Build:** Android Gradle Plugin 9.0.1, Kotlin Compose plugin 2.3.0, KSP 2.3.5, Room plugin 2.8.4, ProGuard minification and resource shrinking in release builds, signing config for release APKs, AAB (Android App Bundle) support, JaCoCo 0.8.13 coverage reporting for all three flavors, Compose enabled, buildConfig enabled

### website

SvelteKit 2.0 frontend application with TypeScript.

- **Routes:** Login, habits (add/edit/home), categories (add), score-rules (add), diary, sleep, emotions graph, stats (dashboard, correlations), points, PDF export, backup, settings, imprint, food-log (home, distribution), sport-graph, activity-log
- **API client:** `src/lib/api.ts` (40+ endpoints, circuit breaker pattern)
- **i18n:** `src/lib/i18n.ts` — client-side internationalization
- **Config:** `src/lib/config.ts` — runtime configuration with dynamic API URL
- **Build:** SvelteKit 2.52.2, Svelte 5.53.0, Vite 7.3.1, `svelte-check` for type checking, TypeScript 5.9.3 (strict mode)
- **Testing:** Vitest 4.0.18, Testing Library (Svelte 5.3.1, user-event 14.6.1, jest-dom 6.9.1), jsdom 28.1.0, Cypress 15.10.0 (E2E)
- **Docker:** `website/Dockerfile` — multi-target build: `prebuilt` target (nginx alpine, copies build/) and default target (Node.js 25 alpine build, nginx alpine runtime); `docker-entrypoint.sh` injects `API_BASE_URL` at runtime
- **Static adapter** with SPA fallback (`index.html`)

### homepage

SvelteKit 2.0 static homepage and documentation site.

- **Routes:** Landing page, features page, documentation pages (android, desktop, api, webserver), imprint
- **Build:** SvelteKit 2.50.2, Svelte 5.50.2, Vite 7.3.1, TypeScript 5.9.3
- **Prerendering:** All routes prerendered (`['*']`), strict mode enabled
- **Docker:** `homepage/Dockerfile` — multi-target build: `prebuilt` target (nginx alpine) and default target (Node.js 25 alpine build, nginx alpine runtime)

### api-bench

API performance benchmarking suite using `@idrinth-api-bench/framework`.

- **Entry point:** `npm start` (runs `iab bench`)
- **Configuration:** `.env` file for target API settings
- **Route structure:**
  - `src/routes/before-all/` — Setup routes (login, init-defaults)
  - `src/routes/main/` — 33 benchmark routes covering all API endpoints (auth, habits, categories, diary, sleep, emotions, food-logs, sport-logs, medications, meetings, stats, sync, magic-links, reminder-settings, score-rules)
- **Requirements:** Node.js >= 20

## REST API

| Method | Path | Description |
| --- | --- | --- |
| POST | `/api/auth/login` | Login |
| POST | `/api/auth/logout` | Logout |
| GET | `/api/auth/me` | Get current user |
| GET | `/api/version` | Get masked API version |
| GET | `/api/habits` | List user's habits |
| GET | `/api/habits/{id}` | Get habit by ID |
| POST | `/api/habits` | Create habit |
| PUT | `/api/habits/{id}` | Update habit |
| DELETE | `/api/habits/{id}` | Delete habit (auto-cleanup unused categories) |
| POST | `/api/habits/{id}/entries` | Add completion entry |
| DELETE | `/api/habits/{id}/entries/last` | Remove last entry for date |
| GET | `/api/habits/{id}/evaluate` | Evaluate habit (optional date range) |
| GET | `/api/habits/{id}/point-development` | Point development chart data (weekly/monthly) |
| GET | `/api/habits/predict` | Predict weekly scores |
| GET | `/api/categories` | List user's categories |
| POST | `/api/categories` | Create category |
| POST | `/api/score-rules` | Create scoring rule (validates threshold ordering) |
| POST | `/api/magic-links` | Create magic link for sharing |
| GET | `/api/magic-links` | List user's magic links |
| DELETE | `/api/magic-links/{id}` | Delete magic link |
| GET | `/api/shared/{token}` | Public shared data access (no auth required) |
| GET | `/api/diary` | List user's diary entries (with migration) |
| POST | `/api/diary` | Create diary entry (converts description to reference) |
| DELETE | `/api/diary/{id}` | Delete diary entry |
| GET | `/api/diary/suggestions` | Get suggestions from past entries |
| GET | `/api/diary/stats` | Diary stats (today/week/month points, weekly average, monthly trend) |
| GET | `/api/sleep-entries` | List user's sleep entries |
| POST | `/api/sleep-entries` | Create sleep entry (validates no overlaps) |
| DELETE | `/api/sleep-entries/{id}` | Delete sleep entry |
| GET | `/api/sleep-entries/stats` | Sleep stats (weekly and monthly: avg/min/max hours) |
| GET | `/api/emotions/pairs` | List user's emotion pairs with labels |
| GET | `/api/emotions/graph` | Emotion graph data (daily averages, overall averages per pair) |
| GET | `/api/food-logs` | List user's food log entries |
| POST | `/api/food-logs` | Create food log entry (auto-resolves food tags) |
| DELETE | `/api/food-logs/{id}` | Delete food log entry |
| GET | `/api/food-logs/suggestions` | Get food item suggestions from tags |
| POST | `/api/food-logs/migrate-tags` | Migrate existing entries to use food tags |
| GET | `/api/sport-logs` | List user's sport log entries |
| POST | `/api/sport-logs` | Create sport log entry (validates name and unit) |
| DELETE | `/api/sport-logs/{id}` | Delete sport log entry |
| GET | `/api/sport-logs/graph` | 30-day sport activity graph (by activity, duration/measurement/count) |
| GET | `/api/sport-logs/stats` | Sport stats (weekly and monthly) |
| GET | `/api/medications` | List user's medications |
| POST | `/api/medications` | Create medication |
| PUT | `/api/medications/{id}` | Update medication (wikipedia link) |
| DELETE | `/api/medications/{id}` | Delete medication |
| GET | `/api/medications/logs` | List user's medication logs |
| POST | `/api/medications/logs` | Create medication log entry |
| DELETE | `/api/medications/logs/{id}` | Delete medication log entry |
| GET | `/api/meetings` | List user's meeting entries |
| POST | `/api/meetings` | Create meeting entry |
| DELETE | `/api/meetings/{id}` | Delete meeting entry |
| GET | `/api/module-visibility` | Get module visibility settings |
| PUT | `/api/module-visibility` | Update module visibility settings |
| GET | `/api/reminder-settings` | Get reminder settings |
| PUT | `/api/reminder-settings` | Update reminder settings |
| GET | `/api/stats/dashboard` | 30-day dashboard (habit points, diary points, sleep duration/entries) |
| GET | `/api/stats/daily-timeline` | 30-day daily timeline of habit entries with category colors |
| GET | `/api/stats/correlations` | Top event correlations across habits, diary, sleep, emotions |
| GET | `/api/stats/emotion-scatter` | Emotion scatter plot by time-of-day |
| GET | `/api/stats/food-distribution` | Food distribution statistics |
| GET | `/api/export/pdf` | PDF report (params: from, to, habits, sleep, diary, emotions flags) |
| POST | `/api/sync` | Bidirectional sync (merges client/server habits and entries) |
| POST | `/api/init-defaults` | Initialize default data for user (with language parameter) |
| GET | `/api/activity-logs` | List user's activity log entries |
| POST | `/api/activity-logs` | Create activity log entry |
| PUT | `/api/activity-logs/{id}` | Update activity log entry |
| DELETE | `/api/activity-logs/{id}` | Delete activity log entry |
| GET | `/api/backup` | Download .hez backup file (password encrypted) |
| POST | `/api/backup` | Upload and restore .hez backup (supports selective RestoreOptions) |

## Database Schema

All entities use UUID string IDs (`@Id @Column(length = 36)`).

- **users** — id, username (unique), password (BCrypt), email (unique), created_at
- **habits** — id, name, description (1000), categoryId (FK), frequency_type (ENUM), target_frequency, max_entries_per_day, positive_scoring, created_at, scoring_rule_id (FK), user_id (FK), name_translations (map), description_translations (map)
- **habit_entries** — id, habit_id (FK), completed_at, notes (500), value
- **habit_categories** — id, name, description (1000), color, user_id (FK)
- **scoring_rules** — id, name, threshold_for_1_point, threshold_for_2_points, threshold_for_4_points, threshold_for_8_points, user_id (FK)
- **magic_links** — id, token (unique, 64), user_id (FK), categoryId, filterStart, filterEnd, created_at, expiresAt
- **diary_entries** — id, legacy_description (500), diary_reference_id (FK), significance (ENUM: MINOR/NORMAL/MAJOR), event_date, start_time, end_time, created_at, user_id (FK)
- **diary_references** — id, description, description_lower (auto-generated lowercase), user_id (FK)
- **sleep_entries** — id, from_time, until_time, date, created_at, notes (500), user_id (FK)
- **emotion_pairs** — id, negative_label, positive_label, user_id (FK)
- **emotion_entries** — id, emotion_pair_id (FK), strength (int, -10 to +10), recorded_at, notes (max 500), user_id (FK)
- **reminder_settings** — id, user_id (FK, unique, OneToOne), sleep_reminder_enabled, sleep_reminder_time, diary_reminder_enabled, diary_reminder_time, emotion_reminder_enabled, emotion_reminder_count (1-10), waking_hours_start, waking_hours_end
- **food_logs** — id, carbohydrates (Double), kcal (Integer), food_items (2000, comma-separated), notes (500), date_time, created_at, user_id (FK); many-to-many with food_tags via `food_log_tags` join table
- **food_tags** — id, name, name_lower (auto-generated lowercase), user_id (FK)
- **sport_logs** — id, name, measurement (double), measurement_unit, start_time, end_time, date, notes (500), created_at, user_id (FK)
- **medications** — id, name, wikipedia_link (500), provision_type (ENUM: PILL/LIQUID_DROPS/LIQUID_ML), user_id (FK)
- **medication_logs** — id, medication_id (FK), dose (double), taken_at, notes (500), created_at, user_id (FK)
- **meeting_entries** — id, place, attendants (1000), start_time, end_time, date, created_at, user_id (FK)
- **module_visibility** — id, user_id (FK, unique, OneToOne), diary_visible, sleep_visible, emotions_visible, points_visible, statistics_visible, food_log_visible, sport_log_visible, medication_visible, backup_visible, pdf_export_visible, activity_log_visible (all default true)
- **activity_logs** — id, persons (1000), location, start_time, end_time, date, activity (500), created_at, user_id (FK)
- **emergency_plan_steps** — id, question (500), step_order, user_id (FK)
- **emergency_plan_actions** — id, action_text (500), phone_number (50, nullable), action_order, step_id (FK to emergency_plan_steps)

**Unique constraints:** Habit (name + categoryId + userId), User (username)
**Cascade:** User -> habits (delete), Habit -> entries (delete), User -> diary_entries (delete), User -> sleep_entries (delete)

**Database profiles:**
- Default (H2): file-based at `./data/habit-evaluator`, user `sa`, DDL auto-update
- MariaDB: activate with `--spring.profiles.active=mariadb`, configured via `MARIADB_URL`, `MARIADB_USERNAME`, `MARIADB_PASSWORD` env vars

## Authentication & Security

- Session-based with HttpSession and BCrypt password encoding
- `RequestIdFilter` requires `X-Request-ID` header (UUID v4) on state-changing HTTP methods (POST, PUT, DELETE, PATCH) for CSRF and replay attack prevention
- Magic links for read-only shared access with optional expiration and category/time filtering
- Public endpoints: `/api/auth/**`, `/api/shared/**`, `/api/version`, `/login`, `/h2-console`, `/score-rules/**`
- All other endpoints require authentication

## Scoring & Evaluation Logic

**HabitEvaluatorService:**
- `evaluate(habit, periodStart, periodEnd)` — total entries, target entries, completion rate (capped at 1.0), current streak, longest streak
- `evaluateCurrentWeek(habit)` / `evaluateCurrentMonth(habit)` — convenience methods
- On-track threshold: completionRate >= 0.8
- Target calculation: DAILY (days x target), WEEKLY (ceil(days/7) x target), MONTHLY (max(1, months) x target)
- Negative habit support: avoidance rate (1 - completion rate), avoidance streaks (consecutive days without completions)

**HabitScoringService:**
- Fixed point scale: 0, 1, 2, 4, 8
- Configurable thresholds per user via ScoringRule entity (defaults: 1, 2, 4, 7)
- `calculateHabitScore()` — weekly completion count to points
- `calculateWeeklyScore()` — aggregates all habits into WeeklyScore
- `calculateWeeklyScoreByWeekNumber()` — score for a specific ISO week
- `getCurrentWeekScore()` / `getCurrentDayScore()` / `getCurrentMonthScore()` — per-habit convenience methods
- `predictHabitScore()` — extrapolates completion rate using elapsed hours from week start (sub-day precision)
- `predictCurrentWeekScore()` — predicts all habits for current week

**DiaryService:**
- Points per entry based on EventSignificance: MINOR (1), NORMAL (2), MAJOR (4)
- `getDayPoints()` / `getCurrentWeekPoints()` / `getCurrentMonthPoints()` — period aggregations
- `getWeeklyAverageForMonth()` — average points per week for current month
- `getDailyAverageForMonth()` — average points per day for current month
- `getMonthlyTrend()` — trend comparison (current week vs. previous weeks)
- `getEntriesInRange()` / `getPointsInRange()` — date-range filtering

**SleepEvaluationService:**
- `calculateStats(entries, periodStart, periodEnd)` — SleepStats with avg/min/max hours and total entries
- `getCurrentWeekStats()` / `getCurrentMonthStats()` — convenience methods
- `hasOverlap()` — validates no overlapping sleep entries (handles midnight crossing)
- Sleep duration handles midnight crossing (e.g., 23:00 to 07:00)

**SportLogService:**
- `getCurrentWeekStats(entries)` / `getCurrentMonthStats(entries)` — SportLogStats aggregation
- Statistics per activity: total duration, total measurement, entry count

**EventCorrelationService:**
- Calculates time-weighted Pearson correlations across habits, diary, sleep, and emotions over the past year (365 days)
- Creates daily signals for habit completions, diary points, diary duration, sleep hours, and emotion pair averages
- Applies linear decay weights (newer entries weighted higher) with proximity boost (2x weight for events within 2 hours)
- Returns top 10 correlations sorted by absolute strength, requiring minimum 7 shared days
- Used by `StatsController` for `/api/stats/correlations` and `PdfExportController` for reports

## Build Commands

```bash
# Build all modules
./gradlew build

# Run webserver
./gradlew :webserver:bootRun

# Run webserver with MariaDB
./gradlew :webserver:bootRun --args='--spring.profiles.active=mariadb'

# Run desktop app
./gradlew :desktop:run

# Run tests
./gradlew test

# Run tests with coverage report
./gradlew test jacocoTestReport

# Build Android APK (debug, oreo flavor)
./gradlew :android:assembleOreoDebug

# Build Android APK (debug, lollipop flavor)
./gradlew :android:assembleLollipopDebug

# Build Android APK (debug, tiramisu flavor)
./gradlew :android:assembleTiramisuDebug

# Build Android AAB (release bundle)
./gradlew :android:bundleRelease

# Package desktop as .deb (Linux)
./gradlew :desktop:jpackageDeb

# Package desktop as .exe (Windows)
./gradlew :desktop:jpackageExe

# Docker Compose (full stack)
docker compose up --build

# Website development
cd website && npm install && npm run dev

# Website type checking
cd website && npm run check

# Website tests
cd website && npm test

# Website E2E tests
cd website && npm run test:e2e

# Homepage development
cd homepage && npm install && npm run dev

# Homepage type checking
cd homepage && npm run check

# API benchmarks
cd api-bench && npm install && npm start
```

## Outbound Proxy Configuration

This environment routes outbound traffic through an HTTP proxy. The `http_proxy` / `https_proxy` / `HTTP_PROXY` / `HTTPS_PROXY` environment variables are set automatically, but **Gradle's JVM does not pick them up** — and even when proxy host/port/credentials are passed as JVM system properties, **Gradle's Apache HttpClient negotiates NTLM instead of Basic auth**, which the proxy rejects.

**Solution: local forwarding proxy.** A Python script at `scripts/local-proxy.py` runs a local unauthenticated proxy on `127.0.0.1:18080` that forwards requests to the upstream proxy with the correct `Proxy-Authorization: Basic ...` header. Gradle connects to the local proxy without credentials, avoiding the NTLM issue entirely.

**Step 1 — Start the local proxy (in background):**

```bash
python3 scripts/local-proxy.py &
LOCAL_PROXY_PID=$!
# Wait briefly for it to start listening
sleep 1
```

**Step 2 — Run Gradle through the local proxy:**

```bash
./gradlew build \
  -Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=18080 \
  -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=18080 \
  -Dhttp.nonProxyHosts="localhost|127.0.0.1"
```

**Step 3 — Stop the local proxy when done:**

```bash
kill $LOCAL_PROXY_PID 2>/dev/null
```

The script reads the upstream proxy URL (including credentials) from the `http_proxy` / `HTTP_PROXY` environment variable automatically. It defaults to port 18080 but accepts an optional port argument: `python3 scripts/local-proxy.py 9999`.

**npm / Node.js** tools (website, homepage) generally respect the environment variables automatically and need no extra configuration.

## Android SDK Setup

The Android SDK is **not pre-installed** in this environment. The android module requires compileSdk 36, build-tools 36.0.0, and platform-tools. Follow these steps to install the SDK from scratch.

**Step 1 — Download and install Android command-line tools:**

```bash
export ANDROID_HOME=/opt/android-sdk
mkdir -p "$ANDROID_HOME/cmdline-tools"
cd /tmp
curl -fsSL -o cmdline-tools.zip \
  "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
unzip -q cmdline-tools.zip -d cmdline-tools-tmp
mv cmdline-tools-tmp/cmdline-tools "$ANDROID_HOME/cmdline-tools/latest"
rm -rf cmdline-tools.zip cmdline-tools-tmp
```

**Step 2 — Accept licenses:**

```bash
mkdir -p "$ANDROID_HOME/licenses"
echo -e "\n24333f8a63b6825ea9c5514f83c2829b004d1fee" > "$ANDROID_HOME/licenses/android-sdk-license"
echo -e "\n84831b9409646a918e30573bab4c9c91346d8abd" > "$ANDROID_HOME/licenses/android-sdk-arm-dbt-license"
```

**Step 3 — Install required SDK packages (via local proxy):**

```bash
python3 scripts/local-proxy.py &
LOCAL_PROXY_PID=$!
sleep 2
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --install \
  "platforms;android-36" "build-tools;36.0.0" "platform-tools" \
  --proxy=http --proxy_host=127.0.0.1 --proxy_port=18080
kill $LOCAL_PROXY_PID 2>/dev/null
```

**Step 4 — Create `local.properties`** (already gitignored):

```bash
echo "sdk.dir=/opt/android-sdk" > local.properties
```

**Step 5 — Verify the build works:**

```bash
python3 scripts/local-proxy.py &
LOCAL_PROXY_PID=$!
sleep 2
export ANDROID_HOME=/opt/android-sdk
./gradlew :android:assembleOreoDebug \
  -Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=18080 \
  -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=18080 \
  -Dhttp.nonProxyHosts="localhost|127.0.0.1"
kill $LOCAL_PROXY_PID 2>/dev/null
```

**Note:** The Gradle wrapper download (`gradle-9.3.1-bin.zip`) also needs network access. If the wrapper fails to download through the proxy, download it manually with curl (which respects `http_proxy`/`https_proxy` env vars) into the wrapper cache directory:

```bash
# Find the wrapper cache dir (created after the first failed attempt)
WRAPPER_DIR=$(ls -d ~/.gradle/wrapper/dists/gradle-*/*/gradle-*.zip.lck 2>/dev/null | head -1 | xargs dirname)
curl -fsSL -o "$WRAPPER_DIR/gradle-9.3.1-bin.zip" \
  "https://services.gradle.org/distributions/gradle-9.3.1-bin.zip"
cd "$WRAPPER_DIR" && unzip -q gradle-9.3.1-bin.zip && rm -f *.lck *.part
```

## Testing

**Java Framework:** JUnit 5 (Jupiter 5.10.2), JaCoCo 0.8.13 for coverage
**Frontend Framework:** Vitest 4.0.18, Testing Library (Svelte/user-event), jsdom 28.1.0, Cypress 15.10.0 (E2E)

**Test locations:**
- `shared/src/test/java/` — 76 test classes:
  - **Service tests (8):** `HabitEvaluatorServiceTest`, `HabitScoringServiceTest`, `HabitScoringServicePredictionTest`, `DiaryServiceTest`, `SleepEvaluationServiceTest`, `EventCorrelationServiceTest`, `SportLogServiceTest`, `DefaultDataInitializerTest`
  - **Model tests (35):** `HabitTest`, `HabitEntryTest`, `UserTest`, `HabitCategoryTest`, `ScoringRuleTest`, `WeeklyScoreTest`, `HabitScoreTest`, `PredictedHabitScoreTest`, `PredictedWeeklyScoreTest`, `EvaluationTest`, `MagicLinkTest`, `FrequencyTypeTest`, `DiaryEntryTest`, `DiaryReferenceTest`, `SleepEntryTest`, `SleepStatsTest`, `SleepDistributionTest`, `EmotionEntryTest`, `EmotionPairTest`, `EmotionStrengthFormatterTest`, `EventSignificanceTest`, `EventCorrelationTest`, `ReminderSettingsTest`, `FoodLogTest`, `FoodTagTest`, `SportLogTest`, `SportLogStatsTest`, `MedicationTest`, `MedicationLogTest`, `MedicationProvisionTypeTest`, `MeetingEntryTest`, `ModuleVisibilityTest`, `EmergencyPlanStepTest`, `EmergencyPlanActionTest`, `ActivityLogTest`
  - **API/Sync tests (8):** `ApiClientTest`, `SyncDataTest`, `SyncServiceTest`, `StorageConfigTest`, `CircuitBreakerTest`, `RemoteHabitRepositoryTest`, `RemoteUserRepositoryTest`, `VersionMismatchExceptionTest`
  - **Backup tests (8):** `BackupServiceTest`, `HezBackupServiceTest`, `HezBackupRestoreTest`, `BackupEncryptionServiceTest`, `BackupDataTest`, `BackupExceptionTest`, `MergeResultTest`, `RestoreOptionsTest`
  - **Persistence tests (9):** `FileSystemHabitRepositoryTest`, `FileSystemHabitCategoryRepositoryTest`, `FileSystemDiaryEntryRepositoryTest`, `FileSystemDiaryReferenceRepositoryTest`, `FileSystemSleepEntryRepositoryTest`, `FileSystemEmotionPairRepositoryTest`, `FileSystemEmotionEntryRepositoryTest`, `InMemoryHabitRepositoryTest`, `InMemoryHabitCategoryRepositoryTest`
  - **Localization tests (3):** `LocalizerTest`, `TranslationCompletenessTest`, `ResourceStreamProviderTest`
  - **Other tests (5):** `GsonSerializersTest`, `DiaryReferenceRepositoryTest`, `DateRangeUtilsTest`, `PdfDataAggregatorTest`, `ReminderScheduleCalculatorTest`
- `webserver/src/test/java/` — 69 test classes:
  - **Controller tests (22):** `AuthControllerTest`, `HabitControllerTest`, `CategoryControllerTest`, `DiaryControllerTest`, `SleepEntryControllerTest`, `EmotionPairControllerTest`, `ScoringRuleControllerTest`, `MagicLinkControllerTest`, `ReminderSettingsControllerTest`, `StatsControllerTest`, `SyncControllerTest`, `PdfExportControllerTest`, `DefaultDataControllerTest`, `BackupControllerTest`, `SecurityConfigTest`, `FoodLogControllerTest`, `SportLogControllerTest`, `MedicationControllerTest`, `MeetingControllerTest`, `ModuleVisibilityControllerTest`, `VersionControllerTest`, `ActivityLogControllerTest`
  - **JPA repository tests (19):** `JpaHabitRepositoryTest`, `JpaUserRepositoryTest`, `JpaDiaryEntryRepositoryTest`, `JpaDiaryReferenceRepositoryTest`, `JpaMagicLinkRepositoryTest`, `JpaScoringRuleRepositoryTest`, `JpaSleepEntryRepositoryTest`, `JpaEmotionEntryRepositoryTest`, `JpaEmotionPairRepositoryTest`, `JpaReminderSettingsRepositoryTest`, `JpaHabitCategoryRepositoryTest`, `JpaFoodLogRepositoryTest`, `JpaFoodTagRepositoryTest`, `JpaSportLogRepositoryTest`, `JpaMedicationRepositoryTest`, `JpaMedicationLogRepositoryTest`, `JpaMeetingEntryRepositoryTest`, `JpaModuleVisibilityRepositoryTest`, `JpaActivityLogRepositoryTest`
  - **Database repository tests (19):** `DatabaseHabitRepositoryTest`, `DatabaseUserRepositoryTest`, `DatabaseDiaryEntryRepositoryTest`, `DatabaseDiaryReferenceRepositoryTest`, `DatabaseMagicLinkRepositoryTest`, `DatabaseScoringRuleRepositoryTest`, `DatabaseSleepEntryRepositoryTest`, `DatabaseEmotionEntryRepositoryTest`, `DatabaseEmotionPairRepositoryTest`, `DatabaseReminderSettingsRepositoryTest`, `DatabaseHabitCategoryRepositoryTest`, `DatabaseFoodLogRepositoryTest`, `DatabaseFoodTagRepositoryTest`, `DatabaseSportLogRepositoryTest`, `DatabaseMedicationRepositoryTest`, `DatabaseMedicationLogRepositoryTest`, `DatabaseMeetingEntryRepositoryTest`, `DatabaseModuleVisibilityRepositoryTest`, `DatabaseActivityLogRepositoryTest`
  - **Service tests (1):** `StatsCacheServiceTest`
  - **Config tests (3):** `RequestIdFilterTest`, `DataInitializerTest`, `AppConfigTest`
  - **DTO tests (2):** `LoginRequestTest`, `LoginResponseTest`
  - **Other (2):** `HabitEvaluatorWebApplicationTest`, `OpenApiGeneratorTest`
- `desktop/src/test/java/` — 26 test classes:
  - **Repository tests (14):** `H2HabitRepositoryTest`, `H2HabitCategoryRepositoryTest`, `H2UserRepositoryTest`, `H2DiaryEntryRepositoryTest`, `H2DiaryReferenceRepositoryTest`, `H2SleepEntryRepositoryTest`, `H2EmotionPairRepositoryTest`, `H2EmotionEntryRepositoryTest`, `H2FoodLogRepositoryTest`, `H2FoodTagRepositoryTest`, `H2SportLogRepositoryTest`, `H2ActivityLogRepositoryTest`, `PersistenceManagerTest`, `JpaTransactionHelperTest`
  - **Controller tests (10):** `MainControllerTest`, `AddHabitControllerTest`, `SettingsDialogControllerTest`, `SyncDialogControllerTest`, `StatsControllerTest`, `SleepTrackingControllerTest`, `EmotionEntryControllerTest`, `EmotionPairControllerTest`, `PdfExportControllerTest`, `ImprintControllerTest`
  - **Other (2):** `ReminderServiceTest`, `HabitEvaluatorDesktopAppTest`
  - **Test base classes (2):** `H2RepositoryTestBase`, `JavaFXControllerTestBase`
- `android/src/test/kotlin/` — 75 Kotlin test classes:
  - **Screen tests (34):** `HomeScreenTest`, `EditHabitsScreenTest`, `AddHabitScreenTest`, `DiaryScreenTest`, `DiaryNavigationScreenTest`, `SleepTrackingScreenTest`, `StatsScreenTest`, `PointDevelopmentScreenTest`, `EmotionalStateScreenTest`, `RecordEmotionEntryScreenTest`, `AddEmotionPairScreenTest`, `ImprintScreenTest`, `SettingsScreenTest`, `FoodLogScreenTest`, `SportLogScreenTest`, `MedicationListScreenTest`, `MedicationLogScreenTest`, `EmergencyPlanScreenTest`, `EmergencyDialogueScreenTest`, `ActivityLogScreenTest`, `PdfExportScreenTest`, `SleepAnalysisScreenTest`, `CorrelationScreenTest`, `MainActivityTest`, `StartupRegressionTest`, `MaestroFormValidationTest`, `MaestroNavigationCoverageTest`, `SettingsConstantsTest`, `FontSizeHelperTest`, `ReminderSchedulerTest`, `ReminderReceiverTest`, `PreApi33PermissionHandlerTest`, `Api33PermissionHandlerTest`, `NotificationHelperTest`
  - **UI/Adapter tests (18):** `HabitAdapterTest`, `EditHabitAdapterTest`, `DiaryEntryAdapterTest`, `SleepEntryAdapterTest`, `EmotionPairAdapterTest`, `EmotionDataAdapterTest`, `FoodLogAdapterTest`, `SportLogAdapterTest`, `MedicationAdapterTest`, `MedicationLogAdapterTest`, `ActivityLogAdapterTest`, `ScreenPagerAdapterTest`, `PointChartTest`, `SleepGraphTest`, `EmotionLineChartTest`, `EmotionScatterChartTest`, `SleepDistributionChartTest`, `ViewPager2SwipeSensitivityReducerTest`
  - **Navigation/Regression tests (4):** `ScreenTest`, `AdapterDataConsistencyTest`, `BottomNavigationRegressionTest`, `StartupRegressionTest`
  - **Persistence tests (19):** `AppDatabaseTest`, `EntitiesTest`, `EntityMappersTest`, `RoomHabitRepositoryTest`, `RoomHabitCategoryRepositoryTest`, `RoomDiaryEntryRepositoryTest`, `RoomDiaryReferenceRepositoryTest`, `RoomSleepEntryRepositoryTest`, `RoomEmotionPairRepositoryTest`, `RoomEmotionEntryRepositoryTest`, `RoomFoodLogRepositoryTest`, `RoomFoodTagRepositoryTest`, `RoomSportLogRepositoryTest`, `RoomMedicationRepositoryTest`, `RoomMedicationLogRepositoryTest`, `RoomEmergencyPlanStepRepositoryTest`, `RoomEmergencyPlanActionRepositoryTest`, `RoomActivityLogRepositoryTest`, `JsonToRoomMigrationTest`, `LegacySqliteToRoomMigrationTest`
- `website/src/` — 6 test files:
  - `lib/api.test.ts`, `lib/config.test.ts`, `lib/i18n.test.ts`, `routes/login/login.test.ts`, `routes/diary/diary.test.ts`, `routes/categories/add/categories-add.test.ts`
- `homepage/src/` — 4 test files:
  - `routes/landing.test.ts`, `routes/imprint/imprint.test.ts`, `routes/docs/docs.test.ts`, `routes/features/features.test.ts`
- `shared/src/test/resources/test-localization/` — German and English YAML test fixtures

**Conventions:**
- Descriptive method names (e.g. `testEvaluateWithNoEntries`, `testStreakCalculation`)
- `@BeforeEach` setup methods for test fixtures
- Standard JUnit assertions (`assertEquals`, `assertTrue`, `assertFalse`)
- Webserver tests use `@ActiveProfiles("test")` with in-memory H2
- Desktop tests use shared `H2RepositoryTestBase` for JPA setup and `JavaFXControllerTestBase` for controller tests
- Android tests use JUnit 5 (Jupiter) with Mockito 5.11.0 and Kotlin Coroutines Test

## CI/CD

GitHub Actions workflows at `.github/workflows/`:

- **ci.yml** — Primary CI pipeline: triggers on push/PR to `the-one`. Contains 11 jobs:
  - **java** — JDK 21 (Temurin), builds shared/webserver/desktop, JaCoCo coverage (individual + aggregate), coverage verification, Coveralls upload
  - **android** — JDK 17, debug APKs for all three flavors (lollipop, oreo, tiramisu), unit tests, JaCoCo coverage, Coveralls upload
  - **frontend-check** — Matrix job for website and homepage `svelte-check` type checking (Node.js 22)
  - **website-test** — Website unit tests with coverage, Coveralls upload
  - **homepage-test** — Homepage unit tests with coverage, Coveralls upload
  - **e2e** — Matrix Cypress E2E tests for website and homepage with coverage (uses `VITE_COVERAGE=true`)
  - **desktop-deb** — Linux .deb packaging via jpackage, JDK 21 (depends on java)
  - **desktop-exe** — Windows .exe packaging via jpackage, JDK 21 (depends on java)
  - **check-signing** — Checks for Android keystore secrets availability
  - **android-release** — Matrix job for signed APK + AAB per flavor (conditional on secrets)
  - **docker** — Matrix Docker builds to GHCR for webserver, website, homepage (conditional on push, depends on java + e2e + frontend-check + website-test + homepage-test)
  - **coveralls-finish** — Finalizes parallel Coveralls reporting (depends on java + android + website-test + homepage-test + e2e)
- **release.yml** — Release automation: artifact builds (Linux .deb, Windows .exe, Android APK for lollipop/oreo/tiramisu flavors, AAB), Docker builds to GHCR (webserver, website, homepage), signing with environment variables
- **release-auto-draft.yml** — Automatic draft release creation with semantic versioning
- **semver-label.yml** — Validates PR semver labels (major/minor/patch)
- **coderabbit-retry.yml** — Code review automation

## Architecture & Conventions

**Layered architecture with shared core:**
- Models (JPA entities) and Services (stateless business logic) live in `shared`
- Each platform provides its own Repository implementation and UI/Controller layer
- Webserver uses Spring DI; desktop and Android use manual dependency management
- FileSystem and InMemory repository implementations also live in `shared` (under `shared.persistence`)

**Package naming:** `de.idrinth.habitevaluator.[module].[layer]`

**Naming patterns:**
- Entities: PascalCase (`Habit`, `HabitEntry`, `User`, `DiaryEntry`, `DiaryReference`, `SleepEntry`, `EmotionEntry`, `EmotionPair`, `ReminderSettings`, `FoodLog`, `FoodTag`, `SportLog`, `Medication`, `MedicationLog`, `MeetingEntry`, `ModuleVisibility`, `EmergencyPlanStep`, `EmergencyPlanAction`, `ActivityLog`)
- Services: `*Service` (`HabitEvaluatorService`, `DiaryService`, `SleepEvaluationService`, `EventCorrelationService`, `SportLogService`, `BackupService`, `SyncService`, `ReminderService`, `StatsCacheService`)
- Repositories: `*Repository` (interface), `Database*Repository` (Spring Data wrapper), `H2*Repository` (desktop), `FileSystem*Repository` (shared, JSON-based), `Room*Repository` (Android), `InMemory*Repository` (shared, testing)
- Controllers: `*Controller` (`HabitController`, `AuthController`, `CategoryController`, `StatsController`, `PdfExportController`, `BackupController`, `ReminderSettingsController`, `FoodLogController`, `SportLogController`, `MedicationController`, `MeetingController`, `ModuleVisibilityController`, `VersionController`, `ActivityLogController`)
- Screens (Android Compose): `*Screen` (`HomeScreen`, `DiaryScreen`, `EditHabitsScreen`, `StatsScreen`, `SettingsScreen`, `FoodLogScreen`, `SportLogScreen`, `MedicationListScreen`, `MedicationLogScreen`, `EmergencyPlanScreen`, `ActivityLogScreen`, etc.)
- Chart Components (Android Compose): `*Chart` / `*Graph` (`PointChart`, `SleepGraph`, `EmotionLineChart`, `EmotionScatterChart`, `SleepDistributionChart`)
- Methods: camelCase (`evaluate`, `calculateTargetEntries`, `isExpired`)

**Key patterns:**
- Repository pattern with interface-based abstraction and multiple implementations per platform
- UUID string IDs for all entities
- `@JsonIgnore` on sensitive fields (User.password, User.habits)
- Lazy loading for JPA relationships
- JPMS module system for shared and desktop modules
- Android persistence: Room database (primary) with `JsonToRoomMigration` from legacy FileSystem format and `LegacySqliteToRoomMigration` from pre-Room SQLite databases
- Android architecture: Single-Activity with Jetpack Compose, Navigation Compose for routing (sealed class `Screen`), `AppViewModel` as central state holder
- Room schema version: `AppDatabase` version 10 with `fallbackToDestructiveMigration()`; schema exported to `android/schemas/` for migration testing
- Android build flavors: `lollipop` (API 23), `oreo` (API 26), `tiramisu` (API 33) with core library desugaring
- Bidirectional sync via SyncController merging client and server data by ID
- Emotion strength clamped to [-10, +10] range with validation in model
- Circuit breaker pattern in API client for resilient network communication
- DiaryReference pattern for reusable diary descriptions with case-insensitive matching
- FoodTag pattern for normalized food items with case-insensitive matching (similar to DiaryReference)
- Encrypted backup/restore via .hez file format with password-based encryption and selective RestoreOptions
- ModuleVisibility for per-user UI section toggling (all visible by default)
- RequestIdFilter for CSRF/replay protection on state-changing API requests
- Version masking via `ApiClient.maskBugfixVersion()` for client-server compatibility checks
- Emergency Plan: step-based crisis intervention with ordered questions and associated actions (phone numbers for crisis contacts); currently Android-only with shared models/interfaces
- ActivityLog: tracks activities with persons, location, time range, and optional activity description; supports duration calculation with midnight crossing; available across all platforms

**Library documentation:** When adding or updating third-party libraries, their name, version, and license must be documented in the Project legal (info) page of the respective project part (website, homepage, desktop, android).

**Default branch:** `the-one` (not `main` or `master`)

## Docker Deployment

`compose.yml` orchestrates the full stack:

- **database** — MariaDB 11 with persistent volume
- **webserver** — Spring Boot app with MariaDB profile, connected to database
- **website** — SvelteKit frontend (nginx), exposed on port 8080 (configurable via `WEBSITE_PORT`)
- **homepage** — SvelteKit static site (nginx), exposed on port 8081 (configurable via `HOMEPAGE_PORT`)
- Nginx inline config routes `/api/` to webserver, serves website with SPA routing

## Translations

The project uses a custom YAML-based localization system implemented in the `shared` module via the `Localizer` class (`shared/src/main/java/de/idrinth/habitevaluator/shared/localization/Localizer.java`).

**Translation files** are YAML files stored on the classpath at `localization/{lang}.yml`:
- Production: `shared/src/main/resources/localization/en.yml`, `de.yml`, `es.yml`, `fr.yml`
- Test fixtures: `shared/src/test/resources/test-localization/en.yml`, `de.yml`

**Supported languages:** English (en), German (de), Spanish (es), French (fr)

**File format** — top-level keys are module names, nested keys are translation keys:

```yaml
general:
  app_name: "Habit Evaluator"
  submit: "Submit"
habits:
  add_button: "Add Habit"
login:
  title: "Habit Evaluator - Login"
```

Current modules: `general`, `login`, `habits`, `evaluation`, `categories`, `scoring`, `settings`, `defaults`, `reminders`, `first_start`, `sync`, `default_data`.

**Resolution chain** — `translate(module, key, language)` looks up translations in this order:
1. `module.key` in the requested language
2. `module.key` in English (fallback)
3. `general.key` in the requested language
4. `general.key` in English
5. Literal string `"module.key"` if nothing matches

**Usage:**

```java
Localizer localizer = new Localizer();
localizer.translate("habits", "add_button");         // English (default)
localizer.translate("habits", "add_button", "de");    // German
```

**Adding a new language:** Create `shared/src/main/resources/localization/{lang}.yml` mirroring the structure of `en.yml`. The `Localizer` lazy-loads and caches language files on first access using `ConcurrentHashMap`. Register the language in `crowdin.yml` for translation management.

**Adding new keys:** Add the key under the appropriate module section in `en.yml` (and any other language files). Add corresponding test entries in the test fixture files and update `LocalizerTest` if needed.

**Android** uses its own native string resources (`android/src/main/res/values/strings.xml` and `values-{lang}/`) rather than the shared `Localizer`. The SvelteKit `website` module has its own `i18n.ts` client-side localization system. The `homepage` does not currently have an i18n system.
