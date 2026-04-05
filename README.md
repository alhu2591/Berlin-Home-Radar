# Berlin Home Radar

Build-focused distribution: non-essential tests and secondary docs were removed to keep the repository lean for app and worker builds.

Berlin-only rental aggregation MVP built with Expo Router + Zustand + SQLite, extended with worker-ready live connectors for Berlin housing sources.

## What changed in v2.5.1
- Renamed the mobile package to `@berlin-home-radar/mobile`.
- Renamed the worker package to `@berlin-home-radar/connectors`.
- Switched the mobile bundle/package identifier to `com.berlinhomeradar.mobile`.
- Renamed the Cloudflare Worker to `berlin-home-radar-connectors`.
- Kept a single workflow file with clearer job names: `verify-mobile` and `verify-connectors`.
- Added `@types/node` so `typecheck:logic` works reliably in clean CI installs.

## What changed in v2.4.0
- Renamed the app to **Berlin Home Radar** across Expo config, npm metadata, worker metadata, and local storage identifiers.
- Reduced GitHub automation to a single workflow file: `.github/workflows/ci.yml`.
- Added professional Android/iOS identifiers: `com.berlinhomeradar.mobile`.

## What changed in v2.2.0
- Removed non-essential test files and secondary documentation to keep the repository lean for build and deployment.
- Simplified app CI to focus on install + logic typecheck.
- Kept build-critical files only for the Expo app, worker, and GitHub workflows.

## What changed in v2.0.1
- Added **GitHub build files** for repository automation.
- Added GitHub Actions for:
  - app CI
  - worker CI
  - manual EAS builds
  - manual Cloudflare Worker deploys
- Added `.gitignore`, `.nvmrc`, `eas.json`, and `workers/wrangler.toml`.
- Added safer repository scripts for CI usage.

## What changed in v2.0.0
- Added a **deeper live-probe layer** for Berlin worker sources, including detail-page preview metrics and sample parsed titles.
- Retained the live connector coverage for:
  - ImmobilienScout24
  - Immowelt
  - Immonet
  - WG-Gesucht
  - Kleinanzeigen
  - Wohnungsboerse
  - Local Agency RSS / Atom feeds
- Added **advanced source config** inside the app:
  - worker fetch strategy
  - district filter
  - custom search URL override
  - RSS / Atom feed URLs
  - include houses toggle
  - WG-Gesucht mode (rooms / apartments / both)
- Added a **Cloudflare Worker starter implementation** under `workers/src`
- Added source manifest coverage and integration notes

## App status
This is still an MVP / scaffold-first codebase.

What is now stronger:
- Berlin-only source catalog
- worker-ready runtime configuration
- source-by-source live connector settings
- source health / sync history / alerts
- GitHub CI / build / deploy scaffolding

Verified in a clean local install before packaging:
- `npm ci` for the mobile app
- `npm run typecheck:logic`
- `npm ci` for the connectors worker
- `npm run typecheck` inside `workers/`

What is still not verified in this environment:
- running Expo end-to-end
- building iOS / Android binaries
- production legality / anti-bot handling for every public source

## Running the app
```bash
npm install
npm run start
```

## GitHub automation
See:
- `.github/workflows/ci.yml`

## Runtime setup
Inside the app, open:
- Settings → Runtime & connectors

Recommended modes:
- `demo` for offline/demo use
- `worker_hybrid` to try live workers and fall back to demo data
- `worker_only` to force live worker fetching

## Worker setup
See:
- `workers/wrangler.toml`
- `workers/source-manifest.json`
- `workers/package.json`

Typical flow:
1. Deploy the worker.
2. Put its base URL into Runtime settings.
3. Save an optional bearer token.
4. Set specific sources to `worker` execution mode.
5. Test each Berlin source from its source detail screen.

## Berlin source coverage
| Source | Strategy | Notes |
|---|---|---|
| ImmobilienScout24 | auto / official API / public HTML | Can use official partner API if credentials exist; otherwise public-page fallback |
| Immowelt | public HTML | Result-page parsing with URL override |
| Immonet | public HTML | Result-page parsing with URL override |
| WG-Gesucht | public HTML | Rooms / apartments / both |
| Kleinanzeigen | public HTML | Marketplace-style public results |
| Wohnungsboerse | public HTML | Long-tail Berlin coverage |
| Local Agency RSS | RSS / Atom | Feed URL(s) provided by you |

## Notes
- The worker code is designed as a realistic starting point, not a guaranteed stable scraper.
- For sources with official access programs, prefer contractual/API integrations over public-page parsing when available.
- The GitHub Actions setup is intentionally conservative: CI is automatic, while mobile builds and worker deploys are manual.


## Lean build notes

This build removes compare, onboarding, notes, and standalone source-health screens to keep the repository focused on the main app, source manager, runtime settings, and worker connectors.

## CI dependency note

This project pins `react-dom@19.1.0` and `react-native-web@0.21.0` to match Expo SDK 54 compatibility.
