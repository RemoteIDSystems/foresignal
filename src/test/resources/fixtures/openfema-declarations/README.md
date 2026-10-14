# `openfema-declarations` fixtures

Captured 2026-10-14. Source: OpenFEMA. Endpoint: `GET https://www.fema.gov/api/open/v2/DisasterDeclarationsSummaries?$top=30&$orderby=lastRefresh`. Licence: US Government work (public domain).

declarations.json = the first 30 rows; state-000.json = a hand-made row with fipsCountyCode 000 (state polygon).

Trimmed to at most 30 items. The parser tests read these; the adapter tests replay them through WireMock.

**Recapture (prompt 143):** repeat the GET above with Foresignal's identifiable User-Agent.
