# 008 — Ingest Normalizer And Policy Engine

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/008-CR-Ingest-Normalizer-And-Policy-Engine.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–138).
- **Previous Completion Report:** read `completion-reports/007-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Core-Data-Model.md` §1 (I4, I6, I8), §2 (time contract), §3 (ingest input, limits, qa_flags, carry-forward, tombstones), §4 (hashing, recipe), §5 (policies: validation, **decision table**, defaults), §6 (`IngestOutcome` definitions, §6.2 step 11 kinematic rules), §11 (PersistGate), §14 (quality validation), and the **"007 Ingest Normalizer And Policy Engine"** bullets under *Per-prompt changes* (007 is this prompt; its "014" is prompt 017)
- `docs/Gap-Analysis.md` §8.A items 1 and 2, §3 row "GeoJSON `title`", §6 row "Field specs + display hints"
- Code from prompts 004–006: `source.spi.*` (`NormalizedRecord`, `SourceDescriptor`, `IngestPolicy`, `IngestPolicyValidator`, `FieldThreshold`, `MotionSpec`, `RecordLimits`), `SourceRegistry`, `DescriptorValidator`, `SourceService`, and any interim `ingest.IngestOutcome`

## Objective

Build the pure core of ingest: the types the writer consumes, the normalizer that enforces the time contract and limits, the hasher, the per-source ingest profile, and the `IngestPolicyEngine` that decides, per observation, whether to persist a version, update live state, touch, or drop. Everything here is deterministic code with no database access, so it can be unit-tested exhaustively before the writer (prompt 009) wires it to SQL. Also add the descriptor and record extras from Gap-Analysis §8.A items 1–2.

## Tasks

### 1. Descriptor and record extras (Gap-Analysis §8.A items 1–2)

Add to `SourceDescriptor` (record component, builder method, default) and validate in `DescriptorValidator`:

| Field | Type, default | Validation |
|---|---|---|
| `titleField` | String, null | top-level payload key name |
| `entityKeys` | `Map<String,String>` key type → top-level field, empty | key types match `^[a-z0-9]+(-[a-z0-9]+)*$` (e.g. `icao24`, `registration`, `mmsi`, `imo`, `callsign`) |
| `fieldSpecs` | `Map<String,FieldSpec>`, empty | keys are top-level names; `FieldSpec(FieldType type, String unit, String label, String description, String datum, Set<String> kinds)`; `FieldType` = STRING, INTEGER, NUMBER, BOOLEAN, TIMESTAMP, OBJECT, ARRAY; `unit` is a UCUM code (`m`, `km`, `[kn_i]`, `[ft_i]`, `deg`, `s`, `1`); `datum` (MSL, WGS84, AGL, PRESSURE, or free text) only for vertical fields; empty `kinds` = all kinds |
| `kindDisplay` | `Map<String,KindDisplay>`, empty | keys ∈ `kinds`; `KindDisplay(titleTemplate, icon, colour, sidc)`; colour `#RRGGBB`; optional `sidc` is a 15- or 20-character MIL-STD-2525 code |
| `aggBelowZoom` | Integer, null | 0–22; null = never aggregate (prompt 016) |
| `startField`, `endField` | String, null | top-level names; both or neither |
| `privateSource` (JSON `private`) | boolean, false | warning when `embedFields` is non-empty (prompt 039 enforces exclusion) |
| `privatePrecisionDecimals` | int, 4 (≈ 11 m) | 2–7; used only when private |

Add `Instant validFrom` (nullable) to `NormalizedRecord` and its builder, after `validUntil`; update every constructor call. A record with `validFrom > validUntil` is rejected (`VALID_RANGE_INVERTED`).

### 2. Ingest types (`ai.foresignal.ingest`), per Core-Data-Model §3 and §6

- `IngestRecord(NormalizedRecord record, Instant receivedAt, long arrival)`.
- `FetchLogEntry`: the provenance of one batch, filled by the collector (prompt 010): `sourceId, trigger (FetchTrigger), status (FetchStatus), startedAt, receivedAt, httpStatus, requestCount, urlRedacted, etag, bytes, bodySha256, adapterVersion, licence, principal, coverage, error`. The writer adds counts, `ingested_at` and seqs.
- Sealed `IngestCommand` = `IngestBatch(sourceId, FetchLogEntry log, List<IngestRecord> records, CompletableFuture<IngestOutcome> future)` | `HeadPurge(List<PurgeSpec> specs, CompletableFuture<PurgeOutcome> future)`; `PurgeSpec(sourceId, floorSeq, staleBefore)`; `PurgeOutcome(Map<String,Integer> headsDeleted)`.
- `PersistReason` with stored codes: FIRST 1, REAPPEAR 2, SIGNIFICANT 3, TOMBSTONE 4, CHANGE 5, SAMPLE 6, KEYFRAME 7, HEARTBEAT 8, TRIGGER_EDGE 9 (`code()`, `fromCode`). `Outcome` = PERSIST, LATE, SKIP, TOUCH, TOUCH_THROTTLED, LIVE. `Decision(Outcome, PersistReason)`.
- `QaFlags`: FUTURE_CLAMPED 1, GEOMETRY_FIXED 2, NULL_ISLAND 4, TIME_IMPUTED 8, POSITION_STALE 16.
- `IngestOutcome`: `seen, persisted, live, touched` (touch rows written), `touchesThrottled, late, skipped, rejected, fromSeq, toSeq, byReason` (all nine reasons, zeros included), `versionBytes`, `rejections` (`index, externalId, reason`). Replace any interim record from prompt 006 and update `HealthTracker` callers (`last_records_changed = persisted`).

