# 008-CR-Ingest-Normalizer-And-Policy-Engine

| | |
|---|---|
| Prompt | 008-Ingest-Normalizer-And-Policy-Engine.md |
| Status | Complete |
| Implementation commit | ba232384f248d3b9649e7338de58206087dbb88d |
| Date | 2026-10-11 |

## Summary

This prompt builds the pure core of ingest. Everything in it is deterministic code with no database
access, so prompt 009's writer only has to apply it to SQL.

**Descriptor and record extras (Gap-Analysis §8.A items 1–2).**
- `SourceDescriptor` gained these fields, each with a builder method and a default:
  - `titleField`
  - `entityKeys`
  - `fieldSpecs` (new `FieldSpec`, `FieldType`)
  - `kindDisplay` (new `KindDisplay`)
  - `aggBelowZoom`
  - `startField` / `endField`
  - `privateSource` (JSON `private`)
  - `privatePrecisionDecimals` (default 4)
- `DescriptorValidator` checks each new field:
  - key-type pattern
  - top-level names
  - lexical UCUM units
  - datum only on vertical fields
  - spec kinds ⊆ source kinds
  - `#RRGGBB` color
  - 15- or 20-character SIDC
  - zoom 0–22
  - start and end set together
  - decimals 2–7
- A private source that declares `embedFields` gets a warning, not a problem.
- `NormalizedRecord` gained `validFrom`, after `validUntil`, plus its builder method.

**Ingest types (`ai.foresignal.ingest`).**
- Inputs and commands:
  - `IngestRecord`, `FetchLogEntry`
  - the sealed `IngestCommand` = `IngestBatch | HeadPurge`, with `PurgeSpec` and `PurgeOutcome`
- Decisions and flags:
  - `PersistReason` with stored codes 1–9 (`code()`, `fromCode`)
  - `Outcome`, including `SKIP` and `TOUCH_THROTTLED`
  - `Decision`
  - `QaFlags`
- Results:
  - `RejectionReason`, `Rejection`
  - `IngestOutcome`, now in its final shape: all nine reasons always present, plus `versionBytes` and `rejections`. It replaces 006's interim record, and `HealthTracker` still stores `persisted` as `last_records_changed`.

**`Normalizer` (pure).** It applies the prompt's ten steps:
- identity
- time base
- clamp, plus the 1970-01-02 floor
- limits, with null map values dropped recursively and payloads converted to plain JSON types by `JsonValues`
- geometry: SRID 4326, forced to 2D, a range check, `GeometryFixer`, null island
- the private precision cap through `GeometryPrecisionReducer`
- quality per §14
- validity: `VALID_RANGE_INVERTED`, the TRACK explicit `validUntil` dropped and counted, effective `validUntil` from the anchor
- tombstones
- the derived search text, title and entity keys

Accepted observations are stable-sorted by `(externalId, observedAt, arrival)`, and duplicates are
kept. A rejection never fails the batch.

**Hashing.** `CanonicalJson` sorts keys recursively and writes numbers in plain form (`5` ≡ `5.0` ≡
`5.0f`). `ContentHasher` provides:
- `fullHash`
- `staticHash`
- `recipe` (`HASHER_VERSION = 1`, the first 4 bytes of SHA-256)
- `kinematicSubset`
- WKT-style geometry text at 1e-7, written by its own deterministic writer

A hash is the first 8 bytes of SHA-256, as a signed `long`.

**Profiles.**
- `SourceIngestProfile` is immutable. It holds the keyframe (the policy's, else
  `ttl − poll − 30 s`, else none), the window (TRACK `retention.trackWindow`; STANDARD the effective
  retention, else `retention.default`), `retentionKeyframe`, `effectiveKeyframe`, `touchMin`, the
  anchor and the load paths.
- `SourceIngestProfiles` is a cache bean that evicts on `SourceSettingsChangedEvent`,
  `SourceRegistryChangedEvent` and the retention `AppSettingChangedEvent`.
- `RetentionSettings` gained `trackWindow`, default `3d`.

**PersistGate SPI.**
- Types: `PersistGate`, `GateSnapshot` (with `EMPTY`), `GateSpec`, `GateState`.
- `NoopPersistGate` is the default `@Component` and holds its snapshot in an `AtomicReference`.

**`IngestPolicyEngine` (pure).**
- `prepare`: TRACK carry-forward with `POSITION_STALE`, applied before hashing.
- `gateFlip`: compares the persisted head state with the candidate.
- `decide`: Core-Data-Model §5.2 row for row, first match wins. The class Javadoc and inline
  comments carry the row numbers. LATEST is SAMPLED without row 10.
- `deviationMeters`: dead reckoning through the new `DeadReckoning` (great-circle destination, then
  haversine, with the antimeridian normalized), falling back to plain haversine, and 0 without
  geometry.
