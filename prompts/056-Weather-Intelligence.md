# 056 — Weather Intelligence

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/056-CR-Weather-Intelligence.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–133).
- **Previous Completion Report:** read `completion-reports/055-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Weather-Imagery-Design.md` §1.1 goal 4, §6.9 (MCP conventions), §7.1 (unknown is not clear; `automated`/`notAWarning`), §7.5 (enrichment, briefings, Ask), §2.3 (`alert.context jsonb`). The design's "046" intelligence prompt = design 043 = v1.2 056; its 024/025/026/028 = v1.2 028/029/030/034.
- `docs/Architecture.md` §19 (LLM safety: upstream text is untrusted), §21 (Ask), §22 (briefings), §23 (enrichment).
- Completion Reports of **017** (alert model, `alert.context` if added), **018** (templates, filters, timezone), **028** (Ask tool loop, `ToolCatalog`, citation verification), **029** (briefing scope, digest renderer, `GET /v1/digest`), **030** (enrichment context assembly and wait), **034** (Agent Guide cookbooks), **052** (SampleService, place-weather, MCP weather tools), **053** (NWS forecasts), **055** (`get_map_image`, `AlertMapService`).

## Objective

Make every intelligence surface weather-aware with the same deterministic, status-carrying facts: each geolocated alert gets a computed `context.weather` block (usable in templates and fed to enrichment), briefings gain a "Weather at places" section, and Ask can sample, forecast, compute area stats and fetch map images, citing run, valid time and age. No path may present no-coverage or stale data as clear weather.

## Tasks

### 1. `WeatherContextService` (`ai.foresignal.raster.sample` or `ai.foresignal.weather`)

- `WeatherContext compute(Geometry g, Instant at)`: deterministic, from the frame caches and record heads only (never an upstream fetch), within a 500-ms budget; a section that misses the budget gets `status: unavailable`.
- Sections, each `{status: ok|stale|no-coverage|unavailable, asOf?, …}` with the same status rules as 052 (and `statusText` for non-ok):
  - `radar`: max dBZ within 10 km of the geometry; nearest `radar-cell` with `motion{speedKmh, bearingDeg}` and `meshMax`
  - `lightning`: GLM flash count within 20 km over the last 15 min (GLM limits note in docs)
  - `precip`: rate (mm/h) at the centroid; QPE 1 h when 053's products exist
  - `warnings`: intersecting active `nws-alert` records (event, severity, expires, id), max 5; text fields are upstream text and are marked untrusted
  - `wind`: GFS/HRRR 10-m wind and gust at the nearest valid time (`run`, `validTime`, `step`)
  - `smoke`: HRRR near-surface smoke (µg m⁻³) at the nearest valid time
  - `summary`: one deterministic sentence built from the sections (e.g. "Radar max 52 dBZ within 10 km; cell moving ENE at 45 km/h; 14 GLM flashes within 20 km (15 min); NWS Severe Thunderstorm Warning in effect. Automated — not an official warning."). Non-ok sections render as "lightning data stale since 14:30 UTC", never as absence.
- Missing products (source disabled) → section `unavailable`, not omitted.

### 2. Alert context (extend 017/030)

