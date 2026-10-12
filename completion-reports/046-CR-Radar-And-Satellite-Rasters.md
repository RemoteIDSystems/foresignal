# 046-CR-Radar-And-Satellite-Rasters

| | |
|---|---|
| Prompt | 046-Radar-And-Satellite-Rasters.md |
| Status | Complete |
| Implementation commit | 4c1fa278cc8553af7ebbd4dc242f4d094c1695c3 |
| Date | 2026-10-12 |

## Summary

Foresignal now ingests its first real raster products:
- MRMS composite reflectivity, precipitation rate with its PrecipFlag band, and 60-minute MESH, every
  2-minute frame;
- GOES-19 ABI band 13 (clean IR), every 5-minute scan.

Both decode through NetCDF-Java, which is fetched only from Unidata's repository under a tightly scoped
`exclusiveContent` exception. The decoders sit behind a `RasterDecoder` SPI, with SSRF and tmpdir
hardening and a startup self-test. All eight tasks are done. No operations were added.

**1. Hooks.** 005's `listS3`, `download`, `getRange` and the response cap already existed. Added:
- `SourceHttp.download(uri, target, maxBytes, expectedBytes)`. It charges the listing's `Size`
  against `mbPerDay` before sending, releases the charge once the meter holds the real bytes (also on
  an error status or an I/O failure), and resolves a relative target under `rasters/tmp`.
- `UpstreamClient.setS3Endpoint` (test property `foresignal.test.s3-endpoint`) for path-style
  listings against WireMock.
- `RasterSink.disabled(productId)`, so adapters skip products the self-test disabled.
- `FrameInput.extraStats` / `Builder.stat`, merged into `raster_frame.stats` without overriding
  computed entries (used for `flagMissing`).
- `FrameAgeHealthRule` now degrades a source whose product is disabled, with the reason.

**2. NetCDF-Java dependency.**
- `gradle/libs.versions.toml`: `netcdfJava = "5.11.0"`, `libaec = "1.1.7.0"`; libraries
  `netcdf-java-bom`, `cdm-core`, `grib` and runtime `libaec-native`. All four POMs were checked:
  HTTP 200 at `https://artifacts.unidata.ucar.edu/all/` on 2026-10-12. `edu.ucar:cdm-core` is not on
  Maven Central (`maven-metadata.xml` returned 404).
- `build.gradle.kts`: the `exclusiveContent` block exactly as the prompt gives it. There is no
  `netcdf4` module.
- `gradle/verification-metadata.xml`:
  - generated with `--write-verification-metadata sha256 help`, then trimmed to the 9 `edu.ucar*`
    components;
  - `<trust group="^(?!edu\.ucar).*" regex="true"/>`.
- `checkUnidataScope` (`gradle/unidata-scope.gradle.kts`, applied by the build and wired into `check`).
  - It walks the resolved `runtimeClasspath` graph and fails when a component from repository
    `unidata` has another group.
  - On the real build it prints `8 component(s) from 'unidata', all in [edu.ucar, edu.ucar.unidata]`.
  - In a throwaway build with `org.example:evil:1.0` served from a repository named `unidata`, it
    failed with `Only edu.ucar and edu.ucar.unidata may resolve from the 'unidata' repository, but
    these did: org.example:evil:1.0`.
- Documentation: the vendored `gradle/vendor-repo/` fallback is in the Operator Guide. Architecture §2
  (stack row and the exception) and §26 record it. `THIRD_PARTY_NOTICES.md` covers NetCDF-Java
  (BSD-3), libaec (BSD-2, from the license file inside the jar), jj2000, JNA (Apache-2.0 option) and
  the other transitive dependencies.

**3. Hardening** (`ai.foresignal.raster.decode`).
- `SafeNetcdf` is the only call site of `NetcdfFiles.open` and `NetcdfDatasets.openDataset`, and the
  only place that constructs the GRIB `RandomAccessFile`. It takes `Path` only (no `String`
  overload). It refuses symbolic links, non-regular files and real paths outside `rasters/tmp` or
  `rasters/selftest`.
