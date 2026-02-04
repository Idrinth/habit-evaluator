# Privacy & Data Protection

This document describes how Habit Evaluator handles your data on Android.

## Overview

Habit Evaluator is a local-first habit tracking application. By default, all data stays on your device. No data is collected, transmitted, or shared without your explicit action.

## Data Collected

The app stores the following data that you provide:

| Data Type | Description |
|-----------|-------------|
| Habits | Name, description, frequency, target, category, scoring rules |
| Habit entries | Completion timestamps, optional notes, values |
| Diary entries | Description, event date, significance level |
| Sleep entries | Date, sleep/wake times, optional notes |
| Emotional states | Custom emotion pairs, strength ratings, timestamps, optional notes |
| Categories | Name, description, colour |
| Settings | Theme, language, storage mode, backup password |
| Account info | Username, user ID (local placeholder by default) |

## Data Storage

### Local Files

All user data is stored as JSON files in the app's private internal storage directory (`/data/data/<package>/files/habit-data/`). Files include:

- `habits.json`
- `habit_categories.json`
- `diary_entries.json`
- `sleep_entries.json`
- `emotion_pairs.json`
- `emotion_entries.json`

These files are only accessible to the app itself, protected by Android's sandboxing.

### Shared Preferences

App settings (theme, language, storage mode, API credentials if remote mode is enabled) are stored in Android SharedPreferences under `habit_evaluator_settings`.

### Encrypted Backups

When a backup password is configured, the app creates daily encrypted backups stored locally in the app's internal storage. Up to 30 backups are retained.

Encryption details:

- Algorithm: AES-256-GCM
- Key derivation: PBKDF2WithHmacSHA256 with 210,000 iterations
- Random 16-byte salt and 12-byte IV per backup
- 128-bit GCM authentication tag

## Network & Remote Sync

### Default Behaviour

By default, the app operates in **local-only mode**. No network requests are made and no data leaves your device.

### Optional Remote Sync

You may optionally configure a remote server in settings. When enabled:

- The app authenticates with username and password via `POST /api/auth/login`
- Habits and categories are synced bidirectionally with the configured server
- A session cookie (JSESSIONID) is used for authenticated requests
- If the server is unreachable, the app falls back to local storage with a 5-minute circuit breaker cooldown

Remote sync is entirely opt-in and can be disabled at any time by switching back to local storage mode.

## Permissions

The app requests a single permission:

| Permission | Purpose |
|------------|---------|
| `INTERNET` | Required only for optional remote server sync. Not used in local-only mode. |

The app does **not** request access to your camera, microphone, location, contacts, calendar, or any other sensitive data.

## Third-Party Services

The app uses **no** third-party services:

- No analytics or telemetry
- No crash reporting
- No advertising SDKs
- No social media SDKs
- No Firebase or Google services

All dependencies are standard AndroidX UI libraries and open-source utilities (Gson, SnakeYAML, SLF4J with no-op logger).

## Android System Backup

The app's manifest sets `android:allowBackup="true"`, which means Android's built-in backup service may include app data in device backups. You can control this through your device's backup settings.

## Data Sharing

Data is only shared when you explicitly choose to:

- **Remote sync**: Only when you configure and enable a remote server
- **Magic links**: Only when you explicitly create a shareable link (with optional expiration)
- **Backups**: Stored locally and encrypted; never uploaded automatically

## Data Deletion

All data is stored within the app's private storage. You can delete all data by:

- Clearing app data through Android Settings
- Uninstalling the app

## Open Source

Habit Evaluator is open-source software licensed under the MIT License. The source code is publicly available for inspection at [https://github.com/Idrinth/habit-evaluator](https://github.com/Idrinth/habit-evaluator).
