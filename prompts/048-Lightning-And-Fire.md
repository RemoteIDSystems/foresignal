# 048 — Lightning And Fire

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/048-CR-Lightning-And-Fire.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–133).
- **Previous Completion Report:** read `completion-reports/047-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Weather-Imagery-Design.md` §1.2 (what is ingested), §2.4 (GLM archive layout, per-kind retention, ladders), §2.5 (budgets), §2.6 (GLM ring scope per profile), §5.1 (`goes-glm`, `goes-fdc`, `hms-*` rows; places AOI), §7.1 (derived-record principles), §7.2 (`lightning-cluster` DBSCAN, GLM limits), §7.4 (rule recipes and required integration tests), §9 (resource budget). Design prompt 035 = this prompt (Roadmap §5); design 039 sampling/place-weather is **052**.
- `docs/Core-Data-Model.md` §2 (FIRMS-style acquisition-time mapping; measurement rows), §3, §5.3
- `docs/Gap-Analysis.md` §8 item 36 (HMS lives here, not in 075)
- GOES-R L2 PUG Vol. 5: GLM LCFA and ABI FDC variables; HMS: https://www.ospo.noaa.gov/Products/land/hms.html
- Completion Reports 036, 042, 045, 046, 047

## Objective

Add lightning and fire inputs: GOES-19 GLM flashes kept in an in-memory AOI ring and a compact daily archive, with `lightning-cluster` records; GOES-19 ABI fire detections (`fire-detection`, `fire-cluster`); and the NOAA HMS analyst smoke polygons and fire points. Absence-driven tombstones happen only after scans that were actually processed, so a feed outage freezes state instead of clearing it.

## Tasks

### 1. Hooks (check, add if missing)

- **046:** `SafeNetcdf`, `NetcdfDecoder` (enhanced mode), the Unidata dependency and the self-test.
- **045:** `GeosGrid` (FDC pixel → lon/lat).
- **036:** `PlacesAoiService` (`aoi()`, `envelope()`, `PlacesAoiChangedEvent`, default AOI fallback).
- **017 / 036:** `near` and `cooldownScope` on trigger rules. **019:** per-kind retention; the 042 `RasterRetentionStep` gains GLM archive retention here.
- **006:** frame-age style health rule, reused as a "newest file age" rule for GLM (`maxGap` 2 min) and FDC (15 min).

### 2. GLM flash store (`ai.foresignal.raster.lightning`)

- `FlashStore`, reached by adapters through `ctx.rasters().flashes()`:
  - **Archive:** `${FORESIGNAL_DATA_DIR}/rasters/glm/{sat}/{yyyymmdd}.bin`, append-only, **16 bytes per flash**, little-endian: `int32 msOfDayUtc`, `float32 lat`, `float32 lon`, `float32 energyFj`. Only flashes with `flash_quality_flag == 0`. Full field of view (≈ 28 MB/day). fsync on each append batch; a torn trailing record (< 16 bytes) is truncated on open.
  - **Ring:** in memory, last 60 min, flashes inside the places-AOI geometry only (small profile). The large profile may opt in to CONUS (`foresignal.raster.glm-ring-scope=aoi|conus`). Primitive arrays in time buckets of 1 min, no per-flash objects.
  - On startup the ring is rebuilt from the tail of today's (and, near midnight, yesterday's) archive.
  - Read API for 052 and 056: `flashesWithin(point, radiusKm, since)`, `countWithin(geometry, since)`, `nearestKm(point, since)`, and `newestFlashFileTime()` (so consumers can tell "no lightning" from "no data").
- Retention: archive files older than 14 d are deleted by `RasterRetentionStep`; bytes count toward the raster disk quota.

### 3. `goes-glm` (`ai.foresignal.source.adapters.goes`)

