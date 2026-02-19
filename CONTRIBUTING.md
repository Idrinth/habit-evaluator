# Contributing to habit-evaluator

Thank you for your interest in contributing to habit-evaluator! This document covers development setup, architecture, and conventions.

## Prerequisites

- **Java 17** (Eclipse Temurin recommended)
- **Gradle 9.3.1** (included via wrapper — use `./gradlew`)
- **Node.js 22** (for the website and homepage modules)
- **Android SDK 35** (only if working on the Android module)
- **Git**

## Getting Started

1. Fork the repository and clone your fork.
2. Create a feature branch from `the-one`:
   ```bash
   git checkout -b your-feature-name the-one
   ```
3. Build the project:
   ```bash
   ./gradlew build
   ```

## Project Structure

The project is a Gradle multi-module build:

| Module | Technology | Description |
|--------|------------|-------------|
| `shared` | Java 17, JPMS | Core models, services, repository interfaces, localisation, and backup logic shared by all platforms |
| `webserver` | Spring Boot 4.0.2, Spring Data JPA | REST API with session-based authentication and Spring Security |
| `desktop` | JavaFX 21.0.2, H2 | Standalone desktop application with local database storage |
| `android` | Android SDK 35, SQLite | Native Android app with three build flavours (lollipop/oreo/tiramisu) |
| `website` | SvelteKit 2.0, TypeScript | Frontend web application |
| `homepage` | SvelteKit 2.0, TypeScript | Static homepage and documentation site |
| `api-bench` | Node.js, idrinth-api-bench | API performance benchmarking suite |

## Architecture

### Layered Design

The project follows a layered architecture with a shared core:

- **Models** (JPA entities) and **Services** (stateless business logic) live in `shared`
- Each platform provides its own **Repository** implementation and **UI/Controller** layer
- `shared` also contains FileSystem and InMemory repository implementations for local persistence

### Repository Implementations by Platform

| Interface (shared) | Webserver | Desktop | Android |
|---------------------|-----------|---------|---------|
| `*Repository` | `Database*Repository` (Spring Data JPA) | `H2*Repository` (Hibernate + H2) | `SQLite*Repository` |

### Authentication & Security

- Session-based authentication with BCrypt password encoding
- `RequestIdFilter` requires an `X-Request-ID` header (UUID v4) on state-changing HTTP methods (POST, PUT, DELETE, PATCH) for CSRF and replay attack prevention
- Magic links for read-only shared access with optional expiration and filtering
- Public endpoints: `/api/auth/**`, `/api/shared/**`, `/api/version`

### REST API

The webserver exposes a full REST API. Key endpoint groups:

| Endpoint group | Description |
|----------------|-------------|
| `/api/auth/*` | Login, logout, current user |
| `/api/habits/*` | Habit CRUD, entries, evaluation, scoring, predictions |
| `/api/categories` | Category management |
| `/api/diary/*` | Diary entries, suggestions, statistics |
| `/api/sleep-entries/*` | Sleep entry management and statistics |
| `/api/emotions/*` | Emotion pairs and graph data |
| `/api/food-logs/*` | Food logging with tag-based suggestions |
| `/api/sport-logs/*` | Sport activity logging, graphs, and statistics |
| `/api/medications/*` | Medication and dose log management |
| `/api/meetings` | Meeting entry management |
| `/api/stats/*` | Dashboard, daily timeline, correlations, scatter plots |
| `/api/export/pdf` | PDF report generation |
| `/api/sync` | Bidirectional data synchronisation |
| `/api/backup` | Encrypted backup download and restore |
| `/api/magic-links` | Shareable read-only link management |

### Database

- **Development / Desktop:** H2 (file-based, console at `/h2-console`)
- **Production:** MariaDB (activate with `--spring.profiles.active=mariadb`, configured via `MARIADB_URL`, `MARIADB_USERNAME`, `MARIADB_PASSWORD` environment variables)
- All entities use UUID string IDs (`@Id @Column(length = 36)`)

