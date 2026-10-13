# swpc-alerts fixtures

Captured 2026-10-13 about 20:47 UTC; both files sent `ETag`, `Last-Modified` and `Cache-Control: max-age=60`.

- `alerts.json`: `GET https://services.swpc.noaa.gov/products/alerts.json`, untrimmed (85 messages, 55.9 KB). It
  holds the real cancel message `WATA30` serial 286 ("CANCEL WATCH", `Cancel Serial Number: 285`) together with the
  watch it cancels (`WATA30` 285), an extended warning (`Now Valid Until:`), both spellings `NOAA Scale:` and
  `Noaa Scale:`, and serials SWPC reused for continued messages (`ALTEF3` 3752 three times, `ALTTP2` 1531 twice).
- `noaa-scales.json`: `GET https://services.swpc.noaa.gov/products/noaa-scales.json`, untrimmed (1.1 KB).

Recapture per prompt 143.
