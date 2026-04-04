# Remote config

This app now supports runtime configuration via a JSON document fetched from `BuildConfig.REMOTE_CONFIG_URL`.

## Supported keys
```json
{
  "disabledSourceIds": ["source-id"],
  "webViewJavaScriptAllowedHosts": ["example.com"],
  "analyticsEnabled": true,
  "analyticsEndpoint": "https://your-endpoint.example/analytics",
  "crashReportingEnabled": true,
  "crashEndpoint": "https://your-endpoint.example/crash"
}
```

## Runtime behavior
- The app loads cached config on startup.
- It refreshes remote config when stale.
- Source enablement, WebView JS policy, and telemetry endpoints are updated in memory without a code change.

## Production recommendation
Set `REMOTE_CONFIG_URL` in the Gradle build config per environment, or replace this lightweight implementation with Firebase Remote Config / your own config backend once credentials are available.
