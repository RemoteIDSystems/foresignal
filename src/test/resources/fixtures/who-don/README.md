# `who-don` fixtures

Captured 2026-10-14. Source: WHO Disease Outbreak News. Endpoint: `GET https://www.who.int/api/emergencies/diseaseoutbreaknews?$orderby=PublicationDate desc&$top=30`. Licence: CC BY-NC-SA 3.0 IGO (WHO).

diseaseoutbreaknews.json = the first 30 rows; multi-country.json = hand-made (a title with two countries, and an OverrideTitle).

Trimmed to at most 30 items. The parser tests read these; the adapter tests replay them through WireMock.

**Recapture (prompt 143):** repeat the GET above with Foresignal's identifiable User-Agent.
