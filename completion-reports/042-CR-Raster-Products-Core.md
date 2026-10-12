# 042-CR-Raster-Products-Core

| | |
|---|---|
| Prompt | 042-Raster-Products-Core.md |
| Status | Complete |
| Implementation commit | 35350efb90e2e81a3322f96eea3365399ca7b06c |
| Date | 2026-10-12 |

## Summary

The raster product core is in place. It includes:
- the product catalog;
- the native-grid frame store with its `.fsg` codec;
- the raster pipeline behind `ctx.rasters()`;
- frame resolution;
- frame-age health;
- frame retention (ladders, quota, orphan sweep);
- the memory profiles;
- the `rasters` block of `/v1/system`;
- the four read operations.

Each complete observation frame, and each completed forecast run, becomes one `raster-frame` record
version. SSE, resume, MCP and triggers therefore need no new distribution code. A test-only
`SyntheticRasterAdapter` proves the pipeline end to end. No real decoder ships (046 adds them).

The four 042 operations are `implemented` and off `pending-operations.txt`.

**1. Hooks from earlier prompts.** All of them already existed, so none were added:
- 004: `SourceDescriptor.products`, `kindRetention`, `Mode.DERIVED`, `ParamType.LAN_URL`, `INBOX_PATH`.
- 005: `SourceHttp.getRange`, `download`, `listS3`, `BudgetGovernor` `mbPerDay` with `correctByteCharge`,
  and the per-descriptor `maxResponseBytes`. `download` already writes to `rasters/tmp`.
- 006: the `HealthRule` SPI, `FrameAgeRule`, and `newestFrameAt` / `frameAgeSeconds` on the health view.
- 014/015: the repeatable `externalId` filter on both change operations (spec and code), and
  `inlinePayloadKinds` defaulting to `raster-frame`.
- 019: `RetentionSettings.kinds`, `RetentionResolver.resolve` in the prompt's order, and the
  `RetentionStep` extension point.

Two seams were added:
- `FetchContext.rasters()`: a default method that throws. `StreamContext` inherits it.
- `collect.RasterSinkFactory`: lets `FetchContextFactory` hand out sinks without depending on `raster`.

**2. ProductSpec and ProductRegistry.**
- `ProductSpec` (still in `source.spi`) now carries every field the prompt lists:
  - `kind` now means the product kind, and the Javadoc says so;
  - category, units, `RasterDtype` dtype, scale, offset, nodata, codes;
  - bands, styles, default style, default opacity;
  - `Temporal`, `Spatial` (optional `bounds` and `maxZoomForAnimation`);
  - a `RetentionLadder` (rungs, `maxAge`, `runs`) with a one-line summary;
  - a structured `Attribution`;
  - a builder, and the old 3-argument constructor.
- `ProductRegistry` (`ai.foresignal.raster`):
  - Collects every descriptor's specs at startup. Any problem stops startup, all problems in one
    message.
  - Checks:
    - the id pattern, reserved ids (`timeline`, `loop`) and uniqueness across sources;
    - that the owner lists `raster-frame`;
    - kind and category;
    - dtype, temporal and spatial facts, and `maxGap ≥ cadence`;
    - ascending rungs with non-shrinking `keepEvery`, and `maxAge` beyond the last rung;
    - zooms, the default style among the styles, and the attribution's text and licence.
  - Mirrors the catalog into `raster_product` on `ApplicationReadyEvent`
    (`StartupOrder.RASTER_PRODUCTS = 27`, before the scheduler).
  - Supports `registerDynamic` and `removeDynamic` (`dynamic=true`).
  - Publishes `ProductCatalogChangedEvent`.
- A source that declares products and lists `raster-frame` gets these builder defaults:
  - `zeroResultsNormal=true`, unless it sets it explicitly;
  - `expiresAt` as a volatile field;
  - `kindRetention{raster-frame: 7d}` for STANDARD sources.

**3. Entities.**
- `RasterProductEntity`, `RasterGridEntity` and `RasterFrameEntity` (with `RasterFrameId`) follow §2.3.
- `raster_frame` also has a `run_complete` column (Deviation 3).
- Hibernate creates the `(product_id, reference_time, forecast_step)` index.
- `SpatialIndexInitializer` creates `(product_id, valid_time DESC)` and GiST(`footprint`). Its pin test
  is updated.