- `NetcdfRuntime` (applied by the first `SafeNetcdf`) pins the caches under
  `${FORESIGNAL_DATA_DIR}/cache/netcdf/`:
  - `DiskCache` gets that root with always-in-cache;
  - `nj22.cache` is set before `DiskCache2.getDefault()` is used, since it reads its root from that
    property;
  - `GribIndexCache` gets a `DiskCache2` under `cache/netcdf/grib`.

  The GRIB decoder uses `Grib2RecordScanner` and `Grib2Record.readData` only, so no `.gbx9` or
  `.ncx4` index is ever created.
- Dockerfile and compose set `JAVA_TOOL_OPTIONS` with `-Djava.io.tmpdir=/data/tmp -Djna.tmpdir=/data/tmp`.
  Compose appends them after the operator's heap flags. The image creates `/data/tmp`, and
  `ForesignalApplication.main` creates both directories before anything uses them.
- `DecoderSelfTest` runs on `ApplicationReadyEvent` on its own thread (`StartupOrder.DECODER_SELF_TEST = 28`).
  - It decodes four bundled fixtures, each under 200 KB:
    - GFS 1° `TMP:2 m` (template 5.3);
    - MRMS Hawaii MESH (5.41);
    - ECMWF IFS `lsm` (5.42, CCSDS through libaec);
    - a GOES-19 ABI DMWVC NetCDF4 file.
  - Provenance is in `src/main/resources/raster/selftest/README.md`.
  - A failed path disables exactly the products that declare it through `DecoderDependent`
    (`ProductStatusTracker.disable`, so `503 PRODUCT_UNAVAILABLE` and a health reason).
- **Verified outside the tests** with a scratch program against the compiled classes: all four
  fixtures decode. The 5.42 message decoded through libaec, which JNA extracted under the given
  `jna.tmpdir`. That exercise counts as Weather-Imagery-Design §10.4's "libaec extracts under the
  hardened tmpdir" for this host (Linux x86-64), not for the container.

**4. RasterDecoder SPI.**
- `RasterDecoder { supports(DecodeRequest); decode(Path, DecodeRequest) }`.
- `DecodedGrid(gridDefinition, nx, ny, float[] | short[], validTime, referenceTime, metadata)`.
- `DecodeRequest` selects GRIB2 by discipline, category, number and optional level, or NetCDF by
  variable with an optional `DQF` limit. It also carries header-only and gridless flags.
- `Grib2Decoder`:
  - never selects by message index;
  - reads the template 3.0 grid from the GDS octets in basic-angle units. NetCDF-Java's floats would
    make MRMS's 230.005° into 230.00499°;
  - normalizes the scanning mode to rows north to south;
  - treats a global grid as −180..180;
  - a missing field raises `SilentFormatChangeException("field MergedReflectivityQCComposite missing
    at level 500 m")`.
- `NetcdfDecoder`:
  - opens in enhanced mode;
  - finds variables and projection attributes by name;
  - builds the `geos` grid from `goes_imager_projection` plus the `x`/`y` scan angles, with the box
    projected from the grid outline;
  - a missing variable raises `"variable CMI missing"`.
- `GzipInputs` inflates `.gz` into a sibling temporary file (capped at 512 MB), deleted in `finally`.

**5. `mrms`** (`ai.foresignal.source.adapters.mrms`).
- POLLING every 2 min; budget 30/min, 12,000/day and 4,000 MB/day; `maxResponseBytes` 8 MB.
- STANDARD, US-PD, kinds `[raster-frame]`, `kindRetention{raster-frame: 7d}`, `zeroResultsNormal`,
  UPSTREAM.
