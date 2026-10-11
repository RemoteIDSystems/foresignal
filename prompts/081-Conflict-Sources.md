# 081 — Conflict Sources

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/081-CR-Conflict-Sources.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–133).
- **Previous Completion Report:** read `completion-reports/080-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Source-Catalog.md` rows `deepstate-frontline`, `alerts-in-ua`, `ua-air-raid-siren`, `acled`, `ucdp-ged`, and "Excluded" (OREF, LiveUAMap, Telegram)
- `docs/Gap-Analysis.md` §9 ("re-serving OpenSky/ACLED/aisstream data to peers or sinks" rejected on licence)
- `docs/Core-Data-Model.md` §2, §3 (adapter rules 7–13, limits), §5.3
- Prompt 072 `AbsenceTombstoner`; prompt 073 `FeatureGeometry`; prompt 035 anonymous profile and feed keys; prompt 109 (licence and egress policy, later) and prompts 110/113 (sinks, federation)
- Upstream docs: `github.com/cyterat/deepstate-map-data` (README), alerts.in.ua developer docs (`devs.alerts.in.ua`), `github.com/PaulAnnekov/uasiren` (siren.pp.ua), ACLED API guides (`acleddata.com/api-documentation/getting-started`) and Terms of Use, UCDP API docs (`ucdp.uu.se/apidocs/`)

## Objective

Add conflict and civil-alert sources: the daily DeepState occupied-area polygon, Ukrainian air-raid and threat alerts (keyed alerts.in.ua, plus the keyless siren.pp.ua wrapper), ACLED events, and UCDP GED candidate events. ACLED forbids redistribution of raw data: mark it with the prompt 062 `egress` field now so feed keys, sinks, share links and federation never re-serve it; prompt 109 later adds the structured licence terms.

## Tasks

### 1. Egress and licence hooks

- Use the descriptor field `egress` (`OPEN | LOCAL_ONLY`) and `EgressPolicy` added by prompt 062; check they exist and add them as specified there if missing (including `SourceDescriptor.egress` in `docs/OpenAPI.json` and Postman regeneration). `LOCAL_ONLY` data is served to this instance's own authenticated API clients and notifications, but never re-served by sinks (110), federation (113), share links or feed keys (112) or off-box exports.
- Prompt 109 later sets structured `licenceTerms` for every source; its binding list already has `acled` as `redistribution=FORBIDDEN`, `commercialUse=FORBIDDEN`. Here, set the free-text `licence`/`termsUrl`/`attribution` precisely so 109's audit resolves them, and record the terms findings below in the Completion Report.

### 2. Ukraine boundaries (`src/main/resources/geo/ukr/`)

- Bundle geoBoundaries `UKR` ADM1 (oblasts, incl. Kyiv city and Crimea/Sevastopol) and ADM2 (raions), CC BY 4.0, simplified to ≤ 1,500 vertices per polygon, ≤ 2 MB total; record version and attribution in a `NOTICE` and `THIRD_PARTY_NOTICES.md`.
- `UaRegionIndex`: lookup by alerts.in.ua `location_uid` / `location_oblast_uid` and by siren.pp.ua `regionId`. Build both crosswalk tables (`alerts-in-ua-uids.json`, `siren-region-ids.json`) during fixture capture from the upstream region lists and names; unmatched ids log once and yield null geometry.

### 3. `alerts-in-ua` (`ai.foresignal.source.adapters.alertsinua`)

