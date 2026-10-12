# 029-CR-Scheduled-Briefings

| | |
|---|---|
| Prompt | 029-Scheduled-Briefings.md |
| Status | Complete |
| Implementation commit | ec03099d62bb3b3a2e02fca6bde74f49ca14729c |
| Date | 2026-10-12 |

## Summary

Scheduled briefings are stored, scheduled, run, rendered, delivered, embedded and served as JSON
and sanitized HTML. `GET /v1/digest` gives clients a structured catch-up document. It is built from
the same statistics and polled with a seq cursor and an alert-id cursor. Digest mode needs no LLM.
LLM mode writes a cited narrative and falls back to the digest on any failure.

- **Model (`ai.foresignal.briefing`).**
  - `BriefingEntity` (`briefing`) holds:
    - the definition: name, enabled, 5-field `schedule`, `timezone`, `scope` jsonb, `lookback` as
      written, `mode`, `instructions`, `channel_ids`, `max_items_per_kind`;
    - the schedule state: `next_run_at`, `last_run_at`, `last_run_status`, and `running_since`, the
      run mark;
    - the owner: `owner_token_id` and `owner_scope`;
    - `created_at` and `updated_at`.

    `@DynamicUpdate` keeps a definition replace from overwriting the run columns.
  - `BriefingRunEntity` (`briefing_run`) holds the briefing's name and timezone at run time, the
    status (`running`/`succeeded`/`fallback`/`failed`), the mode, the period, `content_markdown`, and
    `citations`, `stats`, `usage` and `warnings` as jsonb, plus `error` and `deliveries`.
  - Hibernate creates both tables. They have no foreign keys: a deleted briefing's runs remain.
- **Validation (`BriefingValidator`).**
  - `schedule` is a standard 5-field cron, converted to Spring's `CronExpression` by prefixing `0`
    (macros are refused). It must fire, and its shortest gap between fire times may not be under
    15 minutes. The gap is measured in UTC over 400 days or 2000 fire times, so a DST shift is not
    counted as a gap.
  - `timezone` must be a valid `ZoneId` (default `UTC`). `lookback` must be 1h–7d, or null.
  - Scope sources, kinds and `ruleIds` must exist. A private source the caller cannot see counts as
    unknown. The bbox must be valid.
  - Channels that do not exist, are disabled or are not subscribed to `briefing` give `warnings`, not
    failures. With no channels listed, a warning appears when no enabled channel takes briefings.
  - `mode=llm` is accepted without a provider.
  - Every problem is reported in one 400.
- **Scope resolution.** `ScopeResolver` (default implementation `DefaultScopeResolver`) is the hook
  that 036 extends with `placeIds`.
- **`BriefingScheduler`.**
  - Every `foresignal.briefing.poll-interval` (30 s), one transaction claims enabled briefings that
    are due and not running (`FOR UPDATE SKIP LOCKED`). For each, it sets `next_run_at` to the next
    fire time after now and sets the run mark, then commits.
  - Each claimed briefing runs on a virtual thread. The scheduler starts at the new
    `StartupOrder.BRIEFINGS` (35); tests turn it off.
  - Missed runs run once and are never replayed.
  - `running_since` limits each briefing to one run across instances and paths. A mark older than
    `stale-run` (15 min) counts as abandoned.
  - `runBriefing` while a run is in progress → 409 `BRIEFING_RUNNING`.
  - After every run: the mark is cleared, `last_run_*` is recorded, and `next_run_at` is
    recomputed. It is also set on create and update (null while disabled).
