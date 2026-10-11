# 111 — Exports Archive And Offline Packs

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/111-CR-Exports-Archive-And-Offline-Packs.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–133).
- **Previous Completion Report:** read `completion-reports/110-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §2 row "Flutter mobile" (offline packs); §3 rows "Bulk exports", "Offline packs", "Cold archive", "Snapshots"; §8 item D.60 ("059b")
- `docs/Core-Data-Model.md` §8 (`record_version` columns, `seq_clock`), §9 (tier partitions, `PartitionManager`), §12 asOf algorithm (`S(T)`, `L`, validity on the transaction axis), §13 (RetentionJob steps, `floorSeq`, I7, consumer clamp)
- Completion Reports of 014 (formats, `watermarkSeq`), 016 (MVT tile query), 019 (`RetentionStep` beans, `RetentionState`), 037 (asOf tiles, `/track`), 039 (private sources), 045 (raster tile renderer), 108 (MBTiles/PMTiles export: reuse its writer if present), 109 (`LicenceEgressGuard`, `ManifestSigner`, attributions)
- GeoParquet 1.1.0 spec (`geo` metadata, WKB encoding, `covering.bbox`); OGC GeoPackage 1.4 (`gpkg_contents`, `gpkg_geometry_columns`, GeoPackageBinary header, RTree extension); FlatGeobuf spec; GPX 1.1 schema; PMTiles v3 spec (header, directories, Hilbert tile ids)

## Objective

Give the operator durable, portable copies of Foresignal's data: async bulk exports (GeoParquet 1.1, GeoPackage, FlatGeobuf, GPX) with a licence sidecar and a signed SHA-256 manifest; `atSeq` snapshots and legal-style holds that survive retention; a cold archive that writes data out before retention deletes it; and PMTiles v3 offline packs for phones and field laptops.

## Tasks

### 1. Export jobs (`ai.foresignal.export`)

- **Entity `export_job`:** `id` (ULID), `kind` (`EXPORT | SNAPSHOT | PACK`), `format`, `spec` jsonb, `status` (`QUEUED | RUNNING | DONE | FAILED | CANCELLED | EXPIRED`), `principal` (token id), `row_count`, `bytes`, `dir`, `manifest_sha256`, `licence_summary` jsonb, `watermark_seq`, `personal_use` bool, `created_at`, `started_at`, `finished_at`, `expires_at` (default 7 d, `exports.ttl`), `error`, `progress` (0–100).
- **Spec:** `sources[]`, `kinds[]`, `bbox` or `placeId` (036), `from`/`to` with `timeField` (014 semantics), `mode` (`current | history | asOf | atSeq`), `asOf`/`atSeq`, `fields` (payload allowlist), `flatten` (promote `fieldSpecs` keys to columns), `includePrivate` (admin only), `personalUse`.
- **Worker:** bounded executor (`exports.maxConcurrent`, default 1) on virtual threads. Stream rows with keyset reads (current: `record_head ⋈ record_live`; history: `record_version` by seq ranges in pages of 10,000; asOf/atSeq: task 3). Never load a full result into memory. Write to `${FORESIGNAL_DATA_DIR}/exports/{id}/tmp`, fsync, atomic rename. Limits: `exports.maxRows` (5,000,000), `exports.diskQuotaGb` (20; refuse with `409 EXPORT_QUOTA` when exceeded). Cancellation is checked between pages.
- **Guards:** `LicenceEgressGuard.allows(EXPORT, …)` per source from 109. FORBIDDEN and LOCAL_ONLY records are excluded unless `personalUse=true`; then the sidecar states "personal use only — do not redistribute" and lists those sources. Private sources (039) need admin plus `includePrivate=true`.
- **API:**
  - `POST /v1/exports` (agent, `alwaysAuthenticated` from 023) → 202 with the job.
  - `GET /v1/exports` (agent; own jobs, or all for admin) and `GET /v1/exports/{id}`.
  - `GET /v1/exports/{id}/download` (agent): single file, or `?bundle=zip` with data, `manifest.json`, `manifest.json.jws`, `licence.json`, `ATTRIBUTION.txt`. Supports `Range` and `ETag` = sha256.
  - `GET /v1/exports/{id}/manifest` and `DELETE /v1/exports/{id}` (cancel or delete).
  - Errors: `400 EXPORT_SPEC_INVALID`, `409 EXPORT_QUOTA`, `410 EXPORT_EXPIRED`.

### 2. Formats (each a `ExportWriter` implementation)

- **GeoParquet 1.1** (default):
  - Columns: `source_id`, `external_id`, `kind`, `seq` (int64), `version` (int32), `observed_at`/`event_time`/`valid_until`/`ingested_at` (timestamp micros, UTC adjusted), `title`, `payload` (JSON string), optional flattened columns, `geometry` (WKB binary), `bbox` struct `{xmin,ymin,xmax,ymax}` (float64).
  - File metadata key `geo`: `{"version":"1.1.0","primary_column":"geometry","columns":{"geometry":{"encoding":"WKB","geometry_types":[…observed types…],"bbox":[…],"covering":{"bbox":{"xmin":["bbox","xmin"],"ymin":["bbox","ymin"],"xmax":["bbox","xmax"],"ymax":["bbox","ymax"]}}}}}`. CRS omitted = OGC:CRS84.
  - Row groups of 64k rows, sorted by `(kind, source_id, external_id, seq)`.
  - **Avoid the Hadoop baggage:** use parquet-java (`parquet-column` plus the minimum of `parquet-hadoop`) with `LocalOutputFile` and the Hadoop-free `ParquetConfiguration`/`PlainParquetConfiguration` (parquet-java ≥ 1.14), or Carpet (Apache-2.0) if it gives a smaller tree. Exclude `hadoop-hdfs*`, `hadoop-yarn*`, `hadoop-mapreduce*`, Jetty, Jersey, Guava shading and log4j bindings. If `hadoop-common` is unavoidable, keep it with all its transitive dependencies excluded, and record `./gradlew dependencies --configuration runtimeClasspath` in the Completion Report. Pick a codec that works on that classpath (try ZSTD via zstd-jni, else SNAPPY, else UNCOMPRESSED), and prove it with the round-trip test. Verify versions on Maven Central.
- **GeoPackage 1.4:** via `org.xerial:sqlite-jdbc`. Set `application_id = 0x47504B47` and `user_version = 10400`.
  - Required `gpkg_spatial_ref_sys` rows (4326, −1, 0), `gpkg_contents`, `gpkg_geometry_columns`.
  - One feature table per kind with flattened columns plus `payload` TEXT.
  - Geometry as GeoPackageBinary: `GP`, version 0, flags (little-endian, envelope type 1), `srs_id 4326`, envelope, WKB.
  - Spatial index via the RTree extension (`gpkg_extensions` row plus triggers).
  - Licence in `gpkg_metadata` / `gpkg_metadata_reference`.
- **FlatGeobuf:** `org.wololo:flatgeobuf` (check the licence is permissive, record it in `THIRD_PARTY_NOTICES.md` if required) with JTS. Columns from flattened fields plus `payload` string; geometry type `Unknown` when mixed; packed Hilbert R-tree index on.
- **GPX 1.1:** history mode only. One `<trk>` per entity of TRACK-tier sources (`<trkseg><trkpt lat lon><ele>` from the payload altitude in metres `<time>`) and `<wpt>` for point records. `<metadata><copyright>`/`<link>` from the licence summary; escape XML.

### 3. `atSeq` snapshots and holds

- **Snapshot** (`POST /v1/snapshots`, admin): state at seq `S` (default the current `watermarkSeq`; or `asOf` translated with `S(T)`).
  - Rebuild from `record_version` with the Core-Data-Model §12 query (`DISTINCT ON (source_id, external_id) … seq BETWEEN L AND S ORDER BY … seq DESC`, validity on the transaction axis, tombstones dropped), per tier.
  - Below a source's `floorSeq`, read from `record_version_held` (below) or fail with `400 SNAPSHOT_BEYOND_RETENTION`.
  - Output GeoParquet plus manifest plus JWS under `${FORESIGNAL_DATA_DIR}/snapshots/{id}/`. Snapshots don't expire unless `expiresAt` is set.
- **Holds** (`POST/GET/DELETE /v1/holds`, admin): `{name, reason, sources[], kinds[], from, to (time or seq; to may be open), expiresAt?}`. Limit `holds.max` (20).
- **Hold mechanism: archive-then-delete.** Don't block partition drops. A `HoldCopyStep` (`RetentionStep`, ordered before every delete or drop) copies rows that are about to be removed and fall inside an active hold into **`record_version_held`** (same columns plus `hold_id`, PK `(hold_id, seq)`, plain table with GiST and `(source_id, external_id, seq)` indexes) with `INSERT … ON CONFLICT DO NOTHING`. If the copy fails, retention skips that source this run, the same failure rule as Core-Data-Model §13 step 2. Leaves stay droppable and I7/`floorSeq` are unchanged.
- `GET /v1/holds/{id}/versions` (admin, keyset). `/v1/system` reports held rows and bytes. Deleting a hold deletes its copies.

### 4. Cold archive

- **Settings `archive`:** `enabled` (false), `sources` (allowlist; empty = all non-private STANDARD and TRACK sources), `root` (`${FORESIGNAL_DATA_DIR}/archive`), `target` (`local | s3`), `s3{endpoint, bucket, prefix, region, pathStyle}` plus secrets `accessKey`/`secretKey` (encrypted), `keepLocal` (true).
- **`ArchiveStep`** (`RetentionStep`, after `HoldCopyStep`, before the deletes in Core-Data-Model §13 steps 1–2): for each TRACK leaf about to be dropped and each STANDARD chunk about to be deleted, write the rows as GeoParquet (task 2 writer) to the hive layout `record_version/tier={std|trk}/source_id={id}/date={yyyy-mm-dd of ingested_at UTC}/part-{fromSeq}-{toSeq}.parquet` plus a `_manifest.json` (rows, sha256, licence summary). Verify by reading back the row count **before** the delete or drop proceeds. On failure, skip deletion for that source or leaf this run and alarm `foresignal.archive.failed`.
- **S3 upload** (async after local write): a minimal SigV4 client on `java.net.http` (`PutObject`; multipart above 64 MB), with the endpoint checked by the outbound URL policy (035). Delete the local copy only when `keepLocal=false` and the upload checksum matches. No AWS SDK.
- `GET /v1/archive/catalog?source=&from=&to=` (admin) lists partitions and files. The Operator Guide shows DuckDB access (`read_parquet('archive/record_version/**/*.parquet', hive_partitioning=true)`).
- Licence: `ARCHIVE` egress (operator-owned storage) is allowed for every licence; manifests record the licences anyway.

### 5. PMTiles v3 offline packs

- `POST /v1/packs` (agent, `alwaysAuthenticated`): `{name, bbox | placeId, minZoom (≥ 0), maxZoom (≤ 14), vector: {sources[], kinds[], asOf?}, raster: {productId, frame: "latest" | frameId}?}` → one `.pmtiles` per tile type (MVT pack and/or PNG pack). Cap `packs.maxTiles` (200,000; `400 PACK_TOO_LARGE`, with the computed count).
- **Writer** (`ai.foresignal.export.pmtiles.PmTilesWriter`, in-house; reuse 108's if it exists):
  - Tile id: Hilbert index per the spec (cumulative tile count of lower zooms plus the Hilbert position).
  - Tiles generated via the 016/037 MVT query and the 045 renderer, deduplicated by content hash (`run_length` for consecutive identical tiles, shared offsets for repeats).
  - Entries sorted by tile id; directories encoded as varint count, delta tile ids, run lengths, lengths, offsets (0 = contiguous); gzip internal compression.
  - Root directory ≤ 16,384 bytes, else leaf directories.
  - 127-byte little-endian header: magic `PMTiles`, version 3, section offsets and lengths, addressed/entry/content counts, `clustered=1`, compressions, tile type (1 MVT, 2 PNG), zooms, bounds and centre in e7.
  - Metadata JSON: `name`, `vector_layers` (from field specs), `attribution` (109 aggregate), `generated`, `watermarkSeq`.
- Packs are downloaded (`/v1/exports/{id}/download` with `Range`), not proxied as `pmtiles://` from upstream. Licence and private rules as task 1, with `EXPORT` egress.

