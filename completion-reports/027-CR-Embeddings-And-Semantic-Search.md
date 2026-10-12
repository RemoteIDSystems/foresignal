# 027-CR-Embeddings-And-Semantic-Search

| | |
|---|---|
| Prompt | 027-Embeddings-And-Semantic-Search.md |
| Status | Complete |
| Implementation commit | f88020127e00dec9b015f34c6f7b4f4000dd353c |
| Date | 2026-10-12 |

## Summary

Foresignal now has semantic and hybrid search over records, using pgvector in the same PostgreSQL.
Briefing runs (029) and enriched alerts (030) can be added through `EmbeddingWorker.embedDocument`.
Changing the embedding model builds the new index in a second vector slot while searches keep
answering from the old one, then swaps it in. Without pgvector or an embedding provider, text search
works and the status says why semantic search is off.

- **Database image.**
  - `docker/db/Dockerfile` is `FROM postgis/postgis:17-3.5` plus `postgresql-17-pgvector` from the
    PGDG repository the image already carries. The apt lists are cleaned afterwards.
  - The compose `db` service builds it as `foresignal-db:17-3.5-pgvector` and keeps the 007 flags,
    the volume, the bind and the healthcheck.
  - The image was built once on this host (pgvector `0.8.6-1.pgdg11+1`).
  - `PostgisContainers` builds `docker/db` once per JVM through `ImageFromDockerfile` (tag
    `foresignal-db:17-3.5-pgvector`, `deleteOnExit=false`). It declares the image a compatible
    substitute for `postgres`. The plain image stays available as `POSTGIS_IMAGE`.
- **Bootstrap.**
  - `VectorExtensionInitializer` runs right after the PostGIS initializer, in the same
    pre-EntityManagerFactory phase, and joins the EMF's depends-on list. It runs
    `CREATE EXTENSION IF NOT EXISTS vector`.
  - A failure is non-fatal: one warning, and `SemanticCapability.available=false` with the reason.
- **Storage.**
  - `EmbeddingDocumentEntity` (`embedding_document`) has the PK `(doc_type, doc_id)` through
    `@IdClass` and the columns `source_id`, `kind`, `seq`, `geom`, `effective_time`, `observed_at`,
    `text`, `text_hash` and `updated_at`. Hibernate maps none of the vector columns.
  - `EmbeddingSchemaManager` owns the native, idempotent DDL. These are the bootstrap statements that
    144 moves into Flyway (`BASE_STATEMENTS`, `slotStatements`, `indexStatement`):
    - the generated `tsv` column plus its GIN index, a GiST index on `geom`, and a btree on
      `(source_id, effective_time)`;
    - two slots, `embedding_a vector(N)`/`model_a varchar(200)` and `embedding_b`/`model_b`, with
      HNSW indexes `embedding_document_hnsw_a|b` (`vector_cosine_ops`) built with
      `CREATE INDEX CONCURRENTLY` on an autocommit connection. An invalid index left by an
      interrupted build is dropped first.
  - The index state lives in `app_setting['search.index']` (`SearchIndexState`):
    `activeSlot`, `activeModelId`, `activeSpec`, and `pending` (`slot`, `modelId`, `spec`,
    `fromModelId`, `startedAt`, `total`, `done`, `phase`, plus the keyset `afterKey`/`afterId`).
  - Every read and write resolves its slot from that state; column names come only from
    `VectorSlot`.
- **`EmbeddingWorker`.**
  - It runs only when pgvector is available and an embedding provider is assigned. It starts on
    `EmbeddingModelChangedEvent` and stops when the role is cleared.
  - It follows the `ChangeBus` (queue 4096) with the durable cursor `embedding.cursor`, and catches
    up at start, on `onGap` and on a 5 s tick.
  - `EmbeddingCursorConsumer` registers the cursor with retention's consumer clamp.
  - **Candidates** come from `EmbeddingEligibility`: the source declares `embedFields`, its `embed`
    setting is on, and `EgressPolicy.allowsEmbedding` allows it (never for private sources).
  - `HEARTBEAT` summaries are skipped without loading anything. Other versions are loaded by
    `seq = ANY(?)` and reduced to the latest version per entity.
  - The text is `"<kind> | <source name> | "` plus the `embedFields` values (flattened, at most
    2,000 characters). Unchanged text, judged by `text_hash`, only updates the filter columns.
  - Embedding calls go through `LlmService` with an explicit spec, 64 texts per call. Records are
    upserted with `doc_type='record'`, and a document is never moved back to an older seq.
  - While a reindex is pending, documents are written to both slots.
  - A tombstone, or text with no field values, deletes the document.
  - A budget refusal, a provider failure or a database error pauses the worker until the next minute
    without advancing the cursor.
  - `embedDocument(docType, docId, text, geom, effectiveTime, sourceId, kind)` is the public entry
    for 029 and 030.
  - `EmbeddingRetentionStep` (order 450) deletes record documents whose head no longer exists.
  - The registry now warns when an `embedFields` entry is not significant.
