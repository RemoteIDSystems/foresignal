# `nasa-eonet` fixtures

Captured 2026-10-14. Source: NASA EONET v3. Endpoint: `GET https://eonet.gsfc.nasa.gov/api/v3/events?status=open&days=30`. Licence: public domain (NASA).

events.json = the first 30 open events; polygon.json = a hand-made event whose latest geometry is a Polygon and whose first is a Point.

Trimmed to at most 30 items. The parser tests read these; the adapter tests replay them through WireMock.

**Recapture (prompt 143):** repeat the GET above with Foresignal's identifiable User-Agent.