### 6. Retention, system and docs

- `ExportRetentionStep`: delete expired export and pack directories and set `EXPIRED`; orphan sweep of directories with no row.
- `/v1/system`: `exports` block (queued, running, bytes) and `archive` block (last run, bytes, failures).
- Metrics `foresignal.export.rows`, `foresignal.export.duration`, `foresignal.archive.bytes`.
- `docs/OpenAPI.json`: schemas `ExportJob`, `ExportSpec`, `ExportManifest`, `Snapshot`, `Hold`, `ArchiveSettings`, `ArchivePartition`, `PackSpec`; all operations above with `x-foresignal-scope`; settings key `archive` via the settings API (secrets write-only). Update `AccessPolicy`. Regenerate Postman with BODIES for `createExport`, `createSnapshot`, `createHold`, `createPack`.
- MCP (025): `create_export`, `get_export` (agent). Return the download URL, never file bytes.
- `docs/Operator-Guide.md`: "Exports, snapshots and holds", "Cold archive" (S3/MinIO setup, DuckDB and GDAL access), "Offline packs" (load in MapLibre via the `pmtiles` protocol from a local file, flutter_map PMTiles). `THIRD_PARTY_NOTICES.md` for new dependencies.

## Tests to write (do not run)

- `GeoParquetWriterTest`: round trip with the parquet reader; `geo` metadata JSON matches 1.1.0 including `covering`; timestamps UTC micros; mixed geometry types listed. `ParquetClasspathTest`: no `org.apache.hadoop.hdfs`/`yarn`/`mapreduce` classes on the runtime classpath.
- `GeoPackageWriterTest`: required tables and rows, `application_id`, GeoPackageBinary header bytes, RTree populated, opens with plain SQLite queries.
- `FlatGeobufWriterTest` (read back with the same library) and `GpxWriterTest` (validate against the GPX 1.1 XSD in test resources).
- `ExportJobIT`: lifecycle, cancellation, quotas, keyset streaming of 50k rows with bounded memory, bundle zip contents, `Range` download, expiry → 410.
- `ExportLicenceIT`: FORBIDDEN sources excluded; `personalUse` includes them and the sidecar says so; private needs admin; manifest JWS verifies against the JWKS.
- `SnapshotIT`: snapshot at a seq between two revisions returns version 1; tombstoned entities absent; beyond floor → 400 unless held.
- `HoldCopyStepIT`: rows in a hold survive a TRACK leaf drop and a STANDARD chunk delete; a copy failure skips the deletes; `floorSeq` unchanged.
- `ArchiveStepIT`: hive layout and manifest; deletion happens only after verified write; failure keeps the rows. `S3UploadIT` (MinIO Testcontainer): SigV4 put, multipart, checksum, `keepLocal=false` cleanup.
- `PmTilesWriterTest`: Hilbert tile ids against spec vectors; directory varint round trip; header layout; dedup run lengths; leaf directories when the root exceeds 16 KB; read back with an in-test reader. `PackIT`: an MVT pack for a small bbox; `PACK_TOO_LARGE`.

## Acceptance criteria

- GeoParquet exports open in DuckDB/GDAL and declare GeoParquet 1.1 metadata; GeoPackage opens in QGIS (manual check documented); no Hadoop server stack on the runtime classpath.
- Every export, snapshot and pack carries `licence.json`, `ATTRIBUTION.txt` and a signed SHA-256 manifest.
- Held and archived rows are written out before retention removes them, and retention still drops leaves.
- PMTiles packs validate with the in-test reader and the `pmtiles verify` CLI (manual check documented).
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
2. **Commit** all changes with the message `111: Exports Archive And Offline Packs`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/111-CR-Exports-Archive-And-Offline-Packs.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `111: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
