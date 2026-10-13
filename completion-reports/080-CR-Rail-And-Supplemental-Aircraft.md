# 080-CR-Rail-And-Supplemental-Aircraft

| | |
|---|---|
| Prompt | 080-Rail-And-Supplemental-Aircraft.md |
| Status | Complete |
| Implementation commit | 307c2f23fcf840cabcf25c3d23429bae0e5676c5 |
| Date | 2026-10-13 |

## Summary

Eight new sources, all TRACK, all disabled by default, all passing the registry validator and the §5.1 keyframe
constraint. Four of them are aircraft sources and two are rail sources (one of the aircraft entries is a family of
four slots).

**Shared pieces:**
- **readsb mapper reused, not forked.** Prompt 062's `ReadsbDocument`/`ReadsbAircraftMapper` read the adsb.fi v2,
  adsb.fi v3 and airplanes.live envelopes unchanged: `ac[]`, `now` in milliseconds, `total` and `ctime`, plus `msg` and
  `ptime`, which it ignores. Two things were checked against real data:
  - the v3 `dst`/`dir` fields and the adsb.fi `rr_lat`/`rr_lon` fields fall outside the allowlist and are not stored;
  - `lastPosition`, MLAT and TIS-B entries map as before.

  The only addition is `formats.readsb.ReadsbResponses`, the shared "truncated, or non-JSON 200, is a format error"
  check. It mirrors 062's adsb.lol check, which was left as it is.
- **`HostPacer`** (`ai.foresignal.collect`; 062 had no equivalent): a per-host gate.
  - It allows one request in flight per host and a minimum spacing between request starts, measured on a monotonic
    clock.
  - It is owned by `UpstreamClient` (one per client), so every source calling a host shares it.
  - New descriptor field `hostPacing` (Duration, default null, internal). `BoundSourceHttp` waits on the gate after
    the backoff check and before the budget, and holds it until the response has been read.
  - The wait is bounded at 30 s. After that, `HostPacingTimeoutException` (a subclass of `InBackoffException`) is
    thrown and nothing is sent or charged.
  - `FetchExecutor` reports this as a skip with reason `host-pacing` and an error starting "host pacing". A manual
    fetch gets the existing 409 `IN_BACKOFF`.
- **Refusal reasons:** new descriptor field `accessDeniedReasons` (body substring → reason, internal).
  - `UpstreamAccessDeniedException` carries the matched detail, and `reason(status, detail)` gives
    "upstream refused this client (HTTP 403): access approval required (contact airplanes.live)".
  - `HealthEvaluator` reports that stored reason. Every other refusal keeps the 062 text.

**Aircraft** (`AircraftDescriptors.global`: TTL 6 min, SAMPLED {60 s, 240 s, 1,000 m, thresholds, 30 s}):
- Common settings: 30 s polls (minimum 30 s), budget 2/min and 2,880/day, `hostPacing` 1.1 s,
  `accessDeniedIsTerminal`, egress `LOCAL_ONLY`.
- **`airplaneslive-mil`** (`AirplanesLiveMilAdapter`): re-verified today, the anonymous request gets the 403 with the
  approval body. The result is an hourly retry and health `failing` with the approval reason. There is no
  credential, because no key or header mechanism is published.
- **`adsbfi-mil`** (`AdsbFiMilAdapter`): `GET /api/v2/mil`.
- **`adsbfi-region-1..4`** (`AdsbFiRegionFamily`, `AdsbFiRegionAdapter`): `GET /api/v3/lat/{lat}/lon/{lon}/dist/{distNm}`.
  - Parameters: `lat` and `lon` required; `distNm` 1–250, default 150; `label` is used in log lines only.
  - `checkParams` rejects a `distNm` above 250 through `ParamValidator`.
  - Coverage is reported as the circle's envelope.
  - Results are `zeroResultsNormal`.

**`digitraffic-rail`** (`DigitrafficRailAdapter`, `DigitrafficRailParser`):
- One GET of `train-locations/latest` per poll, with `Digitraffic-User: Foresignal/<version>`. Gzip comes from the
  client.
