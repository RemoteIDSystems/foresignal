# 108-CR-Map-Interop-And-Offline

| | |
|---|---|
| Prompt | 108-Map-Interop-And-Offline.md |
| Status | Complete (post-review fixes applied 2026-10-16) |
| Implementation commit | `4a47d91ea5a10563e55359122860a17231374440` |
| Post-review fix commit | `75726ad` (`108: fix post-review defects`) |
| Date | 2026-10-15 (post-review fixes 2026-10-16) |

## Summary

Opened the raster renderer to GIS, TAK, 3D and offline clients without changing the core gather. All 29
prompt-108 operations are implemented and moved out of `pending-operations.txt`. Compiles; tests written,
not run. The review of 2026-10-16 found defects in the first implementation; they are fixed and listed under
"Post-review fixes" below, and the statements in this report describe the code after those fixes.

- **Renderer (task 1):** `TileMatrixSet` (`WebMercatorQuad`, `WorldCrs84Quad` 2×1 at z0→180/2^z),
  `TileMatrixSets` registry, the `…/tiles/{frameKey}/{tms}/{z}/{x}/{y}.{format}` path with `tileSize=512`
  (native, distinct from `@2x`), `WebpSupport` (startup health check → 406 `FORMAT_UNAVAILABLE` +
  `/v1/system` `WEBP_UNAVAILABLE`; a missing native library — `UnsatisfiedLinkError`/`NoClassDefFoundError` —
  disables WebP instead of failing the context), lossless WebP for palette styles / q0.8 imagery, data tiles
  (`DataTileEncoder`, `custom`/`terrarium` with correct carry, no-data alpha 0, nearest only) with a
  `foresignal:dataTiles` TileJSON block, `.i16` (`I16FieldEncoder`) and tiled `png-uv` fields with both in the
  field manifest `encodings`, and `GifEncoder` behind `/v1/products/loop.gif`, whose frames render through the
  render queue at prefetch priority (`RENDER_BUSY` applies) and whose GIF is cached for content-addressed
  frame sets (`RasterRenderService.loopGif`).
- **WMTS (task 2):** `WmtsController` — capabilities (ETag, `private, max-age=60`, `Vary: Authorization`), KVP
  and RESTful tiles. One layer per product with a `<Style>` per style, both tile matrix sets with per-layer
  `TileMatrixSetLimits` (native zoom range and the rows/columns of the product's bounds), `Time` (200 newest
  frame times; forecast products add `RefTime`); `current` → the `latest` alias (short cache) and a concrete
  time 301-redirects to the content-addressed tile. KVP `TILEROW`/`TILECOL` and the RESTful path map to the
  same y/x. Key-prefixed URLs through `PublicUrls`.
- **OGC API – Tiles and EDR (task 3):** `OgcController` under `/v1/ogc` — landing, conformance, api,
  collections (products as coverage collections), tileMatrixSets, tilesets and the map tile path (OGC
  row/col order, 301 to the content-addressed tile). EDR `position` returns CoverageJSON matching `/v1/sample`
  (`values: [null]` when there is no value); `area` returns the native-grid subset of the polygon's bounding
  box (`EdrArea`: frame read once, flat row-major values, `null` no-data mask; a `Grid` domain for regular
  lat/lon grids, a `MultiPoint` domain of cell centers for projected grids), capped at 100,000 native cells →
  413 `EDR_TOO_LARGE`; `instances` lists forecast runs.
- **Exports (task 4):** `ai.foresignal.raster.export` on the `raster_export_job` table (async worker pool,
  48 h under `${FORESIGNAL_DATA_DIR}/exports/`). Jobs are handed to the pool after their row commits; a
  scheduled sweep enforces expiry by `expires_at` and fails stuck or restart-orphaned jobs. `MbtilesWriter`
  (sqlite-jdbc, schema 1.3, TMS rows), `ai.foresignal.export.pmtiles.PmtilesWriter` (in-house v3: 127-byte
  header, Hilbert ids, clustered data, varint gzip root/leaf directories), `CogWriter` (tiled 512, deflate,
  factor-2 overviews until a level is below 512 px, IFDs and every out-of-line tag value before the tile data,
  TIFF inline/offset rule, GeoKeyDirectory with its header, `GDAL_NODATA`, `Copyright`, `GDAL_METADATA`). MBTiles
  and PMTiles accept only `WebMercatorQuad` (400 otherwise); a COG is EPSG:3857 for `WebMercatorQuad` (the
  default) and EPSG:4326 for `WorldCRS84Quad`. Every file embeds the product's attribution and license
  (`ExportAttribution`), with a "changes made" note for CC BY and ECMWF products. Caps: 50,000 tiles, 2 GB per
  file.
