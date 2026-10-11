# 129 — Object Graph Workspaces And Annotations

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/129-CR-Object-Graph-Workspaces-And-Annotations.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults, analytic retention, intelligence conventions) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and **`docs/Intelligence-Design.md` is authoritative for the intelligence layer** (situations, findings, precursors, base rates, analogs, the forecast ledger, hypotheses, forecasts, impact projection and analysis cycles).
- **Previous Completion Report:** read `completion-reports/128-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Client-Contract.md` (authoritative for this prompt: object references, the object envelope, relation types, the workspace context schema, deep links, synchronisation rules)
- `docs/Intelligence-Design.md` §2, §12 (interfaces)
- Completion Reports of 014/037 (`asOf`), 015 (multiplexed SSE, tickets), 023 (scopes, principals, restricted tokens), 036 (places, saved views, watchlists), 039 (`private`), 049 and 069 (stations and receivers), 064 (Orekit predicted passes), 096 (STAC scenes), 098 (entities and links), 101 (intel events, investigations), 104 (facility graph and paths), 112 (share links), 115 (alert feedback), 116 (canonical events, observability, manifests), 117–128

## Objective

Give every client one shared object contract so no frontend re-implements Foresignal's intelligence. The globe, the analyst workstation, a satellite or RF console, a phone and an agent should all name, fetch, relate, annotate and focus on the same objects in the same way, and stay in sync when they look at the same investigation. Concretely: canonical **object references** and a uniform **object envelope**; a time-aware **relation graph** materialised from what Foresignal already knows; **annotations and human validation** of any object, feeding precision metrics; **workspaces** with a shared, versioned context (selection, time range, viewport, active situation) synchronised live over SSE and shareable as deep links; and a read-only **sensor discovery** view of what can observe an area and when.

## Principles (binding)

- **Foresignal stays headless.** Workspaces carry client layout only as opaque JSON; Foresignal never interprets it. No UI concepts enter the telemetry model.
- **One owner for shared state.** Workspaces, annotations and relations live here; clients cache but never fork them.
- **Useful without an LLM.** Everything in this prompt is deterministic.
- **Time-aware and private-aware.** Relations have validity intervals and honour `asOf`; anything touching a `private` object is private (039).

## Tasks

### 1. Object references and envelope (`ai.foresignal.object`)

- **Reference grammar** (`ObjectRef`, parse and format; Client-Contract §2): `{type}:{id}` with types `record` (`record:{sourceId}/{externalId}`, optional `@{seq}`), `entity`, `canonical-event`, `intel-event`, `situation`, `finding`, `precursor`, `base-rate`, `hypothesis-set`, `hypothesis`, `indicator`, `question`, `forecast`, `assessment`, `alert`, `investigation`, `place`, `view`, `watchlist`, `source`, `sensor`, `workspace`, `annotation`. Unknown types → 400 `OBJECT_REF_INVALID`.
- **`ObjectResolver` SPI:** one resolver per type (implemented by each owning package), returning the **envelope**: `{ref, type, title, summary (≤ 280 chars), status, severity, time {start, end, observedAt}, geometry (simplified, ≤ 500 vertices) or centroid, bbox, private, provenance {sourceIds, manifestId, asOfSeq}, textOrigin (deterministic | llm, for `summary`), links {self (type-specific API URL), related}, actions}`.
- **`actions`:** the operations the **calling token** may perform on this object, computed from AccessPolicy and object state, each `{id, label, method, href, scope}`: e.g. `add-to-workspace`, `track` (creates a 017 watch for a record or entity), `annotate`, `verdict`, `acknowledge` (alerts), `open-investigation`, `propose-watch` (through the proposal pipeline when the caller lacks agent scope). Clients render buttons from this list instead of hard-coding permissions.
- `GET /v1/objects/{ref}?asOf=` (read) and `POST /v1/objects/resolve {refs[≤ 500], asOf?}` (read; missing refs listed, not errors).
- `GET /v1/contract` (read): contract version, object types, relation types, context schema version, deep-link version.

### 1a. Dossier and context summary (composed reads)

