# nuclear-reactors-us fixtures

- `reactors.geojson`: a GeoJSON slice of `GET https://gis.fema.gov/arcgis/rest/services/Partner/Nuclear_Plant_Power_Reactor_Status/FeatureServer/0/query?where=1=1&outFields=*&f=geojson`
  (99 reactors upstream, updated daily from the NRC).

This fixture is a **synthetic** two-reactor sample using the real field names.

`synthetic: true` — recapture with a trimmed real slice per prompt 143.
