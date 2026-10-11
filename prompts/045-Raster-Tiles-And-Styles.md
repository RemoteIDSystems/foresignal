# 045 — Raster Tiles And Styles

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/045-CR-Raster-Tiles-And-Styles.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–134).
- **Previous Completion Report:** read `completion-reports/044-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Weather-Imagery-Design.md` §0.1 (rendering, immutable caching, render queue, charter amendment), §2.3 (MRMS u8 codes, quantisation), §3 (reprojection, PNG rows), §5.1 (external catalogue entries), §6.1 (cache policy), §6.3 (tiles, images, loops, legends, render queue, warming), §6.10 (client recipes, for what clients need), §6.11 (problem codes), §11 (rejected items: no pre-rendered pyramids, no 204 default). Design prompt 031 = this prompt (Roadmap §5); its "037" tile keys are v1.2 **050**, its "038" timeline is **051**.
- GOES-R Product User Guide (L1b, Vol. 3), section "GOES-R ABI fixed grid" (scan-angle ↔ lat/lon navigation): https://www.goes-r.gov/users/docs/PUG-L1b-vol3.pdf; CGMS LRIT/HRIT Global Specification, normalized geostationary projection: https://www.cgms-info.org/ (find the exact section numbers when implementing and cite them in the code comments)
- TileJSON 3.0.0: https://github.com/mapbox/tilejson-spec/tree/master/3.0.0
- Completion Report 042 (FrameStore, FrameResolver, ProductUrls, RasterFrameCommittedEvent) and 016 (vector tile path)

## Objective

Serve every raster product to map clients as on-the-fly WebMercator PNG-8 tiles, single images and animation loops, rendered from the compressed native-grid frames through cached index maps. Add the versioned style registry with JSON and PNG legends, the content-addressed cache policy, a bounded fair render queue, AOI warming, and client-direct external catalogue entries. Amend the Charter and Architecture for "Foresignal hosts derived products, never proxies".

## Tasks

### 1. Grid definitions (`ai.foresignal.raster.GridDef`)

Sealed interface with `lonLatToIndex(lon, lat) → (double i, double j)` (NaN outside), `indexToLonLat`, `bounds()`, built from `raster_grid.definition`:
- `LatLonGrid`: regular lat/lon, `lo1/la1` of the first point, `dx/dy`, longitude wrap (0..360 or −180..180, normalise once).
- `GeosGrid`: GOES-R PUG / CGMS normalized geostationary: `lon0`, `h` (perspective point height above the ellipsoid), `a`, `b` (default GRS80 6378137 / 6356752.31414), `sweep` (`x` for GOES, `y` for Meteosat-style), scan-angle `x0, dx, y0, dy` (radians). Inverse per the PUG fixed-grid formulas; points beyond the visible disc → NaN.
- `LccGrid`: Lambert conformal conic (HRRR: `lat1=lat2=38.5`, `lov=−97.5`, sphere radius 6371229 m, `la1/lo1`, `dx=dy=3000 m`); take every parameter from the definition, never hard-code.
- `AffineGrid`: GeoTIFF-style geotransform in EPSG:4326 or EPSG:3857.
- No GeoTools (LGPL) and no proj4j.

### 2. TileRenderer and IndexMapCache (`ai.foresignal.raster.render`)

- WebMercatorQuad, 256 px tiles; `@2x` renders 512 px for the same extent.
- `IndexMapCache` (Caffeine, byte-weighed, `foresignal.raster.index-cache-mb`): key `(gridId, z, x, y, scale)` → `int[]` source cell index per output pixel (−1 outside). Pixel centres are inverse-projected from WebMercator, then `GridDef.lonLatToIndex`, nearest cell. Products whose spec sets `resample: bilinear` (continuous model fields) use a 4-index + weights variant.
- Render = gather raw codes through the index map → style palette index → PNG-8 with `IndexColorModel` and tRNS (JDK ImageIO; no gzip on PNG). Target 4–8 ms per tile; add a JMH-free microbenchmark test that logs timings only.
- Tile cache (Caffeine, `foresignal.raster.tile-cache-mb`), key `(productId, frameKey, style@v, z, x, y, scale)`.
- Above the product's native `maxZoom` (MRMS 8, GOES C13 7, GFS 4, HRRR 8): 400 `TILE_OUT_OF_RANGE` (clients overzoom). Below `minZoom` likewise.
- An empty or out-of-bounds tile returns **one shared transparent PNG** with one fixed ETag, cached immutable; `?empty=204` opts into 204.

### 3. Styles and legends