- `GET https://api.alerts.in.ua/v1/alerts/active.json` with required credential `token`, sent as `Authorization: Bearer` (never as a query param). Verified 2026-10 (developer docs): soft limit 8–10 requests/min per IP, hard limit 12/min (429; systematic violation blocks the IP and token); use `If-Modified-Since` with `Last-Modified` (conditional GET); the token must stay server-side (Foresignal proxies; never expose it to clients).
- Fields: `id, location_title, location_title_en, location_type` (`oblast|raion|city|hromada|unknown`), `started_at, finished_at, updated_at, alert_type` (`air_raid|artillery_shelling|urban_fights|chemical|nuclear`), `location_uid, location_oblast, location_oblast_uid, location_raion, notes, alert_level` (`red|yellow`), `threats[{threat_type, level, started_at, source_message}]`.
- Record: `kind = air-raid-alert` for `air_raid`, else `threat-alert`; `externalId = id`; geometry = oblast or raion polygon from `UaRegionIndex` (city/hromada → parent raion with payload `geometryApproximate: true`); payload `location, locationEn, locationType, oblast, raion, alertType, alertLevel, threatTypes` (sorted list), `notes` (≤ 1 KB, untrusted); `observedAt = updated_at`, `eventTime = started_at`. Drop `source_message` text from threats (unverified relays).
- End of alert: an alert absent from the active list is tombstoned by `AbsenceTombstoner` (two consecutive complete fetches, ≤ 60 s at the default interval); a record that arrives with `finished_at` set is tombstoned at once.
- Descriptor: interval 30 s (min 20 s), budget 3/min, 4,320/day, STANDARD, ALL, no TTL, `zeroResultsNormal=true`, `titleField=locationEn`, tile `alertType, alertLevel, location`. Licence: no published terms (docs include a no-liability disclaimer and say the data is not for critical infrastructure): `licence = "unspecified (alerts.in.ua)"`, `egress=OPEN`, attribution "alerts.in.ua". Put the disclaimer in `description`.

### 4. `ua-air-raid-siren` (`ai.foresignal.source.adapters.uasiren`)

- `GET https://siren.pp.ua/api/v3/alerts` (verified 2026-10: live data, ETag, 38 regions with alerts; types `AIR`, `ARTILLERY`, `URBAN_FIGHTS`; fields `regionId, regionType` (`State|District|Community`), `regionName, regionEngName, lastUpdate, activeAlerts[{regionId, regionType, type, lastUpdate, activeAlertLevels[{alertLevel, reason, createdAt}]}]`).
- Terms check (verified 2026-10): siren.pp.ua is a public wrapper around `api.ukrainealarm.com` run by an individual (`PaulAnnekov/uasiren`, last code push 2022); the README states a limit of 50 requests/min per IP and no licence or terms. Ship it disabled, `licence = "unspecified (siren.pp.ua wrapper of ukrainealarm.com)"`, and say in `description` that `alerts-in-ua` is the preferred keyed source and that the official `api.ukrainealarm.com` needs an application key.
- Record: one per region and alert type: `externalId = regionId + ":" + type`; `kind = air-raid-alert` for `AIR`, else `threat-alert`; geometry from `UaRegionIndex`; payload `region, regionEn, regionType, alertType, alertLevel` (highest of `activeAlertLevels`); `observedAt = lastUpdate` (of the alert), `eventTime` = earliest `createdAt`. Absence → tombstone (two misses). Some regions show alerts unchanged since 2022 (occupied areas): normal, not stale data.
- Interval 30 s (min 15 s), budget 4/min, 4,000/day, conditional GET.

### 5. `deepstate-frontline` (`ai.foresignal.source.adapters.deepstate`)

