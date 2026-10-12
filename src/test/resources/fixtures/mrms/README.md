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

## Refreshing

`scripts/capture-raster-fixtures.sh` fetches the newest file per product; update the table and the
tests' expected counts afterwards.
