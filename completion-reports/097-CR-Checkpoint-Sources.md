# 097-CR-Checkpoint-Sources

| | |
|---|---|
| Prompt | 097-Checkpoint-Sources.md |
| Status | **Complete — `./gradlew test` green** |
| Implementation commit | `7428f36fdd98cc75d177984a60973e24b85bbdff` |
| Final fixes | 19 commits `db69908 … 23b032c` (see "Commits") |
| Date | 2026-10-14 |

## Summary

This checkpoint ran the whole suite built through 096 (Docker/Testcontainers available) and fixed
every failure. The first run exposed two fatal, suite-wide faults that had never been seen because the
adapter block (062–096) wrote tests but never ran them:

1. **A Spring circular dependency that stopped the application context from starting at all**
   (`SourceRegistry → NwssDetectionAdapter → CountyUnionService → RegionTagger → RegionTaggingService →
   SourceRegistry`). This made ~1785 tests fail with `ApplicationContext failure threshold (1) exceeded`.
2. **A Jackson 3 deserialization failure in `FieldSpec`** (an absent `noLink` primitive creator
   parameter maps to `null` into a `boolean`), which broke every custom-source create/update, mapping
   preset, sample capture and settings import/export.

After those were fixed the suite dropped from 1859 failures to a residual set of per-adapter issues,
which this run then worked through. The final full run is **8084 tests, 0 failed, 0 errors, 0 skipped**.
`AdapterInventoryTest` (focus area 1) and `FamilyDefaultsTest` both exist and pass. Every
`docs/Invariants.md` entry whose owner prompt is ≤ 096 names a test class that exists.

## Test results (pass/fail counts per run)

| Run | Tests | Failed | Notes |
|---|---|---|---|
| 1 | 5304 | 1859 | Died with `OutOfMemoryError` (2 GB test heap). Circular dependency + FieldSpec cascade. |
| 2 | 5702 | 116 | OOM again at the end (heap). Many contexts now load. |
| 3 | 8081 | 120 | No OOM after raising the test JVM heap to 4 GB. Clean failure list. |
| 4 | 8081 | 78 | After EnvTestSupport, Open511, SourceService, ConflictEgress, FamilyDefaults, schema pins, SEC/sanctions fixes. |
| 5 (checkpoint full) | 8081 | 54 | After userAgent/FetchExecutor/MQTT/AWC/GBFS/HealthDetails/CapFeed/XML fixes. |
| 6 (this run, first full) | 8082 | 3 | After the 52 residual fixes below; the 3 remaining were non-deterministic order assumptions. |
| 7 (final full) | **8084** | **0** | After the descriptor-order fix, the GDACS request-order fix and `AdapterInventoryTest`. |

## Fixes grouped by root cause

### A. Infrastructure / context (≈1785+ failures)
- `RegionTagger` → `@Lazy RegionTaggingService`, breaking the `SourceRegistry` ↔ `RegionTaggingService` cycle.
- Removed the erroneous `@Component` from `GoogleStatusAdapter` and `StatusPageAdapter`.
- `build.gradle.kts`: raised `tasks.test.maxHeapSize` from 2 GB to 4 GB.
- **`DatabaseCleaner` now invalidates the `AppSettingService` cache after truncating `app_setting`.** The
  cache is an in-memory copy of the truncated rows; a setting a previous test wrote (for example
  `security.openReads`) leaked into the next test in a shared context, which made settings export/import
  ITs and `SeriesIngestIT` fail only in a full-suite run.

### B. Jackson 3 deserialization (≈35 failures)
- `FieldSpec`: replaced the canonical-constructor `@JsonCreator` with a `create(...)` factory taking a
  nullable `Boolean noLink` defaulting to `false`.

### C. Settings export / import (4 + 1)
- `SourceMapper.wireParams` writes coerced parameters in their API wire form (`Envelope` →
  `minLon,minLat,maxLon,maxLat`, `URI`/`Path` → string, `Duration` → shorthand), and
  `SettingsTransferService.source` omits an empty `params`. An `Envelope` had serialised as a JSON bean
  (`{area:…}`), which the import then rejected. Fixed `ServerSettingsIT`, `LlmControllersIT`,
  `SettingsExportImportIT`, `BriefingsControllerIT`, and (with the cache invalidation) `SeriesIngestIT`.

