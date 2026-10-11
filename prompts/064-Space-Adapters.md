# 064 — Space Adapters

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/064-CR-Space-Adapters.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–134).
- **Previous Completion Report:** read `completion-reports/063-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Core-Data-Model.md` §2 (rows "Revised events" and "Catalogue and reference"), §3 (adapter rules), §5.3 ("Everything else": ALL, STANDARD)
- `docs/Source-Catalog.md` rows `celestrak-gp` and `launch-library`
- `docs/Gap-Analysis.md` §2 (SatDump/Gpredict/Look4Sat row), §3 ("CelesTrak compat"), §5 ("Satellite passes + pass triggers"), §8.C item 30
- `docs/Weather-Imagery-Design.md` §3 (Orekit row), §4.4 (pass lifecycle, `satKey`, externalId `{station}:{satKey}:{aosEpochMinute}`, `predicted` versions), §7.1 (derived-record principles)
- Completion Reports of 049 (SatDump station source), 052 (DERIVED runtime), 054 (Orekit and `orekit-data`), 035 (feed keys), 036 (places)
- GEV (MIT, portable): `server/providers/space/celestrak.js` (descriptive UA, cache), `src/data/spaceProviderRequests.js`, `server/providers/space/launch-library.js` (`Authorization: Token`)
- Upstream: `https://celestrak.org/NORAD/documentation/gp-data-formats.php`, `https://celestrak.org/NORAD/elements/usage-policy.php`, `https://celestrak.org/SOCRATES/socrates-format.php`, `https://thespacedevs.com/llapi`, `https://www.space-track.org/documentation`

## Objective

Add the space sources: `celestrak-gp` (GP element sets), `celestrak-socrates` (conjunctions), `launch-library` (launches) and optional `spacetrack-tip` (re-entry predictions). Add Orekit-based predicted `satellite-pass` records for places and SatDump stations, and a CelesTrak-compatible `gp.php` endpoint so Gpredict, Look4Sat and SatDump can read elements from Foresignal instead of each hitting CelesTrak. CelesTrak's 2026 usage policy is strict, so etiquette is a hard requirement here.

## Tasks

### 1. `celestrak-gp` (`ai.foresignal.source.adapters.celestrak`)

**Verified 2026-10 facts (usage policy of 2026-05-22):**
- GP data is checked only every 2 h, and one download per update is enforced. A repeat gets **HTTP 403 with body "GP data has not updated since your last successful download"**.
- More than 50 HTTP errors (301/403/404) in 2 h firewalls the IP. More than 100 MB/day risks a block.
- Machine clients must **stop on any other non-200 and alert a human**.
- **The default `FORMAT` became CSV on 2026-05-09.**
- **5-digit catalogue numbers ran out on 2026-07-11.** Objects ≥ 100000 have no TLE output (CelesTrak does not use alpha-5), so TLE is not a viable ingest format.
- ETag and Last-Modified support is undocumented.

**Descriptor:**
- Kind `satellite`; params:
  - `group` (string, default `active`)
  - `memberGroups` (string-list; default `stations, visual, weather, amateur, gnss, geo, science, cubesat, last-30-days`; each must match `^[a-z0-9-]+$` and be in a reviewed allowlist copied from the CelesTrak group list)
  - `format` (`csv` default, `json`)
- `minInterval` = `defaultInterval` = 2 h. Budget 3/min, 48/day. `maxResponseBytes` 32 MB. STANDARD, policy ALL.
- `recordTtl` 3 days, `ttlAnchor` RECEIVED, so an object missing from `active` for 3 days expires.
- `observedAtMode` UPSTREAM with `observedAt = EPOCH`, the element epoch (a revised-element pattern: an older epoch is LATE and dropped). `eventTime` null. No geometry; clients propagate with SGP4.
- `titleField` `OBJECT_NAME`; search `OBJECT_NAME, OBJECT_ID`; `zeroResultsNormal` false. Licence "CelesTrak (data from 18th SDS via Space-Track)"; terms unverified.

**Fetch, strictly sequential:**
1. `getConditional("https://celestrak.org/NORAD/elements/gp.php?GROUP={group}&FORMAT={format}")`, so validators are sent if CelesTrak ever adds ETag/Last-Modified (undocumented today). One request per group per run.
2. Then at most **two** member groups whose membership is older than 24 h, oldest first. In a steady state each member group is fetched once a day.
3. Store memberships in adapter state as `members:{group}` = a sorted, delta-encoded NORAD list, plus `membersAt:{group}`.

**Status handling (new `UpstreamHaltException` hook in the SPI; `FetchExecutor` maps it):**

