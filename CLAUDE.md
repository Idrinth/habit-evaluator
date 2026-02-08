# Habit Evaluator

A privacy-focused, multi-platform habit tracker for analyzing and evaluating habit performance data without leaking it anywhere.

**Group ID:** `de.idrinth.habitevaluator`
**Version:** 0.1.0-SNAPSHOT
**License:** MIT (Bjorn Buttner)
**Java:** 17
**Build System:** Gradle 8.5 (wrapper)

## Project Structure

```text
habit-evaluator/
├── shared/              # Core models, services, repositories, localization, backup
├── webserver/           # Spring Boot 3.2.2 web application (REST API)
├── desktop/             # JavaFX 21.0.2 desktop application
├── android/             # Android (SDK 35, min 26) mobile application
├── website/             # SvelteKit 2.0 frontend application
├── homepage/            # SvelteKit 2.0 static homepage/documentation site
├── .github/workflows/   # CI/CD (GitHub Actions)
├── build.gradle         # Root build configuration (JaCoCo coverage)
├── settings.gradle      # Module definitions (6 modules)
├── gradle.properties    # Version pins (Java 17, Spring Boot 3.2.2, JavaFX 21.0.2)
├── compose.yml          # Docker Compose (MariaDB + webserver + website + homepage + nginx)
├── crowdin.yml          # Crowdin localization configuration
├── CONTRIBUTING.md      # Contribution guidelines
└── gradle/wrapper/      # Gradle 8.5 wrapper
```

## Modules

### shared

Core library consumed by all platform modules. Uses JPMS (`module-info.java`) and opens model package to Hibernate for reflection and backup package to GSON for serialization.

- **Models:** `Habit`, `HabitEntry`, `User`, `HabitCategory`, `Evaluation`, `ScoringRule`, `WeeklyScore`, `HabitScore`, `PredictedWeeklyScore`, `PredictedHabitScore`, `MagicLink`, `FrequencyType` (enum: DAILY/WEEKLY/MONTHLY), `DiaryEntry`, `DiaryReference`, `EventSignificance` (enum: MINOR/NORMAL/MAJOR), `SleepEntry`, `SleepStats`, `EmotionEntry`, `EmotionPair`, `EmotionStrengthFormatter`, `EventCorrelation`, `ReminderSettings`
- **Services:** `HabitEvaluatorService` (streaks, completion rates, on-track detection, avoidance streaks for negative habits), `HabitScoringService` (point scoring with thresholds 0/1/2/4/8, weekly aggregation, predicted scores), `DiaryService` (day/week/month points, weekly averages, monthly trends, daily averages), `SleepEvaluationService` (sleep stats: avg/min/max hours, weekly/monthly aggregation, overlap detection), `EventCorrelationService` (time-weighted Pearson correlations with proximity boost across habits, diary, sleep, and emotions over past year; returns top 10 by absolute strength), `DefaultDataInitializer` (42 default habits, 10 categories, 10 emotion pairs with i18n)
- **Repositories (interfaces):** `HabitRepository`, `UserRepository`, `HabitCategoryRepository`, `ScoringRuleRepository`, `MagicLinkRepository`, `DiaryEntryRepository`, `DiaryReferenceRepository`, `SleepEntryRepository`, `EmotionEntryRepository`, `EmotionPairRepository`, `ReminderSettingsRepository`
- **Localization:** `Localizer` service — YAML-based i18n loaded from classpath `localization/{lang}.yml`. Resolution: module.key (requested lang) -> module.key (English) -> general.key (requested lang) -> general.key (English) -> literal fallback. Uses concurrent caching.
- **API client:** `ApiClient`, `RemoteHabitRepository`, `RemoteUserRepository`, `StorageConfig` (with ThemeMode enum: SYSTEM/LIGHT/DARK), `SyncData`, `SyncService`, `CircuitBreaker` for resilient client-server communication and bidirectional sync
- **Backup:** `BackupService`, `HezBackupService` (.hcz/.hez backup format), `BackupEncryptionService` (password-based encryption), `BackupData` (nested data structures for serialization), `BackupException`, `MergeResult`
- **Utilities:** `DateRangeUtils` (date range calculations), `PdfDataAggregator` (PDF report data assembly)
- **Dependencies:** GSON 2.10.1, SLF4J 2.0.11, SnakeYAML 2.2, Jakarta Persistence API 3.1.0 (compile-only), Jackson Annotations 2.16.1 (compile-only)