- **Zero-downtime reindex (`ReindexService`).** It is triggered by `EmbeddingModelChangedEvent` or
  `POST /v1/search/reindex`, which also accepts the same model, to rebuild. Only one runs at a time
  (`409 REINDEX_RUNNING`).
  1. Pick the inactive slot (A for the first index), drop it and re-add it at the new width, and
     record `pending` with `total` = eligible current records + other documents.
  2. **Backfill.** Records come from `record_head ⋈ record_live` with a keyset on
     `(source_id, external_id)`, 64 per call. Briefing and alert documents are re-embedded from their
     stored text.
  3. **Index.** Build the pending HNSW index concurrently, then re-embed every document whose pending
     model is not the new one.
  4. **Swap.** Under the slot write lock, check that no stragglers remain, flip the active slot in
     one settings write, then drop the old slot.
  - Progress and the keyset position are stored after every batch, so a restart resumes
    (`reconcile()` at start, which also starts a reindex for a model change missed while the
    application was down).
  - `DELETE /v1/search/reindex` clears `pending` and the pending slot.
  - The status reports `state`, `fromModel`, `toModel`, `total`, `done`, `percent`, `ratePerMinute`
    (a 10-minute moving average), `etaSeconds` and `startedAt`.
- **Search (`SearchService`).**
  - `text`: `tsv @@ websearch_to_tsquery('simple', q)` ranked by `ts_rank_cd`, with a `ts_headline`
    snippet. It is then merged, without duplicates, with `record_head.search_text ILIKE` matches for
    records that have no document.
  - `semantic`: the query is embedded (budgeted) with `activeSpec`, which during a reindex is the
    outgoing model. Results are `ORDER BY embedding_<active> <=> q` with
    `model_<active> = activeModelId`, under `SET LOCAL hnsw.ef_search = 100`.
  - `hybrid` (the default) uses reciprocal rank fusion with k = 60.
  - Filters: `source[]`, `kind[]`, `docType[]`, `bbox`, and `since`/`until` on `effective_time`
    (`timeField=observed` for `observed_at`).
  - The caller's invisible private sources are excluded.
  - Record hits carry the document's persisted `seq` and the current `Record` DTO.
  - When semantic search is unavailable, `semantic` returns `409 SEMANTIC_UNAVAILABLE` and `hybrid`
    degrades to text with `degraded=true` and a `reason`. When the outgoing model of a running
    reindex cannot be called, both degrade to text until the swap.
- **API, MCP and system.**
  - `SearchController` implements `search`, `getSearchStatus`, `reindexSearch` (202) and
    `cancelReindex`. `AccessPolicy` gains `DELETE /v1/search/reindex` as admin.
  - The MCP `search` tool is read and cost-bearing; its short items carry `seq`.
  - `/v1/system` gains the `search` block plus `features.search` and `features.semanticSearch`.
  - `source_settings.embed` is exposed in the 013 settings DTOs (`SourceSettings.embed`,
    `SourceSettingsUpdate.embed`) and in the settings export.

Tests were written but not run (feature prompt).

## Files created / modified

**New: `ai.foresignal.search`**
- `SemanticCapability`
- `EmbeddingDocumentEntity`, `EmbeddingDocumentId`, `EmbeddingDocumentRepository`
- `VectorSlot`, `EmbeddingSchemaManager`, `SearchIndexState`, `SearchIndexStateStore`
- `DocInput`, `EmbeddingText`, `Geometries`, `EmbeddingStore`, `VersionRows`, `EmbeddingEligibility`
- `EmbeddingIndexer`, `EmbeddingWorker`, `EmbeddingCursorConsumer`, `ReindexService`,
  `EmbeddingRetentionStep`
