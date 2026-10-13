# usdm fixtures

- `usdm_current-d3d4.json`: `GET https://droughtmonitor.unl.edu/data/json/usdm_current.json`, captured
  2026-10-13 22:12 UTC, cut to the D3 and D4 features and written compactly (835 KB). The full answer was
  **23,840,156 bytes**, not gzipped even when asked, with five MultiPolygons (D0 138,210 vertices, D1 43,646, D2 23,324,
  D3 20,458, D4 598) and only `OBJECTID, DM, Shape_Length, Shape_Area`.

Response headers of that capture (the tests replay them):

```
content-type: application/json
last-modified: Thu, 08 Oct 2026 10:16:12 GMT
etag: "7f7f5dbe57dd1:0"
content-length: 23840156
```

`HEAD https://droughtmonitor.unl.edu/data/json/usdm_20261006.json` answered 200 (the map date, the Tuesday before the
Thursday release), `usdm_20260929.json` 200 and `usdm_20261013.json` 404. Recapture per prompt 143.