### Localisation

The `shared` module provides a YAML-based localisation system (`Localizer` class). Translation files live at `shared/src/main/resources/localization/{lang}.yml`. Supported languages: English, German, Spanish, French.

Android uses its own native string resources (`values/strings.xml`, `values-{lang}/`). The website has its own `i18n.ts` system.

New translations can be contributed via [Crowdin](https://crowdin.com/project/habit-evaluator).

## Building

```bash
# Build all Java modules
./gradlew build

# Run the webserver locally (H2 database)
./gradlew :webserver:bootRun

# Run the webserver with MariaDB
./gradlew :webserver:bootRun --args='--spring.profiles.active=mariadb'

# Run the desktop app
./gradlew :desktop:run

# Build Android APK (debug)
./gradlew :android:assembleOreoDebug

# Package desktop as .deb (Linux)
./gradlew :desktop:jpackageDeb

# Package desktop as .exe (Windows)
./gradlew :desktop:jpackageExe

# Docker Compose (full stack)
docker compose up --build
```

### Website and Homepage

```bash
cd website
npm ci
npm run dev    # start dev server
npm run check  # type-check
npm run build  # production build
```

```bash
cd homepage
npm ci
npm run dev
npm run check
```

## Running Tests

### Java

```bash
# Run all Java tests
./gradlew test

# Run tests with coverage report
./gradlew test jacocoTestReport
```

Tests use JUnit 5 and live under each module's `src/test/java` directory. Desktop tests use shared base classes (`H2RepositoryTestBase`, `JavaFXControllerTestBase`). Android tests use JUnit 4 with Mockito.

### Website

```bash
cd website
npm test           # unit tests (Vitest)
npm run test:e2e   # end-to-end tests (Cypress)
```

### Android UI Tests

Maestro UI test files at `android/.maestro/` run on an Android emulator in CI.

## Deployment

- **Docker Compose** orchestrates the full stack: MariaDB, Spring Boot webserver, SvelteKit website, and static homepage behind nginx
- **Dockerfiles** use two-stage builds (build + runtime) for each service
- **CI/CD** runs via GitHub Actions (`.github/workflows/ci.yml`) with coverage reporting to Coveralls

## Code Conventions

### Naming

- **Packages:** `de.idrinth.habitevaluator.[module].[layer]`
- **Entities:** PascalCase (`Habit`, `HabitEntry`, `DiaryEntry`)
- **Services:** `*Service` suffix (`HabitEvaluatorService`, `DiaryService`)
- **Repositories:** `*Repository` interface, `Database*Repository` (webserver), `H2*Repository` (desktop), `SQLite*Repository` (Android), `FileSystem*Repository` / `InMemory*Repository` (shared)
- **Controllers:** `*Controller` suffix
- **Fragments (Android):** `*Fragment` suffix
- **Adapters (Android):** `*Adapter` suffix
- **Methods:** camelCase

### General

- UUID string IDs (36 characters) for all entities
- `@JsonIgnore` on sensitive fields (e.g. `User.password`)
- JPMS module system for `shared` and `desktop`
- Descriptive test method names (e.g. `testEvaluateWithNoEntries`)
- Add tests for new functionality wherever possible

### Third-Party Libraries

When adding or updating third-party libraries, document their name, version, and license in the legal/imprint page of the affected module.

## Submitting Changes

1. Make sure `./gradlew build` passes.
2. If you changed the website or homepage, make sure `npm run check` and `npm run build` pass in the respective directory.
3. Write clear, descriptive commit messages.
4. Open a pull request against the `the-one` branch.
5. Describe what your change does and why in the PR description.

## Reporting Issues

Open an issue on GitHub. Please include:

- A clear description of the problem or suggestion.
- Steps to reproduce (for bugs).
- Which module is affected (shared, webserver, desktop, android, website, or homepage).

## License

By contributing you agree that your contributions will be licensed under the [MIT License](LICENSE).
