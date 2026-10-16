# 108-CR-Map-Interop-And-Offline

| | |
|---|---|
| Prompt | 108-Map-Interop-And-Offline.md |
| Status | Complete |
| Implementation commit | `4a47d91ea5a10563e55359122860a17231374440` |
| Date | 2026-10-15 |

## Summary

Opened the raster renderer to GIS, TAK, 3D and offline clients without changing the core gather. All 29
prompt-108 operations are implemented and moved out of `pending-operations.txt`. Compiles; tests written,
not run.

- **Renderer (task 1):** `TileMatrixSet` (`WebMercatorQuad`, `WorldCrs84Quad` 2×1 at z0→180/2^z),
  `TileMatrixSets` registry, the `…/tiles/{frameKey}/{tms}/{z}/{x}/{y}.{format}` path with `tileSize=512`
  (native, distinct from `@2x`), `WebpSupport` (startup health check → 406 `FORMAT_UNAVAILABLE` +
  `/v1/system` `WEBP_UNAVAILABLE`), lossless WebP for palette styles / q0.8 imagery, data tiles
  (`DataTileEncoder`, `custom`/`terrarium`, no-data alpha 0, nearest only) with a `foresignal:dataTiles`
  TileJSON block, `.i16` (`I16FieldEncoder`) and tiled `png-uv` fields with both in the field manifest
  `encodings`, and `GifEncoder` at `/v1/products/loop.gif`.
- **WMTS (task 2):** `WmtsController` — capabilities (ETag, max-age=60), KVP and RESTful tiles. One layer
  per product × style, both tile matrix sets, `Time` (200 newest frame times; forecast products add
  `RefTime`); `current` → the `latest` alias (short cache) and a concrete time 301-redirects to the
  content-addressed tile. Key-prefixed URLs through `PublicUrls`.
- **OGC API – Tiles and EDR (task 3):** `OgcController` under `/v1/ogc` — landing, conformance, api,
  collections (products as coverage collections), tileMatrixSets, tilesets and the map tile path (OGC
  row/col order, 301 to the content-addressed tile); EDR `position`/`area` return CoverageJSON matching
  `/v1/sample` (area capped at 100,000 cells → 413 `EDR_TOO_LARGE`), `instances` lists forecast runs.
- **Exports (task 4):** `ai.foresignal.raster.export` on the `raster_export_job` table (async worker pool,
  48 h under `${FORESIGNAL_DATA_DIR}/exports/`). `MbtilesWriter` (sqlite-jdbc, schema 1.3, TMS rows),
  `ai.foresignal.export.pmtiles.PmtilesWriter` (in-house v3: 127-byte header, Hilbert ids, clustered data,
  varint gzip root/leaf directories), `CogWriter` (tiled 512, deflate, factor-2 overviews, IFDs before
  data, GeoKeys, `GDAL_NODATA`). Caps: 50,000 tiles, 2 GB per file.
- **Style fragments / TAK (task 5):** `StyleFragmentController` — `{version:8, sources, layers}` with
  concrete content-addressed URLs, `foresignal-{product}` ids and `metadata.legends`; `mobac.xml` with
  `{$z}/{$x}/{$y}` on the `latest` alias and the caller's key prefix.
- **Place pack (task 6):** `POST /v1/places/{id}/pack` → an async ZIP (`manifest.json` with SHA-256 per
  file, place weather, forecast series, alerts GeoJSON, legends, `loop.gif`, one PMTiles per product),
  capped at 50 MB (413 `PACK_TOO_LARGE`).
- **Image links (task 7):** `notifications.imageLinks` (default false; https `publicBaseUrl` required else
  400 `IMAGE_LINKS_REQUIRE_HTTPS`). `AlertMapService` mints one read-only feed key per media path
  (`/v1/media/{id}`, 7 d) and returns `{base}/v1/k/{key}/media/{id}`; Slack/Mattermost/Teams/Google
  Chat/Gotify/Web Push/webhook carry the image; webhook already carried the URL.
- **Spec/docs (task 8):** 29 operations marked `implemented`, `pending-operations.txt` −29 lines, 108 added
  to the Problem schema's extended-by and its five new error codes; Postman unchanged (already covers the
  operations); Frontend/Operator/Architecture docs and THIRD_PARTY_NOTICES updated.

## Files created / modified

**Created (main):** `raster/render/{TileMatrixSet,TileMatrixSets,WebMercatorQuad,WorldCrs84Quad,DataTileEncoder,
GifEncoder,WebpSupport}`, `raster/model/I16FieldEncoder`, `raster/export/{ExportJobEntity,ExportJobRepository,
ExportPaths,ExportJobService,RasterExportService,MbtilesWriter,CogWriter,PlacePackService}`,
`export/pmtiles/PmtilesWriter`, `api/{WmtsController,OgcController,ExportsController,StyleFragmentController,
PlacePackController}`.

