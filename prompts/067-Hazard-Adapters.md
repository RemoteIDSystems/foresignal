# 067 — Hazard Adapters

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/067-CR-Hazard-Adapters.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.2 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, and `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump.
- **Previous Completion Report:** read `completion-reports/066-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Core-Data-Model.md` §2 (rows "Revised events" and "NWS": `eventTime` = onset, `observedAt` = sent/updated, explicit `validUntil = expires`), §3 (rule 9: `validUntil` only for an upstream-asserted expiry)
- `docs/Gap-Analysis.md` §4b (tsunami row: "feed holds only the latest message; close on Cancel, not on absence"), §8.C item 31
- `docs/Source-Catalog.md` rows `nhc-cyclones` and `gdacs` (keep Green alerts)
- Completion Reports of 008 (`validFrom` on `NormalizedRecord`), 024 (`cap` IngestFormat) and 047 (`nws-alerts`)
- GEV (MIT, portable): `server/providers/cyclones.js` (`parseCycloneStatus` validation: id pattern, sanitised strings, advisory number, ISO times)
- Upstream:
  - NHC JSON reference `https://www.nhc.noaa.gov/productexamples/NHC_Tropical_Cyclone_Status_JSON_File_Reference.pdf`
  - GDACS Swagger `https://www.gdacs.org/gdacsapi/swagger/v1/swagger.json` and terms `https://www.gdacs.org/documents/2025/GDACS_Terms_of_use_Mar_25.pdf`
  - tsunami.gov product retrieval `https://www.tsunami.gov/?page=productRetrieval`
  - OASIS CAP 1.2 `https://docs.oasis-open.org/emergency/cap/v1.2/CAP-v1.2.html`

## Objective

Add three hazard sources: `nhc-cyclones`, `gdacs` and `tsunami-warnings`. Build a shared, hardened **CAP 1.2 parser and record mapper** that 024's `cap` ingest format and 089's CAP family reuse. Establish the alert time mapping (`eventTime`, `validFrom`, `validUntil`) so forecast-aware rules (017) work across every alert source.

## Tasks

### 1. Shared CAP 1.2 (`ai.elaro.source.formats.cap`)

**`CapParser.parse(InputStream, CapParseOptions) → CapAlert`:**
- StAX with DTDs and external entities disabled and entity expansion off; 2 MB input cap.
- Namespace `urn:oasis:names:tc:emergency:cap:1.2`. Accept 1.1 with `capVersion` recorded; reject anything else with `UpstreamFormatException`.
- Model, as immutable records:
  - `identifier, sender, sent, status, msgType, source, scope, restriction, addresses`
  - `code[], note, references[]`: parse the space-separated `sender,identifier,sent` triples
  - `incidents`
  - `info[]`: `language, category[], event, responseType[], urgency, severity, certainty, audience, eventCode[], effective, onset, expires, senderName, headline, description, instruction, web, contact, parameter[], resource[], area[]`
  - `area`: `areaDesc, polygon[], circle[], geocode[], altitude, ceiling`
- Times are parsed as `OffsetDateTime`. A missing required element (`identifier`, `sender`, `sent`, `status`, `msgType`, `scope`) throws `UpstreamFormatException`.

**`CapGeometry`:**
- Polygons are `lat,lon` pairs separated by spaces. Swap to lon/lat, close open rings, and build a MultiPolygon.
- A circle `lat,lon radiusKm` becomes a 64-segment geodesic polygon (radius 0 → Point).
- Geocode-only areas give null geometry, and the geocodes go in the payload. 089 resolves boundaries.
- Hand the result to the normalizer's `GeometryFixer`.

**`CapThreadResolver`** gives one Elaro record per alert thread:
- `threadKey` = the source's custom key when supplied (tsunami uses incidents; see §4). Otherwise the earliest referenced `{sender}:{identifier}`, looked up through a bounded `identifier → threadKey` map held in adapter state (TTL 14 d, ≤ 20,000 entries). Otherwise the message's own `{sender}:{identifier}`.

**`CapRecordMapper.toRecord(CapAlert, CapMappingProfile) → NormalizedRecord`** (profile: kind, language preference, parameter allowlist, payload text caps):
- `externalId = threadKey`.
- `observedAt = sent`. `eventTime = onset` when present. **`validFrom = onset ?? effective`** (null when neither). **`validUntil = expires`** (explicit and upstream-asserted).
- `state`:
  - `cancelled` when `msgType = Cancel` or the profile's cancellation predicate matches
  - `updated` when `msgType = Update`
  - otherwise `active`
- A cancellation is **a version, not a tombstone**: `state: cancelled`, `validUntil = info.expires ?? sent`. The record then expires naturally, and a rule on `state` sees the transition as SIGNIFICANT.
- **Absence from a feed never closes an alert.**
- Payload:
  - `identifier, sender, senderName, sent, msgType, status, scope, state, event, category, urgency, severity, certainty`
  - `headline`, `description` and `instruction`, each ≤ 4 KB
  - `web, areaDesc, geocodes` (`{valueName: [values]}`), `parameters` (allowlisted names only), `onset, effective, expires, language, references` (count)
