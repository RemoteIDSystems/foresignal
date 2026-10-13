# 054-CR-SatDump-Polar-Overlays

| | |
|---|---|
| Prompt | 054-SatDump-Polar-Overlays.md |
| Status | Complete |
| Implementation commit | 1dc529628791d894f603708790c27657f739724d |
| Date | 2026-10-13 |

## Summary

Decoded polar passes now become georeferenced map overlays with real footprints. A pass without a
usable GeoTIFF gets an approximate swath instead. Remote stations can also push media and pass bundles.

Foresignal only **reads** the EPSG:4326/3857 GeoTIFFs that SatDump writes. Nothing reimplements
SatDump's projection or raytracer, and no SatDump code was copied: the GPL source was read for format
facts only. `ingestSourceMedia` and `ingestSourceRasters` are `implemented` and off
`pending-operations.txt`.

**1. Producer version.** No owner answer to Weather-Imagery-Design §1.5 Q1 is recorded (049 Issue 1
is still open). So the design's default is documented as the one producer path, best-effort: the
**SatDump 1.2.2** composite `project` block. It needs `"auto": true` (Deviation 1):

```json
"project": {"config": {"type": "equirec", "auto": true}, "img_format": ".tif", "draw_map_overlay": false}
```

Format facts, from the 1.2.2 tag of the local checkout:
- 1.2.2 writes `rgb_{composite}_projected.tif` (`image_processor.cpp`);
- the tags are one tie point at (0,0), the pixel scale `(scale_x, −scalar_y)`, `GTModelType` 2 and
  `GeographicType` 4326; its WebMercator output writes `ProjectedCSType` 3857 (`geotiff_write.cpp`);
- strips are uncompressed, 8- or 16-bit, with 1, 3 or 4 channels and unassociated alpha (`tiffio.cpp`).

The fixture is a synthetic GeoTIFF with identical tags, documented as such:
- `fixtures/satdump/1.2.2/`: a Texas pass, plus a Bering Sea pass whose GeoTIFF runs past 180°;
- `generate-geotiff.py` writes both (standard library only);
- the README section marks them `synthetic: true` and gives the recapture steps.

**2. GeoTIFF reader** (`ai.foresignal.raster.geotiff`), stateless like `raster.decode`.
- **`TiffDirectory`:** parses the classic TIFF first IFD from the header bytes, with bounds-checked
  offsets and counts. Magic 43 (BigTIFF) is refused.
- **`GeoKeyParser`:** reads tags 33550, 33922, 34264 (axis-aligned only; a rotation or shear is
  `ROTATED`) and 34735.
  - EPSG:3857 comes from `ProjectedCSType` 3857; EPSG:4326 from a geographic model with `GeographicType` 4326.
  - Anything else gives "GeoTIFF CRS EPSG:{n} not supported (build with WITH_GDAL=true)"; a user-defined CRS reports 32767.
  - PixelIsPoint shifts the corner transform by half a pixel, as GDAL does.
- **`GeoTiffReader`:** checks in this order, each before any decoding:
  1. BigTIFF;
  2. `ImageGuard.MAX_PIXELS` / `MAX_SIDE` from the header dimensions;
  3. the layout: 8- or 16-bit unsigned, 1–4 samples;
  4. the CRS.

  Only then does the JDK `javax.imageio` TIFF plugin decode the pixels, into packed `0xRRGGBBAA`.
  16-bit samples are scaled to 8 bits. RGB without alpha is transparent where every band is zero, or
  equals `GDAL_NODATA` when that tag is set.
- **Output:** an RGBA `affine` frame through `ctx.rasters()` (`.fsg` rgba8, deflate).
  - The dynamic product is `satdump-{station}-{composite}`: the file stem without `_projected`, kebab-case, at most 64 characters.
  - Its spec: `temporal.type=pass` (cadence 12 h, maxGap 30 d), `coverage: swath`, `rgb@1`, ladder 30 d, nativeMaxZoom 8.
  - Each pass's frame sits at the product timestamp, so re-processing a pass replaces it with a new `sha8`.

