# ourairports fixtures

- `airports.csv`: `GET https://davidmegginson.github.io/ourairports-data/airports.csv` (rebuilt nightly).
- `navaids.csv`: `GET https://davidmegginson.github.io/ourairports-data/navaids.csv`.
- `runways.csv`: `GET https://davidmegginson.github.io/ourairports-data/runways.csv` (the chart symbols).
- `airport-frequencies.csv`: `GET https://davidmegginson.github.io/ourairports-data/airport-frequencies.csv`
  (the towered airports).

All four are **synthetic** small samples using the real column headers (large/heliport/closed rows, one without
coordinates; runways with a heading only at the high end, one with neither heading but both end
coordinates, and one closed). The production files are much larger.

`synthetic: true` — recapture with trimmed real slices per prompt 143.
