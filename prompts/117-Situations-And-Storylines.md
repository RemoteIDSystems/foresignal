# 117 — Situations And Storylines

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/117-CR-Situations-And-Storylines.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults, analytic retention, intelligence conventions) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and **`docs/Intelligence-Design.md` is authoritative for the intelligence layer** (situations, findings, precursors, base rates, analogs, the forecast ledger, hypotheses, forecasts, impact projection and analysis cycles).
- **Previous Completion Report:** read `completion-reports/116-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Intelligence-Design.md` §0, §2 (principles), §3 (stack and DERIVED sources), §6.1 (situations), §11 (interfaces)
- `docs/Core-Data-Model.md` §10 (ChangeBus), §13 (consumer clamp)
- Completion Reports of 015 (multiplexed `/v1/events/stream`), 017 (alerts, AlertBus, rule tags), 018 (event types), 025 (MCP registry), 027 (embeddings), 029 (renderer), 036 (places), 039 (`private`), 040 (region tags), 098 (entities), 099 (`derived-corroboration`), 100 (anomalies), 101 (intelligence events, cases, `elaro-intel`), 102 (analyst profiles), 116 (attention topics)

## Objective

Give Elaro a memory for unfolding stories. A **situation** groups related intelligence events, anomalies, alerts, findings and records into one storyline that persists for days to months ("Red Sea shipping disruption"), with a lifecycle, a daily deterministic snapshot so "what changed since yesterday or last week" is a diff, and an optional LLM brief that refreshes itself when the evidence changes. A deterministic linker builds situations automatically; operators and agents can shape them.

## Principles (binding)

- **Deterministic linking.** Membership comes from the linker's scored rules or from an operator or agent action, never from LLM output. Every link stores its `join_reason`.
- **Evidence, inference, speculation** as in 101: members are evidence; status, severity and summary are inference by a named, versioned component; the LLM brief is labelled speculation-capable narrative and never changes members, status or severity.
- **Coverage-aware and corroboration-aware.** A situation built only on uncorroborated text reports says so.

## Tasks

### 1. Data model (`ai.elaro.situation`)

- **`situation`:**
  - identity: `id` (ULID), `title`, `theme` (maritime | aviation | conflict | infrastructure | weather | cyber | markets | health | space | mixed), `status` (emerging | active | dormant | closed), `severity` (info | warning | critical), `severity_override` (nullable, logged);
  - place and time: `region_codes` text[] (040), `geom` (Geometry 4326: convex hull of member geometries buffered by 25 km, simplified to ≤ 500 vertices; null when no member has geometry), `centroid`, `started_at`, `last_activity_at`, `closed_at`;
  - links: `topic_ids` text[] (116 attention topics), `entity_ids` text[] (098), `case_ids` text[] (101);
  - text: `summary` (deterministic, §4), `brief` (nullable LLM markdown), `brief_generated_at`, `brief_model`;
  - provenance: `origin` (auto | operator | agent | analyst), `linker_version`, `merged_into`, `private` bool, `version` int (optimistic locking), `created_at`, `updated_at`.
  - Indexes: GiST(`geom`), `(status, last_activity_at DESC)`, GIN(`region_codes`).
- **`situation_member`:** `situation_id`, `member_type` (intel_event | finding | alert | anomaly | record | case), `ref` (id or `source/externalId`), `seq` and `version` (records), `kind`, `signal_family`, `observed_at`, `role` (core | context), `join_reason` jsonb (`{score, components: {region, distanceKm, entity, topic, textSimilarity, theme, recencyHours}, linkerVersion}`), `corroboration` (corroborated | uncorroborated | unknown | n/a), `added_at`, `added_by`. Unique `(situation_id, member_type, ref, seq)`.
- **`situation_snapshot`:** `situation_id`, `day` (UTC date), `status`, `severity`, `member_counts` jsonb (by member type and signal family), `new_members` int, `area_km2`, `region_codes`, `key_values` jsonb (latest values of linked analysis series when 118 exists; empty before), `asof_seq`. PK `(situation_id, day)`.
- **`situation_log`:** `situation_id`, `at`, `actor` (token id, `linker:<version>`, `analyst:<profile>`), `action` (created, member_added, member_removed, status, severity, merged, split, renamed, brief), `before`/`after` jsonb.
- **Records:** publish every situation as a record of the DERIVED source `elaro-intel` (kind `situation`, externalId = situation id). Payload: title, theme, status, severity, memberCount, signalFamilies, regionCodes, corroborationShare, `automated: true`; emitted only on change; tombstoned on merge or after closure plus the `intel` retention class. Ordinary 017 rules can then alert on situations.
- **Private:** a situation with any private member is private and follows `EgressPolicy` everywhere.

