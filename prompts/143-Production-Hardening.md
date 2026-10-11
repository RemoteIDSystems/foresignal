# 143 — Production Hardening

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/143-CR-Production-Hardening.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–138).
- **Previous Completion Report:** read `completion-reports/142-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` (whole; the v1.2 note at the top and §25 "Production hardening")
- `docs/Core-Data-Model.md` (whole, to fold in), especially §8 (tables and indexes), §9 (partitioning, `HistorySchemaInitializer`, `PartitionManager`, relkind guard, `extra_physical_table_types`), §17, and "Per-prompt changes" 063 (Flyway baseline holds the tiered DDL; `PartitionManager` stays; `HistorySchemaInitializer` removed)
- `docs/Weather-Imagery-Design.md` (whole, to fold in), especially §2.3 (raster, media and place tables, tile-key columns, `trigger_rule`/`alert` changes, GiST on `raster_frame.footprint`) and §10.3 ("081" Flyway contents; document updates list)
- `docs/Gap-Analysis.md` §6 row "Capabilities" (oasdiff in CI after v1.0); §8 item D.64 (Flyway indexes for the new tables; oasdiff CI against the v1.0.0 spec)
- `docs/Product-Charter.md` (whole), `docs/Source-Catalog.md`, `docs/Roadmap.md`, `docs/Executive-Summary.md`
- Completion Reports 001–142, especially 002 (`SchemaBootstrap`), 007 (history schema), 019 (retention), 027 (reindex DDL), 141 and 142 (open issues)

## Objective

Cut Foresignal over to production form and release `v1.0.0`. Fold the v1.2 design documents into the Architecture and Charter so one set of docs is authoritative. Replace Hibernate DDL and the bootstrap initializers with a Flyway baseline that contains the full tiered and partitioned schema, every extension and index, and every table added through 142; switch to `ddl-auto=validate`. Add an API-compatibility gate against the frozen v1.0.0 spec. Write release notes and tag.

## Tasks

### 1. Documentation consolidation

- **`docs/Architecture.md` v2.0:**
  - Integrate `docs/Core-Data-Model.md`: replace §4–§11 with its time contract, ingest input, hashing, policies, writer, streaming sinks, tables, partitioning (Flyway-owned DDL now), distribution, triggers, query paths, retention, provenance and storage sections. Translate every prompt number to v1.2 (Roadmap §5).
  - Integrate `docs/Weather-Imagery-Design.md` as a new "Weather, rasters and imagery" part: components, data model, storage layout, budgets, memory profiles, SatDump integration, API conventions, alerting integration and resource budget. Drop prompt breakdowns and review-resolution tables.
  - Add sections for everything built after the docs were written: entities (098), derived layers (099), windowed and anomaly rules (100), incidents (101), licence and provenance (109), sinks (110), exports and archive (111), standards outputs (112), federation (113), pattern of life and trips (114), exercise and alert quality (115), the intelligence layer (116–138), adapter doctor (139), SDKs, CLI, MCP OAuth and plugins (140).
  - Update §2 stack (all dependencies actually used, with licences), §3 packages (real package tree), §12 testing, §13 configuration (all env vars), §14 deployment (all compose profiles), §25 (now describes the Flyway regime), §26 reuse policy (GEV MIT, SatDump GPL reference, Meshtastic protobuf exclusion).
  - Remove the "v1.2 note" banner.
- **Archive the design docs:** move `Core-Data-Model.md`, `Weather-Imagery-Design.md` and `Gap-Analysis.md` to `docs/design-history/`, each with a header line "Historical design input, folded into Architecture.md v2.0 on {date}". Fix every link in `docs/`, `README.md` and the prompts that still point at them.
- **`docs/Product-Charter.md` v2.0:**
  - Apply the amendments made along the way: FR-17 (Core-Data-Model §13), FR-25 operators/arrays/windows, FR-27 lifecycle, the 061 chat-command amendment, the 045 raster non-goal wording and FR-55, the new channel types, the private-source and licence requirements.
  - Add FRs for the features delivered in 036–140 in the existing style (one line each, grouped).
  - Update §10 source plan and §11 constraints (Flyway present, tests policy history). Update §14 success criteria with the 142 results (link to `docs/Runtime-Verification.md`).
- **`docs/Source-Catalog.md`:**
  - Renumber prompt references to v1.2.
  - Add columns **Tier** (`STANDARD | TRACK`), **Policy** (default ingest policy and keyframe), **Licence** (terms id) and **Egress** (redistribution: `ALLOWED | LOCAL_ONLY | FORBIDDEN`) to every table.
  - Mark sources that were not implemented, or were dropped, with the reason.
  - Add `SourceCatalogParityTest`: parse the markdown tables and, for every registered source, assert that tier, policy mode, licence id and redistribution match the descriptor. Rows for unregistered ids must be marked "not implemented".
