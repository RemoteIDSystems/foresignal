# 062 — Aircraft Adapters

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/062-CR-Aircraft-Adapters.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/design-history/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/design-history/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/061-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/design-history/Core-Data-Model.md` §2 (adapter mapping rows "readsb snapshots" and "OpenSky states"), §3 (adapter rules 7–13, geometry carry-forward), §5.1 (keyframe constraint), §5.3 (global aircraft row and aircraft significant fields)
- `docs/Source-Catalog.md` rows `adsblol-mil`, `adsblol-region-*`, `opensky-states`, and the Velocity design notes (credit headers, non-JSON 200 = throttling)
- `docs/Adapter-Guide.md` (from 011): package layout, altitude convention, `fieldSpecs` contract test, fixture procedure
- `docs/design-history/Gap-Analysis.md` §9 (re-serving OpenSky data to peers or sinks is rejected)
- GEV (MIT, portable): `server/providers/aircraft/adsb-lol.js` (Retry-After clamp 5–120 s), `src/data/adsbLolFallback.js` (readsb field mapping, `now` in s or ms), `server/providers/aircraft/opensky.js` (OAuth token cache, credit governor, `x-rate-limit-remaining` tiers)
- Upstream: adsb.lol OpenAPI `https://api.adsb.lol/api/openapi.json` and `https://github.com/adsblol/api`; readsb field reference `https://github.com/wiedehopf/readsb/blob/dev/README-json.md`; OpenSky REST `https://openskynetwork.github.io/opensky-api/rest.html`

## Objective

Add the global aircraft sources: `adsblol-mil` (military worldwide), an optional `adsblol-area-*` family of operator-centred circles, and `opensky-states` (worldwide civil, credit-governed). They are the first TRACK sources on the v1.2 pipeline, so they must declare the §5.3 SAMPLED defaults, the §2 time bases and a shared readsb mapper that 069 and 080 reuse.

**Decision (verified 2026-10):** there is no global adsb.lol source. The adsb.lol API declares `radius ≤ 250` nm on `/v2/point/{lat}/{lon}/{radius}` and clamps it in code (`min(radius, 250)`), and `/v2/all` / `/v2/all-with-pos` do not exist. The Velocity report that `/v2/point/0/0/20000` returns ~12k aircraft is contradicted by the published code. Worldwide civil coverage comes from `opensky-states`; adsb.lol provides military worldwide plus up to three ≤ 250 nm areas. During fixture capture, call `/v2/point/0/0/20000` once and record the status (expect 422 or a clamped 250 nm result) in the Completion Report. Do not add a global source even if it happens to work: it is undocumented.

## Tasks

### 1. Shared readsb mapper (`ai.foresignal.source.formats.readsb`)

Shared format code lives under `ai.foresignal.source.formats.*` (not under `adapters`), so adapters may import it without breaking the no-cross-adapter rule. Document this in the Adapter Guide.

- `ReadsbAircraftMapper.map(JsonNode ac, Instant upstreamNow, MapperOptions opts) → Optional<NormalizedRecord>`; `ReadsbDocument.parse(byte[])` reads the top level (`ac` for adsb.lol, `aircraft` for local readsb; `now` as seconds, or milliseconds when > 1e11; `total`, `ctime`).
- `externalId` = `hex` lower-cased, keeping a leading `~` (non-ICAO address). Skip entries without `hex`. Kind `aircraft`.
- **Time base (CDM §2, binding):** `observedAt = upstreamNow − seen`, always. `eventTime` null. `quality.extra["adsb.seen_pos"] = seen_pos` when present.
- **Geometry:** `Point(lon, lat)` when `lat`/`lon` exist; else `lastPosition.{lat,lon}` and `quality.extra["adsb.last_position"] = true`; else null (TRACK carry-forward applies). Never put altitude in Z.
- **Payload allowlist** (omit absent keys, no nulls): `hex, flight` (trimmed), `r, t, desc, squawk, emergency` (omit when `none`), `category, onGround` (`alt_baro == "ground"`), `alt_baro` and `alt_geom` in **metres** (feet × 0.3048, rounded to 1 m; `alt_baro` omitted on ground), `gs` (knots, 0.1), `track` (degrees, 0.1), `baro_rate` (m/s, from ft/min), `posSource` (`adsb|adsr|mlat|tisb|modes|other` from `type` and the `mlat`/`tisb` arrays), `nac_p, nic`, `navDegraded` (`nac_p < 8 || nic < 7`, only when either is present), `mil`, `pia`, `ladd` (from `dbFlags` bits 1, 4, 8; omit when false).
- Excluded: `messages, rssi, seen, seen_pos, rc, version, sil, gva, sda, nav_*` (raw counters and query-relative fields, rule 8).
- `quality.positionSource`: REPORTED for adsb/adsr, CALCULATED for mlat and tisb, UNKNOWN otherwise.
- `fieldSpecs` (008 descriptor extra) for every payload key, with UCUM units (`m`, `[kn_i]`, `deg`, `m/s`) and the datum labels "pressure altitude (1013.25 hPa)" and "WGS84 ellipsoid".
- Thresholds: CDM §5.3 states `alt_baro 1,000` in feet; with metre payloads declare `alt_baro` 300 m and `track` 20° (angular). Motion: `{speedField: gs, speedUnit: KNOTS, courseField: track}`.
- Shared descriptor helper `AircraftDescriptors.global(builder)` applying: TRACK, `ttlAnchor` OBSERVED, `observedAtMode` UPSTREAM, `recordTtl` 6 min (the §5.3 5 min fails `keyframe ≤ ttl − pollInterval − 30 s` at a 60 s poll; 6 min allows polls up to 90 s; record the deviation), policy `SAMPLED{minInterval 60s, keyframe 240s, minDeviationMeters 1000, thresholds true, edgeMinInterval 30s}`, `significantFields = [flight, r, t, squawk, category, emergency, onGround, navDegraded]`, `tileProperties = [flight, r, t, alt_baro, gs, track, squawk, mil]`, `searchFields = [flight, r, t, hex]`, `titleField = flight` (fallback `r`, then `hex`), `licence`, `termsUrl`.