- **Digest statistics.**
  - `DigestStatsProvider` is an interface; `SqlDigestStatsProvider` implements it. 038 swaps in a
    rollup provider.
  - `DigestStatsService` (bounds):
    - `toSeq = ChangeBus.lastPublishedSeq()` is read before any query.
    - `fromSeq` comes from `seq_clock` (Deviation 1).
    - `gap = fromSeq < RetentionState.lowestRetainedSeq(scope sources)`.
  - Counts from `record_version`, grouped by `(source, kind)`, with per-reason `count(*)` and
    distinct entities per group:
    - `new` = FIRST + REAPPEAR, `changed` = SIGNIFICANT + CHANGE, `removed` = TOMBSTONE,
      `edges` = TRIGGER_EDGE;
    - `routine` = SAMPLE + KEYFRAME + HEARTBEAT, reported apart and never counted as change.
  - Activity comes from `record_live ⋈ record_head`: `last_received_at >= fromTime`, tombstoned
    heads excluded. Touch-only entities therefore count.
  - Delta: the previous equal-length period. It is null when that period reaches below the floor,
    or when `gap` is true.
  - Alerts are selected by id (`since < id <= until`, suppressed alerts excluded). They are counted
    by severity, state and rule, and up to 20 are listed, critical first then newest.
  - Notable records: head ⋈ live rows whose head seq is in range, at most `perKind` per kind
    (`row_number()`). They are ranked by the source's `rankField`, read only when it is a JSON number,
    else by effective time. Each carries the persisted head `seq`, the stored `title` (025
    `TitleResolver`), `live` and a link.
  - Health is the current snapshot (Deviation 4).
  - Private sources the caller or owner may not see appear in counts only.
  - `BriefingSectionContributor` beans add ordered sections to the stats, the markdown and the LLM
    context. A failing contributor is skipped.
- **`GET /v1/digest` (`DigestService`, `DigestController`, read).**
  - Defaults: `sinceSeq` = the last seq ingested before now − 24 h; `sinceAlertId` = just before the
    first alert at or after now − 24 h.
  - Response: the `Digest` document, `fromTime` (from `seq_clock`), `toTime` and
    `next {sinceSeq: toSeq, sinceAlertId: untilAlertId}`.
  - The strong ETag covers `(toSeq, untilAlertId, resolved params, caller)` and is computed before
    the statistics, so a 304 skips them.
  - 400 cases: a hidden or unknown source, an unknown kind, `places` (until 056), a bad bbox,
    `limit` outside 1–20.
- **Rendering.**
  - `DigestRenderer` produces deterministic markdown, in order:
    - title, then the period line in the briefing timezone;
    - "Alerts" (counts, items critical first, alert links);
    - "Activity": a table of kind, new, changed, removed, active entities and delta, with routine
      versions on a separate line;
    - "Notable" per kind, with `[title](publicBaseUrl/v1/records/{source}/{externalId})`;
    - "Source health", then contributor sections;
    - "Counts incomplete (retention)" when `gap`.

    Every listed record and alert becomes an `AskCitation`. Untrusted text is folded onto one line and
    escaped as markdown.
  - `LlmBriefingWriter` builds the LLM context:
    - the stats JSON;
    - the notable records in short form (`RecordMapper` plus `McpOutputShaper.shortRecord`) with cite
      keys;
    - alerts with `[alert:id]` cite keys;
    - when semantic search is available and there are instructions, a scope-filtered hybrid search on
      the instructions (top 20).

    Everything passes `EgressPolicy` for the chat provider, and owner-hidden sources are always
    withheld. The call uses the chat provider, purpose `briefing`, effort `medium`, no tools, and the
    constant `BriefingPrompts.SYSTEM`; the period and instructions go in the user turn. The answer is
    verified with 028's `CitationVerifier`, and its warnings go to `run.warnings`.

    Any failure produces status `fallback`: no provider, budget, provider error, refusal, or empty
    text. The run then carries the digest, `error` and a warning.
  - `BriefingHtmlRenderer`:
    - CommonMark 0.30.1 with GFM tables, raw HTML escaped and unsafe URLs dropped;
    - then an OWASP Java HTML Sanitizer allowlist: headings, p, lists, tables (`align` only), links
      (http/https/mailto, `rel="noopener nofollow"`), code/pre, em/strong/del, blockquote, hr;
    - a self-contained page with an inline light/dark stylesheet and an escaped title and footer.
  - Run reads answer HTML for `?format=html`, or for an `Accept` that ranks `text/html` above JSON.
    They carry `Content-Security-Policy: default-src 'none'; style-src 'unsafe-inline'; img-src 'self'
    data:`, `nosniff` and `no-referrer`. The per-run ETag differs between the JSON and HTML renditions,
    and both support 304.
- **Runs and delivery (`BriefingRunner`).**
  - The period is `lookback`, else since the previous completed run (capped at 7 d), else 24 h.
  - The run row is written `running` in its own transaction and completed in another.
  - The completed run is then embedded (`EmbeddingWorker.embedDocument("briefing", runId, …)`) when
    semantic search is available or a reindex is pending.
  - It is published as a `briefing` notification:
    - title `"{name} — {periodTo local date}"`, subtype `briefing.run`, severity `info`;
    - body: the markdown plus "[Read more](…?format=html)", with a link button;
    - `data` = the run JSON, plus `briefingRunId`;
    - sent to `channelIds`, or to every channel subscribed to `briefing` when none are listed.

    `deliveries` records how many were enqueued. Failed runs are not delivered.