- Check `alert.context jsonb` exists (design §2.3); add it if missing (Hibernate column; note for 138's Flyway baseline).
- On alert creation, for alerts whose record has geometry and when any weather product is enabled: compute `context.weather` at the alert's `observed_at` and store it before dispatch. Global setting `weather.alertContext` (`auto` default | `off`) in `app_setting`; rule-level override `weatherContext: auto|off` on `trigger_rule` (add column; OpenAPI `TriggerRuleInput`).
- Computation must not delay dispatch beyond its budget; failures log, store `{status: unavailable}` and never block delivery.
- `GET /v1/alerts/{id}` and lists include `context` (lists: only `context.weather.summary` unless `include=context`).
- **Templates (018):** expose `{{context.weather.summary}}` and the section fields (`{{context.weather.radar.maxDbz}}` …); document them in the channel template reference. Default templates for `discord`, `telegram`, `email`, `ntfy`, `pushover`, `slack` append the summary line when present.
- **Enrichment (030):** the context JSON is added to the enrichment input as a labelled data block. The system prompt adds: "Weather sections with status other than ok are unknown, not clear; never state that weather is clear unless the relevant section is ok." NWS warning text stays inside the untrusted data envelope.
- Persisted notifications (018) store the summary with the message.

### 3. Briefings "Weather at places" (extend 029)

- Briefing scope gains `places: [placeId]` (empty = none) and `weatherProducts: [productId]` (default: the products backing place-weather). Validate ids exist (400 otherwise).
- The digest gets a "Weather at places" section per place, in the place's timezone:
  - **Now:** radar, lightning and active NWS alerts (from the `place-weather` head and `nws-alert` records)
  - **The period** (the briefing lookback): max dBZ, max MESH, lightning total and hazards that entered the radius, from `place-weather` versions in the window (`record_version` by seq range; respect retention, and say "partial period" when the window starts below the retention floor)
  - **Next 24–72 h:** NWS gridpoint summary (053) plus GFS and HRRR maxima (gust, precipitation, smoke), each with run and valid time
  - Stale or unavailable sections are labelled ("Lightning: data stale since 14:30"), never omitted or shown as calm.
- LLM-mode briefings receive the same structured section as a data block with the same "unknown is not clear" instruction; LLM failure falls back to the digest as before. `GET /v1/digest` (029) includes the section when `places` is passed (`?places=a,b`). HTML renditions render it as a compact table.

### 4. Ask tools and citation rules (extend 028)

- Register `sample_weather`, `weather_forecast`, `area_weather_stats` (052) and `get_map_image` (055) in the shared `ToolCatalog` so Ask and MCP behave identically; respect the caller's scope and the `private` gate.
- Each weather tool result carries a `citationId` `wx:{productId}@{frameKey}` (forecasts `wx:{productId}@{run}/{step}`); the citation verifier (028) accepts these alongside record, alert and briefing ids.
- System prompt additions: cite run, validTime and age for every weather value; no-coverage, stale and unavailable mean unknown; weather outputs are automated and not official warnings.
- **Deterministic guard:** after the answer, if any weather tool result in the turn had a non-ok status, and the answer does not contain that result's `statusText` or an equivalent status phrase, append a standard caveat listing the affected products and statuses. Add a warning to the response when the guard fires.
- `get_map_image` results are passed to providers that accept images; for providers without image input, only the caption is sent.

### 5. Agent cookbooks (extend 034's Agent Guide)

`docs/Agent-Guide.md` "Weather" section: sampling a place, forecast series, area stats over a polygon, map images with captions, alert `context.weather` in webhook consumers (Home Assistant and Node-RED examples reading `context.weather.summary`), and the citation and "unknown is not clear" conventions. Add the place-weather rule recipes by reference to the Operator Guide.

### 6. Spec, Postman, docs

- `docs/OpenAPI.json`: `Alert.context` (schema `AlertContext` with `WeatherContext` and its sections), `TriggerRuleInput.weatherContext`, `BriefingScope.places`/`weatherProducts`, `/v1/digest` `places` parameter, Ask citation type `weather`; event schemas (020's webhooks object) updated for `alert.context`. No new operations; scopes unchanged. Regenerate Postman (update the trigger and briefing `BODIES` examples).
- `docs/Operator-Guide.md`: alert weather context, the setting and rule override, template variables.

## Tests to write (do not run)

- `WeatherContextServiceTest`: each section from synthetic frames and records; statuses (stale GLM, no-coverage MRMS, disabled HRRR → unavailable); budget overrun → unavailable; summary sentence golden strings including non-ok wording; no outbound HTTP.
- `AlertWeatherContextIT`: a geolocated alert stores `context.weather` before dispatch; rule `weatherContext=off` and global `off` skip it; non-geometry alerts skip it; a failing computation still dispatches.
- `TemplateWeatherVariablesTest`: `{{context.weather.summary}}` and section fields render; missing context renders empty.
- `EnrichmentWeatherInputIT`: the context block reaches `FakeLlmClient` inside the data envelope; injected instructions in an NWS alert description have no effect.
- `BriefingWeatherSectionIT`: now/period/next sections for two places in different timezones; stale labelling; partial-period note; digest golden markdown; LLM mode receives the block; `/v1/digest?places=` includes it.
- `AskWeatherToolsIT` (`FakeLlmClient`): weather citations verified; an answer claiming clear skies while the radar section is no-coverage gets the caveat and a warning; scope and private gate respected.

## Acceptance criteria

- Every geolocated alert (unless disabled) carries a deterministic `context.weather` with per-section statuses, available to templates and enrichment.
- Briefings show "Weather at places" with stale sections labelled.
- Ask cites run, validTime and age for weather values, and never lets a non-ok section pass as clear.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 133, 136, 137, 138).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 138).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `056: Weather Intelligence`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/056-CR-Weather-Intelligence.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `056: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
