# 095 — Citizen Science And Signals

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/095-CR-Citizen-Science-And-Signals.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–138).
- **Previous Completion Report:** read `completion-reports/094-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §4b row "Health and nature (CDC NWSS, eBird, iNaturalist), Wikipedia edit surge"
- `docs/Core-Data-Model.md` §2 (revised events: `eventTime` = occurrence, `observedAt` = upstream `updated`; RECEIVED when there is no upstream update time), §3 (adapter rules 7–13, `Quality.accuracyMeters`), §7 (streaming sinks)
- Completion Reports for 040 (gazetteer: US county and state centroids, timezones), 039 (private flag, precision cap), 036 (places as AOIs), 035 (outbound URL policy)
- Upstream docs: iNaturalist API `https://api.inaturalist.org/v1/docs/` (limits and "not for scraping"), v2 `https://api.inaturalist.org/v2/docs/`; eBird API 2.0 `https://documenter.getpostman.com/view/664302/S1ENwy59` and terms `https://www.birds.cornell.edu/home/ebird-api-terms-of-use/`; CDC NWSS datasets on `data.cdc.gov` (SODA); Wikimedia EventStreams `https://wikitech.wikimedia.org/wiki/Event_Platform/EventStreams_HTTP_Service` and the Robot policy

## Objective

Add four signal sources that complement sensors and official feeds: iNaturalist observations and eBird notable sightings for an operator's areas, CDC wastewater surveillance levels and detections, and a Wikipedia edit-surge detector over Wikimedia EventStreams. They contain contributor-generated data, so each adapter minimises personal information, keeps per-record licences, and treats all text as untrusted.

## Tasks

### 1. `inat-observations`

- `GET https://api.inaturalist.org/v1/observations?swlat&swlng&nelat&nelng&quality_grade={q}&order_by=id&order=asc&id_above={cursor}&per_page=200` for new observations, plus one `&updated_since={lastRun}&order_by=updated_at` request per run for revisions (quality-grade changes, re-identifications). Params: `aoi` (bbox or place, **required**; no global polling), `qualityGrade` (default `research`), `iconicTaxa` (string-list, optional), `licensedOnly` (default true: skip `license_code` null, which means all rights reserved), `includeCaptive` (default false). At most 5 pages per run.
- Limits: the API allows 100/min and asks for ≤ 60/min and < 10,000/day, and is "intended to support application development, not data scraping" (verified 2026-10). Descriptor: serial requests, interval 30 min (min 10 min), budget 2/min and 500/day; honour `Cache-Control: max-age=300`.
- Kind `nature-observation`, `externalId = inat:{id}`, STANDARD, ALL, no TTL, `observedAtMode=UPSTREAM` with `observedAt = updated_at`, `eventTime = time_observed_at ?? observed_on` (00:00 local → UTC via the 040 timezone of the point).
- Geometry: `geojson` point. Skip records with `geoprivacy` or `taxon_geoprivacy` = `private` or without coordinates. When `obscured=true`, set `quality.accuracyMeters` from `public_positional_accuracy` and payload `obscured: true` (coordinates are already randomised upstream; never try to refine them).
- Payload: `taxonId, taxonName, commonName, iconicTaxon, qualityGrade, licenseCode, obscured, uri`, `photoUrl` only when the first photo's licence is open and its host is `inaturalist-open-data` (otherwise omitted). **Never store** `user`, `user.login`, `description`, or comments.
- `licence = "per-record CC licence (iNaturalist; see licenseCode)"`, attribution "iNaturalist contributors". `searchFields = [taxonName, commonName]`.
- v2 exists and is preferred upstream (needs explicit `fields=`, stricter validation); v1 has no retirement date. Implement v1 with a `ApiVersion` seam and a docs note.

### 2. `ebird-notable` (disabled by default)