- `embedFields` for CAP sources: `headline, description, instruction`. These are untrusted text under 028's rules.
- Multiple `info` blocks: choose by the profile's language list, else the first.

**Reuse:**
- Refactor 024's `cap` IngestFormat to delegate to `CapParser` and `CapRecordMapper`, keeping its API and tests green.
- Check 047's `nws-alerts`: if it does not set `validFrom = onset ?? effective`, add that there; it is a GeoJSON source, so map the fields directly.

**`validFrom` hook:** if 008 did not add `validFrom` to `NormalizedRecord`, the head and the hash inputs, add it as 008 specifies: nullable, hashed like `validUntil`.

### 2. `nhc-cyclones` (`ai.elaro.source.adapters.nhc`)

- `getConditional("https://www.nhc.noaa.gov/CurrentStorms.json")`. Verified 2026-10: CloudFront sends ETag, Last-Modified and `max-age=300`.
- Interval 10 min (min 5 min); budget 2/min, 300/day. `zeroResultsNormal` **true** (out of season). STANDARD, ALL, no TTL. Licence `US-PD`.
- **Parser** (port `parseCycloneStatus`):
  - The top level is `{activeStorms: [...]}` with at most 32 entries.
  - `id` must match `^(al|ep|cp)\d{6}$`; strings are sanitised.
  - `intensity` and `pressure` arrive **as strings** (verified 2026-10; the 2019 spec says numeric), so parse defensively. Keys are camelCase `latitudeNumeric` / `longitudeNumeric`.
  - Any product object may be null.
- **Record:**
  - kind `cyclone`, `externalId = id`, `observedAt = lastUpdate`, geometry `Point(longitudeNumeric, latitudeNumeric)`
  - payload: `name, classification, classificationLabel` (TD/STD/TS/HU/STS/PTC/TY/PC → words), `basin, binNumber, intensityKt, pressureMb, movementDirDeg`
  - `movementSpeedKt`: NHC gives **mph**; convert and keep 1 dp
  - `advisoryNumber, advisoryIssued` (`forecastAdvisory.advNum` / `issuance`)
  - `publicAdvisoryUrl, forecastAdvisoryUrl`: only `https://www.nhc.noaa.gov/` URLs; drop others
- **Tombstones:**
  - Adapter state `active` holds the ids from the last successful response.
  - An id missing from a later successful response gets an explicit tombstone: the storm is no longer in NHC's active set, and the file is the complete set.
  - A 304 changes nothing.
- **Coverage note:** Central Pacific is unverified. The file repackages the AL and EP feeds, and `binNumber` is defined only as AT/EP. Accept `cp` ids when they appear, and document that CPHC storms may be missing (CPHC RSS `https://www.nhc.noaa.gov/index-cp.xml` is not ingested here).
- Cone, track and forecast points are 075's job; do not parse the GIS links.

### 3. `gdacs` (`ai.elaro.source.adapters.gdacs`)

- Verified 2026-10: `GET https://www.gdacs.org/gdacsapi/api/events/geteventlist/MAP?eventtype={T}`, where `eventtype` is **required** and takes a single value (a bare or comma-separated request returns 400).
- Param `eventTypes` (string-list, allowed `EQ, TC, FL, VO, DR, WF`, default all). One request per type, **sequentially**.
- Interval 15 min (min 10 min); budget 6/min, 700/day. STANDARD, ALL. `recordTtl` 48 h (anchor RECEIVED): each poll touches listed events, and an event missing from the list for 48 h expires.
- **Keep Green alerts** (World Monitor drops them; Elaro lets triggers filter).
- **Record:**
  - kind `disaster`, `externalId = {eventtype}:{eventid}` (episodes update one record)
  - revised-event time base: `eventTime = fromdate`, `observedAt = datemodified`
  - geometry from the feature (Point)
  - payload: `eventType, eventId, episodeId, name, description, alertLevel, alertScore, episodeAlertLevel, severity, severityText, severityUnit, country, iso3, affectedCountries` (iso3 list), `fromDate, toDate, isCurrent, isTemporary, glide, reportUrl` (`url.report`), `automated: true`
- `embedFields = [name, description]`; `tileProperties alertLevel, eventType, alertScore`; `rankField alertScore`.
- Licence: "GDACS terms of use (disclaimer; no explicit licence)"; `termsUrl` the March 2025 PDF. Its disclaimer applies: alerts are automatic, unreviewed, and no substitute for official warnings. Put it in the descriptor description and in the Agent Guide.

### 4. `tsunami-warnings` (`ai.elaro.source.adapters.tsunami`)

- Verified 2026-10: each feed holds **only the latest message**.
  - CAP 1.2: `https://www.tsunami.gov/events/xml/PAAQCAP.xml` (NTWC) and `https://www.tsunami.gov/events/xml/PHEBCAP.xml` (PTWC).
  - Atom equivalents: `PAAQAtom.xml` / `PHEBAtom.xml`, whose entry `link rel="related" type="application/cap+xml"` points at a per-message CAP.
