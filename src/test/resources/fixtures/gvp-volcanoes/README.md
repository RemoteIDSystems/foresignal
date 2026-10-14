# gvp-volcanoes fixtures

- `volcanoes.geojson`: `GET https://webservices.volcano.si.edu/geoserver/GVP-VOTW/ows?service=WFS&version=2.0.0&request=GetFeature&typeName=GVP-VOTW:Smithsonian_VOTW_Holocene_Volcanoes&outputFormat=application/json`.
  The upstream response is about 2.4 MB with 1,214 features.

This fixture is a **synthetic** three-feature sample using the real property names, including a BCE
(`Last_Eruption_Year < 0`) volcano, a volcano without `Geologic_Summary`, and normal Holocene volcanoes.

`synthetic: true` — recapture with a trimmed real slice per prompt 143.
