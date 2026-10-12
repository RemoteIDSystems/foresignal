# 045-CR-Raster-Tiles-And-Styles

| | |
|---|---|
| Prompt | 045-Raster-Tiles-And-Styles.md |
| Status | Complete |
| Implementation commit | d73c4bc19c68f38794852afb4b6baaf590d3e833 |
| Date | 2026-10-12 |

## Summary

Foresignal now serves every raster product itself: WebMercator PNG tiles, single images and animation
loops, rendered on the fly from the stored native-grid frames through cached index maps. It also has a
versioned style registry with JSON and PNG legends, one cache rule for every raster route, a bounded fair
render queue, newest-frame warming, and a client-direct external catalog (GIBS, nowCOAST). All eight tasks
are done. The eight 045 operations are `implemented` and off `pending-operations.txt`.

**1. Grids** (`ai.foresignal.raster`). `GridDef` is a sealed interface with `lonLatToIndex` (NaN outside),
`indexToLonLat`, `bounds()`, `column()` (wrapping) and `nearestCell()`, built with `GridDef.of(GridDefinition)`.
Cell centers sit on integer indices, rows north to south, as in the `.fsg` payload.
- `LatLonGrid`: `lo1/la1/dx/dy`, or the definition's box (042's edge convention); longitude normalized once;
  a 360° grid wraps.
- `GeosGrid`: GOES-R PUG / CGMS normalized geostationary; `lon0`, `h`, `a`, `b` (GRS80 default), `sweep`
  x or y, scan angles `x0/dx/y0/dy`. Off-disc points are NaN (visibility from the ellipsoid normal).
- `LccGrid`: spherical Lambert conformal conic (Snyder §15), every parameter from the definition;
  `la1/lo1` is the south-west cell (GRIB2 scanned northwards, as HRRR is).
- `AffineGrid`: GDAL geotransform `gt0..gt5` in EPSG:4326 or EPSG:3857.
- No GeoTools, no proj4j. The math was checked numerically before writing the tests: the PUG worked
  example (x = −0.024052, y = 0.095340 ↔ 33.846162° N, 84.690932° W) reproduces to 1e-6 in both
  directions, and the HRRR corners come out at (−122.7195, 21.1381), (−72.2897, 21.1405),
  (−134.0955, 47.8386), (−60.9172, 47.8422).

**2. Renderer** (`ai.foresignal.raster.render`).
- `WebMercator` tile math; 256 px tiles, `@2x` 512 px for the same extent.
- `IndexMapCache` (Caffeine, byte-weighed, `index-cache-mb`, registered as `index`): tile key
  `(gridId, z, x, y, scale, bilinear)` and an image key; `IndexMap` holds the nearest cell per pixel, or
  four cells and weights for `resample: bilinear` products.
- `TileRenderer`: band-0 codes → palette index (code table for u8/u16/i16, value ramp for f32) → PNG-8
  through `ImageIO` with an `IndexColorModel` (the JDK writes the alpha as `tRNS`). Bilinear sampling
  blends physical values of non-special neighbors. rgba8 passthrough writes 32-bit PNG (Deviation 4).
- `TileCache` (`tile-cache-mb`, registered as `tile`): key `(productId, frameKey, style@v, z/x/y@scale)`;
  an empty render is the shared `EMPTY` marker.
- Zoom outside `[minZoom, nativeMaxZoom]`, or x/y outside the zoom, is `400 TILE_OUT_OF_RANGE`.
- `TransparentTile`: one shared 256 px transparent PNG with the fixed ETag `"fs-transparent-1"`;
  `?empty=204` opts into 204.
- `TileRenderBenchmarkTest` logs timings over an MRMS-sized 7000 × 3500 frame (asserts nothing on speed).

**3. Styles and legends.**
- `StyleRegistry` loads `src/main/resources/raster/styles/{name}@{version}.json`, refuses bad files at
  startup, and compiles each style per product (`CompiledStyle`: ≤ 256 colors, entry 0 transparent; u8
  256-entry table, u16/i16 65,536, f32 per pixel). A product's special codes (`codes` named
  `noCoverage`/`noEcho`/`noData`, and `nodata`) take the style's special colors.
