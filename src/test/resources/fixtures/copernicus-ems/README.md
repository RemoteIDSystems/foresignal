# `copernicus-ems` fixtures

Captured 2026-10-14. Source: Copernicus EMS. Endpoint: `GET https://rapidmapping.emergency.copernicus.eu/backend/dashboard-api/public-activations-info/?limit=30&offset=0`. Licence: Copernicus EMS (attribution).

activations-info.json = the first 30 activations; detail.json = public-activations/?code=EMSR934 (reason, subCategory, reportLink).

Trimmed to at most 30 items. The parser tests read these; the adapter tests replay them through WireMock.

**Recapture (prompt 143):** repeat the GET above with Foresignal's identifiable User-Agent.