- `SearchRequest`, `SearchService`, `SearchStatusService`, `SearchSystemBlock`, `SearchFeature`,
  `package-info`

**New: other**
- `config/VectorExtensionInitializer`
- `api/SearchController`
- `mcp/tools/SearchTool`
- `docker/db/Dockerfile`

**Changed**
- `config/SchemaBootstrap`: the `semanticCapability` and `vectorExtensionInitializer` beans, which
  the EMF depends on. `config/StartupOrder`: `EMBEDDING = 22`. `config/SpatialIndexInitializer`
  and `config/PostgisExtensionInitializer`: Javadoc and message only.
- `security/EgressPolicy`: `allowsEmbedding`, the search and embedding path.
  `security/AccessPolicy`: `DELETE /v1/search/reindex` is admin.
- `store/SourceSettingsEntity` (`embed`), `source/SourceSettingsUpdate` (`embed`, with a
  compatibility constructor), `source/SourceView` (`embed`, with a compatibility constructor),
  `source/SourceService` (`applyEmbed`), `source/DescriptorValidator` (the `embedFields`
  significance warning).
- `api/dto/SourceSettingsDto`, `api/dto/SourceSettingsUpdate`, `api/mapper/SourceMapper`,
  `settings/SettingsTransferService`: `embed`.
- `api/error/ErrorCodes`: `REINDEX_RUNNING` and `SEMANTIC_UNAVAILABLE`.
- `settings/SettingKeys`: `search.index` and `embedding.cursor`.
- `store/query/RecordQueryService`: `currentByKeys`, and `bboxClause` is now public.
- `docker-compose.yml`: `db` builds `docker/db`.
- `docs/OpenAPI.json`: statuses, reconciliations, error codes and the changelog. `postman/` is
  regenerated.
- `docs/Operator-Guide.md`: a new "Search" section.
- `docs/Invariants.md`: EGR-1 also lists `EmbeddingWorkerIT`.
- `src/test/resources/openapi/pending-operations.txt`: the four 027 lines are removed.
- `test/PostgisContainers`: builds `docker/db`.

## Deviations from the prompt

1. **The PGDG host changed.** The base image is Debian 11 (bullseye). PGDG has moved the
   bullseye suite from `apt.postgresql.org`, which now returns 404, to `apt-archive.postgresql.org`
   (same signing key). Before installing, the Dockerfile rewrites the host in the image's own
   `pgdg.list`. The base image and the package (`postgresql-17-pgvector`) are as the prompt asked.
2. **No new dependency.**
   - Hibernate maps no vector column, so `hibernate-vector` is not needed.
   - Vectors are bound as pgvector text literals with
     `CAST(CAST(? AS text) AS vector)`.
   - The DDL and query SQL were checked once with `psql` in a throwaway container of the built
     image: slot add and drop, the `atttypmod` width, the upsert, `CREATE INDEX CONCURRENTLY`, the
     cosine query under `SET LOCAL hnsw.ef_search` and `ts_headline`.
3. **Old slots are dropped, not nulled.** After a swap (and on cancel) the old slot's index and
   columns are dropped instead of setting the column to null. Both are metadata-only, and the next
   reindex re-adds the slot at its new width.
4. **Unchanged text still updates metadata.** When `text_hash` matches, the worker makes no
   embedding call but still updates `seq`, `geom`, `effective_time`, `observed_at` and `kind`. This
   keeps citations valid after retention deletes the older version, and keeps the filters current.
5. **Other documents during a reindex.** The "briefing runs and enriched alerts" phase re-embeds
   those documents from the text stored in `embedding_document`. The prompt names their own tables,
   which 029 and 030 have not created yet.
6. **Private sources.** Search excludes private sources through the existing
   `SourceVisibilityGate`, which allows `agent` and `admin`. The prompt says admin only; 039 folds
   this into `EgressPolicy`. Private records are never embedded at all, so this affects only the
   `record_head` text fallback.
7. **Budget refusal in `hybrid`.** A budget refusal of the query embedding degrades `hybrid` to text
   (`degraded`, with a `reason`). In `semantic` it stays `429 LLM_BUDGET_EXCEEDED`. A provider
   failure outside a reindex is likewise degraded in `hybrid` and surfaced as 502 in `semantic`.
