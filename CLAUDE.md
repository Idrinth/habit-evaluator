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
├── shared/              # Core models, services, repositories, localization
├── webserver/           # Spring Boot 3.2.2 web application (REST API)
├── desktop/             # JavaFX 21.0.2 desktop application
├── android/             # Android (SDK 35, min 26) mobile application
├── website/             # SvelteKit 2.0 frontend application
├── homepage/            # SvelteKit 2.0 static homepage/documentation site
├── .github/workflows/   # CI/CD (GitHub Actions)
├── build.gradle         # Root build configuration
├── settings.gradle      # Module definitions
├── gradle.properties    # Version pins (Java 17, Spring Boot 3.2.2, JavaFX 21.0.2)
├── compose.yml          # Docker Compose (MariaDB + webserver + website + homepage + nginx)
├── crowdin.yml          # Crowdin localization configuration
└── gradle/wrapper/      # Gradle 8.5 wrapper
```

## Modules

### shared

Core library consumed by all platform modules. Uses JPMS (`module-info.java`) and opens model package to Hibernate for reflection.

- **Models:** `Habit`, `HabitEntry`, `User`, `HabitCategory`, `Evaluation`, `ScoringRule`, `WeeklyScore`, `HabitScore`, `PredictedWeeklyScore`, `PredictedHabitScore`, `MagicLink`, `FrequencyType` (enum: DAILY/WEEKLY/MONTHLY), `DiaryEntry`, `EventSignificance` (enum: MINOR/NORMAL/MAJOR), `SleepEntry`, `SleepStats`, `EmotionEntry`, `EmotionPair`, `EventCorrelation`
- **Services:** `HabitEvaluatorService` (streaks, completion rates, on-track detection), `HabitScoringService` (point scoring with thresholds 0/1/2/4/8, weekly aggregation, predicted scores), `DiaryService` (day/week/month points, weekly averages, monthly trends), `SleepEvaluationService` (sleep stats: avg/min/max hours, weekly/monthly aggregation), `EventCorrelationService` (time-weighted Pearson correlations across habits, diary, sleep, and emotions over past year; returns top 10 by absolute strength), `DefaultDataInitializer`
- **Repositories (interfaces):** `HabitRepository`, `UserRepository`, `HabitCategoryRepository`, `ScoringRuleRepository`, `MagicLinkRepository`, `DiaryEntryRepository`, `SleepEntryRepository`, `EmotionEntryRepository`, `EmotionPairRepository`
- **Localization:** `Localizer` service — YAML-based i18n loaded from classpath `localization/{lang}.yml`. Resolution: module.key (requested lang) -> module.key (English) -> general.key (requested lang) -> general.key (English) -> literal fallback. Uses concurrent caching.
- **API client:** `ApiClient`, `RemoteHabitRepository`, `RemoteUserRepository`, `StorageConfig`, `SyncData` for client-server communication and bidirectional sync
- **Dependencies:** GSON 2.10.1, SLF4J 2.0.11, SnakeYAML 2.2, Jakarta Persistence API 3.1.0 (compile-only), Jackson Annotations 2.16.1 (compile-only)

### webserver

Spring Boot 3.2.2 application with Spring Security, Spring Data JPA.

- **Entry point:** `de.idrinth.habitevaluator.webserver.HabitEvaluatorWebApplication`
- **Controllers:** `HabitController`, `AuthController`, `CategoryController`, `ScoringRuleController`, `MagicLinkController`, `DefaultDataController`, `SyncController`, `DiaryController`, `SleepEntryController`, `EmotionPairController`, `StatsController`, `PdfExportController`
- **Repositories:** `DatabaseHabitRepository`, `DatabaseUserRepository`, `DatabaseMagicLinkRepository` (wrapping Spring Data JPA interfaces: `JpaUserRepository`, `JpaHabitRepository`, `JpaHabitEntryRepository`, `JpaMagicLinkRepository`)
- **Configuration:** `SecurityConfig` (BCrypt, session-based auth), `AppConfig`, `DataInitializer`
- **DTOs:** `LoginRequest`, `LoginResponse`, `CreateCategoryRequest`, `CreateScoringRuleRequest`
- **Database:** H2 (dev, console at `/h2-console`) or MariaDB (production via `mariadb` profile)
- **Artifact:** `habit-evaluator-web.jar`
- **Docker:** `webserver/Dockerfile` — two-stage build (Eclipse Temurin JDK 17 build, JRE 17 runtime), exposes port 8080

### desktop

JavaFX 21.0.2 application with JPMS module system.

- **Entry point:** `de.idrinth.habitevaluator.desktop.HabitEvaluatorDesktopApp`
- **Controllers:** `MainController`, `SettingsDialogController`
- **Persistence:** `H2HabitRepository`, `H2HabitCategoryRepository`, `H2UserRepository`, `PersistenceManager`
- **UI:** FXML layouts (`main.fxml`, `add-habit.fxml`, `settings.fxml`, `sync.fxml`), CSS (`styles.css`, `dark.css`), window 800x600
- **Database:** H2 file-based at `~/.habit-evaluator/data`, configured via `persistence.xml`
- **Dependencies:** JavaFX (controls, FXML), Hibernate Core 6.4.2, H2, Logback 1.4.14

### android

Android application (SDK 35, min 26).

- **Namespace:** `de.idrinth.habitevaluator.android`
- **Activities:** `MainActivity` (launcher with bottom navigation and fragment tabs), `SettingsActivity`, `PdfExportActivity`, `SleepAnalysisActivity`
- **Fragments:** `HomeFragment`, `EditHabitsFragment`, `AddHabitFragment`, `DiaryFragment`, `SleepTrackingFragment`, `StatsFragment`, `PointDevelopmentFragment`, `EmotionalStateFragment`, `RecordEmotionEntryFragment`, `AddEmotionPairFragment`, `ImprintFragment`, `SettingsFragment`
- **UI Adapters:** `HabitAdapter`, `TrackHabitAdapter`, `EditHabitAdapter`, `DiaryEntryAdapter`, `SleepEntryAdapter`, `ScreenPagerAdapter`
- **Persistence:** `FileSystemHabitRepository`, `FileSystemHabitCategoryRepository`, `FileSystemDiaryEntryRepository`, `FileSystemSleepEntryRepository` (JSON file-based storage using GSON)
- **Permissions:** INTERNET
- **Dependencies:** AndroidX AppCompat 1.6.1, Material 1.11.0, ConstraintLayout 2.1.4, RecyclerView 1.3.2, CardView 1.0.0, ViewPager2 1.0.0, Lifecycle (ViewModel/LiveData 2.7.0), SLF4J no-op 2.0.11
- **Build:** ProGuard minification and resource shrinking in release builds, signing config for release APKs, AAB (Android App Bundle) support

### website

SvelteKit 2.0 frontend application with TypeScript.

- **Routes:** Login, habits (add/edit/home), categories (add), score-rules (add), diary, sleep, emotions graph, stats dashboard, points, PDF export, imprint
- **API client:** `src/lib/api.ts`
- **Build:** Vite 7.3.1, `svelte-check` for type checking, TypeScript 5.9.3 (strict mode)
- **Docker:** `website/Dockerfile` — Node.js 22 alpine build, nginx alpine runtime

### homepage

SvelteKit 2.0 static homepage and documentation site.

- **Routes:** Features page, documentation pages (android, desktop, api, webserver), imprint
- **Build:** Same toolchain as website
- **Docker:** `homepage/Dockerfile` — Node.js 22 alpine build, nginx alpine runtime

## REST API

| Method | Path | Description |
| --- | --- | --- |
| GET | `/api/habits` | List user's habits |
| GET | `/api/habits/{id}` | Get habit by ID |
| POST | `/api/habits` | Create habit |
| PUT | `/api/habits/{id}` | Update habit |
| DELETE | `/api/habits/{id}` | Delete habit |
| POST | `/api/habits/{id}/entries` | Add completion entry |
| GET | `/api/habits/{id}/evaluate` | Evaluate habit (optional date range) |
| GET | `/api/habits/predict` | Predict weekly scores |
| GET | `/api/categories` | List user's categories |
| POST | `/api/categories` | Create category |
| POST | `/api/score-rules` | Create scoring rule (validates threshold ordering) |
| POST | `/api/sync` | Bidirectional sync (merges client/server habits and entries) |
| POST | `/api/auth/login` | Login |
| POST | `/api/auth/logout` | Logout |
| GET | `/api/auth/me` | Get current user |
| GET | `/api/magic-links` | Magic link management |
| GET | `/api/shared/{token}` | Public shared data access |
| GET | `/api/diary` | List user's diary entries |
| POST | `/api/diary` | Create diary entry |
| DELETE | `/api/diary/{id}` | Delete diary entry |
| GET | `/api/diary/stats` | Diary stats (today/week/month points, weekly average, monthly trend) |
| GET | `/api/sleep-entries` | List user's sleep entries |
| POST | `/api/sleep-entries` | Create sleep entry (validates no overlaps) |
| DELETE | `/api/sleep-entries/{id}` | Delete sleep entry |
| GET | `/api/sleep-entries/stats` | Sleep stats (weekly and monthly: avg/min/max hours) |
| GET | `/api/emotions/pairs` | List user's emotion pairs with labels |
| GET | `/api/emotions/graph` | Emotion graph data (daily averages, overall averages per pair) |
| GET | `/api/stats/dashboard` | 30-day dashboard (habit points, diary points, sleep duration/entries) |
| GET | `/api/stats/daily-timeline` | 30-day daily timeline of habit entries with category colors |
| GET | `/api/stats/correlations` | Top event correlations across habits, diary, sleep, emotions |
| GET | `/api/export/pdf` | PDF report (params: from, to, habits, sleep, diary, emotions flags) |

## Database Schema

All entities use UUID string IDs (`@Id @Column(length = 36)`).

- **users** — id, username (unique), password (BCrypt), email (unique), created_at
- **habits** — id, name, description, categoryId, frequency_type (ENUM), target_frequency, created_at, scoring_rule_id (FK), user_id (FK)
- **habit_entries** — id, habit_id (FK), completed_at, notes, value
- **habit_categories** — id, name (unique), description, color
- **scoring_rules** — id, name, threshold_for_1_point, threshold_for_2_points, threshold_for_4_points, threshold_for_8_points, user_id (FK)
- **magic_links** — id, token (unique), user_id (FK), categoryId, filterStart, filterEnd, created_at, expiresAt
- **diary_entries** — id, description, significance (ENUM: MINOR/NORMAL/MAJOR), event_date, created_at, user_id (FK)
- **sleep_entries** — id, from_time, until_time, date, created_at, notes, user_id (FK)
- **emotion_pairs** — id, negative_label, positive_label, user_id (FK)
- **emotion_entries** — id, emotion_pair_id (FK), strength (int, -10 to +10), recorded_at, notes (max 500), user_id (FK)

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

**HabitScoringService:**
- Fixed point scale: 0, 1, 2, 4, 8
- Configurable thresholds per user via ScoringRule entity (defaults: 1, 2, 4, 7)
- `calculateHabitScore()` — weekly completion count to points
- `calculateWeeklyScore()` — aggregates all habits into WeeklyScore
- `predictHabitScore()` — extrapolates completion rate using elapsed hours from week start
- `predictCurrentWeekScore()` — predicts all habits for current week

**DiaryService:**
- Points per entry based on EventSignificance: MINOR (1), NORMAL (2), MAJOR (4)
- `getDayPoints()` / `getCurrentWeekPoints()` / `getCurrentMonthPoints()` — period aggregations
- `getWeeklyAverageForMonth()` — average points per week for current month
- `getMonthlyTrend()` — trend comparison (current week vs. previous weeks)
- `getEntriesInRange()` / `getPointsInRange()` — date-range filtering

**SleepEvaluationService:**
- `calculateStats(entries, periodStart, periodEnd)` — SleepStats with avg/min/max hours and total entries
- `getCurrentWeekStats()` / `getCurrentMonthStats()` — convenience methods
- Sleep duration handles midnight crossing (e.g., 23:00 to 07:00)

**EventCorrelationService:**
- Calculates time-weighted Pearson correlations across habits, diary, sleep, and emotions over the past year (365 days)
- Creates daily signals for habit completions, diary points, sleep hours, and emotion pair averages
- Applies linear decay weights (newer entries weighted higher)
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

# Build Android APK (debug)
./gradlew :android:assembleDebug

# Build Android AAB (release bundle)
./gradlew :android:bundleRelease

# Docker Compose (full stack)
docker compose up --build

# Website development
cd website && npm install && npm run dev

# Website type checking
cd website && npm run check

# Homepage development
cd homepage && npm install && npm run dev

# Homepage type checking
cd homepage && npm run check
```

