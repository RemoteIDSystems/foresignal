# airnow-hourly fixture

- **synthetic: true** — recapture with a real key (prompt 143, `FORESIGNAL_VERIFY_*`). No key was available while
  building prompt 078; `https://www.airnowapi.org/aq/data/` answered 401 without one on 2026-10-13, and the query
  documentation (`docs.airnowapi.org/Data/docs`) is behind the AirNow API login.
- `data.json` follows the "Observations by Monitoring Site" JSON answer with `verbose=1` and `dataType=B` as
  Foresignal expects it (`Latitude, Longitude, UTC, Parameter, Unit, Value, RawConcentration, AQI, Category,
  SiteName, AgencyName, FullAQSCode, IntlAQSCode`, `-999` for a missing number). Field names, the hour format and
  the missing marker are unverified until a live capture. Values are invented; the sites are real Colorado
  monitors named for realism.
- Five rows: four records (Denver CAMP PM2.5 and ozone, Welby PM2.5, Fort Collins ozone) and one row with every
  value missing (Welby ozone, skipped as `no-values`).
