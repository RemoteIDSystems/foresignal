# cwfis-fwi-stations fixtures (prompt 075)

- `page.json`: `https://cwfis.cfs.nrcan.gc.ca/geoserver/public/wfs?service=WFS&version=2.0.0&request=GetFeature
  &outputFormat=application/json&srsName=urn:ogc:def:crs:EPSG::4326&count=1000&startIndex=0&sortBy=aes
  &typeNames=public:firewx_stns_current`, captured 2026-10-13 about 19:35 UTC; HTTP 200, 1000 of 1020 features
  (`numberMatched` 1020; the second page had 20). **Without `sortBy` GeoServer refuses `startIndex` paging**
  ("Cannot do natural order without a primary key"), so the adapter sends `sortBy=aes`. **Edited:** trimmed to the two
  entries of `aes` 2300503 (Baker Lake: an upper-air `ua: "@"` entry and the surface entry) plus the first 23 other
  features. Every captured `name` is space-padded; several `agency` values are too.
