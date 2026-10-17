# Fixtures: `aifs` (checkpoint 124; adapter from prompt 106)

Captured 2026-10-17 with anonymous HTTPS (User-Agent `Foresignal-build`) from ECMWF open data, run
`20261017/00z`, step 24 (`20261017/00z/aifs-single/0p25/oper/20261017000000-24h-oper-fc.*`). The bytes
come from `https://data.ecmwf.int/forecasts/…`; the same `.index` read from the `ecmwf-forecasts` S3
mirror the adapter uses is byte-identical (same SHA-256). Message ranges come from the JSON `.index`.

| File | Source | Bytes | SHA-256 |
|---|---|---|---|
| `20261017000000-24h-oper-fc.index` | `…/20261017000000-24h-oper-fc.index` (122 entries) | 29,574 | `f52671149a112a1e044dc5cfcbc61f3b16ea3330db44fe4ddc97d37acb07d96b` |
| `aifs-24h-2t.grib2` | `…-24h-oper-fc.grib2`, bytes 82722776–83271340 (`2t`, `sfc`) | 548,565 | `0aa5242814192896c354c6ad18252ce2dc5d29b6ed834297c54b6a8915576128` |
| `aifs-24h-tp.grib2` | `…-24h-oper-fc.grib2`, bytes 14782771–15569894 (`tp`, `sfc`) | 787,124 | `2dae874f8b48ccfd24f7d5b8962f8b57fac74857e828a5d77d081a382547ece1` |

The whole `…-24h-oper-fc.grib2` is 86,266,835 bytes.

## What they hold (read with the production `Grib2Decoder`, `AifsAdapterContractTest`)

- The `.index` lists `tp`, `msl`, `10u`, `10v` and `2t` at `sfc` (every AIFS product component).
- Both messages: grid template 3.0, 1440 × 721, 90° N 180° W, 0.25°, data template 5.42 (CCSDS),
  center 98.
- `2t` (0, 0, 0) at 103 / 2 m: 212.2078 … 308.8016 K, valid 2026-10-18T00:00Z, no NaN.
- `tp` (0, 1, 52) at level type 1, product template 4.8: 0 … 205.0195 **kg m⁻², which is already mm**.
  The IFS `tp` of the same run is (0, 1, 193) in meters and peaks at 0.291 m, so the AIFS product takes
  no ×1000; the adapter's factor was 1000 until checkpoint 124.
