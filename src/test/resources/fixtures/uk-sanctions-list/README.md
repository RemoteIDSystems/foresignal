# `uk-sanctions-list` fixtures

Captured 2026-10-14 from UK FCDO UK Sanctions List. Endpoint: `GET https://sanctionslist.fcdo.gov.uk/docs/UK-Sanctions-List.xml`. Licence: Open Government Licence v3.0.

uk-sanctions-list.xml = trimmed to 50 Designation entries plus the first entry with a UNReferenceNumber (the full list was 22,051,190 bytes, DateGenerated 12/10/2026).

**Recapture (prompt 143):** repeat the GET above with Foresignal's identifiable User-Agent; trim with the same tag-block selection so the XML structure stays intact.
