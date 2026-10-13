# 048-CR-Lightning-And-Fire

| | |
|---|---|
| Prompt | 048-Lightning-And-Fire.md |
| Status | Complete |
| Implementation commit | 1430c7f3b3965e1f1d9cdd6423024cf26ec0ee5d |
| Date | 2026-10-12 |

## Summary

Lightning and fire inputs now exist:
- GOES-19 GLM flashes go to a compact daily archive and an in-memory places-AOI ring. From the ring,
  `goes-glm` derives `lightning-cluster` records.
- `goes-fdc` turns GOES-19 ABI fire detections into `fire-detection` and `fire-cluster` records.
- `hms-smoke` and `hms-fire` bring the NOAA HMS analyst smoke polygons and fire points.

Records are tombstoned only on input that was actually processed. A stalled feed freezes its records
(no tombstones, no exit alerts) and shows the source `degraded`. All seven tasks are done. No operations
were added.

**1. Hooks.** Checked against the code.
- Present and reused:
  - `SafeNetcdf` and `NetcdfDecoder` (enhanced mode) from 046;
  - `GeosGrid` from 045;
  - `PlacesAoiService` (`aoi()`, `envelope()`, `PlacesAoiChangedEvent`, default AOI) from 036;
  - `near`, `cooldownScope` and per-kind retention;
  - `RasterRetentionStep`;
  - `FrameAgeRule`.
- Added:
  - **`NetcdfDecoder.Reader`** (`open(file, sourceId)`): named variables, scalars, gather-by-index, global
    attributes, and `fixedGrid(var)`. The fixed grid gives pixel centers in file order through
    `GeosGrid`. A missing variable is a `SilentFormatChangeException` ("variable flash_lat missing").
  - **SPI `FlashSink` and `FlashBatch`**, reached through `RasterSink.flashes()`. `RasterPipeline`'s sink
    returns the `FlashStore`; its probe sink returns a view that never writes.
  - **`collect.InputAgeHealthRule`** with `AdapterStateStore.recordInput(newestInputAt, maxGap)`. This is
    the frame-age rule applied to the newest processed file: GLM 2 minutes, FDC 15 minutes. The
    reason reads "newest input file is …s old (limit …s)".
  - **`Geo.haversineKm`** in the SPI.
  - **GLM archive retention.** `RasterRetentionStep.expireGlmArchive` keeps 14 days. The archive's bytes
    count toward the quota.

**2. GLM flash store** (`ai.foresignal.raster.lightning`).
- **`FlashArchive`.**
  - Path: `rasters/glm/{sat}/{yyyymmdd}.bin`.
  - Record: 16 bytes, little-endian `int32 msOfDayUtc, float32 lat, lon, energyFj`.
  - Each flash goes to the file of its own UTC day, so a granule that straddles midnight is split.
  - Each batch is fsynced.
  - A torn trailing record is truncated when the file is next opened for appending, and readers ignore it.
  - The satellite name is validated so it cannot leave the root.
- **`FlashRing`.** 61 one-minute buckets of primitive arrays, no per-flash objects. The 60-minute window
  follows the newest flash, so a catch-up fills it in data time.
- **`FlashStore`** (`@Component`):
  - **Scope:** `foresignal.raster.glm-ring-scope=aoi|conus`. `conus` is honored only in the large
    profile; the small profile warns and stays on `aoi`.
  - **AOI:** the AOI geometry is prepared, with an envelope prefilter. A change rebuilds the ring from the
    archive.
  - **Startup:** the ring is rebuilt from today's tail, and from yesterday's within the first hour after
    midnight.
  - **Read API:** `between`, `flashesWithin(point, radiusKm, since)`, `countWithin(geometry, since)`,
    `nearestKm(point, since)` and `newestFlashFileTime()`.
  - **Metrics:** `ringFlashes()` and `archiveBytes()`.
  - **Test reset:** `IngestStateReset` clears the ring between test methods.
- **`/v1/system`:** `rasters.glm{ringFlashes, archiveBytes, newestFileAt}` is filled, and
  `diskQuotaPct` includes the archive.

**3. `goes-glm`.**
- **Listing:** granules come from `GLM-L2-LCFA/{Y}/{DOY}/{HH}/`, listed from the cursor's hour with
  `start-after`, and processed in key order.
