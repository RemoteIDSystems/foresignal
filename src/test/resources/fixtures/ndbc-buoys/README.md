# ndbc-buoys fixtures

- `latest_obs.txt`: the first 40 lines of `GET https://www.ndbc.noaa.gov/data/latest_obs/latest_obs.txt`, captured
  2026-10-13 21:26 UTC (106,148 bytes, `ETag` and `Last-Modified`): the header, the units row and 38 stations,
  among them `15009` at `0.000 -3.051` and `32ST0` at `-22.000 -85.000`.
- `activestations.xml`: `GET https://www.ndbc.noaa.gov/activestations.xml`, captured the same day (272,962 bytes,
  `Last-Modified`, no `ETag`), cut to the XML declaration, the root element (its `count` still says 1354), the
  comment and the 38 stations of `latest_obs.txt`.

Recapture per prompt 143.
