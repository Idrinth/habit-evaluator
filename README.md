# habit-evaluator
[![Crowdin](https://badges.crowdin.net/habit-evaluator/localized.svg)](https://crowdin.com/project/habit-evaluator)
A habit tracker focussed on analysing and evaluating data without leaking it anywhere.

## Features

### Habit Management
- Create, update, and delete habits
- Organise habits into user-defined categories with colour coding
- Track habit completions with optional notes and configurable value/weight
- Support for daily, weekly, and monthly frequency types
- Configurable daily limits per habit
- Positive and negative habit tracking
- Alphabetical sorting of habits and categories

### Evaluation & Scoring
- Evaluate habit performance over custom date ranges, current week, or current month
- Completion rate calculation (percentage of target met)
- Streak tracking (current and longest)
- On-track detection (≥80% completion rate)
- Configurable scoring rules with 0/1/2/4/8 point thresholds, customisable during habit creation
- Weekly score aggregation across all habits and per category
- Score prediction based on current pace
- Point development charts (weekly and monthly)

### Diary / Journal
- Log personal events and activities with date selection
- Event significance levels: minor, normal, and major
- Event description suggestions based on previous entries
- Daily, weekly, and monthly point aggregation and trend analysis

### Sleep Tracking
- Log sleep sessions with date, start time, and end time
- Overlap detection to prevent duplicate entries
- Optional notes per sleep session
- Statistics: daily duration, weekly and monthly averages, minimums, and maximums
- 30-day sleep duration visualisation

### Emotion Tracking
- Define custom emotion pairs (e.g. sad–happy, anxious–calm)
- Record emotion measurements on a scale over time
- Line chart visualisation of emotion trends
- Daily and overall emotion averages
- Event correlation analysis between emotions, habits, diary entries, and sleep

### Statistics & Dashboard
- 30-day statistics dashboard with habit points, diary points, sleep duration, and sleep entry counts
- Daily activity timeline showing when habits were completed
- Colour-coded habit visualisation by category
- Correlation report across all tracked data types

### PDF Export
- Generate comprehensive PDF reports
- Selective export: choose which sections to include (habits, sleep, diary, emotions, correlations)
- Custom date range selection

### Sharing
- Magic links for read-only shared data access with optional time-based and category-based filtering and token expiration

### Backup & Restore
- Encrypted daily backups with password protection
- 30-day backup retention
- Restore from backup or merge backup data with existing data
- Cross-platform backup compatibility (desktop and Android)

### Synchronisation
- Bidirectional sync with a remote web server
- Conflict resolution and entry deduplication
- Circuit breaker with cooldown to prevent sync spam

### Multi-Platform Support
- **Web** — Spring Boot REST API with Svelte frontend
- **Desktop** — JavaFX application with local H2 database storage
- **Android** — Native app with RecyclerView-based UI

### Internationalisation
- English, German, Spanish, and French
- Custom habit name translations

### Customisation
- Dark mode, light mode, and system default theme selection
- Custom scoring rule thresholds
- Default data initialisation with sample habits and categories

### Privacy
- All data stays under your control — no automatic cloud uploads
- Desktop app stores everything locally in an H2 database
- Data is only shared when you explicitly create a magic link
- HTTPS enforcement for remote server connections

## Technical Details

### Authentication
- Session-based authentication with BCrypt password encoding
- HTML login page served via Thymeleaf

### REST API
- Full CRUD endpoints for habits (`/api/habits`), categories (`/api/categories`), and scoring rules (`/api/score-rules`)
- Habit entry creation, evaluation, score prediction, and point development endpoints
- Diary entry management with description suggestions and statistics (`/api/diary`)
- Sleep entry management with statistics (`/api/sleep-entries`)
- Emotion pair management and graph data (`/api/emotions/pairs`, `/api/emotions/graph`)
- Statistics dashboard, daily timeline, and correlation analysis (`/api/stats/*`)
- Bidirectional synchronisation (`/api/sync`)
- PDF export (`/api/export/pdf`)
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