### D. Travel-advisory FIPS vs ISO2 (2)
- Added `TravelAdvisoryCodes`, an explicit documented lookup: FIPS 10-4 base with overrides for the
  collisions the State Department API uses (`AS`→Australia, `YM`→Yemen, `CH`→China, `KH`→Cambodia) and
  the live-capture anomalies (`SR`→Switzerland, `UC`→Curaçao, `BI`, `GW`, `MF`, `MM`, `NA`, `NN`, `SV`).
  `CountryCentroids.byFips` (used by GDELT) is untouched.

### E. Per-adapter parser / fixture defects
- `GppdParser`: the CSV's literal `NULL` placeholders are treated as absent.
- `OurAirportsAdapter`: falls back to the descriptor's default `types` when the parameter is absent.
- `GcatParser`: a pad's parent comes from the `Site` column.
- `FdsnTextParser` test: the hand-written row was misaligned to its own header.
- `GdacsEventParser`: `country` is kept only when `iso3` is a real ISO3 (open-water descriptors dropped).
- `UsgsHansVolcanoAdapter`, `AtlasProbesAdapter`: a record without the upstream time base is skipped.
- `CctvPackParsersTest`: `Compass.normalize` folds headings without perturbing an in-range value.
- `AutoRxPayloadCodec`: the prompt-086 observer frequency is asserted (077 expected null).
- `AgsiAdapter` test: the shipped EU row is status N (skipped by design), so the test gives it data to
  exercise the "EU always kept" filter.
- `NifcPerimeters`/`FeatureGeometry`: a simplified geometry that rounds into an invalid ring is repaired
  with `GeometryFixer`.
- `SatellitePassesGenerator`: `round()` rounds the whole instant to the nearest 10 s; the derived IT
  accepts the horizon extending by one pass on the tick.
- `NhcCyclonesAdapter` test: reads the ETag from the persistent store the conditional GET writes to.
- `FireAndCycloneIngestIT`: the cone fixture uses `"advisnum": "25A"` with a space.

### F. Fixtures mangled by the earlier sanitisation pass
- **tsunami-warnings** `thread-2-update.xml`, `thread-3-cancellation.xml`, `thread-1-warning.xml`,
  `unrelated-next-event.xml`: restored the `<references>` elements and the real P-VTEC values that a
  sanitiser had replaced with `x0`/`x1` tokens.

### G. Wikistream
- `RecentChangeEvent`: the canary is a top-level `domain: "canary"`.
- `SurgeDetector`: banding is by edits (not editors), the phase rises while the count grows, and `sweep`
  recomputes the threshold after pruning so a fallen surge is tombstoned.

### H. Conflict / HTTP retry / auth (5)
- `ErcotAdapter`, `ReliefWebDisastersAdapter`: the client throws on 4xx, so the 401 re-fetch and the 403
  `APPNAME_NOT_APPROVED` mapping now catch `UpstreamHttpException`.
- `ConflictAdaptersTest`: a recording fetch is probe-like and stores no conditional-GET validator; a
  normal fetch does.
- `CopernicusEmsAdapter`: paging stops on a short page (a static `next` could loop forever) and sorts
  newest-first so the per-code detail is fetched only for changed activations.

### I. Streaming / socket (5)
- `application.yml`: `org.java_websocket: warn` — java-websocket logs every decoded frame at TRACE/DEBUG,
  including the aisstream subscription carrying the API key (invariant SRC-5).
- `EntityState.Live` carries `qa_flags` and the `record_live` TOUCH upsert writes them, so a
  `POSITION_STALE` carry-forward reaches the live row even on a touch.
- `LoopbackWebSocketServer.sendFragmentedBinary` sends a copied slice (the permessage-deflate path read
  the whole backing array, duplicating each fragment).

### J. OpenAPI / Postman contract drift (3)
- `ChannelsController.republishMqttChannel` and `WebSubController.receiveWebSubNotification` declare the
  202 `@ApiResponse` so springdoc generates the status the spec (and the runtime) use.
- `scripts/generate-postman.py`: the Telegram interactive example uses numeric chat ids; regenerated
  `postman/Foresignal.postman_collection.json`.
- `PostmanCollectionContractTest`: added the `mqtt`/`meshtastic`/`meshcore` types to the registry, added
  `previewMapping`/`suggestMapping` validators, and justified `testCustomSourceMapping` and
  `receiveWebSubNotification` as schema-only.

