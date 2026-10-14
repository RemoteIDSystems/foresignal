# `wpc-ero-d4` fixtures

Captured 2026-10-14 from the WPC Excessive Rainfall Outlook Day 4 layer (index 3):
<https://mapservices.weather.noaa.gov/vector/rest/services/hazards/wpc_precip_hazards/MapServer/3>. `layer.geojson`
is a GeoJSON query (`where=1=1&outFields=*&returnGeometry=true&f=geojson&resultRecordCount=3`) with categorical
`dn` (1 Marginal, 2 Slight, 3 Moderate, 4 High), `outlook`, `issue_time`, `start_time` and `end_time` (string
`yyyy-MM-dd HH:mm:ss`, UTC). Licence: US-PD, attribution NOAA Weather Prediction Center.

**Recapture (prompt 143):** the same query URL to `layer.geojson`.
