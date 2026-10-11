# 071 — GBFS Bikeshare

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/071-CR-GBFS-Bikeshare.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–126).
- **Previous Completion Report:** read `completion-reports/070-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Core-Data-Model.md` §2 ("Catalogue and reference" / measurement rows), §3 (rules 7 and 8), §5.1 (keyframe constraint; LATEST requires `significantFields`), §5.3
- `docs/Architecture.md` §4.2 rule 5 (families from a static reviewed list)
- `docs/Source-Catalog.md` row `gbfs-*` (60 s, reject redirects)
- GEV (MIT, portable): `server/providers/gbfs.js` (no redirects, 5 MB cap, 12 s timeout), `src/data/gbfsSource.js` (host and path allowlist), `src/layers/bikeshare/registry.js` (32 reviewed systems, BCycle URL builder), `src/sources/gbfsStations.js` (field normalisation)
- Upstream: GBFS spec `https://github.com/MobilityData/gbfs/blob/master/gbfs.md` (v3.0 current; v3.1-RC3 2026-05-26), v2.3 reference for the Lyft and BCycle feeds

## Objective

Add the `gbfs-*` family: docked bikeshare stations from a reviewed list of systems. Each source merges `station_information` (static) and `station_status` (dynamic) into one snapshot per station. A LATEST policy writes versions only when a station's service state changes; availability counts travel as live updates. GBFS v2.x and v3.0 are both supported.

## Tasks

### 1. Reviewed system list (`ai.foresignal.source.adapters.gbfs.GbfsSystems`)

- Port GEV's `RAW_GBFS_CITY_REGISTRY` (32 systems: Lyft-operated `gbfs.lyft.com/gbfs/2.3/{sys}/en/…`, `gbfs.bcycle.com/{sys}/…`, `*.publicbikesystem.net/customer/gbfs/v2/en/…`, Bluebikes, Biketown, CoGo) to a Java record list.
  - Fields: `id, city, provider, stationInformationUrl, stationStatusUrl, gbfsVersion` (`2.x|3.0`), `licence, licenceUrl, attribution`.
  - Keep the BCycle URL builder.
- **Licences:** GEV's registry carries none. During fixture capture, read each system's `system_information.json` (`license_url` / `license_id`, GBFS v2.3+ and v3) and record it in the list. When absent, set `licence = "operator terms"` with the operator's data page as `licenceUrl`, and list those systems in the Completion Report.
- **Host allowlist:** derive it from the list. A unit test asserts that every URL host is in the allowlist and every path ends in `station_information.json` or `station_status.json` (port `isAllowedGbfsHost`/`isAllowedGbfsPath`). No `gbfs.json` auto-discovery at runtime, because discovery would let upstream data choose URLs.
- MobilityData `systems.csv` (CC BY 3.0) is **not** vendored here. Adding systems is a code change to the reviewed list. Note in the Adapter Guide that a catalogue resolver like 070's could follow, with attribution.

### 2. Descriptor (`GbfsFamily`, one source per system: `gbfs-{id}`)

| Field | Value |
|---|---|
| kinds | `{bikeshare-station}` |
| interval / budget | 60 s (min 30 s); 3/min, 2000/day |
| redirects | **Refused.** Add descriptor `followRedirects` (default true; false here). Hook: `UpstreamClient` sends with `Redirect.NEVER` when false, and any 3xx throws `UpstreamFormatException("redirect refused: {host}")`. The 3xx counts as a failure, not a backoff. |
| limits | `maxResponseBytes` 5 MB, `fetchTimeout` 15 s |
| tier / policy | STANDARD, `LATEST{keyframe 20m, edgeMinInterval 30s}`, `recordTtl` 30 min (anchor RECEIVED). Check: `1200 ≤ 1800 − 60 − 30`. |
| significantFields | `name, capacity, isInstalled, isRenting, isReturning, availability` |
| time | UPSTREAM; `observedAt = station_status.last_reported` per station (epoch s in 2.x, RFC 3339 in v3). A station without it is dropped and counted; never imputed (fixed time base). |
| title / search / tiles | `titleField name`; search `name, address`; tiles `bikesAvailable, docksAvailable, availability, ebikesAvailable` |
| `zeroResultsNormal` | false |

### 3. Fetch and merge

