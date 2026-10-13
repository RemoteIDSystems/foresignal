# Fixtures: `gfs` (prompt 051)

Captured 2026-10-12 with anonymous HTTPS from `noaa-gfs-bdp-pds`, run `gfs.20261012/12`. Message
ranges come from the step's `.idx`: a message ends one byte before the next one starts.

| File | Source | Bytes | SHA-256 |
|---|---|---|---|
| `gfs.t12z.pgrb2.0p25.f006.idx` | `…/atmos/gfs.t12z.pgrb2.0p25.f006.idx` (743 messages; `APCP:surface:0-6 hour acc fcst` listed twice) | 40,476 | — |
| `gfs.t12z.pgrb2.0p25.f000.idx` | `…/atmos/gfs.t12z.pgrb2.0p25.f000.idx` (no APCP at f000) | 31,817 | — |
| `gfs.t12z.pgrb2.1p00.f006.idx` | `…/atmos/gfs.t12z.pgrb2.1p00.f006.idx` | 39,708 | — |
| `gfs-1p00-f006-gust.grib2` | `…pgrb2.1p00.f006`, bytes 586148–641118 (msg 14, `GUST:surface`) | 54,971 | `adb359981abaa7c636fba2fe8c688aec45b14fb0d8998df3fb37b4345a61df1d` |
| `gfs-1p00-f006-tmp2m.grib2` | bytes 35230937–35279061 (msg 581, `TMP:2 m above ground`) | 48,125 | `8e55badb63da65919bac9be7a900b609db76eeab5a71a6624e15c3210ec0783a` |
| `gfs-1p00-f006-ugrd-vgrd.grib2` | bytes 35666549–35825559 (msgs 588–589, `UGRD`/`VGRD` `10 m above ground`, one coalesced range) | 159,011 | `99f4134953ccba00f846f86ffb260c61757b59e7f073994077aa950fc04bd5f0` |
| `gfs-1p00-f006-apcp.grib2` | bytes 36127136–36163721 (msg 596, `APCP:surface:0-6 hour acc fcst`) | 36,586 | `933c4d0c073ef59e1164398a36537501421d4cdcb4d6bd9c9ce8645045bf2ef1` |

The messages come from the 1° file (the tests reach it through the `pathTemplate` param) because the
0.25° messages are 0.4–1 MB each; the fields, parameters and packing are the same.

## What they hold (read with NetCDF-Java 5.11.0)

- Grid template 3.0, 360 × 181, first point 90° N 0° E, 1°, scanning mode 0 (rows already north to
  south); resolution and component flags `0x30` (earth-relative u/v); data template 5.3.
- `UGRD` −20.45 … 24.69 m/s, `VGRD` −26.53 … 24.55 m/s, `GUST` 0.08 … 34.78 m/s, `TMP` 213.19 … 315.99 K,
  `APCP` 0 … 85.875 kg m⁻² (product template 4.8, forecast time 0 = the interval start).
- The 0.25° file of the same step is 547,344,038 bytes; its five wanted messages total about 3.5 MB.
