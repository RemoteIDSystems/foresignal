# `fred-series` fixtures

**synthetic: true** — recapture with a real key. FRED needs an API key, and construction uses none
(Conventions, "No credentials during construction"). The shapes follow the FRED API documentation examples
(<https://fred.stlouisfed.org/docs/api/fred/series.html>, `series_observations.html`, `series_release.html`,
`release_sources.html`), with values typed in for October 2026.

- `series-DGS10.json`, `series-DCOILWTICO.json` — `/fred/series` metadata of an open series.
- `series-VIXCLS.json` — a copyrighted series: its `notes` contain "Copyright" (FRED's terms of use: "Copyrighted
  series contain the word 'Copyright' in their notes").
- `observations-DGS10.json` — `/fred/series/observations`, with a missing value `"."` on 2026-10-12.
- `observations-DGS10-revised.json` — the same window after a revision (2026-10-13 4.08 → 4.09) and a new day.
- `observations-VIXCLS.json` — one observation, served only when the series is allowed.
- `release-DGS10.json`, `release-sources-18.json` — `/fred/series/release` and `/fred/release/sources`, which give
  the series' source line.

Recapture with `?series_id=DGS10&api_key=…&file_type=json` (never commit a key).
- `error-api-key.json` — real capture (2026-10-16): the HTTP 400 body FRED returns when `api_key` is missing,
  from `https://api.stlouisfed.org/fred/series?series_id=DGS10&file_type=json`. A rejected key answers the same
  shape ("The value for variable api_key is not registered"), which the adapter turns into a rejected credential.