- `externalId = departureDate:trainNumber`; `observedAt = timestamp`.
- Payload: `trainNumber, departureDate, speedKph, gps, accuracyM` (volatile).
- Enrichment (`enrich`, default true): `trainType, trainCategory, operatorShortCode, commuterLineID` come from
  `/api/v1/trains/{date}/{n}`.
  - Each train is looked up once per departure day; empty answers are cached too.
  - The cache lives in adapter state under `rail:trains` and is purged once the departure date is more than a day old.
  - Limits: at most 200 lookups per UTC day (`rail:lookups`) and 1 per run.
  - A failed lookup never fails the run.
- Descriptor: TTL 5 min, SAMPLED {15 s, 240 s, 150 m, edge 8 s}, 30 s (minimum 15 s), budget 4/min and 4,000/day,
  CC BY 4.0, "Fintraffic / digitraffic.fi", egress OPEN.
- The MQTT feed is noted as a later streaming option.

**`amtraker`** (`AmtrakerAdapter`, `AmtrakerParser`):
- One GET of `/v3/trains` per poll.
- `externalId = trainID`; `observedAt = lastValTS` (offsets honored).
- Payload: `trainNumber, route, provider, state, timeliness, nextStationCode, originCode, destinationCode, speedKph`
  (mph × 1.609344) and `headingDeg` (compass text → degrees).
- `stations[]`, `alerts[]`, colors, time zones and names are dropped.
- Descriptor: TTL 5 min, SAMPLED {15 s, keyframe 180 s, 150 m, edge 8 s}, threshold `headingDeg` 30°, motion
  {`speedKph`, km/h, `headingDeg`}, 60 s (minimum 60 s), budget 2/min, 1,500/day and 250 MB/day.

**Docs:**
- `docs/Source-Catalog.md`: a new section "Rail and supplemental aircraft (prompt 080)" with all rows (tier, policy,
  TTL, keyframes, time base, licenses, the airplanes.live access requirement, the new `adsbfi-mil`, Amtraker's terms,
  host pacing) and a prompt 080 note. The four old idea rows now point to it.
- `docs/Operator-Guide.md`: the "adsb.lol access" section, which the generic refusal reason points to, now covers
  adsb.fi pacing and terms and the airplanes.live approval.
- `docs/Invariants.md`: SRC-3 test entries.
- **No API change.** The OpenAPI `train` and `aircraft` kind entries already list exactly the payload fields used,
  and the new descriptor fields are internal (`SourceMapper` maps fields explicitly).

**Acceptance:**
- All eight sources register on TRACK, disabled, and pass `DescriptorValidator` and `IngestPolicyValidator`. This
  was checked with a scratch `main`; see "Compile result".
- No request sets a non-Foresignal User-Agent: no `userAgentOverride` is set, and the client strips caller
  User-Agent headers.
- Requests to one host never overlap, through `hostPacing`.
- Compiles.

## Files created / modified

- **Created, main:**
  - `collect/{HostPacer, HostPacingTimeoutException}`;
  - `source/formats/readsb/ReadsbResponses`;
  - `source/adapters/adsbfi/{package-info, AdsbFi, AdsbFiMilAdapter, AdsbFiRegionAdapter, AdsbFiRegionFamily}`;
  - `source/adapters/airplaneslive/{package-info, AirplanesLiveMilAdapter}`;
  - `source/adapters/digitraffic/rail/{package-info, DigitrafficRailAdapter, DigitrafficRailParser}`;
  - `source/adapters/amtraker/{package-info, AmtrakerAdapter, AmtrakerParser}`.
- **Modified, main:**
  - `source/spi/SourceDescriptor` (`hostPacing`, `accessDeniedReasons`);
  - `collect/BoundSourceHttp` (the pacing gate, refusal detail), `UpstreamClient` (owns the pacer);
  - `collect/UpstreamAccessDeniedException` (detail), `HealthEvaluator` (stored reason);
  - `collect/InBackoffException` (protected message constructor), `FetchOutcome` (`SKIP_HOST_PACING`),
    `FetchExecutor` (the skip), `ManualFetchService` (409).
