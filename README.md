# habit-evaluator

[![Crowdin](https://badges.crowdin.net/habit-evaluator/localized.svg)](https://crowdin.com/project/habit-evaluator) [![Coverage Status](https://coveralls.io/repos/github/Idrinth/habit-evaluator/badge.svg?branch=the-one)](https://coveralls.io/github/Idrinth/habit-evaluator?branch=the-one)

A privacy-focused, multi-platform habit tracker for analysing and evaluating your personal data — without leaking it anywhere.

Track habits, sleep, emotions, diary entries, food, sport, and medication across web, desktop, and Android. All data stays under your control.

## Why habit-evaluator?

Most habit trackers upload your data to third-party servers. habit-evaluator keeps everything local by default. You decide if and when to sync, share, or export your data.

- **Privacy first** — no automatic cloud uploads, no tracking, no ads
- **Your data, your way** — local storage on every platform, optional self-hosted sync
- **Deep analysis** — go beyond simple checkboxes with scoring, streaks, correlations, and trend analysis
- **Multi-platform** — use on web, desktop, or Android with the same feature set

## Platforms

| Platform | Description |
|----------|-------------|
| **Web** | Self-hosted web application with a Svelte frontend |
| **Desktop** | Standalone desktop application with local database storage |
| **Android** | Native Android app available in three SDK flavours |

## Features

### Habit Management

- Create, update, and delete habits with categories and colour coding
- Daily, weekly, and monthly frequency types
- Positive and negative habit tracking (build good habits or break bad ones)
- Configurable daily limits and optional notes per completion

### Evaluation & Scoring

- Completion rate, current streak, and longest streak for any date range
- On-track detection at 80% or above completion rate
- Configurable point thresholds (0/1/2/4/8) per scoring rule
- Weekly score aggregation across all habits and per category
- Score prediction based on your current pace
- Point development charts (weekly and monthly)

### Diary / Journal

- Log events and activities with significance levels (minor, normal, major)
- Suggestions based on previous entries
- Daily, weekly, and monthly point aggregation and trend analysis

### Sleep Tracking

- Log sleep sessions with start and end times
- Overlap detection to prevent duplicate entries
- Weekly and monthly statistics (average, minimum, maximum hours)
- 30-day sleep duration visualisation

### Emotion Tracking

- Define custom emotion pairs (e.g. sad–happy, anxious–calm)
- Record measurements on a -10 to +10 scale
- Line chart visualisation of trends over time
- Correlation analysis between emotions, habits, diary entries, and sleep

### Food & Sport Logging

- Log meals with calorie and carbohydrate tracking
- Tag-based food item organisation with suggestions
- Log sport activities with duration, measurement, and units
- Weekly and monthly sport statistics per activity

### Medication Tracking

- Track medications with dosage and provision type (pill, liquid drops, liquid ml)
- Log individual doses with timestamps and notes

### Statistics & Dashboard

- 30-day dashboard with habit points, diary points, and sleep data
- Daily activity timeline with category colour coding
- Cross-data correlation reports (habits, diary, sleep, emotions)
- Emotion scatter plots by time of day

### PDF Export

- Generate comprehensive reports with selective sections
- Custom date range selection
- Include habits, sleep, diary, emotions, and correlations

### Sharing

- Magic links for read-only access with optional category filtering, date ranges, and expiration

### Backup & Restore

- Password-encrypted backups (.hez format)
- Selective restore — choose which data types to import
- Cross-platform backup compatibility

### Synchronisation

- Bidirectional sync with a self-hosted web server
- Conflict resolution and entry deduplication
- Circuit breaker to prevent sync spam

### Internationalisation

- English, German, Spanish, and French
- Custom habit name translations per language
- Community translations via [Crowdin](https://crowdin.com/project/habit-evaluator)

### Customisation

- Dark mode, light mode, and system default themes
- Configurable scoring rules and thresholds
- Module visibility toggles to show only what you use
- Configurable reminders for sleep, diary, and emotion tracking

## Getting Started

### Docker (recommended for web)

```bash
docker compose up --build
```

This starts the full stack: database, web server, website, and homepage.

### Desktop

```bash
./gradlew :desktop:run
```

### Android

Build a debug APK for your target SDK level:

```bash
./gradlew :android:assembleOreoDebug
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development setup, build instructions, architecture details, and coding conventions.

## License

[MIT](LICENSE)
