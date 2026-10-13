# Fixtures: `goes-fdc` (prompt 048)

| File | What it is |
|---|---|
| `OR_ABI-L2-FDCC-M6_G19_s20262852156170_e20262852158544_c20262852159041.nc` | One GOES-19 ABI L2 Fire/Hot Spot Characterization CONUS file, scan 2026-10-12 21:56:17Z, 524,009 bytes. |

SHA-256 `2952d3afc167dda9a80c489228d775a3e514f4c24be95c29ed8bea17bb443c0c`.

## What it holds (NetCDF-Java 5.11.0, enhanced)

- `Mask(y=1500, x=2500)` short, `_FillValue` −99; `Power` (MW), `Temp` (K), `Area` (**m²**), `DQF`, all
  on `goes_imager_projection` (lon0 −75°, sweep x) with `x`/`y` scan angles.
- 25 detection pixels (mask 10 × 2, 12 × 1, 30 × 19, 32 × 3); 15 (1), 34 (2) and 35 (2) exist
  but are not detections. FRP 4.59 … 271.03 MW where present (the four cloud-contaminated pixels have
  none).
- **Two detections fall inside the default Texas AOI** `[-106.65, 25.84, -93.51, 36.50]`:
  `g19:608:594` (−103.8093, 34.3155; FRP 159.8 MW, 454.8 K, 214,625 m²) and `g19:927:692`
  (−94.7773, 31.6045; FRP 33.9 MW).
- Clusters over all 25 (1-pixel dilation, 8-connected): 22, three of them with two pixels
  (`1149:603`+`1149:604`, `1027:725`+`1027:726`, `882:1335`+`885:1338`).

## Capture procedure

- **Date:** 2026-10-13 00:38 UTC, anonymous HTTPS:

  ```sh
  B=https://noaa-goes19.s3.amazonaws.com
  curl -s "$B/?list-type=2&prefix=ABI-L2-FDCC/2026/285/21/"   # 12 keys, 515,679-524,009 bytes
  curl -sO "$B/ABI-L2-FDCC/2026/285/21/OR_ABI-L2-FDCC-M6_G19_s20262852156170_e20262852158544_c20262852159041.nc"
  ```

- The adapter tests build further scans (fewer pixels, later times) in code from the parsed detections,
  so no second file is needed.
