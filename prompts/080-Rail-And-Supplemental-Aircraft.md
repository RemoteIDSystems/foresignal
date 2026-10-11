# 080 — Rail And Supplemental Aircraft

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/080-CR-Rail-And-Supplemental-Aircraft.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–126).
- **Previous Completion Report:** read `completion-reports/079-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Source-Catalog.md` rows `amtraker`, `digitraffic-rail`, `airplaneslive-mil`, `adsbfi-region-*`, and the `adsblol-*` notes (no UA spoofing, 451 handling)
- `docs/Core-Data-Model.md` §2 (readsb row: `observed_at = now − seen`, geometry `lat/lon` else `lastPosition`, `quality.extra["adsb.seen_pos"]`), §3, §5.1 (keyframe constraint), §5.3 rows "Global aircraft" and "Transit and rail" plus the aircraft significant fields; *Per-prompt changes* "029 / 030 / 036 / 037 / 044 / 048" (044 = this prompt)
- Prompt 062 (`adsblol-*`: readsb snapshot parser, `navDegraded`, altitude convention) and prompt 069 (readsb v2 fields); prompt 044 (TRACK downsampling)
- Upstream docs: Amtraker (`github.com/piemadd/amtrak`), Digitraffic rail (`www.digitraffic.fi/en/railway-traffic/`), airplanes.live API guide (`airplanes.live/api-guide`), adsb.fi open data (`github.com/adsbfi/opendata`)

## Objective

Add North American and Finnish train positions and supplementary military/regional aircraft feeds on the TRACK tier with the §5.3 SAMPLED policies, reusing the readsb snapshot parser from prompt 062. Every source here is a community or rate-limited service: requests are strictly serial per host, identifiable, and never work around a block.

## Tasks

### 1. Shared pieces

