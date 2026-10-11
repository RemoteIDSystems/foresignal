# 116 — Evidence Identity And Observability

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/116-CR-Evidence-Identity-And-Observability.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults, analytic retention, intelligence conventions) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and **`docs/Intelligence-Design.md` is authoritative for the intelligence layer** (situations, findings, precursors, base rates, analogs, the forecast ledger, hypotheses, forecasts, impact projection and analysis cycles).
- **Previous Completion Report:** read `completion-reports/115-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Intelligence-Design.md` §2 (principles), §4.5 (evidence identity and independence), §4.6 (observability), §4.7 (analysis manifests)
- `docs/Invariants.md` (the `EVD-*`, `OBS-*` and `MAN-*` invariants this prompt owns)
- `docs/Core-Data-Model.md` §10 (ChangeBus), §13 (consumer clamp), §14 (provenance, coverage)
- Completion Reports of 003 (`ForesignalVersion`), 006 (health, coverage), 038 (`activity_rollup`), 040 (regions), 069 (local receivers and station range statistics), 098 (entities), 099 (`derived-corroboration` clustering and tolerances), 100 (CoverageGate, absence rules, anomalies), 101 (EventScorer, evidence quality), 102 (analyst runs), 103 (job framework, backtests)

## Objective

Lay the evidence foundations the intelligence layer stands on, before anything else in the block builds on it:

1. **Evidence identity and independence.** Distinguish a real-world event from the observations and reports that describe it. Link observations to **canonical events**, record who **originated** each observation, and collapse observations that trace to the same originator into one **evidence group**, so twelve re-reports of one claim count once, not twelve times.
2. **Observability.** Model what Foresignal *could* have observed, not only what it did observe: a time-versioned estimate of each source's probability of detecting a phenomenon in a place, so "nothing was detected" is never mistaken for "nothing happened".
3. **Analysis manifests.** Record, for every analytic output, the exact machinery and inputs that produced it (component version, build, configuration, registry hashes, knowledge cutoff, inputs, model and prompt), so any finding or forecast can be replayed and audited.

Then retrofit 099, 100, 101 and 103 to use them.

## Principles (binding)

- **Independence is counted in evidence groups, not records or sources.** Confidence, corroboration and evidence quality never increase because the same originator was repeated.
- **Absence needs detectability.** An absence or "no event" conclusion requires both healthy sources (006) and an estimated detection probability at or above `absenceMinDetect` (default 0.8) for that phenomenon, place and time. Otherwise the answer is `unknown` with reason `LOW_DETECTABILITY`.
- **Estimates are versioned in time.** Retrospective analysis uses the observability estimate that was valid at the time, never today's.
- **Everything is reproducible** from a manifest; non-reproducible parts (LLM output) are labelled as such.

## Tasks

### 1. Originators (`ai.foresignal.evidence`)

