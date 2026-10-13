# swpc-rtsw fixtures

Captured 2026-10-13 about 20:47 UTC; every file sent `ETag`, `Last-Modified` and `Cache-Control: max-age=60`. The old
`products/solar-wind/*` five-minute files answered 404.

- `solar-wind-speed.json`: `GET https://services.swpc.noaa.gov/products/summary/solar-wind-speed.json` (59 bytes).
- `solar-wind-mag-field.json`: `GET https://services.swpc.noaa.gov/products/summary/solar-wind-mag-field.json`
  (60 bytes).
- `rtsw_wind_1m.json`: `GET https://services.swpc.noaa.gov/json/rtsw/rtsw_wind_1m.json` (2.6 MB, 3,519 rows:
  1,127 `SOLAR1` rows with `active: true`, 1,165 `IMAP` and 1,227 `ACE` rows inactive), trimmed to its first
  20 rows (newest first), which mix all three spacecraft; the newest active row is `SOLAR1` 2026-10-13T20:40:00 with
  `proton_density` 4.04.

Recapture per prompt 143.
