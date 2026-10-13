# cctv-caltrans-d4 fixtures (prompt 072)

- `cctvStatusD04.json`: captured 2026-10-13 17:17 UTC from `https://cwwp2.dot.ca.gov/data/d4/cctv/cctvStatusD04.json` with `curl -s --compressed -A "Foresignal-fixture/0.1.0-SNAPSHOT" <url>`.
  HTTP 200, 2,436,981 bytes, 756 cameras (753 `inService=true`); `ETag: "6ac7db15-252f75"`, `Last-Modified: Thu, 08 Oct 2026 18:04:05 GMT`.
  Trimmed to the first 22 in-service cameras and the 3 out-of-service ones (TV965, TV823, TV073); rows unchanged.
- `edge-cases.json`: hand-made (`synthetic: true`): no coordinates, out of bounds, off-host image and stream, `inService=false`, a duplicate id, a camera without a code (id from the image directory), a row with neither.

Frame-access probe: `cwwp2.dot.ca.gov` still → 200 `image/jpeg`, `Access-Control-Allow-Origin: *` → `direct`; `wzmedia.dot.ca.gov` HLS → 200 `application/vnd.apple.mpegurl` (one stream answered 404) → `direct`.
Field meanings are from `https://cwwp2.dot.ca.gov/documentation/cctv/cctv-field-description.htm`: `direction` is the direction of travel of the road, so no `headingDeg` is derived; `currentImageUpdateFrequency` is in minutes.
