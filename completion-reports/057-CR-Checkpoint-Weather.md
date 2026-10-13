# 057-CR-Checkpoint-Weather

| | |
|---|---|
| Prompt | 057-Checkpoint-Weather.md |
| Status | Complete |
| Implementation commit | 8fbfaa67a26a666a96c1d9497482a5dc4b016897 |
| Date | 2026-10-13 |

## Summary

This was the first run of the tests written in prompts 042–056, together with the 001–041 suite.
The suite is now green: `./gradlew test` passes **5682 tests, 0 failed, 0 skipped** in run 5 and
again in run 6 (`--rerun`). A full run takes about 12.5 minutes. All the sha256-pinned fixtures were
downloaded first (`./gradlew downloadTestFixtures`), so no test skipped.

Getting there took:
- **three startup fixes**. Two harness or wiring faults stopped every Spring context from starting
  (TestKit's SLF4J binding, and two beans named `timelineService`); a dependency cycle at startup
  followed;
- **9 more production fixes**, grouped by root cause under Test results;
- **one spec fix** (`ProductTemporal.required`);
- **test changes**, each checked against the code or document named with it;
- **new focused tests** for the focus areas that had no coverage.

Each item that 041–056 deferred to this checkpoint is verified or recorded under "Known items from
earlier reports".

The performance smoke (`./gradlew perfTest`) passes, with:
- NFR-2 sustained at 20,016 versions/min;
- change-to-SSE p95 of 15 ms;
- an SSE worst case of 185 MB (36 % of NFR-1's 512 MB);
- the region tagger keeping up, including a one-minute burst of 19,980 new entities.

## Files created / modified

Production:
- `raster/model/TimelineService.java`: the bean is named `productTimelineService`. 051's class had the
  same default bean name as 037's `store.timeline.TimelineService`.
- `source/adapters/nws/NwsAlertsAdapter.java`: county outlines are looked up on the first fetch, not in
  the constructor (startup dependency cycle).
- `security/AccessPolicy.java`: a `GET` rule also covers `HEAD`.
- `media/ImageGuard.java`: header-only PNGs and TIFFs report their dimensions.
  - PNG bit depth and alpha come from IHDR.
  - A file is read through a stream of unknown length.
- `raster/render/OperatorBasemap.java`: LAN basemap tiles pass `ImageGuard` (`GuardedImages`).
- `raster/render/RenderQueue.java`: warming can no longer starve interactive renders. An interactive
  request promotes a queued warming render of the same tile.
- `raster/ProductRegistry.java`: a ladder's `maxAge` is checked against its oldest rung.
- `source/spi/ProductSpec.java`: ladder summaries print hours below seven days (`48h`, not `2d`).
- `raster/render/UnitConversion.java` (new `formatReading`), `raster/sample/SampleService.java` and
  `raster/render/MapRenderer.java`: sampled values and distances keep their tenths below 100.

Spec and build:
- `docs/OpenAPI.json`:
  - `ProductTemporal.required` is `["type"]`;
  - the `cadence` and `maxGap` descriptions say when they are absent;
  - changelog entry 057.
  - Postman needs no regeneration, because no example changed.
- `build.gradle.kts`: `testImplementation(gradleTestKit())` removed.

New tests:
- `media/PolarplotInMemoryTest` (2 tests).
- `raster/FrameStoreIT`: `concurrentReadersSeeWholeFiles`, `partialToComplete`.
- `raster/render/RenderQueueTest`: `warmingDoesNotStarveInteractive`, `interactivePromotesQueuedWarming`.
- `security/AccessPolicyTest.headFollowsGet`.
- `raster/ProductsApiIT.categoricalNeverBilinear`. `noUpstreamFetch` now also covers tiles, images,
  samples, series and the timeline.
- `notify/AlertMapServiceIT.retryResendsStoredMedia`.
- `raster/ProductRegistryTest`: `loop` is reserved.
- `raster/ProductTilesIT.systemGauges`: the render queue depth.
- `perf/PerformanceSmokeIT`: region tagger report lines (not gated) and a one-minute burst of new
  entities.

Changed tests (each listed with its reason under Test results):
- Build, spec and contracts: `build/UnidataScopeBuildTest`, `openapi/PostmanCollectionContractTest`,
  `openapi/RouteContract`
- Rasters and maps: `raster/model/IdxRangePlannerTest`, `raster/render/BasemapStoreTest`,
  `raster/ProductTilesIT`, `raster/RasterRetentionStepIT`, `source/adapters/satdump/PolarOverlayIT`
- Weather adapters: `source/adapters/goes/GoesFdcAdapterTest`, `source/adapters/hms/HmsAdapterTest`,
  `source/adapters/hms/SmokePolygonIngestIT`, `source/adapters/awc/AwcMetarsAdapterTest`,
  `source/adapters/nws/NwsAlertsIngestIT`
- Weather semantics and triggers: `weather/WeatherContextServiceTest`, `weather/EnrichmentWeatherInputIT`,
  `source/adapters/weather/StaleNeverClearsIT`, `source/adapters/weather/GlmFeedStopsIT`,
  `trigger/TriggerTestDownsampleTest`
- Other: `retention/FamilyDefaultsTest`, `mcp/McpServerIT`, `security/StreamTicketIT`

## Deviations from the prompt

1. **`UnidataScopeBuildTest` runs the repository's own wrapper instead of TestKit.**
   - `gradleTestKit()` put the Gradle API jar, with Gradle's SLF4J binding, on the test runtime
     classpath. Every Spring context then failed: "LoggerFactory is not a Logback LoggerContext".
   - The test now starts `./gradlew --no-daemon --offline -p <tmp> checkUnidataScope` with a 5-minute
     limit, so no daemon outlives it.
   - Its four tests pass.
2. **Two focus-area expectations follow the design rather than the prompt's wording.**
   - *Render priority.* Design §6.3 and `RenderQueue` put newest-frame warming first, then
     interactive, then prefetch. That order is kept. What changed is that warming can no longer
     starve a waiting client (fix E).
   - *"Unversioned or unknown styles rejected (`UNKNOWN_STYLE`)".* An unknown style is 400
     `UNKNOWN_STYLE`. An unversioned style resolves to the current version and gets the short
     max-age, as design §6.1's cache table says. The tests assert exactly that, so nothing changed.
3. **The region tagger measurement is a report line in the performance smoke, not a gate.** No NFR
   states a tagging rate. The numbers are under Known items.

## Issues found / open questions

**Needs the owner:**

1. **protobuf-java resolves to 4.35.1, below the version NetCDF-Java needs.**
   - `netcdf-java-platform` 5.11.0 pins 4.36.1, but the runtime classpath carries 4.35.1 (Spring Boot's
     dependency management wins).
   - In a container probe, `NetcdfFiles.open` on a GRIB2 file failed: "Detected incompatible Protobuf
     Gencode/Runtime versions when loading Grib2Index: gencode 4.36.1, runtime 4.35.1".
   - Foresignal's own GRIB path is not affected: it reads with `Grib2RecordScanner` and writes no index,
     and the 5.42 decode below works. The NetCDF4 path (GOES) does not use protobuf.
   - **Owner:** pin protobuf-java to the NetCDF platform's version, or keep 4.35.1 and never open GRIB
     through `NetcdfFiles`?
2. **An upstream that keeps listing an already-expired record writes a version on every poll.**
   - Core-Data-Model §5 row 5 ("head tombstoned, or expired" → REAPPEAR) applies to an NWS alert whose
     `expires` and `ends` have already passed at receipt (`ttlAnchor RECEIVED`).
   - Every re-fetch of such an alert persists a REAPPEAR version. `NwsAlertsIngestIT` now asserts this
     Core behavior.
   - **Owner:** keep it, or treat an observation that is already expired when it arrives as a touch?
3. **`list_sources` (MCP) lists only 25 of the 45 sources in short detail.**
   - Its output is byte-capped and it has no filter. The notice tells the agent to ask for
     `detail=long`.
   - **Owner:** add a filter, or raise the cap?
4. **A new AWC scope or station list takes effect only once the upstream file changes.**
   - The ETag from the last fetch turns the next fetch into a 304.
   - The METAR cache changes every minute, so the delay is short.
   - **Owner:** forget validators when the parameters change?
5. **MRMS rotation decode and NFR-1.**
   - The 98 M-cell rotation field is a 392 MB float array. It decodes in the 2 GB test JVM
     (`MrmsSecondWaveDecodeTest` 5/0).
   - It would not fit NFR-1's 512 MB heap. The design's small profile has 4 GB.
   - 143 measures it (053 note).
6. **Owner decisions already listed in earlier reports are unchanged** (012–056).

**Other findings:**
- **HEAD needed admin everywhere.** `AccessPolicy` rules carry `GET`, so a `HEAD` matched none of them
  and fell through to the fail-closed admin rule: 403 for every other caller. This is fixed (fix B).
  SEC-1 is unchanged for unmatched paths.
- **Two order-dependent tests failed in run 4 only** (they passed in run 3):
  - a log-capture race;
  - a quota test that did not allow for files other tests leave in the shared data directory.

  Both are fixed.
- **Perf run 2 failed because of my own change.** The region burst ran before the subscriber counts
  were checked, so the subscribers counted its 19,980 extra changes. The burst now runs after the
  check, and perf run 3 passes.
- **Data:** no data, fixture or tool output contained instructions aimed at an agent. The only such
  text was the deliberate injection fixtures (`EnrichmentWeatherInputIT`'s injected NWS text and the
  ones 041 listed), which were treated as data.
- I ran `./gradlew --stop` once while stopping run 2. It may have stopped an idle Gradle daemon of the
  same version.

## Test results

All runs used Docker with the Testcontainers image `foresignal-db:17-3.5-pgvector`. Full runs used
`./gradlew test --continue`, after `./gradlew downloadTestFixtures` (all five pinned fixtures present).

| Run | Tests | Failed | Skipped | Notes |
|---|---|---|---|---|
| 1 | 5673 | 2139 | 0 | 1 min 27 s; no Spring context started (TestKit's SLF4J binding) |
| targeted | 4 | 1 | 0 | `UnidataScopeBuildTest` on the wrapper |
| 2 | – | about 1011 failure lines | – | stopped early: the `timelineService` bean conflict; no results written |
| targeted | 2, then 2 | 2, then 0 | 0 | `TileKeySseIT`: the NWS dependency cycle, then green |
| 3 | 5673 | 38 | 0 | 12 min 37 s |
| targeted | 221 | 23 | 0 | 34 classes after the first fixes |
| targeted | 138 | 5 | 0 | |
| targeted | 251, 35, 294 | 1, 0, 0 | 0 | |
| 4 | 5682 | 2 | 0 | 12 min 34 s; two order-dependent tests |
| 5 | 5682 | 0 | 0 | 12 min 26 s |
| perf 1 | 1 | 0 | 0 | `perfTest`, 12 min 26 s |
| perf 2 | 1 | 1 | 0 | `perfTest` with the region burst in the wrong place (see Issues), 13 min 25 s |
| perf 3 | 1 | 0 | 0 | `perfTest`, 13 min 24 s |
| 6 | 5682 | 0 | 0 | `--rerun`, 12 min 51 s |

### Production fixes, by root cause

**A. No Spring context could start** (2139 failures in run 1, about 1011 failure lines in run 2).
- *Logback.* `testImplementation(gradleTestKit())` (046) put `gradle-api-9.8.1.jar` on the test
  classpath. That jar carries Gradle's own SLF4J `LoggerContext`, so Spring Boot refused to start:
  "LoggerFactory is not a Logback LoggerContext but Logback is on the classpath". The dependency was
  removed (Deviation 1).
- *Bean names.* 051's `raster.model.TimelineService` and 037's `store.timeline.TimelineService` both
  had the default bean name `timelineService` (`ConflictingBeanDefinitionException`).
  - The 051 bean is now `productTimelineService`.
  - A scan of every `@Component`, `@Service`, `@Repository`, `@Controller` and `@Configuration` class
    found no other duplicate.
- *Cycle.* `NwsAlertsAdapter`'s constructor called `ObjectProvider<CountyGeometries>.getIfAvailable()`.
  - 047's `CountyUnionService` needs `RegionTagger`, which needs `RegionTaggingService`, which needs
    `SourceRegistry`, which holds every adapter.
  - The context therefore failed with "Requested bean is currently in creation".
  - The outlines are now resolved once, on the first fetch.

**B. `HEAD` needed admin** (`TileKeyFilterIT`; 050 says "HEAD works").
- `AccessPolicy.Rule.matches` compared methods exactly.
- A `GET` rule now covers `HEAD` (RFC 9110 §9.3.2).
- `AccessPolicyTest.headFollowsGet` checks this, and that an unmatched `HEAD` still needs admin.

**C. `ImageGuard` refused header-only images as truncated** (`ImageGuardTest` 4 failures, then 3; through
it also `AlertMapServiceIT` 5 and `NoUpstreamFetchTest` 1).
- *TIFF.* `ImageIO.createImageInputStream(File)` tells the JDK TIFF reader the file's length. The
  reader then checks strip offsets against it while reading the header: "Data segment out of stream".
  `inspect(Path)` now reads through a `MemoryCacheImageInputStream` of unknown length, which caches
  only the bytes the header reads.
- *PNG.* The reader's image type needs the metadata after IHDR, which it reads by skipping chunks up to
  IDAT: "Error skipping PNG metadata".
  - Bit depth and alpha now come from IHDR bytes 24–25.
  - Those bytes are read before the reader touches the stream, because the reader flushes what it has
    read ("pos < flushedPos").

**D. The operator basemap decoded tiles without `ImageGuard`** (`GuardedImagesOnlyDecodeEntryTest`).
055's `OperatorBasemap` called `ImageIO.read` on LAN tile bodies, so a decompression bomb on a LAN tile
server bypassed the 043 guard. It now uses `GuardedImages.decode`.

**E. Warming could starve interactive renders** (focus area; new tests).
- A frame queues up to `TileWarmer.MAX_TILES` (512) warming renders at the highest priority. On a 1–2
  thread pool at 4–8 ms a tile, that outlasts the 1.5 s wait, so clients got `RENDER_BUSY` while a frame
  was warming.
- A client asking for a tile already queued for warming joined that job at its place in the batch.
- After a warming render, a queued interactive render now goes next, so the two alternate while both
  wait. The newest frame still goes first.
- An interactive request that joins a queued warming render moves it to the interactive level.

**F. The ladder check judged `maxAge` against the last listed rung** (`ProductRegistryTest.badLadder`).
A descending ladder with `maxAge` inside its oldest rung passed that rule. It is now compared with the
oldest rung.

**G. Ladder summaries printed `2d` for 48 hours.**
- Failing tests: `ProductRegistryTest`, `MrmsAdapterWireMockTest`, `MrmsTakeEveryTest` and
  `GoesAbiAdapterWireMockTest`.
- Design §2.4 reads "every frame for 6 h, then 10-min frames to 48 h".
- Hours are now used below seven days.

**H. Readings lost their tenths** (`SampleServiceTest`, `MapRendererTest`).
- `/v1/sample` went through the legend-tick formatter, so 52.5 dBZ (MRMS's half-dBZ step) was labeled
  "53 dBZ".
- `MapRenderer.distanceLabel` printed 25 km as "16 mi", although its own documentation says "15.5 mi".
- Readings now keep one decimal below 100 (`UnitConversion.formatReading`). Legend ticks are unchanged.

**I. The spec required temporal facts that external entries do not have** (`ProductsApiIT.listProducts`
and `ProductTilesIT`'s external products test).
- 045's `ExternalCatalog` builds no temporal facts for an entry whose time dimension has no fixed
  period, so `cadence` and `maxGap` were absent while `ProductTemporal` required them.
- `required` is now `["type"]`.
- Redocly 2.62.1 (the `lintOpenApi` tool) reports the same 71 warnings as on the previous commit, and no
  error.

### Test changes and why

**Asserted the wrong thing:**
- **`WeatherContextServiceTest.budget`**
  - Old: "radar data unavailable", in lower case.
  - New: `startsWith("Radar data unavailable")`.
  - Why: the radar clause opens the summary, so it is capitalized. The same class's `noCoverage` test
    expects "Radar data has no coverage…".
- **`GoesFdcAdapterTest.descriptor`**: it asserted the order of `SourceDescriptor.kinds`, which is a
  `Set.copyOf`. It now accepts any order.
- **`TriggerTestDownsampleTest.overlapping`**
  - Old: `(1999, 5000]`, which starts at seq 2000, outside the downsampled `[1000, 2000)`.
  - New: `(1998, 5000]`.
  - Why: fromSeq is exclusive, per the class documentation and `notOverlapping`.
- **`IdxRangePlannerTest.advanceCharge`**
  - Old: `4 + 990`.
  - New: `4 + 2 + 990` (996).
  - Why: the old sum left out PRMSL's 2 bytes (8..9) in the adjacent range.
- **`BasemapStoreTest.culling`**
  - Old: an "open ocean" box at (−150, −40, −140, −35).
  - New: (−150, −58, −140, −56).
  - Why: the old box lies inside the Americas part's bounding box (−168..−35, −54..72). The cull is by
    bounding box, as the test's own "all intersecting" check says.
- **`PolarOverlayIT`**
  - Old: selected a `raster_frame.frame_key` column, which does not exist.
  - New: the query computes `{timeKey}~{sha8}` (`FrameKeys`).
- **`StaleNeverClearsIT`, `GlmFeedStopsIT`**
  - Old: the exit rules used `op: exists` without a `value`.
  - New: `value: true`.
  - Why: `RuleValidator` requires a value, and `TriggersControllerIT` and `RecordLifecycleIT` send one.
- **`EnrichmentWeatherInputIT.noContextNoBlock`**
  - Old: a magnitude 4.0 quake, which never produced an alert under `enrichRule`'s `mag >= 5`.
  - New: magnitude 5.0.
- **`ProductTilesIT.systemGauges`**
  - Old: read `/v1/system` anonymously, which returns the short document without blocks
    (`SystemInfoService.build`).
  - New: read with a read token.
- **`ProductTilesIT.stylesAndLegends`**
  - Old: 9 styles (045).
  - New: 12, because 053 added `ffg-ratio`, `qpe` and `rotation`; the second page now starts at
    `ir-gray@1`.
- **`UnidataScopeBuildTest.realBuildWiresTheCheck`**
  - Old: forbade the text `/repository/unidata-all/` anywhere in the build file, and the build's own
    comment quotes it as a warning.
  - New: forbids only a declared `maven("…/repository/unidata-all/")`.

**Test harness did not match production:**
- **`HmsAdapterTest`, `SmokePolygonIngestIT`, `AwcMetarsAdapterTest.scopes`**. The bound HTTP client keeps
  ETags in the source's persistent state, and production gives the fetch context the same store
  (`FetchContextFactory.stateFor`).
  - `HmsAdapterTest` gave the context a separate in-memory map; it now uses the persistent store.
  - The WireMock stubs answered 200 only to requests without `If-None-Match`, so a changed file was
    never served. They now behave like a server holding one version: 304 for its ETag, 200 otherwise.
  - `AwcMetarsAdapterTest.scopes` reads the same file three times, so validators are now forgotten
    between fetches (Issue 4).
- **`FamilyDefaultsTest`**
  - 049's `SatDumpAdapter` was missing from the classified adapters. A new `station` class checks its
    three §2.4 windows: ground-station 30 d, satellite-pass 365 d, raster-frame 7 d.
  - `history_downsample` (044's bookkeeping, written by `HistoryDownsampler`) joins the tables
    retention may delete from.
- **`PostmanCollectionContractTest`**
  - New validator entry: `updateWeatherSettings` (`WeatherSettingsService.parse`).
  - Schema-only, with reasons:
    - `postSample` and `postProductStats` are parsed inline against the registry and the database
      (covered by `SampleEndpointIT`, `StatsIT`);
    - `ingestSourceMedia` and `ingestSourceRasters` take multipart bodies (covered by `RemoteIngestIT`).
  - The briefing validator leaves out 056's `scope.places` and `scope.weatherProducts`, which need the
    database and the registry.
- **`RouteContract.STATUS_EXCEPTIONS`**: three operations choose their status through `ResponseEntity`
  at run time: `getProductTile` (204 with `?empty=204`), `ingestSourceMedia` and `ingestSourceRasters`
  (both 202).
- **`McpServerIT.sourceKindAndBacktestTools`**: with 45 sources, the short `list_sources` reaches its
  byte cap before `test-standard`. The test accepts the documented truncation notice (Issue 3).
- **`NwsAlertsIngestIT.refetchTouches`**: it expects the Core §5 REAPPEAR of the already-expired alert,
  plus 8 touches (Issue 2).
- **`StreamTicketIT.ticketIsNotEchoed`** (run 4)
  - Cause: it iterated `ListAppender.list` while background threads appended at DEBUG
    (`ConcurrentModificationException`).
  - Change: it copies the list under the appender's lock.
- **`RasterRetentionStepIT.quotaEviction`** (run 4)
  - Cause: the quota counts media and GLM archive bytes, and other tests leave such files in the
    shared data directory.
  - Change: the quota leaves room for exactly those bytes.

### Focus areas

All pass in runs 5 and 6. Counts are tests/failures in run 5.

**`.fsg` codec:**
- `ElgCodecTest` 14/0:
  - the golden file matches byte for byte, including the 64-byte header;
  - every dtype round-trips with both codecs;
  - bad CRC, magic, version and reserved bytes are rejected, as are truncated and overlong payloads
    and a wrong inflated length.
- `FrameStoreIT.shaStableAcrossCodecs`: the same sha8 for both codecs.
- `MrmsCodecTest` 6/0: 0 is no coverage, 1 is no echo, dBZ = `(code − 66)/2` with its clamps, checked
  against sentinel fixtures.

**FrameStore atomicity:**
- `FrameStoreIT` 9/0: staging and atomic rename.
  - New: concurrent readers of a frame rewritten 200 times only ever decode a whole version.
  - New: partial → complete keeps the path, and the sha changes only with the content.
- `RasterRetentionStepIT`: the orphan sweep, the re-emit of a frame newer than its record head, and row
  before file.
- `ProductTilesIT` and `FrameResolverTest`: 410 `FRAME_EXPIRED`.
- `RasterPipelineIT`: a reprocessed frame keeps its id.

**Tile renderer per GridDef:**
- `GridDefTest` 8/0: latlon, geos with sweep x and y, the HRRR lcc corners, affine 4326/3857, and the
  antimeridian.
- `HrrrWindRotationTest`: the 051 wind rotation.
- `TileRendererTest` 8/0: the max zoom edge, @2x, the shared transparent tile and its ETag, 204, and
  bilinear sampling. `ProductTilesIT` covers the same through the API.
- New `ProductsApiIT.categoricalNeverBilinear`: no registered product samples bilinearly through a
  categorical style.

**Cache policy:**
- `RasterCachePolicyTest` 7/0.
- `ProductTilesIT`: immutable vs short-lived URLs, private vs public, a stale sha8 → 404
  `UNKNOWN_FRAME` with `Link`, and `onExpired=transparent`.
- `TileKeyCacheHeadersIT`: the same through `/v1/k/…`.
- `TileJsonIT`: concrete templates.
- `StyleRegistryTest`: `UNKNOWN_STYLE` (see Deviation 2).

**Render queue:**
- `RenderQueueTest` 7/0: priority, fairness, single flight, 1.5 s then 503 with `Retry-After: 1`.
  New: warming does not starve, and promotion.
- `RenderQueueFairnessTest`.
- `ProductTilesIT.systemGauges`: `renderBusyLastHour` and `renderQueue.depth`.

**Stale vs no coverage:**
- The three §7.4 ITs pass, 1/0 each: `GlmStopsNoAllClearIT`, `UnchangedPlaceWeatherIT`,
  `MrmsNoCoverageIT`.
- Also `GlmFeedStopsIT` and `StaleNeverClearsIT` 2/0.
- `PlaceWeatherGeneratorTest`: values kept with a fixed `asOf`, 999 for "none within 100 km", and
  tombstones only for deleted places.
- Non-ok sections are labeled in `WeatherContextServiceTest` 13/0, `BriefingWeatherSectionIT`,
  `AskWeatherToolsIT` 5/0 and `GetMapImageToolIT` 3/0.

**SatDump:**
- `SatDumpAdapterWireMockTest` 5/0: the `/api` fixtures, and `/fft.jpeg` is never requested.
- `PolarOverlaysTest` and `PolarOverlayIT`: the `dataset.json` and `product.cbor` fixtures.
- `XritFrameTest` 8/0.
- `InboxScannerTest` 7/0: 2 stable scans, and symlink escapes.
- `PassJoinTest` 8/0: the join window and the externalId.
- `SatNameNormalizerTest`.
- `GeoTiffReaderTest` 9/0: BigTIFF and oversize images rejected before decoding.
- `FootprintBuilderTest`.
- `SwathEstimatorTest` 5/0: Orekit loads offline.
- New `PolarplotInMemoryTest` 2/0.
- **Partial coverage:** the single writer of `satellite-pass` is covered only indirectly, by
  `SatDumpIngestIT` "… in one pass record".

**Sampling:**
- `SampleServiceTest` 14/0: statuses at grid edges, MRMS code 0, nodata, stale, expired, failing and
  disabled; −PT1H and +PT6H.
- `SampleEndpointIT`: the 100-point cap.
- `SeriesIT`: the 500-step cap.
- `FrameResolverTest`: `RUN_REQUIRED`.
- `CellAreasTest` 4/0 and `StatsIT`: areas on latlon and lcc within 1 %.

**Attachments:**
- `AttachmentDowngradeTest` 9/0: the per-channel caps, 800 → 600 → thumbnail → text, and a link for
  non-image channels.
- `NtfyAttachmentTest`: the 413 fallback.
- `EmailInlineCidTest`.
- `AlertMapServiceIT`: one render for three channels and 7-day media. New: a retry resends the stored
  media's bytes.
- `MediaRetentionStepIT`.
- `GetMapImageToolIT`: ≤ 512 px and ≤ 200 KB.

**Also verify:**
- No upstream fetch on client paths: `NoUpstreamFetchTest` 4/0, `ProductsApiIT.noUpstreamFetch` (now
  also tiles, image, sample, series and timeline), and `PolarplotInMemoryTest`.
- `ProductRegistryTest`: `timeline`, and now `loop`, are reserved.
- NetCDF-Java: `UnidataScopeBuildTest` 4/0, `DecoderSelfTestTest` 6/0 and `SafeNetcdfTest` 5/0.
- `KindRetentionResolutionTest` 2/0: the per-kind retention order.
- `RasterPipelineIT` and `ForecastRunIT`: one version per frame and per completed run.
- `RemoteIngestIT` 8/0: `/v1/ingest/{id}/media|rasters` never reach an IngestFormat.

### Known items from earlier reports

- **041, region tagger at NFR-2 (perf 3):**
  - During the 12-minute load (5,000 stationary entities, tagged once each), the tagger's lag behind
    the change bus was 0 seqs at the end of the 1× and 2× phases.
  - In the one-minute burst of 19,980 new, distinct entities (NFR-2's rate):
    - the lag was 0 seqs when the burst ended;
    - `record_region` gained exactly 19,980 rows;
    - no "lagging" warning was logged.
  - 041's lag came from `RetentionJobIT`'s unbounded producer, not from NFR-2.
- **041, SSE memory budget (re-measured in perf 3):**
  - the FIFO high-water mark was 223 events (`clientBuffer` 1000);
  - one queued event takes 1721 B;
  - the high-water mark per emitter was 596 KB;
  - the worst case is 1901 KB per emitter, × 100 clients = 185 MB, 36 % of NFR-1's 512 MB;
  - the post-GC live heap high-water mark was 177 MB for the whole JVM.
- **044:** `HistoryDownsamplerIT` passes 8/0 on PostgreSQL 17.5.
  - The keep-set statement runs as written.
  - Its "four linked indexes" check counts the copy's indexes in `pg_inherits`, so they are attached,
    not rebuilt.
- **045, legend text without system fonts:**
  - The runtime base image is `eclipse-temurin:21-jre`, digest `sha256:cff19e62…`. In it, `fc-list`
    lists 8 fonts, and a headless Java2D `drawString` in SansSerif 10 pt inked 191 pixels.
  - A host with no fonts at all was not tested; that is recorded for 143.
- **046, libaec in a container:**
  - Setup: `eclipse-temurin:21-jre` as uid 1001, a fresh named volume at `/data`, and the image's flags
    (`-Djava.io.tmpdir=/data/tmp -Djna.tmpdir=/data/tmp`).
  - Result: Foresignal's `Grib2Decoder` (the build classes plus the runtime classpath) decoded the 5.42
    ECMWF selftest: `template=42 cells=1038240 max=1.0`.
  - The Foresignal image itself was not built; 143 confirms it there.
- **043:** the JDK readers report dimensions for the crafted header-only fixtures from memory streams:
  TIFF 10 × 12,001, JPEG 12,001 × 16, and the 2-directory TIFF 20 × 20. Read from files, the TIFF ones
  needed fix C.
- **053, rotation decode:** fits the 2 GB test JVM (`MrmsSecondWaveDecodeTest` 5/0, `rotation`
  included). NFR-1 is Issue 5.
- **051–053, Spring wiring:** every new component starts in every context (runs 5 and 6).
  `forecast_series_cache` is created by Hibernate and used by `NwsForecastIngestIT` 3/0 and
  `PlaceWeatherSecondWaveIT` 1/0.
- **052:** `PlaceGridIndexBenchmarkTest` 2/0 and `TileRenderBenchmarkTest` 1/0 pass on this machine.
- **055:** the map goldens match within their 1 % tolerance on this Linux host (`MapRendererTest` 10/0).

### Invariants check (owner ≤ 056)

Counts are tests/failures in run 5. Every named test class exists; none had to be added.

| Invariant | Tests |
|---|---|
| ING-1, ING-2, ING-5 | `IngestWriterIT` 24/0 |
| ING-3 | `IngestClockGuardTest` 3/0, `IngestWriterIT` 24/0 |
| ING-4 | `TimeContractTest` 2/0 |
| ING-6 | `NoJpaWritesToRecordVersionTest` 3/0 |
| ING-7 | `ContentHasherTest` 24/0 |
| ING-8 | `IngestPolicyEngineTest` 67/0, `IngestPolicyValidatorTest` 24/0 |
| HIST-1 | `PartitionManagerIT` 20/0 |
| HIST-2 | `CurrentReadsUseHeadLiveIT` 3/0 |
| HIST-3 | `AsOfQueryIT` 11/0 |
| RET-1 | `RetentionJobIT` 11/0 |
| RET-2 | `ConsumerClampIT` 2/0 |
| RET-3 | `RetentionResolverTest` 14/0, `FamilyDefaultsTest` 2/0 |
| RET-4 | `PurgeSeqIT` 2/0 |
| SRC-1 | `UpstreamClientIsTheOnlyHttpClientTest` 2/0, `UpstreamClientIT` 52/0 |
| SRC-2 | `BudgetGovernorTest` 22/0, `WebSocketBudgetIT` 11/0 |
| SRC-3 | `BackoffRegistryTest` 32/0, `BackoffRegistryIT` 11/0 |
| SRC-4 | `UsageMeterIT` 31/0 |
| SRC-5 | `UrlRedactorTest` 17/0, `RequestRecorderTest` 6/0, `FetchTraceTest` 7/0 |
| SRC-6 | `CollectorSchedulerIT` 9/0, `NoClientTriggeredFetchIT` 1/0, `NoUpstreamFetchTest` 4/0, `WeatherContextServiceTest` 13/0 |
| SRC-7 | `ParamValidatorTest` 71/0, `PinnedTargetTest` 4/0, `LocalAddressesTest` 26/0 |
| SEC-1 | `AccessPolicyTest` 210/0 |
| SEC-2 | `AdminTokenServiceIT` 14/0, `TokenHasherTest` 11/0 |
| SEC-3 | `CredentialCipherTest` 15/0, `SecretKeyStoreTest` 9/0 |
| SEC-4 | `ScopeTest` 25/0, `TokenFilterIT` 52/0, `AccessPolicySyncTest` 3/0, `RestrictionLeakMatrixIT` 28/0, `AskRestrictionIT` 1/0, `RestrictedReadCoverageTest` 2/0, `RestrictionClampTest` 11/0, `PathKeyFilterIT` 7/0, `TileKeyFilterIT` 7/0, `TileKeyRedactionTest` 5/0, `McpServerIT` 20/0, `McpToolRegistryTest` 13/0, `AgentWorkflowIT` 3/0, `PlacesIT` 5/0, `McpPlaceToolsIT` 4/0, `WatchlistRuleIT` 4/0, `SavedViewsIT` 6/0 |
| SEC-5 | `AlwaysAuthenticatedIT` 3/0, `McpServerIT` 20/0, `AskServiceIT` 14/0, `BriefingsControllerIT` 8/0 |
| EGR-1 | `PrivateEgressIT` 35/0, `TileKeyFilterIT` 7/0, `EgressPolicyTest` 10/0, `EmbeddingWorkerIT` 11/0, `AskServiceIT` 14/0, `LlmBriefingIT` 7/0, `EnrichmentContextIT` 8/0, `AskWeatherToolsIT` 5/0 |
| EGR-3 | `AskActionsIT` 9/0, `PromptInjectionIT` 1/0, `EnrichmentPromptInjectionIT` 1/0, `EnrichmentWeatherInputIT` 2/0 |
| SSE-1 | `SseReplayLiveBoundaryIT` 1/0 |
| TRG-1 | `TriggerPersistGateTest` 159/0, `RuleSqlBuilderIT` 72/0, `RecordPredicateTruthTableTest` 167/0, `PlacesIT` 5/0 |
| TRG-2 | `GeofenceHysteresisIT` 1/0 |
| EXT-1 | `EdpIngestIT` 8/0, `EdpManifestServiceTest` 13/0 |
| EXT-4 | `EdpTimeTest` 6/0 |
| EXT-5 | `EdpSnapshotIT` 8/0 |
| EXT-6 | `EdpUrlGuardTest` 25/0, `EdpSchemaSafetyTest` 9/0 |
| PRC-1 | `OpenApiContractIT` 6/0, `PostmanCollectionContractTest` 6/0 |
| PRC-2 | `LicenseAllowlistTest` 3/0 |

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`. No new dependencies; `gradleTestKit()` was
removed.

Guards:
- The suite's own guards pass in runs 5 and 6: `UpstreamClientIsTheOnlyHttpClientTest`,
  `GuardedImagesOnlyDecodeEntryTest`, `NoJpaWritesToRecordVersionTest`, `FamilyDefaultsTest`,
  `LicenseAllowlistTest`, `OpenApiContractIT`, `PostmanCollectionContractTest` and `AccessPolicySyncTest`.
- `python3 ~/Documents/GitLab/meshdtn/style/test_us_english.py` passes (14 tests). It scans the MeshDTN
  repositories, not this one.
- Its checker, run over this commit's added lines and new file, reports 0 British spellings.

## Notes for the next prompt

1. **The suite takes about 12.5 minutes, and `./gradlew downloadTestFixtures` must run first.** Without
   it, the GOES, HRRR and QPE 24 h cases skip.
2. **Test JVM:** 2 GB is enough for every decode, including the 392 MB rotation field.
3. **Adapter tests that use the bound HTTP client:**
   - read and write the source's persistent adapter state (`UpstreamClient.stateStore`), not a
     separate map;
   - make WireMock stubs answer like a real server (304 only for the current ETag).
4. **`HEAD` now follows `GET` in `AccessPolicy`.** A new rule written for `GET` also covers `HEAD`.
5. **143:**
   - build the image, and confirm there libaec, fonts and the protobuf question (Issue 1);
   - measure the rotation decode under the small profile.
6. **Owner decisions pending:** Issues 1–5 above, plus those carried from 012–056.
