# Habit Evaluator

A privacy-focused, multi-platform habit tracker for analyzing and evaluating habit performance data without leaking it anywhere.

**Group ID:** `de.idrinth.habitevaluator`
**Version:** 0.1.0-SNAPSHOT
**License:** MIT (Bjorn Buttner)
**Java:** 17

## Project Structure

```
habit-evaluator/
├── shared/              # Core models and services shared across platforms
├── webserver/           # Spring Boot web application (REST API + Thymeleaf)
├── desktop/             # JavaFX desktop application
├── android/             # Android mobile application
├── build.gradle         # Root build configuration
├── settings.gradle      # Module definitions
├── gradle.properties    # Build properties and version pins
└── gradle/wrapper/      # Gradle wrapper
```

## Modules

### shared

Core library consumed by all platform modules. Contains:

- **Models:** `Habit`, `HabitEntry`, `HabitCategory`, `Evaluation`, `ScoringRule`, `WeeklyScore`, `HabitScore`, `FrequencyType` (enum: DAILY/WEEKLY/MONTHLY)
- **Services:** `HabitEvaluatorService` (streaks, completion rates), `HabitScoringService` (scoring with point thresholds 0/1/2/4/8)
- **Repository interface:** `HabitRepository`
- **Dependencies:** GSON 2.10.1, SLF4J 2.0.11, Jakarta Persistence API 3.1.0 (compile-only)

### webserver

Spring Boot 3.2.2 web application.

- **Entry point:** `de.idrinth.habitevaluator.webserver.HabitEvaluatorWebApplication`
- **Controller:** `HabitController` serving REST endpoints at `/api/habits`
- **Repository:** `DatabaseHabitRepository` using Spring Data JPA
- **Database:** H2 (dev, with console at `/h2-console`) or MariaDB (production via `mariadb` profile)
- **Dependencies:** Spring Boot Web, Data JPA, Validation, Thymeleaf, H2, MariaDB JDBC 3.3.2

**REST API:**

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/habits` | List all habits |
| GET | `/api/habits/{id}` | Get habit by ID |
| POST | `/api/habits` | Create habit |
| PUT | `/api/habits/{id}` | Update habit |
| DELETE | `/api/habits/{id}` | Delete habit |
| POST | `/api/habits/{id}/entries` | Add completion entry |
| GET | `/api/habits/{id}/evaluate` | Evaluate habit performance |

**Database profiles:**

- Default (H2): file-based at `./data/habit-evaluator`, user `sa`, DDL auto-update
- MariaDB: activate with `--spring.profiles.active=mariadb`, configured via `MARIADB_URL`, `MARIADB_USERNAME`, `MARIADB_PASSWORD` env vars

### desktop

JavaFX 21.0.2 application.

- **Entry point:** `de.idrinth.habitevaluator.desktop.HabitEvaluatorDesktopApp` (extends JavaFX Application)
- **Controller:** `MainController` for FXML-based UI
- **Repository:** `H2HabitRepository` with manual JPA EntityManager
- **Database:** H2 file-based at `~/.habit-evaluator/data`, configured via `persistence.xml`
- **UI:** FXML layout at `fxml/main.fxml`, styled with `css/styles.css`, window 800x600
- **Dependencies:** JavaFX (controls, FXML), Hibernate Core 6.4.2.Final, H2, Logback 1.4.14

### android

Android application (SDK 34, min SDK 26).

- **Entry point:** `de.idrinth.habitevaluator.android.MainActivity` with ViewBinding
- **UI:** RecyclerView habit list with `HabitAdapter`, CardView evaluation display
- **Layouts:** `activity_main.xml`, `item_habit.xml`
- **Dependencies:** AndroidX AppCompat 1.6.1, Material 1.11.0, ConstraintLayout, RecyclerView, CardView, Lifecycle (ViewModel/LiveData 2.7.0)

## Database Schema

- **habits:** id, name, description, categoryId, frequency_type, target_frequency, created_at
- **habit_entries:** id, habit_id, completed_at, notes, value

## Build System

Gradle multi-module build with parallel builds and caching enabled.

```bash
# Build all modules
./gradlew build

# Run webserver
./gradlew :webserver:bootRun

# Run desktop app
./gradlew :desktop:run

# Run tests
./gradlew test
```

## Testing

JUnit 5 (Jupiter 5.10.2). Test locations:

- `shared/src/test/java/` - `HabitEvaluatorServiceTest` (no-entries, full-completion, streak calculation)
- `webserver/src/test/java/` - `HabitEvaluatorWebApplicationTest` (Spring context load)

## Architecture

Layered architecture with a shared core:

- **Models** (entities) and **Services** (business logic) live in `shared`
- Each platform module provides its own **Repository** implementation and **UI/Controller** layer
- Webserver uses Spring DI; desktop and android use manual dependency management
