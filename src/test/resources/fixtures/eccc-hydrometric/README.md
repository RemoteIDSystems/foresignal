# eccc-hydrometric fixtures

Captured 2026-10-13 22:14 UTC from the MSC GeoMet OGC API.

- `items-page1.json`: `GET https://api.weather.gc.ca/collections/hydrometric-realtime/items?f=json&limit=5&bbox=-76,45,-75,45.6&datetime=2026-10-13T20:14:07Z/..&sortby=-DATETIME`
  (201 rows matched: one per station per 5 minutes; offset paging through `links[rel=next]`). Tests rewrite the
  absolute `https://api.weather.gc.ca` links to the stub server.
- `items-page2.json`: that page's `next` link (`offset=5`), with its own `next` link removed. Together: ten rows of
  seven stations, `02LB022` twice (21:45 and 21:40).

Recapture per prompt 143.
