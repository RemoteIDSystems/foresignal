# Fixtures: `goes-glm` (prompt 048)

| File | What it is |
|---|---|
| `OR_GLM-L2-LCFA_G19_s20262852157200_e20262852157400_c20262852157421.nc` | An **active** 20-second granule (the largest of 2026-10-12 21Z, 432,989 bytes): 450 flashes, 420 with `flash_quality_flag` 0 and 30 with 3; `product_time` 845,114,240 s (2026-10-12T21:57:20Z); first-event offsets −0.964 … 19.413 s; `flash_energy` 1.285 × 10⁻¹⁵ … 5.249 × 10⁻¹² J. No quality-0 flash falls in the default Texas box. DBSCAN (8 km, 3 flashes) over the full field of view finds 37 clusters. |
| `OR_GLM-L2-LCFA_G19_s20262850359400_e20262850400000_c20262850400017.nc` | A **quiet** granule (the smallest of 2026-10-12 03Z, 255,210 bytes): 145 flashes, 134 with flag 0; one quality-0 flash in the default Texas box; 9 full-field clusters. |

SHA-256: active `ddb93be99abd7098b00a39310e58aa157c810c6156350cb998ff02474ffc679a`, quiet
`d32e383256d946c7e41effc2b4ffb305706d6708044c8968d8ae40ba7e054e9e`.

## Capture procedure

- **Date:** 2026-10-13 00:38 UTC, anonymous HTTPS against the public bucket (no credentials, no SDK).
- **Commands:** list a few hours of 2026-10-12 (day of year 285), pick the largest and the smallest
  object, download them:

  ```sh
  B=https://noaa-goes19.s3.amazonaws.com
  for h in 00 03 06 12 18 21; do
    curl -s "$B/?list-type=2&prefix=GLM-L2-LCFA/2026/285/$h/" \
      | grep -o "<Key>[^<]*</Key>.\{0,400\}<Size>[0-9]*</Size>" \
      | sed -E 's/<Key>([^<]*)<\/Key>.*<Size>([0-9]*)<\/Size>/\2 \1/' | sort -n | sed -n '1p;$p'
  done
  curl -sO "$B/GLM-L2-LCFA/2026/285/21/OR_GLM-L2-LCFA_G19_s20262852157200_e20262852157400_c20262852157421.nc"
  curl -sO "$B/GLM-L2-LCFA/2026/285/03/OR_GLM-L2-LCFA_G19_s20262850359400_e20262850400000_c20262850400017.nc"
  ```

- **Observed:** granules of that day ranged from 255,210 to 432,989 bytes; each hour lists 180 keys.
  The variables were read with NetCDF-Java 5.11.0 in enhanced mode: `flash_lat`, `flash_lon` (float,
  degrees), `flash_time_offset_of_first_event` (float after scale, `units` "seconds since
  2026-10-12 21:57:20.000", the same instant as `product_time`), `flash_energy` (float, J),
  `flash_quality_flag` (uint, flag values 0, 1, 3, 5), `product_time` (double, "seconds since
  2000-01-01 12:00:00").