- **API (`BriefingsController`).** All ten Briefings operations: list, create (201 with
  `Location`), get, replace, delete (204), run, list runs (cursor, newest first, limit 1–5000,
  default 500), get run, latest run (the most recent completed run), and preview. Preview stores,
  delivers and schedules nothing.
- **MCP (025 registry).**
  - `list_briefings`, `get_briefing_run` (latest when `runId` is omitted; `detail=long` adds the
    stats) and `get_digest`, all read.
  - `create_briefing` (agent) and `run_briefing` (agent, cost-bearing).

  The read tools become Ask tools automatically.
- **Ask.** `BriefingActionHandlers` declares the `create_briefing` `AskActionHandler`. Its payload
  is validated by `BriefingValidator` and executed through `BriefingService.create` with the
  confirming token as owner. `propose_briefing` now appears in Ask by itself.
- **Retention, export/import.**
  - `BriefingRetentionStep` (order 720) deletes runs older than the `briefings` retention (default
    180 d) that are not running, together with their `embedding_document` rows.
  - `SettingsTransferService` exports `briefings` with their ids. Import upserts them by id, after
    triggers and channels, with an admin owner. `ImportReport` gains `briefingsUpserted`.
- **Security.**
  - `AccessPolicy` adds `GET /v1/digest` (read). The briefing rules were already present and match
    every `x-foresignal-scope` and always-authenticated flag.
  - `FailClosedRestrictionEnforcer` treats `POST /v1/briefings/preview` and `/v1/briefings/*/run` as
    data reads (refused for data-restricted tokens until 035).
- **Spec, Postman, docs.**
  - The 11 operations are `implemented` and removed from `pending-operations.txt`. The changelog
    entry lists the reconciliations (Deviation 9). Problem lists `BRIEFING_RUNNING`.
  - Postman was regenerated; the create, update and preview bodies already existed.
  - `docs/Operator-Guide.md` has a new "Briefings" section.
  - `docs/Agent-Guide.md` has a new "Catch-up with /v1/digest" section and five tool rows; its
    `propose_briefing` sentence was updated.
  - `docs/Invariants.md`: SEC-5 adds `BriefingsControllerIT`; EGR-1 adds `LlmBriefingIT`.
  - `THIRD_PARTY_NOTICES.md` has entries for CommonMark and the OWASP sanitizer.

Tests were written but not run (feature prompt).

## Files created / modified

**New: `ai.foresignal.briefing`**
- `package-info`
- `BriefingEntity`, `BriefingRepository`, `BriefingRunEntity`, `BriefingRunRepository`
- `BriefingProperties`, `BriefingScope`, `BriefingInput`, `BriefingValidator`
- `ScopeResolver`, `DefaultScopeResolver`
- `DigestStats`, `DigestStatsProvider`, `SqlDigestStatsProvider`, `DigestStatsService`, `DigestService`
- `BriefingSection`, `BriefingSectionContributor`
- `DigestRenderer`, `BriefingHtmlRenderer`, `BriefingPrompts`, `LlmBriefingWriter`
- `BriefingLinks`, `BriefingRunner`, `BriefingService`, `BriefingScheduler`, `BriefingViews`
- `BriefingRetentionStep`, `BriefingActionHandlers`

**New: other**
- `api/BriefingsController`, `api/DigestController`
- `mcp/tools/ListBriefingsTool`, `GetBriefingRunTool`, `GetDigestTool`, `CreateBriefingTool`,
  `RunBriefingTool`

**Changed**
- `api/error/ErrorCodes` (`BRIEFING_RUNNING`), `config/StartupOrder` (`BRIEFINGS`),
  `security/AccessPolicy`, `security/FailClosedRestrictionEnforcer`,
  `settings/SettingsTransferService`
- `application.yml` (`foresignal.briefing.*`), `application-test.yml` (scheduler off)
- `build.gradle.kts`, `gradle/libs.versions.toml` (new dependencies)
- `docs/OpenAPI.json`, `postman/Foresignal.postman_collection.json`,
  `src/test/resources/openapi/pending-operations.txt`
