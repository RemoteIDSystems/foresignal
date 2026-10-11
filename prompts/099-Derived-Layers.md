# 099 — Derived Layers

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/099-CR-Derived-Layers.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–134).
- **Previous Completion Report:** read `completion-reports/098-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Weather-Imagery-Design.md` §2.2 (row "Modes": DERIVED), §7.1 (principles for derived records: emit on change, no TTL, explicit tombstones, startup reconciliation, unknown is not clear, `automated: true`), §7.4 (enter/exit recipes). Its "072"/"070 (old 057)" means this prompt; its "039" means 052.
- The 052 Completion Report: the DERIVED runtime in `ai.foresignal.derive` (generator SPI, inputs, reconciliation, health)
- `docs/Source-Catalog.md` "Design notes from VEL" (ais_gap, loiter, rendezvous, deception, GPS-jamming thresholds, freshest-observation fusion) and "Derived layers"
- `docs/Gap-Analysis.md` §8 item 54 (fusion by hex, corroboration/credibility, port calls, kinematic CPA, felt-locally), §5 rows "Reliability grading" and "Kinematic CPA"
- `docs/Core-Data-Model.md` §5.3 (aircraft and vessel significant fields), §10.1 (ChangeBus, LiveBus, `onLiveDropped`), §14 (coverage)
- Completion Reports for 062, 063, 069, 085 (aircraft and vessel payload fields: `nac_p`, `nic`, `squawk`, `emergency`, `dbFlags`, `shipType`, `navStatus`), 088 (PortWatch chokepoints), 089 (ShakeMap), 092 (h3 dependency), 094 (World Port Index, OurAirports), 098 (entities)
- Product Charter FR-54

## Objective

Implement the derived layers as DERIVED sources on the 052 runtime. Each generator consumes stored and live records and publishes ordinary records, so the trigger engine, SSE, tiles, search, briefings and MCP use them unchanged. Every generator is **coverage-aware**: absence of evidence is never reported as evidence of absence, and a section with insufficient input says so instead of clearing.

## Tasks

### 1. Runtime extensions (`ai.foresignal.derive`)

Check what 052 built and add what is missing:
- **`ObservationFeed`:** merges ChangeBus versions (payloads loaded by `seq = ANY(?)`) and LiveBus updates into `Observation(sourceId, externalId, kind, observedAt, geometry, payload, positionSource)`, filtered per generator by source ids and kinds. Generators need LiveBus because SAMPLED persistence thins tracks.
- **Gap handling:** on `onLiveDropped` or a ChangeBus gap, mark every generator's affected window `status: degraded-input` until one full window has passed; never emit an absence-based event (AIS gap, squawk cleared) from a degraded window.
- **`CoverageService`:** per input source, health status, `last_data_at` and `Coverage`; plus `densityNear(sourceId, point, radiusKm, window)` (distinct entities seen nearby, from an in-memory H3 res-5 count ring). Generators call it before asserting any absence.
- **Ticks:** `@Scheduled` evaluation per generator (default 60 s), on virtual threads, with a per-generator in-memory state cap (`maxEntities`, default 200,000) that counts and drops beyond the cap.
- **Registry rule change:** DERIVED requires `recordTtl = null` **except** when `historyTier=TRACK`, which is allowed only for the fusion source (§3.10). Record this deviation from Weather-Imagery-Design §2.2.
- **Common payload:** `automated: true`, `notAWarning: true`, `detector: {id, version}`, `confidence` (0–1, banded to 0.1), `status` (`ok|insufficient-data|no-coverage|degraded-input`), `evidence` (≤ 20 `{sourceId, externalId, observedAt}`, kept out of `significantFields`). Quantise numeric fields (distances 0.5 km, ratios 0.1, durations 5 min) so versions are written only on meaningful change.
- **Reconciliation at startup:** rebuild rolling windows from `record_live` and recent versions; tombstone derived records whose condition no longer holds.

### 2. Generator sources

All are STANDARD, ALL, `recordTtl=null`, disabled by default, parameterised, health-tracked, `licence` = the most restrictive input licence (for example ODbL for adsb.lol inputs), and declare `inputs` (source ids or families). Identity uses 098 entities when present, otherwise hex or MMSI.

