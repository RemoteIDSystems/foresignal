# Elaro v1.2 core data model (final)

Status: final proposal for `docs/Architecture.md` v1.2. It replaces the refined v1.2 proposal and resolves every blocker and major issue from the skeptic round. Minor issues are taken where they are cheap. §18 maps each issue to its resolution. Section headers name the part of Architecture.md they replace or extend.

Repo facts checked for this design:
- The test profile file is `src/test/resources/application-test.yml`. It layers on top of the main `application.yml` and does not shadow it.
- `SchemaBootstrap` already uses `EntityManagerFactoryDependsOnPostProcessor` for PostGIS.
- The Javadoc in `SchemaBootstrap` / `application.yml` still says Flyway arrives in "prompt 042". It should say 063.
- Roadmap §2 guardrail 1 forbids running tests before 061.

---

## 0. Decisions at a glance

| | Decision |
|---|---|
| **Partitioning** | New prompt **006a**. `record_version` is partitioned `LIST (history_tier)` and then `RANGE (seq)`. PK `(seq, history_tier)`. Bootstrap SQL owns its DDL. Hibernate maps it read-only with `hbm2ddl.extra_physical_table_types=PARTITIONED TABLE`. Track blocks are 20M seq; standard blocks are 250M seq. |
| **Tiers and retention** | TRACK has **one window** (`retention.trackWindow`, default 3d) and is retained only by dropping whole partitions. Per-source retention is allowed only on STANDARD (chunked deletes). Heads are purged only below a per-source `floorSeq` under which every version is gone (I7 exact). |
| **Hashes** | `static_hash` (significant content) is stored on the head. `full_hash` (all non-volatile content) is stored on the live row. Persisted-vs-live divergence is the boolean `record_live.dirty`. The head has no full hash. |
| **Time** | Each source has a fixed time base (`observedAtMode`), never chosen per record. Future `observed_at` is always clamped to `received_at`. `ttlAnchor` is OBSERVED or RECEIVED (TRACK must be OBSERVED; STANDARD defaults to RECEIVED). Touches are throttled on received time as well as observed time. |
| **Policies** | `ALL`, `SAMPLED`, `LATEST`, with a keyframe on the anchor axis plus a **retention keyframe** (window/4, received axis). Decision table re-ordered, adding `SKIP`. Constraint: keyframe ≤ TTL − pollInterval − 30 s. |
| **Geometry** | TRACK sources carry the last geometry forward when an observation has none (`POSITION_STALE`). The head stores the persisted geometry, so it holds the full persisted state. |
| **Writer** | `IngestCommand = IngestBatch \| HeadPurge`. JDBC `batchUpdate` with `reWriteBatchedInserts`. State is initialised from the database. Clock guard. Transient retry. The advisory lock is configurable (`exclusive`, false in tests). `resetState()` for tests. `seq_clock` uses minute buckets and gets an insert on every batch. |
| **Triggers** | The gate compares candidate against **persisted** state, which self-heals at rule creation, so no reconcile pass is needed. Exact evaluation, checked by a shared truth-table fixture; conservative when types are ambiguous. `edgeMinInterval` caps the edge rate. A tombstone predecessor counts as NOT P. `reappearAsEnter` (default true for geofences). The edge join is on seq only. |
| **SSE** | `change` (id = seq), `live`, `touch`, `resync` (`gap`, `live_cap`, `live_dropped`), `heartbeat`, `reset`. ChangeSummary carries `prevPersistedBbox` and `prevLiveBbox`. `reset` is sent when the resume point is below the retention floor. |
| **asOf** | Validity is checked on the transaction axis (`ingested_at + ttl`). Lower bound: `L = S(T − ttl − 2 min)` for TRACK, `0` for STANDARD. `horizon` comes from window and keyframe. Batch granularity. |
| **Prompt split** | 007 becomes **007 (pure core, unit tests)** + **007b (writer)**. Sanctioned narrow test runs in 006a, 007 and 007b. **030a** downsampling. |

---

## 1. Invariants (new §6.0)

| ID | Invariant | Enforced by |
|---|---|---|
| I1 | Only the writer thread inserts `record_version` and writes `record_head`, `record_live` and `seq_clock`. Retention may delete versions and drop leaves. Head purges run on the writer. | Code structure. When `elaro.ingest.writer.exclusive=true` (the production default), `pg_try_advisory_lock` is held on a dedicated non-pooled connection. |
| I2 | seq is allocated inside the writer transaction by the single allocator. Every seq of a batch is greater than every seq allocated earlier, including rolled-back allocations. | §6 step 7 and `lastAllocatedSeq` |
| I3 | `ingested_at` never decreases with seq. | `ingestedAt = max(clock.now, lastIngestedAt)`, state loaded from the database at start, clock guard (§6.1) |
| I4 | Per entity, accepted `observed_at` never decreases. | Fixed time base per source, always clamp the future, LATE drop, in-batch sort |
| I5 | Current-state reads touch only `record_head ⋈ record_live`. | §12 |
| I6 | An entity that is continuously observed has a persisted version no older than `effectiveKeyframe + pollInterval` on its anchor axis, where `effectiveKeyframe = min(policy keyframe, window/4)`. For TRACK this is under the TTL on the transaction axis. | Keyframe and retention-keyframe rules (§5), validator |
| I7 | A head is purged only when every version of it is gone. | `h.seq < floorSeq(source)` (§13) |
| I8 | For every enabled rule, after each processed observation, P(head state) = P(latest state). The only exception is a flip held back by `edgeMinInterval`, which is persisted on the first observation after the interval passes. | `PersistGate` compares against persisted state (§11) |
| I9 | `record_version` is changed only by native SQL. | Code review |
| I10 | Every head has exactly one live row, and the reverse. | Writer upserts both. The load query uses LEFT JOIN, repairs a missing live row and counts `elaro.ingest.repair.live_missing`. |

---

## 2. Time contract (new §6.4)

| Column | Meaning | Set by | Hashed | Drives |
|---|---|---|---|---|
| `event_time` (nullable) | When a discrete real-world event happened, or a measurement period | Adapter | Yes | `effective_time`, `{{eventTime}}` |
| `observed_at` (required) | When upstream last observed or updated this state | Adapter, or framework under `observedAtMode=RECEIVED` | No | LATE check, SAMPLED Δt, OBSERVED-anchor TTL and keyframes, client last-writer-wins, cooldown, playback |
| `received_at` | When Elaro received the response, message or push (the sink stamps streams) | Framework | No | Clamp, RECEIVED-anchor TTL and keyframes, retention keyframe, touch throttle, `last_received_at` |
| `ingested_at` | Batch timestamp, monotonic | Writer | No | asOf, retention through `seq_clock` |

`effective_time = COALESCE(event_time, observed_at)`.

**Descriptor time settings:**
- `observedAtMode`: `UPSTREAM` (default) or `RECEIVED`.
  - Under UPSTREAM, a record without `observedAt` is rejected with `MISSING_OBSERVED_AT`.
  - Under RECEIVED, the framework sets `observed_at = received_at` on every record of the source and flags `TIME_IMPUTED`. Any adapter value is ignored.
  - The mode is never mixed within a source.
- `ttlAnchor`: `OBSERVED` or `RECEIVED`.
  - TRACK sources must use OBSERVED. STANDARD defaults to RECEIVED.
  - `anchor(x)` means `x.observed_at` or `x.received_at` accordingly.
  - Effective `valid_until = explicit ?? anchor + ttl`.

**Adapter mapping (binding):**

| Adapter type | `event_time` | `observed_at` | Notes |
|---|---|---|---|
| readsb snapshots (adsb.lol etc.) | null | `now − seen` (last message), **always** | Geometry is `lat/lon`, else `lastPosition`. `quality.extra["adsb.seen_pos"]` holds position age. |
| OpenSky states | null | `last_contact`, always | `quality.extra["opensky.time_position"]` |
| Streaming (AIS, MQTT) | null | Upstream message time of the newest merged message | Merged snapshot (rule 7) |
| GTFS-RT | null | Vehicle `timestamp`, else header `timestamp` (fixed per feed via the descriptor) | |
| Revised events (USGS, EMSC, GDACS) | Origin time | Upstream `updated` | ttlAnchor RECEIVED if a TTL is set |
| FIRMS | Acquisition time | Acquisition time | |
| NWS | Onset if present | `sent` / `updated` | `validUntil = expires` (explicit) |
| Catalogue and reference | null | The descriptor picks UPSTREAM (publication date) or RECEIVED. Never a per-record fallback. | |
| Measurements (OpenAQ, NDBC, EIA) | Measurement period | Measurement time | ttlAnchor OBSERVED (freshness) |
| Push (020) | Per custom source config | Per custom source `observedAtMode` | |

**Normalizer rules:**
1. If `observed_at > received_at`, set `observed_at = received_at`. Only when the excess is greater than `skewTolerance` (default 2 min) is the `FUTURE_CLAMPED` flag set and `elaro.ingest.records.clamped{source}` counted.
2. Reject `observed_at < 1970-01-02`.
3. Writer: if `observed_at < l.observed_at`, the observation is **LATE** and dropped with no side effects (`elaro.ingest.records.late`). Equal timestamps are accepted in arrival order.

---

## 3. Ingest input (extends §4.1 and §4.2)

