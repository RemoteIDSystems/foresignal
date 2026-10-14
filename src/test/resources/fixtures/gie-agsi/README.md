# `gie-agsi` fixtures

- `access-denied.json` — the real HTTP-200 body AGSI returns to a missing or invalid key, captured 2026-10-14 from
  `https://agsi.gie.eu/api?type=eu&size=2`. The adapter must turn this into a rejected credential.
- `storage.json` — synthetic, built from the GIE API manual v013 field list (no key during construction); recapture
  with a real key. It holds Germany (`C`, final), France (`E`, estimated) and the EU aggregate (`N`, no data, which
  the parser skips).

URL: `https://agsi.gie.eu/api?type=eu&size=30`, header `x-key`. Manual:
<https://www.gie.eu/transparency-platform/GIE_API_documentation_v013.pdf>.
