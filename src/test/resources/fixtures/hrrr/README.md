# Fixtures: `hrrr` (prompt 051)

Captured 2026-10-12 with anonymous HTTPS from `noaa-hrrr-bdp-pds`, run `hrrr.20261012/conus/hrrr.t12z`.

| File | Source | Bytes | SHA-256 |
|---|---|---|---|
| `hrrr.t12z.wrfsfcf01.grib2.idx` | `…/hrrr.t12z.wrfsfcf01.grib2.idx` (170 messages) | 10,180 | — |
| `hrrr.t12z.wrfsfcf00.grib2.idx` | `…/hrrr.t12z.wrfsfcf00.grib2.idx` | 9,002 | — |
| `hrrr-f01-refc.grib2` | `…wrfsfcf01.grib2`, bytes 0–424136 (msg 1, `REFC:entire atmosphere`) | 424,137 | `11ef0f7e02e707e3d74ba2c8c4e048684e2f48c7d9341609d9c9267a9c59bc5c` |

**Not committed** (over the 3 MB limit, or large together): `./gradlew downloadTestFixtures` fetches
these with ranged GETs into `build/test-fixtures/hrrr/` and checks their pins; the tests that need them
skip with a logged reason when they are absent.

| File | Range of `…wrfsfcf01.grib2` | Bytes | SHA-256 |
|---|---|---|---|
| `hrrr-f01-ugrd-vgrd.grib2` | 52027080–56790309 (msgs 77–78, `UGRD`/`VGRD` `10 m above ground`) | 4,763,230 | `ac2ea3d466bf3032f82c5bf62501473159892e25af628802d65c6785f3f7df04` |
| `hrrr-f01-gust.grib2` | 3314894–4566989 (msg 9, `GUST:surface`) | 1,252,096 | `8e2a8b1e33027f098e51e1687649258f039a4906a822b56d50a4aef04fc603b4` |
| `hrrr-f01-massden.grib2` | 51042719–52027079 (msg 76, `MASSDEN:8 m above ground`) | 984,361 | `31d8b170fa096b3b846ff17b4b7676165822b0dff706ea2005e870a9a9659556` |

## What they hold (read with NetCDF-Java 5.11.0)

- Grid template 3.30 (Lambert conformal), 1799 × 1059, shape of the earth 6 (sphere 6,371,229 m), La1
  21.138123° N, Lo1 237.280472° E (−122.719528°), LaD = Latin1 = Latin2 = 38.5°, LoV 262.5° (−97.5°),
  Dx = Dy = 3000 m, scanning mode `0x40` (rows south to north), resolution and component flags `0x08`
  (**grid-relative** u/v).
- `REFC` −10 … 67.6 dBZ (template 5.3); `UGRD` −13.02 … 21.67 m/s and `VGRD` −15.56 … 17.75 m/s, simple
  packing (template 5.0, 2.4 MB each); `GUST` 0.03 … 32.53 m/s; `MASSDEN` (discipline 0, category 20,
  number 0, kg m⁻³) up to 9.09 × 10⁻⁸ kg m⁻³ = 90.9 µg m⁻³.
- The whole `wrfsfcf01.grib2` is 153,296,898 bytes.
