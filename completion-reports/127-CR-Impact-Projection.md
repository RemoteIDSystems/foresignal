# 127-CR-Impact-Projection

| | |
|---|---|
| Prompt | 127-Impact-Projection.md |
| Status | Complete (compile-and-report only under the owner directive of 2026-10-17: tests written, **not run**) |
| Implementation commit | `81561cff1f3b329a931811efe5fb22ab5aa90ab4` |
| Date | 2026-10-17 |

## Summary

This prompt adds the impact projector under `ai.foresignal.impact`. It turns hazard forecasts Foresignal already
ingests into `projected-impact` findings and scored ledger questions. It compiles. **The tests are written but were
not run** (owner directive 2026-10-17): no test result, coverage figure or guard result in this report is measured.

- **New source `nhc-wind-probs`** (`source/adapters/nhc/NhcWindProbsAdapter`, `NhcWindProbMapper`). 067 and 075 did
  not ingest NHC wind-speed probabilities. The source:
  - is a DERIVED-free POLLING adapter on the 073 `ArcGisQueryClient`, like `nhc-cyclone-cones`;
  - reads the layers "Probabilistic Winds 34 kts", "50 kts" and "64 kts" of
    `https://mapservices.weather.noaa.gov/tropical/rest/services/tropical/NHC_tropical_weather_summary/MapServer`.
    Checked on 2026-10-17 from the service's `?f=json` document: ids 30, 31 and 32 (children of 29 "Probabilistic
    Winds"), polygon layers whose field `percentage` holds `<5%`, `5-10%`, `10-20%` … `80-90%`, `>90%`. The ids are
    resolved by name once a day;
  - writes kind `wind-probability`, analytic class `event`, 1825 days. One record per band, `externalId`
    `{kt}kt:{low}-{high}`, valid for the 120 hours after the advisory, with tombstones per layer;
  - has an originator mapping (`noaa-nhc`), a Source Catalog row and a spec kind note.
- **Hazard inputs** (`impact.hazard`):
  - `HazardForecast` has every field of the spec schema, plus `hazardKey`, `sourceRefs`, `pointRadiusKm` and `details`.
  - `HazardAdapters` is pure. It maps:
    - NHC bands of the newest advisory → published polygons;
    - a cone → `tropical-track`, model `none`, with the two-thirds note;
    - SPC day 1–2 `torn`/`wind`/`hail` → published point probabilities from `dn`, else the label (hatching excluded),
      radius 25 miles;
    - SPC `cat` → `severe-categorical`, `none`;
    - WPC ERO days 1–3 → the category ranges' lower bound, with the range stored;
    - NWPS gauges → `gauge-category`, calibrated by `GaugeCalibration`;
    - storm tracks → `track-error-ellipse` with no probability.
  - `GaugeCalibration` (pure) works from the gauge's history. A sample is the first observation of a UTC day whose basis
    category (forecast, else current under persistence) reaches the flood-relevant category. A hit is the observed
    category reaching it by the end of the horizon plus 6 hours. The probability is `hits / n` with a Jeffreys interval;
    fewer than 10 samples gives no probability.
  - `HazardProbability` gives `P(hazard at asset)`: the highest band containing the asset. Published polygons count
    within `assetBufferKm`; point probabilities add no buffer.
  - `HazardReader` reads the current records behind a hazard key, the changed keys in a seq range, and gauge history.
- **Conditional rates** (`impact.rate`):
  - `ImpactPairs` loads and validates `intel/impact-pairs.json`: 8 hazard classes, 4 impact families, 10 pairs.
  - `JdbcCoOccurrenceCounter` counts hazard occurrences (admin1 × UTC day, placed by `ST_Intersects` with
    `ref_admin1`) and those followed by the impact family in the same region within the lag. The SQL is built by
    `SelectorSql` from validated field names, with bound values.
  - `ImpactRateService.rate(hazardClass, impactFamily, regionClass)` gives `{rate, interval, n, nImpacts, source,
    status}`:
    - the 121 `BaseRateCalculator.jeffreys` interval;
    - `insufficient` below n = 20;
    - region classes are the 121 continent classes;
    - 1825 days of history ending one lag before now;
    - a 24-hour cache.
- **Projector** (`ImpactProjector`, `impact-projection` 1.0.0, pure): `P(impact) = P(hazard in the region, or at an
  exposed asset in it) × rate`, with the interval as the product of the bounds. It records skipped pairs (insufficient
  history, no region class), drops projections below `impact.minProbability` (0.05), keeps one per family and region
  and the top 20 per hazard.
- **Exposure** (`ImpactExposure`):
  - the admin1 regions of the hazard area;
  - 104 facilities inside it (`InfraImpactService.affected`, depth 2, `possible`), with dependency paths rebuilt from
    the edges (from, to, hops, weakest-edge label);
  - 036 places;
  - the 101 exposure summary of the hazard's part of each region.
