# `reliefweb-disasters` fixtures

Captured 2026-10-14. Source: ReliefWeb disasters. Endpoint: `POST https://api.reliefweb.int/v2/disasters?appname=...`. Licence: ReliefWeb terms (content rights vary).

disasters.json = hand-made (no approved appname was available): two disasters with a primary_country location and an HTML description.

Trimmed to at most 30 items. The parser tests read these; the adapter tests replay them through WireMock.

**Recapture (prompt 143):** repeat the GET above with Foresignal's identifiable User-Agent.