- Fetching:
  - per product it lists with `start-after` the last committed key (adapter state `lastKey:{productId}`);
  - it lists yesterday's prefix too until caught up, within 6 h of UTC midnight;
  - at most 15 frames per product per run, oldest first;
  - frames older than the 6-hour rung are skipped;
  - one access path only.
- `frame_id` and `valid_time` come from the key's timestamp; `upstream_key` is the S3 key.
- `MrmsCodec` holds the u8 and u16 code spaces of the prompt.
- Precipitation pairs rate and flag files by identical timestamp. After 4 minutes it commits without
  the flag, with band 2 all 65,535 and `stats.flagMissing = true`.
- The grid comes from the GDS: 7000 × 3500, first point 54.995° N, 230.005° E.

**Sentinels verified against the 2026-10-12 fixtures** (NetCDF-Java decode, counts in
`src/test/resources/fixtures/mrms/README.md`):

| Field | No coverage | No echo or zero |
|---|---|---|
| Reflectivity | −999 (8,174,561 cells) | −99 (14,293,209 cells) |
| PrecipRate | −3 (8,335,467 cells) | 0 |
| PrecipFlag | −3 | 0 |
| MESH | −3 | **−1** (not 0); mapped to code 1 = 0 mm |

**6. `goes-east-abi`** (`ai.foresignal.source.adapters.goes`).
- POLLING every 5 min; budget 10/min, 2,000/day and 2,500 MB/day; 8 MB cap.
- Lists `ABI-L2-CMIPC/{Y}/{DOY}/{HH}/OR_ABI-L2-CMIPC-M` with `start-after`. Filters to
  `OR_ABI-L2-CMIPC-M\dC13_G19_s(\d{14})_`, so the scan mode is not hard-coded.
- Decodes `CMI` with `DQF > 1` set missing. `GoesCodec` quantizes to u16 with scale 0.01 and offset 150;
  65,535 is nodata.
- `frame_id` comes from `s`; `obs_start` and `obs_end` from `time_coverage_*`.
- Styles `ir-gray@1` (default) and `ir-enhanced@1`; zooms 7 and 6.
- Ladder: 5-minute frames for 24 h, then 30-minute frames to 72 h. Cadence 5 min, `maxGap` 15 min.
- Verified on the fixture: CMI ranges 190.53–298.68 K with 47,163 fill samples. The grid is
  2500 × 1500, about 150.23° W–52.92° W and 14.56° N–56.32° N.

**7. Probe, health, fixtures.**
- Probes (both adapters) list the newest object, download it (one budgeted request), decode the header
  and return the grid summary. They write nothing, adapter state included.
- Health: the frame-age rule, silent format changes reported per product, and disabled products.
- `scripts/capture-raster-fixtures.sh` was run once successfully into a scratch directory.
- Committed MRMS fixtures (all under 3 MB): reflectivity 1.75 MB, PrecipRate 0.87 MB, PrecipFlag
  0.20 MB, MESH 39 KB, and Hawaii MESH 16 KB. The Hawaii file is the no-coverage-heavy sample (41 %).
- The GOES file is 3.8 MB. `./gradlew downloadTestFixtures` fetches it into `build/test-fixtures/goes/`
  and checks the sha256 pin. It ran successfully here. Tests skip with a logged reason when it is
  absent.
- `docs/Adapter-Guide.md` gained "Raster fixtures".

**8. Spec, Postman, docs.**
- `docs/OpenAPI.json`: the four `x-foresignal-products` examples already existed. They were
  reconciled: precipitation and MESH gain `maxZoomForAnimation` 7. There is a changelog entry.
- `lintOpenApi` reports valid, with 71 warnings (unchanged).
- Postman regenerated (unchanged).
- `docs/Source-Catalog.md`: rows for `mrms` and `goes-east-abi`, plus the §1.4 corrections. Re-checked
  today: `noaa-nexrad-level2` still answers AccessDenied; `unidata-nexrad-level2` and `-level3`,
  `ProbSevere/` and `LightningProbabilityNext30minGrid_scale_1/` all list objects.