- `StyleRegistry`: JSON files `src/main/resources/raster/styles/{name}@{version}.json` with `{id, version, type: continuous|discrete|categorical, units, stops[{value, color, label}], categories[{code, color, label}], specials[{kind: noCoverage|noEcho|noData, color, label}], meaning[{min, max, text}]}`. Compile each to a ≤ 256-colour palette plus, per product dtype, a code→palette-index table (u8: 256 entries; u16/i16: 65,536 shorts; f32: computed per pixel from the value). Physical value = `code × scale + offset`.
- Ship: `nws-reflectivity@1` (dBZ; MRMS codes 0 = noCoverage transparent, 1 = noEcho transparent, 2..255 = (code−66)/2 dBZ), `ir-gray@1`, `ir-enhanced@1`, `precip-rate@1`, `mesh@1`, `temperature@1`, `wind-speed@1`, `smoke@1`, `rgb@1` (rgba8 passthrough). Use viridis/cmocean ramps where a scientific ramp fits and add their notices.
- Only registered names are accepted; unversioned names resolve to the current version for alias responses. Unknown → 400 `UNKNOWN_STYLE`. TileJSON, frames, `/loop` and SSE payloads always emit `name@v` (update 042's `ProductUrls`).
- `GET /v1/styles`, `GET /v1/styles/{id}` (read).
- `GET /v1/products/{id}/legend?style&units=metric|imperial` → JSON `{styleId, styleVersion, type, units, unitsLabel, stops[], ticks[], categories[], specials[], meaning[]}` (e.g. "≥ 50 dBZ: heavy rain, hail possible"). Unit conversion: mm/h↔in/h, K→°C/°F, m/s↔kt/mph, mm↔in.
- `GET /v1/products/{id}/legend.png?width&orientation=horizontal|vertical&theme=dark|light&units` rendered with Java2D (no external fonts; use the JDK logical `SansSerif`).

### 4. Endpoints (all `read`)

- `GET /v1/products/{id}/tiles/{frameKey}/{z}/{x}/{y}[@2x].png?style&run&empty&onExpired`
- `GET /v1/products/{id}/tilejson?frameKey&style`: TileJSON 3.0.0 with a **concrete content-addressed template** (aliases resolved at generation; the JSON itself short max-age), `bounds`, `minzoom`, `maxzoom` (native), `center`, `tileSize: 256`, `@2x` variant, attribution text.
- `GET /v1/products/{id}/image/{frameKey}.{png|jpg}?bbox&width&height&crs=EPSG:3857|EPSG:4326&style`: up to 2048 px a side; returns header `Foresignal-Image-Bounds: w,s,e,n`.
- `GET /v1/products/loop?products=a[,b]|past=..&future=..&bbox|place&radiusKm&frames≤24&from&to&width≤1024&crs=EPSG:3857&style`: the server snaps the bbox once and returns `{crs, bounds[w,s,e,n], coordinates[[tl],[tr],[br],[bl]], width, height, frames[{time, timeKey, frameKey, kind, run?, step?, url, bytes, expiresAt}], legendUrl, attribution[], serverTime}`. `place` resolves through 036 places (`UNKNOWN_PLACE` 404). Frame URLs point at `/image`. Never mixes forecast runs.

### 5. Cache policy (one rule for tiles, images, legends, fields and media)

- Content-addressed URL (frame token `{timeKey}~{sha8}`, versioned style, pinned `run` for forecasts, frame `complete`) → `{public|private}, max-age=31536000, immutable`, strong ETag.
- Anything else (alias, bare `timeKey`, unversioned style, partial frame) → `{public|private}, max-age=min(cadence/2, 60s)`, ETag, `WebRequest.checkNotModified` **before** rendering, and header `Foresignal-Frame-Time`.
- `public` only when `security.openReads=true` covers the path; otherwise `private`.
- Stale `sha8` → 404 `UNKNOWN_FRAME` with `Link` to the current key; expired → 410 `FRAME_EXPIRED`, or with `?onExpired=transparent` the transparent tile plus `Foresignal-Frame-Expired: true`.
- Implement as one `RasterCachePolicy` helper used by every raster controller.

### 6. RenderQueue and warming

- `RenderQueue` on a platform pool sized from the profile (`foresignal.raster.render-pool`). Priority: newest-frame warming first, then interactive, then prefetch (`Foresignal-Prefetch: 1` or `?prefetch=1`). Fair queueing round-robin per token id, else tile key (050), else client IP. Single-flight per tile cache key. Bounded wait **1.5 s**; then 503 `RENDER_BUSY` with `Retry-After: 1`. Gauges feed the `/v1/system` `rasters.renderQueue` and `renderBusyLastHour`.
- Warming: on `RasterFrameCommittedEvent` for a complete frame, render the tiles intersecting the places-AOI envelope (036 `PlacesAoiService`; `weather.defaultAoi` when there are no places) at z ≤ `maxZoomForAnimation` (radar 7, IR 6; from the spec) at 1× and 2× into the tile cache. If 036's AOI service is missing, add a minimal one (envelope of place buffers of max(radiusKm, 150 km), fallback Texas bbox with a health warning) and note it.
- **No client request ever causes an upstream fetch.**

### 7. External catalogue entries

- `raster_product` rows with `kind=external`, source `external-catalogue` (a DERIVED-mode source with no upstream calls), seeded from `src/main/resources/raster/external-catalogue.json`. Each entry: `externalTemplate`, `tilingScheme`, `tileSize`, `maxNativeZoom`, `bounds`, `timeDimension{format, period, typicalLatency, defaultLag}`, attribution (CORS `*` noted).
- GIBS WMTS `epsg3857/best` (verified 2026-10 in GetCapabilities): `GOES-East_ABI_GeoColor` (GoogleMapsCompatible_Level7, PT10M), `GOES-East_ABI_Band13_Clean_Infrared` (Level6, PT10M), `GOES-East_ABI_Band2_Red_Visible_1km`, `GOES-East_ABI_Air_Mass` (Level6), `GOES-East_ABI_Dust`, `GOES-East_ABI_FireTemp`, `VIIRS_NOAA20_CorrectedReflectance_TrueColor` (Level9, JPEG, P1D), `IMERG_Precipitation_Rate` (Level6, P1D), `VIIRS_Black_Marble` (Level8). Template `https://gibs.earthdata.nasa.gov/wmts/epsg3857/best/{layer}/default/{time}/GoogleMapsCompatible_Level{n}/{z}/{y}/{x}.{ext}`. Re-check matrix sets against GetCapabilities at implementation.
- nowCOAST GeoServer WMS/GWC WMTS: radar, lightning density, SST, surge, tropical (verify current layer names on `nowcoast.noaa.gov`). GEV `weather.js` is a reference for these entries only; nothing is proxied.
- Listed by `/v1/products?kind=external`; tile and image routes return 400 `VALIDATION_FAILED` ("external product: fetch externalTemplate directly").

### 8. Spec, Postman, docs

- `docs/OpenAPI.json`: operations `getProductTile` (`image/png`), `getProductTileJson`, `getProductImage` (`image/png`, `image/jpeg`), `getProductLoop`, `listStyles`, `getStyle`, `getProductLegend`, `getProductLegendPng`, each `"x-foresignal-scope": "read"`; schemas `TileJson`, `ProductLoop`, `Style`, `Legend`, `ExternalCatalogueEntry`; problem codes `TILE_OUT_OF_RANGE`, `UNKNOWN_STYLE` 400, `RENDER_BUSY` 503 (+ `Retry-After`), `UNKNOWN_PLACE` 404. Regenerate Postman with `python3 scripts/generate-postman.py docs/OpenAPI.json postman`.
- `AccessPolicy`: the routes above under READ (already covered by `GET /v1/products/**` and `/v1/styles/**`; add tests).
- `docs/Product-Charter.md` line 58 non-goal amended: "Foresignal hosts products derived from data it ingests; it never proxies basemaps or third-party tiles (GIBS and nowCOAST are external catalogue entries clients fetch directly)"; FR-55 rewritten for self-rendered radar, satellite and model products. `docs/Executive-Summary.md` line 46 to match. `docs/Architecture.md` §1 rule ("no client request ever causes an upstream fetch: tiles, samples, media, maps, SatDump polar plots") and a new §27 Raster products (grids, renderer, styles, cache policy, queue). `docs/Source-Catalog.md` line 48 replaced.
- `THIRD_PARTY_NOTICES.md`: viridis (CC0) and cmocean (MIT) ramps.

## Tests to write (do not run)

- `GridDefTest`: round trips per grid type; GOES-East known pixel ↔ lon/lat pairs from the PUG worked example; off-disc → NaN; HRRR corner points; antimeridian wrap for global latlon.
- `TileRendererTest`: golden PNG for a synthetic frame (byte-compare decoded pixels, not file bytes); `@2x` dimensions; transparent shared tile and its fixed ETag; `TILE_OUT_OF_RANGE`; tRNS present.
- `StyleRegistryTest`: compiles every shipped style; MRMS code mapping (0/1 transparent, code 166 = 50 dBZ); unknown and unversioned names.
- `LegendServiceTest`: JSON shape, imperial conversion, PNG dimensions per orientation.
- `RasterCachePolicyTest`: the full immutable/short matrix, `public` vs `private` by `openReads`, 304 before render (renderer mock never called).
- `RenderQueueTest`: priority order, per-client fairness, single-flight, 1.5 s timeout → `RENDER_BUSY` with `Retry-After`.
- `LoopServiceIT`, `TileJsonIT`, `ImageEndpointIT` (bounds header, 2048 cap), `ExternalCatalogueTest` (entries load; no HTTP client invoked), `WarmingIT` (frame commit renders AOI tiles only).

## Acceptance criteria

- A committed frame renders through tiles, `/image` and `/loop` with correct cache headers; content-addressed URLs are immutable and nothing else is.
- Under overload the queue sheds with `RENDER_BUSY` after at most 1.5 s.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 134, 137, 138, 139).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 139).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `045: Raster Tiles And Styles`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/045-CR-Raster-Tiles-And-Styles.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `045: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