### webserver

Spring Boot 3.2.2 application with Spring Security, Spring Data JPA.

- **Entry point:** `de.idrinth.habitevaluator.webserver.HabitEvaluatorWebApplication`
- **Controllers:** `HabitController`, `AuthController`, `CategoryController`, `ScoringRuleController`, `MagicLinkController`, `DefaultDataController`, `SyncController`, `DiaryController`, `SleepEntryController`, `EmotionPairController`, `ReminderSettingsController`, `StatsController`, `PdfExportController`, `BackupController`
- **Repositories:** 11 `Database*Repository` wrappers (`DatabaseHabitRepository`, `DatabaseUserRepository`, `DatabaseHabitCategoryRepository`, `DatabaseScoringRuleRepository`, `DatabaseMagicLinkRepository`, `DatabaseDiaryEntryRepository`, `DatabaseDiaryReferenceRepository`, `DatabaseSleepEntryRepository`, `DatabaseEmotionEntryRepository`, `DatabaseEmotionPairRepository`, `DatabaseReminderSettingsRepository`) over 12 Spring Data JPA interfaces (`JpaHabitRepository`, `JpaHabitEntryRepository`, `JpaHabitCategoryRepository`, `JpaUserRepository`, `JpaScoringRuleRepository`, `JpaMagicLinkRepository`, `JpaDiaryEntryRepository`, `JpaDiaryReferenceRepository`, `JpaSleepEntryRepository`, `JpaEmotionEntryRepository`, `JpaEmotionPairRepository`, `JpaReminderSettingsRepository`)
- **Configuration:** `SecurityConfig` (BCrypt, session-based auth, CSRF disabled), `AppConfig` (service beans), `DataInitializer` (demo user: username "demo", password "demo123")
- **DTOs:** `LoginRequest`, `LoginResponse` (standalone); `CreateCategoryRequest`, `CreateScoringRuleRequest`, `MagicLinkRequest` (inner classes in controllers)
- **Database:** H2 (dev, console at `/h2-console`) or MariaDB (production via `mariadb` profile)
- **Artifact:** `habit-evaluator-web.jar`
- **Additional dependency:** OpenPDF 1.3.35 (PDF generation)
- **Docker:** `webserver/Dockerfile` — two-stage build (Eclipse Temurin JDK 17 build, JRE 17 runtime), exposes port 8080

### desktop

JavaFX 21.0.2 application with JPMS module system.

- **Entry point:** `de.idrinth.habitevaluator.desktop.HabitEvaluatorDesktopApp`
- **Controllers:** `MainController`, `AddHabitController`, `SettingsDialogController`, `SyncDialogController`, `StatsController`, `SleepTrackingController`, `EmotionEntryController`, `EmotionPairController`, `PdfExportController`, `ImprintController`
- **Services:** `ReminderService` (reminder and notification system)
- **Persistence:** `PersistenceManager`, `JpaTransactionHelper`, `H2HabitRepository`, `H2HabitCategoryRepository`, `H2UserRepository`, `H2DiaryEntryRepository`, `H2DiaryReferenceRepository`, `H2SleepEntryRepository`, `H2EmotionPairRepository`, `H2EmotionEntryRepository`
- **UI:** FXML layouts (`main.fxml`, `add-habit.fxml`, `settings.fxml`, `sync.fxml`, `stats.fxml`, `sleep-tracking.fxml`, `emotion-entry.fxml`, `emotion-pairs.fxml`, `pdf-export.fxml`, `imprint.fxml`), CSS (`styles.css`, `dark.css`)
- **Database:** H2 file-based at `~/.habit-evaluator/data`, configured via `persistence.xml`
- **Dependencies:** JavaFX (controls, FXML), Hibernate Core 6.4.2, H2, Logback 1.4.14, OpenPDF 1.3.35
- **Packaging:** `jpackageDeb` (Linux .deb), `jpackageExe` (Windows .exe) via jpackage tasks