- `docs/Architecture.md`: §2 (row and exception), §3 (`decode` package, dependency rule), §13
  (`/data/tmp`, the NetCDF cache, the self-test property), §26, §28 (decoders, first product sources).
- `docs/Operator-Guide.md`: a new "Radar and satellite products" section and the Unidata-down build
  procedure.

## Files created / modified

Production, created:
- `raster/decode/`: `package-info`, `DecodeRequest`, `DecodedGrid`, `DecoderCapability`,
  `DecoderDependent`, `RasterDecoder`, `SafeNetcdf`, `NetcdfRuntime`, `GzipInputs`, `Grib2Decoder`,
  `NetcdfDecoder`, `DecoderSelfTest`.
- `source/adapters/mrms/`: `package-info`, `MrmsAdapter`, `MrmsCodec`.
- `source/adapters/goes/`: `package-info`, `GoesAbiAdapter`, `GoesCodec`.
- Resources: `raster/selftest/` (4 fixtures and a README).
- Build and scripts: `gradle/unidata-scope.gradle.kts`, `gradle/verification-metadata.xml`,
  `scripts/capture-raster-fixtures.sh`.

Production, modified:
- `ForesignalApplication` (temp directories);
- `collect/`: `BoundSourceHttp`, `BudgetGovernor` (Deviation 1), `UpstreamClient`;
- `config/StartupOrder`;
- `raster/`: `FrameAgeHealthRule`, `RasterFormatException` (doc), `RasterPaths` (`selfTest`,
  `netcdfCache`), `RasterPipeline`;
- `source/spi/`: `FrameInput`, `RasterSink`, `SourceHttp`;
- `build.gradle.kts`, `gradle/libs.versions.toml`, `Dockerfile`, `docker-compose.yml`.

Docs and spec: `docs/OpenAPI.json`, `docs/Architecture.md`, `docs/Source-Catalog.md`,
`docs/Operator-Guide.md`, `docs/Adapter-Guide.md`, `THIRD_PARTY_NOTICES.md`.

Tests: listed below. Test support, created:
- `testsupport/raster/`: `DecoderFixtures`, `ProductAdapterHarness`, `ProductIngestSupport`;
- fixtures under `src/test/resources/fixtures/mrms/` (5 files and a README) and
  `fixtures/goes/README.md`.

Test support, modified: `FamilyDefaultsTest`, `BudgetGovernorTest`, `UpstreamClientIT`,
`FrameAgeHealthRuleTest`.

## Deviations from the prompt

1. **A 005 defect fixed: bytes were counted twice against `mbPerDay`.**
   - The old behavior: `BudgetGovernor.correctByteCharge` adjusted the advance charge to the actual
     size, while `UsageMeter` had already recorded the same bytes. Every transfer counted twice in
     `remainingBytesToday`.
   - The consequence: MRMS would have been refused once about half of its 4,000 MB were really used.
   - The fix: the advance charge is now released in full. The existing `correctsAfterwards` test
     still holds, and `noDoubleCount` asserts the fix.
2. **Two `RasterDecoder` types.**
   - 042 already had `ai.foresignal.source.spi.RasterDecoder` (`DecodedBands decode(Path)`), the
     handle `FrameInput` carries to the decode pool.
   - The prompt's SPI is `ai.foresignal.raster.decode.RasterDecoder`.
   - Adapters bridge them: they decode the header on the fetch thread (the grid is needed before
     `commit`), then hand the pipeline a lambda that runs the full decode and the codec on the pool.
3. **Adapters import `raster.decode`.** Architecture §3 said adapters depend only on SPI types and
   `UpstreamClient`. The prompt puts the decoders in `ai.foresignal.raster.decode`, and the adapters
   need them, so §3 now allows product adapters to use those stateless decoders, like
   `source.formats`.