- `thresholdExceeded`: uses `FieldThreshold.exceeded`.
- The Javadoc notes that the LATE rule supersedes Gap-Analysis's out-of-order guard.

**`EntityState`** has a head part and a live part, and implements the §6.2 transitions for steps
10–12:
- `afterPersist`: kinematic = the subset of the persisted payload, and `dirty` clears.
- `afterLive`: kinematic = the incoming kinematic keys, plus JSON null for keys that dropped, and
  `dirty` is set.
- `afterTouch`.
- `apply(decision, …)` dispatches to the right one.
- `currentPayload()` mirrors `jsonb_strip_nulls(h.payload || l.kinematic)`.

**Shared hooks.**
- `TitleResolver`: `titleField`, else `title` > `name` > `callsign` > `place`; trimmed, at most 256
  characters.
- `EntityKeyExtractor` and its default, `DescriptorEntityKeyExtractor`, normalize the values:
  - `icao24`: 6 lowercase hex characters, with readsb `~` addresses dropped
  - `mmsi`: 9 digits
  - `imo`: 7 digits, with the `IMO` prefix stripped
  - `registration` and `callsign`: uppercase
- `EntityKeyListener`.

**GeoJSON reader replacement.**
- `ai.foresignal.source.formats.geojson.GeoJsonReader` is a Jackson 3 tree reader. It supports all
  seven geometry types, unwraps Feature and FeatureCollection, counts vertices before construction,
  requires finite ordinates, bounds collection nesting, and reports errors as `InvalidInputException`.
- `Geo.fromGeoJson` now uses it.
- `jts-io-common` is removed from the build and the version catalog. `./gradlew dependencies` shows
  0 matches for `json-simple` and `jts-io`. `FetchResult.unchanged()` was already the only 304
  factory in use.

**Test support.** `testsupport/ingest/SnapshotContract.assertStableStaticHash(descriptor, snapshots)`.

## Files created / modified

**Created (main)**
- `ingest/`:
  - `CanonicalJson`, `ContentHasher`, `DeadReckoning`, `Decision`
  - `DescriptorEntityKeyExtractor`, `EntityKeyExtractor`, `EntityKeyListener`, `EntityState`
  - `FetchLogEntry`, `GateSnapshot`, `GateSpec`, `GateState`
  - `HeadPurge`, `IngestBatch`, `IngestCommand`, `IngestPolicyEngine`, `IngestRecord`
  - `JsonValues`, `NoopPersistGate`, `NormalizedBatch`, `Normalizer`, `Observation`, `Outcome`
  - `PersistGate`, `PersistReason`, `PreparedObservation`, `PurgeOutcome`, `PurgeSpec`, `QaFlags`
  - `Rejection`, `RejectionReason`, `SourceIngestProfile`, `SourceIngestProfiles`, `TitleResolver`
- `source/spi/`: `FieldSpec`, `FieldType`, `KindDisplay`
- `source/formats/geojson/`: `GeoJsonReader`, `package-info`

**Modified (main)**
- `ingest/IngestOutcome` (final shape), `ingest/package-info` (time contract, decision table, no database)
- `source/spi/SourceDescriptor`, `source/spi/NormalizedRecord`, `source/spi/Geo`
- `source/DescriptorValidator`
- `settings/RetentionSettings` (`trackWindow`)
- `build.gradle.kts`, `gradle/libs.versions.toml` (`jts-io-common` and the `jts` version removed)
- `docs/OpenAPI.json`: the description of `IngestOutcome.rejections[].reason` and one changelog entry

**Tests (new)**
- `ingest/`:
  - `IngestFixtures`, `IngestPolicyEngineTest`, `DeadReckoningTest`, `NormalizerTest`
  - `ContentHasherTest`, `EntityStateTest`, `SourceIngestProfileTest`, `TitleResolverTest`
  - `EntityKeyExtractorTest`, `TimeContractTest`, `SourceIngestProfilesTest`, `IngestTypesTest`
- `source/DescriptorValidatorTest`
- `source/formats/geojson/GeoJsonReaderTest`
- `testsupport/ingest/SnapshotContract`, `SnapshotContractTest`

**Tests (updated)**
- `collect/HealthTrackerIT`: `IngestOutcome` arity
- `settings/DurationShorthandModuleTest`, `RetentionAndCorsSettingsTest`, `JacksonDurationIT`,
  `AppSettingServiceIT`: `RetentionSettings` arity. `JacksonDurationIT`'s exact JSON now ends with
  `"trackWindow":"3d"`.

No Flyway, Liquibase or SQL files. Postman was regenerated
(`python3 scripts/generate-postman.py docs/OpenAPI.json postman`) and its output was unchanged.