- `docs/Operator-Guide.md`, `docs/Agent-Guide.md`, `docs/Invariants.md` (sections and rows added;
  existing text kept)
- `THIRD_PARTY_NOTICES.md`
- Tests: `ask/AskServiceIT` (`propose_briefing` and the briefing read tools are now offered),
  `security/AccessPolicyTest` (`/v1/digest`, `/runs/latest`)

## Deviations from the prompt

1. **`fromSeq` is the last seq ingested before the period start, not `cutoffSeq(periodFrom)`.**
   `seq_clock` has one row per minute that had an ingest. `cutoffSeq(t)` is the first seq of the
   latest such minute at or before `t`, so `seq > cutoffSeq(t)` would count the rest of that minute
   even when it lies well before `t`. The service uses `SeqClockQuery.lastSeqBefore(t)` instead
   (Core-Data-Model §8 `S(t⁻)`, exact to the batch). Every seq above it was ingested at or after `t`.
   The same applies to the `/v1/digest` default `sinceSeq` and to the previous period.
2. **Budget exhaustion in LLM mode is a `fallback` run, not `429 LLM_BUDGET_EXCEEDED`.** The prompt
   says "on any LLM failure … render the digest with status fallback" (FR-51), and a scheduled run
   cannot answer 429. `runBriefing` and `previewBriefing` behave the same way; the spec's 429
   descriptions now say so.
3. **`runBriefing` after 120 s.** A run still going is returned with status `running` and completes
   in the background; the run mark keeps a second run out.
4. **Health is the current snapshot.** Foresignal stores no health transitions: 019 records none,
   and `SourceHealthChangedEvent` is not persisted. `health` therefore lists the sources that are
   degraded or failing when the digest is computed, with `healthSnapshot: true` and `since: null`.
   `lastError` is the last error, else the status reason.
5. **Previous-period activity is null.** `record_live` keeps only the latest receipt, so the number
   of entities active in the previous period cannot be known. `DigestActive.previous` and
   `DigestCount.previous.active` are null until 038's rollups.
6. **Who may see private records.**
   - `/v1/digest` lists a private source's records for any caller `SourceVisibilityGate` lets see it
     (agent or admin, within restrictions), as the spec says.
   - Briefings list them only when the owner scope is `admin`, as the prompt says: an owner is the
     token that created or last replaced the briefing; import uses admin.
   - In every other case private sources are counts only.
7. **Markdown renderer.** 018's channel `MarkdownRenderer` exists but has no tables and produces
   chat fragments. CommonMark with GFM tables was added as the prompt's fallback.
8. **Later-prompt members are refused while non-empty**, matching 028's handling of `placeIds`:
   - the scope members of 036, 040 and 056 (`placeIds`, `viewId`, `country`, `admin1`, `county`,
     `places`, `weatherProducts`);
   - a non-null `audio` (141);
   - `/v1/digest?places` (056).

   Output-only `Briefing` members are accepted and ignored on input, so a briefing that was read can
   be sent back.
9. **Spec reconciliations (additive, in the changelog).**
   - createBriefing: the validation details (hidden source, later members, channel warnings) and the
     owner scope. updateBriefing: output-only members.
   - runBriefing: the `running` return after 120 s and the fallback behavior. previewBriefing: the
     fallback behavior.
   - listBriefingRuns: 400, and the runs of a deleted briefing.
   - getBriefingRun: 400 and its ETag and CSP headers. getLatestBriefingRun: completed runs only.
   - getDigest: its 400 cases.
   - DigestStats/DigestHealthItem/DigestActive/DigestCount: the snapshot and null fields.
   - `NotificationMessage.subtype` gains `briefing.run`; Problem gains `BRIEFING_RUNNING`;
     `info.description` lists the MCP tools.
10. **Alert bbox.** Alerts are filtered by bbox through their stored snapshot geometry
    (`record_snapshot.geometry`). An alert without one is excluded when a bbox is given.
11. **Test names.** `BriefingHtmlRendererTest` and `BriefingToolsIT` were added alongside the
    prompt's nine classes. The golden fixture is `src/test/resources/briefing/digest-golden.md`.

## Issues found / open questions

