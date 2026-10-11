# 011 — USGS Earthquakes Adapter

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/011-CR-USGS-Earthquakes-Adapter.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–126).
- **Previous Completion Report:** read `completion-reports/010-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §4.2 (adapter rules)
- `docs/Core-Data-Model.md` §2 (time contract; row "Revised events (USGS, EMSC, GDACS)"), §3 (descriptor fields, adapter rules 7–13), §4 ("Required tests"), §5.3 (defaults: "Everything else" = ALL, STANDARD), and the **"009 USGS"** bullets under *Per-prompt changes* (009 is this prompt)
- `docs/Gap-Analysis.md` §8.A item 3 (Adapter Guide altitude convention; fieldSpecs contract test)
- `docs/Source-Catalog.md` row `usgs-earthquakes`
- GEV reference (MIT, portable): `~/Documents/GitLab/gods-eye-view/src/layers/earthquakes/records.js` and `source.js`
- USGS GeoJSON summary format: https://earthquake.usgs.gov/earthquakes/feed/v1.0/geojson.php

## Objective

Implement the first real adapter end to end on the v1.2 pipeline. It is the **reference pattern** for every later adapter, so make it exemplary, and write the Adapter Guide that later prompts and the adapter-maintenance agent (prompt 127) follow. USGS exercises the event-time vs observed-time split: a revised quake is a SIGNIFICANT version, while an unchanged re-poll is only a touch.

## Tasks

### 1. Adapter (`ai.foresignal.source.adapters.usgs`)

**`UsgsEarthquakesAdapter implements PollingAdapter`**, `@Component`. Descriptor:

| Field | Value |
|---|---|
| id / name / kinds | `usgs-earthquakes` / "USGS Earthquakes" / `{earthquake}` |
| homepage / attribution | `https://earthquake.usgs.gov/earthquakes/feed/` / "U.S. Geological Survey, Earthquake Hazards Program" |
| licence / termsUrl | `US-PD` / `https://www.usgs.gov/information-policies-and-instructions/copyrights-and-credits` |
| mode, credentials | POLLING, none |
| params | `feed` (string, default `all_day`, allowed: `all_hour, all_day, all_week, all_month, 1.0_hour, 1.0_day, 1.0_week, 1.0_month, 2.5_hour, 2.5_day, 2.5_week, 2.5_month, 4.5_hour, 4.5_day, 4.5_week, 4.5_month, significant_hour, significant_day, significant_week, significant_month`) |
| minInterval / defaultInterval | 60 s / 60 s (the feeds regenerate about once a minute) |
| defaultBudget | 4 / minute, 2,000 / day (USGS publishes no numeric limit; this stays well inside one request per feed-minute) |
| historyTier / observedAtMode / ttlAnchor | STANDARD / UPSTREAM / RECEIVED (default) |
| recordTtl | null (quakes don't expire) |
| defaultIngestPolicy | ALL (keyframe "none", since there is no TTL) |
| volatileFields / significantFields | `[updated]` / `[]` (every other key is significant) |
| titleField | `title` |
| zeroResultsNormal | **true** (quiet hours happen, especially on `significant_*`) |
| tileProperties / searchFields / rankField / embedFields | `mag, place, depth, tsunami, alert, sig` / `place, title` / `mag` / none (quakes are found spatially) |
| fieldSpecs | every payload key below, with type, UCUM unit and label (e.g. `mag` NUMBER `1` "Magnitude"; `depth` NUMBER `km` "Depth", datum "network-dependent reference, positive down"; `time`/`updated` TIMESTAMP `ms` "Origin time"/"Last updated"; `nst` INTEGER `{station}`; `dmin`/`gap` NUMBER `deg`; `rms` NUMBER `s`; `felt` INTEGER `{report}`; `cdi`/`mmi` NUMBER `1`; `sig` INTEGER `1`; strings for the rest) |
| kindDisplay | `earthquake`: titleTemplate `M {mag} - {place}`, icon `earthquake`, colour `#D9480F` |

`fetch`:
- `ctx.http().getConditional(URI(baseUrl + "/earthquakes/feed/v1.0/summary/" + feed + ".geojson"), "feed:" + feed)`, with `baseUrl` = `https://earthquake.usgs.gov`, overridable only by the test property `foresignal.test.adapters.usgs.base-url` (document the mechanism in the Adapter Guide).
- A 304 → `FetchResult.unchanged()`; otherwise parse with `UsgsGeoJsonParser` and return `FetchResult.complete(records)` (complete is informational; absent records are never auto-tombstoned, and quakes age out of the rolling window without being deleted).
- Verified 2026-10: responses carry **no `ETag`**, only `Last-Modified` (generation time) and `cache-control: public, max-age=60`; `If-Modified-Since` returns 304. `metadata.api` is `2.7.0`; the feature shape matches the format page; `tz` is always null.

**`UsgsGeoJsonParser`**: a pure static function `List<NormalizedRecord> parse(byte[] json)` using Jackson 3 tree parsing.
- `externalId` = `feature.id`; `kind` = `earthquake`.
- **Time base (Core-Data-Model §2):** `eventTime` = `properties.time` (origin, epoch ms); `observedAt` = `properties.updated` (epoch ms), always. Never fall back to `time` for `observedAt` per record (rule 13); a feature without `updated` is skipped and counted.
- `geometry` = 2D Point(lon, lat) from `coordinates[0..1]`; `coordinates[2]` goes to payload `depth` (km), never geometry Z (altitude convention).
- Payload allowlist: `mag, magType, place, time, updated, url, detail, felt, cdi, mmi, alert, status, tsunami, sig, net, code, ids, sources, types, nst, dmin, rms, gap, type, title, depth`. Omit absent and null keys (rule 8). No `validUntil`.
- `properties.status == "deleted"` → `NormalizedRecord.tombstone(id, "earthquake", updated)`. Summary feeds normally omit deleted events, so this is defensive.
- Skip features with a missing id, coordinates or `updated` (return counts in a `ParseDiagnostics` object logged by the adapter). Throw `UpstreamFormatException` (≤ 500-char snippet) when the document is not a `FeatureCollection` or `features` is missing; that is how format changes surface.

A revised quake (USGS updates `mag`, `status` and `updated`) changes the static hash and becomes version 2 with `SIGNIFICANT`. A re-fetch where only `updated` moved hashes equal, because `updated` is volatile. An unchanged re-poll is TOUCH (or TOUCH_THROTTLED inside `touchMin` = 1 h).

### 2. Fixtures (`src/test/resources/fixtures/usgs-earthquakes/`)

- **Capture procedure:** `curl -s -A "Foresignal-fixture-capture (+https://foresignal.ai)" https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_day.geojson`, trimmed to ≤ 25 features with `metadata` kept, saved as `all_day.geojson`. Write the capture date, URL and trimming method in a sibling `README.md`.
- `edge-cases.geojson` (hand-made): null `mag`, missing optional fields, a feature without an id, one without geometry, one without `updated`, one with `depth` missing, one with `status: deleted`.
- `revised.geojson`: one feature of `all_day` with changed `mag`, `status: reviewed` and later `updated`.
- `updated-only.geojson`: the same as `all_day` with only `updated` advanced on one feature.
- `not-a-feature-collection.json`: `{"error": "x"}`.

### 3. Adapter Guide (`docs/Adapter-Guide.md`)

About 2–3 pages, using USGS as the worked example:
- Package layout; parser purity; adapter rules 1–6 (Architecture §4.2) and **7–13** (Core-Data-Model §3).
- **Descriptor checklist:** interval and budget from the upstream's published policy; `historyTier`, `observedAtMode`, `ttlAnchor`, `recordTtl`, `defaultIngestPolicy` per Core-Data-Model §5.3, `volatileFields`, `significantFields`, `motion`/`thresholds`, `licence`, `termsUrl`, `titleField`, `fieldSpecs`, `kindDisplay`, `entityKeys`, `embedFields` for text-bearing records, `private` for personal data.
- **Time-base mapping:** the Core-Data-Model §2 adapter table, verbatim in spirit, with "never switch timestamp fields per record".
- **Altitude convention:** geometry is always 2D; vertical quantities live in payload fields declared in `fieldSpecs` with a UCUM unit and a `datum` (MSL, WGS84, AGL, PRESSURE). New altitude and elevation fields use metres; an upstream field kept verbatim in a domain-standard unit (readsb `alt_baro` in `[ft_i]`, USGS `depth` in `km`) must declare that unit.
- **fieldSpecs contract:** every payload key an adapter emits must be declared; `FieldSpecContract` (below) enforces it.
- Etiquette: conditional GET where supported, no concurrency unless allowed, honour Retry-After, an identifiable User-Agent and never a spoofed one, no scraping.
- Fixture capture and README; required tests (parser contract with `FieldSpecContract` and `SnapshotContract`, WireMock adapter test, and an ingest IT that drives real fetches through the scheduler and writer, never seeding record tables with raw SQL); the test base-URL property.
- GEV porting rules (origin header + `THIRD_PARTY_NOTICES.md`); AGPL/GPL projects are reference only; adapter families; streaming specifics (`PolicyAwareRecordSink`, first streaming adapter in prompt 063); PUSH sources (prompt 024); DERIVED sources (prompt 052).
- **Repair procedure** for the maintenance agent: probe → diff against the fixture → update the parser → refresh the fixture and README → contract test.

### 4. Test support

`FieldSpecContract.assertPayloadKeysDeclared(SourceDescriptor d, List<NormalizedRecord> records)` in `src/test/java/ai/foresignal/testsupport/adapters`: fails listing every payload key not in `fieldSpecs` (respecting `FieldSpec.kinds`) and every declared key whose observed JSON type contradicts `FieldType`.

### 5. Spec, Postman, docs

- `docs/Source-Catalog.md`: rename the heading "First source (prompt 009)" to "(prompt 011)" and update the `usgs-earthquakes` row with tier STANDARD, policy ALL, TTL none, ttlAnchor RECEIVED, observedAtMode UPSTREAM (`updated`; `eventTime` = `time`), significantFields all, volatileFields `updated`, retention default (7d), licence US-PD, and the verified 2026-10 conditional-GET note.
- No API changes; OpenAPI and Postman are untouched.

## Tests to write (do not run)

- `UsgsGeoJsonParserTest`: the real fixture parses to the expected count with spot-checked fields; `eventTime` = `time`, `observedAt` = `updated`; geometry is 2D with SRID 4326 and `depth` in the payload; edge cases (skips with counts, omitted nulls, missing depth, deleted → tombstone); non-FeatureCollection throws `UpstreamFormatException`; empty `features` → empty list; `FieldSpecContract` passes on every fixture; `SnapshotContract.assertStableStaticHash` holds for `all_day` vs `updated-only`.
- `UsgsEarthquakesAdapterTest` (WireMock): URL per `feed`; conditional GET round trip (200 with `Last-Modified`, then `If-Modified-Since` → 304 → unchanged); 500 → `UpstreamHttpException`; identifiable User-Agent present.
- `UsgsIngestIT` (`@IntegrationTest` + WireMock + `MutableClock`, through the scheduler and the real writer):
  - enable the source: the first fetch writes N versions with `FIRST`, one OK `fetch_log` row with `versions_written = N`, health `healthy`
  - `revised.geojson`: exactly one version 2 with `SIGNIFICANT`, `prev_seq` set, `event_time` unchanged
  - re-serving identical content: 0 versions, no ChangeBus event, `touches_throttled` counted; after advancing the clock past 1 h, a TOUCH advances `record_live.last_received_at`
  - `updated-only.geojson`: 0 versions (volatile `updated`), `record_live.observed_at` advanced
  - a 304 writes a NOT_MODIFIED row and leaves `last_data_at` unchanged

## Acceptance criteria

- `usgs-earthquakes` registers with valid defaults, passing the v1.2 registry validation (STANDARD, no TTL, ALL).
- `docs/Adapter-Guide.md` exists and covers the altitude convention and the fieldSpecs contract.
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
2. **Commit** all changes with the message `011: USGS Earthquakes Adapter`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/011-CR-USGS-Earthquakes-Adapter.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `011: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