- **Style fragments / TAK (task 5):** `StyleFragmentController` — `{version:8, sources, layers}` with
  concrete content-addressed URLs, `foresignal-{product}` ids and `metadata.legends`; `mobac.xml` with
  `{$z}/{$x}/{$y}` on the `latest` alias and the caller's key prefix.
- **Place pack (task 6):** `POST /v1/places/{id}/pack` → an async ZIP (`manifest.json` with SHA-256 per
  file, attribution text and per-product structured attribution/license, place weather, forecast series,
  alerts GeoJSON, legends, `loop.gif`, one PMTiles per product), capped at 50 MB (413 `PACK_TOO_LARGE`). An
  empty or absent product list means the default set; the caller's excluded sources travel in the job spec
  (never shown in the job view) and their products and series are left out; the same input from the same
  token within 10 minutes returns the existing job (`input_hash`).
- **Image links (task 7):** `notifications.imageLinks` (default false; https `publicBaseUrl` required else
  400 `IMAGE_LINKS_REQUIRE_HTTPS`). `AlertMapService` mints one read-only feed key per media path
  (`/v1/media/{id}`, 7 d) and returns `{base}/v1/k/{key}/media/{id}`; Slack/Mattermost/Teams/Google
  Chat/Gotify/Web Push/webhook carry the image; webhook already carried the URL.
- **Spec/docs (task 8):** 29 operations marked `implemented`, `pending-operations.txt` −29 lines, 108 added
  to the Problem schema's extended-by and its five new error codes; Frontend/Operator/Architecture docs and
  THIRD_PARTY_NOTICES updated. `scripts/generate-postman.py` now has BODIES for `createProductExport` and
  `createPlacePack` (the first implementation had none, so Postman sent `{}`); the Postman collection itself
  is regenerated by the coordinator, not in this pass.

## Files created / modified

**Created (main, 4a47d91):** `raster/render/{TileMatrixSet,TileMatrixSets,WebMercatorQuad,WorldCrs84Quad,
DataTileEncoder,GifEncoder,WebpSupport}`, `raster/model/I16FieldEncoder`, `raster/export/{ExportJobEntity,
ExportJobRepository,ExportPaths,ExportJobService,RasterExportService,MbtilesWriter,CogWriter,PlacePackService}`,
`export/pmtiles/PmtilesWriter`, `api/{WmtsController,OgcController,ExportsController,StyleFragmentController,
PlacePackController}`.

**Modified (main, 4a47d91):** `raster/render/{RasterRenderService,IndexMapCache,ImageEncoding,TileRenderer}`,
`raster/model/FieldService`, `api/{RasterTilesController,ModelFieldsController}`, `api/error/ErrorCodes`,
`security/AccessPolicy`, `access/TileKeyPolicy`, `settings/NotificationSettings`,
`notify/{NotificationMessage,NotificationSettingsService,AlertMapService}`,
`notify/push/{PushMessage,PushContentFactory,PushPayloads}`, `notify/types/{Slack,Mattermost,Teams,GoogleChat,
Gotify}Channel`, `build.gradle.kts`, `gradle/libs.versions.toml`.

**Docs/spec:** `docs/OpenAPI.json`, `docs/Architecture.md`, `docs/Frontend-Guide.md`, `docs/Operator-Guide.md`,
`THIRD_PARTY_NOTICES.md`, `src/test/resources/openapi/pending-operations.txt` (−29 lines).

**Post-review (2026-10-16):** created `raster/sample/EdrArea`, `raster/export/ExportAttribution`; modified
`api/{WmtsController,OgcController,RasterTilesController,ExportsController (raster parse only),
PlacePackController}`, `raster/render/{RasterRenderService,WebpSupport,DataTileEncoder}`,
`raster/export/{CogWriter,RasterExportService,ExportJobService,ExportJobEntity,ExportJobRepository,
PlacePackService}`, `docs/OpenAPI.json`, `docs/Operator-Guide.md`, `scripts/generate-postman.py`.

## Deviations from the prompt

1. **WMTS layers are one per product with a `<Style>` per style**, not literally one layer per product ×
   style. The identifier stays the product id so the RESTful `{layer}/{style}/…` path and QGIS/Cesium work
   unchanged; the design rule (all styles addressable, both tile matrix sets linked) is kept.
2. **webp-imageio 0.10.0** is used; the design cites 0.11.0, which is not published on Maven Central
   (verified 2026-10-15). Recorded in `libs.versions.toml`.
3. **Data tiles are WebMercatorQuad at 256 px** and use `nearest` only; `terrarium` is offered only for
   integer products. The TileJSON block is emitted under `foresignal:dataTiles` on the existing `getProductTileJson`
   document (there is no separate data-tile TileJSON operation in the spec).