```java
public record NormalizedRecord(
    String externalId, String kind,
    Instant observedAt,              // required unless observedAtMode=RECEIVED
    Instant eventTime,               // nullable; discrete events / measurement periods only
    Geometry geometry,               // JTS 4326, nullable
    Instant validUntil,              // nullable; ONLY an upstream-asserted expiry
    Map<String, Object> payload,     // COMPLETE snapshot
    boolean tombstone,
    Quality quality, List<Observer> observers) {}
public record Quality(Double accuracyMeters, Double confidence, PositionSource positionSource, Map<String,Object> extra) {}
public record Observer(String receiverId, Long frequencyHz, Double rssi, Double snr, Double decodeQuality) {}
public record Coverage(Integer receivers, Integer expected, Integer actual, Envelope region) {}
public record FetchResult(List<NormalizedRecord> records, boolean complete, boolean notModified, Coverage coverage) {}
```

**SourceDescriptor additions:**

| Field | Default | Rule |
|---|---|---|
| `licence`, `termsUrl` | null | |
| `historyTier` | STANDARD | Fixed, not overridable. TRACK requires `recordTtl` and `ttlAnchor=OBSERVED`. |
| `observedAtMode` | UPSTREAM | Not overridable |
| `ttlAnchor` | OBSERVED for TRACK, RECEIVED for STANDARD | Not overridable |
| `defaultIngestPolicy` | ALL | §5 |
| `volatileFields` | [] | Top-level keys left out of hashes and `kinematic` |
| `significantFields` | [] | Empty means every non-volatile key is significant. Required when the policy is SAMPLED or LATEST. |
| `motion` | null | `{speedField, speedUnit, courseField}` (not significant) |
| `thresholds` | [] | `[{field, delta, angular}]` (not significant) |
| `defaultRetention` | null | STANDARD only. A TRACK source with this set fails registry validation. |
| `skewTolerance` | 2 min | Flag and metric only |
| `limits` | §3 limits | |

**Framework types:**
- `IngestRecord(record, receivedAt, arrival)`
- `IngestBatch(sourceId, FetchLogEntry log, records, future)`
- `HeadPurge(List<PurgeSpec(sourceId, floorSeq, staleBefore)>, future)`
- Sealed `IngestCommand = IngestBatch | HeadPurge`

**Adapter rules, §4.2 items 7–13:**
7. The payload is a complete snapshot. Protocols with split messages merge state per entity. Test with `SnapshotContract.assertStableStaticHash`.
8. Slim, allowlisted payloads with no nulls. Leave out query-relative fields. Raw counters are left out or declared volatile.
9. `validUntil` only for an upstream-asserted expiry. TRACK sources must not set it, and asOf ignores it there.
10. `eventTime` only for discrete events and measurement periods.
11. Scrub sentinels (AIS lat 91 / lon 181, SOG 102.3, heading 511).
12. Quality transitions that matter go in significant fields computed by the adapter (e.g. `navDegraded`). Quality is never hashed.
13. The time base is fixed per source (§2). Never switch an entity between timestamp fields from record to record.

**Limits:**
- Payload ≤ 64 KB, depth ≤ 8, ≤ 512 keys, strings ≤ 8 KB.
- Geometry ≤ 10,000 vertices, checked before `GeometryFixer`.
- No NaN or Infinity. ≤ 8 observers.
- An exact (0,0) point becomes null geometry with flag `NULL_ISLAND`, unless the source sets `allowNullIsland`.
- A violation rejects that record only, with a reason.

**qa_flags:** 1 `FUTURE_CLAMPED`, 2 `GEOMETRY_FIXED`, 4 `NULL_ISLAND`, 8 `TIME_IMPUTED`, 16 `POSITION_STALE`.

**Geometry carry-forward (TRACK only):** a non-tombstone observation with null geometry whose running state has a geometry inherits that geometry and gets `POSITION_STALE`. This is applied before hashing, so loss of position never flips `static_hash`.

**Tombstone normalization:** a tombstone is stored with `geom = NULL`, `payload = {}`, `kinematic = {}`, and `kind` kept.

---

## 4. Hashing (replaces §6.2 step 2)

Canonicalization is as in 007. Hash = the first 8 bytes of SHA-256, as a signed bigint.

| Input | `full_hash` (stored on `record_live`) | `static_hash` (stored on `record_head`) |
|---|---|---|
| kind, event_time, explicit validUntil, tombstone | ✓ | ✓ |
| geometry (WKT 1e-7, after carry-forward) | ✓ | `geom IS NULL` only |
| payload minus volatile | ✓ | significant keys only (all non-volatile when the list is empty) |
| observed/received/ingested, derived valid_until, quality, observer | ✗ | ✗ |

**Recipe** = hash(hasher version, sorted volatile list, sorted significant list). It is stored per head (`record_head.hash_recipe`).

**Migration**, lazy and per source, on the writer thread:
- When to run: at the first batch for a source after start, or after its `SourceIngestProfile` recipe changes (020 custom-source edits). The writer runs `SELECT 1 FROM record_head WHERE source_id=? AND hash_recipe<>? LIMIT 1`. If a row exists, it migrates in its own transactions, in chunks of 2,000, before the batch.
- For each stale head:
  - `h.static_hash` is recomputed from the head columns: kind, event_time, valid_until_explicit, tombstone, `h.payload`, `h.persisted_geom IS NULL`. This is exact, because the head holds the full persisted state.
  - `l.kinematic` is recomputed as the kinematic subset of the merged payload under the new recipe.
  - `l.full_hash` is recomputed from the merged payload and `l.geom`.
  - `h.hash_recipe` is set to the new value.
- `dirty` is unchanged.
- A field that moves from kinematic to significant shows the persisted value until the next observation, which persists SIGNIFICANT if the value differs. That is correct under the new definition.

**Required tests:**
- Observations that differ only in `observed_at` hash equal.
- Adding `volatileFields=[updated]` to a USGS source yields 0 versions on re-fetch.

---

## 5. Ingest policies (new §6.5)

### 5.1 Settings and validation

```json
{"mode":"SAMPLED","minInterval":"60s","keyframe":"240s","minDeviationMeters":1000,"thresholds":true,"edgeMinInterval":"30s"}
{"mode":"LATEST","keyframe":"20m","edgeMinInterval":"15s"}
{"mode":"ALL","keyframe":"4m"}      // default: ttl − pollInterval − 30s; "none" (the default) when there is no TTL
```

`IngestPolicyValidator` runs on descriptor defaults at registry start and on `source_settings.ingest_policy` at PUT:

- `2 s ≤ minInterval ≤ keyframe`.
- `edgeMinInterval ≤ minInterval` (default `minInterval / 2`).
- With a TTL: `keyframe ≤ ttl − pollInterval − 30 s`, where pollInterval is 2 s for streams. If the default comes out ≤ 0, the registry fails: the TTL is too short for the poll interval.
- `keyframe: none` only without a TTL.
- SAMPLED and LATEST require `significantFields`. Threshold and motion fields must not be significant.
- Non-significant `searchFields` and `tileProperties` produce a warning.

### 5.2 Decision function (pure `IngestPolicyEngine`)

Inputs:
- the observation, after normalization, carry-forward and hashing;
- the running state `(h, l)`;
- `SourceIngestProfile` (policy, ttl, ttlAnchor, window, motion, thresholds, gate).

Definitions:
- `Δt = observed_at − h.persisted_observed_at`
- `keyframeDue = (anchor(obs) − h.persisted_anchor ≥ keyframe) OR (received_at − h.persisted_received_at ≥ window/4)`
- `expired = l.valid_until IS NOT NULL AND l.valid_until < anchor(obs)` (null means it never expires)

| # | Condition | Outcome (`persist_reason`) |
|---|---|---|
| 1 | `observed_at < l.observed_at` | **LATE** |
| 2 | tombstone, and (no head, or head tombstoned) | **SKIP** |
| 3 | tombstone | PERSIST `TOMBSTONE` (4) |
| 4 | no head | PERSIST `FIRST` (1) |
| 5 | head tombstoned, or `expired` | PERSIST `REAPPEAR` (2) |
| 6 | `static_hash ≠ h.static_hash` | PERSIST `SIGNIFICANT` (3) |
| 7 | policy ALL and `full_hash ≠ l.full_hash` | PERSIST `CHANGE` (5) |
| 8 | `keyframeDue` | `HEARTBEAT` (8) if `full_hash = l.full_hash AND NOT l.dirty`, else `KEYFRAME` (7) |
| 9 | gate reports P(candidate) ≠ P(persisted) for an applicable rule, and `Δt ≥ edgeMinInterval` | PERSIST `TRIGGER_EDGE` (9) |
| 10 | SAMPLED, `Δt ≥ minInterval`, and (deviation ≥ minDeviation or a threshold is exceeded) | PERSIST `SAMPLE` (6) |
| 11 | `full_hash = l.full_hash` | **TOUCH** if `received_at − l.last_received_at ≥ touchMin` OR `observed_at − l.observed_at ≥ touchMin`, else **TOUCH_THROTTLED** |
| 12 | otherwise | **LIVE** |

- `touchMin = clamp(ttl/10, 30 s, 1 h)`, or 1 h without a TTL.
- `LATEST` is SAMPLED without row 10.
- Deviation is dead reckoning from the persisted centroid using persisted speed and course (from `h.payload` motion paths), else plain haversine. It is 0 with no geometry.
- Thresholds compare against `h.payload` values, with angular wrap where set.
- **Ship rows 1–12 as a parameterised unit-test matrix.**