### 2. `adsblol-mil` (`ai.foresignal.source.adapters.adsblol`)

- GET `https://api.adsb.lol/v2/mil`. Interval 30 s (min 15 s), budget 4/min, 3000/day (no published numbers; limits are dynamic, so stay at this rate). Attribution "adsb.lol", licence `ODbL-1.0`, `termsUrl` `https://github.com/adsblol/api`.
- Keyframe check: `240 ≤ 300 − 30 − 30` holds at the default interval. The validator rejects an interval above 30 s at PUT unless the operator also shortens the keyframe; state this in the descriptor description.
- `complete = true`. Do not tombstone absent aircraft: TTL expiry handles disappearance.
- An HTTP 200 whose content type is not JSON, or whose body does not start with `{`, throws `UpstreamFormatException` ("non-JSON 200, treated as throttling").

### 3. `adsblol-area-*` family

- `AdsbLolAreaFamily implements SourceAdapterFamily` returns three slots, `adsblol-area-1..3`, all disabled by default. Params: `lat` (number, −90..90, required), `lon` (number, −180..180, required), `radiusNm` (integer 1–250, default 250). Values are validated numbers, never URLs. The request is `https://api.adsb.lol/v2/point/{lat}/{lon}/{radiusNm}`.
- Interval 60 s (min 30 s), budget 2/min, 1500/day per slot. Keyframe 210 s (`300 − 60 − 30`); record this deviation from §5.3 in the descriptor.
- `Coverage(region = envelope of the circle, actual = ac.length)`. `zeroResultsNormal` true (a small area can be empty at night).
- This replaces the catalogue's six fixed `adsblol-region-*` centres: 250 nm circles cannot cover continents.

### 4. adsb.lol etiquette and the 451 rule

- Use the identifiable Foresignal User-Agent from 005. **Never spoof a browser UA, never add browser headers to get past a block, never rotate egress.**
- Honour `Retry-After`. Add to `UpstreamClient` (005): `Retry-After` on any 4xx/5xx goes to `BackoffRegistry` clamped to [5 s, 1 h], and an adsb.lol 429 without the header gets 30 s.
- **HTTP 451 or 403:** add a generic rule to `UpstreamClient`. When the status is 451, or 403 for a source whose descriptor sets `accessDeniedIsTerminal=true` (new boolean, default false; true for the adsb.lol sources), call `BackoffRegistry.onThrottled(source, 1h)` and throw `UpstreamAccessDeniedException(status)`. `HealthEvaluator` reports `failing` with the reason "upstream refused this client (HTTP {status}); see Operator Guide → adsb.lol access". Retry hourly, never faster.
- Operator Guide section "adsb.lol access": the API states that a key "will be required in the future" and is obtained by feeding (`https://github.com/adsblol/api`). Running a feeder is the supported route to continued and re-api access. When a key scheme is published, a later change adds an optional `apiKey` credential. Until then, declare no credential field.

