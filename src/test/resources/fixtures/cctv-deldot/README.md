# cctv-deldot fixtures (prompt 072)

- `videocamera.json`: captured 2026-10-13 17:17 UTC from `https://tmc.deldot.gov/json/videocamera.json` with `curl -s --compressed -A "Foresignal-fixture/0.1.0-SNAPSHOT" <url>`.
  HTTP 200, 184,511 bytes, 362 cameras (360 `Active`); `ETag: "2d0bf-65dbbf7600049-gzip"`, `Last-Modified: Tue, 13 Oct 2026 17:15:50 GMT`.
  Trimmed to the first 23 Active cameras and the 2 `Unavailable` ones (NCAM267, SCAM170); the envelope (`cameraCount` still 362) is kept.

Frame-access probe: `https://video.deldot.gov/live/KCAM001.stream/playlist.m3u8` → 200 `application/vnd.apple.mpegurl`, `Access-Control-Allow-Origin: *` → `direct`. DelDOT publishes no still image.
