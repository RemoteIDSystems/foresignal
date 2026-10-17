# `worldbank-indicators` fixtures

Real captures, 2026-10-16, with `curl -A "Foresignal/0.1.0-SNAPSHOT (+https://foresignal.ai; install=fixture)"`:

- `countries.json` — `GET https://api.worldbank.org/v2/country?format=json&per_page=400`, trimmed to Arab World and
  World (aggregates: `region.id = NA`), Germany, Kosovo (`XKX` → `XK`), the United States and Yemen.
- `indicator-SP.POP.TOTL.json` — `GET https://api.worldbank.org/v2/indicator/SP.POP.TOTL?format=json` (the
  `sourceOrganization` the record carries).
- `data-page1.json`, `data-page2.json` — `GET https://api.worldbank.org/v2/country/all/indicator/SP.POP.TOTL?format=json&per_page=20000&date=2022:2023`
  filtered to the six entities above and split into two pages (the header's `page`, `pages`, `per_page` and `total`
  rewritten to match), so the paging test has two real pages.
- `invalid-indicator.json` — the HTTP-200 error body for an unknown indicator id.

License: CC BY 4.0 for most World Development Indicators; the indicator's `sourceOrganization` is recorded.
