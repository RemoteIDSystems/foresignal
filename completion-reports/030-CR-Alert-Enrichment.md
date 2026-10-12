# 030-CR-Alert-Enrichment

| | |
|---|---|
| Prompt | 030-Alert-Enrichment.md |
| Status | Complete |
| Implementation commit | 1e81411538ed53dfe6b4460e4f3e76726936a2f7 |
| Date | 2026-10-12 |

## Summary

Alerts of rules with `enrich=true` get a short, cited LLM summary of what Foresignal knows around
them. Channels that wait for it send it with the alert. Channels that edit in place update a message
already sent. Enrichment never delays delivery beyond each channel's `wait_for_enrichment_seconds`.

- **Model.** `alert` gains `summary` (text), `enriched_at` and `summary_citations` (jsonb, an array
  of `AskCitation` objects). Hibernate adds the columns. `AlertView` carries all three; a
  constructor without them is kept for existing callers. `AlertStore` reads them and gains
  `findAll(ids)`.
- **`EnrichmentWorker` (`ai.foresignal.trigger`).**
  - It is an `AlertBus` listener in front of its own bounded queue (256) and a pool of virtual
    threads (4). It subscribes at the new `StartupOrder.ENRICHMENT` (19): after the notification
    router, before the trigger engine.
  - Only `fired` events of unsuppressed alerts are considered. Before an alert takes a queue slot,
    `EnrichmentService.admit` checks the rule's `enrich` flag, `llm.enrichmentEnabled` and that a chat
    provider is assigned. When the queue is full the alert is dropped and
    `foresignal.enrichment.dropped` is counted.
  - `foresignal.enrichment.auto-start` (off in tests) controls the subscription.
- **`EnrichmentService`.**
  - Skip reasons, counted in `foresignal.enrichment.skipped{reason}`:
    - `rule`, `disabled`, `no_provider` (checked at admission);
    - `gone`, `resolved`, `stale` (checked after the alert is re-read; `stale` means `fired_at` is
      more than 10 min old);
    - `private_source` (Deviation 3).
  - The call: the chat provider with purpose `enrichment`, effort `low`, `maxTokens` 600, no tools and
    the constant `EnrichmentPrompts.SYSTEM`. The anchor and the context go in the user turn inside a
    `<data>` block. A 20 s timeout is enforced around the call on a virtual thread.
  - Failure reasons, counted in `failed{reason}`: `budget`, `provider`, `refusal`, `timeout` and
    `empty`. Failures are logged and never retried.
  - Citations are verified with 028's `CitationVerifier`. Citations it cannot back are also removed
    from the text.
  - The summary is stored through `AlertStore.write` with a new `rev`, which publishes the new
    `AlertEvent.ENRICHED`.
  - The enriched alert is then embedded with `embedDocument("alert", id, message + summary, geometry,
    T, source, kind)` when an embedding index is available or a reindex is pending.
  - Metrics: `foresignal.enrichment.duration` and `succeeded`, plus the counters above.
- **`EnrichmentContextBuilder` (Architecture §23).** The anchor time is `T = observedAt`. The anchor
  geometry is that of the version loaded by `seq`, else `record_snapshot.geometry`. The context
  contains:
  - the alert, and the record (`from: version | snapshot`);
  - `history`: up to 10 prior versions, newest first, HEARTBEAT excluded, short form with
    `persistReason`;
  - `nearby.records`: records of any kind within the radius and `T ± window`. The query takes the
    latest version in the window per entity (`DISTINCT ON`), excludes HEARTBEAT and the alerted
    entity, and uses `ST_DWithin` on geography after an index-friendly envelope prefilter. Head ⋈
    live rows whose live position and time are inside are added, marked `liveOnly` (movement without
    a version). The newest 30 are kept.
  - `nearby.alerts`: up to 10 alerts in the radius and window, found by their snapshot geometry,
    with their state;
  - `related`: when semantic search is available, the top 5 hybrid matches for the alert message.
    The search is limited to the window and the radius envelope, and the radius is then checked
    exactly in SQL;
  - `sections`: one per `EnrichmentContextContributor` bean (the new SPI, for 056's
    `alert.context.weather`). A failing contributor is skipped.

  Records without geometry skip the spatial parts. `EgressPolicy` (LLM-context path) excludes
  withheld sources in SQL and filters every item again. The context is capped at 24 KB: items are
  trimmed in the order related, nearby records, nearby alerts, history, sections, each from its end.
  Only the kept items are citable.
