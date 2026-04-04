# Source Adapter Guide

## When to add a dedicated adapter
Use a dedicated adapter when a source is strategically important, stable enough to maintain, and repeatedly used by users.
Prefer browser-assisted mode or catalog-only entries for dynamic sites until reliability is proven.

## Checklist for a new adapter
1. Add/update the source entry in `SourceCatalog`.
2. Implement `ListingSource`.
3. Add a parser or API client with clear fallback behavior.
4. Add a `healthCheck()` expectation.
5. Add repository or parser tests for:
   - successful parsing
   - empty results
   - malformed markup / schema drift
6. Review whether the source should be enabled by default.
7. Add the host to the in-app browser JavaScript allowlist only if it truly needs JavaScript.

## What to do when a source changes
- Compare previous HTML/API snapshots to current responses.
- Check whether the source should be temporarily disabled via `FeatureFlags.disabledSourceIds`.
- Update parser selectors/mapping.
- Add a regression test reproducing the breakage.
- Verify source health manually from the Source Manager screen.