- **Per run:** at most 4 granules, and nothing that started more than 60 minutes ago.
- **Budget:** a run that runs out of budget keeps what it processed.
- **Descriptor:** as specified.
- **Parsing (`GlmParser`):**
  - flash time = `product_time` (J2000 12:00) + `flash_time_offset_of_first_event`;
  - energy × 10¹⁵ fJ;
  - quality flag 0 only.
- **Clustering (`LightningClusterer`):**
  - DBSCAN (8 km haversine, minPts 3) with an eps-cell grid index;
  - greedy nearest-centroid identity within 15 km; new clusters are `lc-{ulid}`;
  - geometry: the centroid, rounded to 0.01°;
  - payload: quantized as specified, plus `automated` and `notAWarning`;
  - `observedAt` is the newest member; `eventTime` is the first flash, kept in state.
- **Lifecycle, in data time:**
  - each processed file adds its coverage to a cluster's quiet time;
  - the gap before a file counts only when it is within 2 minutes;
  - a cluster is tombstoned at 10 minutes of quiet;
  - while no file is processed nothing moves, which is the freeze;
  - state survives restarts.
- A format change degrades the source and skips the file.

**4. `goes-fdc`.**
- **Listing:** the newest scan of `ABI-L2-FDCC/{Y}/{DOY}/{HH}/` with `start-after`, plus the previous
  hour until a key of the current hour is processed.
- **Descriptor:** 4/min and 800/day.
- **Parameter:** `scope=aoi|conus`.
- **`FdcParser`:**
  - masks 10–13 and 30–33;
  - pixel centers through the fixed grid;
  - `frpMw` and `areaKm2` to 2 significant figures (`Area` is m² upstream);
  - `tempK` to 10 K;
  - confidence `processed`, `saturated`, `cloudy` or `high`.
- **Detection records:** `g19:{col}:{row}`. `observedAt` is the scan start. `eventTime` is the first
  sighting, kept in state.
- **Clusters (`FireClusterer`):**
  - components are linked within a Chebyshev distance of 3 pixels, which is what a 1-pixel dilation
    followed by 8-connectivity means;
  - identity carries over by the largest pixel overlap;
  - geometry: the hull buffered by 1 km, computed in a local km plane;
  - vertices are rounded to 0.01° and repeated points dropped (a Point for one pixel).
- **Lifecycle:** both kinds are tombstoned after 3 processed scans without them. A failed download, an
  unreadable file or a missing variable is not a processed scan.

**5. `hms-smoke` / `hms-fire`** (`ai.foresignal.source.adapters.hms`).
- **Fetching (`HmsDailyFiles`):**
  - conditional GETs of today's file, plus yesterday's for the first 6 h after midnight;
  - a 404 is skipped;
  - any other failure, or a truncated or unparseable body, fails the run before anything is tombstoned,
    and forgets the validators of every file the run received.
- **`HmsKmlParser`:**
  - StAX with DTDs and external entities off; any DTD event is refused;
  - density comes from the style URL, then ExtendedData (a word, or the 5/16/27 classes), then the
    description;
  - Start, End and Satellite come from ExtendedData or the CDATA description;
  - ids and payload as specified.
- **Smoke tombstones:** ids absent from a complete file are tombstoned per file day (`LiveIdSet`). Old
  day scopes are dropped after 3 days.
- **`HmsFireParser`:**
  - columns are found by header name, and padded cells are trimmed;
  - id `hms:{yearDay}:{hhmm}:{lat}:{lon}`;
  - `frpMw` is left out for −999;
  - fire points are never tombstoned; they leave through the 24 h TTL.

**6–7. Health, docs, spec.**
- `docs/OpenAPI.json`:
  - `Record.x-foresignal-kinds` gains `lightning-cluster`, `fire-cluster` and `smoke-polygon`;
  - `fire-detection` gains the `goes-fdc` and `hms-fire` payload fields and examples;
  - `SystemRasters.glm` and `diskQuotaPct` descriptions are updated;
  - a changelog entry is added.
- `lintOpenApi`: valid. Postman was regenerated (unchanged).
- `docs/Source-Catalog.md`: a "Lightning and fire sources" table with the verified URLs, sizes and budget
  arithmetic, and the GLM-vs-Blitzortung note.
- `docs/Agent-Guide.md`: a "Lightning and fire" section with the GLM limits, "stale is not clear", and the
  two recipes.
- `docs/Operator-Guide.md` and `docs/Architecture.md`: §3 package tree and dependency rule, and the
  environment variable.
- `application.yml`: `glm-ring-scope`.

