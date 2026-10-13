# cctv-nsw fixtures (prompt 072)

- `traffic-cam.json`: captured 2026-10-13 17:17 UTC from `https://data.livetraffic.com/cameras/traffic-cam.json` with `curl -s --compressed -A "Foresignal-fixture/0.1.0-SNAPSHOT" <url>`.
  HTTP 200, 146,682 bytes, 250 features; `ETag: W/"6935708879f74b25463028536e1136e7"`, `Last-Modified: Mon, 12 Oct 2026 21:35:58 GMT`; `rights.licence` links the Transport for NSW open-data page.
  Trimmed to the first 24 features plus the only feature whose `href` is not on `webcams.transport.nsw.gov.au` (a real off-host frame URL on `data.livetraffic.com`).
- `edge-cases.geojson`: hand-made (`synthetic: true`): no geometry, string coordinates, out of bounds, off-host and `http:` frame URLs, a duplicate id, a feature without an id.

Frame-access probe: `https://webcams.transport.nsw.gov.au/livetraffic-webcams/cameras/5_ways_miranda.jpeg` → 200 `text/html` (not the frame) → `browser`.
