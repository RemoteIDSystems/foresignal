# Fixtures: `goes-east-abi` (prompt 046)

The band 13 file is 3.8 MB, over the 3 MB commit limit, so it is **not committed**. `./gradlew
downloadTestFixtures` fetches it into `build/test-fixtures/goes/` and checks its pin; the tests that
need it (`NetcdfDecoderTest`, `GoesIngestIT`) skip with a logged reason when it is absent.

| File | Key (`noaa-goes19`) | Bytes | SHA-256 |
|---|---|---|---|
| `OR_ABI-L2-CMIPC-M6C13_G19_s20262851201170_e20262851203554_c20262851204053.nc` | `ABI-L2-CMIPC/2026/285/12/…` | 3,807,021 | `bace11ee913a15467b047a76beb12d241589349431e74424a6fe6d09833cc556` |

Captured 2026-10-12 with `scripts/capture-raster-fixtures.sh` (anonymous HTTPS).

## What it holds (read with NetCDF-Java 5.11.0)

- NetCDF-4 (HDF5); global `platform_ID` G19, `scene_id` CONUS, `timeline_id` "ABI Mode 6",
  `time_coverage_start` 2026-10-12T12:01:17.0Z, `time_coverage_end` 2026-10-12T12:03:55.4Z.
- `CMI(y=1500, x=2500)`, short with `_Unsigned`, `scale_factor` 0.06145332, `add_offset` 89.62,
  `_FillValue` −1, units K, `grid_mapping` `goes_imager_projection`. Enhanced: 190.53 … 298.68 K,
  47,163 fill samples (NaN).
- `DQF(y, x)`: 0 for 3,702,837 samples, fill (255 after enhancement) for the same 47,163.
- `goes_imager_projection`: `perspective_point_height` 35786023, `semi_major_axis` 6378137,
  `semi_minor_axis` 6356752.31414, `longitude_of_projection_origin` −75.0, `sweep_angle_axis` x.
- `x` from −0.101332 to 0.038612 rad, `y` from 0.128212 down to 0.044268 rad, step 5.6 × 10⁻⁵ rad
  (rows already run north to south). Projected outline: about 150.23° W to 52.92° W, 14.56° N to
  56.32° N.