So a client panel never fans out dozens of requests on a selection change:
- **`GET /v1/objects/{ref}/dossier?include=overview,imagery,forecast,impacts,related,timeline,analysis&asOf`** (read; ≤ 64 KB; ETag): the envelope plus, per requested section, a bounded summary with links to the full data:
  - `overview`: the type-specific key fields (for a record, its latest payload fields from `fieldSpecs`);
  - `imagery`: raster frames (042–046), SatDump products (049) and STAC scenes (096) intersecting the object's geometry in the time window, newest first (≤ 12), with frame and thumbnail URLs;
  - `forecast`: official forecasts linked to the object (e.g. NHC track and cone records for a storm, labelled `issuer: official`) and ledger questions about it with their ensemble probability, ICD term, forecasters and knowledge cutoff (labelled `issuer: foresignal-ensemble` or the forecaster id);
  - `impacts`: 127 projected impacts for the object, with probabilities, dependency paths and labels;
  - `related`: relations grouped as `nearby` (spatial, with distance), `operational` (entities and facilities linked by relations), `infrastructure` (104 dependencies) and `historical` (121 analogs);
  - `timeline`: the object's history (versions, alerts, findings, situation log entries) newest first (≤ 50);
  - `analysis`: findings, hypothesis sets and assessments that reference the object.
  Every section carries `status` (ok | empty | unavailable | not-installed) so a panel can render partially. With `facets=true`, sections that list evidence include counts by evidence class (imagery | signals | reports | documents | sensor | social | other, from 119's signal-family registry) and by source.
- **`POST /v1/context/summary {viewport, timeRange, asOf?, filters, layers}`** (read; ≤ 32 KB; cached per normalised context for 30 s; also `GET /v1/workspaces/{id}/summary` for a workspace's current context): layer counts (current head ⋈ live counts per kind or layer in the viewport, aggregated, never feature lists), an activity feed (alerts, findings, situation changes and notable records in the window, merged chronologically, each tagged with its signal family), insights (top findings, ledger forecasts and hypothesis band changes intersecting the viewport, each with probability, term, issuer, base rate and knowledge cutoff), and the top related objects. Deterministic; any LLM text included is labelled `textOrigin: llm` and omitted when no provider is configured.

### 2. Relation graph (temporal, provenance-aware)

- `relation`: `id`, `from_ref`, `to_ref`, `type` (Client-Contract §4: `member-of`, `observed-by`, `same-as`, `located-in`, `near`, `depends-on`, `affects`, `derived-from`, `evidence-for`, `evidence-against`, `about`, `mentions`, `precedes`, `causes`, `related-to`); `precedes` comes from validated precursors (120) and timelines; `causes` is never machine-asserted: it comes only from operators or from a hypothesis's statement (label ≤ `possible` until an operator confirms it), `valid_from`, `valid_to` (null = open), `confidence` (0–1), `label` (confirmed | likely | possible | speculative; reuse 104's labels), `provenance` (`{component, version, manifestId}` or `{operator: principal}`), `private`, `created_at`, `retired_at`. Indexes on `(from_ref, type)`, `(to_ref, type)`, and validity.
- **`RelationProjector`** (version `1.0.0`): materialises relations from existing structures, incrementally (ChangeBus and the owning services' events, durable cursor `relation.cursor` in the consumer clamp) and by a nightly reconcile: situation members (`member-of`), intel-event members (`evidence-for` / `evidence-against` by role), canonical-event observations (`observed-by`, `same-as` for re-reports), 098 entity links (`same-as` with their validity), 104 facility edges (`depends-on` with their labels), 125 indicators and evidence (`evidence-for` / `evidence-against` per hypothesis), 122 question links (`about`), investigation items (`member-of`), findings' members and series (`derived-from`), places containing objects (`located-in`, computed on demand, not stored). Projected relations are never edited by hand; operators and agents add their own (`provenance.operator`).
- **Queries:**
  - `GET /v1/objects/{ref}/related?types&direction&asOf&minConfidence&limit` (read).
  - `POST /v1/graph {refs[≤ 50], depth ≤ 2, types?, asOf?, limit ≤ 2,000}` (read) → nodes (envelopes) and edges, for graph views.
  - `POST /v1/graph/common-dependencies {refs[≤ 20], asOf?}` (read): facilities and infrastructure the given objects share through `depends-on` and `located-in` paths (104), each with its weakest-edge label: "which apparently unrelated disruptions share a dependency?"
- `POST /v1/relations` and `DELETE /v1/relations/{id}` (agent; operator relations only).

### 3. Annotations and validation

- `annotation`: `id`, `target_ref`, `kind` (note | verdict; there is no `tag` kind: labelling objects uses 130 tags), `verdict` (confirmed | rejected | needs-review | duplicate-of), `duplicate_of_ref`, `body` (markdown ≤ 10,000 chars), `tags`, `author` (principal), `private`, `created_at`, `updated_at`, `deleted_at`.
- `GET /v1/objects/{ref}/annotations` (read), `POST /v1/objects/{ref}/annotations` (read scope may add notes; **verdicts need agent**), `PATCH/DELETE /v1/annotations/{id}` (author or admin).
- **Validation feeds the loop:**
  - `GET /v1/analytics/precision?component&since` (read): for findings (by detector), intel events (by recipe), situations (linker) and precursor-active findings, the share confirmed among verdicts, with a Wilson interval and counts. `needs-review` counts as unlabelled.
  - Verdicts on objects inside a time window become candidate **historical benchmark cases** for 123 (`benchmark_case` with `source=operator`), proposed, never auto-added.
  - 128's cycle packet lists new verdicts since the previous run; a `rejected` verdict on a finding or intel event lowers it to `dismissed` only when the author is agent scope and says so explicitly (`applyStatus: true`).

### 4. Workspaces and context synchronisation

- `workspace`: `id`, `name`, `owner` (principal), `members` (principal → role: viewer | editor), `context` jsonb (Client-Contract §5: `selectedRefs`, `focusRef`, `timeRange {start, end}`, `asOf`, `playback {state, rate}`, `viewport {bbox | center+radiusKm}`, `activeSituationId`, `activeHypothesisSetId`, `filters`, `layers` (opaque strings)), `context_version` (int), `layout` jsonb (opaque, ≤ 64 KB, client-owned), `saved_view_ids`, `investigation_id`, `private`, `created_at`, `updated_at`.
- `GET/POST /v1/workspaces`, `GET/PATCH/DELETE /v1/workspaces/{id}` (read scope for own and member workspaces; writes need editor role and agent scope or the owner's token), `PUT /v1/workspaces/{id}/layout` (editor).
- **Context updates:** `PATCH /v1/workspaces/{id}/context` with `If-Match: <context_version>` and a JSON merge patch; 412 on a stale version; the response carries the new version. Validation per the Client-Contract schema (refs well-formed, time range ordered, ≤ 200 selected refs).
- **Live sync:** SSE topic `workspace` on `/v1/events/stream` (filter `workspaceId`): `workspace.context` events carry `{version, patch, originClientId}`; clients ignore their own `originClientId`. Presence: `workspace.presence` events from connected stream subscriptions (`clientId`, `clientKind` such as globe | analyst | satellite | rf | mobile | agent, `connectedAt`), so several apps or monitors can follow one investigation.
- **Deep links:** `GET /v1/workspaces/{id}/context-link` (read) returns the Client-Contract §6 deep-link payload (version, workspace id, base64url context ≤ 2 KB); `POST /v1/context-links {context}` (read) validates an ad-hoc context and returns the encoded payload. When 112 share links exist, `POST /v1/workspaces/{id}/share` (agent) issues a read-only share link to the workspace context.
- MCP: `get_workspace_context`, `set_workspace_focus` (agent), so an agent can point every open client at what it is talking about.

### 5. Sensor discovery (read-only)

- `GET /v1/sensors?bbox|placeId&from&to&kinds` (read): what can observe an area in a window, merged into one list of `sensor` envelopes:
  - enabled sources with 116 observability above 0.2 for the area (with `p_detect`, method and families);
  - local receivers (069) and SatDump stations (049) whose range covers the area;
  - predicted satellite passes over the area (064 Orekit), annotated from a curated, operator-extensible `src/main/resources/intel/satellite-capabilities.json` (seeded with public facts for major civil and commercial Earth-observation, weather, SAR, AIS and ADS-B satellites: operator, sensor types, nominal resolution, swath) with times and elevation; no tasking;
  - STAC scene footprints (096) and raster products (042) intersecting the area in the window, with links.
- No tasking: Foresignal cannot task public satellites or third-party sensors; local receivers are configured through their sources.

### 6. Spec, SDK hooks and docs

- `docs/OpenAPI.json`: `ObjectRef` (pattern), `ObjectEnvelope`, `ObjectAction`, `Dossier`, `ContextSummary`, `ObjectResolveRequest/Result`, `Relation`, `GraphRequest/Result`, `CommonDependencies`, `Annotation`, `Precision`, `Workspace`, `WorkspaceContext`, `ContextPatchEvent`, `PresenceEvent`, `ContextLink`, `Sensor`, `Contract`; tags `Objects`, `Workspaces`; `x-foresignal-scope` everywhere; SSE schemas; Postman BODIES for resolve, graph, annotate, create workspace, patch context.
- Keep `docs/Client-Contract.md` in sync with what was built (it is the frontend teams' reference); record any deviation in the CR.
- Note for 141 (SDKs): the generated SDKs expose `ObjectRef`, the envelope and a `WorkspaceSync` helper (subscribe, apply remote patches, send local patches with `If-Match`, ignore own `originClientId`).
- AccessPolicy rows as listed. Problem codes: `OBJECT_REF_INVALID` (400), `UNKNOWN_OBJECT`, `UNKNOWN_WORKSPACE`, `UNKNOWN_ANNOTATION` (404), `WORKSPACE_CONTEXT_STALE` (412), `WORKSPACE_FORBIDDEN` (403).
- `docs/Operator-Guide.md` "Objects, workspaces and validation"; `docs/Agent-Guide.md`: refer to objects by `ObjectRef`; use workspace focus to show the operator what you mean.

## Tests to write (do not run)

- `ObjectRefTest`: every type round-trips; record refs with and without `@seq`; invalid refs rejected.
- `DossierIT`: each section for a storm record (official track and cone labelled `official`, a ledger question labelled with its issuer and cutoff), partial rendering when a section is unavailable, size cap.
- `ContextSummaryIT`: layer counts from head ⋈ live, merged activity feed, insights with issuer and base rate, caching per normalised context.
- `ObjectActionsTest`: actions differ for read, agent and admin tokens and by object state.
- `ObjectResolverIT`: an envelope for each type; `asOf` honoured for records and situations; private objects hidden from non-private tokens; batch resolve lists missing refs.
- `RelationProjectorIT`: relations appear for situation members, intel-event members, canonical-event re-reports (`same-as`), entity links and facility edges; validity follows the source structure; nightly reconcile repairs a deleted relation; cursor restart exactly-once.
- `GraphQueryTest`: depth and type limits; `asOf` validity filtering; common dependencies through 104 paths with weakest-edge labels.
- `AnnotationIT`: notes by read scope, verdicts need agent; precision with Wilson interval; benchmark case proposals; `applyStatus` dismissal only when explicit.
- `WorkspaceSyncIT`: two SSE clients on one workspace; a context patch with the right `If-Match` reaches the other client with `originClientId`; a stale patch → 412; presence events; member roles enforced.
- `ContextLinkTest`: encode/decode round trip, size cap, schema validation.
- `SensorDiscoveryIT`: sources by observability, receivers by range, predicted passes and STAC scenes in the window.
- `ObjectControllersIT`: endpoints, scopes, MCP shapes.

## Acceptance criteria

- Every Foresignal object type has a canonical reference and a uniform envelope, resolvable singly or in batches, `asOf`-aware.
- A time-aware relation graph is materialised from existing structures and queryable by neighbourhood, subgraph and common dependencies.
- Any object can be annotated; verdicts produce precision metrics and benchmark-case proposals.
- Workspaces keep a versioned context that several clients edit and follow live, and that travels as a deep link.
- Sensor discovery lists what can observe an area and when.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 124, 139, 142, 143, 144) and in the sanctioned narrow runs of 007–009.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and `docs/Intelligence-Design.md`, and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `129: Object Graph Workspaces And Annotations`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/129-CR-Object-Graph-Workspaces-And-Annotations.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `129: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
