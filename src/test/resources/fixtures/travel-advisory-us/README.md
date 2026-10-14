# `travel-advisory-us` fixtures

Captured 2026-10-14. Source: US State Department travel advisories. Endpoint: `GET https://cadataapi.state.gov/api/TravelAdvisories`. Licence: US Government work (public domain).

advisories.json = the first 30 advisories (Category is a FIPS array); no-level.json = hand-made (a title without a level, and a level-2 title).

Trimmed to at most 30 items. The parser tests read these; the adapter tests replay them through WireMock.

**Recapture (prompt 143):** repeat the GET above with Foresignal's identifiable User-Agent.
