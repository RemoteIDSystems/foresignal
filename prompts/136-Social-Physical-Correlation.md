# 136 — Social Physical Correlation

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/136-CR-Social-Physical-Correlation.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults, analytic retention, intelligence conventions) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and **`docs/Intelligence-Design.md` is authoritative for the intelligence layer** (situations, findings, precursors, base rates, analogs, the forecast ledger, hypotheses, forecasts, impact projection and analysis cycles).
- **Previous Completion Report:** read `completion-reports/135-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Intelligence-Design.md` §2 (principles 3, 5, 6, 8, 14, 16), §4.6 (observability), §5.5 (precursors), §6 (situations, hypotheses), §8 (ledger), §12.7 (social signals)
- `docs/Client-Contract.md` §16 (social signals display rules)
- Completion Reports of 017 (alerts), 018 (notification event types), 026 (LLM budgets), 099 (derived layers), 100 (anomalies), 116 (canonical events, evidence groups, observability, manifests), 118 (situations, linker), 119 (findings, event studies), 120 (precursor tests, FDR, validation), 121 (base rates), 122 (ledger, resolvers, forecasters), 123 (benchmark scenarios), 124 (hypotheses, evidence items, indicators), 127 (cycles, assessments), 130 (chronicles), 132 (news), 133 (circular reporting), 134 (social sources, baselines, social observability), 135 (narratives, claims, fingerprints, propagation)

## Objective

Compare what people say with what Foresignal observes. For every significant narrative, test its claims against independent physical evidence in the same place and time, taking sensor coverage into account, and report **both directions**: claims that independent sources corroborate (with the **lead time** the social signal gave), claims that adequate coverage did not see, and significant physical events that drew **less public attention than expected**. Let physical findings **cue** targeted social searches, and score over time which kinds of social signals actually lead the physical world, out of sample.

## Principles (binding)

- **Two worlds, tracked separately.** Physical observations and social narratives are separate evidence streams. A narrative never corroborates itself, never counts as an independent evidence group for the physical event it describes until a non-social originator confirms it, and never moves a hypothesis by volume alone.
- **Absence needs detectability.** "No corresponding change observed" is reported only when `canSeeAbsence` is `ok` for the claim's signal family, place and window; otherwise the status is `insufficient-coverage`. The same rule applies in reverse: an attention gap needs adequate social coverage of the region (134 social observability).
- **Lead times are measured, not assumed.** A social signal is called a leading indicator only from out-of-sample precursor tests (120) and ledger scores (122). Single cases are reported as observed lead times, never as predictive value.
- **No accusations.** Social activity before a physical or market event is reported as timing ("discussion accelerated 47 minutes before the first AIS anomaly"), never as foreknowledge.

## Tasks

### 1. Social–physical comparison (`SocialPhysicalComparator` 1.0.0, `ai.foresignal.social.correlation`)