1. **Unverified at run time.** No test has run. These are first exercised at checkpoint 041:
   - the golden fixture, which was written by hand from the renderer's rules;
   - the exact serialization of `rel` and `align` by the OWASP sanitizer (the assertions are
     pattern-based);
   - the concurrent `SKIP LOCKED` test;
   - the DST expectation that 2026-11-01 07:00 America/Chicago is 13:00 UTC.
2. **Notable-record query cost.** The query filters `record_head` by `seq` range, and that column
   has no index (Core-Data-Model §8 indexes only `kind`), so each digest scans the head table.
   That is acceptable at briefing frequency. Frequent `/v1/digest` polling on a large instance may
   want 038's rollups or an index; this was not added (Hibernate-only schema, scope).
3. **Digest ETag and live activity.** As specified, the ETag covers `(toSeq, untilAlertId, params)`.
   Touch-only activity and alert state changes do not advance either, so a 304 can hide a changed
   `active` count or `byStatus` until the next ingest or alert.
4. **Channel formatting of tables.** Chat channels receive the digest markdown. 018's renderers show
   the Activity table as plain pipe-separated text; the HTML rendition is linked for reading.
5. **Data:** no instructions directed at an agent were found in data, fixtures, docs or tool output.
6. **Owner decisions carried from 012–028** are still open.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `briefing/BriefingValidatorTest` | • cron conversion and refusals (field count, macros, ranges)<br>• the 15-minute floor (`*/15` passes; `*/10`, `0,5 7`, `* * * * *` fail)<br>• never-firing schedules<br>• next run in a zone and across the end of DST<br>• timezones<br>• lookback bounds<br>• a full valid body (defaults, ids, channel warning)<br>• every problem in one 400 (incl. a hidden source and later-prompt members)<br>• the period rule<br>• scope round trip |
| `briefing/BriefingSchedulerIT` | `MutableClock`:<br>• fires at 07:00 Chicago (12:00 UTC) and reschedules<br>• DST end moves it to 13:00 UTC<br>• missed hourly runs run once<br>• a running briefing is neither claimed nor run now (409), and a stale mark is reclaimed<br>• two schedulers claim six briefings exactly once<br>• a disabled briefing is never claimed |
| `briefing/DigestStatsServiceIT` | `TestRecords`:<br>• counts by reason (FIRST/REAPPEAR, SIGNIFICANT or CHANGE, TOMBSTONE; HEARTBEAT, SAMPLE, KEYFRAME routine only)<br>• touch-only entity active with no version<br>• deltas against the previous 24 h<br>• `gap` below the stub floor (no delta)<br>• alerts after `sinceAlertId`, critical first, by severity/status/rule<br>• `rankField` ranking with persisted seqs<br>• a hidden private source counted but not listed |
| `briefing/DigestRendererIT` | • golden markdown fixture byte for byte<br>• citations of alerts and records<br>• gap note and empty sections<br>• untrusted text folded and escaped<br>• number and delta formatting |
| `briefing/DigestControllerIT` | • defaults and the `next` cursor<br>• the round trip (empty, then only new)<br>• ETag/304 and a changed tag after ingest<br>• read scope with and without `openReads`<br>• kind/source/bbox filters<br>• private counted but not listed for read, listed for agent<br>• 400s |
| `briefing/BriefingHtmlIT` | • sanitized page via `Accept`: no `<script>`, `<img>`, event handlers or `javascript:` href from a hostile record title; the title shown escaped<br>• CSP header<br>• `?format=html`, JSON default, `format=xml` 400, 404s<br>• latest run (HTML and JSON, 404 before runs)<br>• per-rendition ETags and 304<br>• Accept ranking |
| `briefing/BriefingHtmlRendererTest` | • allowed markup with `rel` noopener/nofollow and right-aligned cells<br>• raw HTML, handlers, `javascript:`, styles and iframes stripped<br>• page title, stylesheet and footer escaped<br>• the CSP constant |
| `briefing/LlmBriefingIT` | `FakeLlmClient`:<br>• context (system prompt constant, purpose `briefing`, effort `medium`, no tools, instructions, cite keys) and a verified citation with the persisted seq<br>• private records never reach a non-local provider even when the owner is admin<br>• a local provider receives them only for an admin-owned briefing<br>• dropped-citation warnings kept and stored<br>• fallback on provider failure, on refusal, and without a provider |
| `briefing/BriefingDeliveryIT` | WireMock Slack stand-ins:<br>• a scheduled digest briefing (no LLM) is delivered only to its listed channel, with the HTML link, the run id on the delivery and a `briefing` notification row; the next run is rescheduled<br>• with no `channelIds`, every subscribed channel gets it and an alerts-only one does not<br>• the run is embedded when semantic search is available and not otherwise (Mockito) |
| `briefing/BriefingsControllerIT` | • CRUD with `next_run_at`, replace (disabled → null), delete and 404s<br>• validation in one 400 and the unsubscribed-channel warning<br>• scopes: read cannot create or run; run and preview are 401 anonymously under `openReads`<br>• run now (stats, citations, period, briefing updated) and 409 `BRIEFING_RUNNING`<br>• preview stores, notifies and schedules nothing<br>• run pagination with a cursor, 400s, a deleted briefing's runs<br>• the period since the last run<br>• settings export and import |
| `briefing/BriefingToolsIT` | • tool scopes and cost-bearing flags<br>• create, list, run, latest and long run, and digest through the MCP tools; validation errors<br>• Ask's `create_briefing` handler (validate, describe, execute with the agent owner) |

