# `eia-petroleum` fixtures

Synthetic: built by hand from the documented EIA v2 envelope (`response.{total, dateFormat, frequency, data[], warnings[]}`,
each row `{period, series, value (string), units}`) because the API needs an `api_key`; recapture with a real key.

- `spot.json` — `petroleum/pri/spt` daily spot rows for `RWTC` and `RBRTE` (verified shape 2026-10, API 2.1.14).
- `stock.json` — `petroleum/stoc/wstk` weekly `WCESTUS1` rows.
- `incomplete.json` — a `warnings: ["incomplete return"]` answer; the parser must fail rather than accept the
  5,000-row cap.

URL: `https://api.eia.gov/v2/petroleum/pri/spt/data/?frequency=daily&data[0]=value&facets[series][]=RWTC&length=60`.
Docs: <https://www.eia.gov/opendata/documentation.php>.