**Verified outside the tests** with scratch programs against the compiled classes (JDK 21; no test was
run):

| Check | Result |
|---|---|
| Active granule | 450 flashes, 420 with flag 0; times 21:57:19.036–21:57:39.413; energy 1.29–5,249 fJ; 37 full-field DBSCAN clusters |
| Quiet granule | 145 flashes, 134 with flag 0 |
| FDC file, CONUS | 25 detections, 22 clusters (three of 2 pixels, one linked across a 2-pixel gap) |
| FDC file, Texas box | 2 detections: `g19:608:594` at −103.8093, 34.3155 and `g19:927:692` at −94.7773, 31.6045 |
| HMS smoke | 67 polygons (62 light, 5 medium) |
| HMS fire | 16,777 points (55 without FRP) |
| XXE and entity-expansion KML | both refused |
| `DescriptorValidator` | empty for all four descriptors |

The archive was also checked: layout, torn tail, day split, rebuild, retention and the scope rules. A
simulated GLM run with stubbed HTTP showed:
- clusters on the first run;
- `unchanged` with no tombstones through a 30-minute stall;
- tombstones only after 10 minutes of processed data once files resumed.

## Files created / modified

Production, created:
- `source/spi/`: `FlashSink`, `FlashBatch`;
- `raster/lightning/`: `package-info`, `FlashArchive`, `FlashRing`, `FlashStore`;
- `collect/InputAgeHealthRule`;
- `source/adapters/goes/`: `GoesGlmAdapter`, `GlmParser`, `LightningClusterer`, `GoesFdcAdapter`,
  `FdcParser`, `FireClusterer`, `ClusterIds`;
- `source/adapters/hms/`: `package-info`, `HmsDailyFiles`, `HmsKmlParser`, `HmsFireParser`,
  `HmsSmokeAdapter`, `HmsFireAdapter`.

Production, modified:
- `source/spi/`: `RasterSink` (`flashes()`), `AdapterStateStore` (`recordInput` and its keys), `Geo`
  (`haversineKm`);
- `raster/decode/NetcdfDecoder` (`Reader`, `FixedGrid`);
- `raster/`: `RasterPipeline`, `RasterRetentionStep`, `RasterSystemBlock`;
- `source/adapters/goes/package-info`;
- `application.yml`.

Docs and spec: `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Agent-Guide.md`,
`docs/Operator-Guide.md`, `docs/Architecture.md`.

Tests: listed below.
- Test support, created: `testsupport/weather/FlashTestSink`.
- Fixtures, each directory with a README holding the capture procedure, sizes and SHA-256:
  - `fixtures/glm/`: an active and a quiet granule;
  - `fixtures/fdc/`: one CONUS file with fires in Texas;
  - `fixtures/hms/`: the complete 2026-10-12 KML and TXT.
- Modified:
  - `FamilyDefaultsTest`: the four adapters, and the `weather` class now allows several kinds;
  - `WeatherIngestSupport`: the GOES and HMS base URLs and the new source ids;
  - `RasterRetentionStepIT`: GLM archive retention.

## Deviations from the prompt

1. **Adapters reach NetCDF through `NetcdfDecoder.Reader`.** The prompt names `NetcdfDecoder`
   (enhanced) and `GeosGrid`. `NetcdfDecoder.decode` reads one gridded field per call, so a
   named-variable reader was added. Pixel centers come through `Reader.fixedGrid`, which wraps
   `GeosGrid`, so adapters still import only `raster.decode`, as Architecture §3 allows.
2. **`FlashStore` is reached through the SPI type `FlashSink`** (`ctx.rasters().flashes()`), not the
   raster class itself (Architecture §3 dependency rule). `FlashStore` implements it.
3. **The `maxEnergyFj` bucket is a decade lower bound** (1, 10, 100, 1000 fJ…). The prompt says only
   "bucket". Decades keep versions rare as a storm brightens.
4. **`radiusKm` is at least 1** (rounded to 1 km), so a tight cluster never reads "0 km".
5. **Freeze is implemented as "clusters advance only with processed files".** It adds a gap rule: the
   time before a resumed file counts toward quiet time only when it is within `maxGap` (2 min). The
   result:
   - no tombstones or updates while nothing is processed;
   - after a restart or an outage, a cluster not re-matched within 10 minutes of processed data is
     tombstoned.

   A run that processes no new file returns `unchanged`.
