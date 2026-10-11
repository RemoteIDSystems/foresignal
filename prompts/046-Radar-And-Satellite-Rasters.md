# 046 — Radar And Satellite Rasters

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/046-CR-Radar-And-Satellite-Rasters.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–126).
- **Previous Completion Report:** read `completion-reports/045-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Weather-Imagery-Design.md` §0.1 (decoder row), §1.4 (Source Catalog corrections), §2.2 (UpstreamClient, BudgetGovernor, Health, Probe rows), §2.3 (MRMS u8 codes), §2.4 (quantisation, ladders, NetCDF DiskCache path, tmpdir), §2.5 (budgets), §3 (toolchain row for NetCDF-Java, licence notices), §5.1 (raster products table), §9 (resource budget), §10.4 (verify at implementation). Design prompt 032 = this prompt (Roadmap §5).
- `docs/Architecture.md` §2 and §26 (Maven-Central rule this prompt makes an exception to)
- NetCDF-Java 5.11 docs: https://docs.unidata.ucar.edu/netcdf-java/5.11/userguide/ (GRIB, `Grib2RecordScanner`, `DiskCache`, enhancement)
- MRMS on AWS: https://registry.opendata.aws/noaa-mrms-pds/; GOES on AWS: https://registry.opendata.aws/noaa-goes/; GOES-R L2 PUG Vol. 5 (CMIP, `goes_imager_projection`)
- Completion Reports 005, 006, 042, 043, 045

## Objective

Bring in the first real raster products: MRMS reflectivity, precipitation rate with its flag band and MESH every 2 minutes, and GOES-19 ABI band 13 (clean IR) every 5 minutes. Add NetCDF-Java as the single GRIB2 and NetCDF4 decoder under a tightly scoped repository exception, behind a `RasterDecoder` SPI, with SSRF and tmpdir hardening and a startup self-test.

## Tasks

### 1. Hooks (check, add if missing)

- **005:** `listS3(bucket, prefix, startAfter)` (anonymous ListObjectsV2, XXE-safe XML, keys/sizes/ETags, paging), `download(url, Path tmp, maxBytes)` into `${FORESIGNAL_DATA_DIR}/rasters/tmp`, `getRange(url, start, end, maxBytes)` (206 only, or 200 when start=0; short bodies rejected; no redirects), `mbPerDay` charged in advance from S3 `Size` and corrected from actual bytes, per-descriptor `maxResponseBytes`.
- **006:** the frame-age `HealthRule` (042 registers it for product sources).
- **042 / 045:** `ctx.rasters()`, `RasterDecodePool`, `GridStore`, `GeosGrid`, `LatLonGrid`, styles `nws-reflectivity@1`, `precip-rate@1`, `mesh@1`, `ir-gray@1`, `ir-enhanced@1`.

### 2. NetCDF-Java dependency (repository exception)

- `gradle/libs.versions.toml`: `netcdfJava = "5.11.0"`, `libaec = "1.1.7.0"`; libraries `edu.ucar:netcdf-java-bom`, `edu.ucar:cdm-core`, `edu.ucar:grib`, runtime `edu.ucar.unidata:libaec-native` (verified present in `https://artifacts.unidata.ucar.edu/all/` 2026-10). **No `netcdf4` module.**
- `build.gradle.kts`:
  ```kotlin
  repositories {
      mavenCentral()
      exclusiveContent {
          forRepository { maven("https://artifacts.unidata.ucar.edu/all/") { name = "unidata" } }
          filter { includeGroup("edu.ucar"); includeGroup("edu.ucar.unidata") }
      }
  }
  ```
  (the old `/repository/unidata-all/` path 301-redirects; do not use it).