- Daily file from the GitHub mirror: `https://raw.githubusercontent.com/cyterat/deepstate-map-data/{ref}/data/deepstatemap_data_{yyyyMMdd}.geojson`. Verified 2026-10: one Feature, empty properties, MultiPolygon of about 2,900 vertices (77 KB), `crs` CRS84, ETag and `Cache-Control: max-age=300`; the README says files update daily at about 03:00 UTC.
- Fetch today's UTC date; on 404 try yesterday; conditional GET per file name. Params: `ref` (default `main`; may be a 40-hex commit SHA to **pin** the data) and `fileDate` (required when `ref` is a SHA, since a pinned commit has a fixed newest file). With a pinned ref, fetch once and return not-modified afterwards.
- Record: `kind = frontline`, single `externalId = occupied-area`; geometry as published (simplify only above 9,500 vertices); payload `mapDate, areaKm2` (geodesic area), `areaChangeKm2` (vs the previous map, kept in state), `ref`; `observedAt = eventTime = mapDate` at 03:00Z (UPSTREAM; fixed). Each daily map with changed geometry is a new version, which is the point: asOf and `/track`-style history show the front over time.
- Descriptor: interval 6 h (min 1 h), budget 1/min, 20/day, STANDARD, ALL, no TTL. Licence: the mirror repository is GPL-3.0 (Foresignal downloads data only, no code); DeepStateMap itself publishes no data terms → `licence = "GPL-3.0 mirror; DeepStateMap terms unspecified"`, `egress=OPEN`, attribution "DeepStateMap.Live via cyterat/deepstate-map-data".

### 6. `acled` (`ai.foresignal.source.adapters.acled`)

- Auth (verified 2026-10, ACLED getting-started guide): required credentials `email`, `password` (encrypted at rest). `POST https://acleddata.com/oauth/token` (form: `username`, `password`, `grant_type=password`, `client_id=acled`, `scope=authenticated`) → access token (24 h) and refresh token (14 d). Keep both in memory only; refresh with `grant_type=refresh_token`; on restart log in again. Token requests count against the budget. An unauthenticated `api/acled/read` returns 403 `Access denied`.
- Data: `GET https://acleddata.com/api/acled/read?_format=json&limit=5000&page={n}&timestamp={cursor}&timestamp_where=>=&fields=event_id_cnty|event_date|timestamp|disorder_type|event_type|sub_event_type|actor1|actor2|interaction|civilian_targeting|country|iso|admin1|admin2|location|latitude|longitude|geo_precision|time_precision|fatalities|source|notes` plus `iso={codes}` from param `countries` (ISO numeric list, required, ≤ 30). First run: `event_date` window of `lookbackDays` (default 30). Deletions: `GET /api/deleted/read?timestamp={cursor}` → tombstones by `event_id_cnty`. Advance the cursor (max `timestamp` seen) only after all pages succeed. Check field and filter names against the "Elements of ACLED's API" guide during capture.
- Record: `kind = conflict-event`, `externalId = event_id_cnty`, point; `quality.accuracyMeters` from `geo_precision` (1 → 2 km, 2 → 25 km, 3 → 100 km); payload `eventType, subEventType, disorderType, actor1, actor2, interaction, civilianTargeting, country, admin1, admin2, location, fatalities, sources, notes` (≤ 2 KB, `embedFields`, untrusted), `geoPrecision, timePrecision`; `observedAt = timestamp` (epoch seconds, last upstream change), `eventTime = event_date`.
- Access tiers: what an account receives (event-level rows, recency) depends on its ACLED access level; surface 403 after a successful login as health reason "account access level does not allow this query".
- Descriptor: interval 6 h (min 1 h), budget 2/min, 200/day, STANDARD, ALL, no TTL. `licence = "ACLED Terms of Use: no redistribution of raw data"`, `egress=LOCAL_ONLY`, attribution "Armed Conflict Location & Event Data (ACLED), acleddata.com".

### 7. `ucdp-ged` (`ai.foresignal.source.adapters.ucdp`)

