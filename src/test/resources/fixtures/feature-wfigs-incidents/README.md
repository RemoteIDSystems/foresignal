# feature-wfigs-incidents fixtures (prompt 073)

Layer: `https://services3.arcgis.com/T4QMspbfLg3qTGWY/arcgis/rest/services/WFIGS_Incident_Locations_Current/FeatureServer/0`

- `layer.json`: `{layer}?f=json`, captured 2026-10-13 17:52 UTC with
  `curl -s --compressed -A "Foresignal-fixture/0.1.0-SNAPSHOT" <url>`; HTTP 200. Trimmed to `currentVersion`, `id`,
  `name`, `type`, `geometryType`, `objectIdField`, `maxRecordCount` (2000),
  `advancedQueryCapabilities.supportsPagination` (true), `editingInfo` (all three dates present) and the twelve fields
  the spec and the tests use.
- `query-page-1.json`: `{layer}/query?where=1%3D1&outFields=IrwinID,IncidentName,IncidentSize,PercentContained,POOState,POOCounty,UniqueFireIdentifier,IncidentTypeCategory,FireCause,FireDiscoveryDateTime,ModifiedOnDateTime_dt&returnGeometry=true&outSR=4326&f=geojson&resultOffset=0&resultRecordCount=5&orderByFields=OBJECTID`,
  same capture; 5 features, `properties.exceededTransferLimit: true`. Re-indented, otherwise unchanged.
- `query-page-2.json`: the same query with `resultOffset=5&resultRecordCount=3`; 3 features. **Edited:** the
  top-level `properties` member (`exceededTransferLimit: true`) was removed so the page reads as the last one.

Date fields are epoch milliseconds. Features carry no GeoJSON `id` member; identity is `IrwinID`.