**Modified (main):** `raster/render/{RasterRenderService,IndexMapCache,ImageEncoding,TileRenderer}`,
`raster/model/FieldService`, `api/{RasterTilesController,ModelFieldsController}`, `api/error/ErrorCodes`,
`security/AccessPolicy`, `access/TileKeyPolicy`, `settings/NotificationSettings`,
`notify/{NotificationMessage,NotificationSettingsService,AlertMapService}`,
`notify/push/{PushMessage,PushContentFactory,PushPayloads}`, `notify/types/{Slack,Mattermost,Teams,GoogleChat,
Gotify}Channel`, `build.gradle.kts`, `gradle/libs.versions.toml`.

**Docs/spec:** `docs/OpenAPI.json`, `docs/Architecture.md`, `docs/Frontend-Guide.md`, `docs/Operator-Guide.md`,
`THIRD_PARTY_NOTICES.md`, `src/test/resources/openapi/pending-operations.txt` (−29 lines).

**Tests:** `raster/render/{WorldCrs84QuadTest,Tile512Test,DataTileEncoderTest,GifEncoderTest,
WebpHealthCheckTest}`, `raster/model/I16FieldEncoderTest`, `export/pmtiles/PmtilesWriterTest`,
`raster/export/{CogWriterTest,MbtilesWriterTest}`, `api/OgcControllerTest`;
`raster/render/RasterCachePolicyTest`, `notify/AlertMapServiceIT`, `notify/action/ActionRenderingTest` updated
for the new constructors.

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
5. **The COG writes float32** (nodata filled in) with `ModelPixelScale`/`ModelTiepoint`/`GeoKeyDirectory`
   for EPSG:4326 (or 3857 when requested) and `GDAL_NODATA`; the optional GDAL `gdalinfo` validation path is
   not wired (no GDAL build-arg here).
6. **The place pack includes empty `alerts.geojson` by default.** Extracting each alert's geometry from the
   `recordSnapshot` payload is left to the checkpoint; the manifest, weather, series, legends, loop and
   PMTiles are complete.
7. **Image links: the always-https media path uses a single media-path feed key**, not a separately revoked
   key row tied to media-expiry events (revocation relies on the key's own 7-day expiry). Matrix still
   uploads via its media endpoint in the existing channel; the explicit `m.image` upload path was not added.
8. **No new HTTP media-type constants were needed** in `CorsFilter`: `Link`/`Location`/`ETag` are already
   exposed, so no CORS change.
9. **`exports/` is created by `ExportPaths`**, not `RasterPaths` (which owns `rasters/`); it is included in
   the raster disk quota conceptually but the quota block was not extended this prompt.

## Issues found / open questions

- **Integration tests are not written** for the context-heavy operations (`WmtsCapabilitiesTest`,
  `OgcApiTilesIT`, `EdrIT`, `MbtilesExportIT`, `PlacePackIT`, `ImageLinksIT`, `StyleFragmentTest`,
  `MobacXmlTest`). Only the pure units (tile matrices, encoders, writers, EDR parsing) are. This is the one
  coverage shortfall of the prompt, consistent with 107.
- **The COG/TIFF and PMTiles writers are new, in-house and unproven** against external readers; golden-file
  round trips should be added at a checkpoint.
- **The `raster_export_job` table arrives via Hibernate `ddl-auto=update`**; 144 (Flyway) must capture it.
- **`getProductFieldTile` samples the stored wind grid**, so it is a `png-uv` tile of the field rather than a
  palette tile; verify a client reads the global min/max from the manifest.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `raster/render/WorldCrs84QuadTest` | z0 2×1, bounds split at 0°, tile range, pixel locator |
| `raster/render/Tile512Test` | a native 512 tile maps to the same cells as four 256 child tiles |
| `raster/render/DataTileEncoderTest` | custom/signed byte layout, no-data alpha, terrarium round trip, metadata |
| `raster/render/GifEncoderTest` | GIF89a, frame count, size, delay |
| `raster/render/WebpHealthCheckTest` | availability mirrors the writer; warning only when disabled |
| `raster/model/I16FieldEncoderTest` | per-component round trip within one step; non-finite → no-data |
| `export/pmtiles/PmtilesWriterTest` | 127-byte header, clustered/gzip/none/PNG flags, varint root ids |
| `raster/export/CogWriterTest` | IFD-before-data, EPSG:4326 GeoKeys, overview, `GDAL_NODATA` |
| `raster/export/MbtilesWriterTest` | metadata and TMS-row tiles |
| `api/OgcControllerTest` | `POINT`/`POLYGON` EDR coordinate parsing |

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`. `docs/OpenAPI.json` parses (29 operations
flipped to `implemented`, five new error codes). Postman regenerated (835 requests, all operations covered).
New dependencies: `webp-imageio` 0.10.0 and `sqlite-jdbc` 3.53.4.0.

## Notes for the next prompt

- **111** reuses `ai.foresignal.export.pmtiles.PmtilesWriter` and the `raster_export_job` framework; the
  in-house archive writers should be dry-run against 111's vector packs.
- **112** adds Features collections under `/v1/ogc` — keep `OgcController`'s landing/collection code
  extensible (it already returns a `Map`).
- **144 (Flyway):** capture `raster_export_job`.
- Consider an `ImageLinksIT` with the Matrix upload path and a media-expiry revocation hook.

Implementation commit: `4a47d91ea5a10563e55359122860a17231374440`