**4. `.fsg` codec and stores.**
- `ElgCodec` writes and reads the 64-byte little-endian header and the band-sequential payload.
  `Deflater(1)` compresses codec 1.
- Reading rejects, with `RasterFormatException`:
  - a bad magic, a bad version, non-zero reserved words or unknown flag bits;
  - a bad payload length or a CRC32C (`java.util.zip.CRC32C`) mismatch;
  - a payload that inflates to the wrong length.
- `FrameStore`:
  - stages the file in `rasters/tmp/`, forces it to disk, then atomically renames it to
    `rasters/frames/{productId}/{yyyy}/{mm}/{dd}/{frameId}.fsg`;
  - hashes the decoded content with SHA-256, so `sha8` is the same whatever the codec;
  - reads through a byte-weighted Caffeine `frameCache`.
- `RasterPaths` creates the §2.4 layout at startup: `rasters/{tmp,frames,fields,glm,satdump}`,
  `media`, `cache/netcdf` and `tmp`.
- `GridStore` dedupes grids by the SHA-256 of the canonical definition JSON
  (`INSERT … ON CONFLICT DO NOTHING`).

**5. Pipeline.**
- `RasterPipeline` implements `RasterSinkFactory`.
- `commit(FrameInput)`:
  - runs the decode (temp file plus `RasterDecoder`, or decoded bands) and the quantization on
    `RasterDecodePool`, a bounded platform-thread pool behind a fair semaphore with 1 permit (small)
    or 2 (large);
  - `FrameEncoder` then quantizes per spec and computes the stats (`min`, `max`, `mean`, `coveragePct`,
    `codeCounts`) and the footprint (the grid box clipped to rows with valid data, as a MultiPolygon);
  - writes the file, upserts the row, publishes `RasterFrameCommittedEvent`, and returns the
    `raster-frame` record.
- `RasterFrameRecords` builds the records:
  - observation: `observedAt = eventTime = valid_time`, payload
    `{productId, frameKey, time, timeKey, tiles, bounds, stats, expiresAt}`, kept under 1 KB;
  - completed forecast run: `observedAt = reference_time`, payload
    `{productId, run, steps[], validTimes[], tilesTemplate}`;
  - the optional `{productId}:partial` record, tombstoned by `completeRun`.
- `ProductUrls` builds the tile, `@2x`, image and run templates for the 045 routes.
- During a probe the sink is `probing()`. It writes nothing and returns a grid-summary preview:
  `nx`, `ny`, grid type, valid time and variable.

**6. Frame resolution and the API.**
- `FrameResolver` handles:
  - `latest` and `now` (within `maxGap`);
  - `run=latest|previous|<runIso>`, bare `timeKey`, and `frameKey` with its sha8 check;
  - a stale sha8: `404 UNKNOWN_FRAME` with a `Link` header and a `currentFrameKey` property;
  - an ISO-extended time: a `Redirect` to the canonical time key (the 045 routes answer 301);
  - `includePartial`, `400 RUN_REQUIRED` and `410 FRAME_EXPIRED`.
- `ProductsController` serves `listProducts`, `getProduct`, `listProductFrames` and
  `getLatestProductFrame`. `ProductCatalogService` builds every §6.2 field.
- Caching:
  - the catalog and a product carry an ETag and answer 304;
  - `frames/latest` gets `Cache-Control: {public|private}, max-age=min(cadence/2, 60s)`, an ETag and
    `Foresignal-Frame-Time`;
  - `public` only when `ReadCaller.publicCache()` holds and the product's source is not private.
- Products of sources the caller may not read answer `404 UNKNOWN_PRODUCT`.
- `ErrorCodes` gains `UNKNOWN_PRODUCT`, `UNKNOWN_FRAME`, `FRAME_EXPIRED`, `RUN_REQUIRED` and
  `PRODUCT_UNAVAILABLE`.

**7. Health and retention.**
- `FrameAgeHealthRule` (HealthRule SPI):
  - degraded beyond `maxGap`, or `3 × cadence` when no gap is declared;
  - reports `newestFrameAt`;
  - a recorded silent format change degrades the source with the decoder's reason.
- `SilentFormatChangeException` extends `UpstreamFormatException`. The pipeline records it in
  `ProductStatusTracker`, writes no frame, and rethrows it. The next successful commit clears it.
