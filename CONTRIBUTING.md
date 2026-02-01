# Contributing to habit-evaluator

Thank you for your interest in contributing to habit-evaluator! This document explains how to get started.

## Prerequisites

- **Java 17** (Eclipse Temurin recommended)
- **Node.js 22** (for the website module)
- **Android SDK 34** (only if working on the Android module)
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

The project is a Gradle multi-module build with five modules:

| Module | Description |
|--------|-------------|
| `shared` | Core models, services, and repository interfaces shared by all platforms |
| `webserver` | Spring Boot REST API |
| `desktop` | JavaFX desktop application |
| `android` | Native Android application |
| `website` | SvelteKit frontend |

## Building

```bash
# Build all Java modules
./gradlew build

# Build only the website
cd website && npm ci && npm run build

# Run the webserver locally (H2 database)
./gradlew :webserver:bootRun

# Run the desktop app
./gradlew :desktop:run
```

## Running Tests

```bash
# Run all tests
./gradlew test
```

Tests use JUnit 5 and live under each module's `src/test/java` directory. Please add tests for new functionality wherever possible.

## Website Development

The `website` module uses SvelteKit with TypeScript:

```bash
cd website
npm ci
npm run dev    # start dev server
npm run check  # type-check
npm run build  # production build
```

## Submitting Changes

1. Make sure `./gradlew build` passes.
2. If you changed the website, make sure `npm run check` and `npm run build` pass in the `website` directory.
3. Write clear, descriptive commit messages.
4. Open a pull request against the `the-one` branch.
5. Describe what your change does and why in the PR description.

## Reporting Issues

Open an issue on GitHub. Please include:

- A clear description of the problem or suggestion.
- Steps to reproduce (for bugs).
- Which module is affected (shared, webserver, desktop, android, or website).

## Code Conventions

- **Java:** Standard Java naming conventions. Services use a `*Service` suffix, repositories use `*Repository`, controllers use `*Controller`.
- **Entities:** Use UUID string IDs (36 characters).
- **TypeScript/Svelte:** Follow the existing style in the `website` module.
- **Testing:** Use descriptive test method names that explain the scenario being tested.

## License

By contributing you agree that your contributions will be licensed under the [MIT License](LICENSE).
