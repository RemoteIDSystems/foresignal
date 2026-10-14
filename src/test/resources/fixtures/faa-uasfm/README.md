# Fixtures: `faa-uasfm` (prompt 087)

| File | What it is |
|---|---|
| `layer.json` | A real capture of the layer document (`…/FAA_UAS_FacilityMap_Data/FeatureServer/0?f=json`): `maxRecordCount` 2000, pagination supported, `editingInfo.lastEditDate` 1791942657524 (2026-10-14T01:50:57Z), `LAST_EDIT` and `MAP_EFF` are `esriFieldTypeString`. |
| `page-austin.json` | A real query page: the 77 cells intersecting `-97.80,30.20,-97.70,30.30` (Austin), GeoJSON, the adapter's `outFields`. The tests split it into two pages and edit copies of two cells for the incremental read. |

## Capture procedure

- **Date:** 2026-10-14 04:35 UTC.
- **Commands:**

  ```sh
  B=https://services6.arcgis.com/ssFJjBXIUyZDrSYZ/ArcGIS/rest/services/FAA_UAS_FacilityMap_Data/FeatureServer/0
  curl -sS -A "Foresignal/0.1.0 fixture-capture (+https://foresignal.ai)" -o layer.json "$B?f=json"
  curl -sS -A "Foresignal/0.1.0 fixture-capture (+https://foresignal.ai)" -o page-austin.json \
    "$B/query?where=1%3D1&outFields=CEILING,UNIT,MAP_EFF,LAST_EDIT,GLOBALID,ARPT_COUNT,APT1_FAAID,APT2_FAAID,APT3_FAAID,APT4_FAAID,APT5_FAAID,APT1_LAANC,APT2_LAANC,APT3_LAANC,APT4_LAANC,APT5_LAANC&returnGeometry=true&outSR=4326&f=geojson&resultOffset=0&resultRecordCount=2000&orderByFields=OBJECTID&geometry=-97.80,30.20,-97.70,30.30&geometryType=esriGeometryEnvelope&inSR=4326&spatialRel=esriSpatialRelIntersects"
  ```

**Which publication is live:** the organization lists `FAA_UAS_FacilityMap_Data` (edited 2026-10-14, 380,860 cells),
`_V5` (last edited 2023-04-03, 370,441), `_Primary` (2025-08-06) and `_Secondary` (2025-08-08, both 371,352), plus
test copies. The unversioned service is the live one. A `where=LAST_EDIT IN ('10/1/2026',…)` count query answered
600, so the text-date match works on this layer.
