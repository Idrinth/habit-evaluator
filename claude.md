# Habit Evaluator

A privacy-focused, multi-platform habit tracker for analyzing and evaluating habit performance data without leaking it anywhere.

**Group ID:** `de.idrinth.habitevaluator`
**Version:** 0.1.0-SNAPSHOT
**License:** MIT (Bjorn Buttner)
**Java:** 17
**Build System:** Gradle 8.5 (wrapper)

## Project Structure

```
habit-evaluator/
├── shared/              # Core models, services, repositories, localization
├── webserver/           # Spring Boot 3.2.2 web application (REST API + Thymeleaf)
├── desktop/             # JavaFX 21.0.2 desktop application
├── android/             # Android (SDK 34, min 26) mobile application
├── .github/workflows/   # CI/CD (GitHub Actions)
├── build.gradle         # Root build configuration
├── settings.gradle      # Module definitions
├── gradle.properties    # Version pins (Java 17, Spring Boot 3.2.2, JavaFX 21.0.2)
└── gradle/wrapper/      # Gradle 8.5 wrapper
```

## Modules

### shared

Core library consumed by all platform modules. Uses JPMS (`module-info.java`) and opens model package to Hibernate for reflection.

- **Models:** `Habit`, `HabitEntry`, `User`, `HabitCategory`, `Evaluation`, `ScoringRule`, `WeeklyScore`, `HabitScore`, `MagicLink`, `FrequencyType` (enum: DAILY/WEEKLY/MONTHLY)
- **Services:** `HabitEvaluatorService` (streaks, completion rates, on-track detection), `HabitScoringService` (point scoring with thresholds 0/1/2/4/8, weekly aggregation, predicted scores)
- **Repositories (interfaces):** `HabitRepository`, `UserRepository`, `HabitCategoryRepository`, `ScoringRuleRepository`, `MagicLinkRepository`
- **Localization:** `Localizer` service — YAML-based i18n loaded from classpath `localization/{lang}.yml`. Resolution: module.key (requested lang) -> module.key (English) -> general.key (requested lang) -> general.key (English) -> literal fallback. Uses concurrent caching.
- **API client:** `ApiClient`, `RemoteHabitRepository`, `StorageConfig` for client-server communication
- **Dependencies:** GSON 2.10.1, SLF4J 2.0.11, SnakeYAML 2.2, Jakarta Persistence API 3.1.0 (compile-only), Jackson Annotations 2.16.1 (compile-only)

### webserver

Spring Boot 3.2.2 application with Spring Security, Spring Data JPA, Thymeleaf.

- **Entry point:** `de.idrinth.habitevaluator.webserver.HabitEvaluatorWebApplication`
- **Controllers:** `HabitController`, `AuthController`, `MagicLinkController`, `DefaultDataController`, `PageController`
- **Repositories:** `DatabaseHabitRepository`, `DatabaseUserRepository`, `DatabaseMagicLinkRepository` (wrapping Spring Data JPA interfaces)
- **Configuration:** `SecurityConfig` (BCrypt, session-based auth), `AppConfig`, `DataInitializer`
- **DTOs:** `LoginRequest`, `LoginResponse`
- **Templates:** Thymeleaf pages in `src/main/resources/templates/` (login, add-habit, track-habits, add-category, score-rule-add)
- **Database:** H2 (dev, console at `/h2-console`) or MariaDB (production via `mariadb` profile)
- **Artifact:** `habit-evaluator-web.jar`
- **Docker:** `webserver/Dockerfile` — two-stage build (Eclipse Temurin JDK 17 build, JRE 17 runtime), exposes port 8080

### desktop

JavaFX 21.0.2 application with JPMS module system.

- **Entry point:** `de.idrinth.habitevaluator.desktop.HabitEvaluatorDesktopApp`
- **Controllers:** `MainController`, `SettingsDialogController`
- **Persistence:** `H2HabitRepository`, `H2HabitCategoryRepository`, `H2UserRepository`, `PersistenceManager`
- **UI:** FXML layouts (`main.fxml`, `settings.fxml`), CSS (`styles.css`), window 800x600
- **Database:** H2 file-based at `~/.habit-evaluator/data`, configured via `persistence.xml`
- **Dependencies:** JavaFX (controls, FXML), Hibernate Core 6.4.2, H2, Logback 1.4.14

### android

Android application (SDK 34, min 26).

