# Fixtures: `usgs-earthquakes`

| File | What it is |
|---|---|
| `all_day.geojson` | A real capture of the USGS `all_day` summary feed, trimmed to 25 features. `metadata` is kept, with `count` set to 25. |
| `revised.geojson` | `all_day` reduced to the feature `aka2026uekklg`, revised by hand: `mag` 2.4 → 2.5, `status` automatic → reviewed, `updated` + 600,000 ms, and `title` to match the new magnitude. |
| `updated-only.geojson` | `all_day` with only `updated` on `us6000u1i6` advanced by 7,200,000 ms (2 h). Every other byte is the same. |
| `edge-cases.geojson` | Hand-made, not a capture: null `mag` with optional fields missing, a feature without an id, one without geometry, one without `updated`, one without depth (with an integer `mag`), and one with `status: deleted`. |
| `not-a-feature-collection.json` | `{"error": "x"}`, for the format-change check. |

## Capture

- **Date:** 2026-10-11, 22:32:45 UTC (`Last-Modified` of the response).
- **URL:** `https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_day.geojson`
- **Command:** `curl -s -A "Foresignal-fixture-capture (+https://foresignal.ai)" https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_day.geojson`
- **Response:** HTTP 200, `metadata.api` `2.7.0`, 174 features, `cache-control: public, max-age=60`,
  `last-modified` present, **no `ETag`**. `tz` was null on every feature.

## Trimming

A script kept at most 25 features, in feed order, chosen in this priority:

1. features with a non-null `alert`, `mmi`, `felt` or `cdi` (up to 10);
2. features whose `mag` or depth is a JSON integer, or whose `status` is `reviewed` (until 15 are
   chosen);
3. the earliest remaining features in feed order (until 25 are chosen).

The JSON was re-serialized with one-space indentation; no values were changed.

## Refreshing

Recapture with the command above, trim the same way, then rebuild `revised.geojson` and
`updated-only.geojson` from the new `all_day.geojson` (pick a feature that exists in it and update
the ids in `UsgsGeoJsonParserTest` and `UsgsIngestIT`). Update the date above.