6. **GLM: at most 4 granules a run.** A 30-second poll under 10 requests a minute cannot download every
   backlog file in one run; 4 per run catches up about 5 granules a minute faster than they arrive.
7. **`goes-fdc` processes only the newest scan per run** (like `mrms-probsevere`). Skipped scans count as
   not processed.
8. **"Dilation then 8-connected"** is implemented as linking detections within Chebyshev distance 3,
   which is exactly that operation on the pixel grid.
9. **The HMS fire file is not filtered to the AOI** (the prompt names no filter). That is 16,777 records
   on a quiet October day, conditionally fetched, so an unchanged file costs nothing.
10. **`hms-fire` has no `titleField`.** The prompt's payload has no label.
11. **The "system event" is the existing source-health change event** (`SourceHealthChangedEvent`), and
    `GlmFeedStopsIT` asserts it. See Issue 2.
12. **Fixture capture procedures** are in each fixture directory's README, the established pattern (047
    Deviation 10), not in the Java test packages.
13. **Retention class.** The four sources join `FamilyDefaultsTest`'s provisional `weather` class with the
    §2.4 windows. Conventions lists "GLM lightning" and "FIRMS pixels" as `bulk-event` (90 d), but it
    overrides shorter windows only for `event` and `series`, so 3 d and 7 d stand (Issue 1).
14. **`RasterProperties` is unchanged.** `glm-ring-scope` is read with `@Value` (it sits under
    `foresignal.raster` in `application.yml`).
15. **HMS smoke tombstones carry the fetch time** as `observedAt`. A tombstone needs an `observedAt` no
    earlier than the polygon's End.

## Issues found / open questions

**Needs the owner:**

1. **Retention class for GLM clusters and fire records.** Conventions puts "GLM lightning" and "FIRMS
   pixels" in `bulk-event` (90 d); Weather-Imagery-Design §2.4 says 3 d (`lightning-cluster`) and 7 d
   (`fire-detection`, `fire-cluster`, `smoke-polygon`). The §2.4 windows are implemented. **Owner:**
   keep them, or apply 90 d?
2. **"A degraded source raises the existing 015 system event"** (Weather-Imagery-Design §7.4) does not
   match the code. `NotificationRouter` sends `system.source_failing` only on a transition to
   `failing`; a transition to `degraded` publishes `SourceHealthChangedEvent` and no notification. A
   stalled GLM feed is `degraded`. **Owner:** should `degraded` notify (a new `system.source_degraded`),
   or should the design text change?
3. **`fire-detection` is now shared with 065's FIRMS sources.** The spec entry carries three payload
   shapes (`nasa-firms`, `goes-fdc`, `hms-fire`). FIRMS uses `frp`; FDC and HMS use `frpMw`, as this
   prompt specifies. **Owner:** align the field name before 065?

**Other findings:**
- The GLM `flash_time_offset_of_first_event` can be negative (−0.96 s in the active granule: an event in
  the previous granule). Its `units` attribute names the same instant as `product_time`.
- FDCC `Area` is in m², not km²; the record converts.
- The HMS KML has no ExtendedData today. Everything comes from the CDATA description and the style URL;
  the ExtendedData path is covered with synthetic input.
- Today's (2026-10-13) HMS files answered 404 at 00:38 UTC, which confirms that 404 is normal early in
  the day.
- No instructions directed at an agent appeared in any upstream response, listing or file (S3 listings,
  NetCDF attributes, KML descriptions, CSV).

## Tests written (not run)

