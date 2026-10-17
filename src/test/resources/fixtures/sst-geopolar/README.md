# Fixtures: `sst-geopolar` (checkpoint 124; adapter from prompt 106)

Captured 2026-10-17 with anonymous HTTPS (User-Agent `Foresignal-build`) from NOAA CoastWatch ERDDAP,
one griddap subset of `noaacwBLENDEDsstDNDaily` exactly as the adapter requests it for the bbox
83° W–80° W, 24.5° N–27° N (South Florida and the Keys, land included):

`https://coastwatch.noaa.gov/erddap/griddap/noaacwBLENDEDsstDNDaily.nc?analysed_sst[(last)][(24.5):1:(27.0)][(-83.0):1:(-80.0)]`

| File | Source | Bytes | SHA-256 |
|---|---|---|---|
| `noaacwBLENDEDsstDNDaily-last-24.5N-27N-83W-80W.nc` | the URL above | 19,936 | `405ccc528aa8c060e2db3acd0bc08fd8ef8fd444609a33de78a70ec94bed4b33` |

The bounds must be in parentheses (coordinate values). The adapter used to send
`[24.5:1:27.0][-83.0:1:-80.0]`, which griddap reads as array indices; the server answered **400** to that
form (checked 2026-10-17). Fixed in checkpoint 124.

## What it holds (read with the production `NetcdfDecoder` through `SstAdapter.fetch`, `SstAdapterContractTest`)

- NetCDF-3 classic; `analysed_sst(time=1, latitude=51, longitude=61)`, °C, `_FillValue` −327.68;
  latitude 24.525 … 27.025 **south to north**, longitude −82.975 … −79.975, 0.05°.
- Global `time_coverage_start` = `time_coverage_end` = 2026-10-15T12:00:00Z (the `(last)` analysis on
  the capture day). There is no global `time` attribute, which the adapter used to read (so every
  frame got the fetch time); it now reads `time_coverage_start`.
- 1,115 fill (land) cells; ocean 28.389994 … 29.849995 °C, i.e. u16 codes 3039 … 3185. The same
  subset requested as `.csv` from the same server gave the same count, range and spot values.
