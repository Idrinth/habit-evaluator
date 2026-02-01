# habit-evaluator
A habit tracker focussed on analysing and evaluating data without leaking it anywhere.

## Features

### Habit Management
- Create, update, and delete habits
- Organise habits into user-defined categories with colour coding
- Track habit completions with optional notes and configurable value/weight
- Support for daily, weekly, and monthly frequency types

### Evaluation & Scoring
- Evaluate habit performance over custom date ranges, current week, or current month
- Completion rate calculation (percentage of target met)
- Streak tracking (current and longest)
- Configurable scoring rules with 0/1/2/4/8 point thresholds
- Weekly score aggregation across all habits and per category

### Sharing
- Magic links for read-only shared data access with optional time-based and category-based filtering and token expiration

### Multi-Platform Support
- **Web** — Spring Boot REST API with Svelte frontend
- **Desktop** — JavaFX application with local H2 database storage
- **Android** — Native app with RecyclerView-based UI

### Privacy
- All data stays under your control — no automatic cloud uploads
- Desktop app stores everything locally in an H2 database
- Data is only shared when you explicitly create a magic link

## Technical Details

### Authentication
- Session-based authentication with BCrypt password encoding
- HTML login page served via Thymeleaf

### REST API
- Full CRUD endpoints for habits (`/api/habits`)
- Habit entry creation and evaluation endpoints
- Authentication endpoints (`/api/auth/login`, `/api/auth/logout`, `/api/auth/me`)
- Magic link management (`/api/magic-links`) and public shared data access (`/api/shared/{token}`)

### Database
- H2 for development and local/desktop use
- MariaDB support for production (configured via environment variables)
- H2 console available at `/h2-console` in development

### Deployment
- Two-stage Dockerfile for the webserver (Eclipse Temurin JDK 17 build, JRE 17 runtime)
- GitHub Actions CI workflow for automated builds
- Gradle multi-module build (`shared`, `webserver`, `desktop`, `android`)

### Testing
- Unit tests for all shared models and services (JUnit 5)
- Spring Boot integration test for application context loading
