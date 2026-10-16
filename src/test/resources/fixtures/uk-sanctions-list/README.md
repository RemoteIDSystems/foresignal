# `uk-sanctions-list` fixtures

Captured 2026-10-14 from UK FCDO UK Sanctions List. Endpoint: `GET https://sanctionslist.fcdo.gov.uk/docs/UK-Sanctions-List.xml`. Licence: Open Government Licence v3.0.

uk-sanctions-list.xml = trimmed to 50 Designation entries plus the first entry with a UNReferenceNumber (the full list was 22,051,190 bytes, DateGenerated 12/10/2026).

**Added 2026-10-16 (post-review), a real entry, not synthetic:** the ship designation `DPR0075` (Petrel 8, `IMO9562233`, flag Comoros) copied verbatim from the 2026-10-16 capture (22,051,514 bytes, DateGenerated 16/10/2026) and appended, so the fixture has a ship. `AFG0001` (the first entry) is the entity with a UN reference (`TAe.010`).

**Recapture (prompt 143):** repeat the GET above with Foresignal's identifiable User-Agent; trim with the same tag-block selection so the XML structure stays intact.
