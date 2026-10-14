# `ucdp-ged` fixtures

**`synthetic: true` — recapture with a real UCDP token.** No token was available during construction.

| File | What it is |
|---|---|
| `page1.json`, `page2.json` | A two-page `gedevents/26.0.8` result in the shape of the UCDP API docs example (`TotalCount, TotalPages, PreviousPageUrl, NextPageUrl, Result`; event fields of the GED codebook 26.1): an exact event in Ukraine (`where_prec` 1, `type_of_violence` 1), a country-level one-sided event in "Yemen (North Yemen)" (`where_prec` 6, `number_of_sources` -1) and a non-state event in international waters (`where_prec` 7) |
| `probe-hit.json`, `probe-empty.json` | `?pagesize=1` probe answers with and without events |
| `revised.json` | The Ukraine event of `page1.json` revised in a later release (`best` 4, `high` 6) |
| `unauthorized-401.txt` | The real 401 body without a token, captured 2026-10-13 from `https://ucdpapi.pcr.uu.se/api/gedevents/26.0.9?pagesize=1` |

The docs' paging example (a request without `page` answering `PreviousPageUrl …page=0` and `NextPageUrl …page=2`) is
why the adapter reads the next page number from `NextPageUrl` rather than assuming a first index.
