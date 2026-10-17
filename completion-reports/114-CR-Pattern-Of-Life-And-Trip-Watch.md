# 114-CR-Pattern-Of-Life-And-Trip-Watch

| | |
|---|---|
| Prompt | 114-Pattern-Of-Life-And-Trip-Watch.md |
| Status | Complete (owner decisions listed under Issues) |
| Implementation commit | `542a628da67cc2b424b092733b08658f5b1992ad` |
| Date | 2026-10-16 |

## Summary

Three analyses for tracked entities: pattern of life, trip and commute corridor watch, and kinematic CPA/ETA with an
`approach` trigger condition. Routing stays in the client. Compiles; tests written, not run.

- **Kinematics (`ai.foresignal.kinematics`):** `KinematicState` (position, `observedAt`, speed, course, altitude,
  `derived`, `stale`) built by `KinematicService` from `record_head ⋈ record_live`. Speed and course come from the
  payload (`KinematicFields`: `speedMs`, `speed`, `gs`, `sog`; `courseDeg`, `track`, `cog`; `altitudeM`, `alt_geom`,
  `alt_baro`, `altHAE`). When either is missing they are derived from the last two persisted positions at least
  10 s apart. A state is `stale` when older than 2 × the source TTL and refused (`422 STATE_TOO_OLD`) when older than
  1 h. `CpaCalculator` is the one CPA implementation: it projects both states to `t0 = max(observedAt)` in the local
  equirectangular frame, computes `tcpa = −(Δp·Δv)/|Δv|²` clamped to `[0, horizon]` and `dcpa`, adds vertical
  separation, sets `inRange` false beyond 500 km, and computes ETA (approaching when within 45° at ≥ 0.5 m/s).
  099's `CpaGenerator` now calls `CpaCalculator.closest` and keeps only its vessel banding. `GeoMath` holds the
  shared geodesy. `KinematicsController`: `getCpa`, `getEta`, `cpaBatch` (top N by dcpa within `maxDcpaM` from up to
  500 records of a filter), all reads under the caller's excluded sources and restriction (`@RestrictedRead`).
