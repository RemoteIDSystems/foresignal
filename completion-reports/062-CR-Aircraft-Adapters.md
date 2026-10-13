# 062-CR-Aircraft-Adapters

| | |
|---|---|
| Prompt | 062-Aircraft-Adapters.md |
| Status | Complete |
| Implementation commit | 81b5249b3431217d673722f5e04aadec7569d5d8 |
| Date | 2026-10-13 |

## Summary

Foresignal now collects aircraft. Five new TRACK sources, all disabled by default, run on the v1.2 pipeline with
the Core-Data-Model §5.3 SAMPLED defaults and the §2 time bases:
- `adsblol-mil`: military aircraft worldwide;
- `adsblol-area-1..3`: three operator-centered circles of at most 250 nm;
- `opensky-states`: worldwide civil aircraft, credit-governed.

Prompts 069 and 080 can reuse the shared readsb mapper. There are no new operations.

**1. Shared readsb mapper** (`ai.foresignal.source.formats.readsb`).
- **`ReadsbDocument.parse(byte[])`:**
  - reads `ac` (adsb.lol) or `aircraft` (local readsb), plus `total` and `ctime`;
  - `now` is seconds, or milliseconds when above 1e11. adsb.lol sends milliseconds (verified in the captures).
  - A missing array or a non-numeric `now` is an `UpstreamFormatException`.
- **`ReadsbAircraftMapper.map(JsonNode, Instant, MapperOptions)`** and `mapAll`:
  - **Identity:** `externalId` is `hex` lower-cased, keeping any `~`. Entries without `hex` are skipped and counted.
  - **Time:** `observedAt = now − seen`; `eventTime` is null.
  - **Geometry:** a 2D point from `lat`/`lon`, else from `lastPosition` (with `quality.extra["adsb.last_position"]`),
    else null. `adsb.seen_pos` goes in `quality.extra`. An out-of-range coordinate is treated as no position.
  - **Payload:** the allowlist in meters, knots, degrees and m/s, with `posSource`, `navDegraded` and
    `mil`/`pia`/`ladd`. Counters and query-relative fields are left out.
  - **Quality:** REPORTED, CALCULATED or UNKNOWN.
- **`AircraftFields`:** the payload keys, unit conversions (1 m, 0.1 kn, 0.1°, 0.1 m/s) and OpenSky's
  category-to-ADS-B table.
- **`AircraftDescriptors.global(builder[, ttl])`** sets:
  - TRACK, `ttlAnchor` OBSERVED, `observedAtMode` UPSTREAM, TTL 6 min;
  - SAMPLED `{60 s, 240 s, 1000 m, thresholds, 30 s}`;
  - significant `flight, r, t, squawk, category, emergency, onGround, navDegraded`;
  - thresholds `alt_baro` 300 m and `track` 20° (angular); motion `{gs, KNOTS, track}`;
  - tile properties, search fields, `titleField` `flight`, and entity keys `icao24`/`registration`/`callsign`;
  - `aggBelowZoom` 7, the `fieldSpecs` of every key (UCUM units and the two datum labels), and the kind displays
    (`plane`, and `fighter-jet` for mil).

**2. adsb.lol** (`ai.foresignal.source.adapters.adsblol`).
- **`adsblol-mil`:** `GET /v2/mil`, 30 s (minimum 15 s), 4/min and 3,000/day, complete result, no tombstones.
- **`AdsbLolAreaFamily`:** three slots, disabled by default, 60 s (minimum 30 s), 2/min and 1,500/day each.
  - Params: `lat` and `lon` (required numbers) and `radiusNm` (integer, default 250).
  - Ranges are checked in `checkParams`, so `ParamValidator` refuses `radiusNm` 251.
  - Request: `/v2/point/{lat}/{lon}/{radiusNm}`, with plain decimals.
  - `Coverage(region = the circle's envelope, actual = aircraft returned)`; `zeroResultsNormal`.
- **Shared by all four:**
  - attribution "adsb.lol", license `ODbL-1.0`, terms `https://github.com/adsblol/api`, egress OPEN;
  - `accessDeniedIsTerminal`, and 30 s after a 429 without `Retry-After`;
  - a 200 that is not JSON (by content type or first byte) throws `UpstreamFormatException` "… non-JSON 200, treated
    as throttling".

**3. Etiquette, 451 and quota hooks.**
- **`SourceDescriptor`** gains two internal fields, not in the spec:
  - `accessDeniedIsTerminal` (boolean);
  - `defaultRetryAfter` (Duration).
