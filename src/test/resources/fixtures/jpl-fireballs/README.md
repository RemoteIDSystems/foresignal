# jpl-fireballs fixtures

- `fireball.json`: `GET https://ssd-api.jpl.nasa.gov/fireball.api?date-min=2026-04-01&req-loc=true`, captured
  2026-10-13 (signature version 1.2, 18 rows; several rows have `vel` null). The 30-day window of the adapter held
  only 2 rows that day, so a longer window was captured.
- `fireball-empty.json`: `…?date-min=2026-10-12&req-loc=true` the same day: `count` `"0"` with no `fields` or
  `data`.

Recapture per prompt 143.
