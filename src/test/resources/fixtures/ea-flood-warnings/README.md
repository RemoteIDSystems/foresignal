# ea-flood-warnings fixtures

Captured 2026-10-13 22:13 UTC.

- `floods.json`: `GET https://environment.data.gov.uk/flood-monitoring/id/floods` (10 KB, `Last-Modified`, no `ETag`):
  six tidal flood alerts (severity level 3); `meta.licence` is OGL v3. Times are UK local time without an offset.
- `polygon-054WACDV4A.json`: `GET https://environment.data.gov.uk/flood-monitoring/id/floodAreas/054WACDV4A/polygon`
  (the tidal Deben estuary, 346 KB pretty-printed), written compactly: one Feature, a MultiPolygon of 4,172 vertices.

The `message` texts are the agency's and are stored as untrusted text; none addressed an agent. Recapture per
prompt 143.
