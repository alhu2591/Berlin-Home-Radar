# Berlin Home Radar

Berlin Home Radar is an offline-first Android app that aggregates Berlin housing listings from multiple source adapters, deduplicates them, persists them locally with Room, and refreshes in the background with WorkManager.

## Features

- Offline-first listings backed by Room
- Multiple isolated source adapters (`bundled-json`, `bundled-html`, optional remote adapter)
- Deduplication by `source + externalId`, then fallback heuristics
- Manual refresh and periodic background sync
- Compose Material 3 UI
- Hilt dependency injection
- DataStore-backed user settings
- Favorites and filters
- Clean Architecture (`presentation`, `domain`, `data`)

## Tech stack

- Kotlin
- Jetpack Compose + Material 3
- Coroutines + Flow
- Hilt
- Room
- DataStore
- WorkManager
- Navigation Compose
- Retrofit
- Jsoup
- Coil

## Setup

1. Open the project in Android Studio Iguana / Koala or newer.
2. Let Gradle sync.
3. Run the `app` configuration on an emulator or device with minSdk 26+.
4. The app works out of the box using bundled demo listing sources.
5. Optional: enable the remote source in **Settings** and point it at your own feed implementation later.

## Architecture

- `presentation`: Compose UI, navigation, ViewModels, UI state
- `domain`: pure Kotlin models, repository contract, use cases
- `data`: Room, DataStore, source adapters, sync worker, repository implementation

## Background sync note

Android does not guarantee 5-minute periodic execution. The app uses `WorkManager` with the platform-safe minimum periodic interval of 15 minutes. The UI explains this constraint and always offers manual refresh.

## No secrets

This project contains no API keys or hardcoded secrets. The optional remote source adapter is disabled by default.

## Production notes

- Room migrations are declared for schema version 1.
- Network failures never erase local data.
- Sync status is persisted and surfaced in UI.
- Source adapters are isolated and easy to extend.


## CI

This project includes a GitHub Actions workflow at `.github/workflows/android-build.yml` that builds the debug APK with JDK 21 and uploads it as an artifact.


## Added in this updated build

- Arabic, English, and German localization with system-follow default and manual override
- Theme mode: system, light, dark
- Professional filters: favorites, query, rooms, area, max price, district, source, Jobcenter, Wohngeld, WBS
- Sync interval selection: manual, 15 min, 30 min, 1 hour, 3 hours
- Catalog of major Berlin housing sources, with automated sync clearly marked only for supported adapters

## Production extras included

- Debug network security configuration that trusts user-installed certificates in debug builds, which helps when testing behind corporate proxies or local inspection tools.
- Friendly sync error messages instead of exposing raw SSL exceptions in the UI.
- Release workflow for APK/AAB output via GitHub Actions.
- Optional signing via GitHub secrets:
  - `ANDROID_KEYSTORE_BASE64`
  - `ANDROID_KEYSTORE_PASSWORD`
  - `ANDROID_KEY_ALIAS`
  - `ANDROID_KEY_PASSWORD`
- First-run onboarding screen.
- Unit tests for deduplication and sync interval mapping.
- Pluggable telemetry interfaces (`AnalyticsLogger`, `CrashReporter`) with logcat implementations by default.

## Optional Firebase setup

This project does **not** force Firebase so the repository continues to build without a `google-services.json` file.
If you want real production crash reporting and analytics, wire the telemetry interfaces to Firebase Crashlytics and Analytics after adding Firebase configuration for your app. Firebase's official Android setup requires adding Firebase to the project and then adding the Crashlytics SDK and, for breadcrumb logs, enabling Analytics. citeturn459038search0turn459038search8