- `Originator` record: `id` (e.g. `usgs-anss`, `emsc`, `gfz`, `firms-viirs`, `goes-fdc`, `acled`, `ucdp`, `gdelt`, `ioda`, `cloudflare-radar`), `class` (sensor-network | agency | aggregator | media | crowd | operator | derived), `independenceGroup` (sources in the same group are never independent of each other), `aggregates` (originator ids it republishes, e.g. GDACS aggregates USGS for earthquakes), `notes`.
- **Registry:** `src/main/resources/intel/originators.json` maps every catalogued source id (and family patterns such as `transit-*`) to its originator, built from `docs/Source-Catalog.md`; descriptors may override with a new optional `originator` field (add it to `SourceDescriptor`, default null). Operators extend it through settings `intel.originators`. Unknown sources default to an originator equal to their source id, class `aggregator`, with a startup warning listing them.
- **Upstream references:** an optional descriptor field `upstreamRefFields` (payload fields that carry the upstream's own event id, or the cited original source, e.g. GDACS `eventid`, ReliefWeb `source`, GDELT `sourceUrlDomain`), registered for the catalogued sources in the same JSON.

### 2. Canonical events and evidence groups

- **Tables:**
  - `canonical_event`: `id` (ULID), `family` (118's signal family plus subtype, e.g. `seismic`, `fire`, `conflict`, `outage`, `disaster`, `weather-alert`), `event_time`, `geom`, `region_codes`, `first_observed_at`, `first_observation_ref`, `observation_count`, `evidence_group_count`, `quality_flags` text[], `status` (open | merged | closed), `merged_into`, `resolver_version`, `created_at`, `updated_at`.
  - `observation_link`: `canonical_event_id`, `source_id`, `external_id`, `seq`, `version`, `originator_id`, `upstream_event_id` (nullable), `evidence_group_id`, `link_confidence` (0–1), `link_reason` jsonb (`{rule, dtSeconds, distanceKm, attributeDeltas, upstreamIdMatch}`), `linked_at`. Unique `(source_id, external_id, canonical_event_id)`.
  - `evidence_group`: `id`, `canonical_event_id`, `originator_root` (the originator all members trace to), `member_count`, `first_seq`.
- **`EvidenceResolver`** (version `1.0.0`), a ChangeBus subscriber with the durable cursor `evidence.cursor` (consumer clamp), over event-class kinds listed in `src/main/resources/intel/canonical-rules.json`:
  - Matching rules per family, reusing 099's corroboration tolerances where they exist (earthquakes Δt ≤ 60 s, ≤ 100 km, |ΔM| ≤ 0.8; fires ≤ 1 km, ≤ 1 h; conflict events same 0.1° cell and UTC day; outages same country or ASN with overlapping interval) and adding: an **upstream-id match** (same upstream event id ⇒ same event, confidence 1.0); disasters by GDACS id or type + ≤ 200 km + ≤ 24 h; weather alerts by CAP identifier or references chain.
  - `link_confidence` from the rule (1.0 for upstream-id matches; otherwise `1 − max(normalised dt, distance, attribute delta)` floored at 0.3); links below `evidence.minLinkConfidence` (0.5) are not made.
  - **Evidence groups:** within a canonical event, members belong to the same group when they share an originator, when their originators share an `independenceGroup`, when one originator `aggregates` the other, or when one member's upstream reference cites the other member's originator. Text-report sources (GDELT, feed kits, ReliefWeb) group by the cited original domain or source when present.
  - **Quality flags:** `ECHO` (≥ 5 observations in one group within 2 h), `TIMESTAMP_CONFLICT` (member event times disagree beyond 3 × the family tolerance), `SINGLE_ORIGINATOR` (one group only), `LATE_REVISION` (a member's event time or location changed after first publication).
  - Merging: when one observation matches two canonical events, merge them (keep the older id, log); never merge across families.
- **Retrofit 099:** `derived-corroboration` records are now produced from canonical events (payload gains `canonicalEventId`, `evidenceGroups`, `independentOriginators`, `qualityFlags`; `credibility` = evidence group count). Keep 099's published kind and ids stable.
- **Retrofit 101:** `EventScorer` 1.1.0 counts signal families **from distinct evidence groups**: two members in one evidence group contribute once (the stronger). Evidence quality "strong" requires ≥ 3 families from ≥ 2 **independent evidence groups**. Record the scorer version change; existing events are re-scored lazily on their next update.
- **`EvidenceService`** (used by 118, 124 and later): `groupOf(ref)`, `canonicalEventOf(ref)`, `independentCount(refs)`, `sameGroup(refA, refB)`.

### 3. Observability (`ai.foresignal.observability`)

- **Declared observability:** a descriptor field `observability` (optional) and the central `src/main/resources/intel/observability.json` for catalogued sources: per kind, `area` (global | bbox | region codes), `thresholds` (rules such as `{field: "mag", min: 2.5, area: "US"}`, `{field: "mag", min: 4.5, area: "global"}`), `typicalLatency`, `notes`.
- **Empirical estimates** (`ObservabilityEstimator` 1.0.0), weekly at 04:30 UTC, stored as time-versioned rows `observability_estimate(source_id, family, region_code, cell (nullable H3 res 4), band (threshold bucket, e.g. `M4.5-5`), period_start, period_end, p_detect, lo, hi, n, method, computed_at, manifest_id)`:
  1. **Capture–recapture** for families with ≥ 2 independent evidence groups (earthquakes, fires, conflict events, outages): for source A against the union of independent other sources B over the trailing 365 days in a region and band, `p_detect(A) = |A ∩ B| / |B|` using canonical events, Wilson interval, minimum |B| = 20 (else no estimate). Method `capture-recapture`.
  2. **Presence proxy** for track kinds (aircraft, vessels): per source and H3 res-4 cell, the share of healthy hours in the trailing 28 days with ≥ 1 observation (from 038 hourly rollups), divided by the 90th-percentile share across cells with traffic, capped at 1. Method `presence-proxy`, always labelled low confidence. Local receivers (069) with range statistics use their measured range polygon instead (method `receiver-range`).
  3. **Declared** fallback: 1.0 inside the declared area and above threshold, 0 outside. Method `declared`, low confidence.
- **Combination:** `ObservabilityService.pDetect(family, place (point | polygon | region), time, threshold?)` returns `{p, lo, hi, methods, sources}` combining enabled, healthy sources whose estimates were valid at `time`, as `1 − Π(1 − p_s)` over **independence groups** (sources in one group combine by max, not product).
- `ObservabilityService.canSeeAbsence(family, place, window, threshold?)` → `{ok, p, reason}`; `ok` requires every involved source healthy over the window (006/100 CoverageGate) **and** `p ≥ absenceMinDetect`.
- **Retrofit 100:** absence rules and downward anomaly classifications call `canSeeAbsence`; when it fails the state is `unknown` with reason `LOW_DETECTABILITY` (in addition to the existing coverage reasons).
- **Coverage layer:** `GET /v1/observability?family&region|bbox&at` (read; per region or cell `p`, interval and methods) and `GET /v1/observability/tiles/{z}/{x}/{y}.mvt?family&at` (read; H3 cells with `p`, for a "how well instrumented is this area" map layer; reuses 016's tile machinery).

### 4. Analysis manifests (`ai.foresignal.manifest`)

- `analysis_manifest`: `id` (ULID), `component`, `component_version`, `build` (`ForesignalVersion` version and git commit), `config` jsonb (parameters and thresholds), `config_hash`, `registries` jsonb (name → sha256 of each registry file used, e.g. `signal-families.json`, `originators.json`), `asof_seq`, `inputs` jsonb (explicit refs when ≤ 10,000, else the query definition plus a hash of the ordered input refs), `model` jsonb (nullable: provider, model, effort, prompt hash, tools hash, samples), `content_hash` (unique; identical manifests are stored once), `created_at`.
- `ManifestService.record(...)` returns the id. From this prompt on, every analytic output stores `manifest_id`. Retrofit here: 100 anomaly observations, 101 intelligence events and recipe runs, 102 analyst runs, 103 backtest jobs (nullable `manifest_id` columns; written for new outputs).
- **Replay:** `POST /v1/manifests/{id}/replay` (agent; a 103 job): re-run a deterministic component at the manifest's `asof_seq` with the recorded configuration and inputs, compare with the stored output, and report `identical | differs` (with a diff summary) or `not-replayable` (reason: inputs purged by retention, component removed, or LLM output). LLM components are reported as "inputs recorded; model output not reproducible".
- `GET /v1/manifests/{id}` (agent).

### 5. API, MCP, SSE, docs

- Evidence: `GET /v1/evidence/events?family&region&since&flags&cursor` (read), `GET /v1/evidence/events/{id}` (read; observations with originators, groups, link reasons, flags), `GET /v1/evidence/of?source&externalId` (read).
- MCP: `get_evidence`, `get_observability` (read; read catalogue).
- SSE topic `evidence`: `canonical.created|merged|flagged` (summary only).
- `docs/OpenAPI.json`: `Originator`, `CanonicalEvent`, `ObservationLink`, `EvidenceGroup`, `ObservabilityEstimate`, `Detectability`, `AnalysisManifest`, `ReplayResult`; tags `Evidence` and `Observability`; `x-foresignal-scope` everywhere; Postman entries.
- AccessPolicy rows as listed. Problem codes: `UNKNOWN_CANONICAL_EVENT`, `UNKNOWN_MANIFEST` (404), `MANIFEST_NOT_REPLAYABLE` (409).
- `docs/Operator-Guide.md` "Evidence and observability": why independence matters (the earthquake-in-four-feeds example), echo flags, reading detection probabilities and the coverage layer, why some absences say `unknown`, manifests and replay.
- `docs/Invariants.md`: confirm the `EVD-*`, `OBS-*`, `MAN-*` entries name the tests you wrote.

## Tests to write (do not run)

- `OriginatorRegistryTest`: every catalogued source maps to an originator; aggregation and independence groups resolve; unknown sources warned.
- `EvidenceResolverTest`: tolerance rules per family; upstream-id match beats proximity; evidence grouping by originator, independence group, aggregation and citation; `ECHO`, `TIMESTAMP_CONFLICT`, `LATE_REVISION` flags; merge on double match; never across families.
- `EvidenceResolverIT` (`TestRecords`): the same earthquake from USGS, EMSC and GDACS (which aggregates USGS) yields one canonical event with **2** evidence groups; 10 GDELT reports citing one outlet yield one group with `ECHO`; cursor restart exactly-once.
- `EventScorerIndependenceTest`: echoed members never raise confidence or evidence quality; v1.1.0 vs 1.0.0 behaviour documented.
- `CaptureRecaptureTest`: planted detection rates recovered within the interval; minimum-n rule; time-versioned estimates selected by `at`.
- `ObservabilityServiceTest`: combination by independence group (product across groups, max within); `canSeeAbsence` false on unhealthy sources or low p.
- `AbsenceRetrofitIT`: a 100 absence rule over an area with `p_detect` 0.3 returns `unknown` / `LOW_DETECTABILITY`, never "absent".
- `ManifestServiceTest`: content-hash dedup; registry hashes recorded; input hashing for large input sets.
- `ManifestReplayIT`: a replayed 100 anomaly evaluation is `identical`; after a deliberate config change it `differs`; purged inputs → `not-replayable`.
- `EvidenceControllersIT`: endpoints, scopes, MCP shapes, coverage tiles.

## Acceptance criteria

- Event-class observations link to canonical events with evidence groups; repeated reports from one originator never count as independent confirmation anywhere (099 corroboration, 101 scoring).
- Detection probabilities exist per source, family, region and band (capture–recapture, presence proxy, receiver range or declared), versioned in time; absence conclusions require detectability.
- Every analytic output from 100 onward can point to a manifest, and deterministic outputs replay to identical results.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 129, 132, 133, 134).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 134).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and `docs/Intelligence-Design.md`, and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `116: Evidence Identity And Observability`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/116-CR-Evidence-Identity-And-Observability.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `116: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