**3. Footprint and pass geometry.**
- **`FootprintBuilder`:** traces each row's leftmost and rightmost valid pixel, left edge down and right
  edge up, in pixel corners with unwrapped longitudes. Rows without data split the outline into parts.
  The result goes through `TopologyPreservingSimplifier` (0.02°), `GeometryFixer`, and
  `Antimeridian.split` at ±180°.
  - A global 4326 image whose swath wraps at its edges uses the complement of the widest transparent gap.
- **Pass geometry** follows the order footprint → swath → station point (`PassBook.geometry`); a swath
  never replaces a footprint.
  - It is stored as WKT in `PassState` and emitted as the record geometry with `geometrySource`.
  - `products[]` gain `productId`, `frameKey` and `tilesTemplate`, the content-addressed `tiles` of the frame record. An overlay joins the PNG composite entry of the same stem.
  - One new pass version is written per processing.
- **`polarGlob`:** a new param, default `**/*.tif`; `**/` also matches no directory, and case is
  ignored (`PolarGlob`, checked in `checkParams`). It applies only inside ready pass folders, through
  `InboxAccess`, which uses NOFOLLOW and a realpath check per file. A symlinked GeoTIFF is never listed.
- **Provenance:** GeoTIFF copies go to `rasters/satdump/{station}/{passDir}/`, as in 049. The new
  `SatDumpProvenanceRetentionStep` (hourly retention run, order 805) deletes pass directories older than
  30 days. This closes 049 Deviation 14 and Issue 6.
- **`AffineGrid`** now looks a 4326 point up one turn east or west, so a grid that runs past 180° renders.
- **`FrameAgeHealthRule`** skips `pass` products: they have frames only when a satellite passes.

**4. Approximate swath (Orekit).**
- **Dependency:** `org.orekit:orekit:13.1.9`, Apache-2.0. It is the latest 13.1.x on Maven Central
  (`maven-metadata.xml`, 2026-10-13) and brings Hipparchus 4.0.3.
- **Data:** `src/main/resources/orekit-data/` holds `tai-utc.dat` plus `finals2000A.all` trimmed to
  2024-01-01 onward. Both come from orekit-data commit `2422a7d0`.
  - They load through a `ClasspathCrawler` in a private `LazyLoadedDataContext` (`ai.foresignal.orbit.OrekitData`, a lazy holder).
  - `DataContext.getDefault()` is never used.