8. **Spec additions (additive, recorded in the changelog):**
   - `SearchResults.reason`;
   - `search` documents 404 (an unknown source, or a private one the caller may not see) and that
     the window applies to the `timeField` axis;
   - `SourceSettingsUpdate.embed` documents its 400 and that turning it off deletes documents;
   - the `REINDEX_RUNNING` and `SEMANTIC_UNAVAILABLE` Problem codes;
   - `info.description` mentions the MCP `search` tool.
9. **The `embed` flag.**
   - Turning it off deletes the source's record documents.
   - `embed=true` is refused (400, field `embed`) for a source without `embedFields` or for a
     private source.
   - Turning it back on does not backfill; the Operator Guide says to run `POST /v1/search/reindex`.
10. **Event listeners write in their own transaction.** `EmbeddingModelChangedEvent` and
    `SourceSettingsChangedEvent` are published in `afterCommit`. Their 027 listeners therefore write
    in a `REQUIRES_NEW` transaction, because Spring would otherwise join the finished transaction
    and the writes would be lost.
11. **The index state is read from the table.** `SearchIndexStateStore` reads `search.index`
    directly instead of through `AppSettingService`'s cache. A value cached between a write and its
    commit would never be refreshed (see Issues 2).
12. **Status details.**
    - `backlog` is the number of non-heartbeat versions of embeddable sources after
      `embedding.cursor`, counted up to 1,000,000, and 0 while the worker is stopped.
    - `features` gains `search` (always true) and `semanticSearch`.
13. **An extra unit test class.** `EmbeddingWorkerTest`, written with Mockito, proves that a
    heartbeat is skipped without a payload load. An IT cannot observe "without loading".
    `EmbeddingTextTest` and `SearchFusionTest` are unit tests too.

## Issues found / open questions

1. **Unverified at run time.** No test has run. These are first exercised at checkpoint 041:
   - the `ImageFromDockerfile` build inside the test JVM (the same Dockerfile built with
     `docker build`);
   - Hibernate 7 with a record as `@IdClass`;
   - the `REQUIRES_NEW` writes from the after-commit listeners;
   - the cursor clamp with two consumers.
2. **`AppSettingService` cache race (existing code).** `put` drops the cached value before the
   transaction commits. A concurrent reader can then cache the old value and keep it until the next
   write. 027 avoids the problem for its own state (Deviation 11). Other settings written inside
   longer transactions may hit it. Should `put` also drop the cache after commit?
3. **Other after-commit listeners (existing code).** Other listeners of events published from
   `afterCommit` (for example `SourceSettingsChangedEvent`) that write through `@Transactional`
   services have the same exposure as Deviation 10. This was not audited beyond 027's own
   listeners.
4. **A text the provider always rejects stalls the reindex.** A document whose embedding keeps
   failing is retried every minute in the straggler pass, so the swap waits. There is no skip list.
5. **Data:** no instructions directed at an agent were found in data, fixtures, docs or tool
   output.
6. **Owner decisions carried from 012–026** are still open.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `search/EmbeddingSchemaManagerIT` | With the pgvector image:<br>• capability available<br>• slot A at 8, slot B at 16 beside it, `ensureSlot` idempotent and resizing<br>• base DDL idempotent (tsv, GIN, GiST, btree)<br>• concurrent HNSW build over existing rows (valid, `vector_cosine_ops`, rebuild a no-op), drop<br>• slot statements<br>With the plain PostGIS container: the missing extension is non-fatal (reason) and slots are refused |
| `search/EmbeddingWorkerIT` | • eligible records embedded with `effective_time` = event time, else observed time; text format; hash; vector width<br>• a source without `embedFields` is not embedded<br>• unchanged text skipped by `text_hash` (no call, `seq` follows)<br>• a tombstone deletes the document<br>• a private source is never embedded, also by a reindex<br>• a budget refusal pauses without advancing the cursor or losing documents, and resumes<br>• the cursor survives a stop and start<br>• `embed=false` respected (documents deleted, no new ones)<br>• `embedDocument` for briefings<br>• the retention step deletes orphans<br>• the consumer clamp |
| `search/EmbeddingWorkerTest` | Mockito:<br>• a heartbeat-only batch is skipped with no `bySeq` load and the cursor advances<br>• other reasons are loaded and written<br>• a budget refusal pauses until the next minute without advancing<br>• a tombstone deletes<br>• no start without pgvector or a provider |
| `search/ReindexIT` | • 8 → 16 dimensions: searches answer from slot A during the backfill, swap to B, A dropped, B index valid<br>• dual write during the backfill<br>• restart mid-backfill resumes from the keyset without re-embedding<br>• an outgoing model no longer callable degrades to text (status `degraded`) until the swap<br>• cancel<br>• concurrent reindex 409 `REINDEX_RUNNING`; rebuilding the same model<br>• status progress, rate and ETA<br>• the first index (409 while building)<br>• clearing the role cancels and stops |
| `search/SearchServiceIT` | • text ranking with snippets<br>• semantic ordering<br>• hybrid fusion<br>• filters (source, kind, bbox, effective window, observed axis, docType)<br>• record hits carry `seq` and the current record<br>• `record_head` text fallback<br>• degraded without a provider (409 for semantic)<br>• a failing provider<br>• private exclusion |
| `search/SearchControllerIT` | • validation (q, length, mode, docType, timeField, limit, bbox, window, unknown or private source)<br>• always authenticated<br>• status shape before and after an index<br>• admin-only reindex and cancel (202, 409 `SEMANTIC_UNAVAILABLE`, 409 `REINDEX_RUNNING`, cancel idempotent)<br>• `/v1/system` search block and features<br>• the `embed` setting in the source API |
| `search/EmbeddingTextTest`, `search/SearchFusionTest` | • text format, cut and hash<br>• vector literals, slot names<br>• RRF scores and order<br>• titles |

