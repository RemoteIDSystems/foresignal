# wri-power-plants fixtures

- `gppd.csv`: `GET https://raw.githubusercontent.com/wri/global-power-plant-database/master/output_database/global_power_plant_database.csv`.
  The upstream file is about 12 MB and frozen at v1.3.0 (2021); this fixture is a **synthetic** four-row
  sample built from the published header (one row without `gppd_idnr`, one with a missing capacity) so the
  parser contract and the row-skipping path are exercised. It carries the real column names.

`synthetic: true` — recapture with a trimmed slice of the real file per prompt 143.
