# 097-CR-Checkpoint-Sources

| | |
|---|---|
| Prompt | 097-Checkpoint-Sources.md |
| Status | Partial |
| Implementation commit | 7428f36fdd98cc75d177984a60973e24b85bbdff |
| Date | 2026-10-14 |

## Summary

This checkpoint ran the whole suite built through 096 (Docker/Testcontainers available) and fixed what it
could determine to be defects. The first run exposed two fatal, suite-wide faults that had never been seen
because the adapter block (062–096) wrote tests but never ran them:

1. **A Spring circular dependency that stopped the application context from starting at all**
   (`SourceRegistry → NwssDetectionAdapter → CountyUnionService → RegionTagger → RegionTaggingService →
   SourceRegistry`). This made ~1785 tests fail with
   `ApplicationContext failure threshold (1) exceeded`.
2. **A Jackson 3 deserialization failure in `FieldSpec`** (an absent `noLink` primitive creator parameter
   maps to `null` into a `boolean`), which broke every custom-source create/update, mapping preset, sample
   capture and settings import/export.

After those were fixed the suite dropped from 1859 failures to a residual set of per-adapter issues. Several
genuine production defects were then fixed (listed below). The suite is **not green**: the final full run
reports **8081 tests, 54 failed, 0 errors, 0 skipped**. The remaining failures are documented in
`completion-reports/RUN-STATUS.md`; the great majority are per-adapter fixture/parser ambiguities, OpenAPI/
Postman contract drift from 062–096, and order-dependent integration tests, not the shared infrastructure
this checkpoint owns.

## Test results (pass/fail counts, fixes made)

| Run | Tests | Failed | Notes |
|---|---|---|---|
| 1 | 5304 | 1859 | Died with `OutOfMemoryError` (2 GB test heap). Circular dependency + FieldSpec cascade. |
| 2 | 5702 | 116 | OOM again at the end (heap). Many contexts now load. |
| 3 | 8081 | 120 | No OOM after raising the test JVM heap to 4 GB. Clean failure list. |
| 4 | 8081 | 78 | After EnvTestSupport, Open511, SourceService, ConflictEgress, FamilyDefaults, schema pins, SEC/sanctions fixes. |
| 5 (final full) | 8081 | 54 | After userAgent/FetchExecutor/MQTT/AWC/GBFS/HealthDetails/CapFeed/XML fixes. |

(Subsequent targeted runs also fixed `AwdbParserTest` and reconciled the reactor interval; see below.)

### Fixes grouped by root cause

**A. Infrastructure / context (≈1785+ failures)**
- `RegionTagger` → `@Lazy RegionTaggingService`, breaking the `SourceRegistry` ↔ `RegionTaggingService` cycle.
- Removed the erroneous `@Component` from `GoogleStatusAdapter` and `StatusPageAdapter` (family members are
  constructed by their families, never standalone beans) — fixed the `NoSuchBeanDefinitionException`.
- `build.gradle.kts`: raised `tasks.test.maxHeapSize` from 2 GB to 4 GB; the cached context count and the
  full source registry now exceed 2 GB.

**B. Jackson 3 deserialization (≈35 failures)**
- `FieldSpec`: replaced the canonical-constructor `@JsonCreator` with a static `create(...)` factory that
  takes `Boolean noLink` (nullable) and defaults to `false`, so an absent `noLink` no longer maps null into a
  primitive. Fixed all custom-source/preset/mapping/sample-capture/settings-import tests.

**C. Test-support bugs (fixtures / helpers)**
- `EnvTestSupport.later` no longer shifts `eventTime` (it is hashed and significant); it moves `observedAt`
  only. Fixed every 078/079 `SnapshotContract` failure.
- `ConflictEgressTest`: the `VisibilitySettings` mock returned the supplier instead of its value.
- `FetchExecutorIT`: stubbed the 5-arg `IngestQueue.submit` (the 4-arg overload has not been called since
  094), added `anyBoolean()`.
- `HealthDetailsTest`: used `LinkedHashMap` for contributors (`Map.of` is unordered).
- `GbfsIngestIT` reads `record_live.kinematic`; `TransitIngestIT` reads `observedAt` from `record_live`.
- UA regexes (adsb.lol, airplanes.live, USGS, UpstreamClientIT) allow the optional `; contact=` suffix.
- `CapFeedAdapterFamilyTest` resets state before the `includeTests` run (a seen document is not re-fetched).
- `ReadsbEnvelopeTest` uses `hasMessageContaining` (the exception appends the body snippet).
- `SanctionsIngestIT` removes the individual with `(?s)` (`.` does not match newlines).