- Update `docs/Executive-Summary.md` and `README.md` (feature list, quickstart unchanged in spirit, docs index). Mark 001–143 Done in `docs/Roadmap.md`.

### 2. Flyway baseline

- **Dependencies:** `spring-boot-starter-flyway` (the Boot 4 module) plus `org.flywaydb:flyway-database-postgresql`; verify versions on Maven Central and pin them in `gradle/libs.versions.toml`.
- **Generate the baseline:**
  - Start an empty database with the pre-cutover code (`ddl-auto=update` plus bootstrap) and **every feature enabled** so every entity table exists.
  - Dump it with `pg_dump --schema-only --no-owner --no-privileges --no-comments`.
  - Hand-clean into `src/main/resources/db/migration/V1__baseline.sql`, ordered and commented by subsystem.
  - Keep the generation procedure as `scripts/db/dump-schema.sh` for future parity checks.
- **Baseline contents:**
  - `CREATE EXTENSION IF NOT EXISTS postgis; CREATE EXTENSION IF NOT EXISTS vector;` (document that on a non-superuser external Postgres, a superuser must create them first).
  - `CREATE SEQUENCE record_seq` and the partitioned `record_version` exactly as Core-Data-Model §8–§9:
    - parent `PARTITION BY LIST (history_tier)`
    - `record_version_std FOR VALUES IN (0) PARTITION BY RANGE (seq)` and `record_version_trk FOR VALUES IN (1) PARTITION BY RANGE (seq)`
    - PK `(seq, history_tier)`
    - partitioned indexes `(source_id, external_id, seq)`, `(source_id, observed_at)`, GiST `(geom)`
    - **No leaf partitions** (PartitionManager creates them) and no DEFAULT partition.
  - `record_head` (index `(kind)` only), `record_live` (GiST `(geom)` only; `fillfactor=70`, `autovacuum_vacuum_scale_factor=0.02`, `autovacuum_vacuum_cost_limit=2000`), `fetch_log` (`(source_id, started_at DESC)`), `seq_clock`.
  - Every other table, including but not limited to: settings, credentials, health, usage (with the v1.2 counters), `app_setting`, `system_secret`, `api_token` (restrictions, device metadata, tile-key columns, `licence_policy`, federation flag), `audit_event`, `trigger_rule` (all 017/036/114 columns), `alert` (`observed_at`, `record_snapshot`, `context`, lifecycle, feedback, exercise), channels, notifications and deliveries, push subscriptions and devices, LLM providers and usage, embeddings (with the HNSW index at the configured default dimension), Ask threads, briefings and runs, `raster_product`, `raster_grid`, `raster_frame` (GiST `footprint`, `(product_id, valid_time DESC)`, `(product_id, reference_time, forecast_step)`), `media`, `place`, saved views, watchlists, `activity_rollup`, gazetteer tables, entity tables, `intelligence_event` and investigations, `sink`/`sink_cursor`, `export_job`, `record_version_held`, `cap_message`, `share_link`, `peer`/`federation_origin`, `pol_*`, `trip`/`trip_run`, `exercise_run`, `rule_quality_daily`, `rule_tuning_log`, `doctor_report`, plugin state.
  - All GIN indexes (search text, jsonb where used) and GiST indexes that bootstrap code created.
- **Keep dynamic DDL where it belongs:** `PartitionManager` (leaf create, attach, detach, drop) stays and runs after Flyway: depend on the Flyway initializer, or run it from the writer's pre-flight before the writer accepts batches. The 027 reindex job keeps its dimension-change DDL (Architecture §25).
- **Remove** `HistorySchemaInitializer`, `SchemaBootstrap`, `PostgisExtensionInitializer`, `SpatialIndexInitializer` and any other bootstrap DDL bean, plus their `EntityManagerFactoryDependsOnPostProcessor` wiring and tests that only covered them (replace them with the parity test below). Update Javadoc and `application.yml` comments that mention them or "Flyway in prompt …".
- **Configuration:** `spring.jpa.hibernate.ddl-auto=validate`; keep `hibernate.hbm2ddl.extra_physical_table_types: PARTITIONED TABLE` so validation accepts the partitioned parent; `spring.flyway.baseline-on-migrate=true` with `baseline-version=1` so existing development databases adopt the baseline (log a clear warning telling the operator to compare the schemas with `scripts/db/dump-schema.sh`, or recreate the volume). Tests run Flyway on the Testcontainers database; `DatabaseCleaner` skips `flyway_schema_history` and still skips `pg_inherits` children.
- **Conventions for later changes:** `V{n}__{description}.sql`, additive by default, one migration per change, never edit an applied migration. Write this in Architecture §25 and the Adapter Guide.

### 3. Schema parity and validation tests