4. **Field `.i16` maps onto the signed range** `[−32767, 32767]` with per-component `scale`/`offset` in the
   manifest (`physical = code·scale + offset`), so no separate unsigned semantics are needed.
5. **The COG writes float32** (nodata filled in), not the native quantized dtype; the optional GDAL
   `gdalinfo` validation path is not wired (no GDAL build-arg here). The CRS is chosen by the request's `tms`
   (the spec has no separate CRS field): `WebMercatorQuad` → EPSG:3857, `WorldCRS84Quad` → EPSG:4326.
6. **The place pack includes an empty `alerts.geojson`.** Extracting each alert's geometry from the
   `recordSnapshot` payload is left to the checkpoint; the manifest, weather, series, legends, loop and
   PMTiles are complete.
7. **Image links: the always-https media path uses a single media-path feed key**, not a separately revoked
   key row tied to media-expiry events (revocation relies on the key's own 7-day expiry). Matrix still
   uploads via its media endpoint in the existing channel; the explicit `m.image` upload path was not added.
8. **No new HTTP media-type constants were needed** in `CorsFilter`: `Link`/`Location`/`ETag` are already
   exposed, so no CORS change.
9. **`exports/` is created by `ExportPaths`**, not `RasterPaths` (which owns `rasters/`); it is included in
   the raster disk quota conceptually but the quota block was not extended.
10. **EDR `/area` subsets the polygon's bounding box**, not the polygon itself; cells outside the polygon but
    inside its box are included. Projected grids answer a CoverageJSON `MultiPoint` domain because their
    native lattice is not axis-aligned in CRS84.
11. **The place-pack reuse key includes the token** as well as the request and the caller's excluded
    sources, so a pack is reused only for the same caller (the job view shows its creator).
12. **The export sweep is a plain `@Scheduled` job** (15 min, `foresignal.raster.export.sweep-interval`),
    like the other periodic sweeps (`PushSubscriptionService`, `RollupJob`); it has no `ApplicationReadyEvent`
    phase, so nothing is registered in `StartupOrder`.

## Issues found / open questions

- **`PmtilesWriter.metadataJson` escapes only `\`, `"` and `\n`** and stringifies non-scalar values; another
  control character (tab, CR) in a metadata string makes the JSON invalid. The writer belongs to prompt 111's
  package and was not edited here; the raster side now passes only control-character-free strings and
  serializes its `json` value with the JSON mapper. The writer should serialize with the JSON mapper.
- **Integration tests are not written** for the context-heavy operations (`OgcApiTilesIT`, `EdrIT`,
  `MbtilesExportIT`, `PlacePackIT`, `ImageLinksIT`, `StyleFragmentTest`, `MobacXmlTest`, a full
  `WmtsCapabilitiesTest` against the schema subset). Unit tests cover the fixed logic (below).
- **The COG/TIFF and PMTiles writers are in-house and unproven** against external readers; golden-file
  round trips (GDAL, `pmtiles verify`) should be added at a checkpoint.
- **The `raster_export_job` table (now with `input_hash`) arrives via Hibernate `ddl-auto=update`**; 144
  (Flyway) must capture it.
- **`getProductFieldTile` samples the stored wind grid**, so it is a `png-uv` tile of the field rather than a
  palette tile; verify a client reads the global min/max from the manifest.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `raster/render/WorldCrs84QuadTest` | z0 2×1, bounds split at 0°, tile range, pixel locator |
| `raster/render/Tile512Test` | a native 512 tile maps to the same cells as four 256 child tiles |
| `raster/render/DataTileEncoderTest` | custom/signed byte layout, no-data alpha, terrarium round trip and carry, metadata |
| `raster/render/GifEncoderTest` | GIF89a, frame count, size, delay |
| `raster/render/LoopGifRenderTest` | loop frames queued at prefetch priority; GIF cached only for content-addressed sets |
| `raster/render/WebpHealthCheckTest` | availability mirrors the writer; `LinkageError` and runtime failures disable WebP |
| `raster/model/I16FieldEncoderTest` | per-component round trip within one step; non-finite → no-data |
| `raster/sample/EdrAreaTest` | native cells, flat values, null mask, native-cell cap, antimeridian |
| `export/pmtiles/PmtilesWriterTest` | 127-byte header, clustered/gzip/none/PNG flags, varint root ids |
| `raster/export/CogWriterTest` | IFD-before-data, inline/offset rule, GeoKey header, EPSG:3857, copyright, overview levels |
| `raster/export/MbtilesWriterTest` | metadata and TMS-row tiles |
| `raster/export/ExportJobServiceTest` | submit after commit, expiry sweep incl. unfinished jobs, stuck/orphaned jobs, pack reuse, hidden exclusions |
| `raster/export/RasterExportRulesTest` | tms per format, COG CRS, attribution/license embedding, pack product visibility and spec |
| `api/WmtsControllerTest` | KVP and RESTful row/col agree, private capabilities, TileMatrixSetLimits |
| `api/OgcControllerTest` | `POINT`/`POLYGON` EDR coordinate parsing; point with no value is `[null]` |

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL` (implementation commit and post-review pass).
`docs/OpenAPI.json` parses. New dependencies (implementation commit): `webp-imageio` 0.10.0 and `sqlite-jdbc`
3.53.4.0. The first report's claim that Postman was regenerated was inaccurate: the implementation commit did
not touch `postman/`, and `createProductExport` and `createPlacePack` had no BODIES entry. The post-review fixes
add the BODIES entries to `scripts/generate-postman.py`; the coordinator regenerates the collection.

## Notes for the next prompt

- **111** reuses `ai.foresignal.export.pmtiles.PmtilesWriter` and the `raster_export_job` framework; the
  in-house archive writers should be dry-run against 111's vector packs, and `PmtilesWriter` should serialize
  its metadata with the JSON mapper (see Issues).
- **112** adds Features collections under `/v1/ogc` — keep `OgcController`'s landing/collection code
  extensible (it already returns a `Map`).
- **144 (Flyway):** capture `raster_export_job` including `input_hash`.
- Consider an `ImageLinksIT` with the Matrix upload path and a media-expiry revocation hook.

## Post-review fixes (2026-10-16)

| # | Severity | Finding | Fix |
|---|---|---|---|
| 1 | HIGH | WMTS KVP GetTile passed `TILEROW, TILECOL` into a handler declared `(col, row)`, swapping x and y | Handler takes `(row, col)`; the RESTful caller passes row then column; `WmtsControllerTest` |
| 2 | HIGH | EDR `/area` resampled a fixed 512-step lattice (almost every box 413), nested `[[v]]` values, one sample lookup per cell | `EdrArea`: native cells in the box from one frame read, cap on native cells, flat values, `null` mask; `EdrAreaTest` |
| 3 | HIGH | Raster export `sweep()` had no caller; jobs without `finishedAt` never expired | `@Scheduled` sweep by `expires_at` (and old `finished_at`), fails jobs orphaned by a restart or stuck > 6 h |
| 4 | MEDIUM | Jobs were submitted to the pool inside the transaction | Submitted after commit (`afterCommit`) |
| 5 | MEDIUM | `WebpSupport` caught only `RuntimeException` | Also catches `LinkageError`; probe constructor for tests |
| 6 | MEDIUM | Empty product list never fell back to the defaults | Stored only when non-empty; empty/absent → defaults |
| 7 | MEDIUM | `loop.gif` drew with `drawDirect`, bypassing the queue and cache | `RasterRenderService.loopGif`: queued frames at prefetch, GIF cached for content-addressed sets; the pack uses it too |
| 8 | MEDIUM | Place packs ignored the caller's visibility | Excluded sources carried in the job spec (hidden from the view); products and series filtered |
| 9 | MEDIUM | MBTiles/PMTiles accepted `WorldCRS84Quad` but wrote WebMercator XYZ | 400 for non-WebMercatorQuad in the controller and the service |
| 10 | MEDIUM | `GDAL_NODATA` always written as an offset, even ≤ 4 bytes; GeoKeyDirectory lacked its header | Generic TIFF inline/offset rule; GeoKeyDirectory `{1,1,0,n}` header |
| 11 | LOW | COG overview stop; Terrarium carry; WMTS limits; public capabilities; EDR point `[]` | Overviews until < 512 px; carry via 24-bit steps; `TileMatrixSetLimits`; `private` + `Vary`; `[null]`. PMTiles escaping is in 111's `PmtilesWriter` — reported above, not edited |
| — | Undisclosed | COG always EPSG:4326 though the report said 3857 when requested | 3857 for `WebMercatorQuad`, 4326 for `WorldCRS84Quad` |
| — | Undisclosed | No license in any export; attribution only as text in MBTiles/PMTiles | `ExportAttribution` in MBTiles/PMTiles metadata, COG `Copyright`/`GDAL_METADATA`, pack manifest |
| — | Undisclosed | Place pack 10-minute cache by input hash missing | `input_hash` column and reuse within 10 minutes |
| — | Report | Postman BODIES for `createProductExport`/`createPlacePack` missing | Added to `scripts/generate-postman.py` |

Implementation commit: `4a47d91ea5a10563e55359122860a17231374440`
