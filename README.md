# My Reminders — Android App

A reminder app inspired by Samsung Reminder, with one critical addition that's missing from the original: **per-reminder Quiet Hours** that automatically suppress notifications during your sleep window (default: 11 PM – 10 AM).

## The headline feature

Your hourly water-intake reminder no longer wakes you up at 2 AM.

When a reminder is configured with quiet hours enabled (e.g. start = 23:00, end = 10:00):

- If the next scheduled fire time falls inside the quiet window, the alarm is shifted forward to the moment quiet hours end.
- A second check runs at fire time itself (in case you edited the window after scheduling), so a reminder that somehow slipped through is silently dropped instead of buzzing.
- Repeating reminders keep ticking — they just skip the silent hours and resume in the morning.

The logic supports wrap-around windows like 23 → 10 (where the window crosses midnight), which is the normal sleep pattern.

## Features

| Feature | Status |
|---|---|
| Create / edit / delete reminders | ✅ |
| One-time and repeating reminders (30 min – 24 hr presets) | ✅ |
| **Per-reminder quiet hours window** | ✅ |
| Global default quiet hours in Settings | ✅ |
| Categories (General / Health / Work / Personal / Shopping) | ✅ |
| Priority levels (Low / Normal / High) | ✅ |
| Notifications with Snooze (10 min) and Done actions | ✅ |
| Survives device reboot (BootReceiver re-arms alarms) | ✅ |
| Android 12+ exact-alarm permission handling | ✅ |
| Android 13+ POST_NOTIFICATIONS runtime permission | ✅ |
| Material 3 UI | ✅ |
| Room (SQLite) persistence | ✅ |

## Project structure

```
app/src/main/java/com/example/reminderapp/
├── data/
│   ├── Reminder.kt              ← Room entity (includes quietStartHour, quietEndHour)
│   ├── ReminderDao.kt
│   ├── ReminderDatabase.kt
│   └── ReminderRepository.kt
├── notification/
│   ├── AlarmScheduler.kt        ← Schedules alarms, applies quiet-hours shift
│   └── NotificationHelper.kt    ← Builds notifications with Snooze/Done actions
├── receiver/
│   ├── ReminderAlarmReceiver.kt ← Fires when alarm triggers; re-checks quiet hours
│   ├── BootReceiver.kt          ← Reschedules everything after reboot
│   └── NotificationActionReceiver.kt
├── ui/
│   ├── MainActivity.kt
│   ├── AddEditReminderActivity.kt
│   ├── SettingsActivity.kt
│   ├── ReminderAdapter.kt
│   └── ReminderViewModel.kt
├── utils/
│   └── QuietHours.kt            ← The brain of the feature
└── ReminderApplication.kt
```

## How to build and install

### 1. Open in Android Studio

1. Install Android Studio (Hedgehog 2023.1.1 or newer).
2. Choose **File → Open** and select the `ReminderApp` folder.
3. Android Studio will prompt to download the Gradle wrapper and SDK components — let it.
4. Once Gradle sync finishes, click ▶ Run (or `Shift+F10`).

### 2. Or build the APK from the command line

From the project root:

```bash
# Generate the Gradle wrapper script (only needed first time)
gradle wrapper

# Build a debug APK
./gradlew assembleDebug

# The APK lands at:
#   app/build/outputs/apk/debug/app-debug.apk
```

Install on your phone (with USB debugging enabled):

```bash
./gradlew installDebug
```

### 3. Permissions to enable on first launch

- **Notifications** — requested automatically on Android 13+.
- **Exact alarms** — on Android 12+, open Settings → Apps → My Reminders → "Alarms & reminders" and enable. Without this, alarms still fire but may be slightly delayed.
- **Battery optimization** — for the most reliable hourly delivery, disable battery optimization for the app (Settings → Battery → Battery optimization → My Reminders → Don't optimize).

## How to set up your water-intake reminder

1. Tap the ➕ floating button.
2. Title: `Drink water`
3. Description: `Stay hydrated`
4. Pick the first time (e.g. 10:00 AM today).
5. Repeat: `Every 1 hour`.
6. Category: `Health`. Priority: `Normal`.
7. **Quiet hours: ON**. From: `11:00 PM`. Until: `10:00 AM`.
8. Save.

The reminder will fire every hour from 10 AM to 11 PM, then go silent until 10 AM the next morning — automatically, forever.

## Tested on

- Android 14 (API 34) — Pixel 7 emulator
- Android 12 (API 31) — exact-alarm permission flow
- Android 9 (API 28) — minimum target

## Why not use Samsung's app?

Samsung Reminder doesn't expose a quiet-window per reminder. You can only mute device-wide via Do Not Disturb, which also silences everything else (calls, alarms, messaging). This app gives you fine-grained, per-reminder control.
