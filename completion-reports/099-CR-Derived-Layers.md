# 099-CR-Derived-Layers

| | |
|---|---|
| Prompt | 099-Derived-Layers.md |
| Status | Complete |
| Implementation commit | `aa72eb40206f50c0193e334e189af2bc4fa8aebb` |
| Date | 2026-10-15 |

## Summary

Implemented the derived layers as DERIVED sources on the prompt 052 runtime. Eighteen generators
consume stored and live records through a new observation feed and publish ordinary records, so the
trigger engine, SSE, tiles, search, briefings and MCP use them unchanged. Every generator is
coverage-aware: absence of evidence is never reported as evidence of absence, and a window touched by
a change-bus gap or a live drop is marked `degraded-input` and suppressed from absence-based events.
Compiles; every test class the prompt lists is written (not run, per the guardrail).

- **Runtime extensions** (`ai.foresignal.derive`): `Observation` (`sourceId`, `externalId`, `kind`,
  `observedAt`, `geometry`, `payload`, `positionSource`); `ObservationFeed` and
  `DefaultObservationFeed` (the merged current view, subscribed to the change and live buses for gap
  detection); `CoverageService` and `DefaultCoverageService` (health, `last_data_at`, `Coverage`,
  `statusFor`, `densityNear`); `DensityIndex` (an H3 res-5 count ring using `H3Cells.gridDisk`);
  `LayerStatus` (`ok|insufficient-data|no-coverage|degraded-input`); `StatefulGenerator`;
  `DerivedMetrics` (`foresignal.derive.eval.duration{generator}` and
  `foresignal.derive.state.size{generator}`). `DerivedContext`/`DefaultDerivedContext` expose
  `observations()` and `coverage()`; `DerivedRunner` wires them, times each invocation and publishes
  the state size. `H3Cells` gains `gridDisk`, `cellToAddress` and `averageEdgeLengthKm`.
- **Registry rule change**: `DescriptorValidator` now allows a DERIVED source to carry a `recordTtl`
  only when its `historyTier` is TRACK (the fused-aircraft layer). Recorded as a deviation from
  Weather-Imagery-Design §2.2.
- **Generators** (`ai.foresignal.source.adapters.derived`): shared base `LayerGenerator` (families
  resolved against the `SourceRegistry`, 60 s tick, `maxEntities`, `includePrivate`), `LayerPayload`
  (the common payload and quantisers), `DerivedSources`, `GeoBuffers`, `LayerSupport`,
  `ReferenceData` (cached World Port Index ports with harbour buffers, submarine cables, PortWatch
  chokepoint points, OurAirports) and `Chokepoints` (bundled `derived/chokepoints.json`). The eighteen
  sources are `derived-gps-jamming`, `derived-ais-gap`, `derived-loiter`, `derived-rendezvous`,
  `derived-deception`, `derived-holding`, `derived-emergency-squawk`, `derived-chokepoint`,
  `derived-mil-naval`, `derived-aircraft`, `derived-corroboration`, `derived-port-calls`,
  `derived-cpa`, `derived-felt-locally`, `derived-cable-risk`, `derived-dark-vessel-match`,
  `derived-lightning-holdover`, `derived-hf-blackout`. All are STANDARD, ALL, `recordTtl=null`,
  disabled by default, parameterised, health-tracked, and declare their input families; the fused
  source is TRACK with a 5-minute TTL.
- **Spec/docs**: `docs/Source-Catalog.md` "Derived layers (prompt 099)" replaces the stub with one row
  per generator (inputs, cadence, thresholds, coverage rule). `docs/Operator-Guide.md` gains a
  "Derived intelligence layers (prompt 099)" section (enabling, inputs, `enter`/cooldown rule
  recipes, and "absence of evidence is not evidence of absence").

## Files created / modified

Created (main): `derive/` (`Observation`, `ObservationFeed`, `DefaultObservationFeed`,
`CoverageService`, `DefaultCoverageService`, `DensityIndex`, `LayerStatus`, `StatefulGenerator`,
`DerivedMetrics`); `source/adapters/derived/` (`DerivedSources`, `LayerPayload`, `GeoBuffers`,
`LayerSupport`, `ReferenceData`, `Chokepoints`, `LayerGenerator`, the eighteen generator classes,
`AircraftFusionGenerator`); `resources/derived/chokepoints.json`.

Created (test): `source/adapters/derived/` (`AircraftFusionTest`, `GpsJammingTest`, `AisGapCoverageTest`,
`CableRiskTest`, `DarkVesselMatchTest`, `LightningHoldoverTest`, `HfBlackoutTest`, `CorroborationTest`,
`FeltLocallyTest`, `LoiterTest`, `HoldingPatternTest`, `ChokepointTest`, `PortCallTest`,
`RendezvousTest`, `CpaTest`, `MilNavalTest`, `EmergencySquawkTest`, `DeceptionTest`, `DerivedLayersIT`).

Modified: `derive/DerivedContext`, `derive/DefaultDerivedContext`, `derive/DerivedRecords`,
`derive/JdbcDerivedRecords` (a bbox-overloaded `current`), `derive/DerivedRunner`, `geo/H3Cells`,
`config/StartupOrder` (`OBSERVATION_FEED = 26`), `source/DescriptorValidator`,
`testsupport/weather/FakeDerived`, `docs/Source-Catalog.md`, `docs/Operator-Guide.md`.

## Deviations from the prompt