- **Each run:**
  1. If the cached `station_information` is missing, or older than max(its `ttl`, 1 h), `getConditional` it first. Cache the parsed stations in memory, keyed by `station_id`; refetch after a restart.
  2. `getConditional` `station_status`.
  3. Skip the status request entirely when `last_updated + ttl` of the previous status is still in the future and later than the next scheduled run. This honours GBFS `ttl` without polling faster than published. `ttl = 0` means "always refresh".
- **Parsing (`GbfsParser`, pure):** both `{last_updated, ttl, version?, data: {stations: [...]}}` shapes.
  - **v3:** `name` is localised (`[{text, language}]`); pick param `language` (default `en`), else the first. Use `num_vehicles_available` and `vehicle_types_available`; `last_updated` is RFC 3339.
  - **v2.x:** `name` is a string. Use `num_bikes_available`, `num_ebikes_available` (optional) and `num_docks_available`; times are epoch seconds.
  - Booleans may be `0/1` or `true/false` (port `normalizeGbfsBool`).
- **Snapshot (rule 7):** for each status entry with matching information, emit one record.
  - `externalId = station_id`; geometry `Point(lon, lat)` from information. Stations with status but no information are skipped and counted until information arrives.
  - Payload:
    - `name, address` (when present), `capacity, regionId`
    - `bikesAvailable, ebikesAvailable, docksAvailable, disabledBikes, disabledDocks`
    - `isInstalled, isRenting, isReturning`
    - `availability`, computed from the counts as `out-of-service` (not installed or not renting), `empty` (0 bikes), `low` (≤ 2 bikes), `full` (0 docks) or `ok`
    - `system` (the list id)
- Free-floating `vehicle_status` (formerly `free_bike_status`) is out of scope: vehicle ids rotate by design for rider privacy, and nothing here tracks them.
- `complete = true`. A station missing from status for its TTL expires; no tombstones.

### 4. Fixtures

- Capture with a README (date, URLs, the `system_information` licence fields):
  - `station_information` and `station_status` for one Lyft 2.3 system (Citi Bike), one BCycle system and one publicbikesystem system, each trimmed to 25 stations with matching ids
  - one v3.0 pair from any system publishing 3.0, or hand-made from the v3.0 spec examples and marked as such
- Hand-made: `status-only-station.json`, `bool-as-int.json`, `redirect` (a WireMock 302), `ttl-future.json`, `localized-name-v3.json`.

### 5. Catalogue and docs

- `docs/Source-Catalog.md` `gbfs-*` row: the reviewed list size, STANDARD/LATEST, TTL 30 min, `availability` as the significant summary, redirects refused, `ttl` honoured, v2.x and v3.0 support, per-system licences.
- Operator Guide: which systems are included and how to propose more (code review).
- OpenAPI: no operation changes. `followRedirects` is internal.
- `THIRD_PARTY_NOTICES.md`: GEV entries for `gbfs.js`, `gbfsSource.js`, `registry.js` and `gbfsStations.js`.

## Tests to write (do not run)

- `GbfsParserTest`:
  - v2.3 and v3.0 fixtures produce identical payload shapes
  - localised names and the language fallback
  - bool normalisation
  - `availability` boundaries (0, 2, 3 bikes; 0 docks; not renting)
  - missing `last_reported` dropped
- `GbfsSystemsTest`: every URL host allowlisted and path allowlisted; unique ids; registry validation passes for all sources (keyframe constraint).
- `GbfsAdapterTest` (WireMock):
  - information fetched once, then only after its TTL or 1 h
  - status skipped while `last_updated + ttl` is in the future
  - a 302 → `UpstreamFormatException`, and the redirect target is never requested (`verify(0, …)`)
  - status-only station skipped
  - 304 handling
- `GbfsIngestIT` (`@IntegrationTest`):
  - first poll → FIRST versions
  - a bike-count change from 5 to 4 → LIVE, no version
  - 3 → 2 (`ok` → `low`) → SIGNIFICANT
  - `isRenting` false → SIGNIFICANT
  - unchanged polls → touches; keyframe after 20 min

## Acceptance criteria

- `gbfs-*` sources register with valid LATEST defaults, never follow a redirect, and only request URLs from the reviewed list.
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
2. **Commit** all changes with the message `071: GBFS Bikeshare`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/071-CR-GBFS-Bikeshare.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `071: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