- `RasterRetentionStep` is a `RetentionStep` at order 800, after every record and operational step,
  and is not `@Transactional`. It does:
  - ladder thinning: the frame nearest each `keepEvery` boundary survives, is marked `thinned`, and
    gets its `expires_at` recomputed;
  - forecast run retention: the current and previous complete run;
  - quota eviction (`foresignal.raster.disk-quota-gb`, default 200, env
    `FORESIGNAL_RASTER_DISK_QUOTA_GB`), oldest first;
  - an hourly orphan sweep inside the hourly job. It removes rows with no file and files with no row,
    after a 10-minute grace. It re-emits the record of any complete frame or completed run newer than
    its record head, as a `MANUAL` fetch-log batch with principal `system:raster-sweeper`.
  - It always deletes the row and then unlinks the file.

**8. Profiles and `/v1/system`.**
- `FORESIGNAL_PROFILE=small|large` (`foresignal.profile`) selects the `RasterProfile` defaults:
  - frame, tile and index-map cache of 768/128/64 MB or 1536/256/128 MB;
  - a render pool of `min(cores−2, 6)` or `cores−2`;
  - decode concurrency of 1 or 2.
- Each value can be overridden on its own (`FORESIGNAL_RASTER_*`); nothing is derived from the heap.
- `docker-compose.yml` passes these, all overridable from `.env`:
  - `FORESIGNAL_PROFILE`;
  - `JAVA_TOOL_OPTIONS` (default `-Xmx4g -XX:+UseG1GC`, set via `FORESIGNAL_JAVA_TOOL_OPTIONS`);
  - `mem_limit` (default `6g`, via `FORESIGNAL_MEM_LIMIT`);
  - the large values, documented in comments.
- `RasterSystemBlock` contributes `rasters`:
  - profile, products, frames, storeBytes, diskQuotaGb, diskQuotaPct;
  - per-cache size, bound, hit rate and evictions per minute;
  - renderQueue (zeros plus the pool size until 045), renderBusyLastHour, and mbDownloadedToday by
    product sources.
- It also contributes the `RASTER_DISK_QUOTA` warning at ≥ 90 %, which degrades `/v1/system`. It does
  this through a new `SystemWarningContributor` seam.

**9. Spec, Postman, docs.**
- OpenAPI:
  - the four operations are `implemented`;
  - `ProductTemporal.retention.maxAge` is added;
  - `RASTER_DISK_QUOTA` is listed among the SystemInfo warning codes;
  - six codes are added to the problem-code registry;
  - there is a changelog entry.
- `AccessPolicy`: `GET /v1/products`, `/v1/products/**`, `/v1/styles` and `/v1/styles/**` are READ,
  placed before the admin default.
- `docs/Architecture.md`:
  - §3: `raster`, `raster.render`, `raster.sample`, `place`, `derive`, and `media` (see Issue 3);
  - §7: the three tables;
  - §11: `retention.kinds`, per-kind resolution, the ladders, quota and sweep;
  - §13: `FORESIGNAL_PROFILE`, `FORESIGNAL_RASTER_DISK_QUOTA_GB` and the per-value overrides;
  - §14: both profiles, including that Ollama plus Postgres plus the small profile is the limit of a
    32 GB host.

## Files created / modified

Production, created:
- `source/spi/`: `RasterDtype`, `GridDefinition`, `DecodedBands`, `FrameInput`, `RasterDecoder`,
  `RasterSink`, `SilentFormatChangeException`.
- `collect/`: `RasterSinkFactory`, `FetchContextDefaults`.
- `raster/`:
  - catalog: `package-info`, `RasterProfile`, `RasterProperties`, `RasterSettings`, `ProductRegistry`,
    `ProductCatalogChangedEvent`, `ProductSpecJson`, `ProductStatusTracker`;
  - entities: `RasterProductEntity`/`Repository`, `RasterGridEntity`/`Repository`, `RasterFrameEntity`,
    `RasterFrameId`;
  - storage: `RasterFormatException`, `FsgHeader`, `ElgCodec`, `RasterPaths`, `FrameStore`,
    `RasterCacheRegistry`, `GridStore`, `FrameKeys`;
  - pipeline: `EncodedFrame`, `FrameEncoder`, `RasterDecodePool`, `RasterFrameRow`,
    `RasterFrameQueries`, `ProductUrls`, `RasterFrameRecords`, `RasterFrameCommittedEvent`,
    `RasterPipeline`;
  - serving, health and retention: `FrameResolver`, `FrameThinning`, `FrameAgeHealthRule`,
    `RasterRetentionStep`, `RasterSystemBlock`, `ProductCatalogService`.