- For each narrative with ≥ 1 claim whose place precision is `locality` or finer (or `admin1` for large-area families) and either ≥ `social.compare.minGroups` (3) evidence groups or a velocity finding, create a **comparison** per claim: `id`, `claimId`, `narrativeId`, `signalFamily`, `place`, `window` (claim time −6 h to +24 h, family-specific in `social-compare.json`), `status`, `physicalEvidence` (canonical events and findings with non-social originators matching family, place within the family tolerance, and window: `[{ref, family, originator, firstObservedAt, distanceKm}]`), `detectability` (`{p, lo, hi, methods, ok}` from `canSeeAbsence`), `leadTimes` (`socialFirst`, `physicalFirst`, `newsFirst` (132), `officialFirst` (agency records), and the signed differences), `manifestId`, `evaluatedAt`, `revisions`.
- **Status rules:** `corroborated` (≥ 1 non-social evidence group matches); `unobserved` (no match and `canSeeAbsence.ok` over the full window); `contradicted` (a matching agency or sensor record explicitly negates it, e.g. an outage source reports normal service for the place and time, or the claim's media fingerprint is a reuse finding); `mixed` (claims in one narrative split across statuses); `insufficient-coverage` (no match and `canSeeAbsence` fails); `pending` while the window is open. Comparisons re-evaluate as late evidence arrives until the window closes plus the family's publication lag; every change is a revision.
- Narrative `status` (135) is set from its comparisons; claim credibility grades (135 §2) follow.

### 2. Attention gaps (`AttentionGapDetector` 1.0.0)

- For canonical events and situations with severity ≥ `social.attentionGap.minSeverity` (situation escalation, `significant` findings, critical alerts), compute the **expected attention** in the affected region: a regression on past events of the same family and severity band in the trailing 365 days (post volume in the 6 h after the event, per platform, normalised by the region's baseline), falling back to the region-family median.
- When observed attention is below the 10th percentile of the expected distribution **and** 134's social observability is `ok` for the region and window, record an `attention-gap` finding (119 table) with expected and observed volumes and the platforms covered. When coverage is not adequate, no finding (logged as `LOW_SOCIAL_COVERAGE`).

### 3. Cue-driven social search (`SocialCueService`)

- **Automatic cues** from physical triggers (settings `social.cues`): situation activated or escalated (118), `significant` findings of selected types (119: change points in outage, vessel, flight and fire series; 100 anomalies), critical alerts (017), and canonical events of selected families with ≥ 2 evidence groups. Each cue builds queries from the place (036/040 names and multilingual aliases, nearby port, airport or facility names from 098/104), the event type (lexicon terms for the family in the region's languages) and the time window, and runs them on enabled search-capable platforms (`bluesky-search`, `reddit`, `x`, `threads`, `youtube`, `telegram` account mode, `mastodon` hashtags), within `social.cues.maxPerDay` (50), `maxCostUsdPerCue` (1.00) and the platforms' own budgets.
- **Manual cues:** `POST /v1/social/cues` (agent) with place, bbox, entity or situation, extra terms, platforms, window and a cost cap → 202 with the cue.
- Cue record: `id`, `trigger` (`{type, ref}`), `queries` (per platform, exact strings), `platforms`, `window`, `status` (`queued | running | done | failed | budget-refused`), `results` (posts found per platform), `narrativeIds`, `costUsd`, `manifestId`. Cue results are stored as posts with `collection = cue` and `cueId`, joined into narratives (135) and compared (§1).
- Cue-found posts are a **targeted sample**: they never feed velocity baselines (134), and comparisons record that the narrative was found by looking (`foundBy: cue`), which the lead-time statistics keep separate from organically detected narratives.
- Optional `xai-x-search` cue backend (026 provider `xai` with the X search tool, budgeted per result): returns posts with citations as a ranked sample; off by default.

### 4. Lead–lag statistics and scoring

- **Precursor candidates (120):** register social series (velocity by topic, place and family) and claim-type streams as candidate precursors for physical canonical-event families per region; 120's tests, Benjamini–Hochberg control and later-period validation apply unchanged. Survivors appear in the precursor library with `family = social`.
- **Ledger questions (122):** for each comparison opened with status `pending`, a binary question "Independent confirmation of {claim type} at {place} within {window}?" resolved by the comparison's final status (`corroborated` → yes; `unobserved` or `contradicted` → no; `insufficient-coverage` → annulled). Forecaster `social-claim` 1.0.0 forecasts from the base rate of corroboration for the same claim type, platform mix, region and evidence-group band (121 base-rate library, Beta prior), so claim reliability is scored like any other forecaster.
- **Lead-time statistics:** `GET /v1/social/lead-times` aggregates corroborated comparisons by claim type, platform, region and `foundBy`, reporting median and interquartile lead of social first observation over physical, news and official first observation, with counts. Only comparisons evaluated after the statistic's training cut-off are used for any "leads by" statement shown in assessments.
- **Benchmark (123):** scenarios with a planted narrative and no physical event (adequate coverage → `unobserved`; thin coverage → `insufficient-coverage`), a planted physical event with organic discussion and with silence, a reused image, and a cue loop (the cue's own results must not corroborate the trigger).

### 5. Integration

- **Situations (118):** a `social` section (top narratives with status, velocity, origin concentration, lead times, attention gap); the linker links narratives to situations by place, family and time, and situations count narratives as social evidence groups separate from physical corroboration.
- **Hypotheses (124):** comparisons update indicators (`indicator.observed` only from physical evidence; social narratives can mark an indicator `reported` but not `observed`); narratives with status `unobserved` under adequate coverage are evidence against hypotheses that require the claimed event, weighted by detectability.
- **Analysis cycles (127):** packets and assessments gain "What people are saying versus what we observe": narratives by status, attention gaps, notable lead times (out-of-sample only), media reuse and coordination findings, with ICD 203 wording rules.
- **Chronicles (130):** layer types `social-narratives` and `social-volume`; chronicles keep narrative, claim and comparison history and claim summaries even after posts are deleted or expire, and keep post text only when the operator's capture mode says so.
- **Notifications (018):** event types `social.divergence` (comparison became `unobserved` or `contradicted` for a narrative above the velocity threshold) and `social.attention_gap`.

### 6. API, MCP, SSE, spec, docs

- `GET /v1/social/comparisons?status&situationId&narrativeId&family&region&since&cursor` (read), `GET /v1/social/comparisons/{comparisonId}` (read; with revisions), `GET /v1/social/cues?status&since&cursor` (read), `GET /v1/social/cues/{cueId}` (read), `POST /v1/social/cues` (agent), `GET /v1/social/lead-times?claimType&platform&region&foundBy&since` (read), `GET/PUT /v1/settings/social` (admin; `SocialSettings`: cue triggers and caps, comparison windows, attention-gap thresholds, track-record grading, `keepMedia`, languages, `minRegionBaseline`).
- MCP: `compare_claim` (read; comparison for a claim or narrative), `run_social_cue` (agent), `get_social_lead_times` (read).
- SSE topic `social`: `comparison.updated`, `cue.completed`, `attention.gap`.
- `docs/OpenAPI.json`: as specified there (`SocialComparison`, `SocialCue`, `SocialCueInput`, `LeadTimeStats`, `SocialSettings`; situation `social` section; chronicle layer types; notification event types); status implemented; pending list updated.
- `docs/Operator-Guide.md` "Social versus physical": comparison statuses and what they do and do not mean, coverage requirements, cues and their costs, reading lead times.
- Invariants SOC-1 to SOC-5 (`docs/Invariants.md`) asserted by the tests below. AccessPolicy rows. Problem codes: `UNKNOWN_COMPARISON`, `UNKNOWN_CUE` (404), `CUE_LIMIT_REACHED` (429), `BUDGET_EXHAUSTED` (429, from 134).

## Tests to write (do not run)

- `SocialPhysicalComparatorTest`: corroborated with lead times; unobserved only when `canSeeAbsence` is ok; insufficient-coverage otherwise; contradicted by an explicit normal-service record and by a reuse finding; late evidence revises; social-only evidence never corroborates.
- `AttentionGapDetectorTest`: expected-attention model, gap below p10 with adequate coverage, no finding under `LOW_SOCIAL_COVERAGE`.
- `SocialCueServiceTest`: query building from places, aliases, facilities and family lexicons in the region's languages; per-cue and daily caps; X spend refusal; cue results never feed baselines; a cue loop does not self-corroborate.
- `SocialLeadLagIT`: social velocity series as 120 precursor candidates with BH and later-period validation; `social-claim` forecaster resolves and annuls correctly; lead-time statistics use only out-of-sample comparisons.
- `SocialIntegrationIT`: situation social section; hypothesis indicator `reported` versus `observed`; chronicle keeps claim history after post deletion; assessment section wording passes the ICD 203 linter.
- `SocialBenchmarkScenariosTest`: the 123 scenarios above produce the expected statuses and findings.
- `SocialCorrelationControllersIT`: endpoints, scopes, MCP shapes.

## Acceptance criteria

- Significant narratives are compared with independent physical evidence, with corroborated, unobserved, contradicted and insufficient-coverage outcomes that respect detectability, and measured lead times.
- Significant physical events with unexpectedly little public attention are reported when social coverage is adequate.
- Physical findings cue budgeted social searches, and the predictive value of social signals is measured out of sample through precursor tests and the forecast ledger.
- Situations, hypotheses, assessments and chronicles show what people say alongside what Foresignal observes, never confusing the two.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 137, 140, 141, 142).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 142).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and `docs/Intelligence-Design.md`, and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `136: Social Physical Correlation`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/136-CR-Social-Physical-Correlation.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `136: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
