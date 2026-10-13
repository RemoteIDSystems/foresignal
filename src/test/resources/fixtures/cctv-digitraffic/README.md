# cctv-digitraffic fixtures (prompt 072)

- `stations.json`: captured 2026-10-13 17:17 UTC from `https://tie.digitraffic.fi/api/weathercam/v1/stations` with `curl -s --compressed -A "Foresignal-fixture/0.1.0-SNAPSHOT" -H "Digitraffic-User: Foresignal/0.1.0-SNAPSHOT" <url>`.
  HTTP 200, 467,559 bytes decompressed, 814 stations (812 `GATHERING`); `ETag: W/"0dce958ed7ad2c4ab7c6f3436e9502033"`, `Last-Modified: Tue, 13 Oct 2026 16:00:52 GMT`.
  Trimmed to whole stations: the first GATHERING stations up to 24 presets in all, one station with two presets out of collection (C01632) and one `REMOVED_TEMPORARILY` station (C01539); the envelope is kept.

Frame-access probe: `https://weathercam.digitraffic.fi/C0150301.jpg` → 200 `image/jpeg`, `Access-Control-Allow-Origin: *` → `direct`.
