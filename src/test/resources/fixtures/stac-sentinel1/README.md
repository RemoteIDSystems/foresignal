# `stac-sentinel1` fixtures

Prompt 096. Captured 2026-10-14 against the real Earth Search API with Foresignal's identifiable
User-Agent; a real capture, trimmed.

## `search.json`

- **URL:** `POST https://earth-search.aws.element84.com/v1/search`
- **Command:**
  ```
  curl -s -A "Foresignal-fixture-capture (+https://foresignal.ai)" -H "Content-Type: application/json" \
    -X POST "https://earth-search.aws.element84.com/v1/search" \
    -d '{"collections":["sentinel-1-grd"],
         "intersects":{"type":"Polygon","coordinates":[[[-60.2,4.9],[-60.0,4.9],[-60.0,5.1],[-60.2,5.1],[-60.2,4.9]]]},
         "datetime":"2026-10-01T00:00:00Z/..",
         "sortby":[{"field":"properties.datetime","direction":"desc"}],"limit":2,
         "fields":{"include":["id","collection","geometry","bbox","properties.datetime","properties.created",
            "properties.updated","properties.platform","properties.constellation","properties.instruments",
            "properties.sar:instrument_mode","properties.sar:polarizations","properties.view:sun_elevation",
            "assets","links"]}}'
  ```
- **Notable response headers:** `Content-Type: application/geo+json`, HTTP 200.
- **Trimming:** the response's 2 features were reduced to 1 and the feature's `assets` to `thumbnail`,
  `vv` and `vh`; its `links` to the `self` link.

## Notes

- Sentinel-1 carries no `eo:cloud_cover`; the source has no cloud filter. It has `sar:instrument_mode`
  and `sar:polarizations`.
- The `assets.thumbnail.href` is an `s3://sentinel-s1-l1c/…` href in a requester-pays bucket; the record
  drops it.
- Recapture with the same command and re-trim.