- **Runs** (`ImpactProjectionService`, `ImpactWatcher`, `ImpactCursorConsumer`):
  - **Watcher:** a ChangeBus subscriber with the durable cursor `impact.cursor`, in the consumer clamp. Each hazard is
    debounced 10 minutes after its first unprocessed version. The cursor stops before the oldest waiting hazard.
  - **Findings:** one `projected-impact` finding per (hazard, family, admin1), with `stats {pHazard, pConditional,
    pImpact, interval, n}`, exposed assets, paths, inputs (cones as context for tropical winds), skipped pairs, the
    label and a caveat `MODELED`. Each finding stores its 116 manifest (through `FindingService.record`).
  - **Supersession:** the next advisory's finding supersedes the old one by window overlap. A projection the next run
    no longer produces is superseded explicitly (new `FindingService.supersede`).
  - **Events:** `impact.projected` and `impact.superseded` are sent through `FindingService.announce` and the new
    `ImpactEventSummaries`.
  - **Ledger:** at `pImpact ≥ impact.questionMinProbability` (0.10), `ForecastService.openQuestion` opens a binary
    `event-exists` question, category `weather-impact`, linked to the finding. The `impact` forecaster (registered with
    `ForecasterRegistry.ensure`) records `pImpact`; the 122 baselines add theirs.