- **Namespace:** `de.idrinth.habitevaluator.android`
- **Activities:** `MainActivity` (launcher), `TrackHabitsActivity`, `SettingsActivity`
- **UI:** RecyclerView with `HabitAdapter` and `TrackHabitAdapter`, CardView, Material Design 3
- **Permissions:** INTERNET
- **Dependencies:** AndroidX AppCompat 1.6.1, Material 1.11.0, ConstraintLayout, RecyclerView, CardView, Lifecycle (ViewModel/LiveData 2.7.0)

## REST API

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/habits` | List user's habits |
| GET | `/api/habits/{id}` | Get habit by ID |
| POST | `/api/habits` | Create habit |
| PUT | `/api/habits/{id}` | Update habit |
| DELETE | `/api/habits/{id}` | Delete habit |
| POST | `/api/habits/{id}/entries` | Add completion entry |
| GET | `/api/habits/{id}/evaluate` | Evaluate habit (optional date range) |
| GET | `/api/habits/predict` | Predict weekly scores |
| POST | `/api/auth/login` | Login |
| POST | `/api/auth/logout` | Logout |
| GET | `/api/auth/me` | Get current user |
| GET | `/api/magic-links` | Magic link management |
| GET | `/api/shared/{token}` | Public shared data access |

## Database Schema

All entities use UUID string IDs (`@Id @Column(length = 36)`).

- **users** — id, username (unique), password (BCrypt), email (unique), created_at
- **habits** — id, name, description, categoryId, frequency_type (ENUM), target_frequency, created_at, scoring_rule_id (FK), user_id (FK)
- **habit_entries** — id, habit_id (FK), completed_at, notes, value
- **habit_categories** — id, name (unique), description, color
- **scoring_rules** — id, name, threshold_for_1_point, threshold_for_2_points, threshold_for_4_points, threshold_for_8_points, user_id (FK)
- **magic_links** — id, token (unique), user_id (FK), categoryId, filterStart, filterEnd, created_at, expiresAt

**Unique constraints:** Habit (name + categoryId + userId), User (username)
**Cascade:** User -> habits (delete), Habit -> entries (delete)

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

# Docker build (webserver only)
docker build -t habit-evaluator:latest -f webserver/Dockerfile .
```

## Testing

**Framework:** JUnit 5 (Jupiter 5.10.2)

**Test locations:**
- `shared/src/test/java/` — Unit tests for models and services:
  - `HabitEvaluatorServiceTest` (15 tests: evaluation, streaks, frequency types)
  - `HabitScoringServiceTest` (score calculation, weekly aggregation)
  - `HabitScoringServicePredictionTest` (predicted score extrapolation)
  - `LocalizerTest` (localization loading and translation)
  - Model tests: `HabitTest`, `HabitEntryTest`, `UserTest`, `HabitCategoryTest`, `ScoringRuleTest`, `WeeklyScoreTest`, `HabitScoreTest`, `EvaluationTest`, `MagicLinkTest`
- `webserver/src/test/java/` — `HabitEvaluatorWebApplicationTest` (Spring Boot context load)
- `shared/src/test/resources/localization/` — German and English YAML test fixtures

**Conventions:**
- Descriptive method names (e.g. `testEvaluateWithNoEntries`, `testStreakCalculation`)
- `@BeforeEach` setup methods for test fixtures
- Standard JUnit assertions (`assertEquals`, `assertTrue`, `assertFalse`)

## CI/CD

GitHub Actions workflow at `.github/workflows/build.yml`:
- Triggers on push/PR to `the-one` branch
- Steps: checkout -> setup JDK 17 (Temurin) -> setup Gradle -> `./gradlew build`

## Architecture & Conventions

**Layered architecture with shared core:**
- Models (JPA entities) and Services (stateless business logic) live in `shared`
- Each platform provides its own Repository implementation and UI/Controller layer
- Webserver uses Spring DI; desktop and android use manual dependency management

**Package naming:** `de.idrinth.habitevaluator.[module].[layer]`

**Naming patterns:**
- Entities: PascalCase (`Habit`, `HabitEntry`, `User`)
- Services: `*Service` (`HabitEvaluatorService`, `HabitScoringService`)
- Repositories: `*Repository` (interface), `Database*Repository` (Spring Data wrapper), `H2*Repository` (desktop)
- Controllers: `*Controller` (`HabitController`, `AuthController`, `MainController`)
- Methods: camelCase (`evaluate`, `calculateTargetEntries`, `isExpired`)

**Key patterns:**
- Repository pattern with interface-based abstraction and multiple implementations
- UUID string IDs for all entities
- `@JsonIgnore` on sensitive fields (User.password, User.habits)
- Lazy loading for JPA relationships
- JPMS module system for shared library

**Default branch:** `the-one` (not `main` or `master`)