### 3. `Normalizer` (pure)

`NormalizedBatch normalize(SourceIngestProfile p, List<IngestRecord> in)` returns accepted `Observation`s (stable-sorted by `(externalId, observedAt, arrival)`), `Rejection`s and the clamped count. **Duplicates are kept.** Per record, in order:

1. `externalId` non-blank and ≤ 256 chars; `kind` ∈ descriptor kinds.
2. **Time base:** `observedAtMode=RECEIVED` → `observed_at = received_at` plus `TIME_IMPUTED`, ignoring any adapter value. UPSTREAM with null `observedAt` → reject `MISSING_OBSERVED_AT`.
3. **Clamp:** `observed_at > received_at` → set to `received_at`; only if the excess exceeds `skewTolerance` set `FUTURE_CLAMPED` and count it. Reject `observed_at < 1970-01-02`.
4. **Limits** (`RecordLimits`): serialized payload ≤ 64 KB, depth ≤ 8, ≤ 512 keys in total, strings ≤ 8 KB, no NaN/Infinity, ≤ 8 observers, geometry ≤ 10,000 vertices **before** `GeometryFixer`. Drop null map values recursively (payloads carry no nulls).
5. **Geometry:** SRID 4326; force 2D (drop Z/M; vertical values belong in the payload); coordinates outside ±180/±90 → reject; invalid → `GeometryFixer` + `GEOMETRY_FIXED`, empty result → null; an exact (0,0) point → null + `NULL_ISLAND` unless `allowNullIsland`.
6. **Private precision cap** (applied before hashing): when the source is private, reduce geometry with `GeometryPrecisionReducer` at `privatePrecisionDecimals`.
7. **Quality** (§14): `accuracyMeters ≥ 0`, `confidence ∈ [0,1]`, `extra` keys match `^[a-z0-9]+\.[a-z0-9_]+$` with scalar values; else reject `QUALITY_INVALID`.
8. **Validity:** `valid_until_explicit` = the record's `validUntil`, forced to null on TRACK sources (count `foresignal.ingest.records.valid_until_ignored`); effective `valid_until = explicit ?? anchor + ttl` (null without TTL), where `anchor` is `observed_at` or `received_at` per `ttlAnchor`. Keep `validFrom`.
9. **Tombstones:** `geom = null`, `payload = {}`, kinematic `{}`, `kind` kept.
10. **Derived head fields:** `searchText` (lowercase join of `searchFields` values, dot-paths allowed, ≤ 1,000 chars); `title` via `TitleResolver` (below); `entityKeys` via `EntityKeyExtractor` (below).

Rejections never fail the batch; each carries a reason enum value.

### 4. `TitleResolver` and `EntityKeyExtractor` (shared hooks)

- `TitleResolver.resolve(profile, payload)`: `titleField` value if present, else the first present of `title`, `name`, `callsign`, `place`; stringified, trimmed, ≤ 256 chars; null otherwise. One resolver, reused later by tiles (016), MCP (025) and Ask (028). `kindDisplay.titleTemplate` is a client display hint and is not used here.
- `EntityKeyExtractor` interface with a default implementation reading `descriptor.entityKeys`: returns `Map<String,String>` (trimmed; `icao24`/`mmsi`/`imo` normalised to lowercase digits/hex, `registration`/`callsign` uppercase). Declare `EntityKeyListener { void onEntityKeys(String sourceId, String externalId, Map<String,String> keys); }`; prompt 009 calls listener beans after commit for persisted entities, prompt 098 implements one.

### 5. `ContentHasher` and canonical JSON (Core-Data-Model §4)

