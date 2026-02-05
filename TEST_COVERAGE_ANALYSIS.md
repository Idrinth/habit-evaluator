# Test Coverage Analysis

## Current State

The project has **161 source files** across 5 modules but only **17 test files**, all concentrated in the `shared` and `webserver` modules. There is no code coverage tooling (e.g. JaCoCo) configured.

### Coverage by Module

| Module | Source Files | Test Files | Gap |
|--------|-------------|------------|-----|
| shared | 50 | 16 | 68% untested |
| webserver | 38 | 1 | 97% untested |
| desktop | 20 | 0 | 100% untested |
| android | 35 | 0 | 100% untested |
| website | 18 | 0 | 100% untested |

### What IS Tested

The existing tests (~155 test methods) cover:

- **Models** (9/20): Evaluation, Habit, HabitCategory, HabitEntry, HabitScore, MagicLink, ScoringRule, User, WeeklyScore
- **Services** (3/6): HabitEvaluatorService, HabitScoringService (including predictions), SleepEvaluationService
- **API** (1/7): CircuitBreaker
- **Localization** (1/1): Localizer + translation completeness
- **Webserver** (1/38): Application context smoke test

The tested code is well-covered with thorough edge case testing, time-dependent behavior mocking, and parameterized tests.

---

## Recommended Improvements

### Priority 1: Shared Module — Untested Models and Services

These are pure Java classes with no UI or framework dependencies, making them the easiest and highest-value targets.

**Untested models (11 files):**
- `EmotionEntry` — emotion tracking data model
- `EmotionPair` — paired emotion measurements
- `DiaryEntry` — diary/journal data model
- `SleepEntry` — sleep tracking data model
- `SleepStats` — aggregated sleep statistics
- `PredictedWeeklyScore` — prediction result model
- `PredictedHabitScore` — per-habit prediction model
- `EventCorrelation` — correlation between events
- `EventSignificance` — significance classification
- `EmotionStrengthFormatter` — formatting utility
- `FrequencyType` — enum for habit frequency

These models likely follow the same patterns as the tested ones (constructors, getters/setters, equals/hashCode). Adding tests would be straightforward by following the existing model test patterns.

**Untested services (3 files):**
- `EventCorrelationService` — computes correlations between habit events; likely contains non-trivial logic
- `DiaryService` — diary entry management
- `DefaultDataInitializer` — sets up default data for new users

**Untested backup package (5 files):**
- `BackupService` — backup creation and restoration logic
- `BackupEncryptionService` — encryption/decryption of backups
- `BackupData` — backup data container
- `BackupException` — custom exception
- `MergeResult` — result of merging backup data

The backup and encryption services are particularly important to test since data loss or corruption during backup/restore operations would be a serious issue.

**Untested API classes (6 files):**
- `ApiClient` — HTTP client for remote API calls
- `SyncService` — data synchronization logic
- `SyncData` — sync payload model
- `RemoteHabitRepository` — remote data access
- `RemoteUserRepository` — remote user data access
- `StorageConfig` — storage configuration

The `SyncService` is high-priority since synchronization bugs can cause data loss or duplication.

### Priority 2: Webserver Controller Tests

The webserver has 12 REST controllers with zero endpoint-level tests. Only a single context-loading smoke test exists. Spring Boot's `@WebMvcTest` and `MockMvc` make controller testing straightforward.

**High-value controller tests to add:**
- `AuthController` — authentication is security-critical; test login flows, token validation, and unauthorized access
- `HabitController` — core CRUD operations; test request validation, response formats, and error handling
- `SyncController` — data sync endpoint; test conflict resolution and data integrity
- `MagicLinkController` — magic link authentication; test link generation, expiration, and redemption
- `PdfExportController` — test PDF generation and download

**Security configuration test:**
- `SecurityConfig` — verify that endpoints require proper authentication, CORS is configured correctly, and CSRF protection is appropriate

**Repository integration tests:**
- The webserver has 10 `Database*Repository` adapter classes wrapping JPA repositories. Integration tests using `@DataJpaTest` with an H2 in-memory database would verify that queries work correctly.

### Priority 3: Website Frontend Tests

The SvelteKit website has no testing framework configured at all. Setting up Vitest (which integrates natively with Vite/SvelteKit) would enable:

**Utility/library tests:**
- `src/lib/api.ts` — test API client functions, error handling, and request formatting
- `src/lib/i18n.ts` — test internationalization logic

**Component tests (using @testing-library/svelte):**
- Login page — test form validation and submission
- Habit add/edit pages — test form behavior and validation
- Stats page — test data display logic

### Priority 4: Desktop Persistence Tests

The desktop module has 8 `H2*Repository` classes that implement data persistence using Hibernate and H2. These can be tested without a JavaFX runtime:

- `H2HabitRepository`, `H2UserRepository`, `H2HabitCategoryRepository` — test CRUD operations
- `PersistenceManager` — test database initialization and schema creation

The desktop controllers are tightly coupled to JavaFX and harder to unit test without a TestFX setup, so persistence tests provide the best return on investment.

### Priority 5: Android Tests

Android testing requires the Android test framework. Prioritize:

- **Persistence layer** (`FileSystem*Repository`, `InMemory*Repository`) — these classes do file I/O and can be tested with standard JUnit + temp directories
- **UI adapter logic** — test data binding and formatting in adapters

---

## Infrastructure Recommendations

1. **Add JaCoCo for coverage reporting** — Configure the JaCoCo Gradle plugin in `build.gradle` to generate coverage reports. This enables tracking coverage metrics over time and can be integrated into CI.

2. **Add Vitest to the website module** — Add `vitest` and `@testing-library/svelte` to the website's `package.json` devDependencies and create a `vitest.config.ts`.

3. **Add coverage gates to CI** — Once coverage tooling is in place, add minimum coverage thresholds to prevent regression.

4. **Establish test conventions** — The existing shared module tests follow good patterns. Document these as the standard for new tests (e.g., test class naming, assertion style, test method naming).
