# Performance baseline and benchmark plan

This project now includes a starter `app/src/main/baseline-prof.txt` file so release builds can ship a stable startup and navigation profile even before macrobenchmark capture is added in CI.

## Initial targets
- Cold start to listings
- First listings render after app open
- Manual refresh latency
- Scroll smoothness on listings
- Listing details open latency

## Benchmark scenarios to automate next
1. Cold start into `MainActivity`
2. Navigate to a listing details page
3. Trigger manual refresh from settings
4. Open source manager and test one source
5. Toggle favorite on a visible listing

## Recommended next step
Add a dedicated macrobenchmark module and capture a generated baseline profile on a physical device or Gradle Managed Device once the Android build toolchain is available in CI.