- Canonical JSON: keys sorted recursively; numbers in plain form with integral doubles rendered without `.0` (`5` ≡ `5.0`); deterministic across JVMs. Hash = first 8 bytes of SHA-256 as a signed `long`.
- `fullHash`: kind, event_time, explicit validUntil, validFrom, tombstone, geometry WKT at 1e-7 (after carry-forward), payload minus volatile keys.
- `staticHash`: the same scalars, `geom IS NULL` instead of the geometry, and only significant keys (all non-volatile keys when `significantFields` is empty).
- Never hashed: observed/received/ingested times, derived valid_until, quality, observers, searchText, title.
- `HASHER_VERSION = 1`; `recipe(volatile, significant)` = first 4 bytes of SHA-256 over (version, sorted volatile, sorted significant) as `int`.
- `kinematicSubset(payload)` = keys that are neither significant nor volatile (empty when `significantFields` is empty).

### 6. `SourceIngestProfile` and `SourceIngestProfiles`

Immutable profile per source: id, tier, observedAtMode, ttl, ttlAnchor, effective policy (`SourceService.effectiveIngestPolicy`), limits, skewTolerance, allowNullIsland, kinds, volatile, significant, motion, thresholds, searchFields, titleField, entityKeys, private + precision, recipe, and:
- `keyframe` = policy keyframe, else the ALL default `ttl − pollInterval − 30 s` (pollInterval 2 s for streams), else none.
- `window`: TRACK → `retention.trackWindow` (default 3d); STANDARD → `SourceService.effectiveRetention` (source setting, else descriptor `defaultRetention`, else `retention.default` 7d). If `RetentionSettings` has no `trackWindow` yet, add it with default `3d` (prompt 019 extends retention settings).
- `retentionKeyframe = window / 4`; `effectiveKeyframe = min(keyframe, window/4)`.
- `touchMin = clamp(ttl/10, 30 s, 1 h)`, or 1 h without a TTL.
- `loadPaths` = motion speed/course fields ∪ threshold fields ∪ gate paths (from the gate snapshot).

`SourceIngestProfiles` caches profiles and rebuilds on `SourceSettingsChangedEvent`, `SourceRegistryChangedEvent` and retention `AppSettingChangedEvent`.

### 7. `PersistGate` SPI (Core-Data-Model §11)

`PersistGate { GateSnapshot snapshot(); }` backed by an `AtomicReference<GateSnapshot>`; `GateSnapshot.spec(sourceId)` → `GateSpec(Set<String> paths, boolean needsGeometry)` and `GateSnapshot.flips(sourceId, kind, GateState persisted, GateState candidate)`, where `GateState(Map<String,Object> pathValues, Geometry geometry)`. `NoopPersistGate` (empty specs, never flips) is the default bean; prompt 017 adds `TriggerPersistGate` as `@Primary`. The **before** state is the persisted state (`h.payload` paths, `h.persisted_geom`); the candidate is the incoming snapshot after carry-forward.

### 8. `IngestPolicyEngine` (pure)

- `EntityState`: head part (exists, seq, version, tombstone, staticHash, persistedObservedAt, persistedReceivedAt, persisted centroid and bbox, persisted path values, persisted geometry when needed) and live part (observedAt, lastReceivedAt, validUntil, fullHash, dirty, kinematic, geometry/bbox). Transitions `afterPersist`, `afterLive`, `afterTouch` implement §6.2 steps 10–12: on PERSIST, kinematic = subset of the persisted payload, `dirty=false`; on LIVE, kinematic = incoming kinematic keys plus JSON null for keys in the previous kinematic but missing now, `dirty=true`; tombstones clear geometry and kinematic.
- `prepare(obs, state, profile)`: **TRACK carry-forward** (non-tombstone, null geometry, running geometry present → inherit it and set `POSITION_STALE`), then both hashes.
- `decide(prepared, state, profile, gateFlip)` implements **Core-Data-Model §5.2 exactly, first match wins**, with `Δt = observed_at − h.persisted_observed_at`, `keyframeDue = (anchor(obs) − h.persisted_anchor ≥ keyframe) OR (received_at − h.persisted_received_at ≥ window/4)`, `expired = l.valid_until IS NOT NULL AND l.valid_until < anchor(obs)`:
  1. `observed_at < l.observed_at` → LATE (equal timestamps pass) · 2. tombstone and (no head or head tombstoned) → SKIP · 3. tombstone → TOMBSTONE · 4. no head → FIRST · 5. head tombstoned or expired → REAPPEAR · 6. static hash differs → SIGNIFICANT · 7. ALL and full hash differs → CHANGE · 8. keyframeDue → HEARTBEAT if `full = l.full AND NOT l.dirty`, else KEYFRAME · 9. gate flip and `Δt ≥ edgeMinInterval` (null = 0) → TRIGGER_EDGE · 10. SAMPLED, `Δt ≥ minInterval`, and (deviation ≥ `minDeviationMeters` or a threshold exceeded when `thresholds=true`) → SAMPLE · 11. full hash equal → TOUCH if `received − l.last_received ≥ touchMin` or `observed − l.observed ≥ touchMin`, else TOUCH_THROTTLED · 12. → LIVE.
