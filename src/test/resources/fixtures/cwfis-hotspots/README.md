# cwfis-hotspots fixtures (prompt 075)

- `page.json`: `https://cwfis.cfs.nrcan.gc.ca/geoserver/public/wfs?service=WFS&version=2.0.0&request=GetFeature
  &outputFormat=application/json&srsName=urn:ogc:def:crs:EPSG::4326&startIndex=0&count=1000
  &typeNames=public:hotspots_last24hrs`, captured 2026-10-13 about 19:30 UTC; HTTP 200, 610 features. **Edited:**
  trimmed to the first 25.
- `swapped.json` (hand-made from the first three features): coordinates written latitude first, the CWFIS
  swapped-axis sample for the WFS kit's sanity check.
