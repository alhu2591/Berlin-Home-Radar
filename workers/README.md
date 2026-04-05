# HomeSync Berlin Workers 1.9.0

Broad Berlin source catalog worker layer for public, student, municipal, room-share, and furnished-housing websites.

# Workers integration notes

This folder now includes a working **Cloudflare Worker starter** for Berlin live connectors.

## Included files
- `src/index.ts` — worker entrypoint
- `src/types.ts` — shared worker-side types
- `src/utils.ts` — parsing + normalization helpers
- `src/sources.ts` — Berlin source implementations
- `source-manifest.json` — compatibility matrix
- `wrangler.example.toml` — deployment example
- `.dev.vars.example` — local development secrets/env

## Shared contract
The mobile app calls:
- `GET /health`
- `GET /sources/meta`
- `POST /sources/fetch`

The POST body contains:
- `sourceId`
- `adapterId`
- `country`
- `config`

## Source strategies
- ImmoScout24: official API when credentials are present and the source/worker requests it, otherwise public HTML fallback
- Immowelt / Immonet / Wohnungsboerse / Kleinanzeigen: public result-page parsing
- WG-Gesucht: public result-page parsing for rooms/apartments/both
- Local Agency RSS: RSS / Atom feed parsing from configured feed URLs

## Deployment outline
1. Copy `wrangler.example.toml` to `wrangler.toml`
2. Fill env vars or `.dev.vars`
3. Deploy the worker
4. Put the worker URL into the app runtime settings
5. Switch sources to `worker` or keep the app in `worker_hybrid`

## Reality check
This worker is meant to move the project from "worker-ready" to "actual connector code present".
It is still a first implementation and will likely need per-source hardening once you test against live pages.


## What improved in 1.9.0
- Source profiles moved into dedicated worker metadata.
- Detail-page heuristic parsing fills gaps for municipal, student, and furnished pages that do not expose strong JSON-LD.
- Berlin district matching is normalized for umlauts and district aliases.


### Probe a source

`POST /sources/probe` returns the first public search-page URLs, candidate detail URLs, and parser counts so you can inspect whether list-page extraction is healthy before a full fetch.


### 1.9.0 additions

- Search-page probing endpoint for public HTML sources.
- Tuned list-card extraction before detail-page fallback.
