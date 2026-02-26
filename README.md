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
- Configurable daily entry limits and optional notes per completion
- Translatable habit names and descriptions per language

### Evaluation & Scoring

- Completion rate, current streak, and longest streak for any date range
- On-track detection at 80 % or above completion rate
- Avoidance streaks for negative habits (consecutive days without completions)
- Configurable point thresholds (0/1/2/4/8) per scoring rule
- Weekly score aggregation across all habits and per category
- Score prediction based on your current pace (sub-day precision)
- Point development charts (weekly and monthly)

### Diary / Journal

- Log events with significance levels (minor, normal, major) and optional time ranges
- Reusable diary descriptions with auto-complete suggestions
- Daily, weekly, and monthly point aggregation
- Weekly averages, monthly trends, and daily averages

### Sleep Tracking

- Log sleep sessions with start and end times (handles midnight crossing)
- Overlap detection to prevent duplicate entries
- Weekly and monthly statistics (average, minimum, maximum hours)
- Sleep duration and distribution visualisations

### Emotion Tracking

- Define custom emotion pairs (e.g. sad–happy, anxious–calm)
- Record measurements on a −10 to +10 scale with optional notes
- Line chart and scatter plot visualisations of trends over time
- Time-of-day scatter analysis

### Food Logging

- Log meals with calorie and carbohydrate tracking
- Tag-based food item organisation with auto-complete suggestions
- Food distribution statistics

### Sport Logging

- Log sport activities with duration, measurement, and units
- 30-day activity graph per sport (duration, measurement, count)
- Weekly and monthly statistics per activity
- Auto-complete suggestions for sport names and units

### Medication Tracking

- Track medications with dosage and provision type (pill, liquid drops, liquid ml)
- Optional Wikipedia link per medication
- Log individual doses with timestamps and notes

### Meeting Tracking

- Log meetings with place, attendants, and time range
- Auto-complete suggestions for attendants and places

### Activity Logging

- Track activities with persons, location, time range, and optional description
- Organise activities into groups
- Person tags with case-insensitive auto-complete and deduplication
- Duration calculation with midnight-crossing support

### Day Planner

- Define planner groups (e.g. "fitness", "creative projects") and activities
- Assign groups to weekly time slots by day-of-week, hour, and duration
- Random activity suggestions via two-step group-then-activity selection
- Confirm or deny suggested activities with confirmation rate tracking
- Week overview showing filled hours versus total available hours
- Notification-based reminders with inline confirm/deny actions (Android)

### Emergency Plan (Android)

- Step-based crisis intervention with ordered questions
- Associated actions per step with optional phone numbers for crisis contacts

### Statistics & Dashboard

- 30-day dashboard with habit points, diary points, and sleep data
- Daily activity timeline with category colour coding
- Cross-data event correlations across habits, diary, sleep, and emotions (time-weighted Pearson, past year)
- Emotion scatter plots by time of day
- Food distribution statistics

### PDF Export

- Generate comprehensive reports with selective sections
- Custom date range selection
- Include habits, sleep, diary, emotions, and correlations

### Sharing

- Magic links for read-only access with optional category filtering, date ranges, and expiration

### Backup & Restore

- Password-encrypted backups (.hez format)
- Selective restore — choose which data types to import (categories, habits, diary, sleep, sport, food, emotions, meetings, medication, reminders)
- Cross-platform backup compatibility

### Synchronisation

- Bidirectional sync with a self-hosted web server
- Conflict resolution and entry deduplication
- Circuit breaker for resilient network communication
- Version compatibility checks between client and server

### Internationalisation

- English, German, Spanish, and French
- Custom habit name and description translations per language
- Community translations via [Crowdin](https://crowdin.com/project/habit-evaluator)

### Customisation

- Dark mode, light mode, and system default themes
- Configurable scoring rules and thresholds
- Module visibility toggles to show only the sections you use
- Configurable reminders for sleep, diary, and emotion tracking with waking-hours support

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