- **Created, test:**
  - `collect/HostPacerTest`, `formats/readsb/ReadsbEnvelopeTest`;
  - `adapters/adsbfi/AdsbfiAdaptersTest`, `adapters/airplaneslive/AirplanesLiveAdapterTest`;
  - `adapters/digitraffic/rail/DigitrafficRailParserTest`, `adapters/amtraker/AmtrakerParserTest`;
  - `adapters/rail/{RailAndAircraftDescriptorTest, RailIngestIT}`.
- **Modified, test:**
  - `testsupport/weather/WeatherIngestSupport`: four base-URL properties, and `amtraker` and `digitraffic-rail` in
    the backoff list;
  - `retention/FamilyDefaultsTest`: the five new adapter classes;
  - `adapters/FieldSpecsContractTest`: the 080 readsb sources.
- **Created, fixtures** (each with a README):
  - `adsbfi-mil/{mil.json, edge.json}`, `adsbfi-region/region-39.86--104.67-25.json`;
  - `airplaneslive-mil/{forbidden-403.json, mil-synthetic.json}`;
  - `digitraffic-rail/{latest.json, train-2026-10-14-2241.json, latest-edge.json}`;
  - `amtraker/{trains.json, trains-edge.json}`.
- **Docs:** `docs/Source-Catalog.md`, `docs/Operator-Guide.md`, `docs/Invariants.md`.

## Deviations from the prompt

1. **Amtraker publishes terms now.**
   - The README of `github.com/piemadd/amtrak` (commit `126de6a6854df0b86bdfb9843d47e41369088a77`, read 2026-10-13)
     says the data is licensed under **ODC-By 1.0** since 2026-08-19. It asks for attribution with a link to
     amtraker.com and requires an identifying User-Agent.
   - So `licence = "ODC-By 1.0"`, attribution "Amtraker (https://amtraker.com)", `termsUrl` = the README section,
     and egress OPEN. The prompt had "unspecified (community API)" and `termsUrl = null`.
   - The "dropped if Amtraker or the operators object" note is kept. That repository's code is AGPL; none was used.
2. **Amtraker `Predeparture` trains are skipped.** Their coordinates are the origin station and `lastValTS` is the
   scheduled departure (40 minutes in the future for `28-13` on capture). That is not an observed position, and it
   would be ahead of the skew tolerance.
3. **Amtraker keyframe:** 180 s as prompted. The actual ceiling at 60 s is `300 − 60 − 30 = 210 s`, so 180 s holds up
   to a 90 s interval.
4. **`digitraffic-rail` has no motion spec.** `MotionSpec` requires a course field and Digitraffic has no heading. As
   the prompt allows, there is no bearing threshold, and the sampler uses the plain distance, which is what dead
   reckoning without a course reduces to anyway.
5. **Lookups are capped at 1 per run.** The prompt sets only the daily cap of 200. One per run keeps a run at no more
   than 2 requests, inside the 4/min budget at the 30 s interval.
6. **Egress `LOCAL_ONLY`** for the adsb.fi and airplanes.live sources, because their terms are non-commercial and
   personal-use. `digitraffic-rail` (CC BY) and `amtraker` (ODC-By) are OPEN. The prompt does not name egress.
7. **No airplanes.live credential.** The API guide page answers a browser challenge, and no key or header scheme is
   published. The approval reason is matched on the body text `contact@airplanes.live`.
8. **Host pacing is reported as a fetch skip, not a health status.** The prompt asks for an "InBackoffException-style
   skip with health reason 'host pacing'". The skip carries reason `host-pacing` and an error starting "host pacing".
   Like a backoff skip, it does not change health, because nothing failed. Pacing covers `get`, `getConditional`,
   `post`, `head`, `getRange` and S3 listings (every path through `execute`), but not `download` or WebSockets,
   which no paced source uses.