| Response | Result |
|---|---|
| 200 | Parse. |
| 403 whose body contains "has not updated since your last successful download" | `FetchResult.unchanged()`. Not an error and not a backoff. |
| Any other non-200, a redirect, or a non-CSV/JSON 200 body | Throw `UpstreamHaltException`. `BackoffRegistry` holds the source for 24 h. Health is `failing` with "CelesTrak returned {status}; automatic retries stopped per CelesTrak usage policy — investigate, then use fetch-now". Publish a system notification (018 persisted system events). Fetch-now clears the hold. |

**Parsing:**
- `CelestrakGpParser.parseCsv` (RFC 4180, header-driven) and `parseJson` produce the same records.
- The 17 OMM columns: `OBJECT_NAME, OBJECT_ID, EPOCH` (no zone, so UTC), `MEAN_MOTION, ECCENTRICITY, INCLINATION, RA_OF_ASC_NODE, ARG_OF_PERICENTER, MEAN_ANOMALY, EPHEMERIS_TYPE, CLASSIFICATION_TYPE, NORAD_CAT_ID, ELEMENT_SET_NO, REV_AT_EPOCH, BSTAR, MEAN_MOTION_DOT, MEAN_MOTION_DDOT`.
- Keep the OMM key names verbatim in the payload, with numbers as JSON numbers, plus `groups` (sorted list from memberships). `externalId` = `NORAD_CAT_ID` (up to 9 digits; never assume 5).
- A 200 with zero objects for `active` throws `UpstreamFormatException`. Velocity once saw `FORMAT=json` return 0 satellites, which is why CSV is the default.
- Do not re-request the same group in the same window to "try another format": that earns a 403.

**Bytes:** `active` as CSV is about 3 MB × 12 runs, well under 100 MB/day. Log the daily bytes from `UsageMeter`, and make health `degraded` above 60 MB/day.

### 2. `celestrak-socrates`

- GET `https://celestrak.org/SOCRATES/sort-minRange.csv`, RFC 4180 with a header, more than 10 MB, so stream-parse it. Interval 12 h (SOCRATES runs every 10–11 h), budget 1/min, 4/day. `maxResponseBytes` 64 MB. Use `getConditional`; a 304 is notModified.
- Columns: `NORAD_CAT_ID_1, OBJECT_NAME_1, DSE_1, NORAD_CAT_ID_2, OBJECT_NAME_2, DSE_2, TCA, TCA_RANGE` (km), `TCA_RELATIVE_SPEED` (km/s), `MAX_PROB, DILUTION`.
- Params filter rows, keeping a row if any one matches: `minProb` (number, default 1e-5), `maxRangeKm` (number, default 1.0), `watchNorad` (string-list, rows involving these are always kept). Cap at 2,000 records per run.
- Kind `conjunction`. `externalId` = `{lowNorad}-{highNorad}-{TCA epoch minute}`. `eventTime = TCA`. `observedAtMode` RECEIVED (the CSV carries no run time). STANDARD, ALL, `recordTtl` 12 h, anchor RECEIVED. No geometry.
- Payload: the 11 columns renamed to lowerCamel (`norad1, name1, daysSinceEpoch1, …, tcaRangeKm, relSpeedKms, maxProb, dilutionKm`).
- Halt rules as in §1; the same CelesTrak error budget applies.

### 3. `launch-library`

- GET `https://ll.thespacedevs.com/2.3.0/launches/?net__gte={now−7d}&net__lte={now+60d}&limit=100&mode=normal&ordering=net`, following `next` for at most 3 pages. Verified 2026-10: 2.3.0 is current (2.4.0 returns 500), and the anonymous limit is 15 requests/hour.
- Interval 15 min (min 15 min anonymous; 5 min with a token). Budget 3/min, 300/day.
- Optional credential `token`, sent as `Authorization: Token <token>`. Without it the request is anonymous.
- On 429, honour `Retry-After`, else back off 30 min.
- Kind `launch`, `externalId` = LL2 `id`. Revised-event time base: `eventTime = net`, `observedAt = last_updated`. Geometry: pad `Point(longitude, latitude)` (strings in LL2; parse them).
- Payload: `name, status` (abbrev), `net, netPrecision, windowStart, windowEnd, provider, rocket, missionName, missionType, orbit, padName, location, webcastLive, probability, missionDescription`.
- `embedFields = [missionDescription]` (upstream text, untrusted for LLM use per 028 rules). `titleField name`, `rankField probability`. STANDARD, ALL, no TTL.

### 4. `spacetrack-tip` (optional, disabled by default)

