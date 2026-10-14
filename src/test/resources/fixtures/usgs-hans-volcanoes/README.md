# `usgs-hans-volcanoes` fixtures

Captured 2026-10-14. Source: USGS HANS. Endpoint: `GET https://volcanoes.usgs.gov/hans-public/api/volcano/getMonitoredVolcanoes and .../getUSVolcanoes`. Licence: US-PD.

monitored.json and us-volcanoes.json = the first 30 rows each. sent_utc is 'yyyy-MM-dd HH:mm:ss' (UTC).

Trimmed to at most 30 items. The parser tests read these; the adapter tests replay them through WireMock.

**Recapture (prompt 143):** repeat the GET above with Foresignal's identifiable User-Agent.
