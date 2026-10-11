# 114 — Pattern Of Life And Trip Watch

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/114-CR-Pattern-Of-Life-And-Trip-Watch.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–133).
- **Previous Completion Report:** read `completion-reports/113-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §5 rows "Pattern of life, trip watch, commute corridors" (`active_from`/`until`; OSRM can't exclude polygons, only Valhalla can), "Moving-anchor 'near me' geofences", "Kinematic CPA/ETA"; §9 ("Server-side routing or OSRM hazard avoidance" rejected: routing belongs in the client)
- `docs/Core-Data-Model.md` §2 (`observed_at`), §8 (`record_live.kinematic`), §12 (`/versions`, `/history`), §13 (TRACK window, downsampling: positions older than the raw window are thinned)
- Completion Reports of 017 (active windows, `active_from`/`until`, LineString + `bufferMeters` geofences, `near`), 029 (briefing renderer and channels), 036 (places incl. route + buffer, watchlists, moving anchors), 037 (`/track`, simplified tracks), 039 (private sources, precision cap, per-entity purge), 044 (downsampling), 047/074/075/067/101 (hazard kinds: `nws-alert`, road events, fire perimeters, cyclone cones, incidents), 098 (entity registry), 099 (CPA derived layer: reuse its maths)

## Objective

Add three analyses for tracked entities: pattern of life (where an entity dwells and when, learned with DBSCAN, with deviations as records rules can match), trip and commute corridor watch (hazards along a client-supplied route around a scheduled departure), and general kinematic CPA/ETA queries. Routing stays in the client; Foresignal only buffers and watches the geometry it is given.

## Tasks

### 1. Pattern of life (`ai.foresignal.pol`)

- **Opt-in subjects only.** Entity `pol_subject`: `id`, `subject_type` (`record` (sourceId+externalId) | `entity` (098 entity id) | `watchlist` (036; every member)), `ref`, `enabled`, `params` jsonb, `created_by`. Private sources (039) need admin; their outputs inherit `private` and the precision cap.
- **Input:** persisted positions for the subject from `record_version` (TRACK tier, within the track window), read incrementally from a per-subject cursor (`last_seq`). Downsampled ranges (044) are fine: dwell uses observed-time gaps.
- **Stay-point extraction** (before clustering): consecutive fixes within `stayRadiusM` (default 100 m) spanning ≥ `minDwell` (default 10 min) form a stay `{centroid, arrive, depart, fixes}`. Gaps > `maxGap` (30 min) end a stay.
- **DBSCAN** over stay centroids (in-house; ~150 lines):
  - local equirectangular metres around the subject's median position
  - grid index with cell = eps
  - `eps` (default 150 m), `minPts` (default 3 stays)
  - noise stays kept as `unclustered`
  - Deterministic: process stays in arrive order.
  - Recompute nightly at 03:00 local (operator timezone) over the trailing `lookback` (default 28 d of stays, from the stay store, not raw versions); assign new stays incrementally in between.
- **Stores (outlive the TRACK window):**
  - `pol_stay` (`subject_id`, `arrive`, `depart`, `centroid` geometry, `place_id` nullable, `cluster_id`)
  - `pol_place` (`subject_id`, `cluster_id`, `centroid`, `radius_m` (p90 distance), `visits`, `total_dwell`, `hour_of_week` int[168] dwell-minutes histogram, `first_seen`, `last_seen`, `label` (operator-set, e.g. "home"))
  - Retention `pol.retention` (default 180 d) via a `RetentionStep`.
- **Derived source `pattern-of-life`** (`Mode.DERIVED`, STANDARD): publishes
  - kind `pol-place` (one record per place, payload: label, visits, dwell, typical hours)
  - kind `pol-deviation`, with deviation types:
    - `NEW_PLACE` (a stay ≥ `minDwell` outside every place, after ≥ 14 d of history)
    - `UNUSUAL_TIME` (a stay at a known place in an hour-of-week bucket with < 2% of that place's dwell)
    - `EXPECTED_ABSENT` (now in a bucket where the place normally holds ≥ 60% of the subject's dwell, but the subject is elsewhere or unseen for > 1 h; evaluated on a 10-min tick, health-gated: suppressed when the subject's source is not healthy, per the 006 status)
  - Rules (017) match these records like any other; recipes in the docs.
- **API** (agent; private subjects admin):
  - `GET/POST /v1/pol/subjects`, `DELETE /v1/pol/subjects/{id}`
  - `GET /v1/pol/subjects/{id}/places`, `PUT /v1/pol/places/{id}` (label)
  - `GET /v1/pol/subjects/{id}/stays?from&to`
  - `POST /v1/pol/subjects/{id}/recompute`
  - Purge: deleting a subject deletes its stays and places and tombstones its derived records; the 039 per-entity purge also cascades here.

### 2. Trip and commute corridor watch (`ai.foresignal.trip`)

- **Entity `trip`:**
  - `id`, `name`, `route_place_id` (036 route place: LineString + `bufferMeters`) **or** inline `route` (GeoJSON LineString ≤ 5,000 vertices) plus `bufferMeters` (default 2,000, max 50,000)
  - `schedule`: one-off `departAt`, or recurring (`days` of week, `departTime`, `timezone`) for commutes; `expectedDuration`
  - `leadTime` (default 60 min), `hazardKinds[]` (default `nws-alert`, `road-event`, `work-zone`, `fire-perimeter`, `cyclone-cone`, `earthquake` (M ≥ 4), `incident`), `minSeverity`, `channelIds[]`, optional `anchor` (a moving-anchor record from 036, e.g. the operator's OwnTracks device), `enabled`.
- **The route comes from the client.** Document that clients compute it with their own router (Valhalla can exclude polygons; OSRM cannot). Foresignal never calls a routing service. Validate geometry; antimeridian-split long routes.
- **Corridor:** `ST_Buffer(route::geography, bufferMeters)::geometry`, materialised on save into `trip.corridor` with a GiST index.
- **Pre-trip check** (scheduler tick 1 min; fires at `departAt − leadTime` per occurrence):
  - query current records of `hazardKinds` intersecting the corridor, plus forecast hazards whose `validFrom` (008) falls inside `[departAt, departAt + expectedDuration]`
  - order by position along the route (`ST_LineLocatePoint`)
  - render a "trip briefing" via the 029 digest renderer (sections: on route, near route (within 2× buffer), timing)
  - deliver to `channelIds`; store `trip_run` (`trip_id`, `occurrence`, `hazards` jsonb, `status`, `delivered_at`)
  - Quiet result ("route clear") is still delivered unless `notifyWhenClear=false`.
- **In-trip watch** (`departAt` to `departAt + expectedDuration`, extended while the anchor is moving): create an ephemeral trigger rule (017) with a geofence = corridor, source and kind arrays = `hazardKinds`, mode `enter`, `active_from`/`until` = the window, tag `trip:{id}`, and delete it after the window. If 017 lacks `active_from`/`until`, add them per Gap §8.A.7 first.
- **Anchor progress:** with an anchor, compute the remaining route from the anchor's projection onto the route and report hazards **ahead** only, with ETA to each (task 3).
- **API** (agent): `GET/POST /v1/trips`, `GET/PUT/DELETE /v1/trips/{id}`, `POST /v1/trips/{id}/check` (run the pre-trip check now; returns the briefing without delivering when `dryRun=true`), `GET /v1/trips/{id}/runs`.

### 3. Kinematic CPA/ETA (`ai.foresignal.kinematics`)

- **`KinematicState`** from head ⋈ live: position, `observedAt`, speed (m/s) and course (degrees) from payload per the 011 Adapter Guide fields, or derived from the last two persisted positions when absent (≥ 10 s apart). Altitude in metres when present.
- **CPA** (constant velocity, local equirectangular frame at the midpoint as in 099's `derived-cpa`; reject when separation > 500 km → `400 CPA_RANGE`):
  - project both states to a common time `t0 = max(observedAt)`
  - `tcpa = −(Δp·Δv)/|Δv|²` clamped to `[0, horizon]` (default 2 h)
  - `dcpa = |Δp + Δv·tcpa|`
  - include the vertical separation at `tcpa` when both have altitude
  - Extract 099's `derived-cpa` maths into one shared `CpaCalculator` (vessels-only banding stays in 099) and use it here; don't keep two implementations.
- **ETA to a target** (point or place):
  - along-track: `bearingDiff = |course − bearing(target)|`
  - approaching when `bearingDiff ≤ 45°` and speed ≥ 0.5 m/s
  - `eta = distance / (speed · cos(bearingDiff))`
  - otherwise `approaching=false` with no ETA
- **ETA along a trip route:** distance along the route from the anchor's projection to the hazard's projection, divided by the recent average speed (last 10 min).
- **Staleness:** results carry `asOf` and `stale=true` when either state is older than 2× the source TTL; refuse when older than 1 h.
- **API** (read):
  - `GET /v1/kinematics/cpa?a={source}/{externalId}&b={source}/{externalId}&horizon=`
  - `GET /v1/kinematics/eta?record=…&to=placeId|lon,lat`
  - `POST /v1/kinematics/cpa/batch` (one subject against up to 500 records from a filter (kind, bbox); returns the top N by `dcpa` within `maxDcpaM`)
- **Rule integration:** a new condition type `approach` on trigger rules: `{type:"approach", target:{placeId|point|record}, withinMeters, withinMinutes}`. It is evaluated in Java on each processed version of the rule's sources (like the geofence gate) using this service, and fires `enter` when `dcpa ≤ withinMeters` and `tcpa ≤ withinMinutes`.
  - Register it with the `TriggerPersistGate` (017) as a non-SQL predicate. Ambiguous or stale state counts as conservative (Core-Data-Model §11: report a flip iff before ≠ candidate).
  - Document that edge resolution is the poll or flush resolution.

### 4. Spec, MCP, docs

- `docs/OpenAPI.json`: schemas `PolSubject`, `PolPlace`, `PolStay`, `PolDeviation` (kind payload), `Trip`, `TripSchedule`, `TripRun`, `CpaResult`, `EtaResult`, the `ApproachCondition` variant in the trigger condition `oneOf`; every operation above with `x-foresignal-scope`; error codes `CPA_RANGE`, `TRIP_ROUTE_INVALID`, `STATE_TOO_OLD`. Update `AccessPolicy`. Regenerate Postman with BODIES for `createPolSubject`, `createTrip`, `checkTrip`, `cpaBatch`.
- MCP (025): `get_pattern_of_life` (agent), `check_trip` (agent), `get_cpa`, `get_eta` (read; compact output).
- `docs/Operator-Guide.md` "Pattern of life" (privacy: opt-in only, private inheritance, purge), "Trips and commutes" (client routing, Valhalla exclusion example, lead time), "CPA/ETA and approach rules". `docs/Agent-Guide.md`: CPA/ETA tools.

## Tests to write (do not run)

- `StayPointExtractorTest`: synthetic tracks with dwell, drive-through (no stay), gaps, jitter within the radius.
- `DbscanTest`: known clusters, noise, eps boundary, deterministic labels, grid index equals brute force on random data.
- `PolDeviationTest`: NEW_PLACE after the warm-up only, UNUSUAL_TIME bucket maths, EXPECTED_ABSENT tick and health gating.
- `PolServiceIT`: incremental assignment plus nightly recompute; places outlive a TRACK partition drop; private inheritance and precision cap; purge cascade tombstones derived records.
- `TripCorridorIT`: corridor buffer from a LineString; hazards ordered along the route; a forecast hazard within the window via `validFrom`; dry-run check; a recurring occurrence across a DST change; the ephemeral rule created and deleted with `active_from`/`until`.
- `AnchorProgressTest`: hazards behind the anchor excluded; route ETA.
- `CpaCalculatorTest`: head-on, parallel, diverging (tcpa clamped to 0), vertical separation, the 500 km limit; ETA approaching/not approaching; staleness.
- `ApproachConditionIT`: fires `enter` once on approach and respects cooldown; persist-gate flip on approach; stale state conservative.
- `KinematicsApiIT`: endpoints, batch top-N, errors.

## Acceptance criteria

- Pattern of life runs only for opted-in subjects, keeps places beyond the track window, and emits deviation records that rules can match.
- A trip with a client-supplied route sends a pre-trip briefing at `departAt − leadTime` and watches the corridor during the window, with no server-side routing.
- CPA/ETA endpoints and the `approach` condition work on live aircraft and vessel records.
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
2. **Commit** all changes with the message `114: Pattern Of Life And Trip Watch`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/114-CR-Pattern-Of-Life-And-Trip-Watch.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `114: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