- `api/ProductsController`, `system/SystemWarningContributor`.

Production, modified:
- `source/spi/`: `ProductSpec` (rewritten as specified), `FetchContext` (`rasters()`),
  `SourceDescriptor` (product defaults).
- `collect/`: `DefaultFetchContext`, `FetchContextFactory`.
- `config/`: `SpatialIndexInitializer`, `StartupOrder`.
- `security/AccessPolicy`, `system/SystemWarnings` (contributors), `api/error/ErrorCodes`.
- `application.yml` (`foresignal.profile`, `foresignal.raster.*`), `docker-compose.yml`.
- `build.gradle.kts` and `gradle/libs.versions.toml` (Caffeine), `THIRD_PARTY_NOTICES.md`.
- `docs/OpenAPI.json`, `docs/Architecture.md`, `pending-operations.txt`.

Tests: listed under Tests written. Also modified:
- `SpatialIndexInitializerTest` (pin);
- `SourceDescriptorBuilderTest` (product kind `raster`);
- `NoClientTriggeredFetchIT` (product paths added).

## Deviations from the prompt

1. **The adapter-facing raster types live in `ai.foresignal.source.spi`, not `raster`.** These are
   `RasterSink`, `FrameInput`, `GridDefinition`, `DecodedBands`, `RasterDecoder`, `RasterDtype` and
   `SilentFormatChangeException`.
   - The reason is Architecture §3's rule: adapters depend only on SPI types.
   - The implementations are in `raster`. `ProductSpec` stays in `source.spi`, as task 1 says.
2. **What `commit` returns.**
   - A complete observation frame returns its record.
   - A partial observation frame returns `null`, because a partial frame never makes a version.
   - A forecast step returns the optional `{productId}:partial` run-in-progress record.
   - The completed run's version comes from a separate `RasterSink.completeRun(productId, run)`. It
     returns the run record plus the `:partial` tombstone. An adapter knows when a run is complete;
     the pipeline does not.
3. **`raster_frame.run_complete` (boolean) is added.** It is not in the design's column list.
   - Without it nothing persists which runs are complete, and four things need to know:
     - `latest` and `run=latest|previous`;
     - the "current plus previous complete run" retention;
     - the product's `temporal.runs[].status`;
     - the sweep's re-emit of a missing run record.
4. **`ProductSpec.RetentionLadder.maxAge`, and `maxAge` in the spec's `ProductTemporal.retention`
   (additive, changelog).**
   - Rungs of `{olderThan, keepEvery}` cannot express "10-min frames **to 48 h**", "to 72 h", or
     SatDump geostationary's flat 72 h.
5. **Record payload URLs.**
   - `raster-frame` payloads are built with no request, so they use `server.publicBaseUrl` when it is
     set and are otherwise root-relative (`/v1/products/…`).
   - API responses are always absolute, using the request's origin when `publicBaseUrl` is unset.
6. **Frame age of forecast products** uses the newest `ingested_at`. Their valid times lie in the
   future, so a valid-time age would always look fresh.
7. **When a missing frame is `410 FRAME_EXPIRED` rather than `404 UNKNOWN_FRAME`.** No tombstones are
   kept, so 410 applies when:
   - the time is older than the oldest retained frame; or
   - the time falls in the thinned part of the ladder.
8. **Builder product defaults apply only when the source also lists `raster-frame`.** A descriptor
   that does not cannot pass `ProductRegistry` anyway. Two existing descriptor tests declare products
   on non-raster sources and keep their exact field lists.
9. **The orphan sweep runs inside `RasterRetentionStep`** (the hourly `RetentionJob`), not a separate
   scheduler. A 10-minute `orphanGrace` (`foresignal.raster.orphan-grace`) protects commits in flight.
10. **Footprint row edges are linear in latitude.** That is exact for `latlon` grids. Projected grids
    get an approximation until 045 adds the projection maths. A grid crossing the antimeridian gets a
    two-part MultiPolygon, and its `raster_grid.bbox` is the full longitude range.
11. **Cache headers on the non-alias lists.** The catalog, a product and `listProductFrames` use the
    read API's `{public|private}, no-cache`. Only the `latest` alias gets
    `max-age=min(cadence/2, 60s)`.