### 5. `opensky-states` (`ai.foresignal.source.adapters.opensky`)

- **Request:** GET `https://opensky-network.org/api/states/all?extended=1`, plus `lamin,lomin,lamax,lomax` when the optional `bbox` param is set (ParamType BBOX; default world).
- **Auth:** optional credentials `clientId` and `clientSecret` (both required together; validated in `SourceService.setCredentials`). OAuth2 client credentials: POST form `grant_type=client_credentials&client_id=…&client_secret=…` to `https://auth.opensky-network.org/auth/realms/opensky-network/protocol/openid-connect/token` (verified 2026-10: the path includes `/auth/`). Tokens last 30 min: cache the token in memory per source and refresh 60 s before expiry. On a 401, refresh once and retry. A token-endpoint 400/401 throws `CredentialsRejectedException` (010). Without credentials, run anonymous. **Basic auth ended on 2026-03-18; never send it.** The token POST goes through `ctx.http().post` and consumes budget. Redact the secret (005 redaction).
- **Credit governor (verified 2026-10):** daily credits are 400 anonymous, 4,000 standard, 8,000 for an active feeder and 14,400 licensed. Cost per `/states/all` by bbox area: ≤ 25 sq° → 1, ≤ 100 → 2, ≤ 400 → 3, else (world) → 4.
  - After every response, store `X-Rate-Limit-Remaining` and its time in adapter state.
  - Before a fetch, if the stored remaining (from today, UTC) is below `cost + creditReserve`, throw `UpstreamQuotaDeferredException(retryAt)`. `creditReserve` is an integer param, default 40.
  - **Hook:** add `UpstreamQuotaDeferredException extends AdapterException` to the SPI if missing. `FetchExecutor` (010) writes a REFUSED `fetch_log` row and calls `HealthTracker.recordRefused`, not `recordFailure`, so the source shows `degraded` through the refusal rule rather than `failing`.
  - On 429, `X-Rate-Limit-Retry-After-Seconds` already feeds backoff (005).
- **Interval:** default 300 s, min 90 s (`86400 / (4000/4)` ≈ 86 s for a standard account at world cost). Budget 2/min (the token refresh and the fetch can fall in the same minute), 1500/day. Document in the descriptor that anonymous world polling needs ≥ 900 s (400 credits ÷ 4 = 100 calls/day).
- **TTL:** 15 min, not the §5.3 5 min. A 300 s poll fails `keyframe ≤ ttl − poll − 30 s` at 5 min. With 15 min the 240 s keyframe holds up to a 630 s interval. Record this deviation.
- **Parser** `OpenSkyStatesParser` (pure): the positional array is `0 icao24, 1 callsign, 2 origin_country, 3 time_position, 4 last_contact, 5 lon, 6 lat, 7 baro_altitude m, 8 on_ground, 9 velocity m/s, 10 true_track, 11 vertical_rate m/s, 12 sensors, 13 geo_altitude m, 14 squawk, 15 spi, 16 position_source (0 ADS-B, 1 ASTERIX, 2 MLAT, 3 FLARM), 17 category`.
  - Time base: `observedAt = last_contact`, always; `quality.extra["opensky.time_position"]`.
  - Map to the **same payload keys** as the readsb mapper: `gs` in knots from m/s, `alt_baro`/`alt_geom` already in metres, `baro_rate`, `onGround`, `squawk`, `flight`, `posSource` (`adsb|asterix|mlat|flarm`), `originCountry`.
  - `category`: integer to ADS-B code (2→A1 … 8→A7, 9→B1, 10→B2, 11→B3, 12→B4, 14→B6, 15→B7, 16→C1 … 20→C5; 0, 1 and 13 omitted).
  - Rows without lat/lon emit null geometry. Anything other than `{time, states:[…]|null}` throws `UpstreamFormatException`. `states: null` means zero aircraft and is valid.
- Licence "OpenSky Network terms (non-commercial research)", `termsUrl` `https://opensky-network.org/about/terms-of-use`, attribution citing Schäfer et al., IPSN 2014. Verified 2026-10: the terms page refused automated fetches. Have the operator read it; note this in the Source-Catalog row.

### 6. Egress hook (licence)

- Add a descriptor field `egress` (`OPEN` | `LOCAL_ONLY`, default `OPEN`) if missing. `LOCAL_ONLY` means the data is served to this instance's own API clients but never re-served by outbound sinks (110), federation (113), public share links or feed keys (112), or cold-archive exports shared off-box. 109, 110 and 113 enforce it. Here, add the field, expose it on OpenAPI `SourceDescriptor`, and provide `EgressPolicy.allows(sourceId, EgressPath)`.
- `opensky-states`: `LOCAL_ONLY` (terms; Gap-Analysis §9). adsb.lol sources: `OPEN` (ODbL: share-alike, attribution).

