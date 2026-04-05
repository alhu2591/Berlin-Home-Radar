# HomeSync Berlin Live Source Notes (v2.0.0)

This release hardens the Berlin worker layer instead of just expanding the source list.

## What changed

- Added worker-side source profiles for every Berlin adapter
- Added `GET /sources/meta` for runtime inspection
- Added Berlin district alias matching
- Added detail-page fallback heuristics for pages without strong JSON-LD

## Why this matters

Many Berlin municipal and student-housing sites expose useful offer pages, but not always with clean card JSON.
This release improves the chance of extracting structured listings from those pages without pretending the connectors are fully production-hardened yet.


## v1.9.0

- Added per-source live diagnosis endpoint and UI.
- Added tuned detail-page parsers for HOWOGE, Gewobag-like municipal sources, and STADT UND LAND.
- Added explicit fetch-plan resolution for Berlin live sources.


## v1.9.0 notes

- Added search-page probing for live Berlin sources.
- Added tuned list-page parsers for WBM, HOWOGE, and selected municipal-style result pages.


## v1.9.0

- Added live search-page probe support in the worker and app.
- Added tuned result-card extraction for WBM, HOWOGE, and municipal-style result pages.
- Added coverage tests for WBM list parsing.


- v2.0.0: Added deeper detail-page probe metrics and tuned known parsers for inberlinwohnen, STADT UND LAND, and Studentendorf Berlin.