Test support:
- `testsupport/briefing/BriefingTestSources`: the Ask context plus `test-brief-quakes` and
  `test-brief-secret` (private), both ranked by `mag`.
- `testsupport/briefing/BriefingItSupport`: no chat provider by default, a reset retention stub,
  quake, watch, body and caller helpers, and Slack stand-in channels.
- `testsupport/briefing/BriefingHttp`: MockMvc helpers.
- `api/BriefingsControllerAccess`: exposes the Accept negotiation to tests.

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`.

`./gradlew lintOpenApi` → `Woohoo! Your API description is valid.`, 70 warnings (unchanged), 0
errors. `python3 scripts/generate-postman.py docs/OpenAPI.json postman` → 825 requests, all
operations covered.

**New dependencies** (all verified on Maven Central on 2026-10-12 as the latest releases; none has
runtime dependencies):
- `org.commonmark:commonmark` 0.30.1 (BSD-2-Clause)
- `org.commonmark:commonmark-ext-gfm-tables` 0.30.1 (BSD-2-Clause)
- `com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer` 20261011.1 (Apache-2.0 OR
  BSD-2-Clause)

## Notes for the next prompt

1. **Enrichment (030).** Reuse `CitationVerifier` as `LlmBriefingWriter` does: fill a `CitationSet`
   with what the prompt shows, then `addText`, then `verify`. Keep the fallback pattern: a failure is
   a value with its usage, never a thrown 5xx.
2. **Web Push (031).** Briefing notifications have event `briefing`, subtype `briefing.run`, `url` =
   the HTML rendition, and `briefingRunId` set.
3. **Feed keys (035).** Run renditions are ordinary `GET /v1/briefings/**` reads. `/v1/k/{key}/…`
   only needs to reach them. 035 also enforces restricted tokens on `/v1/digest` and on briefing
   run and preview, which `FailClosedRestrictionEnforcer` refuses until then.
4. **Places (036).** Extend `ScopeResolver` (or provide a primary bean) to fold `placeIds`/`viewId`
   into bbox and area filters. Then accept them in `BriefingValidator.SCOPE_MEMBERS` and drop them
   from `LATER_SCOPE_MEMBERS`.
5. **Rollups (038).** Provide a primary `DigestStatsProvider` backed by `activity_rollup` for
   `counts` (and previous-period `active`). Keep the method contracts: exclusive and inclusive seq
   bounds, hidden sources listed nowhere.
6. **Weather at places (056).** Add a `BriefingSectionContributor` bean: key `weatherAtPlaces`,
   markdown starting with `## Weather at places`. Accept `places`/`weatherProducts` in the scope and
   `places` on `/v1/digest`, and add `Digest.weatherAtPlaces` in `DigestService.render`.
7. **Atom/JSON feeds (058)** can page runs with `BriefingService.runs` and render content with
   `BriefingHtmlRenderer.fragment`.
8. **Signatures (109) and audio (141)** add `signature` and `audio` to `BriefingViews.run`.
   Validation refuses non-null `audio` until 141.
9. **Tests.** Briefing ITs import `BriefingTestSources` and extend `BriefingItSupport`. Do not
   import `SearchTestSources` in the same context: both it and the Ask context declare a primary
   `LlmClientFactory`.
