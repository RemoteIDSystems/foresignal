# 124 — Impact Projection

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/124-CR-Impact-Projection.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults, analytic retention, intelligence conventions) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and **`docs/Intelligence-Design.md` is authoritative for the intelligence layer** (situations, findings, precursors, base rates, analogs, the forecast ledger, hypotheses, forecasts, impact projection and analysis cycles).
- **Previous Completion Report:** read `completion-reports/123-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Intelligence-Design.md` §2, §10 (impact projection), §8.2 (resolvers)
- Completion Reports of 036 (places), 047 (`spc-outlooks` probabilistic layers, `nws-alerts`), 052 (DERIVED runtime, sampling), 067 and 075 (NHC cyclones and cones), 068/092 (outage sources), 073 (ArcGIS kit), 079 (NWPS gauges and flood categories), 087 (FAA NAS status), 089 (WPC ERO), 101 (ExposureService), 104 (facility graph, paths, labels), 105 (storm tracks), 118 (findings), 120 (base rates, reference history), 121 (ledger)

## Objective

Turn the hazard forecasts Foresignal already ingests into **probabilistic, scored impact statements**: "Hurricane X: 40% chance of tropical-storm-force winds at Port Y within 48 h; given that, outages are reported in the surrounding admin1 in 55% of comparable cases → 22% chance of an outage report by Thursday 18:00Z." Each statement is a `projected-impact` finding with its inputs and dependency paths, and a ledger question that Foresignal resolves from its own outage, aviation and incident sources.

## Principles (binding)

- **Published probabilities first.** Use a hazard product's own probabilities; never treat a cone or a categorical label as a probability unless the producer publishes the mapping.
- **Conditional rates come from history** (120), with intervals and sample sizes; unknown → no projection, not a guess.
- Labels on every output: "modelled; heuristic links may be wrong; not an official warning".

## Tasks

### 1. Hazard inputs (`ai.foresignal.impact.hazard`)

`HazardForecast` = `{hazardType, source record ref (pinned), validFrom, validTo, geometry or probability field, probabilityModel, issuedAt}`. Adapters from existing records:

| Hazard type | Source | Probability model |
|---|---|---|
| `tropical-wind-34`, `-50`, `-64` | NHC cumulative wind-speed probabilities | published probability polygons (the `5–100%` bands) |
| `tropical-track` | NHC cones (075) | none on its own; used only for context and timing, labelled "the cone covers about two thirds of historical track errors" |
| `tornado`, `wind`, `hail` | SPC day 1–2 probabilistic layers (047) | published probability (e.g. `torn:0.10` → 10% within 25 mi of a point) |
| `severe-categorical` | SPC categorical (047) | not used for probabilities; context only |
| `excessive-rain` | WPC ERO day 1–3 (089) | WPC's published ranges per category (marginal ≥ 5%, slight ≥ 15%, moderate ≥ 40%, high ≥ 70%), using the lower bound and storing the range |
| `river-flood` | NWPS gauges (079) | forecast flood category at the gauge when 079 captured forecasts; otherwise current category with persistence; calibrated by the gauge's historical exceedance rate when history exists |
| `storm-cell` | storm tracks (105) | extrapolated position with the tracker's error ellipse |

- **NHC wind-speed probabilities:** if 067/075 do not already ingest them, add a POLLING source `nhc-wind-probs` on the 073 ArcGIS kit, reading the NHC probabilistic wind layers of the NOAA `tropical` MapServer (verify the service URL and layer ids for 34/50/64-kt cumulative probabilities; record them in the CR and the Source Catalog). Kind `wind-probability`, analytic class `event`.

### 2. Exposure and dependencies