### 2. Linker (`SituationLinker`, version `1.0.0`)

- A ChangeBus subscriber with the durable cursor `situation.cursor`, added to 019's consumer clamp. Catches up in 10k-seq chunks on its own virtual thread.
- **Inputs:** `intelligence-event` records (101) whose status is `emerging` or `active`; `analytic-finding` records (118 onward) of severity ≥ notable; alerts whose rule carries the tag `situation` or whose severity is critical; `rule-windows` anomaly records (100) classified `activity-up|activity-down`.
- **Candidate situations:** open situations (`emerging|active|dormant`) whose `last_activity_at` is within `dormancyWindow` (default 7 d; dormant ones within `reopenWindow`, default 30 d) and that pass a cheap prefilter (shared region code, or `geom` within `maxDistanceKm`, or shared entity, or shared topic).
- **Score** in [0, 1] (documented constants, all in one `LinkerWeights` record):
  - region: 0.30 for a shared admin1, 0.20 for a shared country;
  - distance: `0.25 × max(0, 1 − d / maxDistanceKm)` with `maxDistanceKm` 300 (500 for `maritime`);
  - entity: 0.25 for a shared 098 entity;
  - topic: 0.15 for a match of the input's text fields against a linked topic's keyword query (116);
  - text similarity: `0.20 × max(0, (cos − 0.70) / 0.30)` between the input's 027 embedding and the situation's summary embedding, when embeddings exist;
  - theme: −0.20 when the input's theme (from its type or kind via the theme table in §3) is incompatible with the situation's theme (`mixed` is compatible with all);
  - recency: multiply by `0.5 + 0.5 × exp(−hours since last_activity_at / 72)`.
- **Decision:** join the best situation when its score ≥ `linkThreshold` (0.45). Otherwise, an `active` intel event of severity ≥ warning, a significant finding, or a critical alert **seeds** a new `emerging` situation titled deterministically ("{type title} — {region name}"). Lower-severity inputs that match nothing are not linked.
- **Shared member:** if an input is already a member of situation A (for example through a 101 event), it is not re-linked elsewhere unless the operator moves it.
- Every decision is reproducible from the input, the candidate situations at that seq and the linker version. `POST /v1/situations/linker/test {fromSeq, toSeq}` (agent) dry-runs the linker into memory and returns would-be links.

### 3. Themes and tagging

- `ThemeTable` maps intel event types (101), finding types (118), anomaly rules (by tag), alert rule tags and record kinds to themes. Built-in rows ship in `src/main/resources/intel/themes.json`; the operator can add rows through settings (`intel.themes`).
- **Text records without a type** (feed items from 073, ReliefWeb, travel advisories): keyword rules first (in the same JSON). When no rule matches and `intel.themes.llmClassify=true` and a chat provider exists, an optional LLM classification (effort `low`, budgeted, temperature 0) assigns one theme from the fixed list, cached by `text_hash`. The result is labelled `themeSource: llm`. Off by default.

### 4. Lifecycle, summary and snapshots

- **Status:** `emerging` → `active` when members come from ≥ 2 signal families or ≥ 2 independent sources, or an operator sets it; `active` → `dormant` after `dormancyWindow` without new core members; `dormant` → `active` on a new core member within `reopenWindow`; `dormant` → `closed` after 90 d, or by operator. `closed` is final (a new situation starts instead; the analogs in 120 can relate them).
- **Severity:** the maximum of the core members' severities, mapped to info | warning | critical, unless `severity_override` is set.
- **Summary** (deterministic template, versioned): "{n} signals from {k} families across {regions} since {started}: {top 3 member titles}. {corroboration sentence}. Last activity {ago}."
- **Corroboration roll-up:** for each record or intel-event member, read 099's `derived-corroboration` cluster membership where it exists; `corroborationShare` = corroborated ÷ (corroborated + uncorroborated). Members of kinds 099 does not cluster are `n/a`.
- **Snapshots:** a daily job at 00:10 UTC writes one `situation_snapshot` per non-closed situation for the previous UTC day. `GET /v1/situations/{id}/changes?since=` returns the diff between the snapshot at `since` and now (status, severity, new members by family, area change, regions added).

### 5. Self-refreshing brief (optional LLM)

- When a chat provider is configured and `situation.briefs.enabled=true` (default false), a situation's brief is regenerated when it becomes active, when its severity changes, or when ≥ 3 new core members arrive, debounced to at most once per `briefMinInterval` (default 30 min) and capped per day (`situation.briefs.maxPerDay`, default 50 across all situations).
- It uses the 028 tool loop with the read catalogue, the situation's members as the input packet, effort `low`, and the 102 output validator (typed claims, verified citations). Stored in `brief` with model and time; the previous brief goes to `situation_log`.
- Budget refusal or failure leaves the previous brief in place and logs a warning. Never blocks linking.