- `listS3("noaa-goes19", "GLM-L2-LCFA/{YYYY}/{DOY}/{HH}/", startAfter)`; files are 20-s granules (`OR_GLM-L2-LCFA_G19_s20262831400000_e…_c….nc`, ≈ 0.2 MB quiet to ≈ 0.36 MB active, verified 2026-10). Process every new file in order; catch up at most 60 min after an outage.
- Descriptor: POLLING, interval 30 s, budget 10/min, 10,000/day (≈ 2,880 listings + 4,320 downloads), `mbPerDay` 3,000, kinds `[lightning-cluster]`, `kindRetention{lightning-cluster: 3d}`, STANDARD, ALL, `recordTtl` null, `licence` "US-PD", `zeroResultsNormal=true`, `titleField` `label`.
- Decode with `NetcdfDecoder` (enhanced): `flash_lat`, `flash_lon`, `flash_time_offset_of_first_event` (relative to `product_time`, seconds since 2000-01-01 12:00:00 UTC), `flash_energy`, `flash_quality_flag`. A missing variable → silent-format-change health reason. Append to the archive and the ring.
- **`lightning-cluster` records** after each processed file:
  - DBSCAN over ring flashes of the last **5 min**, eps **8 km** (haversine), minPts **3**; spatial grid index so one pass is O(n).
  - Identity: match each cluster to the previous run's clusters by nearest centroid within 15 km (greedy, closest first); unmatched clusters get `lc-{ulid}`.
  - Geometry: centroid Point rounded to 0.01°. Payload (quantised, so versions only on meaningful change): `flashes5m` bucket lower bound (3, 5, 10, 20, 50, 100, 200), `radiusKm` (1 km), `maxEnergyFj` bucket, `label` ("Lightning cluster, 20+ flashes/5 min"), `automated: true`, `notAWarning: true`. `observedAt` = newest member flash; `eventTime` = the cluster's first flash (kept in adapter state).
  - Tombstone a cluster after **10 min with no member flashes in processed files**. If no file has been processed for more than 2 min (`maxGap`), freeze: no tombstones and no updates until data resumes. On restart, clusters listed in adapter state that are not re-matched within 10 min of resumed processing are tombstoned.
- Place metrics (nearest flash, counts within radius) are computed by `wx-places` in 052 from `FlashStore`; nothing here writes place-weather.

### 4. `goes-fdc` (`ai.foresignal.source.adapters.goes`)