- **`BoundSourceHttp.errorFor`** (the `UpstreamClient` request path):
  - **451, or 403 with `accessDeniedIsTerminal`:** `BackoffRegistry.onThrottled(source, 1h)`, then
    `UpstreamAccessDeniedException(status)`.
  - **`Retry-After`** (or `X-Rate-Limit-Retry-After-Seconds`) on any 4xx/5xx goes to backoff, clamped to [5 s, 1 h].
  - **429 without the header:** the descriptor's `defaultRetryAfter`, else the exponential ladder (as before).
- **`HealthEvaluator`:** `failing` with "upstream refused this client (HTTP {status}); see Operator Guide → adsb.lol
  access" while the last failure is a refusal. The one-hour backoff keeps retries hourly.
- **SPI `UpstreamQuotaDeferredException(message, retryAt)`.** `FetchExecutor` writes a REFUSED row ("upstream quota
  deferred: … (retry at …)"), counts a refusal in `UsageMeter` (an optional setter) and calls
  `HealthTracker.recordRefused`. It never calls `recordFailure`.
- **SPI `SourceAdapter.checkCredentials(Map)`**, called by `SourceService.setCredentials` after the per-field checks.

**4. `opensky-states`** (`ai.foresignal.source.adapters.opensky`).
- **Request:** `GET /api/states/all?extended=1`, plus `lamin,lomin,lamax,lomax` from the optional `bbox`.
- **Auth:** optional `clientId` and `clientSecret`, required together (`checkCredentials`).
  - `OpenSkyTokens` POSTs the client-credentials form through `ctx.http().post`, so it is budgeted and redacted.
  - The token is cached per source, keyed by a SHA-256 of the credentials, and refreshed 60 s before expiry.
  - On a 401 the adapter refreshes once and retries. A 400/401 from the token endpoint is a
    `CredentialsRejectedException`. Anonymous mode sends no `Authorization` header, and nothing ever sends basic auth.
- **Credits:** `OpenSkyCredits` handles the cost by bbox area and stores `X-Rate-Limit-Remaining` and its time after
  every response. When today's (UTC) balance is below cost + `creditReserve` (default 40), the adapter throws
  `UpstreamQuotaDeferredException` until the next UTC midnight.
- **Descriptor:**
  - 300 s (minimum 90 s), 2/min and 1,500/day, TTL 15 min, egress LOCAL_ONLY;
  - the license, terms and Schäfer et al. (IPSN 2014) attribution;
  - the description states the 900 s anonymous floor.
- **`OpenSkyStatesParser`** (pure):
  - the 18-index array mapped to the readsb keys plus `originCountry`, with `posSource` `adsb|asterix|mlat|flarm`;
  - the category table (0, 1 and 13 omitted);
  - `observedAt = last_contact`, and `opensky.time_position` in `quality.extra`;
  - `states: null` is valid; any other shape is an `UpstreamFormatException`.

**5. Egress.**
- `egress` and `Egress` already existed (from 024). `EgressPolicy` gains:
  - `isLocalOnly(sourceId)`;
  - `allows(sourceId, EgressPath)`;
  - a LOCAL_ONLY rule in `allows(path, sourceId, viewer)`: only `EXPORT` (to this instance's own caller) is allowed.
    `SINK`, `FEDERATION`, `SHARE` and `FEED` are refused for every viewer.
- `opensky-states` is LOCAL_ONLY; the adsb.lol sources are OPEN.

**6. Spec, catalog, docs.**
- **OpenAPI:**
  - `SourceDescriptor.egress` was already present; its description now states what is enforced;
  - `Record.title` notes the new `r > hex` fallback;
  - a changelog entry.

  `lintOpenApi` passes. The Postman collection was regenerated and is unchanged, because only descriptions changed.
- **Source Catalog:**
  - rows `adsblol-mil`, `adsblol-area-1..3` (replacing `adsblol-region-*`) and `opensky-states`;
  - a new "Aircraft decisions" block with the no-global-adsb.lol decision and the verified credit tables;
  - the VEL "basic auth is dead" note is removed.
- **Operator Guide:** "Aircraft sources", "adsb.lol access" and "OpenSky credentials and credits".
- **Adapter Guide:**
  - §1: `formats.*` is importable by any adapter;
  - §5: aircraft altitudes are in meters;
  - new §15: the readsb mapper, descriptor defaults, 451/403, quota deferral and `checkCredentials`.
- **Invariants:** SRC-3 gains `AdsbLolAdapterTest` and `OpenSkyAdapterTest`; EGR-2 is detailed.
- **`THIRD_PARTY_NOTICES.md`:** six God's Eye View rows. The ported files carry origin headers naming
  `adsbLolFallback.js`, `adsb-lol.js` and `opensky.js`.

**7. Fixtures** (`src/test/resources/fixtures/`), each directory with a README.
- `adsblol-mil/`: the real `/v2/mil` (304 aircraft, trimmed to 30) and a hand-made `adsblol-html-200.html`.
- `adsblol-area/`: a real `/v2/point/41.98/-87.90/250`, trimmed to 30 with two `~` TIS-B addresses.
- `opensky-states/`: a real anonymous bbox response (236 states, trimmed to 30), `headers.txt` and hand-made
  `states-null.json` and `states-edge.json`.
- `readsb/readsb-edge.json`: hand-made.

All captures used the User-Agent `Foresignal/0.1.0-SNAPSHOT (+https://foresignal.ai; install=fixture)` on
2026-10-13.

## Files created / modified

**Created**
- `src/main/java/ai/foresignal/source/formats/readsb/`: `package-info`, `ReadsbDocument`, `ReadsbAircraftMapper`,
  `MapperOptions`, `AircraftFields`, `AircraftDescriptors`.
- `src/main/java/ai/foresignal/source/adapters/adsblol/`: `package-info`, `AdsbLol`, `AdsbLolMilAdapter`,
  `AdsbLolAreaAdapter`, `AdsbLolAreaFamily`.
- `src/main/java/ai/foresignal/source/adapters/opensky/`: `package-info`, `OpenSkyStatesAdapter`,
  `OpenSkyStatesParser`, `OpenSkyTokens`, `OpenSkyCredits`.
- `src/main/java/ai/foresignal/source/spi/UpstreamQuotaDeferredException.java`,
  `src/main/java/ai/foresignal/collect/UpstreamAccessDeniedException.java`.
- Tests:
  - `source/formats/readsb/ReadsbAircraftMapperTest`;
  - `source/adapters/adsblol/AdsbLolAdapterTest`;
  - `source/adapters/opensky/OpenSkyStatesParserTest` and `OpenSkyAdapterTest`;
  - `source/adapters/FieldSpecsContractTest`, `DescriptorDefaultsTest` and `AircraftIngestIT`;
  - `security/LocalOnlyEgressIT`.
- Fixtures: `src/test/resources/fixtures/{adsblol-mil,adsblol-area,opensky-states,readsb}/`.

**Modified**
- `source/spi/SourceDescriptor` (`accessDeniedIsTerminal`, `defaultRetryAfter`), `source/spi/SourceAdapter`
  (`checkCredentials`), `source/SourceService` (calls it).
- `collect/BoundSourceHttp` (`errorFor`), `collect/HealthEvaluator` (refusal rule), `collect/FetchExecutor` (quota
  deferral, `setUsageMeter`).
- `security/EgressPolicy` (LOCAL_ONLY), `ingest/TitleResolver` (fallbacks `r`, `hex`).
- Tests: `ingest/TitleResolverTest` (aircraft fallback), `source/SourceServiceIT` (cross-field credentials).
- `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Operator-Guide.md`, `docs/Adapter-Guide.md`,
  `docs/Invariants.md`, `THIRD_PARTY_NOTICES.md`.

## Deviations from the prompt

1. **TTL 6 min for every adsb.lol source, keyframe 240 s everywhere.** The prompt's task 1 sets 6 min, as
   Conventions does, but tasks 2 and 3 compute with 300 s (`240 ≤ 300 − 30 − 30`, area keyframe `210 = 300 − 60 −
   30`). Conventions wins, so the area keyframe stays 240 s, which fits under `360 − 60 − 30 = 270`. The descriptors
   state that 240 s holds up to a 90 s interval.
2. **No interval-versus-keyframe check at PUT.** The prompt says "the validator rejects an interval above 30 s at
   PUT unless the operator also shortens the keyframe". The code does not: `IngestPolicyValidator` checks the
   descriptor's default interval, and `SourceService.update` checks only the interval floor. Adding the check would
   change every TTL source (`awc-metars`, `hms-smoke`), so it was not done. The descriptions tell the operator
   instead. See the open questions.
3. **adsb.lol 30 s default as a descriptor field.** "An adsb.lol 429 without the header gets 30 s" is implemented as
   an internal `SourceDescriptor.defaultRetryAfter`, set by the adsb.lol sources, so `UpstreamClient` does not
   hard-code a host.
4. **`UpstreamAccessDeniedException` lives in `collect`** and extends `UpstreamHttpException`, so the HTTP status
   reaches `fetch_log` and health like any HTTP error. Its message is the health reason.
5. **The 451/403 rule ignores `Retry-After`:** a refusal always gets the one-hour backoff, the registry ceiling.
6. **Quota deferral and health.** `HealthTracker.recordRefused` is a no-op by design: the refusal rule reads
   `UsageMeter`. So `FetchExecutor` also calls `UsageMeter.recordRefused` (through an optional setter), otherwise the
   source would never show `degraded`.
7. **Title fallback `r`, then `hex`:** `TitleResolver.FALLBACK_FIELDS` gains `r` and `hex` after `title`, `name`,
   `callsign` and `place`. `titleField` is a single key, so this was the only way. The spec's `Record.title`
   description and changelog are amended.
8. **Egress was already modeled.** `Egress`, `SourceDescriptor.egress` and the OpenAPI field existed from 024/039. The
   prompt's `EgressPolicy.allows(sourceId, EgressPath)` was added as an overload; the existing
   `allows(path, sourceId, viewer)` gained the LOCAL_ONLY rule. `EXPORT` stays allowed, because it is an export to this
   instance's own caller. Off-box archive exports are 111's to refuse; no new `EgressPath` was added.
9. **`FEED` refused for LOCAL_ONLY for every viewer**, including an admin token reading a feed or KML, because
   `Viewer` cannot tell a feed key from a token.
10. **Positionless OpenSky quality.** ASTERIX maps to UNKNOWN and FLARM to REPORTED (the prompt defines quality only
    for readsb).
11. **Out-of-range coordinates** become "no position" instead of failing the fetch: `Geo.point` throws on them, and
    one bad aircraft must not drop the snapshot.
12. **Fixture layout:** the readsb edge fixture is in `fixtures/readsb/` (shared mapper), and the area capture is in
    `fixtures/adsblol-area/` (one directory for the three slots). The `/v2/mil` capture had no `~` address, so that
    case is covered by the area capture and `readsb-edge.json`.
13. **Kind displays and `aggBelowZoom` 7** were added (Adapter Guide §11/§13); the prompt does not list them.

## Issues found / open questions

1. **Owner: `/v2/point/0/0/20000` works.** The prompt expected a 422 or a clamped result. At 2026-10-13T12:23:22Z it
   returned **HTTP 200 with 9,885 aircraft** (5.5 MB), up to about 9,090 nm from (0, 0), although the published
   OpenAPI declares `maximum: 250`. As instructed, no global source was built. Whether to use it remains your
   decision.
2. **Owner: interval PUT validation** (deviation 2). Should `PUT /v1/sources/{id}` refuse an interval that breaks
   `keyframe ≤ ttl − interval − 30 s` for the effective policy? That would affect every source with a TTL.
3. **Owner: licensed OpenSky accounts refill hourly** (14,400 credits, per the REST page). The governor defers until
   the next UTC midnight, as the prompt specifies. A licensed operator who drains the balance would wait up to a day
   instead of an hour.
4. **The probe recorder captures the token response body,** including the 30-minute `access_token`, for an admin
   probe. The client secret itself is never recorded.
5. **Possible 097 fragility:**
   - many ITs use the test TRACK kind `aircraft` (`TestIngestSources.TRACK_KIND`), which the real registry now also
     serves, from five sources;
   - kind-level metadata (first `kindDisplay` wins, kind schema, tile layer lists) may differ from what those tests
     expect;
   - `BoundSourceHttp` now backs off on `Retry-After` for any 4xx/5xx, not only 429/503.
6. **Unverified live:** the OpenSky token endpoint and authenticated credits (no key, per Conventions), and adsb.lol's
   451 behavior. Our identifiable User-Agent got 200 on every call made here.
7. Data contained no instructions directed at the agent.

## Tests written (not run)

- **`ReadsbAircraftMapperTest`:**
  - `now − seen` with `now` in seconds and milliseconds; `seen_pos`; malformed documents;
  - missing `hex`, `~` kept, the point, the `lastPosition` fallback (and turning it off), ground;
  - feet to meters, knots, degrees, ft/min to m/s;
  - `posSource` for mlat, tisb and each `type`;
  - the `navDegraded` boundaries (`nac_p` 7/8, `nic` 6/7, partial);
  - `dbFlags` bits, emergency `none`, the excluded fields and no nulls;
  - `FieldSpecContract` on three fixtures;
  - `SnapshotContract.assertStableStaticHash` across two polls that differ only in kinematics.
- **`FieldSpecsContractTest`:** all three sources, every fixture.
- **`AdsbLolAdapterTest`** (WireMock, real `UpstreamClient`):
  - the mil URL and the area URL from params, with coverage;
  - `radiusNm` 251 refused by `ParamValidator`; lat, lon and URL-like values refused;
  - `Retry-After` on a 429 and on a 502, and 30 s without it;
  - an HTML 200, and a JSON content type with an HTML body;
  - 451 and 403: a one-hour backoff, the health reason, still failing after the backoff, no request while in backoff;
  - the Foresignal User-Agent and no browser headers;
  - the descriptors.
- **`OpenSkyStatesParserTest`:** fixture count, every index, ground and the position sources, the category mapping,
  `states: null`, malformed documents, skipped states, `fieldSpecs`.
- **`OpenSkyAdapterTest`** (WireMock):
  - the token fetched once and reused, refreshed after expiry (`MutableClock`);
  - 401 → one refresh, a second 401 fails;
  - a token 401 → `CredentialsRejectedException`;
  - anonymous mode: no `Authorization` header;
  - the secret absent from every `FetchTrace` and recorded exchange;
  - credentials required together;
  - cost tiers and the bbox URL;
  - remaining stored; remaining below the reserve → deferral with no request (`verify(0, …)`); the boundaries;
  - a 429 with `X-Rate-Limit-Retry-After-Seconds` → backoff;
  - the descriptor.
- **`AircraftIngestIT`:**
  - FIRST versions, with title, licence and `observed_at`;
  - an identical poll writes no versions;
  - a 2 km move after 60 s → SAMPLE; a squawk change → SIGNIFICANT; absent past the TTL → REAPPEAR;
  - an OpenSky quota deferral → a REFUSED `fetch_log` row, one states request, no failure.
- **`DescriptorDefaultsTest`:** all five pass registry validation and the keyframe constraint at their default
  intervals, with every §5.3 field.
- **`LocalOnlyEgressIT`** (EGR-2): `opensky-states` refused on sink, federation, share and feed for every viewer, and
  allowed on export and reads; OPEN and unknown sources.
- `TitleResolverTest.aircraftFallback`; `SourceServiceIT.crossFieldCredentials`.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL (only the existing deprecation and unchecked notes).
- `./gradlew lintOpenApi`: "Your API description is valid", 71 warnings (unchanged).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "832 requests across 80 tag folders … covers all
  ops: True" (collection unchanged).

## Notes for the next prompt

1. **063 (AISStream):**
   - a TRACK source reuses the pattern: descriptor defaults in a helper, the SAMPLED vessel row of §5.3 and its own
     TTL (30 min);
   - a refused key throws `CredentialsRejectedException`;
   - quota checks before a request use `UpstreamQuotaDeferredException`.
2. **069 / 080 (local receivers, feeders):** use `ReadsbDocument.parse` + `ReadsbAircraftMapper.mapAll` with
   `MapperOptions` (kind, `lastPositionFallback`) and `AircraftDescriptors.global(builder, ttl)`. Override the §5.3
   `local-adsb-*` policy row after calling it (the builder's last write wins). MLAT redistribution (069) belongs on
   `egress`.
3. **109 / 110 / 112 / 113:** call `EgressPolicy.allows(path, sourceId, viewer)` or `allows(sourceId, path)`. LOCAL_ONLY
   is already refused there; 109 adds the operator override inside `isLocalOnly`.
4. **097 checkpoint:**
   - first run of the five aircraft sources, the readsb mapper, `errorFor`, the refusal health rule and the quota
     REFUSED path;
   - `AircraftIngestIT` builds adsb.lol bodies whose `now` follows the `MutableClock` (T0 2026-10-13T12:00:00Z); the
     OpenSky case sets 12:23:30Z so the fixture's `last_contact` is in the past;
   - see open question 5 for the possible fragility.
5. **143:**
   - with `FORESIGNAL_VERIFY_*` OpenSky credentials, check the token endpoint, `expires_in` and an authenticated
     `X-Rate-Limit-Remaining`;
   - confirm adsb.lol still answers our User-Agent, and recapture fixtures if shapes changed.
6. **144 (Flyway):** no schema change. Adapter state uses the keys `credits.remaining` and `credits.at` for
   `opensky-states`.
7. **Owner decisions pending:** open questions 1–3 above, plus those carried from 012–061.