### 5.3 Defaults

| Family | Policy | minInt | keyframe | TTL | Deviation | Thresholds | edgeMin | Tier |
|---|---|---|---|---|---|---|---|---|
| Global aircraft (`adsblol-*`, `airplaneslive-*`, `adsbfi-*`, `opensky-states`) | SAMPLED | 60 s | 240 s | 5 min | 1,000 m DR | alt_baro 1,000; track 20° | 30 s | TRACK |
| `local-adsb-*` | SAMPLED | 10 s | 120 s | 5 min | 300 m DR | alt_baro 500; track 10° | 5 s | TRACK |
| Vessels | SAMPLED | 30 s | 1,200 s | 30 min | 250 m DR | cog 15°; sog 3 kn | 15 s | TRACK |
| Transit and rail | SAMPLED | 15 s | 240 s | 5 min | 150 m | bearing 30° | 8 s | TRACK |
| Everything else | ALL | — | ttl − poll − 30 s, or none | per source | — | — | — | STANDARD |

**Significant fields:**
- Aircraft: `flight, r, t, squawk, category, emergency, onGround, navDegraded`.
- Vessels: `name, callsign, imo, shipType, destination, navStatus, draught`.

**Budget:** about 10k versions/min (aircraft 4.5k, vessels 2.5k, local ≤ 1.2k, transit 1.8k, standard < 0.5k).
- Retention keyframes add less than 1% on STANDARD and nothing on TRACK.
- NFR-2: 40,000 observations/min, of which ≤ 20,000 persisted versions/min.
- `elaro.ingest.persist{reason=TRIGGER_EDGE}`: a warning is raised when its share is above 20% of a source's versions over 1 h.

---

## 6. Ingest writer (replaces §6.1–§6.3)

### 6.1 Lifecycle and state

- Platform thread `elaro-ingest-writer`, started on `ApplicationReadyEvent`.
- **Lock** (when `elaro.ingest.writer.exclusive=true`):
  - `pg_try_advisory_lock(hashtext('elaro.ingest.writer'))` on a dedicated connection opened with `DriverManager` from `JdbcConnectionDetails`, outside Hikari.
  - Keepalive `SELECT 1` every 30 s.
  - If the lock is held at start, it fails with a clear error.
  - On connection loss, it pauses command processing, reacquires with backoff, and raises `elaro.ingest.writer.lock_lost`.
- **State init** (`resetState()`, also called by `DatabaseCleaner`):
  - `lastSeq, lastIngestedAt` come from `SELECT seq, ingested_at FROM record_version ORDER BY seq DESC LIMIT 1`.
  - `lastIngestedAt` is then raised to `max(lastIngestedAt, max(seq_clock.minute), max(fetch_log.ingested_at))`.
  - `lastAllocatedSeq` comes from `SELECT last_value FROM record_seq`.
  - The migrated-recipe map is cleared.
- **Clock guard:**
  - At start, if `clock.now < lastIngestedAt − 5 min`, it does not process batches. The queue fills and producers back off. Health alarm `writer.clockBehind`; re-check every 30 s.
  - At run time, each batch compares wall-clock delta with `System.nanoTime` delta. A divergence above 5 min counts `elaro.ingest.clock.jump{direction}` and raises a health warning.
  - Backward jumps are absorbed by the I3 clamp, counted as `elaro.ingest.clock.clamped`. Ingest is never stopped.
- **Profiles:** `SourceIngestProfile` is cached and refreshed on settings events. The rule snapshot is read once at batch start (an `AtomicReference` from `PersistGate`).

### 6.2 Batch steps

1. **Pre-flight.**
   - If `lastAllocatedSeq + n ≥ coveredUpTo(tier)`, call `PartitionManager.ensureFor(tier, lastAllocatedSeq + n)` inline. Fail the batch only if coverage is still insufficient.
   - If headroom is under one block, request an async ensure and warn.
2. **Normalize.** Apply §2 and §3: time base, clamp, limits, flags, carry-forward (needs the running geometry, so it runs in step 6), explicit/effective validUntil. **Keep duplicates.**
3. **Stable-sort** by `(externalId, observedAt, arrival)`.
4. **Recipe check** (§4) when the source is not yet migrated.
5. **Begin the transaction.** `ingestedAt = max(clock.now, lastIngestedAt)`.
6. **Load state** with one query, then fold:
   ```sql
   SELECT h.external_id, h.seq, h.version, h.tombstone, h.static_hash,
          h.persisted_observed_at, h.persisted_received_at,
          ST_X(ST_Centroid(h.persisted_geom)), ST_Y(ST_Centroid(h.persisted_geom)),
          ST_XMin(h.persisted_geom), ST_YMin(h.persisted_geom), ST_XMax(h.persisted_geom), ST_YMax(h.persisted_geom),
          l.observed_at, l.last_received_at, l.valid_until, l.full_hash, l.dirty, l.kinematic,
          ST_XMin(l.geom), ST_YMin(l.geom), ST_XMax(l.geom), ST_YMax(l.geom),
          /* TRACK tier only */ ST_AsBinary(l.geom),
          /* per motion/threshold/gate path, fixed per (source, rule snapshot) */ h.payload #> :p1, ...,
          /* gate needs geometry */ ST_AsBinary(h.persisted_geom)
   FROM record_head h LEFT JOIN record_live l USING (source_id, external_id)
   WHERE h.source_id = ? AND h.external_id = ANY(?)
   ```
   - A head with no live row is repaired (I10).
   - Fold each entity through §5.2, updating the running state after each outcome.
7. **Allocate seq:** `SELECT nextval('record_seq') FROM generate_series(1,:n)`. Sort in Java and assign in order. Set `lastAllocatedSeq = max`, `version = running.version + 1`, `prev_seq = running.seq`.
8. `INSERT INTO fetch_log … RETURNING id` gives `batch_id`.
9. **Version insert:** `JdbcTemplate.batchUpdate`. Geometry via `ST_GeomFromWKB(?,4326)`, jsonb via `?::jsonb`. The JDBC URL property is `reWriteBatchedInserts=true`.
10. **Head upsert:** `batchUpdate` `INSERT … ON CONFLICT (source_id, external_id) DO UPDATE`, one row per entity with at least one PERSIST (asserted before the call). It carries:
    - kind, seq, version, event_time, valid_until_explicit, tombstone, static_hash, hash_recipe
    - persisted_observed_at, persisted_received_at, persisted_geom, payload, quality, search_text
11. **Live upsert:** one row per entity with at least one PERSIST or LIVE.
    - On PERSIST: `kinematic` = the kinematic subset (non-significant, non-volatile) of the persisted payload, and `dirty = false`.
    - On LIVE: `kinematic` = the incoming kinematic keys, plus JSON null for keys present in the previous `l.kinematic` but missing from incoming, and `dirty = true`.
    - `last_received_at` = the batch maximum for that entity.
    - Tombstones: `geom NULL`, `kinematic {}`.
12. **Touch:** `batchUpdate` `UPDATE record_live SET observed_at=?, last_received_at=?, valid_until=? WHERE source_id=? AND external_id=? AND observed_at <= ?`, one row per key.
    - `valid_until` = explicit, else `anchor + ttl` (for RECEIVED this is `r + ttl`).
    - Updates are not rewritten by the driver, so the summed update counts must equal the row count, or the batch fails.
13. `INSERT INTO seq_clock(minute, first_seq) VALUES (date_trunc('minute', :ingestedAt), :lastAllocatedSeqBeforeStep7 + 1) ON CONFLICT DO NOTHING`, on **every** batch, with no cache.
14. **Commit.** Advance `lastSeq` and `lastIngestedAt`.
15. **Publish:** `ChangeBatch`, then `LiveBatch`, then complete the future, then `UsageMeter.recordIngest`.

**Retry:** on SQLState `08*`, `40*`, `55P03` or `57014`, roll back and retry the whole batch up to 3 times (backoff 0.5, 1, 2 s), re-folding from fresh state. On other errors, or once retries are exhausted, fail the future and continue. The thread never dies.

**`HeadPurge`:**
- For each spec, in chunks of 10k:
  ```sql
  SELECT h.external_id FROM record_head h JOIN record_live l USING (source_id, external_id)
  WHERE h.source_id = ? AND h.seq < :floorSeq AND l.last_received_at < :staleBefore LIMIT 10000;
  -- then, in the same tx:
  DELETE FROM record_live WHERE source_id = ? AND external_id = ANY(?);
  DELETE FROM record_head WHERE source_id = ? AND external_id = ANY(?);
  ```
- After each chunk, if more remain, the command re-enqueues itself at the queue tail, so batches interleave.

**IngestOutcome:**
- Fields: `seen, persisted, live, touched` (touch rows written), `touchesThrottled, late, skipped, rejected, fromSeq, toSeq, byReason` (all nine reasons).
- `version_bytes` = UTF-8 length of payload JSON + quality JSON + observer JSON + 120.

**Metrics:**
- `elaro.ingest.records.{seen,persisted,live,touched,touches_throttled,late,skipped,clamped,rejected}{source}`
- `elaro.ingest.persist{source,reason}`
- `elaro.ingest.batch.duration{phase}`
- `elaro.ingest.batch.retries`

---

## 7. Streaming sinks (replaces the §5.5 coalescing paragraph)