- LATEST is SAMPLED without row 10. **Deviation:** dead reckoning from the persisted centroid using persisted speed (converted via `SpeedUnit`) and course from `h.payload` motion paths over Δt, great-circle destination, then haversine to the candidate centroid; plain haversine without motion; 0 when either side lacks geometry. **Thresholds** compare candidate vs `h.payload` values with angular wrap (`FieldThreshold.exceeded`); missing or non-numeric on either side = not exceeded.
- Gap-Analysis §8.A item 2's out-of-order guard ("store the late version, don't regress the head") is **superseded** by Core-Data-Model's LATE rule: late observations are dropped with no side effects, which keeps I4. Note this in the class Javadoc.

### 9. Test support and docs

- `SnapshotContract.assertStableStaticHash(descriptor, List<NormalizedRecord> snapshotsOfOneEntity)` in `src/test/java/ai/foresignal/testsupport/ingest` (adapters from prompt 011 on use it).
- `docs/OpenAPI.json`: add `FieldSpec` and `KindDisplay` schemas and `SourceDescriptor` properties `titleField`, `entityKeys`, `fieldSpecs`, `kindDisplay`, `aggBelowZoom`, `startField`, `endField`, `private`, `privatePrecisionDecimals` (schemas only; prompt 013 exposes them). Regenerate Postman with `python3 scripts/generate-postman.py docs/OpenAPI.json postman`.
- Package Javadoc for `ai.foresignal.ingest`: the time contract, decision table, and that this package never touches the database.

### GeoJSON reader replacement (from the 004 report; see docs/Conventions.md)
- Prompt 004 added `org.locationtech.jts.io:jts-io-common` for `Geo.fromGeoJson`, which pulls in the unmaintained `json-simple:1.1.1` and exposes it to untrusted upstream GeoJSON.
- Add `ai.foresignal.source.formats.geojson.GeoJsonReader`, a Jackson 3 tree reader producing JTS geometries with SRID 4326. It supports Point, MultiPoint, LineString, MultiLineString, Polygon, MultiPolygon and GeometryCollection, and Feature/FeatureCollection unwrapping. It enforces the Core-Data-Model §3 limits (≤ 10,000 vertices checked before construction; no NaN or Infinity; nesting depth bounded) and reports errors as `InvalidInputException`.
- Re-point `Geo.fromGeoJson` at it and **remove the `jts-io-common` dependency** from the version catalog and build (confirm with `./gradlew dependencies` that `json-simple` is gone).
- Use `FetchResult.unchanged()` for the 304 factory everywhere; 004 named it that because `notModified()` clashes with the record accessor.

## Tests to write (do not run)

- `IngestPolicyEngineTest`: **rows 1–12 as a `@ParameterizedTest` matrix**, each row with a firing case, a boundary case (exactly `touchMin`, exactly `keyframe`, exactly `window/4`, `Δt = edgeMinInterval`, equal `observed_at`) and a near-miss falling to the next row; HEARTBEAT vs KEYFRAME on `dirty`; LATEST never samples; keyframe "none"; carry-forward sets `POSITION_STALE` without flipping the static hash.
- `DeadReckoningTest`: predicted position, unit conversion, antimeridian, no-motion fallback, null geometry → 0.
- `NormalizerTest`: time base, clamp with and without the flag, epoch floor, every limit, 2D forcing, GeometryFixer, null island, quality validation, TRACK explicit validUntil ignored, effective validUntil per anchor, `validFrom` range, tombstone normalization, duplicates kept and stable order, private precision cap.
- `ContentHasherTest`: key order, `5`/`5.0`, observations differing only in `observed_at`, volatile keys or quality hash equal; any hashed field changes the hash; static vs full scope; recipe stability and change.
- `EntityStateTest`: LIVE → PERSIST → LIVE keeps the merged payload correct when a key drops.
- `SourceIngestProfileTest` (keyframe default, effectiveKeyframe, touchMin clamp, window per tier), `TitleResolverTest`, `EntityKeyExtractorTest`, `DescriptorValidatorTest` (extend: each new field's failures).

## Acceptance criteria

- `ai.foresignal.ingest` core classes have no Spring, JDBC or JPA dependencies except `SourceIngestProfiles` and the gate beans.
- The decision table in code matches Core-Data-Model §5.2 row for row, and the matrix test names reference the row numbers.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 138, 141, 142, 143).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 143).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `008: Ingest Normalizer And Policy Engine`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/008-CR-Ingest-Normalizer-And-Policy-Engine.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `008: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