- Shipped: `nws-reflectivity@1` (discrete 5 dBZ steps; MRMS 0/1 transparent, code 166 = 50 dBZ),
  `ir-gray@1`, `ir-enhanced@1`, `precip-rate@1`, `mesh@1`, `temperature@1` (cmocean thermal),
  `wind-speed@1` (viridis), `smoke@1` (cmocean matter), `rgb@1` (rgba8 passthrough). The ramp colors were
  sampled from the published tables (matplotlib `_cm_listed.py`, cmocean `rgb/*.txt`, fetched 2026-10-12).
- Only registered names are accepted (`400 UNKNOWN_STYLE`); a bare name resolves to the current version
  and is not content-addressed. `ProductUrls` and the catalog's `render` block now always emit `name@v`
  (through `StyleRegistry.shipped()`), so TileJSON, frames, `/loop` and SSE payloads carry it.
- `GET /v1/styles` (cursor page), `GET /v1/styles/{id}`.
- `LegendService`: JSON (`Legend`) with stops, ticks, categories, specials, meanings, and unit conversion
  (`UnitConversion`: mm/h↔in/h, K→°C/°F, m/s↔mph, mm↔in); PNG with Java2D in the logical `SansSerif`
  font, horizontal (width × 48) or vertical (width × 256), dark or light.

**4. Endpoints** (`api/RasterTilesController`, `api/StylesController`, all `read`).
- `getProductTile`, `getProductTileJson` (TileJSON 3.0.0, concrete content-addressed template, bounds,
  native zooms, center, `tileSize: 256`, `foresignal:tiles2x`, frame key, style, run, expiry,
  attribution), `getProductImage` (≤ 2048 px a side, EPSG:3857/4326, `Foresignal-Image-Bounds`),
  `getProductLoop` (bbox or place; ≤ 24 frames; width ≤ 1024; EPSG:3857; one run per forecast product),
  `listStyles`, `getStyle`, `getProductLegend`, `getProductLegendPng`.
