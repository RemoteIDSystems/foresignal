# 111-CR-Exports-Archive-And-Offline-Packs

| | |
|---|---|
| Prompt | 111-Exports-Archive-And-Offline-Packs.md |
| Status | Complete (raster packs and the snapshot/export point-in-time paths are covered by code and unit tests; see Issues) |
| Implementation commit | `6a002010a2aa7b78d4c3a1ca7df73fbf2e11ae45` |
| Date | 2026-10-16 |

The work spans three commits: `e4a2ed4` and `7f1827e` (work in progress) and the implementation commit above,
which rewrote most of the work in progress and fixed the defects listed under "Issues found".

## Summary

Durable, portable copies of Foresignal's data. Compiles; tests written, not run.

- **Export jobs (`ai.foresignal.export`):** `export_job` (`ExportJobEntity`, JPA entity name `BulkExportJob`,
  repository `BulkExportJobRepository`) with `spec` (the request) and `plan` (allowed sources, exclusions,
  point in time). `ExportJobService` (bean `bulkExportJobService`) submits, runs, lists (keyset, own jobs or all
  for admin), downloads, cancels and deletes. Jobs run on one bounded `ExportWorker` (virtual threads,
  `exports.maxConcurrent`, default 1) shared by exports, snapshots and packs, submitted **after commit**.
  Files go to `{dir}/tmp`, are fsynced and renamed; `exports.maxRows`, `exports.diskQuotaGb` (`409
  EXPORT_QUOTA`), `exports.ttl` (7 d), cancellation between pages, interrupted jobs failed at start-up.
- **Query (`ExportQueryService`):** keyset pages of 10,000, each its own read transaction with a 5-minute
  statement timeout; current from `record_head ⋈ record_live` per kind keyed on `(source_id, external_id)`;
  history per kind and source keyed on `(external_id, seq)`; asOf/atSeq with the Core-Data-Model §12
  `DISTINCT ON` per tier and source, union with covering holds' copies; the caller's `ReadRestriction` in
  every mode.