### 7. Fixtures and capture

Capture under `src/test/resources/fixtures/<source-id>/`, each with a README (date, URL, the UA used, trimming):

- `curl -sA "Foresignal/<ver> (+https://foresignal.ai; install=fixture)" https://api.adsb.lol/v2/mil`, trimmed to ≤ 30 aircraft. Keep one each of: on ground, `lastPosition` only, `~` hex, `mlat` position, `dbFlags` military.
- One `/v2/point/{lat}/{lon}/250` response.
- An OpenSky anonymous `/states/all?extended=1&lamin=45&lomin=5&lamax=48&lomax=10`, trimmed, plus the response headers in `headers.txt`.
- Hand-made: `states-null.json`, `adsblol-html-200.html`, `readsb-edge.json` (ground, missing hex, `now` in ms, emergency `none`).

### 8. Spec, catalogue, docs

- `docs/OpenAPI.json`: add `SourceDescriptor.egress` (enum `OPEN|LOCAL_ONLY`); keep `accessDeniedIsTerminal` internal (not in the spec). No new operations. Regenerate Postman with `python3 scripts/generate-postman.py docs/OpenAPI.json postman`.
- `docs/Source-Catalog.md`:
  - Update the rows for `adsblol-mil`, `opensky-states` and the renamed `adsblol-area-*` with tier, policy, TTL, `ttlAnchor`, `observedAtMode`, significant fields, licence and egress.
  - Record the "no global adsb.lol" decision and the verified credit table.
  - Remove the "basic auth" note.
- Operator Guide: "adsb.lol access", "OpenSky credentials and credits".
- `THIRD_PARTY_NOTICES.md`: GEV entries for the ported mapping and credit-tier logic, with file headers naming `adsbLolFallback.js` and `opensky.js`.

## Tests to write (do not run)

- `ReadsbAircraftMapperTest`:
  - time base `now − seen` with `now` in seconds and in milliseconds
  - `lastPosition` fallback, ground, `~` hex
  - feet-to-metres, knots and m/s conversions
  - `posSource` for mlat and tisb
  - `navDegraded` boundaries (`nac_p` 7/8, `nic` 6/7)
  - `dbFlags` bits, omitted nulls, excluded fields
  - `SnapshotContract.assertStableStaticHash` across two polls that differ only in kinematics
- `FieldSpecsContractTest` (011 contract) for all three sources.
- `AdsbLolAdapterTest` (WireMock):
  - mil URL; area URL built from params; `radiusNm` 251 rejected by `ParamValidator`
  - `Retry-After` honoured
  - non-JSON 200 throws `UpstreamFormatException`
  - 451 sets a 1 h backoff and the health reason
  - the UA header is the Foresignal UA
- `OpenSkyStatesParserTest`: fixture count, every index, category mapping, `states:null`, malformed document.
- `OpenSkyAdapterTest` (WireMock):
  - token fetched once and reused, refreshed after expiry (`MutableClock`), 401 → one refresh
  - anonymous mode sends no `Authorization` header
  - cost tiers by bbox
  - remaining below reserve → `UpstreamQuotaDeferredException` and no states request (`verify(0, …)`)
  - 429 with `X-Rate-Limit-Retry-After-Seconds` goes to backoff
  - the client secret is redacted in the `FetchTrace`
- `AircraftIngestIT` (`@IntegrationTest`, WireMock):
  - first poll → FIRST versions
  - an identical second poll → TOUCH or LIVE, no versions
  - a 2 km move after 60 s → SAMPLE
  - a squawk change → SIGNIFICANT
  - an aircraft absent until past the TTL then back → REAPPEAR
  - `fetch_log` rows carry REFUSED for a quota deferral
- `DescriptorDefaultsTest`: registry validation passes for all five sources (keyframe constraint at the default intervals).

## Acceptance criteria

- `adsblol-mil`, `adsblol-area-1..3` and `opensky-states` register as TRACK + SAMPLED with valid defaults, and the registry starts.
- No code path sends a browser User-Agent or Basic auth to OpenSky.
- The 451 path produces `failing` with an hourly retry and no tighter loop.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 124, 139, 142, 143, 144) and in the sanctioned narrow runs of 007–009.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/design-history/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `062: Aircraft Adapters`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/062-CR-Aircraft-Adapters.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `062: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
