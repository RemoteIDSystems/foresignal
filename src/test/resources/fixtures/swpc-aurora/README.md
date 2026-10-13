# swpc-aurora fixtures

- `ovation_aurora_latest.json`: `GET https://services.swpc.noaa.gov/json/ovation_aurora_latest.json`, captured
  2026-10-13 about 20:47 UTC (919 KB, 65,160 cells, `Observation Time` 2026-10-13T20:34:00Z, `Forecast Time`
  2026-10-13T21:21:00Z; sent `ETag`, `Last-Modified` and `Cache-Control: max-age=60`). Trimmed to the header members
  and the band of rows with latitude 50 to 75 (all longitudes, 9,360 cells, probabilities 0 to 14); a cell left out
  reads as 0.

The antimeridian and 300 km cases of `OvationSamplerTest` use hand-built grids.

Recapture per prompt 143.
