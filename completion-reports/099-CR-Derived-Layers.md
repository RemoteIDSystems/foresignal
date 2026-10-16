# 099-CR-Derived-Layers

| | |
|---|---|
| Prompt | 099-Derived-Layers.md |
| Status | Complete (post-review fixes applied 2026-10-16; see the last section) |
| Implementation commit | `aa72eb40206f50c0193e334e189af2bc4fa8aebb` |
| Post-review fix commit | `fd8d534` (`099: fix post-review defects`) |
| Date | 2026-10-15 (fixes 2026-10-16) |

## Summary

Implemented the derived layers as DERIVED sources on the prompt 052 runtime. Eighteen generators
consume stored and live records through an observation feed and publish ordinary records, so the
trigger engine, SSE, tiles, search, briefings and MCP use them unchanged. Every generator is
coverage-aware: absence of evidence is never reported as evidence of absence, and a window touched by a
change-bus gap or a live drop is marked `degraded-input` and suppressed from absence-based events.
Compiles; every test class the prompt lists is written (not run, per the guardrail).

- **Runtime extensions** (`ai.foresignal.derive`): `Observation` (`sourceId`, `externalId`, `kind`,
  `observedAt`, `geometry`, `payload`, `positionSource`); `ObservationFeed` and
  `DefaultObservationFeed` (the merged current view, subscribed to the change and live buses for gap
  detection; reads filter on `observed_at` in SQL and return the newest entities first, and a read that
  reaches its cap is counted as `foresignal.derive.feed.truncated`); `CoverageService` and
  `DefaultCoverageService` (health, `last_data_at`, `Coverage`, `statusFor`, `densityNear`);
  `DensityIndex` (an H3 res-5 count ring using `H3Cells.gridDisk`); `LayerStatus`
  (`ok|insufficient-data|no-coverage|degraded-input`); `StatefulGenerator`; `DerivedMetrics`
  (`foresignal.derive.eval.duration{generator}` and `foresignal.derive.state.size{generator}`).
  `DerivedContext`/`DefaultDerivedContext` expose `observations()` and `coverage()`; `DerivedRunner`
  wires them, times each invocation and publishes the state size. `H3Cells` gains `gridDisk`,
  `cellToAddress` and `averageEdgeLengthKm`.
- **Registry rule change**: `DescriptorValidator` allows a DERIVED source to carry a `recordTtl` only
  when its `historyTier` is TRACK (the fused-aircraft layer). Recorded as a deviation from
  Weather-Imagery-Design §2.2.
- **Generators** (`ai.foresignal.source.adapters.derived`): shared base `LayerGenerator` (families
  resolved against the `SourceRegistry`, 60 s tick, `maxEntities`, `includePrivate`, and a startup
  reconcile that hands every live record to `adopt`), `LayerPayload` (the common payload and
  quantizers), `DerivedSources`, `GeoBuffers`, `LayerSupport`, `ReferenceData` (cached World Port Index
  ports with harbor buffers, submarine cables, PortWatch chokepoint points, OurAirports) and
  `Chokepoints` (bundled `derived/chokepoints.json`). The eighteen sources are `derived-gps-jamming`,
  `derived-ais-gap`, `derived-loiter`, `derived-rendezvous`, `derived-deception`, `derived-holding`,
  `derived-emergency-squawk`, `derived-chokepoint`, `derived-mil-naval`, `derived-aircraft`,
  `derived-corroboration`, `derived-port-calls`, `derived-cpa`, `derived-felt-locally`,
  `derived-cable-risk`, `derived-dark-vessel-match`, `derived-lightning-holdover`,
  `derived-hf-blackout`. All are STANDARD, ALL, `recordTtl=null`, disabled by default, parameterized,
  health-tracked, and declare their input families; the fused source is TRACK with a 5-minute TTL.
- **Spec/docs**: `docs/Source-Catalog.md` "Derived layers (prompt 099)" replaces the stub with one row
  per generator (inputs, cadence, thresholds, coverage rule). `docs/Operator-Guide.md` gains a "Derived
  intelligence layers (prompt 099)" section (enabling, inputs, `enter`/cooldown rule recipes, and
  "absence of evidence is not evidence of absence").