- One outstanding batch per streaming source. Flush at 2 s or 2,000 entries, once the previous future has completed.
- **SAMPLED / LATEST:** a map keyed by externalId. The greatest `observedAt` wins; ties go to the later arrival.
- **ALL:** an ordered list, capped at `allBufferMax=50,000`. At the cap, `accept` blocks for up to 30 s, then drops and counts `elaro.stream.records.dropped`.
- `receivedAt` is stamped on accept. Each flush produces one `fetch_log` row (`trigger=STREAM`) and updates `source_health.last_data_at`.
- **Documented limit:** under SAMPLED and LATEST, trigger edges and samples have *flush resolution* for streams (≥ 2 s, longer while a batch is in flight) and *poll resolution* for polling sources.

---

## 8. Tables (replaces §7.1 rows)

**`record_version`** (partitioned, DDL owned by bootstrap). Columns, in order:

`seq bigint, ingested_at, observed_at, received_at timestamptz, event_time, valid_until, valid_until_explicit timestamptz null, prev_seq, batch_id bigint null, version int, history_tier, persist_reason, qa_flags smallint, tombstone bool, source_id varchar(64), external_id varchar(256), kind varchar(64), geom geometry(Geometry,4326) null, payload jsonb, quality, observer jsonb null`

- PK `(seq, history_tier)`.
- Partitioned indexes: `(source_id, external_id, seq)`, `(source_id, observed_at)`, GiST `(geom)`.
- No content hash, no `ingested_at` index, no unique constraint.

**`record_head`** (Hibernate; written only on PERSIST, migration and purge):
- PK `(source_id, external_id)`
- `kind, seq, version, event_time, valid_until_explicit, tombstone, static_hash bigint, hash_recipe int`
- `persisted_observed_at, persisted_received_at timestamptz`
- `persisted_geom geometry null` (**no index**)
- `payload jsonb, quality jsonb, search_text`
- Index `(kind)` only.

**`record_live`** (Hibernate; hot):
- PK `(source_id, external_id)`
- `observed_at, last_received_at, valid_until timestamptz, geom geometry null`
- `full_hash bigint, dirty bool, kinematic jsonb not null default '{}', quality jsonb, qa_flags smallint`
- GiST `(geom)` only.
- **There is no index on `observed_at`, by design**, so touches stay HOT.
- `fillfactor=70, autovacuum_vacuum_scale_factor=0.02, autovacuum_vacuum_cost_limit=2000`.

**Current payload** = `jsonb_strip_nulls(h.payload || l.kinematic)`.

**`fetch_log`**:
- `id` identity PK, `source_id`
- `trigger` (SCHEDULED, MANUAL, STREAM, PUSH), `status` (OK, NOT_MODIFIED, FAILED, REFUSED, DISCARDED)
- `started_at, received_at, ingested_at`, `http_status, request_count, url_redacted, etag, bytes, body_sha256`
- `adapter_version, licence, principal`
- `records_seen, rejected, late, skipped, versions_written, live_updates, touches`
- `from_seq, to_seq, coverage jsonb, error`
- Index `(source_id, started_at DESC)`.

**`seq_clock`**: `minute timestamptz` PK, `first_seq bigint`.
- Rows older than (largest retention window + 1 d) are pruned by retention.
- Definitions:
  - `cutoffSeq(t) = first_seq` of the latest minute ≤ t. Every row with seq below it was ingested before t.
  - `S(T) = coalesce(max(seq) WHERE seq ≥ first_seq(m) AND seq < first_seq(next minute > T, if any) AND ingested_at ≤ T, first_seq(m) − 1)`, where `m` is the latest minute ≤ T.

**Additions to existing tables:**
- `source_usage` gains `records_seen, versions, live_updates, touches, late, skipped, rejected, version_bytes`.
- `source_settings` gains `ingest_policy jsonb`.
- `source_health` gains `last_coverage jsonb, recent_coverage jsonb` (ring of 20) and `last_data_at timestamptz` (last fetch or flush with records, i.e. not a 304).
- `alert` gains `observed_at` and `record_snapshot jsonb` (kind, version, observedAt, GeoJSON geometry, payload ≤ 16 KB).
- `trigger_rule` gains `reappear_as_enter bool` (default true when the rule has a geofence, else false).

---

## 9. Partitioning and schema ownership (replaces §7.3)

```
record_version          PARTITION BY LIST (history_tier)
├── record_version_std  FOR VALUES IN (0) PARTITION BY RANGE (seq)   blocks of 250,000,000 (≈17 d at 10k/min)
└── record_version_trk  FOR VALUES IN (1) PARTITION BY RANGE (seq)   blocks of 20,000,000  (≈33 h at 10k/min, ≈17 h at 20k/min)
```

seq is global across tiers. At a 3d track window there are about 3–5 track leaves, plus up to 2 ahead.

**Phase 1:**
- `HistorySchemaInitializer` is registered in `SchemaBootstrap`. It has `@DependsOn("postgisExtensionInitializer")` and is added to `EntityManagerFactoryDependsOnPostProcessor`.
- It and `PartitionManager` depend only on `DataSource` and `MeterRegistry`, never on JPA. It runs before the EntityManagerFactory:
  1. `CREATE SEQUENCE IF NOT EXISTS record_seq INCREMENT BY 1`.
  2. relkind guard: a plain table with `record_version` and `record_head` both empty → `DROP … CASCADE`. Non-empty → fail fast ("run `docker compose down -v`").
  3. `CREATE TABLE IF NOT EXISTS` for the parent and the tier parents, then `ADD COLUMN IF NOT EXISTS` for every column.
  4. `CREATE INDEX IF NOT EXISTS` for the three partitioned indexes.
  5. `PartitionManager.ensureAhead()`: coverage ≥ `last_value + 1.5 × block` per tier.
- Main `application.yml` gets `spring.jpa.properties.hibernate.hbm2ddl.extra_physical_table_types: PARTITIONED TABLE`. `application-test.yml` layers on top and must not override `spring.jpa.properties`.
- Update the Javadoc in `SchemaBootstrap` and the comment in `application.yml` from "prompt 042" to "prompt 063".

**`RecordVersionEntity`:**
- `@Immutable`, `@Id seq`, `@SequenceGenerator(allocationSize=1)`.
- No `@UniqueConstraint` and no `@Index`.
- smallint columns are mapped as `Short` with `@JdbcTypeCode(SqlTypes.SMALLINT)`.

**`PartitionManager`:**
- All operations go through one in-process `ReentrantLock` on a dedicated autocommit connection with `SET lock_timeout='3s'` and 5 retries. It is never `@Transactional`.
- **Create:** check `pg_inherits` first, then `CREATE TABLE rv_t_<start> (LIKE record_version INCLUDING DEFAULTS)`, then `ATTACH PARTITION … FOR VALUES FROM (a) TO (b)`. `42P07`, and `42P17` when the re-check finds a covering leaf, count as success.
- **Drop:**
  - Run `ensureAhead` first.
  - `ALTER TABLE … DETACH PARTITION … CONCURRENTLY` with `statement_timeout='10min'`, then `DROP TABLE`.
  - A pending detach is finished with `FINALIZE` on the next run.
- Never create a DEFAULT partition.
- **Schedule:** phase 1, every 10 min, and inline from pre-flight.
- Gauge `elaro.history.partition.headroom{tier}`. Warn when headroom is below 1 block.
- `ANALYZE record_version` daily and after each attach or drop, under the same lock.

**DatabaseCleaner:**
- Skips `pg_inherits` children. `TRUNCATE record_version` recurses.
- Calls `IngestWriter.resetState()` after truncating.

**Test profile:**
- `elaro.ingest.writer.exclusive=false`.
- Scheduled partition maintenance and retention disabled.

---

## 10. Distribution (replaces §8.1 and §8.2)

### 10.1 Buses

- **ChangeBus:** `ChangeBatch(sourceId, batchId, ingestedAt, fromSeq, toSeq, List<ChangeSummary>)`.
  - `ChangeSummary` = seq, prevSeq, historyTier, sourceId, externalId, kind, version, persistReason, observedAt, eventTime, validUntil, tombstone, qaFlags, `bbox`, `prevPersistedBbox` (from `h.persisted_geom`), `prevLiveBbox` (from `l.geom`).
  - Overflow and `onGap` behave as before.
  - Subscribers: SseHub, TriggerEngine, EmbeddingWorker.
- **LiveBus:** `LiveBatch(sourceId, List<LiveUpdate>, List<Touch>)`.
  - Subscriber queues are bounded. On overflow the batch is dropped and counted, and the subscriber's `onLiveDropped()` is invoked.
  - SseHub reacts by sending `resync{reason:"live_dropped"}` to every client with live=true.
- Change is always published before live.

### 10.2 SSE `/v1/changes/stream`

| Event | `id:` | Lane |
|---|---|---|
| `change` | seq | FIFO (`clientBuffer` 1,000); overflow sends `reset` |
| `live`, `touch` | **none** | Conflation map per client, drained every `liveIntervalMs` (1,000), capped at `liveMaxEntities` (50,000) |
| `resync` | none | `{reason: gap\|live_cap\|live_dropped, watermarkSeq}` |
| `heartbeat`, `reset` | none | |

- Parameters: `live` (true), `touches` (false), `source`, `kind`, `bbox`.
- **Filter match:** `bbox` OR `prevPersistedBbox` OR `prevLiveBbox` intersects the filter. When only a prev box matches, set `leftFilter: true`.
- **Resume floor:** when `after < RetentionState.lowestRetainedSeq(filtered sources)`, send `reset` immediately.
- Payload loads use `seq = ANY(?)` (runtime pruning, one leaf per tier).
- Client contract:
  1. Open SSE with `after = watermarkSeq`.
  2. Page the snapshot with `order=id`.
  3. Apply an event only if its `observedAt` ≥ the stored `observedAt`.

