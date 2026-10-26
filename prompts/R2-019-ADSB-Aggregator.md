# R2-019 — First-party aggregator surface + local ADS-B aggregator

**Context.** v2.0 (R2) backend, Part C (network sourcing). Permission `network.manage`/`collection.manage`
(admin). This prompt builds **the shared aggregator surface** that R2-020…R2-023 extend, and implements its
first domain, **adsb**. Depends on R2-018 only for the `docs/Network-Sourcing.md` doc it adds to.

**Goal.** Where a feed is self-mirrorable, host it locally and read from `localhost` — that removes the external
rate limit *and* the "one system pulling" exposure at the source, strictly better than masking it with egress
rotation. adsb.lol / adsb.fi / airplanes.live are public mirrors of the same readsb/tar1090 data ForeSignal
already parses, and the adsb adapters already take a base-URL override — so stand up our **own readsb** and
point the adapters at `http://localhost`. True 1-second refresh, global coverage, no public endpoint hit.

## Operations (authored, status `planned`, `x-foresignal-prompt: R2-019`) — the shared surface

| operationId | method + path | scope |
|---|---|---|
| `listAggregators` | `GET /v1/aggregators` | admin · `network.manage` |
| `getAggregator` | `GET /v1/aggregators/{domain}` | admin · `network.manage` |
| `updateAggregator` | `PUT /v1/aggregators/{domain}` | admin · `network.manage` |
| `testAggregator` | `POST /v1/aggregators/{domain}/test` | admin · `network.manage` |

`{domain}` ∈ `adsb`, `ais`, `datalink`, `satellite`, `ground-sensors`. Schemas authored: `Aggregator`,
`AggregatorConfig`, `AggregatorList`, `AggregatorTestResult`. **This prompt implements the surface for every
domain and the `adsb` domain's behavior.** R2-020 (`ais`), R2-021 (`datalink`), R2-022 (`satellite`),
R2-023 (`ground-sensors`) each plug their domain's backend into this surface — they add **no new operations**.

## Read first

- `source/formats/readsb/ReadsbAircraftMapper.java`, `source/formats/readsb/RegionPoller.java`,
  `source/formats/readsb/MapperOptions.java` (the native readsb/tar1090 parse; a local readsb serves the full
  `aircraft.json` at once, so the world-tiling poller is not needed against a local host).
- `source/adapters/adsblol/AdsbLol.java` (`BASE_URL_PROPERTY`, `baseUrl(override)`),
  `source/adapters/adsblol/AdsbLolWorldAdapter.java` + `AdsbLolMilAdapter.java` + `AdsbLolAreaAdapter.java`,
  and the `adsbfi` / `airplaneslive` adapters (the public mirrors to default off).
- `source/adapters/derived/GpsJammingGenerator.java` (GPS-jamming derived from ADS-B) and
  `source/adapters/internet/GpsjamAdapter.java` (the public `gpsjam` mirror to default off).
- `source/SourceService.java` (`seedDefaults`, `defaultRow`, `enableCore`, `runsWithoutOperatorInput`,
  `isAutoEnabled`), `source/SourceDescriptor.java` (`autoEnable()`), `source/SourceRegistry.java` (`CORE_IDS`) —
  how a source is marked default-on/off.
- `settings/AppSettingService.java` (settings key per aggregator), R2-014 `OperationalAlertService`,
  `docker-compose.yml` (where a `readsb` compose profile is added).

## Build

1. **Aggregator surface** (`ai.foresignal.source.aggregator`, a new package). An `Aggregator` config per domain
   stored under an `app_setting` key (`aggregator.<domain>`): `enabled` (default **false**), `mode`
   (`receiver`/`feed`/`hybrid`), `endpoint` (the local URL the adapter reads, e.g. `http://localhost` for
   readsb), domain `config` (free-form, domain-specific), and `disableSuperseded` (default true). A
   `DomainAggregator` SPI (`domain()`, `describe() -> Aggregator`, `apply(AggregatorConfig)`,
   `test() -> AggregatorTestResult`, `supersededSourceIds()`) that each domain implements; the controller
   dispatches by `{domain}`. `listAggregators` returns every registered domain's `Aggregator`; `getAggregator`
   one; `updateAggregator` applies config; `testAggregator` probes the local endpoint.

