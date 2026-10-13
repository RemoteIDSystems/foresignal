# openaq fixtures

- **synthetic: true** — recapture with a real key (prompt 143, `FORESIGNAL_VERIFY_*`). No key was available while
  building prompt 078; `GET https://api.openaq.org/v3/parameters` answered 401 without one on 2026-10-13.
- Built from the OpenAQ v3 OpenAPI document (`https://api.openaq.org/openapi.json`, version 3.0.0, read 2026-10-13:
  `ParametersResponse` and `LatestResponse` with `Latest{datetime{utc,local}, value, coordinates{latitude,longitude},
  sensorsId, locationsId}`) and the examples on `docs.openaq.org/resources/parameters` and `/resources/latest`
  (location 2178 is the docs' Albuquerque example).
- `parameters.json`: eight parameters; `o3` and `no2` appear twice with different units, as in v3.
- `latest-2-page1.json`: parameter 2 (pm25), seven results: location 4761 twice (21:00 and 20:00, the newer wins),
  one 11 h old (`maxAge` 6 h drops it at 2026-10-13T21:30Z), one without coordinates, one at exactly (0, 0).
