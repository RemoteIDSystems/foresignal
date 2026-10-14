# ourairports fixtures

- `airports.csv`: `GET https://davidmegginson.github.io/ourairports-data/airports.csv` (rebuilt nightly).
- `navaids.csv`: `GET https://davidmegginson.github.io/ourairports-data/navaids.csv`.

Both are **synthetic** small samples using the real column headers (large/heliport/closed rows, one without
coordinates). The production files are much larger.

`synthetic: true` — recapture with trimmed real slices per prompt 143.