## Deviations from the prompt

1. **`IngestOutcome` keeps prompt 006's `clamped` field.** `UsageMeter` needs it, and the outcome
   is the natural carrier. `byReason` is keyed by `PersistReason` and always holds all nine reasons.
   `rejections` reuses `Rejection(index, externalId, reason, detail)`; `detail` is for logs only.
   The OpenAPI `IngestOutcome` has no `clamped`, so whoever serializes it (013/024) should leave it
   out or add it to the spec.
2. **The normalizer counts but does not emit metrics,** because it is pure. `NormalizedBatch`
   carries `clamped` and `validUntilIgnored`, and the writer increments
   `foresignal.ingest.records.clamped` and `…valid_until_ignored`.
3. **Tombstones are normalized before the payload and geometry steps** (step 9 moved ahead of steps
   4–6). Otherwise a tombstone could be rejected for content it is about to discard. Observer and
   quality checks still apply to tombstones.
4. **`loadPaths` and `needsPersistedGeometry` are methods that take a `GateSnapshot`,** not stored
   profile fields. The gate snapshot changes with trigger rules independently of the profile cache.
   `basePaths()` is the motion ∪ threshold part.
5. **`KindDisplay`'s Java accessor is `color()`,** to keep code in US English. It is mapped with
   `@JsonProperty("colour")`, so the published wire key stays unchanged.
6. **The builder method for `startField`/`endField` is `intervalFields(start, end)`.** Overloading
   `interval(String, String)` against the existing `interval(Duration, Duration)` would make
   `interval(null, null)` ambiguous.
7. **`FieldSpec` has only the six fields the prompt lists.** The spec's extra `FieldSpec` fields
   (`unitLabel`, `format`, `enumLabels`, `noLink`) and `KindDisplay` fields (`rotateField`,
   `sizeField`, `colorField`) are left to the prompts that own them.
8. **Interpretation of "datum only for vertical fields":** the datum must be non-blank, the type
   must be NUMBER or INTEGER, and the unit, if any, must be a UCUM length unit. The UCUM check is
   lexical (printable ASCII, 1–32 characters), not the full UCUM grammar.
9. **OpenAPI was reconciled, not extended.** `FieldSpec`, `KindDisplay` and every new
   `SourceDescriptor` property were already in the spec. The only amendment lists the rejection
   reason codes in the description of `IngestOutcome.rejections[].reason`; it is description only,
   recorded in `info.x-foresignal-changelog`, and `info.version` stays 1.3.0.
10. **`Geo.fromGeoJson` still throws `UpstreamFormatException`.** It wraps the reader's
    `InvalidInputException`, so the adapter contract and `GeoTest` stay unchanged. A
    FeatureCollection becomes a GeometryCollection.
11. **There are more rejection reasons than the prompt names,** one per limit and failure, listed
    in `RejectionReason`: `EXTERNAL_ID_INVALID`, `UNKNOWN_KIND`, `OBSERVED_AT_TOO_OLD`,
    `PAYLOAD_*`, `TOO_MANY_OBSERVERS`, `GEOMETRY_TOO_MANY_VERTICES`, `GEOMETRY_WRONG_SRID`,
    `COORDINATES_OUT_OF_RANGE`. Payload values with no JSON form are rejected as
    `PAYLOAD_UNSUPPORTED_TYPE`. Enums, `java.time` values, UUIDs and URIs become strings. String
    length is measured in UTF-8 bytes.
12. **`EntityState.afterPersist` takes a `seq`.** The writer passes the allocated seq, or a
    provisional one it remaps after step 7. `version` increments from `head.version`.
    `TOUCH_THROTTLED`, `LATE` and `SKIP` leave the state unchanged.
13. **More tests were written than the prompt lists:**
    - `GeoJsonReaderTest`
    - `TimeContractTest`, which is the test `docs/Invariants.md` names for ING-4 (owner 008)
    - `SourceIngestProfilesTest`
    - `IngestTypesTest`
    - `SnapshotContractTest`

    They are not in the sanctioned set and were not run. `DescriptorValidatorTest` did not exist
    before, so it was created rather than extended.

## Issues found / open questions

1. **The normalizer and the validator disagree on dot paths in `searchFields`.** The prompt says
   search text allows dot paths, and the normalizer supports them. The existing
   `DescriptorValidator` rejects dotted `searchFields` as "not a top-level key". I left the
   validator unchanged, so dot paths currently can't reach the normalizer from a valid descriptor.
   The owner should decide which one changes.
2. **`HealthService.TRACK_WINDOW` is still a 3-day constant** (006 deviation 8). It could now read
   `RetentionSettings.trackWindow()`. I left it for 019, as 006 planned.