9. **Amtraker byte budget.** It is 250 MB/day as prompted. However, `UsageMeter` counts decoded bytes (1.28 MB per
   poll, about 1.8 GB/day at 60 s), and `BudgetGovernor` enforces `mbPerDay` only for requests that declare an
   expected size. The value is therefore informational for this source.
10. **Base-URL test properties** replace only the origin and keep the request paths: `foresignal.test.adapters.{adsbfi,
    airplaneslive, digitraffic-rail, amtraker}.base-url`. The `/{host}{path}` scheme of 072–079 is not used.

## Issues found / open questions

1. **Pre-existing gap in `FamilyDefaultsTest`:** these adapter classes are missing from `CLASSES`, so the test would
   fail when run:
   - `AdsbLolMilAdapter`, `AdsbLolAreaAdapter`, `OpenSkyStatesAdapter` (062);
   - `AisStreamAdapter` (063);
   - `CelestrakGpAdapter`, `CelestrakSocratesAdapter`, `SpaceTrackTipAdapter` (064);
   - `LaunchLibraryAdapter`, `SatellitePassesGenerator`.

   This was found by comparing the regex's adapter list with the map keys. I added only the 080 classes.
2. **The airplanes.live terms were not re-read.** The guide page is behind a browser challenge, and I did not work
   around it. The terms in the descriptor are the prompt's own 2026-10 check.
3. **The generic refusal reason** still says "see Operator Guide → adsb.lol access" for adsb.fi refusals. The guide
   section now covers adsb.fi too.
4. **US English:** `check_us_english.py` on every new file reports only the existing `licence(...)` builder and
   accessor and the upstream JSON key `cancelled` in the Digitraffic train fixture (wire data, unchanged). My own
   constant was renamed `LICENSE`.
5. **Instructions in data:** none. The airplanes.live 403 body asks projects to email them; that is addressed to the
   operator, and it is recorded and surfaced as the health reason, not acted on.
6. **`docs/Source-Catalog.html`** was not regenerated (as in 070–079).

**Owner questions (non-blocking):**
1. Accept Amtraker as ODC-By with egress OPEN (Deviation 1)?
2. Accept `LOCAL_ONLY` for the adsb.fi and airplanes.live sources (Deviation 6)?
3. Accept skipping Amtraker `Predeparture` trains (Deviation 2)?
4. Should a host-pacing skip degrade health after repeated occurrences (Deviation 8)?
5. Will you email airplanes.live for approval? That is the only route to `airplaneslive-mil` data.

## Tests written (not run)

- **`HostPacerTest`:**
  - spacing on a fake monotonic clock (first start immediate; the next waits out the remainder; none after the
    spacing);
  - per host, case-insensitive, shared by every caller;
  - a spacing wait that would pass the bound hands out nothing and keeps the gate free;
  - an idempotent permit;
  - one request in flight across 4 threads;
  - a bounded gate wait;
  - real-clock spacing across threads.
- **`ReadsbEnvelopeTest`:**
  - the adsb.fi v2/v3 and airplanes.live envelopes (`now` in ms, `total`, `ctime`);
  - `lastPosition` with `seen_pos` 761.286;
  - no position, MLAT → CALCULATED, ground, a large `seen_pos`;
  - v3 `dst`/`dir` dropped;
  - a `~` TIS-B address;
  - the `ReadsbResponses` checks.

  The existing mapper tests are untouched.
- **`AdsbfiAdaptersTest`** (WireMock, real client):
  - mil and region URLs, User-Agent;
  - `distNm` 251 rejected by `ParamValidator`; the default of 150;
  - three `adsbfi-*` fetches in parallel against 1.5 s responses start at least 1.4 s apart (serial);
  - back-to-back fast requests start at least 1.05 s apart;
  - 403 → refusal and one-hour backoff with nothing re-sent;
  - 429 → `Retry-After` backoff, not a refusal;
  - HTML 200 → format error.