Test support:
- `testsupport/search/SearchTestSources`: the embeddable test sources (one private, one without
  `embedFields`), plus `FakeEmbeddingClients`, a primary `LlmClientFactory` that returns
  `FakeLlmClient`s, counts texts per model id and can fail a provider.
- `testsupport/search/SearchItSupport`: providers through `LlmProviderService`, roles through
  `LlmSettingsService`, `TestRecords` seeding, and a step-driven reindex.

Changed tests:
- `test/PostgisContainers`: builds `docker/db`.

## Compile result

`./gradlew compileJava compileTestJava --rerun-tasks` → `BUILD SUCCESSFUL in 9s`.

`./gradlew lintOpenApi` → `Woohoo! Your API description is valid.`, 70 warnings (unchanged), 0
errors. `python3 scripts/generate-postman.py docs/OpenAPI.json postman` → 825 requests, all
operations covered.

`docker build -t foresignal-db:17-3.5-pgvector docker/db` succeeded and installed
`postgresql-17-pgvector 0.8.6-1.pgdg11+1`.

**New dependencies:** none. The image adds the pgvector package: PostgreSQL License, from PGDG.

## Notes for the next prompt

1. **Retrieval for 028–030.** Call
   `SearchService.search(new SearchRequest(q, Mode.HYBRID, docTypes, sources, kinds, bbox, since,
   until, false, limit), excludedSources)`.
   - It returns a `SearchResults` map; record items carry `seq` (cite with it) and `record`.
   - `hybrid` never fails for lack of semantic search; check `degraded`.
   - Pass the hits through `EgressPolicy.filter` before putting them in a prompt.
2. **Embedding briefings (029) and enriched alerts (030).** Call
   `EmbeddingWorker.embedDocument("briefing" | "alert", id, text, geom, effectiveTime, sourceId,
   kind)`.
   - It dual-writes during a reindex.
   - Blank text deletes the document.
   - It throws `LlmBudgetExceededException` on a budget refusal; catch it and retry later.
   - These documents are already included in reindex totals and backfills, from their stored text.
   - Their retention belongs to 029 and 030 (`EmbeddingRetentionStep` handles only records).
3. **Semantic neighbours (030).** Use mode `SEMANTIC` and catch `ConflictException`
   (`SEMANTIC_UNAVAILABLE`) to skip neighbours when the index is off.
4. **Startup order.** `StartupOrder.EMBEDDING = 22`; `ReindexService.reconcile` runs at 23.
5. **Tests.** Search ITs import `SearchTestSources` and extend `SearchItSupport`:
   - `semanticReady(id, dims)` builds an index;
   - `reindex.setBackgroundEnabled(false)` is the default there, and `runToCompletion()` drives a
     reindex;
   - the test container is now `foresignal-db:17-3.5-pgvector`, built from `docker/db`.
6. **039** folds `SourceVisibilityGate` into `EgressPolicy`. The search exclusion and
   `EgressPolicy.allowsEmbedding` are the places to update.
