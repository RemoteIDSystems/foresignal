# Fixtures: `mrms` (prompt 046)

Real files from the NOAA MRMS bucket on AWS (`noaa-mrms-pds`, anonymous access), captured
2026-10-12 with `scripts/capture-raster-fixtures.sh`. They are committed as published (gzip-compressed
GRIB2), because a GRIB2 field cannot be cropped without re-encoding it. Each is under 3 MB.

| File | Key | Bytes | SHA-256 |
|---|---|---|---|
| `MRMS_MergedReflectivityQCComposite_00.50_20261012-120037.grib2.gz` | `CONUS/MergedReflectivityQCComposite_00.50/20261012/…` | 1,752,446 | `b1352fff4eb6575fce73f1cfe3bfd38ce9815ae6d28482e197c489012e0b9ad1` |
| `MRMS_PrecipRate_00.00_20261012-120000.grib2.gz` | `CONUS/PrecipRate_00.00/20261012/…` | 874,286 | `0d090b340337b2b87235f5fc022a002c5944bfa4702a57e3da493ffb0050ff58` |
| `MRMS_PrecipFlag_00.00_20261012-120000.grib2.gz` | `CONUS/PrecipFlag_00.00/20261012/…` | 204,018 | `ab1c6c995714cd0816357246b7bf079c8beb9f9325298f1aa79b6e7f603d88c0` |
| `MRMS_MESH_Max_60min_00.50_20261012-120000.grib2.gz` | `CONUS/MESH_Max_60min_00.50/20261012/…` | 38,716 | `6cd87e607a4824d910c58b5b03effbdaf6a8a8d87e6c065b1f38996354e04164` |
| `HAWAII_MRMS_MESH_Max_60min_00.50_20261012-000000.grib2.gz` | `HAWAII/MESH_Max_60min_00.50/20261012/MRMS_MESH_Max_60min_00.50_20261012-000000.grib2.gz` (renamed) | 16,443 | `3c046a56cad85477b3b91b07139493d9f5f50145f6ab2238f65fece7e939afa2` |

The reflectivity file is a quiet day: maximum 60.5 dBZ, and 2,032,230 of its 24,500,000 cells (8.3 %)
carry a value; the rest are no coverage or no echo. The Hawaii MESH file is the no-coverage-heavy sample: 41 % of its cells are no coverage.

## Sentinels found (decoded with NetCDF-Java 5.11.0)

All five fields: discipline 209 (MRMS local), grid template 3.0, 0.01° (Hawaii 0.005°), scanning
mode 0, data template 5.41. CONUS grid 7000 × 3500, first point 54.995° N, 230.005° E (basic angle
1, subdivisions 1,000,000).

| Field | Parameter (cat/num), level | No coverage | No echo / zero | Values |
|---|---|---|---|---|
| Reflectivity | 10/0, type 102 at 500 m | **−999** (8,174,561 cells) | **−99** (14,293,209) | −28.5 … 60.5 dBZ in 0.5 steps |
| PrecipRate | 6/1, type 102 at 0 m | **−3** (8,335,467) | 0 (14,919,738) | 0 … 125 mm/h |
| PrecipFlag | 6/0, type 102 at 0 m | **−3** (8,335,467) | 0 (14,771,674) | flag codes up to 91 |
| MESH 60 min | 3/30, type 102 at 500 m | **−3** (8,174,561) | **−1** (16,323,860) | 0 … 16.5 mm |
| MESH (Hawaii) | 3/30, type 102 at 500 m | −3 (2,365,802) | −1 (3,354,198) | none |

The design's −999 / −99 / −3 sentinels hold. MESH uses −1 (not 0) for "no hail", which
`MrmsCodec` maps to code 1 (0 mm).

## Second wave (prompt 053)

Captured 2026-10-13 04:35 UTC (newest key of each prefix, listed anonymously; `curl` of
`https://noaa-mrms-pds.s3.amazonaws.com/<key>`, the same objects `aws s3 cp --no-sign-request` fetches).

| File | Key prefix | Bytes | SHA-256 |
|---|---|---|---|
| `MRMS_RotationTrack60min_00.50_20261013-043200.grib2.gz` | `CONUS/RotationTrack60min_00.50/20261013/` | 291,825 | `3fc6c6c5ff7280c51078c471a162569fec38e217e0af3483f2b3b0ef019167e2` |
| `MRMS_RadarOnly_QPE_01H_00.00_20261013-043200.grib2.gz` | `CONUS/RadarOnly_QPE_01H_00.00/20261013/` | 688,155 | `ac59cea2a533d03a6aac4f015a140674590faab83d9ca3bfb113507b28842cc5` |
| `MRMS_FLASH_QPE_FFG01H_00.00_20261013-043000.grib2.gz` | `CONUS/FLASH_QPE_FFG01H_00.00/20261013/` | 147,619 | `2dc9d01807dbd22dfaff182565a55952f56b5f220864533538789b1fbf5dd098` |
| `MRMS_MultiSensor_QPE_24H_Pass2_00.00_20261013-030000.grib2.gz` (**not committed**: 4.7 MB, over the 2 MB limit; `./gradlew downloadTestFixtures` puts it in `build/test-fixtures/mrms/`) | `CONUS/MultiSensor_QPE_24H_Pass2_00.00/20261013/` | 4,725,486 | `349bb80cd2e485ff9a8771f3462987398c8c05e82a149ad0f898cdbe4c5ccebb` |

Decoded with NetCDF-Java (all template 5.41):

| Field | Parameter (cat/num), level | Grid | No coverage | Zero | Values |
|---|---|---|---|---|---|
| RotationTrack60min | 3/3, type 102 at 500 m | **14,000 × 7,000 at 0.005°**, first point 54.9975° N, −129.9975° | none (the MRMS table gives 0 for missing and no coverage) | 97,784,554 | 3 … 49 (units 0.001 s⁻¹, whole numbers) |
| RadarOnly_QPE_01H | 6/2, type 102 at 0 m | 7000 × 3500 at 0.01° | **−3** (8,257,230) | 15,190,723 | 0.1 … 39.2 mm |
| MultiSensor_QPE_24H_Pass2 | 6/41, type 102 at 0 m | 7000 × 3500 at 0.01° | **−3** (1,512,918) | 14,866,789 | 0.1 … 385.5 mm |
| FLASH_QPE_FFG01H | 12/26, type 1 (surface) | 7000 × 3500 at 0.01°, first longitude **230.004996°** (encoded) | **−999** (15,738,788) | 8,542,783 | 5 … 90 in steps of 5 (percent) |

Reduced 2 × 2 by maximum, the rotation grid equals the reflectivity grid exactly; its codes are 24,408,713 ×
code 1 (0 s⁻¹), 91,287 with a value, none no coverage, maximum code 491 (0.049 s⁻¹).

## Refreshing

`scripts/capture-raster-fixtures.sh` fetches the newest file per product; update the table and the
tests' expected counts afterwards.