- **`AirplanesLiveAdapterTest`** (WireMock):
  - the real 403 body → `UpstreamAccessDeniedException` with the approval reason and a one-hour backoff;
  - health `failing` with that reason before and after the backoff;
  - the generic reason for a 451 without the text;
  - hourly retry (nothing at +59 min, one request at +61 min);
  - Foresignal User-Agent and no browser or extra headers;
  - a 200 maps;
  - descriptor values.
- **`DigitrafficRailParserTest`** and **`AmtrakerParserTest`**:
  - real fixtures, unit conversions, dropped fields, provider coverage, edge cases;
  - `SnapshotContract.assertStableStaticHash`, the fieldSpecs contract, `DescriptorValidator`;
  - Digitraffic: the cache round trip, staleness and the daily counter.
- **`RailAndAircraftDescriptorTest`:**
  - every descriptor passes `DescriptorValidator` and `IngestPolicyValidator`, and `keyframe ≤ ttl − interval − 30 s`;
  - the per-source values;
  - the ceilings (300 s, 210 s, 240 s).
- **`RailIngestIT`** (`@IntegrationTest` via `WeatherIngestSupport`):
  - all eight sources are registered and disabled;
  - Amtraker: a move under 150 m within `minInterval` writes no version; a NW → W heading change writes a SAMPLE
    version; an older fix is LATE with no version;
  - Digitraffic: 32 trains, one lookup with `Digitraffic-User`, `trainType` on the head, the lookup counter.
- **Updated:** `FamilyDefaultsTest`, `FieldSpecsContractTest`.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL.
- `lintOpenApi` and the Postman generator were not needed: `docs/OpenAPI.json` is unchanged.
- No project tests were run (feature prompt).
- **Scratch check:** a throwaway `main` in the scratchpad, outside the repository and run with Gradle's JDK 21:
  - `DescriptorValidator.problems` and `IngestPolicyValidator.problems` are empty for all eight descriptors (only the
    usual "not significant" warnings);
  - keyframe ceilings: 5 min for the aircraft, 4 min for `digitraffic-rail`, 3 min 30 s for `amtraker`;
  - every fixture was parsed to confirm the counts and values used in the assertions.
- **Workspace guard** `python3 ~/Documents/GitLab/meshdtn/style/test_us_english.py`: ran 14 tests, OK.

## Notes for the next prompt

1. **081 onward:**
   - `hostPacing` and `accessDeniedReasons` are available to any descriptor;
   - `ReadsbResponses.map` is the shared check for hosted readsb APIs;
   - adapters still do not import each other.
2. **097 checkpoint:**
   - first run of every 080 test;
   - `RailIngestIT` uses `WeatherIngestSupport`, which has four new base-URL properties;
   - `AdsbfiAdaptersTest` depends on wall-clock timing (WireMock `loggedDate`, a 1.5 s delay), which a loaded machine
     could make flaky;
   - fix the pre-existing `FamilyDefaultsTest` gap (Issue 1);
   - 074 Issue 1 (constructors in 070–073) still applies.
3. **099 (fusion):** `adsbfi-region-*`, `adsbfi-mil`, `adsblol-*` and `airplaneslive-mil` share `externalId = hex`
   across sources.
4. **109:** license strings: adsb.fi "personal, non-commercial … attribution", airplanes.live "non-commercial …
   attribution", Amtraker "ODC-By 1.0", Digitraffic "CC BY 4.0". MLAT records (`quality.positionSource =
   CALCULATED`) need per-record `LOCAL_ONLY`.
5. **143:**
   - recapture per the fixture READMEs, including a daytime Digitraffic capture;
   - retry airplanes.live (still a 403?);
   - re-read the airplanes.live and Amtraker terms.
6. **144 (Flyway):** no schema change. Adapter state for `digitraffic-rail`: `rail:trains`, `rail:lookups`.
7. **Owner decisions pending:** the five questions above, plus those carried from 012–079.
