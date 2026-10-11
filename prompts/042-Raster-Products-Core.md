# 042 — Raster Products Core

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/042-CR-Raster-Products-Core.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/041-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Weather-Imagery-Design.md` §0.1 (settled choices), §2.1–§2.6 (components, framework reuse, data model, `.fsg` format, storage layout, retention, budgets, memory profiles), §6.1 (time, frame keys, caching), §6.2 (catalogue and frames), §6.8 (SSE), §6.11 (AccessPolicy, problem codes), §7.1 (derived-record principles). Its prompt numbers are translated with `docs/Roadmap.md` §5 (design 029 = this prompt; design 031 = 045, 032 = 046, 036 = 049, 037 = 050, 038 = 051).
- `docs/Core-Data-Model.md` §2 (time contract), §3 (descriptor fields), §5.3 (defaults), §10 (buses), §13 (retention)
- `docs/OpenAPI.json` schemas `SourceDescriptor`, `Change`, `SystemInfo`, `RetentionSettings`; operations `listChanges`, `streamChanges`
- Completion Reports 004, 005, 006, 014, 015, 019 (what the hooks below actually look like)

## Objective

Build the raster product core that every weather and imagery prompt sits on: the product catalogue, the native-grid frame store with its `.fsg` codec, `raster-frame` records, frame retention and the read API for products and frames. Frames live on disk and in `raster_frame`; each observation frame (or each completed forecast run) also becomes one `raster-frame` record version, so SSE, resume, MCP and triggers work with no new distribution code. No real upstream decoder ships here (046 adds GRIB2/NetCDF); a synthetic test product proves the pipeline.

## Tasks

### 1. Hooks from earlier prompts (check, add if missing)

- **004:** `SourceDescriptor.products` (`List<ProductSpec>`), `kindRetention`, `Mode.DERIVED`, `ParamType.LAN_URL` and `INBOX_PATH` exist in `ai.foresignal.source.spi`. Keep them.
- **005:** `SourceHttp.getRange`, `download(url, Path tmp, maxBytes)`, `listS3(bucket, prefix, startAfter)`, `BudgetGovernor` `mbPerDay` (charged in advance from the known size, corrected afterwards) and the per-descriptor `maxResponseBytes`. Add whichever is missing exactly as Weather-Imagery-Design §2.2 describes.
- **006:** the `HealthRule` SPI and the optional `newestFrameAt` / `frameAgeSeconds` fields on the health view.
- **014 / 015:** an `externalId` filter (exact match, repeatable) on `GET /v1/changes` and `GET /v1/changes/stream`, and `kind=raster-frame` change events always carrying their payload inline (under 1 KB). Add the parameter to `listChanges` and `streamChanges` in the spec if absent.
- **019:** per-kind retention resolution `retention.kinds.{kind}` → `retention.sources.{id}` → descriptor `kindRetention[kind]` → `retention.default`, with the RetentionJob iterating (source, kind) pairs. Add it to `RetentionSettings` (`kinds` map) and the job if missing, and expose a `RetentionStep` extension point the job calls after its record steps.

### 2. ProductSpec and ProductRegistry (`ai.foresignal.raster`)

- Extend `ProductSpec` (currently `id, title, kind`). **`kind` now means the product kind** (`raster | field | external`); the record kind for frames is always `raster-frame`, so fix the Javadoc. Add: `category` (radar, satellite, model, lightning, imagery), `units`, `dtype` (u8, u16, i16, f32, rgba8), `scale`, `offset`, `nodata` (raw code), `codes` (categorical and special values, e.g. `{0:"noCoverage",1:"noEcho"}`), `styles` and `defaultStyle`, `temporal{type observation|forecast|pass, cadence, maxGap}`, `spatial{coverage CONUS|global|station-disc|swath, nativeMaxZoom, minZoom}`, `retention` ladder (list of `{olderThan, keepEvery}` rungs, or `runs: 2` for forecasts), structured `attribution{text, html, url, licence, licenceUrl, requiredDisplay}`.
- `ProductRegistry`: collects specs from every registered descriptor at startup, validates (id pattern, unique across sources, the owning source lists `raster-frame` in `kinds`, ladder rungs ascending, `maxGap ≥ cadence`), upserts `raster_product` rows, and supports **dynamic products** (`registerDynamic(sourceId, spec)`, `dynamic=true`) for SatDump (049). Publishes `ProductCatalogChangedEvent`.
- A source declaring products gets `zeroResultsNormal=true` by default.

### 3. Entities (Hibernate; GiST through `SpatialIndexInitializer`)

Implement exactly as Weather-Imagery-Design §2.3:
- `raster_product`: `id` PK, `source_id`, `category`, `kind`, `spec` jsonb, `dynamic`, `created_at`, `updated_at`.
- `raster_grid`: `id` = sha256 of the canonical definition JSON, `type` (latlon | geos | lcc | affine), `definition` jsonb, `nx`, `ny`, `bbox` Polygon 4326.
- `raster_frame`: PK (`product_id`, `frame_id`); `valid_time`, `reference_time`, `forecast_step`, `obs_start`, `obs_end`, `grid_id`, `path`, `bytes`, `sha256` (of the **decoded** content), `dtype`, `codec`, `stats` jsonb, `footprint` MultiPolygon 4326, `upstream_key`, `status` (partial | complete), `thinned`, `ingested_at`, `expires_at`. Indexes (`product_id`, `valid_time` DESC), (`product_id`, `reference_time`, `forecast_step`), GiST(`footprint`).
- `frame_id` is time-based and immutable: `20261010T143240Z` for observations, `2026101012Z-f006` for forecast steps.

### 4. `.fsg` codec and FrameStore

- `ElgCodec`: the 64-byte little-endian header in §2.3 (magic `FSRG`, version 1, dtype, codec 0/1, nx, ny, bands, flags bit 0 = partial, reserved zeros, scale, offset, nodata, payloadLength, crc32c, reserved). Payload band-sequential, rows north→south, `Deflater(1)` for codec 1. Reading verifies magic, version, reserved fields and CRC32C (`java.util.zip.CRC32C`) and rejects anything else with `RasterFormatException`.
- `FrameStore`: writes to `${FORESIGNAL_DATA_DIR}/rasters/tmp/` then atomic-renames to `rasters/frames/{productId}/{yyyy}/{mm}/{dd}/{frameId}.fsg`; computes sha256 of the decoded content (URL token `sha8` = first 8 hex chars); reads through a Caffeine `frameCache` with a byte weigher. Create the §2.4 directory layout at startup.
- `GridStore`: dedupes `raster_grid` by definition hash. 045 adds the projection maths; here a grid is stored data only.

### 5. Raster pipeline and `ctx.rasters()`

- Add `RasterSink rasters()` to `FetchContext` (and `StreamContext`). `RasterSink.commit(FrameInput)` takes product id, times, grid definition, decoded band arrays (or a temp file plus decoder handle) and status; it quantises per spec, computes `stats` (min, max, mean, coverage %, per-code counts for categorical products), footprint (grid bbox clipped to valid-data rows, as MultiPolygon), writes the `.fsg`, upserts `raster_frame` and **returns the `raster-frame` `NormalizedRecord`** for the adapter to include in its `FetchResult`. Adapters never touch the store directly.
- Heavy decode/quantise runs on `RasterDecodePool`: a bounded platform-thread pool behind a semaphore (1 permit in the small profile, 2 in the large), never on virtual threads.
- **`raster-frame` record** (source = owning source, `externalId` = productId):
  - Observation: one version per frame. `observedAt = eventTime = valid_time`. Payload `{productId, frameKey, time, timeKey, tiles, bounds, stats, expiresAt}`.
  - Forecast: one version per **completed run**. `observedAt = eventTime = reference_time`. Payload `{productId, run, steps[], validTimes[], tilesTemplate}`. Optionally an `{productId}:partial` externalId reports the run in progress and is tombstoned when the run completes.
  - Geometry = product bounds. STANDARD tier, `recordTtl` null, `volatileFields=[expiresAt]`, `kindRetention{raster-frame: 7d}`.
  - `ProductUrls` builds the `tiles` and `image` templates for the routes 045 serves (`/v1/products/{id}/tiles/{frameKey}/{z}/{x}/{y}.png?style={name@v}`).
- Publish `RasterFrameCommittedEvent(productId, frameId, status)` after commit (045 warms tiles from it).

### 6. Frame resolution and the read API

- `FrameResolver`: aliases `latest` (newest complete observed frame; for forecasts the newest complete run's step nearest now), `now` (valid time ≤ now with the smallest gap within `maxGap`), `run=latest|previous|<runIso>`; bare `timeKey`; `frameKey` with sha8 check (stale sha8 → 404 `UNKNOWN_FRAME` plus a `Link` header to the current key); ISO-extended time in a path → 301 to the canonical `timeKey`; partial frames excluded unless `includePartial=true`; forecast concrete time without `run` → 400 `RUN_REQUIRED`; retention-removed → 410 `FRAME_EXPIRED`.
- Endpoints (all `read`):
  - `GET /v1/products?category&kind&bbox&source&q&compact` with every field in §6.2 (`coverage` plus its one-line meaning, `temporal{type, cadence, maxGap, earliest, latest, retention, runs[]}`, `spatial`, `render{defaultStyle, styles[], defaultOpacity}`, `legend` link, attribution, `status{health, stale, lastFrameAt, reason}`, links). ETag + conditional GET.
  - `GET /v1/products/{id}`
  - `GET /v1/products/{id}/frames?from&to&run&limit&cursor&includePartial`: cursor page; each frame has `time`, `timeKey`, `frameKey`, `expiresAt`, `status`, `run`/`step` for forecasts, expanded `tiles` and `image` templates; plus `prefetch{recommendedFrames: 12, maxZoomForAnimation}`, `serverTime`, `earliest`.
  - `GET /v1/products/{id}/frames/latest` (short max-age).
- Responses on aliases get `Cache-Control: {public|private}, max-age=min(cadence/2, 60s)` with an ETag; `public` only when `security.openReads` covers the path. Clients never build time strings: the server returns fully expanded URLs.
- Problem codes in `ErrorCodes`: `UNKNOWN_PRODUCT` 404, `UNKNOWN_FRAME` 404, `FRAME_EXPIRED` 410, `RUN_REQUIRED` 400, `PRODUCT_UNAVAILABLE` 503.

### 7. Health, probe, retention

- `FrameAgeHealthRule` (the 006 SPI): a product source is `degraded` when its newest frame is older than `3 × cadence` or `maxGap`; the view gets `newestFrameAt` and `frameAgeSeconds`. A missing upstream field or variable reported by a decoder degrades health and writes **no** empty frame (`SilentFormatChangeException` → health reason).
- Probe for product sources: the adapter's probe lists the newest object, decodes the header only and returns a grid summary (`nx`, `ny`, grid type, valid time, variable). Writes nothing.
- `RasterRetentionStep` (registered on the 019 extension point, not `@Transactional`):
  - Ladder thinning (e.g. MRMS every frame 6 h then 10-min frames to 48 h; GOES C13 5-min 24 h then 30-min to 72 h; forecasts current + previous complete run; SatDump geostationary 72 h): keep the frame nearest each `keepEvery` boundary, mark it `thinned`, recompute `expires_at`; delete the row, **then** unlink the file.
  - Quota: `foresignal.raster.disk-quota-gb` (default 200, env `FORESIGNAL_RASTER_DISK_QUOTA_GB`) evicts oldest-first; at ≥ 90% `/v1/system` degrades.
  - Hourly orphan sweep: remove files with no row, rows with no file, and **re-emit the `raster-frame` record** for any complete frame newer than its record head (crash between frame commit and enqueue). Re-emits go through the ingest queue as a `MANUAL` fetch-log row with principal `system:raster-sweeper`.

### 8. Memory profiles and `/v1/system`

- `FORESIGNAL_PROFILE=small|large` (`foresignal.profile`, default `small`) selects `foresignal.raster.*` defaults: frame/tile/index cache MB 768/128/64 (small) or 1536/256/128 (large); render pool `min(cores−2, 6)` or `cores−2`; decode concurrency 1 or 2. Each value is individually env-overridable. Never derive from `MaxRAMPercentage`.
- `docker-compose.yml`: `FORESIGNAL_PROFILE`, `JAVA_TOOL_OPTIONS` default `-Xmx4g -XX:+UseG1GC` (large: `-Xmx8g -XX:+UseZGC -XX:+ZGenerational`) and `mem_limit` 6g (large 11g), all overridable from `.env`.
- `/v1/system` gains a `rasters` block: profile, products, frames, storeBytes, diskQuotaPct, cache hit and eviction rates per cache, renderQueue (filled by 045), renderBusyLastHour, mbDownloadedToday.

### 9. Spec, Postman, docs

- `docs/OpenAPI.json`: tag `Products`; operations `listProducts`, `getProduct`, `listProductFrames`, `getLatestProductFrame` (each `"x-foresignal-scope": "read"`); schemas `Product`, `ProductPage`, `ProductTemporal`, `ProductSpatial`, `Attribution`, `RasterFrame`, `RasterFramePage`, `RasterFrameEvent` (observation and forecast variants), `SystemInfo.rasters`; the `externalId` parameter on the change operations; the five problem codes. Regenerate Postman with `python3 scripts/generate-postman.py docs/OpenAPI.json postman`.
- `AccessPolicy`: `GET /v1/products/**` and `GET /v1/styles/**` → READ (add before the `/v1/**` admin default).
- `docs/Architecture.md`: §3 packages `raster`, `raster.render`, `raster.sample`, `media`, `place`, `derive`; §7 the three tables; §11 `retention.kinds` and the ladders; §13 `FORESIGNAL_PROFILE`, `FORESIGNAL_RASTER_DISK_QUOTA_GB`; §14 the two memory profiles, stating that Ollama plus Postgres plus the small profile is the limit of a 32 GB host.

## Tests to write (do not run)

- `ElgCodecTest`: golden file `src/test/resources/raster/golden-u8.fsg` (byte-exact header), round trip per dtype, CRC mismatch, bad magic, non-zero reserved bytes, partial flag.
- `FrameStoreIT`: atomic write (no partial file visible on simulated failure), sha256 of decoded content stable across codecs, cache weigher accounting.
- `ProductRegistryTest`: validation failures (duplicate id, missing `raster-frame` kind, bad ladder), dynamic registration event.
- `FrameResolverTest`: `latest`, `now` within and beyond `maxGap`, `run=previous`, stale sha8 → `UNKNOWN_FRAME` with `Link`, ISO → 301, partial exclusion, `RUN_REQUIRED`.
- `RasterPipelineIT` with a test-only `SyntheticRasterAdapter` (latlon u8 grid, observation and forecast variants): frame row, file, record version per frame, one version per completed run, payload < 1 KB, SSE `externalId` filter delivers the event inline.
- `RasterRetentionStepIT`: ladder thinning keeps boundary frames; row deleted before file; quota eviction; orphan sweep removes both orphan kinds and re-emits a missing record.
- `FrameAgeHealthRuleTest`, `ProductsApiIT` (every endpoint, codes, Cache-Control `public` vs `private`, ETag 304), `KindRetentionResolutionTest` (resolution order).

## Acceptance criteria

- A synthetic product ingests frames that appear in `/v1/products/{id}/frames` and as `raster-frame` change events with inline payloads.
- Retention thins, evicts and sweeps without leaving orphan rows or files.
- No client request path causes an upstream fetch.
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
2. **Commit** all changes with the message `042: Raster Products Core`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/042-CR-Raster-Products-Core.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `042: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
