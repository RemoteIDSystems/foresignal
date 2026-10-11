# 054 — SatDump Polar Overlays

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/054-CR-SatDump-Polar-Overlays.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/053-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Weather-Imagery-Design.md` §0.1 (rows "SatDump polar georeferencing", "SatDump"), §1.5 (owner questions), §2.4 (satdump provenance path, SatDump polar ladder 30 d), §3 (GeoTIFF reader, image guard, Orekit, GDAL rows; licence boundaries), §4.4 (pass geometry order), §4.5 items 2–4, §4.6, §4.7(b), §6.6 (ingest endpoints), §11 (rows on two SatDump versions and Orekit). The design's "043"/"043b" polar prompt = design 041 = v1.2 054; its "074"/"075" SatDump extras = 107; "044/046 celestrak-gp" = 064.
- Completion Reports of **049** (station source, inbox loop, pass lifecycle, owner's answers to §1.5 if recorded, pinned SatDump version), **043** (MediaStore, ImageGuard, thumbnails), **045** (`affine` GridDef, tiles), **042** (FrameStore, ladders), **024** (ingest keys, `/v1/ingest/{id}/{format}` routing), **020** (OpenAPI conformance).
- GeoTIFF 1.1 (OGC 19-008r4) tags 33550, 33922, 34264, 34735; Orekit 13.1 TLE/SGP4 docs.

## Objective

Turn decoded polar passes (Meteor-M LRPT, optional Metop AHRPT) into georeferenced map overlays with real footprints, and give passes without GeoTIFFs an approximate swath. Foresignal only **reads** EPSG:4326/3857 GeoTIFFs that SatDump wrote into a pass folder; it never reimplements SatDump's projection. Add remote-Pi push ingest for media and pass bundles, and an optional GDAL path for exotic CRS.

## Tasks

### 1. Producer version (decide first)

- Read 049's CR and `docs/Operator-Guide.md` SatDump section for the owner's SatDump version (design §1.5 Q1). Document exactly **one** producer path:
  - **1.2.2:** composite `"project": {"config": {"type": "equirec"}, "img_format": ".tif", "draw_map_overlay": false}`.
  - **2.0-alpha:** preset `project` keys are ignored; passes get thumbnails and approximate swaths only until the hook in 107.
  - **No answer recorded:** default to the 1.2.2 composite `project`, best-effort, and say so in the CR.
- Add one GeoTIFF fixture produced by that version (or a synthetic GeoTIFF with identical tags, documented as such, if no real sample is available).

### 2. GeoTIFF reader (`ai.foresignal.raster.geotiff`)

- JDK `javax.imageio` TIFF plugin plus a small GeoKey parser: ModelPixelScale (33550), ModelTiepoint (33922), ModelTransformation (34264, axis-aligned only; rotation → reject), GeoKeyDirectory (34735).
- Accept EPSG:4326 (`GeographicTypeGeoKey` 2048 = 4326) and EPSG:3857 (`ProjectedCSTypeGeoKey` 3072 = 3857); honour `GTRasterTypeGeoKey` (PixelIsArea vs PixelIsPoint half-pixel shift). Anything else → GDAL path (task 6) or rejection with health reason "GeoTIFF CRS EPSG:{n} not supported (build with WITH_GDAL=true)".
- **BigTIFF** (header magic 43) → rejected with health reason "BigTIFF not supported" (or routed to GDAL when enabled).
- **ImageGuard first:** read dimensions from the header and reject > 12,000 px per side or > 150 MP **before** decoding (043's guard). 8-bit RGB/RGBA and 16-bit grayscale; RGB without alpha gets alpha from a nodata/zero mask.
- Output: an RGBA `affine` frame (grid `type=affine`, `definition{crs, a, b, c, d, e, f}`) through `ctx.rasters()` → FrameStore (`.fsg` dtype 5 rgba8, deflate). Product per pass composite: dynamic product `satdump-{station}-{composite}` (049's dynamic-product mechanism), `temporal.type=pass`, `coverage: swath`, style `rgb@1`, ladder 30 d.

### 3. Footprint and pass geometry

- Footprint from the alpha mask: per row, the leftmost and rightmost valid pixel; build the polygon (left edge top→bottom, right edge bottom→top) in grid coordinates, transform to 4326, `TopologyPreservingSimplifier` at ~0.02°, validate with `GeometryFixer`.
- **Antimeridian:** unwrap longitudes; if the span crosses ±180, split into a MultiPolygon at 180 (test with a Bering Sea pass fixture).
- Store as `raster_frame.footprint`; update the `satellite-pass` record geometry using the order **actual footprint → approximate swath → station point** (design §4.4). Add `products[].tilesTemplate` (content-addressed) to the pass payload. Re-processing a pass replaces the frame (new `sha8`) and writes one new pass version.
- `polarGlob` param on `satdump-{station}` (default `**/*.tif`), matched only inside **ready** pass folders (049 readiness rules) with `InboxFiles` (NOFOLLOW, per-file realpath checks).
- Copy the original GeoTIFF to `${FORESIGNAL_DATA_DIR}/rasters/satdump/{station}/{passId}/` as provenance (`.cadu` and basebands are never copied).

### 4. Approximate swath (Orekit)

- Add `org.orekit:orekit:13.1.9` (verified latest 13.1.x on Maven Central, 2026-10; Apache-2.0) and a pinned minimal `orekit-data` subset (UTC-TAI history plus one EOP file) under `src/main/resources/orekit-data/`, loaded through a `ClasspathCrawler` `DataContext` at first use. Orekit is not loaded before this prompt's code paths run; 064 reuses this setup.
- `SwathEstimator`: TLE from `product.cbor` (fallback: 064's `celestrak-gp` cache when present), SGP4 between the first and last product timestamps at 10-s steps, ± half swath across track (`corr_swath` from the product, else per-instrument table: MSU-MR ≈ 2,800 km, AVHRR ≈ 2,900 km, MHS ≈ 2,200 km). Build the polygon, split at the antimeridian.
- Used when a pass has no usable GeoTIFF (2.0 path, rejected CRS, failed decode). Never produce tiles from it.
- Add the THIRD_PARTY_NOTICES entry for Orekit and its data.

### 5. Remote push ingest (remote Pi, design §4.7 b)

- `POST /v1/ingest/{sourceId}/media` and `POST /v1/ingest/{sourceId}/rasters`, ingest-key auth (024's `INGEST_KEY_OR_ADMIN`). Only sources whose descriptor sets `acceptsRemoteMedia=true` (the `satdump-*` family); others → 404 `NOT_FOUND`.
- **Routing:** `media` and `rasters` are reserved segments and must win over 024's `/v1/ingest/{id}/{format}` IngestFormat route; register both names as reserved in the IngestFormat registry so no format can take them.
- `/rasters`: `multipart/form-data` pass bundle: a `meta` JSON part `{passFolder, files[]}` plus file parts (`dataset.json`, `product.cbor`, PNG, TIF). Each file ≤ 25 MB (413 `MEDIA_TOO_LARGE`), bundle ≤ 200 MB, filenames sanitised (no separators, no dot-dot). Written atomically to `${FORESIGNAL_DATA_DIR}/rasters/satdump/{station}/incoming/{uploadId}/` (the inbox mount is read-only); the inbox loop scans this second root with the same parsers and treats a completed upload as ready. Returns 202 `{uploadId, passFolder}`.
- `/media`: single image + `meta {passId?, composite, timestamp, satellite}` → ImageGuard → MediaStore + thumbnails; attaches to the matching pass by the 049 join rule, else mints a pass.
- Budget: these count against the source's LAN budget; recommend rsync/Syncthing (design §4.7 a) in docs as the primary path.

### 6. Optional GDAL (`WITH_GDAL`)

- `Dockerfile` build-arg `WITH_GDAL=false`; when true install `gdal-bin` only (no AGPL drivers, no JNI).
- `GdalWarp`: detect `gdalwarp --version` at startup (property `foresignal.raster.gdal.enabled` auto); run `gdalwarp -t_srs EPSG:4326 -of GTiff -co COMPRESS=DEFLATE <in> <out>` via `ProcessBuilder` with an argument list, env `GDAL_CACHEMAX=256`, a 120-s timeout, inputs/outputs only under Foresignal temp or inbox paths, on the decode pool. Used for non-4326/3857 GeoTIFFs and BigTIFF.

### 7. Spec, Postman, docs

- `docs/OpenAPI.json` (tag Ingest): `ingestSourceMedia`, `ingestSourceRasters` (multipart schemas, 202/413/404), `x-foresignal-scope: open` (the value the spec uses for `/v1/ingest/{sourceId}`; the ingest key is checked by the endpoint); problem `MEDIA_TOO_LARGE`. `AccessPolicy`: covered by `INGEST_KEY_OR_ADMIN` on `/v1/ingest/**`; add explicit tests. Postman `BODIES` for both (multipart placeholders); regenerate.
- `docs/Operator-Guide.md` SatDump section: the one producer config, `polarGlob`, remote Pi options (sync recommended; push endpoints with an ingest key), GDAL build-arg, health reasons. GPL boundary note: Foresignal reads SatDump output only.
- `docs/Source-Catalog.md` `satdump-{station}` row: polar overlays, footprints, swath fallback.

## Tests to write (do not run)

- `GeoKeyParserTest`: 4326 and 3857 fixtures, PixelIsPoint shift, rotated transformation rejected, unsupported EPSG reason.
- `GeoTiffReaderTest`: BigTIFF rejected before decode, ImageGuard rejects oversize from header only (assert no raster read), 16-bit grayscale, RGB without alpha mask.
- `FootprintBuilderTest`: alpha-mask outline, simplification bounds, antimeridian split.
- `SwathEstimatorTest`: a Meteor-M2-3 TLE fixture propagates offline; swath width from `corr_swath` and from the table; output crosses the antimeridian correctly.
- `PolarOverlayIT`: a ready pass folder with the producer fixture → frame, footprint, tiles render, pass geometry upgraded from station point to footprint, provenance copy, `polarGlob` honoured, symlink escape ignored.
- `RemoteIngestIT`: `/rasters` bundle processed like an inbox pass; 413 on a 26 MB part; path-traversal filenames rejected; wrong source → 404; ingest key required; `media`/`rasters` never routed to an IngestFormat.
- `GdalWarpTest`: disabled when absent; argument list and timeout (process mocked).

## Acceptance criteria

- A pass folder from the documented producer yields a tiled RGBA overlay and a footprint geometry on its `satellite-pass` record.
- Passes without usable GeoTIFFs get an Orekit approximate swath; nothing reimplements SatDump's raytracer.
- Remote bundles flow through the same inbox pipeline with size caps and ImageGuard.
- Only one SatDump producer path is documented.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 139, 142, 143, 144).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `054: SatDump Polar Overlays`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/054-CR-SatDump-Polar-Overlays.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `054: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
