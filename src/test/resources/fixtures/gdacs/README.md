# gdacs fixtures (prompt 067)

Captured on 2026-10-13 at 14:50 UTC (no key needed; `synthetic: false` for the captured parts).

```sh
curl -s "https://www.gdacs.org/gdacsapi/api/events/geteventlist/MAP?eventtype=EQ" > eq.json
curl -s "https://www.gdacs.org/gdacsapi/api/events/geteventlist/MAP?eventtype=TC" > tc.json
curl -s "https://www.gdacs.org/gdacsapi/api/events/geteventlist/MAP" > 400-no-eventtype.json   # HTTP 400
```

The EQ response had 22 features (11 events, one Point_Centroid point each plus a polygon), the TC response 333
(5 events, each with a centroid point, track lines and wind-buffer polygons). No `ETag` or `Last-Modified` was sent.

| File | What it is |
|---|---|
| `MAP-EQ.json` | The EQ response trimmed to 10 features, otherwise unmodified: 7 events (6 Green, 1 Orange: `1570339`, M 7.7 in Panama) and 3 polygons. |
| `MAP-TC.json` | The TC response trimmed to 10 features: the 5 centroid points (3 Green, 2 Orange) and 5 track lines of `1001333`. |
| `MAP-EQ-next-episode.json` | Hand-made from `MAP-EQ.json`: event `1570346` moved to episode `1739999`, `datemodified` 2026-10-12T09:00:00, alert level Orange (score 2). |
| `400-no-eventtype.json` | Captured: the body of the HTTP 400 answer to a request without `eventtype`, `{"message":"Eventtype is required."}`. |

GDACS times carry no offset (`2026-10-09T18:22:15`) and are read as UTC.
