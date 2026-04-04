# Continuous Improvement Roadmap

## Completed in this pass

### P0
- Added CI quality gates for unit tests, Android lint, and Detekt.
- Added Detekt configuration and formatting plugin.
- Added unit tests for repository refresh behavior and saved-search alerts.
- Reworked `HousingRepositoryImpl.refreshListings()` to preload existing rows once and batch upserts.

### P1
- Fixed invalid string resource references in `SettingsScreen.kt`.
- Completed Arabic and German string coverage.
- Localized Source Manager status/type labels and action buttons.
- Split `ListingsScreen` into smaller top-bar and content composables to reduce the main function size.

### P2
- Added Compose previews for the listings screen and listing card.
- Added an instrumentation smoke test for `MainActivity` launch.

## Next recommended follow-up
- Extract additional card/filter sections from `ListingsScreen.kt` into separate files.
- Add dedicated tests for `SyncScheduler` and settings persistence.
- Replace remaining hardcoded UI copy with string resources.
- Add baseline performance measurement around refresh latency and source parsing.


## Round 2 progress
- Moved additional Compose UI copy from hardcoded English strings into localized string resources.
- Extended localization coverage for English, Arabic, and German across onboarding, listing details, saved searches, listings filters, settings diagnostics, and source manager screens.
- Reworked `ListingsScreen` to rely on resource-backed labels and more reusable UI sections.
- Updated `ListingDetailsScreen`, `OnboardingScreen`, and `SavedSearchesScreen` to use translated resources instead of inline strings.

## Round 3 progress
- Refactored `SettingsScreen` into smaller sections (`SettingsContent`, diagnostics, toggles, and source-management blocks) to reduce screen-level complexity.
- Refactored `SourceManagerScreen` into smaller, reusable UI blocks for hero actions, source details, chips, primary actions, and reorder controls.
- Reduced repeated mutation and serialization logic in `UserPreferencesRepository` by extracting helper methods for enabled-source updates, saved-search updates, backup export mapping, and custom-source construction.


## Round 4 progress
- Split `ListingsScreen` into a smaller entry file plus `ListingsComponents.kt` to reduce screen-level complexity and improve readability.
- Extracted listing filter groups, card header rendering, badge rendering, and save-search dialog logic into focused composables.
- Replaced remaining numeric room and price chip labels with localized resource-backed labels.
- Refactored `SyncScheduler` with an internal `applyStoredSettings()` path so scheduling behavior can be tested without relying on background coroutine timing.
- Added unit tests for `SyncScheduler` covering stored-settings application, manual mode cancellation, and manual refresh work enqueueing.


## Round 5 progress
- Refactored `UserPreferencesRepository` so tests can create isolated DataStore-backed instances without relying on the production file.
- Hardened backup restore by normalizing custom sources, enabled source IDs, and source order before writing restored settings.
- Added instrumentation tests for settings persistence, remote-source toggling, custom source add/remove flows, backup export/restore, and invalid backup input handling.
- Added `androidx.test:core` to support repository-level instrumentation tests with application context access.


## Round 6 progress
- Added validation and user feedback for backup import/export in the source manager flow.
- Introduced snackbar-based success and failure messaging for reading, importing, and exporting backup files.
- Added in-progress UI state for backup import to disable conflicting actions and show progress.
- Added unit tests for `SettingsViewModel` backup validation and import/export outcomes.
- Added a Compose instrumentation test covering the source manager import progress state.
- Fixed a source manager UI bug where `currentStatus` was passed twice into `SourceDetails`.


## Round 7 progress
- Unified user-facing settings messages through a typed `UiMessage` payload that supports string resources with format arguments.
- Added user feedback snackbars for manual refresh start and source test outcomes.
- SettingsRoute now collects shared settings messages so manual refresh feedback is visible outside the source manager flow.
- Centralized refresh and source-health copy in `UserFacingMessages` to reduce scattered hardcoded operational text.
- Kept localized resource coverage aligned for English, Arabic, and German.


## Round 8 progress
- Removed startup notification permission requests from `MainActivity` and moved notification consent to the saved-search alerts toggle flow.
- Added a notification-permission-aware `SavedSearchesRoute` with snackbar guidance when the user declines permission.
- Expanded onboarding to explain refresh behavior, supported vs. catalog sources, and alert permission timing.
- Switched the main UI screens to `collectAsStateWithLifecycle()` for lifecycle-aware state collection.
- Disabled alerts by default for newly saved searches so alert opt-in is explicit.


## Round 9 progress
- Reworked listings state handling so sync failures are shown as persistent inline UI instead of being reused as snackbar-only transient messages.
- Added more helpful empty, loading, and partial-failure states on the listings screen with direct actions for refresh and clearing filters.
- Introduced derived listings UI state such as `hasActiveFilters` and `syncIssueMessage` for clearer rendering decisions.
- Added accessibility labels for key listings actions and tests for filter activity detection and matching behavior.


## Round 10 progress
- Centralized saved-search and listings filter matching into a shared matcher so alert evaluation and UI filtering use the same rules.
- Replaced hardcoded English number/date formatting with locale-aware currency, number, and timestamp formatters backed by translated resources (`Never`, room/area units, and EUR formatting).
- Upgraded the theme foundation with dynamic color support on Android 12+, richer color roles, custom typography, shapes, and a shared spacing scale.
- Removed blocking startup initialization from `BerlinHomeRadarApp`, restored scheduling asynchronously from stored settings, and eliminated the manual background scope from `SyncScheduler`.
- Hardened the in-app browser with restricted WebView settings, JavaScript allowlisting by host, mixed-content blocking, third-party cookie blocking, and explicit WebView disposal.
- Moved backup import/export file reads and writes off the main thread and wired the manifest to the existing Android backup rules.
- Completed the main accessibility pass for remaining icon actions by adding user-facing content descriptions across details, source manager, saved searches, navigation, and the in-app browser.

- Added an initial source kill-switch and WebView host allowlist through `FeatureFlags`, plus release/incident/source-adapter operational docs and GitHub dependency review automation.


## Round 11 progress
- Moved active listings filtering into Room with support for query, room count, area, max rent, district, badges, favorites, and source filtering.
- Added listing lifecycle fields (`lastSeenAtEpochMillis`, `isActive`, `lifecycleStatus`) and refresh-time lifecycle updates for active, stale, and archived listings.
- Added persistent source reliability metrics, a source metrics table, and source dashboard UI in source management.
- Introduced adaptive large-screen layouts for listings, details, settings, and source manager screens.
- Added remote-config backed runtime feature flags with cached fetch support.
- Replaced logcat-only telemetry bindings with network-capable analytics and crash reporting clients.
- Enabled release minification/resource shrinking and added starter baseline profile + performance planning docs.


## Round 12 progress
- Split remaining heavyweight UI files into smaller feature-focused component files for listings, settings, and source management.
- Extracted repository and preferences helper logic into dedicated support files to reduce class/file size and improve maintainability.
- Hardened WebView navigation policy for non-http(s) schemes with explicit external handoff or blocking.
- Made instrumentation tests less localization-fragile by switching from English text assertions to resource-based and test-tag-based checks.
