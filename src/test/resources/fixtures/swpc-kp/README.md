# swpc-kp fixtures

Captured 2026-10-13 about 20:47 UTC, untrimmed; both files sent `ETag`, `Last-Modified` and `Cache-Control: max-age=60`.

- `planetary_k_index_1m.json`: `GET https://services.swpc.noaa.gov/json/planetary_k_index_1m.json` (358 rows,
  27.9 KB; `{time_tag, kp_index, estimated_kp, kp}`, the thirds notation `0Z`…`3Z`, `2P`, `3M`).
- `noaa-planetary-k-index.json`: `GET https://services.swpc.noaa.gov/products/noaa-planetary-k-index.json` (4.8 KB;
  verified 2026-10-13 an array of objects `{time_tag, Kp, a_running, station_count}`, not the older array of arrays).

Recapture per prompt 143.
