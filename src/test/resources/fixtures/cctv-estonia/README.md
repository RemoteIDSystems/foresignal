# cctv-estonia fixtures (prompt 072)

- `roadCameraLocations.xml` and `roadCameraImages.xml`: captured 2026-10-13 17:17 UTC from `https://tarktee.transpordiamet.ee/api/v1/datex/roadCameraLocations` and `…/roadCameraImages` with `curl -s --compressed -A "Foresignal-fixture/0.1.0-SNAPSHOT" <url>`.
  HTTP 200 (`application/xml`), 120,935 and 162,234 bytes, 176 locations and 176 traffic views; no `ETag` or `Last-Modified`.
  Trimmed to the first 25 locations and the traffic views of the first 23 of them, so two locations have no current image.
- `edge-locations.xml` / `edge-images.xml`: hand-made (`synthetic: true`): no coordinates, out of bounds, off-host image, a location without an image, a duplicate id.
- `xxe-locations.xml`: hand-made: a DOCTYPE with an external entity, which the hardened reader must not resolve.

Frame-access probe: `https://tarktee.transpordiamet.ee/images/987/987_202610132006.jpg` → 200 `image/jpeg` → `direct`. The image file name changes with every new image, so `imageUrl` changes between fetches.
