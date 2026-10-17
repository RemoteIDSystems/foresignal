# Fixtures: `gfs-wave` (checkpoint 124; adapter from prompt 106)

Captured 2026-10-17 with anonymous HTTPS (User-Agent `Foresignal-build`) from `noaa-gfs-bdp-pds`, run
`gfs.20261017/00`, step f006: `gfs.20261017/00/wave/gridded/gfswave.t00z.global.0p25.f006.grib2`
(11,446,249 bytes). Message ranges come from the `.idx`: a message ends one byte before the next starts.
(Before this directory existed, the inventory guard matched `gfs-wave` to the GFS atmosphere fixtures
in `gfs/` through the id prefix; those hold no wave message.)

| File | Source | Bytes | SHA-256 |
|---|---|---|---|
| `gfswave.t00z.global.0p25.f006.grib2.idx` | `…/gfswave.t00z.global.0p25.f006.grib2.idx` (19 messages) | 1,004 | `a261102ab748b06353ff501159485ad1273a6d4b3b22fb951247a2586c9579aa` |
| `gfswave-t00z-f006-htsgw.grib2` | `…f006.grib2`, bytes 3011229–3443043 (msg 5, `HTSGW:surface:6 hour fcst`) | 431,815 | `9d354d2e8d20d41634a47108572e07b4a2e9efbe715eff406a0962f7dba95023` |

## What they hold (read with the production `Grib2Decoder` and `GribIdx`, `GfsWaveAdapterContractTest`)

- The `.idx` lists `HTSGW:surface` msg 5, `PERPW:surface` msg 6 and `DIRPW:surface` msg 7 (the three
  products), among 19 messages (wind, swell partitions, wind-wave fields).
- HTSGW: (10, 0, 3) at level type 1, grid template 3.0, 1440 × 721, first point 90° N 0° E, 0.25°,
  **data template 5.40 (JPEG 2000)**, which NetCDF-Java decodes, although the adapter declares only
  `GRIB2_COMPLEX_PACKING`. Reference 2026-10-17T00:00Z, valid 06:00Z; 0 … 10.65 m, 470,952 NaN
  (land and ice) of 1,038,240 cells.