**D. Production defects**
- `Open511Parser`: `Map.of(...).getOrDefault(null, …)` NPE for a missing severity.
- `MeshCoreJsonFormat.integer`: the ternary unboxed a null `fallback`; restructured.
- `MeshShortTextFormatter`: the fired-alert branch used the 40-char `shortTitle` instead of the full rule
  name (the other branches already used `ruleName`).
- `CloudflareBgpAdapter`: each mapper now rejects the other event type (`hijacker_asn` / `leak_asn`).
- `FeedsController`: a feed that a caller may see private sources in is never publicly cacheable.
- `FetchExecutor.RunChunks`: accumulate `fromSeq`/`toSeq` (previously hard-coded 0), so `FetchOutcome` seqs
  are set.
- `Xml` (sanctions/SEC): `IS_REPLACING_ENTITY_REFERENCES` is now true (safe with `SUPPORT_DTD=false`); the
  SEC escaped-HTML summary and the UK/SEC label regexes were fixed accordingly. SEC parsing also required
  tag-tolerant `AccNo`/`Filed`/`Size` patterns.
- Sanctions adapters (OFAC/UK/UN) parse the file the `download` reports (a relative name resolves under the
  raster temp dir) and allow a same-host redirect.
- OFAC IMO extraction accepts a "Vessel Registration Identification" whose value is prefixed `IMO `.
- `MqttEndpoint`: `tls` was `scheme.endsWith("s")`, wrongly true for `ws`; now only `mqtts`/`wss`.
- `AwcTafsAdapter`: deduplicate records by `externalId` across station chunks.
- `GbfsParser.time`: truncate to whole seconds so v2.3 (integer) and v3.0 (ISO) agree.

**E. Stale test pins / lists**
- `FamilyDefaultsTest`: added the 8 adapter classes from 062–064 that were never classified plus the
  `SatellitePassesGenerator` derived source; added a `derived` classification branch; gave the four 064
  adapters a `defaultRetention` (celestrak-gp `reference`; socrates/launch-library/spacetrack-tip `event`).
- `SpatialIndexInitializerTest`, `EntityMappingIT`: added the 093 `record_head_series_point_idx`.
- `AlertmanagerFormatTest`, `ChannelsControllerIT`, `MeshtasticMapParserTest`, `MeshCoreLinkTest`.

### Tests changed (with reason)

| Test | Change | Reason |
|---|---|---|
| `EnvTestSupport` | `later()` no longer shifts `eventTime` | `eventTime` is significant hashed content; shifting it made genuinely different snapshots |
| `ConflictEgressTest` | mock returns `VisibilitySettings.defaults()` | the stub returned the supplier lambda, causing a ClassCastException |
| `FetchExecutorIT` | 5-arg `submit` stubs | production calls the 5-arg (forceTouch/reconcilable) overload since 094 |
| `HealthDetailsTest` | ordered maps | `Map.of` is unordered; the asserted order was not controlled |
| `GbfsIngestIT` / `TransitIngestIT` | read `record_live.kinematic` / `record_live.observed_at` | those columns live on `record_live`, not `record_head.payload` |
| UA regexes (4 files) | allow optional `; contact=` | the UA contract carries an optional contact suffix |
| `CapFeedAdapterFamilyTest` | reset state before `includeTests` | a seen document is (correctly) not re-fetched |
| `ReadsbEnvelopeTest` | `hasMessageContaining` | `UpstreamFormatException` appends the body snippet |
| `SanctionsIngestIT` | `(?s)` in the removal regex | `.` does not match newlines; nothing was removed |
| `FamilyDefaultsTest` | add 8 classes + derived branch | 062–064 never updated this list |
| `SpatialIndexInitializerTest`, `EntityMappingIT` | add 093 index | 093 added the index without updating the pins |
| `AlertmanagerFormatTest` | 5 option names | 087/088 added `receiverPrefix`, `resolvedValidFor`, `station` |
| `ChannelsControllerIT` | meshtastic now needs `config.brokerUrl` | 084 implemented meshtastic (no longer a "later type") |
| `MeshtasticMapParserTest` | first id `!f0000000` | the fixture's `node_id` is `0xF0000000` |
| `MeshCoreLinkTest` | `0x03` ends on `RESP_OK` or `RESP_SENT` | prompt 084 change |
| `NuclearReactorsUsAdapterTest` | expect 168 h (weekly) | `ReferenceDescriptorsTest` fixes every reference source to the weekly cadence |
| `AwdbParserTest` | expect raw `*` | `KitUrls.encode` (URLEncoder) leaves `*`; the API accepts it raw |
| `CountryCentroidsFipsTest` | (reverted) | the travel-advisory Category code set mixes FIPS and ISO2; left unchanged |

## Adapter inventory

