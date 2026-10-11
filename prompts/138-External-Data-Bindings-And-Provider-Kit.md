# 138 — External Data Bindings And Provider Kit

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/138-CR-External-Data-Bindings-And-Provider-Kit.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults, analytic retention, intelligence conventions) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and **`docs/Intelligence-Design.md` is authoritative for the intelligence layer** (situations, findings, precursors, base rates, analogs, the forecast ledger, hypotheses, forecasts, impact projection and analysis cycles).
- **Previous Completion Report:** read `completion-reports/137-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/External-Data-Protocol.md` (all, especially §5 bindings), `docs/Conventions.md` "Operator choice", `docs/Source-Catalog.md` "External provider ideas"
- `docs/Intelligence-Design.md` §4.5–§4.6 (evidence identity, observability), §8.6 (external forecasters), §12.7 (social signals)
- Completion Reports of 024 (EDP core, custom sources), 036 (places), 093 (series model), 098 (entities), 116 (canonical families, originators, observability), 118 (situations), 119 (analysis-series registry), 122 (ledger, forecasters, ensemble), 125 (evidence items, indicators), 129 (object references), 131 (chronicle layers), 133 (news articles), 135–137 (social pipeline)

## Objective

Make atomic EDP streams useful inside the intelligence layer. Prompt 024 lets any third-party provider register and deliver items; this prompt lets the operator **bind** those streams to where they belong (series, canonical-event families, the social and news pipelines, hypothesis evidence and indicators, forecast questions, places, situations and chronicles), applies each provider's declared originator and coverage to evidence counting and observability, and ships the **provider kit** so specialist sources (web maps, scrapers, Telegram channels, prediction markets, niche feeds) can live in their own projects.

## Principles (binding)

- **Foresignal ships no fringe adapters.** Sources with unofficial endpoints, scraping, browsers, restrictive terms or narrow audiences are EDP providers in separate projects; the operator decides which to connect. Nothing in core blocks or polices them.
- **Atomic streams do nothing until bound.** Unbound atomic items are stored, searchable and rule-able, but never enter series, evidence, indicators or forecasts by themselves.
- **Provenance travels.** Every bound item keeps its provider (source), stream and originator. Independence counts originators, never providers: the item's `origin` or else the manifest originator, with `aggregates`, `derivedFrom` and upstream ids joining re-published copies into one evidence group (EDP §3.4).
- **Receipt is the clock.** Bindings use `effectiveKnownAt` (024), never a provider's claimed time alone, for series `knownAt`, look-ahead and forecast `made_at`.
- **Scored, not trusted.** External forecasts are scored in the ledger and join the ensemble only as their skill earns.

## Tasks

### 1. Bindings (`ai.foresignal.inbound.edp.binding`)

