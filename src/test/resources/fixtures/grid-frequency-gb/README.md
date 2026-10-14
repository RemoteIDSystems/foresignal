# `grid-frequency-gb` fixtures

Real capture, 2026-10-14, with
`curl -A "Foresignal-fixture-capture (+https://foresignal.ai)" "https://data.elexon.co.uk/bmrs/api/v1/system/frequency?from=2026-10-13T00:00:00Z&to=2026-10-13T00:05:00Z"`,
trimmed to the first eight 15-second samples.

- `frequency.json` — `{metadata: {datasets: ["FREQ"]}, data: [{measurementTime, frequency}]}` (all readings in band).

Attribution "Contains BMRS data © Elexon Limited copyright and database right 2026"; licence "BMRS Data Licence".
