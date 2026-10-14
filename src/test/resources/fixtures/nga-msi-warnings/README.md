# Fixtures: `nga-msi-warnings` (prompt 088)

| File | What it is |
|---|---|
| `broadcast-warn-active.json` | A real capture of the active list (`GET https://msi.nga.mil/api/publications/broadcast-warn?status=active&output=json`, 386 warnings, 206 KB) trimmed to 30 warnings that keep an example of every geometry rule: areas `BOUND BY` positions (one with lettered areas A, B, C), circles `WITHIN FIVE MILES OF` / `WITHIN ONE MILE OF` a position, tracklines (one lettered), DMS positions, a single position, a list of drilling-unit positions, warnings with no position (the "warnings in force" lists) and `CANCEL …` lines of every NAVAREA. Unchanged otherwise (pretty-printed). |
| `handmade.json` | Seven hand-made warnings for the rules the capture shows only in word form or not at all: `WITHIN 2 NM OF`, `WITHIN 500 METERS OF`, `BETWEEN … AND …`, an `AREA BOUNDED BY` DMS polygon written without the space (`34-10-30.5N119-20-15.0W`) that also cancels `HYDROPAC 1234/26`, an invalid position (`12-75.0N`, minutes ≥ 60), lettered `AREAS BOUNDED BY` and lettered `TRACKLINES`. |

## Capture procedure

- **Date:** 2026-10-14 05:24 UTC.
- **Command:**

  ```sh
  curl -sS -A "Foresignal/0.1.0 fixture-capture (+https://foresignal.ai)" -D nga.h -o nga.json \
    'https://msi.nga.mil/api/publications/broadcast-warn?status=active&output=json'
  ```

- **Result:** HTTP 200 with Foresignal's own User-Agent, no Referer and no browser headers (the Velocity survey had
  needed both). `Cache-Control: max-age=8186`, no `ETag` or `Last-Modified`, an Akamai cookie (ignored).
- **On the full list:** 35 polygons (4 lettered), 3 circles, 15 tracklines (1 lettered), 279 single points,
  36 multipoints and 18 warnings without a usable position (4.7%). The newest `issueDate` was in May 2024 although
  the response was served on 2026-10-14 (a CDN cache hit); recapture at 143.