- **`SwathEstimator`:**
  - **Input:** the TLE from `product.cbor` (else the pass's), propagated with SGP4 in TEME and transformed to ITRF over the WGS84 ellipsoid.
  - **Window:** the first to last `projection_cfg.timestamps`, at 10-s steps, capped at 30 min. The fallbacks are AOS–LOS, then the dataset timestamp ± 5 min.
  - **Width:** half the swath on each side by great-circle offset. It is `corr_swath`, else MSU-MR 2,800 km, AVHRR 2,900 km or MHS 2,200 km.
  - **Outline:** the union of one quad per step, each split at ±180° on its own; a quad straddling a pole is dropped.
  - It is used only when no overlay succeeded, and never makes tiles.
- **Notices:** `THIRD_PARTY_NOTICES.md` has Orekit and Hipparchus (from the jar's NOTICE), an "Orekit
  data" section with SHA-256 values, and optional GDAL.

**5. Remote push ingest** (`RemoteUploadController`, `RemoteBundles`, `RemoteMediaRegistry`).
- **Routing:** literal `media` and `rasters` segments win over `/v1/ingest/{sourceId}/{format}`. Both
  names were already reserved in `IngestFormatRegistry`; the listing now names them as the push endpoints.
- **Auth:** `INGEST_KEY_OR_ADMIN` from the existing `/v1/ingest/**` rule. Device and signature-only
  callers are refused.
- **Sources:** only those whose descriptor sets `acceptsRemoteMedia`, a new `SourceDescriptor` field
  that the `satdump-*` family sets. Any other source is 404 `NOT_FOUND`, never `FORMAT_UNKNOWN`.
- **Budget:** each push costs one unit of the source's budget (`BudgetGovernor`; 429 `RATE_LIMITED`
  with `Retry-After`). The inbox loop does not charge again for uploads.
- **`/rasters`:**
  - checks before anything is written:
    - `meta {passFolder, files[], product?}` with safe single segments (no separator, no `..`, no leading dot or control characters, at most 128 characters);
    - every listed file has a part, and every part is listed;
    - only json, cbor, png, jpg and tif;
    - `dataset.json` is required;
    - each file at most 25 MB and the bundle at most 200 MB (413 `MEDIA_TOO_LARGE`);
    - images pass the ImageGuard header check; a BigTIFF is left to the pipeline and GDAL.
  - layout: `dataset.json` goes in the pass folder, everything else in `product` or the dataset's single product.
  - The files are staged in `rasters/tmp/` and moved atomically to `rasters/satdump/{station}/incoming/{uploadId}/`.
  - Answers 202 `{uploadId, passFolder}`.
  - The inbox loop scans that root through `RasterSink.uploads()`, an `InboxFiles` over `rasters/satdump/`. A second `InboxScanner(0)` makes an upload ready on the first scan, with or without `inboxPath`. Uploads are processed by the same parsers and overlays and deleted after 2 days.
- **`/media`:** one image plus `meta {passId?, composite, timestamp, satellite}`, handled by the running
  station (`SatDumpStation.receive`, registered while it runs):
  - the guard runs through `ctx.media().decode`;
  - the image is stored with 256 and 1024 px thumbnails, owned by the pass: `passId` if the book knows it, else the 049 join rule, else a minted pass;
  - a `decoded` version is emitted;
  - answers 202 `{mediaId, sha256, passId}`.
- **Multipart limits:** `spring.servlet.multipart` is set to 200 MB per file and 201 MB per request,
  spooling parts over 1 MB. A request over the container limit is 413 `MEDIA_TOO_LARGE`
  (`GlobalExceptionHandler.handleMaxUploadSizeExceededException`).

**6. Optional GDAL.**
- `Dockerfile` has `ARG WITH_GDAL=false`; `true` installs `gdal-bin` only. The compose broker passes
  `WITH_GDAL: ${WITH_GDAL:-false}`, and `docker compose config -q` passes.
- **`GdalWarp`:** `foresignal.raster.gdal.enabled` (`FORESIGNAL_RASTER_GDAL_ENABLED`) is `auto`, `true`
  or `false`. It probes `gdalwarp --version` once at `ApplicationReadyEvent`.
  - It runs `gdalwarp -t_srs EPSG:4326 -of GTiff -co COMPRESS=DEFLATE <in> <out>` as a `ProcessBuilder` argument list.
  - Settings: `GDAL_CACHEMAX=256`, a 120-s timeout with forced kill, on the decode pool.
  - Input and output files live only in `rasters/tmp/` and are deleted afterwards.
- It is reached through `RasterSink.warpToWgs84` for the `UNSUPPORTED_CRS`, `ROTATED` and `BIGTIFF`
  refusals. Without GDAL the default returns empty.

**7. Spec, Postman, docs.**
- **OpenAPI:**
  - both operations are `implemented`, with descriptions;
  - `RemoteRasterBundleMeta` gains `product`;
  - `SatellitePassPayload` gains `geometrySource` and documents `products[].instrument`, `mediaId`, `productId`, `frameKey` and `tilesTemplate`;
  - there is a changelog entry.
- **Postman:** a `FORMDATA_BODIES` table in the generator gives both operations `formdata` bodies with
  file placeholders. `PostmanCollectionContractTest.body()` skips non-raw bodies.
- **Docs:**
  - Operator Guide: the one producer config, `polarGlob`, the `ingestKey` credential, remote Pi push with a curl example, GDAL, health reasons, the GPL boundary;
  - Source Catalog row;
  - Architecture §3 (package layout, dependency rule), §13 (environment variable), §26.

**Verified outside the tests** with scratch programs against the compiled classes (JDK 21 toolchain;
no test was run):

| Check | Result |
|---|---|
| 1.2.2 Texas fixture | 120 × 160, EPSG:4326, gt `[-110, 0.25, 0, 42, 0, -0.25]`, 6416 valid px; footprint 1 part, valid, −106…−84°, 2…42° |
| 1.2.2 Bering fixture | footprint 2 parts, bounds `[167.75, 30, −167.75, 70]` (west > east) |
| Station over the Texas fixture (recording sinks) | 1 frame `satdump-home-rgb-msu-mr-221-composite`, affine grid −110/2/−80/42, pass `geometrySource=footprint`, product entry merged, the `.tif` in provenance, no health reason |
| Station, `polarGlob=nothing/*.tif` | no frame; pass `geometrySource=swath` (906 deg²) |
| 2.0-alpha fixture (049) through the new path | one pass record, swath geometry, products unchanged, 3 budget units over 3 scans |
| Orekit offline | Meteor-M2-3 TLE: 61 points over 10 min, 66.2–66.7 km per 10-s step, first call 0.2 s; a window crossing ±180° (2026-10-12T21:05Z + 4 min) gives 2 parts, bounds `[165.0, 52.6, −127.5, 73.7]`, 715 deg² |
| GeoTiffFixtures (Java) through the reader | 16-bit gray 0/0x4000/0x8000 → `00000000 404040ff 808080ff`; RGBA alpha kept; `GDAL_NODATA` 255 masked; 20000² header-only → OVERSIZE before decode; BigTIFF → "BigTIFF not supported" |

## Files created / modified

Production, created:
- `raster/geotiff/`: `package-info`, `TiffDirectory`, `GeoKeyParser`, `GeoReference`, `GeoTiffReader`,
  `GeoTiffException`, `FootprintBuilder`, `Antimeridian`, `GdalWarp`;
- `orbit/`: `package-info`, `OrekitData`, `SwathEstimator`;
- `raster/SatDumpProvenanceRetentionStep`;
- `source/adapters/satdump/`: `PolarOverlays`, `PolarGlob`;
- `source/spi/`: `RemoteMediaReceiver`, `RemoteMediaReceivers`;
- `inbound/`: `RemoteUploadController`, `RemoteBundles`, `RemoteMediaRegistry`;
- `resources/orekit-data/`: `tai-utc.dat`, `finals2000A.all`.

Production, modified:
- `source/spi/`: `RasterSink` (`warpToWgs84`, `uploads`), `SourceDescriptor` (`acceptsRemoteMedia`,
  `REMOTE_MEDIA_KEY`, `REMOTE_MEDIA_SEGMENTS`);
- `source/InboxFiles` (`rootedAt`);
- `raster/`: `RasterPipeline`, `AffineGrid`, `FrameAgeHealthRule`;
- `source/adapters/satdump/`: `SatDumpStation`, `PassBook`, `PassState`, `InboxScanner`, `StationStore`,
  `SatDumpDescriptor`, `SatDumpAdapter`, `SatDumpFamily`, `XritProducts` (`bounded`);
- `inbound/`: `IngestAuthenticator`, `IngestController`, `format/IngestFormatRegistry`;
- `api/error/GlobalExceptionHandler`;
- `application.yml`, `build.gradle.kts`, `gradle/libs.versions.toml`, `Dockerfile`, `docker-compose.yml`.

Docs and spec: `docs/OpenAPI.json`, `docs/Operator-Guide.md`, `docs/Source-Catalog.md`,
`docs/Architecture.md`, `THIRD_PARTY_NOTICES.md`, `postman/Foresignal.postman_collection.json`,
`scripts/generate-postman.py`, `src/test/resources/openapi/pending-operations.txt`.

Tests:
- Created:
  - `raster/geotiff/`: `GeoKeyParserTest`, `GeoTiffReaderTest`, `FootprintBuilderTest`, `GdalWarpTest`;
  - `orbit/SwathEstimatorTest`;
  - `source/adapters/satdump/`: `PolarOverlaysTest`, `PolarGlobTest`, `PolarOverlayIT`;
  - `inbound/`: `RemoteBundlesTest`, `RemoteIngestIT`;
  - `raster/SatDumpProvenanceRetentionStepTest`;
  - `testsupport/raster/GeoTiffFixtures`.
- Modified: `AccessPolicyTest`, `FrameAgeHealthRuleTest`, `GridDefTest`, `SatDumpDescriptorTest`,
  `SatDumpFixtures`, `SatDumpTestContext`, `RasterTestSources`, `PostmanCollectionContractTest`.
- Fixtures: `fixtures/satdump/1.2.2/` (two pass folders), `generate-geotiff.py`, and the README section.

## Deviations from the prompt

1. **The producer config needs `"auto": true`.** SatDump 1.2.2's `projectImg` logs "No width or height
   defined for projection!" and writes nothing unless the target config has `width`/`height` or
   `auto: true` (`image_processor.cpp`, `reprojector.cpp` `tryAutoTuneProjection`, default 0.016°). The
   Operator Guide documents `{"type": "equirec", "auto": true}`. Weather-Imagery-Design §4.5 still shows
   the form without it (Issue 2).
2. **`affine` grid parameters are `crs` and `gt0 … gt5`**, the GDAL corner geotransform 045 built, not
   the prompt's `{crs, a, b, c, d, e, f}`.
3. **Remote-key auth.** Built-in sources have no custom-source row, so there is no minted ingest key. A
   `satdump-*` source declares an optional `ingestKey` credential, encrypted at rest. `IngestAuthenticator`
   accepts it, in constant time, only on that source's own `media` and `rasters` paths. An admin token
   always works. This is recorded in the spec changelog (Issue 1).
4. **`/rasters` bundle layout.** File names are single segments (the spec pattern), so a bundle cannot
   carry product subdirectories. Without a placement rule, the `product.cbor` files of a
   multi-instrument pass would collide. The spec gains an optional `product`: the directory of every
   file except `dataset.json`, defaulting to the dataset's single product. A multi-instrument pass is
   sent as one bundle per product, and the pass join merges them.
5. **`/media` needs the station running** (404 otherwise). The join state lives in the running station,
   which registers a `RemoteMediaReceiver` through the new SPI type `RemoteMediaReceivers`. A disabled
   station cannot say which pass an image belongs to. `/rasters` is accepted while the station is
   disabled, and is processed when it runs, unless the 2-day upload expiry comes first.
6. **No `celestrak-gp` TLE fallback yet.** 064 does not exist, so the swath uses the `product.cbor` TLE,
   or the pass's stored TLE.
7. **SPI and package placement.**
   - The reader is `raster.geotiff` and the Orekit code is a new top-level `orbit` package (the prompt names no package; 064 reuses it).
   - The adapter calls both directly as stateless helpers, like `raster.decode`. GDAL and the upload area come through `RasterSink`, in keeping with 049's approach to the Architecture §3 rule. Architecture §3 is updated.
8. **Health reasons.** The polar reasons replace 049's `bigtiff` key with a `polar` key; the BigTIFF
   text stays "BigTIFF not supported.". The reader's own exception text has no period, as the prompt
   gives it.
9. **Postman:** the bodies are in a new `FORMDATA_BODIES` table, because `BODIES` holds raw JSON only.
10. **`AffineGrid` and `FrameAgeHealthRule` changes** were not named in the prompt. Both are needed for
    a Bering Sea overlay to render, and for a pass product not to read as stale between passes.
11. **Orekit data trimmed.** `finals2000A.all` holds the rows from 2024-01-01 (267 KB instead of
    3.8 MB). Outside its range Orekit uses zero corrections.

## Issues found / open questions

**Needs the owner:**

1. **The remote key mechanism (Deviation 3).** Should built-in push-capable sources get minted ingest
   keys (a `rotateIngestKey` equivalent outside custom sources) instead of an operator-chosen
   credential?
2. **Weather-Imagery-Design §4.5 item 2 lacks `"auto": true`** (Deviation 1). Should the design text
   change?
3. **§1.5 Q1 is still unanswered** (049 Issue 1). The overlay producer is documented for 1.2.2, while
   049's `/api` and inbox defaults assume 2.0.0-alpha. A 2.0 station gets swaths only until 107. Which
   version does the station run?

**Other findings:**

4. **The spec and the code disagree on `SatellitePassPayload.tle`.** The spec says `string | null`, but
   049 emits an object `{name, line1, line2}`. This is untouched here; which should change?
5. **049 Issue 5 (a plain gray style) is still open.** Polar overlays are rgba8 and use `rgb@1`, so they
   are unaffected.
6. **Polar swaths near a pole leave a gap**, where quads straddling a pole are dropped. Passes over
   Texas never get there.
7. **No instructions directed at an agent** appeared in the SatDump source, in the Orekit data, in the
   CelesTrak response, or in anything else read.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `geotiff/GeoKeyParserTest` | SatDump 1.2.2 4326 tags; big-endian; offset tie point; 3857 via `ProjectedCSType` (with `GeographicType` also present); PixelIsPoint shift; axis-aligned ModelTransformation read, rotated refused (GDAL can help); EPSG 32614 and 4269 refused with the exact reason; user-defined 32767; no keys, no tie point, no scale |
| `geotiff/GeoTiffReaderTest` | BigTIFF refused first; 20000² and 12001-wide headers refused as OVERSIZE without decoding (no pixel data, no cause), while a 100² header without pixels fails only at decode; CRS refused before decode; RGB zero mask; `GDAL_NODATA`; RGBA alpha; 16-bit grayscale scaling; SampleFormat 3 and non-TIFF refused; the producer fixture |
| `geotiff/FootprintBuilderTest` | rectangle outline exact to the corner and simplified to 5 points; slanted swath simplification within tolerance (area within 2 %); empty rows give parts, an empty mask gives nothing; antimeridian split for a grid past 180° (two parts, bounds, area preserved); a wrapped global grid; 3857 in degrees |
| `geotiff/GdalWarpTest` | absent gives unavailable, empty and one probe; `false` never runs; failing probe; exact argument list, `GDAL_CACHEMAX=256`, 120 s, files only in `rasters/tmp` and deleted; non-zero exit and timeout give an IOException with files deleted; the real runner on a missing program |
| `orbit/SwathEstimatorTest` | width from `corr_swath` and the table; offline ground track (61 points, 60–75 km steps); a mid-latitude swath containing its track; the Bering Sea window split at ±180° (two parts, bounds west > east, about 715 deg²); degenerate input; the 30-minute cap |
| `satdump/PolarOverlaysTest` | producer fixture: spec, frame grid and box, footprint, frame record, pass geometry and `geometrySource`, merged product entry, provenance, no reason; Bering (west > east, two parts); `polarGlob` with no match gives the swath; refused CRS gives the reason and swath, and with GDAL the warped overlay; uploads processed on the first scan without charge; pushed image owned by the joined pass, then by `passId`; helpers (composite, product id length, merge, grid box); the swath window |
| `satdump/PolarGlobTest` | `**/*.tif` at any depth and case; `*`, `?`, literals; refused globs |
| `satdump/PolarOverlayIT` | real pipeline and writer: dynamic pass product, complete frame with a 1-part footprint, one `raster-frame` version, pass geometry polygon with `footprint`, `tilesTemplate` with the frame key, z4/3/6 tile renders the band, provenance copied, symlinked GeoTIFF neither read nor copied; RF session at the station point then the footprint (`ST_Point`, `ST_Point`, polygon); `polarGlob` honored with the swath |
| `inbound/RemoteIngestIT` | bundle with the `ingestKey` credential: 202, files under `incoming/`, processed by one scan to a decoded pass with footprint and a frame; admin token works; 26 MB part gives 413 `MEDIA_TOO_LARGE`; traversal in meta and in part names gives 400; `system-canary` gives 404 `NOT_FOUND`, never `FORMAT_UNKNOWN`; no key or a wrong key gives 401, bearer key gives 202, the key refused on `ingestRecords`; reserved names and listing; `/media` 404 while not running, then 202 with media owned by the pass, a non-image 400, bad meta 400; non-multipart 415 |
| `inbound/RemoteBundlesTest` | layout and atomic move, nothing left in staging; `product` override; meta and part validation; size caps judged from declared sizes (never reads an oversize part); guard refusal versus BigTIFF passthrough; safe names |
| `raster/SatDumpProvenanceRetentionStepTest` | 30 d provenance and 2 d uploads; newer kept, `incoming` kept; a newer nested file keeps a pass; a planted link removed as a link, its target kept |
| `AccessPolicyTest` (added) | `POST /v1/ingest/{satdump}/media` and `/rasters` are `INGEST_KEY_OR_ADMIN` |
| `FrameAgeHealthRuleTest` (added) | a pass product 3 days old stays healthy and reports `newestFrameAt` |
| `GridDefTest` (added) | 4326 affine grid past 180° found one turn east, and back |
| `SatDumpDescriptorTest` (added) | `polarGlob` default and check, the `ingestKey` credential, `acceptsRemoteMedia` |

## Compile result

- `./gradlew compileJava compileTestJava`: `BUILD SUCCESSFUL`.
- `./gradlew lintOpenApi`: valid, 71 warnings (unchanged).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: 829 requests, all operations covered.
- `docker compose config -q`: OK.
- New dependency: `org.orekit:orekit` 13.1.9 (Apache-2.0) with Hipparchus 4.0.3 (Apache-2.0).

## Notes for the next prompt

1. **055 (`get_map_image`, static maps):** polar overlays are ordinary rgba8 `affine` products
   (`satdump-{station}-{composite}`, `rgb@1`) and render through the existing paths. A frame past 180°
   has `west > east`.
2. **057 checkpoint:**
   - Context sharing:
     - `PolarOverlayIT` and `RemoteIngestIT` use the `RasterTestSources` web context (no new context);
     - `satdump-itest` now also takes `RemoteMediaReceivers`.
   - What the ITs leave behind:
     - both remove the `satdump-itest-rgb-msu-mr-221-composite` dynamic product;
     - `RemoteIngestIT` sets the `ingestKey` credential and writes uploads under `rasters/satdump/itest/incoming/` (expired by retention after 2 days);
     - `PolarOverlayIT` creates a temp file outside the inbox for the symlink case and deletes it.
   - Other behavior to know:
     - the new unit tests run Orekit offline (about 0.2 s for the first context);
     - `InboxScannerTest.processing` and `SatDumpIngestIT` now also compute a swath for the 2.0 fixture (its TLE parses), which changes no assertion;
     - multipart limits are 200 MB per file and 201 MB per request.
3. **064 (celestrak-gp, predicted passes):**
   - Reuse `ai.foresignal.orbit.OrekitData.context()` and `SwathEstimator.groundTrack`.
   - Add the cached-TLE fallback in `PolarOverlays.swath` (Deviation 6).
   - Replace the trimmed `finals2000A.all` when its predictions run out (late 2026).
4. **107 (SatDump 2.0 hook):** a 2.0 station gets swaths only; the hook should write a 4326 GeoTIFF into
   the pass folder so `polarGlob` picks it up.
5. **143:** measure:
   - a real 1.2.2 projected composite: size, decode time and heap, against `MAX_GEOTIFF_BYTES` of 200 MB and the 150 MP guard;
   - the footprint vertex count;
   - Orekit's first-use time on the bench server.
6. **144 (Flyway):** no new tables or columns.
   - `adapter_state` gains `uploads.watches`, `uploads.processed` and `polar.products`.
   - `PassState` JSON gains `geometry` and `geometrySource`.
   - `satdump-*` sources may hold a `source_credential` row (`ingestKey`).
7. **Owner decisions pending:** Issues 1–4 above, plus those carried from 012–053.
