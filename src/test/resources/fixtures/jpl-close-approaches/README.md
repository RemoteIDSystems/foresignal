# jpl-close-approaches fixtures

- `cad.json`: `GET https://ssd-api.jpl.nasa.gov/cad.api?date-min=now&date-max=%2B60&dist-max=0.05&fullname=true`,
  captured 2026-10-13 (signature version 1.5, 42 rows, values as strings; no row lacked `h`).
- `cad-empty.json`: `…?date-min=now&date-max=%2B1&dist-max=0.0001&fullname=true` the same day: `count` 0 with no
  `fields` or `data`.
- `cad-edge.json`: **hand-made** from the first three rows of `cad.json`: the first row, the second with `h` set to
  null, the first again (a repeated id) and the third with `des` set to null.

Recapture per prompt 143.
