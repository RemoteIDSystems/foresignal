# nifc-perimeters fixtures (prompt 075)

Layer: `https://services3.arcgis.com/T4QMspbfLg3qTGWY/arcgis/rest/services/WFIGS_Interagency_Perimeters_Current/FeatureServer/0`

Captured 2026-10-13 about 19:30 UTC with `curl -s -A "Foresignal/0.1.0-SNAPSHOT (+https://foresignal.ai)" <url>`;
every request answered HTTP 200.

- `layer.json`: `{layer}?f=json`. Trimmed to `currentVersion`, `id`, `name`, `type`, `geometryType`,
  `objectIdField` (`OBJECTID`), `maxRecordCount` (2000), `editingInfo` (`lastEditDate`, `schemaLastEditDate`,
  `dataLastEditDate`, all present), `advancedQueryCapabilities.supportsPagination` (true) and the seventeen fields
  the adapter requests or orders by.
- `query-page-1.json`: `{layer}/query?where=1%3D1&outFields=<the sixteen GEV fields>&returnGeometry=true&outSR=4326
  &f=geojson&resultOffset=0&resultRecordCount=2000&orderByFields=OBJECTID&maxAllowableOffset=0.001` returned 126
  features (no `exceededTransferLimit`). **Edited:** trimmed to its first 24 features, and a 25th appended: the
  Wrights Spring perimeter (`2026-ORFWF-260286`, 10,750 vertices) from the same query **without**
  `maxAllowableOffset` (`orderByFields=poly_GISAcres DESC&resultOffset=3&resultRecordCount=12`), so the fixture
  exercises the simplification above 9,500 vertices. Serialized compactly.

Date fields are epoch milliseconds. Identity is `attr_UniqueFireIdentifier` (every captured feature has one, and an
`attr_IrwinID`).
