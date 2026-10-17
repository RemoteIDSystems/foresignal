# Fixtures: `rtma-ru` (checkpoint 124; adapter from prompt 106)

Captured 2026-10-17 with anonymous HTTPS (User-Agent `Foresignal-build`) from `noaa-rtma-pds`, analysis
`rtma2p5_ru.20261017/rtma2p5_ru.t1100z.2dvaranl_ndfd.grb2` (84,732,283 bytes). Message ranges come from
the `.idx`.

| File | Source | Bytes | SHA-256 |
|---|---|---|---|
| `rtma2p5_ru.t1100z.2dvaranl_ndfd.grb2.idx` | `…/rtma2p5_ru.t1100z.2dvaranl_ndfd.grb2.idx` (13 messages) | 662 | `c97cc4e26974c1e96e2465c1bdcc70242bdabc722142596d3ca53183bb7238ce` |

**Not committed** (every RTMA-RU message is 5.6–7.5 MB, simple packing on 2345 × 1597, over the commit
limit): `./gradlew downloadTestFixtures` fetches it with a ranged GET into `build/test-fixtures/rtma-ru/`
and checks its pin; `RtmaAdapterContractTest.gust` skips with a logged reason when it is absent.

| File | Range of `…t1100z.2dvaranl_ndfd.grb2` | Bytes | SHA-256 |
|---|---|---|---|
| `rtma-ru-t1100z-gust.grib2` | 57580533–63198168 (msg 10, `GUST:10 m above ground:anl`) | 5,617,636 | `1906f27d0c8b8a1fe6c948e6336f3f5e996ccd69bedab8d352f6242b54458b54` |

## What they hold (read with the production `Grib2Decoder` and `GribIdx`, `RtmaAdapterContractTest`)

- The `.idx` lists `HGT`, `PRES`, `TMP:2 m`, `DPT:2 m`, `UGRD`/`VGRD:10 m`, `SPFH`, `WDIR`, `WIND`,
  `GUST:10 m`, `VIS:surface`, `CEIL` and `TCDC`, all `anl`, `d=2026101711` (hour only); every field
  the adapter reads is present.
- GUST: (0, 2, 22) at 103 / 10 m, grid template 3.30 (Lambert conformal, Dx = Dy = 2539.703 m, LoV −95°,
  Latin1 = Latin2 = 25°), scanning mode `0x40`, resolution and component flags `0x08` (grid-relative),
  **data template 5.0** (simple packing; the adapter declares `GRIB2_COMPLEX_PACKING`), valid
  2026-10-17T11:00Z; 0 … 32.37 m/s, no NaN.