- For each hazard forecast: `ExposureService.exposure(geometry)` (101) for facilities, places and population; places (036) intersecting; 104 dependency paths from exposed facilities to downstream assets (with each path's weakest-edge label).
- **P(hazard at asset):** the probability polygon containing the asset (or the highest band within `assetBufferKm`, default 5 km); for SPC point-probabilities, the published value applies within 25 miles; for river gauges, the probability the forecast category ≥ the asset's flood-relevant category.

### 3. Conditional impact rates

- `ImpactRateService.rate(hazardClass, impactFamily, regionClass)` → `{rate, interval, n, source}` from 120 base-rate machinery over historical co-occurrence of hazard records and impact records (e.g. `tropical-wind-34` in an admin1 → an `outage` record in that admin1 within 48 h; `tornado ≥ 10%` → NWS tornado warning and IODA/Cloudflare outage in the county within 12 h; `excessive-rain moderate` → river gauge at moderate flood within 72 h). Built-in pairs in `src/main/resources/intel/impact-pairs.json`.
- Minimum n = 20 hazard occurrences; otherwise the pair is skipped for that region class and reported in the projection's `skipped` list.

### 4. Projector (`ImpactProjector`, `impact-projection` 1.0.0)

- Runs when a hazard record version arrives (ChangeBus subscriber, durable cursor `impact.cursor` in the consumer clamp), debounced 10 min per hazard.
- For each exposed asset or area and each applicable impact pair: `P(impact) = P(hazard at asset) × P(impact | hazard class)`, with a propagated interval (product of interval bounds) and the inputs listed.
- Group by impact family and admin1; keep the top 20 projections by probability per hazard; drop those below `impact.minProbability` (0.05).
- **Findings:** one `projected-impact` finding per (hazard, impact family, admin1) with `stats {pHazard, pConditional, pImpact, interval, n}`, the exposed assets and dependency paths, and the labels. Superseded by the next run for the same hazard.
- **Ledger:** for projections with `pImpact ≥ impact.questionMinProbability` (0.10), open a binary question via `ForecastService.openQuestion`: "Will {impact family} be reported in {admin1} between {validFrom} and {validTo + lag}?" with an `event-exists` resolver over the impact family's sources (068, 092 for outages; 087 for airport ground stops; 079 for flood categories), category `weather-impact`, dedup per hazard and admin1, forecaster `impact` with `pImpact`. The 121 baselines add their forecasts automatically.

### 5. API, MCP, SSE, docs

- `GET /v1/analytics/impacts?hazardType&region&since&minProbability&cursor` (read), `GET /v1/analytics/impacts/{findingId}` (read; inputs, paths, labels), `GET /v1/analytics/impact-rates?hazardClass&impactFamily&regionClass` (read).
- MCP: `list_projected_impacts`, `get_impact_rate` (read; read catalogue).
- SSE topic `analytics`: `impact.projected|superseded`.
- `docs/OpenAPI.json`: `HazardForecast`, `ProjectedImpact`, `ImpactRate`; tag `Analytics`; Postman entries.
- AccessPolicy rows. Problem code: `UNKNOWN_IMPACT` (404).
- `docs/Operator-Guide.md` "Impact projection": what the numbers mean, which products carry real probabilities, why cones are not probabilities, how projections are scored, and the labels.
- `docs/Source-Catalog.md`: `nhc-wind-probs` row (if added) and the impact pairs table.

## Tests to write (do not run)

- `HazardAdapterTest` per hazard type: SPC point-probability mapping, WPC category ranges, cone yields no probability, NWPS persistence fallback, NHC probability bands.
- `ImpactRateServiceTest`: co-occurrence rate with interval on seeded history; skipped when n < 20.
- `ImpactProjectorIT`: a seeded hurricane wind-probability record over a seeded admin1 with facilities produces findings with paths and labels, opens ledger questions above the threshold with the `impact` forecaster, supersedes on the next advisory, and resolves from a seeded outage record.
- `ImpactControllersIT`: endpoints, scopes, MCP shapes.

## Acceptance criteria

- Hazard forecasts with published probabilities produce `projected-impact` findings that combine them with historical conditional rates, exposed assets and dependency paths.
- Projections above the threshold become ledger questions that resolve from Foresignal's own sources and are scored.
- Cones and categorical labels are never used as probabilities on their own.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 126, 129, 130, 131).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 131).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and `docs/Intelligence-Design.md`, and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `124: Impact Projection`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/124-CR-Impact-Projection.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `124: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