### android

Android application (SDK 35, min 26).

- **Namespace:** `de.idrinth.habitevaluator.android`
- **Activities:** `MainActivity` (launcher with bottom navigation and fragment tabs), `SettingsActivity`, `PdfExportActivity`, `SleepAnalysisActivity`, `CorrelationActivity`
- **Fragments:** `HomeFragment`, `EditHabitsFragment`, `AddHabitFragment`, `DiaryFragment`, `SleepTrackingFragment`, `StatsFragment`, `PointDevelopmentFragment`, `EmotionalStateFragment`, `RecordEmotionEntryFragment`, `AddEmotionPairFragment`, `ImprintFragment`, `SettingsFragment`
- **UI Adapters:** `HabitAdapter`, `TrackHabitAdapter`, `EditHabitAdapter`, `DiaryEntryAdapter`, `SleepEntryAdapter`, `EmotionPairAdapter`, `EmotionDataAdapter`, `ScreenPagerAdapter`
- **Chart Views:** `PointChartView`, `SleepGraphView`, `EmotionLineChartView`, `EmotionScatterChartView` (custom Android views)
- **Utilities:** `FontSizeHelper`, `ViewPager2SwipeSensitivityReducer`, `GsonSerializers`
- **Reminders:** `ReminderScheduler`, `ReminderReceiver` (broadcast receiver for boot-completed and reminder intents)
- **Persistence (3 implementations):**
  - FileSystem (legacy, JSON-based via GSON): `FileSystemHabitRepository`, `FileSystemHabitCategoryRepository`, `FileSystemDiaryEntryRepository`, `FileSystemDiaryReferenceRepository`, `FileSystemSleepEntryRepository`, `FileSystemEmotionPairRepository`, `FileSystemEmotionEntryRepository`
  - SQLite (modern): `SQLiteHabitRepository`, `SQLiteHabitCategoryRepository`, `SQLiteDiaryEntryRepository`, `SQLiteDiaryReferenceRepository`, `SQLiteSleepEntryRepository`, `SQLiteEmotionPairRepository`, `SQLiteEmotionEntryRepository`, `SQLiteHelper`
  - In-memory: `InMemoryHabitRepository`, `InMemoryHabitCategoryRepository`
  - Migration: `JsonToSqliteMigration` (migrates from FileSystem to SQLite)
- **Permissions:** INTERNET, POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED, SCHEDULE_EXACT_ALARM (API ≤32)
- **Build Flavors:** `modern` (minSdk 26) and `legacy` (minSdk 21) with Core Library Desugaring
- **Dependencies:** AndroidX AppCompat 1.6.1, Material 1.11.0, ConstraintLayout 2.1.4, RecyclerView 1.3.2, CardView 1.0.0, ViewPager2 1.0.0, Lifecycle (ViewModel/LiveData 2.7.0), Core Library Desugaring 2.0.4, SLF4J no-op 2.0.11
- **Build:** ProGuard minification and resource shrinking in release builds, signing config for release APKs, AAB (Android App Bundle) support, JaCoCo coverage reporting for both flavors

### website

SvelteKit 2.0 frontend application with TypeScript.

- **Routes:** Login, habits (add/edit/home), categories (add), score-rules (add), diary, sleep, emotions graph, stats (dashboard, correlations), points, PDF export, backup, settings, imprint
- **API client:** `src/lib/api.ts` (463 lines, 40+ endpoints, circuit breaker pattern)
- **i18n:** `src/lib/i18n.ts` — client-side internationalization
- **Config:** `src/lib/config.ts` — runtime configuration with dynamic API URL
- **Build:** SvelteKit 2.50.1, Svelte 5.49.1, Vite 7.3.1, `svelte-check` for type checking, TypeScript 5.9.3 (strict mode)
- **Testing:** Vitest 4.0.18, Testing Library (Svelte, user-event), jsdom 28.0.0
- **Docker:** `website/Dockerfile` — Node.js 22 alpine build, nginx alpine runtime; `docker-entrypoint.sh` injects `API_BASE_URL` at runtime
- **Static adapter** with SPA fallback (`index.html`)