### K. Contract / inventory fixes
- `SourceDescriptor.kinds` preserves insertion order (`Collections.unmodifiableSet(LinkedHashSet)`)
  instead of `Set.copyOf`, whose iteration order is randomised per JVM (`ImmutableCollections.SALT`).
  This fixed `MapStyleServiceTest` and `LocalReceiversFamilyTest`.
- `SourcesResource` lists every visible source (a resource is read directly, not capped for an LLM).
- `docs/Agent-Guide.md`: added `get_series` to the tool table.
- `MeshReportService.resetRateLimiter()` so each `MeshReportIngestIT` starts with its per-minute budget.
- `TransitFeeds.problems` returns distinct problems; `TransitCatalogControllerIT` decodes its query
  parameters (MockMvc re-encodes a pre-encoded template).
- `CustomSourceService.validate` checks the effective policy before the descriptor, so a bad policy
  reports `INGEST_POLICY_INVALID` rather than `VALIDATION_FAILED`.
- `GdacsAdapterTest.requestOrder` reverses WireMock's newest-first events rather than sorting by
  millisecond `loggedDate` (ties were unstable).
- `UdpTransportIT`: the AIVDM datagram was longer than the transport's configured 32-byte cap.

## Tests changed (with reason)

| Test | Change | Reason |
|---|---|---|
| `UsTravelAdvisoryParserTest.noLevel` | expect `us:AU` | `AS` is FIPS Australia, not ISO2 American Samoa (live capture) |
| `CountryCentroidsFipsTest` | use `TravelAdvisoryCodes`, add collision test | the Category set is mixed FIPS/ISO2 |
| `OurAirportsParserTest` | `frequencyKhz` 115900 | the real OurAirports value is in kHz |
| `FdsnTextParserTest` | realign the row | the hand-written row did not match its own header |
| `AutoRxPayloadCodecTest` | observer `frequencyHz` 404500000 | prompt 086 sets it from `freq` |
| `AgsiAdapterTest` | give the EU row data | the shipped EU row is status N (skipped) |
| `NhcAdapterTest` | read the persistent state store | conditional GET validators live there |
| `FireAndCycloneIngestIT` | `"advisnum": "25A"` | the fixture has a space after the colon |
| `SatellitePassesDerivedIT` | expect 6 live passes | the 48 h horizon re-anchors on the tick |
| `VesselIngestIT.keyframe` | accept KEYFRAME or HEARTBEAT | an unchanged keyframe is a HEARTBEAT (§5.2 row 8) |
| `AisStreamIngestIT.sentinelCarriedForward` | advance past `touchMin` (200 s) | the sentinel does not change the payload, so the live row updates on the touch throttle |
| `MeshReportIngestIT` | (support) reset the rate limiter | the service singleton kept per-sender windows |
| `ShakeMapAdapterTest` | stub the other events' details 404 | the adapter correctly fetches each changed event's detail |
| `MqttChannelIT.healthAndSummary` | await live-or-retained, verify retention fresh, wait-then-clear the subscriber | the connect-time publish/subscribe race; the retained message otherwise looks like a re-publish |
| `UdpTransportIT` | shorter AIVDM datagram | the 47-byte message exceeded the 32-byte cap |
| `InatObservationsAdapterTest` | decode the URL before matching | the adapter URL-encodes the ISO instant |
| `EarthSearchAdapterTest` | index 0 (newest-first); `.181Z` | WireMock serve events are newest-first; `Instant.toString` trims micros |
| `ConflictAdaptersTest` | add a normal fetch before the 304 check | a recording fetch is probe-like |
| `TransitFamilyTest`/`TransitCatalogControllerIT` | (production fixes; test helper decodes params) | see root cause K |
| `PostmanCollectionContractTest` | registry types, validators, schema-only set | see root cause J |
| `GdacsAdapterTest` | reverse serve events | see root cause K |
| `EntityStateTest` | add the `qaFlags` argument | new `Live` field |
| `DatabaseCleanerIT` | pass an `AppSettingService` | new constructor argument |

## Adapter inventory

Focus area 1 is complete. **`AdapterInventoryTest`** (`@IntegrationTest`) walks `SourceRegistry.all()`
(every built-in and family member, ~380 sources) and asserts for every non-DERIVED, non-PUSH source:

