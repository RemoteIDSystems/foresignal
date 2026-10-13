# nwps-gauges fixtures

Captured 2026-10-13 22:11 UTC.

- `gauges-denver.json`: `GET https://api.water.noaa.gov/nwps/v1/gauges?bbox.xmin=-105.5&bbox.ymin=39.5&bbox.xmax=-104.5&bbox.ymax=40.2&srid=EPSG_4326`
  (38 gauges, 38 KB), cut to `BBCC2`, `CGCC2` (`obs_not_current` with a valid time), `DNVC2` and `GSEC2` (observed
  `-999` and `0001-01-01T00:00:00Z`). No gauge had a current forecast (`fcst_not_current`).
- `gauges-forecast.json`: the same request for `-92,36,-88,40` (172 gauges), cut to `AKFM7` and `ALNI2` (current
  forecasts; `AKFM7` in `low_threshold`) and `CTVI2` (`out_of_service`, every value a sentinel).
- `detail-DNVC2.json`: `GET https://api.water.noaa.gov/nwps/v1/gauges/DNVC2` (South Platte River at Denver, 37 KB),
  whole: flood stages 9.5 / 11 / 13 / 15 ft, a `-9999` moderate flow, a blank `usgsId`, `inService.enabled: true`.

Recapture per prompt 143.
