# `eia-930` fixtures

Synthetic: built by hand from the documented EIA v2 envelope because the API needs an `api_key`; recapture with a
real key.

- `region.json` — `electricity/rto/region-data` rows for ERCOT (`D`, `DF`, `NG`, `TI`) and one California ISO row.
- `fuel.json` — `electricity/rto/fuel-type-data` rows for ERCOT (natural gas, wind, solar, nuclear).

URL: `https://api.eia.gov/v2/electricity/rto/region-data/data/?frequency=hourly&data[0]=value&facets[respondent][]=ERCO&facets[type][]=D&start=…&length=48`.
Docs: <https://www.eia.gov/opendata/documentation.php>.
