# cwfis-perimeters fixtures (prompt 075)

- `page.json`: `https://cwfis.cfs.nrcan.gc.ca/geoserver/public/wfs?service=WFS&version=2.0.0&request=GetFeature
  &outputFormat=application/json&srsName=urn:ogc:def:crs:EPSG::4326&startIndex=0&count=1000
  &typeNames=public:m3_polygons_current`, captured 2026-10-13 about 19:30 UTC with
  `curl -s -A "Foresignal/0.1.0-SNAPSHOT (+https://foresignal.ai)" <url>`; HTTP 200, 736 features (`numberReturned`
  736), coordinates longitude first although `crs` reads `urn:ogc:def:crs:EPSG::4326`. **Edited:** trimmed to the
  first 24 features plus the largest polygon (14,208 vertices, `firstdate` 2026-05-31T18:17:00Z), serialized
  compactly; the top-level `numberReturned` still reads 736.
- **Id stability:** the prompt asked for two captures a day apart. That was not possible within one run. The single
  capture already shows the GeoServer ids are not stable: the ids (220959 to 228632, sparse) are unrelated to
  `firstdate`, and the highest ids carry the newest `lastdate`, so a row is rewritten with a new id when its polygon
  grows. The adapter therefore uses `externalId = firstdate@lat,lon` of the centroid (2 decimals).
- The fixture keeps two upstream rows (`m3_polygons_current.221200` and `.221216`) with identical properties and
  centroid: the second is a duplicate id.
