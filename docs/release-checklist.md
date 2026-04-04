# Release Checklist

## Before cutting a release
- Confirm CI is green for `testDebugUnitTest`, `lintDebug`, and `detekt`.
- Review source kill switches in `FeatureFlags.disabledSourceIds`.
- Review WebView JavaScript allowlist in `FeatureFlags.webViewJavaScriptAllowedHosts`.
- Confirm changelog entries are complete.
- Verify app versionCode and versionName.
- Smoke-test onboarding, listings refresh, saved-search alerts, source manager, language/theme switching, and in-app browser.
- Review crash/error dashboards for recent regressions.
- Confirm signing secrets are present for release CI.

## Release steps
- Create or update release notes.
- Tag the release with `v*`.
- Upload and inspect generated APK/AAB artifacts.
- Perform staged rollout first before broad release.
- Monitor refresh failures, source health, and crashes for 24 hours after rollout.
