# 047 — Severe Weather Record Sources

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/047-CR-Severe-Weather-Record-Sources.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–129).
- **Previous Completion Report:** read `completion-reports/046-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Weather-Imagery-Design.md` §1.4 (AWC request limits), §2.4 (per-kind retention table), §2.5 (budgets), §5.1 (record sources and the places AOI), §7.1 (derived-record principles: unknown is not clear), §7.3–§7.4 (trigger additions and rule recipes). Design prompt 034 = this prompt; design 033 Places-And-AOI is merged into **036** (Roadmap §5).
- `docs/Core-Data-Model.md` §2 (time contract; NWS row: eventTime = onset, observedAt = `sent`, `validUntil = expires`), §3 (descriptor fields, adapter rules 7–13), §5.3 (defaults: everything here is ALL / STANDARD)
- `docs/Gap-Analysis.md` §8 item 31 (NWS onset → `validFrom`), item 46 (METAR overlap with 087), item 48 (`spc-outlooks` now lives here, not in 089)
- `docs/Source-Catalog.md` `nws-alerts` row
- Upstream docs: https://www.weather.gov/documentation/services-web-api, https://www.spc.noaa.gov/gis/, https://aviationweather.gov/data/api/ (cache files and limits), https://registry.opendata.aws/noaa-mrms-pds/
- Completion Reports 008, 011 (Adapter Guide), 017, 019, 036, 040

## Objective

Add four record sources that turn official severe-weather products into alertable records: NWS active alerts (moved here from the hazard adapters), MRMS ProbSevere v3 storm objects inside the places AOI, SPC convective outlooks, and AWC METARs. Every tombstone comes from a successfully processed, complete upstream response, never from a feed outage, so stale data never reads as an all-clear.

## Tasks

### 1. Hooks (check, add if missing)

- **036:** `PlacesAoiService` with `aoi()` (union of each place buffered by max(`radiusKm`, 150 km)) and `envelope()`, falling back to `weather.defaultAoi` (Texas bbox `-106.65,25.84,-93.51,36.50`) with a health warning when there are no places, and a `PlacesAoiChangedEvent`. Add a minimal version per Weather-Imagery-Design §5.1 if absent.
- **017 (or 036):** trigger `cooldownScope: record|rule` (rule scope: `NOT EXISTS (… a.rule_id = :rule AND a.fired_at > now() − :cooldown)` and the batch reduced with `ROW_NUMBER() OVER (PARTITION BY rule_id ORDER BY seq) = 1`) and `near: {placeId|point, radiusKm}` materialised into `geofence` via `ST_Buffer(geography)` and re-materialised when the place moves. Implement per §7.3 if missing.
- **019:** per-kind retention resolution, so descriptor `kindRetention` defaults apply.
- **008:** the `validFrom` descriptor field / record extra and `titleField`, `fieldSpecs`. **040:** the `ref_admin2` US county table (`fips5`, `geom`), used for NWS county geometry below.
- `InstallIdService.userAgent()` must include a contact (NWS requires one). If it does not, append the optional `server.contactEmail` setting; never spoof a browser.

### 2. `nws-alerts` (`ai.foresignal.source.adapters.nws`)

- `GET https://api.weather.gov/alerts/active?status=actual` (+ `area=` when the param is set), `Accept: application/geo+json`. Verified 2026-10: the national feed is ≈ 2.6 MB raw, ≈ 207 KB gzip; **`If-None-Match` is not honoured** (a repeat returns 200), so rely on gzip and record `body_sha256`.
- Descriptor: POLLING, interval 60 s (min 30 s), budget 2/min, 1,600/day, kind `nws-alert`, `kindRetention{nws-alert: 30d}`, STANDARD, policy ALL, `observedAtMode` UPSTREAM, `ttlAnchor` RECEIVED, `recordTtl` null, `licence` "US-PD", `termsUrl` https://www.weather.gov/disclaimer, `titleField` `headline`, `embedFields` `[headline, description, instruction]`, `searchFields` `[event, headline, areaDesc]`, `zeroResultsNormal=true`.
- Params: `area` (string-list of state/marine codes, default empty = national), `minSeverity` (Extreme|Severe|Moderate|Minor|Unknown, default none).
- Mapping: `externalId` = `properties.id`; `observedAt` = `sent`; `eventTime` = `onset` ?? `effective`; `validFrom` = `onset`; `validUntil` = `expires` (explicit upstream expiry).
- Payload allowlist (no nulls; strings ≤ 8 KB, longer text truncated with `…`): `event`, `eventCode` (SAME/NWS), `headline`, `severity`, `certainty`, `urgency`, `messageType`, `category`, `response`, `senderName`, `areaDesc`, `sent`, `effective`, `onset`, `expires`, `ends`, `ugc[]`, `same[]`, `vtec[]`, `motion` (from `eventMotionDescription`), `maxHailSizeIn`, `maxWindGustMph`, `tornadoDetection`, `thunderstormDamageThreat`, `description`, `instruction`, `web`. `fieldSpecs` with units for the numeric ones.
- Geometry: the polygon when present. Otherwise, when every UGC is a county (`C`) and 040's `ref_admin2` is loaded, the union of the counties whose `fips5` = the last five digits of each `SAME` code (6 digits = `0` + state FIPS + county FIPS), simplified to 0.005°, with payload `geometrySource: "county-union"`; otherwise null geometry (`geometrySource: "none"`; zones stay in `ugc`).
- Lifecycle: `FetchResult.complete=true`; ids absent from a successful complete response are tombstoned (scoped to the configured `area`). `messageType: Cancel` and `Update` tombstone the ids in `references[].identifier`. A failed or truncated fetch tombstones nothing.
- Description and instruction are untrusted upstream text: they are embedded and searchable but marked as data in any LLM context (the 028 citation rules apply).
- Prompt 067 (Hazard-Adapters) no longer owns `nws-alerts`; add a note to the Source Catalog so it is not re-implemented there.

