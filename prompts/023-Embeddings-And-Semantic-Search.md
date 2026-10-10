# 023 — Embeddings And Semantic Search

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/023-CR-Embeddings-And-Semantic-Search.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`.
- **Previous Completion Report:** read `completion-reports/022-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §20 (embeddings and semantic search), §7.3 (SchemaBootstrap), §14 (DB image)
- `docs/Product-Charter.md` §8.12 (FR-47, FR-48)
- `docs/OpenAPI.json`: operations tagged `Search`, schemas `SearchResult`, `SearchResults`, `SearchStatus`

## Objective

Semantic and hybrid search over records, briefings and enriched alerts, using pgvector in the same Postgres database. This is the retrieval layer for Ask (prompt 024), briefings (025) and enrichment (026).

## Tasks

### 1. Database image with pgvector

- `docker/db/Dockerfile`: `FROM postgis/postgis:17-3.5`, then install `postgresql-17-pgvector` from the PGDG apt repository that image already uses, and clean the apt lists.
- `docker-compose.yml`: `db` builds from `docker/db` (image name `elaro-db:17-3.5-pgvector`).
- **Testcontainers:** `PostgisContainers` uses `new ImageFromDockerfile("elaro-db-test", false).withFileFromPath(".", Path.of("docker/db"))` (or an equivalent Testcontainers 2.x API), declared as a compatible substitute for `postgres`. Build it once per JVM.
- **SchemaBootstrap phase 1:** `CREATE EXTENSION IF NOT EXISTS vector` after `postgis`. Failure is non-fatal: set `SemanticCapability.available=false` with the reason (e.g. "pgvector extension not installed"), and log a single warning.

### 2. Storage

- Add the `hibernate-vector` module (version from the Hibernate BOM) for mapping.
- `EmbeddingDocumentEntity` (`embedding_document`) per §7.1. The `embedding` column's dimension is dynamic, so:
  - Hibernate creates the table with every column **except** `embedding` and `tsv`, or with a vector type that has no dimension, if supported (verify).
  - `EmbeddingSchemaManager` adds or replaces `embedding vector(N)` and creates `embedding_document_hnsw ON embedding_document USING hnsw (embedding vector_cosine_ops)` for the active dimension N.
  - It adds `tsv tsvector GENERATED ALWAYS AS (to_tsvector('simple', coalesce(text,''))) STORED` plus a GIN index, and a GiST index on `geom`.
  - All statements are idempotent and recorded in the bootstrap statement list, which prompt 063 moves into Flyway.

### 3. EmbeddingWorker (`ai.elaro.search`)

- Runs only when semantic search is available and an embedding provider is assigned.
- **Change stream:** subscribes to `ChangeBus` (queue 4096) with a durable cursor `embedding.cursor`, catching up at startup and on `onGap`.
- **Candidates:**
  - record versions from sources whose descriptor declares `embedFields` **and** whose settings have `embed=true` (add the `embed` flag to `source_settings`; default true when `embedFields` is non-empty; exposed in source settings by prompt 010's DTOs, so update them)
  - tombstones delete the record's document
- **Text:** `"<kind> | <source name> | " + joined embedFields values` (flattened, ≤ 2000 characters). Skip when `text_hash` is unchanged for that document.
- **Batching:** 64 texts per `embed()` call through the embedding provider's budget. On a budget refusal, pause until the next minute without advancing the cursor.
- **Upsert:** on (`doc_type='record'`, `doc_id='<source>:<externalId>'`) with `seq`, `source_id`, `kind`, `geom`, `observed_at`, `text`, `model`, `embedding`.
- **Public API:** `embedDocument(docType, docId, text, geom, observedAt, sourceId, kind)` for briefings (025) and alerts (026).
- **Retention:** add a `RetentionStep` (prompt 016's mechanism) that deletes record documents whose record head no longer exists.

### 4. Reindex

- On `EmbeddingModelChangedEvent` (prompt 022), or `POST /v1/search/reindex`:
  1. mark reindexing
  2. drop and recreate the embedding column and index at the new dimension
  3. reset the cursor to 0
  4. re-embed every eligible current record (from `record_head`), then all briefing runs and enriched alerts
- Only one reindex at a time (a second request → 409). Progress is reported in `SearchStatus.backlog`.

### 5. Search service and API

- `GET /v1/search`:
  - `mode=text`: `tsv @@ websearch_to_tsquery('simple', :q)` ranked by `ts_rank_cd`. Also include `record_head.search_text ILIKE` matches for records without documents, merged with deduplication.
  - `mode=semantic`: embed `q` (purpose `search`, through budgets), then `ORDER BY embedding <=> :qvec LIMIT :k` with filters (source, kind, `geom && envelope`, time range, `docType`). Set `hnsw.ef_search` to 100 for the query.
  - `mode=hybrid` (default): run both and fuse them with reciprocal rank fusion (k = 60).
  - When semantic search is unavailable, `semantic` returns 409 `SEMANTIC_UNAVAILABLE` and `hybrid` degrades to text with `degraded=true`.
  - Results join `Record` DTOs for record documents. The snippet is the matched text, ≤ 300 characters, with the match highlighted in text mode (`ts_headline`).
- `GET /v1/search/status` and `POST /v1/search/reindex` (admin).
- **MCP:** register the `search` tool (prompt 021's registry).
- **System:** `/v1/system` `search` block.

### 6. Descriptor updates

Set `embedFields` on already-implemented adapters only where text exists. `usgs-earthquakes` has none, so don't add any; later adapter prompts set `embedFields` per the Source Catalog.

## Tests to write (do not run)

- `EmbeddingSchemaManagerIT` (pgvector image): column and HNSW index created for N = 8; recreated for N = 16; idempotent; the missing-extension path is non-fatal (use a plain `postgis` image for that case).
- `EmbeddingWorkerIT` (`FakeLlmClient` returning deterministic vectors): eligible records embedded; unchanged text skipped; tombstone deletes; budget refusal pauses without losing documents; durable cursor across a restart; `embed=false` respected.
- `SearchServiceIT`: text ranking, semantic ordering with fake vectors, hybrid fusion, filters (source, kind, bbox, time, docType), the degraded path, snippets.
- `ReindexIT`: a dimension change rebuilds and backfills; concurrent reindex → 409.
- `SearchControllerIT`: parameter validation, status, reindex scope (admin only).

## Acceptance criteria

- With pgvector and an embedding provider, semantic and hybrid search work over embedded records.
- Without them, text search still works and status explains why semantic search is off.
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
2. **Commit** all changes with the message `023: Embeddings And Semantic Search`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/023-CR-Embeddings-And-Semantic-Search.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `023: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
