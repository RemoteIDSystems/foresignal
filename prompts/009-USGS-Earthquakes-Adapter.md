# 009 — USGS Earthquakes Adapter

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/009-CR-USGS-Earthquakes-Adapter.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`.
- **Previous Completion Report:** read `completion-reports/008-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §4.2 (adapter rules)
- `docs/Source-Catalog.md` row `usgs-earthquakes`
- GEV reference (MIT, portable): `~/Documents/GitLab/gods-eye-view/src/layers/earthquakes/records.js` and `source.js`
- USGS GeoJSON summary format: https://earthquake.usgs.gov/earthquakes/feed/v1.0/geojson.php

## Objective

Implement the first real adapter end to end. It also serves as the **reference pattern** for every later adapter, so make it exemplary, and write the Adapter Guide that future prompts and the AI maintenance agent will follow.

## Tasks

### 1. Adapter (`ai.elaro.source.adapters.usgs`)

**`UsgsEarthquakesAdapter implements PollingAdapter`**, `@Component`. Descriptor:

| Field | Value |
|---|---|
| id | `usgs-earthquakes` |
| name | "USGS Earthquakes" |
| kinds | `{earthquake}` |
| homepage | `https://earthquake.usgs.gov/earthquakes/feed/` |
| attribution | "U.S. Geological Survey, Earthquake Hazards Program" |
| mode | POLLING |
| credentials | none |
| params | `feed` (string, default `all_day`, allowed: `all_hour, all_day, all_week, all_month, 1.0_hour, 1.0_day, 1.0_week, 2.5_hour, 2.5_day, 2.5_week, 2.5_month, 4.5_hour, 4.5_day, 4.5_week, 4.5_month, significant_hour, significant_day, significant_week, significant_month`) |
| minInterval | 60 s |
| defaultInterval | 60 s |
| defaultBudget | 4 / minute, 2000 / day |
| recordTtl | null |
| zeroResultsNormal | **true** (quiet hours happen, especially on `significant_*`) |
| tileProperties | `mag, place, depth, tsunami, alert, sig` |
| searchFields | `place, title` |
| rankField | `mag` |
| embedFields | none (quakes are found spatially, not semantically) |

`fetch`:

- `ctx.http().getConditional(URI("https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/" + feed + ".geojson"), "feed:" + feed)`
- 304 → `FetchResult.notModified()`; otherwise parse with `UsgsGeoJsonParser`
- `complete = true`

**`UsgsGeoJsonParser`** is a pure, static, stateless function: `List<NormalizedRecord> parse(byte[] json)`.

- `externalId` = `feature.id`; `kind` = `earthquake`; `observedAt` = `properties.time` (epoch ms).
- `geometry` = Point(lon, lat) from `geometry.coordinates[0..1]`.
- Payload: `mag, magType, place, time, updated, tz, url, detail, felt, cdi, mmi, alert, status, tsunami, sig, net, code, ids, sources, types, nst, dmin, rms, gap, type, title`, plus `depth` = `coordinates[2]` (km). Keep nulls out of the payload map (omit absent keys) so hashes are stable.
- Robustness:
  - skip features with a missing id or coordinates, counting skips in a returned diagnostics object or a log line
  - throw `UpstreamFormatException` when the document is not a FeatureCollection, or `features` is missing (this is how format changes surface)
- Use Jackson 3 tree parsing.

A revised quake (USGS updates `mag` / `updated`) naturally produces a new version through content hashing. No special handling.

### 2. Fixtures (`src/test/resources/fixtures/usgs-earthquakes/`)

- `all_hour.geojson`: capture a **real** response once (`curl -s https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_day.geojson`), trimmed to ≤ 25 features, with `metadata` kept. Record the capture date in a sibling `README.md` along with the source URL.
- `edge-cases.geojson`: hand-made, covering a null `mag`, missing optional fields, a feature without an id (skipped), a feature without geometry (skipped), and a feature with `depth` missing.
- `revised.geojson`: the same as one feature in `all_hour` but with a changed `mag` and `updated`.
- `not-a-feature-collection.json`: e.g. `{"error": "x"}`.

### 3. Adapter Guide (`docs/Adapter-Guide.md`)

A concise guide (≈ 1–2 pages) using USGS as the worked example:

- package layout
- descriptor checklist, including how to choose interval and budget from the upstream's published policy
- parser purity
- fixture capture procedure and its README
- the required tests (parser contract, WireMock adapter, ingest IT)
- etiquette rules (conditional GET, no concurrency unless allowed, honour Retry-After, never spoof the UA, never scrape)
- the GEV porting rules (origin header + `THIRD_PARTY_NOTICES.md`)
- adapter families
- streaming-adapter specifics (prompt 030 adds the first one), and PUSH-mode sources (prompt 020)
- **"Repair procedure"** for the future maintenance agent: probe → diff against the fixture → update the parser → refresh the fixture → contract test

## Tests to write (do not run)

- `UsgsGeoJsonParserTest`: the real fixture parses to the expected count, with spot-checked fields; edge cases (skips, omitted nulls, depth); a non-FeatureCollection throws `UpstreamFormatException`; empty `features` returns an empty list.
- `UsgsEarthquakesAdapterTest` (WireMock): correct URL per `feed` param; the conditional GET round trip (200 with ETag, then 304 → notModified); 500 → `UpstreamHttpException`; the User-Agent header is present.
- `UsgsIngestIT` (`@IntegrationTest` + WireMock, with the adapter's base URL overridable for tests via a package-private constructor or a test-only property; document the mechanism in the Adapter Guide):
  - enable the source, so the first fetch ingests N records
  - serve `revised.geojson`, so exactly one record gets version 2
  - re-serving the same content produces no new versions
  - health becomes `healthy`

## Acceptance criteria

- `usgs-earthquakes` appears in the registry with valid defaults.
- `docs/Adapter-Guide.md` exists.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests are first run in prompt 061 (Full Test Run And Fix).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`). **Do not add Flyway, Liquibase or SQL migration files.**
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor, which are AGPL. They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `009: USGS Earthquakes Adapter`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/009-CR-USGS-Earthquakes-Adapter.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `009: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
