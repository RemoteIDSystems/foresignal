# `ember-electricity` fixtures

Synthetic: built from the documented Ember v1 monthly shape (the API answers 403 `{"detail":"No API key set"}`
without a key; captured 2026-10-14); recapture with a real key.

- `generation.json` — `electricity-generation/monthly` rows for `USA` and `GBR` (two series and two months).
- `options.json` — the `/v1/options/electricity-generation/monthly/date` month list, the change marker.

URL: `https://api.ember-energy.org/v1/electricity-generation/monthly?entity_code=USA&start_date=2025-10&api_key=…`.
Docs: <https://api.ember-energy.org/v1/docs>.