### 3. `mrms-probsevere` (`ai.foresignal.source.adapters.mrms`)

- `listS3("noaa-mrms-pds", "ProbSevere/{YYYYMMDD}/", startAfter)` then download **only the newest** file (`MRMS_PROBSEVERE_20261010_000041.json`, ≈ 0.25–0.27 MB, verified 2026-10). Objects are snapshots; skipping intermediate files loses nothing.
- Descriptor: POLLING, interval 2 min, **budget 4/min, 1,600/day** (a list plus a download every 2 min is 1,440/day; the design's 800/day cannot cover it: record this), `mbPerDay` 300, kind `probsevere-object`, `kindRetention{probsevere-object: 3d}`, STANDARD, ALL, `recordTtl` null, `licence` "US-PD", `titleField` `label`, `zeroResultsNormal=true`.
- Parse: header `validTime` ("20261010_000041 UTC") → `observedAt`; `eventTime` null. Features: Polygon geometry; **every property is a string** → convert to numbers.
- Filter: keep features whose polygon intersects `PlacesAoiService.envelope()`; refresh on `PlacesAoiChangedEvent`.
- `externalId` = `ID`. Payload: `probSevere`, `probTor`, `probHail`, `probWind` (ints), `meshIn` (0.25 in steps), `compRefDbz` (1 dBZ), `vil`, `echoTop50Kft`, `flashRate`, `muCape`, `mlCape`, `ebShearKt`, `srh01km`, `motion{speedKmh (5), bearingDeg (10)}` derived from `MOTION_EAST`/`MOTION_SOUTH` (m/s), `label` ("ProbSevere 44% (tor 26%)"), `automated: true`, `notAWarning: true`. `volatileFields`: `[muCape, mlCape, ebShearKt, srh01km, flashRate]` (environmental fields that drift every scan).
- Tombstone ids absent from the newest **successfully processed** file (within the AOI). An object leaving the AOI is tombstoned too (documented). No tombstones when the listing or download failed or the file is older than the last processed one.

### 4. `spc-outlooks` (`ai.foresignal.source.adapters.spc`)

- Files under `https://www.spc.noaa.gov/products/outlook/`, each fetched with `getConditional` (ETag and Last-Modified verified 2026-10):
  - day 1 and day 2: `day{d}otlk_{cat,torn,wind,hail,sigtorn,sigwind,sighail}.lyr.geojson`
  - day 3: `day3otlk_{cat,prob,sigprob}.lyr.geojson` (verified 2026-10: day 3 has no torn/wind/hail layers; those URLs return 404)
- Descriptor: POLLING, interval 15 min, budget 20/min, 2,000/day (17 conditional requests per run, mostly 304), kind `spc-outlook`, `kindRetention{spc-outlook: 14d}`, STANDARD, ALL, `licence` "US-PD", `titleField` `title`.
- Feature properties `DN`, `LABEL`, `LABEL2`, `VALID`, `EXPIRE`, `ISSUE` (`yyyyMMddHHmm` UTC). `externalId` = `day{d}:{type}:{LABEL}` (e.g. `day1:cat:MDT`, `day1:torn:0.10`, `day1:sigtorn:SIGN`). `observedAt` = ISSUE, `eventTime` = VALID, `validUntil` = EXPIRE. Payload `day`, `type`, `label`, `label2`, `dn` (numeric rank for rules such as `dn >= 5`), `issue`, `valid`, `expire`, `title` ("Day 1 Moderate Risk"). MultiPolygon geometry.
- Per-file completeness: labels absent from a successfully fetched (200) file are tombstoned for that day and type only. A 304 changes nothing; a 404 for a layer marks health degraded with reason "SPC layer {file} missing" and tombstones nothing.

### 5. `awc-metars` (`ai.foresignal.source.adapters.awc`)

- `GET https://aviationweather.gov/data/cache/metars.cache.csv.gz` with `getConditional` (ETag, Last-Modified, `max-age=30` verified 2026-10). AWC asks for at most 1 request/min per thread and 100/min overall: a 5-minute poll complies. Put this in the descriptor notes.
- Descriptor: POLLING, interval 5 min (min 1 min), budget 2/min, 400/day, `mbPerDay` 100, kind `metar`, `kindRetention{metar: 7d}`, STANDARD, ALL, `ttlAnchor` OBSERVED, `recordTtl` 3 h (an old report must not read as current), `licence` "US-PD", `titleField` `stationId`.
- Params: `scope` = `aoi` (default; places-AOI envelope) | `conus` | `global`; `stations` (string-list allowlist, overrides scope).
- Parse: skip lines until the header beginning `raw_text,station_id,` (older files carried a preamble). The four repeated `sky_cover,cloud_base_ft_agl` pairs are read by position. Keep only the newest `observation_time` per station.
- Mapping: `externalId` = `station_id`; `observedAt` = `eventTime` = `observation_time`; point geometry. Payload: `raw`, `stationId`, `metarType` (METAR|SPECI), `tempC`, `dewpointC`, `windDirDeg` (omit when VRB, with `windVariable: true`), `windKt`, `gustKt`, `visibilitySm` ("10+" → 10 with `visibilityPlus: true`), `altimInHg`, `slpMb`, `wx`, `sky[{cover, baseFtAgl}]`, `flightCategory`, `elevationM`. Omit empty fields (no nulls). `fieldSpecs` with UCUM units. An identical re-fetch is a touch.
- Prompt 087 adds TAF, PIREP and G-AIRMET; it must reuse this METAR source.

### 6. Rule recipes and documentation

- Document in `docs/Agent-Guide.md` (weather section) with ready-to-POST trigger bodies:
  - Tornado warning touching home: `kind=nws-alert`, `event == "Tornado Warning"`, `near{home, 0}`, `enter`.
  - Tornado probability entering the county: `kind=probsevere-object`, `probTor >= 30`, geofence, `enter`, `cooldownScope=rule`.
  - Moderate risk or higher today: `kind=spc-outlook`, `type == "cat"`, `day == 1`, `dn >= 6`, `near{home, 0}`, `enter`.
  - Low ceiling at an airport: `kind=metar`, `flightCategory in [IFR, LIFR]`, `enter`.
- State plainly: ProbSevere is automated guidance (`notAWarning`); NWS alerts are authoritative.

### 7. Spec, Postman, docs

- No new operations. If 042 did not add them, ensure `SourceHealth` health reasons and `Record` payload examples exist; add `x-foresignal-examples` for the four kinds under `Record`. Regenerate Postman with `python3 scripts/generate-postman.py docs/OpenAPI.json postman` if the spec changed.
- `docs/Source-Catalog.md`: rows for all four sources (tier, policy, TTL, `ttlAnchor`, `observedAtMode`, significant/volatile fields, retention, licence, budgets and the verified facts above); move `nws-alerts` out of the hazard group; remove `spc-outlooks` from the 089 group.
- `docs/Adapter-Guide.md`: "tombstone only from a complete, successful response" rule with these sources as examples.

## Tests to write (do not run)

- Fixture capture procedure in each adapter's test package README: `curl -A "<foresignal UA>" --compressed` for NWS (national and `area=TX`), the newest ProbSevere key via the S3 listing, each SPC layer, and the AWC cache file; store under `src/test/resources/fixtures/{nws,probsevere,spc,awc}/`.
- Parser contract tests: `NwsAlertsParserTest` (polygon vs county-union vs none, onset → eventTime/validFrom, Cancel/Update references, text truncation, `fieldSpecs` coverage via the 011 contract helper), `ProbSevereParserTest` (string → number, motion derivation, AOI filter), `SpcOutlookParserTest` (ids, ISSUE/VALID/EXPIRE parsing, day-3 layer set), `MetarCsvParserTest` (preamble skip, repeated sky columns, VRB, "10+", newest-per-station).
- WireMock adapter tests: `NwsAlertsAdapterTest` (UA contains contact, gzip, `area` param, 503 + Retry-After backoff, truncated body → no tombstones), `ProbSevereAdapterTest` (listing with `startAfter`, newest-only download, failed download → no tombstones), `SpcOutlooksAdapterTest` (304 path, 404 layer → degraded and no tombstones), `AwcMetarsAdapterTest` (conditional GET, budget).
- Ingest ITs (seeding through the real pipeline, `TestRecords` for assertions only): `NwsAlertsIngestIT` (expiry via `validUntil`, absent id tombstoned, re-fetch → touches only), `ProbSevereIngestIT` (AOI change re-filters), `SpcOutlookIngestIT`, `MetarIngestIT` (new report → version; same report → touch; TTL expiry after 3 h).
- `StaleNeverClearsIT`: NWS and ProbSevere feeds failing produce no tombstones and no exit alerts, and the source shows `degraded`.

## Acceptance criteria

- All four sources ingest their fixtures with the stated time bases and kinds; budgets cover the request arithmetic.
- No tombstone is ever emitted from a failed, truncated or older response.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 129, 132, 133, 134).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 134).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `047: Severe Weather Record Sources`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/047-CR-Severe-Weather-Record-Sources.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `047: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