- `GET https://api.ebird.org/v2/data/obs/geo/recent/notable?lat={lat:.2f}&lng={lng:.2f}&dist={km}&back={days}&detail=simple`, header `x-ebirdapitoken` (credential `apiKey`, one per eBird account). Params: `lat`, `lng` (or a 036 place), `distKm` (1–50, default 25), `backDays` (1–30, default 3), `includeRecent` (default false: also call `/data/obs/geo/recent`, which returns only the latest observation per species).
- Interval 1 h (min 30 min), budget 2/min and 100/day; no published limit, and eBird may suspend access for "adverse impact".
- Kind `bird-sighting`, `externalId = ebird:{subId}:{speciesCode}`, `observedAtMode=RECEIVED` (no upstream update time). `obsDt` is local time with no zone: keep it as `obsDtLocal` and set `eventTime` using the 040 timezone at the point.
- Payload `speciesCode, comName, sciName, howMany` (omit when absent), `obsValid, obsReviewed, locId`; `locName` only when `locationPrivate=false`. For `locationPrivate=true` (often a home), round coordinates to 0.05° and set `quality.accuracyMeters = 5000`.
- Sensitive species are withheld upstream. `licence = "eBird API Terms of Use (non-commercial, attribution)"`, attribution "eBird.org (Cornell Lab of Ornithology)"; egress local-only for 109/110.

### 3. CDC NWSS wastewater

Verified 2026-10: the older datasets `2ew6-ywp6` and `g653-rqe2` were **archived on 2025-09-12** (with `ptc_15d`, `detect_prop_15d`, `percentile`). Current datasets update weekly on Fridays.

- Shared `SodaClient` helper: `GET https://data.cdc.gov/resource/{id}.json?$where=date_updated > '{cursor}'&$order=date_updated ASC&$limit=5000&$offset=n`, optional `X-App-Token` (credential `appToken`, recommended; anonymous requests share an IP pool and get 429s). Cursor = max `date_updated` in adapter state.
- **`cdc-nwss-wval`** (`atcp-73re`): site viral activity levels for SARS-CoV-2, Influenza A and RSV. Kind `wastewater-level`, `externalId = {site}:{pathogen_target}`, payload `pathogen, wval` (1 decimal), `category` (`site_wval_category`), `weekEnd, state, countiesServed, populationServed, source`. `eventTime = week_end`, `observedAt = date_updated`.
- **`cdc-nwss-detections`**: param `pathogens` (default `h5, measles, mpox`) mapping to `mtpu-urpp`, `akvg-8vrb`, `xpxn-rzgz` (`ymmh-divb` Influenza A, `45cq-cw4i` RSV and `j9g8-acpt` SARS-CoV-2 selectable). Kind `wastewater-detection`, `externalId = {site}:{pcr_target}`: the latest sample per site and target, payload `detected` (`pcr_target_detect`), `concentration` (`pcr_target_avg_conc_lin`, unit `pcr_target_units`), `sampleCollectDate, countyFips, populationServed, source`. `eventTime = sample_collect_date`. A detection flip is significant (rules: `detected == true` enter).
- Geometry: no site coordinates exist. Use the 040 county centroid for `county_fips` (detections) or for the first county in `counties_served` plus state (levels), else the state centroid; payload `geometryApproximate: true`.
- **Licence:** "Public Domain U.S. Government", **except rows with `source = WastewaterSCAN`, which are CC BY-NC 4.0** (Stanford). Param `includeWastewaterScan` (default false); when true, those records carry `licenceOverride: "CC BY-NC 4.0 (WastewaterSCAN)"` and 109 must honour it.
- Interval 12 h (min 6 h), budget 4/min and 100/day.

### 4. `wiki-edit-surge` (streaming, disabled by default)