### homepage

SvelteKit 2.0 static homepage and documentation site.

- **Routes:** Landing page, features page, documentation pages (android, desktop, api, webserver), imprint
- **Build:** SvelteKit 2.50.1, Svelte 5.49.1, Vite 7.3.1, TypeScript 5.9.3
- **Prerendering:** All routes prerendered (`['*']`), strict mode enabled
- **Docker:** `homepage/Dockerfile` — Node.js 22 alpine build, nginx alpine runtime

## REST API

| Method | Path | Description |
| --- | --- | --- |
| POST | `/api/auth/login` | Login |
| POST | `/api/auth/logout` | Logout |
| GET | `/api/auth/me` | Get current user |
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
| GET | `/api/reminder-settings` | Get reminder settings |
| PUT | `/api/reminder-settings` | Update reminder settings |
| GET | `/api/stats/dashboard` | 30-day dashboard (habit points, diary points, sleep duration/entries) |
| GET | `/api/stats/daily-timeline` | 30-day daily timeline of habit entries with category colors |
| GET | `/api/stats/correlations` | Top event correlations across habits, diary, sleep, emotions |
| GET | `/api/stats/emotion-scatter` | Emotion scatter plot by time-of-day |
| GET | `/api/export/pdf` | PDF report (params: from, to, habits, sleep, diary, emotions flags) |
| POST | `/api/sync` | Bidirectional sync (merges client/server habits and entries) |
| POST | `/api/init-defaults` | Initialize default data for user (with language parameter) |
| GET | `/api/backup` | Download .hez backup file (password encrypted) |
| POST | `/api/backup` | Upload and restore .hez backup |

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

**Unique constraints:** Habit (name + categoryId + userId), User (username)
**Cascade:** User -> habits (delete), Habit -> entries (delete), User -> diary_entries (delete), User -> sleep_entries (delete)

**Database profiles:**
- Default (H2): file-based at `./data/habit-evaluator`, user `sa`, DDL auto-update
- MariaDB: activate with `--spring.profiles.active=mariadb`, configured via `MARIADB_URL`, `MARIADB_USERNAME`, `MARIADB_PASSWORD` env vars

## Authentication & Security

- Session-based with HttpSession and BCrypt password encoding
- Magic links for read-only shared access with optional expiration and category/time filtering
- Public endpoints: `/api/auth/**`, `/api/shared/**`, `/login`, `/h2-console`, `/score-rules/**`
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

# Build Android APK (debug, modern flavor)
./gradlew :android:assembleModernDebug

# Build Android APK (debug, legacy flavor)
./gradlew :android:assembleLegacyDebug

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

# Homepage development
cd homepage && npm install && npm run dev

# Homepage type checking
cd homepage && npm run check
```

## Outbound Proxy Configuration

This environment routes outbound traffic through an HTTP proxy. The `http_proxy` / `https_proxy` / `HTTP_PROXY` / `HTTPS_PROXY` environment variables are set automatically, but **Gradle's JVM does not pick them up**. You must pass proxy settings as JVM system properties when running any Gradle command that needs network access (dependency resolution, etc.).

**Extract the proxy host and port from the environment and pass them to Gradle:**

```bash
# Parse proxy URL from environment
PROXY_URL="${http_proxy:-$HTTP_PROXY}"
PROXY_HOST="$(echo "$PROXY_URL" | sed -E 's|https?://([^@]*@)?([^:]+):([0-9]+).*|\2|')"
PROXY_PORT="$(echo "$PROXY_URL" | sed -E 's|https?://([^@]*@)?([^:]+):([0-9]+).*|\3|')"

