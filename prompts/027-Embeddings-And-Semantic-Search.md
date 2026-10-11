# 027 — Embeddings And Semantic Search

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/027-CR-Embeddings-And-Semantic-Search.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–138).
- **Previous Completion Report:** read `completion-reports/026-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §20 (embeddings and semantic search), §7.3 (SchemaBootstrap), §14 (DB image)
- `docs/Core-Data-Model.md` §10.1 (ChangeBus subscribers incl. EmbeddingWorker; `ChangeSummary` fields), §12 (`timeField`, default `effective` for search), §13 (consumer clamp on `embedding.cursor`), §9 (`HistorySchemaInitializer` ordering), *Per-prompt changes* → "Other affected prompts" → 023 (= this prompt)
- `docs/Gap-Analysis.md` §1 item 7 (private sources never embedded)
- `docs/Product-Charter.md` §8.12 (FR-47, FR-48)
- `docs/OpenAPI.json`: tag `Search`, schemas `SearchResult`, `SearchResults`, `SearchStatus`
- pgvector README (HNSW, `vector_cosine_ops`, `hnsw.ef_search`, `CREATE INDEX CONCURRENTLY`): https://github.com/pgvector/pgvector

## Objective

Semantic and hybrid search over records, briefings and enriched alerts, using pgvector in the same Postgres. This is the retrieval layer for Ask (028), briefings (029) and enrichment (030). Changing the embedding model must never take search offline: the new index is built beside the old one and swapped in when the backfill completes.

## Tasks

### 1. Database image with pgvector

- `docker/db/Dockerfile`: `FROM postgis/postgis:17-3.5` (the image pinned in compose today), install `postgresql-17-pgvector` from the PGDG apt repository that image already uses, clean apt lists. Compose `db` builds from `docker/db` (image `foresignal-db:17-3.5-pgvector`), keeping the 007 PostgreSQL flags and binds.
- Testcontainers: `PostgisContainers` builds `docker/db` once per JVM (`ImageFromDockerfile` or the Testcontainers 2.x equivalent), declared a compatible substitute for `postgres`.
- `SchemaBootstrap` phase 1: `CREATE EXTENSION IF NOT EXISTS vector` right after `postgis`, in the same pre-EntityManagerFactory phase as `HistorySchemaInitializer`. Failure is non-fatal: `SemanticCapability.available=false` with the reason, one warning.

### 2. Storage

- `EmbeddingDocumentEntity` (`embedding_document`): PK (`doc_type`, `doc_id`), `source_id`, `kind`, `seq` (persisted version seq the text came from), `geom`, `effective_time` (= `COALESCE(event_time, observed_at)` of that version), `observed_at`, `text`, `text_hash` bigint, `updated_at`. Hibernate maps none of the vector columns.
- **Two vector slots** managed only by `EmbeddingSchemaManager` with native, idempotent DDL (recorded in the bootstrap statement list that prompt 143 moves into Flyway):
  - slot A: `embedding_a vector(N)`, `model_a varchar(200)`; slot B: `embedding_b vector(M)`, `model_b varchar(200)` (`model_*` holds `EmbeddingModelSpec.modelId()` from 026, the **model_id tag** of each row's vector)
  - HNSW index per slot: `embedding_document_hnsw_a ON embedding_document USING hnsw (embedding_a vector_cosine_ops)`, likewise `_b`
  - `tsv tsvector GENERATED ALWAYS AS (to_tsvector('simple', coalesce(text,''))) STORED` + GIN; GiST on `geom`; btree on (`source_id`, `effective_time`)
- **Index state** in `app_setting['search.index']`: `{activeSlot, activeModelId, activeSpec, pending: {slot, modelId, spec, startedAt, total, done} | null}`. All reads and writes resolve the slot from this state; never hardcode a column.

### 3. EmbeddingWorker (`ai.foresignal.search`)

- Runs only when semantic search is available and an embedding provider is assigned.
- Subscribes to `ChangeBus` (queue 4096) with the durable cursor `embedding.cursor`; catches up at startup and on `onGap`. Advance the cursor promptly: retention clamps to `min(trigger.cursor, embedding.cursor) + 1` (Core-Data-Model §13), so a stalled worker holds retention until the clamp is released at 2 × window.
- **Candidates:** versions from sources whose descriptor declares `embedFields`, whose settings have `embed=true` (add the flag to `source_settings`; default true when `embedFields` is non-empty; expose it in prompt 013's settings DTOs), and whose descriptor is **not** `private` (008 flag). Tombstones delete the record document.
- **Skip non-text reasons cheaply:** `HEARTBEAT` is skipped without loading anything. For every other reason, load the version's payload by `seq = ANY(?)`, build the text, and skip when `text_hash` equals the stored hash. Text = `"<kind> | <source name> | " + embedFields values` (flattened, ≤ 2,000 chars). Warn at registry start when an `embedFields` entry is not significant (its changes never persist a version).
- **Batching:** 64 texts per `embed(texts, spec)` call through the provider budget. On budget refusal pause until the next minute without advancing the cursor.
- **Upsert** (`doc_type='record'`, `doc_id='<source>:<externalId>'`) with `seq`, `source_id`, `kind`, `geom`, `effective_time`, `observed_at`, `text`, `text_hash`, and the vector into the active slot (`model_<slot> = activeModelId`). While a reindex is pending, **also** embed with the pending spec into the pending slot (dual write), so new documents are never missing after the swap.
- Public `embedDocument(docType, docId, text, geom, effectiveTime, sourceId, kind)` for briefings (029) and alerts (030), dual-writing the same way.
- `RetentionStep` (prompt 019 mechanism): delete record documents whose head no longer exists.

### 4. Zero-downtime reindex

Triggered by `EmbeddingModelChangedEvent(oldSpec, newSpec)` (026) or `POST /v1/search/reindex` (admin; same model allowed, to rebuild). One at a time (second → 409 `REINDEX_RUNNING`).
1. Pick the inactive slot; `ALTER TABLE … DROP COLUMN IF EXISTS` and re-add it as `vector(newDims)` with `model_<slot>` cleared (metadata-only DDL on a nullable column). Record `pending` with `total` = eligible current records (head ⋈ live of embeddable sources) + briefing runs + enriched alerts.
2. **Search keeps working:** queries keep using the active slot, embedding the query with `activeSpec` (the outgoing model; 026's explicit-spec `embed`). If the outgoing model is no longer callable (provider deleted or model removed), semantic degrades to text with `degraded=true` and `reason` until the swap.
3. Backfill the pending slot from `record_head` (keyset on `(source_id, external_id)`, 64 per call, budgeted), then briefing runs and enriched alerts, resumable after restart from `pending.done` + a stored keyset position.
4. When done: `CREATE INDEX CONCURRENTLY` the pending slot's HNSW index (outside a transaction), verify every eligible row has `model_<pending> = pendingModelId` (re-embed stragglers), then flip `activeSlot`/`activeModelId`/`activeSpec` in one settings write. Drop the old slot's index and null its column afterwards.
5. Cancel: `DELETE /v1/search/reindex` (admin) clears `pending` and the pending slot.

`SearchStatus` gains `activeModel`, `reindex: {state (idle|backfilling|indexing|swapping), fromModel, toModel, total, done, percent, ratePerMinute (10-min moving average), etaSeconds, startedAt}`, alongside `documents` and `backlog` (records behind `embedding.cursor`).

### 5. Search service and API

- `GET /v1/search` (`q`, `mode`, `source[]`, `kind[]`, `bbox`, `since`, `until`, `docType`, `limit ≤ 100`). Time filters apply to **`effective_time`** (the default `timeField` for search; accept `timeField=effective|observed`).
  - `mode=text`: `tsv @@ websearch_to_tsquery('simple', :q)` ranked by `ts_rank_cd`, merged and de-duplicated with `record_head.search_text ILIKE` matches for records without documents; snippet via `ts_headline`.
  - `mode=semantic`: embed `q` (purpose `search`, budgeted) with `activeSpec`, `ORDER BY embedding_<active> <=> :qvec` with `model_<active> = activeModelId` and filters; `SET LOCAL hnsw.ef_search = 100`.
  - `mode=hybrid` (default): reciprocal rank fusion (k = 60).
  - Semantic unavailable: `semantic` → 409 `SEMANTIC_UNAVAILABLE`; `hybrid` degrades to text with `degraded=true`.
  - Record results join the current `Record` DTO (`seq`, `version`, `live`, `title`) plus the document's `seq`, so citations use persisted seqs.
- Exclude private sources from results unless the caller is admin (039 hardens this); `semantic`/`hybrid` are cost-bearing (023 `alwaysAuthenticated`).
- `GET /v1/search/status`, `POST /v1/search/reindex`, `DELETE /v1/search/reindex` (admin).
- **MCP:** register `search` (025 registry; `costBearing=true`), short output with `seq` per record hit.
- `/v1/system` `search` block (enabled, active model, documents, reindex state).
- Set `embedFields` only on already-implemented adapters that carry text (`usgs-earthquakes` has none); later adapters set them per the Source Catalog.

### 6. Spec, Postman, docs

- `docs/OpenAPI.json`: `SearchStatus.activeModel` and `reindex` object; `SearchResult.seq`, `effectiveTime`; params `timeField`, `docType`; `cancelReindex` (DELETE, admin); error codes `REINDEX_RUNNING`, `SEMANTIC_UNAVAILABLE`; `x-foresignal-scope` read for search/status, admin for reindex; `AccessPolicy` rules; regenerate Postman (`python3 scripts/generate-postman.py docs/OpenAPI.json postman`).
- `docs/Operator-Guide.md` "Search": DB image, choosing an embedding model, what a model change does (dual index, ETA in status, cost of the backfill), the `embed` flag.

## Tests to write (do not run)

- `EmbeddingSchemaManagerIT` (pgvector image): slot A at N = 8; slot B at N = 16 beside it; idempotent; concurrent index build; missing-extension path non-fatal (plain `postgis` image).
- `EmbeddingWorkerIT` (`FakeLlmClient` with deterministic vectors; seed via `TestRecords`): eligible records embedded with `effective_time`; HEARTBEAT skipped without a payload load; unchanged text skipped by `text_hash`; tombstone deletes; private source never embedded; budget refusal pauses without losing documents; cursor survives restart; `embed=false` respected.
- `ReindexIT`: model change 8 → 16 dims; searches during backfill return results from the old slot; new records during backfill land in both slots; swap after backfill; restart mid-backfill resumes; outgoing model unavailable → degraded text; cancel; concurrent reindex → 409; status progress and ETA.
- `SearchServiceIT`: text ranking, semantic ordering, hybrid fusion, filters (source, kind, bbox, effective time window, docType), degraded path, snippets, record hits carry `seq`.
- `SearchControllerIT`: validation, status shape, admin-only reindex/cancel.

## Acceptance criteria

- With pgvector and an embedding provider, semantic and hybrid search work over embedded records.
- Changing the embedding model keeps semantic search answering throughout, reports progress and ETA, and swaps automatically.
- Without pgvector or a provider, text search works and status explains why semantic search is off.
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
2. **Commit** all changes with the message `027: Embeddings And Semantic Search`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/027-CR-Embeddings-And-Semantic-Search.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `027: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