- **Channels (018), `EnrichmentDeliveries` (`ai.foresignal.notify`).** `NotificationRouter` handles
  `enriched` bus events, in order after the alert's `fired` event, and:
  - updates the persisted `alert.fired` notification row: the summary is added to `data` and the
    default body is re-rendered, so the row, the future inbox and later lifecycle updates show it;
  - sets `next_attempt_at = now` on the alert's pending sends held by the enrichment wait
    (`deferred_reason=enrichment`, due in the future, not leased);
  - enqueues one `update` delivery for each delivered, unfolded send that went out before the summary,
    less than 1 h ago, carries a provider reference, and went to an enabled channel whose type edits
    in place (Discord, Telegram, Matrix) with `editInPlace` on. Other channels get nothing extra.

  `DeliveryWorker` now renders `alert.fired` messages from the latest alert's summary, read at send
  time. An `update` delivery of the fired notification itself edits the original message
  (`renderUpdate`). If the edit is impossible it is settled with a note and nothing is resent.
- **API, SSE, MCP.**
  - `AlertMapper` adds `summary`, `enrichedAt` and `summaryCitations` (`[]` until enriched) to `GET
    /v1/alerts/{id}`, the lists and the `AlertEvent` payloads.
  - `AlertsTopic` sends `alert.enriched` with the full `Alert` and id = the new `rev` (Deviation 1).
    Replay is unchanged and includes the summary.
  - MCP `get_alert` includes `summary`, `enrichedAt` and `summaryCitations` in both detail levels.
  - `testTrigger` already reported `enrich` (017). The rule bounds were already validated (017).
- **Retention.** `EmbeddingRetentionStep` also deletes `alert` documents whose alert is gone (027's
  step deferred this to 030).
- **Spec, Postman, docs.**
  - `docs/OpenAPI.json`: `alert.enriched` id and descriptions on both streams; the `streamAlerts`
    description; the descriptions of `Alert.summary`, `enrichedAt`, `summaryCitations` and
    `TriggerRuleInput.enrich`; a changelog entry. No new operations, so `AccessPolicy` and
    `pending-operations.txt` are unchanged.
  - Postman was regenerated.
  - `docs/Operator-Guide.md` has a new "Alert enrichment" section: wait versus edit in place per
    channel type, cost per alert, egress.
  - `docs/Invariants.md`: EGR-1 adds `EnrichmentContextIT`; EGR-3 adds `EnrichmentPromptInjectionIT`.

Tests were written but not run (feature prompt).

## Files created / modified

**New: `ai.foresignal.trigger`**
- `EnrichmentWorker`, `EnrichmentService`, `EnrichmentContextBuilder`, `EnrichmentContext`
- `EnrichmentAnchor`, `EnrichmentContextContributor`, `EnrichmentPrompts`, `EnrichmentProperties`

**New: other**
- `notify/EnrichmentDeliveries`

**Changed**
- `store/AlertEntity`, `trigger/AlertView`, `trigger/AlertStore`, `trigger/AlertEvent` (`ENRICHED`,
  `alert.enriched`)
- `api/mapper/AlertMapper`, `stream/sse/AlertsTopic`, `mcp/tools/AlertItems`, `mcp/tools/GetAlertTool`
- `notify/NotificationRouter`, `notify/DeliveryWorker`, `notify/NotificationStore` (`refresh`)
- `search/EmbeddingRetentionStep`, `config/StartupOrder` (`ENRICHMENT`)
- `application.yml` (`foresignal.enrichment.*`), `application-test.yml` (worker not auto-started,
  timeout 2 s)