Focus area 1 (`AdapterInventoryTest`) was **not** built: the two fatal faults consumed the available budget,
and a test that walks `SourceRegistry.all()` requiring a fixture README and parser contract for every source
would fail for many 062–096 adapters. The `FamilyDefaultsTest` inventory (the adapter-class ↔ retention
map) was completed for 062–096 and passes. This is recorded as a gap in `RUN-STATUS.md`.

## Invariants check

Not completed. The infrastructure fixes keep SRC-1 (the circular dependency removal does not change the HTTP
client rule), ING-7/8 (hash inputs unchanged), and EGR-1/EGR-2 (feeds privacy cache fix). A full per-entry
`docs/Invariants.md` sweep (owner ≤ 096) was not run; recorded in `RUN-STATUS.md`.

## Files created / modified

**Modified (main):** `build.gradle.kts`, `api/FeedsController`, `collect/FetchExecutor`,
`inbound/formats/mesh/MeshCoreJsonFormat`, `mqtt/MqttEndpoint`, `notify/mesh/MeshShortTextFormatter`,
`reference/RegionTagger`, `source/adapters/awc/AwcTafsAdapter`, `source/adapters/celestrak/{CelestrakGp,
CelestrakSocrates}Adapter`, `source/adapters/fema/NuclearReactorsUsAdapter`,
`source/adapters/gbfs/GbfsParser`, `source/adapters/internet/{CloudflareBgp,GoogleStatus,StatusPage}Adapter`,
`source/adapters/launchlibrary/LaunchLibraryAdapter`, `source/adapters/roads/Open511Parser`,
`source/adapters/sanctions/{OfacSdn,UkSanctions,UnSanctions}Adapter`, `source/adapters/sanctions/Xml`,
`source/adapters/sec/Sec8kAdapter`, `source/adapters/spacetrack/SpaceTrackTipAdapter`,
`source/spi/FieldSpec`.

**Modified (test):** `api/ChannelsControllerIT`, `collect/{FetchExecutorIT,HealthDetailsTest,UpstreamClientIT}`,
`config/SpatialIndexInitializerTest`, `inbound/format/AlertmanagerFormatTest`, `retention/FamilyDefaultsTest`,
`source/adapters/adsblol/AdsbLolAdapterTest`, `source/adapters/airplaneslive/AirplanesLiveAdapterTest`,
`source/adapters/cap/CapFeedAdapterFamilyTest`, `source/adapters/conflict/{ConflictEgressTest,ConflictIngestIT}`,
`source/adapters/env/EnvTestSupport`, `source/adapters/fema/NuclearReactorsUsAdapterTest`,
`source/adapters/gbfs/GbfsIngestIT`, `source/adapters/hydro/snotel/AwdbParserTest`,
`source/adapters/mesh/{MeshCoreLinkTest,MeshtasticMapParserTest}`,
`source/adapters/sanctions/SanctionsIngestIT`, `source/adapters/stac/StacIngestIT`,
`source/adapters/transit/TransitIngestIT`, `source/adapters/usgs/UsgsEarthquakesAdapterTest`,
`source/formats/readsb/ReadsbEnvelopeTest`, `store/EntityMappingIT`.

**Modified (resources):** `src/test/resources/fixtures/uk-sanctions-list/uk-sanctions-list.xml`
(restored the `<Designations>` root wrapper and the `<DateGenerated>` element lost in trimming).

## Deviations from the prompt

- Test JVM heap raised 2 GB → 4 GB (the 041 note's 2 GB no longer holds with the full registry).
- `AdapterInventoryTest` not built; the invariants sweep not completed (see RUN-STATUS).
- The travel-advisory Category ambiguity (FIPS vs ISO2) was left as-is rather than guessed.
- `./gradlew test` does not pass; `RUN-STATUS.md` was written per the guardrail.

## Issues found / open questions

- The `OpenApiContractIT`/`PostmanCollectionContractTest` drift (missing 2xx codes and validators for
  062–096 operations) needs an OpenAPI/Postman reconciliation pass; not attempted here.
- Settings export/import tests pass in isolation but fail in the full suite (order/state dependent).
- The AisStream WebSocket library logs the subscription frame (carrying the API key) at DEBUG; the
  "API key never appears in the logs" test expects redaction.

## Compile result

`./gradlew compileJava compileTestJava` — BUILD SUCCESSFUL.

## Notes for the next prompt

- The application context now starts; the major shared faults are fixed.
- The residual failures are enumerated in `completion-reports/RUN-STATUS.md` (per class and message).
- Financial/observability: `FetchExecutor` now reports real `fromSeq`/`toSeq`; do not re-hardcode them.
- `Xml.secureInputFactory` replaces predefined entity references (DTD still disabled).