---

## 11. Triggers (amends §8.3)

- Rules evaluate persisted versions over `seq ∈ (from, to]`, **excluding `HEARTBEAT`**.
- **Edge join:** `LEFT JOIN record_version p ON p.seq = v.prev_seq` (seq only; prunes to one leaf per tier).
  - `enter = NOT v.tombstone AND P(v) AND (v.prev_seq IS NULL OR p.tombstone OR (r.reappear_as_enter AND v.persist_reason = 2) OR (p.seq IS NOT NULL AND NOT P(p)))`
  - `exit = p.seq IS NOT NULL AND NOT p.tombstone AND P(p) AND (v.tombstone OR NOT P(v))`
  - A missing `p` (retention) fires nothing except via REAPPEAR or FIRST. Expiry itself is not an exit.
- **PersistGate:** 007 defines the interface plus `NoopPersistGate`, `GateSpec(paths per source, needsGeometry)` and a snapshot `AtomicReference`. 014 implements `TriggerPersistGate` (`@Primary`), refreshed on `TriggerRulesChangedEvent`.
  - The **before state is the persisted state**: `h.payload` paths and `h.persisted_geom`. The candidate is the incoming snapshot and its geometry after carry-forward.
  - Any applicable rule with P(before) ≠ P(candidate) gives `TRIGGER_EDGE`, subject to `edgeMinInterval`.
  - Because the comparison is against persisted state:
    - a new or edited rule converges on each entity's next observation, with no reconcile pass;
    - a flip held back by the interval is persisted on the first observation after it passes;
    - a flip that reverses before then is correctly never persisted.
  - **Evaluation is exact**, using the same operators as `RuleSqlBuilder` (numeric with a number type guard, boolean, string equality, geofence via JTS `PreparedGeometry.intersects`).
  - When either side's JSON type differs from the operator's expected type, or a value is missing, the gate reports a flip iff before ≠ candidate (conservative).
  - Both `RuleSqlBuilderIT` and `TriggerPersistGateTest` consume one fixture, `src/test/resources/trigger/operator-truth-table.json`.
- **Validation** (014): a condition whose path root is in the source's `volatileFields` is rejected with 400.
- **Match mode** fires per non-HEARTBEAT version, keyframes included. The docs recommend edge modes or cooldown on SAMPLED sources.
- **Cooldown** on `observed_at`: SQL seeds `NOT EXISTS (alert … a.observed_at > v.observed_at − :cooldown)`, then Java filters per (rule, entity) in seq order.
- Alerts store `observed_at` and `record_snapshot`. `getAlert` loads the version by `seq = ?` and falls back to the snapshot.
- **Limits** (documented): flush or poll resolution (§7); edges need the predecessor version to still be retained.

---

## 12. Query paths (replaces §9)

| Endpoint | Strategy |
|---|---|
| `GET /v1/records` | `record_head h JOIN record_live l`, merged payload. Filters: `l.geom &&`, `NOT h.tombstone`, `l.valid_until > now()` unless `includeExpired`, `timeField` window. `order=observed` (keyset `(l.observed_at, source_id, external_id)`; sorts the matched set per page with no index, by design) or `order=id`. `watermarkSeq = ChangeBus.lastPublishedSeq()` is read before the query. |
| `GET /v1/records/{s}/{id}` | Head plus live. `include=observer` reads observers from the head's version, gated (§14). |
| `…/versions` | `WHERE source_id=? AND external_id=? ORDER BY seq` |
| `GET /v1/history` | By `timeField` (default `observed`) and bbox. `ingested` is translated to seq bounds through `seq_clock`. |
| `GET /v1/changes` | `seq > :after` |
| Tiles | `record_live l JOIN record_head h`, `l.geom &&`, merged properties, `l.observed_at` |
| `kinds` | Head ⋈ live, current |

- `timeField = effective | observed | event | ingested`. Default is `effective` for records, search and briefings; `observed` for history.
- **Record DTO** gains: `version`, `seq`, `observedAt`, `persistedObservedAt`, `eventTime`, `quality`, `live` (= `l.dirty`), `qaFlags`.
- Version items carry: `persistReason, ingestedAt, receivedAt, batchId, quality, observer` (gated).

**asOf (transaction time):**
1. A `source` or `kind` filter is required (`400 ASOF_FILTER_REQUIRED`).
   - `horizon(source) = now − window + effectiveKeyframe + 1 h`.
   - For T < horizon of any involved source, return `400 ASOF_BEYOND_RETENTION`.
2. `S = S(T)` (§8).
3. Per tier, one query each:
   - TRACK: `L = S(T − ttl − 2 min)`.
   - STANDARD: `L = 0`.
4. Query:
   ```sql
   SELECT DISTINCT ON (source_id, external_id) …
   FROM record_version v JOIN (VALUES …) s(source_id, ttl) USING (source_id)
   WHERE history_tier = :tier AND seq BETWEEN :L AND :S
   ORDER BY source_id, external_id, seq DESC
   ```
   Keep rows where `NOT tombstone AND (valid_until_explicit > T OR (valid_until_explicit IS NULL AND (s.ttl IS NULL OR ingested_at + s.ttl > T)))`. On TRACK the explicit value is ignored. Apply bbox and kind to the chosen version. Keyset on `(source_id, external_id)`.
5. The response adds `asOf`, `watermarkSeq=S`, `approximate` (true when any source is SAMPLED or LATEST, or the range is downsampled) and `horizon`.

Granularity is one batch. Valid-time asOf is not offered; use `/v1/history`.

---

## 13. Retention (replaces §11)

**Windows:**
- TRACK uses `retention.trackWindow` (default 3d).
- STANDARD uses the source setting, else descriptor `defaultRetention`, else `retention.defaultWindow` (7d).
- A per-source retention PUT on a TRACK source is rejected with `400 RETENTION_TIER_FIXED`.

**Consumer clamp:** `cutoffSeq` is clamped to `min(trigger.cursor, embedding.cursor) + 1`. If a cursor is older than `now − 2 × window`, the clamp is released and `elaro.retention.consumer_lag` is alarmed.

