# nps-boundaries fixtures

- `boundaries.geojson`: a GeoJSON slice of the NPS Land Resources Division boundary layer
  (`https://services1.arcgis.com/fBc8EJBxQRMcHlei/arcgis/rest/services/NPS_Land_Resources_Division_Boundary_and_Tract_Data_Service/FeatureServer/2`).
  The upstream layer has 442 features.

This fixture is a **synthetic** two-feature sample (a Polygon and a MultiPolygon) using the real property
names. The simplification path is exercised by a generated > 9,500-vertex polygon in the parser test rather
than a huge fixture.

`synthetic: true` — recapture with a trimmed real slice per prompt 143.