## Testing

**Framework:** JUnit 5 (Jupiter 5.10.2)

**Test locations:**
- `shared/src/test/java/` — Unit tests for models and services:
  - `HabitEvaluatorServiceTest` (15+ tests: evaluation, streaks, frequency types)
  - `HabitScoringServiceTest` (score calculation, weekly aggregation)
  - `HabitScoringServicePredictionTest` (predicted score extrapolation)
  - `LocalizerTest` (localization loading and translation)
  - `TranslationCompletenessTest` (i18n coverage verification)
  - Model tests: `HabitTest`, `HabitEntryTest`, `UserTest`, `HabitCategoryTest`, `ScoringRuleTest`, `WeeklyScoreTest`, `HabitScoreTest`, `EvaluationTest`, `MagicLinkTest`
- `webserver/src/test/java/` — `HabitEvaluatorWebApplicationTest` (Spring Boot context load)
- `shared/src/test/resources/localization/` — German and English YAML test fixtures

**Conventions:**
- Descriptive method names (e.g. `testEvaluateWithNoEntries`, `testStreakCalculation`)
- `@BeforeEach` setup methods for test fixtures
- Standard JUnit assertions (`assertEquals`, `assertTrue`, `assertFalse`)

## CI/CD

GitHub Actions workflows at `.github/workflows/`:

