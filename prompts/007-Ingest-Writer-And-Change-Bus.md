# 007 — Ingest Writer And Change Bus

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/007-CR-Ingest-Writer-And-Change-Bus.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`.
- **Previous Completion Report:** read `completion-reports/006-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §6 (single-writer design, write algorithm, sequence), §7.1 (`record_version`, `record_head`), §8.1 (ChangeBus)
- `docs/Product-Charter.md` FR-13 to FR-16

## Objective

Turn adapter output into versioned records with a **gap-free, commit-ordered** `seq`, and announce each committed batch on an in-process change bus. This is the heart of the broker, so correctness matters more than cleverness.

## Tasks

### 1. IngestQueue (`ai.elaro.ingest`)

- A bounded `ArrayBlockingQueue<IngestBatch>` with capacity 64.
- `IngestBatch(String sourceId, List<NormalizedRecord> records, CompletableFuture<IngestOutcome> result)`.
- `CompletableFuture<IngestOutcome> submit(sourceId, records, Duration timeout)` blocks up to the timeout when full, then fails with `IngestBackpressureException`.
- Expose a gauge `elaro.ingest.queue.depth`.

### 2. RecordNormalizer

For each record, using the source's descriptor:

- Reject a record (count it; never throw for the whole batch) when:
  - `externalId` is blank or longer than 256 characters
  - `kind` is not in `descriptor.kinds`
  - `observedAt` is null
  - the payload is not serializable
- Geometry: force SRID 4326 and drop Z/M (store 2D). Invalid geometries go through JTS `GeometryFixer`; if the result is empty, use null geometry plus a warning counter. Coordinates outside ±180 / ±90 cause the record to be rejected.
- `validUntil`: use the record's value if present; otherwise `observedAt + recordTtl` if the descriptor has a TTL; otherwise null.
- `searchText`: lowercase concatenation of the `searchFields` values found in the payload (top-level or dot-path), space-separated, ≤ 1000 chars.
- Duplicate `externalId`s within one batch: keep the **last** occurrence.

### 3. ContentHasher

- SHA-256 hex over canonical JSON of `{kind, observedAt (ISO millis UTC), validUntil, geometryWkt, payload, tombstone}`.
  - Payload keys are sorted recursively.
  - Numbers are serialized in plain (non-scientific) form, and integral doubles are rendered without a trailing `.0`, so `5` and `5.0` hash equally.
  - Geometry is WKT at a fixed precision of 1e-7 degrees (JTS `PrecisionModel(1e7)`).
- Must be deterministic across JVM runs, independent of `HashMap` ordering.

### 4. IngestWriter

- One dedicated **platform** thread named `elaro-ingest-writer`, started on `ApplicationReadyEvent` and stopped gracefully on shutdown (drain the queue with a 30 s limit).
- For each batch, inside a `TransactionTemplate`:
  1. Normalize.
  2. Load the heads for the batch's externalIds in one query: `SELECT external_id, content_hash, version, tombstone FROM record_head WHERE source_id = ? AND external_id = ANY(?)`.
  3. Decide which records to write:
     - skip a record whose hash equals the head's hash
     - skip a tombstone when there is no head, or the head is already tombstoned
  4. Allocate `seq` values for the records to write in one call: `SELECT nextval('record_seq') FROM generate_series(1, ?)`. Assign them in batch order.
  5. Batch insert into `record_version` with `JdbcTemplate.batchUpdate`. Geometry goes in as `ST_GeomFromWKB(?, 4326)` from JTS `WKBWriter` bytes (or null); payload as `?::jsonb` from a JSON string.
  6. Batch upsert `record_head` with `INSERT … ON CONFLICT (source_id, external_id) DO UPDATE SET seq, version, kind, observed_at, valid_until, geom, content_hash, tombstone, search_text`.
- After commit, publish a `ChangeBatch` on the `ChangeBus`, then complete the future with `IngestOutcome(seen, changed, rejected, fromSeq, toSeq)`. When nothing changed, `fromSeq` and `toSeq` are null and nothing is published.
- If a batch fails (DB error), roll back, complete the future exceptionally, log, and **continue** with the next batch. The writer thread must never die. Wrap the loop body accordingly.
- Metrics: counters `elaro.ingest.records.seen`, `.changed` and `.rejected`, tagged by source; a timer `elaro.ingest.batch.duration`.

### 5. ChangeBus (`ai.elaro.stream`)

- `ChangeBatch(String sourceId, long fromSeq, long toSeq, List<ChangeSummary> changes)`.
- `ChangeSummary(seq, sourceId, externalId, kind, version, observedAt, tombstone, Envelope bbox /* nullable */)`, where `bbox` is the minLon/minLat/maxLon/maxLat of the geometry.
- `subscribe(String name, ChangeListener listener, int queueCapacity)` returns a `Subscription`.
  - `ChangeListener { void onBatch(ChangeBatch b); void onGap(long lastDeliveredSeq); }`
  - Each subscriber has its own bounded queue and its own **virtual** thread for delivery, so a slow subscriber never blocks the writer.
  - On queue overflow: drop the batch, mark the subscriber lagging, and when the queue drains call `onGap(lastDeliveredSeq)` once, so the subscriber catches up from the database.
- `publish(ChangeBatch)` is non-blocking.
- `long lastPublishedSeq()`, initialized at startup from `SELECT coalesce(max(seq),0) FROM record_version`.

## Tests to write (do not run)

- `ContentHasherTest`: key order independence, `5` vs `5.0`, nested maps and lists, geometry precision, a change in any field changes the hash.
- `RecordNormalizerTest`: each rejection rule, TTL application, geometry fixing and 2D forcing, out-of-range coordinates, in-batch dedupe, search text building.
- `IngestWriterIT`:
  - first write creates version 1 with head
  - identical re-submit writes nothing and publishes nothing
  - a changed payload creates version 2 and updates the head
  - tombstone handling (existing head, missing head, already tombstoned)
  - `seq` strictly increasing across batches **and** across sources submitted concurrently from several threads (assert commit order equals seq order by subscribing to the bus and checking monotonicity)
  - a DB failure in one batch (e.g. inject via a payload that violates a constraint) doesn't stop later batches
  - geometry stored with SRID 4326
- `IngestQueueTest`: back-pressure timeout.
- `ChangeBusTest`: fan-out to multiple subscribers; a slow subscriber doesn't block others or the publisher; overflow leads to `onGap` with the correct seq; unsubscribe.

## Acceptance criteria

- Only the writer thread writes `record_version` and `record_head`.
- `seq` is commit-ordered by construction (single writer); this is documented in the class Javadoc with the reasoning from Architecture §6.1.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests are first run in prompt 061 (Full Test Run And Fix).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`). **Do not add Flyway, Liquibase or SQL migration files.**
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor, which are AGPL. They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `007: Ingest Writer And Change Bus`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/007-CR-Ingest-Writer-And-Change-Bus.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `007: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