| Test | What it covers |
|---|---|
| `raster/lightning/FlashArchiveTest` | 16-byte little-endian layout; day split; torn tail ignored, then truncated; satellite names; 14-day retention; AOI filtering and the read API; places geometry and the rebuild on change; startup rebuild across midnight; quality-flag filter; scopes; ring window; probing view |
| `goes/LightningClusterTest` | DBSCAN eps and minPts (pairs, 9 km spacing, chains, noise, two storms); new cluster time base and payload; identity within 15 km, closest first, 40 km is new; quantized payload stable; buckets; tombstone at 10 min, not before; members reset quiet time; freeze and the gap rule; born and gone in one fetch; restart round trip; ULIDs |
| `goes/GlmParserTest` | both fixtures (counts, times, energies, 37 clusters); `assemble` (offsets, fJ, flags, bad positions); a missing variable → `SilentFormatChangeException` |
| `goes/FdcParserTest` | 25 CONUS detections by mask; the mask set; Texas pixels and lon/lat; 2 significant figures and 10 K; record shape; missing values; 22 clusters with the three pairs; missing `Mask` |
| `hms/HmsKmlParserTest` | 67 polygons, 62/5; density from style, ExtendedData and the 5/16/27 classes; skips and duplicates; ids; XXE and entity expansion refused; format errors; julian times |
| `hms/HmsFireParserTest` | 16,777 points, 55 without FRP; padded rows; reordered columns, skips and duplicates; format errors |
| `goes/GoesGlmAdapterTest` (WireMock S3) | key order and filtering, cursor, ring, state, 37 clusters, then `unchanged`; 60-minute catch-up cap; per-minute budget keeps processed files; format change degrades; failed download moves nothing; probe writes nothing; descriptor |
| `goes/GoesFdcAdapterTest` (WireMock S3) | newest only, AOI 2 + 2; `conus` 25 + 22; misses only on processed scans (download 500, GLM file served as FDC), then tombstones on the third processed miss; first sighting stable; older scan ignored; hour rollover; probe; pixel state; descriptor |
| `hms/HmsAdapterTest` (WireMock) | conditional GET 200 then 304; yesterday window at 03:00 versus 07:00; 404 today; analyst-edit tombstone, then a cut-off body tombstones nothing and forgets validators; old-day cleanup; fire points and validator forgetting on failure; descriptors |
| `goes/FireDetectionIngestIT` | 25 FIRST; identical scan → no version, eventTime stays; AOI scope → 23 misses; outage not counted; 23 tombstones on the third processed miss |
| `goes/FireClusterIngestIT` | 22 FIRST, 3 polygons; identical scan → same ids, no versions; 20 tombstones after 3 processed misses |
| `hms/SmokePolygonIngestIT` | 67 FIRST; time base and `valid_until` = End + 24 h; 304 → `NOT_MODIFIED`; edited file → 1 tombstone |
| `weather/GlmFeedStopsIT` | Weather-Imagery-Design §7.4: the GLM feed stops → no tombstones, no exit alerts, `degraded` with "newest input file", a `SourceHealthChangedEvent` to `degraded` |
| `collect/InputAgeHealthRuleTest` | degraded and healthy thresholds (2 and 15 min), no verdict, `recordInput` |
| `RasterRetentionStepIT` (added) | GLM archive retention at 14 days |

## Compile result

- `./gradlew compileJava compileTestJava`: `BUILD SUCCESSFUL`.
- `./gradlew lintOpenApi`: valid, 71 warnings (unchanged).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: 829 requests, all operations covered
  (collection unchanged).
- No new dependencies.

## Notes for the next prompt

1. **049 (SatDump geostationary):** `NetcdfDecoder.Reader.fixedGrid` gives fixed-grid pixel centers
   in file order, if useful.
2. **052 (place weather, `wx-places`):**
   - read lightning metrics from `FlashSink` (`ctx.rasters().flashes()`): `nearestKm`, `countWithin`,
     `flashesWithin`;
   - check `newestFlashFileTime()` against 2 minutes to tell "no lightning" from "no data" (`asOf`,
     `status: stale`);
   - the ring holds the last 60 minutes inside the places AOI only;
   - add the other two §7.4 ITs; the GLM one is `GlmFeedStopsIT`.
3. **056:** the same read API; `between(from, to)` returns the raw ring flashes.
4. **065 (FIRMS):** `fire-detection` is shared with `goes-fdc` and `hms-fire` (Issue 3).
5. **Any adapter whose input is a stream of files:** `AdapterStateStore.recordInput` plus
   `InputAgeHealthRule` gives "newest file age" health without raster products.
6. **057 checkpoint:**
   - run the 048 tests;
   - `WeatherIngestSupport` gained two base-URL properties (still one cached context);
   - `FlashStore` rebuilds its ring at `ApplicationReadyEvent` in every context and implements
     `IngestStateReset`;
   - the four new sources exist (disabled) in every context;
   - `HmsAdapterTest` and `HmsFireParserTest` parse the 1.6 MB fire file.
7. **143:** measure the GLM download (about 1.1–1.9 GB/day) and the archive (about 28 MB/day, 14 days).
8. **144 (Flyway):** no new tables or columns. `adapter_state` gains the keys `health.newestInputAt`,
   `health.maxInputGap`, `clusters`, `pixels`, `lastEnd`, `lastScan` and `days`.
9. **Owner decisions pending:** Issues 1–3 above, plus those carried from 012–047.
