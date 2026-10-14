# `unhcr-population` fixtures

Captured 2026-10-14. Source: UNHCR population. Endpoint: `GET https://api.unhcr.org/population/v1/population/?year=2024&coo_all=true&limit=30&page=1`. Licence: CC BY 4.0.

population-2024.json = the first 30 items of the 2024 country-of-origin view (coa_iso '-' rows included).

Trimmed to at most 30 items. The parser tests read these; the adapter tests replay them through WireMock.

**Recapture (prompt 143):** repeat the GET above with Foresignal's identifiable User-Agent.
