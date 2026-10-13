# MBTA fixtures

- `VehiclePositions.pb`: `https://cdn.mbta.com/realtime/VehiclePositions.pb`, 2026-10-13 16:18:04 UTC; full feed 555
  entities, all with a vehicle timestamp; header timestamp 1791908282 (16:18:02 UTC); FULL_DATASET, version 2.0.
  Response: `content-type: application/x-protobuf`, `etag: "a2b442a116f326a71e32c7f8e30746dd"`,
  `last-modified: Tue, 13 Oct 2026 16:18:05 GMT`.
- `Alerts.pb`: `https://cdn.mbta.com/realtime/Alerts.pb`, same time; full feed 131 alerts, all with `active_period`
  only; header timestamp 1791908252. Response: `etag: "0318d06a79b7d7dd335a1621a768abd3"`,
  `last-modified: Tue, 13 Oct 2026 16:17:53 GMT`.
- Request headers: the Foresignal User-Agent only. Trimmed to the first 30 entities.
