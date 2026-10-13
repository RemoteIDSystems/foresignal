# inciweb-incidents fixtures (prompt 075)

- `rss.xml`: `https://inciweb.wildfire.gov/incidents/rss.xml`, captured 2026-10-13 19:29 UTC with
  `curl -s -A "Foresignal/0.1.0-SNAPSHOT (+https://foresignal.ai)" <url>`; HTTP 200, `application/rss+xml`, RSS 2.0,
  50 items, no `ETag` or `Last-Modified` (`cache-control: must-revalidate, no-cache, no-store, private`).
  **Edited:** trimmed to the first 25 items, and every `dc:creator` (staff user names) replaced with `redacted`.
  Of the 25: 22 carry a coordinate line, two of them with seconds of 60 or more (`46° 49 76`, `119° 36 82`), and
  three have an empty description.
- `edge-cases.xml` (hand-made): a description without coordinates, an Alaska position, a position outside the US
  envelope with a title without a unit code, a Guam position (east of 180°), and seconds of 60 or more.
