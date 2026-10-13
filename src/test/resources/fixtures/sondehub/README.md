# sondehub fixtures

- `sondes.json`: `GET https://api.v2.sondehub.org/sondes?lat=50.0&lon=10.0&distance=1500000&last=10800`, captured
  2026-10-13 about 20:50 UTC (11 sondes: 6 RS41, 4 M10, 1 DFM). For privacy every `uploader_callsign` was replaced by
  `UPLOADER-n` (one per distinct uploader), every `uploader_position` by `51.0000,10.0000`, every `uploader_antenna`
  by `redacted antenna` and every `uploader_alt` by 100.0. The field is kept so the tests can show it is never
  stored.
- `sondes-edge.json`: **hand-made**: a sonde without a position and with sentinel sensor values (`temp` -273.15,
  `humidity` -1, `pressure` 0, `heading` 361), a sonde without `datetime`, and an entry without a serial or key.

Recapture per prompt 143 (and redact the uploader members again).
