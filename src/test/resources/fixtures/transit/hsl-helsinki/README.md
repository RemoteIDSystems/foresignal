# HSL fixtures

- `vehicle-positions.pb`: `https://realtime.hsl.fi/realtime/vehicle-positions/v2/hsl`, 2026-10-13 16:18:04 UTC; full
  feed 788 entities, all with a vehicle timestamp; header timestamp 1791908285; FULL_DATASET, version 2.0. Response:
  `Content-Type: application/x-protobuf`, `Cache-Control: max-age=1`, `ETag: 0x8DF29458FEE5CC9`.
- `service-alerts.pb`: `https://realtime.hsl.fi/realtime/service-alerts/v2/hsl`, 16:18:06 UTC; full feed 34 alerts,
  `active_period` only, Finnish, Swedish and English translations. Response: `Cache-Control: max-age=5`,
  `ETag: 0x8DF2944F200DEED`.
- Request headers: the Foresignal User-Agent only (these realtime hosts need no Digitransit subscription key).
  Trimmed to the first 30 entities.
