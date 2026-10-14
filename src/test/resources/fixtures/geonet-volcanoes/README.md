# `geonet-volcanoes` fixtures

Captured 2026-10-14. Source: GeoNet. Endpoint: `GET https://api.geonet.org.nz/volcano/val (Accept: application/vnd.geo+json;version=2)`. Licence: CC BY 4.0.

volcano-val.json = the first 30 GeoJSON features (nzLevel 0-5, acc colour).

Trimmed to at most 30 items. The parser tests read these; the adapter tests replay them through WireMock.

**Recapture (prompt 143):** repeat the GET above with Foresignal's identifiable User-Agent.