- Required credential `token`, header `x-ucdp-access-token` (verified 2026-10: 401 "API token required. Add header: x-ucdp-access-token" without it; tokens are issued on request by the API maintainer). Limit 5,000 requests/day, errors included → budget 2/min, 300/day.
- Version discovery (daily, cached in state): the docs name annual GED `26.1` and monthly candidate releases `YY.0.M` (e.g. `26.0.8`). Probe `GET https://ucdpapi.pcr.uu.se/api/gedevents/{YY}.0.{M}?pagesize=1` from the current month backwards (at most 14 probes, crossing into the previous year); the first version with `TotalCount > 0` is current. Param `version` overrides discovery.
- Fetch the current candidate with `StartDate={today − lookbackDays}` (default 120) and `EndDate=today` (filters on `date_end`), `pagesize=1000`, iterating `page` up to `TotalPages` (out-of-range pages error, so never guess).
- Merge: records keep the same `externalId = id` across candidate versions, so a revised event becomes a new version; events missing from a newer candidate **inside the queried window** are tombstoned (`AbsenceTombstoner` scoped to ids whose `date_end` lies in the window); `datasetVersion` is a volatile payload field. Annual GED backfill is out of scope (param `includeAnnual` reserved, default false).
- Record: `kind = conflict-event`, point, `quality.accuracyMeters` from `where_prec` (1 → 1 km … 7 → null geometry for country-level events, use country centroid with `geometryApproximate`); payload `conflictName, dyadName, sideA, sideB, violenceType` (1 state-based, 2 non-state, 3 one-sided → text), `country, region, adm1, adm2, deathsBest, deathsLow, deathsHigh, deathsCivilians, sourceCount, sourceHeadline` (≤ 1 KB, untrusted), `datePrecision, wherePrecision, datasetVersion`; `eventTime = date_start`, `observedAtMode=RECEIVED` (no per-event update stamp).
- Descriptor: interval 6 h, STANDARD, ALL, no TTL. Licence CC BY 4.0 (UCDP Download Center; confirm during capture), `egress=OPEN`, attribution "UCDP Georeferenced Event Dataset (candidate), Uppsala Conflict Data Program".

### 8. Fixtures, docs

- Real captures (alerts.in.ua and UCDP with your own tokens, ACLED with your account, all secrets stripped; siren.pp.ua; one DeepState daily file plus the next day's to exercise `areaChangeKm2`). Edge cases: alert with `finished_at`, unknown `location_uid`, UCDP `where_prec = 7`, ACLED deleted list, DeepState 404 → yesterday.
- `docs/Source-Catalog.md`: rows for all five sources with licence, egress, key/terms findings (siren.pp.ua wrapper status, DeepState mirror licence, ACLED restriction, UCDP token).
- No API change (the `egress` property comes from prompt 062).

## Tests to write (do not run)

- `UaRegionIndexTest` (both crosswalks, approximate fallback), `AlertsInUaParserTest`, `UaSirenParserTest`, `DeepStateParserTest` (area and change), `AcledParserTest` (precision → accuracy, deleted list), `UcdpParserTest` (violence types, country-level events): real fixtures, `SnapshotContract.assertStableStaticHash`, fieldSpecs contract.
- `UcdpVersionDiscoveryTest` (WireMock): month walk-back across the year boundary, `version` override, `TotalPages` iteration.
- `AcledAuthTest` (WireMock): password grant, refresh before expiry, re-login after restart, tokens never in traces or state.
- Adapter tests: alerts.in.ua Bearer header (redacted) and `If-Modified-Since`, 429 → backoff; DeepState pinned SHA fetched once.
- `ConflictEgressTest`: `acled` is `LOCAL_ONLY` and `EgressPolicy.allows("acled", <sink/federation/feed-key paths>)` is false; the other four sources are `OPEN`.
- `ConflictIngestIT` (`@IntegrationTest`): an alert absent twice is tombstoned; a new DeepState map creates version 2 of `occupied-area`; a UCDP event missing from a newer candidate inside the window is tombstoned, one outside the window is kept.

## Acceptance criteria

- `deepstate-frontline`, `alerts-in-ua`, `ua-air-raid-siren`, `acled` and `ucdp-ged` register, disabled; the keyed sources require their credentials before enable.
- `acled` has `egress=LOCAL_ONLY`, visible in `/v1/sources`.
- No adapter scrapes HTML or uses OREF/LiveUAMap/Telegram.
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
2. **Commit** all changes with the message `081: Conflict Sources`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/081-CR-Conflict-Sources.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `081: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
