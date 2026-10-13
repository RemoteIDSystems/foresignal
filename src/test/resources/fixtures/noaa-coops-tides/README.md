# noaa-coops-tides fixtures

Captured 2026-10-13 22:12–22:13 UTC. Every data request carried `application=Foresignal`.

- `water_level-8518750.json`, `water_level-9414290.json`: `GET https://api.tidesandcurrents.noaa.gov/api/prod/datagetter?product=water_level&station={id}&date=latest&datum=MLLW&units=metric&time_zone=gmt&application=Foresignal&format=json`
  (The Battery, San Francisco).
- `predictions-8518750.json`: `…&product=predictions&interval=hilo&begin_date=20261013&end_date=20261014&station=8518750&datum=MLLW&units=metric&time_zone=gmt…`.
- `floodlevels-{id}.json`, `datums-{id}.json`: `GET https://api.tidesandcurrents.noaa.gov/mdapi/prod/webapi/stations/{id}/floodlevels.json`
  and `…/datums.json`. Both are in feet above the station datum (`STND` = 0); The Battery's record 17.329 ft
  (2012-10-30) minus its MLLW 3.29 ft is Sandy's 14.04 ft MLLW crest, which is how the flood levels' reference was
  confirmed (the mdapi documentation does not state it).
- `stations-waterlevels.json`: `GET …/mdapi/prod/webapi/stations.json?type=waterlevels` (302 stations, 777 KB), cut to
  8518750, 9414290 and the Great Lakes station 9063020 (Buffalo); `count` still says 302.
- `error-invalid-station.json`: the water-level request for station `0000000`.
- `error-no-mllw-9063020.json`: the water-level request for Buffalo (`There is no MLLW for the station`).

Recapture per prompt 143.