- Credentials `identity` and `password`, both required.
- **Login:** POST form to `https://www.space-track.org/ajaxauth/login`. Read `Set-Cookie` from the response headers and send it back as a `Cookie` header (`SourceHttp` has no cookie jar; keep the session in memory only). Re-login once on 401. Bad credentials throw `CredentialsRejectedException`.
- **Query:** GET `https://www.space-track.org/basicspacedata/query/class/tip/INSERT_EPOCH/%3Enow-0.042/format/json`, hourly. Verified 2026-10: TIP is limited to 1/hour, or every 10 min during a re-entry within 12 h; overall limits are 30/min and 300/h. Interval 1 h, budget 2/min, 60/day.
- **Records:** kind `reentry-prediction`, `externalId = NORAD_CAT_ID`, `observedAt = MSG_EPOCH`, `eventTime = DECAY_EPOCH`, payload `window` (minutes).
  - Fields confirmed: `NORAD_CAT_ID, MSG_EPOCH, DECAY_EPOCH, WINDOW`. `LAT, LON, INCL, DIRECTION, REV, HIGH_INTEREST, NEXT_REPORT` are unverified: parse them when present and confirm them from the captured fixture.
  - Geometry `Point(LON, LAT)` when present, with payload `geometryNote: "predicted position at 10 km altitude, not an impact point"`.
- **Licence:** Space-Track User Agreement, `egress` **LOCAL_ONLY**. Data may not be transferred to other entities without approval, and blanket approval for TIP is not confirmed. The Operator Guide says one account per person or entity, and never shares credentials.

### 5. Predicted passes (Orekit)

- **Dependency hook:** confirm 054 added `org.orekit:orekit` (latest verified 13.1.9, Apache-2.0) and the pinned minimal `orekit-data`. Add them if missing, with a startup self-test that propagates a bundled ISS element set and checks a known pass from a fixture. Orekit's documentation site refused automated fetches, so verify these locally:
  - SGP4 needs UTC-TAI.
  - TEME→ITRF uses EOP; if EOP is absent, configure Orekit's no-EOP fallback and accept the sub-kilometre error.
  - 6-digit NORAD numbers must be accepted when you build a `TLE` object from OMM numbers through the field constructor. No text TLE is involved.
- **`ai.foresignal.space.PassPredictor`:** `predict(OmmElements e, Observer(lat, lon, altM), Instant from, Duration horizon, double minElevationDeg) → List<PredictedPass(aos, tca, los, maxElevationDeg, aosAzimuthDeg, losAzimuthDeg, groundTrack)>`.
  - Use `TLEPropagator` with an `ElevationDetector` on a `TopocentricFrame` (WGS84 `OneAxisEllipsoid`), a 60 s max check and a 1 ms threshold.
  - The ground track is ≤ 60 sub-points from AOS to LOS.
  - Pure, apart from Orekit data loading.
- **DERIVED source `satellite-passes`** (052 runtime):
  - params `norad` (string-list, default `25544, 57166, 59051`: ISS, METEOR-M2 3, METEOR-M2 4), `places` (string-list of place ids, default all point places), `minElevationDeg` 10, `horizon` 48 h, `maxPassesPerPlace` 100
  - recompute on a 30 min tick and when `celestrak-gp` changes a watched NORAD id; read elements from `record_head` of `celestrak-gp`
  - kind `satellite-pass`, `externalId` `place:{placeId}:{norad}:{aosEpochMinute}`, `eventTime = aos`, `observedAtMode` RECEIVED, STANDARD, no TTL, `kindRetention satellite-pass` 14 days
  - payload: `satellite, norad, place, status: "predicted", aos, tca, los, maxElevationDeg` (0.1°), `aosAzimuthDeg, losAzimuthDeg`, with times rounded to 10 s so element refreshes do not churn versions; geometry is the ground track
  - **tombstone** at LOS + 10 min, and when a recomputation no longer finds the pass; reconcile on startup (§7.1)
  - private places (039) produce private records
- **SatDump stations (049 hook):** if the station source exists, it calls `PassPredictor` for its station location and configured satellites. It emits `status: "predicted"` versions under the 049 externalId `{station}:{satKey}:{aosEpochMinute}`.
  - When an RF session or `dataset.json` mints a pass, reuse the externalId of a predicted pass with the same `satKey` whose AOS is within ±5 min, so `receiving`/`recorded`/`decoded` become later versions of the same record.
  - A predicted pass with no session by LOS + 20 min becomes `missed` if the station was healthy during the window; otherwise tombstone it.
- **Trigger recipe** (Agent-Guide): "pass in 10 minutes" uses 017's `forecastHorizon` on `eventTime` with `kind=satellite-pass`, `maxElevationDeg > 30`.

### 6. CelesTrak-compatible output

