# HomeSync Berlin Worker Contract

## Endpoints

- `GET /health`
- `POST /sources/fetch`
- `GET /sources/meta`

## Supported adapters in v2.0.0

- `immoscout-de`
- `immowelt-de`
- `immonet-de`
- `wg-gesucht-de`
- `ebay-kleinanzeigen-de`
- `wohnungsboerse-de`
- `inberlinwohnen-de`
- `degewo-de`
- `gesobau-de`
- `howoge-de`
- `stadtundland-de`
- `wbm-de`
- `gewobag-de`
- `berlinovo-de`
- `bgg-berlin-student-de`
- `studierendenwerk-berlin-de`
- `studentendorf-berlin-de`
- `wunderflats-berlin-de`
- `housinganywhere-berlin-de`
- `spotahome-berlin-de`
- `coming-home-berlin-de`
- `urbanbnb-berlin-de`
- `local-agency-rss-de`

## Request body

```json
{
  "sourceId": "source-immoscout-de",
  "adapterId": "immoscout-de",
  "country": "DE",
  "config": {
    "city": "Berlin",
    "listingType": "rent",
    "maxResults": "20",
    "executionMode": "worker",
    "workerPath": "/sources/fetch",
    "fetchStrategy": "auto",
    "district": "Mitte"
  }
}
```

## Strategy notes

- `immoscout-de`: official API if credentials + geocode are configured, otherwise public HTML fallback
- `immowelt-de`, `immonet-de`, `wohnungsboerse-de`: public result-page parsing
- `wg-gesucht-de`: public result-page parsing, mode = rooms/apartments/both
- `ebay-kleinanzeigen-de`: public result-page parsing
- `inberlinwohnen-de`, `degewo-de`, `gesobau-de`, `howoge-de`, `stadtundland-de`, `wbm-de`, `gewobag-de`: public-housing page parsing
- `berlinovo-de`, `bgg-berlin-student-de`, `studierendenwerk-berlin-de`, `studentendorf-berlin-de`: student or institutional page parsing
- `wunderflats-berlin-de`, `housinganywhere-berlin-de`, `spotahome-berlin-de`, `coming-home-berlin-de`, `urbanbnb-berlin-de`: furnished / temporary housing pages
- `local-agency-rss-de`: RSS / Atom feed parsing

## Auth

If `WORKER_BEARER_TOKEN` is set in the worker environment, send it as:

```
Authorization: Bearer <token>
```


## New in v1.9.0

- `GET /sources/meta` returns worker-side source profiles, parser family, live search URLs, and detail-pattern hints.
- Detail-page fallback now uses metadata/title/price heuristics when JSON-LD is missing.
- District filtering is normalized for Berlin aliases like `Neukölln`/`Neukoelln`, `Schöneberg`/`Schoeneberg`, and multi-part districts.


## v1.9.0

- Added per-source live diagnosis endpoint and UI.
- Added tuned detail-page parsers for HOWOGE, Gewobag-like municipal sources, and STADT UND LAND.
- Added explicit fetch-plan resolution for Berlin live sources.


## Additional endpoint

- `POST /sources/probe` — fetches the first public search page(s) for a source and returns probe data: candidate detail links, parsed list-card counts, JSON-LD counts, snippet counts, and HTML byte size.

- Probe response fields include `candidateDetailUrls`, `parsedListCount`, `jsonLdCount`, `snippetCount`, and `htmlBytes` per probed URL.


Probe responses now also include detail-page preview metrics: `detailFetchedCount`, `detailKnownCount`, `detailJsonLdCount`, `detailHeuristicCount`, and `sampleTitles`.