## Files created / modified

Created (main): `derive/` (`Observation`, `ObservationFeed`, `DefaultObservationFeed`,
`CoverageService`, `DefaultCoverageService`, `DensityIndex`, `LayerStatus`, `StatefulGenerator`,
`DerivedMetrics`); `source/adapters/derived/` (`DerivedSources`, `LayerPayload`, `GeoBuffers`,
`LayerSupport`, `ReferenceData`, `Chokepoints`, `LayerGenerator`, the eighteen generator classes,
`AircraftFusionGenerator`); `resources/derived/chokepoints.json`.

Created (test): `source/adapters/derived/` (`AircraftFusionTest`, `GpsJammingTest`, `AisGapCoverageTest`,
`CableRiskTest`, `DarkVesselMatchTest`, `LightningHoldoverTest`, `HfBlackoutTest`, `CorroborationTest`,
`FeltLocallyTest`, `LoiterTest`, `HoldingPatternTest`, `ChokepointTest`, `PortCallTest`,
`RendezvousTest`, `CpaTest`, `MilNavalTest`, `EmergencySquawkTest`, `DeceptionTest`, `DerivedLayersIT`);
post-review: `source/adapters/derived/LayerHarness`, `derive/DefaultObservationFeedTest`.

Modified: `derive/DerivedContext`, `derive/DefaultDerivedContext`, `derive/DerivedRecords`,
`derive/JdbcDerivedRecords` (a bbox-overloaded `current`; post-review `currentSince`),
`derive/DerivedRunner`, `geo/H3Cells`, `config/StartupOrder` (`OBSERVATION_FEED = 26`),
`source/DescriptorValidator`, `testsupport/weather/FakeDerived`, `docs/Source-Catalog.md`,
`docs/Operator-Guide.md`.

## Deviations from the prompt

- **The observation feed is pull-based.** `ObservationFeed.recent` reads `record_head ⋈ record_live`
  (a version payload joined with the freshest live state), and the feed subscribes to the change and
  live buses only to record gap/drop times. It does not hold its own per-version history. A generator
  that needs a rolling window keeps it in its own state. The read filters on `observed_at` in SQL and
  orders newest first, so a cap drops the stalest entities (post-review).
- **Ticks use the 052 runtime, not `@Scheduled`.** Each generator declares its tick through
  `DerivedInputs.tick()` (60 s by default; 5 or 10 min where the prompt names a cadence), and the
  runtime runs it on the source's own virtual thread. The per-generator `maxEntities` parameter caps
  in-memory state.
- **Identity uses hex or MMSI, not the 098 entity id**, because the current-record view does not expose
  the open entity link; 098 entities remain the authoritative identity for records that carry one.
- **Corroboration independence is by source id** (there is no 109 reliability grade yet), and
  reliability is emitted as `F`; credibility is 2 (≥ 2 independent), 3 (one) or 6 (unknown).
- **The `derived-felt-locally` IPE coefficients are unverified.** The 089 ShakeMap is stored as MMI
  contours, not a sampleable grid, so the nearest stored contour's MMI is used for `method: shakemap`
  and the Atkinson & Wald (2007) equation (California west of −105°, CEUS east) for
  `method: ipe-aw2007`. The class Javadoc cites the paper and flags the coefficient tables for
  verification.
- **`derived-lightning-holdover` uses `mrms-qpe-24h` as the available proxy for the 72-hour QPE** (no
  72-hour QPE product exists); unknown coverage is not treated as dry.
- **`derived-hf-blackout` approximates the path class** by the sender's Maidenhead field and the
  receiver DXCC (radio-spot records carry no continent pair) and calls any UTC hour 06–18 sunlit.
- **`derived-cable-risk` excludes service ship types (31, 32, 52, 53, 55) as the cable-ship test**;
  AIS has no dedicated cable-ship type.
