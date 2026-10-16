# Fixtures: `nexrad-l3` (prompt 106, post-review fixes)

Real NEXRAD Level III files from the Unidata bucket on AWS (`unidata-nexrad-level3`, anonymous access),
captured 2026-10-16 with a plain anonymous GET of each key. They are committed as published (a WMO/AWIPS text
header followed by the NIDS message). US public domain (NOAA/NWS).

Capture procedure: list `https://unidata-nexrad-level3.s3.amazonaws.com/?list-type=2&prefix={SITE}_{N0B|N0G}_{yyyy}_{MM}_{dd}_{HH}`
and GET one key (each file is well under 2 MB).

| File | Product | Bytes | SHA-256 |
|---|---|---|---|
| `FWS_N0B_2026_10_16_19_15_25` | 153, super-resolution base reflectivity | 249,815 | `3b8fe955c6f4ac1717e115a98728c28b05ba3398c760d9bde10921fb859eb4d4` |
| `FWS_N0G_2026_10_16_19_01_16` | 154, super-resolution base velocity | 216,927 | `f116e32fbe1ed271fcf29bfcac0f40bf0a2cac13a750020280f8233164027a2d` |

## What NetCDF-Java 5.11.0 (`cdm-radial`, NIDS IOSP) reads from them

Checked 2026-10-16 by opening each file with `NetcdfFiles.open`:

- IOSP id `NIDS`; global `RadarLatitude` 32.573, `RadarLongitude` −97.303 (Dallas/Fort Worth),
  `time_coverage_start` `2026-10-16T19:15:25Z` (N0B) and `2026-10-16T19:01:16Z` (N0G).
- `azimuth(azimuth=720)` degrees (0 = true north), 0.5° radials; `gate` "Radial distance to the start of
  gate", meters, 0 … 459,290.25 (N0B, 1,840 gates) and 0 … 299,450.25 (N0G, 1,200 gates), 249.75 m apart.
- N0B: `BaseReflectivityDR(azimuth, gate)` float, units `dBz`, −26.0 … 49.0, NaN for no data (979,317 of
  1,324,800 samples).
- N0G: `BaseVelocityDV(azimuth, gate)` float, units `m/s`, −62.0 … 50.0, NaN for no data (568,449 of 864,000).
- Resampled to 0.005° within 230 km (`PolarResampler`): a 981 × 827 grid, 189,220 (N0B) and 148,968 (N0G)
  cells with a value; N0B maximum 45.5 dBZ.
