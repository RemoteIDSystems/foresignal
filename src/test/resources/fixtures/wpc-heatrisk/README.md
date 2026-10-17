# Fixtures: `wpc-heatrisk` (checkpoint 124; adapter from prompt 106)

Captured 2026-10-17 with anonymous HTTPS (User-Agent `Foresignal-build`) from the WPC HeatRisk site.

| File | Source | Bytes | SHA-256 |
|---|---|---|---|
| `data.html` | `https://www.wpc.ncep.noaa.gov/heatrisk/data.html` | 11,011 | `4626dcbc3c3b26cfe01d271af8e2f9aaabe330ce2177c177a2b3a432559fd3d7` |
| `HeatRisk_1_Mercator.tif` | `https://www.wpc.ncep.noaa.gov/heatrisk/data/HeatRisk_1_Mercator.tif` (`Last-Modified` Sat, 17 Oct 2026 10:49:12 GMT) | 131,713 | `cd82ef76119ef15db5880133fbef5244af163812a5bc4a1f82b1507383763bdb` |

Days 2–7 are not captured (day 7 was 135,065 bytes); the contract test lets them answer 404.

## What they hold (read with the production `HeatRiskAdapter.links` and `GeoTiffValues`, `HeatRiskAdapterContractTest`)

- The page links `data/HeatRisk_1_Mercator.tif` … `data/HeatRisk_7_Mercator.tif`.
- The GeoTIFF: 2866 × 1672, 8-bit palette, LZW, EPSG:3857, pixel 2539.703 m, origin
  (−14,326,021.825, 6,722,143.668); GDAL nodata **5** (not 53, as the 2026-10-15 note in the adapter
  says). Envelope about 128.69° W–63.31° W, 21.70° N–51.57° N.
- Palette indices: 0 → 1,819,633, 1 → 227,771, 2 → 6,567, 3 → 798, 5 (nodata) → 2,737,183 pixels;
  no 4. The adapter stores 0–3 as categories and 5 as 255. Independently confirmed with Pillow 10.2.0.