- **`derived-dark-vessel-match` is idle with a health note** because prompt 088 did not build a SAR
  detection source; the join logic is present for when one exists. Its `externalId` uses the ISO
  `gapStart` text where the prompt says `gapStartEpoch`; left unchanged post-review because it is a
  persisted wire value (no records exist, since the generator is idle) — the owner may decide.
- **`chokepoint` buffers come from the point geometry of the `chokepoint-transit` records** (the
  PortWatch adapter stores only Points) matched to the bundled `chokepoints.json` radii by name; an
  unknown chokepoint gets the default 50 km.
- **Startup reconciliation re-adopts records for `derived-loiter` and `derived-gps-jamming` only**
  (post-review). Their records re-enter the generator's state so the normal rules end a stale one; the
  other layers rebuild their state from the feed and keep their records until their own rules end them.
  Rebuilding rolling windows from recent versions is not implemented.
- **OpenAPI/Postman unchanged.** The prompt adds no operations; the derived kinds are documented at
  runtime through each descriptor's `fieldSpecs`, and a `jamming-cell` example already exists from the
  `gpsjam-daily` adapter.

## Issues found / open questions

- Generators read their whole input family's current records each tick (bounded by `maxEntities`).
  The `DerivedRecords.current(...bbox...)` overload exists for a spatial prefilter and `derived-cpa`
  uses its area.
- `DefaultObservationFeed` records a single process-wide `lastDegradedAt`; a change-bus gap is not
  attributed to a source, so every generator treats its window as degraded. This is the conservative
  reading of the prompt and never invents an absence.
- The derived-source id sets are resolved from the registry once and cached; a source family slot added
  at runtime after start is not picked up until the runner restarts.

## Tests written (not run)

- One unit test class per generator: `AircraftFusionTest` (post-review: MLAT beats TIS-B),
  `GpsJammingTest` (post-review: reconcile adopts live cells), `AisGapCoverageTest`, `CableRiskTest`,
  `DarkVesselMatchTest`, `LightningHoldoverTest`, `HfBlackoutTest`, `CorroborationTest`,
  `FeltLocallyTest`, `LoiterTest`, `HoldingPatternTest`, `ChokepointTest`, `PortCallTest`,
  `RendezvousTest`, `CpaTest`, `MilNavalTest`, `EmergencySquawkTest`, `DeceptionTest`.
- Post-review, the generator tests that the original report over-described are now behavioral, driven
  tick by tick through `LayerHarness` over `FakeDerived`:
  - `AisGapCoverageTest`: no gap below density 3 (and one at 3), none before 30 minutes, none from a
    degraded window or a failing source; reappearance closes the gap with `gapEnd`, minutes, displacement
    and implied knots, tombstoned six hours later; a never-returning vessel expires after 12 hours; the
    cap counts drops.
  - Boundaries the prompt names: `LoiterTest` 0.49 vs 0.51 nm and 29 vs 30 min (and uneven fix spacing),
    `HoldingPatternTest` 299° vs 301° (and the stable id and five-minute end), `DeceptionTest`
    1,399 vs 1,401 kn (MLAT/TIS-B never checked) and a duplicate MMSI 49 vs 51 km apart within 120 s.
  - `LightningHoldoverTest`: a dry cell with flashes is a candidate, a FIRMS detection 3 km away five
    hours later confirms with the delay, 6 km does not, a wet cell or an unknown QPE creates none, and
    flashes are not counted twice.
  - `DarkVesselMatchTest`: a detection inside the reach (measured to the detection time) and window is
    matched; outside reach or window it is not; no SAR source → idle with a health note.
  - `HfBlackoutTest`: a planted 75% spot drop against 14 days of the same hour is flagged while healthy,
    not when failing, not at night; several ticks within the hour do not pollute the baseline.
- `DefaultObservationFeedTest` (post-review): the time filter applies before the cap, the newest
  entities are kept, a capped read is counted.
