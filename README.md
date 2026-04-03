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