12. **`ProductStatusTracker` is in memory.** It holds format-change reasons and disabled products, so
    both are re-detected after a restart. `PRODUCT_EXISTS` (409, a dynamic id owned elsewhere) is
    added to the problem-code registry.
13. **Dependency: Caffeine 3.2.4**, the version the Spring Boot 4.1.1 BOM manages, checked on Maven
    Central. It is Apache-2.0, with `jspecify` and `error_prone_annotations` (both Apache-2.0).
    `THIRD_PARTY_NOTICES.md` records it.
14. **The probe.** The pipeline gives a probe a non-writing sink that returns a header-level grid
    summary. Listing the newest object and range-reading a header is each real adapter's job (046).

## Issues found / open questions

**Needs the owner:**

1. **Forecast run payloads can exceed 1 KB.**
   - The spec's forecast variant carries `steps[]` and `validTimes[]`.
   - A GFS run (0–384 h every 3 h, 129 steps) would make more than 3 KB: 129 ISO times of about 24
     bytes each (an estimate; not measured). The synthetic test's 4-step run is asserted under 1 KB.
   - Change events still carry them inline, because the inline rule is by kind, not by size.
   - **Owner:** accept that, or drop `validTimes` (derivable from `run + steps`)?
2. **`raster-frame` 7d vs the analytic retention classes.** Conventions has no class for frame
   records. When 046 adds real product adapters, `FamilyDefaultsTest` will need one, for example
   `system`-like, or a `raster` class. **Owner:** which?
3. **Package name collision: `media`.**
   - Architecture §3 already listed `media` for news (133).
   - The Weather design puts `MediaStore`/`ImageGuard`/`ThumbnailService` (043) there too.
   - I merged both into one §3 line. **Owner:** keep one package, or rename one (for example
     `news` for 133)?
4. **The Dockerfile's `ENTRYPOINT` still passes `-XX:MaxRAMPercentage=75`.** The explicit `-Xmx`
   from `JAVA_TOOL_OPTIONS` overrides it for the heap. I left the Dockerfile alone. **Owner:** drop
   the flag?
5. **Docs vs code.**
   - The Weather design §2.3 says GiST indexes come via `SchemaBootstrap`. The code follows the
     Conventions/Architecture pattern: `SpatialIndexInitializer`, SchemaBootstrap's last step.
   - The design's `raster_frame` column list lacks `run_complete` (Deviation 3) and the ladder lacks
     `maxAge` (Deviation 4). **Owner:** should the design doc be updated?

**Other findings:**
- No instructions directed at an agent appeared in any data, file or tool output.

## Tests written (not run)

Every 042 integration test is a `@WebIntegrationTest` that imports `RasterTestSources`
(`CollectorTestSources` plus `SyntheticRasterAdapter`), so together they add **one** cached context.
The unit tests need no context.

