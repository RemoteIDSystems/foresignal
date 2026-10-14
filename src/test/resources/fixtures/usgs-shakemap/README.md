# `usgs-shakemap` fixtures

Captured 2026-10-14 from USGS:

| File | What it is |
|---|---|
| `index.geojson` | The FDSN event index `format=geojson&producttype=shakemap&minmagnitude=5&orderby=time` (3 events) |
| `detail.json` | The detail document of event `us6000u24c` (shakemap maxMMI 5.707, loss-PAGER `green`) |
| `cont_mmi.json` | The `download/cont_mmi.json` MMI contours of that event (4 MultiLineString features) |

Licence: US-PD, <https://www.usgs.gov/information-policies-and-instructions/copyrights-and-credits>.

The test rewrites the index `detail` links and the detail `cont_mmi` URL to the stub server.

**Recapture (prompt 143):** the index URL to `index.geojson`; `.../event/1/query?eventid=us6000u24c&format=geojson`
to `detail.json`; then the `download/cont_mmi.json` URL from the detail to `cont_mmi.json`.