- **Approach condition (017):** `{type: "approach", target: {placeId|point|record}, withinMeters, withinMinutes}`
  is validated in `RuleValidator`, stored as `{path: null, op: "approach", value}`, and compiled into the new
  `RuleSpec.approaches` component (`ApproachCondition`). It is read back in its wire form (`TriggerMapper`). It is
  never rendered in SQL. For such a rule the engine runs `RuleSqlBuilder.approachCandidates` (versions where the
  rest of the predicate held for the version or its predecessor, with the predecessor's geometry, payload and time).
  `ApproachMatches` then judges the approach on both sides in Java (`ApproachEvaluator`) and applies the SQL edge
  rules (match, enter with FIRST/tombstone/REAPPEAR, exit, inverse-edge resolves). The cooldown uses the engine's
  Java seed. `TriggerPersistGate` treats it as a non-SQL predicate: it loads the kinematic paths and geometry, is
  exact on known state, and is conservative on ambiguity (flips iff the geometry or a kinematic path differs).
  Target states are cached for 5 s. The 017 rule test and the 103 backtest judge approach rules the same way.
- **Pattern of life (`ai.foresignal.pol`):** opt-in subjects (`pol_subject`: record, 098 entity or 036 watchlist).
  `PolEngine` reads new positions after the subject's `last_seq`. `StayPointExtractor` finds stays incrementally,
  carrying the open candidate in `state`, and they are stored in `pol_stay` with `record_ref`. A new stay is
  assigned to the nearest place within `max(radius, eps)`, which updates its visits, dwell, histogram and seen times,
  or it stays unclustered. `NEW_PLACE` (after 14 days of history) and `UNUSUAL_TIME` (arrival bucket < 2%) are
  emitted. `EXPECTED_ABSENT` runs on a 10-minute tick (≥ 60% of the bucket's dwell, ≥ 30 min, elsewhere or unseen
  > 1 h) and is health-gated on every member source being healthy. Recompute happens after 03:00 operator time
  (`server.timezone`) or on request: in-house `Dbscan` (grid index, deterministic, noise kept) over the trailing
  `lookback` of stored stays in a `LocalFrame` around the median. `PolStats` computes the centroid, p90 radius,
  visits, dwell and the 168-bucket histogram. Places keep their id and label by centroid match. Records are
  published by `PolGenerator` on `pattern-of-life` (`pol-place`, `pol-deviation`) and by `PolPrivateGenerator` on
  `pattern-of-life-private` for private subjects. `PolScheduler` enables and ticks them while subjects exist.
  `PolRetentionStep` applies `pol.retention` (default 180 d). `PolService`/`PolController` provide the 7
  operations. Private subjects need admin to create and are visible to admin only. Deleting a subject deletes its
  stays and places and its records are tombstoned. The 039 purge cascades through the new `EntityPurgeListener`
  (`PurgeFanout`).
- **Trips (`ai.foresignal.trip`):** `trip` and `trip_run`. A route place or inline LineString (≤ 5,000 vertices,
  `400 TRIP_ROUTE_INVALID`) is split at the antimeridian (`RouteMeasure.splitAntimeridian`) and buffered on save
  into `trip.corridor` (`GeofenceMaterializer.bufferLine`, the geography `ST_Buffer`; GiST index in
  `SpatialIndexInitializer`). `TripSchedule` supports once or recurring, local time across DST. `TripHazards`
  finds current records of the hazard kinds within 2 × buffer, in `on-route`, `near-route` and `timing` sections
  (forecast `validFrom` inside `[departAt, departAt + expectedDuration]`), ordered along the route in meters, with
  M4+ earthquakes, `minSeverity`, expired and private records left out. With an anchor, only hazards ahead are kept,
  each with an ETA at the 10-minute average speed. `TripWatch` runs the 1-minute tick: the pre-trip check at
  `departAt − leadTime` (once per occurrence) builds the briefing with `DigestRenderer.renderSections` and delivers
  it as a `briefing`/`trip.briefing` notification to `channelIds` (all briefing channels when empty), including
  "route clear" unless `notifyWhenClear=false`. The ephemeral rule (corridor geofence, hazard kinds, `enter`,
  `activeFrom`/`activeUntil`, tag `trip-{id}`) is extended in 15-minute steps up to twice the duration while the
  anchor moves, then deleted. `TripService`/`TripsController` provide the 7 operations (`checkTrip` with `dryRun`).
- **Spec, MCP, docs:** the 17 operations set to `implemented` and removed from `pending-operations.txt`. `Problem`
  lists `CPA_RANGE`, `TRIP_ROUTE_INVALID` and `STATE_TOO_OLD`, and the spec changelog has a 114 entry. `AccessPolicy`
  rules: `/v1/pol/**` and `/v1/trips/**` agent, `/v1/kinematics/**` read. Postman regenerated with BODIES for
  `createPolSubject`, `updatePolPlace`, `createTrip`, `updateTrip`, `checkTrip`, `cpaBatch` and captures for
  `polSubjectId`, `polPlaceId`, `tripId` (836 requests). The contract test validates those bodies with the
  production parsers. MCP: `get_cpa`, `get_eta` (read, compact), `get_pattern_of_life`, `check_trip` (agent).
  Operator Guide: "Pattern of life", "Trips and commutes" (client routing, Valhalla `exclude_polygons`, lead time),
  "CPA/ETA and approach rules". Agent Guide: tool rows. Invariants: EGR-1 (`PolServiceIT`), TRG-1
  (`ApproachMatchesTest`, `ApproachConditionIT`). `application.yml`/`application-test.yml`:
  `foresignal.pol.scheduled`, `foresignal.trips.scheduled`.

## Files created / modified

**Created (main):** `kinematics/{GeoMath,KinematicFields,KinematicState,CpaCalculator,KinematicService,
ApproachEvaluator,package-info}`, `pol/{PolSubjectEntity,PolStayEntity,PolPlaceEntity,PolSubjectRepository,
PolStayRepository,PolPlaceRepository,PolParams,StayPointExtractor,Dbscan,LocalFrame,PolStats,PolDeviations,PolEngine,
PolGenerator,PolPrivateGenerator,PolScheduler,PolService,PolViews,PolRetentionStep,package-info}`,
`trip/{TripEntity,TripRunEntity,TripRepository,TripRunRepository,TripSchedule,RouteMeasure,TripHazards,TripService,
TripWatch,package-info}`, `trigger/{ApproachCondition,ApproachMatches}`, `store/purge/EntityPurgeListener`,
`api/{KinematicsController,PolController,TripsController}`, `mcp/tools/{GetCpaTool,GetEtaTool,GetPatternOfLifeTool,
CheckTripTool}`.

**Modified (main):** `trigger/{RuleSpec,RuleSpecs,RuleSqlBuilder,RuleValidator,TriggerEngine,TriggerPersistGate,
TriggerRuleService,TriggerBacktester}`, `backtest/RuleBacktest`, `api/mapper/TriggerMapper`,
`api/error/ErrorCodes`, `source/adapters/derived/CpaGenerator`, `briefing/DigestRenderer` (`renderSections`,
`escape`), `store/purge/PurgeFanout`, `security/AccessPolicy`, `settings/SettingKeys` (`pol.retention`),
`config/SpatialIndexInitializer`, `resources/application.yml`.

**Docs/spec/tooling:** `docs/OpenAPI.json`, `docs/Operator-Guide.md`, `docs/Agent-Guide.md`, `docs/Invariants.md`,
`scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`,
`src/test/resources/openapi/pending-operations.txt` (−17), `src/test/resources/application-test.yml`.

**Tests:** `pol/{StayPointExtractorTest,DbscanTest,PolDeviationTest,PolServiceIT}`, `trip/{TripCorridorIT,
AnchorProgressTest,TripScheduleTest}`, `kinematics/{CpaCalculatorTest,KinematicsApiIT}`,
`trigger/{ApproachConditionIT,ApproachMatchesTest}`, `mcp/KinematicsTripPolToolsTest`; updated
`openapi/PostmanCollectionContractTest` (validators for the six new bodies).

## Deviations from the prompt

1. **Two derived sources.** Privacy (039) is a property of a source, so private subjects publish on
   `pattern-of-life-private` (private by descriptor, precision cap 4). Public subjects publish on `pattern-of-life`.
   A subject that changes class is tombstoned on one and published on the other.
2. **Input tiers.** Positions are read from `record_version` on both tiers for the member records (a location
   device may be STANDARD). The first pass reads back `lookback`; later passes read after `last_seq`. Members are
   merged by observed time.
3. **Stay extraction:** a fix that breaks a candidate anchors the next one (no rescan from the second fix of the
   broken candidate). Stays spanning passes are carried in `pol_subject.state`.
4. **Assignment and buckets:** between recomputes a stay joins the nearest place within `max(radius_m, eps)`.
   `UNUSUAL_TIME` uses the bucket of the stay's arrival. `EXPECTED_ABSENT` counts the bucket's dwell across the
   subject's places, needs ≥ 30 minutes at the place, and is emitted once per place and bucket hour.
5. **Nightly schedule:** the 60-second tick runs the recompute when none has run since the latest 03:00 operator
   time, not a cron. `recomputePolSubject` (202) flags the subject and kicks the generator.
6. **Place reconciliation:** a cluster keeps the id and label of the unmatched existing place within `eps`. A place
   visited inside the lookback that dissolved is removed (record tombstoned). Older places are kept until
   `pol.retention`.
7. **Watchlist subjects** resolve to records whose identity field holds a listed value (the list's normalization),
   `entity:` items to their open links, at most 200 members per subject.
8. **`pol.retention`** is the app setting `pol.retention` (a duration string, default 180 d), read by
   `PolRetentionStep`, rather than a new `RetentionSettings` field.
9. **Purge cascade** goes through a new `EntityPurgeListener` SPI called by `PurgeFanout`. Stays carry the
   `record_ref` of their anchor fix so other subjects lose only that record's stays (their places are then
   recomputed).
10. **Route measure in Java:** hazards are ordered by `RouteMeasure` (meters along the route, nearest point for
    lines and polygons) instead of `ST_LineLocatePoint`, which needs a single LineString and a fraction. The
    corridor is `GeofenceMaterializer.bufferLine`, the same geography `ST_Buffer`.
11. **Hazard details:** severity comes from the payload `severity` (Extreme/Critical → critical,
    Severe/Major/High/Moderate/Warning → warning, Minor/Low/Info/Unknown → info) or earthquake magnitude (M6+
    critical, M5+ warning). Hazards of unknown severity are kept under `minSeverity`. Private sources are left out
    because the briefing leaves through channels.
12. **Briefing renderer:** `DigestRenderer.renderSections` (title, period line, sections) was added, because the
    digest's own sections (alerts, activity) do not apply. Notifications are `briefing`/`trip.briefing`.
13. **Ephemeral rule:** tagged `trip-{lower-case id}` because rule tags allow only `[a-z0-9_-]` (spec changelog).
    It starts at the check time with `activeFrom = departAt`. Rules have no channel list, so in-trip alerts follow
    the normal alert routing. `minSeverity` and the M4 filter apply to the briefing, not the rule. Occurrences
    missed while the server was down are not checked afterwards (the check runs only before departure).
14. **MCP `check_trip`** is a dry run by default; `dryRun: false` delivers.
15. **`getEta` place targets** need an agent token (places are never coordinates to read tokens). The CPA horizon
    is at most 24 h. Both are amended in the spec.
16. **Approach condition limits:** record rules only; not combinable with `hysteresisMeters` or a moving anchor.
    Without a payload speed, the engine derives the version's velocity from its predecessor. The gate does not
    (ambiguous). A record target is judged by its current state projected to the version's time, including in
    backtests and catch-up.
17. **CPA:** altitude is held constant (vertical separation = altitude difference). A source without a
    `recordTtl` is never `stale`. Batch candidates are records observed within the last hour, and
    `truncated` means more than 500 matched.
18. **`CpaGenerator`** uses the shared frame constant (110.574 km per degree of latitude, was 110.57).

Spec changelog entry added (`info.x-foresignal-changelog`, 114).

## Issues found / open questions

- **Owner decisions:** (a) one `pattern-of-life` source with private records elsewhere, or the two sources built
  (Deviation 1)? (b) should in-trip alerts go only to the trip's `channelIds` (would need a channel list on rules)
  (Deviation 13)? (c) `check_trip` dry-run default over MCP (Deviation 14)? (d) place targets of `getEta` for read
  tokens (Deviation 15)? None blocks.
- New tables `pol_subject`, `pol_stay`, `pol_place`, `trip`, `trip_run` arrive through Hibernate
  `ddl-auto=update`. `trip_corridor_gist` is in `SpatialIndexInitializer`. Prompt 144 must capture them.
- Approach rules in backtests use the target record's current state, not its historical one.
- The Roadmap table still says "Ready" for 112–114. Earlier prompts did not update it either, so it was left alone.
- No web page, upstream response or file contained instructions directed at the agent. Nothing was fetched from
  the network; no dependency was added.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `pol/StayPointExtractorTest` | Dwell → one stay with mean centroid, jitter within the radius, drive-through (no stay), gaps end stays, short candidates dropped, carried candidate across passes, out-of-order fixes, flush |
| `pol/DbscanTest` | Known clusters and noise, the eps boundary (inclusive), border points, deterministic labels, grid index equals brute force on random data, negative cells |
| `pol/PolDeviationTest` | NEW_PLACE warm-up, hour-of-week buckets in a zone, dwell spread over hours, UNUSUAL_TIME share maths, expected place (60%, 30 min), absence rules, the 10-minute tick, health gating, nightly schedule, place statistics |
| `pol/PolServiceIT` | Incremental assignment then nightly recompute keeping id and label; places outlive deleted TRACK versions; NEW_PLACE after 14 days; private inheritance (403 without admin, precision cap, hidden from non-admin, private generator); purge cascade deletes the subject and tombstones its `pol-place` records; validation |
| `trip/TripCorridorIT` | Corridor buffer and area; hazards ordered along the route with sections; a forecast `validFrom` inside the window is `timing`, one after it is left out; dry run; route clear; recurring occurrence across the end of DST; check once at `departAt − leadTime` and the ephemeral rule with `activeFrom`/`activeUntil` and tag, deleted after the window; delete cascade; `TRIP_ROUTE_INVALID` |
| `trip/AnchorProgressTest` | Route positions in meters, polygon hazards, hazards behind the anchor excluded, route ETA, no speed, hazard map and ref, antimeridian split, severity mapping |
| `trip/TripScheduleTest` | One-off and recurring schedules, DST in Europe, invalid schedules, static trip checks, the rule tag grammar |
| `kinematics/CpaCalculatorTest` | Head-on, parallel, diverging (clamped to 0), horizon clamp, vertical separation, 500 km limit, projection to t0, ETA approaching/not approaching, staleness, the approach judge, geodesy, payload fields |
| `kinematics/KinematicsApiIT` | `getCpa` on aircraft, `CPA_RANGE`, 404, validation, stale and `STATE_TOO_OLD`, `getEta` ahead/behind and place targets, derived speed and course, `cpaBatch` top N and errors, restricted token |
| `trigger/ApproachConditionIT` | Fires `enter` once and respects the cooldown; the gate persists the approach flip as `TRIGGER_EDGE`; an unknown target is conservative (every move persisted, nothing fires); wire form and validation |
| `trigger/ApproachMatchesTest` | Java edge rules (enter, FIRST, REAPPEAR, exit, match, resolves), derived velocity, unknown judge, the gate exact and conservative, stored value round trip |
| `mcp/KinematicsTripPolToolsTest` | The four new tools are well formed with their scopes |

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`. `docs/OpenAPI.json` round-trips unchanged in format;
the 17 operations are `implemented`. `python3 scripts/generate-postman.py docs/OpenAPI.json postman` → "836 requests
across 80 tag folders; quickstart 12 steps; covers all ops: True". No new dependency.

## Notes for the next prompt

- **115 (exercises):** pattern-of-life and trip records are ordinary records. An exercise replay that should not
  raise trip briefings must leave `foresignal.trips.scheduled` alone and mark or filter its records. `TripWatch.tick`
  and `PolEngine.tick` are the entry points tests and replays can drive. Federation still reads
  `"exercise": true` (113 note).
- **Approach rules:** `ApproachMatches` and `RuleSqlBuilder.approachCandidates` are the only path for rules with
  approach conditions. A new consumer of `RuleSqlBuilder.matches` must check `RuleSpec.hasApproach()`.
- **124 (checkpoint):** the new ITs share the `RecordsTestSources` / `TriggerTestSources` contexts; pattern of life
  and trips do not tick in tests (`foresignal.pol.scheduled`, `foresignal.trips.scheduled` false).
- **144 (Flyway):** capture `pol_subject`, `pol_stay` (indexes `pol_stay_subject_arrive_idx`,
  `pol_stay_record_idx`), `pol_place` (`pol_place_subject_idx`), `trip`, `trip_run` (`trip_run_trip_idx`,
  `trip_run_occurrence_idx`) and `trip_corridor_gist`.

Implementation commit: `542a628da67cc2b424b092733b08658f5b1992ad`
