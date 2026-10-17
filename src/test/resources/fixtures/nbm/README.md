# Fixtures: `nbm` (checkpoint 124; adapter from prompt 106)

Captured 2026-10-17 with anonymous HTTPS (User-Agent `Foresignal-build`) from `noaa-nbm-grib2-pds`.
Message ranges come from the step's `.idx`: a message ends one byte before the next one starts.

| File | Source | Bytes | SHA-256 |
|---|---|---|---|
| `blend.t01z.core.f012.co.grib2.idx` | `blend.20261017/01/core/blend.t01z.core.f012.co.grib2.idx` (156 messages) | 11,693 | `a2aa26b2bee74f5e7687254f41e6d72d8ccafccdee720f246afe12c0ba0aeff8` |
| `blend.20261016.t12z.core.f012.co.grib2.idx` | `blend.20261016/12/core/blend.t12z.core.f012.co.grib2.idx` | 16,813 | `acc4d5ddc319faa7a3c75e87b5fcf9bf546fd689efbef0788fff53c2322f99fa` |
| `nbm-t01z-f012-gust.grib2` | `blend.20261017/01/core/blend.t01z.core.f012.co.grib2`, bytes 153176082–154950551 (msg 150, `GUST:10 m above ground:12 hour fcst`) | 1,774,470 | `1daf75ec4f5fa6a4f1b8893e43512b29a60de274edcc4b692cd277dc550abace` |

Every wanted NBM message is 0.9–2.1 MB (CONUS 2345 × 1597); GUST is one of the smaller ones.

## What they hold (read with the production `Grib2Decoder` and `GribIdx`, `NbmAdapterContractTest`)

- 01z f012 `.idx`: `TMP:2 m above ground` msg 133, `GUST:10 m above ground` msg 150, `WIND:10 m above
  ground` msg 154, `TCDC:surface` msg 70 (each followed by an `ens std dev` line). APCP is listed only
  as the 1-hour `11-12 hour acc` bucket (deterministic msg 68, `prob >0.254` msg 51). **There is no
  `6-12 hour acc` bucket** in the 01z file, nor in the 13z (2026-10-16) or 01z f006/f018 files checked.
- 12z f012 `.idx` (2026-10-16): `APCP:surface:6-12 hour acc fcst` msg 100 and its `prob >0.254` msg 78
  are present, plus `TCDC:surface` msg 102.
- No file lists `TCDC:entire atmosphere`; a downloaded (not committed) 01z f012 `TCDC:surface` message
  is (0, 6, 1) at level type 1. The adapter's SKY selector was `entire atmosphere` / 200 until
  checkpoint 124.
- GUST message: (0, 2, 22) at 103 / 10 m, grid template 3.30 (Lambert conformal, Dx = Dy = 2539.703 m,
  LoV −95°, Latin1 = Latin2 = 25°), scanning mode `0x50`, data template 5.3, reference
  2026-10-17T01:00Z, valid 13:00Z; 0 … 36.5 m/s, no NaN.