- An ISO-extended time in a path answers 301 to its time key; a stale `sha8` is `404 UNKNOWN_FRAME` with a
  real `Link` (042's `currentKeyLink`).
- A `place` loop needs a token (401 otherwise, as 036's place filters do) and is always `private`;
  an unknown place is `404 UNKNOWN_PLACE`.

**5. Cache policy.** `RasterCachePolicy` is the one helper every raster controller uses: content-addressed
(`~sha8`, a `style=name@v` in the URL, a pinned run, a complete frame) → `{public|private},
max-age=31536000, immutable` and a strong ETag; anything else → `max-age=min(cadence/2, 60s)`, ETag and
`Foresignal-Frame-Time`. ETags are computed from the resolved frame, style and region, so
`WebRequest.checkNotModified` runs before rendering. `public` only when `ReadCaller.publicCache()` holds and
the source is not private. `?onExpired=transparent` serves the transparent tile with
`Foresignal-Frame-Expired: true` instead of 410.

**6. Render queue and warming.**
- `RenderQueue`: platform threads (`render-pool`), priority warm > interactive > prefetch
  (`Foresignal-Prefetch: 1` / `?prefetch=1`), round-robin per client key (token, else IP), single-flight
  per cache key, 1.5 s bounded wait → `503 RENDER_BUSY` with `Retry-After: 1`; an abandoned, unstarted
  render is dropped. `/v1/system` `rasters.renderQueue` (depth, active, poolSize) and
  `renderBusyLastHour` are now live.
- `TileWarmer`: on `RasterFrameCommittedEvent` for a complete frame, queues (without waiting) the tiles
  over 036's `PlacesAoiService` envelope (which already falls back to `weather.defaultAoi` with its health
  warning) from the min zoom to `maxZoomForAnimation`, at 1× and 2×, at most 512 per frame.
- `RasterRenderService` drops a product's tiles and compiled styles on `ProductCatalogChangedEvent`.
- No client request causes an upstream fetch; `NoClientTriggeredFetchIT` now covers the 045 paths.

**7. External catalog.** `src/main/resources/raster/external-catalog.json` (Deviation 2) seeds 14
`kind=external` products of the DERIVED source `external-catalogue` (`ExternalCatalogSource`, no fetch
implementation, no HTTP client). Each product's catalog document carries its `ExternalCatalogueEntry` in
`external`. Tile, image, loop, TileJSON and legend routes answer `400 VALIDATION_FAILED` ("external
product: fetch externalTemplate directly").
- GIBS (`epsg3857/best`), re-checked in today's GetCapabilities: GeoColor Level7, Band13 Level6, Band2
  Level7, Air_Mass Level6, Dust Level7, FireTemp Level7, VIIRS NOAA-20 true color Level9 (`.jpeg`),
  IMERG Level6, Black Marble Level8.
- nowCOAST: radar (`weather_radar:conus_base_reflectivity_mosaic`), lightning density
  (`lightning_detection:ldn_lightning_strike_density`), SST
  (`sea_surface_temperature:global_sea_surface_temperature`), storm surge
  (`tropical_storm_surge:inundation`), tropical cone
  (`tropical_cyclones:tropical_cyclone_cone_of_uncertainty_forecast`), as WMS GetMap templates (Deviation 3).
- Both services answered `Access-Control-Allow-Origin: *` today.

**8. Spec, Postman, docs.**
- OpenAPI: the eight operations `implemented`; problem-code registry gains `TILE_OUT_OF_RANGE` (400),
  `UNKNOWN_STYLE` (400), `RENDER_BUSY` (503); the loop's 401 description mentions places; changelog
  entry. The schemas (`TileJson`, `ProductLoop`, `Style`, `Legend`, `ExternalCatalogueEntry`) already
  existed and were reconciled with the code.
- `ErrorCodes` gains the three codes. `AccessPolicy` needed no change (042's GET rules cover the routes);
  `RasterAccessPolicyTest` asserts it.
- Postman regenerated (unchanged: the generator emits no status).
- `docs/Product-Charter.md` non-goal and FR-55, `docs/Executive-Summary.md`, `docs/Source-Catalog.md`,
  `docs/Architecture.md` (§1 rule, §3 package lines, new §28), `THIRD_PARTY_NOTICES.md` (viridis CC0,
  cmocean MIT).

## Files created / modified

Production, created:
- `raster/`: `GridDef`, `LatLonGrid`, `GeosGrid`, `LccGrid`, `AffineGrid`, `ExternalCatalog`,
  `ExternalCatalogSource`.
- `raster/render/`: `package-info`, `WebMercator`, `IndexMap`, `IndexMapCache`, `StyleDefinition`,
  `CompiledStyle`, `StyleRegistry`, `ImageEncoding`, `TileRenderer`, `TransparentTile`, `TileCache`,
  `RenderQueue`, `RasterCachePolicy`, `RasterRenderService`, `UnitConversion`, `LegendService`,
  `TileWarmer`, `LoopService`.
- `api/`: `RasterTilesController`, `StylesController`.
- Resources: `raster/styles/*.json` (9), `raster/external-catalog.json`.

Production, modified:
- `source/spi/ProductSpec`: `resample` component (Deviation 1).
- `raster/`: `ProductUrls` (versioned styles, explicit-style and concrete image overloads),
  `ProductCatalogService` (`external` member, versioned `render`, public `requireAvailable`, no null
  cadence), `ProductRegistry` (resample check), `ProductSpecJson`, `RasterPipeline` (public `bounds`),
  `RasterSystemBlock` (queue gauges), `package-info`.
- `api/error/ErrorCodes`.

Docs and spec: `docs/OpenAPI.json`, `docs/Architecture.md`, `docs/Product-Charter.md`,
`docs/Executive-Summary.md`, `docs/Source-Catalog.md`, `THIRD_PARTY_NOTICES.md`,
`src/test/resources/openapi/pending-operations.txt` (8 lines removed).

Tests: listed below. Modified: `SyntheticRasterAdapter` (styles `nws-reflectivity@1` / `wind-speed@1`,
Deviation 6), `ProductsApiIT`, `RasterPipelineIT`, `RasterFrameRecordsTest` (style names; catalog filters
by source), `NoClientTriggeredFetchIT` (045 paths).

## Deviations from the prompt

1. **`resample` is a new `ProductSpec` component** (default `nearest`, validated by the registry, stored in
   `raster_product.spec`). The prompt says "products whose spec sets `resample: bilinear`" but 042's spec
   had no such field.
2. **US English names.** The Java classes are `ExternalCatalog`, `ExternalCatalogSource` and
   `ExternalCatalogTest` (not `…Catalogue…`), and the seed file is `raster/external-catalog.json`. The
   source id `external-catalogue` and the spec schema `ExternalCatalogueEntry` keep the prompt's and the
   spec's spelling, because they cross the wire.
3. **nowCOAST entries are WMS GetMap templates**, not GWC WMTS. Today's GWC WMTS GetCapabilities publishes
   only `bluetopo:*` layers, none of the weather layers. Each template uses MapLibre's `{bbox-epsg-3857}`
   token at 256 px; `tilingScheme` says so. The storm-surge bounds are a US Gulf/Atlantic box, because
   the layer's advertised extent follows the current storm.
4. **rgba8 passthrough (`rgb@1`) writes 32-bit PNG.** A frame's own colors do not fit a 256-entry palette.
5. **The transparent tile is immutable only on a content-addressed URL.** On an alias URL emptiness is not
   permanent, so it gets the short max-age, still with the fixed ETag.
6. **The synthetic test products use registered styles** (`nws-reflectivity@1`, `wind-speed@1`) instead of
   042's unregistered `test-reflectivity@1` / `test-wind@1`, since unregistered styles are now refused.
   The obs product's scale and offset already match MRMS.
7. **Architecture section number.** §27 is already "Intelligence layer", so raster products are §28.
8. **Line numbers.** The Charter non-goal was at line 65 (not 58) and the Executive Summary row at line 59
   (not 46); the text named by the prompt was changed wherever it was.
9. **Unit choices.** `units=imperial` gives mph for m/s (kt is not selectable with `metric|imperial`);
   metric K is shown as °C, as the prompt's "K→°C/°F" implies.
10. **Snapping.** `/image` and `/loop` boxes are snapped outward to 0.01°, and latitudes are clamped to
    WebMercator's range for EPSG:3857. The prompt does not fix the grid.
11. **An absent `style` parameter is not content-addressed**, even when the product's default style is
    versioned: the rule is about the URL.
12. **The 108 extensions on these operations** (`tileSize`, `tms`, `encoding`, `format=webp`) are left to
    108. Only `format=webp` is actively refused (400).

## Issues found / open questions

**Needs the owner:**

1. **The GOES-R PUG URL in the prompt no longer serves the PDF.** `https://www.goes-r.gov/users/docs/PUG-L1b-vol3.pdf`
   redirects to a NESDIS landing page.
   - The formulas were taken from the NOAA STAR "GOES Imager Projection" page, and checked against the
     PUG worked example.
   - The code cites PUG Vol. 3 §5.1.2.8 and CGMS 03 §4.4.3.2. **Neither section number was verified
     against the documents.** **Owner:** confirm them, or supply the current PUG link.
2. **CORS for the new headers.** `Foresignal-Image-Bounds`, `Foresignal-Frame-Expired`,
   `Foresignal-Frame-Time`, `Retry-After`, `Link` and `ETag` are not in `CorsFilter.EXPOSED_HEADERS`, and
   `Foresignal-Prefetch` is not an allowed request header (Weather-Imagery-Design §6.7). The prompt does
   not ask for it. **Owner:** add it now, or with the tile keys in 050?
3. **Fonts in the container.** The PNG legend uses Java2D text. A slim image without fontconfig may fail
   to draw text. This was not verified (no tests ran). **Owner:** check at 057 or 143.
4. **Product styles are not checked against the registry at startup.** A product that declares an
   unregistered style answers `UNKNOWN_STYLE` on its default URLs. **Owner:** make `ProductRegistry`
   refuse such products?

**Other findings:**
- Not verified at run time (tests first run at 057): the JDK PNG writer's `tRNS` for palette images, the
  Spring capture of `{y}.png` with `@2x`, and the render timings.
- The US English guard named in `~/Documents/GitLab/CLAUDE.md` covers the MeshDTN repositories, not this
  one. A grep of the 045 diff for common British spellings found none; pre-existing ones elsewhere were
  left alone.
- No instructions directed at an agent appeared in any data, file or tool output. That includes the GIBS
  and nowCOAST capabilities, GEV `weather.js`, and the colormap files.

## Tests written (not run)

The ITs are `@WebIntegrationTest @Import(RasterTestSources.class)` and extend the new
`testsupport/raster/RasterApiSupport`, so they share 042's one cached context. The unit tests need no
context.

| Test | What it covers |
|---|---|
| `raster/GridDefTest` | latlon box and round trip, global 0..360 wrap at the antimeridian, GOES-East PUG worked example both ways, geos round trip and disc limit (both sweeps), HRRR corners and round trip, affine 4326/3857, parameter refusals |
| `raster/render/TileRendererTest` | golden synthetic frame compared on decoded pixels, `@2x` 512 px, PLTE + `tRNS` + indexed IHDR, empty renders, the shared transparent tile and fixed ETag, `TILE_OUT_OF_RANGE`, bilinear versus nearest, JPEG |
| `raster/render/StyleRegistryTest` | the nine shipped styles compile for u8/u16/f32 within 256 colors; MRMS codes 0/1 transparent, 166 = 50 dBZ; versions and bare names; unknown and malformed names; invalid definitions; categorical and clamped continuous |
| `raster/render/LegendServiceTest` | JSON shape validated as `Legend`; imperial and metric conversion; continuous ticks; PNG sizes and themes |
| `raster/render/RasterCachePolicyTest` | immutable/short × public/private matrix, `min(cadence/2, 60s)`, frame-time header, strong ETags, 304 before render with the renderer mock never called |
| `raster/render/RenderQueueTest` | priority order, per-client fairness, single-flight, 1.5 s wait → `RENDER_BUSY` + `Retry-After: 1` and the abandoned render dropped, failures |
| `raster/render/TileRenderBenchmarkTest` | logs per-tile timings only |
| `raster/ProductTilesIT` | committed frame renders with correct pixels and immutable headers; short-lived alias, time-key and unversioned URLs; `@2x`; transparent tile, 304 and 204; every problem code, 301, `onExpired`; external products refused and listed with `external`; private caching with `openReads` off; styles and legends over HTTP; `/v1/system` gauges |
| `raster/TileJsonIT` | concrete content-addressed template, zooms, bounds, center, `@2x`, short versus immutable document, forecast run pinning, errors; the template serves a tile |
| `raster/ImageEndpointIT` | `Foresignal-Image-Bounds`, sizes and pixels in 4326, snapping and mercator aspect, the 2048 cap, JPEG, validation, 301 |
| `raster/LoopServiceIT` | snapped box and corners, newest frames oldest first, URLs serving the loop size, `bytes` once rendered, seamless past/future with one run, places (401, private, `UNKNOWN_PLACE`), validation |
| `raster/WarmingIT` | a complete frame warms AOI tiles at z ≤ 7 at 1× and 2×, none outside the AOI or above the zoom, no fetch; partial frames warm nothing |
| `raster/ExternalCatalogTest` | 14 entries with required members, GIBS levels = `maxNativeZoom`, registry rules pass under the DERIVED source, no fetch path or HTTP client field |
| `security/RasterAccessPolicyTest` | every 045 route is READ and not always-authenticated; writes are not READ |
| `collect/NoClientTriggeredFetchIT` (paths added) | SRC-6 over the 045 paths |

## Compile result

- `./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`.
- `./gradlew lintOpenApi` → valid, 71 warnings (unchanged).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman` → 829 requests, covers all operations
  (collection unchanged).
- No new dependencies.

## Notes for the next prompt

1. **046 (decoders):**
   - grids must carry the 045 parameters (latlon `lo1/la1/dx/dy`; geos `lon0/h/a/b/sweep/x0/dx/y0/dy`
     in radians; lcc `lat1/lat2/lov/radius/la1/lo1/dx/dy` with the south-west first point and rows
     flipped north to south; affine `crs/gt0..gt5`);
   - set `resample(bilinear)` on continuous model fields;
   - name registered, versioned styles.
2. **049 (SatDump):**
   - rgba8 composites use `rgb@1`;
   - dynamic products trigger `ProductCatalogChangedEvent`, which already drops their cached tiles.
3. **050 (tile keys):** client keys for fairness are `token:<id>`, else `ip:<addr>`
   (`RasterTilesController.clientKey`); add `tilekey:<id>` there. CORS headers: Issue 2.
4. **051 (timeline):** reuse `LoopService`'s run selection; never mix runs.
5. **055 (static maps):** reuse `RasterRenderService.image` and `LegendService`.
6. **108:** `tileSize=512`, `tms`, `encoding` and `webp` on these routes are still to do.
7. **057 checkpoint:**
   - run the 045 tests. `RenderQueueTest.boundedWait` takes about 1.5 s by design;
   - every frame commit in the raster context now queues warming renders asynchronously;
   - the external catalog adds 14 products and the `external-catalogue` source (disabled, DERIVED) to
     every context.
8. **Owner decisions pending:** Issues 1–4 above, plus those carried from 012–044.