| Source id | Kind / externalId | Algorithm and defaults |
|---|---|---|
| `derived-gps-jamming` | `jamming-cell` / `h3:{cell}` or `deg:{lat}:{lon}` | Inputs: readsb-family aircraft sources (not OpenSky, which lacks NACp). 15-min rolling window, evaluated every 5 min. Count distinct airborne aircraft per cell with ADS-B positions only (exclude MLAT and TIS-B). Bad = NACp < 8 **or** NIC < 7. Needs ≥ 3 aircraft; ratio ≥ 0.3 → `medium`, ≥ 0.5 → `high`. Grid param `grid=h3` (res 4, the `com.uber:h3` dependency added in 092) or `deg1` (1° cells). Emit only cells at medium or above; tombstone after 2 consecutive windows below medium **with** ≥ 3 aircraft. Fewer than 3 aircraft keeps the last level with `status: insufficient-data`, tombstoned after `staleAfter` 2 h. Geometry: cell polygon. |
| `derived-ais-gap` | `ais-gap` / `{mmsi}:{gapStartEpoch}` | A vessel with SOG > 1 kn and `navStatus` not moored or at anchor, and outside port buffers, has no fix for 30 min. Open only when `densityNear` shows ≥ 3 other vessels reported by the same source within 50 km during the gap; otherwise do nothing. Payload `gapStart, lastSog, lastCog, open`. On reappearance: `gapEnd, gapMinutes, displacementKm, impliedKn`, `open=false`; tombstone 6 h later. |
| `derived-loiter` | `loiter` / `{entity}` | Vessel: ≥ 4 fixes spanning ≥ 30 min, all within 0.5 nm of their centroid, outside port buffers and not moored. Ends when a fix lies > 0.5 nm from the centroid (tombstone). |
| `derived-rendezvous` | `rendezvous` / `{a}:{b}` (sorted) | Two vessels within 0.5 nm, both SOG < 3 kn, for ≥ 15 min, ≥ 5 nm from any World Port Index port, excluding tug, pilot, SAR and port-tender ship types (31, 32, 50–53, 55). Grid-bucketed neighbour search (cells about 1 nm). Tombstone when separated > 1 nm for 10 min. |
| `derived-deception` | `deception` / `{entity}` | Checks: duplicate MMSI at two positions > 50 km apart within 120 s (any sources); vessel implied speed > 80 kn between fixes ≥ 10 s apart (skip SOG 102.3, HSC types 40–49, WIG types 20–29); aircraft implied ground speed > 1,400 kn between ADS-B fixes ≥ 10 s apart (skip MLAT and TIS-B). Payload lists `checks[]` with counts; tombstone after 1 h without recurrence. |
| `derived-holding` | `holding-pattern` / `{hex}:{startEpoch}` | Airborne aircraft, GS < 300 kn, altitude within ±1,000 ft, cumulative signed track change > 300° within 10 min. Geometry: convex hull of the fixes. Payload `turnsDeg, durationMin, altitudeFt, nearestAirport` (094 OurAirports, ≤ 50 km). Ends after 5 min without turning. |
| `derived-emergency-squawk` | `emergency-squawk` / `{hex}` | `squawk` 7500, 7600 or 7700, or a readsb `emergency` other than `none`, seen in ≥ 2 observations ≥ 10 s apart (or from 2 sources). Payload `squawk, meaning` (unlawful interference / radio failure / emergency), `unverified: true`; geometry = the live position, updated when it moves ≥ 10 km. Tombstone after the code has been absent for 2 min with fresh observations (no fresh data keeps it with `status: insufficient-data`). |
| `derived-chokepoint` | `chokepoint-activity` / `{chokepointId}`, and `chokepoint-uturn` / `{mmsi}:{chokepointId}:{epoch}` | Chokepoint polygons: 088 PortWatch chokepoints buffered by a per-chokepoint radius from a bundled `chokepoints.json` (id, name, radiusKm). Per vessel inside: course change > 60° within 10 min, U-turn > 150°, or speed drop > 50% to < 3 kn. Per chokepoint, hourly banded counts `transits, uTurns, stopped` with coverage status. Baselines and anomaly scoring are 100's job. |
| `derived-mil-naval` | `mil-naval-cooccurrence` / `h3:{res3 cell}` | Within 30 min: ≥ 1 military aircraft (`adsblol-mil`, `airplaneslive-mil`, or readsb `dbFlags & 1`) and ≥ 1 naval vessel (AIS ship type 35, or a 098 entity attribute `military=true`) within the same res-3 cell or 100 km. Payload: counts plus `aircraft[]` and `vessels[]` (≤ 20 each). Tombstone after 30 min without co-occurrence. |
| `derived-aircraft` | `aircraft` / `{hex}` | **Fusion by hex** across `adsblol-*`, `airplaneslive-*`, `adsbfi-*`, `opensky-states`, `local-adsb-*`. The **freshest observation by `observedAt` wins** (never merge order); ties prefer ADS-B over MLAT over TIS-B over other, then local over network. Payload: the winning fields plus `sources[]`, `primarySource`, `positionSource`. TRACK, TTL 5 min, §5.3 global-aircraft SAMPLED defaults and significant fields. Feeds the readsb `aircraft.json` sink (110). |
| `derived-corroboration` | `corroboration` / `{kindFamily}:{clusterUlid}` | Clusters event records across **independent** sources: earthquakes (Δt ≤ 60 s, ≤ 100 km, \|ΔM\| ≤ 0.8: USGS, EMSC, FDSN); fires (≤ 1 km, ≤ 1 h: FIRMS, GOES FDC); conflict events (same 0.1° cell and UTC day: GDELT, ACLED, UCDP); outages (same country or ASN, overlapping interval: IODA, Cloudflare Radar). Payload `members[]`, `independentSources`, `credibility` on the Admiralty 1–6 scale (≥ 2 independent → 2 "probably true"; 1 → 3 "possibly true"; unknown → 6), `reliability` from 109 grades when present, else `F`. "F" and "6" mean unknown, never false. |
| `derived-port-calls` | `port-call` / `{mmsi}:{portNumber}:{arrivalEpoch}` | Vessel inside a World Port Index port buffer (094; radius by harbour size: small 3 km, medium 6 km, large 10 km, very small 2 km) with SOG < 1 kn for ≥ 30 min → arrival; leaving the buffer → `departure, durationH`; tombstone 24 h after departure. Without fixes for the whole stay, emit `departure` as unknown, never guessed. |
| `derived-cpa` | `cpa` / `{a}:{b}` | Param `aoi` (place or bbox, required). Vessel pairs both > 3 kn: CPA and TCPA from position, SOG and COG in a local equirectangular frame; flag CPA < 0.5 nm with 0 < TCPA ≤ 20 min. Payload `cpaNm, tcpaMin` (banded); tombstone once TCPA < 0 or the pair diverges. |
| `derived-felt-locally` | `felt-report-estimate` / `{eventId}:{placeId}` | For each earthquake (USGS, EMSC, FDSN) and each 036 place within 1,000 km: if a 089 ShakeMap grid exists, sample MMI at the place (`method: shakemap`); otherwise use the Atkinson & Wald (2007) intensity prediction equation (`method: ipe-aw2007`) with the California or CEUS coefficient set chosen by place longitude (west of −105° → California). **Verify the coefficients against the paper before coding** and cite it in the class Javadoc. Emit when MMI ≥ 2.0; payload `mmi` (0.5 bands), `distanceKm`, `feltLikely` (MMI ≥ 2.5), `method`. |
| `derived-cable-risk` | `cable-proximity-risk` / `{mmsi}:{cableId}:{startEpoch}` | Seabed-infrastructure guard. A vessel that is not a cable ship (AIS ship type) with navigational status "at anchor", or sustained speed 0.3–3 kn, within 2 km of a 094 `submarine-cables` route, outside any port or anchorage buffer (World Port Index point + 5 km), for ≥ 30 min. **Possible anchor drag** when "at anchor" and moving 0.5–3 kn for ≥ 15 min within 1 km of a route. Severity by duration, number of cables within 2 km and drag flag. Coverage-aware: a vessel losing AIS near a cable adds `aisGapNearCable`. |
| `derived-dark-vessel-match` | `dark-vessel-candidate` / `{mmsi}:{gapStartEpoch}:{detectionId}` | Joins a `derived-ais-gap` record with an unmatched SAR vessel detection (088 `sar-vessel-detection`, when enabled) inside the vessel's reachable area (last fix + max speed × time, capped at 200 km) within the gap window ± 6 h. Payload: distance, time offset, detection length estimate where provided, `matchScore` (distance and time normalised). Labelled "candidate; SAR detections are not identifications". |
| `derived-lightning-holdover` | `holdover-fire-risk` / `h3:{res6 cell}:{utcDay}` | GLM flash clusters (048) over cells with low recent precipitation (053 MRMS 72 h QPE < 2.5 mm, or the model field from 051 when MRMS is outside coverage) and low relative humidity where available. Each candidate is watched for 72 h: a FIRMS or GOES FDC detection (065, 048) within 5 km sets `ignitionConfirmed` and the delay; otherwise the record expires. Severity by flash count × dryness. |
| `derived-hf-blackout` | `hf-propagation-anomaly` / `{band}:{pathClass}:{utcHour}` | From 082 `pskreporter-ft8`: per amateur band and path class (continent pair), spot count and median SNR per hour against a 14-day same-hour baseline; a drop of ≥ 60% in spots (or ≥ 10 dB median SNR) on sunlit paths while the source is healthy marks a candidate HF blackout. Payload includes the concurrent SWPC X-ray class (077) when known (`flareConcurrent`), so the layer can be checked as an early indicator of flare-driven absorption. |
- Rule recipes in docs use `enter`/`exit` or explicit cooldown, never bare `match` (Weather-Imagery-Design §7.1).