| Test | What it covers |
|---|---|
| `raster/ElgCodecTest` | Golden file `src/test/resources/raster/golden-u8.fsg`, written independently with Python `struct` and a bitwise CRC32C checked against `123456789` (byte-exact header, every field); round trip per dtype × codec; deflate; CRC mismatch; bad magic; non-zero reserved bytes at 20 and 60; unknown version, dtype and flags; partial flag; truncated or overlong payload; wrong inflated length |
| `raster/FrameStoreIT` (no Spring) | §2.4 layout; path shape and read-back; atomic write (failed rename leaves no file and empty staging); sha256 stable across codecs; cache weigher accounting, bound and eviction; rewrite invalidates; path escape refused |
| `raster/ProductRegistryTest` | Declared products and the mirror; `UNKNOWN_PRODUCT`; duplicate id; missing `raster-frame`; bad ladder; `maxGap`, id pattern, reserved id, missing facts; dynamic REGISTERED, UPDATED and REMOVED events, conflict and validation; product-source defaults; ladder summaries |
| `raster/FrameResolverTest` | `latest` with and without partials; `now` within and beyond `maxGap`; time key and frame key immutability; stale sha8 → `UNKNOWN_FRAME` with `Link`; ISO → redirect; partial exclusion; `FRAME_EXPIRED`; `RUN_REQUIRED`; `run=latest`, `previous` and pinned; empty product; key shapes |
| `raster/FrameEncoderTest` | u8 codes and stats, footprint clipping; i16 scale, offset, clamp and NaN; f32; rgba8; antimeridian and empty footprints; refusals; partial flag |
| `raster/FrameThinningTest` | Initial expiry; the full MRMS ladder over 50 h of 2-min frames; forecast runs (two complete kept, a newer run in progress kept, older and abandoned removed); ties; no ladder |
| `raster/RasterFrameRecordsTest` | Observation record shape; the 1 KB budget drops `codeCounts` first; forecast run, `:partial` and its tombstone |
| `raster/RasterSettingsTest` | Small and large defaults, per-value overrides, profile parsing, quota percent |
| `raster/FrameAgeHealthRuleTest` | Fresh, stale (names the product, newest across products), the 3 × cadence rule, format change and clearing, no frames |
| `raster/KindRetentionResolutionTest` | `retention.kinds` → `sources` → `kindRetention` → `defaultRetention` → `default` |
| `raster/RasterPipelineIT` | Frame row, file, grid, stats, footprint and one version per frame with a payload under 1 KB; partial frame stored with no version; one version per completed run, `:partial` tombstoned, `run_complete`, forecast frame ids; SSE `kind=raster-frame&externalId=` delivers the event inline and filters the forecast out; `RasterFrameCommittedEvent`; reprocessing; silent format change; refusals; probe writes nothing |
| `raster/RasterRetentionStepIT` | Ladder thinning keeps the boundary frames and marks them with the 48 h expiry; `maxAge` and forecast runs; row deleted before file (read-only directory); quota eviction; orphan sweep (both kinds, grace); re-emit as `MANUAL` with `system:raster-sweeper` |
| `raster/ProductsApiIT` | Every endpoint, filters, compact, cursors, schema validation (`ProductPage`, `Product`, `RasterFramePage`, `RasterFrame`, `SystemRasters`); ETag 304; `UNKNOWN_PRODUCT`, `UNKNOWN_FRAME`, `PRODUCT_UNAVAILABLE`, 400s; `Cache-Control` `public` (openReads) vs `private` (token, openReads off) and 401; forecast run and step; no request causes a fetch; `/v1/system` `rasters` |
| `collect/NoClientTriggeredFetchIT` (paths added) | SRC-6 over the product paths |

## Compile result

- `./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`.
- `./gradlew lintOpenApi` → valid, 71 warnings (unchanged).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman` → 829 requests, covers all
  operations. The collection is unchanged, because the generator does not emit the status
  extension.

## Notes for the next prompt

1. **043 (media):**
   - add `mediaBytes` to `RasterSystemBlock` and count it against the quota (`RasterSettings`);
   - `RasterPaths` already creates `media/`.
2. **045 (tiles, styles):**
   - register the tile and index-map caches with `RasterCacheRegistry`;
   - fill `renderQueue` and `renderBusyLastHour` in `RasterSystemBlock`;
   - resolve frame tokens with `FrameResolver`: answer `Redirect` with a 301, and pass
     `currentKeyLink` so a stale sha8 gets a real `Link`;
   - build URLs with `ProductUrls`;
   - warm tiles from `RasterFrameCommittedEvent`, and drop caches on `ProductCatalogChangedEvent`;
   - `GET /v1/styles/**` is already READ in `AccessPolicy`.
3. **046 (decoders):**
   - decoders implement `RasterDecoder` and throw `SilentFormatChangeException`;
   - the self-test calls `ProductStatusTracker.disable`;
   - real product adapters need `FamilyDefaultsTest` entries (Issue 2);
   - adapters commit through `ctx.rasters()` and return the records in their `FetchResult`.
4. **049 (SatDump):** use `ProductRegistry.registerDynamic` and `removeDynamic`.
5. **052:** `POST /v1/products/{id}/stats` is READ in the design. The policy still sends it to the
   admin default: add the rule with the operation.
6. **057 checkpoint:** run the 042 tests. They add one `@WebIntegrationTest` context
   (`RasterTestSources`), and the test data directory is shared, so the ITs clean their product
   directories.
7. **144 (Flyway):** baseline the following.
   - Tables: `raster_product`, `raster_grid`, `raster_frame`.
   - Indexes: `raster_frame_product_run_idx`, `raster_frame_product_valid_idx`,
     `raster_frame_footprint_gist`.
8. **Owner decisions pending:** Issues 1–5 above, plus those carried from 012–041.
