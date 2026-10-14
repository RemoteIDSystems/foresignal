# `gb-carbon-intensity` fixtures

Real captures, 2026-10-14, with `curl -A "Foresignal-fixture-capture (+https://foresignal.ai)"`:

- `intensity.json` — `GET https://api.carbonintensity.org.uk/intensity` (national half-hour, forecast + actual).
- `regional.json` — `GET https://api.carbonintensity.org.uk/regional`, trimmed to the first two regions.
- `generation.json` — `GET https://api.carbonintensity.org.uk/generation`.

`/intensity/pt24h` (the hourly late-actual re-read) has the same envelope as `intensity.json` and is not separately
fixtured. Licence CC BY 4.0; attribution "Carbon Intensity API (NESO, EDF, University of Oxford, WWF)".
