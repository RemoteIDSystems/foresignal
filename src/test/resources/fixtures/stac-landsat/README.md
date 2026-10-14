# `stac-landsat` fixtures

Prompt 096. Captured 2026-10-14 against the real Earth Search API with Foresignal's identifiable
User-Agent; real captures, trimmed.

## `search.json`

- **URL:** `POST https://earth-search.aws.element84.com/v1/search`
- **Command:**
  ```
  curl -s -A "Foresignal-fixture-capture (+https://foresignal.ai)" -H "Content-Type: application/json" \
    -X POST "https://earth-search.aws.element84.com/v1/search" \
    -d '{"collections":["landsat-c2-l2"],
         "intersects":{"type":"Polygon","coordinates":[[[-97.8,30.1],[-97.6,30.1],[-97.6,30.3],[-97.8,30.3],[-97.8,30.1]]]},
         "datetime":"2026-09-01T00:00:00Z/..",
         "query":{"eo:cloud_cover":{"lt":30}},
         "sortby":[{"field":"properties.datetime","direction":"desc"}],"limit":2,
         "fields":{"include":["id","collection","geometry","bbox","properties.datetime","properties.created",
            "properties.updated","properties.eo:cloud_cover","properties.platform","properties.constellation",
            "properties.instruments","properties.landsat:wrs_path","properties.landsat:wrs_row",
            "properties.view:sun_elevation","assets","links"]}}'
  ```
- **Notable response headers:** `Content-Type: application/geo+json`, HTTP 200.
- **Trimming:** the response's 3 features were reduced to 2 and each feature's `assets` to `thumbnail`,
  `red` and `blue`; each feature's `links` to its `self` link.

## Notes

- Verified 2026-10: the Landsat `assets.thumbnail.href` is an `s3://usgs-landsat/…` href in a
  requester-pays bucket. The record drops it; it is never rewritten or signed.
- The item carries `landsat:wrs_path`/`landsat:wrs_row` and `grid:code` (`WRS2-…`).
- Recapture with the same command and re-trim.