4. **`DecodedGrid` and `DecodeRequest` extras.**
   - `DecodedGrid` may carry neither array (header-only decode) or a null grid (a gridless NetCDF
     variable). The self-test's only small real NetCDF4 file (DMWVC winds) is not gridded.
     NetCDF-Java cannot write NetCDF4 without the native library, so a small gridded CMIP file could
     not be made.
   - `DecodeRequest` has `header()` and `valuesOnly()` for the same reasons.
5. **Fixture choices.**
   - The 5.41 self-test fixture is the MRMS **Hawaii** MESH field (2600 × 2200), not CONUS, so the
     startup decode allocates 23 MB instead of 98 MB.
   - The 5.3 fixture is from the GFS **1°** file. 0.25° messages are 0.3–1 MB.
   - The 5.42 fixture is ECMWF `lsm` (183 KB). Smaller step-0 messages are constant fields encoded
     without CCSDS.
6. **Self-test fixtures are unpacked into `rasters/selftest/`.** NetCDF-Java reads files, and
   `SafeNetcdf` allows only that directory and `rasters/tmp`.
7. **`valid_time` for GOES is the key's `s` time.** That is what fixes `frame_id`. It equals
   `time_coverage_start` (12:01:17.0 on the fixture), and `obs_start` is set from the attribute.
8. **GOES listing.**
   - Hours are listed from the last committed key's hour, at most 3 hours back, plus the current hour.
   - The prompt does not bound the catch-up. A cold start lists the previous and current hour.
   - The same 15-frame cap as MRMS is applied.
   - A mid-hour scan-mode change sorts the new mode's keys before the cursor, so that hour's remaining
     frames are skipped (documented on the class).
9. **Per-adapter base URL properties.**
   - Object downloads use `foresignal.test.adapters.mrms.base-url` and `…goes.base-url`
     (path-style). Listings go through `foresignal.test.s3-endpoint` on `UpstreamClient`.
   - The ingest ITs run the adapters through the real pipeline and the context's `UpstreamClient`
     inside 042's shared raster context, so they add **no** new Spring context. The cost: their
     `raster-frame` record is asserted as returned by the pipeline, not read back from the writer.
10. **`FamilyDefaultsTest`** classifies `MrmsAdapter` and `GoesAbiAdapter` as a new `raster` class:
    kinds exactly `[raster-frame]`, `kindRetention ≥ 7d`. Conventions has no class for frame records
    (042 Issue 2, still open).
11. **MESH "no hail" is −1 in the data**, and is mapped to code 1. `MrmsCodec` also treats any MRMS
    reflectivity below −32 dBZ as no echo, per the prompt.
12. **`UnidataScopeBuildTest` uses Gradle TestKit** (`testImplementation(gradleTestKit())`, shipped
    with Gradle). `checkUnidataScope` reads the internal `ResolvedComponentResultInternal.getRepositoryName`,
    because Gradle 9.8's public API does not expose a component's repository. The task fails loudly if
    that type changes.

## Issues found / open questions

**Needs the owner:**

1. **jj2000's license.** `edu.ucar:grib` declares `edu.ucar:jj2000` 5.4 at runtime. Its copyright
   notice grants use only in products conforming to the JPEG 2000 standard, and warns about patents.
   Foresignal uses no GRIB2 template 5.40 yet. **Owner:** keep it, or exclude it until a JPEG 2000
   product appears?
2. **Commons Math 3 arrives with `cdm-core`.** Conventions say "no Commons Math 3.x" for the
   intelligence layer; it is NetCDF-Java's runtime dependency only. **Owner:** acceptable as
   transitive-only, or try excluding it (unverified whether NetCDF-Java needs it on these code paths)?
3. **Apache HttpClient 4 arrives with `httpservices`.** NetCDF-Java's remote access can never be
   reached because `SafeNetcdf` opens only local paths. **Owner:** fine, or exclude
   `edu.ucar:httpservices` (unverified)?