- **build.yml** — Primary build: triggers on push/PR to `the-one`, JDK 17 (Temurin), `./gradlew build`, parallel jobs for desktop packaging (.deb, .exe) and Android builds (APK, AAB with signing secrets)
- **frontend.yml** — SvelteKit CI: push/PR to `the-one`, Node.js 22, `npm run check` and `npm run build` for both website and homepage
- **release.yml** — Release automation: Docker builds to GHCR (webserver, website, homepage), artifact uploads (JAR, DEB, EXE, APK, AAB)
- **docker.yml** — Docker image builds
- **coderabbit-retry.yml** — Code review automation

## Architecture & Conventions

**Layered architecture with shared core:**
- Models (JPA entities) and Services (stateless business logic) live in `shared`
- Each platform provides its own Repository implementation and UI/Controller layer
- Webserver uses Spring DI; desktop and Android use manual dependency management

**Package naming:** `de.idrinth.habitevaluator.[module].[layer]`

**Naming patterns:**
- Entities: PascalCase (`Habit`, `HabitEntry`, `User`, `DiaryEntry`, `SleepEntry`, `EmotionEntry`, `EmotionPair`)
- Services: `*Service` (`HabitEvaluatorService`, `DiaryService`, `SleepEvaluationService`, `EventCorrelationService`)
- Repositories: `*Repository` (interface), `Database*Repository` (Spring Data wrapper), `H2*Repository` (desktop), `FileSystem*Repository` (Android)
- Controllers: `*Controller` (`HabitController`, `AuthController`, `CategoryController`, `StatsController`, `PdfExportController`)
- Fragments (Android): `*Fragment` (`DiaryFragment`, `SleepTrackingFragment`, `EditHabitsFragment`, `EmotionalStateFragment`, `StatsFragment`)
- Adapters (Android): `*Adapter` (`HabitAdapter`, `DiaryEntryAdapter`, `SleepEntryAdapter`)
- Methods: camelCase (`evaluate`, `calculateTargetEntries`, `isExpired`)

