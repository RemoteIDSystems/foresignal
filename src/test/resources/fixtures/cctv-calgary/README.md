# cctv-calgary fixtures (prompt 072)

- `k7p9-kppz.json`: captured 2026-10-13 17:17 UTC from `https://data.calgary.ca/resource/k7p9-kppz.json?$limit=5000` with `curl -s --compressed -A "Foresignal-fixture/0.1.0-SNAPSHOT" <url>`.
  HTTP 200, 76,769 bytes, 214 rows; `ETag` and `Last-Modified: Tue, 13 Oct 2026 02:57:13 GMT`. Trimmed to the first 25 rows.
  Socrata metadata (`/api/views/k7p9-kppz.json`): license "See Terms of Use", attribution "The City of Calgary".

Frame-access probe: `https://trafficcam.calgary.ca/loc74.jpg` → 200 `image/jpeg` → `direct`. The rows publish `http:` URLs; the host serves https, so the parser upgrades them.