### 3. Coverage and safety rules

- No absence-based record (AIS gap, squawk cleared, port departure inferred) is emitted when the input source is not `healthy`, or when the window was `degraded-input`.
- Detection payloads say "automated detection" and never accuse: deception and jamming records state the check that matched, not intent.
- Private sources (039) are never inputs unless the generator's `includePrivate` param is set, and then the derived source is itself `private`.
- Bound memory and CPU: per-generator `maxEntities`, grid-bucketed pair searches (no O(n²) over the world), CPA restricted to its AOI. Expose `foresignal.derive.eval.duration{generator}` and `foresignal.derive.state.size{generator}`.

### 4. Spec, Postman, docs

- `docs/OpenAPI.json`: no new operations. Document the derived kinds under the kind schema (`/v1/kinds/{kind}/schema` via each descriptor's `fieldSpecs`) and add an example `jamming-cell` Feature. Regenerate Postman with `python3 scripts/generate-postman.py docs/OpenAPI.json postman`.
- `docs/Source-Catalog.md`: replace the "Derived layers (prompt 057)" section with one row per generator (inputs, cadence, thresholds, coverage rule).
- `docs/Operator-Guide.md` "Derived layers": enabling, inputs, rule recipes (jamming `level == high` enter; emergency squawk enter; AIS gap enter with cooldown), and "absence of evidence is not evidence of absence".

## Tests to write (do not run)

- One unit test per generator with synthetic observation sequences and a controllable clock: thresholds at and either side of each boundary (2 vs 3 aircraft, ratio 0.29/0.30/0.49/0.50, 29 vs 30 min, 0.49 vs 0.51 nm, 299° vs 301°, 1,399 vs 1,401 kn, 49 vs 51 km in 120 s).
- `AisGapCoverageTest`: no gap opens when nearby density is below 3 or the source is degraded; a gap opens and closes with the displacement fields.
- `AircraftFusionTest`: freshest `observedAt` wins regardless of arrival order; ADS-B beats MLAT on ties; `sources[]` is correct.
- `CableRiskTest`: anchored vessel near a cable outside port buffers flagged; inside a port buffer not flagged; drag pattern detected; cable ships excluded.
- `DarkVesselMatchTest`: SAR detection inside the reachable area and window matched; outside not; no SAR source → generator idle with health note.
- `LightningHoldoverTest`: dry cell with flashes creates a candidate; a later FIRMS detection confirms with delay; wet cell creates none.
- `HfBlackoutTest`: planted spot drop on sunlit paths with the source healthy flagged; source failing → no record.
- `CorroborationTest`: USGS + EMSC within tolerances cluster with credibility 2; same-source duplicates do not count as independent.
- `FeltLocallyTest`: ShakeMap preferred; IPE values for documented reference cases from the paper.
- `DerivedLayersIT` (`@IntegrationTest`, `TestRecords`): jamming cell appears, survives a sparse window as `insufficient-data`, and is tombstoned after recovery; an unchanged condition over 30 min writes no new versions; restart reconciliation tombstones a stale loiter; a LiveBus drop marks `degraded-input` and suppresses gap events; an `enter` rule on `level == high` fires once.

## Acceptance criteria

- Every generator is a registered DERIVED source with defaults that pass `IngestPolicyValidator`, and disabled by default.
- No generator emits an absence-based event without coverage evidence.
- Steady-state conditions write no versions (emit on quantised change only).
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
2. **Commit** all changes with the message `099: Derived Layers`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/099-CR-Derived-Layers.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `099: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