4. **Frame-record retention class** (carried from 042 Issue 2): `raster` is used provisionally in
   `FamilyDefaultsTest`.
5. **Container `noexec`.** The libaec extraction in the container (`/data/tmp` on a named volume) was
   not verified; no Docker image was built. **Owner:** check at 057 or 143.

**Other findings:**
- Spring's BOM downgrades two of NetCDF-Java's declared versions: protobuf-java 4.36.1 → 4.35.1 and
  slf4j 2.0.19 → 2.0.18. Neither is on the decode paths exercised (the scratch decodes ran with the
  same resolved classpath).
- Not verified at run time (tests first run at 057): the WireMock and IT expectations, TestKit inside
  the test JVM, and the async self-test in every context. The decoders, the self-test fixtures, the
  descriptor and product validation (`DescriptorValidator.problems` and `ProductRegistry.problems`
  both empty) and style compilation for all five product/style pairs were exercised with scratch
  programs against the compiled classes.
- Instructions directed at an agent: none appeared in any data, file or tool output. That includes the
  S3 listings, the ECMWF index, the GRIB and NetCDF files, the POMs and the jj2000 COPYRIGHT file.

## Tests written (not run)

The WireMock adapter tests are `@IntegrationTest` (the default context) with a hand-built fetch
context; the ingest ITs share 042's `@WebIntegrationTest @Import(RasterTestSources.class)` context.

| Test | What it covers |
|---|---|
| `source/adapters/mrms/MrmsCodecTest` | −999 → 0, −99 → 1, 50 dBZ → 166, clamp 255, the dBZ round trip; precipitation u16 and the flag band; MESH codes; real fixture counts: every −999 cell is code 0, and the Hawaii MESH maps only to 0 and 1 |
| `source/adapters/goes/GoesCodecTest` | u16 codes, NaN → 65,535, clamping |
| `raster/decode/Grib2DecoderTest` | selection by parameter and level, grid from the GDS (−130..−60, 20..55, `la1` 54.995, `lo1` −129.995), missing field or level → `SilentFormatChangeException` with the exact reason, header-only, 5.3 global grid, 5.42 via libaec, non-GRIB → `RasterFormatException`, scan-mode reordering, sign-magnitude octets and the angle unit |
| `raster/decode/NetcdfDecoderTest` | CMI enhancement (190.53–298.68 K), 47,163 fill → NaN, `geos` parameters and outline, `DQF` limit, header-only, "variable CMI missing", gridless values, outline refusal off the disc (the GOES cases skip without the downloaded fixture) |
| `raster/decode/SafeNetcdfTest` | opens under a root; refuses outside paths, symlinks (inside and escaping), directories, missing files, `..` escapes and null; no public `String` parameter; source scan for other NetCDF-Java opener call sites; cache pinning |
| `raster/decode/DecoderSelfTestTest` | all four fixtures pass; a corrupted 5.42 fixture disables only the CCSDS-dependent products; a missing fixture fails its capability; `verify` rules; disabled by property |
| `source/adapters/mrms/MrmsAdapterWireMockTest` | `start-after` listing and cursor, day rollover, 15-frame cap and the 6-hour skip, precip/flag pairing with decoded bands and the 4-minute fallback (`flagMissing`), `mbPerDay` advance refusal before any GET, 503 + `Retry-After` backoff, probe, silent format change, disabled products, descriptor |
| `source/adapters/goes/GoesAbiAdapterWireMockTest` | C13 filtering across channels, satellites and scan modes; `start-after` within an hour; request and byte accounting; "variable CMI missing"; disabled product; descriptor and key grammar |
| `source/adapters/mrms/MrmsIngestIT` | fixture → frame row (`20261012T120037Z`, `codeCounts` 0 = 8,174,561), `.fsg`, `raster-frame` record; the z=6 tile draws echoes; a tile outside coverage is transparent |
| `source/adapters/goes/GoesIngestIT` | fixture → `geos` frame row with coverage times and stats, `.fsg`, record; the z=6 tile renders (skips without the downloaded fixture) |
| `build/UnidataScopeBuildTest` | TestKit: another group from `unidata` fails, `edu.ucar` passes, other repositories are ignored; the real build's wiring and checksum file |
| `raster/RasterPipelineStatsTest` | extra stats merged without overriding |
| `ForesignalApplicationTempDirTest` | temp directories created at startup |
| `collect/BudgetGovernorTest` (added `noDoubleCount`) | Deviation 1 |
| `collect/UpstreamClientIT` (3 added) | advance charge refuses before sending and is released; relative target under `rasters/tmp`; a failed download releases its charge |
| `raster/FrameAgeHealthRuleTest` (added `disabledProduct`) | disabled product degrades with its reason |
| `retention/FamilyDefaultsTest` (extended) | the two new adapters under the `raster` class |