### 6. Operator and agent actions

- Create (`origin=operator|agent`), rename, set theme, override severity, close, reopen.
- Add or remove members (pinned refs verified by seq); move a member between situations.
- **Merge** two situations (keep the older id, move members, set `merged_into`, tombstone the merged record, log). **Split**: move a chosen member set into a new situation.
- All changes are logged with the actor.

### 7. API, SSE, MCP, notifications

- `GET /v1/situations?status&theme&region&bbox&since&q&cursor` (read; keyset on `last_activity_at, id`)
- `GET /v1/situations/{id}` (read; evidence first: members grouped by family; inference: status, severity, summary; speculation: brief, labelled)
- `GET /v1/situations/{id}/members?type&cursor`, `GET /v1/situations/{id}/timeline?from&to` (members and log entries in time order), `GET /v1/situations/{id}/changes?since` (read)
- `POST /v1/situations`, `PATCH /v1/situations/{id}`, `POST /v1/situations/{id}/members`, `DELETE /v1/situations/{id}/members/{memberId}`, `POST /v1/situations/{id}/merge {intoId}`, `POST /v1/situations/{id}/split {memberIds, title}` (agent)
- `POST /v1/situations/{id}/brief` (agent, `alwaysAuthenticated`; regenerate now, 202)
- `POST /v1/situations/linker/test` (agent)
- **SSE:** topic `situations` on `/v1/events/stream`: `situation.created|updated|activated|escalated|dormant|closed|merged` (summary plus `version`). Respect `private`.
- **MCP:** `list_situations`, `get_situation`, `get_situation_changes` (read); `create_situation`, `link_to_situation`, `merge_situations` (agent). Add the read tools to the read catalogue so Ask and the analyst can use them.
- **Notifications (018 event types):** `situation.activated`, `situation.escalated` (severity raised). Defaults: off; documented recipe to enable for a channel.
- Problem codes: `UNKNOWN_SITUATION` (404), `SITUATION_MEMBER_NOT_FOUND` (400), `SITUATION_VERSION_CONFLICT` (409), `SITUATION_CLOSED` (409 on member add to a closed situation).

### 8. Spec, Postman, docs

- `docs/OpenAPI.json`: schemas `Situation`, `SituationSummary`, `SituationMember`, `SituationChanges`, `SituationTimelineEntry`, `SituationInput`, `SituationPatch`; tag `Situations`; `x-elaro-scope` on every operation; SSE event schemas. Regenerate Postman with `python3 scripts/generate-postman.py docs/OpenAPI.json postman`, with BODIES for create, add member, merge and split.
- AccessPolicy rows as above.
- `docs/Operator-Guide.md` "Situations": what links and why (the score table), tuning `linkThreshold` and windows, themes, briefs and their cost.
- `docs/Agent-Guide.md`: present situations evidence first; quote the corroboration share.

## Tests to write (do not run)

- `SituationLinkerScoreTest`: every score component and constant; theme incompatibility; recency decay; threshold boundary (exactly 0.45).
- `SituationLinkerIT` (`TestRecords`): a critical intel event seeds an emerging situation; a nearby event of a second family joins and activates it; an unrelated region does not join; a shared entity joins across 800 km; cursor restart gives exactly-once membership; dry-run equals the live result over the same seq range.
- `SituationLifecycleTest`: emerging → active → dormant → active (reopen) → closed; closed is final.
- `SituationSnapshotIT`: daily snapshot written once; `/changes` diff over two days.
- `SituationMergeSplitIT`: members move, tombstone emitted, log written, optimistic-lock 409.
- `SituationBriefIT` (fake `LlmClient`): debounce; daily cap; failure keeps the previous brief; typed claims and citations validated.
- `ThemeTableTest`: keyword rules; LLM classification off by default and cached by hash when on.
- `SituationControllersIT`: CRUD, scopes, `private` filtering, SSE topic, MCP tool shapes.

## Acceptance criteria

- Situations form, activate, go dormant and reopen from seeded intel events and findings, with a stored, reproducible `join_reason` on every link.
- "What changed since" is answered from snapshots, without an LLM.
- The LLM brief is optional, budgeted, debounced and never changes members, status or severity.
- Situations appear as `elaro-intel` records, on SSE and in MCP.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 126, 129, 130, 131).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 131).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and `docs/Intelligence-Design.md`, and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `117: Situations And Storylines`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/117-CR-Situations-And-Storylines.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `117: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