**Key patterns:**
- Repository pattern with interface-based abstraction and multiple implementations per platform
- UUID string IDs for all entities
- `@JsonIgnore` on sensitive fields (User.password, User.habits)
- Lazy loading for JPA relationships
- JPMS module system for shared and desktop modules
- Android file-based persistence using GSON with thread-safe ConcurrentHashMap
- Bidirectional sync via SyncController merging client and server data by ID
- Emotion strength clamped to [-10, +10] range with validation in model

**Library documentation:** When adding or updating third-party libraries, their name, version, and license must be documented in the Project legal (info) page of the respective project part (website, homepage, desktop, android).

**Default branch:** `the-one` (not `main` or `master`)

## Docker Deployment

`compose.yml` orchestrates the full stack:

- **database** — MariaDB 11 with persistent volume
- **webserver** — Spring Boot app with MariaDB profile, connected to database
- **website** — SvelteKit frontend (nginx), exposed on port 8080
- **homepage** — SvelteKit static site (nginx), exposed on port 8081
- Nginx reverse proxy routes `/api/` to webserver, serves website with SPA routing

## Translations

The project uses a custom YAML-based localization system implemented in the `shared` module via the `Localizer` class (`shared/src/main/java/de/idrinth/habitevaluator/shared/localization/Localizer.java`).

**Translation files** are YAML files stored on the classpath at `localization/{lang}.yml`:
- Production: `shared/src/main/resources/localization/en.yml`, `de.yml`, `es.yml`, `fr.yml`
- Test fixtures: `shared/src/test/resources/localization/en.yml`, `de.yml`

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

Current modules: `general`, `login`, `habits`, `evaluation`, `categories`, `scoring`, `settings`.

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

**Android** uses its own native string resources (`android/src/main/res/values/strings.xml` and `values-{lang}/`) rather than the shared `Localizer`. The SvelteKit-based `website` and `homepage` subprojects do not currently have an i18n system.