## Compile result

- `./gradlew compileJava compileTestJava checkUnidataScope`: `BUILD SUCCESSFUL`. NetCDF-Java resolves
  only from `unidata`, and `checkUnidataScope` prints 8 components, all `edu.ucar*`.
- `./gradlew lintOpenApi`: valid, 71 warnings (unchanged).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: 829 requests, all operations
  covered (collection unchanged).
- `./gradlew downloadTestFixtures`: downloaded and verified the GOES file.
- New dependencies:
  - from Unidata: `edu.ucar:cdm-core`, `grib`, `httpservices`, `udunits`, `libaec-jna` and `jj2000`,
    plus `edu.ucar.unidata:libaec-native`;
  - from Maven Central: JNA 5.19.1, Guava, jdom2, joda-time, protobuf-java, jcommander,
    commons-math3, jsr305, httpclient/httpcore/httpmime 4;
  - test: `gradleTestKit()`.

## Notes for the next prompt

1. **047+ (models):**
   - use `Grib2Decoder` with `DecodeRequest.grib2(…)` matched by parameter and level. It decodes
     5.3 and 5.42, but **only grid template 3.0**: HRRR's Lambert template 3.30 needs adding to
     `Grib2Decoder.latLonBox`'s caller (refused today with `RasterFormatException`);
   - range-read messages into `rasters/tmp` (`getRange`, or `download` with `expectedBytes`);
   - declare `DecoderDependent` requirements (`GRIB2_COMPLEX_PACKING` for GFS, `GRIB2_CCSDS` for
     ECMWF);
   - set `resample(bilinear)` on continuous fields.
2. **049 (SatDump geostationary):** `NetcdfDecoder`'s `geos` grid and `GeosGrid` outline are
   reusable. A full-disc grid crossing the antimeridian is not handled by `NetcdfDecoder.outline`
   yet; it takes min/max longitudes.
3. **050 (CORS and tile keys):** nothing new here.
4. **057 checkpoint:**
   - run the 046 tests, after `./gradlew downloadTestFixtures` so the GOES cases run;
   - the MRMS precip test decodes two 24.5 M-cell fields (a few hundred MB of heap; the test JVM
     has 2 GB);
   - every context now starts the decoder self-test thread at `ApplicationReadyEvent`. It decodes
     about 7 M cells and unpacks into `rasters/selftest`;
   - `UnidataScopeBuildTest` starts TestKit Gradle builds, which take tens of seconds;
   - the `mrms` and `goes-east-abi` sources and their 4 products now exist in every context
     (disabled), alongside the 14 external products.
5. **143:** confirm the libaec extraction under `/data/tmp` in the container (Issue 5) and the
   download volumes (§9).
6. **144 (Flyway):** no new tables or columns.
7. **Owner decisions pending:** Issues 1–5 above, plus those carried from 012–045.
