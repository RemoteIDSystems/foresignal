# nhc-cyclone-cones fixtures (prompt 075)

Service: `https://mapservices.weather.noaa.gov/tropical/rest/services/tropical/NHC_tropical_weather_summary/MapServer`

Captured 2026-10-13 about 19:25 UTC with `curl -s -A "Foresignal/0.1.0-SNAPSHOT (+https://foresignal.ai)" <url>`;
every request answered HTTP 200. Two active storms: Hurricane Simon (`ep202026`, advisory 25A) and Tropical
Depression Twenty-One-E (`ep212026`, advisory 1).

- `service.json`: `{service}?f=json`, trimmed to `currentVersion`, `serviceDescription`, `mapName`, `maxRecordCount`
  and the `layers` list (`id`, `name`, `parentLayerId`, `subLayerIds`, `geometryType`). Layer ids: 5 Forecast Points,
  6 Forecast Track, 7 Forecast Cone, 8 Watch-Warning.
- `layer-{points,track,cone,warning}.json`: `{service}/{id}?f=json`, trimmed to the identity, `maxRecordCount`
  (2000), `supportsPagination` (true) and the fields. No `editingInfo`; no `objectIdField` (the OID field is
  `objectid`).
- `query-{points,track,cone,warning}.json`: `{service}/{id}/query?where=1%3D1&outFields=*&returnGeometry=true
  &outSR=4326&f=geojson`, unchanged (18 points, 2 tracks, 2 cones of 1,282 and 1,484 vertices, 3 watch segments:
  one TWA and two HWA for Simon). Serialized compactly.
- `service-missing-layer.json` (hand-made): `service.json` with "Forecast Cone" renamed.

`idp_filedate` is epoch milliseconds; `validtime` is `DD/HHMM` UTC; 9999 marks a missing pressure or motion.