- `listS3("noaa-goes19", "ABI-L2-FDCC/{YYYY}/{DOY}/{HH}/", startAfter)`; one ≈ 0.48 MB file per 5 min (verified 2026-10).
- Descriptor: POLLING, interval 5 min, **budget 4/min, 800/day** (288 listings + 288 downloads + hour/day rollover listings leave no headroom under the design's 600; record this), `mbPerDay` 300, kinds `[fire-detection, fire-cluster]`, `kindRetention` 7 d each, STANDARD, ALL, `recordTtl` null, `licence` "US-PD", `titleField` `label`.
- Params: `scope` = `aoi` (default; places-AOI envelope) | `conus`.
- Decode `Mask`, `Power` (FRP, MW), `Temp` (K), `Area` (km²), `DQF`, plus `goes_imager_projection`/`x`/`y` → `GeosGrid` for pixel centres.
- **`fire-detection`:** pixels with mask **10–13 or 30–33** (processed, saturated, cloud-contaminated, high-probability, and their temporally filtered variants). `externalId` = `g19:{col}:{row}`. Point geometry at the pixel centre. `observedAt` = scan start (`s` time); `eventTime` = `firstSeenAt` (adapter state; stable across scans). Payload: `maskCode`, `confidence` (processed | saturated | cloudy | high), `temporallyFiltered`, `frpMw` (**2 significant figures**), `tempK` (rounded to 10 K), `areaKm2` (2 significant figures), `satellite: "G19"`, `method: "FDC"`, `label`.
- Tombstone a pixel after **3 consecutive processed scans** without it (miss counters in adapter state). Scans not processed (feed down, decode failure) do not count.
- **`fire-cluster`:** 8-connected components over current detections after a 1-pixel dilation. Identity carried from the previous scan by pixel overlap (largest overlap wins), else `fc-{ulid}`. Geometry: convex hull of member pixel centres buffered 1 km, vertices rounded to 0.01° (a Point for one pixel). Payload: `pixels`, `frpTotalMw`, `frpMaxMw` (2 significant figures), `firstSeenAt`, `label`. Tombstoned after 3 processed scans without members.

### 5. `hms-smoke` and `hms-fire` (`ai.foresignal.source.adapters.hms`)

- Daily files (verified 2026-10, ETag and Last-Modified present, so use `getConditional`):
  - smoke: `https://satepsanone.nesdis.noaa.gov/pub/FIRE/web/HMS/Smoke_Polygons/KML/{YYYY}/{MM}/hms_smoke{YYYYMMDD}.kml`
  - fire: `https://satepsanone.nesdis.noaa.gov/pub/FIRE/web/HMS/Fire_Points/Text/{YYYY}/{MM}/hms_fire{YYYYMMDD}.txt` (CSV header `Lon, Lat, YearDay, Time, Satellite, Method, Ecosystem, FRP`, padded with spaces)
  - Fetch today's file (UTC); during the first 6 h after midnight also re-fetch yesterday's (analysts finish it late). A 404 for today before the first analysis is normal.
- Both: POLLING, interval 30 min, budget 4/min, 200/day, STANDARD, ALL, `ttlAnchor` OBSERVED, `recordTtl` 24 h, `licence` "US-PD", `termsUrl` https://www.ospo.noaa.gov/Products/land/hms.html.
- `hms-smoke` → kind `smoke-polygon` (7 d). KML Placemarks (parse with a hardened XML parser: no DTDs, no external entities). Density from the style URL (`#Smoke_Light_style`, `_Medium_`, `_Heavy_`) or ExtendedData `Density`; `Start`/`End` and `Satellite` from ExtendedData or the description. `eventTime` = Start, `observedAt` = End. `externalId` = first 16 hex chars of sha256(`date|start|end|density|satellite|centroid rounded 0.1°`). Payload `density` (light|medium|heavy), `densityRank` (1–3), `start`, `end`, `satellite`, `label`. Within one file (complete), ids absent from a successful fetch are tombstoned (analyst edits).
- `hms-fire` → kind `fire-detection` (shared with FDC so "fire near home" covers both). `externalId` = `hms:{yearDay}:{hhmm}:{lat 4dp}:{lon 4dp}`; `observedAt` = `eventTime` = YearDay + Time (UTC); payload `satellite`, `method`, `ecosystem`, `frpMw` (2 s.f.; omit when −999), `source: "HMS"`.

### 6. Health and documentation

- GLM: degraded when the newest processed file is older than 2 min; FDC: older than 15 min; HMS: standard rules (`zeroResultsNormal` for fire outside season).
- `docs/Agent-Guide.md` weather section, **GLM limits:** GLM's footprint over Texas is about 8–10 km and its detection efficiency is below 100%; GLM-only lightning rules should use 15 km or more, and all-clears are advisory. Rule recipes: "Fire near home" (`kind=fire-detection`, `near{home, 20}`, `match`, `only_new`); "Lightning cluster near home" (`kind=lightning-cluster`, `near{home, 15}`, `enter`).
- "Stale is not clear": document that a stalled GLM or FDC feed freezes clusters and detections (no tombstones, no exit alerts) and raises the source-degraded system event instead.

### 7. Spec, Postman, docs

- No new operations. `/v1/system` `rasters` block gains `glm{ringFlashes, archiveBytes, newestFileAt}`; update `SystemInfo` in `docs/OpenAPI.json` and add `Record` examples for `lightning-cluster`, `fire-detection`, `fire-cluster`, `smoke-polygon`. Regenerate Postman with `python3 scripts/generate-postman.py docs/OpenAPI.json postman`.
- `docs/Source-Catalog.md`: rows for `goes-glm`, `goes-fdc`, `hms-smoke`, `hms-fire` (tier, policy, TTL, anchors, time base, budgets with the arithmetic, licence, the verified URLs and sizes); note GLM vs Blitzortung (skipped: non-commercial).

## Tests to write (do not run)

- Fixture capture procedure (in each test package README): one GLM granule from an active hour and one from a quiet hour, one FDCC file with fires inside a Texas AOI, today's HMS KML and TXT; via the S3 HTTPS endpoint / `curl`, stored under `src/test/resources/fixtures/{glm,fdc,hms}/`.
- `FlashArchiveTest`: 16-byte layout, torn-tail truncation, ring rebuild from archive, AOI filtering, quality-flag filter.
- `LightningClusterTest`: DBSCAN eps/minPts behaviour, identity carry-over within 15 km, quantised payload stable under small changes, tombstone after 10 min, **freeze when no file for > 2 min**.
- `GlmParserTest`, `FdcParserTest` (mask set 10–13/30–33, pixel → lon/lat, 2 s.f. rounding), `HmsKmlParserTest` (density from style, XXE attempt rejected), `HmsFireParserTest` (padded CSV, −999 FRP).
- WireMock adapter tests: `GoesGlmAdapterTest` (listing order, 60-min catch-up cap, budget), `GoesFdcAdapterTest` (miss counting only on processed scans), `HmsAdapterTest` (conditional GET, yesterday re-fetch window, 404 today).
- Ingest ITs: `FireDetectionIngestIT` (versions only when quantised values change, tombstone after 3 processed misses, none during an outage), `FireClusterIngestIT`, `SmokePolygonIngestIT`.
- Design §7.4 integration test (first of three; 052 adds the place-weather ones): `GlmFeedStopsIT`: the GLM feed stops → no cluster tombstones, no exit alerts, the source goes degraded and the system event fires.

## Acceptance criteria

- GLM granules populate the archive and the AOI ring; clusters appear, persist only on quantised change and freeze during outages.
- FDC and HMS fixtures produce `fire-detection`, `fire-cluster` and `smoke-polygon` records with the stated ids and time bases.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 133, 136, 137, 138).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 138).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `048: Lightning And Fire`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/048-CR-Lightning-And-Fire.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `048: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
