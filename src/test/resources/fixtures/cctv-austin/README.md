# cctv-austin fixtures (prompt 072)

- `b4k4-adkb.json`: captured 2026-10-13 17:17 UTC from `https://data.austintexas.gov/resource/b4k4-adkb.json?$limit=5000` with `curl -s --compressed -A "Foresignal-fixture/0.1.0-SNAPSHOT" <url>`.
  HTTP 200, 867,894 bytes, 1009 rows (822 `TURNED_ON`, 123 `DESIRED`, 34 `VOID`, 30 `REMOVED`); `ETag` and `Last-Modified: Tue, 13 Oct 2026 06:55:39 GMT`.
  Trimmed to the first 21 `TURNED_ON` rows and one row of each other status. Socrata metadata: license Public Domain.

Frame-access probe: `https://cctv.austinmobility.io/image/{id}.jpg` → 200 `image/jpeg` for a TURNED_ON camera (403 `application/xml` for a DESIRED one) → `direct`.