- Commit `gradle/verification-metadata.xml` with sha256 entries for the `edu.ucar*` artifacts only and `<trusted-artifacts><trust group="^(?!edu\.ucar).*" regex="true"/></trusted-artifacts>` so other dependencies stay unaffected. Generate with `./gradlew --write-verification-metadata sha256 help` and then trim.
- Add a `checkUnidataScope` task (wired into `check`) that walks `runtimeClasspath` resolution results and fails if any component resolved from repository `unidata` has a group other than `edu.ucar` / `edu.ucar.unidata`.
- Fallback documented in the Operator Guide: a vendored file repository under `gradle/vendor-repo/` if the Unidata host is down.
- `docs/Architecture.md` §2 and §26: record this as the one explicit exception to the Maven-Central rule. `THIRD_PARTY_NOTICES.md`: NetCDF-Java (BSD-3), libaec (BSD-2), jj2000, JNA (Apache-2.0 option).

### 3. Hardening

- `SafeNetcdf.open(Path)`: the only call site of `NetcdfFiles.open` / `NetcdfDatasets.openDataset`. It requires a regular file whose realpath sits under `rasters/tmp` or the bundled self-test directory; it never accepts a `String` (NetCDF-Java accepts URLs, which would be SSRF). A test scans `src/main/java` for other call sites.
- Pin `ucar.nc2.util.DiskCache` and `DiskCache2` to `${FORESIGNAL_DATA_DIR}/cache/netcdf/`, never the user home. Use `Grib2RecordScanner.readData` per message; never create `.gbx9` or `.ncx4` indexes.
- Dockerfile/compose `JAVA_TOOL_OPTIONS` add `-Djava.io.tmpdir=/data/tmp -Djna.tmpdir=/data/tmp` (exec allowed on that mount, so libaec's native library can be extracted). Create the directory at startup.
- **Startup self-test** (`DecoderSelfTest`, on `ApplicationReadyEvent`, async): decode bundled minimal fixtures under `src/main/resources/raster/selftest/` for GRIB2 templates 5.3 (GFS complex packing), 5.41 (MRMS PNG) and 5.42 (CCSDS/AEC, via libaec), plus one NetCDF4/HDF5 file. On failure, the affected products are disabled with a health reason, and their endpoints return 503 `PRODUCT_UNAVAILABLE`. Fixtures: cut single messages from real files (range-read via the `.idx` for GFS; one ECMWF open-data message for 5.42); keep each under 200 KB; record provenance in `src/main/resources/raster/selftest/README.md`.

### 4. RasterDecoder SPI (`ai.foresignal.raster.decode`)

- `RasterDecoder { boolean supports(DecodeRequest r); DecodedGrid decode(Path file, DecodeRequest r) throws RasterFormatException, SilentFormatChangeException; }`. `DecodedGrid(gridDefinition, nx, ny, float[] or short[] data, validTime, referenceTime, metadata)`.
- `Grib2Decoder`: scans messages; selects by discipline/category/number and level from the request, **never by message index**. A missing field → `SilentFormatChangeException` (health degraded, no frame written).
- `NetcdfDecoder`: enhanced mode (scale_factor, add_offset, `_Unsigned`, `_FillValue`); variable and projection attributes by name. A missing variable → `SilentFormatChangeException`.
- Gzip inputs (`.grib2.gz`) are inflated by streaming into a sibling temp file before decoding. Temp files are deleted in `finally`.

### 5. `mrms` source (`ai.foresignal.source.adapters.mrms`)

- POLLING, interval 2 min (min 2 min), budget 30/min, 12,000/day, `mbPerDay` 4,000, `maxResponseBytes` 8 MB. STANDARD, `licence` "US-PD", `termsUrl` https://registry.opendata.aws/noaa-mrms-pds/, kinds `[raster-frame]`, `kindRetention{raster-frame: 7d}`, `zeroResultsNormal=true`, `observedAtMode` UPSTREAM.
- Products (S3 bucket `noaa-mrms-pds`, prefix `CONUS/{Product}/{YYYYMMDD}/`, keys like `MRMS_MergedReflectivityQCComposite_00.50_20261010-000041.grib2.gz`, verified 2026-10):
  - `mrms-reflectivity` ← `MergedReflectivityQCComposite_00.50` (1.0–2.4 MB): u8 codes per §2.3: 0 = no coverage (MRMS −999), 1 = no echo / below −32 dBZ (MRMS −99), 2..255 = `clamp(round(dBZ×2)+66, 2, 255)`; scale 0.5, offset −33, nodata 0. Style `nws-reflectivity@1`. nativeMaxZoom 8, `maxZoomForAnimation` 7.
  - `mrms-precip-rate` ← `PrecipRate_00.00` (0.6–1.1 MB) + `PrecipFlag_00.00` as **band 2**: u16; band 1 code = `round(mm/h × 10)` capped 65,534, 65,535 = no coverage (MRMS −3); band 2 = flag value (0 none, 1 warm stratiform, 3 snow, 6 convective, 7 hail, 10 cool stratiform, 91/96 tropical), 65,535 = missing. Pair the two files by identical timestamp (precip files land on even 2-minute marks, e.g. `-000000`); commit when both exist, or after 4 min with band 2 all-missing and `stats.flagMissing=true`. Style `precip-rate@1`.
  - `mrms-mesh-60m` ← `MESH_Max_60min_00.50` (40–150 KB): u8, 0 = no coverage, 1 = 0 mm, 2..255 = `clamp(round(mm)+1, 2, 255)`; scale 1, offset −1. Style `mesh@1`.
  - **Verify the −999/−99/−3 sentinels against the fixtures** and record the values found in the Completion Report.
- Grid: lat/lon 0.01°, 7000 × 3500, taken from the GRIB grid definition (expected NW corner 54.995 N, 230.005 E); never hard-coded.
- Fetch loop per product: `listS3(..., startAfter=lastKey)` (key kept in adapter state); include yesterday's prefix until caught up after UTC midnight; **every frame, no adaptive cadence**; at most 15 new frames per product per run, skipping frames older than the 6-h ladder rung. One access path only (no NCEP `.latest`).
- `frame_id` from the key timestamp (`20261010T000041Z`); `valid_time` = that time; `upstream_key` = S3 key.
- Ladder: every frame for 6 h, then 10-min frames to 48 h. temporal cadence 2 min, `maxGap` 6 min (frame-age degraded after 6 min).

### 6. `goes-east-abi` source (`ai.foresignal.source.adapters.goes`)

- POLLING, interval 5 min, budget 10/min, 2,000/day, `mbPerDay` 2,500, `maxResponseBytes` 8 MB. STANDARD, "US-PD", kinds `[raster-frame]`.
- Product `goes-east-ir`: bucket `noaa-goes19`, prefix `ABI-L2-CMIPC/{YYYY}/{DOY}/{HH}/`, filter keys matching `OR_ABI-L2-CMIPC-M\dC13_G19_s(\d{14})_` (keys sort by channel, so list with prefix `.../OR_ABI-L2-CMIPC-M` and `startAfter`; do not hard-code the scan mode M6). Files 3.4–3.9 MB (verified 3.87 MB, 2026-10).
- Decode variable `CMI` (brightness temperature, K) with the `goes_imager_projection` attributes (`perspective_point_height`, `semi_major_axis`, `semi_minor_axis`, `longitude_of_projection_origin` −75.0 for GOES-19, `sweep_angle_axis` "x") and `x`/`y` scan angles → `GeosGrid` (2500 × 1500). Quantise u16 with scale 0.01, offset 150 (code = `round((K − 150) × 100)`), nodata 65,535 for `_FillValue` / DQF > 1.
- `frame_id` from the `s` start time; `obs_start`/`obs_end` from `time_coverage_start`/`end`; `valid_time = obs_start`. Styles `ir-gray@1` (default), `ir-enhanced@1`. nativeMaxZoom 7, `maxZoomForAnimation` 6. Ladder: 5-min frames 24 h, then 30-min to 72 h. cadence 5 min, `maxGap` 15 min.

### 7. Probe, health, fixtures

- Probe: list the newest object, download it to tmp (counts budget), decode the header only, return the grid summary; write nothing.
- Health: frame-age rule; silent-format-change reasons ("field MergedReflectivityQCComposite missing at level 500 m", "variable CMI missing").
- Fixture capture procedure (`docs/Adapter-Guide.md` + `scripts/capture-raster-fixtures.sh`): `aws s3 cp --no-sign-request` (or `curl` against the HTTPS endpoint) for one file per product. Real files cannot be cropped without re-encoding, so commit them under `src/test/resources/fixtures/mrms/` and `/goes/` when each is < 3 MB (MESH, PrecipFlag, a quiet-day reflectivity); larger ones are fetched by a Gradle `downloadTestFixtures` task with sha256 pins, and their tests are skipped with a logged reason when absent. Include a no-coverage-heavy MRMS sample for the sentinel test.

### 8. Spec, Postman, docs

- No new operations. `docs/OpenAPI.json`: `x-foresignal-products` examples on `Product` for the four products; `SourceHealth` already carries `newestFrameAt`. Regenerate Postman with `python3 scripts/generate-postman.py docs/OpenAPI.json postman` if the spec changed.
- `docs/Source-Catalog.md`: rows for `mrms` and `goes-east-abi` (tier STANDARD, policy ALL, TTL none, time base, budgets, licence) and the §1.4 corrections: NEXRAD L2 at `unidata-nexrad-level2` (`noaa-nexrad-level2` returns AccessDenied), L3 at `unidata-nexrad-level3`; GOES-East is GOES-19 (`noaa-goes19`); NOAA-15/18/19 decommissioned (137 MHz APT gone); ProbSevere v3 at `noaa-mrms-pds/ProbSevere/`; lightning probability at `CONUS/LightningProbabilityNext30minGrid_scale_1/`; HRRR `wrfsfcf01` ≈ 142 MB; GFS PNS 26-29/26-30 notes; ECMWF CC BY 4.0; AWC request limits.
- `docs/Architecture.md` §2 (stack row), §13 (`/data/tmp`, NetCDF cache).

## Tests to write (do not run)

- `MrmsCodecTest`: sentinel round trip (−999 → 0, −99 → 1, 50 dBZ → 166, clamp at 255), precip u16 and flag band, MESH codes.
- `Grib2DecoderTest` (fixture): field selection by parameter and level, missing field → `SilentFormatChangeException`, grid from GDS.
- `NetcdfDecoderTest` (fixture): CMI enhancement, fill handling, geos attributes.
- `SafeNetcdfTest`: rejects paths outside the roots, symlinks, and any `String` overload; source scan for other `NetcdfFiles.open` call sites.
- `DecoderSelfTestTest`: all four fixtures decode; a corrupted 5.42 fixture disables only the products needing it.
- `MrmsAdapterWireMockTest`: S3 XML listing with `start-after`, day rollover, 15-frame cap, precip/flag pairing and the 4-min fallback, `mbPerDay` advance charge, 503 + `Retry-After` backoff.
- `GoesAbiAdapterWireMockTest`: C13 filtering across channels and scan modes, budget accounting.
- `MrmsIngestIT` / `GoesIngestIT`: fixture → frame row, `.fsg`, `raster-frame` record, tile renders non-empty at z=6.
- `UnidataScopeBuildTest` (Gradle TestKit or a simple resolution-result unit test) for `checkUnidataScope`.

## Acceptance criteria

- `./gradlew compileJava compileTestJava` resolves NetCDF-Java only from the Unidata repository under `exclusiveContent`.
- MRMS and GOES fixtures ingest into frames that render as tiles; no-coverage pixels never read as "no echo".
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 126, 129, 130, 131).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 131).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `046: Radar And Satellite Rasters`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/046-CR-Radar-And-Satellite-Rasters.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `046: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
