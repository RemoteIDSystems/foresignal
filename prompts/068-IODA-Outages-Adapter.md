# 068 — IODA Outages Adapter

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/068-CR-IODA-Outages-Adapter.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–126).
- **Previous Completion Report:** read `completion-reports/067-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Core-Data-Model.md` §2, §3 (rules 7, 8 and 13), §4 (volatile fields), §5.3 ("Everything else")
- `docs/Source-Catalog.md` row `ioda-outages` ("geometry from a bundled country-centroid table, no Nominatim calls")
- Completion Report of 040 (gazetteer and shared centroid table: `GazetteerService`, country and admin1 lookups)
- ShadowBroker `services/fetchers/infrastructure.py` (AGPL, **reference only**)
- Upstream: IODA API v2 `https://api.ioda.inetintel.cc.gatech.edu/v2/` (embedded Swagger), `https://ioda.inetintel.cc.gatech.edu/`

## Objective

Ingest IODA internet-outage alerts as one `outage` state record per entity and datasource. The record's level (`critical` / `normal`) changes when IODA raises or clears an alert, so rules get clean enter/exit transitions. Locate country and region entities with the gazetteer's centroids, never with a geocoding service.

## Tasks

### 1. Descriptor (`ai.foresignal.source.adapters.ioda.IodaOutagesAdapter`)

| Field | Value |
|---|---|
| id / kinds | `ioda-outages` / `{outage}` |
| params | `entityTypes` (string-list, allowed `country, region, asn`, default `country, region`); `datasources` (string-list, allowed `bgp, ping-slash24, gtr, merit-nt`, default `bgp, ping-slash24`); `window` (duration, 1–24 h, default 12 h); `countries` (string-list of ISO 3166-1 alpha-2, optional) |
| interval / budget | 5 min (min 5 min); 4/min, 1200/day (≤ 3 pages per run) |
| tier / policy | STANDARD, ALL, `recordTtl` 6 h (anchor RECEIVED). Each poll re-observes the latest alert per key inside `window`, so a key that falls out of the window expires 6 h later. |
| time | UPSTREAM; `observedAt` = the alert `time` (epoch s) of the newest alert for the key; `eventTime` null (this is a state) |
| volatile | `value, historyValue, ratio` (raw measurements change on every alert; the level is the state) |
| title / search / tiles | `titleField summary`; search `entityName, entityCode`; tiles `level, datasource, entityType, ratio` |
| licence / egress | "IODA, Georgia Tech Research Corporation — all rights reserved (no published licence)", `termsUrl` `https://ioda.inetintel.cc.gatech.edu/`, **`egress` LOCAL_ONLY** (verified 2026-10: every response carries the GTRC copyright and no terms are published; do not re-serve without permission) |
| `zeroResultsNormal` | true (quiet windows happen at country level) |

### 2. Fetch

- GET `https://api.ioda.inetintel.cc.gatech.edu/v2/outages/alerts?from={now−window}&until={now}&limit=500&page={n}`, with times in epoch seconds.
  - Add `entityType={t}` when `entityTypes` has exactly one value; otherwise filter client-side.
  - Add `entityCode` only when `countries` has exactly one entry.
  - Follow pages while a page returns 500 rows, up to 3 pages. If the cap is hit, set `Coverage(expected=null, actual=rows)` and log it once per hour.
- `complete = false` (window-based). No tombstones: TTL expiry handles staleness.
- Reject a 200 that is not a JSON object with a `data` array: throw `UpstreamFormatException`, since IODA wraps results with `copyright`, `type`, `metadata` and `data`. Verify the exact envelope from the captured fixture and record it in the parser Javadoc.

### 3. Parser and state folding (`IodaAlertsParser`, pure)

- **Alert fields (verified 2026-10):**
  - `datasource`, `entity{code, name, type, attrs}`, `time`
  - `level` (`critical | normal`; `warning` is possible but was not observed: accept it)
  - `condition` (e.g. `"< 0.99"`), `value`, `historyValue`, `method`
- **Fold:** group by `{entity.type}:{entity.code}:{datasource}`, keep the newest alert (ties: the later in the array), and drop datasources and entity types not in params. One record per key (rule 7: the record is the latest complete state).
- **Record:**
  - `externalId = {entityType}:{entityCode}:{datasource}`
  - payload: `entityType, entityCode, entityName, datasource, level, condition, value, historyValue, ratio` (value / historyValue to 3 dp when historyValue > 0), `method`
  - `summary` (`"{entityName}: {datasource} {level}"`); `country` (ISO2 for country entities; for regions from `attrs` when present)
- **Geometry (no network):**
  - `country`: `GazetteerService.countryCentroid(code)` (040). IODA uses ISO2 codes; verify on the fixture.
  - `region`: an admin1 centroid from 040, matched by IODA region code if 040 carries a crosswalk, else by (country, normalised name). On no match, null geometry and count `foresignal.ioda.region_unmatched`.
  - `asn`: null geometry.
  - Every centroid sets `geometryApproximate: true`.
- **040 hook:** if `GazetteerService` lacks `countryCentroid` or admin1 name lookup, add it to the 040 service, backed by the bundled Natural Earth data, without network access.

### 4. Fixtures

- Capture with a README (date, URL, window):
  - `curl -s "https://api.ioda.inetintel.cc.gatech.edu/v2/outages/alerts?from=$(($(date +%s)-43200))&until=$(date +%s)&limit=500"`
  - trim to 40 alerts, covering several datasources, both levels, and country, region and asn entities
- Hand-made:
  - `critical-then-normal.json` (same key, two alerts)
  - `unknown-region.json`
  - `not-envelope.html`
  - `page-full-500.json` (generated in the test)

### 5. Catalogue and docs

- `docs/Source-Catalog.md` `ioda-outages` row: the state-record model, volatile fields, TTL 6 h, centroid geometry via 040, egress LOCAL_ONLY with the reason, verified datasource list.
- Agent Guide: "IODA `critical` on one datasource is a signal, not a confirmed outage. Corroborate across datasources (and Cloudflare Radar, 092)."
- OpenAPI: no operation changes.

## Tests to write (do not run)

- `IodaAlertsParserTest`:
  - envelope parsing; fold keeps the newest per key
  - datasource and entity-type filters
  - `ratio` computation and divide-by-zero
  - an unknown `level` value preserved
  - malformed envelope → `UpstreamFormatException`
- `IodaGeometryTest`: country centroid; region matched by name; unmatched region → null and counted; asn → null; `geometryApproximate` set.
- `IodaAdapterTest` (WireMock):
  - `from`/`until` computed from `window` (`MutableClock`)
  - pagination stops at a short page and at 3 pages
  - single-entityType query param
  - 429 → backoff (005)
- `IodaIngestIT` (`@IntegrationTest`):
  - critical → FIRST
  - a re-poll with changed `value` only → no version (volatile)
  - a normal alert → SIGNIFICANT
  - a key outside the window expires after 6 h
  - a rule `level == critical` with enter/exit fires on both transitions (when 017 is present)

## Acceptance criteria

- `ioda-outages` registers with `egress=LOCAL_ONLY` and valid defaults, and makes no geocoding calls.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 126, 129, 130, 131).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 131).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `068: IODA Outages Adapter`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/068-CR-IODA-Outages-Adapter.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `068: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