- **readsb mapper:** use prompt 062's shared `ai.foresignal.source.formats.readsb` (`ReadsbDocument.parse`, `ReadsbAircraftMapper`, `AircraftDescriptors.global`). It already implements the §2 mapping (`observedAt = now − seen`, geometry from `lat/lon` else `lastPosition`, `quality.extra["adsb.seen_pos"]`), metre altitudes with datum labels, `posSource`/`quality.positionSource` (MLAT and TIS-B as CALCULATED), `navDegraded`, the excluded counters and `fieldSpecs`. Confirm it accepts the adsb.fi and airplanes.live envelopes (`ac[]`, `now` in ms); extend it there if not. Do not fork it.
- **Host pacing:** add `HostPacer` in `ai.foresignal.collect` (if prompt 062 has not added an equivalent): a per-host gate allowing one in-flight request and a minimum spacing between request starts. Descriptor field `hostPacing` (Duration, default null). `UpstreamClient` waits on the gate before the budget check (bounded wait 30 s, then `InBackoffException`-style skip with health reason "host pacing"). All `adsbfi-*` sources share host `opendata.adsb.fi` with spacing 1.1 s; airplanes.live 1.1 s.
- **Policy constraint check:** §5.1 requires `keyframe ≤ ttl − pollInterval − 30 s`. `AircraftDescriptors.global` sets TTL 6 min and keyframe 240 s (prompt 062's recorded deviation), so aircraft polls up to 90 s pass; the rail sources set their own keyframes. The registry validator must accept every descriptor in this prompt.

### 2. Aircraft sources (`ai.foresignal.source.adapters.airplaneslive`, `ai.foresignal.source.adapters.adsbfi`)

Common descriptor: `AircraftDescriptors.global(builder)` from prompt 062 (TRACK, TTL 6 min OBSERVED, SAMPLED `minInterval` 60 s, `keyframe` 240 s, 1,000 m dead-reckoned deviation, thresholds `alt_baro` 300 m and `track` 20°, `edgeMinInterval` 30 s, the §5.3 aircraft significant fields, motion on `gs`/`track`, `titleField = flight`), interval 30 s (min 30 s), `accessDeniedIsTerminal=true` (prompt 062's 403/451 rule: hourly retry, health `failing` with the refusal reason). All disabled by default.

- **`airplaneslive-mil`:** `GET https://api.airplanes.live/v2/mil`. Verified 2026-10: anonymous requests now get **HTTP 403** with `{"error": "Please contact us at contact@airplanes.live. Your email MUST include any links, a description of the project, …"}`. Implement the adapter, but:
  - with `accessDeniedIsTerminal=true` the 403 becomes `UpstreamAccessDeniedException` (hourly retry); extend the health reason for this source to "access approval required (contact airplanes.live)" by matching the body;
  - if airplanes.live grants access through a key or header, add it as an optional credential following their instructions (adapt to reality and record it in the Completion Report); never retry with other headers.
  - Terms: non-commercial, no SLA, attribution "airplanes.live"; 1 request per second → `hostPacing` 1.1 s, budget 2/min, 2,880/day.
- **`adsbfi-mil`** (new, keyless alternative verified 2026-10): `GET https://opendata.adsb.fi/api/v2/mil` (readsb `ac[]` with `dbFlags`, `mlat[]`, `tisb[]`). Same descriptor and pacing. The shared mapper sets `posSource=mlat` and `quality.positionSource=CALCULATED` for MLAT positions; prompt 109 makes such records LOCAL_ONLY per record.
- **`adsbfi-region-1` … `adsbfi-region-4`:** operator-configured slots on the fixed host. `GET https://opendata.adsb.fi/api/v3/lat/{lat}/lon/{lon}/dist/{nm}`; params `lat`, `lon` (required), `distNm` (integer, default 150, max 250, the documented maximum), `label`. Records carry the same `externalId = hex` across regions; overlap between slots is resolved by derived-layer fusion (prompt 099), not here.
- adsb.fi terms (verified 2026-10): public endpoints 1 request/second; repeated 4xx can cause a temporary IP restriction; data for personal, non-commercial use; credit adsb.fi with a link. Put these in `licence`/`termsUrl`/`attribution`, treat 429 as throttling (prompt 005 backoff) and 403/451 through `accessDeniedIsTerminal`; never rotate anything. Licence slug for prompt 109: non-commercial, attribution required.

### 3. `digitraffic-rail` (`ai.foresignal.source.adapters.digitraffic.rail`)

- `GET https://rata.digitraffic.fi/api/v1/train-locations/latest` with headers `Digitraffic-User: Foresignal/<version>` and gzip (verified 2026-10: 7.5 KB, `Cache-Control: max-age=1`, fields `trainNumber, departureDate, timestamp, location{type: Point, coordinates}, speed` (km/h), `accuracy, isGpsLocation`).
- Record: `kind = train`, `externalId = departureDate + ":" + trainNumber`, point, payload `trainNumber, departureDate, speedKph, gps` (bool), `accuracyM` (declare `accuracyM` volatile); `observedAt = timestamp`. Optional enrichment (param `enrich`, default true): once per train per day `GET /api/v1/trains/{departureDate}/{trainNumber}` → `trainType, trainCategory, operatorShortCode, commuterLineID` cached in state (max 200 lookups/day).
- Descriptor (§5.3 "Transit and rail"): TRACK, TTL 5 min OBSERVED, SAMPLED `minInterval` 15 s, `keyframe` 240 s, deviation 150 m, threshold `bearing` 30° (compute `bearingDeg` from successive fixes in the adapter only if upstream lacks a heading; Digitraffic has none, so skip the threshold and rely on deviation), `edgeMinInterval` 8 s, `significantFields = trainNumber, departureDate, trainType, operatorShortCode`, `motion {speedField: speedKph, speedUnit: km/h}`. Interval 30 s (min 15 s), budget 4/min, 4,000/day. Licence CC BY 4.0, attribution "Fintraffic / digitraffic.fi". Note the MQTT feed (`rata-mqtt.digitraffic.fi`, topic `train-locations/#`) as a later streaming option.

### 4. `amtraker` (`ai.foresignal.source.adapters.amtraker`)

- `GET https://api.amtraker.com/v3/trains` (verified 2026-10: 105 KB gzip, object keyed by train number → array of trains; 186 active trains from providers `Amtrak`, `Via`, `Brightline`). Fields used: `trainID` (`1-10`), `trainNum, routeName, provider, providerShort, lat, lon, heading` (compass text), `velocity` (mph), `trainState, trainTimely, eventCode, origCode, destCode, lastValTS, updatedAt`. Drop `stations[]`, colours, time-zone fields and `alerts[]` text.
- Record: `kind = train`, `externalId = trainID`, point; payload `trainNumber, route, provider, state, timeliness, nextStationCode` (`eventCode`), `originCode, destinationCode, speedKph` (mph × 1.609), `headingDeg` (from compass text); `observedAt = lastValTS` (time of the last valid position; fixed).
- Descriptor: TRACK, TTL 5 min OBSERVED, SAMPLED `minInterval` 15 s, deviation 150 m, threshold `headingDeg` 30°, `edgeMinInterval` 8 s, `significantFields = trainNumber, route, provider, state, destinationCode`, interval **60 s** (min 60 s; a community service, be gentle), therefore `keyframe` **180 s** (`≤ 300 − 60 − 30`), budget 2/min, 1,500/day, `mbPerDay` 250.
- Terms: Amtraker is an unofficial community relay of Amtrak, VIA Rail and Brightline map data; it publishes no API terms or rate limit (verified 2026-10). Set `licence = "unspecified (community API)"`, `termsUrl = null`, state this in `description`, keep it disabled by default, and note in the Source Catalog that the source is dropped if Amtraker or the operators object.

### 5. Fixtures, docs

- Real captures: `adsbfi-mil` and one `adsbfi` v3 region (≤ 25 aircraft), the airplanes.live 403 body, Digitraffic rail latest (+ one train lookup), Amtraker trimmed to 10 trains across all providers. Edge cases: aircraft with only `lastPosition`, `seen_pos` large, non-ICAO `~` hex, train without position.
- `docs/Source-Catalog.md`: update rows (tier, policy, TTL, keyframes, time base, licences, the airplanes.live access requirement, the new `adsbfi-mil`, Amtraker's terms status).
- No API change.

## Tests to write (do not run)

- `ReadsbEnvelopeTest`: prompt 062's mapper parses the adsb.fi v2/v3 and airplanes.live fixtures (`ac[]`, ms `now`) without changes to its existing tests.
- `HostPacerTest`: single in-flight per host, spacing honoured across sources, bounded wait.
- `AirplanesLiveAdapterTest` (WireMock): the 403 approval body raises `UpstreamAccessDeniedException`, health shows the approval reason, retries are hourly; User-Agent is Foresignal's.
- `AdsbfiAdaptersTest` (WireMock): region URL from params, `distNm` > 250 rejected by `ParamValidator`, serial requests across `adsbfi-*` sources.
- `DigitrafficRailParserTest`, `AmtrakerParserTest` (unit conversions, dropped fields, provider coverage): real fixtures, `SnapshotContract.assertStableStaticHash`, fieldSpecs contract.
- `RailAndAircraftDescriptorTest`: every descriptor passes `IngestPolicyValidator` (keyframe constraint) and registry TRACK rules.
- `RailIngestIT` (`@IntegrationTest`): a train moving < 150 m within `minInterval` creates no version; a 30° heading change creates a SAMPLE version; positions older than the head are LATE.

## Acceptance criteria

- `airplaneslive-mil`, `adsbfi-mil`, `adsbfi-region-1…4`, `digitraffic-rail` and `amtraker` register on the TRACK tier, disabled, passing the §5.1 validator.
- No request in this prompt sets a non-Foresignal User-Agent, and requests to one host never overlap.
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
2. **Commit** all changes with the message `080: Rail And Supplemental Aircraft`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/080-CR-Rail-And-Supplemental-Aircraft.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `080: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
