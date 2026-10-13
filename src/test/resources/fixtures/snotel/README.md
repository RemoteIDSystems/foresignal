# snotel fixtures

Captured 2026-10-13 22:11 UTC from the NRCS AWDB REST API.

- `stations.json`: `GET https://wcc.sc.egov.usda.gov/awdbRestApi/services/v1/stations?stationTriplets=*:CO:SNTL&returnForecastPointMetadata=false`
  (118 stations, all `networkCode` `SNTL`, all `dataTimeZone` `-8.0`), cut to the three stations of `data.json`. The
  `stateCds=CO&networkCds=SNTL` form returned 4,398 stations of every network, so the triplet wildcard is used.
- `data.json`: `GET …/v1/data?stationTriplets=1344:CO:SNTL,335:CO:SNTL,1030:CO:SNTL&elements=WTEQ,SNWD,PREC,TOBS&duration=DAILY&beginDate=2026-10-11&endDate=2026-10-13`,
  whole (values for 2026-10-11 and 2026-10-12; `WTEQ`, `SNWD`, `PREC` in inches, `TOBS` in °F).

Recapture per prompt 143.
