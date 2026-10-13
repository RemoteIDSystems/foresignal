# Entur fixtures

- `vehicle-positions.pb`: `https://api.entur.io/realtime/v1/gtfs-rt/vehicle-positions`, 2026-10-13 16:18:06 UTC; full
  feed 8667 entities (1.7 MB), all with a vehicle timestamp; header timestamp 1791908279; FULL_DATASET, version 1.0.
- `alerts.pb`: `https://api.entur.io/realtime/v1/gtfs-rt/alerts`, 16:18:07 UTC; full feed 676 alerts, `active_period`
  only.
- Request headers: the Foresignal User-Agent and `ET-Client-Name: foresignal-devcapture` (Entur's mandatory
  identification header; production sends `foresignal-{installId}`).
- Response: `content-type: application/octet-stream`, `cache-control: no-cache, no-store, max-age=0, must-revalidate`,
  no `ETag` or `Last-Modified` (a conditional GET always gets a 200). Trimmed to the first 30 entities.