- **Formats:** `GeoParquetWriter` (GeoParquet 1.1, Hadoop-free, ZSTD, 65,536-row groups, `geo` metadata with
  the observed types and `covering`), `GeoPackageWriter` (1.4: required tables, GeoPackageBinary flags `0x03`,
  RTree filled from Java with triggers at close, `gpkg_metadata`/reference and extension rows, quoted
  identifiers), `FlatGeobufWriter` (two passes through a spill file, packed Hilbert R-tree, `Unknown` when
  mixed), `GpxWriter` (waypoints and tracks in two spill files concatenated in schema order, metadata in
  schema order, elevation in meters from the source's altitude field). `GeoParquetFiles` reads footers back.
- **License rules (`ExportLicenceService`):** every source goes through the single
  `EgressPolicy.decide(EXPORT, sourceId, null, null, EgressContext.export(viewer, includePrivate, personalUse))`,
  and license terms come from `EgressPolicy.sourceTerms`; private sources need admin and `includePrivate` (`403` otherwise); the
  `ExportLicenceSummary` (`licence.json`) per license with terms, sources and rows; `ATTRIBUTION.txt` with the
  personal-use notice; `manifest.json` signed by `ManifestSigner` (`manifest.json.jws`).
- **Snapshots and holds:** `SnapshotService` + `PointInTimePlanner` (validity at the transaction time of S,
  TRACK `L = S(T − ttl − 2 min)`, held reads when T is before a source's horizon and a hold covers the source,
  kinds and `[L, S]` and was created while the floor was ≤ L, else `400 SNAPSHOT_BEYOND_RETENTION`).
  `HoldService`/`HoldsController` (limit 20, keyset lists, versions, delete with copies) and `HoldCopyStep`.
- **Retention:** new `RetentionPlanner` computes once per run (kept on `RetentionRun.memo`) the TRACK leaves to
  drop and the STANDARD scopes and clamped cutoffs to delete; `HoldCopyStep` (order 90) and `ArchiveStep`
  (95) copy/archive exactly those rows; `StandardHistoryStep` and `TrackPartitionStep` delete/drop from the same
  plan. A failed copy or archive skips the STANDARD source or blocks the TRACK leaf (`RetentionRun.blockLeaf`);
  the TRACK step stops at the first blocked leaf.
- **Cold archive:** `ArchiveService` writes hive-partitioned GeoParquet `part-{fromSeq}-{toSeq}.parquet` per UTC
  date, verifies each file's footer row count before the delete, keeps a per-scope `archive_watermark` so no row
  is archived twice, maintains `_manifest.json`, and uploads through `S3Uploader` (SigV4 on `java.net.http`,
  multipart above 64 MiB, `Content-MD5` and ETag verification, local delete only after a verified upload with
  `keepLocal=false`, endpoint checked under `LAN_ALLOWED`). `ArchiveSettingsService` (secrets encrypted, write-only,
  `URL_NOT_ALLOWED`), `ArchiveCatalogService` (keyset by partition path).
- **Offline packs:** `PackService` (MVT from the 016/037 tile queries, PNG from the 045 renderer with retries on
  `RENDER_BUSY`, `413 PACK_TOO_LARGE` with `tileCount`), on a rewritten `PmtilesWriter` (spec-exact dedup,
  clustered order, leaf directories grown until the root fits, JSON metadata through the mapper, header zooms
  from the tiles present, a streaming `Archive` through a spill file).
- **System and metrics:** `/v1/system` blocks `exports` (queued, running, bytes, quotaBytes, maxConcurrent),
  `archive` (enabled, target, last run, bytes, failures) and `holds`; metrics `foresignal.export.rows`,
  `foresignal.export.duration`, counters `foresignal.archive.bytes` and `foresignal.archive.failed`.
- **API/spec/docs:** the 14 operations implemented and removed from `pending-operations.txt`; `getExport` serves
  111 jobs to their creator or admin; MCP `create_export`/`get_export` return URLs only; AccessPolicy; Postman
  BODIES and CAPTURE for `createExport`, `createSnapshot`, `createHold`, `createPack` (collection not regenerated);
  Operator Guide sections; THIRD_PARTY_NOTICES.

## Files created / modified

**Created (main):** `export/{ParquetCodecs,GeoParquetFiles,Wkb,FlatGeobufWriter,ExportRows,ExportWorker,PointInTime,
PointInTimePlanner,ArchiveWatermarkEntity,ArchiveWatermarkRepository}`, `retention/RetentionPlanner`,
`api/ExportCallers`.

**Rewritten or modified (main, 111-owned):** `export/{ExportSchema,ExportWriter,ExportWriterFactory,GeoParquetWriter,
GeoPackageWriter,GpxWriter,ExportSpec,ExportProperties,ExportJobEntity,BulkExportJobRepository (renamed from
ExportJobRepository),ExportJobService,ExportQueryService,ExportLicenceService,ExportManifests,ExportStore,
ExportRetentionStep,ExportSystemBlock,HoldEntity,HoldRepository,HoldService,HoldCopyStep,HoldsSystemBlock,
SnapshotService,PackService,PackSpec,ArchiveService,ArchiveStep,ArchiveSettingsService,ArchiveCatalogService,
ArchiveSystemBlock,S3Uploader}`, `export/pmtiles/PmtilesWriter`; `LocalOutputFile` deleted (parquet's own is used);
`api/{BulkExportsController,ArchiveController,HoldsController,SnapshotsController}`, `api/PacksController`
(createPack), `api/ExportsController` (getExport fallback), `mcp/tools/{CreateExportTool,GetExportTool}`.

**Modified outside `ai.foresignal.export`:** `retention/{RetentionRun,StandardHistoryStep,TrackPartitionStep}`,
`security/AccessPolicy` (explicit `GET /v1/exports`), `api/error/ErrorCodes` (`EXPORT_NOT_READY` added; the unused
`UNKNOWN_PACK` and `PACK_SPEC_INVALID` of the work in progress removed), `src/main/resources/application.yml`
(`foresignal.exports.*`), `build.gradle.kts`, `gradle/libs.versions.toml`, `docs/OpenAPI.json`,
`docs/Operator-Guide.md`, `THIRD_PARTY_NOTICES.md`, `scripts/generate-postman.py`,
`src/test/resources/openapi/pending-operations.txt` (−14 lines).

**Tests:** `export/{GeoParquetWriterTest,ParquetClasspathTest,ParquetCodecsTest,WkbTest,GeoPackageWriterTest,
FlatGeobufWriterTest,GpxWriterTest,ExportSpecTest,ExportLicenceServiceTest,PointInTimeTest,S3UploaderTest,
ExportJobIT,ExportLicenceIT,SnapshotIT,HoldCopyStepIT,ArchiveStepIT,S3UploadIT,PackIT}`,
`export/pmtiles/PmtilesWriterTest` (rewritten), helpers `export/{ExportRowsFixture,GeoParquetTestReader}`,
`testsupport/export/ExportItSupport`, resource `src/test/resources/export/gpx-1.1.xsd`.

## Deviations from the prompt

1. **Hadoop.** parquet-java's writer runs without Hadoop: pages are compressed with zstd-jni directly
   (`ParquetCodecs`), so `hadoop-common` is **compile-only** (javac needs `org.apache.hadoop.conf.Configuration`
   to resolve `ParquetFileWriter`'s overloads) and test-only (the round-trip reader). No Hadoop jar is on the runtime
   classpath. The archive verifies files by decoding the Parquet footer with `parquet-format-structures`, because
   `ParquetReadOptions.builder()` instantiates Hadoop codec classes; the test reader builds the options through the
   package-private constructor.
2. **Row order.** Current and history exports are ordered `(kind, source_id, external_id, seq)` (kinds a source
   does not declare come last). History pages are keyset on `(external_id, seq)` within each kind and source (the
   `(source_id, external_id, seq)` index) rather than raw seq ranges, which is what makes that order possible with
   bounded memory. Point-in-time reads are ordered `(tier, source_id, external_id)`; archive parts in seq order.
3. **Snapshot validity and holds.** A snapshot needs held copies when its transaction time is before a source's
   asOf horizon (the Core-Data-Model §12 criterion), not merely when S is below `floorSeq`. Holds record each
   covered source's floor at creation (`hold.floors_at_create`) so coverage is exact. `SnapshotInput` gains
   `includePrivate` and `personalUse` (the export rules; spec changelog).
4. **Hold ranges** given as times are on the ingestion (transaction) axis and translated through `seq_clock` when
   the copy runs; a `to` still in the future leaves the range open.
5. **FlatGeobuf** is written without an index when some record has no geometry (the index cannot address it);
   rows are never dropped. Columns are the export schema columns plus flattened fields and `payload` (`Json`).
6. **PmtilesWriter** keeps 108's class name (`PmtilesWriter`, not `PmTilesWriter`). The header's zoom range is
   that of the tiles present (`pmtiles verify` requires it); this also applies to 108's raster exports.
7. **Packs** use one MVT layer named `foresignal`; empty tiles are left out; a raster `frame` is resolved to a frame
   key when the pack is queued. `vector.kinds` narrows the caller's restriction.
8. **Archive** is exactly-once through `archive_watermark` (Hibernate table); the cold archive covers the
   allowlist or every non-private registered source (unregistered source ids are not archived). `root` may be
   absolute. `foresignal.archive.bytes` is a counter (bytes written), not a gauge of the archive's size.
9. **S3UploadIT** uses `adobe/s3mock:4.8.0`: `minio/minio` answered 401 on Docker Hub's registry (and quay.io) on
   2026-10-16. S3Mock does not check signatures; `S3UploaderTest` checks the signing key against AWS's published
   derivation vector and the signed headers.
10. **Downloads** use Spring MVC's resource handling for `Range` (206/416) and `If-None-Match` (304); the bundle zip
    is built once with fixed entry times so its ETag is stable. `getExport` returns an `EXPIRED` job's status (200);
    downloads and the manifest answer 410.
11. **License decision** goes through `EgressPolicy.decide` on the `EXPORT` path with an export context (registration,
    private visibility, descriptor egress and the license table with `personalUse`), and the cold archive through
    `EgressPolicy.decide` on `ARCHIVE`. This was moved onto the single policy when the branch was merged with the
    108–110 post-review fixes, which added `decide` and `EgressContext.export`; `ExportLicenceService` no longer
    references `LicenceEgressGuard` (enforced by `LicenceEgressGuardUsageTest`).
12. **Configuration** lives under `foresignal.exports.*` (`pack-max-tiles` is `packs.maxTiles`, `holds-max` is
    `holds.max`), plus `page-size` and `query-timeout`.
13. **Operator Guide** sections are appended, as earlier prompts did, rather than rewriting the 4,900-line guide.

## Issues found / open questions

**Defects found in the work in progress, all fixed:** duplicate `exportJobService` bean (now `bulkExportJobService`)
— and two more startup failures the list missed: the duplicate JPA entity name `ExportJobEntity` (now
`BulkExportJob`) and the duplicate repository bean `exportJobRepository` (now `BulkExportJobRepository`);
hold copy and archive cutoffs from `largestStandardWindow()` (now the shared planner); TRACK drops ignoring failed
copies (now blocked leaves); archive verification against the database (now the file footer); `mode` lowercased
(now canonical); GeoParquet duplicate geometry/WKB-as-string/type byte/`["Point"]`/world bbox/UNCOMPRESSED; the
GeoPackage insert, flags `0x01`, triggers needing `ST_*`, Instant timestamps, unquoted names, missing extension and
reference rows; FlatGeobuf missing; GPX ordering and copyright; submit inside the transaction and unbounded
executors; `.geoparquet` extensions; snapshot validity at `now`; PMTiles run lengths over gaps, the root/leaf loop,
hand-written metadata JSON; S3 without multipart or checksum; the orphan sweep that would have deleted 108's raster
export directories (now skipped, and only directories older than an hour are swept).

**Open:**
- Raster (PNG) packs, `RENDER_BUSY` retries and asOf vector packs have no integration test (they need a rendered
  product frame); the point-in-time export mode (`mode=asOf`/`atSeq` through `/v1/exports`) is exercised only
  through snapshots.
- `export_job`, `hold`, `archive_watermark` arrive via Hibernate `ddl-auto=update`, `record_version_held` via the
  bootstrap SQL of `ExportSchemaInitializer`; prompt 144 must capture them.
- A pack with no tile at all is written with an empty root directory; `pmtiles verify` rejects such an archive.
- The read restriction of a queued job is kept in memory; a restart fails queued jobs rather than resuming them.
- Manual checks documented, not performed here: GeoPackage in QGIS, `pmtiles verify`, DuckDB/GDAL on the archive.
  A scratch program (not a test) wrote each format once with Hadoop off the classpath and read it back with its
  library (parquet-java reader, FlatGeobuf index search, SQLite `integrity_check`, the GPX XSD).

**Invariants.md (changes recommended, not made):** add an invariant "rows inside an active hold or the cold archive
are written out before retention removes them; a failed write keeps them" owned by 111, enforced by
`RetentionPlanner`, `HoldCopyStep`, `ArchiveStep`, tests `HoldCopyStepIT`, `ArchiveStepIT`; add `ExportLicenceIT`
to EGR-1's tests (exports) and `ParquetClasspathTest` to PRC-2's.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `export/GeoParquetWriterTest` | Round trip through parquet-java, `geo` 1.1.0 with `covering`, UTC micros, mixed and Z types, `[]` without geometry, 65,536-row groups, ZSTD, footer reader |
| `export/ParquetClasspathTest` | No HDFS/YARN/MapReduce class or Hadoop jar on the runtime classpath; zstd-jni present |
| `export/ParquetCodecsTest`, `WkbTest` | ZSTD round trip; WKB byte order, ISO/EWKB dimensions |
| `export/GeoPackageWriterTest` | `application_id`, `user_version`, required tables and rows, GeoPackageBinary bytes, RTree, triggers, metadata, keyword names |
| `export/FlatGeobufWriterTest` | Read back with the library: header, columns, `Unknown`, index search, no index with a null geometry |
| `export/GpxWriterTest` | XSD validation, waypoints before tracks, metadata, elevation, escaping, longitude normalization |
| `export/ExportSpecTest`, `PointInTimeTest`, `ExportLicenceServiceTest` | Parsing and canonical modes; validity on the transaction axis; exclusions, personal use, summary |
| `export/S3UploaderTest`, `S3UploadIT` | SigV4 vector and headers, single and multipart uploads, ETag checks, abort, policy; keepLocal=false cleanup |
| `export/pmtiles/PmtilesWriterTest` | Spec Hilbert vectors, header, varints, dedup runs vs shared offsets, leaf directories, metadata escaping, streaming archive, in-test reader |
| `export/ExportJobIT` | Lifecycle, ownership, formats, bundle zip, Range, ETag, cancellation, quota, 50,000 rows in ≤ 10,000-row pages, expiry 410, sweeps |
| `export/ExportLicenceIT` | FORBIDDEN excluded, personalUse notice, private rules, JWS verifies against the JWKS key |
| `export/SnapshotIT` | Version 1 between revisions, tombstones absent, beyond retention 400 unless held |
| `export/HoldCopyStepIT` | Held rows survive TRACK drop and STANDARD delete, floor unchanged, copy failure keeps rows, ranges, limits |
| `export/ArchiveStepIT` | Hive layout and manifests, verified before delete, never twice, failure keeps rows and leaf, catalog, settings |
| `export/PackIT` | MVT pack header and metadata, Range download, `413 PACK_TOO_LARGE`, private and license rules |

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`. `docs/OpenAPI.json` parses; the 14 operations are
`implemented`. Postman not regenerated (the coordinator regenerates).

Dependency versions checked on Maven Central on 2026-10-16 (`maven-metadata.xml` and POMs): parquet 1.18.1 (latest
1.18.1, Apache-2.0), flatgeobuf 3.29.0 (latest, **ISC**), hadoop-common 3.4.1 (exists; latest 3.5.0;
compile/test only), zstd-jni 1.5.7-15 (the version parquet-hadoop declares; BSD-2-Clause), flatbuffers-java 24.12.23
(Apache-2.0), snappy-java 1.1.10.8, aircompressor 2.0.3, commons-pool 1.6 (Apache-2.0, transitive).

`./gradlew dependencies --configuration runtimeClasspath` (relevant lines; no `org.apache.hadoop` artifact):

```
+--- org.xerial:sqlite-jdbc:3.53.4.0
+--- org.apache.parquet:parquet-column:1.18.1
|    +--- org.apache.parquet:parquet-common:1.18.1
|    |    +--- org.apache.parquet:parquet-format-structures:1.18.1
|    +--- org.apache.parquet:parquet-encoding:1.18.1
+--- org.apache.parquet:parquet-hadoop:1.18.1
|    +--- org.apache.parquet:parquet-jackson:1.18.1
|    +--- org.xerial.snappy:snappy-java:1.1.10.8
|    +--- io.airlift:aircompressor:2.0.3
|    +--- commons-pool:commons-pool:1.6
|    +--- com.github.luben:zstd-jni:1.5.7-15
+--- com.github.luben:zstd-jni:1.5.7-15
+--- org.wololo:flatgeobuf:3.29.0
|    +--- com.google.flatbuffers:flatbuffers-java:24.12.23
```

Codec: **ZSTD** through zstd-jni (`ParquetCodecs`); the scratch program above wrote a 70,000-row file on the
runtime classpath without `hadoop-common` and the footer reported codec `ZSTD` in 2 row groups.

## Notes for the next prompt

- **112/113** decide share-link and federation egress with `EgressPolicy.decide(path, …)`; private records never
  leave on `SHARE`, `PUBLIC_SHARE` or `FEDERATION` (109's rule as implemented in `EgressPolicy`).
- Retention steps that remove rows should take their scope from `RetentionPlanner` so the hold copy and the cold
  archive keep covering exactly what is removed.
- **144 (Flyway):** capture `export_job` (with `plan`), `hold` (with `floors_at_create`), `archive_watermark` and the
  bootstrap `record_version_held`.

Implementation commit: `6a002010a2aa7b78d4c3a1ca7df73fbf2e11ae45`