- `FlywayBaselineIT`: migrate an empty database; Hibernate `validate` passes; `record_version` has `relkind='p'` with the two tier children and the three partitioned indexes; PartitionManager `ensureAhead` creates leaves afterwards; no DEFAULT partition.
- `FlywaySchemaParityIT`: build schema A with Flyway and schema B with the pre-cutover generator kept as a test-only utility (Hibernate `update` via `SchemaManager` plus the old bootstrap SQL preserved in test resources). Compare `information_schema.columns` (name, type, nullability, default), constraints and `pg_indexes` (normalised definitions). They must be equal, apart from an explicit allowlist with reasons.
- Replace `SchemaParityIT` (Core-Data-Model §17) with the Flyway variant: entity-mapped columns of `record_version` equal the table's columns.
- **Sanctioned run:** this prompt changes schema ownership. Run the full suite once at the end (`./gradlew test jacocoTestReport jacocoTestCoverageVerification`), fix until green, and record counts and coverage in the Completion Report.

### 4. API compatibility gate (oasdiff)

- Set `info.version` to `1.0.0` in `docs/OpenAPI.json` and freeze a copy at `docs/api/v1.0.0/OpenAPI.json`.
- Gradle task `checkApiCompat`: runs `oasdiff breaking docs/api/v1.0.0/OpenAPI.json docs/OpenAPI.json --fail-on ERR` via a local `oasdiff` binary or Docker image `tufin/oasdiff:<pinned version>`; skipped with a warning when neither is available. Not wired to `build`.
- Add `.gitlab-ci.yml` (none exists yet) with stages `build` (`./gradlew compileJava compileTestJava`), `test` (`./gradlew test jacocoTestCoverageVerification` with the Docker-in-Docker service for Testcontainers), `api` (the oasdiff job plus the 142 `metric-drift.sh` against a started instance if feasible, else `allow_failure`), and `lint` (`lintOpenApi`). Document NFR-10 (additive-only `/v1`; breaking changes go to `/v2`) and the 020 API-evolution convention next to it.

### 5. Production defaults and release

- **Production review of defaults:**
  - `security.openReads` documented trade-offs; compose binds 8080 and 5433 to `127.0.0.1` by default
  - the Docker image runs as non-root with a read-only root filesystem where feasible (writable `/data` and `tmp` only)
  - health-check `curl` present
  - log format and level defaults; `FORESIGNAL_BIND` and the profile docs
  - Fix anything 141/142 listed as a production blocker.
- **Versioning:** set the Gradle `version = "1.0.0"`; `ForesignalVersion` and `/v1/system` report it; the image label `org.opencontainers.image.version`.
- **Release notes:** `docs/Release-Notes-v1.0.0.md` and a `CHANGELOG.md` entry.
  - Highlights by area; supported upstream sources (from the catalog); compose profiles; hardware sizing from 142.
  - Breaking-change policy; known limitations (flush/poll resolution of triggers, asOf granularity, approximate asOf under SAMPLED, skipped verification steps); upgrade notes (the baseline-on-migrate behaviour, or recreate the volumes).
  - Licences and third-party notices.
- **Tag:** after the implementation and report commits, create an annotated tag `v1.0.0` ("Foresignal 1.0.0") on the implementation commit and push it with `git push origin v1.0.0`. Record the tag and hash in the Completion Report.

## Tests to write

- `FlywayBaselineIT`, `FlywaySchemaParityIT`, the replacement `SchemaParityIT` (task 3).
- `SourceCatalogParityTest` (task 1).
- `NoBootstrapDdlTest`: scan `src/main/java` for `CREATE TABLE`/`CREATE INDEX`/`ALTER TABLE` string literals outside `PartitionManager` and the reindex job, and fail if any exist.
- `DatabaseCleanerIT` (update): Flyway history table untouched; partition children skipped.
- `ApiVersionTest`: `info.version`, `/v1/system.apiVersion` and the Gradle version agree on `1.0.0`.

## Acceptance criteria

- `docs/Architecture.md` and `docs/Product-Charter.md` are the only authoritative design docs; the design inputs live in `docs/design-history/` and no link is broken.
- The Source Catalog has Tier, Policy, Licence and Egress columns that match the descriptors (enforced by a test).
- A fresh database is created only by Flyway; `ddl-auto=validate` passes; no bootstrap DDL remains except PartitionManager leaf management and the reindex job.
- `checkApiCompat` runs against the frozen v1.0.0 spec, and CI runs it.
- The full suite passes with ≥ 90% line coverage; `v1.0.0` is tagged and pushed with release notes.
- Compiles.

## Guardrails (mandatory)

1. **Tests: this is a test-run prompt.** Run the test suite as the Test run section describes, and fix failures until it passes. Fix production code, and change a test only when the test itself is wrong (record each such change in the Completion Report). Don't add features.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 143).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `143: Production Hardening`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/143-CR-Production-Hardening.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `143: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