- **API, MCP, SSE, docs:**
  - `api/ImpactController` serves `listProjectedImpacts`, `getProjectedImpact` and `listImpactRates`, with
    `AccessPolicy` read rows and Problem code `UNKNOWN_IMPACT`.
  - MCP tools `list_projected_impacts` and `get_impact_rate` (read; also in the analyst's read catalog).
  - The settings `impact.cursor`, `impact.minProbability`, `impact.questionMinProbability` and `impact.assetBufferKm`.
  - The three operations are `implemented` and their lines are gone from `pending-operations.txt`. The spec has
    reconciliations and a changelog entry; Postman was regenerated (no change to the collection).
  - The Operator Guide gains "Impact projection (prompt 127)" and three cross-references; the Agent Guide gains two
    tool rows, the finding type and a wording rule. The Source Catalog gains the `nhc-wind-probs` row and "Impact
    pairs (prompt 127)". `docs/Invariants.md` gains test names (RET-2, MAN-1).

## Files created / modified

**Created (main):**
- `impact/`: `package-info`, `ImpactPairs`, `ImpactRules`, `ImpactExposure`, `ImpactProjector`,
  `ImpactProjectionService`, `ImpactWatcher`, `ImpactCursorConsumer`, `ImpactViews`, `ImpactEventSummaries`,
  `ImpactQueryService`.
- `impact/hazard/`: `package-info`, `HazardForecast`, `HazardRecord`, `HazardAdapters`, `HazardProbability`,
  `GaugeCalibration`, `HazardReader`.
- `impact/rate/`: `package-info`, `CoOccurrenceCounter`, `JdbcCoOccurrenceCounter`, `SelectorSql`,
  `ImpactRateService`.
- `api/ImpactController`, `mcp/tools/{ListProjectedImpactsTool,GetImpactRateTool}`,
  `source/adapters/nhc/{NhcWindProbsAdapter,NhcWindProbMapper}`.
- Resource `intel/impact-pairs.json`.

**Modified (main):**
- `analytics/finding/FindingService`: `supersede`, and the `announce` text covers impact events.
- `analytics/finding/FindingEvent`: `IMPACT_PROJECTED`, `IMPACT_SUPERSEDED`, and `flat` covers `impact.*`.
- `analytics/finding/FindingCaveats`: `MODELED`.
- `outlook/OutlookEventSummaries`: `handles` is narrowed to `outlook.*`/`index.*`, so it no longer swallows impact
  events.
- `notify/NotificationRouter`: `origin-impact` alerts route only to channels that name the rule.
- `analyst/AnalystRunner` (two read tools), `api/error/ErrorCodes`, `security/AccessPolicy`, `settings/SettingKeys`,
  `config/StartupOrder` (`IMPACT = 33`).
- Resource `intel/originators.json`.

**Docs, spec and tooling:** `docs/OpenAPI.json`, `docs/Operator-Guide.md`, `docs/Agent-Guide.md`,
`docs/Source-Catalog.md`, `docs/Invariants.md`, `src/test/resources/openapi/pending-operations.txt` (−3).

**Tests:** the new classes below. Modified:
- `stream/sse/EventSchemaConformanceTest`: payload factories for the two `impact.*` events;
- `retention/ConsumerClampIT`: `impact.cursor` among the intelligence cursors.

## Deviations from the prompt

1. **Admin1 placement by geometry.** `record_region` holds one admin1 per record, from its representative point. A
   probability polygon covers many regions, so hazards and impacts are placed in regions by `ST_Intersects` with
   `ref_admin1`, both for the rates and for the projection.
2. **Rates are Foresignal's own co-occurrence, not 121 `BaseRateService` definitions.** 121 computes weekly rates over
   `reference_history` families, which have no hazard classes such as `tropical-wind-34`, and pools regions. The rate
   is therefore an own-history count per admin1 × day, with 121's Jeffreys interval. `source` is always
   `own-history` and `baseRateId` is null.
3. **Hazard classes are forecast thresholds for NHC, SPC and WPC.** The prompt's pairs are of this form ("tornado ≥
   10%", "excessive-rain moderate"). For tropical winds, "the hazard in an admin1" is taken as a band of 50% or more,
   because Foresignal observes no realized wind field. The conditional rate is then P(impact | likely forecast), and
   the product tends to understate. Gauge pairs use observed categories.
4. **Resolution goes through one trigger rule per impact family.** The 122 `event-exists` resolver has no
   sources/kinds/region parameters. Each family gets a 017 rule (`ImpactRules`) with a deterministic UUID, tags
   `impact`, `origin-impact` and `impact-{family}`, and the family's sources, kinds and conditions. The question
   selects `alertRuleId` with `area.regionCodes [admin1]`, so detectability comes from the rule's sources and kinds.
   The router change keeps those alerts off channels that do not name the rule.
5. **Impact families.**
   - `outage` uses IODA region-level outages only (068). Cloudflare Radar outages (092) name countries, so they cannot
     resolve an admin1 question.
   - `tornado-warning` (NWS alerts) was added for the prompt's "tornado ≥ 10% → NWS tornado warning" pair.
   - `river-flood` gauge categories (079) and `ground-stop` (087) are as specified.
   - The pairs were chosen here: 10 pairs in `intel/impact-pairs.json`.
6. **Gauges without history give no probability.** The prompt allows the forecast (or persistent) category
   "calibrated when history exists". The binding principle says a category is never a probability without a published
   mapping. So a gauge with fewer than 10 calibration samples projects nothing (`details.reason: uncalibrated`).
   - The flood-relevant category is the class's `minCategory`, `moderate`. It is one category for every asset, not a
     category per asset, because 104 nodes carry no flood thresholds.
   - The gauge's area is a 5 km circle.
7. **Storm cells give no probability.** The 105 tracker publishes no error ellipse, so `storm-cell` forecasts carry
   model `track-error-ellipse` with no band (`no-position-error`).
8. **SPC day 3** (`prob`, any severe) and hatching (`SIGN`) are not used: the spec has no hazard type for the former,
   and the latter is not a probability.
9. **Question dedup is per hazard issuance, family and admin1.** The prompt says "dedup per hazard and admin1".
   Families are distinct questions, and the hazard key of NHC probabilities (`nhc-wind-probs:34`) spans every storm, so
   the dedup key is `impact:{hazardKey}:{issuedAt}:{family}:{admin1}`.
   - The question closes at the window start, or an hour after opening once the window has begun (as 120's precursor
     questions do), and resolves by `validTo + lag`.
10. **Supersession.** Besides the overlap rule of `FindingService.record`, a run supersedes the hazard's open
    projections it no longer produces: the successor is the new projection of the same family and region, else none.
11. **Severity** is `significant` at `pImpact ≥ 0.5`, `notable` at 0.2 or more, else `info`. The prompt names none.
12. **Spec reconciliations** (additive, with a changelog entry):
    - the two SSE events carry `id` (they replay from the finding log);
    - `HazardForecast` gains `hazardKey`, `sourceRefs`, `pointRadiusKm` and `details`;
    - a band's `geometry` is no longer required, because lists leave band geometries out and the detail includes them;
    - `ProjectedImpact` gains `admin1Name`, `lagHours` and `questionWindow`; assets gain `band` and paths gain
      `depth`;
    - `UNKNOWN_IMPACT` is in the Problem codes;
    - the `wind-probability` kind note.
13. **Wire names kept as published** (US English rule): the label "modelled; heuristic links may be wrong; not an
    official warning" is the spec's and the prompt's published text, carried in findings and API responses. The new
    caveat code is the US spelling `MODELED`.
14. **Edited by script, not rewritten by hand:** the spec, as in every earlier prompt. The Operator Guide (a new
    section appended and three sentences), the Agent Guide, the Source Catalog and the Invariants were changed by
    script and written whole.

## Issues found / open questions

**Owner decisions:**
- (a) the forecast-threshold hazard classes and the "≥ 50% band" stand-in for realized tropical winds (Deviation 3);
- (b) the built-in pairs and impact families, including leaving Cloudflare Radar out (Deviation 5);
- (c) no gauge probability without 10 calibration samples (Deviation 6);
- (d) retention. `spc-outlooks` keeps 14 days and the WPC ERO sources 90 days, so the SPC and WPC pairs can never
  reach n = 20 by default. Lengthening their retention (or adding a reference import of SPC and WPC history) would
  make those pairs usable;
- (e) question volume: one question per advisory × family × admin1 at 10% or more. During an active hurricane with
  4 advisories a day this could be a few dozen questions a day;
- (f) the impact-family trigger rules write alerts to the inbox on every matching outage, ground stop or tornado
  warning (Deviation 4).

**Doc discrepancy, left for the planner:** `docs/Intelligence-Design.md` §12 lists only `/v1/analytics/impacts/**` and
`list_projected_impacts` for 127. The spec also defines `/v1/analytics/impact-rates`, and the prompt asks for
`get_impact_rate`. Both are built; the design document was not edited.

**Known limits:**
- Rates are computed on request (cached for a day). The first listing of every pair × continent class runs up to 70
  co-occurrence queries.
- Assets are assigned to the region containing their representative point.
- `population` on exposed assets is null; the exposure summary in `inputs` carries the region's population estimate
  when a pack is installed.
- The watcher's waiting hazards are in memory. A restart re-reads them from `impact.cursor`.

**Schema via Hibernate, for prompt 144:** no new table. New settings: `impact.cursor`, `impact.minProbability`,
`impact.questionMinProbability` and `impact.assetBufferKm`.

**Guards not run:** the US English guard and `lintOpenApi` were not run (no-tests directive). The new and changed
files were checked by grep for British spellings; the only hit is the published label of Deviation 13.

**Risks for the owner's first test run:**
- `ImpactProjectorIT` assumes the reference fixture's Texas polygon contains the seeded bands and the plant.
- `ImpactProjectorIT` relies on the trigger engine firing the `outage` rule and tagging the alert `US-TX` before
  `ResolutionService.runOnce`.
- `ImpactControllersIT` expects a `read` test token without private access to see no private projection.

No file, page or tool output contained instructions directed at the agent.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `source/adapters/nhc/NhcWindProbMapperTest` | Band ids and bounds (`<5%`, `>90%`), the 120-hour validity, skips, declared payload keys, layer lookup by name and its format errors, the descriptor |
| `impact/hazard/HazardAdapterTest` | Per hazard type: NHC probability bands (newest advisory only, buffer, band bounds); a cone yields no probability; SPC point probability (no buffer, hatching excluded, label fallback, categorical context only, keys); WPC category ranges; NWPS forecast calibration, the persistence fallback, no probability without calibration or below category, no look-ahead; storm cells without an error |
| `impact/rate/ImpactRateServiceTest` | Co-occurrence rate with its Jeffreys interval on seeded counts, the history window, insufficient below n = 20, unknown pairs, region classes, caching, listing, the SQL the selectors render, pairs-file validation |
| `impact/ImpactProjectorTest` | 40% × 55% = 22% with the product interval, assets and paths per region, skipped pairs, the minimum probability, top 20 and one per family and region, nothing from a cone, the question draft (resolver, window, closing, dedup), the family rule spec |
| `impact/ImpactProjectorIT` | A seeded hurricane wind-probability advisory over Texas with a built infrastructure graph and 25 days of history: a finding with paths, labels and a manifest; a skipped pair; a question forecast by `impact`; an unchanged rerun; supersession by the next advisory; resolution `yes` from a seeded outage; the watcher's debounce and cursor in the clamp; below-threshold projections without questions; superseding without a successor once the bands are tombstoned |
| `impact/ImpactControllersIT` | List order, filters, cursor and 400s; 401 without a token; detail with geometries, 404 `UNKNOWN_IMPACT`, private projections hidden; rates (insufficient without history, paging, 400s); MCP scopes and shapes |

## Compile result

- `./gradlew compileJava compileTestJava` on the committed tree: BUILD SUCCESSFUL.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "844 requests across 80 tag folders; quickstart 12
  steps; covers all ops: True".

## Notes for the next prompt

- **128 (cycles):**
  - Assessments can quote current projections through `ImpactQueryService.list` and the `impact` forecaster's ledger
    scores.
  - `ImpactRateService.clearCache()` belongs after a reference import or a monthly refresh.
- **Tests:** `foresignal.analytics.scheduled` is false in tests, so the watcher does not start. After the table
  truncation:
  - call `ImpactProjectionService.run(hazardKey)` or `ImpactWatcher.scan/runDue/flush`;
  - call `ImpactRateService.clearCache()`, `ImpactRules.reset()` and `ForecasterRegistry.registerBuiltIns()`;
  - load the reference regions (`ReferenceFixtures.loadAll`) and then call `RegionClasses.reset()`.
- **144 (Flyway):** no new table; capture the four `impact.*` settings.

Implementation commit: `81561cff1f3b329a931811efe5fb22ab5aa90ab4`