3. **`FetchLogEntry.licence` keeps the stored spelling** to mirror the `fetch_log.licence` column and
   the descriptor accessor. Prose uses "license".
4. **Gap-Analysis §8.A item 2's out-of-order guard is superseded** by the LATE rule, as the prompt
   directs. The `IngestPolicyEngine` Javadoc says so.

No instructions were found in data or tool output.

## Test results

Sanctioned narrow run (`docs/Core-Data-Model.md` §17), exactly the listed classes, run once:

```
./gradlew test --tests "*NormalizerTest" --tests "*ContentHasherTest" --tests "*IngestPolicyEngineTest" --tests "*EntityStateTest" --tests "*DeadReckoningTest" --tests "*SourceIngestProfileTest" --tests "*DescriptorValidatorTest" --tests "*EntityKeyExtractorTest" --tests "*TitleResolverTest"
```

**Result: 216 tests, 0 failures, 0 errors, 0 skipped. `BUILD SUCCESSFUL`.** Every test passed on
the first run, so no production or test code needed fixing.

| Class (including nested) | Tests | Failures |
|---|---|---|
| `IngestPolicyEngineTest` (36-case §5.2 matrix, 12 rows × firing/boundary/near-miss, plus details) | 67 | 0 |
| `NormalizerTest` | 42 | 0 |
| `DescriptorValidatorTest` | 33 | 0 |
| `ContentHasherTest` | 24 | 0 |
| `SourceIngestProfileTest` | 22 | 0 |
| `DeadReckoningTest` | 10 | 0 |
| `EntityStateTest` | 7 | 0 |
| `TitleResolverTest` | 7 | 0 |
| `EntityKeyExtractorTest` | 4 | 0 |

No other tests were run. The run included no `*IT` classes, so it needed no Testcontainers.

## Compile result

```
$ ./gradlew compileJava compileTestJava
BUILD SUCCESSFUL in 662ms
```

## Notes for the next prompt

1. **Per batch (009):**
   - Get the profile with `SourceIngestProfiles.get(sourceId)`.
   - Read `PersistGate.snapshot()` once.
   - Run `new Normalizer()` (or `new Normalizer(extractorBean)`) and `normalize(profile, records)`.
   - Emit `batch.clamped()` and `batch.validUntilIgnored()` as metrics, and the rejections into
     `IngestOutcome`.
2. **Load query:**
   - Select `h.payload #> path` for each entry of `profile.loadPaths(snapshot)`. Paths are dot
     separated, so split them on `.` for the `text[]` argument.
   - Select `ST_AsBinary(h.persisted_geom)` only when `profile.needsPersistedGeometry(snapshot)`.
   - Select `ST_AsBinary(l.geom)` only when `profile.isTrack()`.
   - Build `EntityState.Head`: `persistedPathValues`, with null for absent paths;
     `persistedPayload = null`.
   - Build `EntityState.Live`: `kinematic` may contain JSON nulls; keep them.
3. **Fold:** for each observation, run:
   - `prepare`
   - `gateFlip(prepared, state, profile, snapshot)`
   - `decide`
   - `state.apply(decision, prepared, profile, provisionalSeq)`
   Then write:
   - PERSIST: version row plus head. `version = state.head().version()` after apply.
   - LIVE: live row.
   - TOUCH: touch.
   - `TOUCH_THROTTLED`, `LATE`, `SKIP`: nothing.
4. **Column sources:**
   - `qa_flags` = `Observation.qaFlags()`; `persist_reason` = `PersistReason.code()`.
   - `record_head`: `title` ← `Observation.title()`, `search_text` ← `searchText()`,
     `valid_until_explicit` ← `validUntilExplicit()`, `valid_from` ← `validFrom()`.
   - `record_live`: `valid_until` ← `validUntil()`, `full_hash` ← `PreparedObservation.fullHash()`.
   - `record_head.static_hash` ← `staticHash()`, `hash_recipe` ← `profile.recipe()`.
5. **Payload storage.** Payloads hold only JSON types (`Map`, `List`, `String`, `Boolean`, `Long`,
   `Double`, `BigInteger`, `BigDecimal`), so Jackson serializes them exactly as they were hashed.
6. **`IngestOutcome`:** pass an `EnumMap` for `byReason`; missing reasons become 0. `versionBytes`
   = UTF-8 length of payload + quality + observer JSON + 120.
7. **Entity keys:** after commit, call every `EntityKeyListener` bean for persisted entities whose
   `Observation.entityKeys()` is non-empty.
8. **Lazy recipe migration:** use `ContentHasher.staticHash(kind, eventTime, validUntilExplicit,
   validFrom, tombstone, geomIsNull, payload, volatile, significant)`, the `fullHash` overload and
   `kinematicSubset` with the new profile's sets.