- `GET /compat/celestrak/gp.php` and the alias `/compat/celestrak/NORAD/elements/gp.php`, served from `celestrak-gp` heads, never by calling CelesTrak.
- Query keys are uppercase, as at CelesTrak: `GROUP` (membership, or `active` = all non-expired heads), `CATNR`, `INTDES`, `NAME` (case-insensitive substring). Exactly one is required; otherwise 400 as plain text. `SPECIAL` returns 400 "not supported".
- **`FORMAT`:** `TLE`/`3LE` (name line + 2 lines), `2LE`, `JSON`, `JSON-PRETTY`, `CSV`, `XML` (CCSDS OMM XML), `KVN`. **An absent or unknown FORMAT returns CSV**, matching CelesTrak since 2026-05-09.
- TLE output generates lines with correct column layout and mod-10 checksums from OMM numbers. It **omits objects ≥ 100000** (no alpha-5, as CelesTrak).
- An empty result returns the text `No GP data found`, as plain text with status 200.
- Headers: `ETag` = hash of (source max seq, query); `Cache-Control: max-age=3600`; content types `text/plain`, `application/json`, `text/csv`, `application/xml`.
- **Access:** `read` scope. Add an explicit AccessPolicy rule for `/compat/**`, because the fail-closed default covers only `/v1/**`; the rule must not leave it open. Accept 035 path-bound feed keys (`?key=`), since Gpredict and Look4Sat cannot send headers.
- **Client notes (Operator Guide):**
  - Gpredict uses `GROUP=…&FORMAT=tle`; Look4Sat uses `GROUP=active&FORMAT=csv`.
  - SatDump defaults to orbicentral (`kepler_settings.urls_to_fetch` = `http://orbicentral.net/gp.php?GROUP=active&FORMAT=omm`, verified 2026-10). Point it at Foresignal with `FORMAT=json` and confirm with SatDump's parser. `FORMAT=omm` falls to CSV here; a SatDump-specific OMM feed is 107's job.

### 7. Spec, catalogue, docs

- `docs/OpenAPI.json`:
  - Add the two `/compat/celestrak/...` operations (`x-foresignal-scope: read`, with parameters, the response media types above, 400 text).
  - Add `satellite`, `conjunction`, `launch`, `reentry-prediction` and `satellite-pass` payload examples to the kind docs.
  - Regenerate Postman with `python3 scripts/generate-postman.py docs/OpenAPI.json postman`.
  - MCP (025): no new tool; `query_records` with `kind=satellite-pass` covers it. Document this in the Agent Guide.
- `docs/Source-Catalog.md`:
  - Rewrite `celestrak-gp` with the verified policy: CSV default, 6-digit numbers, the halt rule, 100 MB/day, member groups. The old "fall back to TLE" note is wrong now.
  - Add rows for `celestrak-socrates`, `spacetrack-tip` and `satellite-passes`.
  - Update `launch-library` (15/h anonymous, `Token` header).
- `THIRD_PARTY_NOTICES.md`: GEV entries (CelesTrak UA practice, LL2 headers), Orekit (Apache-2.0) if not already listed.

## Tests to write (do not run)

- `CelestrakGpParserTest`:
  - CSV and JSON fixtures produce identical records
  - 6-digit NORAD
  - EPOCH without a zone
  - zero objects for `active` → `UpstreamFormatException`
- `CelestrakGpAdapterTest` (WireMock):
  - the "not updated" 403 body → notModified, no backoff
  - other 403/404/301 → `UpstreamHaltException` with a 24 h hold and no retry until fetch-now
  - at most two member groups per run; membership merged into `groups`
  - requests are serial (`verify` order)
- `SocratesParserTest`: header-driven columns, filters, the 2,000 cap, a streaming parse of a 12 MB synthetic file within the heap budget.
- `LaunchLibraryAdapterTest`: pagination capped at 3, Token header only with a credential, 429 backoff, pad geometry from strings.
- `SpaceTrackTipAdapterTest`: login cookie reused, re-login on 401, bad credentials → `CredentialsRejectedException`, password redacted in traces.
- `PassPredictorTest`: ISS fixture passes against a reference table (AOS/LOS within 5 s, max elevation within 0.2°), no passes below the minimum elevation, a 6-digit NORAD element set.
- `SatellitePassesDerivedIT`: passes emitted for a place, unchanged on an element refresh within rounding, tombstoned after LOS.
- `StationPredictedPassIT` (when 049 is present): a predicted pass is followed by `receiving` on the same externalId.
- `CelestrakCompatControllerIT`:
  - every FORMAT; unknown FORMAT → CSV
  - TLE checksums and column positions match a CelesTrak fixture; ≥ 100000 omitted from TLE
  - `No GP data found`
  - 400s; feed-key access; denied without read
- `SpaceIngestIT`: re-fetching identical elements → touches, no versions; a new EPOCH → version 2.

## Acceptance criteria

- The CelesTrak sources never send more than one request per group per 2 h window, and stop on unexpected statuses.
- `/compat/celestrak/gp.php?GROUP=stations&FORMAT=tle` returns valid TLEs that Orekit re-parses.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 134, 137, 138, 139).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 139).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `064: Space Adapters`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/064-CR-Space-Adapters.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `064: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