- SSE `GET https://stream.wikimedia.org/v2/stream/recentchange` with our descriptive UA (an empty UA gets 403). Resume with `Last-Event-ID` = the last event `id` (a JSON array; store it verbatim in adapter state). The server closes every connection after about 15 min: reconnect immediately without counting a failure. On errors use 1 s → 5 s → 30 s → 120 s with jitter and honour 429/`Retry-After`. One connection only.
- Filter: `meta.domain != "canary"`, `type ∈ {edit, new}`, `namespace == 0`, `bot == false`, `wiki ∈ wikis` (param, default `enwiki, dewiki, frwiki, eswiki, ruwiki, ukwiki, arwiki, zhwiki`).
- `SurgeDetector` (pure): per `(wiki, title)`, sliding 15-min window of edit timestamps and an exact set of hashed editor names (SHA-256 truncated, held in memory only, never stored). Surge when edits ≥ `minEdits` (default 15) **and** distinct editors ≥ `minEditors` (default 5), or a `new` page reaches 10 edits by 5 editors within 30 min. Ends after 60 min below threshold → tombstone. State cap 200,000 pages (evict least recent; count evictions).
- Kind `wiki-edit-surge`, `externalId = {wiki}:{title}`, STANDARD, ALL, `observedAtMode=UPSTREAM` (newest `meta.dt`). Push to the sink only when the banded state changes (edits in bands of 5, editors in bands of 2, `phase` rising/peak/fading) so steady surges become touches. Payload `wiki, title, url` (`title_url`), `edits15m, editors15m, firstEditAt, isNewPage, phase`. **Never store** `user`, `comment` or `parsedcomment`.
- Geotag: on a new surge, one budgeted request `GET {server_url}/w/api.php?action=query&prop=coordinates&titles={title}&format=json&formatversion=2` (through the outbound URL policy, host allowlisted to `*.wikipedia.org`; cached 24 h). Coordinates → Point; otherwise null geometry.
- Bandwidth: the full recentchange stream is a continuous firehose; measure it in the IT harness and document the expected daily volume in the Operator Guide. Count bytes through `StreamContext` metering; set `mbPerDay` so the budget reflects reality.
- `licence = "Wikimedia metadata (titles CC BY-SA 4.0)"`, attribution "Wikimedia Foundation EventStreams". `embedFields = [title]` (untrusted text).

### 5. Spec, Postman, docs

- No new operations. Document the new kinds through `fieldSpecs`; regenerate Postman with `python3 scripts/generate-postman.py docs/OpenAPI.json postman` only if examples change.
- `docs/Source-Catalog.md` rows for all five sources, including the NWSS dataset change, the WastewaterSCAN licence split, iNaturalist v2 and the "not for scraping" note, eBird non-commercial terms, and EventStreams resume behaviour. `docs/Operator-Guide.md`: privacy choices (no usernames, obscured and private locations), AOI requirement, bandwidth of the surge detector, rule recipes (wastewater `detected == true` enter; `wiki-edit-surge` with `phase == rising` enter and a 6 h cooldown).

## Tests to write (do not run)

- Parser contract tests with trimmed real fixtures (README with URL and date): `InatParserTest` (obscured accuracy, private geoprivacy skipped, null licence skipped, photo host rule, no user fields), `EbirdParserTest` (local time to UTC, private-location rounding, absent `howMany`), `NwssWvalParserTest`, `NwssDetectionParserTest` (WastewaterSCAN filtered or overridden, county centroid fallback), `RecentChangeEventTest` (canary dropped, bot filtered, `id` array preserved).
- `SurgeDetectorTest`: thresholds at the boundaries, editor hashing, new-page rule, banding produces no update for steady counts, tombstone after 60 min, eviction cap.
- WireMock adapter tests: iNaturalist `id_above` cursor and `updated_since` pass, ≤ 5 pages; eBird header and 403 handling; SODA paging with the `date_updated` cursor and app token; Wikipedia geotag request goes through the URL policy.
- `WikiStreamAdapterTest` against an embedded SSE server: `Last-Event-ID` sent on reconnect, server close after 15 min reconnects without a failure count, 429 honoured.
- `SignalsIngestIT` (`@IntegrationTest`): an iNaturalist quality-grade change creates a SIGNIFICANT version; an unchanged NWSS re-poll writes nothing; no stored payload contains a username.

## Acceptance criteria

- Every source registers with STANDARD/ALL defaults that pass `IngestPolicyValidator`; `ebird-notable` and `wiki-edit-surge` are disabled by default; `inat-observations` refuses to enable without an AOI.
- No payload stores contributor usernames, edit comments or eBird private location names.
- NWSS uses the current dataset ids, and WastewaterSCAN rows are excluded or carry the CC BY-NC override.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 138, 141, 142, 143).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 143).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `095: Citizen Science And Signals`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/095-CR-Citizen-Science-And-Signals.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `095: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