- Fetch both CAP URLs sequentially with `getConditional`.
  - If the captured fixture shows the CAP URL returns something other than a single `<alert>`, fall back to the Atom feed and follow the related link.
  - Accept only `https://www.tsunami.gov/events/(PAAQ|PHEB)/…/*CAP.xml` paths; never follow other hosts.
- Interval 2 min (min 1 min); budget 4/min, 3000/day. `zeroResultsNormal` true. STANDARD, ALL, no TTL (expiry is CAP `expires`). Licence `US-PD`.
- **Mapping profile:**
  - kind `tsunami-warning`
  - thread key `{center}:{incidents}` (center = PAAQ or PHEB), falling back to the event id suffix of `identifier` (`PAAQ-<seq>-<eventId>`), then the resolver
  - parameter allowlist `EventPreliminaryMagnitude, EventOriginTime, EventLatLon, VTEC, NWSUGC`
  - payload `messageType` (`Warning|Advisory|Watch|Information Statement|Threat`, from `event`), `center`, `epicenter` `{lat, lon}` from `EventLatLon`
  - geometry from area circles, else the epicentre point
- **Cancellation (verified 2026-10):** tsunami cancellations arrive as `msgType=Update` with `event="Tsunami Cancellation"`, urgency Past, certainty Unlikely. The predicate is `msgType == Cancel` OR `event` matches `(?i)cancel` OR the VTEC action is `CAN`. A cancellation produces a `state: cancelled` version.
- **Never close on absence.** A feed that moves on to an unrelated event leaves the earlier thread to expire at its own `expires`.

### 5. Fixtures

Capture each with a README (date, URL):
- `CurrentStorms.json` (or a 2025 in-season copy from the NHC examples page) plus an empty `{activeStorms: []}`; hand-make `string-numbers.json` and `null-products.json`.
- GDACS MAP responses for EQ and TC (trimmed to 10 features), and `400-no-eventtype.json`.
- PAAQ and PHEB CAP and Atom documents, plus a hand-made cancellation thread: Warning → Update → "Tsunami Cancellation".
- CAP conformance set:
  - polygon, circle and geocode-only alerts
  - multi-language info blocks
  - `references` chain
  - `msgType=Cancel` with no info
  - an XXE attempt
  - a CAP 1.1 alert
  - a 3 MB oversize alert

### 6. Catalogue and docs

- `docs/Source-Catalog.md`:
  - `nhc-cyclones`: conditional GET, mph conversion, string numerics, tombstone-on-absence from the complete set, CP caveat.
  - `gdacs`: single `eventtype` per call, TTL 48 h, Green kept, terms.
  - New `tsunami-warnings` row.
- `docs/Adapter-Guide.md`: "Alert sources: `eventTime`/`validFrom`/`validUntil` mapping, threads, cancellation as a version, never close on absence", pointing at `formats.cap`.
- OpenAPI: no operation changes. Confirm `validFrom` is on `Record`/`Change` (from 008/014); add it if missing and regenerate Postman with `python3 scripts/generate-postman.py docs/OpenAPI.json postman`.
- `THIRD_PARTY_NOTICES.md`: GEV entry for the cyclone validation port.

## Tests to write (do not run)

- `CapParserTest`: the conformance set; references parsing; XXE rejected; size cap; 1.1 accepted with the version recorded; missing required element rejected.
- `CapGeometryTest`: lat/lon swap, ring closure, circle polygon area within 1% of πr², radius 0 → Point, geocode-only → null.
- `CapRecordMapperTest`:
  - `validFrom` from onset, then effective, then null
  - `validUntil` from expires
  - Cancel → `state: cancelled` version with `validUntil = sent` when no info
  - language selection, text caps, parameter allowlist
- `CapThreadResolverTest`: chain via references, custom key wins, eviction.
- `NhcCurrentStormsParserTest`: fixtures; mph→kt; string numerics; invalid id rejected; null products; non-NHC URLs dropped.
- `NhcAdapterTest` (WireMock): 304 → notModified; an absent storm → tombstone; empty list → zero records, healthy (`zeroResultsNormal`).
- `GdacsAdapterTest` (WireMock): one request per configured type in order; 400 surfaced; Green kept; `externalId` per event across episodes.
- `TsunamiAdapterTest` (WireMock): two CAP fetches; Atom fallback; the host/path allowlist on related links; the cancellation thread yields three versions of one record ending `cancelled`; an unrelated next event does not close the first.
- `HazardIngestIT` (`@IntegrationTest`):
  - the tsunami thread persists FIRST → SIGNIFICANT → SIGNIFICANT(`cancelled`) and expires at the cancellation's `expires`
  - the cyclone tombstone appears as a change
  - `validFrom` is stored on the head and returned by `/v1/records`
- `CapIngestFormatTest` (024, updated): the format still passes after delegating to the shared parser.

## Acceptance criteria

- One CAP parser in the codebase. 024 uses it, and 089 is documented to.
- No alert source closes an alert because it is missing from a feed (NHC is the documented exception: its file is the complete active set).
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 118, 119, 120).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 120).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `067: Hazard Adapters`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/067-CR-Hazard-Adapters.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `067: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
