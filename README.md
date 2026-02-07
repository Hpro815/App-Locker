# App Locker (Distance Unlock Prototype)

This is a starter Android Studio project for an app that locks a target app after a usage time limit and unlocks it only after walking/running a set distance. It includes:

- A setup screen to configure a single lock rule.
- A usage monitor service that checks `UsageStatsManager` for time-in-foreground.
- A distance unlock service that tracks traveled distance using Fused Location.

## How it works (prototype flow)

1. Configure the target package name, lock-after minutes, and unlock distance.
2. Start monitoring. The service polls usage stats every 15 seconds.
3. Once the app exceeds the time limit, the distance unlock service starts and tracks distance.
4. When the distance is reached, the lock is cleared.

## Next steps you should implement

- Add an accessibility overlay or a dedicated lock screen activity to block the target app while locked.
- Replace the single-rule storage with a database to support multiple locked apps.
- Promote the services to foreground services for reliability and user transparency.
- Add proper runtime permission requests for location and activity recognition.

## Running

Open the project in Android Studio, sync Gradle, and run on a device with location and usage access enabled.