- `external_binding`: `id`, `source_id`, `stream_id`, `target` (`series | canonical-family | social | news | evidence | indicator | forecast | place | situation | chronicle`), `config` jsonb (per target, below), `filter` jsonb (optional property conditions in the 017 condition grammar), `enabled`, `created_by`, timestamps, `manifest_sha256` (the manifest version it was made for; a breaking manifest update disables bindings whose stream changed, with a notification).
- A `BindingRouter` consumes the change bus for EDP records (durable cursor `external.binding.cursor`, joining 019's retention clamp) and applies each enabled binding to matching records, idempotently by `(binding, record version)`. New bindings backfill from stored records within retention (`backfill: true` default, capped at 100,000 items per binding).
- **Targets:**
  - `series` (observation streams): register `series:ext-{id}/{kind}/{seriesKey}` in 119's analysis-series registry with `unit`, `transform` (none, log1p, diff), `knownAt` from the item, release lag from the manifest or config; the 093 series reader serves them.
  - `canonical-family` (event streams): link records into a 116 canonical family (`config.family`, matching tolerances from the family's rules or overrides), with the manifest originator (and its `aggregates`) in the originator registry, so EDP events corroborate or are corroborated once per independent origin; the stream's declared `coverage` becomes a `declared` observability estimate (low confidence) for that family and area.
  - `social` (`social-post` documents): convert to 135 `social-post` records (platform from `config.platform` or the document's `properties.platform`; author key salted as 135 does) so 136 narratives and 137 comparisons include them; the collection type is `import`, so they never feed velocity baselines unless the manifest declares the stream `complete`.
  - `news` (`article` documents): convert to 133 `news-article` records (outlet resolved from the URL's domain) so stories, coverage and blindspots include them.
  - `evidence`: create 125 evidence items on a hypothesis set (`config.hypothesisSetId`, `config.hypothesisId?`, Admiralty `sourceReliability` and `informationCredibility`, default `F6` unless set).
  - `indicator`: when an item matches the filter, mark a 125 indicator `observed` (feature, event or observation streams) or `reported` (document streams), with the item as evidence.
  - `forecast`: forecast items become forecasts of the external forecaster `edp:{sourceId}/{streamId}` (registered on first use, `type=external`) on linked ledger questions. **Question links** (`external_question_link`: `source_id`, `stream_id`, `external_question_id`, `question_id`, `orientation` (`same | inverse`)) map the provider's question ids to Foresignal questions; items with `questionRef` need no link. A forecast is recorded on a probability change ≥ 0.02 or at least daily per question; `made_at` is never earlier than Foresignal received the item. External forecasters join the 122 ensemble only after 30 resolved linked questions.
  - `place`: expose the latest bound value or document per stream as a place attribute (036 place response `external` block).
  - `situation`: link matching records to a situation (`config.situationId`) or let the 118 linker consider them (`config.linker: true`).
  - `chronicle`: a 131 chronicle layer type `external-stream` (`{sourceId, streamId, filter}`) captures the stream into a chronicle.
- **Suggested bindings:** on approval, the manifest's `suggestedBindings` are listed; `POST /v1/external/sources/{sourceId}/bindings/accept-suggested` creates them (each still editable).
- `POST /v1/external/bindings/{bindingId}/preview` evaluates a binding over the last 100 items without side effects (what would be created or changed).

### 2. Provider kit and catalogue

- `docs/protocol/edp-1.0/` (exported schemas from 024) plus `docs/protocol/edp-1.0/examples/`: one minimal provider per item type as annotated JSON and a 60-line Python reference provider (push and pull) that reads a CSV and serves observations; the examples are documentation, not part of the server.
- `docs/Source-Catalog.md` "External provider ideas": the specialist sources Foresignal does not ship (flight and ship web maps, Liveuamap, OREF sirens, UKMTO, Hormuz dashboards, Broadcastify listener counts, TinyGS, unofficial camera loaders, Facebook pages, TikTok, Telegram channels, prediction markets (Kalshi, Polymarket), Metaculus, VIEWS, CME and ICE data, Yahoo Finance, CNN Fear & Greed, Google Trends, Electricity Maps, NewsGuard and Ad Fontes ratings, Shodan, Windy webcams, article bodies and bylines, GDELT URL metadata), each with the stream modes and item types a provider would use and the binding that makes it useful.
- 141's SDK work adds the Python and TypeScript provider libraries and `foresignal edp validate`; note the interface needed in the CR.

### 3. API, MCP, SSE, spec, docs

- `GET /v1/external/sources/{sourceId}/bindings` (admin), `POST /v1/external/sources/{sourceId}/bindings` (admin; `ExternalBindingInput`), `POST /v1/external/sources/{sourceId}/bindings/accept-suggested` (admin), `GET/PUT/DELETE /v1/external/bindings/{bindingId}` (admin), `POST /v1/external/bindings/{bindingId}/preview` (admin), `GET /v1/external/sources/{sourceId}/question-links` (read), `POST /v1/external/sources/{sourceId}/question-links` (agent), `DELETE /v1/external/question-links/{linkId}` (agent).
- MCP: `list_external_sources` (read), `get_external_source` (read).
- SSE topic `external`: `external.source.state` (registered, approved, suspended, manifest changed), `external.binding.disabled`.
- `docs/OpenAPI.json`: as specified there (`ExternalBinding`, `ExternalBindingInput`, `ExternalBindingPreview`, `ExternalQuestionLink`, `ExternalQuestionLinkInput`; chronicle layer selector `external-stream`; place `external` block); status implemented; pending list updated.
- `docs/Operator-Guide.md` "Binding external streams": each target with an example, question links, suggested bindings, what happens on a breaking manifest update.
- AccessPolicy rows. Problem codes: `UNKNOWN_BINDING`, `UNKNOWN_QUESTION_LINK` (404), `BINDING_TARGET_INVALID` (400, with the stream's item type and the target's requirement), `QUESTION_ALREADY_LINKED` (409).

## Tests to write (do not run)

- `BindingRouterIT`: each target from a fixture provider; idempotency per record version; backfill cap; durable cursor resume; breaking manifest update disables affected bindings and notifies.
- `EdpEvidenceIndependenceTest`: EDP events with a declared originator that `aggregates` a built-in source do not add an independent evidence group; distinct originators do.
- `EdpObservabilityTest`: declared coverage produces a low-confidence `declared` estimate used by `canSeeAbsence`.
- `EdpForecastBindingTest`: question links with orientation, `questionRef` items, ≥ 0.02 change or daily cadence, `made_at` never before receipt, ensemble entry only after 30 resolutions.
- `EdpSocialNewsBindingTest`: social-post documents reach narratives without feeding velocity baselines; article documents reach stories.
- `BindingPreviewTest`: preview has no side effects.
- `ExternalBindingControllersIT`: endpoints, scopes, MCP shapes.

## Acceptance criteria

- An approved provider's atomic streams can be bound to series, canonical families, the social and news pipelines, evidence, indicators, forecasts, places, situations and chronicles, with provenance and independence preserved.
- External forecasts are scored in the ledger and join the ensemble only on earned skill.
- The provider kit and the provider-ideas catalogue let specialist sources be built as separate projects.
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
2. **Commit** all changes with the message `138: External Data Bindings And Provider Kit`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/138-CR-External-Data-Bindings-And-Provider-Kit.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `138: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
