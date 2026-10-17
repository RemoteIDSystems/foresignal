# Fixtures: `ecmwf-ifs` (checkpoint 124; adapter from prompt 105)

Captured 2026-10-17 with anonymous HTTPS (User-Agent `Foresignal-build`) from ECMWF open data, run
`20261017/00z`, step 24 (`20261017/00z/ifs/0p25/oper/20261017000000-24h-oper-fc.*`). The adapter reads
the same keys from the `ecmwf-forecasts` S3 mirror; during the capture the mirror answered `503 SlowDown`
to GETs, so the bytes come from `https://data.ecmwf.int/forecasts/…`. A `HEAD` on the mirror gave the
same sizes (`.index` 40,210 bytes, `.grib2` 143,587,252 bytes). Message ranges come from the step's JSON
`.index` (`_offset`, `_length`).

| File | Source | Bytes | SHA-256 |
|---|---|---|---|
| `20261017000000-24h-oper-fc.index` | `…/20261017000000-24h-oper-fc.index` (184 entries) | 40,210 | `07d0a182c4c0d45b9cb21b8ea79d638f7af319c6659297d534974a0b1ae1fcd5` |
| `ifs-24h-msl.grib2` | `…-24h-oper-fc.grib2`, bytes 96826485–97347949 (`msl`, `sfc`) | 521,465 | `a2aa8b4d3613f71d0c5953f54497c24e2d89533b984cd62f91b0557a585752db` |
| `ifs-24h-tp.grib2` | `…-24h-oper-fc.grib2`, bytes 0–817045 (`tp`, `sfc`) | 817,046 | `48fcfe457b9b5cf7d84c56f5abe734adee1a166fb694cf9c253d1423efbe61c1` |

The inventory is `…-fc.index`, **not** `…-fc.grib2.index`: the second answers 404 on the mirror. The
engine used to request the second; `EcmwfRunIngest.indexKey` now strips `.grib2` (checkpoint 124).

## What they hold (read with the production `Grib2Decoder`, `EcmwfAdapterContractTest`)

- The `.index` lists `tp`, `10u`, `10v`, `2t`, `10fg` and `msl` at `sfc` (every component of every IFS
  product); `10fg` is written as plain `10fg` at f024.
- Both messages: grid template 3.0, 1440 × 721, first point 90° N 180° W, 0.25°, scanning mode 0, data
  template 5.42 (CCSDS), center 98 (ECMWF), reference time 2026-10-17T00:00Z.
- `msl` (0, 3, 0) at level type 101: 94,572.375 … 103,928.375 Pa, valid 2026-10-18T00:00Z, no NaN.
- `tp` (0, 1, 193) at level type 1, product template 4.8, accumulated from the run start: 0 … 0.29110 m
  (291.1 mm with the product's ×1000), no NaN.