- the descriptor is well formed (id, ≥ 1 kind, a licence; `system-*` excepted as Foresignal's own);
- a fixture directory `src/test/resources/fixtures/<source-id or family-id>/` exists with a `README.md`,
  or the source is a family member whose capture is the family's representative fixture or an inline
  WireMock body (`cctv-caltrans-d1..d12`, `cctv-tallinn`, `cctv-warendorf`, `feed-*`, `local-*`,
  `meteoalarm-<country>`, `ris-live`, `station-readsb-*`, `status-*`, `wxstation-*`);
- the adapter package has a parser/contract test by naming convention (`*ParserTest`, `*CodecTest`,
  `*MapperTest`, `*Adapter(s)Test`, `*FamilyTest`, `*SpecTest`, `*GeneratorTest`), except `system`,
  `acars` and `space.localsonde` whose codec/mapper tests are the shared `source/codec` and
  `source/formats` tests.

The 008 `fieldSpecs` contract and the `SnapshotContract` static-hash check are per adapter and live in
each package's `*ParserTest` (they call `FieldSpecContract.assertPayloadKeysDeclared` and
`SnapshotContract.assertStableStaticHash`); `AdapterInventoryTest.contractHelpersAreUsed` asserts those
helpers are exercised widely (≥ 20 and ≥ 10 test classes), since no generic per-adapter parser entry
point exists. `FamilyDefaultsTest` was re-verified and passes.

## Invariants check

Every `docs/Invariants.md` entry whose owner prompt is ≤ 096 (37 entries) names at least one test class,
and every named class exists in `src/test/java` (checked mechanically against the registry text). No
invariant had to be re-asserted or added. The infrastructure fixes keep SRC-1 (the `@Lazy` cycle break
does not change the HTTP client rule), ING-7/8 (hash inputs unchanged) and EGR-1/EGR-2 (feeds privacy).

## Deviations

- Test JVM heap raised 2 GB → 4 GB (the 041 note's 2 GB no longer holds with the full registry).
- `AdapterInventoryTest` asserts the fixture/contract inventory plus the helper-usage counts; the
  per-adapter `fieldSpecs`/`SnapshotContract` invocations remain in each `*ParserTest` rather than being
  reproduced generically (no per-adapter parser entry point is declared). The family members with no
  fixture directory of their own are listed explicitly.
- Fixtures mangled by the earlier sanitiser (tsunami thread files) were restored from `README.md`.
- The State Department `Category` code set is treated as the explicit `TravelAdvisoryCodes` table, not
  a single FIPS table.

## Commits

`db69908` settings wire form · `632fd01` travel advisory · `7e5b1c3` ourairports/gppd/gcat/fdsn ·
`ce0147a` gdacs/hans/atlas/cctv/autorx/agsi · `add7d86` wikistream · `b0875c8` satellite passes ·
`1fdf9cb` tsunami fixtures · `3733123` NHC · `9ed8dbc` inat/earthsearch · `bf889bb` ercot/cache ·
`0ab6188` reliefweb/copernicus · `23155ca` shakemap · `8417295` transit/custom-source ·
`4283292` mesh rate limiter · `41f6458` MCP resource/Agent Guide · `030a35b` streaming ·
`f03684d` MQTT test · `66c8f57` OpenAPI/Postman · `da8eb91` descriptor order/GDACS ·
`23b032c` AdapterInventoryTest.

## Compile result

`./gradlew compileJava compileTestJava` — BUILD SUCCESSFUL.

## Final full run

`./gradlew test` — **8084 tests, 0 failed, 0 errors, 0 skipped** (1155 test classes), BUILD SUCCESSFUL in
17m 26s.

## Notes for the next prompt

- The application context starts; the full suite is green.
- Financial/observability: `FetchExecutor` reports real `fromSeq`/`toSeq`; do not re-hardcode them.
- `record_live` TOUCH now also writes `qa_flags`; `EntityState.Live` carries them.
- `SourceDescriptor.kinds()` is insertion-ordered (deterministic) — do not revert to `Set.copyOf`.
- `org.java_websocket` logging is pinned to `warn` in `application.yml` (SRC-5).
- The tsunami thread fixtures carry their `<references>` and P-VTEC values; recapture with a key in 143.