**RetentionJob** (hourly, not `@Transactional`):
1. **TRACK:** `PartitionManager.drop` for every `rv_t_*` whose upper bound ≤ `cutoffSeq(now − trackWindow)`.
   - `floorSeq(track source)` = lower bound of the oldest remaining attached track leaf (or the next leaf's start if none).
2. **STANDARD:** for each source, chunked deletes in one transaction per chunk:
   ```sql
   DELETE FROM record_version WHERE history_tier = 0 AND seq < :cutoff
     AND seq IN (SELECT seq FROM record_version WHERE history_tier = 0 AND source_id = ? AND seq < :cutoff LIMIT 10000)
   ```
   - On completion, `floorSeq(source) = cutoff`. On failure the source is skipped this run.
3. **STANDARD leaf drop** for every `rv_s_*` whose upper bound ≤ `cutoffSeq(now − max standard window)`. These leaves are already empty.
4. **Heads:**
   - Pre-check candidates with a read query.
   - Submit **one** `HeadPurge` with a `PurgeSpec(source, floorSeq, staleBefore)` for each source whose steps succeeded and which has candidates.
   - `staleBefore = min(now − window, source_health.last_data_at − touchMin)`. A 304 streak therefore never ages out entities from the last full response.
5. `fetch_log`: rows with `versions_written = 0` after 7d; other rows after `retention.provenance` (30d).
6. `seq_clock`: drop minutes older than (largest window + 1 d).
7. Other tables as before. Publish `RetentionState` (floors, `lowestRetainedSeq`).

**FR-17 is amended:** current state is kept while current. Versions follow the window. Observed entities are re-keyframed every window/4, so they always keep a recent version.

**Downsampling:** prompt **030a** (rewrite-and-swap of track leaves older than `history.track.rawWindow`). It keeps:
- reasons FIRST, REAPPEAR, SIGNIFICANT, TOMBSTONE and TRIGGER_EDGE;
- the **maximum seq per entity per leaf**;
- every seq referenced by a head or an alert;
- one row per entity per `downsampleInterval` among the other reasons.

Track `floorSeq` is unchanged by it. asOf over a downsampled range widens TTL validity to `downsampleInterval` and sets `approximate`.

---

## 14. Provenance, quality, observer, coverage (new §6.6)

- `record_version.batch_id → fetch_log.id`, no FK. Licence and adapter version are snapshotted on the fetch row; push rows record the principal.
- **Quality:** validated (`accuracyMeters ≥ 0`, `confidence ∈ [0,1]`, `extra` keys `^[a-z0-9]+\.[a-z0-9_]+$` with scalar values). Stored on versions, head and live. Never hashed.
- **Observer:** ≤ 8, on versions only. Returned only with `include=observer`, to agent or admin tokens, or when `security.exposeObserver=true`.
- **Coverage:** stored in `fetch_log.coverage` and `source_health.last_coverage` / `recent_coverage`. The silent-break rule reports "coverage dropped" when receivers or expected fall by ≥ 50% against the median of the last 20.
- `licence` and `termsUrl` are exposed in `/v1/sources`.

---

## 15. Storage visibility (extends §11 and `/v1/system`)

- `source_usage` accumulates per-source counts and `version_bytes` (§6).
- **Health `storage`:**
  - `observationsPerMinute, versionsPerMinute, persistRatio, triggerEdgeShare`
  - `bytesPerDay` (7d), `projectedBytes` (× window)
  - `lateLastHour, clampedLastHour`
- **`/v1/system.history`**, per tier from `pg_partition_tree('record_version')` leaves:
  - `partitions`, `rows` (Σ reltuples), `bytes` (Σ `pg_total_relation_size`), `headroomSeq`, `windowDays`
  - Totals: `versionsPerMinute, projectedBytes`, and a warning when versions/min > 20,000.
- `RecordVersionRepository.countBySourceId` is deleted.

---

## 16. Deployment (amends §13 and §14)

```yaml
db:
  command: ["postgres","-c","shared_buffers=512MB","-c","max_wal_size=8GB","-c","checkpoint_timeout=15min",
            "-c","wal_compression=lz4","-c","max_locks_per_transaction=256","-c","autovacuum_vacuum_cost_limit=2000"]
  ports: ["${ELARO_DB_BIND:-127.0.0.1}:5433:5432"]
elaro:
  ports: ["${ELARO_BIND:-127.0.0.1}:8080:8080"]
```

- `application.yml`: `spring.datasource.hikari.data-source-properties.reWriteBatchedInserts: true`. This also covers the Testcontainers `@ServiceConnection` URL.
- Docs:
  - LAN access needs `ELARO_BIND=0.0.0.0`.
  - Expect about 10–15 GB/day of history writes plus 15–20 GB/day of WAL.
  - A 3d track window holds about 40M rows, roughly 20 GB including indexes.
  - NTP must sync before Elaro starts; otherwise the clock guard holds ingest.

---

## 17. Test strategy changes

- **Roadmap §2 guardrail 1 is amended:** "...do not run any tests until 061, **except** the classes listed in a prompt's *Sanctioned test run* section, which are run exactly once with `./gradlew test --tests <class>…` and fixed until green."
- Sanctioned runs:
  - 006a: `SchemaParityIT`, `HistorySchemaInitializerIT`, `PartitionManagerIT`, `EntityMappingIT`, `DatabaseCleanerIT`.
  - 007: `NormalizerTest`, `ContentHasherTest`, `IngestPolicyEngineTest`.
  - 007b: `IngestWriterIT` plus the 006a set.
- **`TestRecords`** helper (007b) seeds through `IngestWriter`. ITs from 011 onward must not seed `record_head`, `record_live` or `record_version` with raw SQL.
- `SchemaParityIT` is scoped to `record_version`:
  - `relkind='p'`;
  - `information_schema.columns` matches the entity's mapped columns (name, nullability, type);
  - no constraint `record_version_source_external_version_key`;
  - a second `SchemaManager` update with captured `org.hibernate.SQL` output contains no statement mentioning `record_version`.

---

## 18. Disposition of the remaining issues

| Issue | Sev. | Resolution |
|---|---|---|
| Touch throttle on observed_at purges current heads; 304 streaks | blocker | Throttle on received OR observed (§5.2 row 11). `staleBefore` uses `last_data_at` (§13). Retention keyframe (§5.2). IT. |
| TTL anchored on a fixed observed_at | major | `ttlAnchor` (§2), keyframes on the anchor axis |
| Standard-tier current entity loses its only version | major | Retention keyframe every window/4, `horizon`, `L=0` for STANDARD |
| asOf misses tracked aircraft | major | Transaction-axis validity `ingested_at + ttl`; keyframe ≤ ttl − poll − 30 s; `L = S(T − ttl − 2 min)` |
| Time-base switching poisons the LATE check | major | Fixed time base per source (`observedAtMode`, readsb `now − seen`); always clamp the future |
| Null-geometry flapping | major | TRACK carry-forward with `POSITION_STALE` (§3) |
| Tombstone predecessor suppresses enter | major | `p.tombstone` terms, `reappear_as_enter`, normalized tombstones, reordered decision table |
| I8 false at rule creation | major | Gate compares against persisted state (self-healing) |
| Recipe migration recomputes the wrong state | major | Head holds persisted geometry; `full_hash` lives only on live; `dirty`; lazy per-source migration |
| Writer startup state and clock jumps | major | `resetState` from the database, clock guard, jump metric (§6.1) |
| prevBbox ghosts | major | `prevPersistedBbox` + `prevLiveBbox`; `live_dropped` resync |
| Partition maintenance fails batches | major | Allocated-seq pre-flight, fail only when uncovered, 1.5-block ensureAhead, PartitionManager lock, transient retry |
| Advisory lock and in-memory state vs. the test context cache (×2) | major | `exclusive` flag, dedicated connection with keepalive, `resetState()` from DatabaseCleaner, seq_clock written every batch |
| kinematic needs h.payload | major | kinematic is never null; LIVE needs only `l.kinematic` |
| I7 at leaf granularity | major | `floorSeq` per source (§13) |
| Chunk deletes on the hottest tier | major | One TRACK window, partition drops only |
| Decision rule order | major | Reordered table, `SKIP`, Δt defined, unit matrix |
| 007 too large | major | 007 / 007b split; sanctioned runs |
| Gate duplicates the predicate language | major | Shared truth-table fixture; conservative on type mismatch |
| TRIGGER_EDGE rate (minor) | — | `edgeMinInterval`, share metric |
| Volatile rule paths, cursor-vs-retention, partial retention, purge flood, outer DELETE bound, tier join, downsampling, stream resolution, skew, batching mechanics, seq_clock granularity, partition math, parity test, LEFT JOIN, observed_at index, phase-1 wiring, DETACH in a transaction, IngestOutcome definitions (minor) | — | All adopted, in the sections above |
| `alert.history_tier` (minor) | — | Rejected (see below) |

---

# Per-prompt changes

**Planner, before 004 (docs):**
- Merge §0–§17 into Architecture.md at the stated places. Delete §25 step 3. §7.1 says "no FK" for `batch_id`.
- **Product Charter:**
  - FR-13: new fields.
  - FR-14: static/full hash, `dirty`, policies, touches, retention keyframe.
  - FR-17: amended (§13).
  - NFR-2: 40k observations/min, ≤ 20k versions/min.
- **Source-Catalog**, columns per source: tier, policy, TTL (transit 5 min), `ttlAnchor`, `observedAtMode`, `significantFields`, `volatileFields`, retention (STANDARD only), licence.
- **Roadmap:**
  - Insert `006a-History-Schema-And-Partitions` (after 006).
  - Rename 007 to `007-Ingest-Normalizer-And-Policy-Engine`.
  - Insert `007b-Ingest-Writer-And-Change-Bus` (after 007).
  - Insert `030a-History-Downsampling` (after 030).
  - Amend guardrail 1 per §17.
- Fix the "prompt 042" Flyway references to 063 (left to 006a for code comments).
- OpenAPI per the Spec changes section below.

**004 Source Framework:**
- `NormalizedRecord` gains `eventTime, quality, observers` (builder). Add `Quality, PositionSource, Observer, Coverage`. `FetchResult.coverage`.
- Descriptor fields per §3, including `observedAtMode`, `ttlAnchor` (tier-dependent default) and `allowNullIsland`. Builder defaults.
- Sealed `IngestPolicy` (`All`, `Sampled`, `Latest`) with `edgeMinInterval`, JSON mapping, `IngestPolicyValidator` (§5.1, keyframe ≤ ttl − pollInterval − 30 s, ALL default keyframe). Add `MotionSpec, FieldThreshold, RecordLimits`.
- Registry validation:
  - top-level field names;
  - TRACK ⇒ TTL and OBSERVED, no `defaultRetention`;
  - SAMPLED/LATEST ⇒ `significantFields`;
  - threshold and motion fields not significant;
  - warnings for non-significant search and tile fields.
- `SourceService`:
  - `update` validates `ingestPolicy` (explicit null clears it);
  - rejects retention on TRACK sources (`RETENTION_TIER_FIXED`);
  - adds `effectiveIngestPolicy(id)` and `effectiveRetention(id)`;
  - publishes `SourceSettingsChangedEvent`;
  - `source_settings.ingest_policy` column.
- Javadoc: `RecordSink.accept` may block. SPI rules 7–13.
- Tests: validator matrix, registry failures, settings round trip, TRACK retention rejection.

**005 Upstream Client And Budgets:**
- `SourceHttp` records a `FetchTrace` (redacted URL, status, ETag, bytes, streaming SHA-256; no bodies).
- `UsageMeter.recordIngest(sourceId, IngestCounts)`. `SourceUsageEntity` gains the §8 columns; the flush upsert accumulates them.
- Tests.

**006 Health Tracking:**
- `recordSuccess(IngestOutcome)`: `last_records_changed = persisted`; last-hour late, clamped and rejected counts.
- `last_data_at` (set on OK fetches with records and on stream flushes). `last_coverage` and `recent_coverage` ring.
- Evaluator rule 5: coverage dropped vs. format change.
- View adds `coverage`, `storage` (§15), `lastDataAt`.
- Tests: coverage/activity table, storage math.

**006a History Schema And Partitions (new, after 006):**
- Entities:
  - rewrite `RecordVersionEntity` (§8/§9, `Short` smallints);
  - rewrite `RecordHeadEntity` to §8 (with `persisted_geom`, no full hash);
  - new `RecordLiveEntity` (with `dirty`, `kinematic` not null), `FetchLogEntity`, `SeqClockEntity` (minute PK);
  - `alert.observed_at`, `alert.record_snapshot`, `trigger_rule.reappear_as_enter`;
  - `source_health.last_data_at`.
- Delete `RecordVersionRepository.countBySourceId` and `RecordHeadRepository.deleteBySourceIdAndObservedAtBefore`.
- `HistorySchemaInitializer`, wired as in §9 (`@DependsOn` postgis, added to `EntityManagerFactoryDependsOnPostProcessor`). `extra_physical_table_types` in main `application.yml`.
- `SpatialIndexInitializer`: GiST on `record_live.geom`, drop the `record_head` GiST, `record_live` storage parameters.
- `PartitionManager`: `ensureAhead` (1.5 blocks), `ensureFor`, `coveredUpTo`, lock, 42P07/42P17 handling, headroom gauge, daily ANALYZE. `drop` uses DETACH CONCURRENTLY on an autocommit connection plus FINALIZE recovery; implemented and tested here, called from 016.
- Properties: `elaro.history.trackBlock=20000000`, `standardBlock=250000000`, `elaro.ingest.writer.exclusive=true` (false in `application-test.yml`). Disable scheduled maintenance in the test profile.
- DatabaseCleaner: partition filter; calls `IngestWriter.resetState()` if the bean exists (`ObjectProvider`).
- `TestPartitions` helper. Update `EntityMappingIT`. Javadoc and comment fixes (Flyway 063).
- Compose: bindings and PostgreSQL flags; `reWriteBatchedInserts` Hikari property.
- Tests:
  - `SchemaParityIT` (§17);
  - `HistorySchemaInitializerIT` (empty plain table → partitioned; non-empty fails);
  - `PartitionManagerIT`: ensure is idempotent; concurrent ensure from two threads; attach succeeds while another session holds an open insert transaction (assert via lock_timeout, no sleeps); drop rejects running inside a transaction; detach and finalize.
- **Sanctioned test run:** the 006a set (§17).

**007 Ingest Normalizer And Policy Engine (rewrite of the existing file, pure code):**
- `IngestRecord`, `FetchLogEntry`, `IngestCommand` (sealed: batch, HeadPurge with `PurgeSpec`), `IngestOutcome` (§6 definitions), `PersistReason` and `Outcome` enums (including `SKIP`, `TOUCH_THROTTLED`).
- `Normalizer`: time base, always-clamp, limits, flags, null island, explicit/effective validUntil, tombstone normalization, keep duplicates, stable sort.
- `ContentHasher`: static and full hash, recipe.
- `SourceIngestProfile` (built from descriptor and settings).
- `IngestPolicyEngine.decide(obs, state, profile, gateResult)`: §5.2 including carry-forward, dead reckoning, thresholds, keyframe and retention keyframe, touch throttle.
- `PersistGate`, `GateSpec`, `NoopPersistGate`.
- `SnapshotContract.assertStableStaticHash`.
- Unit tests: §5.2 parameterised matrix; hash invariance (observed_at, volatile, quality); clamp; limits; carry-forward; recipe stability.
- **Sanctioned test run:** `NormalizerTest`, `ContentHasherTest`, `IngestPolicyEngineTest`.

**007b Ingest Writer And Change Bus (new, after 007):**
- `IngestWriter` per §6:
  - lock (`exclusive`, DriverManager connection, keepalive, reacquire);
  - `resetState()`, clock guard;
  - pre-flight on allocated seq;
  - load query (LEFT JOIN, repair), fold;
  - seq sorted in Java;
  - `batchUpdate` statements; one row per key (asserted); touch count check;
  - `seq_clock` on every batch; `fetch_log` insert;
  - transient retry;
  - lazy recipe migration;
  - HeadPurge chunking with re-enqueue.
- `ChangeBatch` / `ChangeSummary` per §10. `LiveBus` with `onLiveDropped`. Bounded queue (64).
- `TestRecords` helper.
- ITs:
  - 5 observations in one batch under ALL → 5 versions; head and live equal the 5th;
  - LATE has no side effects;
  - clamp;
  - TTL re-observation is a touch;
  - USGS-style fixed `observed_at` refreshes `last_received_at` past `touchMin`;
  - HEARTBEAT vs KEYFRAME;
  - retention keyframe at window/4;
  - SAMPLED deviation and threshold;
  - LATEST;
  - TRACK null geometry carries forward with no SIGNIFICANT;
  - LIVE then PERSIST then LIVE keeps the merged payload correct when a key drops;
  - adding `volatileFields` → 0 versions;
  - adding a significant field → no storm;
  - HeadPurge respects floorSeq and staleBefore;
  - second exclusive writer refuses;
  - restart with an earlier clock waits;
  - `resetState` after truncate;
  - transient-error retry (injected `55P03`);
  - NOT NULL failure fails only that batch;
  - change published before live.
- **Sanctioned test run:** `IngestWriterIT` and the 006a set.

**008 Collector Scheduler:**
- `FetchExecutor`:
  - stamps `receivedAt`;
  - builds `FetchLogEntry` from `FetchTrace` and coverage;
  - writes FAILED, REFUSED, NOT_MODIFIED and DISCARDED rows;
  - updates `last_data_at` on OK with records.
- `PolicyAwareRecordSink` (§7) replaces `CoalescingRecordSink`: one outstanding batch, one fetch row per flush.
- Lifecycle: phase 1 → PartitionManager schedule → IngestWriter (lock, state, clock guard) → TriggerEngine → scheduler.
- Probe previews include `eventTime`, `quality`, `observers`.
- Tests: ALL ordering; blocking at the cap and drop on timeout; SAMPLED latest-wins by `observedAt`; fetch_log rows per status; `last_data_at` not touched by 304.

**009 USGS:**
- `observedAt = properties.updated`, `eventTime = properties.time`, `volatileFields = [updated]`, `licence = "US-PD"`, STANDARD, no TTL.
- The revised fixture creates version 2 with `SIGNIFICANT`.
- An unchanged re-poll is a touch.

**010 Sources API:**
- DTOs and mappers for the new descriptor, settings and health fields.
- PUT `ingestPolicy` (400 with the validator's errors). Retention on TRACK → 400 `RETENTION_TIER_FIXED`.
- `FetchOutcome` counts.
- `GET /v1/sources/{id}/fetches` (admin, keyset on id).
- Tests.

**011 Records Changes API:**
- Current reads from head ⋈ live with the merged payload.
- State in the prompt: **"No index on `record_live.observed_at`. Do not add one."**
- `order`, `watermarkSeq`, `timeField`, asOf (§12, including `horizon` and the three 400 codes), `include=observer` gating.
- `versions` ordered by seq. History `ingested` via `seq_clock`. DTO fields (`live = dirty`, `qaFlags`).
- Use `TestRecords` only.
- Tests:
  - merged payload after LIVE;
  - snapshot paging stable under mutation;
  - asOf before and after a revision;
  - asOf for a stationary keyframed aircraft at a T between keyframes;
  - asOf for an NWS alert that is long-valid (STANDARD, L=0);
  - 400 beyond the horizon;
  - `effective` vs `observed` for a revised quake.

**012 SSE Change Stream:**
- Two lanes per client; LiveBus subscription; `live`, `touch`, `resync` (gap, live_cap, live_dropped); no `id:` on live or touch.
- Three-box filter with `leftFilter`. Live updates go into the conflation map during replay.
- `reset` when `after < RetentionState.lowestRetainedSeq`.
- `StreamLimits.liveIntervalMs` and `liveMaxEntities`.
- Tests: conflation under burst; resync after onGap; resync on LiveBus drop; a ghost case with live=false leaving via the persisted box; `reset` below the floor; no id lines.

**013 Vector Tiles:**
- `record_live l JOIN record_head h`; `l.geom` filter; merged properties; `l.observed_at`; cap order `l.observed_at DESC`.
- Test: a live position appears before any new version.

**014 Trigger Engine And Alerts:**
- `RuleSqlBuilder`: seq-only `prev_seq` join, enter/exit per §11 (tombstone and `reappear_as_enter` terms), HEARTBEAT excluded, cooldown seeded on `observed_at`.
- Java cooldown. Alert gets `observed_at` and `record_snapshot`; `getAlert` falls back to the snapshot.
- `TriggerPersistGate` (`@Primary`): persisted-state comparison, `edgeMinInterval` applied by the engine in 007, exact evaluation with conservative type handling, `GateSpec` per source.
- Rule validation rejects volatile paths. `reappearAsEnter` field.
- `testTrigger since` → seq via `seq_clock`. Templates `{{eventTime}}`, `{{time}}`.
- Shared fixture `trigger/operator-truth-table.json` used by `RuleSqlBuilderIT` and `TriggerPersistGateTest`.
- Tests:
  - geofence crossing between samples → TRIGGER_EDGE plus enter and exit;
  - rule created while live and persisted diverge → converges on the next observation;
  - flapping at a threshold is bounded by `edgeMinInterval`;
  - HEARTBEAT does not re-fire match;
  - a tombstone, then REAPPEAR inside, fires enter;
  - deterministic cooldown;
  - a missing prev fires nothing.

**016 Retention Settings System:**
- `RetentionJob` per §13 (not `@Transactional`):
  - track drops via `PartitionManager.drop`;
  - standard chunked deletes with the outer seq bound;
  - std leaf drops;
  - floor computation;
  - one HeadPurge command with pre-check;
  - consumer clamp;
  - fetch_log and seq_clock pruning;
  - `RetentionState` bean.
- `RetentionSettings.trackWindow` (3d) and `provenance` (30d). `/v1/system.history`.
- Concurrency paragraph rewritten (I1, I7).
- Tests:
  - track leaf dropped only past the window;
  - standard chunked deletes;
  - a head survives a drop of its referenced version while observed;
  - reappearance after a purge restarts at version 1 with no surviving older versions;
  - failed deletes → no HeadPurge for that source;
  - 304 streak → no purge;
  - consumer clamp;
  - fetch_log retention.

**017 OpenAPI Conformance:** cover every operation and schema in the Spec changes section below.

**018 End To End Milestone:**
- Add steps:
  - an identical re-fetch → no version and no change event, a touch, `fetch_log.versions_written = 0`;
  - the revision event has `persistReason = SIGNIFICANT`;
  - asOf before the revision returns version 1;
  - `/v1/system.history` reports two tiers.
- Operator guide: policies, tiers and windows, `ELARO_BIND`, storage projection, NTP.
- Smoke script checks `watermarkSeq`.

**Other affected prompts:**
- **020:** push uses `IngestRecord`; custom sources declare `observedAtMode`, `ttlAnchor` (STANDARD), `volatileFields`, `significantFields`; editing them triggers lazy recipe migration; `principal` on fetch rows; per-source records/min cap.
- **021 / 024:** citations use persisted seqs.
- **023:** skip non-text reasons via `text_hash`; search on the `effective` time axis.
- **025:** activity from `record_live`; counts by `persist_reason`.
- **029 / 030 / 036 / 037 / 044 / 048:** declare §5.3 defaults and §2 time bases (readsb `now − seen`, `lastPosition`).
- **057:** LiveBus.
- **062:** measure versions/min, persist ratio, TRIGGER_EDGE share, WAL.
- **063:** Flyway baseline holds the tiered DDL; `PartitionManager` stays; `HistorySchemaInitializer` removed.

**030a History Downsampling (new, after 030):**
- Rewrite-and-swap per §13 (keep the max seq per entity per leaf, plus head- and alert-referenced seqs).
- Settings `history.track.rawWindow` and per-source `downsampleInterval`.
- `/versions` shows `downsampled: true`. asOf widening plus `approximate`. `testTrigger` warns.

---

# Spec changes (OpenAPI)

**Schemas:**
- `Record`: + `version`, `seq`, `observedAt`, `persistedObservedAt`, `eventTime`, `quality` (`Quality`), `live` (bool), `qaFlags` (int).
- `RecordVersion`: + `persistReason` (enum FIRST, REAPPEAR, SIGNIFICANT, TOMBSTONE, CHANGE, SAMPLE, KEYFRAME, HEARTBEAT, TRIGGER_EDGE), `ingestedAt`, `receivedAt`, `batchId`, `quality`, `observer` (array of `Observer`), `downsampled`.
- `RecordPage`, `FeatureCollection`: + `watermarkSeq`, `asOf`, `approximate`, `horizon`.
- `Feature.properties`: + `observedAt`, `version`, `live`.
- `Change`: + `prevSeq`, `historyTier`, `persistReason`, `observedAt`, `eventTime`, `qaFlags`, `prevPersistedBbox`, `prevLiveBbox`, `leftFilter`.
- New `LiveEvent` (sourceId, externalId, kind, observedAt, validUntil, version, geometry, bbox, prevBbox, properties, quality, leftFilter).
- New `TouchEvent` (sourceId, externalId, observedAt, validUntil, bbox).
- New `ResyncEvent` (reason enum `gap|live_cap|live_dropped`, watermarkSeq).
- New `Quality`, `Observer`, `Coverage`.
- New `IngestPolicy` (oneOf All / Sampled / Latest: mode, minInterval, keyframe, minDeviationMeters, thresholds, edgeMinInterval).
- `SourceDescriptor`: + `licence`, `termsUrl`, `historyTier`, `observedAtMode`, `ttlAnchor`, `defaultIngestPolicy`, `volatileFields`, `significantFields`, `motion`, `thresholds`, `defaultRetention`.
- `SourceSettings`, `SourceSettingsUpdate`: + `ingestPolicy` (nullable).
- `SourceHealth`: + `coverage`, `storage` (observationsPerMinute, versionsPerMinute, persistRatio, triggerEdgeShare, bytesPerDay, projectedBytes, lateLastHour, clampedLastHour), `lastDataAt`.
- `FetchOutcome`, `IngestOutcome`: + `persisted`, `live`, `touched`, `touchesThrottled`, `late`, `skipped`, `byReason`.
- New `FetchLogEntry` / `FetchPage` (keyset).
- `SystemInfo.history` (per tier: partitions, rows, bytes, headroomSeq, windowDays; totals; warning).
- `Alert`: + `observedAt`, `recordSnapshot`.
- `TriggerRule`, `TriggerRuleCreate`/`Update`: + `reappearAsEnter`.
- `RetentionSettings`: + `trackWindow`, `provenance`.

**Operations and parameters:**
- `listRecords`: + `asOf`, `order` (`observed|id`), `timeField` (`effective|observed|event|ingested`), `include` (`observer`).
- `getRecord`: + `include`.
- `listHistory`: + `timeField`.
- `streamChanges`: + `live`, `touches`; event types `live`, `touch`, `resync`.
- New `GET /v1/sources/{id}/fetches` (`listSourceFetches`, admin, `after`, `limit`).
- `updateSourceSettings`: `ingestPolicy`; 400 `RETENTION_TIER_FIXED`.

**Error codes:** `ASOF_FILTER_REQUIRED`, `ASOF_BEYOND_RETENTION`, `RETENTION_TIER_FIXED`, `INGEST_POLICY_INVALID`, `TRIGGER_PATH_VOLATILE`.

---

# Rejected suggestions

| Suggestion | Reason |
|---|---|
| `IngestCommand.Reconcile` scan on rule change, plus suppressing alerts that predate the rule | Comparing the gate against persisted state gives the same convergence on the next observation with no scan, no extra command and no race with in-flight batches. |
| Recipe migration as a startup writer command | A lazy per-source check on the writer thread at the first batch (cold branch, own transactions) covers startup and 020 edits with one mechanism. |
| Excluding head-referenced seqs from deletes and drops (needs a `record_head(seq)` index) | Retention keyframes every window/4 keep observed entities' versions recent at negligible cost, and leaf drops stay unconditional. |
| `posStale` as a significant adapter field | Its own flapping would persist SIGNIFICANT versions. Writer carry-forward with a qa flag is generic and never touches the hash. |
| Conservative "any referenced path changed" gate | Under SAMPLED, altitude and speed change on almost every poll, so any rule on them would force near-ALL persistence and break the budget. Exact evaluation plus a shared fixture plus conservative handling of type mismatch keeps both safety and volume. |
| Per-rule hysteresis or deadband, and a flip-rate estimate in `testTrigger` | `edgeMinInterval` (self-healing under persisted-state comparison) and the TRIGGER_EDGE share warning bound the volume with less surface. Can be revisited after 062. |
| Evaluating the gate inside `PolicyAwareRecordSink` | Duplicates writer state in the sink. Flush resolution is documented instead. |
| Stopping ingest on a forward clock jump | On a home server, stopping is worse than retention lag. Alarm plus metric only. A backward start-up skew waits, not fails. |
| `alert.history_tier` | A seq-only lookup probes one leaf per tier (2 probes). Not worth a column. The edge join is also seq-only. |
| A FIRST-outcome probe for surviving versions | I7 is exact via `floorSeq`, so the probe would never fire. |
| Keeping the `unnest` mandate | `batchUpdate` with `reWriteBatchedInserts` is simpler for the agent and fast enough at about 333 versions/s. One row per key is still asserted. |
| Hourly `seq_clock` with a cached last hour | Replaced by minute buckets written on every batch: no cache, a tighter asOf scan, and retention conservative by ≤ 1 min. |
| Per-source retention on TRACK, and another tier for shorter aircraft history | One window per tier removes the costliest path. A third tier value can be added later if wanted. |
| Putting `extra_physical_table_types` in the test YAML too | The test profile file (`application-test.yml`) layers on the main file. Duplicating it invites drift. |
| A two-lane ingest queue, per-source auto-throttling, TimescaleDB, `ingested_at` partitioning, composite FK, carry-forward version tables | Rejected earlier for unchanged reasons: one outstanding batch bounds delay; auto-throttling hides data; portability and licence; the seq design prunes for free; I5 + I6 suffice. |

Files referenced: `/Users/adamallard/Documents/GitLab/elaro/docs/Architecture.md`, `/Users/adamallard/Documents/GitLab/elaro/docs/Roadmap.md`, `/Users/adamallard/Documents/GitLab/elaro/src/main/java/ai/elaro/config/SchemaBootstrap.java`, `/Users/adamallard/Documents/GitLab/elaro/src/main/resources/application.yml`, `/Users/adamallard/Documents/GitLab/elaro/src/test/resources/application-test.yml`, `/Users/adamallard/Documents/GitLab/elaro/src/test/java/ai/elaro/test/DatabaseCleaner.java`, `/Users/adamallard/Documents/GitLab/elaro/prompts/007-Ingest-Writer-And-Change-Bus.md`.