# HomeSync Berlin

Berlin-only rental aggregation MVP built with Expo Router + Zustand + SQLite, extended with worker-ready live connectors for Berlin housing sources.

## What changed in v2.0.1
- Added **GitHub build files** for repository automation.
- Added GitHub Actions for:
  - app CI
  - worker CI
  - manual EAS builds
  - manual Cloudflare Worker deploys
- Added `.gitignore`, `.nvmrc`, `eas.json`, `workers/wrangler.toml`, and `GITHUB_SETUP.md`.
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
- Added `workers/wrangler.example.toml` and `.dev.vars.example`
- Added source manifest coverage and integration notes

## App status
This is still an MVP / scaffold-first codebase.

What is now stronger:
- Berlin-only source catalog
- worker-ready runtime configuration
- source-by-source live connector settings
- backup / restore
- diagnostics / source health / sync history / alerts / insights
- GitHub CI / build / deploy scaffolding

What is still not verified in this environment:
- `npm install`
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
- `GITHUB_SETUP.md`
- `.github/workflows/app-ci.yml`
- `.github/workflows/worker-ci.yml`
- `.github/workflows/eas-build.yml`
- `.github/workflows/worker-deploy.yml`

## Runtime setup
Inside the app, open:
- Settings → Runtime & connectors

Recommended modes:
- `demo` for offline/demo use
- `worker_hybrid` to try live workers and fall back to demo data
- `worker_only` to force live worker fetching

## Worker setup
See:
- `WORKER_CONTRACT.md`
- `workers/README.md`
- `workers/wrangler.toml`
- `workers/.dev.vars.example`

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
