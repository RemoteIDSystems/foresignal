# `stac-sentinel2` fixtures

Prompt 096. Captured 2026-10-14 against the real Earth Search API with Foresignal's identifiable
User-Agent; real captures, trimmed.

## `search.json`

- **URL:** `POST https://earth-search.aws.element84.com/v1/search`
- **Command:**
  ```
  curl -s -A "Foresignal-fixture-capture (+https://foresignal.ai)" -H "Content-Type: application/json" \
    -X POST "https://earth-search.aws.element84.com/v1/search" \
    -d '{"collections":["sentinel-2-c1-l2a"],
         "intersects":{"type":"Polygon","coordinates":[[[-97.8,30.1],[-97.6,30.1],[-97.6,30.3],[-97.8,30.3],[-97.8,30.1]]]},
         "datetime":"2026-10-01T00:00:00Z/..",
         "query":{"eo:cloud_cover":{"lt":30}},
         "sortby":[{"field":"properties.datetime","direction":"desc"}],"limit":100,
         "fields":{"include":["id","collection","geometry","bbox","properties.datetime","properties.created",
            "properties.updated","properties.eo:cloud_cover","properties.platform","properties.constellation",
            "properties.instruments","properties.s2:mgrs_tile","properties.grid:code",
            "properties.landsat:wrs_path","properties.landsat:wrs_row","properties.view:sun_elevation",
            "properties.sar:instrument_mode","properties.sar:polarizations","assets","links"]}}'
  ```
- **Notable response headers:** `Content-Type: application/geo+json`, HTTP 200.
- **Trimming:** the response's 3 features were reduced to 2 and each feature's `assets` to `thumbnail`,
  `red`, `blue` and `visual`; each feature's `links` to its `self` link. The `properties`, `geometry`,
  `bbox` and `id` are unchanged. The response's top-level `links` were reduced to `root` (the capture had
  a `next` link because the AOI matched more items than the page; the trimmed set has none, so the
  pagination test builds its own `next` body).

## Notes

- Verified 2026-10: the C1 collection uses `properties.grid:code` (`MGRS-14RPU`), not `s2:mgrs_tile`; the
  `assets.thumbnail.href` is an `https://` URL, so the record keeps it.
- Recapture with the same command and re-trim; the schema is stable (STAC 1.0.0).