- `docs/OpenAPI.json`, `postman/Foresignal.postman_collection.json`, `docs/Operator-Guide.md`,
  `docs/Invariants.md` (rows extended; existing text kept)
- Tests: `stream/sse/EventSchemaConformanceTest` (an `alert.enriched` payload factory: the test's
  table must match the spec's current events), `stream/sse/AlertsTopicTest` (the enriched frame)

## Deviations from the prompt

1. **`alert.enriched` id is the alert's new `rev`, not the alert id.** `/v1/alerts/stream` resumes by
   `rev` (017: "`id:` = alert rev"). An event whose id is the alert id would make a client that
   reconnects resume from far below its position. Storing the summary bumps `rev` like every alert
   change, and the event carries it. The prompt's "resume semantics unchanged" holds. The spec text
   "id = alert id" was amended, with a changelog entry.
2. **The history query is our own SQL, not the `/versions` service.** That service lists versions
   ascending from the start, without a seq bound or a HEARTBEAT filter. The builder selects the 10
   prior non-heartbeat seqs (`seq < :seq ORDER BY seq DESC`) and loads them with
   `RecordQueryService.versionsBySeq`, so the mapping and titles are those of the versions endpoint.
3. **An alert on a source withheld from the provider is not enriched** (skip reason
   `private_source`). The alerted record itself may not leave through the LLM path (EGR-1), and a
   summary without it would be meaningless.
4. **Citations that verification drops are removed from the summary text too,** not only left out of
   `summaryCitations`, so a channel never shows an invented reference.
5. **No `renderUpdate` for Slack or Mattermost.** 018 implemented edit in place for Discord, Telegram
   and Matrix only: Slack and Mattermost are incoming webhooks without bot tokens. The edit path covers
   the three types that have it. Slack and Mattermost rely on the wait, as the Operator Guide says.
6. **The enrichment edit reuses the fired notification.** The `update` delivery points at the
   `alert.fired` notification itself, rather than a new notification row. So no extra inbox or
   `notification` SSE entry appears, and the edit renders exactly the original message with the
   summary (channel templates included).
7. **Admission happens before queueing.** The rule, setting and provider checks run before an alert
   takes one of the 256 slots, so non-enriched alerts cannot fill the queue in a storm. The open-state
   and age checks run when the alert is dequeued.
8. **Test configuration.** `foresignal.enrichment.auto-start: false` and `timeout: 2s` in
   `application-test.yml`. The enrichment ITs start the worker themselves when they need it, and the
   timeout test runs in seconds.

## Issues found / open questions

1. **Unverified at run time.** No test has run. These are first exercised at checkpoint 041:
   - the spatial SQL (geography casts, the snapshot-geometry expression on `alert`);
   - the live-only scenario (it relies on `test-track`'s 60 s / 1,000 m sampling producing no version
     for a 450 m move at 0 knots);
   - the heartbeat scenario (copied from `DigestStatsServiceIT`'s 580 s TTL heartbeat).
2. **Small race on in-flight sends.** A send that the delivery worker has already claimed and is
   rendering when the summary is stored can go out without the summary. It is then not edited,
   because it was not yet `delivered` when the edit pass ran. The window is the duration of one send.
3. **Cost of the nearby-alerts query.** Alerts have no geometry column. The query filters on
   `fired_at >= T − window` (indexed), then evaluates the snapshot geometry per row. That is fine at
   alert volumes; a geometry column would be a later optimization (Hibernate-only schema, scope).
4. **Data:** the test fixture `EnrichmentPromptInjectionIT` deliberately contains an injected
   instruction. No real instructions directed at an agent were found in data, docs or tool output.
5. **Owner decisions carried from 012–029** are still open.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `trigger/EnrichmentContextIT` | `TestRecords`:<br>• radius and window across kinds (a vessel of another kind kept; far and old quakes left out; the alerted entity excluded)<br>• a HEARTBEAT-only entity excluded<br>• live-only movement included (`liveOnly`)<br>• two prior versions, newest first, with `persistReason`<br>• nearby alerts with state<br>• citable set<br>• snapshot fallback (record and anchor from the snapshot)<br>• no-geometry path<br>• private records withheld from a remote destination and sent to a local one<br>• the size cap (history kept, newest nearby kept, trimmed items not citable)<br>• the semantic path (mocked search: the alerted, far and private hits filtered; request mode, doc type, bbox, window, axis and limit)<br>• contributor section, and a failing contributor skipped<br>• the envelope helper |
| `trigger/EnrichmentWorkerIT` | `FakeLlmClient`, WireMock:<br>• summary stored with verified citations (bad seq dropped from text and citations), `enrichedAt`, `rev` bump, request shape (purpose, effort, 600 tokens, no tools, system constant), `alert.enriched` on the bus<br>• waiting Slack delivery pulled forward and rendered with the summary; the notification row updated<br>• a delivered Discord message edited with PATCH, and the Slack channel gets nothing extra<br>• skips: disabled, no provider, stale, resolved, `enrich=false`<br>• `testTrigger` reports `enrich` without calling the model<br>• provider error, refusal and budget failures leave the delivery at T+30 s with no summary; the budget-refused call is never sent<br>• timeout<br>• queue overflow counted, and only unsuppressed `fired` events admitted<br>• acceptance through the running worker<br>• no provider: delivered unchanged after the wait<br>• embedding as an `alert` document only with an index<br>• unverified citations removed from the text |
| `trigger/EnrichmentPromptInjectionIT` | • an injected nearby record appears only inside `<data>`, never in the system prompt<br>• no tools offered and one call<br>• an invented citation dropped<br>• no configuration changed |
| `stream/sse/AlertsTopicTest` (added test) | the `alert.enriched` frame: full Alert, cursor = new rev |
| `stream/sse/EventSchemaConformanceTest` (added factory) | `alert.enriched` payloads validate against the `Alert` schema on both streams |

Test support:
- `testsupport/enrichment/EnrichmentTestSources`: the Ask context plus a switchable
  `alert.context.weather` contributor.
- `testsupport/enrichment/EnrichmentItSupport`: the anchor, ingest helpers with explicit times,
  enrich rules, destinations, metric reads and stand-in channels.

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`.

`./gradlew lintOpenApi` → `Woohoo! Your API description is valid.`, 70 warnings (unchanged), 0
errors. `python3 scripts/generate-postman.py docs/OpenAPI.json postman` → 825 requests, all
operations covered.

No new dependencies.

## Notes for the next prompt

1. **Web Push (031).** `webpush` is a fan-out type without edit in place. Enriched alerts reach it only
   through the wait. `NotificationMessage.data.summary` is present on the stored `alert.fired` row
   once enriched.
2. **Inbox (033).** The `alert.fired` notification row is updated in place with the summary
   (`NotificationStore.refresh`). No separate notification is created for an enrichment, and the inbox
   can show `data.summary`.
3. **Weather context (056).** Declare an `EnrichmentContextContributor` bean with key
   `alert.context.weather`. Respect `EnrichmentAnchor.allows(sourceId)` for any record you include.
   Sections are trimmed last, after related, nearby and history items.
4. **AlertBus listeners.** There is a new event type, `enriched`. Listeners that switch on
   `AlertEvent.type()` must ignore it unless they mean to handle it, as `NotificationRouter`'s
   `default` branch does.
5. **Tests.** Enrichment ITs import `EnrichmentTestSources` and extend `EnrichmentItSupport`. The
   worker is not running in tests unless started, so a test that creates an enrich rule is not
   raced by background enrichment.
