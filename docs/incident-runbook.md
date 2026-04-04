# Incident Runbook

## Common incidents

### 1. Refresh suddenly returns zero listings
- Check whether one or more sources were disabled intentionally.
- Review source health messages in-app.
- Verify network and certificate issues.
- Inspect parser assumptions for HTML sources.
- If a key source is broken, add it to `FeatureFlags.disabledSourceIds` and release a hotfix.

### 2. Source parser drift
- Reproduce with current upstream page/API.
- Capture failing HTML or payload example.
- Update selectors or mapping.
- Add regression coverage before release.

### 3. Alert complaints
- Verify notification permission status.
- Check whether saved-search alerts were enabled explicitly.
- Confirm `AppNotificationManager` is not being gated by system-level notification disablement.

### 4. In-app browser issues
- Confirm whether the site requires JavaScript.
- Only allowlist hosts when necessary.
- If security behavior blocks critical navigation, prefer external browser as fallback.

## Escalation
- P0: app crashes on launch, data corruption, mass alert spam
- P1: refresh broken for major sources, onboarding broken, settings import/export broken
- P2: single-source breakage, isolated localization or layout issues