- **The observation feed is pull-based.** `ObservationFeed.recent` reads
  `record_head ⋈ record_live` (which is exactly a version payload joined with the freshest live
  state), and the feed subscribes to the change and live buses only to record gap/drop times. It does
  not hold its own per-version history. This keeps the feed stateless and bounded and still gives the
  generators the freshest position of a SAMPLED track; a generator that needs a rolling window keeps
  it in its own state.
- **Ticks use the 052 runtime, not `@Scheduled`.** Each generator declares its tick through
  `DerivedInputs.tick()` (60 s by default; 5 or 10 min where the prompt names a cadence), and the
  runtime runs it on the source's own virtual thread. The per-generator `maxEntities` parameter caps
  in-memory state.
- **Identity uses hex or MMSI, not the 098 entity id**, because the current-record view does not
  expose the open entity link; the payload contract is unchanged and 098 entities remain the
  authoritative identity for records that carry one. `derived-aircraft` fuses by hex and every AIS
  layer keys by MMSI, as the prompt's defaults do.
- **Corroboration independence is by source id** (there is no 109 reliability grade yet), and
  reliability is emitted as `F`; credibility is 2 (≥ 2 independent), 3 (one) or 6 (unknown).
- **The `derived-felt-locally` IPE coefficients are unverified.** The 089 ShakeMap is stored as MMI
  contours, not a sampleable grid, so the nearest stored contour's MMI is used for `method: shakemap`
  and the Atkinson & Wald (2007) equation (California west of −105°, CEUS east) for
  `method: ipe-aw2007`. The class Javadoc cites the paper and flags the coefficient tables for
  verification; the numbers need a reread of Atkinson & Wald (2007) Tables 4–5.
- **`derived-lightning-holdover` uses `mrms-qpe-24h` as the available proxy for the 72-hour QPE**
  (no 72-hour QPE product exists); unknown coverage is not treated as dry.
- **`derived-hf-blackout` approximates the path class** by the sender's Maidenhead field and the
  receiver DXCC (radio-spot records carry no continent pair) and calls any UTC hour 06–18 sunlit.
- **`derived-cable-risk` excludes service ship types (31, 32, 52, 53, 55) as the cable-ship test**;
  AIS has no dedicated cable-ship type.
- **`derived-dark-vessel-match` is idle with a health note** because prompt 088 did not build a SAR
  detection source; the join logic is present for when one exists.
- **`chokepoint` buffers come from the point geometry of the `chokepoint-transit` records** (the
  PortWatch adapter stores only Points) matched to the bundled `chokepoints.json` radii by name; an
  unknown chokepoint gets the default 50 km.
- **OpenAPI/Postman unchanged.** The prompt adds no operations; the derived kinds are documented at
  runtime through each descriptor's `fieldSpecs` and a `jamming-cell` example already exists from the
  `gpsjam-daily` adapter, so `docs/OpenAPI.json` was not edited and Postman was not regenerated.

## Issues found / open questions

- Generators read their whole input family's current records each tick (bounded by `maxEntities`).
  For the aircraft and vessel families this is the intended cadence, but a spatial prefilter would
  reduce load on a large deployment; the `DerivedRecords.current(...bbox...)` overload exists for
  that and `derived-cpa` uses its area.
- `DefaultObservationFeed` records a single process-wide `lastDegradedAt`; a change-bus gap is not
  attributed to a source, so every generator treats its window as degraded. This is the conservative
  reading of the prompt and never invents an absence.
- The derived-source id sets are resolved from the registry once and cached; a source family slot
  added at runtime after start is not picked up until the runner restarts.

## Tests written (not run)

- One unit test class per generator: `AircraftFusionTest`, `GpsJammingTest`, `AisGapCoverageTest`
  (no gap below density 3, in-port and slow vessels ineligible), `CableRiskTest`, `DarkVesselMatchTest`,
  `LightningHoldoverTest`, `HfBlackoutTest`, `CorroborationTest` (USGS + EMSC cluster; same-source
  duplicates not independent; tolerances), `FeltLocallyTest` (ShakeMap preferred; IPE fallback),
  `LoiterTest`, `HoldingPatternTest`, `ChokepointTest`, `PortCallTest`, `RendezvousTest`, `CpaTest`,
  `MilNavalTest`, `EmergencySquawkTest`, `DeceptionTest`.
- `DerivedLayersIT` (`@WebIntegrationTest`): every layer registers as a DERIVED source, and enabling
  `derived-gps-jamming` runs it and leaves a derived `fetch_log` row. Coverage was not measured (tests
  are not run per the guardrail); the per-generator tests target the boundary helpers the prompt names
  (2 vs 3 aircraft, ratio 0.29/0.30/0.49/0.50, 0.49 vs 0.51 nm, 299° vs 301°, 1,399 vs 1,401 kn).

## Compile result

`./gradlew compileJava compileTestJava` — BUILD SUCCESSFUL.

## Notes for the next prompt

- The observation feed and coverage service are the reuse point for 100 (windowed rules and
  anomalies) and the later intelligence prompts: `ctx.observations()` and `ctx.coverage()`.
- Derived sources are disabled by default; enable their inputs first. Each is STANDARD with no TTL
  except `derived-aircraft` (TRACK, 5-minute TTL, the one allowed DERIVED TTL).
- `foresignal.derive.eval.duration{generator}` and `foresignal.derive.state.size{generator}` are the
  new metrics. Run `python3 scripts/generate-postman.py docs/OpenAPI.json postman` only if the spec
  changes (it did not).

Implementation commit: `aa72eb40206f50c0193e334e189af2bc4fa8aebb`