- `DerivedLayersIT` (`@WebIntegrationTest`, rewritten post-review): every layer registers as DERIVED,
  and the five required scenarios — a jamming cell appears, survives a sparse window as
  `insufficient-data` and is tombstoned after two recovered windows; an unchanged high cell writes no
  new versions over six windows; an `enter` rule on `level == high` fires once; restart reconciliation
  tombstones a stale loiter; a LiveBus drop marks the window `degraded-input` and suppresses the AIS
  gap (with a control where the gap opens). Input health and density are stubbed through a
  `@MockitoBean CoverageService` whose window status still reads the real feed's drop state.

## Compile result

`./gradlew compileJava compileTestJava` — BUILD SUCCESSFUL (also after the post-review fixes).

## Notes for the next prompt

- The observation feed and coverage service are the reuse point for 100 (windowed rules and anomalies)
  and the later intelligence prompts: `ctx.observations()` and `ctx.coverage()`.
- Derived sources are disabled by default; enable their inputs first. Each is STANDARD with no TTL
  except `derived-aircraft` (TRACK, 5-minute TTL, the one allowed DERIVED TTL).
- `foresignal.derive.eval.duration{generator}`, `foresignal.derive.state.size{generator}` and (post-review)
  `foresignal.derive.feed.truncated` are the metrics.

## Post-review fixes (2026-10-16)

Fix commit `fd8d534`. Every finding was re-verified against the code before it was changed.

1. **Feed truncation hid recent entities (HIGH, fixed).** `JdbcDerivedRecords` ordered by
   `source_id, external_id` with `LIMIT maxEntities` and the time filter was applied later in Java, so
   recent entities that sorted late were never seen and nothing counted it. The new
   `DerivedRecords.currentSince` filters `l.observed_at >= since` in SQL and orders
   `l.observed_at DESC`; `DefaultObservationFeed` counts and logs (rate-limited) a capped read.
2. **AIS gaps grew without bound (MEDIUM, fixed).** An open gap whose vessel never returns is closed
   after `FORGET` (12 h) with an unknown end (`expired`, `insufficient-data`) and tombstoned six hours
   later; closed gaps are tracked by record id (so a new gap may open within the six hours); `seen` is a
   `HashSet`; a gap not opened at the cap is counted. While there: `gapEnd` is the reappearance fix (it
   was the tick time) and `impliedKn` is in knots (it was km/h).
3. **MLAT and TIS-B were indistinguishable (LOW, fixed).** The fusion tie-break ranks the raw
   `posSource`: ADS-B, then MLAT, then TIS-B, then other.
4. **Defects found while writing the behavioral tests (fixed):** `derived-holding` required a span of
   exactly 10:00 (trimming kept ≤ 10 min and the test required ≥ 10 min), used a moving id and
   tombstoned the bare hex — now "within 10 minutes", a stable `{hex}:{start}` and an end five minutes
   after the last turn; `derived-loiter` had the same exact-span defect (the window now keeps the
   shortest tail spanning 30 min) and orphaned the record of a vessel that disappeared;
   `derived-lightning-holdover` added a cluster's flashes again on every tick; `derived-dark-vessel-match`
   measured reach to the tick instead of the detection; `derived-hf-blackout` kept 14 entries (about two
   hours at a 10-minute tick) instead of 14 days of the same hour, so its baseline never existed; the
   startup reconcile only cleared state, leaving records nothing could end (loiter and jamming now
   re-adopt them).
5. **`DefaultObservationFeed` resets its degraded state with the store** (`IngestStateReset`), so a
   simulated drop in one test does not degrade the next.

**Inaccuracies corrected in this report:** the original report claimed boundary tests for 0.49 vs
0.51 nm, 299° vs 301° and 1,399 vs 1,401 kn that did not exist (written now); `AisGapCoverageTest`'s "no
gap below density 3" only checked a constant (now behavioral); `LightningHoldoverTest`,
`DarkVesselMatchTest` and `HfBlackoutTest` tested none of the required behaviors (rewritten); and
`DerivedLayersIT` covered none of the five required scenarios (rewritten) — it also autowired
`TestRecords`, which that context does not provide.