# Run Gradle with proxy system properties
./gradlew build \
  -Dhttp.proxyHost="$PROXY_HOST" -Dhttp.proxyPort="$PROXY_PORT" \
  -Dhttps.proxyHost="$PROXY_HOST" -Dhttps.proxyPort="$PROXY_PORT" \
  -Dhttp.nonProxyHosts="localhost|127.0.0.1"
```

If the proxy URL contains credentials (user:password@host:port format), also set `-Dhttp.proxyUser` / `-Dhttp.proxyPassword` and their `https.*` equivalents.

**npm / Node.js** tools (website, homepage) generally respect the environment variables automatically and need no extra configuration.

## Testing

**Java Framework:** JUnit 5 (Jupiter 5.10.2), JaCoCo 0.8.11 for coverage
**Frontend Framework:** Vitest 4.0.18, Testing Library (Svelte/user-event), jsdom

**Test locations:**
- `shared/src/test/java/` — 43 test classes:
  - **Service tests:** `HabitEvaluatorServiceTest`, `HabitScoringServiceTest`, `HabitScoringServicePredictionTest`, `DiaryServiceTest`, `SleepEvaluationServiceTest`, `EventCorrelationServiceTest`, `DefaultDataInitializerTest`
  - **Model tests:** `HabitTest`, `HabitEntryTest`, `UserTest`, `HabitCategoryTest`, `ScoringRuleTest`, `WeeklyScoreTest`, `HabitScoreTest`, `PredictedHabitScoreTest`, `PredictedWeeklyScoreTest`, `EvaluationTest`, `MagicLinkTest`, `FrequencyTypeTest`, `DiaryEntryTest`, `DiaryReferenceTest`, `SleepEntryTest`, `SleepStatsTest`, `EmotionEntryTest`, `EmotionPairTest`, `EmotionStrengthFormatterTest`, `EventSignificanceTest`, `EventCorrelationTest`, `ReminderSettingsTest`
  - **API/Sync tests:** `ApiClientTest`, `SyncDataTest`, `SyncServiceTest`, `StorageConfigTest`, `CircuitBreakerTest`
  - **Backup tests:** `BackupServiceTest`, `HezBackupServiceTest`, `BackupEncryptionServiceTest`, `BackupDataTest`, `BackupExceptionTest`, `MergeResultTest`
  - **Localization tests:** `LocalizerTest`, `TranslationCompletenessTest`
- `webserver/src/test/java/` — 17 test classes:
  - **Controller tests:** `AuthControllerTest`, `HabitControllerTest`, `CategoryControllerTest`, `DiaryControllerTest`, `SleepEntryControllerTest`, `EmotionPairControllerTest`, `ScoringRuleControllerTest`, `MagicLinkControllerTest`, `StatsControllerTest`, `SyncControllerTest`, `PdfExportControllerTest`, `DefaultDataControllerTest`, `SecurityConfigTest`
  - **Repository tests:** `JpaHabitRepositoryTest`, `JpaUserRepositoryTest`, `JpaDiaryEntryRepositoryTest`, `JpaMagicLinkRepositoryTest`
  - **Application test:** `HabitEvaluatorWebApplicationTest`
- `desktop/src/test/java/` — 8 test classes:
  - `H2RepositoryTestBase` (shared base), `H2HabitRepositoryTest`, `H2HabitCategoryRepositoryTest`, `H2UserRepositoryTest`, `H2DiaryEntryRepositoryTest`, `H2SleepEntryRepositoryTest`, `H2EmotionPairRepositoryTest`, `H2EmotionEntryRepositoryTest`
- `android/src/test/java/` — 9 test classes:
  - `FileSystemHabitRepositoryTest`, `FileSystemHabitCategoryRepositoryTest`, `FileSystemDiaryEntryRepositoryTest`, `FileSystemSleepEntryRepositoryTest`, `FileSystemEmotionPairRepositoryTest`, `FileSystemEmotionEntryRepositoryTest`, `InMemoryHabitRepositoryTest`, `InMemoryHabitCategoryRepositoryTest`, `ScreenPagerAdapterTest`
- `website/src/` — 6 test files:
  - `lib/api.test.ts`, `lib/config.test.ts`, `lib/i18n.test.ts`, `routes/login/login.test.ts`, `routes/diary/diary.test.ts`, `routes/categories/add/categories-add.test.ts`
- `shared/src/test/resources/test-localization/` — German and English YAML test fixtures

**Conventions:**
- Descriptive method names (e.g. `testEvaluateWithNoEntries`, `testStreakCalculation`)
- `@BeforeEach` setup methods for test fixtures
- Standard JUnit assertions (`assertEquals`, `assertTrue`, `assertFalse`)
- Webserver tests use `@ActiveProfiles("test")` with in-memory H2
- Desktop tests use shared `H2RepositoryTestBase` for JPA setup

## CI/CD

GitHub Actions workflows at `.github/workflows/`:

- **build.yml** — Primary build: triggers on push/PR to `the-one`, JDK 17 (Temurin), `./gradlew build`, JaCoCo coverage reports (shared + webserver), parallel jobs for desktop packaging (.deb) and desktop JAR
- **frontend.yml** — SvelteKit CI: push/PR to `the-one`, Node.js 22, `npm run check` and `npm run build` for both website and homepage; separate test job for website (`npm test` with coverage)
- **release.yml** — Release automation: artifact builds (Linux .deb, Windows .exe, Android APK for modern/legacy flavors, AAB), Docker builds to GHCR (webserver, website, homepage), signing with environment variables
- **docker.yml** — Docker image builds
- **coderabbit-retry.yml** — Code review automation

## Architecture & Conventions

**Layered architecture with shared core:**
- Models (JPA entities) and Services (stateless business logic) live in `shared`
- Each platform provides its own Repository implementation and UI/Controller layer
- Webserver uses Spring DI; desktop and Android use manual dependency management

**Package naming:** `de.idrinth.habitevaluator.[module].[layer]`

**Naming patterns:**
- Entities: PascalCase (`Habit`, `HabitEntry`, `User`, `DiaryEntry`, `DiaryReference`, `SleepEntry`, `EmotionEntry`, `EmotionPair`, `ReminderSettings`)
- Services: `*Service` (`HabitEvaluatorService`, `DiaryService`, `SleepEvaluationService`, `EventCorrelationService`, `BackupService`, `SyncService`, `ReminderService`)
- Repositories: `*Repository` (interface), `Database*Repository` (Spring Data wrapper), `H2*Repository` (desktop), `FileSystem*Repository` (Android legacy), `SQLite*Repository` (Android modern), `InMemory*Repository` (Android testing)
- Controllers: `*Controller` (`HabitController`, `AuthController`, `CategoryController`, `StatsController`, `PdfExportController`, `BackupController`, `ReminderSettingsController`)
- Fragments (Android): `*Fragment` (`DiaryFragment`, `SleepTrackingFragment`, `EditHabitsFragment`, `EmotionalStateFragment`, `StatsFragment`)
- Adapters (Android): `*Adapter` (`HabitAdapter`, `DiaryEntryAdapter`, `SleepEntryAdapter`, `EmotionPairAdapter`)
- Chart Views (Android): `*View` / `*ChartView` (`PointChartView`, `SleepGraphView`, `EmotionLineChartView`)
- Methods: camelCase (`evaluate`, `calculateTargetEntries`, `isExpired`)

**Key patterns:**
- Repository pattern with interface-based abstraction and multiple implementations per platform
- UUID string IDs for all entities
- `@JsonIgnore` on sensitive fields (User.password, User.habits)
- Lazy loading for JPA relationships
- JPMS module system for shared and desktop modules
- Android dual persistence: FileSystem (legacy JSON via GSON) and SQLite with `JsonToSqliteMigration`
- Android build flavors: `modern` (API 26) and `legacy` (API 21) with core library desugaring
- Bidirectional sync via SyncController merging client and server data by ID
- Emotion strength clamped to [-10, +10] range with validation in model
- Circuit breaker pattern in API client for resilient network communication
- DiaryReference pattern for reusable diary descriptions with case-insensitive matching
- Encrypted backup/restore via .hez file format with password-based encryption

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