2. **adsb domain** (`AdsbAggregator implements DomainAggregator`). When enabled with a local endpoint, point the
   adsb adapters at it via `AdsbLol.BASE_URL_PROPERTY` (a runtime override, not a system property edit) so the
   world/mil/area adapters read the full global `aircraft.json` from `localhost` through `ReadsbAircraftMapper`
   — a **full-global local read**, no world-tiling against a public host. `mode` selects: `receiver` (a local
   SDR-fed readsb), `feed` (aggregating the global feed into a local readsb), or `hybrid`. `test()` reads a
   sample from the endpoint and reports reachability + `sampleCount` (`AggregatorTestResult`).

3. **Supersede, don't remove.** `supersededSourceIds()` for adsb = the public mirror sources
   (adsb.lol / adsb.fi / airplanes.live adapters) **and** `gpsjam` (GPS-jamming is now first-party via
   `GpsJammingGenerator` off the local feed). When the adsb aggregator is live and `disableSuperseded` is true,
   flip those source rows disabled through `SourceService` (and mark their descriptors `autoEnable=false` so a
   fresh install seeds them off). They stay in the catalog as a zero-setup, opt-in fallback — nothing is removed.

4. **Stand up readsb.** Add a documented local readsb setup — a `readsb` **compose profile** in
   `docker-compose.yml` (opt-in profile, not started by default) and setup docs covering the two modes (own SDR
   receiver vs. aggregating the global feed). Do not bundle or copy GPL firmware; reference the upstream and
   document the run. The adapter only ever reads `http://localhost`.

5. **Status + health.** `Aggregator.status` (`ok`/`degraded`/`down`/`unconfigured`) and `lastSeenAt` from the
   adapter's health; raise `system.aggregator_down` via R2-014 when a configured aggregator goes down.

**Migration.** Aggregator config lives in `app_setting` (key per domain) — **no new table** unless you persist
per-domain health history; if so, use the next free `V{n}` (R2-001…R2-013 claim V14 upward — read the directory).

## Contract

The four surface ops are authored `planned`; implement them to `Aggregator`/`AggregatorConfig`/`AggregatorList`/
`AggregatorTestResult`. Extend a schema / add an error code if needed, regenerate Postman
(`python3 scripts/generate-postman.py`), but **leave `x-foresignal-status: planned` and do not regenerate the
SDKs** (R2-027 flips + regenerates). `docs/OpenAPI.json` is the only contract.

## Docs

Extend `docs/Network-Sourcing.md` (created in R2-018) with the first-party-aggregator principle and the ADS-B
section (local readsb modes, the base-URL override, the full-global local read, superseded mirrors default-off).
Update `docs/Source-Catalog.md` / `docs/Source-Onboarding.md` where the adsb mirrors' default-enable changes.

## Tests to write (do not run)

- `AggregatorSurfaceIT` — list/get/update/test dispatch by `{domain}`; an unknown domain → `404`; config round-trips.
- `AdsbAggregatorTest` — enabling it points the adsb adapters' base URL at the local endpoint; `test()` reports
  a sample count; disabling restores the previous behavior.
- `SupersededSourcesIT` — with the adsb aggregator live and `disableSuperseded`, adsb.lol/fi/airplanes.live and
  `gpsjam` seed/flip disabled but remain in the catalog.

## What runs

Compile only — `./gradlew compileJava compileTestJava`. No test suite, no `lintOpenApi`/`checkApiCompat`.
**Do** run `./gradlew checkUsEnglish`. Write the tests but do not run them. US English.

## Done when

`GET/PUT /v1/aggregators` and `GET/PUT/POST /v1/aggregators/{domain}`(`/test`) work for all five domains with
the adsb domain fully implemented; enabling the local ADS-B aggregator reads the full global feed from
`localhost` through `ReadsbAircraftMapper` and defaults the public mirrors + `gpsjam` off (kept as fallback);
a `readsb` compose profile + setup docs exist; compile passes; `checkUsEnglish` green.

## Do not touch

Any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the `x-foresignal` changelog;
`.gitlab-ci.yml`; other prompts' files. Do not copy GPL firmware (readsb/SatDump are reference only). **Stage
only this prompt's files.**

## Commit

`R2-019: Aggregator surface + local ADS-B aggregator`, then
`completion-reports/R2-019-CR-ADSB-Aggregator.md` with the 40-character implementation hash.
